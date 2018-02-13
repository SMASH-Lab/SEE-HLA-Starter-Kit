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
package skf.transition;

import skf.core.ExecutionTask;
import skf.core.SEEAbstractFederate;

public class TransitionManager {
	
	private SEEAbstractFederate federate = null;
	private ExecutionTask executionTask = null;
	private int MAX_WAIT_TIME = 0;
	

	public TransitionManager(SEEAbstractFederate federate, int MAX_WAIT_TIME) {
		this.federate = federate;
		this.MAX_WAIT_TIME = MAX_WAIT_TIME;
	}

	public void setExecutionTask(ExecutionTask executionTask) {
		this.executionTask  = executionTask;
		
	}

	public void start() {
		Thread run_th = new Thread(new RunTask(federate, executionTask, MAX_WAIT_TIME));
		run_th.start();
	}

	public void freeze() {
		Thread freeze_th = new Thread(new FreezeTask(federate, executionTask, MAX_WAIT_TIME));
		freeze_th.start();
	}

	public void resume() {
		Thread resume_th = new Thread(new RunTask(federate, executionTask, true, MAX_WAIT_TIME));
		resume_th.start();
	}

	public void shutdown() {
		Thread shutdown_th = new Thread(new ShutdownTask(federate, executionTask, MAX_WAIT_TIME));
		shutdown_th.start();
	}
}
