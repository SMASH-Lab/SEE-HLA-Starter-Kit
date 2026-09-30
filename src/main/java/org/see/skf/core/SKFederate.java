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

package org.see.skf.core;

import hla.rti1516_2025.exceptions.*;

import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * <p>
 * The root interface for all SEE HLA Starter Kit federates. The interface wraps around many of the methods provided by the
 * RTIAmbassador class and additionally offers specific functionality such as managing object instances and callback listeners.
 * </p>
 *
 * <p>
 * The framework includes two implementations: {@link SKAbstractFederate} and {@link SEEAbstractFederate}. SEE teams are recommended
 * to use the {@link SEEAbstractFederate} class as the starting point for their federates as it is a fully SRFOM-compliant late
 * joiner implementation.
 * </p>
 *
 * @since 2.0
 */
public interface SKFederate {

    /**
     * Loads federate configuration parameters and begins execution. Implementing classes should include all
     * initialization-related operations here.
     */
    void configureAndStart() throws RTIexception;

    /**
     * Connects a federate to the RTI.
     */
    void connectToRti() throws Unauthorized, ConnectionFailed;

    /**
     * Joins this federate to the specified federation execution and adds any FOM modules found to the federation object model  (FOM).
     */
    void joinFederationExecution() throws RestoreInProgress, Unauthorized, NotConnected, RTIinternalError, SaveInProgress, FederateNotExecutionMember;

    /**
     * Terminates the federate execution.
     */
    void shutdownExecution() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * Publishes a set of attributes on an object class, making it possible to register object instances of this object
     * class. The special attribute HLAprivilegeToDeleteObject will be automatically published if other attributes are
     * published on this class.
     *
     * @param clazz The type used to represent instances of this object class
     * @param attributeNames Names of the attributes of this object class that need to be published
     */
    void publishObjectClass(Class<?> clazz, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress, AttributeNotDefined, ObjectClassNotDefined;

    /**
     * Unpublishes the specified attributes on the specified object class. If no attribute names are provided, then all
     * attributes will be unpublished. All specified attributes on object instances whose known class is the specified
     * object class will immediately become unowned.
     *
     * @param name Name of the object class to be unpublished
     * @param attributeNames Names of the attributes of this object class that need to be unpublished
     */
    void unpublishObjectClass(String name, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, OwnershipAcquisitionPending, NotConnected, RTIinternalError, SaveInProgress, ObjectClassNotDefined, AttributeNotDefined;

    /**
     * Subscribes to the specified attributes on the specified  object class. The federate will discover object instances
     * registered using the specified class. The federate will be notified of discovery via any
     * {@link ObjectInstanceListener} listeners that have been added.
     *
     * @param clazz The type used to represent instances of this object class
     * @param attributeNames Names of the attributes of this object class that need to be subscribed
     */
    void subscribeObjectClass(Class<?> clazz, String... attributeNames) throws FederateNotExecutionMember, NotConnected, RTIinternalError, RestoreInProgress, SaveInProgress, AttributeNotDefined, ObjectClassNotDefined;

    /**
     * Notifies the RTI that the federate is no longer interested in the specified attributes on the specified object
     * class. If no attribute names are provided, then all attributes will be unsubscribed.
     *
     * @param name Name of the object class to be unsubscribed
     * @param attributeNames Names of the attributes of this object class that need to be unsubscribed
     */
    void unsubscribeObjectClass(String name, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, AttributeNotDefined, ObjectClassNotDefined;

    /**
     * Creates an unnamed HLA object instance using the {@link org.see.skf.core.annotations.ObjectClass} annotation attached to the
     * provided object.
     *
     * @param objectInstance The object to be used to represent this HLA object instance
     * @return The name assigned to the resulting object instance supplied by the RTI
     */
    String createObjectInstance(Object objectInstance) throws FederateNotExecutionMember, ObjectClassNotPublished, ObjectClassNotDefined, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * Creates a named HLA object instance using the {@link org.see.skf.core.annotations.ObjectClass} annotation attached
     * to the provided object. Since name reservation requires an RTI callback that is not immediately delivered, this
     * operation does not finish in the same time step. Use {@link Future#get()} or {@link Future#isDone()} on the returned
     * Future object to ensure that it has completed without incident before performing any object instance-related
     * operations.
     *
     * @param objectInstance The object to be used to represent this HLA object instance
     * @param name Name to be assigned to the object instance
     * @return A future object for tracking the completion of this operation
     */
    Future<Void> createObjectInstance(Object objectInstance, String name);

    /**
     * <p>
     * Updates the specified attributes on the specified object instance. The updated values will be delivered to
     * subscribing federates. The federate must own all the specified attributes. The values are translated into raw byte
     * arrays through the coders assigned to the {@link org.see.skf.core.annotations.Attribute} annotated fields of the
     * object.
     * </p>
     *
     * <p>The absence of any attributes supplied to this method will elicit an update for all the object's values.</p>
     *
     * @param objectInstance The object that is being used to represent this HLA object instance
     * @param attributes Names of the attributes to be updated
     */
    void updateObjectInstance(Object objectInstance, String... attributes) throws FederateNotExecutionMember, RestoreInProgress, AttributeNotOwned, NotConnected, RTIinternalError, SaveInProgress, AttributeNotDefined, ObjectInstanceNotKnown;

    /**
     * <p>
     * Deletes the specified object instance. Federates that have discovered this object instance are notified of the object
     * instance's deletion.
     * </p>
     *
     * <p>After this call, the object instance is no longer available for updates or other operations.</p>
     * @param objectInstance The object that is being used to represent this HLA object instance
     */
    void destroyObjectInstance(Object objectInstance) throws FederateNotExecutionMember, RestoreInProgress, DeletePrivilegeNotHeld, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * Returns if the specified object instance has been discovered by the federate.
     *
     * @param name Name of the object instance
     */
    boolean isObjectInstanceDiscovered(String name);

    /**
     * Searches for an object instance that exists at the federate.
     *
     * @param name Name of the object instance to query
     * @return An object representing the requested object instance if it exists or null if nothing was found
     */
    Object queryObjectInstance(String name);

    /**
     * Requests the RTI for the latest attribute value updates pertaining to the specified object instance.
     *
     * @param name Name of the object instance
     * @param attributeNames Names of the attributes to requests
     */
    void requestObjectInstanceUpdates(String name, String... attributeNames) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * Adds a {@link ObjectInstanceListener} object to the federate for processing callbacks relating to object instance
     * discovery and deletion.
     *
     * @param objectInstanceName Name of the object instance
     * @param listener The listener object
     */
    void addObjectInstanceListener(String objectInstanceName, ObjectInstanceListener listener);

    /**
     * Removes the provided {@link ObjectInstanceListener} object if it was previously added to the federate.
     *
     * @param listener The listener object
     */
    void removeObjectInstanceListener(ObjectInstanceListener listener);

    /**
     * Adds a {@link PropertyChangeListener} object to the federate for processing updates affecting the attributes of the
     * specified remote object instance.
     *
     * @param objectInstance The object representing the HLA object instance
     * @param listener The listener object
     */
    void addPropertyChangeListener(Object objectInstance, PropertyChangeListener listener);

    /**
     * Adds a {@link PropertyChangeListener} object to the federate for processing updates affecting a specific attribute
     * of the specified remote object instance.
     *
     * @param objectInstance The object representing the HLA object instance
     * @param propertyName Name of the attribute that is the target of the specified listener
     * @param listener The listener object
     */
    void addPropertyChangeListener(Object objectInstance, String propertyName, PropertyChangeListener listener);

    /**
     * Removes the provided {@link PropertyChangeListener} object if it was previously added to the federate.
     *
     * @param objectInstance The object representing the HLA object instance
     * @param propertyName Name of the attribute that is the target of the specified listener
     * @param listener The listener object
     */
    void removePropertyChangeListener(Object objectInstance, String propertyName, PropertyChangeListener listener);

    /**
     * Removes the provided {@link PropertyChangeListener} object if it was previously added to the federate.
     *
     * @param objectInstance The object representing the HLA object instance
     * @param listener The listener object
     */
    void removePropertyChangeListener(Object objectInstance, PropertyChangeListener listener);

    /**
     * <p>Publishes an interaction class, making it possible to send interactions of that interaction class.</p>
     *
     * <p>
     *     This operation does not affect subclasses to the specified interaction class. For example, it is not possible
     *     to send interactions of a subclass, unless that subclass is explicitly published.
     * </p>
     *
     * @param clazz The type used to represent this interaction class
     */
    void publishInteractionClass(Class<?> clazz) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * <p>
     * Unpublishes an interaction class, preventing sending further interactions of that interaction class. This operation
     * does not affect subclasses to the specified interaction class.
     * </p>
     *
     * @param name Name of the interaction class
     */
    void unpublishInteractionClass(String name) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * <p>
     * Notifies the RTI that this federate wants to receive interactions sent as the specified interaction class or any
     * subclass. The federate will be notified via any registered {@link InteractionListener} objects.
     * </p>
     *
     * @param clazz The type used to represent this interaction class
     */
    void subscribeInteractionClass(Class<?> clazz) throws FederateNotExecutionMember, RestoreInProgress, FederateServiceInvocationsAreBeingReportedViaMOM, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * <p>
     * Notifies the RTI that this federate no longer wants to receive interactions sent as the specified interaction class
     * or any subclass.
     * </p>
     *
     * @param name Name of the interaction class
     */
    void unsubscribeInteractionClass(String name) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * <p>
     * Sends the specified parameters and their specified values that subscribe to the specified interaction class or any
     * superclass. The receiving interaction class will be the specified interaction class, if subscribed, or the closest
     * subscribed superclass. Any parameters defined on a subclass will not be delivered. The sending federate is not
     * required to provide all parameters that are defined on the specified interaction class. The RTI will not provide any
     * default values for parameters that are not part of this operation.
     * </p>
     *
     * <p>
     *     The values are translated into raw byte arrays through the coders assigned to the {@link org.see.skf.core.annotations.Parameter}
     *     annotated fields of the object.
     * </p>
     *
     * @param interaction The object that is being used to represent this HLA interaction
     */
    void sendInteraction(Object interaction) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress, InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined;

    /**
     * Adds a {@link InteractionListener} object to the federate for processing callbacks relating to interactions.
     *
     * @param listener The listener object
     */
    void addInteractionListener(InteractionListener listener);

    /**
     * Removes the provided {@link InteractionListener} object if it was previously added to the federate.
     *
     * @param listener The listener object
     */
    void removeInteractionListener(InteractionListener listener);

    /**
     * Notifies the RTI that this federate has successfully achieved the synchronization point.
     *
     * @param label Name of the synchronization point
     */
    void achieveSyncPoint(String label) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * Notifies the RTI that this federate has achieved the synchronization point. The success flag indicates whether the
     * federate was successful or not.
     *
     * @param label Name of the synchronization point
     * @param success Indicates whether the federate was successful
     */
    void achieveSyncPoint(String label, boolean success) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * Adds a {@link SyncPointListener} object to the federate for processing callbacks relating to synchronization point.
     *
     * @param label The name of the synchronization point for which the listener should be triggered
     * @param listener The listener object
     */
    void addSyncPointListener(String label, SyncPointListener listener);

    /**
     * Removes the provided {@link SyncPointListener} object if it was previously added to the federate.
     *
     * @param listener The listener object
     */
    void removeSyncPointListener(SyncPointListener listener);

    /**
     * <p>
     * Requests a report on the ownership of the specified attributes. Since the outcome of this operation is asynchronous
     * and the results are not immediately available, a {@link Future} is returned. It is advised to call {@link Future#get()}
     * in a separate thread to block until a result is returned or call {@link Future#isDone()} in each time step until
     * the result becomes available.
     * </p>
     *
     * @param objectInstanceName Name of the object instance that is the target of this query
     * @param attributeNames Names of the attributes whose ownership is to be queried
     * @return A map consisting of attribute name to the federate name as the key-value pair
     */
    Future<Map<String, String>> queryAttributeOwnership(String objectInstanceName, String... attributeNames);

    /**
     * <p>
     * Initiates an acquisition sequence. The specified attributes fall into one of three categories: Unowned attributes
     * will become immediately owned by this federate. Attributes that are part of a negotiated divestiture will require
     * the owning federate to confirm the divestiture. Owned attributes that are not part of a divestiture will have to release
     * ownership at their discretion.
     * </p>
     *
     * <p>
     *     The federate is notified of the outcome of the attribute acquisition sequence through any {@link AttributeOwnershipListener}
     *     objects that were registered with the federate.
     * </p>
     *
     * @param objectInstanceName Name of the object instance
     * @param attributeNames Names of the attributes whose ownership is of interest to this federate
     */
    void acquireAttributeOwnership(String objectInstanceName, String... attributeNames) throws FederateNotExecutionMember, ObjectClassNotPublished, AttributeNotDefined, RestoreInProgress, FederateOwnsAttributes, ObjectInstanceNotKnown, NotConnected, RTIinternalError, AttributeNotPublished, SaveInProgress;

    /**
     * Releases ownership of unowned attributes provided that a new owner is immediately available, that is, another federate
     * must have previously requested acquisition.
     *
     * @param objectInstance Name of the object instance
     * @param attributeNames Names of the attributes whose ownership is to be released
     */
    void divestAttributeOwnershipIfWanted(Object objectInstance, String... attributeNames) throws FederateNotExecutionMember, AttributeNotDefined, RestoreInProgress, AttributeNotOwned, ObjectInstanceNotKnown, NotConnected, RTIinternalError, SaveInProgress;

    /**
     * Adds a {@link AttributeOwnershipListener} object to the federate for processing callbacks relating to attribute
     * ownership.
     *
     * @param objectInstance
     * @param listener
     */
    void addAttributeOwnershipListener(Object objectInstance, AttributeOwnershipListener listener);

    /**
     * Removes the provided {@link AttributeOwnershipListener} object if it was previously added to the federate.
     *
     * @param listener The listener object
     */
    void removeAttributeOwnershipListener(AttributeOwnershipListener listener);

    /**
     * Returns the current simulation time of the federate in Julian Date Time (JDT) format.
     */
    double getSimulationTime();

    /**
     * Returns true if the federate has sent a time advance request (TAR) to the RTI and false if the federate was granted
     * a time advance grant (TAG).
     */
    boolean isAdvancing();

    /**
     * Returns the name of the federate.
     */
    String getName();

    /**
     * The role of the federate in accordance to the SRFOM i.e., early or late joiner.
     */
    enum Role {
        EARLY,
        LATE
    }
}
