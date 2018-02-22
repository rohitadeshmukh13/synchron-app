package io.synchron.task1.model;

import java.util.ArrayList;
import java.util.List;

public class ContextElement {

	private Entity entityId;
	private List<Attribute> attributes;
	
	public ContextElement() {
		super();
		this.attributes = new ArrayList<>();
	}
	
	public ContextElement(Entity entityId, List<Attribute> attributes) {
		super();
		this.entityId = entityId;
		this.attributes = attributes;
	}
	
	public Entity getEntityId() {
		return entityId;
	}
	public void setEntityId(Entity entityId) {
		this.entityId = entityId;
	}
	public List<Attribute> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}
}
