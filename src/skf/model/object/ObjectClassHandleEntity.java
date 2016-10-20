package skf.model.object;

import hla.rti1516e.ObjectClassHandle;

public class ObjectClassHandleEntity {
	
	private ObjectClassHandle objectClassHandle = null;
	
	private String instanceName = null;

	public ObjectClassHandleEntity(ObjectClassHandle objectClassHandle, String instanceName) {
		this.objectClassHandle = objectClassHandle;
		this.instanceName = instanceName;
	}

	public ObjectClassHandle getObjectClassHandle() {
		return objectClassHandle;
	}

	public void setObjectClassHandle(ObjectClassHandle objectClassHandle) {
		this.objectClassHandle = objectClassHandle;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((objectClassHandle == null) ? 0 : objectClassHandle
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectClassHandleEntity other = (ObjectClassHandleEntity) obj;
		if (objectClassHandle == null) {
			if (other.objectClassHandle != null)
				return false;
		} else if (!objectClassHandle.equals(other.objectClassHandle))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ObjectClassHandleEntity [objectClassHandle="
				+ objectClassHandle + ", instanceName=" + instanceName + "]";
	}
	
}
