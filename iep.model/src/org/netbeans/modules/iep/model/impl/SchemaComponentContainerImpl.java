package org.netbeans.modules.iep.model.impl;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.w3c.dom.Element;

public class SchemaComponentContainerImpl extends ComponentImpl implements SchemaComponentContainer {

	public SchemaComponentContainerImpl(IEPModel model) {
		super(model);
		setType("/IEP/Model/Plan|Schemas"); //NOI18N
	}

	public SchemaComponentContainerImpl(IEPModel model, Element element) {
		super(model, element);
		setType("/IEP/Model/Plan|Schemas"); //NOI18N
	}
	
	public IEPComponent createChild (Element childEl) {
		IEPComponent child = null;
        
        if (childEl != null) {
            String localName = childEl.getLocalName();
            if (localName == null || localName.length() == 0) {
                    localName = childEl.getTagName();
            }
            if (localName.equals(COMPONENT_CHILD)) {
            		child = new SchemaComponentImpl(getModel(), childEl);
            } else {
            	child = super.createChild(childEl);
            }
        }
        
        return child;
	}
	
	public void addSchemaComponent(SchemaComponent schema) {
		addChildComponent(schema);
	}

	public List<SchemaComponent> getAllSchemaComponents() {
		return getChildren(SchemaComponent.class);
	}

	public void removeSchemaComponent(SchemaComponent schema) {
		removeChildComponent(schema);
	}

	public SchemaComponent findSchema(String name) {
		SchemaComponent schema = null;
		if(name == null) {
			return null;
		}
		
		List<SchemaComponent> schemas = getAllSchemaComponents();
		Iterator<SchemaComponent> it = schemas.iterator();
		
		while(it.hasNext()) {
			SchemaComponent sc = it.next();
			if(name.equals(sc.getName())) {
				schema = sc;
				break;
			}
		}
		
		return schema;
	}
}
