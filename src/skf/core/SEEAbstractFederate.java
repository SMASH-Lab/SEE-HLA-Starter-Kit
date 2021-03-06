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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import siso.smackdown.frame.FrameType;
import skf.config.Configuration;
import skf.exception.FirewallExeption;
import skf.exception.PublishException;
import skf.exception.SubscribeException;
import skf.exception.TimeOutException;
import skf.exception.UnsubscribeException;
import skf.exception.UpdateException;
import skf.model.interaction.annotations.InteractionClass;
import skf.model.object.annotations.ObjectClass;
import skf.model.object.executionConfiguration.ExecutionConfiguration;
import skf.synchronizationPoint.SynchronizationPoint;
import skf.transition.TransitionManager;
import skf.utility.SystemUtility;
import hla.rti1516e.exceptions.AsynchronousDeliveryAlreadyEnabled;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
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
import hla.rti1516e.exceptions.InvalidLookahead;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.ObjectClassNotPublished;
import hla.rti1516e.exceptions.ObjectInstanceNameInUse;
import hla.rti1516e.exceptions.ObjectInstanceNameNotReserved;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RequestForTimeConstrainedPending;
import hla.rti1516e.exceptions.RequestForTimeRegulationPending;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.SynchronizationPointLabelNotAnnounced;
import hla.rti1516e.exceptions.TimeConstrainedAlreadyEnabled;
import hla.rti1516e.exceptions.TimeRegulationAlreadyEnabled;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;

public abstract class SEEAbstractFederate implements SEEFederateInterface {

	private static final Logger logger = LogManager.getLogger(SEEAbstractFederate.class);

	private SEEHLAModule hlamodule= null;
	private Configuration config = null;

	private ExecutionTask executionTask = null;

	private ExecutionConfiguration exco = null;

	private Time time = null;

	private TransitionManager transitionManager = null;

	public SEEAbstractFederate(SEEAbstractFederateAmbassador seefedamb) {
		this.hlamodule = new SEEHLAModule(this, seefedamb);
		this.transitionManager = new TransitionManager(this, 100000);
	}

	@Override
	public void configure(Configuration config) {
		this.config = config;
	}

	@Override
	public void connectToRTI(String local_settings_designator) throws ConnectionFailed, 
	InvalidLocalSettingsDesignator, UnsupportedCallbackModel, 
	CallNotAllowedFromWithinCallback, RTIinternalError {

		this.hlamodule.connect(local_settings_designator);

	}

	@Override
	public void joinFederationExecution() throws CouldNotCreateLogicalTimeFactory, FederationExecutionDoesNotExist, 
										InconsistentFDD, ErrorReadingFDD, CouldNotOpenFDD, SaveInProgress, 
										RestoreInProgress, NotConnected, CallNotAllowedFromWithinCallback, 
										RTIinternalError, MalformedURLException, FederateNotExecutionMember {


		// check the status of windows firewall
		if(System.getProperty("os.name").toUpperCase().contains("WINDOWS") && SystemUtility.windowsFirewallIsEnabled()){
			logger.error("Windows Firewall is enabled. Please disable it before you ran your SEEFederate.");
			throw new FirewallExeption("Windows Firewall is enabled. Please disable it before you ran your SEEFederate.");
		}

		this.hlamodule.joinIntoFederationExecution();

		//enable Asynchronous Delivery
		try {
			this.hlamodule.configureAsynchronousDelivery();
		} catch (AsynchronousDeliveryAlreadyEnabled e) {
			e.printStackTrace();
		}
	}

	@Override
	public void subscribeReferenceFrame(FrameType frame) throws AttributeNotDefined, ObjectClassNotDefined, 
	SaveInProgress, RestoreInProgress, FederateNotExecutionMember, 
	NotConnected, RTIinternalError, NameNotFound, InvalidObjectClassHandle {

		this.hlamodule.subscribeReferenceFrame(frame);

	}

	@Override
	public void unsubscribeReferenceFrame(FrameType frameType) {
		this.hlamodule.unsubscribeReferenceFrame(frameType);
	}

	@Override
	public void subscribeSubject(Observer observer) {
		this.hlamodule.subscribeToSubject(observer);
	}

	@Override
	public void unsubscribeSubject(Observer observer) {
		this.hlamodule.unsubscribeFromSubject(observer);
	}

	protected abstract void doAction();

	public Configuration getConfig() {
		return config;
	}

	public void setupHLATimeManagement() {
		try {
			this.time = new Time(config.getSimulationScenarioTimeEphoc());
			this.hlamodule.setTime(time);
			this.hlamodule.configureTimePolicy();

			if(config.getFederateRole().equalsIgnoreCase("late"))
				this.hlamodule.queryToGALT();

		} catch (InvalidLookahead | InTimeAdvancingState | RequestForTimeRegulationPending
				| FederateNotExecutionMember | RequestForTimeConstrainedPending  e) {
			e.printStackTrace();
		} catch (TimeRegulationAlreadyEnabled | TimeConstrainedAlreadyEnabled | AsynchronousDeliveryAlreadyEnabled e) {
			e.printStackTrace();
		} catch (NotConnected | RTIinternalError e) {
			e.printStackTrace();
		} catch (SaveInProgress | RestoreInProgress e) {
			e.printStackTrace();
		} catch (RTIexception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startExecution() {
		executionTask = new ExecutionTask(hlamodule, this.time);
		transitionManager.setExecutionTask(executionTask);
		transitionManager.start();
	}

	@Override
	public void freezeExecution() {
		transitionManager.freeze();
	}

	@Override
	public void resumeExecution() {
		transitionManager.resume();
	}

	@Override
	public void shudownExecution() {
		transitionManager.shutdown();
	}

	@Override
	public void publishElement(Object element) throws NameNotFound, FederateNotExecutionMember, NotConnected, RTIinternalError, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, InstantiationException, IllegalAccessException, IllegalName, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectClassNotPublished, AttributeNotOwned, ObjectInstanceNotKnown, PublishException, UpdateException {

		publishElement(element, null);
	}

	@Override
	public void publishElement(Object element, String name) throws NameNotFound, FederateNotExecutionMember, NotConnected, 
	RTIinternalError, InvalidObjectClassHandle, AttributeNotDefined,
	ObjectClassNotDefined, SaveInProgress, RestoreInProgress, PublishException, InstantiationException, IllegalAccessException, IllegalName, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectClassNotPublished, AttributeNotOwned, ObjectInstanceNotKnown, UpdateException {

		if(objectClassIsValid(element.getClass()))
			this.hlamodule.publishElement(element, name);
		else{
			logger.error("ObjectElement: '"+ element +"' is not valid!");
			throw new PublishException("ObjectElement: '"+ element +"' is not valid!");
		}

	}

	@Override
	public void publishInteraction(Object interaction) throws RTIinternalError, NameNotFound, FederateNotExecutionMember, NotConnected, InvalidInteractionClassHandle, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, InteractionClassNotPublished, InteractionParameterNotDefined, PublishException {

		if(interactionClassIsValid(interaction.getClass()))
			this.hlamodule.publishInteraction(interaction);
		else{
			logger.error("Interaction: '"+ interaction +"' is not valid!");
			throw new PublishException("Interaction: '"+ interaction +"' is not valid!");
		}
	}

	@Override
	public void updateElement(Object element) throws FederateNotExecutionMember, NotConnected, AttributeNotOwned, AttributeNotDefined, 
	ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress, RTIinternalError, UpdateException, 
	IllegalName, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectClassNotPublished, ObjectClassNotDefined {

		if(objectClassIsValid(element.getClass()))
			this.hlamodule.updateElementObject(element);
		else{
			logger.error("ObjectElement: '"+ element +"' is not valid!");
			throw new UpdateException("ObjectElement: '"+ element +"' is not valid!");
		}
	}

	@Override
	public void updateInteraction(Object interaction) throws InteractionClassNotPublished, InteractionParameterNotDefined, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError, UpdateException {

		if(interactionClassIsValid(interaction.getClass()))
			this.hlamodule.updateInteraction(interaction);
		else{
			logger.error("Interaction: '"+ interaction +"' is not valid!");
			throw new UpdateException("Interaction: '"+ interaction +"' is not valid!");
		}
	}

	@Override
	public void subscribeElement(Class<? extends ObjectClass> objectClass) throws InstantiationException, IllegalAccessException, NameNotFound, FederateNotExecutionMember, NotConnected, RTIinternalError, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, SubscribeException {

		if(objectClassIsValid(objectClass))
			this.hlamodule.subscribeElementObject(objectClass);
		else{
			logger.error("ObjectClass: '"+ objectClass +"' is not valid!");
			throw new SubscribeException("ObjectClass: '"+ objectClass +"' is not valid!");
		}
	}

	public void requestAttributeValueUpdate(Class<? extends ObjectClass> objectClass) throws AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError, UnsubscribeException, SubscribeException {

		if(objectClassIsValid(objectClass))
			this.hlamodule.requestAttributeValueUpdate(objectClass);
		else{
			logger.error("ObjectClass: '"+ objectClass +"' is not valid!");
			throw new SubscribeException("ObjectClass: '"+ objectClass +"' is not valid!");
		}

	}

	public void waitForAttributeValueUpdate(Class<? extends ObjectClass> objectClass, int MAX_WAIT_TIME) throws SubscribeException {

		if(objectClassIsValid(objectClass))
			this.hlamodule.waitForAttributeValueUpdate(objectClass, MAX_WAIT_TIME);
		else{
			logger.error("ObjectClass: '"+ objectClass +"' is not valid!");
			throw new SubscribeException("ObjectClass: '"+ objectClass +"' is not valid!");
		}

	}

	@Override
	public void subscribeInteraction(Class<? extends InteractionClass> interactionClass) throws RTIinternalError, InstantiationException, IllegalAccessException, NameNotFound, FederateNotExecutionMember, NotConnected, InvalidInteractionClassHandle, FederateServiceInvocationsAreBeingReportedViaMOM, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, SubscribeException {

		if(interactionClassIsValid(interactionClass))
			this.hlamodule.subscribeInteractionObject(interactionClass);
		else{
			logger.error("Interaction: '"+ interactionClass +"' is not valid!");
			throw new SubscribeException("Interaction: '"+ interactionClass +"' is not valid!");
		}

	}

	@Override
	public void unsubscribeElement(Class<? extends ObjectClass> objectClass) throws ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError, UnsubscribeException {

		if(objectClassIsValid(objectClass))
			this.hlamodule.unsubscribeObjectClass(objectClass);
		else{
			logger.error("ObjectClass: '"+ objectClass +"' is not valid!");
			throw new UnsubscribeException("ObjectClass: '"+ objectClass +"' is not valid!");
		}

	}

	@Override
	public void unsubscribeInteraction(Class<? extends InteractionClass> interactionClass) throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError, UnsubscribeException {

		if(interactionClassIsValid(interactionClass))
			this.hlamodule.unsubscribeInteractionObject(interactionClass);
		else{
			logger.error("Interaction: '"+ interactionClass +"' is not valid!");
			throw new UnsubscribeException("Interaction: '"+ interactionClass +"' is not valid!");
		}
	}

	public Time getTime() {
		return time;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean objectClassIsValid(Class objectClass) {

		if(((Class<? extends ObjectClass>)objectClass).getAnnotation(ObjectClass.class) != null &&
				((Class<? extends ObjectClass>)objectClass).getAnnotation(ObjectClass.class).name() != null)
			return true;
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean interactionClassIsValid(Class interactionClass) {
		if(((Class<? extends InteractionClass>)interactionClass).getAnnotation(InteractionClass.class) != null &&
				((Class<? extends InteractionClass>)interactionClass).getAnnotation(InteractionClass.class).name() != null)
			return true;
		return false;
	}

	public void waitForElementDiscovery(Class<? extends ObjectClass> objectClass, int MAX_WAIT_TIME) throws UnsubscribeException {
		if(objectClassIsValid(objectClass))
			this.hlamodule.waitForElementDiscovery(objectClass, MAX_WAIT_TIME);
		else{
			logger.error("ObjectClass: '"+ objectClass +"' is not valid!");
			throw new UnsubscribeException("ObjectClass: '"+ objectClass +"' is not valid!");
		}
	}

	public void waitingForSynchronization(SynchronizationPoint sp, int max_wait_time) throws InterruptedException {

		long finishTime = System.currentTimeMillis() + max_wait_time;
		while(!sp.federationIsSynchronized()){
			Thread.sleep(10);
			if(System.currentTimeMillis() >= finishTime)
				throw new TimeOutException("Timeout waiting for synchronization ["+sp+"]");
		}
	}

	public void waitingForAnnouncement(SynchronizationPoint sp, int max_wait_time) throws InterruptedException {

		long finishTime = System.currentTimeMillis() + max_wait_time;
		while(!sp.isAnnounced()){
			Thread.sleep(10);
			if(System.currentTimeMillis() >= finishTime)
				throw new TimeOutException("Timeout waiting for synchronization ["+sp+"]");
		}
	}

	public void achieveSynchronizationPoint(SynchronizationPoint sp) throws SynchronizationPointLabelNotAnnounced, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError{
		this.hlamodule.achieveSynchronizationPoint(sp);
	}

	public synchronized void setExecutionConfiguration(ExecutionConfiguration exco){
		this.exco = exco;
	}

	public ExecutionConfiguration getExecutionConfiguration(){
		return exco;
	}

}
