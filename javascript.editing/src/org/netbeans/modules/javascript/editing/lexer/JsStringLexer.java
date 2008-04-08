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
package org.netbeans.modules.javascript.editing.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;


/**
 * Lexical analyzer for JavaScript quoted Strings
 *
 * @author Tor Norbye
 * @version 1.00
 */
public final class JsStringLexer implements Lexer<JsStringTokenId> {
    private static final int EOF = LexerInput.EOF;
    private final LexerInput input;
    private final TokenFactory<JsStringTokenId> tokenFactory;

    /**
     * A Lexer for ruby strings
     * @param substituting If true, handle substitution rules for double quoted strings, otherwise
     *    single quoted strings.
     */
    public JsStringLexer(LexerRestartInfo<JsStringTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // passed argument always null
    }

    public Object state() {
        return null;
    }

    public Token<JsStringTokenId> nextToken() {
        while (true) {
            int ch = input.read();

            switch (ch) {
            case EOF:

                if (input.readLength() > 0) {
                    return token(JsStringTokenId.STRING_TEXT);
                } else {
                    return null;
                }

            case '\\':

                if (input.readLength() > 1) { // already read some text
                    input.backup(1);

                    return tokenFactory.createToken(JsStringTokenId.STRING_TEXT,
                        input.readLength());
                }

                switch (ch = input.read()) {
                // In general, \x = x
                // Thus, just special case out the exceptions

                // Unicode escape: \\uXXXX
                case 'u':
                    if (Character.isDigit(input.read())) {
                        if (Character.isDigit(input.read())) {
                            if (Character.isDigit(input.read())) {
                                if (Character.isDigit(input.read())) {
                                    return token(JsStringTokenId.STRING_ESCAPE); // valid unicode
                                } else {
                                    input.backup(4);
                                }
                            } else {
                                input.backup(3);
                            }
                        } else {
                            input.backup(2);
                        }
                    } else {
                        input.backup(1);
                    }
                    break;
                    
                // Hex escape: \xnn = Hex nn
                case 'x':
                    if (isHexDigit(input.read())) {
                        if (isHexDigit(input.read())) {
                            return token(JsStringTokenId.STRING_ESCAPE); // valid unicode
                        } else {
                            input.backup(2);
                        }
                    } else {
                        input.backup(1);
                    }
                    break;

                // Octal escape: \nnn = Octal nnn
                //case '0':  \0 means the NUL character, you can only specify octal 1-377
                case '1':
                case '2':
                case '3':

                    switch (input.read()) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':

                        switch (input.read()) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                            return token(JsStringTokenId.STRING_ESCAPE); // valid octal escape
                        }

                        input.backup(1);

                        continue;
                    }

                    input.backup(1);

                    continue; // Just a \0 etc -> 0


                case '0':
                case 'b':
                case 't':
                case 'n':
                case 'v':
                case 'f':
                case '\"':
                case '\'':
                case '\\':
                    return token(JsStringTokenId.STRING_ESCAPE);

                default:
                    return token(JsStringTokenId.STRING_INVALID);
                }
            }
        }
    }

    private static boolean isHexDigit(int c) {
        return Character.isDigit(c) || ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'));
    }

    private Token<JsStringTokenId> token(JsStringTokenId id) {
        return tokenFactory.createToken(id);
    }

    public void release() {
    }
}
