/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class InputImpl extends OperationParameterImpl implements Input {
    
    /** Creates a new instance of InputImpl */
    public InputImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public InputImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.INPUT.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
}
