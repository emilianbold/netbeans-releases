/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
