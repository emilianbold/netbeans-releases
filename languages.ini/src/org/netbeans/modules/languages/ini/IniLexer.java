/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.languages.ini;

import java.util.Stack;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

class IniLexer implements Lexer<IniTokenId> {

    private enum State {
        START,
        COMMENT,
        NAME,
        EQUALS,
        VALUE,
        ERROR, // non-comment after section

        WHITESPACE,
        WHITESPACE_AFTER_NAME,
        WHITESPACE_AFTER_EQUALS,
    }

    // common
    private static final char ESCAPE = '\\'; // NOI18N
    private static final char SPACE = ' '; // NOI18N
    private static final char TAB = '\t'; // NOI18N
    private static final char LF = '\n'; // NOI18N
    private static final char CR = '\r'; // NOI18N

    // ini specific
    private static final char COMMENT = ';'; // NOI18N
    private static final char SECTION_START = '['; // NOI18N
    private static final char SECTION_END = ']'; // NOI18N
    private static final char EQUALS = '='; // NOI18N
    private static final char LONG_STRING_DOUBLE = '"'; // NOI18N
    private static final char LONG_STRING_SINGLE = '\''; // NOI18N

    private final LexerRestartInfo<IniTokenId> info;
    private State state = State.START;

    IniLexer(LexerRestartInfo<IniTokenId> info) {
        this.info = info;
        State startState = (State) info.state();
        if (startState != null) {
            state = startState;
        }
    }

    @Override
    public Object state() {
        return state;
    }

    @Override
    public void release() {
    }

    @Override
    public Token<IniTokenId> nextToken() {
        LexerInput input = info.input();
        int ch = input.read();
        if (ch == LexerInput.EOF) {
            return null;
        }
        input.backup(1);

        if (state.equals(State.START)) {
            if (ch == COMMENT) {
                state = State.COMMENT;
            } else if (isWhiteSpace(ch)) {
                state = State.WHITESPACE;
            } else {
                // NAME = VALUE
                state = State.NAME;
            }
        }

        switch (state) {
            case WHITESPACE:
            case WHITESPACE_AFTER_NAME:
            case WHITESPACE_AFTER_EQUALS:
                boolean newLine = readWhitespace(input);

                if (newLine) {
                    state = State.START;
                } else {
                    switch (state) {
                        case WHITESPACE:
                            state = State.START;
                            break;
                        case WHITESPACE_AFTER_NAME:
                            state = State.EQUALS;
                            break;
                        case WHITESPACE_AFTER_EQUALS:
                            state = State.VALUE;
                            break;
                        default:
                            assert false : "Unhandled state: " + state;
                            break;
                    }
                }
                return info.tokenFactory().createToken(IniTokenId.WHITESPACE);

            case COMMENT:
                readTillEndLine(input);
                state = State.WHITESPACE;
                return info.tokenFactory().createToken(IniTokenId.COMMENT);

            case NAME:
                // could be also SECTION
                if (ch == SECTION_START) {
                    ch = readTillEndLine(input, SECTION_END, COMMENT);

                    // possible comment is processed below (yes, i know, for the second time)
                    if (ch == SECTION_END) {
                        input.read(); // read ']' itself
                        state = State.START;

                        // possible error? any non-comment text on the same line?
                        final int read = input.readLength();
                        ch = readTillEndLine(input, COMMENT);
                        int readNext = input.readLength();
                        int diff = readNext - read;
                        if (diff > 0) {
                            String error = input.readText(0, readNext).toString().substring(read).trim();
                            if (!error.isEmpty()) {
                                state = State.ERROR;
                            }
                            input.backup(diff);
                        }

                        return info.tokenFactory().createToken(IniTokenId.SECTION);
                    }
                }

                // not SECTION but NAME
                input.backup(input.readLength());

                ch = readTillEndLine(input, EQUALS, COMMENT);
                if (ch == COMMENT) {
                    processComment(input);

                } else if (ch == EQUALS) {
                    // any trailing whitespaces?
                    String name = input.readText(0, input.readLength()).toString();
                    String trimmed = name.replaceAll("\\s+$", ""); // NOI18N
                    int backup = name.length() - trimmed.length();
                    if (backup > 0) {
                        state = State.WHITESPACE_AFTER_NAME;
                        input.backup(backup);
                    } else {
                        // no whitespace, just '='
                        state = State.EQUALS;
                    }

                } else {
                    // whole line is NAME
                    state = State.START;
                }
                return info.tokenFactory().createToken(IniTokenId.NAME);

            case EQUALS:
                assert ch == EQUALS : "Unexpected char: " + (char) ch;
                ch = input.read(); // read '=' itself
                assert ch == EQUALS : "Unexpected char: " + (char) ch;

                ch = input.read();
                if (isWhiteSpace(ch)) {
                    state = State.WHITESPACE_AFTER_EQUALS;
                } else if (ch == COMMENT) {
                    state = State.COMMENT;
                } else {
                    state = State.VALUE;
                }
                input.backup(1);
                return info.tokenFactory().createToken(IniTokenId.EQUALS);

            case VALUE:
                ch = readTillEndLine(input, COMMENT);

                if (ch == COMMENT) {
                    processComment(input);
                } else {
                    state = State.WHITESPACE;
                }

                return info.tokenFactory().createToken(IniTokenId.VALUE);

            case ERROR:
                // [...] ANY TEXT HERE IS ERROR
                ch = readTillEndLine(input, COMMENT);
                if (ch == COMMENT) {
                    processComment(input);
                } else {
                    state = State.START;
                }
                return info.tokenFactory().createToken(IniTokenId.ERROR);

            default:
                assert false : "Unknown state: " + state;
                break;
        }
        assert false : "Should not get here";
        return null;
    }

    private void processComment(LexerInput input) {
        // any trailing whitespaces?
        String read = input.readText(0, input.readLength()).toString();
        String trimmed = read.replaceAll("\\s+$", ""); // NOI18N
        int backup = read.length() - trimmed.length();
        if (backup > 0) {
            state = State.WHITESPACE;
            input.backup(backup);
        } else {
            // no whitespace (perhaps not so common)
            state = State.COMMENT;
        }
    }

    private boolean readWhitespace(LexerInput input) {
        boolean newLine = false;
        int ch;
        do {
            ch = input.read();
            if (!newLine && isEndLine(ch)) {
                newLine = true;
            }
        } while (isWhiteSpace(ch) && ch != LexerInput.EOF);
        input.backup(1);
        return newLine;
    }

    private int readTillEndLine(LexerInput input) {
        return readTillEndLine(input, null);
    }

    private int readTillEndLine(LexerInput input, char... stoppers) {
        int ch = -1;
        int previous = -1;
        Stack<Character> longStrings = new Stack<Character>();
        do {
            if (previous == ESCAPE && ch == ESCAPE) {
                // '\\'
                previous = -1;
            } else if (ch != -1) {
                previous = ch;
            }

            ch = input.read();

            if (ch == LexerInput.EOF) {
                // prevent infinite loop
                break;
            }

            if (previous != ESCAPE) {
                handleLongStrings(longStrings, (char) ch);
            }
        } while (!longStrings.isEmpty()
                || (longStrings.isEmpty() && !isEndLine(ch) && !isStopper(stoppers, ch, previous)));
        input.backup(1);
        return ch;
    }

    private void handleLongStrings(Stack<Character> longStrings, char ch) {
        if (ch != LONG_STRING_DOUBLE && ch != LONG_STRING_SINGLE) {
            return;
        }
        if (longStrings.isEmpty()) {
            longStrings.push(ch);
        } else {
            Character peek = longStrings.peek();
            if (peek == ch) {
                longStrings.pop();
            } else {
                longStrings.push(ch);
            }
        }
    }

    private boolean isStopper(char[] stoppers, int ch, int previous) {
        if (stoppers == null || previous == ESCAPE) {
            return false;
        }
        for (char stopper : stoppers) {
            if (stopper == ch) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWhiteSpace(int ch) {
        return ch == SPACE || ch == TAB || isEndLine(ch);
    }

    private static boolean isEndLine(int ch) {
        return ch == LF || ch == CR || ch == LexerInput.EOF;
    }
}
