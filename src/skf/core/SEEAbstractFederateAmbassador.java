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

import siso.smackdown.FrameType;
import siso.smackdown.ReferenceFrameObject;
import skf.core.observer.Subject;
import skf.exception.UpdateException;
import skf.model.interaction.InteractionClassModel;
import skf.model.interaction.InteractionClassModelManager;
import skf.model.interaction.annotations.InteractionClass;
import skf.model.object.ObjectClassEntity;
import skf.model.object.ObjectClassModel;
import skf.model.object.NameReservationStatus;
import skf.model.object.ObjectClassModelManager;
import skf.model.object.annotations.ObjectClass;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
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
import hla.rti1516e.time.HLAinteger64Time;

@SuppressWarnings("rawtypes")
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

	public SEEAbstractFederateAmbassador() {
		this.subscribedReferenceFrame = new HashSet<FrameType>();
		this.discoveredReferenceFrame = new HashMap<ObjectInstanceHandle, ReferenceFrameObject>();
		this.subject = new Subject();

		this.objectManager = new ObjectClassModelManager();
		this.interactionManager = new InteractionClassModelManager();

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

	public void setAdvancing(boolean isAdvancing) {
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

		logger.trace("ObjectInstanceHandle: "+arg0+", ObjectClassHandle: "+ arg1+", Name: "+arg2+", FederateHandle: "+arg3);

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


	@Override
	public void reflectAttributeValues(ObjectInstanceHandle arg0,
			AttributeHandleValueMap arg1, byte[] arg2, OrderType arg3,
			TransportationTypeHandle arg4, LogicalTime arg5, OrderType arg6,
			MessageRetractionHandle arg7, SupplementalReflectInfo arg8) throws FederateInternalError {

		this.reflAttributeValues(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);


	}

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

	private void reflAttributeValues(ObjectInstanceHandle arg0, AttributeHandleValueMap arg1, byte[] arg2, OrderType arg3,
			TransportationTypeHandle arg4, LogicalTime arg5, OrderType arg6,
			MessageRetractionHandle arg7, SupplementalReflectInfo arg8) throws FederateInternalError {

		logger.trace("ObjectInstanceHandle "+ arg0+",AttributeHandleValueMap "+ arg1+",byte[] "+ arg2+",OrderType "+arg3+", TransportationTypeHandle"+arg4+",LogicalTime"+ arg5+",OrderType"+arg6+"MessageRetractionHandle"+arg7+" ,SupplementalReflectInfo"+arg8);

		if(discoveredReferenceFrame.get(arg0) != null){
			ReferenceFrameObject rfo = discoveredReferenceFrame.get(arg0);
			rfo.updateAttributes(arg1);
			if(subscribedReferenceFrame.contains(rfo.getReferenceFrame().getFrameType()))
				subject.notifyUpdate(rfo.getReferenceFrame());
			return;
		}

		if(objectManager.objectInstanceHandleIsSubscribed(arg0)){
			try {
				Object ris = objectManager.reflectAttributeValues(arg0, arg1);
				if(ris != null)
					this.subject.notifyUpdate(ris);
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

	@Override
	public void timeConstrainedEnabled(LogicalTime time) throws FederateInternalError {

		this.time.setFederateTime(((HLAinteger64Time)time).getValue());
		this.isConstrained = true;
	}

	@Override
	public void timeRegulationEnabled(LogicalTime time) throws FederateInternalError {

		this.time.setFederateTime(((HLAinteger64Time)time).getValue());
		this.isRegulating = true;
	}

	@Override
	public void timeAdvanceGrant(LogicalTime time) throws FederateInternalError {

		if (((HLAinteger64Time)time).compareTo(this.time.getLogicalTime()) >= 0 ){
			this.time.setFederateTime(((HLAinteger64Time)time).getValue());
			this.isAdvancing = true;
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

	@SuppressWarnings("unchecked")
	public boolean objectClassModelIsAlreadySubscribed(Class objectClass) {
		if(((Class<ObjectClass>)objectClass).getAnnotation(ObjectClass.class) != null &&
				objectManager.getSubscribedMap().containsKey(((Class<ObjectClass>)objectClass).getAnnotation(ObjectClass.class).name()))
			return true;
		return false;
	}

	public void subscribeObjectClassModel(Class objectClass) throws RTIinternalError, InstantiationException, IllegalAccessException, 
	NameNotFound, FederateNotExecutionMember, NotConnected, InvalidObjectClassHandle, 
	AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress {

		objectManager.subscribe(objectClass);

	}

	public void unsubscribeObjectClassModel(Class objectClass) throws ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, 
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

	@SuppressWarnings("unchecked")
	public boolean interactionClassModelIsAlreadySubscribed(Class interactionClass) {

		if(((Class<InteractionClass>)interactionClass).getAnnotation(InteractionClass.class) != null &&
				interactionManager.getSubscribedMap().containsKey(((Class<InteractionClass>)interactionClass).getAnnotation(InteractionClass.class).name()))
			return true;
		return false;

	}

	public void subscribeInteractionClassModel(Class interactionClass) throws RTIinternalError, NameNotFound, FederateNotExecutionMember, NotConnected, InvalidInteractionClassHandle, FederateServiceInvocationsAreBeingReportedViaMOM, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, InstantiationException, IllegalAccessException {
		interactionManager.subscribe(interactionClass);
	}

	public void unsubscribeInteractionClassModel(Class interactionClass) throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError {
		interactionManager.unsubscribe(interactionClass);
	}
}
