package au.blindmot.make;

public class AttributePair {
	
	public AttributePair(String instance, String instanceValue) {
		super();
		this.instance = instance;
		this.instanceValue = instanceValue;
	}
	public AttributePair() {
		// TODO Auto-generated constructor stub
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
	public boolean equals(AttributePair comparingTo) {
		if(this.getInstance().equalsIgnoreCase(comparingTo.getInstance()) 
				&& this.getInstanceValue().equalsIgnoreCase(comparingTo.getInstanceValue()))
		{
			return true;
		}
		else return false;
	}

}
