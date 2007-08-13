package org.netbeans.modules.iep.model.impl;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.LinkComponent;
import org.netbeans.modules.iep.model.ModelHelper;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.w3c.dom.Element;

public class LinkComponentImpl extends ComponentImpl implements LinkComponent {

	public LinkComponentImpl(IEPModel model,  Element e) {
    	super(model, e);
    }
	
	public LinkComponentImpl(IEPModel model) {
    	super(model);
    }
	
	

	public OperatorComponent getFrom() {
		Property fromProperty = getProperty(LinkComponent.PROP_FROM);
		if(fromProperty != null) {
			return ModelHelper.findOperator(fromProperty.getValue(), getModel());
		}
		
		return null;
	}

	//TODO change it to Referenceable component
	public void setFrom(OperatorComponent from) {
		if(from != null) {
			Property p = getProperty(LinkComponent.PROP_FROM);
			if(p == null) {
				p = getModel().getFactory().createProperty(getModel());
				p.setName(LinkComponent.PROP_FROM);
				addProperty(p);
			}
			
			p.setValue(from.getId());
		}
		
	}
	
	public OperatorComponent getTo() {
		Property fromProperty = getProperty(LinkComponent.PROP_TO);
		if(fromProperty != null) {
			return ModelHelper.findOperator(fromProperty.getValue(), getModel());
		}
		
		return null;
	}
	
	public void setTo(OperatorComponent to) {
		if(to != null) {
			Property p = getProperty(LinkComponent.PROP_TO);
			if(p == null) {
				p = getModel().getFactory().createProperty(getModel());
				p.setName(LinkComponent.PROP_TO);
				addProperty(p);
			}
			
			p.setValue(to.getId());
		}
		
		
	}
	
	public String toString() {
		String from = null;
		String to = null;
		OperatorComponent fromComp = getFrom();
		OperatorComponent toComp = getTo();
		if(fromComp != null) {
			from = fromComp.getDisplayName();
		}
		
		if(toComp != null) {
			to = toComp.getDisplayName();
		}
		
		return "From: " + from + " -->To: " + to;
	}
}
