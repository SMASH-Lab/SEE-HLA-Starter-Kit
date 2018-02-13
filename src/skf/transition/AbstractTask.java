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

public abstract class AbstractTask implements Runnable {
	
	protected SEEAbstractFederate federate = null;
	
	protected ExecutionTask executionTask = null;
	
	protected int MAX_WAIT_TIME = 0;
	
	//shared object among threads
	protected final Thread executionThread;

	public AbstractTask(SEEAbstractFederate federate, ExecutionTask executionTask, int MAX_WAIT_TIME) {
		this.federate  = federate;
		this.executionTask  = executionTask;
		this.executionThread = new Thread(executionTask);
		this.MAX_WAIT_TIME = MAX_WAIT_TIME;
	}
	
	protected Thread getExecutionThread(){
		return this.executionThread;
	}

}
