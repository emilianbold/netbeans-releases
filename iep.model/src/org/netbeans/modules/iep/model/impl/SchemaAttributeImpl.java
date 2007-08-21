package org.netbeans.modules.iep.model.impl;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.w3c.dom.Element;

public class SchemaAttributeImpl extends ComponentImpl implements SchemaAttribute {

	public SchemaAttributeImpl(IEPModel model) {
		super(model);
	}
	
	public SchemaAttributeImpl(IEPModel model, Element element) {
		super(model, element);
	}

	public String getAttributeName() {
		String propValue = null;
		Property p = super.getProperty(PROP_NAME);
		if(p != null) {
			propValue = p.getValue();
		}
		return propValue;
	}

	public void setAttributeName(String attributeName) {
		Property p = super.getProperty(PROP_NAME);
		if(p == null) {
			p = getModel().getFactory().createProperty(getModel());
			p.setName(PROP_NAME);
		}
		
		p.setValue(attributeName);
	}
	
	public String getAttributeScale() {
		String propValue = null;
		Property p = super.getProperty(PROP_SCALE);
		if(p != null) {
			propValue = p.getValue();
		}
		return propValue;
	}

	public void setAttributeScale(String attributeScale) {
		Property p = super.getProperty(PROP_SCALE);
		if(p == null) {
			p = getModel().getFactory().createProperty(getModel());
			p.setName(PROP_SCALE);
		}
		
		p.setValue(attributeScale);
		
	}
	
	public String getAttributeSize() {
		String propValue = null;
		Property p = super.getProperty(PROP_SIZE);
		if(p != null) {
			propValue = p.getValue();
		}
		return propValue;
	}
	
	public void setAttributeSize(String attributeSize) {
		Property p = super.getProperty(PROP_SIZE);
		if(p == null) {
			p = getModel().getFactory().createProperty(getModel());
			p.setName(PROP_SIZE);
		}
		
		p.setValue(attributeSize);
		
	}

	public String getAttributeType()  {
		String propValue = null;
		Property p = super.getProperty(PROP_TYPE);
		if(p != null) {
			propValue = p.getValue();
		}
		return propValue;
	}

	public void setAttributeType(String attributeType) {
		Property p = super.getProperty(PROP_TYPE);
		if(p == null) {
			p = getModel().getFactory().createProperty(getModel());
			p.setName(PROP_TYPE);
		}
		
		p.setValue(attributeType);
		
	}
	
	public String getAttributeComment() {
		String propValue = null;
		Property p = super.getProperty(PROP_COMMENT);
		if(p != null) {
			propValue = p.getValue();
		}
		return propValue;
	}

	public void setAttributeComment(String attributeComment) {
		Property p = super.getProperty(PROP_COMMENT);
		if(p == null) {
			p = getModel().getFactory().createProperty(getModel());
			p.setName(PROP_COMMENT);
		}
		
		p.setValue(attributeComment);
		
	}
	
}
