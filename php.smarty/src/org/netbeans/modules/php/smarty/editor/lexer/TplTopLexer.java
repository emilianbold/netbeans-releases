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

    private boolean inPhpSources;

    private TplTopLexer(LexerRestartInfo<TplTopTokenId> info) {
        State state = info.state() == null? State.INIT : (State)info.state();
        this.tokenFactory = info.tokenFactory();
        this.inPhpSources = false;
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
        AFTER_PHP_DELIMITER,
        AFTER_DELIMITER,
        OPEN_DELIMITER,
        CLOSE_DELIMITER,
        IN_COMMENT,
        IN_SMARTY,
        IN_PHP,
        IN_PHP_TAG
    }

    private class TplTopColoringLexer {

        private State state;
        private final LexerInput input;

        public TplTopColoringLexer(LexerRestartInfo<TplTopTokenId> info, State state) {
            this.input = info.input();
            this.state = state;
        }

        public TplTopTokenId nextToken() {
            int c = input.read();
            CharSequence text;
            int textLength;
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
                        if (cc == SmartyFramework.TPL_OPEN_DELIMITER) {
                            state = State.OPEN_DELIMITER;
                            if (textLength > 1) {
                                input.backup(1);
                                return TplTopTokenId.T_HTML;
                            }
                            else {
                                input.backup(1);
                            }
                        }
                        break;
                    case OPEN_DELIMITER:
                        state = State.AFTER_DELIMITER;
                        return TplTopTokenId.T_SMARTY_OPEN_DELIMITER;
                    case CLOSE_DELIMITER:
                        state = State.OUTER;
                        return TplTopTokenId.T_SMARTY_CLOSE_DELIMITER;
                    case AFTER_DELIMITER:
                        // whitespaces after SMARTY delimiter
                        if (LexerUtils.isWS(c)) {
                            return TplTopTokenId.T_SMARTY;
                        }
                        // begin of SMARTY commands
                        else {
                            switch(c) {
                                case '*':
                                    state = State.IN_COMMENT;
                                    break;
                                case 'p':
                                    if (input.read() == 'h') {
                                        if (input.read() == 'p') {
                                            state = State.IN_PHP_TAG;
                                            return TplTopTokenId.T_PHP_DEL;
                                        }
                                        input.backup(1);
                                    }
                                    input.backup(1);
                                default:
                                    state = State.IN_SMARTY;
                                    input.backup(1);
                            }
                        }
                        break;
                    case IN_COMMENT:
                        if (cc == '*') {
                            state = State.IN_SMARTY;
                            return TplTopTokenId.T_COMMENT;
                        }
                        return TplTopTokenId.T_COMMENT;
                    case IN_PHP_TAG:
                        if( LexerUtils.isWS(c) ) {
                            return TplTopTokenId.T_SMARTY;
                        } else if (c == SmartyFramework.TPL_CLOSE_DELIMITER) {
                            if (!inPhpSources) {
                                state = State.IN_PHP;
                                inPhpSources = true;
                            }
                            else {
                                state = State.OUTER;
                                inPhpSources = false;
                            }
                            return TplTopTokenId.T_SMARTY_CLOSE_DELIMITER;
                        } else {
                            return TplTopTokenId.T_ERROR;
                        }
                    case IN_PHP:
                        if (cc == SmartyFramework.TPL_OPEN_DELIMITER) {
                            if (input.read() == '/') {
                                if (input.read() == 'p') {
                                    if (input.read() == 'h') {
                                        if (input.read() == 'p') {
                                            state = State.AFTER_PHP_DELIMITER;
                                            input.backup(4);
                                            return TplTopTokenId.T_SMARTY_OPEN_DELIMITER;
                                        }
                                        input.backup(1);
                                    }
                                    input.backup(1);
                                }
                                input.backup(1);
                            }
                            input.backup(1);
                        }
                        return TplTopTokenId.T_PHP;
                    case AFTER_PHP_DELIMITER:
                        state = State.IN_SMARTY;
                        input.read(); input.read(); input.read();
                        return TplTopTokenId.T_PHP_DEL;
                    case IN_SMARTY:
                        if (cc == SmartyFramework.TPL_CLOSE_DELIMITER) {
                            if (textLength == 1) {
                                state = State.OUTER;
                                return TplTopTokenId.T_SMARTY_CLOSE_DELIMITER;
                            }
                            else {
                                state = State.CLOSE_DELIMITER;
                                input.backup(1);
                                if (input.readLength() != 0) {
                                    return TplTopTokenId.T_SMARTY;
                                }
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
            return state;
        }
    }
}
