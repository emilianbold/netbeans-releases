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

import org.netbeans.lib.lexer.lang.TestHTMLTagTokenId;
import org.netbeans.lib.lexer.lang.TestJavadocLexer;
import org.netbeans.lib.lexer.test.simple.*;
import java.util.Collection;
import java.util.EnumSet;
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
 * Token ids for simple javadoc language 
 * - copied from JavadocTokenId.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public enum TestJavadocTokenId implements TokenId {

    IDENT("comment"),
    TAG("javadoc-tag"),
    HTML_TAG("html-tag"),
    DOT("comment"),
    HASH("comment"),
    OTHER_TEXT("comment");

    private final String primaryCategory;

    TestJavadocTokenId() {
        this(null);
    }

    TestJavadocTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<TestJavadocTokenId> language
    = new LanguageHierarchy<TestJavadocTokenId>() {
        @Override
        protected Collection<TestJavadocTokenId> createTokenIds() {
            return EnumSet.allOf(TestJavadocTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<TestJavadocTokenId>> createTokenCategories() {
            return null; // no extra categories
        }

        @Override
        protected Lexer<TestJavadocTokenId> createLexer(LexerRestartInfo<TestJavadocTokenId> info) {
            return new TestJavadocLexer(info);
        }

        @Override
        protected LanguageEmbedding<? extends TokenId> embedding(
        Token<TestJavadocTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            switch (token.id()) {
                case HTML_TAG:
                    return LanguageEmbedding.create(TestHTMLTagTokenId.language(), 0, 0);
            }
            return null; // No embedding
        }

        @Override
        protected String mimeType() {
            return "text/x-javadoc";
        }
    }.language();

    public static Language<TestJavadocTokenId> language() {
        return language;
    }

}
