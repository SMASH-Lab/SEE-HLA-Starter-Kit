package skf.model.object.executionConfiguration;

import skf.coder.HLAfloat64LECoder;
import skf.coder.HLAinteger64BECoder;
import skf.coder.HLAunicodeStringCoder;
import skf.model.object.annotations.Attribute;
import skf.model.object.annotations.ObjectClass;

@ObjectClass(name = "ExecutionConfiguration")
public class ExecutionConfiguration {

	@Attribute(name = "root_frame_name", coder = HLAunicodeStringCoder.class)
	private String root_frame_name = null;

	@Attribute(name = "scenario_time_epoch", coder = HLAfloat64LECoder.class)
	private Double scenario_time_epoch = null;

	@Attribute(name = "next_mode_scenario_time", coder = HLAfloat64LECoder.class)
	private Double next_mode_scenario_time = null;

	@Attribute(name = "next_mode_cte_time", coder = HLAfloat64LECoder.class)
	private Double next_mode_cte_time = null;

	@Attribute(name = "current_execution_mode", coder = ExecutionModeCoder.class)
	private ExecutionMode current_execution_mode = null;

	@Attribute(name = "next_execution_mode", coder = ExecutionModeCoder.class)
	private ExecutionMode next_execution_mode = null;
	
	@Attribute(name = "least_common_time_step", coder = HLAinteger64BECoder.class)
	private Long least_common_time_step = null;

	public ExecutionConfiguration() {}
	
	public ExecutionConfiguration(String root_frame_name,
			Double scenario_time_epoch, Double next_mode_scenario_time,
			Double next_mode_cte_time, ExecutionMode current_execution_mode,
			ExecutionMode next_execution_mode, Long least_common_time_step) {
		this.root_frame_name = root_frame_name;
		this.scenario_time_epoch = scenario_time_epoch;
		this.next_mode_scenario_time = next_mode_scenario_time;
		this.next_mode_cte_time = next_mode_cte_time;
		this.current_execution_mode = current_execution_mode;
		this.next_execution_mode = next_execution_mode;
		this.least_common_time_step = least_common_time_step;
	}

	/**
	 * @return the root_frame_name
	 */
	public String getRoot_frame_name() {
		return root_frame_name;
	}

	/**
	 * @param root_frame_name the root_frame_name to set
	 */
	public void setRoot_frame_name(String root_frame_name) {
		this.root_frame_name = root_frame_name;
	}

	/**
	 * @return the scenario_time_epoch
	 */
	public Double getScenario_time_epoch() {
		return scenario_time_epoch;
	}

	/**
	 * @param scenario_time_epoch the scenario_time_epoch to set
	 */
	public void setScenario_time_epoch(Double scenario_time_epoch) {
		this.scenario_time_epoch = scenario_time_epoch;
	}

	/**
	 * @return the next_mode_scenario_time
	 */
	public Double getNext_mode_scenario_time() {
		return next_mode_scenario_time;
	}

	/**
	 * @param next_mode_scenario_time the next_mode_scenario_time to set
	 */
	public void setNext_mode_scenario_time(Double next_mode_scenario_time) {
		this.next_mode_scenario_time = next_mode_scenario_time;
	}

	/**
	 * @return the next_mode_cte_time
	 */
	public Double getNext_mode_cte_time() {
		return next_mode_cte_time;
	}

	/**
	 * @param next_mode_cte_time the next_mode_cte_time to set
	 */
	public void setNext_mode_cte_time(Double next_mode_cte_time) {
		this.next_mode_cte_time = next_mode_cte_time;
	}

	/**
	 * @return the current_execution_mode
	 */
	public ExecutionMode getCurrent_execution_mode() {
		return current_execution_mode;
	}

	/**
	 * @param current_execution_mode the current_execution_mode to set
	 */
	public void setCurrent_execution_mode(ExecutionMode current_execution_mode) {
		this.current_execution_mode = current_execution_mode;
	}

	/**
	 * @return the next_execution_mode
	 */
	public ExecutionMode getNext_execution_mode() {
		return next_execution_mode;
	}

	/**
	 * @param next_execution_mode the next_execution_mode to set
	 */
	public void setNext_execution_mode(ExecutionMode next_execution_mode) {
		this.next_execution_mode = next_execution_mode;
	}

	/**
	 * @return the least_common_time_step
	 */
	public Long getLeast_common_time_step() {
		return least_common_time_step;
	}

	/**
	 * @param least_common_time_step the least_common_time_step to set
	 */
	public void setLeast_common_time_step(Long least_common_time_step) {
		this.least_common_time_step = least_common_time_step;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ExecutionConfiguration [root_frame_name=" + root_frame_name
				+ ", scenario_time_epoch=" + scenario_time_epoch
				+ ", next_mode_scenario_time=" + next_mode_scenario_time
				+ ", next_mode_cte_time=" + next_mode_cte_time
				+ ", current_execution_mode=" + current_execution_mode
				+ ", next_execution_mode=" + next_execution_mode
				+ ", least_common_time_step=" + least_common_time_step + "]";
	}

	public String getName() {
		return "ExCO";
	}

}