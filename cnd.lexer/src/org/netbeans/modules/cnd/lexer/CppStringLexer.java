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

import org.netbeans.cnd.api.lexer.CppStringTokenId;
import org.netbeans.api.lexer.Token;
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

    private static final int EOF = LexerInput.EOF;

    private LexerInput input;
    
    private TokenFactory<CppStringTokenId> tokenFactory;
    private boolean escapedLF = false;
    private final boolean dblQuoted;
    
    public CppStringLexer(LexerRestartInfo<CppStringTokenId> info, boolean doubleQuotedString) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        this.dblQuoted = doubleQuotedString;
        assert (info.state() == null); // passed argument always null
    }
    
    public Object state() {
        return null;
    }
    
    public Token<CppStringTokenId> nextToken() {
        while(true) {
            int ch = read();
            switch (ch) {
                case EOF:
                    if (input.readLength() > 0) {
                        return token(CppStringTokenId.TEXT);
                    } else {
                        return null;
                    }
                case '\\': //NOI18N
                    if (input.readLength() > 1) {// already read some text
                        input.backup(1);
                        return tokenFactory.createToken(CppStringTokenId.TEXT, input.readLength());
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
                            return token(CppStringTokenId.SINGLE_QUOTE);
                        case '"': //NOI18N
                            return token(CppStringTokenId.DOUBLE_QUOTE);
                        case '\\': //NOI18N
                            return token(CppStringTokenId.BACKSLASH);
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
//                                    return token(CppStringTokenId.OCTAL_ESCAPE_INVALID);
                                    return token(CppStringTokenId.OCTAL_ESCAPE);
                            }
                            input.backup(1);
//                            return token(CppStringTokenId.OCTAL_ESCAPE_INVALID);
                            return token(CppStringTokenId.OCTAL_ESCAPE);
                    }
                    input.backup(1);
                    return token(CppStringTokenId.ESCAPE_SEQUENCE_INVALID);
            } // end of switch (ch)
        } // end of while(true)
    }

    private Token<CppStringTokenId> token(CppStringTokenId id) {
        escapedLF = false;
        return tokenFactory.createToken(id);
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
