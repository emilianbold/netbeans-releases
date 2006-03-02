/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * TokenType.java
 *
 * Created on August 24, 2005, 3:07 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.xdm.nodes;

/**
 *
 * @author Ajit Bhate
 */
public enum TokenType {

	TOKEN_ELEMENT_NAME,
	TOKEN_ELEMENT_START_TAG,
	TOKEN_ELEMENT_END_TAG,
	TOKEN_ATTR_NAME,
	TOKEN_ATTR_NS,
	TOKEN_ATTR_VAL,
	TOKEN_ATTR_QUOTATION,
	TOKEN_ATTR_EQUAL,
	TOKEN_CHARACTER_DATA,
	TOKEN_WHITESPACE,
	TOKEN_COMMENT,
	TOKEN_COMMENT_TAG,
	TOKEN_PI_START_TAG,
	TOKEN_PI_NAME,
	TOKEN_PI_VAL,
	TOKEN_PI_END_TAG,
	TOKEN_DEC_ATTR_NAME,
	TOKEN_DEC_ATTR_VAL,
	TOKEN_CDATA_VAL,
	TOKEN_DTD_VAL,
	TOKEN_DOC_VAL,
	TOKEN_NS,
	TOKEN_NS_SEPARATOR,
	TOKEN_PRETTY_PRINT,
}
