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

import siso.smackdown.utilities.*;

public class ReferenceFrame {

	// Public class data.
	private FrameType frame;
	private double[] position = null;
	private double[] velocity = null;
	private Quaternion attitude = null;
	private double[][] T_parent_body = null;
	private double[] attitudeRate = null;
	private double time;

	public ReferenceFrame(FrameType framename) {
		this.frame = framename;
		this.position = new double[3];
		this.velocity = new double[3];
		this.attitude = new Quaternion();
		this.T_parent_body = new double[3][3];
		this.attitudeRate = new double[3];
		this.time = 0.0;

		Vector3.initialize(position);
		Vector3.initialize(velocity);
		attitude.make_identity();
		Matrix3x3.identity(T_parent_body);
		Vector3.initialize(attitudeRate);
	}
	
	public ReferenceFrame (ReferenceFrame orig){
		this.frame = orig.frame; 
		Vector3.copy(orig.position, this.position );
		Vector3.copy(orig.velocity, this.velocity );
		this.attitude = orig.attitude;
		Matrix3x3.copy(orig.T_parent_body, this.T_parent_body );
		Vector3.copy(orig.attitudeRate, this.attitudeRate );
		this.time = orig.time;
	}

	public FrameType getFrameType() {
		return frame;
	}

	public String getFrameName(){
		return frame.toString();
	}

	public String getParentFrameName(){
		return frame.getParentFrame();
	}

	public double[] getPosition() {
		return position;
	}

	protected void setPosition(double arg0, double arg1, double arg2) {
		this.position[0] = arg0;
		this.position[1] = arg1;
		this.position[2] = arg2;
	}

	public double[] getVelocity() {
		return velocity;
	}

	protected void setVelocity(double arg0, double arg1, double arg2) {
		this.velocity[0] = arg0;
		this.velocity[1] = arg1;
		this.velocity[2] = arg2;
	}

	public Quaternion getAttitude() {
		return attitude;
	}

	protected void setAttitude(double scalar, double arg0, double arg1, double arg2) {
		this.attitude.scalar = scalar;
		this.attitude.vector[0] = arg0;
		this.attitude.vector[1] = arg1;
		this.attitude.vector[2] = arg2;
		this.attitude.left_quat_to_transform(this.T_parent_body);
	}

	public double[][] getT_parent_body() {
		return T_parent_body;
	}

	protected void setT_parent_body(double[][] t_parent_body) {
		T_parent_body = t_parent_body;
	}

	public double[] getAttitudeRate() {
		return attitudeRate;
	}

	protected void setAttitudeRate(double arg0, double arg1, double arg2) {
		this.attitudeRate[0] = arg0;
		this.attitudeRate[1] = arg1;
		this.attitudeRate[2] = arg2;
	}

	public double getTime() {
		return time;
	}

	protected void setTime(double time) {
		this.time = time;
	}
	
	@Override
	public String toString() {
		return this.frame + "\n"+
				" Position: "+ Vector3.getString(this.position) +"\n"+
				" Velocity: "+Vector3.getString(this.velocity) +"\n"+
				" Attitude: "+ this.attitude.toString() +"\n"+
				" Attitude rate: "+ this.attitudeRate[0]+", "+ this.attitudeRate[1]+", "+ this.attitudeRate[2];
	}

}
