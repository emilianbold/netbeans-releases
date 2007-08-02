package org.netbeans.modules.iep.model.impl;

import java.util.List;

import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.w3c.dom.Element;

public class SchemaComponentImpl extends ComponentImpl implements SchemaComponent {

	public SchemaComponentImpl(IEPModel model) {
		super(model);
	}

	public SchemaComponentImpl(IEPModel model, Element element) {
		super(model, element);
	}
	
	public IEPComponent createChild (Element childEl) {
		IEPComponent child = null;
		
		return child;
	}
	
	public SchemaComponent duplicateSchema(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getAttributeCount() throws Exception {
		return 0;
	}

	public SchemaAttribute getSchemaAttribute(int i) throws Exception {
		return null;
	}

	public List<SchemaAttribute> getSchemaAttributes() throws Exception {
		return null;
	}

	public boolean hasSameSchemaAttribute(List<SchemaAttribute> columns) throws Exception {
		return false;
	}

	public void setSchemaAttributes(List<SchemaAttribute> columns) throws Exception {
		
	}

	

}
