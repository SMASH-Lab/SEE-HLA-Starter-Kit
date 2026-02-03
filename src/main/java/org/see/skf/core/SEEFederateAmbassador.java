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

import hla.rti1516_2025.FederateHandleSet;
import hla.rti1516_2025.SynchronizationPointFailureReason;
import hla.rti1516_2025.exceptions.FederateInternalError;
import hla.rti1516_2025.time.HLAinteger64Time;
import hla.rti1516_2025.time.LogicalTime;
import org.see.skf.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A complete federate ambassador implementation designed to complement the SEEAbstractFederate and its subclasses.
 *
 * @since 2.0
 */
public class SEEFederateAmbassador extends SKFederateAmbassador {
    private static final Logger logger = LoggerFactory.getLogger(SEEFederateAmbassador.class);

    private final Time simulationTime;

    private final AtomicBoolean advancing;
    private final AtomicBoolean regulating;
    private final AtomicBoolean constrained;

    public SEEFederateAmbassador() {
        simulationTime = new Time();
        advancing = new AtomicBoolean(false);
        regulating = new AtomicBoolean(false);
        constrained = new AtomicBoolean(false);
    }

    @Override
    public void timeConstrainedEnabled(LogicalTime<?, ?> time) {
        simulationTime.setFederateLogicalTime(time);
        setConstrained(true);
    }

    @Override
    public void timeRegulationEnabled(LogicalTime<?, ?> time) {
        simulationTime.setFederateLogicalTime(time);
        setRegulating(true);
    }

    @Override
    public void timeAdvanceGrant(LogicalTime<?, ?> time) {
        HLAinteger64Time convertedTime = (HLAinteger64Time) time;
        if (convertedTime.compareTo(simulationTime.getFederationLogicalTime()) >= 0) {
            simulationTime.setFederateLogicalTime(convertedTime);
            advancing.set(false);
        }
    }

    @Override
    public void announceSynchronizationPoint(String synchronizationPointLabel, byte[] userSuppliedTag) throws FederateInternalError {
        SyncPoint syncPoint = SyncPoint.query(synchronizationPointLabel);

        if (syncPoint != null) {
            syncPoint.announced();
            logger.debug("The synchronization point <{}> was announced.", synchronizationPointLabel);
        } else {
            logger.debug("An unsupported synchronization point <{}> was announced.", synchronizationPointLabel);
        }
    }

    @Override
    public void federationSynchronized(String synchronizationPointLabel, FederateHandleSet failedToSyncSet) throws FederateInternalError {
        SyncPoint syncPoint = SyncPoint.query(synchronizationPointLabel);

        if (syncPoint != null) {
            syncPoint.setFederationSynchronized();
            logger.debug("The synchronization point <{}> was achieved by the federation execution.", synchronizationPointLabel);
        } else {
            logger.debug("An unsupported synchronization point <{}> was achieved by the federation execution.", synchronizationPointLabel);
        }
    }

    @Override
    public void synchronizationPointRegistrationSucceeded(String synchronizationPointLabel) throws FederateInternalError {
        SyncPoint syncPoint = SyncPoint.query(synchronizationPointLabel);

        if (syncPoint != null) {
            syncPoint.registered(true);
            logger.debug("Successfully achieved the synchronization point <{}>.", synchronizationPointLabel);
        } else {
            logger.debug("An unsupported synchronization point <{}> was achieved.", synchronizationPointLabel);
        }
    }

    @Override
    public void synchronizationPointRegistrationFailed(String synchronizationPointLabel, SynchronizationPointFailureReason reason) throws FederateInternalError {
        SyncPoint syncPoint = SyncPoint.query(synchronizationPointLabel);

        if (syncPoint != null) {
            syncPoint.registered(false);
            logger.debug("Failed to achieve the synchronization point <{}>.", synchronizationPointLabel);
        } else {
            logger.debug("Failed to achieve an unsupported synchronization point <{}>.", synchronizationPointLabel);
        }
    }

    public final boolean isAdvancing() {
        return advancing.get();
    }

    public final void setAdvancing(boolean flag) {
        advancing.set(flag);
    }

    public final boolean isRegulating() {
        return regulating.get();
    }

    public final void setRegulating(boolean flag) {
        regulating.set(flag);
    }

    public final boolean isConstrained() {
        return constrained.get();
    }

    public final void setConstrained(boolean flag) {
        constrained.set(flag);
    }

    public Time getSimulationTime() {
        return simulationTime;
    }
}
