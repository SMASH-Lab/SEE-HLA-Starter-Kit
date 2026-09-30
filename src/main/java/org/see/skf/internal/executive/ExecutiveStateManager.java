/*****************************************************************
 SEE HLA Starter Kit Framework -  A Java framework for developing
 SRFOM-compliant HLA Federates in the Simulation Exploration
 Experience (SEE) program.

 Copyright (c) 2014, 2026 SMASH Lab - University of Calabria
 (Italy), Hridyanshu Aatreya - Modelling & Simulation Group (MSG)
 at Brunel University of London (UK). All rights reserved.

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

package org.see.skf.internal.executive;

import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.*;
import org.see.skf.internal.ExCONotInitializedException;
import org.see.skf.core.ExecutionMode;
import org.see.skf.internal.SRFOMSynchronizationPoint;
import org.see.skf.internal.TimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExecutiveStateManager {

    private static final Logger logger = LoggerFactory.getLogger(ExecutiveStateManager.class);

    private final SKAbstractFederate federate;
    private final TransitiveState runState;
    private final TransitiveState freezeState;

    private final TimeManager timeManager;

    private volatile ExecutionMode localExecutionMode;
    private volatile ExecutionMode nextExecutionMode;

    public ExecutiveStateManager(SKAbstractFederate federate, TimeManager timeManager) {
        this.federate = federate;
        this.timeManager = timeManager;
        this.runState = new RunState(federate, timeManager);
        this.freezeState = new FreezeState(federate);

        this.federate.addSyncPointListener(SRFOMSynchronizationPoint.MTR_RUN.getLabel(), createRunModeAnnouncedListener());
    }

    private void init() {
        // Perfectly safe cast because we properly vet the existence and type for the ExCO object instance early on.
        ExecutionConfiguration exCO = (ExecutionConfiguration) this.federate.queryObjectInstance("ExCO");

        if (exCO == null) {
            throw new ExCONotInitializedException("Cannot proceed with federate execution because ExCO attribute values were not properly initialized.");
        }

        this.localExecutionMode = exCO.getCurrentExecutionMode();
        this.nextExecutionMode = exCO.getNextExecutionMode();
    }

    public void run() throws RTIexception {
        init();

        while (this.localExecutionMode != ExecutionMode.EXEC_MODE_SHUTDOWN) {
            if (this.localExecutionMode != this.nextExecutionMode) {
                if (this.nextExecutionMode != ExecutionMode.EXEC_MODE_SHUTDOWN) {
                    TransitiveState state = getTransitiveState(this.localExecutionMode);
                    state.transition(this.nextExecutionMode);
                }

                this.localExecutionMode = this.nextExecutionMode;
                logger.info("Federate execution mode set to: {}.", this.localExecutionMode);
            }

            if (this.localExecutionMode == ExecutionMode.EXEC_MODE_RUNNING) {
                runModeUpdate();
            }
        }

        synchronized (this) {
            notifyAll();
        }

        this.federate.processShutdownJobs();
    }

    private void runModeUpdate() throws RTIexception {
        this.federate.processRunJobs();
        this.timeManager.advanceTime();
    }

    private TransitiveState getTransitiveState(ExecutionMode executionMode) {
        return executionMode == ExecutionMode.EXEC_MODE_RUNNING ? this.runState : this.freezeState;
    }

    public synchronized ExecutionMode getLocalExecutionMode() {
        return this.localExecutionMode;
    }

    public synchronized void changeExecutionMode(ExecutionMode executionMode) {
        this.nextExecutionMode = executionMode;
    }

    private SyncPointListener createRunModeAnnouncedListener() {
        return new SyncPointListener() {
            @Override
            public void announced() {
                String runModeTransitionLabel = SRFOMSynchronizationPoint.MTR_RUN.getLabel();

                try {
                    federate.achieveSyncPoint(runModeTransitionLabel);
                    logger.debug("Achieved SRFOM <{}> sync point.", runModeTransitionLabel);
                } catch (RTIexception e) {
                    logger.error("Failed to achieve the SRFOM synchronization point <{}>.", runModeTransitionLabel, e);
                }

                changeExecutionMode(ExecutionMode.EXEC_MODE_RUNNING);
            }

            @Override
            public void federationSynchronized() {
                // Ignore.
            }
        };
    }
}
