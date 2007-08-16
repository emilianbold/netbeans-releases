package org.netbeans.modules.iep.model.impl;

import java.util.Iterator;
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
        
        if (childEl != null) {
            String localName = childEl.getLocalName();
            if (localName == null || localName.length() == 0) {
                    localName = childEl.getTagName();
            }
            if (localName.equals(COMPONENT_CHILD)) {
            		child = new SchemaAttributeImpl(getModel(), childEl);
            } else {
            	child = super.createChild(childEl);
            }
        }
        
        return child;
	}
	
	public SchemaComponent duplicateSchema(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getAttributeCount() {
		return 0;
	}

	public SchemaAttribute getSchemaAttribute(int i) {
		return null;
	}

	public List<SchemaAttribute> getSchemaAttributes() {
		return getChildren(SchemaAttribute.class);
	}

	public boolean hasSameSchemaAttribute(List<SchemaAttribute> columns)  {
		return false;
	}

	public void setSchemaAttributes(List<SchemaAttribute> columns)  {
		
	}

	public SchemaAttribute findSchemaAttribute(String attributeName) {
		SchemaAttribute attr = null;
		
		if(attributeName == null) {
			return null;
		}
		
		List<SchemaAttribute> schemaAttributes = getSchemaAttributes();
		Iterator<SchemaAttribute> it = schemaAttributes.iterator();
		
		while(it.hasNext()) {
			SchemaAttribute sa = it.next();
			
			if(attributeName.equals(sa.getName())) {
				attr = sa;
				break;
			}
		}
		
		return attr;
	}

}
