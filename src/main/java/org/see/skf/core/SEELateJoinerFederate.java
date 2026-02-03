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

import hla.rti1516_2025.RtiConfiguration;
import hla.rti1516_2025.exceptions.*;
import org.see.skf.conf.FederateConfiguration;
import org.see.skf.util.listeners.ExecutionConfigurationListener;
import org.see.skf.util.models.ExecutionConfiguration;
import org.see.skf.util.models.ModeTransitionRequest;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A complete SpaceFOM late-joiner implementation. It is recommended for student teams participating in the Simulation
 * Exploration Experience (SEE) program to use this implementation as the starting point for building federates.
 *
 * @since 2.0
 */
public abstract class SEELateJoinerFederate extends SEEAbstractFederate {
    protected SEELateJoinerFederate(SEEFederateAmbassador federateAmbassador, FederateConfiguration federateConfiguration) {
        super(federateAmbassador, federateConfiguration);
    }

    /**
     * Parses the configuration provided to the federate, joins the federation execution, and begins running in time
     * with the rest of the federation execution.
     */
    @Override
    public void configureAndStart() {
        try {
            RtiConfiguration rtiConfig = RtiConfiguration.createConfiguration().withRtiAddress(getConfiguration().rtiAddress());
            connectToRTI(rtiConfig);
            joinFederationExecution();

            subscribeObjectClass(ExecutionConfiguration.class);
            publishInteractionClass(ModeTransitionRequest.class);

            AtomicBoolean excoDiscovered = new AtomicBoolean(false);
            addRemoteObjectInstanceListener(new ExecutionConfigurationListener(this, excoDiscovered));

            while (!excoDiscovered.get()) {
                Thread.yield();
            }

            declareClasses();
            declareObjectInstances();

            setupTimeManagement();
            startExecution();
        } catch (RTIexception e) {
            throw new IllegalStateException("Failed to configure and initialize the federate <" + getConfiguration().federationName() + ">.", e);
        }
    }

    /**
     * Publish/subscribe the object and interaction classes that are relevant to the federate.
     * @throws FederateNotExecutionMember if the federate is not a member of the federation execution.
     * @throws AttributeNotDefined if the object class does not have a specified attribute.
     * @throws ObjectClassNotDefined if the object class is not defined in the FOM.
     * @throws RestoreInProgress if the federation execution is being restored at the time of declaring the class.
     * @throws NameNotFound if the name of the object/interaction class was not found.
     * @throws NotConnected if the federate is not connected to the RTI.
     * @throws RTIinternalError if an unknown internal error associated with the RTI is encountered.
     * @throws InvalidObjectClassHandle if the handle provided for the object class is invalid.
     * @throws SaveInProgress if the federation execution is in the process of undergoing a save operation at the time of declaring the class.
     * @throws InvalidInteractionClassHandle if the handle provided for the interaction class is invalid.
     * @throws InteractionClassNotDefined if the interaction class is not defined in the FOM.
     * @throws FederateServiceInvocationsAreBeingReportedViaMOM if the federate service invocation is reported via the Management Object Model (MOM).
     */
    public abstract void declareClasses() throws FederateNotExecutionMember, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, NameNotFound, NotConnected, RTIinternalError, InvalidObjectClassHandle, SaveInProgress, InvalidInteractionClassHandle, InteractionClassNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM;

    /**
     * Creates the object instances (and potentially reserves their names) for use by the federate.
     * @throws FederateNotExecutionMember if the federate is not a member of the federation execution.
     * @throws ObjectClassNotPublished if an attempt is made to create an instance of an object class that has yet to be published by the federate.
     * @throws ObjectClassNotDefined if the object class is not defined in the FOM.
     * @throws RestoreInProgress if the federation execution is being restored at the time of creating the object instance.
     * @throws ObjectInstanceNotKnown if an attempt to access an unknown/deleted object instance is made.
     * @throws IllegalName if an attempt is made to use disallowed name for an object instance.
     * @throws ObjectInstanceNameInUse if the federate attempts to use name that is being used by another federate for one of its object instances.
     * @throws ObjectInstanceNameNotReserved if the federate attempts to use a name that it has not yet been reserved.
     * @throws NotConnected if the federate is not connected to the RTI.
     * @throws RTIinternalError if an unknown internal error associated with the RTI is encountered.
     * @throws SaveInProgress if the federation execution is in the process of undergoing a save operation at the time of creating the object instance.
     */
    public abstract void declareObjectInstances() throws FederateNotExecutionMember, ObjectClassNotPublished, ObjectClassNotDefined, RestoreInProgress, ObjectInstanceNotKnown, IllegalName, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, NotConnected, RTIinternalError, SaveInProgress;
}
