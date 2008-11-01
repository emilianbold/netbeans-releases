/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
//    private int curlies = 0;
//    private int parens = 0;
//    private int squares = 0;
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
