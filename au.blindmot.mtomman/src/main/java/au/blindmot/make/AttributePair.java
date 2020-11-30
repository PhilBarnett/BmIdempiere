package au.blindmot.make;

public class AttributePair {
	
	public AttributePair(String instance, String instanceValue) {
		super();
		this.instance = instance;
		this.instanceValue = instanceValue;
	}
	private String instance = null;
	private String instanceValue = null;
	
	public String getInstance() {
		return instance;
	}
	public void setInstance(String instance) {
		this.instance = instance;
	}
	public String getInstanceValue() {
		return instanceValue;
	}
	public void setInstanceValue(String instanceValue) {
		this.instanceValue = instanceValue;
	}

}
