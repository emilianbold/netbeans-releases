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
 * MQBodyImpl.java
 *
 * Created on December 14, 2006, 2:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.mq.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.mq.MQBinding;
import org.netbeans.modules.wsdlextensions.mq.MQBody;
import org.netbeans.modules.wsdlextensions.mq.MQOperation;
import org.netbeans.modules.wsdlextensions.mq.MQComponent;
import org.netbeans.modules.wsdlextensions.mq.MQQName;
import org.w3c.dom.Element;


/**
 *
 * @author rchen
 */
public class MQBodyImpl extends MQComponentImpl implements MQBody {
    
    /** Creates a new instance of MQBodyImpl */
     public MQBodyImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
     public MQBodyImpl(WSDLModel model){
        this(model, createPrefixedElement(MQQName.BODY.getQName(), model));
    }
    
    public void accept(MQComponent.Visitor visitor) {
        visitor.visit(this);
    }
    
     public String getMessageType() {
        return getAttribute(MQAttribute.MQ_MESSAGE_MESSAGE_TYPE);        
    }
    
    public void setMessageType(String val) {
        setAttribute(MQBody.ATTR_MESSAGE_TYPE, 
                     MQAttribute.MQ_MESSAGE_MESSAGE_TYPE,
                     val);        
    }
    
     public String getUse() {
          return getAttribute(MQAttribute.MQ_MESSAGE_USE);      
     }
     
    public void setUse(String val) {
         setAttribute(MQBody.ATTR_USE, 
                     MQAttribute.MQ_MESSAGE_USE,
                     val);    
    }
    
    public String getMessageBodyPart()
    {
        return getAttribute(MQAttribute.MQ_MESSAGEBODY_PART);    
    }
    
    public void setMessageBodyPart(String val){
         setAttribute(MQBody.ATTR_MESSAGEBODY, 
                     MQAttribute.MQ_MESSAGEBODY_PART,
                     val);   
    }

    public Boolean getSyncpoint() {
        String value = getAttribute(MQAttribute.MQ_MESSAGEBODY_SYNCPOINT);
        return value != null && ("1".equals(value.trim()) || Boolean.valueOf(
                value
        ));
    }

    public void setSyncpoint(Boolean useSyncpoint) {
        setAttribute(MQBody.ATTR_SYNCPOINT,
                MQAttribute.MQ_MESSAGEBODY_SYNCPOINT,
                useSyncpoint.toString()
        );
    }
}
