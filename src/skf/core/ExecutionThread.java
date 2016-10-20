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

import java.util.concurrent.atomic.AtomicBoolean;

import hla.rti1516e.exceptions.RTIexception;

public class ExecutionThread implements Runnable {

	private Thread runningThread = null;

	private SEEAbstractFederate federate = null;
	private SEEAbstractFederateAmbassador fedamb = null;
	private SEEHLAModule hlamodule = null;

	private AtomicBoolean CONTINUE_EXECUTION = null;	

	private long exec_loop_counter  = 0;     		 	

	// Simulation time parameters.
	private Time time = null;
	private static final long USEC_PER_CYCLE = 1000000; 

	protected ExecutionThread(SEEHLAModule hlamodule, Time time) {
		this.hlamodule = hlamodule;
		this.federate = hlamodule.getFederate();
		this.fedamb = hlamodule.getAmbassador();
		this.time = time;
		this.runningThread = new Thread(this);
	}

	@Override
	public void run() {

		CONTINUE_EXECUTION = new AtomicBoolean(true);

		while(CONTINUE_EXECUTION.get()){
			time.setFederateExecutionTimeCycle((exec_loop_counter * USEC_PER_CYCLE));

			// check realtime execution
			if (federate.getConfig().isRealtime()) {
				try {
					Thread.sleep(1000);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			// Wait for the time advance grant.
			while(!fedamb.isAdvancing()){
				try {
					Thread.sleep(10);
				} catch( Exception e ) {
					e.printStackTrace();
				}
			}

			federate.doAction();

			// Request a time advance by the logical time interval.
			try {
				hlamodule.makeTARequest();
			} catch (RTIexception e) {
				e.printStackTrace();
			}
			exec_loop_counter++;
		}

	}

	protected void start(){
		if(!runningThread.isAlive())
			runningThread.start();
	}

	protected void shutdown() {
		this.CONTINUE_EXECUTION.set(false);
	}
}
