/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer.lang;

import org.netbeans.api.lexer.PartType;
import org.netbeans.lib.lexer.lang.TestJoinSectionsTextTokenId;
import org.netbeans.api.lexer.Token;
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
                while (true) {
                    switch ((c = input.read())) {
                        case '}':
                            return token(TestJoinSectionsTextTokenId.BRACES);
                        case EOF:
                            state = IN_BRACES;
                            return tokenFactory.createToken(TestJoinSectionsTextTokenId.BRACES,
                                    input.readLength(), PartType.START);
                    }
                }
                // break;

            case EOF: // no more chars on the input
                return null; // the only legal situation when null can be returned

            default: // In regular text
                while (true) {
                    switch ((c = input.read())) {
                        case '{':
                        case EOF:
                            input.backup(1);
                            return token(TestJoinSectionsTextTokenId.TEXT);
                    }
                }
                // break;
        }
    }
    
    private Token<TestJoinSectionsTextTokenId> finishIncompleteBraces() {
        while (true) {
            switch (input.read()) {
                case '}':
                    state = 0;
                    return tokenFactory.createToken(TestJoinSectionsTextTokenId.BRACES,
                            input.readLength(), PartType.END);

                case EOF:
                    input.backup(1);
                    if (input.readLength() == 0)
                        return null;
                    return tokenFactory.createToken(TestJoinSectionsTextTokenId.BRACES,
                            input.readLength(), PartType.MIDDLE);
            }
        }
    }
        
    private Token<TestJoinSectionsTextTokenId> token(TestJoinSectionsTextTokenId id) {
        return tokenFactory.createToken(id);
    }
    
    public void release() {
    }

}
