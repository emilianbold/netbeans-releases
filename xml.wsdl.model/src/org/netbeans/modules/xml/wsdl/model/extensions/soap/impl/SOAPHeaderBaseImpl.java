/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderBase;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLAttribute;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
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

    public void setPartRef(Reference<Part> partRef) {
        String v = partRef == null ? null : partRef.getRefString();
        setAttribute(PART_PROPERTY, SOAPAttribute.PART, v);
    }

    public Reference<Part> getPartRef() {
        String v = getPart();
        return v == null ? null : new PartReference(this, v);
    }
    
    public void setMessage(NamedComponentReference<Message> message) {
        setAttribute(MESSAGE_PROPERTY, WSDLAttribute.MESSAGE, message);
    }

    public NamedComponentReference<Message> getMessage() {
        String s = getAttribute(WSDLAttribute.MESSAGE);
        return s == null ? null : resolveGlobalReference(Message.class, WSDLAttribute.MESSAGE);
    }

}
