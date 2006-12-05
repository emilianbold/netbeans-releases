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
    WHITESPACE("whitespace"),
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
        protected Collection<JspTokenId> createTokenIds() {
            return EnumSet.allOf(JspTokenId.class);
        }
        
        protected Map<String,Collection<JspTokenId>> createTokenCategories() {
            //Map<String,Collection<JspTokenId>> cats = new HashMap<String,Collection<JspTokenId>>();
            // Additional literals being a lexical error
            //cats.put("error", EnumSet.of());
            return null;
        }
        
        public Lexer<JspTokenId> createLexer(LexerRestartInfo<JspTokenId> info) {
            return new JspLexer(info);
        }
        
        public LanguageEmbedding<? extends TokenId> embedding(
        Token<JspTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            switch(token.id()) {
                case TEXT:
                    return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0);
                case EL:
                    return LanguageEmbedding.create(ELTokenId.language(), 2, 1);
                    //                case SCRIPTLET:
                    //                    return new LanguageEmbedding() {
                    //                        public LanguageDescription language() {
                    //                            return JavaLanguage.description();
                    //                        }
                    //
                    //                        public int startSkipLength() {
                    //                            return 0;
                    //                        }
                    //
                    //                        public int endSkipLength() {
                    //                            return 0;
                    //                        }
                    //                    };
                default:
                    return null;
            }
        }
        
        public String mimeType() {
            return "text/html";
        }
    }.language();
    
    public static Language<JspTokenId> language() {
        return language;
    }
    

}

