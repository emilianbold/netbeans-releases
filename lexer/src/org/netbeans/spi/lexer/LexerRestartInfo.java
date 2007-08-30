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

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;

/**
 * Lexer restart info contains all the necessary information for restarting
 * of a lexer mainly the lexer input, state and token factory.
 * 
 * <p>
 * When lexing embedded sections if {@link LanguageEmbedding#joinSections()}
 * returns true then the {@link #state()} will return state after
 * the last token of a corresponding previous section (with the same language path).
 * </p>
 *
 * @author Miloslav Metelka
 */

public final class LexerRestartInfo<T extends TokenId> {

    private final LexerInput input;
    
    private final TokenFactory<T> tokenFactory;
    
    private final Object state;
    
    private final LanguagePath languagePath;
    
    private final InputAttributes inputAttributes;
    
    LexerRestartInfo(LexerInput input,
    TokenFactory<T> tokenFactory, Object state,
    LanguagePath languagePath, InputAttributes inputAttributes) {
        this.input = input;
        this.tokenFactory = tokenFactory;
        this.state = state;
        this.languagePath = languagePath;
        this.inputAttributes = inputAttributes;
    }
    
    /**
     * Get lexer input from which the lexer should read characters.
     */
    public LexerInput input() {
        return input;
    }

    /**
     * Get token factory through which the lexer should produce tokens.
     */
    public TokenFactory<T> tokenFactory() {
        return tokenFactory;
    }
    
    /**
     * Get state from which the lexer should start lexing.
     */
    public Object state() {
        return state;
    }
    
    /**
     * Get language path at which the lexer operates.
     */
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    /**
     * Get supplementary information about particular input source
     * or null if there are no extra attributes.
     */
    public InputAttributes inputAttributes() {
        return inputAttributes;
    }
    
    /**
     * Get value of an attribute or null if the attribute is not set
     * or if there are no attributes at all.
     */
    public Object getAttributeValue(Object key) {
        return (inputAttributes != null)
                ? inputAttributes.getValue(languagePath, key)
                : null;
    }
    
}
