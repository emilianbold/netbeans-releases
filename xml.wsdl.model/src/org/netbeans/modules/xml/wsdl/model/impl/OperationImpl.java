/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * @author nn136682
 */
public abstract class OperationImpl extends NamedImpl implements Operation {
    
    /** Creates a new instance of OperationImpl */
    public OperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public void setInput(Input input) {
    }
  
    public Input getInput() {
        return null;
    }
    
    public void setOutput(Output output) {
    }

    public Output getOutput() {
        return null;
    }
    
    public Collection<Fault> getFaults() {
        return getChildren(Fault.class);
    }

    public void addFault(Fault fault) {
        appendChild(Operation.FAULT_PROPERTY, fault);
    }

    public void removeFault(Fault fault) {
        removeChild(Operation.FAULT_PROPERTY, fault);
    }

    public List<String> getParameterOrder() {
        String s = getAttribute(WSDLAttribute.PARAMETER_ORDER);
        return Util.parse(s);
    }

    public void setParameterOrder(List<String> parameterOrder) {
        setAttribute(PARAMETER_ORDER_PROPERTY, WSDLAttribute.PARAMETER_ORDER, 
                Util.toString(parameterOrder));
    }
    
    protected Object getAttributeValueOf(WSDLAttribute attr, String s) {
        if (attr == WSDLAttribute.PARAMETER_ORDER) {
            return Util.parse(s);
        } else {
            return super.getAttributeValueOf(attr, s);
        }
    }

}
