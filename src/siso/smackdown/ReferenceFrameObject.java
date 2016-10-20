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
package siso.smackdown;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	private static AttributeHandle name_handle;
	private static AttributeHandle parent_frame_handle;
	private static AttributeHandle trans_state_handle;
	private static AttributeHandle rot_state_handle;
	private static AttributeHandle time_handle;

	// ReferenceFrame encoders
	private static HLAfloat64LE double_encoder = null;
	private static HLAfixedArray<HLAfloat64LE> vec_encoder = null;
	private static HLAfixedArray<HLAfloat64LE> vec_dot_encoder = null;
	private static HLAfixedArray<HLAfloat64LE> q_vec_encoder = null;
	private static HLAfixedRecord quat_encoder = null;
	private static HLAfixedRecord trans_state_encoder = null;
	private static HLAfixedRecord rot_state_encoder = null;

	// HLA object instance data.
	private ObjectInstanceHandle instance_handle;


	public ReferenceFrameObject(FrameType frameType) throws RTIinternalError {
		this.frame = new ReferenceFrame(frameType);
	}

	// This method is used to create the ReferenceFrame encoder/decoders.
	private static void createReferenceFrameEncoders() throws RTIinternalError {

		EncoderFactory encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

		// Create the attribute encoders/decoders.
		double_encoder = encoderFactory.createHLAfloat64LE();
		vec_encoder = encoderFactory.createHLAfixedArray(encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE());
		vec_dot_encoder = encoderFactory.createHLAfixedArray(encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE() );
		q_vec_encoder = encoderFactory.createHLAfixedArray(encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE());

		quat_encoder = encoderFactory.createHLAfixedRecord();
		quat_encoder.add(double_encoder);
		quat_encoder.add(q_vec_encoder);

		trans_state_encoder = encoderFactory.createHLAfixedRecord();
		trans_state_encoder.add(vec_encoder);
		trans_state_encoder.add(vec_dot_encoder);

		rot_state_encoder = encoderFactory.createHLAfixedRecord();
		rot_state_encoder.add(quat_encoder);
		rot_state_encoder.add(vec_dot_encoder);

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
		trans_state_handle = rti_ambassador.getAttributeHandle(obj_class_handle,  "translational_state");
		rot_state_handle = rti_ambassador.getAttributeHandle(obj_class_handle, "rotational_state");
		time_handle = rti_ambassador.getAttributeHandle(obj_class_handle, "time");

		// Generate an attribute handle set.
		attributeSet = rti_ambassador.getAttributeHandleSetFactory().create();
		attributeSet.add(name_handle);
		attributeSet.add(parent_frame_handle);
		attributeSet.add(trans_state_handle);
		attributeSet.add(rot_state_handle);
		attributeSet.add(time_handle);
		
		initialized = true;

	}

	@SuppressWarnings("unchecked")
	public void updateAttributes(AttributeHandleValueMap  attributes) {

		logger.debug("Updating the attributes of the ReferenceFrame: "+ frame.getFrameType()+".");

		try{
			// Check for the translational state attribute.
			if (attributes.containsKey(trans_state_handle)){
				logger.trace("Processing the translational state attribute.");
				// Decode the incoming translational state.
				HLAfixedArray<HLAfloat64LE> hla_pos_vector;
				HLAfixedArray<HLAfloat64LE> hla_vel_vector;

				// Decode the incoming attribute.
				trans_state_encoder.decode(attributes.get(trans_state_handle));

				// Get references to embedded encoders.
				hla_pos_vector = (HLAfixedArray<HLAfloat64LE>)(trans_state_encoder.get(0));
				hla_vel_vector = (HLAfixedArray<HLAfloat64LE>)(trans_state_encoder.get(1));

				// Decode the position vector.
				this.frame.setPosition(hla_pos_vector.get(0).getValue(), hla_pos_vector.get(1).getValue(), hla_pos_vector.get(2).getValue());

				// Decode the velocity vector.
				this.frame.setVelocity(hla_vel_vector.get(0).getValue(), hla_vel_vector.get(1).getValue(), hla_vel_vector.get(2).getValue());
			}

			// Check for the rotational state attribute.
			if (attributes.containsKey(rot_state_handle)) {
				logger.trace("Processing the rotational state attribute.");
				// Decode the incoming rotational state.
				HLAfixedRecord              hla_quat;
				HLAfloat64LE                hla_q_scalar;
				HLAfixedArray<HLAfloat64LE> hla_q_vector;
				HLAfixedArray<HLAfloat64LE> hla_omega_vector;

				// Decode the incoming attribute.
				rot_state_encoder.decode(attributes.get(rot_state_handle));

				// Get references to embedded encoders.
				hla_quat = (HLAfixedRecord)(rot_state_encoder.get(0));
				hla_q_scalar = (HLAfloat64LE)(hla_quat.get(0));
				hla_q_vector = (HLAfixedArray<HLAfloat64LE>)(hla_quat.get(1));
				hla_omega_vector = (HLAfixedArray<HLAfloat64LE>)(rot_state_encoder.get(1));

				// Decode the quaternion
				this.frame.setAttitude(hla_q_scalar.getValue(), hla_q_vector.get(0).getValue(), hla_q_vector.get(1).getValue(),	hla_q_vector.get(2).getValue());

				// Decode the angular velocity vector.
				this.frame.setAttitudeRate(hla_omega_vector.get(0).getValue(), hla_omega_vector.get(1).getValue(), hla_omega_vector.get(2).getValue());

			}

			// Check for time attribute.
			if (attributes.containsKey(time_handle)){
				logger.trace("Processing the time attribute.");
				// Decode the incoming time.
				double_encoder.decode(attributes.get(time_handle));
				this.frame.setTime(double_encoder.getValue());

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