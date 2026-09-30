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

import org.see.skf.core.ExecutionMode;
import org.see.skf.core.SKAbstractFederate;
import org.see.skf.core.SyncPointListener;
import org.see.skf.internal.SRFOMSynchronizationPoint;

import java.util.concurrent.CountDownLatch;

public final class FreezeState implements TransitiveState {

    private final SKAbstractFederate federate;

    public FreezeState(SKAbstractFederate federate) {
        this.federate = federate;
    }

    @Override
    public void transition(ExecutionMode nextExecutionMode) {
        if (nextExecutionMode == ExecutionMode.EXEC_MODE_RUNNING) {
            CountDownLatch latch = new CountDownLatch(1);
            SyncPointListener listener = createFederationSynchronizedToFreezeListener(latch);

            String runModeTransitionLabel = SRFOMSynchronizationPoint.MTR_RUN.getLabel();
            this.federate.addSyncPointListener(runModeTransitionLabel, listener);

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Federate thread interrupted while waiting for federation synchronization of the SRFOM <" + runModeTransitionLabel + "> synchronization point.", e);
            }

            this.federate.removeSyncPointListener(listener);
        }
    }

    private SyncPointListener createFederationSynchronizedToFreezeListener(CountDownLatch latch) {
        return new SyncPointListener() {
            @Override
            public void announced() {
                // Ignore.
            }

            @Override
            public void federationSynchronized() {
                latch.countDown();
            }
        };
    }
}
