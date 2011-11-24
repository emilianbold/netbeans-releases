/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.lexer;

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
 *
 * @author Petr Pisl
 * @author Petr Hejl
 */
public enum JsTokenId implements TokenId {
    UNKNOWN_TOKEN(null, "error"), // NOI18N
    WHITESPACE(null, "whitespace"), // NOI18N

    OPERATOR_LPARAN("(", "operator"), // NOI18N
    OPERATOR_RPARAN(")", "operator"), // NOI18N
    OPERATOR_LCURLY("{", "operator"), // NOI18N
    OPERATOR_RCURLY("}", "operator"), // NOI18N
    OPERATOR_LBRACKET("[", "operator"), // NOI18N
    OPERATOR_RBRACKET("]", "operator"), // NOI18N

    NUMBER(null, "constant"), // NOI18N
    COMMENT(null, "comment"), // NOI18N
    IDENTIFIER(null, "identifier"), // NOI18N
    STRING(null, "string"), // NOI18N

    KEYWORD_BREAK("break", "keyword"), // NOI18N
    KEYWORD_CASE("case", "keyword"), // NOI18N
    KEYWORD_CATCH("catch", "keyword"), // NOI18N
    KEYWORD_CONTINUE("continue", "keyword"), // NOI18N
    KEYWORD_DEFAULT("default", "keyword"), // NOI18N
    KEYWORD_DELETE("delete", "keyword"), // NOI18N
    KEYWORD_DO("do", "keyword"), // NOI18N
    KEYWORD_ELSE("else", "keyword"), // NOI18N
    KEYWORD_FINALLY("finally", "keyword"), // NOI18N
    KEYWORD_FOR("for", "keyword"), // NOI18N
    KEYWORD_FUNCTION("function", "keyword"), // NOI18N
    KEYWORD_IF("if", "keyword"), // NOI18N
    KEYWORD_IN("in", "keyword"), // NOI18N
    KEYWORD_INSTANCEOF("instanceof", "keyword"), // NOI18N
    KEYWORD_NEW("new", "keyword"), // NOI18N
    KEYWORD_RETURN("return", "keyword"), // NOI18N
    KEYWORD_SWITCH("switch", "keyword"), // NOI18N
    KEYWORD_THIS("this", "keyword"), // NOI18N
    KEYWORD_THROW("throw", "keyword"), // NOI18N
    KEYWORD_TRY("try", "keyword"), // NOI18N
    KEYWORD_TYPEOF("typeof", "keyword"), // NOI18N
    KEYWORD_VAR("var", "keyword"), // NOI18N
    KEYWORD_VOID("void", "keyword"), // NOI18N
    KEYWORD_WHILE("while", "keyword"), // NOI18N
    KEYWORD_WITH("with", "keyword"), // NOI18N

    RESERVED_ABSTRACT("abstract", "reserved"), // NOI18N
    RESERVED_BOOLEAN("boolean", "reserved"), // NOI18N
    RESERVED_BYTE("byte", "reserved"), // NOI18N
    RESERVED_CHAR("char", "reserved"), // NOI18N
    RESERVED_CLASS("class", "reserved"), // NOI18N
    RESERVED_CONST("const", "reserved"), // NOI18N
    RESERVED_DEBUGGER("debugger", "reserved"), // NOI18N
    RESERVED_DOUBLE("double", "reserved"), // NOI18N
    RESERVED_ENUM("enum", "reserved"), // NOI18N
    RESERVED_EXPORT("export", "reserved"), // NOI18N
    RESERVED_EXTENDS("extends", "reserved"), // NOI18N
    RESERVED_FINAL("final", "reserved"), // NOI18N
    RESERVED_FLOAT("float", "reserved"), // NOI18N
    RESERVED_GOTO("goto", "reserved"), // NOI18N
    RESERVED_IMPLEMENTS("implements", "reserved"), // NOI18N
    RESERVED_IMPORT("import", "reserved"), // NOI18N
    RESERVED_INT("int", "reserved"), // NOI18N
    RESERVED_INTERFACE("interface", "reserved"), // NOI18N
    RESERVED_LONG("long", "reserved"), // NOI18N
    RESERVED_NATIVE("native", "reserved"), // NOI18N
    RESERVED_PACKAGE("package", "reserved"), // NOI18N
    RESERVED_PRIVATE("private", "reserved"), // NOI18N
    RESERVED_PROTECTED("protected", "reserved"), // NOI18N
    RESERVED_PUBLIC("public", "reserved"), // NOI18N
    RESERVED_SHORT("short", "reserved"), // NOI18N
    RESERVED_STATIC("static", "reserved"), // NOI18N
    RESERVED_SUPER("super", "reserved"), // NOI18N
    RESERVED_SYNCHRONIZED("synchronized", "reserved"), // NOI18N
    RESERVED_THROWS("throws", "reserved"), // NOI18N
    RESERVED_TRANSIENT("transient", "reserved"), // NOI18N
    RESERVED_VOLATILE("volatile", "reserved"), // NOI18N

    KEYWORD_TRUE("true", "keyword"), // NOI18N
    KEYWORD_FALSE("false", "keyword"), // NOI18N
    KEYWORD_NULL("null", "keyword"), // NOI18N

    TYPE(null, "keyword"), // NOI18N
    OPERATOR(null, "operator"); // NOI18N

    public static final String JAVASCRIPT_MIME_TYPE = "text/javascript"; // NOI18N

    private final String fixedText;
    private final String primaryCategory;

    JsTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

     private static final Language<JsTokenId> language =
        new LanguageHierarchy<JsTokenId>() {
                protected String mimeType() {
                    return JsTokenId.JAVASCRIPT_MIME_TYPE;
                }

                protected Collection<JsTokenId> createTokenIds() {
                    return EnumSet.allOf(JsTokenId.class);
                }

                @Override
                protected Map<String, Collection<JsTokenId>> createTokenCategories() {
                    Map<String, Collection<JsTokenId>> cats =
                        new HashMap<String, Collection<JsTokenId>>();
                    return cats;
                }

                protected Lexer<JsTokenId> createLexer(LexerRestartInfo<JsTokenId> info) {
                    return JsLexer.create(info, true);
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<JsTokenId> token,
                    LanguagePath languagePath, InputAttributes inputAttributes) {
                    JsTokenId id = token.id();

//                    if (id == STRING_LITERAL) {
//                        return LanguageEmbedding.create(JsStringTokenId.language(), 0, 0);
//                    } else if (id == BLOCK_COMMENT || id == LINE_COMMENT) {
//                        return LanguageEmbedding.create(JsCommentTokenId.language(), 0, 0);
//                    }

                    return null; // No embedding
                }
            }.language();

     public static Language<JsTokenId> language() {
        return language;
    }
}
