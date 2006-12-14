/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.lexer.nbbridge.test.simple;

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
final class SimpleCharLexer implements Lexer<SimpleCharTokenId> {
    
    private LexerInput input;
    
    private TokenFactory<SimpleCharTokenId> tokenFactory;
    
    SimpleCharLexer(LexerRestartInfo<SimpleCharTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
    }
    
    public Token<SimpleCharTokenId> nextToken() {
        int ch = input.read();
        if (ch == LexerInput.EOF) {
            return null;
        } else if (Character.isDigit(ch)) {
            return tokenFactory.createToken(SimpleCharTokenId.DIGIT);
        } else {
            return tokenFactory.createToken(SimpleCharTokenId.CHARACTER);
        }
    }
    
    public Object state() {
        return null; // always in default state after token recognition
    }
    
    public void release() {
    }

}
