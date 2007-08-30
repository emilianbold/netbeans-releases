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

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author mmetelka
 */
final class TestJoinSectionsTopLexer implements Lexer<TestJoinSectionsTopTokenId> {

    // Copy of LexerInput.EOF
    private static final int EOF = LexerInput.EOF;

    private final LexerInput input;
    
    private final TokenFactory<TestJoinSectionsTopTokenId> tokenFactory;
    
    TestJoinSectionsTopLexer(LexerRestartInfo<TestJoinSectionsTopTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // never set to non-null value in state()
    }

    public Object state() {
        return null; // always in default state after token recognition
    }

    public Token<TestJoinSectionsTopTokenId> nextToken() {
        int c = input.read();
        switch (c) {
            case '<':
                if (input.readLength() > 1) {
                    input.backup(1);
                    return token(TestJoinSectionsTopTokenId.TEXT);
                }
                while (true) {
                    switch ((c = input.read())) {
                        case '>':
                        case EOF:
                            return token(TestJoinSectionsTopTokenId.TAG);
                    }
                }
                // break;

            case EOF: // no more chars on the input
                return null; // the only legal situation when null can be returned

            default:
                while (true) {
                    switch ((c = input.read())) {
                        case '<':
                        case EOF:
                            input.backup(1);
                            return token(TestJoinSectionsTopTokenId.TEXT);
                    }
                }
                // break;
        }
    }
        
    private Token<TestJoinSectionsTopTokenId> token(TestJoinSectionsTopTokenId id) {
        return tokenFactory.createToken(id);
    }
    
    public void release() {
    }

}
