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

import org.see.skf.core.annotations.Attribute;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.encoding.*;

/**
 * The Execution Configuration defines the base set of parameters necessary to coordinate federation and federate execution time lines
 * and execution mode transitions in a SISO Space Reference FOM compliant federation execution. An instance of this class is used to
 * represent the ExCO object instance created by the SpaceMaster federate.
 *
 * @since 1.0
 */
@ObjectClass(name = "HLAobjectRoot.ExecutionConfiguration")
public final class ExecutionConfiguration {

    @Attribute(name = "root_frame_name", coder = HLAunicodeStringCoder.class)
    private String rootFrameName;

    @Attribute(name = "scenario_time_epoch", coder = HLAfloat64LECoder.class)
    private Double scenarioTimeEpoch;

    @Attribute(name = "current_execution_mode", coder = ExecutionModeCoder.class)
    private ExecutionMode currentExecutionMode;

    @Attribute(name = "next_execution_mode", coder = ExecutionModeCoder.class)
    private ExecutionMode nextExecutionMode;

    @Attribute(name = "next_mode_scenario_time", coder = HLAfloat64LECoder.class)
    private Double nextModeScenarioTime;

    @Attribute(name = "next_mode_cte_time", coder = HLAfloat64LECoder.class)
    private Double nextModeCTETime;

    @Attribute(name = "least_common_time_step", coder = HLAinteger64BECoder.class)
    private Long leastCommonTimeStep;

    public ExecutionConfiguration() {
        this.rootFrameName = "";
        this.currentExecutionMode = ExecutionMode.EXEC_MODE_UNDESIGNATED;
        this.nextExecutionMode = ExecutionMode.EXEC_MODE_UNDESIGNATED;
    }

    public String getRootFrameName() {
        return rootFrameName;
    }

    public void setRootFrameName(String rootFrameName) {
        this.rootFrameName = rootFrameName;
    }

    public Double getScenarioTimeEpoch() {
        return scenarioTimeEpoch;
    }

    public void setScenarioTimeEpoch(Double scenarioTimeEpoch) {
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

    public Double getNextModeScenarioTime() {
        return nextModeScenarioTime;
    }

    public void setNextModeScenarioTime(Double nextModeScenarioTime) {
        this.nextModeScenarioTime = nextModeScenarioTime;
    }

    public Double getNextModeCTETime() {
        return nextModeCTETime;
    }

    public void setNextModeCTETime(Double nextModeCTETime) {
        this.nextModeCTETime = nextModeCTETime;
    }

    public Long getLeastCommonTimeStep() {
        return leastCommonTimeStep;
    }

    public void setLeastCommonTimeStep(Long leastCommonTimeStep) {
        this.leastCommonTimeStep = leastCommonTimeStep;
    }
}
