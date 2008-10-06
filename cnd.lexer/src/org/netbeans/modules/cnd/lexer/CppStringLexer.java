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

import org.netbeans.api.lexer.PartType;
import org.netbeans.cnd.api.lexer.CppStringTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for C/C++ string language.
 * based on JavaStringLexer
 *
 * @author Vladimir Voskeresensky
 * @version 1.00
 */

public class CppStringLexer implements Lexer<CppStringTokenId> {
    private static final int INIT              = 0;
    private static final int OTHER              = 1;

    private static final int EOF = LexerInput.EOF;

    private LexerInput input;

    private TokenFactory<CppStringTokenId> tokenFactory;
    private boolean escapedLF = false;
    private final boolean dblQuoted;
    private int state = INIT;

    public CppStringLexer(LexerRestartInfo<CppStringTokenId> info, boolean doubleQuotedString) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        this.dblQuoted = doubleQuotedString;
        Integer stateObj = (Integer) info.state();
        fromState(stateObj); // last line in contstructor
    }

    public Object state() {
        return Integer.valueOf(state);
    }

    private void fromState(Integer state) {
        this.state = state == null ? INIT : state.intValue();
    }

    public Token<CppStringTokenId> nextToken() {
        int startState = state;
        state = OTHER;
        while(true) {
            int ch = read();
            switch (ch) {
                case 'L':
                    if (startState == INIT) {
                        return token(CppStringTokenId.PREFIX);
                    }
                    break;
                case EOF:
                    if (input.readLength() > 0) {
                        return token(CppStringTokenId.TEXT);
                    } else {
                        return null;
                    }
                case '\'':
                    if (input.readLength() > 1) {// already read some text
                        input.backup(1);
                        return token(CppStringTokenId.TEXT);
                    }
                    return token(CppStringTokenId.SINGLE_QUOTE);
                case '"':
                    if (input.readLength() > 1) {// already read some text
                        input.backup(1);
                        return token(CppStringTokenId.TEXT);
                    }
                    return token(CppStringTokenId.DOUBLE_QUOTE);
                case '\\': //NOI18N
                    if (input.readLength() > 1) {// already read some text
                        input.backup(1);
                        return token(CppStringTokenId.TEXT);
                    }
                    switch (ch = read()) {
                        case 'b': //NOI18N
                            return token(CppStringTokenId.BACKSPACE);
                        case 'e': //NOI18N
                            return token(CppStringTokenId.ANSI_COLOR);
                        case 'f': //NOI18N
                            return token(CppStringTokenId.FORM_FEED);
                        case 'n': //NOI18N
                            return token(CppStringTokenId.NEWLINE);
                        case 'r': //NOI18N
                            return token(CppStringTokenId.CR);
                        case 't': //NOI18N
                            return token(CppStringTokenId.TAB);
                        case '\'': //NOI18N
                            return token(CppStringTokenId.SINGLE_QUOTE_ESCAPE);
                        case '"': //NOI18N
                            return token(CppStringTokenId.DOUBLE_QUOTE_ESCAPE);
                        case '\\': //NOI18N
                            return token(CppStringTokenId.BACKSLASH_ESCAPE);
                       case 'u': //NOI18N
                            while ('u' == (ch = read())) {}; //NOI18N

                            for(int i = 0; ; i++) {
                                ch = Character.toLowerCase(ch);

                                if ((ch < '0' || ch > '9') && (ch < 'a' || ch > 'f')) { //NOI18N
                                    input.backup(1);
                                    return token(CppStringTokenId.UNICODE_ESCAPE_INVALID);
                                }

                                if (i == 3) { // four digits checked, valid sequence
                                    return token(CppStringTokenId.UNICODE_ESCAPE);
                                }

                                ch = read();
                            }
                        case 'x': // NOI18N
                        {
                            int len = 0;
                            while (true) {
                                switch (read()) {
                                    case '0':
                                    case '1':
                                    case '2':
                                    case '3':
                                    case '4':
                                    case '5':
                                    case '6':
                                    case '7':
                                    case '8':
                                    case '9':
                                    case 'a':
                                    case 'b':
                                    case 'c':
                                    case 'd':
                                    case 'e':
                                    case 'f':
                                    case 'A':
                                    case 'B':
                                    case 'C':
                                    case 'D':
                                    case 'E':
                                    case 'F':
                                        len++;
                                        break;
                                    default:
                                        input.backup(1);
                                        // if float then before mandatory binary exponent => invalid
                                        return token(len > 0 ? CppStringTokenId.HEX_ESCAPE : CppStringTokenId.HEX_ESCAPE_INVALID);
                                }
                            } // end of while(true)      
                        }
                        case '0': case '1': case '2': case '3': //NOI18N
                            switch (read()) {
                                case '0': case '1': case '2': case '3': //NOI18N
                                case '4': case '5': case '6': case '7': //NOI18N
                                    switch (read()) {
                                        case '0': case '1': case '2': case '3': //NOI18N
                                        case '4': case '5': case '6': case '7': //NOI18N
                                            return token(CppStringTokenId.OCTAL_ESCAPE);
                                    }
                                    input.backup(1);
                                    return token(CppStringTokenId.OCTAL_ESCAPE);
                            }
                            input.backup(1);
                            return token(CppStringTokenId.OCTAL_ESCAPE);
                    }
                    input.backup(1);
                    return token(CppStringTokenId.ESCAPE_SEQUENCE_INVALID);
            } // end of switch (ch)
        } // end of while(true)
    }

    protected final Token<CppStringTokenId> token(CppStringTokenId id) {
        return token(id, id.fixedText(), PartType.COMPLETE);
    }

    private Token<CppStringTokenId> token(CppStringTokenId id, String fixedText, PartType part) {
        assert id != null : "id must be not null";
        Token<CppStringTokenId> token = null;
        if (fixedText != null && !escapedLF) {
            // create flyweight token
            token = tokenFactory.getFlyweightToken(id, fixedText);
        } else {
            if (part != PartType.COMPLETE) {
                token = tokenFactory.createToken(id, input.readLength(), part);
            } else {
                token = tokenFactory.createToken(id);
            }
        }
        escapedLF = false;
        assert token != null : "token must be created as result for " + id;
        return token;
    }

    @SuppressWarnings("fallthrough")
    protected final int read() {
        boolean skipEscapedLF = true;
        int c = input.read();
        if (skipEscapedLF) { // skip escaped LF
            int next;
            while (c == '\\') {
                switch (next = input.read()) {
                    case '\r':
                        input.consumeNewline();
                        // nobreak
                    case '\n':
                        escapedLF = true;
                        next = input.read();
                        break;
                    default:
                        input.backup(1);
                        assert c == '\\' : "must be backslash " + (char)c;
                        return c; // normal backslash, not escaped LF
                }
                c = next;
            }
        }
        return c;
    }

    public void release() {
    }

}
