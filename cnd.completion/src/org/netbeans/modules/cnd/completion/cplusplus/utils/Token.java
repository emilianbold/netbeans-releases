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

package org.netbeans.modules.cnd.completion.cplusplus.utils;

import org.netbeans.editor.TokenID;

/**
 * simple token to present one element of document
 * @author Vladimir Voskresensky
 */
public final class Token {
    
    private int tokenLen;
    private int tokenStart;
    private TokenID tokenID;
    private String text;
    
    public Token(int start, int len, TokenID tokenID, String text) {
        this.tokenStart = start;
        this.tokenLen = len;
        this.tokenID = tokenID;
        this.text = text;
    }

    public int getStartOffset() {
        return tokenStart;
    }
    
    public int getEndOffset() {
        return tokenStart + tokenLen;
    }

    public int getLength() {
        return tokenLen;
    }
    
    public TokenID getTokenID() {
        return tokenID;
    }

    public String getText() {
        return text;
    }
    
    public String toString() {
        String retValue = tokenID + "[" + tokenStart + ", " + tokenLen + "]" + text; //NOI18N
        return retValue;
    }
}
