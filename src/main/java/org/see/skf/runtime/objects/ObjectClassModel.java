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

package org.see.skf.runtime.objects;

import hla.rti1516_2025.*;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.HLAUtilityFactory;
import org.see.skf.runtime.AbstractClassModel;
import org.see.skf.runtime.DeclarationStatus;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ObjectClassModel extends AbstractClassModel {
    private final RTIambassador rtiAmbassador;
    private final ObjectClassHandle handle;
    private AttributeHandleSet publicationSet;
    private AttributeHandleSet subscriptionSet;

    private final Map<String, AttributeHandle> attributeNameToHandle;
    private final Map<AttributeHandle, String> attributeHandleToName;

    private final ObjectClassModelParser parser;

    public ObjectClassModel(ObjectClassModelParser parser, ObjectClassHandle handle) throws FederateNotExecutionMember, NotConnected, RTIinternalError, InvalidObjectClassHandle {
        rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.handle = handle;
        this.attributeNameToHandle = new HashMap<>();
        this.attributeHandleToName = new HashMap<>();
        this.parser = parser;

        Set<Field> fomFields = parser.getAllFields();

        try {
            for (Field fomField : fomFields) {
                String fieldName = parser.getFomElementNameForField(fomField);
                AttributeHandle attributeHandle = rtiAmbassador.getAttributeHandle(handle, fieldName);
                attributeNameToHandle.put(fieldName, attributeHandle);
                attributeHandleToName.put(attributeHandle, fieldName);
            }
        } catch (NameNotFound e) {
            throw new IllegalStateException("Failed to procure an attribute handle for the object class <" + getName() + ">. Attribute names must match the one specified in the FOM.", e);
        }
    }

    public void publish() throws FederateNotExecutionMember, NotConnected, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, RTIinternalError, SaveInProgress {
        if (!parser.isPublishable()) {
            logger.warn("The HLA object class <{}> was not published because it has no attributes marked as publishable.", getName());
            return;
        }

        DeclarationStatus declarationStatus = getDeclarationStatus();
        if (declarationStatus != DeclarationStatus.PUBLISHED && declarationStatus != DeclarationStatus.PUBLISHED_SUBSCRIBED) {
            Set<String> publishableAttributeNames = parser.getPublishableAttributeNames();

            // Prior publications may already have initialized the set.
            if (publicationSet == null) {
                this.publicationSet = rtiAmbassador.getAttributeHandleSetFactory().create();

                for (String attributeName : publishableAttributeNames) {
                    AttributeHandle attributeHandle = attributeNameToHandle.get(attributeName);
                    publicationSet.add(attributeHandle);
                }
            }

            rtiAmbassador.publishObjectClassAttributes(handle, publicationSet);

            setModelDeclarationStatus(DeclarationStatus.setPublishFlag(declarationStatus));
            logger.info("The HLA object class <{}> was published.", getName());
        } else {
            logger.warn("A duplicate attempt to publish the HLA object class <{}> was made", getName());
        }
    }

    public void unpublish() throws FederateNotExecutionMember, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, OwnershipAcquisitionPending, NotConnected, RTIinternalError, SaveInProgress {
        DeclarationStatus declarationStatus = getDeclarationStatus();

        if (declarationStatus != DeclarationStatus.SUBSCRIBED && declarationStatus != DeclarationStatus.UNDESIGNATED) {
            rtiAmbassador.unpublishObjectClassAttributes(handle, publicationSet);
            setModelDeclarationStatus(DeclarationStatus.unsetPublishFlag(declarationStatus));

            logger.info("The HLA object class <{}> was unpublished.", getName());
        } else {
            logger.warn("Cannot unpublish the HLA object class <{}> because it has not yet been published by this federate.", getName());
        }
    }

    public void subscribe() throws FederateNotExecutionMember, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (!parser.isSubscribable()) {
            logger.warn("The HLA object class <{}> was not subscribed because it has no attributes marked as subscribable.", getName());
            return;
        }

        DeclarationStatus declarationStatus = getDeclarationStatus();
        if (declarationStatus != DeclarationStatus.SUBSCRIBED && declarationStatus != DeclarationStatus.PUBLISHED_SUBSCRIBED) {
            Set<String> subscribableAttributeNames = parser.getSubscribableAttributeNames();

            // Prior subscriptions may already have initialized the set.
            if (subscriptionSet == null) {
                this.subscriptionSet = rtiAmbassador.getAttributeHandleSetFactory().create();

                for (String attributeName : subscribableAttributeNames) {
                    AttributeHandle attributeHandle = attributeNameToHandle.get(attributeName);
                    subscriptionSet.add(attributeHandle);
                }
            }

            rtiAmbassador.subscribeObjectClassAttributes(handle, subscriptionSet);
            setModelDeclarationStatus(DeclarationStatus.setSubscribeFlag(declarationStatus));
            logger.info("The HLA object class <{}> was subscribed.", getName());
        } else {
            logger.warn("A duplicate attempt to subscribe to the HLA object class <{}> was made.", getName());
        }
    }

    public void unsubscribe() throws FederateNotExecutionMember, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        DeclarationStatus declarationStatus = getDeclarationStatus();

        if (declarationStatus != DeclarationStatus.PUBLISHED && declarationStatus != DeclarationStatus.UNDESIGNATED) {
            rtiAmbassador.unsubscribeObjectClassAttributes(handle, subscriptionSet);
            setModelDeclarationStatus(DeclarationStatus.unsetSubscribeFlag(declarationStatus));

            logger.info("The HLA object class <{}> was unsubscribed.", getName());
        } else {
            logger.warn("Cannot unsubscribe the HLA object class <{}> because it has not yet been subscribed by this federate.", getName());
        }
    }

    public ObjectClassHandle getHandle() {
        return handle;
    }

    @Override
    public String getName() {
        return parser.getFomClassName();
    }

    public Class<?> getObjectClass() {
        return parser.getFomClass();
    }

    public AttributeHandleSet getPublicationSet() {
        return publicationSet;
    }

    public AttributeHandleSet getSubscriptionSet() {
        return subscriptionSet;
    }

    public AttributeHandleValueMap getEncodedAttributeValues(Object element) {
        int mapSize = parser.getPublishableAttributesCount();
        try {
            AttributeHandleValueMap attributeHandleToValue = rtiAmbassador.getAttributeHandleValueMapFactory().create(mapSize);
            Map<String, byte[]> encodingMap = parser.encode(element);

            for (var entry : encodingMap.entrySet()) {
                AttributeHandle attributeHandle = attributeNameToHandle.get(entry.getKey());
                attributeHandleToValue.put(attributeHandle, entry.getValue());
            }

            return attributeHandleToValue;
        } catch (FederateNotExecutionMember | NotConnected e) {
            throw new IllegalStateException("Failed to build encoded representation of attribute values for an object instance of the HLA object class <" + getName() + ">.");
        }
    }

    public void unpackEncodedAttributeValues(Object element, AttributeHandleValueMap attributeHandleToValue) {
        parser.decode(element, attributeHandleToValue, attributeHandleToName);
    }
}
