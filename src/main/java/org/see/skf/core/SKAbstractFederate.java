/*****************************************************************
 SEE HLA Starter Kit Framework -  A Java framework for developing
 SRFOM-compliant HLA Federates in the Simulation Exploration
 Experience (SEE) program.

 Copyright (c) 2014, 2026 SMASH Lab - University of Calabria
 (Italy), Hridyanshu Aatreya - Modelling & Simulation Group (MSG)
 at Brunel University of London (UK). All rights reserved.

 GNU Lesser General Public License (GNU LGPL).

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3.0 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library.
 If not, see http://http://www.gnu.org/licenses/
 *****************************************************************/

package org.see.skf.core;

import hla.rti1516_2025.CallbackModel;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.ResignAction;
import hla.rti1516_2025.RtiConfiguration;
import hla.rti1516_2025.exceptions.*;

import org.see.skf.internal.*;
import org.see.skf.internal.executive.ExecutiveStateManager;
import org.see.skf.internal.runtime.*;
import org.see.skf.internal.callbacks.FederateCallbackManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Map;
import java.util.concurrent.*;

/**
 * <p>
 *     This class provides a skeletal implementation of the <code>SKFederate</code> interface, to minimize the effort
 *     required to implement this interface.
 * </p>
 *
 * <p>
 *     To implement an SKF federate, the programmer needs to extend this class and call the methods corresponding to the
 *     execution steps outlined in the SRFOM standard such as <code>connectToRti</code> and <code>joinFederationExecution</code>.
 *     The <code>setupTimeManagement</code> should be called appropriately: it performs time management operations contingent
 *     upon whether the federate was configured to behave as an early or late joiner. Finally, the <code>exec</code>
 *     method should only be called once the federate is ready to begin execution.
 * </p>
 *
 * <p><i>N.B. Early joiner initialization support is yet to be implemented. Only late joiner support is complete at this time.</i></p>
 *
 * @since 2.0
 * @see SKFederate
 * @see SEEAbstractFederate
 */
public abstract class SKAbstractFederate implements SKFederate {

    private static final Logger logger = LoggerFactory.getLogger(SKAbstractFederate.class);

    private final RtiConfiguration rtiConfiguration;
    private final RTIambassador rtiAmbassador;

    private String federateName;
    private final String federateType;
    private final String federationName;
    private final String[] additionalFomModules;
    private final SKFederate.Role federateRole;
    private final SKFederateAmbassador federateAmbassador;

    private final ExecutorService executor;
    private final ExecutiveStateManager executiveStateManager;
    private final HLAObjectManager objectManager;
    private final HLAInteractionManager interactionManager;
    private final SyncPointManager syncPointManager;
    private final TimeManager timeManager;

    private ExecutionConfiguration exCO;
    private final CountDownLatch exCODiscoveryLatch;

    protected SKAbstractFederate(File configurationFile) {
        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

        SKFederateConfiguration config = new FederatePropertyConfiguration(configurationFile);
        this.federateName = config.federateName();
        this.federateType = config.federateType();
        this.federationName = config.federationName();
        this.federateRole = config.federateRole();
        this.additionalFomModules = config.additionalFomModules();
        this.rtiConfiguration = RtiConfiguration.createConfiguration().withRtiAddress(config.rtiAddress());

        this.executor = Executors.newFixedThreadPool(config.maxThreads());
        FederateCallbackManager callbackManager = new FederateCallbackManager(this.executor);
        FederateMapping federateMapping = new FederateMapping();

        CoderManager coderManager = new CoderManager();
        SKAnnotatedTypeParser parser = new SKAnnotatedTypeParser(coderManager);

        this.objectManager = new HLAObjectManager(callbackManager, this.executor, parser);
        this.interactionManager = new HLAInteractionManager(parser);

        this.syncPointManager = new SyncPointManager();

        this.federateAmbassador = new SKFederateAmbassador.Builder()
                .executor(this.executor)
                .callbackManager(callbackManager)
                .objectManager(this.objectManager)
                .interactionManager(this.interactionManager)
                .syncPointManager(this.syncPointManager)
                .federateMapping(federateMapping)
                .build();

        this.timeManager = new TimeManager(config.lookahead(), callbackManager);
        this.executiveStateManager = new ExecutiveStateManager(this, this.timeManager);

        this.exCODiscoveryLatch = new CountDownLatch(1);
        setupExCOListeners();
    }

    private void setupExCOListeners() {
        addObjectInstanceListener("ExCO", new ObjectInstanceListener() {
            @Override
            public void discovered(String producingFederateName) {
                // Ignore.
            }

            @Override
            public void initialized(Object instance) {
                exCO = (ExecutionConfiguration) instance;
                exCODiscoveryLatch.countDown();
            }

            @Override
            public void destroyed(String producingFederateName) {
                shutdownExecution();
            }
        });
    }

    @Override
    public final void connectToRti() throws Unauthorized, ConnectionFailed {
        String rtiAddress = this.rtiConfiguration.rtiAddress();

        try {
            rtiAmbassador.connect(this.federateAmbassador, CallbackModel.HLA_IMMEDIATE, this.rtiConfiguration);
            logger.info("Established connection to RTI hosted at <{}>.", rtiAddress);
        } catch (AlreadyConnected ignore) {
            logger.warn("<{}> is already connected to the RTI.", this.federateName);
        } catch (UnsupportedCallbackModel | CallNotAllowedFromWithinCallback |
                 RTIinternalError e) {
            throw new FederateStartupException("Failed to establish connection to the RTI hosted at <" + rtiAddress + ">.", e);
        }
    }

    @Override
    public final void joinFederationExecution() throws RestoreInProgress, Unauthorized, NotConnected, RTIinternalError, SaveInProgress, FederateNotExecutionMember {
        String originalFederateName = this.federateName;
        String suffix = "";
        boolean joined = false;
        int attempts = 1;

        while (!joined) {
            try {
                attemptJoin();

                if (attempts > 1) {
                    logger.warn("The name <{}> was already taken by another federate. Assuming the name <{}> instead.", originalFederateName, this.federateName);
                }

                logger.info("Joined the federation execution <{}>.", federationName);
                joined = true;
            } catch (FederateNameAlreadyInUse e) {
                // Attempt to join again with an incremented suffix at the end of the federate name.
                federateName = originalFederateName + suffix + "_" + attempts++;
            } catch(FederateAlreadyExecutionMember ignore) {
                logger.warn("<{}> is already a member of the federation execution <{}>.", this.federateName, this.federationName);
            } catch (FederationExecutionDoesNotExist e) {
                throw new FederateStartupException("The federation execution <" + this.federationName + "> does not exist.", e);
            } catch (InvalidFOM | ErrorReadingFOM | CouldNotOpenFOM | InconsistentFOM e) {
                throw new FederateStartupException("The federation execution <" + this.federationName + "> could not be joined due to problems with parsing the supplied FOM modules.", e);
            } catch (CouldNotCreateLogicalTimeFactory e) {
                // The lack of a time implementation is catastrophic as none of the succeeding time management procedures will work. This is unfortunately an irrecoverable situation.
                throw new FederateStartupException(e);
            } catch (CallNotAllowedFromWithinCallback ignore) {
                // Highly unlikely to occur since the framework shields RTI callbacks from users. They would never have the privilege
                // to throw this exception in the first place.
            }
        }

        // Now that the federate is part of the federation execution, we can initialize logical time related data.
        this.timeManager.initializeLogicalTimeComponents();
    }

    private void attemptJoin() throws CouldNotOpenFOM, NotConnected, InvalidFOM, RTIinternalError, ErrorReadingFOM, CouldNotCreateLogicalTimeFactory, FederateNameAlreadyInUse, RestoreInProgress, CallNotAllowedFromWithinCallback, InconsistentFOM, FederationExecutionDoesNotExist, Unauthorized, FederateAlreadyExecutionMember, SaveInProgress {
        if (this.additionalFomModules.length > 0) {
            rtiAmbassador.joinFederationExecution(this.federateName, this.federateType, this.federationName, this.additionalFomModules);
        } else {
            rtiAmbassador.joinFederationExecution(this.federateName, this.federateType, this.federationName);
        }
    }

    @Override
    public final void shutdownExecution() {
        this.executiveStateManager.changeExecutionMode(ExecutionMode.EXEC_MODE_SHUTDOWN);

        FutureTask<Void> task = new FutureTask<>(() -> {
            synchronized (this.executiveStateManager) {
                while (this.executiveStateManager.getLocalExecutionMode() != ExecutionMode.EXEC_MODE_SHUTDOWN) {
                    try {
                        this.executiveStateManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new FederateShutdownException("Graceful termination of federate failed.", e);
                    }
                }
            }

            try {
                rtiAmbassador.disableTimeRegulation();
                rtiAmbassador.disableTimeConstrained();
                rtiAmbassador.resignFederationExecution(ResignAction.DELETE_OBJECTS_THEN_DIVEST);
            } catch (OwnershipAcquisitionPending | FederateOwnsAttributes | InvalidResignAction e) {
                throw new FederateShutdownException("Federate shutdown attempt was interrupted by ongoing processes that are yet to be completed.", e);
            } catch (CallNotAllowedFromWithinCallback | TimeConstrainedIsNotEnabled | TimeRegulationIsNotEnabled ignore) {
                // The chances of this being thrown is less because we enable time regulation/constraint during the time management setup phase.
                // However, if this method is prematurely called then this catch block will be triggered, but it's nothing serious, so it can be safely ignored.
                // Also like in joinFederationExecution(), the CallNotAllowedFromWithinCallback exception is not a problem because the framework hides callbacks from the user.
            }

            this.executor.shutdown();
            logger.info("Federate terminated.");

            return null;
        });

        this.executor.submit(task);
    }

    @Override
    public final void publishObjectClass(Class<?> clazz, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress, AttributeNotDefined, ObjectClassNotDefined {
        this.objectManager.publishObjectClass(clazz, attributeNames);
    }

    public final void unpublishObjectClass(String className, String... attributes) throws FederateNotExecutionMember, RestoreInProgress, OwnershipAcquisitionPending, NotConnected, RTIinternalError, SaveInProgress, ObjectClassNotDefined, AttributeNotDefined {
        this.objectManager.unpublishObjectClass(className, attributes);
    }

    @Override
    public final void subscribeObjectClass(Class<?> clazz, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress, AttributeNotDefined, ObjectClassNotDefined {
        this.objectManager.subscribeObjectClass(clazz, attributeNames);
    }

    public final void unsubscribeObjectClass(String className, String... attributes) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, AttributeNotDefined, ObjectClassNotDefined {
        this.objectManager.unsubscribeObjectClass(className, attributes);
    }

    @Override
    public final String createObjectInstance(Object objectInstance) throws FederateNotExecutionMember, ObjectClassNotPublished, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        return this.objectManager.createObjectInstance(objectInstance);
    }

    @Override
    public final Future<Void> createObjectInstance(Object objectInstance, String name) {
        return this.objectManager.createObjectInstance(objectInstance, name);
    }

    @Override
    public final void updateObjectInstance(Object objectInstance, String... attributes) throws FederateNotExecutionMember, RestoreInProgress, AttributeNotOwned, NotConnected, RTIinternalError, SaveInProgress, AttributeNotDefined, ObjectInstanceNotKnown {
        this.objectManager.updateObjectInstance(objectInstance, attributes);
    }

    @Override
    public final void destroyObjectInstance(Object objectInstance) throws FederateNotExecutionMember, RestoreInProgress, DeletePrivilegeNotHeld, NotConnected, RTIinternalError, SaveInProgress {
        this.objectManager.destroyObjectInstance(objectInstance);
    }

    @Override
    public final boolean isObjectInstanceDiscovered(String name) {
        return this.objectManager.isRemoteObjectInstanceDiscovered(name);
    }

    @Override
    public final Object queryObjectInstance(String name) {
        return this.objectManager.queryObjectInstance(name);
    }

    @Override
    public final void requestObjectInstanceUpdates(String name, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.objectManager.requestRemoteObjectInstanceUpdates(name, attributeNames);
    }

    @Override
    public final void addObjectInstanceListener(String objectInstanceName, ObjectInstanceListener listener) {
        this.objectManager.addObjectInstanceListener(objectInstanceName, listener);
    }

    @Override
    public final void removeObjectInstanceListener(ObjectInstanceListener listener) {
        this.objectManager.removeObjectInstanceListener(listener);
    }

    @Override
    public final void addPropertyChangeListener(Object objectInstance, String propertyName, PropertyChangeListener listener) {
        this.objectManager.addPropertyChangeListener(objectInstance, propertyName, listener);
    }

    @Override
    public final void addPropertyChangeListener(Object objectInstance, PropertyChangeListener listener) {
        this.objectManager.addPropertyChangeListener(objectInstance, listener);
    }

    @Override
    public final void removePropertyChangeListener(Object objectInstance, String propertyName, PropertyChangeListener listener) {
        this.objectManager.removePropertyChangeListener(objectInstance, propertyName, listener);
    }

    @Override
    public final void removePropertyChangeListener(Object objectInstance, PropertyChangeListener listener) {
        this.objectManager.removePropertyChangeListener(objectInstance, listener);
    }

    @Override
    public final void publishInteractionClass(Class<?> clazz) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.interactionManager.publishInteractionClass(clazz);
    }

    @Override
    public final void unpublishInteractionClass(String name) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.interactionManager.unpublishInteractionClass(name);
    }

    @Override
    public final void subscribeInteractionClass(Class<?> clazz) throws FederateNotExecutionMember, RestoreInProgress, FederateServiceInvocationsAreBeingReportedViaMOM, NotConnected, RTIinternalError, SaveInProgress {
        this.interactionManager.subscribeInteractionClass(clazz);
    }

    @Override
    public final void unsubscribeInteractionClass(String name) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.interactionManager.unsubscribeInteractionClass(name);
    }

    @Override
    public final void sendInteraction(Object interaction) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined {
        this.interactionManager.sendInteraction(interaction);
    }

    @Override
    public final void addInteractionListener(InteractionListener listener) {
        this.interactionManager.addInteractionListener(listener);
    }

    @Override
    public final void removeInteractionListener(InteractionListener listener) {
        this.interactionManager.removeInteractionListener(listener);
    }

    @Override
    public final void achieveSyncPoint(String label) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.syncPointManager.achieveSyncPoint(label);
    }

    @Override
    public final void achieveSyncPoint(String label, boolean success) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.syncPointManager.achieveSyncPoint(label, success);
    }

    @Override
    public final void addSyncPointListener(String label, SyncPointListener listener) {
        this.syncPointManager.addSyncPointListener(label, listener);
    }

    @Override
    public final void removeSyncPointListener(SyncPointListener listener) {
        this.syncPointManager.removeSyncPointListener(listener);
    }

    @Override
    public final Future<Map<String, String>> queryAttributeOwnership(String objectInstanceName, String... attributeNames) {
        return this.objectManager.queryAttributeOwnership(objectInstanceName, attributeNames);
    }

    @Override
    public final void acquireAttributeOwnership(String objectInstanceName, String... attributeNames) throws FederateNotExecutionMember, ObjectClassNotPublished, AttributeNotDefined, RestoreInProgress, FederateOwnsAttributes, ObjectInstanceNotKnown, NotConnected, RTIinternalError, AttributeNotPublished, SaveInProgress {
        this.objectManager.acquireAttributeOwnership(objectInstanceName, attributeNames);
    }

    @Override
    public final void divestAttributeOwnershipIfWanted(Object objectInstance, String... attributeNames) throws FederateNotExecutionMember, AttributeNotDefined, RestoreInProgress, AttributeNotOwned, ObjectInstanceNotKnown, NotConnected, RTIinternalError, SaveInProgress {
        this.objectManager.divestAttributeOwnershipIfWanted(objectInstance, attributeNames);
    }

    @Override
    public final void addAttributeOwnershipListener(Object objectInstance, AttributeOwnershipListener listener) {
        this.objectManager.addAttributeOwnershipListener(objectInstance, listener);
    }

    @Override
    public final void removeAttributeOwnershipListener(AttributeOwnershipListener listener) {
        this.objectManager.removeAttributeOwnershipListener(listener);
    }

    @Override
    public final String getName() {
        return this.federateName;
    }

    @Override
    public final synchronized double getSimulationTime() {
        return this.timeManager.getSimulationScenarioTime();
    }

    @Override
    public final boolean isAdvancing() {
        return this.timeManager.isTimeAdvancing();
    }

    protected final void setupTimeManagement() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        try {
            logger.info("Awaiting discovery of ExCO object instance.");
            this.exCODiscoveryLatch.await();

            addPropertyChangeListener(this.exCO, new ExCOUpdateListener(this.executiveStateManager));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExCONotInitializedException("Cannot proceed with initialization because the ExCO object instance could not be acquired.");
        }

        double federationScenarioTimeEpoch = this.exCO.getScenarioTimeEpoch();
        this.timeManager.setFederationScenarioTimeEpoch(federationScenarioTimeEpoch);

        if (this.federateRole == Role.LATE) {
            lateJoinerTimeSetup();
        } else {
            earlyJoinerTimeSetup();
        }
    }

    private void lateJoinerTimeSetup() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.timeManager.constrainTime();
        this.timeManager.regulateTime();
        this.timeManager.advanceToLogicalTimeBoundary(this.exCO.getLeastCommonTimeStep());
    }

    private void earlyJoinerTimeSetup() {
        // TODO - Early joiner initialization sequence to be added at a later date.
    }

    /**
     * Begins executing the federate. This is a blocking action that will only unblock once the federate is slated
     * for termination.
     */
    protected final void exec() throws RTIexception {
        logger.info("Beginning federate execution.");
        this.executiveStateManager.run();
    }

    /**
     * All jobs that need to be run during the SpaceFOM run executive mode should be carried out in this method.
     */
    public abstract void processRunJobs() throws RTIexception;

    /**
     * Any final jobs that must execute prior to termination should be carried out in this method. Since the federate
     * cannot time advance any further at the point this method is called, it is ill-advised to make any RTI-related
     * calls here.
     */
    public abstract void processShutdownJobs();
}
