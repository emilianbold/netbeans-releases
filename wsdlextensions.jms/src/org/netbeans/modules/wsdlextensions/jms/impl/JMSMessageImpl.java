/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.wsdlextensions.jms.impl;

import java.util.List;

import org.netbeans.modules.wsdlextensions.jms.JMSComponent;
import org.netbeans.modules.wsdlextensions.jms.JMSQName;
import org.netbeans.modules.wsdlextensions.jms.JMSMessage;
import org.netbeans.modules.wsdlextensions.jms.JMSProperties;
import org.netbeans.modules.wsdlextensions.jms.JMSMapMessage;

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
    
}
