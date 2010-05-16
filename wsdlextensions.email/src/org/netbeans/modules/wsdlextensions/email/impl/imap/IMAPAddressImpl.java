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

package org.netbeans.modules.wsdlextensions.email.impl.imap;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPAddress;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPComponent;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPQName;
import org.netbeans.modules.wsdlextensions.email.impl.EMAILAttribute;
import org.w3c.dom.Element;

/**
 *
 * @author Sainath Adiraju
 */

public class IMAPAddressImpl extends IMAPComponentImpl implements IMAPAddress {
    public IMAPAddressImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public IMAPAddressImpl(WSDLModel model){
        this(model, createPrefixedElement(IMAPQName.ADDRESS.getQName(), model));
    }
    
    public void accept(IMAPComponent.Visitor visitor) {
        visitor.visit(this);
    }
     
    public String getEmailServer() {
        return getAttribute(IMAPAddress.ATTR_EMAILSERVER);
    }

    public void setEmailServer(String val) {
        setAttribute(IMAPAddress.ATTR_EMAILSERVER, EMAILAttribute.EMAIL_SERVER_NAME, val);
    }

    public String getPort() {
        return getAttribute(IMAPAddress.ATTR_PORT);
    }

    public void setPort(String val) {
        setAttribute(IMAPAddress.ATTR_PORT, EMAILAttribute.EMAIL_PORT, val);
    }

    public String getUserName() {
        return getAttribute(IMAPAddress.ATTR_USERNAME);
    }

    public void setUserName(String val) {
        setAttribute(IMAPAddress.ATTR_USERNAME, EMAILAttribute.EMAIL_USERNAME, val);
    }

    public String getPassword() {
        return getAttribute(IMAPAddress.ATTR_PASSWORD);
    }

    public void setPassword(String val) {
        setAttribute(IMAPAddress.ATTR_PASSWORD, EMAILAttribute.EMAIL_PASSWORD, val);
    }

    public boolean getUseSSL() {
        String s = getAttribute(IMAPAddress.ATTR_USESSL);
        return s!=null && Boolean.parseBoolean(s);
    }

    public void setUseSSL(boolean val) {
        setAttribute(IMAPAddress.ATTR_USESSL, EMAILAttribute.EMAIL_USESSL, val);
    }

    public String getMailFolder() {
        return getAttribute(IMAPAddress.ATTR_MAIL_FOLDER);
    }

    public void setMailFolder(String val) {
        setAttribute(IMAPAddress.ATTR_MAIL_FOLDER, EMAILAttribute.EMAIL_MAIL_FOLDER, val);
    }

    public String getMaxMessageCount() {
        return getAttribute(IMAPAddress.ATTR_MAXMSG_COUNT);
    }

    public void setMaxMessageCount(String val) {
        setAttribute(IMAPAddress.ATTR_MAXMSG_COUNT, EMAILAttribute.EMAIL_MAXMSG_COUNT, val);
    }

    public String getMessageAckMode() {
        return getAttribute(IMAPAddress.ATTR_MSGACK_MODE);
    }

    public void setMessageAckMode(String val) {
        setAttribute(IMAPAddress.ATTR_MSGACK_MODE, EMAILAttribute.EMAIL_MSGACK_MODE, val);
    }

    public String getMessageAckOperation() {
        return getAttribute(IMAPAddress.ATTR_MSGACK_OPERATION);
    }

    public void setMessageAckOperation(String val) {
        setAttribute(IMAPAddress.ATTR_MSGACK_OPERATION, EMAILAttribute.EMAIL_MSGACK_OPERATION, val);
    }

    public String getPollingInterval() {
        return getAttribute(IMAPAddress.ATTR_POLLING_INTERVAL);
    }

    public void setPollingInterval(String val) {
        setAttribute(IMAPAddress.ATTR_POLLING_INTERVAL, EMAILAttribute.EMAIL_POLLING_INTERVAL, val);
    }

}
