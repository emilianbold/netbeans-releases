package org.netbeans.modules.iep.model.impl;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OutputOperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.lib.TcgProperty;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;
import org.w3c.dom.Element;

public class OutputOperatorComponentImpl extends OperatorComponentImpl implements OutputOperatorComponent {
	
	public OutputOperatorComponentImpl(IEPModel model,  Element e) {
            super(model, e);
        }
        
    public OutputOperatorComponentImpl(IEPModel model) {
        super(model);
    }
        
    public boolean isWebServiceOutput() {
    	boolean result = false;
    	TcgPropertyType p = getComponentType().getPropertyType(PROP_WS_OUTPUT_KEY);
    	if(p != null) {
    		result = (Boolean) p.getDefaultValue();
    	}
    	return result;
    }
    
}
