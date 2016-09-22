/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.antlr.collections.impl.BitSet;

public class NoViableAltException extends RecognitionException {
    public Token token = null;
    public AST node = null;	// handles parsing and treeparsing
    private BitSet expected = null;
    private String[] tokenNames;

    private static final boolean hideExpected = Boolean.getBoolean("antlr.exceptions.hideExpectedTokens");

    public NoViableAltException(AST t) {
        super("NoViableAlt", "<AST>", t.getLine(), t.getColumn());
        node = t;
    }

    public NoViableAltException(Token t, String fileName_) {
        super("NoViableAlt", fileName_, t.getLine(), t.getColumn());
        token = t;
    }

    public NoViableAltException(Token t, String fileName_, BitSet expected, String[] tokenNames) {
        this(t, fileName_);
        this.expected = expected;
        this.tokenNames = tokenNames;
    }

    public BitSet getExpected() {
        return expected;
    }
    
    /**
     * Returns a clean error message (no line number/column information)
     */
    @Override
    public String getMessage() {
        if (token != null) {
            String res = "unexpected token: " + token.getText();
            if (hideExpected) {
                return res;
            }
            if (tokenNames != null) {
                res += "(" + MismatchedTokenException.tokenName(tokenNames, token.getType()) + ")";
            }
            if (expected != null) {
                res += ", expected one of ";
                if (tokenNames != null) {
                    StringBuilder sb = new StringBuilder();
                    int[] elems = expected.toArray();
                        for (int i = 0; i < elems.length; i++) {
                            sb.append(", ");
                            sb.append(MismatchedTokenException.tokenName(tokenNames, elems[i]));
                    }
                    res += sb;
                } else {
                    res += expected;
                }
            }
            return res;
        }

        // must a tree parser error if token==null
        if (node == TreeParser.ASTNULL) {
            return "unexpected end of subtree";
        }
        return "unexpected AST node: " + node.toString();
    }

    @Override
    public String getTokenText() {
        return token.getText();
    }

}
