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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observer;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import siso.smackdown.frame.FrameType;
import siso.smackdown.frame.ReferenceFrameObject;
import skf.core.observer.Subject;
import skf.exception.TimeOutException;
import skf.exception.UpdateException;
import skf.model.interaction.InteractionClassModel;
import skf.model.interaction.InteractionClassModelManager;
import skf.model.interaction.annotations.InteractionClass;
import skf.model.object.ObjectClassEntity;
import skf.model.object.ObjectClassModel;
import skf.model.object.NameReservationStatus;
import skf.model.object.ObjectClassModelManager;
import skf.model.object.annotations.ObjectClass;
import skf.synchronizationPoint.SynchronizationPoint;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.SynchronizationPointFailureReason;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateServiceInvocationsAreBeingReportedViaMOM;
import hla.rti1516e.exceptions.IllegalName;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotPublished;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.ObjectClassNotPublished;
import hla.rti1516e.exceptions.ObjectInstanceNameInUse;
import hla.rti1516e.exceptions.ObjectInstanceNameNotReserved;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;

public abstract class SEEAbstractFederateAmbassador extends NullFederateAmbassador {

	private static Logger logger = LogManager.getLogger(SEEAbstractFederateAmbassador.class);

	private Subject subject = null;

	//subscribed ReferenceFrame
	private Set<FrameType> subscribedReferenceFrame = null;
	//discovered ReferenceFrame
	private Map<ObjectInstanceHandle, ReferenceFrameObject> discoveredReferenceFrame = null;

	private ObjectClassModelManager objectManager = null;
	private InteractionClassModelManager interactionManager = null;

	//To manage concurrent access to the variable 'logical_time'
	private boolean isRegulating = false;
	private boolean isConstrained = false;
	private boolean isAdvancing = false;

	private Time time = null;

	private Set<ObjectInstanceHandle> updateRequested = null;

	public SEEAbstractFederateAmbassador() {

		logger.info("SEEAbstractFederateAmbassador Costruttore");

		this.subscribedReferenceFrame = new HashSet<FrameType>();
		this.discoveredReferenceFrame = new HashMap<ObjectInstanceHandle, ReferenceFrameObject>();
		this.subject = new Subject();

		this.objectManager = new ObjectClassModelManager();
		this.interactionManager = new InteractionClassModelManager();
		this.updateRequested = new HashSet<ObjectInstanceHandle>();
	}

	protected void setTime(Time time) {
		this.time  = time;
	}

	public boolean isRegulating() {
		return isRegulating;
	}

	public void setRegulating(boolean isRegulating) {
		this.isRegulating = isRegulating;
	}

	public boolean isConstrained() {
		return isConstrained;
	}

	public void setConstrained(boolean isConstrained) {
		this.isConstrained = isConstrained;
	}

	public boolean isAdvancing() {
		return isAdvancing;
	}

	public void setIsAdvancing(boolean isAdvancing) {
		this.isAdvancing = isAdvancing;
	}

	public void addReferenceFrameToSubcribedList(FrameType frametype) {
		subscribedReferenceFrame.add(frametype);
	}

	public boolean referenceFrameIsSubcribed(FrameType frametype) {
		return subscribedReferenceFrame.contains(frametype);
	}

	public void deleteReferenceFrameFromSubcribedList(FrameType frameType) {
		subscribedReferenceFrame.remove(frameType);
	}

	public void addObserverToSubject(Observer observer) {
		subject.addObserver(observer);
	}

	public void deleteObserverFromSubject(Observer observer) {
		subject.deleteObserver(observer);
	}

	@Override
	public void discoverObjectInstance(ObjectInstanceHandle arg0, ObjectClassHandle arg1, String arg2, FederateHandle arg3) throws FederateInternalError {
		try {
			discObjectInstance(arg0, arg1, arg2, arg3);
		} catch (RTIinternalError e) {
			e.printStackTrace();
		}
	}

	@Override
	public void discoverObjectInstance(ObjectInstanceHandle arg0, ObjectClassHandle arg1, String arg2) throws FederateInternalError {
		try {
			discObjectInstance(arg0, arg1, arg2, null);
		} catch (RTIinternalError e) {
			e.printStackTrace();
		}
	}

	private void discObjectInstance(ObjectInstanceHandle arg0, ObjectClassHandle arg1, String arg2, FederateHandle arg3) throws RTIinternalError {

		logger.info("ObjectInstanceHandle: "+arg0+", ObjectClassHandle: "+ arg1+", Name: "+arg2+", FederateHandle: "+arg3);

		if(ReferenceFrameObject.matches(arg1)){
			try{
				ReferenceFrameObject rfo = new ReferenceFrameObject(FrameType.valueOf(arg2));
				rfo.setObjectInstanceHandle(arg0);
				discoveredReferenceFrame.put(arg0, rfo);

			}catch(IllegalArgumentException e){
				logger.error("Can't find the FrameType: "+arg2);
			}
		}
		else if(objectManager.objectClassIsSubscribed(arg1)){
			objectManager.addDiscoverObjectInstance(arg0, arg1, arg2);
		}// else-if
	}


	@SuppressWarnings("rawtypes")
	@Override
	public void reflectAttributeValues(ObjectInstanceHandle arg0,
			AttributeHandleValueMap arg1, byte[] arg2, OrderType arg3,
			TransportationTypeHandle arg4, LogicalTime arg5, OrderType arg6,
			MessageRetractionHandle arg7, SupplementalReflectInfo arg8) throws FederateInternalError {

		this.reflAttributeValues(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);


	}

	@SuppressWarnings("rawtypes")
	@Override
	public void reflectAttributeValues(ObjectInstanceHandle arg0,
			AttributeHandleValueMap arg1, byte[] arg2, OrderType arg3,
			TransportationTypeHandle arg4, LogicalTime arg5, OrderType arg6,
			SupplementalReflectInfo arg7) throws FederateInternalError {
		this.reflAttributeValues(arg0, arg1, arg2, arg3, arg4, arg5, arg6, null, arg7);
	}

	@Override
	public void reflectAttributeValues(ObjectInstanceHandle arg0,
			AttributeHandleValueMap arg1, byte[] arg2, OrderType arg3,
			TransportationTypeHandle arg4, SupplementalReflectInfo arg5) throws FederateInternalError {

		this.reflAttributeValues(arg0, arg1, arg2, arg3, arg4, null, null, null, arg5);

	}

	@SuppressWarnings("rawtypes")
	private void reflAttributeValues(ObjectInstanceHandle arg0, AttributeHandleValueMap arg1, byte[] arg2, OrderType arg3,
			TransportationTypeHandle arg4, LogicalTime arg5, OrderType arg6,
			MessageRetractionHandle arg7, SupplementalReflectInfo arg8) throws FederateInternalError {

		logger.trace("ObjectInstanceHandle "+ arg0+",AttributeHandleValueMap "+ arg1+",byte[] "+ arg2+",OrderType "+arg3+", TransportationTypeHandle"+arg4+",LogicalTime"+ arg5+",OrderType"+arg6+"MessageRetractionHandle"+arg7+" ,SupplementalReflectInfo"+arg8);

		if(discoveredReferenceFrame.get(arg0) != null){
			ReferenceFrameObject rfo = discoveredReferenceFrame.get(arg0);
			rfo.updateAttributes(arg1);
			if(subscribedReferenceFrame.contains(rfo.getReferenceFrame().getFrame()))
				subject.notifyUpdate(rfo.getReferenceFrame());
			return;
		}

		if(objectManager.objectInstanceHandleIsSubscribed(arg0)){
			try {
				Object ris = objectManager.reflectAttributeValues(arg0, arg1);
				if(ris != null){
					updateRequested.remove(arg0);
					this.subject.notifyUpdate(ris);
				}
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				logger.error("Error during the decoding operation of the Object with ObjectInstanceHandle="+arg0);
			}
			return;
		}

	}

	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass,
			ParameterHandleValueMap theParameters, byte[] userSuppliedTag,
			OrderType sentOrdering, TransportationTypeHandle theTransport,
			SupplementalReceiveInfo receiveInfo) throws FederateInternalError {

		this.recInteraction(interactionClass, theParameters, userSuppliedTag,
				sentOrdering, theTransport, null, null, null, receiveInfo);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass,
			ParameterHandleValueMap theParameters, byte[] userSuppliedTag,
			OrderType sentOrdering, TransportationTypeHandle theTransport,
			LogicalTime theTime, OrderType receivedOrdering,
			SupplementalReceiveInfo receiveInfo) throws FederateInternalError {

		this.recInteraction(interactionClass, theParameters, userSuppliedTag,
				sentOrdering, theTransport, theTime, receivedOrdering,
				null, receiveInfo);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass,
			ParameterHandleValueMap theParameters, byte[] userSuppliedTag,
			OrderType sentOrdering, TransportationTypeHandle theTransport,
			LogicalTime theTime, OrderType receivedOrdering,
			MessageRetractionHandle retractionHandle,
			SupplementalReceiveInfo receiveInfo) throws FederateInternalError {

		this.recInteraction(interactionClass, theParameters, userSuppliedTag,
				sentOrdering, theTransport, theTime, receivedOrdering,
				retractionHandle, receiveInfo);
	}

	@SuppressWarnings("rawtypes")
	private void recInteraction(InteractionClassHandle arg0, ParameterHandleValueMap arg1, byte[] arg2,
			OrderType arg3, TransportationTypeHandle arg4,
			LogicalTime arg5, OrderType arg6,
			MessageRetractionHandle arg7,
			SupplementalReceiveInfo arg8) {

		logger.trace("InteractionClassHandle "+ arg0+",ParameterHandleValueMap "+ arg1+",byte[] "+ arg2+
				",OrderType "+arg3+", TransportationTypeHandle"+arg4+",LogicalTime"+ arg5+",OrderType"+arg6+"MessageRetractionHandle"+arg7+" ,SupplementalReflectInfo"+arg8);


		if(interactionManager.interactionInstanceHandleIsSubscribed(arg0)){
			Object ris = interactionManager.receiveInteraction(arg0, arg1);
			if(ris != null)
				this.subject.notifyUpdate(ris);
		}

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void timeConstrainedEnabled(LogicalTime time) throws FederateInternalError {

		this.time.setFederateLogicalTime(time);
		this.isConstrained = true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void timeRegulationEnabled(LogicalTime time) throws FederateInternalError {

		this.time.setFederateLogicalTime(time);
		this.isRegulating = true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void timeAdvanceGrant(LogicalTime time) throws FederateInternalError {
		if (time.compareTo(this.time.getFederationLogicalTime()) >= 0 ){
			this.time.setFederateLogicalTime(time);
			this.isAdvancing = false;
		}
	}

	@Override
	public void objectInstanceNameReservationSucceeded(String instance_name) throws FederateInternalError {

		ObjectClassEntity oce = objectManager.getObjectClassEntityByIstanceName(instance_name);

		if (oce != null)
			oce.setStatus(NameReservationStatus.SUCCEDED);
		else 
			logger.error("ElementObject "+instance_name+" not found!");

	}


	@Override
	public void objectInstanceNameReservationFailed(String instance_name) throws FederateInternalError {

		ObjectClassEntity oce = objectManager.getObjectClassEntityByIstanceName(instance_name);

		if (oce != null)
			oce.setStatus(NameReservationStatus.FAILED);
		else 
			logger.error("ElementObject "+instance_name+" not found!");

	}

	public boolean objectClassModelIsAlreadySubscribed(Class<? extends ObjectClass> objectClass) {
		if(objectClass.getAnnotation(ObjectClass.class) != null &&
				objectManager.getSubscribedMap().containsKey(objectClass.getAnnotation(ObjectClass.class).name()))
			return true;
		return false;
	}

	public void subscribeObjectClassModel(Class<? extends ObjectClass> objectClass) throws RTIinternalError, InstantiationException, IllegalAccessException, 
	NameNotFound, FederateNotExecutionMember, NotConnected, InvalidObjectClassHandle, 
	AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress {

		objectManager.subscribe(objectClass);

	}

	public void unsubscribeObjectClassModel(Class<? extends ObjectClass> objectClass) throws ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, 
	NotConnected, RTIinternalError {

		objectManager.unsubscribe(objectClass);

	}

	public boolean objectClassEntityIsAlreadyPublished(Object element) {

		if(element.getClass().getAnnotation(ObjectClass.class) != null &&
				element.getClass().getAnnotation(ObjectClass.class).name() != null){

			ObjectClassModel ocm = objectManager.getPublishedMap().get(element.getClass().getAnnotation(ObjectClass.class).name());
			if(ocm != null)
				return ocm.getEntities().containsKey(element);
		}
		return false;
	}

	public void publishObjectClassEntity(Object element, String name) throws RTIinternalError, NameNotFound, FederateNotExecutionMember, NotConnected, InvalidObjectClassHandle, InstantiationException, IllegalAccessException, AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, IllegalName, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectClassNotPublished, AttributeNotOwned, ObjectInstanceNotKnown, UpdateException {
		objectManager.publish(element, name);

	}

	public void updateObjectClassEntityOnRTI(Object element) throws IllegalName, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
	NotConnected, RTIinternalError, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, 
	ObjectClassNotPublished, ObjectClassNotDefined, AttributeNotOwned, AttributeNotDefined, 
	ObjectInstanceNotKnown, UpdateException {


		ObjectClassModel ocm = objectManager.getPublishedMap().get(element.getClass().getAnnotation(ObjectClass.class).name());
		ocm.updatePublishedObject(ocm.getEntities().get(element));
	}

	public boolean interactionClassEntityIsAlreadyPublished(Object interaction) {

		if(interaction.getClass().getAnnotation(InteractionClass.class) != null &&
				interaction.getClass().getAnnotation(InteractionClass.class).name() != null){

			InteractionClassModel icm = interactionManager.getPublishedMap().get(interaction.getClass().getAnnotation(InteractionClass.class).name());
			if(icm != null)
				return icm.getEntity() != null;
		}

		return false;

	}

	public void publishInteractionClassEntity(Object interaction) throws RTIinternalError, NameNotFound, FederateNotExecutionMember, NotConnected, InvalidInteractionClassHandle, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, InteractionClassNotPublished, InteractionParameterNotDefined {
		interactionManager.publish(interaction);

	}

	public void updateInteractionClassEntityOnRTI(Object interaction) throws InteractionClassNotPublished, InteractionParameterNotDefined, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError {

		InteractionClassModel icm = interactionManager.getPublishedMap().get(interaction.getClass().getAnnotation(InteractionClass.class).name());
		icm.updatePublishedInteraction();
	}

	public boolean interactionClassModelIsAlreadySubscribed(Class<? extends InteractionClass> interactionClass) {

		if(interactionClass.getAnnotation(InteractionClass.class) != null &&
				interactionManager.getSubscribedMap().containsKey(interactionClass.getAnnotation(InteractionClass.class).name()))
			return true;
		return false;

	}

	public void subscribeInteractionClassModel(Class<? extends InteractionClass> interactionClass) throws RTIinternalError, NameNotFound, FederateNotExecutionMember, NotConnected, InvalidInteractionClassHandle, FederateServiceInvocationsAreBeingReportedViaMOM, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, InstantiationException, IllegalAccessException {
		interactionManager.subscribe(interactionClass);
	}

	public void unsubscribeInteractionClassModel(Class<? extends InteractionClass> interactionClass) throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError {
		interactionManager.unsubscribe(interactionClass);
	}

	public ObjectClassModelManager getObjectManager() {
		return objectManager;
	}

	public void waitForAttributeValueUpdate(Class<? extends ObjectClass> objectClass, int MAX_WAIT_TIME) throws InterruptedException {

		//get instanceHandle
		String objectName = objectClass.getAnnotation(ObjectClass.class).name();
		ObjectClassModel ocm = objectManager.getSubscribedMap().get(objectName);
		ObjectClassHandle objectClassHandle = ocm.getObjectClassHandle();


		ObjectInstanceHandle instance_handle = objectManager.getObjectInstanceHandle(objectClassHandle);
		updateRequested.add(instance_handle);

		long finishTime = System.currentTimeMillis() + MAX_WAIT_TIME;
		while(updateRequested.contains(instance_handle)){
			Thread.sleep(10);
			if(System.currentTimeMillis() >= finishTime)
				throw new TimeOutException("Timeout waiting for update request of instance ["+objectName+"]");
		}

	}

	public void waitForElementDiscovery(Class<? extends ObjectClass> objectClass, int MAX_WAIT_TIME) throws InterruptedException {
		
		

		String name = objectClass.getAnnotation(ObjectClass.class).name();
		ObjectClassModel ocm = objectManager.getSubscribedMap().get(name);
		ObjectClassHandle objectClassHandle = ocm.getObjectClassHandle();
		
		long finishTime = System.currentTimeMillis() + MAX_WAIT_TIME;
		while(!objectManager.objectInstanceHasBeenDiscovered(objectClassHandle))
			Thread.sleep(10);
		if(System.currentTimeMillis() >= finishTime)
			throw new TimeOutException("Timeout waiting for element discovery ["+objectClass.getName()+"]");

	}

	public Time getTime() {
		return this.time;
	}

	/* Start SynchronizationPoint Callback methods */

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#synchronizationPointRegistrationSucceeded(java.lang.String)
	 */
	@Override
	public void synchronizationPointRegistrationSucceeded(
			String synchronizationPointLabel) throws FederateInternalError {

		logger.info("Incoming Callback SynchronizationPoint Registration Succeeded: " + synchronizationPointLabel);


		SynchronizationPoint sp = SynchronizationPoint.lookup(synchronizationPointLabel);
		if(sp != null){
			sp.isRegistered(true);
			logger.info("Successfully registered sync point: " + sp);
		}
		else
			throw new IllegalArgumentException("SynchronizationPoint["+sp+"] not defined.");
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#synchronizationPointRegistrationFailed(java.lang.String, hla.rti1516e.SynchronizationPointFailureReason)
	 */
	@Override
	public void synchronizationPointRegistrationFailed (
			String synchronizationPointLabel,
			SynchronizationPointFailureReason reason)
					throws FederateInternalError {

		SynchronizationPoint sp = SynchronizationPoint.lookup(synchronizationPointLabel);
		if(sp != null)
			logger.info("Failed to register sync point: " + sp + ", reason: " + reason);
	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#announceSynchronizationPoint(java.lang.String, byte[])
	 */
	@Override
	public void announceSynchronizationPoint(String synchronizationPointLabel,
			byte[] userSuppliedTag) throws FederateInternalError {

		SynchronizationPoint sp = SynchronizationPoint.lookup(synchronizationPointLabel);
		if(sp != null){
			sp.isAnnounced(true);
			logger.info("Synchronization point announced: " + sp);
		}
		else
			throw new IllegalArgumentException("SynchronizationPoint["+sp+"] not defined.");

	}

	/* (non-Javadoc)
	 * @see hla.rti1516e.NullFederateAmbassador#federationSynchronized(java.lang.String, hla.rti1516e.FederateHandleSet)
	 */
	@Override
	public void federationSynchronized(String synchronizationPointLabel,
			FederateHandleSet failedToSyncSet) throws FederateInternalError {

		SynchronizationPoint sp = SynchronizationPoint.lookup(synchronizationPointLabel);
		if(sp != null){
			sp.federationIsSynchronized(true);
			logger.info("Federation Synchronized: " + sp);
		}
		else
			throw new IllegalArgumentException("SynchronizationPoint["+sp+"] not defined");

	}
	/* End SynchronizationPoint Callback methods */
}
