/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class FaultImpl extends OperationParameterImpl implements Fault {
    
    /** Creates a new instance of OperationImpl */
    public FaultImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public FaultImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.FAULT.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
}
