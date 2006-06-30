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
 * Token.java
 *
 * Created on August 24, 2005, 3:03 PM
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
public class Token {
	
	/** Creates a new instance of Token */
	public Token(String val, TokenType tokenType) {
		value = val;
		type = tokenType;
	}
	
	public String getValue() {
		return value;
	}
	
	public TokenType getType() {
		return type;
	}

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Token)) return false;
        Token token = (Token)obj;
        return((value == token.getValue()) && (type == token.getType()));
    }

    private String value;
	private TokenType type;
}
