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

package org.netbeans.modules.wsdlextensions.jms.impl;

import org.netbeans.modules.wsdlextensions.jms.JMSAddress;
import org.netbeans.modules.wsdlextensions.jms.JMSComponent;
import org.netbeans.modules.wsdlextensions.jms.JMSQName;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * JMSAddressImpl
 */
public class JMSAddressImpl extends JMSComponentImpl implements JMSAddress {

    public JMSAddressImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public JMSAddressImpl(WSDLModel model){
        this(model, createPrefixedElement(JMSQName.ADDRESS.getQName(), model));
    }

    public void setConnectionURL(String val) {
        setAttribute(JMSAddress.ATTR_CONNECTION_URL, 
                     JMSAttribute.JMS_ADDRESS_CONNECTION_URL,
                     val);
    }
    
    public String getConnectionURL() {
        return getAttribute(JMSAttribute.JMS_ADDRESS_CONNECTION_URL);
    }
        
    public String getUsername() {
        return getAttribute(JMSAttribute.JMS_ADDRESS_USERNAME);        
    }
    
    public void setUsername(String val) {
        setAttribute(JMSAddress.ATTR_USERNAME, 
                     JMSAttribute.JMS_ADDRESS_USERNAME,
                     val);                
    }

    public String getPassword() {
        return getAttribute(JMSAttribute.JMS_ADDRESS_PASSWORD);        
    }
    
    public void setPassword(String val) {
        setAttribute(JMSAddress.ATTR_PASSWORD, 
                     JMSAttribute.JMS_ADDRESS_PASSWORD,
                     val);        
    }
    
}
