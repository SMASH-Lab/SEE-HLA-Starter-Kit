package skf.model.object.executionConfiguration;

public enum ExecutionMode {

	EXEC_MODE_UNINITIALIZED((short)0),
	EXEC_MODE_INITIALIZING((short)1),
	EXEC_MODE_RUNNING((short)2),
	EXEC_MODE_FREEZE((short)3),
	EXEC_MODE_SHUTDOWN((short)4);

	private short value;

	private ExecutionMode(short value){
		this.value = value;
	}

	public static ExecutionMode lookup(short value) {
		for(ExecutionMode rm : ExecutionMode.values())
			if(rm.value == value)
				return rm;
		return null;
	}

	public short getValue() {
		return this.value;
	}
}
