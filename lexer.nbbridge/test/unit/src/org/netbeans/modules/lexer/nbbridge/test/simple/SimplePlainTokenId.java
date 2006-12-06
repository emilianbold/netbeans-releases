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

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token identifications of the simple plain language.
 *
 * @author mmetelka
 */
public enum SimplePlainTokenId implements TokenId {
    
    WORD,
    WHITESPACE("whitespace");

    private final String primaryCategory;

    SimplePlainTokenId() {
        this(null);
    }

    SimplePlainTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }
    
    private static final Language<SimplePlainTokenId> language = new LanguageHierarchy<SimplePlainTokenId>() {

        @Override
        protected Collection<SimplePlainTokenId> createTokenIds() {
            return EnumSet.allOf(SimplePlainTokenId.class);
        }
        
        @Override
        protected Lexer<SimplePlainTokenId> createLexer(LexerRestartInfo<SimplePlainTokenId> info) {
            return new SimplePlainLexer(info);
        }
        
        @Override
        protected String mimeType() {
            return "text/x-simple-plain";
        }
        
    }.language();
    
    public static Language<SimplePlainTokenId> language() {
        return language;
    }


}
