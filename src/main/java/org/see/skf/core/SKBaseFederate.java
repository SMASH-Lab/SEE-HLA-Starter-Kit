/*****************************************************************
 SEE HLA Starter Kit Framework -  A Java library that supports
 the development of HLA Federates in the Simulation Exploration
 Experience (SEE) program.

 Copyright (c) 2014, 2026 SMASH Lab - University of Calabria
 (Italy), Hridyanshu Aatreya - Modelling & Simulation Group (MSG)
 at Brunel University of London. All rights reserved.

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
import hla.rti1516_2025.exceptions.*;
import org.see.skf.annotations.InteractionClass;
import org.see.skf.annotations.ObjectClass;
import org.see.skf.conf.FederateConfiguration;
import org.see.skf.exceptions.UpdateException;
import org.see.skf.runtime.interactions.InteractionClassModel;
import org.see.skf.runtime.objects.ObjectClassEntity;
import org.see.skf.runtime.objects.ObjectClassModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

/**
 * A rudimentary implementation of the SKFederateInterface that comes with object and interaction management functionality.
 * It is a good starting point for building SpaceFOM federates that require specialized behavior not provided by the
 * SEEAbstractFederate and its descendants.
 *
 * @see SKFederateInterface
 * @see SEEAbstractFederate
 * @since 2.0
 */
public abstract class SKBaseFederate implements SKFederateInterface {
    private static final Logger logger = LoggerFactory.getLogger(SKBaseFederate.class);

    private final SKFederateAmbassador federateAmbassador;
    private final FederateConfiguration config;

    protected SKBaseFederate(SKFederateAmbassador federateAmbassador, FederateConfiguration config) {
        this.federateAmbassador = federateAmbassador;
        this.config = config;
    }

    public abstract void configureAndStart();

    @Override
    public final void connectToRTI(RtiConfiguration rtiConfig) throws CallNotAllowedFromWithinCallback, Unauthorized, RTIinternalError, ConnectionFailed, UnsupportedCallbackModel {
        try {
            RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

            rtiAmbassador.connect(federateAmbassador, CallbackModel.HLA_IMMEDIATE, rtiConfig);
            String rtiAddress = rtiConfig.rtiAddress();
            logger.debug("Successfully established connection to the RTI hosted at <{}>.", (rtiAddress != null ? rtiAddress : "Unknown"));
        } catch(AlreadyConnected ignore) {
            // Let it be: we don't want to throw an exception for this.
        }
    }

    @Override
    public final void joinFederationExecution() throws ErrorReadingFOM, CouldNotCreateLogicalTimeFactory, RestoreInProgress, CallNotAllowedFromWithinCallback, InconsistentFOM, FederationExecutionDoesNotExist, Unauthorized, CouldNotOpenFOM, NotConnected, InvalidFOM, RTIinternalError, SaveInProgress, FederateNotExecutionMember, AsynchronousDeliveryAlreadyEnabled {
        String[] additionalFomModules = config.additionalFomModules();

        boolean joined = false;
        String federateNameSuffix = "";
        int attempt = 1;

        RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        while (!joined) {
            try {
                if (additionalFomModules.length > 0) {
                    rtiAmbassador.joinFederationExecution(config.federateName() + federateNameSuffix, config.federateType(), config.federationName(), additionalFomModules);
                } else {
                    rtiAmbassador.joinFederationExecution(config.federateName() + federateNameSuffix, config.federateType(), config.federationName());
                }

                joined = true;
            } catch (FederateNameAlreadyInUse e) {
                federateNameSuffix = "_" + attempt;
            } catch (FederateAlreadyExecutionMember ignore) {
                // Let it be: we don't want to throw an exception for this.
                break;
            }
        }

        if (config.asynchronousDelivery()) {
            rtiAmbassador.enableAsynchronousDelivery();
            logger.debug("Asynchronous delivery has been enabled for this federate.");
        }
    }

    @Override
    public final void resignFederationExecution() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, CallNotAllowedFromWithinCallback, InvalidResignAction, OwnershipAcquisitionPending, FederateOwnsAttributes, FederateIsExecutionMember {
        RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

        if (config.timeConstrained()) {
            try {
                rtiAmbassador.disableTimeConstrained();
                logger.debug("The RTI is no longer time constraining this federate.");
            } catch (TimeConstrainedIsNotEnabled ignore) {
                // Let it be: we don't want to throw an exception for this.
            }
        }

        if (config.timeRegulating()) {
            try {
                rtiAmbassador.disableTimeRegulation();
                logger.debug("The RTI is no longer time regulating this federate.");
            } catch (TimeRegulationIsNotEnabled ignore) {
                // Let it be: we don't want to throw an exception for this.
            }
        }

        if (config.asynchronousDelivery()) {
            try {
                rtiAmbassador.disableAsynchronousDelivery();
                logger.debug("Asynchronous delivery has been disabled for this federate.");
            } catch (AsynchronousDeliveryAlreadyDisabled ignore) {
                // Let it be: we don't want to throw an exception for this.
            }
        }

        rtiAmbassador.resignFederationExecution(ResignAction.DELETE_OBJECTS_THEN_DIVEST);
        String federationName = config.federationName();
        logger.info("The federate has resigned from the federation execution <{}>.", federationName);
        rtiAmbassador.disconnect();
        logger.debug("The RTI ambassador has been disconnected from its RTI.");
    }

    private void verifyAnnotationExists(Class<?> targetClass, Class<? extends Annotation> annotationClass) {
        String errorMessage = "Failed to parse the class " + targetClass.getName() + " because it is missing the " + annotationClass.getName() + " annotation.";
        if (!targetClass.isAnnotationPresent(annotationClass)) {
            throw new IllegalStateException(errorMessage);
        }
    }

    @Override
    public final void publishObjectClass(Class<?> objectClass) throws FederateNotExecutionMember, NameNotFound, NotConnected, RTIinternalError, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, SaveInProgress {
        verifyAnnotationExists(objectClass, ObjectClass.class);

        Predicate<ObjectClassModel> searchPredicate = model -> model.getObjectClass().equals(objectClass);
        ObjectClassModel objectClassModel = federateAmbassador.queryObjectClassModels(searchPredicate);

        if (objectClassModel == null) {
            objectClassModel = federateAmbassador.addObjectClassModel(objectClass);
        }

        objectClassModel.publish();
    }

    @Override
    public final void unpublishObjectClass(Class<?> objectClass) throws FederateNotExecutionMember, ObjectClassNotDefined, RestoreInProgress, OwnershipAcquisitionPending, NotConnected, RTIinternalError, SaveInProgress, AttributeNotDefined {
        verifyAnnotationExists(objectClass, ObjectClass.class);

        Predicate<ObjectClassModel> searchPredicate = model -> model.getObjectClass().equals(objectClass);
        ObjectClassModel objectClassModel = federateAmbassador.queryObjectClassModels(searchPredicate);

        if (objectClassModel != null) {
            objectClassModel.unpublish();
            federateAmbassador.removeObjectClassEntities(objectClassModel);
        } else {
            logger.warn("Failed to unpublish the object class <{}> because it has not been published yet.", objectClass.getName());
        }
    }

    @Override
    public final void subscribeObjectClass(Class<?> objectClass) throws FederateNotExecutionMember, NameNotFound, NotConnected, RTIinternalError, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, SaveInProgress {
        verifyAnnotationExists(objectClass, ObjectClass.class);

        Predicate<ObjectClassModel> searchPredicate = model -> model.getObjectClass().equals(objectClass);
        ObjectClassModel objectClassModel = federateAmbassador.queryObjectClassModels(searchPredicate);

        if (objectClassModel == null) {
            objectClassModel = federateAmbassador.addObjectClassModel(objectClass);
        }

        objectClassModel.subscribe();
    }

    @Override
    public final void unsubscribeObjectClass(Class<?> objectClass) throws FederateNotExecutionMember, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        verifyAnnotationExists(objectClass, ObjectClass.class);

        Predicate<ObjectClassModel> searchPredicate = model -> model.getObjectClass().equals(objectClass);
        ObjectClassModel objectClassModel = federateAmbassador.queryObjectClassModels(searchPredicate);

        if (objectClassModel != null) {
            objectClassModel.unsubscribe();
            federateAmbassador.removeObjectClassEntities(objectClassModel);
        } else {
            logger.warn("Failed to unsubscribe the object class <{}> because it has not been subscribed yet.", objectClass.getName());
        }
    }

    @Override
    public final void publishInteractionClass(Class<?> interactionClass) throws FederateNotExecutionMember, NameNotFound, NotConnected, RTIinternalError, InvalidInteractionClassHandle, RestoreInProgress, InteractionClassNotDefined, SaveInProgress {
        verifyAnnotationExists(interactionClass, InteractionClass.class);

        Predicate<InteractionClassModel> searchPredicate = model -> model.getInteractionClass().equals(interactionClass);
        InteractionClassModel interactionClassModel = federateAmbassador.queryInteractionClassModels(searchPredicate);

        if (interactionClassModel == null) {
            interactionClassModel = federateAmbassador.addInteractionClassModel(interactionClass);
        }

        interactionClassModel.publish();
    }

    @Override
    public final void unpublishInteractionClass(Class<?> interactionClass) throws FederateNotExecutionMember, RestoreInProgress, InteractionClassNotDefined, NotConnected, RTIinternalError, SaveInProgress {
        verifyAnnotationExists(interactionClass, InteractionClass.class);

        Predicate<InteractionClassModel> searchPredicate = model -> model.getInteractionClass().equals(interactionClass);
        InteractionClassModel interactionClassModel = federateAmbassador.queryInteractionClassModels(searchPredicate);

        if (interactionClassModel != null) {
            interactionClassModel.unpublish();
        } else {
            logger.warn("Failed to unpublish the interaction class <{}? because it has not yet been published yet.", interactionClass.getName());
        }
    }

    @Override
    public final void subscribeInteractionClass(Class<?> interactionClass) throws FederateNotExecutionMember, NameNotFound, NotConnected, RTIinternalError, InvalidInteractionClassHandle, RestoreInProgress, InteractionClassNotDefined, SaveInProgress, FederateServiceInvocationsAreBeingReportedViaMOM {
        verifyAnnotationExists(interactionClass, InteractionClass.class);

        Predicate<InteractionClassModel> searchPredicate = model -> model.getInteractionClass().equals(interactionClass);
        InteractionClassModel interactionClassModel = federateAmbassador.queryInteractionClassModels(searchPredicate);

        if (interactionClassModel == null) {
            interactionClassModel = federateAmbassador.addInteractionClassModel(interactionClass);
        }

        interactionClassModel.subscribe();
    }

    @Override
    public final void unsubscribeInteractionClass(Class<?> interactionClass) throws FederateNotExecutionMember, RestoreInProgress, InteractionClassNotDefined, NotConnected, RTIinternalError, SaveInProgress {
        verifyAnnotationExists(interactionClass, InteractionClass.class);

        Predicate<InteractionClassModel> searchPredicate = model -> model.getInteractionClass().equals(interactionClass);
        InteractionClassModel interactionClassModel = federateAmbassador.queryInteractionClassModels(searchPredicate);

        if (interactionClassModel != null) {
            interactionClassModel.unsubscribe();
        } else {
            logger.warn("Failed to unsubscribe the interaction class <{}> because it has not been subscribed yet.", interactionClass.getName());
        }
    }

    @Override
    public final Object queryRemoteObjectInstance(String instanceName) {
        if (instanceName == null) {
            logger.warn("Futile attempt to query object instance because NULL was provided as the search string.");
            return null;
        }

        Predicate<ObjectClassEntity> searchPredicate = e -> e.getName().equals(instanceName) && federateAmbassador.isRemoteEntity(e);
        ObjectClassEntity entity = federateAmbassador.queryEntities(searchPredicate);
        boolean matured = federateAmbassador.isRemoteEntityMatured(entity);

        if (entity != null && matured) {
            return entity.getElement();
        } else {
            return null;
        }
    }

    @Override
    public final String registerObjectInstance(Object objectInstanceElement) throws FederateNotExecutionMember, ObjectClassNotPublished, ObjectClassNotDefined, RestoreInProgress, ObjectInstanceNotKnown, NotConnected, RTIinternalError, SaveInProgress {
        if (objectInstanceElement == null) {
            logger.warn("Object instance creation at the RTI failed because a NULL value was supplied.");
            return null;
        }

        verifyAnnotationExists(objectInstanceElement.getClass(), ObjectClass.class);
        return federateAmbassador.createEntity(objectInstanceElement);
    }

    @Override
    public final String registerObjectInstance(Object objectInstanceElement, String requestedName) throws FederateNotExecutionMember, RestoreInProgress, IllegalName, NotConnected, RTIinternalError, SaveInProgress, ObjectClassNotPublished, ObjectClassNotDefined, ObjectInstanceNotKnown, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved {
        if (objectInstanceElement == null) {
            logger.warn("Failed to create object instance <{}> at the RTI because a NULL value was supplied.", requestedName);
            return null;
        }

        if (requestedName == null) {
            logger.warn("Failed to create a named object instance at the RTI because a NULL value was supplied as its name.");
            return null;
        }

        verifyAnnotationExists(objectInstanceElement.getClass(), ObjectClass.class);
        return federateAmbassador.createEntity(objectInstanceElement, requestedName);
    }

    @Override
    public final void updateObjectInstance(Object objectInstance) {
        if (objectInstance == null) {
            throw new UpdateException("Failed to send updated object instance values to the RTI because the provided object instance is NULL.");
        }

        verifyAnnotationExists(objectInstance.getClass(), ObjectClass.class);
        federateAmbassador.updateEntity(objectInstance);
    }

    @Override
    public final void deleteObjectInstance(Object objectInstance, boolean relinquishNameReservation) throws FederateNotExecutionMember, RestoreInProgress, ObjectInstanceNotKnown, DeletePrivilegeNotHeld, NotConnected, RTIinternalError, SaveInProgress, ObjectInstanceNameNotReserved {
        if (objectInstance == null) {
            logger.warn("Futile attempt to delete an object instance, NULL was provided as the value.");
            return;
        }

        verifyAnnotationExists(objectInstance.getClass(), ObjectClass.class);
        federateAmbassador.deleteEntity(objectInstance, relinquishNameReservation);
    }

    @Override
    public final boolean sendInteraction(Object interaction) throws FederateNotExecutionMember, InteractionParameterNotDefined, RestoreInProgress, InteractionClassNotDefined, InteractionClassNotPublished, NotConnected, RTIinternalError, SaveInProgress {
        if (interaction == null) {
            logger.warn("Futile attempt to send interaction because NULL was provided as the value.");
            return false;
        }

        verifyAnnotationExists(interaction.getClass(), InteractionClass.class);
        return federateAmbassador.sendInteraction(interaction);
    }

    @Override
    public void addRemoteObjectInstanceListener(RemoteObjectInstanceListener listener) {
        federateAmbassador.addRemoteObjectInstanceListener(listener);
    }

    @Override
    public void removeRemoteObjectInstanceListener(RemoteObjectInstanceListener listener) {
        federateAmbassador.removeRemoteObjectInstanceListener(listener);
    }

    @Override
    public final void addInteractionListener(InteractionListener listener) {
        federateAmbassador.addInteractionListener(listener);
    }

    @Override
    public final void removeInteractionListener(InteractionListener listener) {
        federateAmbassador.removeInteractionListener(listener);
    }

    public final FederateConfiguration getConfiguration() {
        return config;
    }

    public SKFederateAmbassador getFederateAmbassador() {
        return federateAmbassador;
    }
}
