/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class OneWayOperationImpl extends OperationImpl implements OneWayOperation {
    
    /** Creates a new instance of OneWayOperation */
    public OneWayOperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public OneWayOperationImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.OPERATION.getQName(), model));
    }
    
    public Input getInput() {
        return getChild(Input.class);
    }

    public void setInput(Input input) {
        setChildAfter(Input.class, INPUT_PROPERTY, input, TypeCollection.DOCUMENTATION.types());
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

}
