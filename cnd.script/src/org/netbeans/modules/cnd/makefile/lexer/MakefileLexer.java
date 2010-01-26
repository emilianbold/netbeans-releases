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
 * @author Jan Jancura
 * @author Alexey Vladykin
 */
/*package*/ final class MakefileLexer implements Lexer<MakefileTokenId> {

    private static final Set<String> SPECIAL_TARGETS = new HashSet<String>(Arrays.asList(
            ".DEFAULT", // NOI18N
            ".DONE", // NOI18N
            ".FAILED", // NOI18N
            ".GET_POSIX", // NOI18N
            ".IGNORE", // NOI18N
            ".INIT", // NOI18N
            ".KEEP_STATE", // NOI18N
            ".KEEP_STATE_FILE", // NOI18N
            ".MAKE_VERSION", // NOI18N
            ".NO_PARALLEL", // NOI18N
            ".PARALLEL", // NOI18N
            ".PHONY", // NOI18N
            ".POSIX", // NOI18N
            ".PRECIOUS", // NOI18N
            ".SCCS_GET", // NOI18N
            ".SCCS_GET_POSIX", // NOI18N
            ".SILENT", // NOI18N
            ".SUFFIXES", // NOI18N
            ".WAIT")); // NOI18N

    private static final Set<String> KEYWORDS = new HashSet<String>(Arrays.asList(
            "include", // NOI18N
            "ifdef", // NOI18N
            "endif")); // NOI18N

    private static enum State {
        NORMAL,
        AT_LINE_START,
        AFTER_TAB
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

            case NORMAL:
                token = readToken(false);
                if (token != null) {
                    switch (token.id()) {
                        case NEW_LINE:
                            state = State.AT_LINE_START;
                            break;
                    }
                }
                break;

            case AT_LINE_START:
                token = readToken(true);
                if (token != null) {
                    switch (token.id()) {
                        case NEW_LINE:
                            // remain in AT_LINE_START state
                            break;
                        case TAB:
                            state = State.AFTER_TAB;
                            break;
                        default:
                            state = State.NORMAL;
                    }
                }
                break;

            case AFTER_TAB:
                token = readShell();
                if (token == null) {
                    token = readToken(false);
                    if (token != null) {
                        switch (token.id()) {
                            case NEW_LINE:
                                state = State.AT_LINE_START;
                                break;
                            default:
                                state = State.NORMAL;
                        }
                    }
                }
                break;

            default:
                throw new IllegalStateException("Internal lexer error"); // NOI18N
        }
        return token;
    }

    @Override
    public Object state() {
        return state == State.AT_LINE_START? null : state.ordinal();
    }

    @Override
    public void release() {
    }


    private Token<MakefileTokenId> readToken(boolean atLineStart) {
        LexerInput input = info.input();
        TokenFactory<MakefileTokenId> factory = info.tokenFactory();

        switch (input.read()) {

            case '\t': // tab has special meaning only at line start
                if (atLineStart) {
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
                if (input.read() != '=') {
                    input.backup(1);
                }
                return factory.createToken(MakefileTokenId.SEPARATOR);

            case '=':
                return factory.createToken(MakefileTokenId.SEPARATOR);

            case LexerInput.EOF:
                return null;

            default:
                input.backup(1);
                return readOther(input, factory);
        }
    }

    private Token<MakefileTokenId> readShell() {
        LexerInput input = info.input();
        if (consumeAnythingTillEndOfLine(input, true)) {
            return info.tokenFactory().createToken(MakefileTokenId.SHELL);
        } else {
            return null;
        }
    }

    private static void consumeWhitespace(LexerInput input) {
        for (;;) {
            switch (input.read()) {
                case '\t':
                case ' ':
                    // proceed
                    break;
                default:
                    input.backup(1);
                    return;
            }
        }
    }

    /**
     * @param input
     * @param consumeEscapedNewlines
     * @return <code>true</code> if consumed at least one character
     */
    private static boolean consumeAnythingTillEndOfLine(LexerInput input, boolean consumeEscapedNewlines) {
        int consumed = 0;
        for (;;) {
            switch (input.read()) {
                case LexerInput.EOF:
                case '\r':
                case '\n':
                    input.backup(1);
                    return 0 < consumed;

                case '\\':
                    ++consumed;
                    if (consumeEscapedNewlines) {
                        consumed += consumeNewline(input);
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
                    if (consumeNewline(input) == 0) {
                        input.read(); // skip any character after backslash
                    }
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

    private static Token<MakefileTokenId> readOther(LexerInput input, TokenFactory<MakefileTokenId> factory) {
        if (input.read() == '\\' && 0 < consumeNewline(input)) {
            return factory.createToken(MakefileTokenId.ESCAPED_NEW_LINE);
        } else {

            input.backup(1);
            for (;;) {
                int c = input.read();
                switch (c) {
                    case '\\':
                        int consumed = consumeNewline(input);
                        if (0 < consumed) {
                            input.backup(1 + consumed);
                            return createToken(input.readText().toString(), factory);
                        } else {
                            input.read();
                        }
                        break;

                    case LexerInput.EOF:
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                    case ':':
                    case '=':
                    case '$':
                    case '#':
                        input.backup(1);
                        return createToken(input.readText().toString(), factory);
                }
            }
        }
    }

    private static Token<MakefileTokenId> createToken(String text, TokenFactory<MakefileTokenId> factory) {
        if (SPECIAL_TARGETS.contains(text)) {
            return factory.createToken(MakefileTokenId.SPECIAL_TARGET);
        } else if (KEYWORDS.contains(text)) {
            return factory.createToken(MakefileTokenId.KEYWORD);
        } else {
            return factory.createToken(MakefileTokenId.BARE);
        }
    }
}
