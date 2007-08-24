package org.netbeans.modules.iep.model.impl;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.LinkComponent;
import org.netbeans.modules.iep.model.LinkComponentContainer;
import org.netbeans.modules.iep.model.ModelConstants;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.w3c.dom.Element;

public class LinkComponentContainerImpl extends ComponentImpl implements LinkComponentContainer {

	public LinkComponentContainerImpl(IEPModel model) {
		super(model);
		setType("/IEP/Model/Plan|Links"); //NOI18N
		
	}
	
	public LinkComponentContainerImpl(IEPModel model, Element element) {
		super(model, element);
		setType("/IEP/Model/Plan|Links"); //NOI18N
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
	
	public LinkComponent findLink(String linkName) {
		if(linkName == null) {
			return null;
		}
		
		LinkComponent linkComponent = null;
		
		List<LinkComponent> links = getAllLinkComponents();
		Iterator<LinkComponent> it = links.iterator();
		while(it.hasNext()) {
			LinkComponent lc = it.next();
			if(linkName.equals(lc.getName())) {
				linkComponent = lc;
				break;
			}
		}
		
		return linkComponent;
	}
	
	
	public LinkComponent findLink(OperatorComponent from, OperatorComponent to) {
		if(from == null || to == null) {
			return null;
		}
		
		LinkComponent linkComponent = null;
		
		List<LinkComponent> links = getAllLinkComponents();
		Iterator<LinkComponent> it = links.iterator();
		while(it.hasNext()) {
			LinkComponent lc = it.next();
			OperatorComponent fromComp = lc.getFrom();
			OperatorComponent toComp = lc.getTo();
			
			if(from.equals(fromComp) && to.equals(toComp)) {
				linkComponent = lc;
				break;
			}
		}
		
		return linkComponent;
	}

}
