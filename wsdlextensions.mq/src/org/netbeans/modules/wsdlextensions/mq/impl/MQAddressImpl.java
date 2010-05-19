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

/*
 * MQAddressImpl.java
 *
 * Created on December 14, 2006, 12:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.mq.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.mq.MQAddress;
import org.netbeans.modules.wsdlextensions.mq.MQComponent;
import org.netbeans.modules.wsdlextensions.mq.MQQName;
import org.w3c.dom.Element;


/**
 *
 * @author rchen
 */
public class MQAddressImpl extends MQComponentImpl implements MQAddress {
    
    /** Creates a new instance of MQAddressImpl */
    public MQAddressImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public MQAddressImpl(WSDLModel model){
        this(model, createPrefixedElement(MQQName.ADDRESS.getQName(), model));
    }
    
    public void accept(MQComponent.Visitor visitor) {
        visitor.visit(this);
    }
    
    public String getQueueManagerName() {
        return getAttribute(MQAttribute.MQ_ADDRESS_QUEUEMANAGERNAME);
    }
    public void setQueueManagerName(String val) {
        setAttribute(MQAddress.ATTR_QUEUEMANAGERNAME,
                MQAttribute.MQ_ADDRESS_QUEUEMANAGERNAME,
                val);
    }
    
    public String getHostName() {
        return getAttribute(MQAttribute.MQ_ADDRESS_HOSTNAME);
    }
    public void setHostName(String val) {
        setAttribute(MQAddress.ATTR_HOSTNAME,
                MQAttribute.MQ_ADDRESS_HOSTNAME,
                val);
    }

    public String getPortNumber() {
        return getAttribute(MQAttribute.MQ_ADDRESS_PORT);
    }

    public void setPortNumber(String val) {
        setAttribute(MQAddress.ATTR_PORT,
                MQAttribute.MQ_ADDRESS_PORT,
                val);
    }

    public String getChannelName() {
        return getAttribute(MQAttribute.MQ_ADDRESS_CHANNEL);
    }

    public void setChannelName(String val) {
        setAttribute(MQAddress.ATTR_CHANNEL,
                MQAttribute.MQ_ADDRESS_CHANNEL,
                val);
    }
    
    public String getCipherSuite() {
        return getAttribute(MQAttribute.MQ_ADDRESS_CIPHERSUITE);
    }
    
    public void setCipherSuite(String name) {
        setAttribute(MQAddress.ATTR_CIPHERSUITE,
                MQAttribute.MQ_ADDRESS_CIPHERSUITE,
                name);
    }
    
    public String getSslPeerName() {
        return getAttribute(MQAttribute.MQ_ADDRESS_SSLPEERNAME);
    }
    
    public void setSslPeerName(String name) {
        setAttribute(MQAddress.ATTR_SSLPEERNAME,
                MQAttribute.MQ_ADDRESS_SSLPEERNAME,
                name);
    }
}
