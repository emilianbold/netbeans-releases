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
