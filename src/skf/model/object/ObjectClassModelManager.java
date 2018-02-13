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
package skf.model.object;

import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.IllegalName;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import skf.exception.UpdateException;
import skf.model.object.annotations.ObjectClass;

public class ObjectClassModelManager {

	//maps for published element
	private Map<String, ObjectClassModel> published = null;
	private Map<String, ObjectClassEntity> mapInstanceNameObjectClassEntity = null;

	//maps for subscribed element
	private Map<String, ObjectClassModel> subscribed = null;
	private Map<ObjectClassHandle, Class<? extends ObjectClass>> mapHandleClassObjectClass = null;
	private Map<ObjectInstanceHandle, ObjectClassHandleEntity> objectInstanceHandleObjectClassHandle = null;

	public ObjectClassModelManager() {
		this.published = new HashMap<String, ObjectClassModel>();
		this.mapInstanceNameObjectClassEntity = new HashMap<String, ObjectClassEntity>();

		this.subscribed = new HashMap<String, ObjectClassModel>();
		this.mapHandleClassObjectClass = new HashMap<ObjectClassHandle, Class<? extends ObjectClass>>();
		this.objectInstanceHandleObjectClassHandle = new HashMap<ObjectInstanceHandle, ObjectClassHandleEntity>();
	}

	public Map<String, ObjectClassModel> getPublishedMap() {
		return this.published;
	}

	public Map<String, ObjectClassModel> getSubscribedMap() {
		return this.subscribed;
	}

	public void subscribe(Class<? extends ObjectClass> objectClass) throws RTIinternalError, InstantiationException, IllegalAccessException, NameNotFound, 
	FederateNotExecutionMember, NotConnected, InvalidObjectClassHandle, AttributeNotDefined,
	ObjectClassNotDefined, SaveInProgress, RestoreInProgress {


		ObjectClassModel ocm = new ObjectClassModel(objectClass);
		subscribed.put(objectClass.getAnnotation(ObjectClass.class).name(), ocm);
		mapHandleClassObjectClass.put(ocm.getObjectClassHandle(), objectClass);
		ocm.subscribe();
	}

	public void unsubscribe(Class<? extends ObjectClass> objectClass) throws ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, 
	NotConnected, RTIinternalError {

		ObjectClassModel ocm = subscribed.get(objectClass.getAnnotation(ObjectClass.class).name());
		mapHandleClassObjectClass.remove(ocm.getObjectClassHandle());

		for(Entry<Object, ObjectClassEntity> entry : ocm.getEntities().entrySet())
			this.objectInstanceHandleObjectClassHandle.remove(entry.getValue().getObjectInstanceHandle());

		this.subscribed.remove(ocm);
		ocm.unsubscribe();
	}

	@SuppressWarnings("unchecked")
	public void publish(Object element, String name) throws RTIinternalError, NameNotFound, FederateNotExecutionMember, NotConnected, InvalidObjectClassHandle, InstantiationException, IllegalAccessException, AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, IllegalName, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectClassNotPublished, AttributeNotOwned, ObjectInstanceNotKnown, UpdateException {

		ObjectClassModel ocm = published.get(element.getClass().getAnnotation(ObjectClass.class).name());

		if(ocm == null){
			ocm = new ObjectClassModel((Class<? extends ObjectClass>) element.getClass());
			published.put(element.getClass().getAnnotation(ObjectClass.class).name(), ocm);
			ocm.publish();
		}
		
		ObjectClassEntity entity = null;
		if(name != null)
			entity = new ObjectClassEntity(name, element);
		else
			entity = new ObjectClassEntity(element.getClass().getName()+element.hashCode(), element);
		
		ocm.addEntity(entity);
		this.mapInstanceNameObjectClassEntity.put(entity.getInstanceName(), entity);

	}

	public ObjectClassEntity getObjectClassEntityByIstanceName(String instance_name) {
		return this.mapInstanceNameObjectClassEntity.get(instance_name);
	}

	public void addDiscoverObjectInstance(ObjectInstanceHandle instanceHandle, ObjectClassHandle objectClassHandle, String instanceName) {
		this.objectInstanceHandleObjectClassHandle.put(instanceHandle, new ObjectClassHandleEntity(objectClassHandle, instanceName));
	}
	
	public boolean objectInstanceHasBeenDiscovered(ObjectClassHandle objectClassHandle) {
		Set<Map.Entry<ObjectInstanceHandle, ObjectClassHandleEntity>> entrySet = objectInstanceHandleObjectClassHandle.entrySet();
		for(Map.Entry<ObjectInstanceHandle, ObjectClassHandleEntity> entry: entrySet)
			if(entry.getValue().getObjectClassHandle().equals(objectClassHandle))
				return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	public Object reflectAttributeValues(ObjectInstanceHandle instanceHandle, AttributeHandleValueMap attributes) throws InstantiationException, IllegalAccessException {

		ObjectClassHandleEntity oche = objectInstanceHandleObjectClassHandle.get(instanceHandle);
		ObjectClassHandle och = oche.getObjectClassHandle();
		ObjectClassModel ocm = subscribed.get(((Class<ObjectClass>)mapHandleClassObjectClass.get(och)).getAnnotation(ObjectClass.class).name());

		ObjectClassEntity entity = ocm.getObjectClassEntity(instanceHandle);
		if(entity == null){
			Object element = mapHandleClassObjectClass.get(och).newInstance();
			entity = new ObjectClassEntity(instanceHandle, element);
			entity.setInstanceName(oche.getInstanceName());
			entity.setStatus(NameReservationStatus.SUCCEDED);
			ocm.addEntity(entity);
		}
		ocm.updateSubscribedObject(entity, attributes);
		return entity.getElement();
	}
	
	public boolean objectClassIsSubscribed(ObjectClassHandle arg1) {
		return this.mapHandleClassObjectClass.get(arg1) != null;
	}

	public boolean objectInstanceHandleIsSubscribed(ObjectInstanceHandle arg0) {
		return this.objectInstanceHandleObjectClassHandle.get(arg0) != null;
	}

	public ObjectInstanceHandle getObjectInstanceHandle(ObjectClassHandle objectClassHandle) {
		for(Entry<ObjectInstanceHandle, ObjectClassHandleEntity> entry : objectInstanceHandleObjectClassHandle.entrySet())
			if(entry.getValue().getObjectClassHandle().equals(objectClassHandle))
				return entry.getKey();
		return null;
	}
}
