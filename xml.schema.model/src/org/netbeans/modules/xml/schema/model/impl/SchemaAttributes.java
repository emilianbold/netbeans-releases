/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.schema.model.impl;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Any.ProcessContents;
import org.netbeans.modules.xml.schema.model.Derivation;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.xam.Attribute;

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
    ITEM_TYPE("itemType"), // where used ?
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
    REFER("refer"), // where used?
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
    
    SchemaAttributes(String docName) {
        this(docName, null);
    }
    
    public String getName() {
        return docName;
    }
    
    public Class getType() {
        if (type == null) {
            throw new RuntimeException("Attribute '"+ getName()+"' does not have proper type"); //NOI18N
        }
        return type;
    }
    
    public Class getMemberType() {
        return memberType;
    }
    
    private final String docName;
    private final Class type;
    private final Class memberType;
} 
