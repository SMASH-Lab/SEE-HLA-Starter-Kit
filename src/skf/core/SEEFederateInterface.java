/*****************************************************************
SEE HLA Starter Kit -  A Java framework to develop HLA Federates
in the context of the SEE (Simulation Exploration Experience) 
project.
Copyright (c) 2014, SMASH Lab - University of Calabria (Italy), 
All rights reserved.

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
package skf.core;

import java.net.MalformedURLException;
import java.util.Observer;

import siso.smackdown.frame.FrameType;
import skf.config.Configuration;
import skf.exception.PublishException;
import skf.exception.SubscribeException;
import skf.exception.UnsubscribeException;
import skf.exception.UpdateException;
import skf.model.interaction.annotations.InteractionClass;
import skf.model.object.annotations.ObjectClass;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.FederateIsExecutionMember;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.FederateServiceInvocationsAreBeingReportedViaMOM;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.IllegalName;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotPublished;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.ObjectClassNotPublished;
import hla.rti1516e.exceptions.ObjectInstanceNameInUse;
import hla.rti1516e.exceptions.ObjectInstanceNameNotReserved;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;

public interface SEEFederateInterface {
	
	/**
	 * Configures the SEE federate parameters
	 * @param config the Configuration object
	 */
	public void configure(Configuration config);
	
	/**
	 * Connects the SEE Federate on the SEE Federation
	 * 
	 * @param local_settings_designator represents the "local settings designator" string related to the specific
	 * RTI vendor
	 * <p>
	 * For PITCH RTI the local_settings_designator parameter MUST be "crcHost=" + {crc_host} + "\ncrcPort=" + {crc_port}<br>
	 * e.g. "crcHost=localhost\ncrcPort=8989"
	 * <p>
	 * For MAK RTI the local_settings_designator parameter MUST be an empty string<br>
	 * e.g. ""
	 * 
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws CallNotAllowedFromWithinCallback call not allowed from within a callback
	 * @throws UnsupportedCallbackModel unsupported callback model
	 * @throws InvalidLocalSettingsDesignator invalid localSettingsDesignator
	 * @throws ConnectionFailed connection failed
	 */
	public void connectToRTI(String local_settings_designator) throws RTIinternalError, ConnectionFailed, InvalidLocalSettingsDesignator, UnsupportedCallbackModel, CallNotAllowedFromWithinCallback;
	
	/**
	 * Joins the Federate to the specified Federation Execution.
	 * 
	 * @throws MalformedURLException malformed URL exception
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws CallNotAllowedFromWithinCallback call not allowed from within a callback
	 * @throws NotConnected not connected
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 * @throws CouldNotOpenFDD could not open FDD 
	 * @throws ErrorReadingFDD error reading FDD
	 * @throws InconsistentFDD inconsistent FDD
	 * @throws FederationExecutionDoesNotExist federation execution does not exist
	 * @throws CouldNotCreateLogicalTimeFactory could not create LogicalTimeFactory
	 * @throws FederateNotExecutionMember federate not execution member
	 */
	public void joinFederationExecution() throws CouldNotCreateLogicalTimeFactory, FederationExecutionDoesNotExist, InconsistentFDD, ErrorReadingFDD, CouldNotOpenFDD, SaveInProgress, RestoreInProgress, NotConnected, CallNotAllowedFromWithinCallback, RTIinternalError, MalformedURLException, FederateNotExecutionMember;
	
	/**
	 * Shutdowns and disconnects the SEE Federate from the federation execution
	 *
	 * @throws InterruptedException interrupted exception
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 * @throws FederateIsExecutionMember federate is execution member
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws CallNotAllowedFromWithinCallback call not allowed from within a callback
	 * @throws NotConnected not connected
	 * @throws FederateNotExecutionMember federate not execution member
	 * @throws FederateOwnsAttributes federate owns attributes
	 * @throws OwnershipAcquisitionPending ownership acquisition pending
	 * @throws InvalidResignAction invalid resign action
	 */
	public void shudownExecution() throws InterruptedException, InvalidResignAction, OwnershipAcquisitionPending, FederateOwnsAttributes, FederateNotExecutionMember, NotConnected, CallNotAllowedFromWithinCallback, RTIinternalError, FederateIsExecutionMember, SaveInProgress, RestoreInProgress;
	
	/**
	 * Starts the Simulation Execution
	 * 
	 * @throws InterruptedException interrupted exception
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws NotConnected not connected
	 * @throws FederateNotExecutionMember federate not execution member
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 */
	public void startExecution() throws InterruptedException, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError;
	
	/**
	 * Freezes the Simulation Execution
	 * 
	 * @throws InterruptedException interrupted exception
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws NotConnected not connected
	 * @throws FederateNotExecutionMember federate not execution member
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 */
	public void freezeExecution() throws InterruptedException, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError;
	
	/**
	 * Resumes the Simulation Execution after Freeze
	 * 
	 * @throws InterruptedException interrupted exception
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws NotConnected not connected
	 * @throws FederateNotExecutionMember federate not execution member
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 */
	public void resumeExecution() throws InterruptedException, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError;
	
	/**
	 * Subscribes the specific ReferenceFrame
	 * 
	 * @param frameType the name of the ReferenceFrame
	 * 
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws NotConnected not connected
	 * @throws FederateNotExecutionMember federate not execution member
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 * @throws ObjectClassNotDefined HLA ObjectClass not defined
	 * @throws AttributeNotDefined HLA attribute not defined
	 * @throws InvalidObjectClassHandle invalid HLA ObjectClass handle 
	 * @throws NameNotFound name not found
	 */
	public void subscribeReferenceFrame(FrameType frameType) throws AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError, NameNotFound, InvalidObjectClassHandle;
	
	/**
	 * Unsubscribes the specific ReferenceFrame
	 * 
	 * @param frameType the specific ReferenceFrame
	 */
	public void unsubscribeReferenceFrame(FrameType frameType);

	/**
	 * Subscribes the SEE Federate to the Subject, in order to be notified about updates.
	 * 
	 * @param observer the observer object
	 */
	public void subscribeSubject(Observer observer);
	
	/**
	 * Unsubscribes the SEE Federate from the Subject.
	 * 
	 * @param observer the observer object
	 */
	public void unsubscribeSubject(Observer observer);
	
	/**
	 * Publishes the Element on HLA/RTI platform
	 * 
	 * @param element the element to be published
	 * 
	 * @throws UpdateException update exception
	 * @throws PublishException publish exception
	 * @throws ObjectInstanceNotKnown object instance not known
	 * @throws AttributeNotOwned attribute not owned
	 * @throws ObjectClassNotPublished HLA ObjectClass not published
	 * @throws ObjectInstanceNameNotReserved object instance name not reserved
	 * @throws ObjectInstanceNameInUse object instance name in use
	 * @throws IllegalName illegal name
	 * @throws IllegalAccessException illegal access exception
	 * @throws InstantiationException instantiation exception
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 * @throws ObjectClassNotDefined HLA ObjectClass not defined
	 * @throws AttributeNotDefined HLA Attribute not defined
	 * @throws InvalidObjectClassHandle invalid HLA ObjectClass handle
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws NotConnected not connected 
	 * @throws FederateNotExecutionMember federate not execution member
	 * @throws NameNotFound name not found
	 */
	public void publishElement(Object element) throws NameNotFound, FederateNotExecutionMember, NotConnected, RTIinternalError, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, InstantiationException, IllegalAccessException, IllegalName, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectClassNotPublished, AttributeNotOwned, ObjectInstanceNotKnown, PublishException, UpdateException;
	
	/**
	 * Publishes the Element on HLA/RTI platform with the specified name
	 * 
	 * @param element the element to be published
	 * @param name the name to give to the element on the HLA/RTI infrastructure
	 * 
	 * @throws UpdateException update exception
	 * @throws PublishException publish exception
	 * @throws ObjectInstanceNotKnown object instance not known
	 * @throws AttributeNotOwned attribute not owned
	 * @throws ObjectClassNotPublished HLA ObjectClass not published
	 * @throws ObjectInstanceNameNotReserved object instance name not reserved
	 * @throws ObjectInstanceNameInUse object instance name in use
	 * @throws IllegalName illegal name
	 * @throws IllegalAccessException illegal access exception
	 * @throws InstantiationException instantiation exception
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 * @throws ObjectClassNotDefined HLA ObjectClass not defined
	 * @throws AttributeNotDefined HLA Attribute not defined
	 * @throws InvalidObjectClassHandle invalid HLA ObjectClass handle
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws NotConnected not connected 
	 * @throws FederateNotExecutionMember federate not execution member
	 * @throws NameNotFound name not found
	 */
	public void publishElement(Object element, String name) throws NameNotFound, FederateNotExecutionMember, NotConnected, RTIinternalError, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, PublishException, InstantiationException, IllegalAccessException, IllegalName, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectClassNotPublished, AttributeNotOwned, ObjectInstanceNotKnown, UpdateException;
	
	/**
	 * Publishes the Interaction on HLA/RTI platform
	 * 
	 * @param element the interaction to be published
	 * 
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws NameNotFound name not found
	 * @throws FederateNotExecutionMember federate not execution member
	 * @throws NotConnected not connected
	 * @throws InvalidInteractionClassHandle invalid interaction class handle
	 * @throws PublishException publish exception
	 * @throws InteractionClassNotPublished interaction class not published
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 * @throws InteractionClassNotDefined interaction class not defined
	 * @throws InteractionParameterNotDefined interaction parameter not defined
	 */
	public void publishInteraction(Object element) throws RTIinternalError, 
	NameNotFound, FederateNotExecutionMember, NotConnected, 
	InvalidInteractionClassHandle, 
	PublishException, InteractionClassNotDefined, 
	SaveInProgress, RestoreInProgress, 
	InteractionClassNotPublished, InteractionParameterNotDefined;
	
	
	/**
	 * Updates the Element on HLA/RTI platform
	 * 
	 * @param element the element to be updated
	 * 
	 * @throws FederateNotExecutionMember federate not execution member
	 * @throws NotConnected not connected
	 * @throws AttributeNotOwned attribute not owned
	 * @throws AttributeNotDefined attribute not defined
	 * @throws ObjectInstanceNotKnown object instance not known
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws UpdateException update exception
	 * @throws ObjectClassNotDefined HLA ObjectClass not defined
	 * @throws ObjectClassNotPublished HLA ObjectClass not published
	 * @throws ObjectInstanceNameNotReserved object instance name not reserved
	 * @throws ObjectInstanceNameInUse object instance name in use
	 * @throws IllegalName illegal name
	 */
	public void updateElement(Object element) throws FederateNotExecutionMember, NotConnected, AttributeNotOwned, AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress, RTIinternalError, UpdateException, IllegalName, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectClassNotPublished, ObjectClassNotDefined;
	
	/**
	 * Updates the Interaction on the HLA/RTI platform
	 * 
	 * @param interaction the interaction to be updated
	 * 
	 * @throws InteractionClassNotPublished interaction class not published
	 * @throws InteractionParameterNotDefined interaction parameter not defined
	 * @throws InteractionClassNotDefined interaction class not defined
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 * @throws FederateNotExecutionMember federate not execution member
	 * @throws NotConnected not connected
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws UpdateException update exception
	 */
	public void updateInteraction(Object interaction) throws InteractionClassNotPublished, InteractionParameterNotDefined, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError, UpdateException;
	
	/**
	 * Subscribes an element
	 * 
	 * @param objectClass the element to be subscribed
	 * 
	 * @throws InstantiationException instantiation exception
	 * @throws IllegalAccessException illegal access exception
	 * @throws NameNotFound name not found
	 * @throws FederateNotExecutionMember federate not execution member
	 * @throws NotConnected not connected
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws InvalidObjectClassHandle invalid HLA ObjectClass handle
	 * @throws AttributeNotDefined attribute not defined
	 * @throws ObjectClassNotDefined object class not defined
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 * @throws SubscribeException subscribe exception
	 */
	public void subscribeElement(Class<? extends ObjectClass> objectClass) throws InstantiationException, IllegalAccessException, NameNotFound, FederateNotExecutionMember, NotConnected, RTIinternalError, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, SubscribeException;
	
	/**
	 * Subscribes an InteractionObject
	 * 
	 * @param interactionClass the interaction to be subscribed
	 * 
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws InstantiationException instantiation exception
	 * @throws IllegalAccessException illegal access exception
	 * @throws NameNotFound name not found
	 * @throws FederateNotExecutionMember federate not execution member
	 * @throws NotConnected not connected
	 * @throws InvalidInteractionClassHandle invalid interaction class handle
	 * @throws FederateServiceInvocationsAreBeingReportedViaMOM federate service invocations are being reported via MOM
	 * @throws InteractionClassNotDefined interaction class not defined
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 * @throws SubscribeException subscribe exception
	 */
	public void subscribeInteraction(Class<? extends InteractionClass> interactionClass) throws RTIinternalError, InstantiationException, IllegalAccessException, NameNotFound, FederateNotExecutionMember, NotConnected, InvalidInteractionClassHandle, FederateServiceInvocationsAreBeingReportedViaMOM, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, SubscribeException;
	
	/**
	 * Unsubscribes an ElementObject
	 * 
	 * @param objectClass the objectClass to be subscribed
	 * 
	 * @throws ObjectClassNotDefined HLA ObjectClass not defined
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 * @throws FederateNotExecutionMember federate not execution member
	 * @throws NotConnected not connected
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws UnsubscribeException unsubscribe exception
	 */
	public void unsubscribeElement(Class<? extends ObjectClass> objectClass) throws ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError, UnsubscribeException;
	
	/**
	 * Unsubscribes an InteractionObject
	 * 
	 * @param interactionClass the interactionClass to be unsubscribed
	 * 
	 * @throws InteractionClassNotDefined interaction class not defined
	 * @throws RestoreInProgress restore in progress
	 * @throws SaveInProgress save in progress
	 * @throws FederateNotExecutionMember federate not execution member
	 * @throws NotConnected not connected
	 * @throws RTIinternalError internal error related to the specific RTI infrastructure
	 * @throws UnsubscribeException unsubscribe exception
	 */
	public void unsubscribeInteraction(Class<? extends InteractionClass> interactionClass) throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError, UnsubscribeException;


}
