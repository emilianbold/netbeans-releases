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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl;

import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author Nam Nguyen
 * changed by 
 * @author ads
 */
public enum BPELAttribute implements Attribute {
    TYPE("type"),
    PORT_TYPE("portType"),
    PROPERTY_NAME("propertyName"),
    MESSAGE_TYPE("messageType"),
    PART("part"),
    QUERY("query"),
    QUERY_LANGUAGE( Query.QUERY_LANGUAGE ),
    ELEMENT( CorrelationProperty.ELEMENT_PROPERTY ),
    SOURCE(Documentation.SOURCE),
    LANG( Documentation.LANG ),
    ;
    
    
    private String name;
    private Class type;
    private Class subtype;
    
    /** Creates a new instance of SOAPAttribute */
    BPELAttribute(String name) {
        this(name, String.class);
    }
    BPELAttribute(String name, Class type) {
        this(name, type, null);
    }
    BPELAttribute(String name, Class type, Class subtype) {
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
