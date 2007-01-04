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
 * filter to remove comments from token stream
 * @author Vladimir Voskresensky
 */
public class APTCommentsFilter implements TokenStream {
    private final TokenStream orig;
    
    /** Creates a new instance of APTCommentsFilter */
    public APTCommentsFilter(TokenStream orig) {
        this.orig = orig;
    }

    public Token nextToken() throws TokenStreamException {
        Token next = orig.nextToken();
        while (APTUtils.isCommentToken(next)) {
            next = orig.nextToken();
        }
        return next;
    }    

    public String toString() {
        String retValue;
        
        retValue = orig.toString();
        return retValue;
    }
    
    
}
