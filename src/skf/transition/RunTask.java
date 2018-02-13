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

import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.SynchronizationPointLabelNotAnnounced;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import skf.core.ExecutionTask;
import skf.core.SEEAbstractFederate;
import skf.synchronizationPoint.SynchronizationPoint;

public class RunTask extends AbstractTask {

	private static final Logger logger = LogManager.getLogger(RunTask.class);

	private boolean resumeExecution = false;

	public RunTask(SEEAbstractFederate federate, ExecutionTask executionTask, int MAX_WAIT_TIME) {
		super(federate, executionTask, MAX_WAIT_TIME);
	}

	public RunTask(SEEAbstractFederate federate, ExecutionTask executionTask, boolean resumeExecution, int MAX_WAIT_TIME) {
		this(federate, executionTask, MAX_WAIT_TIME);
		this.resumeExecution = resumeExecution;
	}

	@Override
	public void run() {
		try {

			if(federate.getConfig().getFederateRole().equalsIgnoreCase("early") || 
					resumeExecution){

				federate.waitingForAnnouncement(SynchronizationPoint.MTR_RUN, MAX_WAIT_TIME);
				logger.info("SynchronizationPoint.MTR_RUN has been announced!");

				try {
					federate.achieveSynchronizationPoint(SynchronizationPoint.MTR_RUN);
					logger.info("SynchronizationPoint.MTR_RUN has been achieved!");
				} catch (SaveInProgress | RestoreInProgress | FederateNotExecutionMember
						| NotConnected | RTIinternalError | SynchronizationPointLabelNotAnnounced e) {
					e.printStackTrace();
				} 

				federate.waitingForSynchronization(SynchronizationPoint.MTR_RUN, MAX_WAIT_TIME);
				logger.info("Federate synchronized with SynchronizationPoint.MTR_RUN!");		
			}
			
			if(!resumeExecution)
				executionThread.start();
			else
				executionTask.resume();

			//reset SynchronizationPoint properties
			SynchronizationPoint.MTR_RUN.resetProperties();
			logger.trace("SynchronizationPoint.MTR_RUN properties have been reset");		


		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
