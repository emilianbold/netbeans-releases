/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.xsd;

import org.xml.sax.Attributes;

/**
 * Specialized element representing xs:complexType or xs:simpleType
 * @author  anovak
 */
class Type extends SchemaElement {
    
    public static final String XS_SIMPLE_TYPE = "xs:simpleType";
    public static final String XS_COMPLEX_TYPE = "xs:complexType";
    
    /** Type representing xs:string */
    public static final Type XS_STRING = new Type();
    
    /** Creates a new instance of Type */
    private Type() {
    }
    
    protected Type(String namespaceURI, String qname, Attributes attributes) {
        super(namespaceURI, qname, attributes);
    }
    
}
