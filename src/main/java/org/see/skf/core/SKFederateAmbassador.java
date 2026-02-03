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
import hla.rti1516_2025.time.LogicalTime;
import org.see.skf.annotations.InteractionClass;
import org.see.skf.annotations.ObjectClass;
import org.see.skf.exceptions.UpdateException;
import org.see.skf.runtime.interactions.InteractionClassModel;
import org.see.skf.runtime.interactions.InteractionClassModelParser;
import org.see.skf.runtime.objects.ObjectClassEntity;
import org.see.skf.runtime.objects.ObjectClassModel;
import org.see.skf.runtime.objects.ObjectClassModelParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A rudimentary federate ambassador implementation designed to complement the SKFederateAmbassador with object and
 * interaction management functionality.
 *
 * @since 2.0
 */
public class SKFederateAmbassador extends NullFederateAmbassador {
    private static final Logger logger = LoggerFactory.getLogger(SKFederateAmbassador.class);

    // Lifecycle of remote entities:
    // Discovered -> No attribute data and therefore maturity = false.
    // Reflected attributes -> Attribute data was received and thus maturity = true.
    // N.B. If an entity is in this map, it's guaranteed to be remote.
    private final ConcurrentMap<ObjectClassEntity, Boolean> remoteEntityToMaturity;
    private final CopyOnWriteArraySet<ObjectClassEntity> entities;

    private final CopyOnWriteArraySet<ObjectClassModel> objectClassModels;
    private final CopyOnWriteArraySet<InteractionClassModel> interactionClassModels;
    private final ConcurrentMap<String, NameReservationStatus> nameRegistry;
    private final CopyOnWriteArraySet<InteractionListener> interactionListeners;
    private final CopyOnWriteArraySet<RemoteObjectInstanceListener> instanceListeners;

    public SKFederateAmbassador() {
        remoteEntityToMaturity = new ConcurrentHashMap<>();
        entities = new CopyOnWriteArraySet<>();
        objectClassModels = new CopyOnWriteArraySet<>();
        interactionClassModels = new CopyOnWriteArraySet<>();
        nameRegistry = new ConcurrentHashMap<>();
        interactionListeners = new CopyOnWriteArraySet<>();
        instanceListeners = new CopyOnWriteArraySet<>();
    }

    final ObjectClassModel queryObjectClassModels(Predicate<ObjectClassModel> predicate) {
        Optional<ObjectClassModel> query = objectClassModels.stream()
                .filter(predicate)
                .findAny();

        return query.orElse(null);
    }

    final ObjectClassModel addObjectClassModel(Class<?> objectClass) throws FederateNotExecutionMember, NameNotFound, NotConnected, RTIinternalError, InvalidObjectClassHandle {
        if (objectClass.isAnnotationPresent(ObjectClass.class)) {
            ObjectClassModelParser parser = new ObjectClassModelParser(objectClass);
            String className = parser.getFomClassName();
            RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
            ObjectClassHandle classHandle = rtiAmbassador.getObjectClassHandle(className);

            ObjectClassModel model = new ObjectClassModel(parser, classHandle);
            objectClassModels.add(model);

            return model;
        } else {
            throw new IllegalStateException("Failed to parse the class <" + objectClass.getName() + "> because it is not annotated with @ObjectClass.");
        }
    }

    final InteractionClassModel queryInteractionClassModels(Predicate<InteractionClassModel> predicate) {
        Optional<InteractionClassModel> query = interactionClassModels.stream()
                .filter(predicate)
                .findAny();

        return query.orElse(null);
    }

    final InteractionClassModel addInteractionClassModel(Class<?> interactionClass) throws FederateNotExecutionMember, NameNotFound, NotConnected, RTIinternalError, InvalidInteractionClassHandle {
        if (interactionClass.isAnnotationPresent(InteractionClass.class)) {
            InteractionClassModelParser parser = new InteractionClassModelParser(interactionClass);
            String className = parser.getFomClassName();
            RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
            InteractionClassHandle classHandle = rtiAmbassador.getInteractionClassHandle(className);

            InteractionClassModel model = new InteractionClassModel(parser, classHandle);
            interactionClassModels.add(model);

            return model;
        } else {
            throw new IllegalStateException("Failed to parse the class <" + interactionClass.getName() + "> because it is not annotated with @InteractionClass.");
        }
    }

    final ObjectClassEntity queryEntities(Predicate<ObjectClassEntity> predicate) {
        Optional<ObjectClassEntity> query = entities.stream()
                .filter(predicate)
                .findAny();

        return query.orElse(null);
    }

    final void removeObjectClassEntities(ObjectClassModel model) {
        Set<ObjectClassEntity> entitySet = entities.stream()
                .filter(entity -> entity.getModel().equals(model))
                .collect(Collectors.toSet());

        for (ObjectClassEntity entity : entitySet) {
            entities.remove(entity);
        }
    }

    public final boolean isRemoteEntity(ObjectClassEntity entity) {
        return remoteEntityToMaturity.containsKey(entity);
    }

    @Override
    public void discoverObjectInstance(ObjectInstanceHandle objectInstance, ObjectClassHandle objectClass, String objectInstanceName, FederateHandle producingFederate) {
        try {
            ObjectClassModel model = queryObjectClassModels(c -> c.getHandle().equals(objectClass));

            if (model != null) {
                Class<?> modelClass = model.getObjectClass();
                Object entityElement = modelClass.getDeclaredConstructor().newInstance();
                ObjectClassEntity entity = new ObjectClassEntity(objectInstanceName, objectInstance, model, entityElement);
                entities.add(entity);
                remoteEntityToMaturity.put(entity, false);
                logger.debug("Discovered object instance <{}> of the class <{}>. Awaiting latest attribute values from the RTI.", objectInstanceName, model.getName());

                RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
                rtiAmbassador.requestAttributeValueUpdate(objectInstance, model.getSubscriptionSet(), null);
            } else {
                logger.error("Failed to build an internalized representation for the discovered object instance <{}>.", objectInstanceName);
            }
        } catch (RTIinternalError | NotConnected | FederateNotExecutionMember | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException | AttributeNotDefined | RestoreInProgress | ObjectInstanceNotKnown | SaveInProgress e) {
            throw new IllegalStateException("Unexpected error encountered while trying to build a representation for the discovered object instance <" + objectInstanceName + ">.", e);
        }
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstance, byte[] userSuppliedTag, FederateHandle producingFederate) {
        removeRemoteEntity(objectInstance);
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstance, byte[] userSuppliedTag, FederateHandle producingFederate, LogicalTime<?, ?> time, OrderType sentOrderType, OrderType receivedOrderType, MessageRetractionHandle optionalRetraction) {
        removeRemoteEntity(objectInstance);
    }

    public final void removeRemoteEntity(ObjectInstanceHandle objectInstance) {
        Predicate<ObjectClassEntity> searchPredicate = e -> e.getHandle().equals(objectInstance);
        ObjectClassEntity entity = queryEntities(searchPredicate);

        if (entity != null) {
            entities.remove(entity);
            remoteEntityToMaturity.remove(entity);
            new Thread(() ->notifyRemoteInstanceRemoved(entity.getName())).start();
            logger.info("The remote object instance <{}> was deleted.", entity.getName());
        }
    }

    @Override
    public void objectInstanceNameReservationSucceeded(String objectInstanceName) {
        nameRegistry.replace(objectInstanceName, NameReservationStatus.SUCCEEDED);
    }

    @Override
    public void objectInstanceNameReservationFailed(String objectInstanceName) {
        nameRegistry.replace(objectInstanceName, NameReservationStatus.FAILED);
    }

    @Override
    public void provideAttributeValueUpdate(ObjectInstanceHandle objectInstance, AttributeHandleSet attributes, byte[] userSuppliedTag) {
        Predicate<ObjectClassEntity> searchPredicate = e -> e.getHandle().equals(objectInstance);
        ObjectClassEntity entity = queryEntities(searchPredicate);

        if (entity != null) {
            ObjectClassModel objectClassModel = entity.getModel();
            Object objectInstanceElement = entity.getElement();
            AttributeHandleValueMap attributeValues = objectClassModel.getEncodedAttributeValues(objectInstanceElement);

            sendUpdatedAttributeValues(entity.getName(), objectInstance, attributeValues);
        }
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstance, AttributeHandleValueMap attributeValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions) {
        reflectAttributes(objectInstance, attributeValues);
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstance, AttributeHandleValueMap attributeValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions, LogicalTime<?, ?> time, OrderType sentOrderType, OrderType receivedOrderType, MessageRetractionHandle optionalRetraction) {
        reflectAttributes(objectInstance, attributeValues);
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap parameterValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions) {
        receiveInteraction(interactionClass, parameterValues);
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap parameterValues, byte[] userSuppliedTag, TransportationTypeHandle transportationType, FederateHandle producingFederate, RegionHandleSet optionalSentRegions, LogicalTime<?, ?> time, OrderType sentOrderType, OrderType receivedOrderType, MessageRetractionHandle optionalRetraction) {
        receiveInteraction(interactionClass, parameterValues);
    }

    final boolean isRemoteEntityMatured(ObjectClassEntity entity) {
        return remoteEntityToMaturity.get(entity);
    }

    final String createEntity(Object objectInstanceElement) throws FederateNotExecutionMember, ObjectClassNotPublished, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, ObjectInstanceNotKnown {
        String fomClassName = objectInstanceElement.getClass().getAnnotation(ObjectClass.class).name();
        Predicate<ObjectClassModel> searchPredicate = c -> c.getName().equals(fomClassName);
        ObjectClassModel model = queryObjectClassModels(searchPredicate);

        if (model != null) {
            RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

            ObjectClassHandle classHandle = model.getHandle();
            ObjectInstanceHandle instanceHandle = rtiAmbassador.registerObjectInstance(classHandle);
            String instanceName = rtiAmbassador.getObjectInstanceName(instanceHandle);
            ObjectClassEntity entity = new ObjectClassEntity(instanceName, instanceHandle, model, objectInstanceElement);
            entities.add(entity);

            logger.info("An object instance <{}> of the HLA object class <{}> was created.", instanceName, fomClassName);
            updateEntity(objectInstanceElement);

            return instanceName;
        } else {
            logger.warn("Failed to create an object instance <{}> for the class <{}> because it likely has not been published yet.", objectInstanceElement, fomClassName);
        }

        return null;
    }

    final NameReservationStatus queryNameReservationStatus(String name) {
        return nameRegistry.getOrDefault(name, NameReservationStatus.UNRESERVED);
    }

    final String createEntity(Object objectInstanceElement, String name) throws FederateNotExecutionMember, RestoreInProgress, IllegalName, NotConnected, RTIinternalError, SaveInProgress, ObjectClassNotPublished, ObjectClassNotDefined, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectInstanceNotKnown {
        String fomClassName = objectInstanceElement.getClass().getAnnotation(ObjectClass.class).name();
        Predicate<ObjectClassModel> searchPredicate = c -> c.getName().equals(fomClassName);
        ObjectClassModel model = queryObjectClassModels(searchPredicate);

        if (model != null) {
            RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

            if (queryNameReservationStatus(name) == NameReservationStatus.UNRESERVED) {
                nameRegistry.put(name, NameReservationStatus.PENDING);
                rtiAmbassador.reserveObjectInstanceName(name);

                while (nameRegistry.get(name) == NameReservationStatus.PENDING) {
                    Thread.yield();
                }
            }

            if (nameRegistry.get(name) == NameReservationStatus.SUCCEEDED) {
                ObjectClassHandle classHandle = model.getHandle();
                ObjectInstanceHandle instanceHandle = rtiAmbassador.registerObjectInstance(classHandle, name);
                ObjectClassEntity entity = new ObjectClassEntity(name, instanceHandle, model, objectInstanceElement);
                entities.add(entity);

                logger.info("The object instance <{}> of the HLA object class <{}> was created.", name, model.getName());
                updateEntity(objectInstanceElement);
                return name;
            } else {
                logger.warn("Failed to reserve \"{}\" for use as an object instance name. Relying on the RTI to allocate a name.", name);
                createEntity(objectInstanceElement);
            }
        } else {
            logger.warn("Failed to create object instance <{}> because it likely has not been published yet.", name);
        }

        return null;
    }

    final void updateEntity(Object objectInstanceElement) {
        Predicate<ObjectClassEntity> searchPredicate = e -> e.getElement().equals(objectInstanceElement);
        ObjectClassEntity entity = queryEntities(searchPredicate);

        if (entity != null) {
            if (!isRemoteEntity(entity)) {
                ObjectClassModel model = entity.getModel();
                AttributeHandleValueMap attributeValues = model.getEncodedAttributeValues(objectInstanceElement);
                ObjectInstanceHandle instanceHandle = entity.getHandle();

                sendUpdatedAttributeValues(entity.getName(), instanceHandle, attributeValues);
                logger.debug("Dispatched updated values for the object instance <{}>.", entity.getName());
            } else {
                logger.warn("Cannot send updated values for the object instance <{}> because it is a remote instance and not managed by this federate.", entity.getName());
            }
        } else {
            throw new UpdateException("Cannot update the object instance <" + objectInstanceElement + "> because it has not been created by the RTI yet.");
        }
    }

    public final void sendUpdatedAttributeValues(String entityName, ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributeValues) {
        try {
            RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
            rtiAmbassador.updateAttributeValues(instanceHandle, attributeValues, null);
        } catch (AttributeNotOwned | AttributeNotDefined | ObjectInstanceNotKnown | SaveInProgress |
                 RestoreInProgress | FederateNotExecutionMember | NotConnected | RTIinternalError e) {
            throw new UpdateException("Failed to provide updated attribute values for <" + entityName + ">.");
        }
    }

    public final void reflectAttributes(ObjectInstanceHandle objectInstance, AttributeHandleValueMap attributeValues) {
        Predicate<ObjectClassEntity> searchPredicate = e -> e.getHandle().equals(objectInstance);
        ObjectClassEntity entity = queryEntities(searchPredicate);

        if (entity != null) {
            ObjectClassModel model = entity.getModel();
            Object objectInstanceElement = entity.getElement();
            model.unpackEncodedAttributeValues(objectInstanceElement, attributeValues);

            if (Boolean.FALSE.equals(remoteEntityToMaturity.get(entity))) {
                remoteEntityToMaturity.replace(entity, true);
                new Thread(() -> notifyRemoteInstanceAdded(entity.getName(), entity.getElement())).start();
                logger.info("New remote object instance \"{}\" has been initialized for in-federate use.", entity.getName());
            }
        } else {
            throw new UpdateException("Failed to update the object instance <" + objectInstance + "> with incoming attribute values because its representation is missing.");
        }
    }

    final void deleteEntity(Object objectInstanceElement, boolean relinquishNameReservation) throws FederateNotExecutionMember, RestoreInProgress, ObjectInstanceNotKnown, DeletePrivilegeNotHeld, NotConnected, RTIinternalError, SaveInProgress, ObjectInstanceNameNotReserved {
        Predicate<ObjectClassEntity> searchPredicate = e -> e.getElement().equals(objectInstanceElement);
        ObjectClassEntity entity = queryEntities(searchPredicate);

        if (entity != null) {
            if (isRemoteEntity(entity)) {
                logger.warn("Blocked attempt to delete the object instance <{}>. It is remote and managed by another federate.",  entity.getName());
                return;
            }

            RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

            if (relinquishNameReservation && nameRegistry.containsKey(entity.getName())) {
                rtiAmbassador.releaseObjectInstanceName(entity.getName());
                nameRegistry.remove(entity.getName());
                logger.info("Ownership of the reserved object instance name \"{}\" was relinquished by this federate.", entity.getName());
            }

            rtiAmbassador.deleteObjectInstance(entity.getHandle(), null);
            logger.info("The object instance <{}> has been deleted.", entity.getName());

            entities.remove(entity);
        }
    }

    public final boolean sendInteraction(Object interactionClassElement) throws FederateNotExecutionMember, InteractionParameterNotDefined, RestoreInProgress, InteractionClassNotDefined, InteractionClassNotPublished, NotConnected, RTIinternalError, SaveInProgress {
        Class<?> interactionClass = interactionClassElement.getClass();
        String className = interactionClass.getAnnotation(InteractionClass.class).name();
        Predicate<InteractionClassModel> searchPredicate = c -> c.getName().equals(className);
        InteractionClassModel model = queryInteractionClassModels(searchPredicate);

        if (model != null) {
            InteractionClassHandle classHandle = model.getHandle();
            ParameterHandleValueMap parameterValues = model.getEncodedParameterValues(interactionClassElement);

            RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
            rtiAmbassador.sendInteraction(classHandle, parameterValues, null);
            logger.debug("The interaction <{}> was sent.", interactionClassElement);

            return true;
        } else {
            logger.warn("Failed to send the interaction <{}>, its associated HLA interaction class has likely not been published yet.", interactionClassElement);
            return false;
        }
    }

    public final void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap parameterValues) {
        Predicate<InteractionClassModel> predicate = i -> i.getHandle().equals(interactionClass);
        InteractionClassModel model = queryInteractionClassModels(predicate);

        if (model != null) {
            Class<?> modelClass = model.getInteractionClass();
            try {
                Object interactionElement = modelClass.getDeclaredConstructor().newInstance();
                model.unpackEncodedParameterValues(interactionElement, parameterValues);

                new Thread(() ->notifyInteractionReceived(interactionElement)).start();
                logger.debug("An interaction <{}> was received.", interactionElement);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new IllegalStateException("Error encountered while trying to build a representation for the received interaction <" + interactionClass + ">.", e);
            }
        } else {
            logger.error("Failed to build internalized representation for the incoming interaction with the class handle <{}>.", interactionClass);
        }
    }

    public final void addRemoteObjectInstanceListener(RemoteObjectInstanceListener listener) {
        instanceListeners.add(listener);
    }

    public final void removeRemoteObjectInstanceListener(RemoteObjectInstanceListener listener) {
        instanceListeners.remove(listener);
    }

    public final void addInteractionListener(InteractionListener listener) {
        interactionListeners.add(listener);
    }

    public final void removeInteractionListener(InteractionListener listener) {
        interactionListeners.remove(listener);
    }

    private void notifyInteractionReceived(Object receivedInteractionElement) {
        for (var listener : interactionListeners) {
            listener.received(receivedInteractionElement);
        }
    }

    private void notifyRemoteInstanceAdded(String instanceName, Object objectInstanceElement) {
        for (var listener : instanceListeners) {
            listener.instanceAdded(instanceName, objectInstanceElement);
        }
    }

    private void notifyRemoteInstanceRemoved(String instanceName) {
        for (var listener : instanceListeners) {
            listener.instanceRemoved(instanceName);
        }
    }

    public enum NameReservationStatus {
        UNRESERVED,
        PENDING,
        SUCCEEDED,
        FAILED
    }
}
