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

import java.util.Collection;
import org.netbeans.modules.xml.xam.dom.Attribute;

import org.netbeans.modules.wsdlextensions.jms.JMSAddress;
import org.netbeans.modules.wsdlextensions.jms.JMSMessage;
import org.netbeans.modules.wsdlextensions.jms.JMSOperation;
import org.netbeans.modules.wsdlextensions.jms.JMSOption;
import org.netbeans.modules.wsdlextensions.jms.JMSMapMessagePart;
import org.netbeans.modules.wsdlextensions.jms.JMSProperty;
import org.netbeans.modules.wsdlextensions.jms.JMSJNDIEnvEntry;

/**
 * 
 * JMSAttribute
 */
public enum JMSAttribute implements Attribute {
    JMS_ADDRESS_CONNECTION_URL(JMSAddress.ATTR_CONNECTION_URL),
    JMS_ADDRESS_USERNAME(JMSAddress.ATTR_USERNAME),
    JMS_ADDRESS_PASSWORD(JMSAddress.ATTR_PASSWORD),
    JMS_ADDRESS_JNDI_CONNECTIONFACTORY_NAME(JMSAddress.ATTR_JNDI_CONNECTION_FACTORY_NAME),
    JMS_ADDRESS_JNDI_INITIAL_CONTEXT_FACTORY(JMSAddress.ATTR_JNDI_INITIAL_CONTEXT_FACTORY),
    JMS_ADDRESS_JNDI_PROVIDER_URL(JMSAddress.ATTR_JNDI_PROVIDER_URL),
    JMS_ADDRESS_JNDI_SECURITY_PRINCIPAL(JMSAddress.ATTR_JNDI_SECURITY_PRINCIPAL),
    JMS_ADDRESS_JNDI_SECURITY_CREDENTIALS(JMSAddress.ATTR_JNDI_SECURITY_CRDENTIALS),
    
    JMS_OPERATION_DESTINATION(JMSOperation.ATTR_DESTINATION),
    JMS_OPERATION_DESTINATION_TYPE(JMSOperation.ATTR_DESTINATION_TYPE),
    JMS_OPERATION_TRANSACTION(JMSOperation.ATTR_TRANSACTION),
    JMS_OPERATION_TIME_TO_LIVE(JMSOperation.ATTR_TIME_TO_LIVE),
    JMS_OPERATION_DELIVERY_MODE(JMSOperation.ATTR_DELIVERY_MODE),
    JMS_OPERATION_PRIORITY(JMSOperation.ATTR_PRIORITY),
    JMS_OPERATION_DISABLE_MESSAGE_ID(JMSOperation.ATTR_DISABLE_MESSAGE_ID),
    JMS_OPERATION_DISABLE_MESSAGE_TIMESTAMP(JMSOperation.ATTR_DISABLE_MESSAGE_TIMESTAMP),
    JMS_OPERATION_TIMEOUT(JMSOperation.ATTR_TIMEOUT),
    JMS_OPERATION_CLIENT_ID(JMSOperation.ATTR_CLIENT_ID),
    JMS_OPERATION_MESSAGE_SELECTOR(JMSOperation.ATTR_MESSAGE_SELECTOR),
    JMS_OPERATION_SUBSCRIPTION_DURABILITY(JMSOperation.ATTR_SUBSCRIPTION_DURABILITY),
    JMS_OPERATION_SUBSCRIPTION_NAME(JMSOperation.ATTR_SUBSCRIPTION_NAME),
    JMS_OPERATION_MAX_CONCURRENT_CONSUMERS(JMSOperation.ATTR_MAX_CONCURRENT_CONSUMERS),
    JMS_OPERATION_CONCURRENCY_MODE(JMSOperation.ATTR_CONCURRENCY_MODE),
    JMS_OPERATION_BATCH_SIZE(JMSOperation.ATTR_BATCH_SZIE),
    JMS_OPERATION_REDELIVERY_HANDLING(JMSOperation.ATTR_REDELIVERY_HANDLING),
    JMS_OPERATION_OPTIONS(JMSOperation.ELEMENT_OPTIONS),
    JMS_OPERATION_VERB(JMSOperation.ATTR_VERB),
    
    JMS_MESSAGE_MESSAGE_TYPE(JMSMessage.ATTR_MESSAGE_TYPE),
    JMS_MESSAGE_TEXTPART(JMSMessage.ATTR_TEXTPART),
    JMS_MESSAGE_CORRELATION_ID_PART(JMSMessage.ATTR_CORRELATION_ID_PART),
    JMS_MESSAGE_DELIVERY_MODE_PART(JMSMessage.ATTR_DELIVERY_MODE_PART),
    JMS_MESSAGE_PRIORITY_PART(JMSMessage.ATTR_PRIORITY_PART),
    JMS_MESSAGE_TYPE_PART(JMSMessage.ATTR_TYPE_PART),
    JMS_MESSAGE_MESSAGE_ID_PART(JMSMessage.ATTR_MESSAGE_ID_PART),
    JMS_MESSAGE_REDELIVERED_PART(JMSMessage.ATTR_REDELIVERED_PART),
    JMS_MESSAGE_TIMESTAMP_PART(JMSMessage.ATTR_TIMESTAMP_PART),
    JMS_MESSAGE_USE(JMSMessage.ATTR_USE),
    JMS_MESSAGE_ENCODING_STYLE(JMSMessage.ATTR_ENCODING_STYLE),
    JMS_MESSAGE_FORWARD_AS_ATTACHMENT(JMSMessage.ATTR_FORWARD_AS_ATTACHMENT),

    JMS_OPTION_NAME(JMSOption.ATTR_NAME),
    JMS_OPTION_VALUE(JMSOption.ATTR_VALUE),

    JMS_JNDIENVENTRY_NAME(JMSJNDIEnvEntry.ATTR_NAME),
    JMS_JNDIENVENTRY_VALUE(JMSJNDIEnvEntry.ATTR_VALUE),
    
    JMS_MAPPART_NAME(JMSMapMessagePart.ATTR_NAME),
    JMS_MAPPART_TYPE(JMSMapMessagePart.ATTR_TYPE),
    JMS_MAPPART_PART(JMSMapMessagePart.ATTR_PART),
    
    JMS_PROPERTY_NAME(JMSProperty.ATTR_NAME),
    JMS_PROPERTY_TYPE(JMSProperty.ATTR_TYPE),
    JMS_PROPERTY_PART(JMSProperty.ATTR_PART),
    JMS_MESSAGE_BYTESPART(JMSMessage.ATTR_BYTES_PART);
    
    private String name;

    public JMSAttribute getJMS_ADDRESS_CONNECTION_URL() {
        return JMS_ADDRESS_CONNECTION_URL;
    }
    private Class type;
    private Class subtype;
    
    JMSAttribute(String name) {
        this(name, String.class);
    }
    
    JMSAttribute(String name, Class type) {
        this(name, type, null);
    }
    
    JMSAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    public String toString() { return name; }
    
    public Class getType() {
        return type;
    }
    
    public String getName() { return name; }
    
    public Class getMemberType() { return subtype; }
}
