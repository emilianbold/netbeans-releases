/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.html.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguageDescription;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.html.lexer.HTMLLexer;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Token ids of HTML language
 *
 * @author Jan Lahoda, Miloslav Metelka
 */
public enum HTMLTokenId implements TokenId {
    
    TEXT("html-text"),
    SCRIPT("html-script"),
    WS("html-ws"),
    ERROR("html-error"),
    TAG_OPEN("html-tag"),
    TAG_CLOSE("html-tag"),
    ARGUMENT("html-argument"),
    OPERATOR("html-operator"),
    VALUE("html-value"),
    BLOCK_COMMENT("html-block-comment"),
    SGML_COMMENT("html-sgml-comment"),
    DECLARATION("html-sgml-declaration"),
    CHARACTER("html-character"),
    EOL("html-text"),
    TAG_OPEN_SYMBOL("html-tag"),
    TAG_CLOSE_SYMBOL("html-tag");
    
    private final String primaryCategory;

    HTMLTokenId() {
        this(null);
    }

    HTMLTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final LanguageDescription<HTMLTokenId> language = new LanguageHierarchy<HTMLTokenId>() {
        protected Collection<HTMLTokenId> createTokenIds() {
            return EnumSet.allOf(HTMLTokenId.class);
        }
        
        protected Map<String,Collection<HTMLTokenId>> createTokenCategories() {
            //Map<String,Collection<HTMLTokenId>> cats = new HashMap<String,Collection<HTMLTokenId>>();
            // Additional literals being a lexical error
            //cats.put("error", EnumSet.of());
            return null;
        }
        
        public Lexer<HTMLTokenId> createLexer(
        LexerInput input, TokenFactory<HTMLTokenId> tokenFactory, Object state,
        LanguagePath languagePath, InputAttributes inputAttributes) {
            return new HTMLLexer(input, tokenFactory, state, languagePath, inputAttributes);
        }
        
        public LanguageEmbedding embedding(
        Token<HTMLTokenId> token, boolean tokenComplete,
        LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }
        
        public String mimeType() {
            return "text/html";
        }
    }.language();
    
    public static LanguageDescription<HTMLTokenId> language() {
        return language;
    }
    
}
