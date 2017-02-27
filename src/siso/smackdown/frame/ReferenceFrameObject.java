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
package siso.smackdown.frame;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.siso.spacefom.frame.time.FloatingPointTime;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedArray;
import hla.rti1516e.encoding.HLAfixedRecord;
import hla.rti1516e.encoding.HLAfloat64LE;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;

public class ReferenceFrameObject {

	private static Logger logger = LogManager.getLogger(ReferenceFrameObject.class);

	// Internal class control variables.
	private static boolean initialized = false;

	private ReferenceFrame frame;

	// HLA object and attribute handles.
	private static ObjectClassHandle  obj_class_handle;
	private static AttributeHandleSet attributeSet;

	private static AttributeHandle name_handle; // name
	private static AttributeHandle parent_frame_handle; // parent
	private static AttributeHandle state_handle; // state

	// ------------------- ReferenceFrame encoders ------------------------ //
	private static HLAunicodeString name = null;

	private static HLAfixedRecord spaceTimeCoordinateStateCoder;

	//Translational State
	private static HLAfixedRecord translationalCoder = null;
	private static HLAfixedArray<HLAfloat64LE> positionVector = null;
	private static HLAfixedArray<HLAfloat64LE> velocityVector = null;

	//Rotational State
	private static HLAfixedRecord rotationalCoder = null;
	private static HLAfixedRecord quaternionCoder = null;
	private static HLAfloat64LE scalar;
	private static HLAfixedArray<HLAfloat64LE> vector;
	private static HLAfixedArray<HLAfloat64LE> angularVelocityVector = null;

	//Time
	private static HLAfloat64LE time;




	// HLA object instance data.
	private ObjectInstanceHandle instance_handle;


	public ReferenceFrameObject(FrameType frameType) throws RTIinternalError {
		this.frame = new ReferenceFrame(frameType);
	}

	// This method is used to create the ReferenceFrame encoder/decoders.
	private static void createReferenceFrameEncoders() throws RTIinternalError {

		EncoderFactory factory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

		// Create the attribute encoders/decoders.

		//name
		name = factory.createHLAunicodeString();

		// ---------- SpaceTimeCoordinateStateCoder ---------- //

		spaceTimeCoordinateStateCoder = factory.createHLAfixedRecord();

		//Translational State
		translationalCoder = factory.createHLAfixedRecord();
		positionVector = factory.createHLAfixedArray(factory.createHLAfloat64LE(), factory.createHLAfloat64LE(), factory.createHLAfloat64LE());
		velocityVector = factory.createHLAfixedArray(factory.createHLAfloat64LE(), factory.createHLAfloat64LE(), factory.createHLAfloat64LE());

		translationalCoder.add(positionVector);
		translationalCoder.add(velocityVector);


		//Rotational State
		rotationalCoder = factory.createHLAfixedRecord();

		quaternionCoder = factory.createHLAfixedRecord();
		scalar = factory.createHLAfloat64LE();
		vector = factory.createHLAfixedArray(factory.createHLAfloat64LE(), factory.createHLAfloat64LE(), factory.createHLAfloat64LE());
		quaternionCoder.add(scalar);
		quaternionCoder.add(vector);

		angularVelocityVector = factory.createHLAfixedArray(factory.createHLAfloat64LE(), factory.createHLAfloat64LE(), factory.createHLAfloat64LE());

		rotationalCoder.add(quaternionCoder);
		rotationalCoder.add(angularVelocityVector);

		//Time
		time = factory.createHLAfloat64LE();

		spaceTimeCoordinateStateCoder.add(translationalCoder);
		spaceTimeCoordinateStateCoder.add(rotationalCoder);
		spaceTimeCoordinateStateCoder.add(time);

	}


	public static void initialize(RTIambassador rti_ambassador) throws NameNotFound, FederateNotExecutionMember, NotConnected, 
	RTIinternalError, InvalidObjectClassHandle, AttributeNotDefined, 
	ObjectClassNotDefined, SaveInProgress, RestoreInProgress {

		if(initialized)
			return;

		createReferenceFrameEncoders();

		// Get a handle to the ReferenceFrame class.
		obj_class_handle = rti_ambassador.getObjectClassHandle("ReferenceFrame");

		// Get handles to all the ReferenceFrame attributes.
		name_handle = rti_ambassador.getAttributeHandle(obj_class_handle, "name");
		parent_frame_handle = rti_ambassador.getAttributeHandle(obj_class_handle, "parent_name");
		state_handle = rti_ambassador.getAttributeHandle(obj_class_handle,  "state");

		// Generate an attribute handle set.
		attributeSet = rti_ambassador.getAttributeHandleSetFactory().create();
		attributeSet.add(name_handle);
		attributeSet.add(parent_frame_handle);
		attributeSet.add(state_handle);

		initialized = true;

	}

	@SuppressWarnings("unchecked")
	public void updateAttributes(AttributeHandleValueMap  attributes) {

		logger.debug("Updating the attributes of the ReferenceFrame: "+ frame.getFrame()+".");

		try{

			if(attributes.containsKey(name_handle)){
				name.decode(attributes.get(name_handle));
				this.frame.setFrame(FrameType.lookup(name.getValue()));
			}

			// Check for the translational state attribute.
			if (attributes.containsKey(state_handle)){

				//decodeState
				spaceTimeCoordinateStateCoder.decode(attributes.get(state_handle));

				// ************** Translational State *********************
				translationalCoder = (HLAfixedRecord) spaceTimeCoordinateStateCoder.get(0);
				positionVector = (HLAfixedArray<HLAfloat64LE>) translationalCoder.get(0);
				velocityVector = (HLAfixedArray<HLAfloat64LE>) translationalCoder.get(1);
				
				double[] decodePosition = new double[3]; 
				decodePosition[0] = positionVector.get(0).getValue();
				decodePosition[1] = positionVector.get(1).getValue();
				decodePosition[2] = positionVector.get(2).getValue();

				double[] decodeVelocity = new double[3]; 
				decodeVelocity[0] = velocityVector.get(0).getValue();
				decodeVelocity[1] = velocityVector.get(1).getValue();
				decodeVelocity[2] = velocityVector.get(2).getValue();

				// ************** Rotational State *********************
				rotationalCoder = (HLAfixedRecord) spaceTimeCoordinateStateCoder.get(1);
				
				quaternionCoder = (HLAfixedRecord) rotationalCoder.get(0);
				scalar = (HLAfloat64LE) quaternionCoder.get(0);
				vector = (HLAfixedArray<HLAfloat64LE>) quaternionCoder.get(1);
				
				angularVelocityVector = (HLAfixedArray<HLAfloat64LE>) rotationalCoder.get(1);
				
				double decodeScalar = scalar.getValue();
				double[] decodeVector = new double[3]; 
				decodeVector[0] = vector.get(0).getValue();
				decodeVector[1] = vector.get(1).getValue();
				decodeVector[2] = vector.get(2).getValue();

				double[] decodeAngularVelocity = new double[3]; 
				decodeAngularVelocity[0] = angularVelocityVector.get(0).getValue();
				decodeAngularVelocity[1] = angularVelocityVector.get(1).getValue();
				decodeAngularVelocity[2] = angularVelocityVector.get(2).getValue();

				// ************** Time *********************
				time = (HLAfloat64LE) spaceTimeCoordinateStateCoder.get(2);
				double decodeTime = time.getValue();

				// ************** Build SpaceTimeCoordinateState object *********************
				this.frame.getState().getTranslationalState().setPosition(new Vector3D(decodePosition));
				this.frame.getState().getTranslationalState().setVelocity(new Vector3D(decodeVelocity));

				this.frame.getState().getRotationState().setAttitudeQuaternion(new Quaternion(decodeScalar, decodeVector));
				this.frame.getState().getRotationState().setAngularVelocityVector(new Vector3D(decodeAngularVelocity));
				
				((FloatingPointTime)this.frame.getState().getTime()).setValue(decodeTime);
			}

		}catch(DecoderException e){
			logger.error("A DecoderException occured while updating the attributes of the ReferenceFrame "+ e.getMessage());
			e.printStackTrace();
		}
	}

	public static boolean matches(ObjectClassHandle other_class) {
		if(initialized)
			return obj_class_handle.equals(other_class);
		return false;
	}

	public ObjectInstanceHandle getObjectInstanceHandle() {
		return instance_handle;
	}

	public void setObjectInstanceHandle(ObjectInstanceHandle instance_handle) {
		this.instance_handle = instance_handle;
	}

	public ReferenceFrame getReferenceFrame() {
		return frame;
	}

	public static ObjectClassHandle getObjectClassHandle() {
		return obj_class_handle;
	}

	public static AttributeHandleSet getAttributeSet() {
		return attributeSet;
	}

	@Override
	public String toString() {
		return frame.toString();
	}

}