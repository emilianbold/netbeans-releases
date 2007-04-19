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

package org.netbeans.lib.lexer.lang;

import org.netbeans.lib.lexer.lang.TestStringTokenId;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.lib.lexer.test.simple.*;
import org.netbeans.lib.lexer.lang.TestJavadocTokenId;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
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
 * Simple implementation of enumerated token id.
 *
 * @author mmetelka
 */
public enum TestTokenId implements TokenId {
    
    IDENTIFIER(null, null),
    WHITESPACE(null, null), // normally would be "whitespace" category here but testing to do it in language hierarchy
    BLOCK_COMMENT(null, "comment"),
    LINE_COMMENT(null, "comment"),
    JAVADOC_COMMENT(null, "comment"),
    PLUS("+", "operator"),
    MINUS("-", "operator"),
    PLUS_MINUS_PLUS("+-+", null),
    DIV("/", "operator"),
    STAR("*", "operator"),
    ERROR(null, "error"),
    PUBLIC("public", "keyword"),
    PRIVATE("private", "keyword"),
    STATIC("static", "keyword"),
    STRING_LITERAL(null, "string"),

    BLOCK_COMMENT_INCOMPLETE(null, "comment"),
    JAVADOC_COMMENT_INCOMPLETE(null, "comment"),
    STRING_LITERAL_INCOMPLETE(null, "string"),
    ;

    private final String fixedText;

    private final String primaryCategory;

    TestTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }
    
    public String fixedText() {
        return fixedText;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<TestTokenId> language
    = new LanguageHierarchy<TestTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-simple";
        }

        @Override
        protected Collection<TestTokenId> createTokenIds() {
            return EnumSet.allOf(TestTokenId.class);
        }

        @Override
        protected Map<String,Collection<TestTokenId>> createTokenCategories() {
            Map<String,Collection<TestTokenId>> cats = new HashMap<String,Collection<TestTokenId>>();
            cats.put("operator",EnumSet.of(TestTokenId.PLUS_MINUS_PLUS));
            // Normally whitespace category would be a primary category in token id's declaration
            cats.put("whitespace",EnumSet.of(TestTokenId.WHITESPACE));
            cats.put("incomplete",EnumSet.of(
                    TestTokenId.BLOCK_COMMENT_INCOMPLETE,TestTokenId.JAVADOC_COMMENT_INCOMPLETE,TestTokenId.STRING_LITERAL_INCOMPLETE
            ));
            cats.put("error",EnumSet.of(
                    TestTokenId.BLOCK_COMMENT_INCOMPLETE,TestTokenId.JAVADOC_COMMENT_INCOMPLETE,TestTokenId.STRING_LITERAL_INCOMPLETE
            ));
            cats.put("test-category",EnumSet.of(
                    TestTokenId.IDENTIFIER,TestTokenId.PLUS,TestTokenId.MINUS
            ));
            return cats;
        }

        @Override
        protected Lexer<TestTokenId> createLexer(LexerRestartInfo<TestTokenId> info) {
            return new TestLexer(info);
        }
        
        @Override
        public LanguageEmbedding<? extends TokenId> embedding(
        Token<TestTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            // Test language embedding in the block comment
            switch (token.id()) {
                                                                                                                                case BLOCK_COMMENT:
                    return LanguageEmbedding.create(TestPlainTokenId.language(), 2, 2);

                case JAVADOC_COMMENT:
                    return LanguageEmbedding.create(TestJavadocTokenId.language(), 3, 2);

                case STRING_LITERAL:
                case STRING_LITERAL_INCOMPLETE:
                    return LanguageEmbedding.create(TestStringTokenId.language(), 1, 1);
            }
            return null; // No embedding
        }

//        protected CharPreprocessor createCharPreprocessor() {
//            return CharPreprocessor.createUnicodeEscapesPreprocessor();
//        }

    }.language();

    public static Language<TestTokenId> language() {
        return language;
    }

}
