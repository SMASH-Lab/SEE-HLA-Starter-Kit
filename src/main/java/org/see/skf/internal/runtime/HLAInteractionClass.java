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

import hla.rti1516_2025.InteractionClassHandle;
import hla.rti1516_2025.ParameterHandle;
import hla.rti1516_2025.ParameterHandleValueMap;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.internal.HLAUtilityFactory;
import org.see.skf.internal.InternalObjectBuilderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

final class HLAInteractionClass {

    private static final Logger logger = LoggerFactory.getLogger(HLAInteractionClass.class);

    private final RTIambassador rtiAmbassador;

    private final InteractionClassHandle handle;

    private final String name;

    private final Class<?> proxyClass;

    private final Map<Parameter, Trait> parameterToTrait;

    private final ParameterHandleValueMap parameterValues;

    private final AtomicBoolean published;

    private final AtomicBoolean subscribed;

    HLAInteractionClass(Builder builder) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();

        this.handle = builder.handle;
        this.name = builder.objectMetadata.getFomClassName();
        this.proxyClass = builder.objectMetadata.getProxyClass();
        this.parameterToTrait = computeParameterToTraitAssociation(builder.objectMetadata.getTraits());
        this.parameterValues = rtiAmbassador.getParameterHandleValueMapFactory().create(this.parameterToTrait.size());

        this.published = new AtomicBoolean(false);
        this.subscribed = new AtomicBoolean(false);
    }

    private Map<Parameter, Trait> computeParameterToTraitAssociation(Set<Trait> traits) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        // N.B. A standard HashMap will suffice since no new write operations will occur again as parameters are FIXED.
        Map<Parameter, Trait> map = new HashMap<>();

        for (Trait trait : traits) {
            String parameterName = trait.getAnnotatedName();
            try {
                ParameterHandle parameterHandle = rtiAmbassador.getParameterHandle(this.handle, parameterName);
                Parameter parameter = new Parameter(parameterName, parameterHandle);

                map.put(parameter, trait);
            } catch (InvalidInteractionClassHandle e) {
                throw new RtiHandleAcquisitionException(e);
            } catch (NameNotFound e) {
                throw new RtiHandleAcquisitionException("The parameter <" + parameterName + "> is not defined for the HLA interaction class <" + this.name + ">.");
            }
        }

        return map;
    }

    void publish() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (published.get()) {
            return;
        }

        try {
            rtiAmbassador.publishInteractionClass(this.handle);
        } catch (InteractionClassNotDefined e) {
            // Highly unlikely to occur as we've already taken care of this during initialization.
            throw new RuntimeException(e);
        }

        logger.info("Published HLA interaction class <{}>.", this.name);
        this.published.set(true);
    }

    void unpublish() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (!published.get()) {
            return;
        }

        try {
            rtiAmbassador.unpublishInteractionClass(this.handle);
        } catch (InteractionClassNotDefined e) {
            throw new RuntimeException(e);
        }

        logger.info("Unpublished HLA interaction class <{}>.", this.name);
        this.published.set(false);
    }

    void subscribe() throws FederateNotExecutionMember, RestoreInProgress, FederateServiceInvocationsAreBeingReportedViaMOM, NotConnected, RTIinternalError, SaveInProgress {
        if (subscribed.get()) {
            return;
        }

        try {
            rtiAmbassador.subscribeInteractionClass(this.handle);
        } catch (InteractionClassNotDefined e) {
            throw new RuntimeException(e);
        }

        logger.info("Subscribed HLA interaction class <{}>.", this.name);
        this.subscribed.set(true);
    }

    void unsubscribe() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (!subscribed.get()) {
            return;
        }

        try {
            rtiAmbassador.unsubscribeInteractionClass(this.handle);
        } catch (InteractionClassNotDefined e) {
            throw new RuntimeException(e);
        }

        logger.info("Unsubscribed HLA interaction class <{}>.", this.name);
        this.subscribed.set(false);
    }

    private ParameterHandleValueMap serialize(Object forObject, ParameterHandleValueMap map) {
        for (Map.Entry<Parameter, Trait> entry : this.parameterToTrait.entrySet()) {
            Parameter parameter = entry.getKey();
            Trait trait = entry.getValue();

            map.put(parameter.handle, trait.encode(forObject));
        }

        return map;
    }

    void send(Object proxy) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress, InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined {
        ParameterHandleValueMap map = serialize(proxy, this.parameterValues);

        rtiAmbassador.sendInteraction(this.handle, map, null);
        logger.debug("Sent an interaction of class <{}>.", this.name);

        this.parameterValues.clear();
    }

    Object receive(ParameterHandleValueMap map) {
        Object proxy = createProxy();

        for (Map.Entry<ParameterHandle, byte[]> entry : map.entrySet()) {
            Parameter parameter = getParameter(entry.getKey());

            if (parameter != null) {
                Trait t = this.parameterToTrait.get(parameter);
                t.decode(proxy, entry.getValue());
            }
        }

        return proxy;
    }

    private Parameter getParameter(ParameterHandle parameterHandle) {
        return this.parameterToTrait.keySet()
                .stream()
                .filter(p -> p.getHandle().equals(parameterHandle))
                .findFirst()
                .orElse(null);
    }

    String getName() {
        return this.name;
    }

    InteractionClassHandle getHandle() {
        return this.handle;
    }

    Class<?> getProxyClass() {
        return this.proxyClass;
    }

    Object createProxy() {
        try {
            return this.proxyClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not create object to deserialize incoming interaction data for the HLA interaction class <" + this.name + ">.", e);
        }  catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("The class " + this.proxyClass + "> assigned to the HLA interaction class <" + this.name  + "> may lack a public constructor that accepts zero arguments.", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("The constructor for " + this.proxyClass + "threw an exception during instantiation.", e);
        }
    }

    private static class Parameter {

        private final String name;

        private final ParameterHandle handle;

        Parameter(String name, ParameterHandle handle) {
            this.name = name;
            this.handle = handle;
        }

        public String getName() {
            return name;
        }

        public ParameterHandle getHandle() {
            return handle;
        }
    }

    static final class Builder {

        private InteractionClassHandle handle;

        private SKAnnotatedTypeParser.Metadata objectMetadata;

        Builder withHandle(InteractionClassHandle handle) {
            this.handle = handle;
            return this;
        }

        Builder withMetadata(SKAnnotatedTypeParser.Metadata objectMetadata) {
            this.objectMetadata = objectMetadata;
            return this;
        }

        HLAInteractionClass build() throws FederateNotExecutionMember, NotConnected, RTIinternalError {
            if (this.handle == null || this.objectMetadata == null) {
                throw new InternalObjectBuilderException("Missing one or more arguments required to initialize internal framework object representing an HLA interaction class.");
            }

            return new HLAInteractionClass(this);
        }
    }
}
