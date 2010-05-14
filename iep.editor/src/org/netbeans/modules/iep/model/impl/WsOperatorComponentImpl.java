package org.netbeans.modules.iep.model.impl;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.WsOperatorComponent;
import org.w3c.dom.Element;

public class WsOperatorComponentImpl extends MultiWSDLComponentReferenceOperatorComponentImpl implements WsOperatorComponent {
    
    public WsOperatorComponentImpl(IEPModel model,  Element e) {
            super(model, e);
        }
        
    public WsOperatorComponentImpl(IEPModel model) {
        super(model);
    }
}
