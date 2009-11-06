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

package org.netbeans.modules.cnd.antlr;

/**
 *
 * @author gorrus
 */
public class TokenImpl implements Token {
    // each Token has at least a token type
    protected int type = INVALID_TYPE;

    // the illegal token object
    public static Token badToken = new TokenImpl(INVALID_TYPE, "<no text>");

    public static Token EOF_TOKEN = new TokenImpl(EOF_TYPE, "<EOF>");

    public TokenImpl() {
    }

    public TokenImpl(int t) {
        type = t;
    }

    public TokenImpl(int t, String txt) {
        type = t;
        setText(txt);
    }

    public int getColumn() {
        return 0;
    }

    public int getLine() {
        return 0;
    }

    public String getFilename() {
        return null;
    }

    public void setFilename(String name) {
    }

    public String getText() {
        return "<no text>";
    }

    public void setText(String t) {
    }

    public void setColumn(int c) {
    }

    public void setLine(int l) {
    }

    public int getType() {
        return type;
    }

    public void setType(int t) {
        type = t;
    }

    public String toString() {
        return "[\"" + getText() + "\",<" + getType() + ">]";
    }
}
