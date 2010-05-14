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

package org.netbeans.modules.wsdlextensions.email.impl.smtp;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPAddress;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPComponent;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPQName;
import org.netbeans.modules.wsdlextensions.email.impl.EMAILAttribute;

import org.w3c.dom.Element;

/**
 *
 * @author Sainath Adiraju
 */

public class SMTPAddressImpl extends SMTPComponentImpl implements SMTPAddress {

    public SMTPAddressImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SMTPAddressImpl(WSDLModel model){
        this(model, createPrefixedElement(SMTPQName.ADDRESS.getQName(), model));
    }
    
    public void accept(SMTPComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public void setLocation(String val) {
        setAttribute(SMTPAddress.ATTR_LOCATION, EMAILAttribute.EMAIL_RECEIVER_LOCATIONURL, val);
    }

    public String getLocation(){
        return getAttribute(SMTPAddress.ATTR_LOCATION);
    }

    public String getEmailServer() {
        return getAttribute(SMTPAddress.ATTR_SMTPSERVER);
    }

    public void setEmailServer(String val) {
        setAttribute(SMTPAddress.ATTR_SMTPSERVER, EMAILAttribute.EMAIL_SERVER_NAME, val);
    }
    
    public String getPort() {
        return getAttribute(SMTPAddress.ATTR_PORT);
    }

    public void setPort(String val) {
        setAttribute(SMTPAddress.ATTR_PORT, EMAILAttribute.EMAIL_PORT, val);
    }

    public String getUserName() {
        return getAttribute(SMTPAddress.ATTR_USERNAME);
    }

    public void setUserName(String val) {
        setAttribute(SMTPAddress.ATTR_USERNAME, EMAILAttribute.EMAIL_USERNAME, val);
    }

    public String getPassword() {
        return getAttribute(SMTPAddress.ATTR_PASSWORD);
    }

    public void setPassword(String val) {
        setAttribute(SMTPAddress.ATTR_PASSWORD, EMAILAttribute.EMAIL_PASSWORD, val);
    }

    public boolean getUseSSL() {
        String s = getAttribute(SMTPAddress.ATTR_USESSL);
        return s!=null && Boolean.parseBoolean(s);
    }

    public void setUseSSL(boolean val) {
        setAttribute(SMTPAddress.ATTR_USESSL, EMAILAttribute.EMAIL_USESSL, val);
    }
}
