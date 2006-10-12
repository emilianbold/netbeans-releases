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

package org.netbeans.lib.lexer.test.state;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Simple implementation a lexer.
 *
 * @author mmetelka
 */
final class StateLexer implements Lexer<StateTokenId> {
    
    // Copy of LexerInput.EOF
    private static final int EOF = LexerInput.EOF;
    
    static final Object AFTER_A = "after_a";
    static final Object AFTER_B = "after_b";
    
    static final Integer AFTER_A_INT = 1;
    static final Integer AFTER_B_INT = 2;

    private boolean useIntStates;
    
    private Object state;
    
    private LexerInput input;

    private TokenFactory<StateTokenId> tokenFactory;
    
    private LanguagePath languagePath;
    
    private InputAttributes inputAttributes;
    
    StateLexer(LexerInput input, TokenFactory<StateTokenId> tokenFactory, Object state,
    LanguagePath languagePath, InputAttributes inputAttributes) {
        this.input = input;
        this.tokenFactory = tokenFactory;
        this.state = state;
        this.languagePath = languagePath;
        this.inputAttributes = inputAttributes;

        this.useIntStates = (inputAttributes != null)
                ? Boolean.TRUE.equals((Boolean)inputAttributes.getValue(languagePath, "states"))
                : false;
        
        Object expectedRestartState = (inputAttributes != null)
                ? inputAttributes.getValue(languagePath, "restartState")
                : null;
        if (expectedRestartState != null && !expectedRestartState.equals(state)) {
            throw new IllegalStateException("Expected restart state " + expectedRestartState + ", but real is " + state);
        }
    }

    public Object state() {
        return state;
    }

    public Token<StateTokenId> nextToken() {
        boolean returnNullToken = (inputAttributes != null && Boolean.TRUE.equals(inputAttributes.getValue(languagePath, "returnNullToken")));
        while (true) {
            int c = input.read();
            if (returnNullToken) // Test early return of null token
                return null;
            switch (c) {
                case 'a':
                    state = useIntStates ? AFTER_A_INT : AFTER_A;
                    return token(StateTokenId.A);

                case 'b':
                    while (input.read() == 'b') {}
                    input.backup(1);
                    state = useIntStates ? AFTER_B_INT : AFTER_B;
                    return token(StateTokenId.BMULTI);

                case EOF: // no more chars on the input
                    return null; // the only legal situation when null can be returned

                default:
                    // Invalid char
                    state = null;
                    return token(StateTokenId.ERROR);
            }
        }
    }
        
    private Token<StateTokenId> token(StateTokenId id) {
        return tokenFactory.createToken(id);
    }
    
}
