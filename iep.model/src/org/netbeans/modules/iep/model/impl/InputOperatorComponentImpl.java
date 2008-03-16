package org.netbeans.modules.iep.model.impl;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.InputOperatorComponent;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;
import org.w3c.dom.Element;

public class InputOperatorComponentImpl extends OperatorComponentImpl implements InputOperatorComponent {
	
	public InputOperatorComponentImpl(IEPModel model,  Element e) {
            super(model, e);
        }
        
    public InputOperatorComponentImpl(IEPModel model) {
        super(model);
    }
        
 public boolean isWebServiceInput() {
	 boolean result = false;
	 	TcgPropertyType p = getComponentType().getPropertyType(PROP_WS_INPUT_KEY);
	 	if(p != null) {
	 		result = (Boolean) p.getDefaultValue();
	 	}
	 	return result;
 	}
}
