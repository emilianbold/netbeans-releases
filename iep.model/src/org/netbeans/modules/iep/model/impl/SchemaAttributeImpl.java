package org.netbeans.modules.iep.model.impl;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.w3c.dom.Element;

public class SchemaAttributeImpl extends ComponentImpl implements SchemaAttribute {

	public SchemaAttributeImpl(IEPModel model) {
		super(model);
		setType("/IEP/Metadata/ColumnMetadata"); //NOI18N
	}
	
	public SchemaAttributeImpl(IEPModel model, Element element) {
		super(model, element);
		setType("/IEP/Metadata/ColumnMetadata"); //NOI18N
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
			this.addProperty(p);
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
			this.addProperty(p);
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
			this.addProperty(p);
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
			this.addProperty(p);
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
			this.addProperty(p);
		}
		
		p.setValue(attributeComment);
		
	}
	
	public String toString() {
		StringBuffer resultStrBuffer = new StringBuffer();
		
		resultStrBuffer.append("name: ");
		resultStrBuffer.append(getAttributeName());
		resultStrBuffer.append("type: ");
		resultStrBuffer.append(getAttributeType());
		resultStrBuffer.append("scale: ");
		resultStrBuffer.append(getAttributeScale());
		resultStrBuffer.append("size: ");
		resultStrBuffer.append(getAttributeSize());
		resultStrBuffer.append("comment: ");
		resultStrBuffer.append(getAttributeComment());
		
		return resultStrBuffer.toString();
	}
}
