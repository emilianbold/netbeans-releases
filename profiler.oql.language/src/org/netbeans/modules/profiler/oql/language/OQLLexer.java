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

package org.netbeans.modules.profiler.oql.language;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;


/**
 *
 * @author Jan Jancura
 */
class OQLLexer implements Lexer<OQLTokenId> {

    enum State {
        BEFORE_SELECT,
        AFTER_SELECT,
        AFTER_FROM,
        AFTER_INSTANCEOF,
        AFTER_CLASS,
        AFTER_ID,
        AFTER_WHERE
    };

    private LexerInput          input;
    private TokenFactory<OQLTokenId>
                                tokenFactory;
    private State               state = State.BEFORE_SELECT;


    OQLLexer (LexerRestartInfo<OQLTokenId> info) {
        input = info.input ();
        tokenFactory = info.tokenFactory ();
        if (info.state () != null)
            state = (State) info.state ();
    }

    public Token<OQLTokenId> nextToken () {
        switch (state) {
            case BEFORE_SELECT:
                if (input.read () == 's' &&
                    input.read () == 'e' &&
                    input.read () == 'l' &&
                    input.read () == 'e' &&
                    input.read () == 'c' &&
                    input.read () == 't'
                ) {
                    state = State.AFTER_SELECT;
                    return tokenFactory.createToken (OQLTokenId.KEYWORD);
                }
                if (input.readLength () == 0)
                    return null;
                return tokenFactory.createToken (OQLTokenId.ERROR);
            case AFTER_SELECT:
            case AFTER_WHERE:
                int i = input.read ();
                switch (i) {
                    case LexerInput.EOF:
                        return null;
                    case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        return finishNumberLiteral(input.read(), false);
                    case '0': // in a number literal
                        i = input.read();
                            if (i == 'x' || i == 'X') { // in hexadecimal (possibly floating-point) literal
                                boolean inFraction = false;
                                while (true) {
                                    switch (input.read()) {
                                        case '0': case '1': case '2': case '3': case '4':
                                        case '5': case '6': case '7': case '8': case '9':
                                        case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                                        case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                                            break;
                                        case '.': // hex float literal
                                            if (!inFraction) {
                                                inFraction = true;
                                            } else { // two dots in the float literal
                                                return tokenFactory.createToken (OQLTokenId.HEX_NUMBER);
                                            }
                                            break;
                                        case 'p': case 'P': // binary exponent
                                            return finishFloatExponent();
                                        default:
                                            input.backup(1);
                                            // if float then before mandatory binary exponent => invalid
                                            return tokenFactory.createToken (OQLTokenId.HEX_NUMBER);
                                    }
                                } // end of while(true)
                            }
                            return finishNumberLiteral(i, false);
                    case '"':
                        do {
                            i = input.read ();
                            if (i == '\\') {
                                i = input.read ();
                                i = input.read ();
                            }
                        } while (
                            i != '"' &&
                            i != '\n' &&
                            i != '\r' &&
                            i != LexerInput.EOF
                        );
                        return tokenFactory.createToken (OQLTokenId.STRING);
                    case '\'':
                        do {
                            i = input.read ();
                            if (i == '\\') {
                                i = input.read ();
                                i = input.read ();
                            }
                        } while (
                            i != '\'' &&
                            i != '\n' &&
                            i != '\r' &&
                            i != LexerInput.EOF
                        );
                        return tokenFactory.createToken (OQLTokenId.STRING);
                    case '>':
                    case '<':
                    case '=':
                    case '!':
                    case '*':
                    case '/':
                    case '+':
                    case '-':
                    case '%':
                        return tokenFactory.createToken (OQLTokenId.OPERATOR);
                    case '(':
                    case ')':
                    case '[':
                    case ']':
                        return tokenFactory.createToken (OQLTokenId.BRACES);
                    default:
                        String id = readIdentifier (i);
                        if (id != null) {
                            if (state == State.AFTER_SELECT &&
                                id.equals ("from")
                            ) {
                                state = State.AFTER_FROM;
                                return tokenFactory.createToken (OQLTokenId.KEYWORD);
                            }
                        }
                        return tokenFactory.createToken (OQLTokenId.IDENTIFIER);
                }// switch (i)
            case AFTER_FROM:
                i = input.read ();
                switch (i) {
                    case LexerInput.EOF:
                        return null;
                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                        return tokenFactory.createToken (OQLTokenId.WHITESPACE);
                    default:
                        String id = readIdentifier (i);
                        if (id != null) {
                            if (id.equals ("instanceof")) {
                                state = State.AFTER_INSTANCEOF;
                                return tokenFactory.createToken (OQLTokenId.KEYWORD);
                            }
                            return readClass ();
                        }
                        return tokenFactory.createToken (OQLTokenId.ERROR);
                }
            case AFTER_INSTANCEOF:
                i = input.read ();
                switch (i) {
                    case LexerInput.EOF:
                        return null;
                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                        return tokenFactory.createToken (OQLTokenId.WHITESPACE);
                    default:
                        String id = readIdentifier (input.read ());
                        if (id != null)
                            return readClass ();
                        return tokenFactory.createToken (OQLTokenId.ERROR);
                }
            case AFTER_CLASS:
                i = input.read ();
                switch (i) {
                    case LexerInput.EOF:
                        return null;
                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                        return tokenFactory.createToken (OQLTokenId.WHITESPACE);
                    default:
                        String id = readIdentifier (i);
                        if (id == null)
                            return tokenFactory.createToken (OQLTokenId.ERROR);
                        state = State.AFTER_ID;
                        return tokenFactory.createToken (OQLTokenId.IDENTIFIER);
                }
            case AFTER_ID:
                i = input.read ();
                switch (i) {
                    case LexerInput.EOF:
                        return null;
                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                        return tokenFactory.createToken (OQLTokenId.WHITESPACE);
                    default:
                        String id = readIdentifier (i);
                        if (id != null &&id.equals ("where")) {
                            state = State.AFTER_WHERE;
                            return tokenFactory.createToken (OQLTokenId.KEYWORD);
                        }
                        return tokenFactory.createToken (OQLTokenId.ERROR);
                }
            default:
                input.read ();
                return tokenFactory.createToken (OQLTokenId.ERROR);
        } // switch (state)
    }

    public Object state () {
        return state;
    }

    public void release () {
    }

    private Token<OQLTokenId> finishNumberLiteral(int c, boolean inFraction) {
        while (true) {
            switch (c) {
                case '.':
                    if (!inFraction) {
                        inFraction = true;
                    } else { // two dots in the literal
                        return tokenFactory.createToken (OQLTokenId.NUMERIC);
                    }
                    break;
                case 'l': case 'L': // 0l or 0L
                    return tokenFactory.createToken (OQLTokenId.NUMERIC);
                case 'd': case 'D':
                    return tokenFactory.createToken (OQLTokenId.NUMERIC);
                case 'f': case 'F':
                    return tokenFactory.createToken (OQLTokenId.NUMERIC);
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    break;
                case 'e': case 'E': // exponent part
                    return finishFloatExponent();
                default:
                    input.backup (1);
                    return tokenFactory.createToken (OQLTokenId.NUMERIC);
            }
            c = input.read();
        }
    }

    private Token<OQLTokenId> finishFloatExponent() {
        int c = input.read();
        if (c == '+' || c == '-') {
            c = input.read();
        }
        if (c < '0' || '9' < c)
            return tokenFactory.createToken (OQLTokenId.NUMERIC);
        do {
            c = input.read();
        } while ('0' <= c && c <= '9'); // reading exponent
        switch (c) {
            case 'd': case 'D':
                return tokenFactory.createToken (OQLTokenId.NUMERIC);
            case 'f': case 'F':
                return tokenFactory.createToken (OQLTokenId.NUMERIC);
            default:
                input.backup(1);
                return tokenFactory.createToken (OQLTokenId.NUMERIC);
        }
    }

    private String readIdentifier (int i) {
        if (i == LexerInput.EOF) return null;
        while (
            (i >= 'a' && i <= 'z') ||
            (i >= 'A' && i <= 'Z')
        )
            i = input.read ();
        if (i == LexerInput.EOF)
            return input.readText ().toString ();
        if (input.readLength () > 1) {
            if (i != LexerInput.EOF)
                input.backup (1);
            return input.readText ().toString ();
        }
        return null;
    }

    private Token<OQLTokenId> readClass () {
        while (true) {
            int i = input.read ();
            if (i != '.') {
                while (i == '[') {
                    if (input.read () != ']') {
                        if (i != LexerInput.EOF)
                            input.backup (2);
                        else
                            input.backup (1);
                        state = State.AFTER_CLASS;
                        return tokenFactory.createToken (OQLTokenId.CLASS);
                    }
                    i = input.read ();
                }
                if (i != LexerInput.EOF)
                    input.backup (1);
                state = State.AFTER_CLASS;
                return tokenFactory.createToken (OQLTokenId.CLASS);
            }
            String id = readIdentifier (input.read ());
            if (id != null) continue;
            if (i != LexerInput.EOF)
                input.backup (1);
            state = State.AFTER_CLASS;
            return tokenFactory.createToken (OQLTokenId.CLASS);
        }
    }
}


