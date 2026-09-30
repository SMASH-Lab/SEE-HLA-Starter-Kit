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

package org.see.skf.internal.callbacks;

import hla.rti1516_2025.*;
import hla.rti1516_2025.exceptions.*;
import hla.rti1516_2025.time.HLAinteger64Interval;
import hla.rti1516_2025.time.HLAinteger64Time;
import org.see.skf.internal.HLAUtilityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

public final class FederateCallbackManager {

    private static final Logger logger = LoggerFactory.getLogger(FederateCallbackManager.class);

    private final RTIambassador rtiAmbassador;

    private final ExecutorService executor;

    private final Map<String, FederateCallback<Boolean>> nameReservationCallbacks;
    private final Map<AttributeOwnershipQuery, FederateCallback<Map<AttributeHandle, String>>> attributeOwnershipQueries;

    private FederateCallback<HLAinteger64Time> timeConstrainedEnabledCallback;
    private FederateCallback<HLAinteger64Time> timeRegulationEnabledCallback;
    private FederateCallback<HLAinteger64Time> timeAdvanceGrantCallback;

    public FederateCallbackManager(ExecutorService executor) {
        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.executor = executor;

        this.nameReservationCallbacks = new ConcurrentHashMap<>();
        this.attributeOwnershipQueries = new ConcurrentHashMap<>();
    }

    public Future<Boolean> invokeNameReservationCallback(String objectInstanceName) throws FederateNotExecutionMember, RestoreInProgress, IllegalName, NotConnected, RTIinternalError, SaveInProgress {
        FederateCallback<Boolean> callback = new FederateCallbackImpl<>();
        this.nameReservationCallbacks.put(objectInstanceName, callback);

        FutureTask<Boolean> task = callback.getTask();
        this.executor.submit(task);

        rtiAmbassador.reserveObjectInstanceName(objectInstanceName);

        return task;
    }

    public void completeNameReservationCallback(String objectInstanceName, boolean outcomeValue) {
        for (Map.Entry<String, FederateCallback<Boolean>> entry : this.nameReservationCallbacks.entrySet()) {
            String instanceName = entry.getKey();
            FederateCallback<Boolean> callback = entry.getValue();

            if (instanceName.equals(objectInstanceName)) {
                callback.complete(outcomeValue);
                this.nameReservationCallbacks.remove(instanceName);
            }
        }
    }

    public Future<HLAinteger64Time> invokeTimeConstrainedCallback() throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.timeConstrainedEnabledCallback = new FederateCallbackImpl<>();
        FutureTask<HLAinteger64Time> task = this.timeConstrainedEnabledCallback.getTask();
        this.executor.submit(task);

        try {
            rtiAmbassador.enableTimeConstrained();
        } catch (InTimeAdvancingState | RequestForTimeConstrainedPending | TimeConstrainedAlreadyEnabled e) {
            logger.warn("Redundant attempt to time constrain this federate.", e);
        }

        return task;
    }

    public void completeTimeConstrainedCallback(HLAinteger64Time newLogicalTime) {
        if (this.timeConstrainedEnabledCallback != null) {
            this.timeConstrainedEnabledCallback.complete(newLogicalTime);
            this.timeConstrainedEnabledCallback = null;
        }
    }

    public Future<HLAinteger64Time> invokeTimeRegulationCallback(HLAinteger64Interval lookaheadInLogicalTime) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.timeRegulationEnabledCallback = new FederateCallbackImpl<>();
        FutureTask<HLAinteger64Time> task = this.timeRegulationEnabledCallback.getTask();
        this.executor.submit(task);

        try {
            rtiAmbassador.enableTimeRegulation(lookaheadInLogicalTime);
        } catch (InTimeAdvancingState | RequestForTimeRegulationPending | TimeRegulationAlreadyEnabled e) {
            logger.warn("Redundant attempt to time regulate this federate.", e);
        } catch (InvalidLookahead e) {
            throw new RuntimeException(e);
        }

        return task;
    }

    public void completeTimeRegulationCallback(HLAinteger64Time newLogicalTime) {
        if (this.timeRegulationEnabledCallback != null) {
            this.timeRegulationEnabledCallback.complete(newLogicalTime);
            this.timeRegulationEnabledCallback = null;
        }
    }

    public Future<HLAinteger64Time> invokeTimeAdvanceGrantCallback(HLAinteger64Time nextLogicalTime) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        this.timeAdvanceGrantCallback = new FederateCallbackImpl<>();
        FutureTask<HLAinteger64Time> task = this.timeAdvanceGrantCallback.getTask();
        this.executor.submit(task);

        try {
            rtiAmbassador.timeAdvanceRequest(nextLogicalTime);
        } catch (LogicalTimeAlreadyPassed | InvalidLogicalTime e) {
            throw new RuntimeException("Federate time is completely out of sync with the federation execution time.", e);
        } catch (InTimeAdvancingState | RequestForTimeRegulationPending | RequestForTimeConstrainedPending e) {
            logger.warn("Unsuccessful attempt to advance federate time.", e);
        }

        return task;
    }

    public void completeTimeAdvanceGrantCallback(HLAinteger64Time grantedTime) {
        if (this.timeAdvanceGrantCallback != null) {
            this.timeAdvanceGrantCallback.complete(grantedTime);
            this.timeAdvanceGrantCallback = null;
        }
    }

    public Future<Map<AttributeHandle, String>> invokeAttributeOwnershipQuery(ObjectInstanceHandle objectInstance, AttributeHandleSet set) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        AttributeOwnershipQuery query = new AttributeOwnershipQuery(objectInstance, set);
        FederateCallback<Map<AttributeHandle, String>> callback = new FederateCallbackImpl<>();
        this.attributeOwnershipQueries.put(query, callback);

        FutureTask<Map<AttributeHandle, String>> task = callback.getTask();
        this.executor.submit(task);

        try {
            rtiAmbassador.queryAttributeOwnership(objectInstance, set);
        } catch (AttributeNotDefined | ObjectInstanceNotKnown e) {
            throw new RuntimeException("Failed to query attribute ownership.", e);
        }

        return task;
    }

    public void completeAttributeOwnershipQuery(ObjectInstanceHandle instanceHandle, AttributeHandleSet set, String ownerName) {
        for (Map.Entry<AttributeOwnershipQuery, FederateCallback<Map<AttributeHandle, String>>> entry : this.attributeOwnershipQueries.entrySet()) {
            AttributeOwnershipQuery query = entry.getKey();
            FederateCallback<Map<AttributeHandle, String>> callback = entry.getValue();

            if (instanceHandle.equals(query.getInstanceHandle())) {
                query.inform(set, ownerName);
                if (query.isCompleted()) {
                    callback.complete(query.getResult());
                }
            }
        }
    }
}
