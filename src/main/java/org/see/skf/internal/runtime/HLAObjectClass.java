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
import org.see.skf.internal.HLAUtilityFactory;
import org.see.skf.internal.InternalObjectBuilderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

final class HLAObjectClass {

    private static final Logger logger = LoggerFactory.getLogger(HLAObjectClass.class);
    private static final String HLA_PRIVILEGE_TO_DELETE_OBJECT = "HLAprivilegeToDeleteObject";

    private final RTIambassador rtiAmbassador;

    private final ObjectClassHandle handle;

    private final String name;

    private final Class<?> proxyClass;

    private final Map<Attribute, Trait> attributeToTrait;

    private final AttributeHandleValueMap attributeValues;

    private final AttributeHandleSet attributes;

    private HLAObjectClass(Builder builder) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

        this.handle = builder.handle;
        this.name = builder.objectMetadata.getFomClassName();
        this.proxyClass = builder.objectMetadata.getProxyClass();
        this.attributeToTrait = computeAttributeToTraitAssociation(builder.objectMetadata.getTraits());
        this.attributes = rtiAmbassador.getAttributeHandleSetFactory().create();
        this.attributeValues = rtiAmbassador.getAttributeHandleValueMapFactory().create(this.attributeToTrait.size());

        initializePrivilegeToDeleteAttribute();
    }

    private void initializePrivilegeToDeleteAttribute() throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        AttributeHandle privilegeToDeleteObjectHandle = createHLAPrivilegeToDeleteObjectAttribute();
        Attribute privilegeToDeleteObjectAttribute = new Attribute(HLA_PRIVILEGE_TO_DELETE_OBJECT, privilegeToDeleteObjectHandle);
        this.attributeToTrait.put(privilegeToDeleteObjectAttribute, null);
    }

    private AttributeHandle createHLAPrivilegeToDeleteObjectAttribute() throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        AttributeHandle attributeHandle = null;
        try {
            attributeHandle = rtiAmbassador.getAttributeHandle(this.handle, HLA_PRIVILEGE_TO_DELETE_OBJECT);
        } catch (NameNotFound | InvalidObjectClassHandle e) {
            logger.warn("The handle for the attribute HLAprivilegeToDeleteObject of the object class <{}> could not be retrieved from the RTI. Attribute ownership operations may exhibit undefined behavior.", this.name);
        }

        return attributeHandle;
    }

    private Map<Attribute, Trait> computeAttributeToTraitAssociation(Set<Trait> traits) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        Map<Attribute, Trait> map = new HashMap<>();

        for (Trait trait : traits) {
            String attributeName = trait.getAnnotatedName();

            try {
                AttributeHandle attributeHandle = rtiAmbassador.getAttributeHandle(this.handle, attributeName);
                Attribute attribute = new Attribute(attributeName, attributeHandle);

                map.put(attribute, trait);
            } catch (InvalidObjectClassHandle e) {
                throw new RtiHandleAcquisitionException(e);
            } catch (NameNotFound e) {
                throw new RtiHandleAcquisitionException("The attribute <" + attributeName + "> is not defined for the HLA object class <" + this.name + ">.");
            }
        }

        return map;
    }

    void publishAttributes(String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, AttributeNotDefined, ObjectClassNotDefined {
        this.attributes.clear();

        for (String attributeName : attributeNames) {
            Attribute attribute = getAttribute(a -> a.name.equals(attributeName));

            if (attribute != null && !attribute.published.get()) {
                attribute.published.set(true);
                this.attributes.add(attribute.handle);
            }
        }

        rtiAmbassador.publishObjectClassAttributes(this.handle, this.attributes);

        String loggableAttributeNames = getNamesInLoggableFormat(attributeNames);
        logger.info("Published HLA object class <{}> attributes: {}.", this.name, loggableAttributeNames);
    }

    void unpublishAttributes(String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, OwnershipAcquisitionPending, NotConnected, RTIinternalError, SaveInProgress, ObjectClassNotDefined, AttributeNotDefined {
        this.attributes.clear();

        if (attributeNames.length > 1) {
            rtiAmbassador.unpublishObjectClass(this.handle);
        } else {
            for (String attributeName : attributeNames) {
                Attribute attribute = getAttribute(a -> a.name.equals(attributeName));

                if (attribute != null && attribute.published.get()) {
                    attribute.published.set(false);
                    this.attributes.add(attribute.handle);
                }
            }

            rtiAmbassador.unpublishObjectClassAttributes(this.handle, this.attributes);
        }

        String loggableAttributeNames = getNamesInLoggableFormat(attributeNames);
        logger.info("Unpublished HLA object class <{}> attributes: {}.", this.name, loggableAttributeNames);
    }

    void subscribeAttributes(String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, AttributeNotDefined, ObjectClassNotDefined {
        this.attributes.clear();

        for (String attributeName : attributeNames) {
            Attribute attribute = getAttribute(a -> a.name.equals(attributeName));

            if (attribute != null && !attribute.subscribed.get()) {
                attribute.subscribed.set(true);
                this.attributes.add(attribute.handle);
            }
        }

        rtiAmbassador.subscribeObjectClassAttributes(this.handle, this.attributes);

        String loggableAttributeNames = getNamesInLoggableFormat(attributeNames);
        logger.info("Subscribed HLA object class <{}> attributes: {}.", this.name, loggableAttributeNames);
    }

    void unsubscribeAttributes(String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, AttributeNotDefined, ObjectClassNotDefined {
        this.attributes.clear();

        if (attributeNames.length > 1) {
            rtiAmbassador.unsubscribeObjectClass(this.handle);
        } else {
            for (String attributeName : attributeNames) {
                Attribute attribute = getAttribute(a -> a.name.equals(attributeName));

                if (attribute != null && attribute.subscribed.get()) {
                    attribute.subscribed.set(false);
                    this.attributes.add(attribute.handle);
                }
            }

            rtiAmbassador.unsubscribeObjectClassAttributes(this.handle, this.attributes);
        }

        String loggableAttributeNames = getNamesInLoggableFormat(attributeNames);
        logger.info("Unsubscribed HLA object class <{}> attributes: {}.", this.name, loggableAttributeNames);
    }

    private String getNamesInLoggableFormat(String... attributeNames) {
        StringBuilder sb = new StringBuilder("[ ");

        for (int i = 0; i < attributeNames.length; i++) {
            sb.append(attributeNames[i]);

            if (i != attributeNames.length - 1) {
                sb.append(", ");
            } else {
                sb.append(" ]");
            }
        }

        return sb.toString();
    }

    void updateAttributeValues(ObjectInstanceHandle instanceHandle, Object proxy, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, AttributeNotOwned, NotConnected, RTIinternalError, SaveInProgress, AttributeNotDefined, ObjectInstanceNotKnown {
        this.attributeValues.clear();

        if (attributeNames == null || attributeNames.length < 1) {
            for (Map.Entry<Attribute, Trait> entry : this.attributeToTrait.entrySet()) {
                Attribute attribute = entry.getKey();
                Trait t = entry.getValue();

                // Edge-case for the HLAprivilegeToDeleteObject attribute which has no corresponding trait in the map.
                if (t != null) {
                    byte[] encodedValue = t.encode(proxy);
                    this.attributeValues.put(attribute.handle, encodedValue);
                }
            }
        } else {
            for (String attributeName : attributeNames) {
                Attribute attribute = getAttribute(a -> a.name.equals(attributeName));
                if (attribute != null /* && attribute.published.get() */) {
                    Trait t = this.attributeToTrait.get(attribute);
                    byte[] encodedValue = t.encode(proxy);

                    this.attributeValues.put(attribute.handle, encodedValue);
                }
            }
        }

        rtiAmbassador.updateAttributeValues(instanceHandle, this.attributeValues, null);
    }

    void provideUpdate(ObjectInstanceHandle instanceHandle, Object proxy, AttributeHandleSet requestedAttributes) throws FederateNotExecutionMember, RestoreInProgress, AttributeNotOwned, NotConnected, RTIinternalError, SaveInProgress, AttributeNotDefined, ObjectInstanceNotKnown {
        this.attributeValues.clear();

        for (AttributeHandle attributeHandle : requestedAttributes) {
            Attribute attribute = getAttribute(a -> a.handle.equals(attributeHandle));
            if (attribute != null) {
                Trait t = this.attributeToTrait.get(attribute);
                byte[] encodedValue = t.encode(proxy);

                this.attributeValues.put(attribute.handle, encodedValue);
            }
        }

        rtiAmbassador.updateAttributeValues(instanceHandle, this.attributeValues, null);
    }

    void reflectRemoteUpdate(HLAObjectManager.ObjectInstance objectInstance, AttributeHandleValueMap attributeValues) {
        if (objectInstance.getProxy() == null) {
            Object proxy = createProxy();
            objectInstance.setProxy(proxy);
        }

        for (Map.Entry<AttributeHandle, byte[]> entry :  attributeValues.entrySet()) {
            Attribute attribute = getAttribute(a -> a.handle.equals(entry.getKey()));

            if (attribute != null) {
                Trait trait = this.attributeToTrait.get(attribute);
                Object[] traitValues = trait.decode(objectInstance.getProxy(), entry.getValue());

                objectInstance.notifyAllListeners(trait.getAnnotatedName(), traitValues[0], traitValues[1]);
            }
        }
    }

    String getName() {
        return this.name;
    }

    ObjectClassHandle getHandle() {
        return this.handle;
    }

    Class<?> getProxyClass() {
        return this.proxyClass;
    }

    Object createProxy() {
        try {
            return this.proxyClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not create object to manage values for remote object instance of the HLA object class <" + this.name + ">.", e);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("The class <" + this.proxyClass + "> assigned to the HLA object class <" + this.name + "> may lack a public constructor that accepts zero arguments.", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("The constructor for " + this.proxyClass + "threw an exception during instantiation.", e);
        }
    }

    private Attribute getAttribute(Predicate<Attribute> predicate) {
        return this.attributeToTrait.keySet()
                .stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    AttributeHandleSet getSubscribedAttributes() {
        this.attributes.clear();

        for (Attribute attribute : this.attributeToTrait.keySet()) {
            if (attribute.subscribed.get()) {
                this.attributes.add(attribute.handle);
            }
        }

        return this.attributes;
    }

    AttributeHandleSet getAttributeHandles(String... attributeNames) {
        this.attributes.clear();

        for (String attributeName : attributeNames) {
            Attribute attribute = getAttribute(a -> a.name.equals(attributeName));

            if (attribute != null) {
                this.attributes.add(attribute.handle);
            } else {
                logger.warn("The attribute <{}> is unknown for the object class <{}>.", attributeName, this.name);
            }
        }

        return this.attributes;
    }

    Set<String> getAttributeNames(AttributeHandleSet set) {
        Set<String> attributeNames = new HashSet<>();

        set.forEach(attributeHandle -> {
            Attribute attribute = getAttribute(a -> a.handle.equals(attributeHandle));

            if (attribute != null) {
                attributeNames.add(attribute.name);
            }
        });

        return attributeNames;
    }

    String getAttributeName(AttributeHandle attributeHandle) {
        Attribute attribute = getAttribute(a -> a.handle.equals(attributeHandle));
        return attribute == null ? null : attribute.name;
    }

    private static final class Attribute {

        private final String name;

        private final AttributeHandle handle;

        private final AtomicBoolean published;

        private final AtomicBoolean subscribed;

        Attribute(String name, AttributeHandle handle) {
            this.name = name;
            this.handle = handle;
            this.published = new AtomicBoolean(false);
            this.subscribed = new AtomicBoolean(false);
        }
    }

    static final class Builder {

        private ObjectClassHandle handle;

        private SKAnnotatedTypeParser.Metadata objectMetadata;

        Builder withHandle(ObjectClassHandle handle) {
            this.handle = handle;
            return this;
        }

        Builder withMetadata(SKAnnotatedTypeParser.Metadata objectMetadata) {
            this.objectMetadata = objectMetadata;
            return this;
        }

        HLAObjectClass build() throws FederateNotExecutionMember, NotConnected, RTIinternalError {
            if (this.handle == null || this.objectMetadata == null) {
                throw new InternalObjectBuilderException("Missing one or more arguments required to initialize internal framework object representing an HLA object class.");
            }

            return new HLAObjectClass(this);
        }
    }
}
