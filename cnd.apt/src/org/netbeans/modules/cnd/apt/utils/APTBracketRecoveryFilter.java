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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.apt.utils;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Vladimir Voskresensky
 */
public class APTBracketRecoveryFilter implements TokenStream {
    private final TokenStream orig;
    private int curlies = 0;
    private int parens = 0;
    private int squares = 0;
    private int pos = -1;
    private final List<Token> stack = new ArrayList<Token>(1000);
    private Token last = null;
    
    ////////////////////////////////////////////////////////////////
    // state machine:
    // action + token to return
    // stack\cur    {       }       (       )       [       ]
    //            push     err={   push   err=(    push   err=[
    //  {         push     pop     push   skip=)   push   skip=]
    //  (         err=)    err=)   push   pop      push   err=)
    // "!M"(      err=)    skip=}  push   pop      push   skip=]
    //  [         err=]    err=]   push   err=]    push   pop
    // "!M"[      err=]    skip=}  push   skip=}   push   pop
    
    private boolean recoveryMode = false;
    
    public APTBracketRecoveryFilter(TokenStream orig) {
        this.orig = orig;
    }
 
    public Token nextToken() throws TokenStreamException {
        Token out;
        if (recoveryMode) {
            assert last != null;
            out = checkToken(last);
        } else {
            last = orig.nextToken();
            out = checkToken(last);
        }
        return out;
    }     

    private Token checkToken(Token last) {
        int matchedBracket = APTUtils.getMatchBracket(last.getType());
        int topToken = peek().getType();
        return null;
    }
    
    private Token createMatchedToken(Token base) {
        return APTUtils.createAPTToken(base);
    }
    
    private Token peek() {
        assert pos < stack.size();
        return pos < 0 ? APTUtils.EOF_TOKEN : stack.get(pos);
    }

    private Token pop() {
        assert pos >= 0;
        return stack.remove(pos--);
    }
}
