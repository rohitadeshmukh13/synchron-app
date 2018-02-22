package io.synchron.task1.model;

public class Entity {

	private String id;
	private String type;
	boolean isPattern = false;
	
	public Entity() {
		super();
	}

	public Entity(String id, String type) {
		super();
		this.id = id;
		this.type = type;
		this.isPattern = false;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean getIsPattern() {
		return isPattern;
	}
	public void setIsPattern(boolean isPattern) {
		this.isPattern = isPattern;
	}
	
}
