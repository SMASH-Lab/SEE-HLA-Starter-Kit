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
import hla.rti1516_2025.exceptions.*;
import org.see.skf.core.SyncPointListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SyncPointManager {

    private static final Logger logger = LoggerFactory.getLogger(SyncPointManager.class);

    private final RTIambassador rtiAmbassador;

    private final Map<String, Set<SyncPointListener>> labelToListeners;

    public SyncPointManager() {
        this.rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.labelToListeners = new ConcurrentHashMap<>();
    }

    public void achieveSyncPoint(String label) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        try {
            rtiAmbassador.synchronizationPointAchieved(label);
        } catch (SynchronizationPointLabelNotAnnounced e) {
            logger.warn("Cannot achieve the synchronization point <{}> because it has not yet been announced.", label);
        }
    }

    public void achieveSyncPoint(String label, boolean outcome) throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError, SaveInProgress {
        try {
            rtiAmbassador.synchronizationPointAchieved(label, outcome);
        } catch (SynchronizationPointLabelNotAnnounced e) {
            if (outcome) {
                logger.warn("Could not notify RTI of successful achievement of the synchronization point {} because it has not yet been announced.", label);
            } else {
                logger.warn("Could not notify RTI of unsuccessful achievement of the synchronization point {} because it has not yet been announced.", label);
            }
        }
    }

    public void addSyncPointListener(String label, SyncPointListener listener) {
        if (label == null || listener == null) {
            return;
        }

        this.labelToListeners
                .computeIfAbsent(label, set -> new HashSet<>())
                .add(listener);
    }

    public void removeSyncPointListener(SyncPointListener listener) {
        for (Map.Entry<String, Set<SyncPointListener>> entry : this.labelToListeners.entrySet()) {
            String label = entry.getKey();
            Set<SyncPointListener> listeners = entry.getValue();

            if (listeners.contains(listener)) {
                listeners.remove(listener);

                if (listeners.isEmpty()) {
                    this.labelToListeners.remove(label);
                }

                break;
            }
        }
    }

    public void syncPointAnnounced(String label) {
        Set<SyncPointListener> listeners = this.labelToListeners.get(label);

        if (!listeners.isEmpty()) {
            listeners.forEach(SyncPointListener::announced);
        }
    }

    public void federationSynchronized(String label) {
        Set<SyncPointListener> listeners = this.labelToListeners.get(label);

        if (!listeners.isEmpty()) {
            listeners.forEach(SyncPointListener::federationSynchronized);
        }
    }
}
