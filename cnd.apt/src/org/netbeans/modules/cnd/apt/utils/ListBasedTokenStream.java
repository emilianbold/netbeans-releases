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
import java.util.Iterator;
import java.util.List;

/**
 * implementation of TokenStream based on list
 * passed list is unchanged
 * @author Vladimir Voskresensky
 */
public class ListBasedTokenStream implements TokenStream {
    private final List/*<Token>*/ tokens;
    private Iterator position;
    
    /** Creates a new instance of ListBasedTokenStream */
    public ListBasedTokenStream(List/*<Token>*/ tokens) {
        assert(tokens != null) : "not valid to pass null list"; // NOI18N
        this.tokens = tokens;
        position = tokens.iterator();
    }

    public Token nextToken() throws TokenStreamException {
        if (position.hasNext()) {
            return (Token) position.next();
        } else {
            return APTUtils.EOF_TOKEN;
        }
    }   

    public String toString() {
        return APTUtils.toString(new ListBasedTokenStream(tokens));
    }
    
    public List/*<Token>*/ getList() {
        return tokens;
    }
}
