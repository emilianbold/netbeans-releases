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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author nn136682
 */
public enum WSDLAttribute implements Attribute {
        BINDING("binding"),
        ELEMENT("element"),
        LOCATION("location"),
        MESSAGE("message"),
        NAME("name"),
        NAMESPACE_URI("namespace"),
        TARGET_NAMESPACE("targetNamespace"),
        PARAMETER_ORDER("parameterOrder"),
        PORT_TYPE("type"),
        TYPE("type");
    
    private String name;
    private Class type;
    private Class subtype;
    
    /** Creates a new instance of WSDLAttribute */
    WSDLAttribute(String name) {
        this(name, String.class);
    }
    WSDLAttribute(String name, Class type) {
        this(name, type, null);
    }
    WSDLAttribute(String name, Class type, Class subtype) {
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

    private QName qname() {
        return new QName(name);
    }
    
    private static Map<QName,List<QName>> qnameValuedAttributes = null;
    private static void initAttributeMap() {
        qnameValuedAttributes = new HashMap<QName,List<QName>>();
        qnameValuedAttributes.put(
                WSDLQNames.BINDING.getQName(), Arrays.asList(new QName[] { PORT_TYPE.qname()}));
        qnameValuedAttributes.put(
                WSDLQNames.PART.getQName(), Arrays.asList(new QName[] { ELEMENT.qname(), TYPE.qname() }));
        qnameValuedAttributes.put(
                WSDLQNames.INPUT.getQName(), Arrays.asList(new QName[] { MESSAGE.qname() }));
        qnameValuedAttributes.put(
                WSDLQNames.OUTPUT.getQName(), Arrays.asList(new QName[] { MESSAGE.qname() }));
        qnameValuedAttributes.put(
                WSDLQNames.FAULT.getQName(), Arrays.asList(new QName[] { MESSAGE.qname() }));
        qnameValuedAttributes.put(
                WSDLQNames.PORT.getQName(), Arrays.asList(new QName[] { BINDING.qname() }));
    }
    
    static Map<QName,List<QName>> getQNameValuedAttributes() {
        if (qnameValuedAttributes == null) {
            initAttributeMap();
        }
        return Collections.unmodifiableMap(qnameValuedAttributes);
    }
}
