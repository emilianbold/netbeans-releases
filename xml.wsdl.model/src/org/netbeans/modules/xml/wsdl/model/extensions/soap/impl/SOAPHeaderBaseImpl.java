/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderBase;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLAttribute;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public abstract class SOAPHeaderBaseImpl extends SOAPMessageBaseImpl implements SOAPHeaderBase {
    
    /** Creates a new instance of SOAPHeaderBaseImpl */
    public SOAPHeaderBaseImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public String getPart() {
        return getAttribute(SOAPAttribute.PART);
    }

    public void setPart(String part) {
        setAttribute(PART_PROPERTY, SOAPAttribute.PART, part);
    }

    public void setMessage(GlobalReference<Message> message) {
        setGlobalReference(MESSAGE_PROPERTY, WSDLAttribute.MESSAGE, message);
    }

    public GlobalReference<Message> getMessage() {
        String s = getAttribute(WSDLAttribute.MESSAGE);
        return s == null ? null : resolveGlobalReference(Message.class, WSDLAttribute.MESSAGE);
    }
}
