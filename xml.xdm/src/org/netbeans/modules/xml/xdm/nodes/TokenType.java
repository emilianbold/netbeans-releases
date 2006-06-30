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
