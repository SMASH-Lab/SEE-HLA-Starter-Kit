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

/**
 * The base interface that for all Starter Kit federates. The primary provisions for federate implementations
 * are the management of objects and interactions.
 *
 * @since 2.0
 */
public interface SKFederateInterface {
    /**
     * Connect the federate to the RTI.
     * In HLA 4, the local_settings_designator string has been supplanted by the RTIConfiguration class that
     * encapsulates the information needed to connect to the RTI.
     * @param rtiConfig An RTI configuration object.
     */
    void connectToRTI(RtiConfiguration rtiConfig) throws CallNotAllowedFromWithinCallback, Unauthorized, RTIinternalError, ConnectionFailed, UnsupportedCallbackModel;

    /**
     * Join the federation execution.
     */
    void joinFederationExecution() throws ErrorReadingFOM, CouldNotCreateLogicalTimeFactory, RestoreInProgress, CallNotAllowedFromWithinCallback, InconsistentFOM, FederationExecutionDoesNotExist, Unauthorized, CouldNotOpenFOM, NotConnected, InvalidFOM, RTIinternalError, SaveInProgress, FederateNotExecutionMember, AsynchronousDeliveryAlreadyEnabled;

    /**
     * Gracefully resign from the federation execution.
     */
    void resignFederationExecution() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, CallNotAllowedFromWithinCallback, InvalidResignAction, OwnershipAcquisitionPending, FederateOwnsAttributes, FederateIsExecutionMember;

    /**
     * Declare to the RTI that attribute information about the instances of the specified HLA object class will be
     * published by the federate.
     * @param objectClass The Java class representation of the HLA object class.
     */
    void publishObjectClass(Class<?> objectClass) throws FederateNotExecutionMember, NameNotFound, NotConnected, RTIinternalError, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, SaveInProgress;

    /**
     * Declare to the RTI that all information about instances of a previously published object class will no longer
     * be sent by the federate.
     * Does nothing if the HLA object class has not been published by the federate beforehand.
     * @param objectClass The Java class representation of the HLA object class.
     * @see #publishObjectClass(Class)
     */
    void unpublishObjectClass(Class<?> objectClass) throws FederateNotExecutionMember, ObjectClassNotDefined, RestoreInProgress, OwnershipAcquisitionPending, NotConnected, RTIinternalError, SaveInProgress, AttributeNotDefined;

    /**
     * Declare to the RTI that the federate will subscribe to instance updates of the specified HLA object class.
     * @param objectClass The Java class representation of the HLA object class.
     */
    void subscribeObjectClass(Class<?> objectClass) throws FederateNotExecutionMember, NameNotFound, NotConnected, RTIinternalError, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, SaveInProgress;

    /**
     * Declare to the RTI that the federate wants to stop receiving updates about the instances of the specified HLA
     * object class. Does nothing is the object class was not subscribed to beforehand.
     * @param objectClass The Java class representation of the HLA object class.
     * @see #subscribeObjectClass(Class)
     */
    void unsubscribeObjectClass(Class<?> objectClass) throws FederateNotExecutionMember, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * Declare to the RTI that interactions of the specified HLA interaction class will be published by the federate.
     * @param interactionClass The Java class representation of the HLA interaction class.
     */
    void publishInteractionClass(Class<?> interactionClass) throws FederateNotExecutionMember, NameNotFound, NotConnected, RTIinternalError, InvalidInteractionClassHandle, RestoreInProgress, InteractionClassNotDefined, SaveInProgress;

    /**
     * Declare to the RTI that the federate will subscribe to interactions of the specified HLA interaction class.
     * Does nothing if the interaction class was not published beforehand.
     * @param interactionClass The Java class representation of the HLA interaction class.
     * @see #publishInteractionClass(Class)
     */
    void unpublishInteractionClass(Class<?> interactionClass) throws FederateNotExecutionMember, RestoreInProgress, InteractionClassNotDefined, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * Declare to the RTI that the federate wants to stop receiving interactions of the specified HLA interaction class.
     * Does nothing if the interaction class was not subscribed to beforehand.
     * @param interactionClass The Java class representation of the HLA interaction class.
     */
    void subscribeInteractionClass(Class<?> interactionClass) throws FederateNotExecutionMember, NameNotFound, NotConnected, RTIinternalError, InvalidInteractionClassHandle, RestoreInProgress, InteractionClassNotDefined, SaveInProgress, FederateServiceInvocationsAreBeingReportedViaMOM;

    /**
     * Declare to the RTI that interactions of a previously published HLA interaction class will no longer be sent
     * by the federate.
     * @param interactionClass The Java class representation of the HLA interaction class.
     */
    void unsubscribeInteractionClass(Class<?> interactionClass) throws FederateNotExecutionMember, RestoreInProgress, InteractionClassNotDefined, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * Create an instance of an HLA object class that will be published by the federate.
     * <br><br>
     * The HLA object class that this instance belongs to must have been previously declared as publishable to the RTI.
     * @param objectInstance An object with attributes aligned with the attributes of its HLA object class
     *                       equivalent in the FOM.
     * @return The name of the object instance assigned by the RTI.
     * @see #publishObjectClass(Class)
     */
    String registerObjectInstance(Object objectInstance) throws FederateNotExecutionMember, ObjectClassNotPublished, ObjectClassNotDefined, RestoreInProgress, ObjectInstanceNotKnown, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * Create a named instance of an HLA object class that will be published by the federate. It should be noted that
     * there is no guarantee the federate will be able to reserve the provided name. If this happens, the RTI will
     * assign a unique name of its own, which wil be returned by the method for your reference.
     * <br><br>
     * The HLA object class that this instance belongs to must have been previously declared as publishable to the RTI.
     * @param objectInstance An object with attributes aligned with the attributes of its HLA object class
     *                       equivalent in the FOM.
     * @param requestedName The requestedName for the object instance.
     * @return The name of the object instance at the RTI.
     * @see #publishObjectClass(Class)
     */
    String registerObjectInstance(Object objectInstance, String requestedName) throws FederateNotExecutionMember, RestoreInProgress, IllegalName, NotConnected, RTIinternalError, SaveInProgress, ObjectClassNotPublished, ObjectClassNotDefined, ObjectInstanceNotKnown, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved;

    /**
     * Issue an update in the instance's attributes to the RTI for other federates to receive. The HLA object class
     * must have already been declared as publishable by the federate to the RTI beforehand AND the object must have
     * been previously created.
     * @param objectInstance The object instance representation to be updated.
     * @see #publishObjectClass(Class)
     * @see #registerObjectInstance(Object)
     */
    void updateObjectInstance(Object objectInstance);

    /**
     * Delete the object instance from the RTI. The HLA object class that the instance belongs to must have already been
     * declared as publishable by the federate to the RTI beforehand AND the object must have previously been created.
     * It is possible to choose whether to release the name of the object instance if it uses a custom name that had to
     * be reserved at the time of creation.
     * @param objectInstance The object instance representation to be deleted.
     * @param relinquishNameReservation true/false to release ownership of the instance name if reserved.
     * @see #publishObjectClass(Class)
     * @see #registerObjectInstance(Object)
     */
    void deleteObjectInstance(Object objectInstance, boolean relinquishNameReservation) throws FederateNotExecutionMember, RestoreInProgress, ObjectInstanceNotKnown, DeletePrivilegeNotHeld, NotConnected, RTIinternalError, SaveInProgress, ObjectInstanceNameNotReserved;

    /**
     * Finds the representation of a remote object instance if previously discovered and stored by the federate.
     *
     * @param instanceName Name of the object instance to look for.
     * @return The object representing the HLA object instance or null if not found.
     */
    Object queryRemoteObjectInstance(String instanceName);

    /**
     * Send an interaction of an HLA interaction class that has been previously declared as publishable by the federate
     * to the RTI. The HLA interaction class must have already been declared as publishable by the federate to the RTI
     * beforehand.
     * @param interaction An object with attributes aligned with the parameters of its HLA interaction class
     *                    equivalent in the FOM.
     * @return true/false depending on the operation's success.
     * @see #publishInteractionClass(Class)
     */
    boolean sendInteraction(Object interaction) throws FederateNotExecutionMember, InteractionParameterNotDefined, RestoreInProgress, InteractionClassNotDefined, InteractionClassNotPublished, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * Register a remote object instance listener to be notified about the initialization of remote object instances
     * created by another federate.
     * @param listener The remote object instance listener to be added.
     */
    void addRemoteObjectInstanceListener(RemoteObjectInstanceListener listener);

    /**
     * Register a remote object instance listener to be notified about the deletion of remote object instances.
     * @param listener The remote object instance listener to be removed.
     */
    void removeRemoteObjectInstanceListener(RemoteObjectInstanceListener listener);

    /**
     * Register an interaction listener to be notified about the dispatch of interactions in the federation
     * execution.
     * @param listener The interaction listener.
     */
    void addInteractionListener(InteractionListener listener);

    /**
     * Remove a previously registered interaction listener to stop receiving events when interactions are sent.
     * @param listener The interaction listener.
     */
    void removeInteractionListener(InteractionListener listener);
}