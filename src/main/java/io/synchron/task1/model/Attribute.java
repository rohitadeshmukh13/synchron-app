package io.synchron.task1.model;

import java.util.ArrayList;
import java.util.List;

public class Attribute {

	private String name;
	private String type;
	private String contextValue;
	private List<Metadata> metadata;
	
	public Attribute() {
		super();
		this.metadata = new ArrayList<>();
	}
	
	public Attribute(String name, String type, String contextValue) {
		super();
		this.name = name;
		this.type = type;
		this.contextValue = contextValue;
		this.metadata = new ArrayList<>();
	}
	
	public Attribute(String name, String type, String contextValue, List<Metadata> metadata) {
		super();
		this.name = name;
		this.type = type;
		this.contextValue = contextValue;
		this.metadata = metadata;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getContextValue() {
		return contextValue;
	}
	public void setContextValue(String contextValue) {
		this.contextValue = contextValue;
	}
	public List<Metadata> getMetadata() {
		return metadata;
	}
	public void setMetadata(List<Metadata> metadata) {
		this.metadata = metadata;
	}
}
