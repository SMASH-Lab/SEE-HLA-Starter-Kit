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

import org.see.skf.annotations.Attribute;
import org.see.skf.annotations.ObjectClass;
import org.see.skf.runtime.ScopeLevel;
import org.see.skf.core.PropertyChangeSubject;
import org.see.skf.util.encoding.ExecutionModeCoder;
import org.see.skf.util.encoding.HLAfloat64LECoder;
import org.see.skf.util.encoding.HLAinteger64BECoder;
import org.see.skf.util.encoding.HLAunicodeStringCoder;

import java.io.Serializable;

/**
 * Representation of the ExecutionConfiguration object class in the SpaceFOM.
 *
 * @since 1.5
 */
@ObjectClass(name = "HLAobjectRoot.ExecutionConfiguration")
public final class ExecutionConfiguration extends PropertyChangeSubject implements Serializable {
    @Attribute(name = "root_frame_name", coder = HLAunicodeStringCoder.class, scope = ScopeLevel.SUBSCRIBE)
    private String rootFrameName;

    @Attribute(name = "scenario_time_epoch", coder = HLAfloat64LECoder.class, scope = ScopeLevel.SUBSCRIBE)
    private double scenarioTimeEpoch;

    @Attribute(name = "current_execution_mode", coder = ExecutionModeCoder.class, scope = ScopeLevel.SUBSCRIBE)
    private ExecutionMode currentExecutionMode;

    @Attribute(name = "next_execution_mode", coder = ExecutionModeCoder.class, scope = ScopeLevel.SUBSCRIBE)
    private ExecutionMode nextExecutionMode;

    @Attribute(name = "next_mode_scenario_time", coder = HLAfloat64LECoder.class, scope = ScopeLevel.SUBSCRIBE)
    private double nextModeScenarioTime;

    @Attribute(name = "next_mode_cte_time", coder = HLAfloat64LECoder.class, scope = ScopeLevel.SUBSCRIBE)
    private double nextModeCTETime;

    @Attribute(name = "least_common_time_step", coder = HLAinteger64BECoder.class, scope = ScopeLevel.SUBSCRIBE)
    private long leastCommonTimeStep;

    public ExecutionConfiguration() {
        /* Zero-arg constructor as required by the framework and the JavaBeans standard. */
        rootFrameName = "";
        currentExecutionMode = ExecutionMode.EXEC_MODE_UNDESIGNATED;
        nextExecutionMode = ExecutionMode.EXEC_MODE_UNDESIGNATED;
    }

    public long getLeastCommonTimeStep() {
        return leastCommonTimeStep;
    }

    public void setLeastCommonTimeStep(long leastCommonTimeStep) {
        this.leastCommonTimeStep = leastCommonTimeStep;
    }

    public String getRootFrameName() {
        return rootFrameName;
    }

    public void setRootFrameName(String rootFrameName) {
        this.rootFrameName = rootFrameName;
    }

    public double getScenarioTimeEpoch() {
        return scenarioTimeEpoch;
    }

    public void setScenarioTimeEpoch(double scenarioTimeEpoch) {
        this.scenarioTimeEpoch = scenarioTimeEpoch;
    }

    public ExecutionMode getCurrentExecutionMode() {
        return currentExecutionMode;
    }

    public void setCurrentExecutionMode(ExecutionMode currentExecutionMode) {
        this.currentExecutionMode = currentExecutionMode;
    }

    public ExecutionMode getNextExecutionMode() {
        return nextExecutionMode;
    }

    public void setNextExecutionMode(ExecutionMode nextExecutionMode) {
        this.nextExecutionMode = nextExecutionMode;
    }

    public double getNextModeScenarioTime() {
        return nextModeScenarioTime;
    }

    public void setNextModeScenarioTime(double nextModeScenarioTime) {
        this.nextModeScenarioTime = nextModeScenarioTime;
    }

    public double getNextModeCTETime() {
        return nextModeCTETime;
    }

    public void setNextModeCTETime(double nextModeCTETime) {
        this.nextModeCTETime = nextModeCTETime;
    }

    /**
     * The mode for running the federation execution. This enumeration type is used for coordinating transitions between
     * federation execution run states.
     *
     * @since 1.5
     */
    public enum ExecutionMode {
        EXEC_MODE_UNDESIGNATED((short) -1),
        EXEC_MODE_UNINITIALIZING((short) 0),
        EXEC_MODE_INITIALIZED((short) 1),
        EXEC_MODE_RUNNING((short) 2),
        EXEC_MODE_FREEZE((short) 3),
        EXEC_MODE_SHUTDOWN((short) 4);

        private final short value;

        ExecutionMode(short value) {
            this.value = value;
        }

        public static ExecutionMode query(short value) {
            for (ExecutionMode mode : values()) {
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
