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

package org.netbeans.modules.wsdlextensions.hl7.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.hl7.HL7Message;
import org.netbeans.modules.wsdlextensions.hl7.HL7Component;
import org.netbeans.modules.wsdlextensions.hl7.HL7QName;
import org.w3c.dom.Element;

/**
 * @author raghunadh.teegavarapu@sun.com
 */
public class HL7MessageImpl extends HL7ComponentImpl implements HL7Message {
    
    public HL7MessageImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public HL7MessageImpl(WSDLModel model){
        this(model, createPrefixedElement(HL7QName.MESSAGE.getQName(), model));
    }
    
    public void accept(HL7Component.Visitor visitor) {
        visitor.visit(this);
    }

    public String getUse() {
        return getAttribute(HL7Attribute.HL7_USE_PROPERTY);
    }

    public void setUse(String use) {
        setAttribute(HL7Message.HL7_USE_PROPERTY, HL7Attribute.HL7_USE_PROPERTY, use);
    }
  
    public String getPart() {
        return getAttribute(HL7Attribute.HL7_PART_PROPERTY);
    }

    public void setPart(String part) {
        setAttribute(HL7_PART_PROPERTY, HL7Attribute.HL7_PART_PROPERTY, part);
    }

    public String getEncodingStyle() {
        return getAttribute(HL7Attribute.HL7_ENCODINGSTYLE_PROPERTY);
    }

    public void setEncodingStyle(String encodingStyle) {
        setAttribute(HL7_ENCODINGSTYLE_PROPERTY, HL7Attribute.HL7_ENCODINGSTYLE_PROPERTY, encodingStyle);
    }
}
