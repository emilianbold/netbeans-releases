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
package org.netbeans.modules.cnd.lexer;

import org.netbeans.cnd.api.lexer.MakefileTokenId;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Jan Jancura
 */
class MakefileLexer implements Lexer<MakefileTokenId> {

    private static Set<String> specialTarget = new HashSet<String>();

    static {
        specialTarget.add("%"); // NOI18N
        specialTarget.add(".DEFAULT");// NOI18N
        specialTarget.add(".DONE");// NOI18N
        specialTarget.add(".FAILED");// NOI18N
        specialTarget.add(".GET_POSIX");// NOI18N
        specialTarget.add(".IGNORE");// NOI18N
        specialTarget.add(".INIT");// NOI18N
        specialTarget.add(".KEEP_STATE");// NOI18N
        specialTarget.add(".KEEP_STATE_FILE");// NOI18N
        specialTarget.add(".MAKE_VERSION");// NOI18N
        specialTarget.add(".NO_PARALLEL");// NOI18N
        specialTarget.add(".PARALLEL");// NOI18N
        specialTarget.add(".PHONY");// NOI18N
        specialTarget.add(".POSIX");// NOI18N
        specialTarget.add(".PRECIOUS");// NOI18N
        specialTarget.add(".SCCS_GET");// NOI18N
        specialTarget.add(".SCCS_GET_POSIX");// NOI18N
        specialTarget.add(".SILENT");// NOI18N
        specialTarget.add(".SUFFIXES");// NOI18N
        specialTarget.add(".WAIT");// NOI18N
    }
    private LexerRestartInfo<MakefileTokenId> info;

    MakefileLexer(LexerRestartInfo<MakefileTokenId> info) {
        this.info = info;
    }

    @SuppressWarnings("fallthrough")
    public Token<MakefileTokenId> nextToken() {
        LexerInput input = info.input();
        int i = input.read();
        switch (i) {
            case LexerInput.EOF:
                return null;
            case ' ':
                do {
                    i = input.read();
                    if (i == '\\') {
                        i = input.read();
                        if (i != 'n') {
                            input.backup(2);
                            return info.tokenFactory().createToken(MakefileTokenId.WHITESPACE);
                        }
                        i = input.read();
                    }
                } while (i == ' ');
                if (i != LexerInput.EOF) {
                    input.backup(1);
                }
                return info.tokenFactory().createToken(MakefileTokenId.WHITESPACE);
            case '\\':
                i = input.read();
                if (i == 'n') {
                    do {
                        i = input.read();
                        if (i == '\\') {
                            i = input.read();
                            if (i != 'n') {
                                input.backup(2);
                                return info.tokenFactory().createToken(MakefileTokenId.WHITESPACE);
                            }
                            i = input.read();
                        }
                    } while (i == ' ');
                    if (i != LexerInput.EOF) {
                        input.backup(1);
                    }
                    return info.tokenFactory().createToken(MakefileTokenId.WHITESPACE);
                } else {
                    //string
                }
                // no break
            case '\n':
                return info.tokenFactory().createToken(MakefileTokenId.NEW_LINE);
            case '\t':
                return info.tokenFactory().createToken(MakefileTokenId.TAB);
            case '#':
                do {
                    i = input.read();
                } while (i != '\n' && i != LexerInput.EOF);
                return info.tokenFactory().createToken(MakefileTokenId.LINE_COMMENT);
            case '$':
                if (readMacro(input)) {
                    return info.tokenFactory().createToken(MakefileTokenId.MACRO);
                }
                return info.tokenFactory().createToken(MakefileTokenId.ERROR);
            case ':':
                i = input.read();
                if (i == 's') {
                    i = input.read();
                    if (i == 'h') {
                        return info.tokenFactory().createToken(MakefileTokenId.MACRO_OPERATOR);
                    }
                    input.backup(2);
                    return info.tokenFactory().createToken(MakefileTokenId.SEPARATOR);
                }
                if (i == '=') {
                    return info.tokenFactory().createToken(MakefileTokenId.MACRO_OPERATOR);
                }
                if (i == ':') {
                    return info.tokenFactory().createToken(MakefileTokenId.SEPARATOR);
                }
                if (i != LexerInput.EOF) {
                    input.backup(1);
                }
                return info.tokenFactory().createToken(MakefileTokenId.SEPARATOR);
            case '=':
                return info.tokenFactory().createToken(MakefileTokenId.MACRO_OPERATOR);
            case '+':
                i = input.read();
                if (i == '=') {
                    return info.tokenFactory().createToken(MakefileTokenId.MACRO_OPERATOR);
                }
                return info.tokenFactory().createToken(MakefileTokenId.RULE_OPERATOR);
            case '-':
            case '@':
            case '?':
            case '!':
                return info.tokenFactory().createToken(MakefileTokenId.RULE_OPERATOR);
            case '(':
            case ')':
            case '[':
            case ']':
            case '{':
            case '}':
            case ';':
            case ',':
            case '<':
            case '>':
            case '&':
                return info.tokenFactory().createToken(MakefileTokenId.SEPARATOR);
            case '"':
                do {
                    i = input.read();
                    if (i == '\\') {
                        i = input.read();
                        i = input.read();
                    }
                } while (i != '"' && i != LexerInput.EOF);
                return info.tokenFactory().createToken(MakefileTokenId.STRING_LITERAL);
            case '\'':
                do {
                    i = input.read();
                    if (i == '\\') {
                        i = input.read();
                        i = input.read();
                    }
                } while (i != '\'' && i != LexerInput.EOF);
                return info.tokenFactory().createToken(MakefileTokenId.STRING_LITERAL);
            case '`':
                do {
                    i = input.read();
                    if (i == '\\') {
                        i = input.read();
                        i = input.read();
                    }
                } while (i != '`' && i != LexerInput.EOF);
                return info.tokenFactory().createToken(MakefileTokenId.STRING_LITERAL);
            default:
                do {
                    i = input.read();
                    if (i == '\\') {
                        i = input.read();
                        if (i == '\n') {
                            input.backup(2);
                            return identifier(input);
                        }
                    }
                } while (i != '\t' &&
                        i != '\n' &&
                        i != '\r' &&
                        i != ' ' &&
                        i != '\\' &&
                        i != '$' &&
                        i != '\'' &&
                        i != '"' &&
                        i != '`' &&
                        i != ':' &&
                        i != '(' &&
                        i != ')' &&
                        i != '[' &&
                        i != ']' &&
                        i != '{' &&
                        i != '}' &&
                        i != ';' &&
                        i != '&' &&
                        i != '=' &&
                        i != LexerInput.EOF);
                if (i != LexerInput.EOF) {
                    input.backup(1);
                }
                return identifier(input);
        }
    }

    private Token<MakefileTokenId> identifier(LexerInput input) {
        String identifier = input.readText().toString();
        if ("include".equals(identifier)) { // NOI18N
            return info.tokenFactory().createToken(MakefileTokenId.KEYWORD);
        }
        if (specialTarget.contains(identifier)) {
            return info.tokenFactory().createToken(MakefileTokenId.SPECIAL_TARGET);
        }
        return info.tokenFactory().createToken(MakefileTokenId.IDENTIFIER);
    }

    private static boolean readMacro(LexerInput input) {
        switch (input.read()) {
            case '(': // NOI18N
                return readTo(input, ')'); // NOI18N
            case '{': // NOI18N
                return readTo(input, '}'); // NOI18N
            case LexerInput.EOF:
            case ' ': // NOI18N
            case '\t': // NOI18N
            case '\r': // NOI18N
            case '\n': // NOI18N
                return false;
            default:
                return true;
        }
    }

    private static boolean readTo(LexerInput input, char barrier) {
        int i = input.read();
        while (i != LexerInput.EOF) {
            switch (i) {
                case '$': // NOI18N
                    if (!readMacro(input)) {
                        return false;
                    }
                    break;
                case '\\': // NOI18N
                    input.read(); // skip any character after backslash
                    break;
                default:
                    if (i == barrier) {
                        return true;
                    }
            }
            i = input.read();
        }
        return false;
    }

    public Object state() {
        return null;
    }

    public void release() {
    }
}


