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

package org.netbeans.modules.wsdlextensions.email.impl.pop3;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.email.pop3.POP3Address;
import org.netbeans.modules.wsdlextensions.email.pop3.POP3Component;
import org.netbeans.modules.wsdlextensions.email.pop3.POP3QName;
import org.netbeans.modules.wsdlextensions.email.impl.EMAILAttribute;

import org.w3c.dom.Element;

/**
 *
 * @author Sainath Adiraju
 */

public class POP3AddressImpl extends POP3ComponentImpl implements POP3Address {

    public POP3AddressImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public POP3AddressImpl(WSDLModel model){
        this(model, createPrefixedElement(POP3QName.ADDRESS.getQName(), model));
    }
    
    public void accept(POP3Component.Visitor visitor) {
        visitor.visit(this);
    }
    
    public String getEmailServer() {
        return getAttribute(POP3Address.ATTR_EMAILSERVER);
    }

    public void setEmailServer(String val) {
        setAttribute(POP3Address.ATTR_EMAILSERVER, EMAILAttribute.EMAIL_SERVER_NAME, val);
    }

    public String getPort() {
        return getAttribute(POP3Address.ATTR_PORT);
    }

    public void setPort(String val) {
        setAttribute(POP3Address.ATTR_PORT, EMAILAttribute.EMAIL_PORT, val);
    }

    public String getUserName() {
        return getAttribute(POP3Address.ATTR_USERNAME);
    }

    public void setUserName(String val) {
        setAttribute(POP3Address.ATTR_USERNAME, EMAILAttribute.EMAIL_USERNAME, val);
    }

    public String getPassword() {
        return getAttribute(POP3Address.ATTR_PASSWORD);
    }

    public void setPassword(String val) {
        setAttribute(POP3Address.ATTR_PASSWORD, EMAILAttribute.EMAIL_PASSWORD, val);
    }

    public boolean getUseSSL() {
        String s = getAttribute(POP3Address.ATTR_USESSL);
        return s!=null && Boolean.parseBoolean(s);
    }

    public void setUseSSL(boolean val) {
        setAttribute(POP3Address.ATTR_USESSL, EMAILAttribute.EMAIL_USESSL, val);
    }

   /* public String getMailFolder() {
        return getAttribute(POP3Address.ATTR_MAIL_FOLDER);
    }

    public void setMailFolder(String val) {
        setAttribute(POP3Address.ATTR_MAIL_FOLDER, EMAILAttribute.EMAIL_MAIL_FOLDER, val);
    }*/

    public String getMaxMessageCount() {
        return getAttribute(POP3Address.ATTR_MAXMSG_COUNT);
    }

    public void setMaxMessageCount(String val) {
        setAttribute(POP3Address.ATTR_MAXMSG_COUNT, EMAILAttribute.EMAIL_MAXMSG_COUNT, val);
    }

    public String getMessageAckMode() {
        return getAttribute(POP3Address.ATTR_MSGACK_MODE);
    }

    public void setMessageAckMode(String val) {
        setAttribute(POP3Address.ATTR_MSGACK_MODE, EMAILAttribute.EMAIL_MSGACK_MODE, val);
    }

    public String getMessageAckOperation() {
        return getAttribute(POP3Address.ATTR_MSGACK_OPERATION);
    }

    public void setMessageAckOperation(String val) {
        setAttribute(POP3Address.ATTR_MSGACK_OPERATION, EMAILAttribute.EMAIL_MSGACK_OPERATION, val);
    }

    public String getPollingInterval() {
        return getAttribute(POP3Address.ATTR_POLLING_INTERVAL);
    }

    public void setPollingInterval(String val) {
        setAttribute(POP3Address.ATTR_POLLING_INTERVAL, EMAILAttribute.EMAIL_POLLING_INTERVAL, val);
    }

}
