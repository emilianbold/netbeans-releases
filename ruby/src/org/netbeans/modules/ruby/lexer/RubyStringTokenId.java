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
package org.netbeans.modules.ruby.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.ruby.lexer.RubyStringLexer;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids for java string language
 * (embedded in java string or character literals).
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public enum RubyStringTokenId implements TokenId {STRING_TEXT("string"),
    STRING_ESCAPE("string-escape"),
    STRING_INVALID("string-escape-invalid"),
    EMBEDDED_RUBY("string");

    private final String primaryCategory;

    RubyStringTokenId() {
        this(null);
    }

    RubyStringTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    /** Language for double quoted strings */
    private static final Language<RubyStringTokenId> languageDouble =
        new LanguageHierarchy<RubyStringTokenId>() {
                @Override
                protected Collection<RubyStringTokenId> createTokenIds() {
                    return EnumSet.allOf(RubyStringTokenId.class);
                }

                @Override
                protected Map<String, Collection<RubyStringTokenId>> createTokenCategories() {
                    return null; // no extra categories
                }

                @Override
                protected Lexer<RubyStringTokenId> createLexer(
                    LexerRestartInfo<RubyStringTokenId> info) {
                    return new RubyStringLexer(info, true);
                }

                @Override
                protected LanguageEmbedding<?extends TokenId> embedding(
                    Token<RubyStringTokenId> token, LanguagePath languagePath,
                    InputAttributes inputAttributes) {
                    RubyStringTokenId id = token.id();

                    if (id == EMBEDDED_RUBY) {
                        return LanguageEmbedding.create(RubyTokenId.language(), 0, 0);
                    }

                    return null; // No embedding
                }

                @Override
                public String mimeType() {
                    return "text/x-ruby-string-double";
                }
            }.language();

    /** Language for single quoted strings */
    private static final Language<RubyStringTokenId> languageSingle =
        new LanguageHierarchy<RubyStringTokenId>() {
                @Override
                protected Collection<RubyStringTokenId> createTokenIds() {
                    return EnumSet.allOf(RubyStringTokenId.class);
                }

                @Override
                protected Map<String, Collection<RubyStringTokenId>> createTokenCategories() {
                    return null; // no extra categories
                }

                @Override
                protected Lexer<RubyStringTokenId> createLexer(
                    LexerRestartInfo<RubyStringTokenId> info) {
                    return new RubyStringLexer(info, false);
                }

                @Override
                protected LanguageEmbedding<?extends TokenId> embedding(
                    Token<RubyStringTokenId> token, LanguagePath languagePath,
                    InputAttributes inputAttributes) {
                    RubyStringTokenId id = token.id();

                    if (id == EMBEDDED_RUBY) {
                        return LanguageEmbedding.create(RubyTokenId.language(), 0, 0);
                    }

                    return null; // No embedding
                }

                @Override
                public String mimeType() {
                    return "text/x-ruby-string-single";
                }
            }.language();

    public static Language<RubyStringTokenId> languageDouble() {
        return languageDouble;
    }

    public static Language<RubyStringTokenId> languageSingle() {
        return languageSingle;
    }
}
