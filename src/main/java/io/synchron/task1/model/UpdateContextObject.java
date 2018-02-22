package io.synchron.task1.model;

import java.util.ArrayList;
import java.util.List;

public class UpdateContextObject {

	private String updateAction = "APPEND";
	private List<ContextElement> contextElements;
	
	public UpdateContextObject() {
		super();
		this.contextElements = new ArrayList<>();
	}
	
	public UpdateContextObject(String updateAction, List<ContextElement> contextElements) {
		super();
		this.updateAction = updateAction;
		this.contextElements = contextElements;
	}
	
	public String getUpdateAction() {
		return updateAction;
	}
	public void setUpdateAction(String updateAction) {
		this.updateAction = updateAction;
	}
	public List<ContextElement> getContextElements() {
		return contextElements;
	}
	public void setContextElements(List<ContextElement> contextElements) {
		this.contextElements = contextElements;
	}
	
	
}
