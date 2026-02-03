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

package org.see.skf.util.models;

import org.see.skf.annotations.InteractionClass;
import org.see.skf.annotations.Parameter;
import org.see.skf.util.encoding.MTRModeCoder;

/**
 * Representation of the ModeTransitionRequest interaction class in the SpaceFOM.
 *
 * @since 1.5
 */
@InteractionClass(name = "HLAinteractionRoot.ModeTransitionRequest")
public class ModeTransitionRequest {
    @Parameter(name = "execution_mode", coder = MTRModeCoder.class)
    private MTRMode executionMode;

    public ModeTransitionRequest() {
        /* Zero-arg constructor as required by the framework and the JavaBeans standard. */
        executionMode = MTRMode.MTR_UNDESIGNATED;
    }

    public MTRMode getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(MTRMode executionMode) {
        this.executionMode = executionMode;
    }

    /**
     * The run mode that can be requested by a federate other than the Master federate. There are only 3 valid Mode Transition Request (MTR) mode values: MTR_GOTO_RUN, MTR_GOTO_FREEZE, MTR_GOTO_SHUTDOWN.
     * Of these three valid mode requests, only 7 combinations of current execution mode and requested mode are valid:
     * <ol>
     *     <li>EXEC_MODE_UNINITIALIZED -&gt; EXEC_MODE_SHUTDOWN</li>
     *     <li>EXEC_MODE_INITIALIZED -&gt; EXEC_MODE_FREEZE</li>
     *     <li>EXEC_MODE_INITIALIZED -&gt; EXEC_MODE_SHUTDOWN</li>
     *     <li>EXEC_MODE_RUNNING -&gt; EXEC_MODE_FREEZE</li>
     *     <li>EXEC_MODE_RUNNING -&gt; EXEC_MODE_SHUTDOWN</li>
     *     <li>EXEC_MODE_FREEZE -&gt; EXEC_MODE_RUNNING</li>
     *     <li>EXEC_MODE_FREEZE -&gt; EXEC_MODE_SHUTDOWN</li>
     * </ol>
     *
     * @since 1.5
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
            return value;
        }
    }
}
