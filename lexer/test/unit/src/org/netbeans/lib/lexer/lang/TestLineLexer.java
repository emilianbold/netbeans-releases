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
 * Simple implementation a lexer.
 *
 * @author mmetelka
 */
final class TestLineLexer implements Lexer<TestLineTokenId> {

    // Copy of LexerInput.EOF
    private static final int EOF = LexerInput.EOF;
    
    private LexerInput input;
    
    private TokenFactory<TestLineTokenId> tokenFactory;
    
    TestLineLexer(LexerRestartInfo<TestLineTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null);
    }
    
    public Token<TestLineTokenId> nextToken() {
        int ch = input.read();
        while (true) {
            switch (ch) {
                case '\n':
                    return tokenFactory.createToken(TestLineTokenId.LINE);
                case EOF:
                    input.backup(1);
                    return (input.readLength() > 0)
                            ? tokenFactory.createToken(TestLineTokenId.LINE)
                            : null;
                default:
                    ch = input.read();
                    break;
            }
        }
    }
    
    public Object state() {
        return null; // always in default state after token recognition
    }
    
    public void release() {
    }

}
