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

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.hl7.HL7Address;
import org.netbeans.modules.wsdlextensions.hl7.HL7Component;
import org.netbeans.modules.wsdlextensions.hl7.HL7QName;
import org.w3c.dom.Element;

/**
 *
 * @author raghunadh.teegavarapu@sun.com
 */
public class HL7AddressImpl extends HL7ComponentImpl implements HL7Address {
    public HL7AddressImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public HL7AddressImpl(WSDLModel model){
        this(model, createPrefixedElement(HL7QName.ADDRESS.getQName(), model));
    }
    
    public void accept(HL7Component.Visitor visitor) {
        visitor.visit(this);
    }

    public void setHL7ServerLocationURL(String hl7URL) {
        setAttribute(HL7Address.HL7_SVR_LOCATIONURL, HL7Attribute.HL7_SVR_LOCATIONURL, hl7URL);
    }

    public String getHL7ServerLocationURL() {
        return getAttribute(HL7Attribute.HL7_SVR_LOCATIONURL);
    }

  
    public String getTransportProtocolName() {
        return getAttribute(HL7Attribute.HL7_TRANS_PROTOCOL_NAME);
    }

    public void setTransportProtocolName(String val) {
        setAttribute(HL7Address.HL7_TRANS_PROTOCOL_NAME, HL7Attribute.HL7_TRANS_PROTOCOL_NAME, val);
    }
}
