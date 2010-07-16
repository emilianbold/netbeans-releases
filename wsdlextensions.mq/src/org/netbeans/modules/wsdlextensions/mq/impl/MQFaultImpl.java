/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.mq.impl;

import org.netbeans.modules.wsdlextensions.mq.MQComponent;
import org.netbeans.modules.wsdlextensions.mq.MQFault;
import org.netbeans.modules.wsdlextensions.mq.MQQName;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 * MQ Binding Fault extensibility element.
 *
 * @author Noel.Ang@sun.com
 */
public class MQFaultImpl extends MQComponentImpl implements MQFault {
    
    /** Creates a new instance of MQFaultImpl */
    public MQFaultImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public MQFaultImpl(WSDLModel model){
        this(model, createPrefixedElement(MQQName.FAULT.getQName(), model));
    }
    
    public String getReasonTextPart() {
        return getAttribute(MQAttribute.MQ_FAULT_REASONTEXT);
    }

    public String getReasonCodePart() {
        return getAttribute(MQAttribute.MQ_FAULT_REASONCODE);
    }

    public void setReasonTextPart(String part) {
        setAttribute(MQFault.ATTR_TEXT_PART,
                MQAttribute.MQ_FAULT_REASONTEXT,
                part);
    }
    
    public void setReasonCodePart(String part) {
        setAttribute(MQFault.ATTR_CODE_PART,
                MQAttribute.MQ_FAULT_REASONCODE,
                part);
    }
    
    public void accept(MQComponent.Visitor visitor) {
        visitor.visit(this);
    }
}
