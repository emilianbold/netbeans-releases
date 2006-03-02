/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class OutputImpl extends OperationParameterImpl implements Output {
    
    /** Creates a new instance of OutputImpl */
    public OutputImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public OutputImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.OUTPUT.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
}
