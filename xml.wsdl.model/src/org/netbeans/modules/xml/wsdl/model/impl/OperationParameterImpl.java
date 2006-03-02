/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public abstract class OperationParameterImpl extends NamedImpl implements OperationParameter {
    
    /** Creates a new instance of OperationParameterImpl */
    public OperationParameterImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public GlobalReference<Message> getMessage() {
        return resolveGlobalReference(Message.class, WSDLAttribute.MESSAGE);
    }
    
    public void setMessage(GlobalReference<Message> message) {
        setGlobalReference(MESSAGE_PROPERTY, WSDLAttribute.MESSAGE, message);
    }
}
