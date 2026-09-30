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

package org.see.skf.core;

/**
 * Mode Transition Request (MTR) values. This enumeration is used to request a specific mode transition. However, not all
 * mode transition requests are accepted for any given Run Mode. Refer to the mode transition validation table in the
 * Space Reference FOM documentation.
 *
 * @since 1.0
 */
public enum MTRMode {
    MTR_UNDESIGNATED((short) -1),
    MTR_GOTO_RUN((short) 2),
    MTR_GOTO_FREEZE((short) 3),
    MTR_GOTO_SHUTDOWN((short) 4);

    private final short value;

    MTRMode(short value) {
        this.value = value;
    }

    public static MTRMode query(short value) {
        for (MTRMode  mode : MTRMode.values()) {
            if (mode.value == value) {
                return mode;
            }
        }

        return null;
    }

    public short getValue() {
        return this.value;
    }
}
