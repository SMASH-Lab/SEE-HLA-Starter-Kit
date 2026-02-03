/*****************************************************************
 SEE HLA Starter Kit Framework -  A Java library that supports
 the development of HLA Federates in the Simulation Exploration
 Experience (SEE) program.

 Copyright (c) 2014, 2026 SMASH Lab - University of Calabria
 (Italy), Hridyanshu Aatreya - Modelling & Simulation Group (MSG)
 at Brunel University of London. All rights reserved.

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

package org.see.skf.core;

import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.TimeQueryReturn;
import hla.rti1516_2025.exceptions.*;
import hla.rti1516_2025.time.HLAinteger64Time;
import hla.rti1516_2025.time.HLAinteger64TimeFactory;
import org.see.skf.conf.FederateConfiguration;
import org.see.skf.exceptions.DeadlineReachedException;
import org.see.skf.time.Time;
import org.see.skf.util.models.ExecutionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A complete federate implementation for SEE. It contains the necessary of methods needed for a SpaceFOM federate.
 * Depending on the role of the federate i.e., early or late joiner, these methods must be called in the sequence put
 * forth in the SpaceFOM standard.
 *
 * @since 1.5
 */
public abstract class SEEAbstractFederate extends SKBaseFederate {
    private static final Logger logger = LoggerFactory.getLogger(SEEAbstractFederate.class);
    private static long threadWaitInterval = 10L;

    private final SEEFederateAmbassador federateAmbassador;
    private final ExecutiveState state;
    private final Process process;

    protected SEEAbstractFederate(SEEFederateAmbassador federateAmbassador, FederateConfiguration config) {
        super(federateAmbassador, config);

        this.federateAmbassador = federateAmbassador;
        process = new Process();
        state = new ExecutiveState();
    }

    /**
     * Begin executing the simulation.
     */
    public void startExecution() {
        state.gotoRun(false);
    }

    /**
     * Enter the SpaceFOM freeze executive control state and pause all simulation activity indefinitely.
     */
    public void freezeExecution() {
        state.gotoFreeze();
    }

    /**
     * Leave the SpaceFOM freeze executive control state and resume simulation activity.
     */
    public void resumeExecution() {
        state.gotoRun(true);
    }

    /**
     * Disconnect from the federation execution and terminate the simulation.
     */
    public void shutdownExecution() {
        state.gotoShutdown();
    }

    /**
     * This method should only be called post ExCO-discovery or there is a risk of inducing a time regulation failure
     * due to missing look ahead interval value.
     */
    public void setupTimeManagement() {
        Time simulationTime = federateAmbassador.getSimulationTime();
        FederateConfiguration config = getConfiguration();
        Long lookAhead = config.lookAhead();

        simulationTime.setLookAhead(lookAhead);

        try {
            configureTimePolicy();
            if (getConfiguration().federateRole().equalsIgnoreCase("late")) {
                advanceToHLTB();
            }
        } catch (RTIexception e) {
            throw new IllegalStateException("Unexpected error encountered while trying to configure time regulation and constraints.", e);
        }
    }

    private void configureTimePolicy() throws RTIexception {
        RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        FederateConfiguration config = getConfiguration();

        if (config.timeConstrained()) {
            rtiAmbassador.enableTimeConstrained();
            while (!federateAmbassador.isConstrained()) {
                try {
                    Thread.sleep(threadWaitInterval);
                } catch (InterruptedException e) {
                    logger.warn("Program thread was interrupted while waiting for the RTI to time constrain this federate.");
                    Thread.currentThread().interrupt();
                }
            }
            logger.debug("The RTI is now time constraining this federate.");
        }

        if (config.timeRegulating()) {
            Time simulationTime = federateAmbassador.getSimulationTime();
            rtiAmbassador.enableTimeRegulation(simulationTime.getLookAheadAsLogicalTime());
            while (!federateAmbassador.isRegulating()) {
                try {
                    Thread.sleep(threadWaitInterval);
                } catch (InterruptedException e) {
                    logger.warn("Program thread was interrupted while waiting for the RTI to enable time regulation for this federate.");
                    Thread.currentThread().interrupt();
                }
            }
            logger.debug("The RTI is now time regulating this federate.");
        }

        // In line with the SpaceFOM standard, asynchronous delivery is disallowed for late joiners.
        if (config.asynchronousDelivery() && !config.federateRole().equalsIgnoreCase("late")) {
            rtiAmbassador.enableAsynchronousDelivery();
            logger.debug("Asynchronous delivery of messages has been enabled for this federate.");
        }
    }

    /**
     * Advances the federate to the HLA logical time boundary (HLTB).
     */
    public final void advanceToHLTB() throws RTIexception {
        RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        HLAinteger64TimeFactory timeFactory = HLAUtilityFactory.INSTANCE.getTimeFactory();
        ExecutionConfiguration executionConfiguration = (ExecutionConfiguration) queryRemoteObjectInstance("ExCO");

        if (executionConfiguration == null) {
            logger.error("Failed to advance to the HLA logical time boundary because the ExCO object instance was not found.");
            return;
        }

        TimeQueryReturn galtQuery = rtiAmbassador.queryGALT();
        if (galtQuery.timeIsValid) {
            HLAinteger64Time galt = (HLAinteger64Time) galtQuery.time;
            long lcts = executionConfiguration.getLeastCommonTimeStep();
            long hltbValue = ((galt.getValue() / lcts) + 1) * 1000000;
            HLAinteger64Time hltb = timeFactory.makeTime(hltbValue);

            Time simulationTime = federateAmbassador.getSimulationTime();
            simulationTime.setFederationLogicalTime(hltb);
            advanceTime(hltb);
        } else {
            throw new RTIexception("Failed to advance time because the RTI returned an invalid Greatest Available Logical Time (GALT) value.");
        }
    }

    /**
     * Advances the federate from its current time step to the HLA logical time provided.
     * @param timeStep The HLA logical time the federate should advance to.
     */
    public final void advanceTime(HLAinteger64Time timeStep) throws InTimeAdvancingState, FederateNotExecutionMember, RestoreInProgress, RequestForTimeConstrainedPending, NotConnected, LogicalTimeAlreadyPassed, InvalidLogicalTime, RTIinternalError, SaveInProgress, RequestForTimeRegulationPending {
        RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        rtiAmbassador.timeAdvanceRequest(timeStep);
        federateAmbassador.setAdvancing(true);
    }

    /**
     * Activities performed by the simulation should be done here. It is called once per tick.
     */
    public abstract void update();

    /**
     * Registers a federation-wide synchronization point that must be achieved by all federates.
     *
     * @param syncPoint A supported synchronization point in the SpaceFOM standard.
     */
    public void registerSyncPoint(SyncPoint syncPoint) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        String syncPointLabel = syncPoint.getLabel();

        rtiAmbassador.registerFederationSynchronizationPoint(syncPointLabel, null);
    }

    /**
     * Signals that the federate has achieved a previously-announced synchronization point.
     *
     * @param syncPoint A supported synchronization point in the SpaceFOM standard.
     * @param flag true or false depending on if the synchronization point was achieved or not.
     */
    public void achieveSyncPoint(SyncPoint syncPoint, boolean flag) throws SynchronizationPointLabelNotAnnounced, FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        RTIambassador rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        String syncPointLabel = syncPoint.getLabel();

        rtiAmbassador.synchronizationPointAchieved(syncPointLabel, flag);
    }

    /**
     * Waits for a synchronization point to be announced within the supplied time period.
     * @param syncPoint The SpaceFOM sync point.
     * @param maxWaitingTime The anticipated time period to wait for the sync point to be announced.
     * @return true or false depending on whether the synchronization point announcement was successful or not.
     */
    public boolean awaitSyncPointAnnouncement(SyncPoint syncPoint, int maxWaitingTime) {
        long deadline = System.currentTimeMillis() + maxWaitingTime;

        while (!syncPoint.isAnnounced()) {
            try {
                Thread.sleep(threadWaitInterval);
                if (System.currentTimeMillis() >= deadline) {
                    throw new DeadlineReachedException("The synchronization point <" + syncPoint.getLabel() + "> was not announced within the specified waiting period.");
                }
            } catch (InterruptedException e) {
                logger.error("Program thread was interrupted while waiting for the synchronization point <{}> to be announced.", syncPoint.getLabel());
                Thread.currentThread().interrupt();
            } catch (DeadlineReachedException e) {
                return false;
            }
        }

        return true;
    }

    /**
     * Waits for a synchronization point to be achieved by the federation within the supplied time period.
     * @param syncPoint The SpaceFOM sync point.
     * @param maxWaitingTime The anticipated time period to wait for the sync point to be achieved.
     * @return true or false depending on whether the federation was successfully synchronized or not.
     */
    public boolean awaitFederationSynchronization(SyncPoint syncPoint, int maxWaitingTime) {
        long deadline = System.currentTimeMillis() + maxWaitingTime;

        while (!syncPoint.isFederationSynchronized()) {
            try {
                Thread.sleep(threadWaitInterval);
                if (System.currentTimeMillis() >= deadline) {
                    throw new DeadlineReachedException("Failed to achieve the synchronization point <" + syncPoint.getLabel() + "> within the specified waiting period.");
                }
            } catch (InterruptedException e) {
                logger.error("Program thread was interrupted while waiting for the federation to achieve the synchronization point <{}>.", syncPoint.getLabel());
                Thread.currentThread().interrupt();
            } catch (DeadlineReachedException e) {
                return false;
            }
        }

        return true;
    }

    public Time getSimulationTime() {
        return federateAmbassador.getSimulationTime();
    }

    public static long getThreadWaitInterval() {
        return threadWaitInterval;
    }

    public static void setThreadWaitInterval(long interval) {
        threadWaitInterval = interval;
    }

    @Override
    public SEEFederateAmbassador getFederateAmbassador() {
        return federateAmbassador;
    }

    private final class Process implements Runnable {
        private static final long MICROSECONDS_PER_CYCLE = 1000000L;

        private final SEEFederateAmbassador federateAmbassador;
        private final Time simulationTime;

        private final AtomicBoolean running;
        private final AtomicBoolean suspended;

        private long executionCounter;

        public Process() {
            this.federateAmbassador = getFederateAmbassador();
            this.simulationTime = getSimulationTime();

            running = new AtomicBoolean(true);
            suspended = new AtomicBoolean(false);
        }

        @Override
        public void run() {
            while (isRunning()) {
                simulationTime.setTimeCyclesExecuted(executionCounter * MICROSECONDS_PER_CYCLE);

                waitForTimeAdvanceGrant();
                update();

                synchronized (this) {
                    while (isSuspended()) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            logger.warn("Program thread was interrupted in suspended state waiting for the federation execution to resume activities.");
                            Thread.currentThread().interrupt();
                        }
                    }
                }

                try {
                    // There is a possibility that we've got the go-ahead to shut down on an event listener thread, in
                    // which case attempting to advance time could be catastrophic. This check allows a graceful exit.
                    if (isRunning()) {
                        advanceTime(simulationTime.nextTimeStep());
                    }
                } catch (RTIexception e) {
                    throw new IllegalStateException("The federate encountered an unexpected error when trying to advance to the next time step.", e);
                }

                ++executionCounter;
            }

            try {
                resignFederationExecution();
            } catch (Exception e) {
                throw new IllegalStateException("Unexpected error encountered when attempting to resign and disconnect from the federation execution.", e);
            }
        }

        public void waitForTimeAdvanceGrant() {
            while (federateAmbassador.isAdvancing() && isRunning()) {
                try {
                    Thread.sleep(threadWaitInterval);
                } catch (InterruptedException e) {
                    logger.warn("Program thread was interrupted while waiting for time advance grant from the RTI.");
                    Thread.currentThread().interrupt();
                }
            }
        }

        public void suspend() {
            suspended.set(true);
        }

        public synchronized void resume() {
            suspended.set(false);
            notifyAll();
        }

        public void shutdown() {
            running.set(false);
        }

        public boolean isRunning() {
            return running.get();
        }

        public boolean isSuspended() {
            return suspended.get();
        }
    }

    private final class ExecutiveState {
        public void gotoRun(boolean resumeFlag) {
            new Thread(() -> {
                if (resumeFlag) {
                    process.resume();
                } else {
                    process.run();
                }
            }).start();
        }

        public void gotoFreeze() {
            // TODO - Implement federate freeze sequence.
        }

        public void gotoShutdown() {
            process.shutdown();
        }
    }
}
