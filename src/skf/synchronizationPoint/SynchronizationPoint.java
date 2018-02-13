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
package skf.synchronizationPoint;

public enum SynchronizationPoint {

	INITIALIZATION_STARTED("initialization_started"),
	INITIALIZATION_COMPLETED("initialization_completed"),
	OBJECTS_DISCOVERED("objects_discovered"),	
	MTR_RUN("mtr_run"),
	MTR_FREEZE("mtr_freeze"),
	MTR_SHUTDOWN("mtr_shutdown"),
	MPI1("MPI1"),
	MPI2("MPI2");

	private String value = null;
	private boolean isAnnounced = false;
	private boolean isRegistered = false;
	private boolean federationIsSynchronized = false;

	private SynchronizationPoint(String value) {
		this.value = value;
	}

	public synchronized static SynchronizationPoint lookup(String value) {
		for(SynchronizationPoint sp : SynchronizationPoint.values())
			if(sp.value.equalsIgnoreCase(value))
				return sp;
		return null;
	}

	public String getValue() {
		return this.value;
	}
	
	public synchronized void isAnnounced(boolean value) {
		this.isAnnounced = value;
	}

	public synchronized void federationIsSynchronized(boolean value) {
		this.federationIsSynchronized = value;
	}
	
	public boolean isAnnounced() {
		return isAnnounced;
	}

	public boolean federationIsSynchronized() {
		return federationIsSynchronized;
	}

	public synchronized void isRegistered(boolean value) {
		this.isRegistered = value;
	}
	
	public boolean isRegistered() {
		return isRegistered;
		
	}
	
	public synchronized void resetProperties(){
		this.isAnnounced = false;
		this.isRegistered = false;
		this.federationIsSynchronized = false;
	}

}
