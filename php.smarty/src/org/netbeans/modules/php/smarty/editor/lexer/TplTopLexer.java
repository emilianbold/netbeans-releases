/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.smarty.editor.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.modules.php.smarty.SmartyFramework;
import org.netbeans.modules.php.smarty.editor.utlis.LexerUtils;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author Martin Fousek
 */
public class TplTopLexer implements Lexer<TplTopTokenId> {

    private final TplTopColoringLexer scanner;
    private TokenFactory<TplTopTokenId> tokenFactory;

    private class CompoundState {
        private State lexerState;
        private SubState lexerSubState;

        public CompoundState(State lexerState, SubState lexerSubState) {
            this.lexerState = lexerState;
            this.lexerSubState = lexerSubState;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CompoundState other = (CompoundState) obj;
            if (this.lexerState != other.lexerState) {
                return false;
            }
            if (this.lexerSubState != other.lexerSubState) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + this.lexerState.ordinal();
            hash = 17 * hash + this.lexerSubState.ordinal();
            return hash;
        }

        @Override
        public String toString() {
            return "State(hash=" + hashCode() + ",s=" + lexerState + ",ss=" + lexerSubState + ")"; //NOI18N
        }

    }

    private TplTopLexer(LexerRestartInfo<TplTopTokenId> info) {
        CompoundState state = null;
        if (info.state() == null) {
            state = new CompoundState(State.INIT, SubState.NO_SUB_STATE);
        } else {
            state = (CompoundState)info.state();
        }
        this.tokenFactory = info.tokenFactory();
        scanner = new TplTopColoringLexer(info, state);
    }

    /**
     * Create new top lexer.
     * @param info where was the parsing started
     * @return new lexer for additional parsing
     */
    public static synchronized TplTopLexer create(LexerRestartInfo<TplTopTokenId> info) {
        return new TplTopLexer(info);
    }

    public Token<TplTopTokenId> nextToken() {
        TplTopTokenId tokenId = scanner.nextToken();
        Token<TplTopTokenId> token = null;
        if (tokenId != null) {
            token = tokenFactory.createToken(tokenId);
        }
        return token;
    }

    public Object state() {
        return scanner.getState();
    }

    public void release() {
    }

    private enum State {
        INIT,
        OUTER,
        AFTER_DELIMITER,
        OPEN_DELIMITER,
        CLOSE_DELIMITER,
        IN_COMMENT,
        IN_SMARTY,
        IN_PHP,
        AFTER_SUBSTATE,
        IN_LITERAL
    }

    private enum SubState {
        NO_SUB_STATE,
        PHP_CODE,
        LITERAL
    }

    private class TplTopColoringLexer {

        private State state;
        private final LexerInput input;
        private SubState subState;

        public TplTopColoringLexer(LexerRestartInfo<TplTopTokenId> info, CompoundState state) {
            this.input = info.input();
            this.state = state.lexerState;
            this.subState = state.lexerSubState;
        }

        public TplTopTokenId nextToken() {
            int c = input.read();
            CharSequence text;
            int textLength;
            int openDelimiterLength = getOpenDelimiterLength();
            int closeDelimiterLength = getCloseDelimiterLength();
            if (c == LexerInput.EOF) {
                return null;
            }
            while (c != LexerInput.EOF) {
                char cc = (char) c;
                text = input.readText();
                textLength = text.length();
                switch (state) {
                case INIT:
                case OUTER:
                    if (isSmartyOpenDelimiter(text)) {
                        state = State.OPEN_DELIMITER;
                        input.backup(openDelimiterLength);
                        if (textLength > openDelimiterLength) {
                            return TplTopTokenId.T_HTML;
                        }
                    }
                    if (cc == '\n') {
                        return TplTopTokenId.T_HTML;
                    }
                    break;

                case OPEN_DELIMITER:
                    if (textLength < openDelimiterLength) {
                        break;
                    }
                    state = State.AFTER_DELIMITER;
                    if (subState == subState.NO_SUB_STATE) {
                        return TplTopTokenId.T_SMARTY_OPEN_DELIMITER;
                    } else {
                        if (input.readLength() > openDelimiterLength) {
                            input.backup(input.readLength() - openDelimiterLength);
                            if (subState == subState.LITERAL)
                                return TplTopTokenId.T_HTML;
                            else
                                return TplTopTokenId.T_PHP;
                        }
                        break;
                    }

                case AFTER_DELIMITER:
                    if (LexerUtils.isWS(c)) {
                        if (subState == subState.NO_SUB_STATE) {
                            return TplTopTokenId.T_SMARTY;
                        } else {
                            break;
                        }
                    }
                    else {
                        String lastWord = readNextWord(c);
                        switch(subState){
                            case LITERAL:
                                if (lastWord.startsWith("/literal")) {
                                    subState = SubState.NO_SUB_STATE;
                                    state = State.OPEN_DELIMITER;
                                    input.backup(input.readLength());
                                    break;
                                } else {
                                    input.backup(input.readLength()-1);
                                    state = State.IN_LITERAL;
                                }
                                return TplTopTokenId.T_HTML;
                            case PHP_CODE:
                                if (lastWord.startsWith("/php")) {
                                    subState = SubState.NO_SUB_STATE;
                                    state = State.OPEN_DELIMITER;
                                    input.backup(input.readLength());
                                    break;
                                } else {
                                    state = State.IN_PHP;
                                }
                                return TplTopTokenId.T_PHP;
                            default:
                               if (lastWord.charAt(0) == '*') {
                                    state = State.IN_COMMENT;
                                    input.backup(lastWord.length()-1);
                                    return TplTopTokenId.T_COMMENT;
                                }
                                else if (lastWord.startsWith("literal")) {
                                    subState = SubState.LITERAL;
                                    state = State.AFTER_SUBSTATE;
                                    input.backup(lastWord.length()-7);
                                    return TplTopTokenId.T_SMARTY;
                                }
                                else if (lastWord.startsWith("php")) {
                                    subState = SubState.PHP_CODE;
                                    state = State.AFTER_SUBSTATE;
                                    input.backup(lastWord.length()-3);
                                    return TplTopTokenId.T_SMARTY;
                                }
                                else {
                                    state = State.IN_SMARTY;
                                    input.backup(lastWord.length());
                                }
                        }
                    }
                    break;

                case IN_COMMENT:
                    if (cc == '*') {
                        state = State.AFTER_SUBSTATE;
                        return TplTopTokenId.T_COMMENT;
                    }
                    return TplTopTokenId.T_COMMENT;

                case AFTER_SUBSTATE:
                    if (LexerUtils.isWS(c)) {
                        return TplTopTokenId.T_SMARTY;
                    }
                    else if (isSmartyCloseDelimiter(text)) {
                        state = State.CLOSE_DELIMITER;
                        input.backup(closeDelimiterLength);
                        break;
                    } else {
                        break;
                    }

                case CLOSE_DELIMITER:
                    if (textLength < closeDelimiterLength) {
                        break;
                    }
                    switch(subState){
                        case LITERAL:
                            state = State.IN_LITERAL;
                            break;
                        case PHP_CODE:
                            state = State.IN_PHP;
                            break;
                        default:
                            state = State.OUTER;
                            break;
                    }
                    return TplTopTokenId.T_SMARTY_CLOSE_DELIMITER;

                case IN_PHP:
                    if (isSmartyOpenDelimiter(text)) {
                        state = State.OPEN_DELIMITER;
                        input.backup(openDelimiterLength);
                        if (input.readLength() > 0)
                            return TplTopTokenId.T_PHP;
                    }
                    if (input.readLength() > 1) {
                        return TplTopTokenId.T_PHP;
                    }
                    break;

                case IN_LITERAL:
                    if (isSmartyOpenDelimiter(text)) {
                        state = State.OPEN_DELIMITER;
                        input.backup(openDelimiterLength);
                    }
                    if (input.readLength() > 0) {
                        return TplTopTokenId.T_HTML;
                    }
                    break;

                case IN_SMARTY:
                    if (isSmartyCloseDelimiter(text)) {
                        if (textLength == closeDelimiterLength) {
                            state = State.OUTER;
                            return TplTopTokenId.T_SMARTY_CLOSE_DELIMITER;
                        }
                        else {
                            state = State.CLOSE_DELIMITER;
                            input.backup(closeDelimiterLength);
                            if (input.readLength() != 0) {
                                return TplTopTokenId.T_SMARTY;
                            }
                        }
                    }
                    switch(c) {
                        case '\n':
                           return TplTopTokenId.T_SMARTY;
                        case LexerInput.EOF:
                           return TplTopTokenId.T_SMARTY;
                        case '<':
                           state = State.OUTER;
                           input.backup(1);
                           if (input.readLength() > 1) {
                                return TplTopTokenId.T_SMARTY;
                           }
                    }
                    break;
                }
                c = input.read();
            }

            return getTokenId(state);
        }

        private TplTopTokenId getTokenId(State state) {
            switch (state) {
                case IN_SMARTY:
                    return TplTopTokenId.T_SMARTY;
                case OPEN_DELIMITER:
                    return TplTopTokenId.T_SMARTY_OPEN_DELIMITER;
                case CLOSE_DELIMITER:
                    return TplTopTokenId.T_SMARTY_CLOSE_DELIMITER;
                default:
                    return TplTopTokenId.T_HTML;
            }
        }

        Object getState() {
            return new CompoundState(state, subState);
        }

        private boolean isSmartyOpenDelimiter(CharSequence text) {
            if (SmartyFramework.useCustomDelimiters) {
                return (text.toString().endsWith(SmartyFramework.DELIMITER_CUSTOM_OPEN));
            }
            else {
                return (text.toString().endsWith(SmartyFramework.DELIMITER_DEFAULT_OPEN));
            }
        }

        private boolean isSmartyCloseDelimiter(CharSequence text) {
            if (SmartyFramework.useCustomDelimiters) {
                return (text.toString().endsWith(SmartyFramework.DELIMITER_CUSTOM_CLOSE));
            }
            else {
                return (text.toString().endsWith(SmartyFramework.DELIMITER_DEFAULT_CLOSE));
                }
        }
        
        private int getOpenDelimiterLength() {
            return (SmartyFramework.useCustomDelimiters?
                SmartyFramework.DELIMITER_CUSTOM_OPEN.length() : SmartyFramework.DELIMITER_DEFAULT_OPEN.length());
        }

        private int getCloseDelimiterLength() {
            return (SmartyFramework.useCustomDelimiters?
                SmartyFramework.DELIMITER_CUSTOM_CLOSE.length() : SmartyFramework.DELIMITER_DEFAULT_CLOSE.length());
        }

        private String readNextWord(int lastChar) {
            String word = Character.toString((char)lastChar);
            int c;
            while (!LexerUtils.isWS(c = input.read()) && c != LexerInput.EOF) {
                word += Character.toString((char)c);
            }
            input.backup(1);
            return word;
        }
    }
}
