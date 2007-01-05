/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.schema.model.impl;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Element;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.Any.ProcessContents;
import org.netbeans.modules.xml.schema.model.Derivation;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * An enumeration representing the schema attribute names.
 * @author Samaresh Panda (Samaresh.Panda@Sun.Com)
 * @author Chris Webster
 */
public enum SchemaAttributes implements Attribute {
    ABSTRACT("abstract", Boolean.class),
    ATTR_FORM_DEFAULT("attributeFormDefault", Form.class),
    BASE("base", String.class),
    BLOCK("block", Set.class, Derivation.Type.class),
    BLOCK_DEFAULT("blockDefault", Set.class, Schema.Block.class),
    DEFAULT("default", String.class),
    ELEM_FORM_DEFAULT("elementFormDefault", Form.class),
    ID("id", String.class),
    ITEM_TYPE("itemType", String.class), 
    FINAL("final", Set.class, Derivation.Type.class),
    FINAL_DEFAULT("finalDefault", Set.class, Schema.Final.class),
    FIXED("fixed", Boolean.class),
    FORM("form", Form.class),
    LANGUAGE("xml:lang", String.class),
    MAX_OCCURS("maxOccurs", String.class),
    MEMBER_TYPES("memberTypes", String.class),
    MIN_OCCURS("minOccurs", Integer.class),
    MIXED("mixed", Boolean.class),
    NAME("name", String.class),
    NAMESPACE("namespace", String.class),
    NILLABLE("nillable", Boolean.class),
    PROCESS_CONTENTS("processContents", ProcessContents.class),
    PUBLIC("public", String.class),
    REF("ref", String.class),
    REFER("refer", String.class), 
    SCHEMA_LOCATION("schemaLocation", String.class),
    SOURCE("source", String.class),
    SUBSTITUTION_GROUP("substitutionGroup", String.class),
    SYSTEM("system", String.class),
    TARGET_NS("targetNamespace", String.class),
    TYPE("type", String.class),
    USE("use", LocalAttribute.Use.class),
    VALUE("value", String.class),
    VERSION("version", String.class),
    XPATH("xpath", String.class);

    SchemaAttributes(String docName, Class type, Class memberType) {
        this.docName = docName;
        this.type = type;
        this.memberType = memberType;
    }
    
    SchemaAttributes(String docName, Class type) {
        this(docName, type, null);
    }
    
    public String getName() {
        return docName;
    }
    
    public Class getType() {
        return type;
    }
    
    public Class getMemberType() {
        return memberType;
    }
    
    public static Map<QName,List<QName>> getQNameValuedAttributes() {
        return qnameValuedAttributes;
    }
    
    private QName qname() {
        return new QName(docName);
    }
    
    private final String docName;
    private final Class type;
    private final Class memberType;

    
    private static Map<QName,List<QName>> qnameValuedAttributes = new HashMap<QName,List<QName>>();
    static {
        qnameValuedAttributes.put(
                SchemaElements.UNION.getQName(), Arrays.asList(new QName[] { MEMBER_TYPES.qname()}));
        qnameValuedAttributes.put(
                SchemaElements.RESTRICTION.getQName(), Arrays.asList(new QName[] { BASE.qname()}));
        qnameValuedAttributes.put(
                SchemaElements.EXTENSION.getQName(), Arrays.asList(new QName[] { BASE.qname()}));
        qnameValuedAttributes.put(
                SchemaElements.LIST.getQName(), Arrays.asList(new QName[] { ITEM_TYPE.qname()}));
        qnameValuedAttributes.put(
                SchemaElements.ATTRIBUTE.getQName(), Arrays.asList(new QName[] { REF.qname(), TYPE.qname()}));
        qnameValuedAttributes.put(
                SchemaElements.ELEMENT.getQName(), Arrays.asList(new QName[] { REF.qname(), SUBSTITUTION_GROUP.qname(), TYPE.qname() }));
        qnameValuedAttributes.put(
                SchemaElements.GROUP.getQName(), Arrays.asList(new QName[] { REF.qname()}));
        qnameValuedAttributes.put(
                SchemaElements.ATTRIBUTE_GROUP.getQName(), Arrays.asList(new QName[] { REF.qname()}));
    }

} 
