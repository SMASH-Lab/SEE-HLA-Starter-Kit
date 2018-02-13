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
package skf.model.interaction.modeTransitionRequest;

/**
 * The run mode requested.  
 * There are only 3 valid Mode Transition Request (MTR) mode values: 
 * MTR_GOTO_RUN, MTR_GOTO_FREEZE, MTR_GOTO_SHUTDOWN.  
 * Of these three valid mode requests, only 7 combinations of current 
 * execution mode and requested mode are valid: 
 * 1. EXEC_MODE_UNINITIALIZED to EXEC_MODE_SHUTDOWN 
 * 2. EXEC_MODE_INITIALIZED to EXEC_MODE_FREEZE 
 * 3. EXEC_MODE_INITIALIZED to EXEC_MODE_SHUTDOWN 
 * 4. EXEC_MODE_RUNNING to EXEC_MODE_FREEZE 
 * 5. EXEC_MODE_RUNNING to EXEC_MODE_SHUTDOWN 
 * 6. EXEC_MODE_FREEZE to EXEC_MODE_RUNNING 
 * 7. EXEC_MODE_FREEZE to EXEC_MODE_SHUTDOWN
 *
 */
public enum MTRMode {

	MTR_GOTO_RUN((short)2), 
	MTR_GOTO_FREEZE((short)3), 
	MTR_GOTO_SHUTDOWN((short)4);

	private short value;

	private MTRMode(short value){
		this.value = value;
	}

	public short getValue(){
		return value;
	}

	public static MTRMode lookup(short value){
		for(MTRMode mode : MTRMode.values())
			if(mode.value == value)
				return mode;
		return null;
	}

}
