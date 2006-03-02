/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class NotificationOperationImpl extends OperationImpl implements NotificationOperation {
    
    /** Creates a new instance of NotificationOperation */
    public NotificationOperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public NotificationOperationImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.OPERATION.getQName(), model));
    }
    
    public Output getOutput() {
        return getChild(Output.class);
    }

    public void setOutput(Output output) {
        super.setChildAfter(Output.class, OUTPUT_PROPERTY, output, TypeCollection.DOCUMENTATION.types());
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

}
