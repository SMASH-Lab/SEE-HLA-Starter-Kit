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

import hla.rti1516_2025.*;
import hla.rti1516_2025.exceptions.FederateInternalError;
import hla.rti1516_2025.time.HLAinteger64Time;
import hla.rti1516_2025.time.LogicalTime;
import org.see.skf.internal.FederateMapping;
import org.see.skf.internal.InternalObjectBuilderException;
import org.see.skf.internal.SyncPointManager;
import org.see.skf.internal.runtime.HLAInteractionManager;
import org.see.skf.internal.callbacks.FederateCallbackManager;
import org.see.skf.internal.runtime.HLAObjectManager;

import java.util.concurrent.ExecutorService;

/**
 * The federate ambassador internally used by {@link SKAbstractFederate} to handle RTI callbacks.
 *
 * @since 2.0
 */
final class SKFederateAmbassador extends NullFederateAmbassador {

    private final FederateMapping federateMapping;
    private final FederateCallbackManager callbackManager;
    private final HLAObjectManager objectManager;
    private final HLAInteractionManager interactionManager;
    private final SyncPointManager syncPointManager;
    private final ExecutorService executor;

    private SKFederateAmbassador(Builder builder) {
        this.executor = builder.executor;
        this.callbackManager = builder.callbackManager;
        this.objectManager = builder.objectManager;
        this.interactionManager = builder.interactionManager;
        this.syncPointManager = builder.syncPointManager;
        this.federateMapping = builder.federateMapping;
    }

    // N.B. This operation is NOT safe to launch in ExecutorService because of a contentious race condition where the RTI service thread will
    // relay updates via the reflectAttributeValues callback before this thread is done creating the internal object instance representations for the federate.
    @Override
    public void discoverObjectInstance(ObjectInstanceHandle objectInstance, ObjectClassHandle objectClass, String objectInstanceName, FederateHandle producingFederate) throws FederateInternalError {
        String producingFederateName = this.federateMapping.get(producingFederate);
        this.objectManager.remoteObjectInstanceDiscovered(objectInstance, objectInstanceName, objectClass, producingFederateName);
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstance, byte[] userSuppliedTag, FederateHandle producingFederate) throws FederateInternalError {
        this.executor.submit(() -> remoteObjectInstanceDestroyed(objectInstance, producingFederate));
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstance, byte[] userSuppliedTag, FederateHandle producingFederate, LogicalTime<?, ?> time, OrderType sentOrderType, OrderType receivedOrderType, MessageRetractionHandle optionalRetraction) throws FederateInternalError {
        this.executor.submit(() -> remoteObjectInstanceDestroyed(objectInstance, producingFederate));
    }

    private void remoteObjectInstanceDestroyed(ObjectInstanceHandle objectInstance, FederateHandle producingFederate) {
        String producingFederateName = this.federateMapping.get(producingFederate);
        this.objectManager.remoteObjectInstanceDestroyed(objectInstance, producingFederateName);
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstance, AttributeHandleValueMap attributeValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions) throws FederateInternalError {
        this.executor.submit(() -> reflectAttributeValueCallback(objectInstance, attributeValues));
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstance, AttributeHandleValueMap attributeValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions, LogicalTime<?, ?> time, OrderType sentOrderType, OrderType receivedOrderType, MessageRetractionHandle optionalRetraction) throws FederateInternalError {
        this.executor.submit(() -> reflectAttributeValueCallback(objectInstance, attributeValues));
    }

    private void reflectAttributeValueCallback(ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributeValues) {
        this.executor.submit(() -> this.objectManager.remoteObjectInstanceUpdated(instanceHandle, attributeValues));
    }

    @Override
    public void provideAttributeValueUpdate(ObjectInstanceHandle objectInstance, AttributeHandleSet attributes, byte[] userSuppliedTag) throws FederateInternalError {
        this.executor.submit(() -> this.objectManager.provideObjectInstanceUpdate(objectInstance, attributes));
    }

    @Override
    public void objectInstanceNameReservationSucceeded(String objectInstanceName) throws FederateInternalError {
        this.executor.submit(() -> this.callbackManager.completeNameReservationCallback(objectInstanceName, true));
    }

    @Override
    public void objectInstanceNameReservationFailed(String objectInstanceName) throws FederateInternalError {
        this.executor.submit(() -> this.callbackManager.completeNameReservationCallback(objectInstanceName, false));
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap parameterValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions) throws FederateInternalError {
        interactionReceived(interactionClass, parameterValues, producingFederate);
    }

    private void interactionReceived(InteractionClassHandle interactionClass, ParameterHandleValueMap parameterValues, FederateHandle producingFederate) throws FederateInternalError {
        this.executor.submit(() -> {
            String producingFederateName = this.federateMapping.get(producingFederate);
            this.interactionManager.interactionReceived(interactionClass, parameterValues, producingFederateName);
        });
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap parameterValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions, LogicalTime<?, ?> time, OrderType sentOrderType, OrderType receivedOrderType, MessageRetractionHandle optionalRetraction) throws FederateInternalError {
        interactionReceived(interactionClass, parameterValues, producingFederate);
    }

    @Override
    public void timeConstrainedEnabled(LogicalTime<?, ?> time) throws FederateInternalError {
        this.executor.submit(() -> this.callbackManager.completeTimeConstrainedCallback((HLAinteger64Time) time));
    }

    @Override
    public void timeRegulationEnabled(LogicalTime<?, ?> time) throws FederateInternalError {
        this.executor.submit(() -> this.callbackManager.completeTimeRegulationCallback((HLAinteger64Time) time));
    }

    @Override
    public void timeAdvanceGrant(LogicalTime<?, ?> time) throws FederateInternalError {
        this.executor.submit(() -> this.callbackManager.completeTimeAdvanceGrantCallback((HLAinteger64Time) time));
    }

    @Override
    public void announceSynchronizationPoint(String synchronizationPointLabel, byte[] userSuppliedTag) throws FederateInternalError {
        this.executor.submit(() -> this.syncPointManager.syncPointAnnounced(synchronizationPointLabel));
    }

    @Override
    public void federationSynchronized(String synchronizationPointLabel, FederateHandleSet failedToSyncSet) throws FederateInternalError {
        this.executor.submit(() -> this.syncPointManager.federationSynchronized(synchronizationPointLabel));
    }

    @Override
    public void attributeIsNotOwned(ObjectInstanceHandle objectInstance, AttributeHandleSet attributes) throws FederateInternalError {
        this.executor.submit(() -> this.callbackManager.completeAttributeOwnershipQuery(objectInstance, attributes, null));
    }

    @Override
    public void attributeIsOwnedByRTI(ObjectInstanceHandle objectInstance, AttributeHandleSet attributes) throws FederateInternalError {
        this.executor.submit(() -> this.callbackManager.completeAttributeOwnershipQuery(objectInstance, attributes, "RTI"));
    }

    @Override
    public void informAttributeOwnership(ObjectInstanceHandle objectInstance, AttributeHandleSet attributes, FederateHandle owner) throws FederateInternalError {
        this.executor.submit(() -> {
            String ownerName = this.federateMapping.get(owner);
            this.callbackManager.completeAttributeOwnershipQuery(objectInstance, attributes, ownerName);
        });
    }

    @Override
    public void requestAttributeOwnershipRelease(ObjectInstanceHandle objectInstance, AttributeHandleSet candidateAttributes, byte[] userSuppliedTag) throws FederateInternalError {
        this.executor.submit(() -> this.objectManager.notifyAttributeOwnershipReleaseRequested(objectInstance, candidateAttributes));
    }

    @Override
    public void attributeOwnershipAcquisitionNotification(ObjectInstanceHandle objectInstance, AttributeHandleSet securedAttributes, byte[] userSuppliedTag) throws FederateInternalError {
        this.executor.submit(() -> this.objectManager.notifyAttributeOwnershipAcquired(objectInstance, securedAttributes, true));
    }

    @Override
    public void attributeOwnershipUnavailable(ObjectInstanceHandle objectInstance, AttributeHandleSet attributes, byte[] userSuppliedTag) throws FederateInternalError {
        this.executor.submit(() -> this.objectManager.notifyAttributeOwnershipAcquired(objectInstance, attributes, false));
    }

    static final class Builder {

        private ExecutorService executor;

        private FederateCallbackManager callbackManager;

        private HLAObjectManager objectManager;

        private HLAInteractionManager interactionManager;

        private SyncPointManager syncPointManager;

        private FederateMapping federateMapping;

        Builder executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        Builder callbackManager(FederateCallbackManager callbackManager) {
            this.callbackManager = callbackManager;
            return this;
        }

        Builder objectManager(HLAObjectManager objectManager) {
            this.objectManager = objectManager;
            return this;
        }

        Builder interactionManager(HLAInteractionManager interactionManager) {
            this.interactionManager = interactionManager;
            return this;
        }

        Builder syncPointManager(SyncPointManager syncPointManager) {
            this.syncPointManager = syncPointManager;
            return this;
        }

        Builder federateMapping(FederateMapping federateMapping) {
            this.federateMapping = federateMapping;
            return this;
        }

        SKFederateAmbassador build() {
            if (this.callbackManager == null || this.objectManager == null || this.interactionManager == null || this.federateMapping == null || this.executor == null || this.syncPointManager == null) {
                throw new InternalObjectBuilderException("Missing one or more arguments required to initialize internal federate ambassador object.");
            }

            return new SKFederateAmbassador(this);
        }
    }
}
