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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.cnd.api.lexer;

import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Vladirmir Voskresensky
 */
public class CndTokenUtilities {

    private CndTokenUtilities() {
    }

    /**
     * method should be called under document read lock
     * @param doc
     * @param offset
     * @return
     */
    public static boolean isInPreprocessorDirective(Document doc, int offset) {
        TokenSequence<CppTokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, offset);
        if (cppTokenSequence != null) {
            cppTokenSequence.move(offset);
            if (cppTokenSequence.moveNext()) {
                return cppTokenSequence.token().id() == CppTokenId.PREPROCESSOR_DIRECTIVE;
            }
        }
        return false;
    }

    /**
     * method should be called under document read lock and token processor must be
     * very fast to prevent document blocking
     * @param tp
     * @param doc
     * @param startOffset
     * @param lastOffset
     */
    public static void processTokens(CppTokenProcessor tp, Document doc, int startOffset, int lastOffset) {
        TokenSequence<CppTokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, 0);
        if (cppTokenSequence == null) {
            return;
        }
        if (startOffset > lastOffset) {
            return;
        }
        int shift = cppTokenSequence.move(startOffset);
        if (tp.getLastSeparatorOffset() >=0 && 
            tp.getLastSeparatorOffset() > startOffset && 
            tp.getLastSeparatorOffset() < lastOffset) {
            shift = cppTokenSequence.move(tp.getLastSeparatorOffset());
        }
        tp.start(startOffset, startOffset - shift);
        if (processTokensImpl(tp, cppTokenSequence, startOffset, lastOffset)) {
            tp.end(lastOffset, cppTokenSequence.offset());
        } else {
            tp.end(lastOffset, lastOffset);
        }
    }

    /**
     * method should be called under document read lock
     * @param doc
     * @param offset
     * @return
     */
    public static Token<CppTokenId> shiftToNonWhiteBwd(Document doc, int offset) {
        Token<CppTokenId> out = getOffsetTokenImpl(doc, offset, true, false);
        boolean firstTime = true;
        while (out != null &&
                (out.id() == CppTokenId.WHITESPACE ||
                (firstTime && (CppTokenId.WHITESPACE_CATEGORY.equals(out.id().primaryCategory()))))) {
            firstTime = false;
            int tokOffset = out.offset(null);
            if (tokOffset == 0) {
                break;
            }
            out = getOffsetTokenImpl(doc, tokOffset-1, true, false);
        }
        return out;
    }

    /**
     * method should be called under document read lock
     * returns offsetable token on interested offset
     * @param cppTokenSequence token sequence
     * @param offset interested offset
     * @return returns ofssetable token, but if offset is in the beginning of whitespace
     * or comment token, then it returns previous token
     */
    public static Token<CppTokenId> getOffsetTokenCheckPrev(Document doc, int offset) {
        return getOffsetTokenImpl(doc, offset, true, true);
    }

    /**
     * method should be called under document read lock
     * @param doc
     * @param offset
     * @return
     */
    public static Token<CppTokenId> getOffsetToken(Document doc, int offset) {
        return getOffsetToken(doc, offset, false);
    }

    /**
     * method should be called under document read lock
     * @param doc
     * @param offset
     * @param tokenizePP
     * @return
     */
    public static Token<CppTokenId> getOffsetToken(Document doc, int offset, boolean tokenizePP) {
        return getOffsetTokenImpl(doc, offset, tokenizePP, false);
    }

    private static Token<CppTokenId> getOffsetTokenImpl(Document doc, int offset, boolean tokenizePP, boolean checkPrevious) {
        TokenSequence<CppTokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, offset);
        if (cppTokenSequence == null) {
            return null;
        }
        Token<CppTokenId> offsetToken = getOffsetTokenImpl(cppTokenSequence, offset, checkPrevious);
        if (tokenizePP && offsetToken != null && offsetToken.id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
            @SuppressWarnings("unchecked")
            TokenSequence<CppTokenId> embedded = (TokenSequence<CppTokenId>) cppTokenSequence.embedded();
            assert embedded != null : "no embedding for preprocessor directive " + offsetToken;
            offsetToken = getOffsetTokenImpl(embedded, offset, checkPrevious);
        }
        return offsetToken;
    }

    private static Token<CppTokenId> getOffsetTokenImpl(TokenSequence<CppTokenId> cppTokenSequence, int offset, boolean checkPrevious) {
        if (cppTokenSequence == null) {
            return null;
        }
        int shift = cppTokenSequence.move(offset);
        Token<CppTokenId> offsetToken = null;
        boolean checkPrev = false;
        if (cppTokenSequence.moveNext()) {
            offsetToken = cppTokenSequence.offsetToken();
            if (checkPrevious && (shift == 0)) {
                String category = offsetToken.id().primaryCategory();
                if (CppTokenId.WHITESPACE_CATEGORY.equals(category) ||
                    CppTokenId.COMMENT_CATEGORY.equals(category) ||
                    CppTokenId.SEPARATOR_CATEGORY.equals(category) ||
                    CppTokenId.OPERATOR_CATEGORY.equals(category)) {
                    checkPrev = true;
                }
            }
        }
        if (checkPrev && cppTokenSequence.movePrevious()) {
            offsetToken = cppTokenSequence.offsetToken();
        }
        return offsetToken;
    }

    private static boolean processTokensImpl(CppTokenProcessor tp, TokenSequence<CppTokenId> cppTokenSequence, int startOffset, int lastOffset) {
        boolean processedToken = false;
        while (cppTokenSequence.moveNext()) {
            if (cppTokenSequence.offset() >= lastOffset) {
                break;
            }
            Token<CppTokenId> token = (Token<CppTokenId>) cppTokenSequence.token();
            if (tp.token(token, cppTokenSequence.offset())) {
                // process embedding
                @SuppressWarnings("unchecked")
                TokenSequence<CppTokenId> embedded = (TokenSequence<CppTokenId>) cppTokenSequence.embedded();
                if (embedded != null) {
                    if (cppTokenSequence.offset() < startOffset) {
                        embedded.move(startOffset);
                    }
                    processedToken |= processTokensImpl(tp, embedded, startOffset, lastOffset);
                }
            } else {
                processedToken = true;
            }
        }
        return processedToken;
    }


}
