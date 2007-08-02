package org.netbeans.modules.iep.model.impl;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.LinkComponent;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.w3c.dom.Element;

public class LinkComponentImpl extends ComponentImpl implements LinkComponent {

	public LinkComponentImpl(IEPModel model,  Element e) {
    	super(model, e);
    }

	public OperatorComponent getFrom() {
		return null;
	}

	public OperatorComponent getTo() {
		return null;
	}
}
