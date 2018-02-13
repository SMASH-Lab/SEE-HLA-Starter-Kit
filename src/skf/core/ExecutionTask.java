/*****************************************************************
HLADevelopmentKit -  A Java framework to develop HLA Federates.
Copyright (c) 2015, SMASH Lab - University of Calabria (Italy), 
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

import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.FederateIsExecutionMember;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;

public class ExecutionTask implements Runnable {

	private static final long USEC_PER_CYCLE = 1000000;

	private SEEAbstractFederate federate = null;
	private SEEAbstractFederateAmbassador fedamb = null;
	private SEEHLAModule hlamodule = null;

	private boolean isRunning = true;	
	private boolean isSuspended = false;

	private long exec_loop_counter  = 0;     		 	

	// Simulation time parameters.
	private Time time = null;

	public ExecutionTask(SEEHLAModule hlamodule, Time time) {
		this.hlamodule = hlamodule;
		this.federate = hlamodule.getFederate();
		this.fedamb = hlamodule.getAmbassador();
		this.time = time;
	}

	@Override
	public void run() {

		while(isRunning){

			time.setFederateTimeCycle(exec_loop_counter * USEC_PER_CYCLE);

			// check realtime execution
			if (federate.getConfig().isRealtime()) {
				try {
					Thread.sleep(1000);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			// Wait for the time advance grant.
			waitForAdvanceTimeGrant();

			federate.doAction();

			synchronized (this) {
				while(isSuspended)
					try {
						wait();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
			}
			// Request a time advance by the logical time interval.
			try {
				hlamodule.advanceTime(time.nextTimeStep());
				// Wait here for time advance grant.
				waitForAdvanceTimeGrant();
			} catch (RTIexception e) {
				e.printStackTrace();
			}
			exec_loop_counter++;
		}
		
		try {
			hlamodule.disconnect();
		} catch (InvalidResignAction | OwnershipAcquisitionPending
				| FederateOwnsAttributes | FederateNotExecutionMember
				| NotConnected | CallNotAllowedFromWithinCallback
				| RTIinternalError | FederateIsExecutionMember | SaveInProgress
				| RestoreInProgress e) {
			e.printStackTrace();
		}

	}

	private void waitForAdvanceTimeGrant() {
		while(fedamb.isAdvancing() && isRunning){
			try {
				Thread.sleep(10);
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void shutdown() {
		this.isRunning = false;
	}

	public synchronized void suspend() {
		this.isSuspended = true;

	}

	public synchronized void resume() {
		this.isSuspended = false;
		notify();
	}
}
