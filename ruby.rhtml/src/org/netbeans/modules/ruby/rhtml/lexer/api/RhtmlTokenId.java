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

package org.netbeans.modules.ruby.rhtml.lexer.api;

import org.netbeans.modules.ruby.rhtml.lexer.*;
import org.netbeans.modules.ruby.rhtml.editor.RhtmlKit;
import org.netbeans.modules.ruby.rhtml.*;
import org.netbeans.modules.ruby.rhtml.lexer.RhtmlLexer;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token Ids for Embedded Ruby (RHTML)
 *
 * @todo Worry about trim mode - See section 22.1 of Agile Web Development With Rails
 * 
 * @author Marek Fukala
 * @author Tor Norbye
 */

public enum RhtmlTokenId implements TokenId {

    HTML("html"),
    /** Contents inside <%# %> */
    RUBYCOMMENT("comment"),
    /** Contents inside <%= %> */
    RUBY_EXPR("ruby"),
    /** Contents inside <% %> */
    RUBY("ruby"),
    /** <% or %> */
    DELIMITER("ruby-delimiter"); // Note - referenced in LexUtilities

    public static final String MIME_TYPE = "application/x-httpd-eruby"; // NOI18N
    
    private final String primaryCategory;
    
    public static boolean isRuby(TokenId id) {
        return id == RUBY || id == RUBY_EXPR || id == RUBYCOMMENT;
    }

    RhtmlTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    // Token ids declaration
    private static final Language<RhtmlTokenId> language = new LanguageHierarchy<RhtmlTokenId>() {
        protected Collection<RhtmlTokenId> createTokenIds() {
            return EnumSet.allOf(RhtmlTokenId.class);
        }
        
        protected Map<String,Collection<RhtmlTokenId>> createTokenCategories() {
            return null;
        }
        
        public Lexer<RhtmlTokenId> createLexer(LexerRestartInfo<RhtmlTokenId> info) {
            return new RhtmlLexer(info);
        }
        
        @Override
        protected LanguageEmbedding<? extends TokenId> embedding(Token<RhtmlTokenId> token,
                                  LanguagePath languagePath, InputAttributes inputAttributes) {
            switch(token.id()) {
                case HTML:
                    return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, false);
                case RUBY_EXPR:
                case RUBY:
                    return LanguageEmbedding.create(RubyTokenId.language(), 0, 0, false);
                default:
                    return null;
            }
        }
        
        public String mimeType() {
            return RhtmlTokenId.MIME_TYPE;
        }
    }.language();
    
    public static Language<RhtmlTokenId> language() {
        return language;
    }
}
