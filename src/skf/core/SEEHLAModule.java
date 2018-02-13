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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import siso.smackdown.frame.FrameType;
import siso.smackdown.frame.ReferenceFrameObject;
import skf.config.Configuration;
import skf.exception.PublishException;
import skf.exception.UnsubscribeException;
import skf.exception.UpdateException;
import skf.model.interaction.annotations.InteractionClass;
import skf.model.object.ObjectClassModel;
import skf.model.object.annotations.ObjectClass;
import skf.synchronizationPoint.SynchronizationPoint;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.TimeQueryReturn;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.AsynchronousDeliveryAlreadyDisabled;
import hla.rti1516e.exceptions.AsynchronousDeliveryAlreadyEnabled;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.FederateAlreadyExecutionMember;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.FederateIsExecutionMember;
import hla.rti1516e.exceptions.FederateNameAlreadyInUse;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.FederateServiceInvocationsAreBeingReportedViaMOM;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.IllegalName;
import hla.rti1516e.exceptions.InTimeAdvancingState;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotPublished;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.LogicalTimeAlreadyPassed;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.ObjectClassNotPublished;
import hla.rti1516e.exceptions.ObjectInstanceNameInUse;
import hla.rti1516e.exceptions.ObjectInstanceNameNotReserved;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RequestForTimeConstrainedPending;
import hla.rti1516e.exceptions.RequestForTimeRegulationPending;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.SynchronizationPointLabelNotAnnounced;
import hla.rti1516e.exceptions.TimeConstrainedIsNotEnabled;
import hla.rti1516e.exceptions.TimeRegulationIsNotEnabled;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;
import hla.rti1516e.time.HLAinteger64Time;
import hla.rti1516e.time.HLAinteger64TimeFactory;

public class SEEHLAModule {

	private static final Logger logger = LogManager.getLogger(SEEHLAModule.class);

	private RTIambassador rtiamb = null;
	private SEEAbstractFederate federate = null;
	private SEEAbstractFederateAmbassador fedamb = null;

	//Handle simulation time
	private Time time = null;

	protected SEEHLAModule(SEEAbstractFederate federate, SEEAbstractFederateAmbassador fedamb) {
		this.federate = federate;
		this.fedamb = fedamb;
	}

	protected void setTime(Time time) {
		this.time = time;
		this.fedamb.setTime(time);
	}

	protected void connect(String local_settings_designator) throws RTIinternalError, ConnectionFailed, 
	InvalidLocalSettingsDesignator, UnsupportedCallbackModel, 
	CallNotAllowedFromWithinCallback {

		logger.info("Connecting to HLA/RTI.");
		// Create the RTIambassador and Connect
		this.rtiamb = SEERTIAmbassador.getInstance();

		// connect	
		try {
			rtiamb.connect(this.fedamb, CallbackModel.HLA_IMMEDIATE, local_settings_designator);
		} catch (AlreadyConnected e) {
			//ignore
		}

	}

	protected void joinIntoFederationExecution() throws CouldNotCreateLogicalTimeFactory, FederationExecutionDoesNotExist, 
	InconsistentFDD, ErrorReadingFDD, CouldNotOpenFDD, SaveInProgress, 
	RestoreInProgress, NotConnected, CallNotAllowedFromWithinCallback, 
	RTIinternalError, MalformedURLException {

		Configuration config = federate.getConfig();
		File dir = config.getFomDirectory();
		logger.info("Loading FOMs modules.");

		//load FOMs Modules
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				if(file.getName().endsWith(".xml")){
					logger.debug("Loading the "+file+" file.");
					return true;
				}
				return false;
			}
		};

		URL foms[] = new URL[dir.listFiles(filter).length];
		int index = 0;
		for(File file: dir.listFiles(filter)){
			try {
				foms[index] = file.toURI().toURL();
				index ++;
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("An error occurred during the processing of the FOM modules "+e.getMessage());
			}
		}
		logger.info("Join the federate ' "+config.getFederateName()+" ' into the Federation execution ' "+config.getFederationName()+" '.");
		//join the federate into the Federation execution
		try {
			boolean joined = false;
			String federateNameSuffix = "";
			int federateNameIndex = 1;
			while (!joined) {
				try {
					if(foms.length != 0)
						rtiamb.joinFederationExecution(config.getFederateName()+federateNameSuffix, config.getFederateType(), config.getFederationName(), foms);
					else
						rtiamb.joinFederationExecution(config.getFederateName()+federateNameSuffix, config.getFederateType(), config.getFederationName());

					joined = true;
				} catch (FederateNameAlreadyInUse e) {
					federateNameSuffix = "-" + federateNameIndex++;
				} 
			}//while
		} catch (FederateAlreadyExecutionMember ignored) {
			//ignore
		}

	}

	protected void configureAsynchronousDelivery() throws AsynchronousDeliveryAlreadyEnabled, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError{
		//enable asynchronous delivey
		if(federate.getConfig().isAsynchronousDelivery()){
			logger.info("Asynchronous delivery enabled.");
			rtiamb.enableAsynchronousDelivery();
		}
	}


	protected void configureTimePolicy() throws RTIexception {
		logger.info("Set up time management.");


		// Make the local logical time object.
		time.initializeFederationLogicalTime();

		// Make the local logical time interval.
		time.initializeFederationLookaheadInterval(federate.getConfig().getLookahead());

		// Make this federate time constrained.
		// Enable time constraint.
		if(federate.getConfig().isTimeConstrained()){
			logger.info("Time constrained enabled.");
			rtiamb.enableTimeConstrained();
			// Wait for time constraint to take affect.
			while(!fedamb.isConstrained()){
				try {
					Thread.sleep(10);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}// IF-TIME CONSTRAINED

		// Make this federate time regulating.
		// Enable time regulation.
		if(federate.getConfig().isTimeRegulating()){
			logger.debug("Time regulating enabled.");
			rtiamb.enableTimeRegulation(time.getFederationLookaheadInterval());
			// Wait for time regulation to take affect.
			while(!fedamb.isRegulating()){
				try {
					Thread.sleep(10);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}// IF-TIME REGULATION

	}


	@SuppressWarnings("rawtypes")
	public void advanceTime(LogicalTime nextLogicaTime) throws LogicalTimeAlreadyPassed, InvalidLogicalTime, InTimeAdvancingState, RequestForTimeRegulationPending, RequestForTimeConstrainedPending, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError{
		// At initialization, time can advance.
		fedamb.setIsAdvancing(true);
		rtiamb.timeAdvanceRequest(nextLogicaTime);
	}

	public void queryToGALT() throws RTIexception{

		// For late joining federates, advance time to GALT.
		TimeQueryReturn TimeQueryGALT = rtiamb.queryGALT();

		if (TimeQueryGALT.timeIsValid) {
			//time.setLogicalTime(startingGALT.time);
			HLAinteger64Time GALT = (HLAinteger64Time) TimeQueryGALT.time;
			long LCTS = federate.getExecutionConfiguration().getLeast_common_time_step();
			
			// Create Time object
			HLAinteger64TimeFactory time_factory = (HLAinteger64TimeFactory) time.getTimeFactory();	
			
			//Compute HLTB
			double time_floor = (Math.floor(GALT.getValue()/LCTS)+1)*LCTS;
			HLAinteger64Time hltb = time_factory.makeTime((long) time_floor);
			time.setFederationLogicalTime(hltb);
			advanceTime(hltb);
		} 
		else{
			logger.error("An error occurred during the advance to current HLA time.");
			throw new RTIexception("An error occurred during the advance to current HLA time.");
		}
	}

	public void disconnect() throws InvalidResignAction, OwnershipAcquisitionPending, 
	FederateOwnsAttributes, FederateNotExecutionMember, 
	NotConnected, CallNotAllowedFromWithinCallback, 
	RTIinternalError, FederateIsExecutionMember, 
	SaveInProgress, RestoreInProgress {

		// Disable time management.
		if (federate.getConfig().isTimeConstrained())
			try {
				rtiamb.disableTimeConstrained();
				logger.info("Time constrained disabled.");
			} catch (TimeConstrainedIsNotEnabled e) {
				//ignored
			}

		if (federate.getConfig().isTimeRegulating())
			try {
				rtiamb.disableTimeRegulation();
				logger.info("Time regulating disabled.");
			} catch (TimeRegulationIsNotEnabled e) {
				//ignored
			}

		if (federate.getConfig().isAsynchronousDelivery())
			try {
				rtiamb.disableAsynchronousDelivery();
				logger.info("Asynchronous delivery disabled.");
			} catch (AsynchronousDeliveryAlreadyDisabled e) {
				//ignored
			}

		// Clean up connectivity to Federation Execution.
		rtiamb.resignFederationExecution(ResignAction.DELETE_OBJECTS_THEN_DIVEST);
		logger.info("Resign from the federation execution");
		// Disconnect from the RTI.
		rtiamb.disconnect();
		logger.info("The federate has been disconnected from the federation execution.");
		rtiamb = null;

	}

	public void subscribeReferenceFrame(FrameType frameType) throws AttributeNotDefined, ObjectClassNotDefined, 
	SaveInProgress, RestoreInProgress, FederateNotExecutionMember, 
	NotConnected, RTIinternalError, NameNotFound, InvalidObjectClassHandle {

		logger.info("Subscribing the ' "+frameType+" ' ReferenceFrame");
		ReferenceFrameObject.initialize(rtiamb);
		fedamb.addReferenceFrameToSubcribedList(frameType);

		rtiamb.subscribeObjectClassAttributes(ReferenceFrameObject.getObjectClassHandle(), ReferenceFrameObject.getAttributeSet());

	}

	public void unsubscribeReferenceFrame(FrameType frameType) {
		logger.info("Unsubscribing the ' "+frameType+" ' ReferenceFrame");
		fedamb.deleteReferenceFrameFromSubcribedList(frameType);
	}

	public SEEAbstractFederate getFederate() {
		return this.federate;
	}

	public SEEAbstractFederateAmbassador getAmbassador() {
		return this.fedamb;
	}


	public void subscribeToSubject(Observer observer) {
		fedamb.addObserverToSubject(observer);
		logger.info("Subscribed to the Subject");
	}

	public void unsubscribeFromSubject(Observer observer) {
		fedamb.deleteObserverFromSubject(observer);
		logger.info("Unsubscribed from the Subject");
	}


	public void publishElement(Object element, String name) throws NameNotFound, FederateNotExecutionMember, NotConnected, RTIinternalError, InvalidObjectClassHandle, PublishException, InstantiationException, IllegalAccessException, AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, IllegalName, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectClassNotPublished, AttributeNotOwned, ObjectInstanceNotKnown, UpdateException  {

		if(!fedamb.objectClassEntityIsAlreadyPublished(element)){
			fedamb.publishObjectClassEntity(element, name);
			logger.info("The Object ' "+element+" ' has been published.");
		}
		else{
			logger.warn("Object: "+element+", is already published");
			throw new PublishException("Object: "+element+", is already published.");
		}

	}

	public void publishInteraction(Object interaction) throws PublishException, RTIinternalError, NameNotFound, FederateNotExecutionMember, NotConnected, InvalidInteractionClassHandle, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, InteractionClassNotPublished, InteractionParameterNotDefined {

		if(!fedamb.interactionClassEntityIsAlreadyPublished(interaction)){
			fedamb.publishInteractionClassEntity(interaction);
			logger.info("The Interaction ' "+interaction+" ' has been published.");
		}
		else{
			logger.warn("Interaction: "+interaction+", is already published");
			throw new PublishException("Interaction: "+interaction+", is already published.");
		}
	}

	public void updateElementObject(Object element) throws UpdateException, FederateNotExecutionMember, NotConnected, AttributeNotOwned, 
	AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress, RTIinternalError, 
	IllegalName, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectClassNotPublished, ObjectClassNotDefined {

		if(fedamb.objectClassEntityIsAlreadyPublished(element)){
			fedamb.updateObjectClassEntityOnRTI(element);
			logger.info("The Object '"+element+"' has been updated on the HLA/RTI platform.");
		}
		else{
			logger.warn("Object: "+element+", is not published. The equal() and hascode() methods must be consistent.");
			throw new UpdateException("Object: "+element+", is not published.The equal() and hashcode() methods must be consistent.");
		}
	}

	public void updateInteraction(Object interaction) throws InteractionClassNotPublished, InteractionParameterNotDefined, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError, UpdateException {


		if(fedamb.interactionClassEntityIsAlreadyPublished(interaction)){
			fedamb.updateInteractionClassEntityOnRTI(interaction);
			logger.info("The Interaction ' "+interaction+" ' has been updated on the HLA/RTI platform.");
		}
		else{
			logger.warn("Interaction: "+interaction+", not published.");
			throw new UpdateException("You can't update an unpublished interaction.");
		}

	}

	public void subscribeElementObject(Class<? extends ObjectClass> objectClass) throws InstantiationException, IllegalAccessException, 
	NameNotFound, FederateNotExecutionMember, NotConnected, 
	RTIinternalError, InvalidObjectClassHandle, AttributeNotDefined, 
	ObjectClassNotDefined, SaveInProgress, RestoreInProgress {

		if(!fedamb.objectClassModelIsAlreadySubscribed(objectClass)){
			fedamb.subscribeObjectClassModel(objectClass);
			logger.info("The ObjectClass '"+objectClass+"' has been subscribed.");
		}
		else
			logger.warn("The ObjectClass '"+objectClass+"' is already subscribed.");
	}

	public void subscribeInteractionObject(Class<? extends InteractionClass> interactionClass) throws RTIinternalError, NameNotFound, FederateNotExecutionMember, NotConnected, InvalidInteractionClassHandle, FederateServiceInvocationsAreBeingReportedViaMOM, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, InstantiationException, IllegalAccessException  {

		if(!fedamb.interactionClassModelIsAlreadySubscribed(interactionClass)){
			fedamb.subscribeInteractionClassModel(interactionClass);
			logger.info("The InteractionClass '"+interactionClass+"' has been subscribed.");
		}
		else
			logger.warn("The InteractionClass '"+interactionClass+"' is already subscribed.");
	}

	public void unsubscribeObjectClass(Class<? extends ObjectClass> objectClass) throws ObjectClassNotDefined, SaveInProgress, 
	RestoreInProgress, FederateNotExecutionMember, 
	NotConnected, RTIinternalError, UnsubscribeException {

		if(fedamb.objectClassModelIsAlreadySubscribed(objectClass)){
			fedamb.unsubscribeObjectClassModel(objectClass);
			logger.info("The ObjectClass '"+objectClass+" ' has been unsubscribed.");
		}
		else{	
			logger.error("Error during unsubscribe the "+objectClass);
			throw new UnsubscribeException("Error during unsubscribe the '"+objectClass+"'");
		}
	}

	public void unsubscribeInteractionObject(Class<? extends InteractionClass> interactionClass) throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError, UnsubscribeException {

		if(fedamb.interactionClassModelIsAlreadySubscribed(interactionClass)){
			fedamb.unsubscribeInteractionClassModel(interactionClass);
			logger.info("The InteractionClass '"+interactionClass+" ' has been unsubscribed.");
		}
		else{	
			logger.error("Error during unsubscribe the "+interactionClass);
			throw new UnsubscribeException("Error during unsubscribe the "+interactionClass);
		}
	}

	public void requestAttributeValueUpdate(Class<? extends ObjectClass> objectClass) throws AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError, UnsubscribeException {

		if(fedamb.objectClassModelIsAlreadySubscribed(objectClass)){

			String classHandleName = objectClass.getAnnotation(ObjectClass.class).name();
			ObjectClassModel model = fedamb.getObjectManager().getSubscribedMap().get(classHandleName);
			
			ObjectClassHandle objectClassHandle = model.getObjectClassHandle();
			AttributeHandleSet attributeSet = model.getAttributeHandleSet(objectClassHandle);
			
			rtiamb.requestAttributeValueUpdate(objectClassHandle, attributeSet, null);
		}
		else{	
			logger.error("Error: "+objectClass+" is not subscribed!");
			throw new UnsubscribeException("Error: "+objectClass+" is not subscribed!");
		}


	}

	public void waitForAttributeValueUpdate(Class<? extends ObjectClass> objectClass, int MAX_WAIT_TIME) {
		try {
			fedamb.waitForAttributeValueUpdate(objectClass, MAX_WAIT_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void waitForElementDiscovery(Class<? extends ObjectClass> objectClass, int MAX_WAIT_TIME) {
		try {
			fedamb.waitForElementDiscovery(objectClass, MAX_WAIT_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/* Start SynchronizationPoint Methods */
	public void achieveSynchronizationPoint(SynchronizationPoint sp) throws SynchronizationPointLabelNotAnnounced, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError {
		rtiamb.synchronizationPointAchieved(sp.getValue());
	}	

	public void registerSynchronizationPoint(SynchronizationPoint sp) throws SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError {
		logger.info("Register SynchronizationPoint: "+ sp);	
		rtiamb.registerFederationSynchronizationPoint(sp.getValue(), null);
	}

	public void announceSynchronizationPoint(SynchronizationPoint sp) throws FederateInternalError {
		logger.info("Announce SynchronizationPoint: "+ sp);	
		fedamb.announceSynchronizationPoint(sp.getValue(), null);
	}
	/* End SynchronizationPoint Methods */

}
