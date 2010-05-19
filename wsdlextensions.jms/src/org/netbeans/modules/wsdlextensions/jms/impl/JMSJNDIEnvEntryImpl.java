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
import org.netbeans.modules.wsdlextensions.jms.JMSJNDIEnvEntry;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * JMSJNDIEnvEntryImpl
 */
public class JMSJNDIEnvEntryImpl extends JMSComponentImpl implements JMSJNDIEnvEntry {

    public JMSJNDIEnvEntryImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public JMSJNDIEnvEntryImpl(WSDLModel model) {
        this(model, createPrefixedElement(JMSQName.JNDIENVENTRY.getQName(), model));
    }

    public String getName() {
        return getAttribute(JMSJNDIEnvEntry.ATTR_NAME);
    }

    public void setName(String val) {
        setAttribute(JMSJNDIEnvEntry.ATTR_NAME,
                JMSAttribute.JMS_JNDIENVENTRY_NAME,
                val);
    }

    public String getValue() {
        return getAttribute(JMSJNDIEnvEntry.ATTR_VALUE);
    }

    public void setValue(String val) {
        setAttribute(JMSJNDIEnvEntry.ATTR_VALUE,
                JMSAttribute.JMS_JNDIENVENTRY_VALUE,
                val);
    }

    public void accept(JMSComponent.Visitor visitor) {
        visitor.visit(this);
    }
}
