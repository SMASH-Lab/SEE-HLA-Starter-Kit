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

package org.see.skf.internal.runtime;

import hla.rti1516_2025.*;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.AttributeOwnershipListener;
import org.see.skf.core.ObjectInstanceListener;
import org.see.skf.internal.HLAUtilityFactory;
import org.see.skf.internal.callbacks.FederateCallbackManager;
import org.see.skf.internal.callbacks.NameReservationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Predicate;

public final class HLAObjectManager {

    private static final Logger logger = LoggerFactory.getLogger(HLAObjectManager.class);

    private final RTIambassador rtiAmbassador;

    private final ExecutorService executor;
    private final FederateCallbackManager callbackManager;
    private final SKAnnotatedTypeParser parser;

    private final Set<HLAObjectClass> objectClasses;
    private final Set<ObjectInstance> objectInstances;

    private final Map<String, Set<ObjectInstanceListener>> remoteInstanceNameToListeners;
    private final Set<ObjectInstanceHandle> instancesPendingInitialValues;
    private final Map<ObjectInstance, Set<AttributeOwnershipListener>> instanceNameToAttributeOwnershipListeners;

    public HLAObjectManager(FederateCallbackManager callbackManager, ExecutorService executor, SKAnnotatedTypeParser parser) {
        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

        this.executor = executor;
        this.callbackManager = callbackManager;
        this.parser = parser;

        this.objectClasses = new CopyOnWriteArraySet<>();
        this.objectInstances = new CopyOnWriteArraySet<>();
        this.remoteInstanceNameToListeners = new ConcurrentHashMap<>();
        this.instancesPendingInitialValues = new CopyOnWriteArraySet<>();
        this.instanceNameToAttributeOwnershipListeners = new ConcurrentHashMap<>();
    }

    private HLAObjectClass createObjectClass(Class<?> proxyClass) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        SKAnnotatedTypeParser.Metadata proxyMetadata = this.parser.parseObjectInstanceProxy(proxyClass);
        String className = proxyMetadata.getFomClassName();

        ObjectClassHandle handle;
        try {
            handle = rtiAmbassador.getObjectClassHandle(className);
        } catch (NameNotFound e) {
            throw new IllegalArgumentException("The HLA object class <" + className + "> is not defined in any FOM modules currently being used in the federation execution.");
        }

        return new HLAObjectClass.Builder()
                .withHandle(handle)
                .withMetadata(proxyMetadata)
                .build();
    }

    public void publishObjectClass(Class<?> proxyClass, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress, AttributeNotDefined, ObjectClassNotDefined {
        if (proxyClass == null) {
            throw new IllegalArgumentException("Class representing how instances of the HLA object class should be interpreted by the federate cannot be NULL.");
        } else if (attributeNames == null || attributeNames.length < 1) {
            throw new IllegalArgumentException("At least one attribute has to be provided for the object class to be published.");
        }

        HLAObjectClass objectClass;
        if ((objectClass = getObjectClass(objClass -> objClass.getProxyClass().equals(proxyClass))) == null) {
            objectClass = createObjectClass(proxyClass);
            this.objectClasses.add(objectClass);
        }

        objectClass.publishAttributes(attributeNames);
    }

    public void unpublishObjectClass(String name, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, OwnershipAcquisitionPending, NotConnected, RTIinternalError, SaveInProgress, ObjectClassNotDefined, AttributeNotDefined {
        HLAObjectClass objectClass;
        if ((objectClass = getObjectClass(objClass -> objClass.getName().equals(name))) != null) {
            objectClass.unpublishAttributes(attributeNames);
        }
    }

    public void subscribeObjectClass(Class<?> proxyClass, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress, AttributeNotDefined, ObjectClassNotDefined {
        if (proxyClass == null) {
            throw new IllegalArgumentException("Class representing how instances of the HLA object class should be interpreted by the federate cannot be NULL.");
        } else if (attributeNames == null || attributeNames.length < 1) {
            throw new IllegalArgumentException("At least one attribute has to be provided for the object class to be subscribed.");
        }

        HLAObjectClass objectClass;
        if ((objectClass = getObjectClass(objClass -> objClass.getProxyClass().equals(proxyClass))) == null) {
            objectClass = createObjectClass(proxyClass);
            this.objectClasses.add(objectClass);
        }

        objectClass.subscribeAttributes(attributeNames);
    }

    public void unsubscribeObjectClass(String name, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, AttributeNotDefined, ObjectClassNotDefined {
        HLAObjectClass objectClass;
        if ((objectClass = getObjectClass(objClass -> objClass.getName().equals(name))) != null) {
            objectClass.unsubscribeAttributes(attributeNames);
        }
    }

    private HLAObjectClass getObjectClass(Predicate<HLAObjectClass> predicate) {
        return this.objectClasses.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    private ObjectInstance getObjectInstance(Predicate<ObjectInstance> predicate) {
        return this.objectInstances.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    public Future<Void> createObjectInstance(Object proxy, String instanceName) {
        if (instanceName == null || proxy == null) {
            throw new IllegalArgumentException("Cannot create HLA object instance because one or more NULL references were passed as argument(s).");
        }

        if (getObjectInstance(instance -> instance.name.equals(instanceName)) != null) {
            throw new IllegalArgumentException("Cannot create the object instance <" + instanceName + "> because it already exists.");
        }

        String objectClassName = this.parser.getInstanceProxyFomName(proxy.getClass());
        HLAObjectClass objectClass = getObjectClass(objClass -> objClass.getName().equals(objectClassName) /* && objClass.getProxyClass().equals(proxy.getClass())*/ );
        if (objectClass == null) {
            throw new IllegalArgumentException("Cannot create the object instance <" + instanceName + "> because its object class <" + objectClassName + "> may not have been declared yet or the provided object instance class is not of the same type.");
        }

        FutureTask<Void> task = new FutureTask<>(() -> {
            if (!reserveName(instanceName)) {
                throw new ObjectInstanceCreationException("Could not create the object instance <" + instanceName + "> because its name could not be reserved.");
            }

            ObjectInstanceHandle instanceHandle = rtiAmbassador.registerObjectInstance(objectClass.getHandle(), instanceName);
            ObjectInstance objectInstance = new ObjectInstance(instanceName, instanceHandle, objectClass, proxy);
            this.objectInstances.add(objectInstance);
            logger.info("Created named object instance <{}> of object class <{}>.", instanceName, objectClassName);

            return null;
        });

        this.executor.submit(task);
        return task;
    }

    private boolean reserveName(String name) throws FederateNotExecutionMember, RestoreInProgress, IllegalName, NotConnected, RTIinternalError, SaveInProgress {
        Future<Boolean> task = this.callbackManager.invokeNameReservationCallback(name);
        try {
            return task.get(8L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new NameReservationException("Could not reserve the object instance name <" + name + ">.", e);
        }  catch (TimeoutException e) {
            throw new NameReservationException("Could not reserve the object instance name <" + name + "> in time.", e);
        }
    }

    public String createObjectInstance(Object proxy) throws FederateNotExecutionMember, ObjectClassNotPublished, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (proxy == null) {
            throw new IllegalArgumentException("Cannot create HLA object instance because one or more NULL references were passed as argument(s).");
        }

        String objectClassName = this.parser.getInstanceProxyFomName(proxy.getClass());
        HLAObjectClass objectClass = getObjectClass(objClass -> objClass.getName().equals(objectClassName));
        if (objectClass == null) {
            throw new IllegalArgumentException("Cannot create the object instance because its object class <" + objectClassName + "> has not been declared yet.");
        }

        ObjectInstanceHandle instanceHandle = rtiAmbassador.registerObjectInstance(objectClass.getHandle());
        String assignedName;
        try {
            assignedName = rtiAmbassador.getObjectInstanceName(instanceHandle);
        } catch (ObjectInstanceNotKnown e) {
            throw new ObjectInstanceCreationException("Failed to create object instance of the class <" + objectClassName + "> because its assigned name could not be retrieved.");
        }

        ObjectInstance objectInstance = new ObjectInstance(assignedName, instanceHandle, objectClass, proxy);
        this.objectInstances.add(objectInstance);

        logger.info("Object instance <{}> of the object class <{}> was created.", assignedName, objectClassName);
        return assignedName;
    }

    public void updateObjectInstance(Object proxy, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, AttributeNotOwned, NotConnected, RTIinternalError, SaveInProgress, AttributeNotDefined, ObjectInstanceNotKnown {
        if (proxy == null) {
            throw new IllegalArgumentException("The object instance reference passed in as argument cannot be NULL.");
        }

        ObjectInstance objectInstance = getObjectInstance(instance -> instance.proxy.equals(proxy));
        if (objectInstance == null) {
            throw new IllegalArgumentException("The provided object is not associated with any known object instances that were previously created.");
        }

        objectInstance.objectClass.updateAttributeValues(objectInstance.handle, objectInstance.proxy, attributeNames);
    }

    public void destroyObjectInstance(Object proxy) throws FederateNotExecutionMember, RestoreInProgress, DeletePrivilegeNotHeld, NotConnected, RTIinternalError, SaveInProgress {
        if (proxy == null) {
            throw new IllegalArgumentException("Cannot find object instance to delete since the argument passed in is a NULL reference.");
        }

        ObjectInstance objectInstance = getObjectInstance(instance -> instance.proxy.equals(proxy));
        if (objectInstance == null) {
            logger.error("Failed to delete object instance <{}> because its associated proxy information was not found.", proxy);
            return;
        }

        try {
            rtiAmbassador.deleteObjectInstance(objectInstance.handle, null);
            this.objectInstances.remove(objectInstance);

            logger.info("Object instance <{}> was successfully destroyed.", objectInstance.name);
        } catch (ObjectInstanceNotKnown e) {
            logger.error("Failed to delete object instance <{}>. It may already have been previously deleted.", proxy, e);
        }
    }

    public void remoteObjectInstanceDiscovered(ObjectInstanceHandle instanceHandle, String instanceName, ObjectClassHandle classHandle, String producingFederateName) {
        HLAObjectClass objectClass = getObjectClass(objClass -> objClass.getHandle().equals(classHandle));

        if (objectClass != null) {
            ObjectInstance instance = new ObjectInstance(instanceName, instanceHandle, objectClass);

            this.objectInstances.add(instance);
            this.instancesPendingInitialValues.add(instanceHandle);

            notifyRemoteObjectInstanceDiscovered(instanceName, producingFederateName);

            try {
                sendAttributeValueUpdateRequest(instanceName, instanceHandle, objectClass.getSubscribedAttributes());
            } catch (RTIexception e) {
                logger.error("Failed to request latest attribute values for the newly-discovered object instance <{}>", instanceName);
            }

        } else {
            logger.debug("Ignored newly-discovered object instance <{}> as no corresponding object class information for it is known.", instanceName);
        }
    }

    public void remoteObjectInstanceUpdated(ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributeValues) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.handle.equals(instanceHandle));

        if (objectInstance != null) {
            HLAObjectClass objectClass = objectInstance.objectClass;
            objectClass.reflectRemoteUpdate(objectInstance, attributeValues);

            if (this.instancesPendingInitialValues.contains(instanceHandle)) {
                notifyRemoteObjectInstanceInitialized(objectInstance.name, objectInstance.proxy);
                this.instancesPendingInitialValues.remove(instanceHandle);
            }
        } else {
            logger.error("Discarded update received for object instance with the handle <{}> as no corresponding serialization information for it is known to the federate.", instanceHandle);
        }
    }

    public void remoteObjectInstanceDestroyed(ObjectInstanceHandle instanceHandle, String producingFederateName) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.handle.equals(instanceHandle));

        if (objectInstance != null) {
            this.objectInstances.remove(objectInstance);
            notifyRemoteObjectInstanceDestroyed(objectInstance.name, producingFederateName);
        }
    }

    private void notifyRemoteObjectInstanceDiscovered(String name, String producingFederateName) {
        Set<ObjectInstanceListener> listeners = this.remoteInstanceNameToListeners.get(name);

        if (!listeners.isEmpty()) {
            listeners.forEach(listener -> listener.discovered(producingFederateName));
        }
    }

    private void notifyRemoteObjectInstanceInitialized(String name, Object proxy) {
        Set<ObjectInstanceListener> listeners = this.remoteInstanceNameToListeners.get(name);

        if (!listeners.isEmpty()) {
            listeners.forEach(listener -> listener.initialized(proxy));
        }
    }

    private void notifyRemoteObjectInstanceDestroyed(String name, String producingFederateName) {
        Set<ObjectInstanceListener> listeners = this.remoteInstanceNameToListeners.get(name);

        if (!listeners.isEmpty()) {
            listeners.forEach(listener -> listener.destroyed(producingFederateName));
        }
    }

    public boolean isRemoteObjectInstanceDiscovered(String name) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.name.equals(name));
        return objectInstance != null && objectInstance.proxy != null;
    }

    public Object queryObjectInstance(String instanceName) {
        for (ObjectInstance instance : this.objectInstances) {
            if (instance.name.equals(instanceName)) {
                return instance.proxy;
            }
        }

        return null;
    }

    private void sendAttributeValueUpdateRequest(String name, ObjectInstanceHandle handle, AttributeHandleSet attributes) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        try {
            rtiAmbassador.requestAttributeValueUpdate(handle, attributes, null);
        } catch (AttributeNotDefined | ObjectInstanceNotKnown e) {
            throw new RuntimeException("Failed to request the latest attribute values for the object instance <" + name + ">.", e);
        }
    }

    public void provideObjectInstanceUpdate(ObjectInstanceHandle instanceHandle, AttributeHandleSet attributes) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.handle.equals(instanceHandle));

        if (objectInstance != null) {
            HLAObjectClass objectClass = objectInstance.objectClass;

            try {
                objectClass.provideUpdate(instanceHandle, objectInstance.proxy, attributes);
            } catch (RTIexception e) {
                logger.error("Failed to dispatch updates for object instance <{}>.", objectInstance.name, e);
            }
        }
    }

    public void requestRemoteObjectInstanceUpdates(String name, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.name.equals(name));

        if (objectInstance != null) {
            AttributeHandleSet set = objectInstance.objectClass.getAttributeHandles(attributeNames);
            sendAttributeValueUpdateRequest(name, objectInstance.handle, set);
        } else {
            logger.warn("Cannot request the latest the values for the remote object instance <{}> because it has not been discovered by this federate yet.", name);
        }
    }

    public void addObjectInstanceListener(String objectInstanceName, ObjectInstanceListener listener) {
        if (objectInstanceName == null || listener == null) {
            return;
        }

        this.remoteInstanceNameToListeners.computeIfAbsent(objectInstanceName, set -> new HashSet<>());
        this.remoteInstanceNameToListeners.get(objectInstanceName).add(listener);
    }

    public void removeObjectInstanceListener(ObjectInstanceListener listener) {
        for (Map.Entry<String, Set<ObjectInstanceListener>> entry : this.remoteInstanceNameToListeners.entrySet()) {
            String objectInstanceName = entry.getKey();
            Set<ObjectInstanceListener> listeners = entry.getValue();

            if (listeners.contains(listener)) {
                listeners.remove(listener);

                if (listeners.isEmpty()) {
                    this.remoteInstanceNameToListeners.remove(objectInstanceName);
                }

                break;
            }
        }
    }

    public void addPropertyChangeListener(Object proxy, PropertyChangeListener listener) {
        ObjectInstance objectInstance = getObjectInstance(instance -> /* instance.proxy != null && */ instance.proxy.equals(proxy));

        if (objectInstance != null) {
            objectInstance.pcs.addPropertyChangeListener(listener);
        }
    }

    public void addPropertyChangeListener(Object proxy, String propertyName, PropertyChangeListener listener) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.proxy.equals(proxy));

        if (objectInstance != null) {
            objectInstance.pcs.addPropertyChangeListener(propertyName, listener);
        }
    }

    public void removePropertyChangeListener(Object proxy, PropertyChangeListener listener) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.proxy.equals(proxy));

        if (objectInstance != null) {
            objectInstance.pcs.removePropertyChangeListener(listener);
        }
    }

    public void removePropertyChangeListener(Object proxy, String propertyName, PropertyChangeListener listener) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.proxy.equals(proxy));

        if (objectInstance != null) {
            objectInstance.pcs.removePropertyChangeListener(propertyName, listener);
        }
    }

    public Future<Map<String, String>> queryAttributeOwnership(String instanceName, String... attributeNames) {
        if (instanceName == null || attributeNames == null) {
            throw new IllegalArgumentException("Cannot query attribute ownership since a NULL reference was passed in for one of the arguments.");
        }

        if (attributeNames.length < 1) {
            throw new IllegalArgumentException("At least one attribute name is required for querying attribute ownership.");
        }

        ObjectInstance objectInstance = getObjectInstance(instance -> instance.name.equals(instanceName));
        if (objectInstance != null) {
            HLAObjectClass objectClass = objectInstance.objectClass;
            AttributeHandleSet set = objectClass.getAttributeHandles(attributeNames);

            FutureTask<Map<String, String>> task = new FutureTask<>(() -> {
                Map<AttributeHandle, String> queryResult = this.callbackManager.invokeAttributeOwnershipQuery(objectInstance.handle, set).get();
                Map<String, String> attributeNameToOwnerName = new HashMap<>();

                queryResult.forEach((attributeHandle, ownerName) -> {
                    String attributeName = objectClass.getAttributeName(attributeHandle);
                    attributeNameToOwnerName.put(attributeName, ownerName);
                });

                return attributeNameToOwnerName;
            });

            this.executor.submit(task);
            return task;
        } else {
            return null;
        }
    }

    public void acquireAttributeOwnership(String instanceName, String... attributeNames) throws FederateNotExecutionMember, ObjectClassNotPublished, AttributeNotDefined, RestoreInProgress, FederateOwnsAttributes, NotConnected, RTIinternalError, AttributeNotPublished, SaveInProgress, ObjectInstanceNotKnown {
        if (instanceName == null || attributeNames == null) {
            throw new IllegalArgumentException("Cannot attempt to acquire attribute ownership since a NULL reference was passed in for one of the arguments.");
        }

        if (attributeNames.length < 1) {
            throw new IllegalArgumentException("At least one attribute name is required for attempting attribute ownership acquisition.");
        }

        ObjectInstance objectInstance = getObjectInstance(instance -> instance.name.equals(instanceName));
        if (objectInstance != null) {
            HLAObjectClass objectClass = objectInstance.objectClass;
            AttributeHandleSet set = objectClass.getAttributeHandles(attributeNames);

            rtiAmbassador.attributeOwnershipAcquisition(objectInstance.handle, set, null);
        }
    }

    public void divestAttributeOwnershipIfWanted(Object proxy, String... attributeNames) throws FederateNotExecutionMember, AttributeNotDefined, RestoreInProgress, AttributeNotOwned, ObjectInstanceNotKnown, NotConnected, RTIinternalError, SaveInProgress {
        if (proxy == null || attributeNames == null) {
            throw new IllegalArgumentException("Cannot divest attribute ownership since a NULL reference was passed in for one of the arguments.");
        }

        if (attributeNames.length < 1) {
            throw new IllegalArgumentException("At least one attribute name is required for divesting attribute ownership.");
        }

        ObjectInstance objectInstance = getObjectInstance(instance -> instance.proxy.equals(proxy));
        if (objectInstance != null) {
            HLAObjectClass objectClass = objectInstance.objectClass;
            AttributeHandleSet set = objectClass.getAttributeHandles(attributeNames);
            rtiAmbassador.attributeOwnershipDivestitureIfWanted(objectInstance.handle, set, null);
        }
    }

    public void notifyAttributeOwnershipReleaseRequested(ObjectInstanceHandle instanceHandle, AttributeHandleSet candidateAttributes) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.handle.equals(instanceHandle));

        if (objectInstance != null) {
            HLAObjectClass objectClass = objectInstance.objectClass;
            Set<String> candidateAttributeNames = objectClass.getAttributeNames(candidateAttributes);

            this.instanceNameToAttributeOwnershipListeners
                    .get(objectInstance)
                    .forEach(listener -> listener.releaseRequested(candidateAttributeNames));
        } else {
            logger.warn("Missed attribute ownership release request for object instance <{}>.", instanceHandle);
        }
    }

    public void notifyAttributeOwnershipAcquired(ObjectInstanceHandle instanceHandle, AttributeHandleSet attributes, boolean outcome) {
        ObjectInstance objectInstance = getObjectInstance(instance -> instance.handle.equals(instanceHandle));

        if (objectInstance != null) {
            HLAObjectClass objectClass = objectInstance.objectClass;
            Set<String> attributeNames = objectClass.getAttributeNames(attributes);

            this.instanceNameToAttributeOwnershipListeners
                    .get(objectInstance)
                    .forEach(listener -> listener.secured(outcome, attributeNames));
        } else {
            logger.warn("Missed attribute ownership secured notification for object instance <{}>.", instanceHandle);
        }
    }

    public void addAttributeOwnershipListener(Object proxy, AttributeOwnershipListener listener) {
        if (proxy == null || listener == null) {
            return;
        }

        ObjectInstance objectInstance = getObjectInstance(instance -> instance.proxy.equals(proxy));
        this.instanceNameToAttributeOwnershipListeners
                .computeIfAbsent(objectInstance, set -> new HashSet<>())
                .add(listener);
    }

    public void removeAttributeOwnershipListener(AttributeOwnershipListener listener) {
        for (Map.Entry<ObjectInstance, Set<AttributeOwnershipListener>> entry : this.instanceNameToAttributeOwnershipListeners.entrySet()) {
            ObjectInstance instance = entry.getKey();
            Set<AttributeOwnershipListener> listeners = entry.getValue();

            if (listeners.contains(listener)) {
                listeners.remove(listener);

                if (listeners.isEmpty()) {
                    this.instanceNameToAttributeOwnershipListeners.remove(instance);
                }

                break;
            }
        }
    }

    public static final class ObjectInstance {

        private final HLAObjectClass objectClass;

        private final String name;

        private final ObjectInstanceHandle handle;

        private Object proxy;

        private final PropertyChangeSupport pcs;

        private ObjectInstance(String name, ObjectInstanceHandle handle, HLAObjectClass objectClass) {
            this.name = name;
            this.handle = handle;
            this.objectClass = objectClass;

            this.proxy = objectClass.createProxy();
            this.pcs = new PropertyChangeSupport(this);
        }

        private ObjectInstance(String name, ObjectInstanceHandle handle, HLAObjectClass objectClass, Object proxy) {
            this.name = name;
            this.handle = handle;
            this.objectClass = objectClass;
            this.proxy = proxy;

            this.pcs = new PropertyChangeSupport(this);
        }

        Object getProxy() {
            return this.proxy;
        }

        void setProxy(Object proxy) {
            this.proxy = proxy;
        }

        void notifyAllListeners(String propertyName, Object oldValue, Object newValue) {
            this.pcs.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
}
