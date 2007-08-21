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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.ruby.RubyMimeResolver;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;


/**
 * @todo I should handle embeddings of =begin/=end token pairs such that they
 *   get comment/rdoc highlighting!
 * 
 * @author Tor Norbye
 */
public class RubyTokenId extends GsfTokenId {
    // FixedText on these tokens is risky since the JRuby parser in some cases
    // seems to use these tokens for multiple source words; when I try opening
    // for example imap.rb Bad Stuff happens. For now compute token text on the fly.
    public static final GsfTokenId DOT = new GsfTokenId("DOT", null, "operator");
    public static final GsfTokenId RANGE = new GsfTokenId("RANGE", null, "operator");
    public static final GsfTokenId COLON3 = new GsfTokenId("COLON3", null, "operator");
    public static final GsfTokenId SUPER = new GsfTokenId("SUPER", null, "keyword");
    public static final GsfTokenId SELF = new GsfTokenId("SELF", null, "keyword");
    public static final GsfTokenId QUOTED_STRING_LITERAL =
        new GsfTokenId("QUOTED_STRING_LITERAL", null, "string");
    public static final GsfTokenId QUOTED_STRING_BEGIN =
        new GsfTokenId("QUOTED_STRING_BEGIN", null, "string");
    public static final GsfTokenId QUOTED_STRING_END =
        new GsfTokenId("QUOTED_STRING_END", null, "string");
    public static final GsfTokenId EMBEDDED_RUBY = 
        //new GsfTokenId("EMBEDDED_RUBY", null, "string");
        new GsfTokenId("EMBEDDED_RUBY", null, "default");
    public static final GsfTokenId MODULE = new GsfTokenId("MODULE", null, "keyword");
    public static final GsfTokenId CLASS = new GsfTokenId("CLASS", null, "keyword");
    public static final GsfTokenId DEF = new GsfTokenId("DEF", null, "keyword");
    public static final GsfTokenId END = new GsfTokenId("END", null, "keyword");
    public static final GsfTokenId BEGIN = new GsfTokenId("BEGIN", null, "keyword");
    public static final GsfTokenId FOR = new GsfTokenId("FOR", null, "keyword");
    public static final GsfTokenId CASE = new GsfTokenId("CASE", null, "keyword");
    public static final GsfTokenId LOOP = new GsfTokenId("LOOP", null, "keyword");

    /** The "do" keyword, when used as a block (NOT as a statement modifier or in a block/closure) */
    public static final GsfTokenId DO = new GsfTokenId("DO", null, "keyword");

    /** The "if" keyword, when used as a block (NOT as a statement modifier) */
    public static final GsfTokenId IF = new GsfTokenId("IF", null, "keyword");

    /** The "while" keyword, when used as a block (NOT as a statement modifier) */
    public static final GsfTokenId WHILE = new GsfTokenId("WHILE", null, "keyword");

    /** The "until" keyword, when used as a block (NOT as a statement modifier) */
    public static final GsfTokenId UNTIL = new GsfTokenId("UNTIL", null, "keyword");

    /** The "unless" keyword, when used as a block (NOT as a statement modifier) */
    public static final GsfTokenId UNLESS = new GsfTokenId("UNLESS", null, "keyword");

    // Indent words
    public static final GsfTokenId ELSE = new GsfTokenId("ELSE", null, "keyword");
    public static final GsfTokenId ELSIF = new GsfTokenId("ELSIF", null, "keyword");
    public static final GsfTokenId ENSURE = new GsfTokenId("ENSURE", null, "keyword");
    public static final GsfTokenId WHEN = new GsfTokenId("WHEN", null, "keyword");
    public static final GsfTokenId RESCUE = new GsfTokenId("RESCUE", null, "keyword");

    // Non-unary operators which indicate a line continuation if used at the end of a line
    public static final GsfTokenId NONUNARY_OP = new GsfTokenId("NONUNARY_OP", null, "operator");
    
    private static final Language<GsfTokenId> language =
        new LanguageHierarchy<GsfTokenId>() {
                protected String mimeType() {
                    return RubyMimeResolver.RUBY_MIME_TYPE;
                }

                protected Collection<GsfTokenId> createTokenIds() {
                    return getUsedTokens();
                }

                @Override
                protected Map<String, Collection<GsfTokenId>> createTokenCategories() {
                    Map<String, Collection<GsfTokenId>> cats =
                        new HashMap<String, Collection<GsfTokenId>>();

                    //            // Incomplete tokens
                    //            cats.put("incomplete", EnumSet.of(
                    //                RubyTokenId.CHAR_LITERAL_INCOMPLETE,
                    //                RubyTokenId.STRING_LITERAL_INCOMPLETE,
                    //                RubyTokenId.BLOCK_COMMENT_INCOMPLETE
                    //            ));
                    //            // Additional literals being a lexical error
                    //            cats.put("error", EnumSet.of(
                    //                RubyTokenId.CHAR_LITERAL_INCOMPLETE,
                    //                RubyTokenId.STRING_LITERAL_INCOMPLETE,
                    //                RubyTokenId.BLOCK_COMMENT_INCOMPLETE,
                    //                RubyTokenId.FLOAT_LITERAL_INVALID
                    //            ));
                    //            // Complete and incomplete literals
                    //            EnumSet<RubyTokenId> l = EnumSet.of(
                    //                RubyTokenId.INT_LITERAL,
                    //                RubyTokenId.LONG_LITERAL,
                    //                RubyTokenId.FLOAT_LITERAL,
                    //                RubyTokenId.DOUBLE_LITERAL,
                    //                RubyTokenId.CHAR_LITERAL
                    //            );
                    //            l.addAll(EnumSet.of(
                    //                RubyTokenId.CHAR_LITERAL_INCOMPLETE,
                    //                RubyTokenId.STRING_LITERAL,
                    //                RubyTokenId.STRING_LITERAL_INCOMPLETE
                    //            ));
                    //            cats.put("literal", l);
                    return cats;
                }

                protected Lexer<GsfTokenId> createLexer(LexerRestartInfo<GsfTokenId> info) {
                    return RubyLexer.create(info);
                }

                @Override
                protected LanguageEmbedding<?extends TokenId> embedding(Token<GsfTokenId> token,
                    LanguagePath languagePath, InputAttributes inputAttributes) {
                    GsfTokenId id = token.id();

                    if (id == QUOTED_STRING_LITERAL) {
                        return LanguageEmbedding.create(RubyStringTokenId.languageDouble(), 0, 0);
                    } else if (id == STRING_LITERAL) {
                        return LanguageEmbedding.create(RubyStringTokenId.languageSingle(), 0, 0);
                    } else if ((id == LINE_COMMENT) || (id == BLOCK_COMMENT) ||
                            (id == BLOCK_COMMENT_INCOMPLETE)) {
                        return LanguageEmbedding.create(RubyCommentTokenId.language(), 1, 0);

                        //} else if (id == REGEXP_LITERAL) {
                        //    return LanguageEmbedding.create(RubyRegexpTokenId.language(), 0, 0);
                    } else if (id == DOCUMENTATION) {
                        return LanguageEmbedding.create(RubyCommentTokenId.language(), 0, 0);
                    } else if (id == EMBEDDED_RUBY) {
                        return LanguageEmbedding.create(RubyTokenId.language(), 0, 0);
                    }

                    return null; // No embedding
                }

                //        protected CharPreprocessor createCharPreprocessor() {
                //            return CharPreprocessor.createUnicodeEscapesPreprocessor();
                //        }
            }.language();

    public RubyTokenId(String name, String fixedText, String primaryCategory) {
        super(name, fixedText, primaryCategory);
    }

    public static List<GsfTokenId> getUsedTokens() {
        List<GsfTokenId> types = new ArrayList<GsfTokenId>(20);
        types.add(ERROR);
        types.add(IDENTIFIER);
        types.add(CLASS_VAR);
        types.add(INSTANCE_VAR);
        types.add(GLOBAL_VAR);
        types.add(CONSTANT);
        types.add(DOCUMENTATION);
        types.add(INT_LITERAL);
        types.add(REGEXP_LITERAL);
        types.add(LONG_LITERAL);
        types.add(FLOAT_LITERAL);
        types.add(DOUBLE_LITERAL);
        types.add(CHAR_LITERAL);
        types.add(QUOTED_STRING_LITERAL);
        types.add(STRING_LITERAL);
        types.add(WHITESPACE);
        types.add(LINE_COMMENT);
        types.add(BLOCK_COMMENT);
        types.add(TODO);
        types.add(TYPE_SYMBOL);
        types.add(EMBEDDED_RUBY);
        types.add(RANGE);
        types.add(COLON3);
        types.add(NONUNARY_OP);
        types.add(DOT);
        types.add(SUPER);
        types.add(SELF);
        types.add(QUOTED_STRING_BEGIN);
        types.add(QUOTED_STRING_END);

        types.add(LPAREN);
        types.add(RPAREN);
        types.add(LBRACE);
        types.add(RBRACE);
        types.add(LBRACKET);
        types.add(RBRACKET);
        types.add(STRING_BEGIN);
        types.add(STRING_END);
        types.add(REGEXP_BEGIN);
        types.add(REGEXP_END);

        // Keywords: block oriented
        types.add(DEF);
        types.add(END);
        types.add(DO);
        types.add(BEGIN);
        types.add(IF);
        types.add(CLASS);
        types.add(MODULE);
        types.add(FOR);
        types.add(CASE);
        types.add(LOOP);
        types.add(WHILE);
        types.add(UNTIL);
        types.add(UNLESS);
        types.add(ELSE);
        types.add(ELSIF);
        types.add(ENSURE);
        types.add(WHEN);
        types.add(RESCUE);

        types.add(ANY_KEYWORD);
        types.add(ANY_OPERATOR);

        return types;
    }

    public static Language<GsfTokenId> language() {
        return language;
    }
}
