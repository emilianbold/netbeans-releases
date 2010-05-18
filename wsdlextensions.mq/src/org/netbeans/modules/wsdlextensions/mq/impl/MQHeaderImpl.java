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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.wsdlextensions.mq.impl;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import javax.xml.namespace.QName;
import javax.xml.XMLConstants;

import org.netbeans.modules.wsdlextensions.mq.MQHeader;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 * @author Noel.Ang@sun.com
 */
public class MQHeaderImpl extends MQComponentImpl implements MQHeader {
    public MQHeaderImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public String getDescriptor() {
        return getAttribute(MQAttribute.MQ_HEADER_DESCRIPTOR);      
    }

    public void setDescriptor(String name) {
        setAttribute(MQHeader.ATTR_DESCRIPTOR, 
                     MQAttribute.MQ_HEADER_DESCRIPTOR,
                     name);        
    }

    public String getPart() {
        return getAttribute(MQAttribute.MQ_HEADER_PART);      
    }

    public void setPart(String name) {
        setAttribute(MQHeader.ATTR_PART, 
                     MQAttribute.MQ_HEADER_PART,
                     name);        
    }
}
