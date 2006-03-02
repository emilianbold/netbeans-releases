/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.HashSet;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * An enumeration representing the valid schema element and attribute
 * names. 
 * @author Samaresh Panda (Samaresh.Panda@Sun.Com)
 * @author Chris Webster
 */
public enum SchemaElements {
    ALL("all"),
    ANNOTATION("annotation"),
    ANY("any"),
    ANYTYPE("anyType"),
    ANY_ATTRIBUTE("anyAttribute"),
    APPINFO("appinfo"),
    ATTRIBUTE("attribute"),
    ATTRIBUTE_GROUP("attributeGroup"),
    CHOICE("choice"),
    COMPLEX_CONTENT("complexContent"),
    COMPLEX_TYPE("complexType"),
    DOCUMENTATION("documentation"),
    ELEMENT("element"),
    ENUMERATION("enumeration"),
    EXTENSION("extension"),
    FIELD("field"),
    FRACTION_DIGITS("fractionDigits"),
    GROUP("group"),
    INCLUDE("include"),
    IMPORT("import"),
    KEY("key"),
    KEYREF("keyref"),
    LENGTH("length"),
    LIST("list"),
    MAX_EXCLUSIVE("maxExclusive"),
    MAX_INCLUSIVE("maxInclusive"),
    MIN_EXCLUSIVE("minExclusive"),
    MIN_INCLUSIVE("minInclusive"),
    MAX_LENGTH("maxLength"),
    MIN_LENGTH("minLength"),
    NOTATION("notation"),
    PATTERN("pattern"),
    REDEFINE("redefine"),
    RESTRICTION("restriction"),
    SCHEMA("schema"),
    SELECTOR("selector"),
    SEQUENCE("sequence"),
    SIMPLE_CONTENT("simpleContent"),
    SIMPLE_TYPE("simpleType"),
    TOTAL_DIGITS("totalDigits"),
    UNION("union"),
    UNIQUE("unique"),
    WHITESPACE("whiteSpace");

    SchemaElements(String docName) {
        this.docName = docName;
        //this.possibleAttrs = attrs;
    }
    
    public String getName() {
        return docName;
    }
    
    public QName getQName() {
        return new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, docName);
    }
    
    public static Set<QName> allQNames() {
        if (allQNames == null) {
            allQNames = new HashSet<QName>();
            for (SchemaElements v : values()) {
                allQNames.add(v.getQName());
            }
        }
        return allQNames;
    }
    
    private final String docName;
    private static Set<QName> allQNames = null;
} 
