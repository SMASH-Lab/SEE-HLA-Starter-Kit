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

package org.see.skf.internal;

import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.TimeQueryReturn;
import hla.rti1516_2025.exceptions.*;
import hla.rti1516_2025.time.HLAinteger64Interval;
import hla.rti1516_2025.time.HLAinteger64Time;
import hla.rti1516_2025.time.HLAinteger64TimeFactory;

import org.see.skf.internal.callbacks.FederateCallbackManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TimeManager {

    private static final Logger logger = LoggerFactory.getLogger(TimeManager.class);

    private static final double UNINITIALIZED_JDT_VALUE = -1.0;
    private static final double HLT_PER_SECOND = 1000000.0;

    private final RTIambassador rtiAmbassador;

    private HLAinteger64TimeFactory timeFactory;
    private HLAinteger64Time logicalTime;
    private HLAinteger64Interval logicalTimeInterval;

    private final FederateCallbackManager callbackManager;

    private final long lookaheadValue;
    private double simulationElapsedTime;
    private double federationScenarioTimeEpoch;
    private double simulationScenarioTimeEpoch;
    private double simulationScenarioTime;

    private final AtomicBoolean isTimeAdvancing;

    public TimeManager(long lookaheadValue, FederateCallbackManager callbackManager) {
        this.lookaheadValue = lookaheadValue;
        this.callbackManager = callbackManager;

        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.simulationElapsedTime = 0.0;
        this.federationScenarioTimeEpoch = UNINITIALIZED_JDT_VALUE;
        this.simulationScenarioTimeEpoch = UNINITIALIZED_JDT_VALUE;
        this.simulationScenarioTime = UNINITIALIZED_JDT_VALUE;
        this.isTimeAdvancing = new AtomicBoolean(false);
    }

    public void constrainTime() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        Future<HLAinteger64Time> task = this.callbackManager.invokeTimeConstrainedCallback();

        if (task != null) {
            HLAinteger64Time newLogicalTime;
            try {
                newLogicalTime = task.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TimeInitializationException("Federate could not be time constrained.", e);
            } catch (ExecutionException e) {
                throw new TimeInitializationException("Unexpected exception thrown while trying to time constrain federate", e);
            }

            this.logicalTime = newLogicalTime;
            logger.info("Federate is now time constrained.");
        }
    }

    public void regulateTime() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (this.timeFactory == null) {
            throw new IllegalStateException("Cannot enable time regulation as no valid time factory has been set.");
        }

        HLAinteger64Interval lookaheadInLogicalTime = this.timeFactory.makeInterval(lookaheadValue);
        Future<HLAinteger64Time> task = this.callbackManager.invokeTimeRegulationCallback(lookaheadInLogicalTime);

        if (task != null) {
            HLAinteger64Time newLogicalTime;
            try {
                newLogicalTime = task.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TimeInitializationException("Federate could not be time constrained.", e);
            } catch (ExecutionException e) {
                throw new TimeInitializationException("Unexpected exception thrown while trying to time constrain federate", e);
            }

            this.logicalTime = newLogicalTime;
        }

        logger.info("Federate is now time regulated.");
    }

    public void advanceTime() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (this.timeFactory == null) {
            throw new IllegalStateException("Cannot send time advance request to RTI as no valid time factory has been internally set.");
        }

        if (!this.isTimeAdvancing.get()) {
            try {
                HLAinteger64Time newLogicalTime = this.logicalTime.add(this.logicalTimeInterval);
                dispatchTimeAdvanceRequest(newLogicalTime);

                advanceAllTimelines(this.logicalTime);
            } catch (IllegalTimeArithmetic e) {
                throw new RuntimeException("Could not advance time to <" + this.logicalTime.getValue() + ">.", e);
            }
        }
    }

    private void dispatchTimeAdvanceRequest(HLAinteger64Time targetLogicalTime) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        Future<HLAinteger64Time> task = this.callbackManager.invokeTimeAdvanceGrantCallback(targetLogicalTime);

        this.isTimeAdvancing.set(true);
        try {
            this.logicalTime = task.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Federate failed to advance logical time to <" + targetLogicalTime.getValue() + ">.", e);
        }
        this.isTimeAdvancing.set(false);
    }

    private void advanceAllTimelines(HLAinteger64Time newLogicalTime) {
        this.logicalTime = newLogicalTime;
        this.simulationElapsedTime += 1.0;
        this.simulationScenarioTime = computeSimulationScenarioTime(this.federationScenarioTimeEpoch, newLogicalTime);
    }

    public void advanceToLogicalTimeBoundary(long leastCommonTimeStep) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        if (this.timeFactory == null) {
            throw new IllegalStateException("Cannot enable time regulation as no valid time factory has been set.");
        }

        TimeQueryReturn timeQuery = rtiAmbassador.queryGALT();
        if (!timeQuery.timeIsValid) {
            throw new TimeInitializationException("Advance to HLA logical time boundary failed due to invalid GALT value.");
        } else {
            HLAinteger64Time galt = (HLAinteger64Time) timeQuery.time;
            long galtValue = galt.getValue();

            long logicalTimeBoundaryValue = computeLogicalTimeBoundary(leastCommonTimeStep, galtValue);
            HLAinteger64Time logicalTimeBoundary = this.timeFactory.makeTime(logicalTimeBoundaryValue);

            this.simulationScenarioTimeEpoch = computeSimulationScenarioTime(this.federationScenarioTimeEpoch, logicalTimeBoundary);
            dispatchTimeAdvanceRequest(logicalTimeBoundary);
        }
    }

    private double computeSimulationScenarioTime(double scenarioTimeEpoch, HLAinteger64Time logicalTime) {
        return scenarioTimeEpoch + (logicalTime.getValue() / HLT_PER_SECOND);
    }

    private long computeLogicalTimeBoundary(long leastCommonTimeStep, long greatestAvailableLogicalTime) {
        return (Math.floorDiv(greatestAvailableLogicalTime, leastCommonTimeStep) + 1) * leastCommonTimeStep;
    }

    public double getSimulationScenarioTime() {
        return this.simulationScenarioTime;
    }

    public void setFederationScenarioTimeEpoch(double federationScenarioTimeEpoch) {
        this.federationScenarioTimeEpoch = federationScenarioTimeEpoch;
    }

    public double getSimulationScenarioTimeEpoch() {
        return this.simulationScenarioTimeEpoch;
    }

    public void setSimulationScenarioTimeEpoch(double simulationScenarioTimeEpoch) {
        this.simulationScenarioTimeEpoch = simulationScenarioTimeEpoch;
    }

    public void initializeLogicalTimeComponents() throws FederateNotExecutionMember, NotConnected {
        this.timeFactory = (HLAinteger64TimeFactory) rtiAmbassador.getTimeFactory();
        this.logicalTime = this.timeFactory.makeInitial();
        this.logicalTimeInterval = this.timeFactory.makeInterval(this.lookaheadValue);
    }

    public boolean isTimeAdvancing() {
        return this.isTimeAdvancing.get();
    }
}
