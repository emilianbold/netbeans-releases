/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class BindingFaultImpl extends NamedImpl implements BindingFault {
    
    /** Creates a new instance of BindingFaultImpl */
    public BindingFaultImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public BindingFaultImpl(WSDLModel model){
        this(model, createNewElement(WSDLQNames.FAULT.getQName(), model));
    }

    public void accept(org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor visitor) {
        visitor.visit(this);
    }
}
