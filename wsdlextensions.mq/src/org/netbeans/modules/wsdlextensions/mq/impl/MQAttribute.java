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
 * MQAttribute.java
 *
 * Created on December 14, 2006, 3:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.mq.impl;

import org.netbeans.modules.wsdlextensions.mq.MQAddress;
import org.netbeans.modules.wsdlextensions.mq.MQBody;
import org.netbeans.modules.wsdlextensions.mq.MQFault;
import org.netbeans.modules.wsdlextensions.mq.MQHeader;
import org.netbeans.modules.wsdlextensions.mq.MQOperation;
import org.netbeans.modules.wsdlextensions.mq.MQRedelivery;
import org.netbeans.modules.xml.xam.dom.Attribute;


/**
 *
 * @author rchen
 */
public enum MQAttribute implements Attribute {
    
    MQ_OPERATION_QUEUENAME(MQOperation.ATTR_QUEUENAME),
    MQ_OPERATION_TRANSACTION(MQOperation.ATTR_TRANSACTION),
    MQ_OPERATION_POLLING(MQOperation.ATTR_POLLING),
    MQ_OPERATION_OPENOPTIONS(MQOperation.ATTR_OPENOPTIONS),
    MQ_FAULT_REASONCODE(MQFault.ATTR_CODE_PART),
    MQ_FAULT_REASONTEXT(MQFault.ATTR_TEXT_PART),
    MQ_MESSAGE_MESSAGE_TYPE(MQBody.ATTR_MESSAGE_TYPE),
    MQ_MESSAGE_USE(MQBody.ATTR_USE),
    MQ_MESSAGEBODY_PART(MQBody.ATTR_MESSAGEBODY),
    MQ_MESSAGEBODY_SYNCPOINT(MQBody.ATTR_SYNCPOINT),
    MQ_ADDRESS_QUEUEMANAGERNAME(MQAddress.ATTR_QUEUEMANAGERNAME),
    MQ_ADDRESS_HOSTNAME(MQAddress.ATTR_HOSTNAME),
    MQ_ADDRESS_PORT(MQAddress.ATTR_PORT),
    MQ_ADDRESS_CHANNEL(MQAddress.ATTR_CHANNEL),
    MQ_ADDRESS_CIPHERSUITE(MQAddress.ATTR_CIPHERSUITE),
    MQ_ADDRESS_SSLPEERNAME(MQAddress.ATTR_SSLPEERNAME),
    MQ_HEADER_DESCRIPTOR(MQHeader.ATTR_DESCRIPTOR),
    MQ_HEADER_PART(MQHeader.ATTR_PART),
    MQ_REDELIVERY_COUNT(MQRedelivery.ATTR_COUNT),
    MQ_REDELIVERY_DELAY(MQRedelivery.ATTR_DELAY),
    MQ_REDELIVERY_TARGET(MQRedelivery.ATTR_TARGET),
    ;
    
    private String name;
    
    private Class type;
    private Class subtype;


    MQAttribute(String name) {
        this(name, String.class);
    }
    
    MQAttribute(String name, Class type) {
        this(name, type, null);
    }
    
    MQAttribute(String name, Class type, Class subtype) {
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
