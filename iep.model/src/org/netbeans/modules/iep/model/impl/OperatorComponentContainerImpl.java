package org.netbeans.modules.iep.model.impl;

import java.util.List;

import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.w3c.dom.Element;

public class OperatorComponentContainerImpl extends ComponentImpl implements OperatorComponentContainer {

	public OperatorComponentContainerImpl(IEPModel model) {
		super(model);
	}

	public OperatorComponentContainerImpl(IEPModel model, Element element) {
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
            		child = new OperatorComponentImpl(getModel(), childEl);
            } else {
            	super.createChild(childEl);
            }
        }
        
        return child;
	}
	
	public void addOperatorComponent(OperatorComponent operator) {
		addChildComponent(operator);
	}

	public List<OperatorComponent> getAllOperatorComponent() {
		return getChildren(OperatorComponent.class);
	}

	public void removeOperatorComponent(OperatorComponent operator) {
		removeChildComponent(operator);
	}

}
