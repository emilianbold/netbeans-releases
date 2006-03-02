/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class BindingOutputImpl extends NamedImpl implements BindingOutput {
    
    /** Creates a new instance of BindingOutputImpl */
    public BindingOutputImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public BindingOutputImpl(WSDLModel model){
        this(model, createNewElement(WSDLQNames.OUTPUT.getQName(), model));
    }

    public void accept(org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor visitor) {
        visitor.visit(this);
    }
}
