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

package org.netbeans.lib.lexer.test.dump;

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
final class TextAsSingleTokenLexer implements Lexer<TextAsSingleTokenTokenId> {
    
    // Copy of LexerInput.EOF
    private static final int EOF = LexerInput.EOF;
    
    private LexerInput input;

    private TokenFactory<TextAsSingleTokenTokenId> tokenFactory;
    
    TextAsSingleTokenLexer(LexerRestartInfo<TextAsSingleTokenTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
    }

    public Object state() {
        return null;
    }

    public Token<TextAsSingleTokenTokenId> nextToken() {
        while (true) {
            switch (input.read()) {
                case EOF:
                    return (input.readLength() > 0)
                        ? tokenFactory.createToken(TextAsSingleTokenTokenId.TEXT)
                        : null;
            }
        }
    }
    
    public void release() {
    }

}
