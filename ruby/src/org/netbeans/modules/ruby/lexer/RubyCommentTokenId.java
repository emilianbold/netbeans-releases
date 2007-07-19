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

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids for comments
 *
 * @author Miloslav Metelka
 * @author Tor Norbye
 */
public enum RubyCommentTokenId implements TokenId {COMMENT_TEXT("comment"),
    COMMENT_TODO("todo"),
    COMMENT_RDOC("rdoc"),
    COMMENT_LINK("comment-link"),
    COMMENT_BOLD("comment-bold"),
    COMMENT_ITALIC("comment-italic"),
    COMMENT_HTMLTAG("dochtml");

    private final String primaryCategory;

    RubyCommentTokenId() {
        this(null);
    }

    RubyCommentTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<RubyCommentTokenId> language =
        new LanguageHierarchy<RubyCommentTokenId>() {
                @Override
                protected Collection<RubyCommentTokenId> createTokenIds() {
                    return EnumSet.allOf(RubyCommentTokenId.class);
                }

                @Override
                protected Map<String, Collection<RubyCommentTokenId>> createTokenCategories() {
                    return null; // no extra categories
                }

                @Override
                protected Lexer<RubyCommentTokenId> createLexer(
                    LexerRestartInfo<RubyCommentTokenId> info) {
                    return new RubyCommentLexer(info);
                }

                @Override
                public String mimeType() {
                    return "text/x-ruby-comment";
                }
            }.language();

    public static Language<RubyCommentTokenId> language() {
        return language;
    }
}
