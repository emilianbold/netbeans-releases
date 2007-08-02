package org.netbeans.modules.iep.model.impl;

import java.util.List;

import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.LinkComponentContainer;
import org.netbeans.modules.iep.model.ModelConstants;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.w3c.dom.Element;

public class PlanComponentImpl extends ComponentImpl implements PlanComponent {

	public PlanComponentImpl(IEPModel model) {
		super(model);
	}

	public PlanComponentImpl(IEPModel model, Element element) {
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
            		String name = childEl.getAttribute("name");
            		if(name != null) {
            			if(name.equals(ModelConstants.COMPONENT_OPERATORS)) {
            				child = new OperatorComponentContainerImpl(getModel(), childEl);
            			} else if(name.equals(ModelConstants.COMPONENT_LINKS)) {
            				child = new LinkComponentContainerImpl(getModel(), childEl);
            			} else if(name.equals(ModelConstants.COMPONENT_SCHEMAS)) {
            				child = new SchemaComponentContainerImpl(getModel(), childEl);
            			} 
            		}
            } else {
            	super.createChild(childEl);
            }
        }
        
        return child;
	}
	
	
	public LinkComponentContainer getLinkComponentContainer() {
		List<LinkComponentContainer> children = getChildren(LinkComponentContainer.class);
		if(children.size() != 0) {
			return children.get(0);
		}
		return null;
	}

	public OperatorComponentContainer getOperatorComponentContainer() {
		List<OperatorComponentContainer> children = getChildren(OperatorComponentContainer.class);
		if(children.size() != 0) {
			return children.get(0);
		}
		return null;
		
	}

	public SchemaComponentContainer getSchemaComponentContainer() {
		List<SchemaComponentContainer> children = getChildren(SchemaComponentContainer.class);
		if(children.size() != 0) {
			return children.get(0);
		}
		return null;
	}

}
