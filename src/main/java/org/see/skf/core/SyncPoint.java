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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An enum representation of all supported synchronization points in the SpaceFOM standard.
 *
 * @since 1.5
 */
public enum SyncPoint {
    INITIALIZATION_STARTED("initialization_started"),
    INITIALIZATION_COMPLETED("initialization_completed"),
    OBJECTS_DISCOVERED("objects_discovered"),
    ROOT_FRAME_DISCOVERED("root_frame_discovered"),
    MTR_RUN("mtr_run"),
    MTR_FREEZE("mtr_freeze"),
    MTR_SHUTDOWN("mtr_shutdown"),
    MPI_1("MPI1"),
    MPI_2("MPI2");

    private final String value;

    // The lifecycle of a sync point:
    // Initially, it is UNANNOUNCED (the announced variable is enough for this). Once the achieve sync point method is
    // called, it becomes UNACHIEVED. Pending the outcome of the registration, it ends up either as SUCCEEDED or FAILED.
    private Registration state;

    private final AtomicBoolean announced;
    private final AtomicBoolean registered;
    private final AtomicBoolean federationSynchronized;

    SyncPoint(String value) {
        this.value = value;
        announced = new AtomicBoolean(false);
        registered = new AtomicBoolean(false);
        federationSynchronized = new AtomicBoolean(false);
        state = Registration.UNACHIEVED;
    }

    public static synchronized SyncPoint query(String value) {
        for (SyncPoint sp : values()) {
            if (sp.value.equals(value)) {
                return sp;
            }
        }
        return null;
    }

    public static synchronized void resetAll() {
        for (SyncPoint sp : values()) {
            sp.reset();
        }
    }

    public String getLabel() {
        return value;
    }

    public boolean isFederationSynchronized() {
        return federationSynchronized.get();
    }

    void setFederationSynchronized() {
        federationSynchronized.set(true);
    }

    public void reset() {
        registered.set(false);
        federationSynchronized.set(false);
        announced.set(false);
        state = Registration.UNACHIEVED;
    }

    private enum Registration {
        FAILED,
        SUCCEEDED,
        UNACHIEVED
    }

    public boolean isRegistered() {
        return registered.get();
    }

    void registered(boolean flag) {
        registered.set(flag);

        if (flag) {
            state = Registration.SUCCEEDED;
        } else {
            state = Registration.FAILED;
        }
    }

    public boolean isAnnounced() {
        return announced.get();
    }

    void announced() {
        announced.set(true);
    }

    public synchronized boolean isAchieved() {
        return state != Registration.UNACHIEVED;
    }

    public synchronized boolean registrationSucceeded() {
        return state == Registration.SUCCEEDED;
    }
}
