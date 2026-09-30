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

import java.util.concurrent.CountDownLatch;

public final class RunState implements TransitiveState {

    private static final Logger logger = LoggerFactory.getLogger(RunState.class);

    private final SKAbstractFederate federate;
    private final TimeManager timeManager;

    public RunState(SKAbstractFederate federate, TimeManager timeManager) {
        this.federate = federate;
        this.timeManager = timeManager;
    }

    @Override
    public void transition(ExecutionMode nextExecutionMode) throws RTIexception {
        if (nextExecutionMode == ExecutionMode.EXEC_MODE_FREEZE) {
            CountDownLatch latch = new CountDownLatch(1);
            SyncPointListener listener = createFreezeModeSyncPointListener(latch);

            this.federate.addSyncPointListener(SRFOMSynchronizationPoint.MTR_FREEZE.getLabel(), listener);

            ExecutionConfiguration exCO = (ExecutionConfiguration) this.federate.queryObjectInstance("ExCO");
            if (exCO == null) {
                throw new ExCONotInitializedException("Cannot perform federate executive state transition as ExCO object instance values could not be retrieved in time.");
            }

            double nextModeScenarioTime = exCO.getNextModeScenarioTime();

            double timeToFreeze = nextModeScenarioTime - this.timeManager.getSimulationScenarioTime();
            while (timeToFreeze > 0.0) {
                this.federate.processRunJobs();
                this.timeManager.advanceTime();

                timeToFreeze -= 1.0;
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Federate thread interrupted while waiting for federation synchronization of MTR_FREEZE synchronization point.", e);
            }

            // Reset the following values for future freeze transitions.
            exCO.setNextExecutionMode(null);
            exCO.setNextModeScenarioTime(null);

            this.federate.removeSyncPointListener(listener);
        }
    }

    private SyncPointListener createFreezeModeSyncPointListener(CountDownLatch latch) {
        return new SyncPointListener() {
            @Override
            public void announced() {
                String freezeModeTransitionLabel = SRFOMSynchronizationPoint.MTR_FREEZE.getLabel();

                try {
                    federate.achieveSyncPoint(freezeModeTransitionLabel);
                    logger.debug("Achieved SRFOM <{}> sync point.", freezeModeTransitionLabel);

                    latch.countDown();
                } catch (RTIexception e) {
                    logger.error("Failed to achieve the SRFOM synchronization point <{}>.", freezeModeTransitionLabel, e);
                }
            }

            @Override
            public void federationSynchronized() {
                latch.countDown();
            }
        };
    }
}
