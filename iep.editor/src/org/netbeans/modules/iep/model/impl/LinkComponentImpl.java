package org.netbeans.modules.iep.model.impl;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.LinkComponent;
import org.netbeans.modules.iep.model.ModelHelper;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.w3c.dom.Element;

public class LinkComponentImpl extends ComponentImpl implements LinkComponent {

    public LinkComponentImpl(IEPModel model,  Element e) {
        super(model, e);
//        setType("/IEP/Model/Link"); //NOTI18N
    }
    
    public LinkComponentImpl(IEPModel model) {
        super(model);
        setType("/IEP/Model/Link"); //NOTI18N
    }
    
     public void accept(IEPVisitor visitor) {
        visitor.visitLinkComponent(this);
     }
     

    public OperatorComponent getFrom() {
        String from = getString(PROP_FROM);
        if(!from.trim().equals("")) {
            return ModelHelper.findOperator(from, getModel());
        }
        return null;
    }

    //TODO change it to Referenceable component
    public void setFrom(OperatorComponent from) {
        if(from != null) {
            setString(PROP_FROM, from.getString(PROP_ID));
        }
    }
    
    public OperatorComponent getTo() {
        String from = getString(PROP_TO);
        if(!from.trim().equals("")) {
            return ModelHelper.findOperator(from, getModel());
        }
        return null;
    }
    
    public void setTo(OperatorComponent to) {
        if(to != null) {
            setString(PROP_TO, to.getString(PROP_ID));
        }
    }
    
    @Override
    public String toString() {
        String from = null;
        String to = null;
        OperatorComponent fromComp = getFrom();
        OperatorComponent toComp = getTo();
        if(fromComp != null) {
            from = fromComp.getString(PROP_NAME);
        }
        
        if(toComp != null) {
            to = toComp.getString(PROP_NAME);
        }
        
        return "From: " + from + " -->To: " + to;
    }
}
