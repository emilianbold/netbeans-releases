/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.makefile.lexer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Makefile lexer. Works with
 * <a href="http://www.opengroup.org/onlinepubs/009695399/utilities/make.html">standard makefiles</a>
 * and some extensions.
 *
 * @author Jan Jancura
 * @author Alexey Vladykin
 */
/*package*/ final class MakefileLexer implements Lexer<MakefileTokenId> {

    private static final Set<String> SPECIAL_TARGETS = new HashSet<String>(Arrays.asList(
            // standard
            ".DEFAULT", // NOI18N
            ".IGNORE", // NOI18N
            ".POSIX", // NOI18N
            ".PRECIOUS", // NOI18N
            ".SCCS_GET", // NOI18N
            ".SILENT", // NOI18N
            ".SUFFIXES", // NOI18N

            // gmake extensions
            // http://www.gnu.org/software/automake/manual/make/Special-Targets.html
            ".PHONY", // NOI18N
            ".INTERMEDIATE", // NOI18N
            ".SECONDARY", // NOI18N
            ".SECONDEXPANSION", // NOI18N
            ".DELETE_ON_ERROR", // NOI18N
            ".LOW_RESOLUTION_TIME", // NOI18N
            ".EXPORT_ALL_VARIABLES", // NOI18N
            ".NOTPARALLEL", // NOI18N

            // dmake extensions
            // http://docs.sun.com/source/820-4180/man1/dmake.1.html
            ".KEEP_STATE", // NOI18N
            ".KEEP_STATE_FILE", // NOI18N
            ".NO_PARALLEL", // NOI18N
            ".PARALLEL", // NOI18N
            ".LOCAL", // NOI18N
            ".WAIT", // NOI18N

            // other extensions
            ".DONE", // NOI18N
            ".FAILED", // NOI18N
            ".GET_POSIX", // NOI18N
            ".INIT", // NOI18N
            ".MAKE_VERSION", // NOI18N
            ".SCCS_GET_POSIX")); // NOI18N

    /**
     * All keywords are extensions of the standard.
     */
    private static final Set<String> KEYWORDS = new HashSet<String>(Arrays.asList(
            // gmake
            "override", // NOI18N
            "define", // NOI18N
            "endef", // NOI18N
            "include", // NOI18N
            "ifdef", // NOI18N
            "ifndef", // NOI18N
            "ifeq", // NOI18N
            "ifneq", // NOI18N
            "else", // NOI18N
            "endif")); // NOI18N

    private static enum State {
        AT_LINE_START,
        AFTER_LINE_START,
        AFTER_TAB_OR_SEMICOLON,
        IN_SHELL,
        AFTER_EQUALS,
        AFTER_COLON
    }

    private final LexerRestartInfo<MakefileTokenId> info;
    private State state;

    /*package*/ MakefileLexer(LexerRestartInfo<MakefileTokenId> info) {
        this.info = info;
        state = info.state() == null ? State.AT_LINE_START : State.values()[(Integer) info.state()];
    }

    @Override
    public Token<MakefileTokenId> nextToken() {
        Token<MakefileTokenId> token;
        switch (state) {

            case AT_LINE_START:
                token = readToken(true, true, true, false);
                if (token != null) {
                    switch (token.id()) {
                        case NEW_LINE:
                            // remain in AT_LINE_START state
                            break;
                        case TAB:
                            state = State.AFTER_TAB_OR_SEMICOLON;
                            break;
                        case EQUALS:
                        case COLON_EQUALS:
                        case PLUS_EQUALS:
                            state = State.AFTER_EQUALS;
                            break;
                        case COLON:
                            state = State.AFTER_COLON;
                            break;
                        case SEMICOLON:
                            throw new IllegalStateException("Internal error"); // NOI18N
                        default:
                            state = State.AFTER_LINE_START;
                    }
                }
                break;

            case AFTER_LINE_START:
                token = readToken(false, true, true, false);
                if (token != null) {
                    switch (token.id()) {
                        case NEW_LINE:
                            state = State.AT_LINE_START;
                            break;
                        case EQUALS:
                        case COLON_EQUALS:
                        case PLUS_EQUALS:
                            state = State.AFTER_EQUALS;
                            break;
                        case COLON:
                            state = State.AFTER_COLON;
                            break;
                        case TAB:
                        case SEMICOLON:
                            throw new IllegalStateException("Internal error"); // NOI18N
                        default:
                            // remain in AFTER_LINE_START state
                    }
                }
                break;

            case AFTER_TAB_OR_SEMICOLON:
                token = readTokenInShell(true);
                if (token != null) {
                    switch (token.id()) {
                        case NEW_LINE:
                            state = State.AT_LINE_START;
                            break;
                        case SHELL:
                        case MACRO:
                            state = State.IN_SHELL;
                            break;
                        case WHITESPACE:
                            // remain in AFTER_TAB_OR_SEMICOLON state
                            break;
                        default:
                            throw new IllegalStateException("Internal error"); // NOI18N
                    }
                }
                break;

            case IN_SHELL:
                token = readTokenInShell(false);
                if (token != null) {
                    switch (token.id()) {
                        case NEW_LINE:
                            state = State.AT_LINE_START;
                            break;
                        case SHELL:
                        case MACRO:
                            // remain in IN_SHELL state
                            break;
                        default:
                            throw new IllegalStateException("Internal error"); // NOI18N
                    }
                }
                break;

            case AFTER_EQUALS:
                token = readToken(false, false, false, false);
                if (token != null) {
                    switch (token.id()) {
                        case NEW_LINE:
                            state = State.AT_LINE_START;
                            break;
                        case TAB:
                        case COLON:
                        case EQUALS:
                        case COLON_EQUALS:
                        case PLUS_EQUALS:
                        case SEMICOLON:
                            throw new IllegalStateException("Internal error"); // NOI18N
                        default:
                            // remain in AFTER_EQUALS state
                    }
                }
                break;

            case AFTER_COLON:
                token = readToken(false, false, false, true);
                if (token != null) {
                    switch (token.id()) {
                        case NEW_LINE:
                            state = State.AT_LINE_START;
                            break;
                        case SEMICOLON:
                            state = State.AFTER_TAB_OR_SEMICOLON;
                            break;
                        case TAB:
                        case COLON:
                        case EQUALS:
                        case COLON_EQUALS:
                        case PLUS_EQUALS:
                            throw new IllegalStateException("Internal error"); // NOI18N
                        default:
                            // remain in AFTER_EQUALS state
                    }
                }
                break;

            default:
                throw new IllegalStateException("Internal error"); // NOI18N
        }
        return token;
    }

    @Override
    public Object state() {
        return state == State.AT_LINE_START ? null : state.ordinal();
    }

    @Override
    public void release() {
    }


    private Token<MakefileTokenId> readToken(boolean wantTab, boolean wantColon, boolean wantEquals, boolean wantSemicolon) {
        LexerInput input = info.input();
        TokenFactory<MakefileTokenId> factory = info.tokenFactory();

        switch (input.read()) {

            case LexerInput.EOF:
                return null;

            case '\t':
                if (wantTab) {
                    return factory.createToken(MakefileTokenId.TAB);
                } else {
                    consumeWhitespace(input);
                    return factory.createToken(MakefileTokenId.WHITESPACE);
                }

            case ' ':
                consumeWhitespace(input);
                return factory.createToken(MakefileTokenId.WHITESPACE);

            case '#':
                consumeAnythingTillEndOfLine(input, false);
                return factory.createToken(MakefileTokenId.COMMENT);

            case '\r':
                input.consumeNewline();
                return factory.createToken(MakefileTokenId.NEW_LINE);

            case '\n':
                return factory.createToken(MakefileTokenId.NEW_LINE);

            case '$':
                return readMacro(input, factory);

            case ':':
                if (input.read() == '=' && wantEquals) {
                    return factory.createToken(MakefileTokenId.COLON_EQUALS);
                } else if (wantColon) {
                    input.backup(1);
                    return factory.createToken(MakefileTokenId.COLON);
                } else {
                    consumeBare(input, wantColon, wantEquals, wantSemicolon);
                    return factory.createToken(MakefileTokenId.BARE);
                }

            case '+':
                if (input.read() == '=' && wantEquals) {
                    return factory.createToken(MakefileTokenId.PLUS_EQUALS);
                } else {
                    input.backup(1);
                    consumeBare(input, wantColon, wantEquals, wantSemicolon);
                    return factory.createToken(MakefileTokenId.BARE);
                }

            case '=':
                if (wantEquals) {
                    return factory.createToken(MakefileTokenId.EQUALS);
                } else {
                    consumeBare(input, wantColon, wantEquals, wantSemicolon);
                    return factory.createToken(MakefileTokenId.BARE);
                }

            case ';':
                if (wantSemicolon) {
                    return factory.createToken(MakefileTokenId.SEMICOLON);
                } else {
                    consumeBare(input, wantColon, wantEquals, wantSemicolon);
                    return factory.createToken(MakefileTokenId.BARE);
                }

            case '\\':
                if (0 < consumeNewline(input)) {
                    return factory.createToken(MakefileTokenId.ESCAPED_NEW_LINE);
                } else {
                    consumeEscape(input);
                    consumeBare(input, wantColon, wantEquals, wantSemicolon);
                    return factory.createToken(MakefileTokenId.BARE);
                }

            default:
                consumeBare(input, wantColon, wantEquals, wantSemicolon);
                return createBareOrKeyword(input.readText().toString(), factory);
        }
    }

    private void consumeBare(LexerInput input, boolean wantColon, boolean wantEquals, boolean wantSemicolon) {
        for (;;) {
            switch (input.read()) {
                case ' ':
                case '\t':
                    input.backup(1);
                    return;

                case ':':
                    if (wantColon) {
                        input.backup(1);
                        return;
                    } else if (wantEquals && input.read() == '=') {
                        input.backup(2);
                        return;
                    }
                    break;

                case '+':
                    if (wantEquals && input.read() == '=') {
                        input.backup(2);
                        return;
                    }
                    break;

                case '=':
                    if (wantEquals) {
                        input.backup(1);
                        return;
                    }
                    break;

                case ';':
                    if (wantSemicolon) {
                        input.backup(1);
                        return;
                    }
                    break;

                case '\\':
                    int consumed = consumeNewline(input);
                    if (0 < consumed) {
                        input.backup(1 + consumed);
                        return;
                    } else {
                        consumeEscape(input);
                    }
                    break;

                case LexerInput.EOF:
                case '\r':
                case '\n':
                case '#':
                case '$':
                    input.backup(1);
                    return;
            }
        }
    }

    private Token<MakefileTokenId> readTokenInShell(boolean wantWhitespace) {
        LexerInput input = info.input();
        TokenFactory<MakefileTokenId> factory = info.tokenFactory();

        switch (input.read()) {
            case '$':
                return readMacro(input, factory);
            case ' ':
            case '\t':
                if (wantWhitespace) {
                    consumeWhitespace(input);
                    return factory.createToken(MakefileTokenId.WHITESPACE);
                }
                break;
        }

        input.backup(1);

        if (consumeAnythingTillEndOfLine(input, true)) {
            return factory.createToken(MakefileTokenId.SHELL);
        } else if (0 < consumeNewline(input)) {
            return factory.createToken(MakefileTokenId.NEW_LINE);
        } else {
            return null;
        }
    }

    private static void consumeEscape(LexerInput input) {
        switch (input.read()) {
            case ':':
                switch (input.read()) {
                    case '=':
                        input.backup(2);
                        break;
                    default:
                        input.backup(1);
                }
                break;

            case ' ':
            case '\t':
            case '#':
                return;

            default:
                input.backup(1);
        }
    }

    private static boolean consumeWhitespace(LexerInput input) {
        int consumed = 0;
        for (;;) {
            switch (input.read()) {
                case '\t':
                case ' ':
                    ++consumed;
                    break;
                default:
                    input.backup(1);
                    return 0 < consumed;
            }
        }
    }

    /**
     * @param input
     * @return <code>true</code> if consumed at least one character
     */
    private static boolean consumeAnythingTillEndOfLine(LexerInput input, boolean wantMacro) {
        int consumed = 0;
        for (;;) {
            switch (input.read()) {
                case LexerInput.EOF:
                case '\r':
                case '\n':
                    input.backup(1);
                    return 0 < consumed;

                case '\\':
                    consumed += 1 + consumeNewline(input);
                    break;

                case '$':
                    if (wantMacro) {
                        input.backup(1);
                        return 0 < consumed;
                    }
                    break;

                default:
                    ++consumed;
            }
        }
    }

    private static int consumeNewline(LexerInput input) {
        if (input.consumeNewline()) {
            return 1;
        } else if (input.read() == '\r' && input.consumeNewline()) {
            return 2;
        } else {
            input.backup(1);
            return 0;
        }
    }

    private static Token<MakefileTokenId> readMacro(LexerInput input, TokenFactory<MakefileTokenId> factory) {
        if (consumeMacro(input)) {
            return factory.createToken(MakefileTokenId.MACRO);
        } else {
            return factory.createToken(MakefileTokenId.MACRO, input.readLength(), PartType.START);
        }
    }

    /**
     * @param input
     * @return <code>true</code> if consumed complete macro
     */
    private static boolean consumeMacro(LexerInput input) {
        switch (input.read()) {
            case '(':
                return consumeMacroBody(input, ')');

            case '{':
                return consumeMacroBody(input, '}');

            case LexerInput.EOF:
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                input.backup(1);
                return false;

            default: // any other single character is OK
                return true;
        }
    }

    private static boolean consumeMacroBody(LexerInput input, char barrier) {
        for (;;) {
            int c = input.read();
            switch (c) {
                case '$':
                    if (!consumeMacro(input)) {
                        return false;
                    }
                    break;

                case '\\':
                    consumeNewline(input);
                    break;

                case LexerInput.EOF:
                case '\r':
                case '\n':
                    input.backup(1);
                    return false;

                default:
                    if (c == barrier) {
                        return true;
                    }
            }
        }
    }

    private static Token<MakefileTokenId> createBareOrKeyword(String text, TokenFactory<MakefileTokenId> factory) {
        if (SPECIAL_TARGETS.contains(text)) {
            return factory.createToken(MakefileTokenId.SPECIAL_TARGET);
        } else if (KEYWORDS.contains(text)) {
            return factory.createToken(MakefileTokenId.KEYWORD);
        } else {
            return factory.createToken(MakefileTokenId.BARE);
        }
    }
}
