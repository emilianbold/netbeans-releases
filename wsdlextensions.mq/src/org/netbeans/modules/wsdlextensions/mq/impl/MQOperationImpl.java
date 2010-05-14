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
 * MQOperationImpl.java
 *
 * Created on December 14, 2006, 11:23 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.mq.impl;

import org.netbeans.modules.wsdlextensions.mq.MQComponent;
import org.netbeans.modules.wsdlextensions.mq.MQOperation;
import org.netbeans.modules.wsdlextensions.mq.MQQName;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * @author rulong.chen@sun.com
 */
public class MQOperationImpl extends MQComponentImpl implements MQOperation {
    
    /** Creates a new instance of MQOperationImpl */
    public MQOperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    public MQOperationImpl(WSDLModel model){
        this(model, createPrefixedElement(MQQName.OPERATION.getQName(), model));
    }
    
    public void accept(MQComponent.Visitor visitor) {
        visitor.visit(this);
    }
    
    public String getQueueName() {
        return getAttribute(MQAttribute.MQ_OPERATION_QUEUENAME);
    }
    public void setQueueName(String val) {
        setAttribute(MQOperation.ATTR_QUEUENAME,
                MQAttribute.MQ_OPERATION_QUEUENAME,
                val);
    }
    
    public String getTransaction() {
        return getAttribute(MQAttribute.MQ_OPERATION_TRANSACTION);
    }
    
    public void setTransaction(String val) {
        setAttribute(MQOperation.ATTR_TRANSACTION,
                MQAttribute.MQ_OPERATION_TRANSACTION,
                val);
    }

    public String getPollingInterval() {
        return getAttribute(MQAttribute.MQ_OPERATION_POLLING);
    }

    public void setPollingInterval(String val) {
        setAttribute(MQOperation.ATTR_POLLING,
                MQAttribute.MQ_OPERATION_POLLING,
                val);
    }

    public String getQueueOpenOptions() {
        return getAttribute(MQAttribute.MQ_OPERATION_OPENOPTIONS);
    }

    public void setQueueOpenOptions(String val) {
        setAttribute(MQOperation.ATTR_OPENOPTIONS,
                MQAttribute.MQ_OPERATION_OPENOPTIONS,
                val);
    }
}
