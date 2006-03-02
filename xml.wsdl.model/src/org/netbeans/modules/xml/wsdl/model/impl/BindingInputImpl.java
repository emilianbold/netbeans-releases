/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class BindingInputImpl extends NamedImpl implements BindingInput {
    
    /** Creates a new instance of BindingInputImpl */
    public BindingInputImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public BindingInputImpl(WSDLModel model){
        this(model, createNewElement(WSDLQNames.INPUT.getQName(), model));
    }

    public void accept(org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor visitor) {
        visitor.visit(this);
    }
}
