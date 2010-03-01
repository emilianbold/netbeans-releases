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

    private TplTopLexer(LexerRestartInfo<TplTopTokenId> info) {
        State state = info.state() == null? State.INIT : (State)info.state();
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
        OPEN_DELIMITER,
        CLOSE_DELIMITER,
        IN_SMARTY
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
                        if (cc == '{') {
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
                        state = State.IN_SMARTY;
                        return TplTopTokenId.T_SMARTY_OPEN_DELIMITER;
                    case CLOSE_DELIMITER:
                        state = State.OUTER;
                        return TplTopTokenId.T_SMARTY_CLOSE_DELIMITER;
                    case IN_SMARTY:
                        if (cc == '}') {
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
