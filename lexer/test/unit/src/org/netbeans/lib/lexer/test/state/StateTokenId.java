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

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguageDescription;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * @author mmetelka
 */
public enum StateTokenId implements TokenId {
    
    A,
    BMULTI,
    ERROR;

    StateTokenId() {
    }
    
    public String primaryCategory() {
        return null;
    }
    
    
    private static final LanguageDescription<StateTokenId> lang = new LanguageHierarchy<StateTokenId>() {

        protected String mimeType() {
            return "text/x-simple";
        }

        protected Collection<StateTokenId> createTokenIds() {
            return EnumSet.allOf(StateTokenId.class);
        }

        protected Lexer<StateTokenId> createLexer(
        LexerInput input, TokenFactory<StateTokenId> tokenFactory, Object state,
        LanguagePath languagePath, InputAttributes inputAttributes) {
            return new StateLexer(input, tokenFactory, state, languagePath, inputAttributes);
        }
        
    }.language();
    
    public static LanguageDescription<StateTokenId> language() {
        return lang;
    }
        
}
