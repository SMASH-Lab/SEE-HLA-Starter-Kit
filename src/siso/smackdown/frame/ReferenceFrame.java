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

import org.siso.spacefom.frame.SpaceTimeCoordinateState;
import org.siso.spacefom.util.Matrix;

public class ReferenceFrame {

	// Public class data.
	private FrameType frame = null; // name
	
	private SpaceTimeCoordinateState state = null;
	
	private Matrix T_parent_body = null;

	public ReferenceFrame(FrameType framename) {
		this.frame = framename;
		this.state = new SpaceTimeCoordinateState();
		this.T_parent_body = new Matrix(3, 3);
	}

	/**
	 * @return the frame
	 */
	public FrameType getFrame() {
		return frame;
	}

	/**
	 * @param frame the frame to set
	 */
	public void setFrame(FrameType frame) {
		this.frame = frame;
	}

	/**
	 * @return the state
	 */
	public SpaceTimeCoordinateState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(SpaceTimeCoordinateState state) {
		this.state = state;
	}

	/**
	 * @return the t_parent_body
	 */
	public Matrix getT_parent_body() {
		return T_parent_body;
	}

	/**
	 * @param t_parent_body the t_parent_body to set
	 */
	public void setT_parent_body(Matrix t_parent_body) {
		T_parent_body = t_parent_body;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ReferenceFrame [frame=" + frame
				+ ", state=" + state + "]";
	}

}
