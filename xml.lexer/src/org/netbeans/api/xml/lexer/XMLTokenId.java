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
package org.netbeans.api.xml.lexer;

import org.netbeans.api.lexer.TokenId;

/**
 * Token ids of XML language
 */
public enum XMLTokenId implements TokenId {
    
    /** Plain text */
    TEXT("xml-text"),
    /** Erroneous Text */
    WS("xml-ws"),
    /** Plain Text*/
    ERROR("xml-error"),
    /** XML Tag */
    TAG("xml-tag"),
    /** Argument of a tag */
    ARGUMENT("xml-attribute"),
    /** Operators - '=' between arg and value */
    OPERATOR("xml-operator"),
    /** Value - value of an argument */
    VALUE("xml-value"),
    /** Block comment */
    BLOCK_COMMENT("xml-comment"),
    /** SGML declaration in XML document - e.g. <!DOCTYPE> */
    DECLARATION("xml-doctype"),
    /** Character reference, e.g. &amp;lt; = &lt; */
    CHARACTER("xml-ref"),
    /** End of line */
    EOL("xml-EOL"),
    /* PI start delimiter <sample><b>&lt;?</b>target content of pi ?></sample> */
    PI_START("xml-pi-start"),    
    /* PI target <sample>&lt;?<b>target</b> content of pi ?></sample> */
    PI_TARGET("xml-pi-target"),
    /* PI conetnt <sample>&lt;?target <b>content of pi </b>?></sample> */
    PI_CONTENT("pi-content"),
    /* PI end delimiter <sample>&lt;?target <content of pi <b>?></b></sample> */
    PI_END("pi-end"),
    /** Cdata section including its delimiters. */
    CDATA_SECTION("xml-cdata-section");

    private final String primaryCategory;

    XMLTokenId() {
        this(null);
    }

    XMLTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

}
