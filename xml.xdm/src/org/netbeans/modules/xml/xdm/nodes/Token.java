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

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
