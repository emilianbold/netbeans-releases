/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.Collection;
import org.netbeans.modules.xml.xam.Attribute;

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
}
