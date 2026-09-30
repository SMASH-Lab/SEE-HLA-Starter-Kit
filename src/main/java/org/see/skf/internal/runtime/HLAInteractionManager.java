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
import hla.rti1516_2025.ParameterHandleValueMap;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.internal.HLAUtilityFactory;
import org.see.skf.core.InteractionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class HLAInteractionManager {

    private static final Logger logger = LoggerFactory.getLogger(HLAInteractionManager.class);

    private final RTIambassador rtiAmbassador;

    private final SKAnnotatedTypeParser parser;

    private final Set<HLAInteractionClass> interactionClasses;
    private final Set<InteractionListener> interactionListeners;

    public HLAInteractionManager(SKAnnotatedTypeParser parser) {
        rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.parser = parser;

        this.interactionClasses = new CopyOnWriteArraySet<>();
        this.interactionListeners = new CopyOnWriteArraySet<>();
    }

    private HLAInteractionClass getInteractionClass(InteractionClassHandle handle) {
        return this.interactionClasses.stream()
                .filter(i -> i.getHandle().equals(handle))
                .findFirst()
                .orElse(null);
    }

    private HLAInteractionClass getInteractionClass(Class<?> proxyClass) {
        return this.interactionClasses.stream()
                .filter(i -> i.getProxyClass().equals(proxyClass))
                .findFirst()
                .orElse(null);
    }

    private HLAInteractionClass getInteractionClass(String name) {
        return this.interactionClasses.stream()
                .filter(i -> i.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private HLAInteractionClass createInteractionClass(Class<?> proxyClass) throws FederateNotExecutionMember, NotConnected, RTIinternalError {
        SKAnnotatedTypeParser.Metadata proxyMetadata = this.parser.parseInteractionProxy(proxyClass);
        String className = proxyMetadata.getFomClassName();

        InteractionClassHandle handle;
        try {
            handle = rtiAmbassador.getInteractionClassHandle(className);
        } catch (NameNotFound e) {
            throw new IllegalArgumentException("The HLA interaction class <" + className + "> is not defined in any FOM modules currently being used in this federation execution.");
        }

        return new HLAInteractionClass.Builder()
                .withHandle(handle)
                .withMetadata(proxyMetadata)
                .build();
    }

    public void publishInteractionClass(Class<?> proxyClass) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress {
        if (proxyClass == null) {
            throw new IllegalArgumentException("Class representing how the data of the HLA interaction class should be interpreted by the federate cannot be NULL.");
        }

        HLAInteractionClass interactionClass;
        if ((interactionClass = getInteractionClass(proxyClass)) == null) {
            interactionClass = createInteractionClass(proxyClass);
            this.interactionClasses.add(interactionClass);
        }

        interactionClass.publish();
    }

    public void unpublishInteractionClass(String name) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        HLAInteractionClass interactionClass;
        if ((interactionClass = getInteractionClass(name)) != null) {
            interactionClass.unpublish();
        }
    }

    public void subscribeInteractionClass(Class<?> proxyClass) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, FederateServiceInvocationsAreBeingReportedViaMOM, SaveInProgress {
        HLAInteractionClass interactionClass;
        if ((interactionClass = getInteractionClass(proxyClass)) == null) {
            interactionClass = createInteractionClass(proxyClass);
            this.interactionClasses.add(interactionClass);
        }

        interactionClass.subscribe();
    }

    public void unsubscribeInteractionClass(String name) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        HLAInteractionClass interactionClass;
        if ((interactionClass = getInteractionClass(name)) != null) {
            interactionClass.unsubscribe();
        }
    }

    public void sendInteraction(Object proxy) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined {
        if (proxy == null) {
            throw new IllegalArgumentException("The object representing the interaction to be sent must not be null.");
        }

        Class<?> proxyClass = proxy.getClass();
        HLAInteractionClass interactionClass = getInteractionClass(proxyClass);
        if (interactionClass == null) {
            String interactionClassName = this.parser.getInteractionProxyFomName(proxyClass);
            throw new IllegalArgumentException("Cannot send interaction because the class <" + interactionClassName + "> has not been published yet.");
        } else {
            interactionClass.send(proxy);
        }
    }

    public void interactionReceived(InteractionClassHandle handle, ParameterHandleValueMap parameterValues, String producingFederateName) {
        HLAInteractionClass interactionClass = getInteractionClass(handle);

        if (interactionClass != null) {
            Object proxy = interactionClass.receive(parameterValues);
            this.interactionListeners.forEach(listener -> listener.received(proxy, producingFederateName));
        } else {
            logger.warn("Missed interaction from <{}> as no interaction class is known internally by the federate to deserialize the values received.", producingFederateName);
        }
    }

    public void addInteractionListener(InteractionListener listener) {
        if (listener != null) {
            this.interactionListeners.add(listener);
        }
    }

    public void removeInteractionListener(InteractionListener listener) {
        if (listener != null) {
            this.interactionListeners.remove(listener);
        }
    }
}
