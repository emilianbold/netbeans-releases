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

	public OperatorComponent getFrom() {
		Property fromProperty = getProperty(LinkComponent.PROP_FROM);
		if(fromProperty != null) {
			return ModelHelper.findOperator(fromProperty.getValue(), getModel());
		}
		
		return null;
	}

	public OperatorComponent getTo() {
		Property fromProperty = getProperty(LinkComponent.PROP_TO);
		if(fromProperty != null) {
			return ModelHelper.findOperator(fromProperty.getValue(), getModel());
		}
		
		return null;
	}
}
