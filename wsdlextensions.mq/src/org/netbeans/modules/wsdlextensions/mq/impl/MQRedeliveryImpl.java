/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.mq.impl;

import org.netbeans.modules.wsdlextensions.mq.MQComponent;
import org.netbeans.modules.wsdlextensions.mq.MQRedelivery;
import org.netbeans.modules.wsdlextensions.mq.MQQName;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 * MQ Binding Redelivery extensibility element.
 *
 * @author Noel.Ang@sun.com
 */
public class MQRedeliveryImpl extends MQComponentImpl implements MQRedelivery {
    
    /** Creates a new instance of MQRedeliveryImpl */
    public MQRedeliveryImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public MQRedeliveryImpl(WSDLModel model){
        this(model, createPrefixedElement(MQQName.REDELIVERY.getQName(), model));
    }
    
    public String getCount() {
        return getAttribute(MQAttribute.MQ_REDELIVERY_COUNT);
    }

    public void setCount(String count) {
        setAttribute(MQRedelivery.ATTR_COUNT,
                MQAttribute.MQ_REDELIVERY_COUNT,
                count);
    }

    public String getDelay() {
        return getAttribute(MQAttribute.MQ_REDELIVERY_DELAY);
    }

    public void setDelay(String delay) {
        setAttribute(MQRedelivery.ATTR_DELAY,
                MQAttribute.MQ_REDELIVERY_DELAY,
                delay);
    }

    public String getTarget() {
        return getAttribute(MQAttribute.MQ_REDELIVERY_TARGET);
    }

    public void setTarget(String target) {
        setAttribute(MQRedelivery.ATTR_TARGET,
                MQAttribute.MQ_REDELIVERY_TARGET,
                target);
    }

    public void accept(MQComponent.Visitor visitor) {
        visitor.visit(this);
    }
}
