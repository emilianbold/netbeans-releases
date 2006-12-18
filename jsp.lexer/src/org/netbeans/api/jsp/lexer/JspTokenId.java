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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.jsp.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.jsp.lexer.JspLexer;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token Ids for JSP language
 *
 * @author Marek Fukala
 */

public enum JspTokenId implements TokenId {

    TEXT("text"),
    SCRIPTLET("scriptlet"),
    ERROR("error"),
    TAG("tag-directive"),
    SYMBOL("symbol"),
    SYMBOL2("scriptlet-delimiter"),
    COMMENT("comment"),
    ATTRIBUTE("attribute-name"),
    ATTR_VALUE("attribute-value"),
    EOL("EOL"),
    WHITESPACE("jsp-whitespace"), //coloring workaround - prefix must be removed once the coloring is fully constructed based on language path
    EL("expression-language");
    

    private final String primaryCategory;

    JspTokenId() {
        this(null);
    }

    JspTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    // Token ids declaration
    private static final Language<JspTokenId> language = new LanguageHierarchy<JspTokenId>() {
        @Override
        protected Collection<JspTokenId> createTokenIds() {
            return EnumSet.allOf(JspTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<JspTokenId>> createTokenCategories() {
            //Map<String,Collection<JspTokenId>> cats = new HashMap<String,Collection<JspTokenId>>();
            // Additional literals being a lexical error
            //cats.put("error", EnumSet.of());
            return null;
        }
        
        @Override
        protected Lexer<JspTokenId> createLexer(LexerRestartInfo<JspTokenId> info) {
            return new JspLexer(info);
        }
        
        @Override
        protected LanguageEmbedding<? extends TokenId> embedding(
        Token<JspTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            switch(token.id()) {
                case TEXT:
                    return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0);
                case EL:
                    //lexer infrastructure workaround - need to adjust skiplenghts in case of short token
                    int startSkipLength = token.length() > 2 ? 2 : token.length();
                    int endSkipLength = token.length() > 2 ? 1 : 0;
                    return LanguageEmbedding.create(ELTokenId.language(), startSkipLength, endSkipLength);
                    
                case SCRIPTLET:
                    return LanguageEmbedding.create(JavaTokenId.language(), 0, 0);
                    
                default:
                    return null;
            }
        }
        
        @Override
        protected String mimeType() {
            return "text/html";
        }
    }.language();
    
    public static Language<JspTokenId> language() {
        return language;
    }
    

}

