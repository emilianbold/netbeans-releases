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

package org.netbeans.modules.cnd.apt.utils;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;

/**
 *
 * @author Vladimir Voskresensky
 */
public class TokenBasedTokenStream implements TokenStream {
    private Token token;
    private boolean first;
    
    /** Creates a new instance of TokenBasedTokenStream */
    public TokenBasedTokenStream(Token token) {
        if (token == null) {
            throw new NullPointerException("not possible to create token stream for null token");
        }
        this.token = token;
        this.first = true;
    }

    public Token nextToken() throws TokenStreamException {
        Token ret = null;
        if (first) {
            ret = token;
            first = false;
        } else {
            ret = APTUtils.EOF_TOKEN;
        }
        return ret;
    }

    public String toString() {
        String retValue;
        
        retValue = token.toString();
        return retValue;
    }    
}
