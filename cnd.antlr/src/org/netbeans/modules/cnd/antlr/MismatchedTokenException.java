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

import org.netbeans.modules.cnd.antlr.collections.impl.BitSet;
import org.netbeans.modules.cnd.antlr.collections.AST;

public class MismatchedTokenException extends RecognitionException {
    // Token names array for formatting
    String[] tokenNames;
    // The token that was encountered
    public Token token;
    // The offending AST node if tree walking
    public AST node;

    String tokenText = null; // taken from node or token object

    // Types of tokens
    public static final int TOKEN = 1;
    public static final int NOT_TOKEN = 2;
    public static final int RANGE = 3;
    public static final int NOT_RANGE = 4;
    public static final int SET = 5;
    public static final int NOT_SET = 6;
    // One of the above
    public int mismatchType;

    // For TOKEN/NOT_TOKEN and RANGE/NOT_RANGE
    public int expecting;

    // For RANGE/NOT_RANGE (expecting is lower bound of range)
    public int upper;

    // For SET/NOT_SET
    public BitSet set;

    /** Looking for AST wildcard, didn't find it */
    public MismatchedTokenException() {
        super("Mismatched Token: expecting any AST node", "<AST>", -1, -1);
    }

    // Expected range / not range
    public MismatchedTokenException(String[] tokenNames_, AST node_, int lower, int upper_, boolean matchNot) {
        super("Mismatched Token", "<AST>", node_==null? -1:node_.getLine(), node_==null? -1:node_.getColumn());
        tokenNames = tokenNames_;
        node = node_;
        if (node_ == null) {
            tokenText = "<empty tree>";
        }
        else {
            tokenText = node_.toString();
        }
        mismatchType = matchNot ? NOT_RANGE : RANGE;
        expecting = lower;
        upper = upper_;
    }

    // Expected token / not token
    public MismatchedTokenException(String[] tokenNames_, AST node_, int expecting_, boolean matchNot) {
		super("Mismatched Token", "<AST>", node_==null? -1:node_.getLine(), node_==null? -1:node_.getColumn());
        tokenNames = tokenNames_;
        node = node_;
        if (node_ == null) {
            tokenText = "<empty tree>";
        }
        else {
            tokenText = node_.toString();
        }
        mismatchType = matchNot ? NOT_TOKEN : TOKEN;
        expecting = expecting_;
    }

    // Expected BitSet / not BitSet
    public MismatchedTokenException(String[] tokenNames_, AST node_, BitSet set_, boolean matchNot) {
		super("Mismatched Token", "<AST>", node_==null? -1:node_.getLine(), node_==null? -1:node_.getColumn());
        tokenNames = tokenNames_;
        node = node_;
        if (node_ == null) {
            tokenText = "<empty tree>";
        }
        else {
            tokenText = node_.toString();
        }
        mismatchType = matchNot ? NOT_SET : SET;
        set = set_;
    }

    // Expected range / not range
    public MismatchedTokenException(String[] tokenNames_, Token token_, int lower, int upper_, boolean matchNot, String fileName_) {
        super("Mismatched Token", fileName_, token_.getLine(), token_.getColumn());
        tokenNames = tokenNames_;
        token = token_;
        tokenText = token_.getText();
        mismatchType = matchNot ? NOT_RANGE : RANGE;
        expecting = lower;
        upper = upper_;
    }

    // Expected token / not token
    public MismatchedTokenException(String[] tokenNames_, Token token_, int expecting_, boolean matchNot, String fileName_) {
        super("Mismatched Token", fileName_, token_.getLine(), token_.getColumn());
        tokenNames = tokenNames_;
        token = token_;
        tokenText = token_.getText();
        mismatchType = matchNot ? NOT_TOKEN : TOKEN;
        expecting = expecting_;
    }

    // Expected BitSet / not BitSet
    public MismatchedTokenException(String[] tokenNames_, Token token_, BitSet set_, boolean matchNot, String fileName_) {
        super("Mismatched Token", fileName_, token_.getLine(), token_.getColumn());
        tokenNames = tokenNames_;
        token = token_;
        tokenText = token_.getText();
        mismatchType = matchNot ? NOT_SET : SET;
        set = set_;
    }

    /**
     * Returns a clean error message (no line number/column information)
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();

        switch (mismatchType) {
            case TOKEN:
                sb.append("expecting " + tokenName(expecting) + ", found '" + tokenText + "'");
                break;
            case NOT_TOKEN:
                sb.append("expecting anything but " + tokenName(expecting) + "; got it anyway");
                break;
            case RANGE:
                sb.append("expecting token in range: " + tokenName(expecting) + ".." + tokenName(upper) + ", found '" + tokenText + "'");
                break;
            case NOT_RANGE:
                sb.append("expecting token NOT in range: " + tokenName(expecting) + ".." + tokenName(upper) + ", found '" + tokenText + "'");
                break;
            case SET:
            case NOT_SET:
                sb.append("expecting " + (mismatchType == NOT_SET ? "NOT " : "") + "one of (");
                int[] elems = set.toArray();
                for (int i = 0; i < elems.length; i++) {
                    sb.append(" ");
                    sb.append(tokenName(elems[i]));
                }
                sb.append("), found '" + tokenText + "'");
                break;
            default :
                sb.append(super.getMessage());
                break;
        }

        return sb.toString();
    }

    private String tokenName(int tokenType) {
        return tokenName(tokenNames, tokenType);
    }

    public static String tokenName(String[] tokenNames, int tokenType) {
        if (tokenType == Token.INVALID_TYPE) {
            return "<Set of tokens>";
        }
        else if (tokenType < 0 || tokenType >= tokenNames.length) {
            return "<" + String.valueOf(tokenType) + ">";
        }
        else {
            return tokenNames[tokenType];
        }
    }

    @Override
    public String getTokenText() {
        return tokenText;
    }

}
