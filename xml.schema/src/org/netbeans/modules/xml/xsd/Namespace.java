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
package org.netbeans.modules.xml.xsd;

/**
 * Represents a Namespace
 * @author  anovak
 */
class Namespace {

    public static final String XMLNS_ATTR = "xmlns"; //NOI18N
    public static final String XSI_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance"; //NOI18N
    public static final String XSI_LOCATION = "schemaLocation"; //NOI18N
    public static final String XSI_NO_NAMESPACE_LOCATION = "noNamespaceSchemaLocation"; //NOI18N
    public static final String XSD_SCHEMA_URI =  "http://www.w3.org/2001/XMLSchema"; // NOI18N

    /** Not real URI, anything will work */
    private final String uri;
    /** Prefix */
    private final String prefix;
    /** Schema Location */
    private String schemaLocation;
    /** Grammar for this Namespace */
    private XSDGrammar grammar;
    
    /** Creates a new instance of Type */
    public Namespace(String uri, String prefix) {
        this.uri = uri;
        this.prefix = prefix;
        this.schemaLocation = null;
        this.grammar = null;
    }  
    
    /**
     * Getter for property uri.
     * @return Value of property uri.
     */
    public java.lang.String getURI() {
        return uri;
    }
    
    /**
     * Getter for property prefix.
     * @return Value of property prefix.
     */
    public java.lang.String getPrefix() {
        return prefix;
    }

    public void setSchemaLocation(String location) {
        this.schemaLocation = location;
    }
    
    public String getSchemaLocation() {
        return schemaLocation;
    }
    
    /**
     * Getter for property grammar.
     * @return Value of property grammar.
     */
    public org.netbeans.modules.xml.xsd.XSDGrammar getGrammar() {
        return grammar;
    }
    
    /**
     * Setter for property grammar.
     * @param grammar New value of property grammar.
     */
    public void setGrammar(org.netbeans.modules.xml.xsd.XSDGrammar grammar) {
        this.grammar = grammar;
    }
    
    /** @ret xsdf for String of form xsdf:some_name */
    public static String getPrefix(String name) {
        int i = name.indexOf(':');
        if (i >= 0) {
            return name.substring(0, i);
        }
        
        return null;
    }
    
    /** @ret some_name for String of form xsdf:some_name */
    public static String getSufix(String name) {
        int i = name.indexOf(':');
        if (i >= 0) {
            return name.substring(i + 1);
        }
        
        return null;
    }

}
