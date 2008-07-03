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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.lib.lexer.lang;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author mmetelka
 */
final class TestJoinSectionsTextLexer implements Lexer<TestJoinSectionsTextTokenId> {

    // Copy of LexerInput.EOF
    private static final int EOF = LexerInput.EOF;

    private final LexerInput input;
    
    private final TokenFactory<TestJoinSectionsTextTokenId> tokenFactory;
    
    private Token<TestJoinSectionsTextTokenId> previousSectionLastToken;
    
    private int state;
    
    private static final int IN_BRACES = 1;
    
    private StringBuilder text = new StringBuilder();
    
    TestJoinSectionsTextLexer(LexerRestartInfo<TestJoinSectionsTextTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        this.state = (info.state() != null) ? (Integer)info.state() : 0;
    }

    public Object state() {
        return state;
    }

    public Token<TestJoinSectionsTextTokenId> nextToken() {
        // Check for unfinished incomplete token
        if (state == IN_BRACES) {
            return finishIncompleteBraces();
        }

        int c = input.read();
        switch (c) {
            case '{':
                if (input.readLength() > 1) {
                    input.backup(1);
                    return token(TestJoinSectionsTextTokenId.TEXT);
                }
                text.append((char)c);
                while (true) {
                    switch ((c = input.read())) {
                        case '}':
                            text.append((char)c);
                            return token(TestJoinSectionsTextTokenId.BRACES);
                        case EOF:
                            state = IN_BRACES;
                            input.backup(1); // Backup EOF so that compareAndClearText() works
                            return token(TestJoinSectionsTextTokenId.BRACES, PartType.START);
                    }
                    text.append((char)c);
                }
                // break;

            case EOF: // no more chars on the input
                return null; // the only legal situation when null can be returned

            default: // In regular text
                text.append((char) c);
                while (true) {
                    switch ((c = input.read())) {
                        case '{':
                        case EOF:
                            input.backup(1);
                            return token(TestJoinSectionsTextTokenId.TEXT);
                    }
                    text.append((char) c);
                }
                // break;
        }
    }
    
    private Token<TestJoinSectionsTextTokenId> finishIncompleteBraces() {
        while (true) {
            int c;
            switch ((c = input.read())) {
                case '}':
                    text.append((char) c);
                    state = 0;
                    return token(TestJoinSectionsTextTokenId.BRACES, PartType.END);

                case EOF:
                    input.backup(1);
                    if (input.readLength() == 0)
                        return null;
                    return token(TestJoinSectionsTextTokenId.BRACES, PartType.MIDDLE);
            }
        }
    }
        
    private Token<TestJoinSectionsTextTokenId> token(TestJoinSectionsTextTokenId id) {
        compareAndClearText();
        return tokenFactory.createToken(id);
    }
    
    private Token<TestJoinSectionsTextTokenId> token(TestJoinSectionsTextTokenId id, PartType partType) {
        compareAndClearText();
        return tokenFactory.createToken(id, input.readLength(), partType);
    }

    private void compareAndClearText() {
        String str = input.readText().toString();
        assert (str.length() == text.length()) : dumpText(str);
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != text.charAt(i)) {
                throw new IllegalStateException("Difference at index " + i + ": " + dumpText(str));
            }
        }
        input.backup(str.length());
        for (int i = 0; i < str.length(); i++) {
            int c = input.read();
            if (c != text.charAt(i)) {
                input.backup(1); 
                c = input.read();
                c = input.readText().charAt(i);
                throw new IllegalStateException("Read difference at index " + i + ", c='" + c + "':\n" + dumpText(str));
            }
        }
        text.delete(0, text.length());
    }

    private String dumpText(String str) {
        return "str(" + str.length() + ")=\"" + CharSequenceUtilities.debugText(str) +
                "\"\ntext(" + text.length() + ")=\"" + CharSequenceUtilities.debugText(text) + "\"\n";
    }

    public void release() {
    }

}
