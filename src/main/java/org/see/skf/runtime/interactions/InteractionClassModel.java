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

package org.see.skf.runtime.interactions;

import hla.rti1516_2025.InteractionClassHandle;
import hla.rti1516_2025.ParameterHandle;
import hla.rti1516_2025.ParameterHandleValueMap;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.HLAUtilityFactory;
import org.see.skf.runtime.AbstractClassModel;
import org.see.skf.runtime.DeclarationStatus;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class InteractionClassModel extends AbstractClassModel {
    private final RTIambassador rtiAmbassador;
    private final InteractionClassHandle handle;

    private final Map<String, ParameterHandle> parameterNameToHandle;
    private final Map<ParameterHandle, String> parameterHandleToName;

    private final InteractionClassModelParser parser;

    public InteractionClassModel(InteractionClassModelParser parser, InteractionClassHandle handle) throws FederateNotExecutionMember, InvalidInteractionClassHandle, NameNotFound, NotConnected, RTIinternalError {
        rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.handle = handle;
        this.parameterNameToHandle = new HashMap<>();
        this.parameterHandleToName = new HashMap<>();
        this.parser = parser;

        Set<Field> fomFields = parser.getAllFields();

        try {
            for (Field fomField : fomFields) {
                String fieldName = parser.getFomElementNameForField(fomField);
                ParameterHandle parameterHandle = rtiAmbassador.getParameterHandle(handle, fieldName);
                parameterNameToHandle.put(fieldName, parameterHandle);
                parameterHandleToName.put(parameterHandle, fieldName);
            }
        } catch (NameNotFound e) {
            throw new IllegalStateException("Failed to procure a parameter handle for the interaction class <" + getName() + ">. Parameter names match the one specified in the FOM.", e);
        }
    }

    public void publish() throws FederateNotExecutionMember, RestoreInProgress, InteractionClassNotDefined, NotConnected, RTIinternalError, SaveInProgress {
        DeclarationStatus declarationStatus = getDeclarationStatus();
        if (declarationStatus != DeclarationStatus.PUBLISHED && declarationStatus != DeclarationStatus.PUBLISHED_SUBSCRIBED) {
            rtiAmbassador.publishInteractionClass(handle);
            setModelDeclarationStatus(DeclarationStatus.setPublishFlag(declarationStatus));
            logger.info("The HLA interaction class <{}> was published.", getName());
        } else {
            logger.warn("A duplicate attempt to publish the HLA interaction class <{}> was made", getName());
        }
    }

    public void unpublish() throws FederateNotExecutionMember, RestoreInProgress, InteractionClassNotDefined, NotConnected, RTIinternalError, SaveInProgress {
        DeclarationStatus declarationStatus = getDeclarationStatus();

        if (declarationStatus != DeclarationStatus.SUBSCRIBED && declarationStatus != DeclarationStatus.UNDESIGNATED) {
            rtiAmbassador.unpublishInteractionClass(handle);
            setModelDeclarationStatus(DeclarationStatus.unsetPublishFlag(declarationStatus));

            logger.info("The HLA interaction class <{}> was unpublished.", getName());
        } else {
            logger.warn("Cannot unpublish the HLA interaction class <{}> because it has not yet been published by this federate.", getName());
        }
    }

    public void subscribe() throws FederateNotExecutionMember, RestoreInProgress, InteractionClassNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM, NotConnected, RTIinternalError, SaveInProgress {
        DeclarationStatus declarationStatus = getDeclarationStatus();
        if (declarationStatus != DeclarationStatus.SUBSCRIBED && declarationStatus != DeclarationStatus.PUBLISHED_SUBSCRIBED) {
            rtiAmbassador.subscribeInteractionClass(handle);
            setModelDeclarationStatus(DeclarationStatus.setSubscribeFlag(declarationStatus));
            logger.info("The HLA interaction class <{}> was subscribed.", getName());
        } else {
            logger.warn("A duplicate attempt to subscribe to the HLA interaction class <{}> was made.", getName());
        }
    }

    public void unsubscribe() throws FederateNotExecutionMember, RestoreInProgress, InteractionClassNotDefined, NotConnected, RTIinternalError, SaveInProgress {
        DeclarationStatus declarationStatus = getDeclarationStatus();

        if (declarationStatus != DeclarationStatus.PUBLISHED && declarationStatus != DeclarationStatus.UNDESIGNATED) {
            rtiAmbassador.unsubscribeInteractionClass(handle);
            setModelDeclarationStatus(DeclarationStatus.unsetSubscribeFlag(declarationStatus));

            logger.info("The HLA interaction class <{}> was unsubscribed.", getName());
        } else {
            logger.warn("Cannot unsubscribe the HLA interaction class <{}> because it has not yet been subscribed by this federate.", getName());
        }
    }

    public ParameterHandleValueMap getEncodedParameterValues(Object element) {
        int mapSize = parser.getParameterCount();
        try {
            ParameterHandleValueMap parameterHandleToValue = rtiAmbassador.getParameterHandleValueMapFactory().create(mapSize);
            Map<String, byte[]> encodingMap = parser.encode(element);

            for (var entry : encodingMap.entrySet()) {
                ParameterHandle parameterHandle = parameterNameToHandle.get(entry.getKey());
                parameterHandleToValue.put(parameterHandle, entry.getValue());
            }

            return parameterHandleToValue;
        } catch (FederateNotExecutionMember | NotConnected e) {
            throw new IllegalStateException("Failed to build encoded representation of parameter values for an interaction of the class <" + getName() + ">.");
        }
    }

    public void unpackEncodedParameterValues(Object element, ParameterHandleValueMap parameterHandleToValue) {
        parser.decode(element, parameterHandleToValue, parameterHandleToName);
    }

    public InteractionClassHandle getHandle() {
        return handle;
    }

    public Class<?> getInteractionClass() {
        return parser.getFomClass();
    }

    @Override
    public String getName() {
        return parser.getFomClassName();
    }
}
