package org.netbeans.modules.iep.model.impl;

import java.util.List;

import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.LinkComponent;
import org.netbeans.modules.iep.model.LinkComponentContainer;
import org.netbeans.modules.iep.model.ModelConstants;
import org.w3c.dom.Element;

public class LinkComponentContainerImpl extends ComponentImpl implements LinkComponentContainer {

	public LinkComponentContainerImpl(IEPModel model) {
		super(model);
	}
	
	public LinkComponentContainerImpl(IEPModel model, Element element) {
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
            		child = new LinkComponentImpl(getModel(), childEl);
            } else {
            	child = super.createChild(childEl);
            }
        }
        
        return child;
	}

	public void addLinkComponent(LinkComponent link) {
		addChildComponent(link);
	}

	public List<LinkComponent> getAllLinkComponents() {
		return getChildren(LinkComponent.class);
	}

	public void removeLinkComponent(LinkComponent link) {
		removeChildComponent(link);
	}

}
