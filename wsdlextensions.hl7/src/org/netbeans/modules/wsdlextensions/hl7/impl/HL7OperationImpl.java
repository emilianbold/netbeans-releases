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
import org.netbeans.modules.wsdlextensions.hl7.HL7Binding;
import org.netbeans.modules.wsdlextensions.hl7.HL7Operation;
import org.netbeans.modules.wsdlextensions.hl7.HL7Component;
import org.netbeans.modules.wsdlextensions.hl7.HL7QName;
import org.w3c.dom.Element;

/**
 * @author raghunadh.teegavarapu@sun.com
 *
 */
public class HL7OperationImpl extends HL7ComponentImpl implements HL7Operation {
    
    public HL7OperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public HL7OperationImpl(WSDLModel model){
        this(model, createPrefixedElement(HL7QName.OPERATION.getQName(), model));
    }
    
    public void accept(HL7Component.Visitor visitor) {
        visitor.visit(this);
    }

    public String getMessageType() {
        return getAttribute(HL7Attribute.HL7_MESSAGETYPE_PROPERTY);
    }

    public void setMessageType(String messageType) {
        setAttribute(HL7Operation.HL7_MESSAGETYPE_PROPERTY, HL7Attribute.HL7_MESSAGETYPE_PROPERTY, messageType);
    }
	
}
