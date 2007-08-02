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

	public String getAttributeName() throws Exception {
		String propValue = null;
		Property p = super.getProperty(PROP_NAME);
		if(p != null) {
			propValue = p.getValue();
		}
		return propValue;
	}

	public String getAttributeScale() throws Exception {
		String propValue = null;
		Property p = super.getProperty(PROP_SCALE);
		if(p != null) {
			propValue = p.getValue();
		}
		return propValue;
	}

	public String getAttributeSize() throws Exception {
		String propValue = null;
		Property p = super.getProperty(PROP_SIZE);
		if(p != null) {
			propValue = p.getValue();
		}
		return propValue;
	}

	public String getAttributeType() throws Exception {
		String propValue = null;
		Property p = super.getProperty(PROP_TYPE);
		if(p != null) {
			propValue = p.getValue();
		}
		return propValue;
	}

	public String getComment() throws Exception {
		String propValue = null;
		Property p = super.getProperty(PROP_COMMENT);
		if(p != null) {
			propValue = p.getValue();
		}
		return propValue;
	}

	
}
