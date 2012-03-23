/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.editor.cplusplus;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppStringTokenId;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;
import org.openide.util.CharSequences;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CppTypingCompletion {
    private CppTypingCompletion() {}

    static final class ExtraText {
        private final int caretPosition;
        private final int textPosition;
        private final String extraText;

        public ExtraText(int caretPosition) {
            this.caretPosition = caretPosition;
            this.textPosition = -1;
            this.extraText = null;
        }

        public ExtraText(int caretPosition, int textPosition, String text) {
            this.caretPosition = caretPosition;
            this.textPosition = textPosition;
            this.extraText = text;
        }

        public int getCaretPosition() {
            return caretPosition;
        }

        public String getExtraText() {
            return extraText;
        }

        public int getExtraTextPostion() {
            return textPosition;
        }
    }
    /**
     *
     * @param context
     * @return -1 if not handled, otherwise caret position to shift after insert
     */
    static ExtraText checkRawStringInsertion(TypedTextInterceptor.MutableContext context) {
        String text = context.getText();
        if (text.length() != 1) {
            return null;
        }
        BaseDocument doc = (BaseDocument) context.getDocument();
        int dotPos = context.getOffset();
        TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, dotPos, true, false);
        if (ts == null) {
            return null;
        }
        ExtraText rawStringTypingInfo = null;
        char typedChar = text.charAt(0);
        int offsetInToken = ts.move(dotPos);
        // special check for start of raw strings
        if (typedChar == '"') {
            if (offsetInToken == 0) { // between tokens
                if (ts.movePrevious()) { // move to previous possible ID token
                    Token<TokenId> tokenAtDot = ts.token();
                    if (tokenAtDot != null && (CppTokenId.IDENTIFIER == tokenAtDot.id() || CppTokenId.PREPROCESSOR_IDENTIFIER == tokenAtDot.id())) {
                        CharSequence tokText = tokenAtDot.text();
                        if (CppStringTokenId.PREFIX_R.fixedText().contentEquals(tokText)
                                || CppStringTokenId.PREFIX_UR.fixedText().contentEquals(tokText)
                                || CppStringTokenId.PREFIX_u8R.fixedText().contentEquals(tokText)) {
                            // this is start of raw string, need to close it, otherwise it will continue
                            // till the end of document
                            context.setText("\"()\"", 1); // NOI18N
                            rawStringTypingInfo = new ExtraText(dotPos + 1);
                        }
                    }
                    if (rawStringTypingInfo == null) {
                        ts.move(dotPos);
                    }
                }
            }
        }
        if (rawStringTypingInfo == null && ts.moveNext()) {
            Token<TokenId> token = ts.token();
            if (token.id() == CppTokenId.RAW_STRING_LITERAL) {
                // typing inside raw string delimeter should be symmetrical
                TokenSequence<?> es = ts.embedded();
                int offsetInStringToken = es.move(dotPos);
//                int tokenStartOffset = es.offset();
                if (es.moveNext()) {
                    @SuppressWarnings("unchecked")
                    Token<CppStringTokenId> strToken = (Token<CppStringTokenId>) es.token();
                    int strTokenOffset = es.offset();
                    CppStringTokenId id = strToken.id();
                    if (id != CppStringTokenId.START_DELIMETER &&
                        id != CppStringTokenId.START_DELIMETER_PAREN &&
                        id != CppStringTokenId.END_DELIMETER &&
                        id != CppStringTokenId.LAST_QUOTE) {
                        return null;
                    }
                    if (typedChar == '(' && id == CppStringTokenId.START_DELIMETER_PAREN) {
                        // eat (
                        context.setText("", 0);
                        return new ExtraText(dotPos + 1);
                    } else if (typedChar == '"' && id == CppStringTokenId.LAST_QUOTE) {
                        // eat closing "
                        context.setText("", 0);
                        return new ExtraText(dotPos + 1);
                    }
                    if (true) {
                        return null;
                    }
                    int startDelimPos = -1;
                    Token<?> startDelim = null;
                    Token<?> endDelim = null;
                    int endDelimPos = -1;
                    if (id == CppStringTokenId.START_DELIMETER_PAREN) {
                        // check if there is start delimeter before paren
                        if (es.movePrevious() && es.token().id() == CppStringTokenId.START_DELIMETER) {
                            strToken = (Token<CppStringTokenId>) es.token();
                            offsetInStringToken = strToken.length();
                        }
                    }
                    if (id == CppStringTokenId.START_DELIMETER) {
                        // before or inside start delimeter
                        es.moveEnd();
                        while (es.movePrevious()) {
                            if (es.token().id() == CppStringTokenId.END_DELIMETER) {
                                if (CharSequences.comparator().compare(strToken.text(), es.token().text()) == 0) {
                                    return new ExtraText(dotPos + 1, es.offset() + offsetInStringToken, "" + typedChar);
                                }
                                break;
                            } if (es.token().id() == CppStringTokenId.END_DELIMETER_PAREN) {
                                // no end delimeter
                                break;
                            }
                        }
                    } else if (id == CppStringTokenId.START_DELIMETER_PAREN) {

                        startDelim = strToken;
                        startDelimPos = strTokenOffset;
                        int index = es.index();
                        es.moveEnd();
                        while (es.movePrevious()) {
                            if (es.token().id() == CppStringTokenId.END_DELIMETER) {
                                startDelim = es.token();
                                startDelimPos = es.offset();
                                break;
                            }
                            if (es.token().id() == CppStringTokenId.END_DELIMETER_PAREN) {
                                return null;
                            }
                        }
                        es.moveIndex(index);
                    } else if (id == CppStringTokenId.END_DELIMETER) {

                    } else if (id == CppStringTokenId.LAST_QUOTE) {
                        int index = es.index();
                        es.moveStart();
                        while (es.moveNext()) {
                            if (es.token().id() == CppStringTokenId.START_DELIMETER ||
                                es.token().id() == CppStringTokenId.START_DELIMETER_PAREN) {
                                startDelim = es.token();
                                startDelimPos = es.offset();
                                break;
                            }
                        }
                        es.moveIndex(index);
                    }
                    int tokLen = strToken.length();

                    if (id == CppStringTokenId.START_DELIMETER_PAREN ||
                        id == CppStringTokenId.START_DELIMETER) {
                        if (typedChar == '(' && id == CppStringTokenId.START_DELIMETER_PAREN) {
                        } else {
                            // prepare for insertion
                            startDelim = strToken;
                            startDelimPos = es.offset();
                            es.moveEnd();
                            while (es.movePrevious()) {
                                endDelim = es.token();
                                if (endDelim.id() == CppStringTokenId.END_DELIMETER) {
                                    // found
                                    endDelimPos = es.offset();
                                    break;
                                } else if (endDelim.id() == CppStringTokenId.END_DELIMETER_PAREN) {
                                    // no delimeter move back to the end
                                    if (es.moveNext()) {
                                        endDelim = es.token();
                                        endDelimPos = es.offset();
                                    } else {
                                        endDelim = null;
                                        endDelimPos = -1;
                                    }
                                    break;
                                } else {
                                    endDelim = null;
                                }
                            }
                        }
                    } else if (id == CppStringTokenId.LAST_QUOTE || id == CppStringTokenId.END_DELIMETER) {
                        if (typedChar == '"' && id == CppStringTokenId.LAST_QUOTE) {
                            assert offsetInStringToken == 0;
                        } else {
                            endDelim = strToken;
                            endDelimPos = es.offset();
                            es.moveStart();
                            while (es.moveNext()) {
                                startDelim = es.token();
                                if (startDelim.id() == CppStringTokenId.START_DELIMETER
                                        || startDelim.id() == CppStringTokenId.START_DELIMETER_PAREN) {
                                    startDelimPos = es.offset();
                                    break;
                                } else {
                                    startDelim = null;
                                }
                            }
                        }
                    }
                    if (rawStringTypingInfo == null && startDelim != null && endDelim != null) {
                        // typing in start delimeter should by synced with end delim
                        if ((startDelim.id() == CppStringTokenId.START_DELIMETER_PAREN &&
                             endDelim.id() == CppStringTokenId.LAST_QUOTE) ||
                             CharSequences.comparator().compare(startDelim.text(), endDelim.text()) == 0) {
                            // we sync only equal texts
                            if (startDelim == strToken) {
                                // insert at the end token
                                rawStringTypingInfo = new ExtraText(dotPos + 1, endDelimPos + offsetInStringToken, "" + typedChar);
                            } else {
                                // insert at the end token
                                rawStringTypingInfo = new ExtraText(dotPos + 2, startDelimPos + offsetInStringToken, "" + typedChar);
                            }
                        }
                    }
                }
            }
        }
        return rawStringTypingInfo;
    }

}
