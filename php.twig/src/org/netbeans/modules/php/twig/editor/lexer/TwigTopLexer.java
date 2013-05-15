/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s): Sebastian Hörl
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.twig.editor.lexer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.lexer.Token;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

public class TwigTopLexer implements Lexer<TwigTopTokenId> {

    protected TwigTopLexerState state;
    protected final TokenFactory<TwigTopTokenId> tokenFactory;
    protected final LexerInput input;
    static String OPEN_BLOCK = "{%"; //NOI18N
    static String OPEN_VAR = "{{"; //NOI18N
    public static final String OPEN_COMMENT = "{#"; //NOI18N
    static String CLOSE_BLOCK = "%}"; //NOI18N
    static String CLOSE_VAR = "}}"; //NOI18N
    public static final String CLOSE_COMMENT = "#}"; //NOI18N
    static Pattern START_RAW = Pattern.compile("^\\{%[\\s]raw"); //NOI18N
    static Pattern END_RAW = Pattern.compile("\\{%[\\s]*endraw[\\s]*%\\}$"); //NOI18N

    private TwigTopLexer(LexerRestartInfo<TwigTopTokenId> info) {
        tokenFactory = info.tokenFactory();
        input = info.input();
        state = info.state() == null ? new TwigTopLexerState() : new TwigTopLexerState((TwigTopLexerState) info.state());
    }

    public static synchronized TwigTopLexer create(LexerRestartInfo<TwigTopTokenId> info) {
        return new TwigTopLexer(info);
    }

    @Override
    public Token<TwigTopTokenId> nextToken() {
        TwigTopTokenId tokenId = findNextToken();
        return tokenId == null ? null : tokenFactory.createToken(tokenId);
    }

    @Override
    public Object state() {
        return new TwigTopLexerState(state);
    }

    @Override
    public void release() {
    }

    TwigTopLexerState.Type findTag(CharSequence text, boolean open) {

        if (open && CharSequenceUtilities.endsWith(text, OPEN_BLOCK)) {
            return TwigTopLexerState.Type.BLOCK;
        }
        if (!open && CharSequenceUtilities.endsWith(text, CLOSE_BLOCK)) {
            return TwigTopLexerState.Type.BLOCK;
        }

        if (open && CharSequenceUtilities.endsWith(text, OPEN_VAR)) {
            return TwigTopLexerState.Type.VAR;
        }
        if (!open && CharSequenceUtilities.endsWith(text, CLOSE_VAR)) {
            return TwigTopLexerState.Type.VAR;
        }

        if (open && CharSequenceUtilities.endsWith(text, OPEN_COMMENT)) {
            return TwigTopLexerState.Type.COMMENT;
        }
        if (!open && CharSequenceUtilities.endsWith(text, CLOSE_COMMENT)) {
            return TwigTopLexerState.Type.COMMENT;
        }

        return TwigTopLexerState.Type.NONE;

    }

    public TwigTopTokenId findNextToken() {
        int c = input.read();
        TwigTopLexerState.Type result;

        if (c == LexerInput.EOF) {
            return null;
        }

        while (c != LexerInput.EOF) {

            CharSequence text = input.readText();

            switch (state.main) {
                case RAW:
                    if (CharSequenceUtilities.endsWith(text, "%}")) { //NOI18N
                        Matcher matcher = END_RAW.matcher(text);
                        if (matcher.find()) {
                            String captured = matcher.group();
                            state.main = TwigTopLexerState.Main.OPEN;
                            state.type = TwigTopLexerState.Type.BLOCK;
                            if (text.length() - captured.length() > 0) {
                                input.backup(captured.length());
                                return TwigTopTokenId.T_TWIG_RAW;
                            }
                        }
                    }
                    break;
                case INIT:
                case HTML:
                    result = findTag(text, true);
                    if (result != TwigTopLexerState.Type.NONE) {
                        state.main = TwigTopLexerState.Main.OPEN;
                        state.type = result;
                        if (input.readLength() > 2) {
                            input.backup(2);
                            return TwigTopTokenId.T_HTML;
                        }
                    } else {
                        break;
                    }
                case OPEN:
                    if (input.readLength() == 2) {
                        state.main = TwigTopLexerState.Main.TWIG;
                    }
                    break;
                case TWIG:
                    result = findTag(text, false);
                    if (result != TwigTopLexerState.Type.NONE) {
                        if (result == state.type) {

                            boolean escape = false;
                            boolean doubleQuotes = false;
                            boolean singleQuotes = false;

                            if (result != TwigTopLexerState.Type.COMMENT) {

                                for (int i = 0; i < text.length() - 2; i++) {
                                    char q = text.charAt(i);
                                    if (q == '\\') {
                                        escape = true;
                                    } else if (!escape) {
                                        if (q == '"' && !singleQuotes) {
                                            doubleQuotes = !doubleQuotes;
                                        } else if (q == '\'' && !doubleQuotes) {
                                            singleQuotes = !singleQuotes;
                                        }
                                    } else {
                                        escape = false;
                                    }
                                }

                            }

                            if (singleQuotes || doubleQuotes) {
                                break;
                            }

                            if (result == TwigTopLexerState.Type.BLOCK && START_RAW.matcher(text).find()) {
                                state.main = TwigTopLexerState.Main.CLOSE_RAW;
                            } else {
                                state.main = TwigTopLexerState.Main.CLOSE;
                            }

                            if (input.readLength() > 2) {
                                input.backup(2);
                            }
                        }
                        break;
                    }
                    break;
                case CLOSE_RAW:
                case CLOSE:
                    if ((state.type == TwigTopLexerState.Type.BLOCK && CharSequenceUtilities.endsWith(text, CLOSE_BLOCK))
                            || (state.type == TwigTopLexerState.Type.VAR && CharSequenceUtilities.endsWith(text, CLOSE_VAR))
                            || (state.type == TwigTopLexerState.Type.COMMENT && CharSequenceUtilities.endsWith(text, CLOSE_COMMENT))) {
                        state.main = (state.main == TwigTopLexerState.Main.CLOSE) ? TwigTopLexerState.Main.HTML : TwigTopLexerState.Main.RAW;
                        return TwigTopTokenId.T_TWIG;
                    }
                    break;

            }

            c = input.read();

        }

        switch (state.main) {
            case RAW:
                return TwigTopTokenId.T_TWIG_RAW;
            case TWIG:
                return TwigTopTokenId.T_TWIG;
            case HTML:
                return TwigTopTokenId.T_HTML;
        }

        return TwigTopTokenId.T_HTML;

    }
}