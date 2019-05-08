/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.indent;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;

/**
 *
 */
public final class TokenItem {

    private final int index;
    private final TokenId tokenId;
    protected final TokenSequence<TokenId> tokenSeq;
    private final boolean skipPP;

    public TokenItem(TokenSequence<TokenId> ts, boolean skipPP) {
        index = ts.index();
        tokenId = ts.token().id();
        this.tokenSeq = ts;
        this.skipPP = skipPP;
    }

    public TokenSequence<TokenId> getTokenSequence() {
        return tokenSeq;
    }

    public boolean isSkipPP(){
        return skipPP;
    }
    
    private void go() {
        tokenSeq.moveIndex(index);
        tokenSeq.moveNext();
    }

    public TokenId getTokenID() {
        return tokenId;
    }

    public CppTokenId getTokenPPID() {
        TokenSequence<CppTokenId> prep = tokenSeq.embedded(CppTokenId.languagePreproc());
        if (prep == null){
            return CppTokenId.PREPROCESSOR_START;
        }
        prep.moveStart();
        while (prep.moveNext()) {
            if (!(prep.token().id() == CppTokenId.WHITESPACE ||
                    prep.token().id() == CppTokenId.PREPROCESSOR_START ||
                    prep.token().id() == CppTokenId.PREPROCESSOR_START_ALT)) {
                break;
            }
        }
        Token<CppTokenId> directive = null;
        if (prep.token() != null) {
            directive = prep.token();
        }
        if (directive != null) {
             switch (directive.id()) {
                case PREPROCESSOR_DIRECTIVE:
                case PREPROCESSOR_IF:
                case PREPROCESSOR_IFDEF:
                case PREPROCESSOR_IFNDEF:
                case PREPROCESSOR_ELSE:
                case PREPROCESSOR_ELIF:
                case PREPROCESSOR_ENDIF:
                case PREPROCESSOR_DEFINE:
                case PREPROCESSOR_UNDEF:
                case PREPROCESSOR_INCLUDE:
                case PREPROCESSOR_INCLUDE_NEXT:
                case PREPROCESSOR_LINE:
                case PREPROCESSOR_IDENT:
                case PREPROCESSOR_PRAGMA:
                case PREPROCESSOR_WARNING:
                case PREPROCESSOR_ERROR:
                case PREPROCESSOR_DEFINED:
                    return directive.id();
                default:
                     break;
            }
        }
        return CppTokenId.PREPROCESSOR_START;
    }

    public int index() {
        return index;
    }

    public TokenItem getNext() {
        go();
        while (tokenSeq.moveNext()) {
            if (!skipPP || tokenSeq.token().id() != CppTokenId.PREPROCESSOR_DIRECTIVE) {
                return new TokenItem(tokenSeq, skipPP);
            }
        }
        return null;
    }

    public TokenItem getPrevious() {
        go();
        while (tokenSeq.movePrevious()) {
            if (!skipPP || tokenSeq.token().id() != CppTokenId.PREPROCESSOR_DIRECTIVE) {
                return new TokenItem(tokenSeq, skipPP);
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TokenItem) {
            return ((TokenItem) obj).index == index;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + this.index;
        hash = 43 * hash + (this.tokenId != null ? this.tokenId.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return tokenId+"("+index+")"; // NOI18N
    }
}
