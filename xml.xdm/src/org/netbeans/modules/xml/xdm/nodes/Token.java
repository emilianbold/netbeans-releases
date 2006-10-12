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

package org.netbeans.modules.xml.xdm.nodes;

/**
 *
 * @author Ajit Bhate
 */
public class Token {
    
    Token(String val, TokenType type) {
	value = val;
	this.type = type;
    }
    
    public String getValue() {
	return value;
    }
    
    public TokenType getType() {
	return type;
    }
    
    @Override
    public int hashCode() {
	return getValue().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof Token)) return false;
	Token token = (Token)obj;
	return((token.getValue().equals(getValue())) && 
	       (token.getType().equals(getType())));
    }
    
    @Override
    public String toString() {
	return getType() + " '" + value + "'";
    }
    
    public static Token create(String value, TokenType type) {
	Token t = null;
	switch(type) {
	    case TOKEN_ATTR_EQUAL: {
		t = EQUALS_TOKEN;
		break;
	    } case TOKEN_ELEMENT_END_TAG: {
		t = value.length() == 1 ? CLOSE_ELEMENT:SELF_CLOSE_ELEMENT;
		break;
	    } 
	    default: {
		t = new Token(value,type);
	    }
	}
	assert t != null;
	return t;
    }
    
    private static final Token EQUALS_TOKEN = 
	new Token("=", TokenType.TOKEN_ATTR_EQUAL); //NOI18N
    
    private static final Token CLOSE_ELEMENT =
	new Token(">", TokenType.TOKEN_ELEMENT_END_TAG); //NOI18N
    
    private static final Token SELF_CLOSE_ELEMENT =
	new Token("/>", TokenType.TOKEN_ELEMENT_END_TAG); //NOI18N
    
    public static final Token CDATA_START = 
	new Token("<![CDATA[", TokenType.TOKEN_CDATA_VAL); //NOI18N
    
    public static final Token CDATA_END =
	new Token("]]>", TokenType.TOKEN_CDATA_VAL); //NOI18N
    
    public static final Token COMMENT_START = 
	new Token("<!--", TokenType.TOKEN_CDATA_VAL); //NOI18N
    
    public static final Token COMMENT_END =
	new Token("-->", TokenType.TOKEN_CDATA_VAL); //NOI18N
    
    private final String value;
    private final TokenType type;
}
