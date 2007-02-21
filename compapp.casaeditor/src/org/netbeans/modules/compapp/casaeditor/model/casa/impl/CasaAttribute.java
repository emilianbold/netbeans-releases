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

package org.netbeans.modules.compapp.casaeditor.model.casa.impl;

import java.util.Collection;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author jqian
 */
public enum CasaAttribute implements Attribute {
        NS("xmlns"), // TMP
        TYPE("type"),
        NAME("name"),
        UNIT_NAME("unit-name"),
        DESCRIPTION("description"),
        COMPONENT_NAME("component-name"),
        ARTIFACTS_ZIP("artifacts-zip"),
        WIDTH("width"),
        X("x"),
        Y("y"),
        CONSUMER("consumer"),
        PROVIDER("provider"),
        IS_CONSUME("is-consume"),
        ENDPOINT_NAME("endpoint-name"),
        SERVICE_NAME("service-name"),
        INTERFACE_NAME("interface-name"),
        ENDPOINT("endpoint"),
        STATE("state"),
        INTERNAL("internal"),
        DEFINED("defined"),
        UNKNOWN("unknown"),
        BINDINGSTATE("bindingState"),
        BINDINGTYPE("bindingType"),
        PORTTYPE("portType"),
        TARGET_NAMESPACE("targetNamespace"),;
    
    private String name;
    private Class type;
    private Class subtype;
    private String state;
    
    /**
     * Creates a new instance of CasaAttribute
     */
    CasaAttribute(String name) {
        this(name, String.class);
    }
    
    CasaAttribute(String name, Class type) {
        this(name, type, null);
    }
    
    CasaAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    public String toString() { 
        return name; 
    }

    public Class getType() {
        return type;
    }

    public String getName() { 
        return name; 
    }

    public Class getMemberType() { 
        return subtype; 
    }
    
    public QName getQName() {
        return new QName(CasaQName.CASA_NS_URI, name);
    }
}
