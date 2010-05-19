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
package org.netbeans.modules.wsdlextensions.jms.impl;


import org.netbeans.modules.wsdlextensions.jms.JMSComponent;
import org.netbeans.modules.wsdlextensions.jms.JMSQName;
import org.netbeans.modules.wsdlextensions.jms.JMSMessage;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 * JMSMessageImpl
 */
public class JMSMessageImpl extends JMSComponentImpl implements JMSMessage {

    public JMSMessageImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public JMSMessageImpl(WSDLModel model){
        this(model, createPrefixedElement(JMSQName.MESSAGE.getQName(), model));
    }

    public void accept(JMSComponent.Visitor visitor) {
        visitor.visit(this);
    }    
    
    public String getMessageType() {
        return getAttribute(JMSAttribute.JMS_MESSAGE_MESSAGE_TYPE);        
    }
    
    public void setMessageType(String val) {
        setAttribute(JMSMessage.ATTR_MESSAGE_TYPE, 
                     JMSAttribute.JMS_MESSAGE_MESSAGE_TYPE,
                     val);        
    }

    public String getUse() {
        return getAttribute(JMSAttribute.JMS_MESSAGE_USE);        
    }
    
    public void setUse(String val) {
        setAttribute(JMSMessage.ATTR_USE, 
                     JMSAttribute.JMS_MESSAGE_USE,
                     val);        
    }
    
    public String getTextPart() {
        return getAttribute(JMSAttribute.JMS_MESSAGE_TEXTPART);        
    }
    
    public void setTextPart(String val) {
        setAttribute(JMSMessage.ATTR_TEXTPART, 
                     JMSAttribute.JMS_MESSAGE_TEXTPART,
                     val);                
    }
    
    public String getBytesPart() {
        return getAttribute(JMSAttribute.JMS_MESSAGE_BYTESPART);        
    }
    
    public void setBytesPart(String val) {
        setAttribute(JMSMessage.ATTR_BYTES_PART, 
                     JMSAttribute.JMS_MESSAGE_BYTESPART,
                     val);                
    }

    public String getCorrelationIdPart() {
        return getAttribute(JMSAttribute.JMS_MESSAGE_CORRELATION_ID_PART);        
    }
    
    public void setCorrelationIdPart(String val) {
        setAttribute(JMSMessage.ATTR_CORRELATION_ID_PART, 
                     JMSAttribute.JMS_MESSAGE_CORRELATION_ID_PART,
                     val);        
    }

    public String getDeliveryModePart() {
        return getAttribute(JMSAttribute.JMS_MESSAGE_DELIVERY_MODE_PART);                
    }
    
    public void setDeliveryModePart(String val) {
        setAttribute(JMSMessage.ATTR_DELIVERY_MODE_PART, 
                     JMSAttribute.JMS_MESSAGE_DELIVERY_MODE_PART,
                     val);                
    }

    public String getPriorityPart() {
        return getAttribute(JMSAttribute.JMS_MESSAGE_PRIORITY_PART);                        
    }
    
    public void setPriorityPart(String val) {
        setAttribute(JMSMessage.ATTR_PRIORITY_PART, 
                     JMSAttribute.JMS_MESSAGE_PRIORITY_PART,
                     val);                        
    }

    public String getTypePart() {
        return getAttribute(JMSAttribute.JMS_MESSAGE_TYPE_PART);        
    }
    
    public void setTypePart(String val) {
        setAttribute(JMSMessage.ATTR_TYPE_PART, 
                     JMSAttribute.JMS_MESSAGE_TYPE_PART,
                     val);                                
    }

    public String getMessageIDPart() {
        return getAttribute(JMSAttribute.JMS_MESSAGE_MESSAGE_ID_PART);        
    }
    
    public void setMessageIDPart(String val) {
        setAttribute(JMSMessage.ATTR_MESSAGE_ID_PART, 
                     JMSAttribute.JMS_MESSAGE_MESSAGE_ID_PART,
                     val);                                        
    }

    public String getRedeliveredPart() {
        return getAttribute(JMSAttribute.JMS_MESSAGE_REDELIVERED_PART);                
    }
    
    public void setRedeliveredPart(String val) {
        setAttribute(JMSMessage.ATTR_REDELIVERED_PART, 
                     JMSAttribute.JMS_MESSAGE_REDELIVERED_PART,
                     val);                                                
    }
    
    public String getTimestampPart() {
        return getAttribute(JMSAttribute.JMS_MESSAGE_TIMESTAMP_PART);                        
    }
    
    public void setTimestampPart(String val) {
        setAttribute(JMSMessage.ATTR_TIMESTAMP_PART, 
                     JMSAttribute.JMS_MESSAGE_TIMESTAMP_PART,
                     val);        
    }
    
    public void setJMSEncodingStyle(String val) {
        setAttribute(JMSMessage.ATTR_ENCODING_STYLE, 
                     JMSAttribute.JMS_MESSAGE_ENCODING_STYLE,
                     val);        
    }
    
    public String getJMSEncodingStyle() {
        return getAttribute(JMSAttribute.JMS_MESSAGE_ENCODING_STYLE);        
    }

    public void setForwardAsAttachment(boolean b) {
        setAttribute(JMSMessage.ATTR_FORWARD_AS_ATTACHMENT, JMSAttribute.JMS_MESSAGE_FORWARD_AS_ATTACHMENT, b? "true" : "false");
    }     
    
    public boolean getForwardAsAttachment() {
        String s = getAttribute(JMSAttribute.JMS_MESSAGE_FORWARD_AS_ATTACHMENT);
        return s != null && s.equals("true");
    }    
}
