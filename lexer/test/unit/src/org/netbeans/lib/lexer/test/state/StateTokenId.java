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
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

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
    
    
    private static final Language<StateTokenId> lang = new LanguageHierarchy<StateTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-simple";
        }

        @Override
        protected Collection<StateTokenId> createTokenIds() {
            return EnumSet.allOf(StateTokenId.class);
        }

        @Override
        protected Lexer<StateTokenId> createLexer(LexerRestartInfo<StateTokenId> info) {
            return new StateLexer(info);
        }
        
    }.language();
    
    public static Language<StateTokenId> language() {
        return lang;
    }
        
}
