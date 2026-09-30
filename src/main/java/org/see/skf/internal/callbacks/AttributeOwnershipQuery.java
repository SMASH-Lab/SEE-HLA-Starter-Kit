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

import hla.rti1516_2025.AttributeHandle;
import hla.rti1516_2025.AttributeHandleSet;
import hla.rti1516_2025.ObjectInstanceHandle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class AttributeOwnershipQuery {

    private final ObjectInstanceHandle instanceHandle;

    private final Map<AttributeHandle, Boolean> handleToAcquisitionStatus;

    private final Map<AttributeHandle, String> handleToFederateName;

    AttributeOwnershipQuery(ObjectInstanceHandle instanceHandle, AttributeHandleSet set) {
        this.instanceHandle = instanceHandle;

        this.handleToAcquisitionStatus = new ConcurrentHashMap<>();
        set.forEach(handle -> this.handleToAcquisitionStatus.put(handle, false));

        this.handleToFederateName = new ConcurrentHashMap<>();
    }

    boolean isCompleted() {
        for (boolean status : this.handleToAcquisitionStatus.values()) {
            if (!status) {
                return false;
            }
        }

        return true;
    }

    void inform(AttributeHandleSet set, String ownerName) {
        set.forEach(handle -> {
            if (this.handleToAcquisitionStatus.containsKey(handle)) {
                this.handleToFederateName.put(handle, ownerName);
                this.handleToAcquisitionStatus.replace(handle, true);
            }
        });
    }

    ObjectInstanceHandle getInstanceHandle() {
        return this.instanceHandle;
    }

    Map<AttributeHandle, String> getResult() {
        return this.handleToFederateName;
    }
}
