/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.javascript.editing.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.javascript.editing.JsMimeResolver;
import org.netbeans.modules.javascript.editing.lexer.JsCommentTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;


/**
 * @todo Update for JavaScript
 * @todo I should handle embeddings of =begin/=end token pairs such that they
 *   get comment/rdoc highlighting!
 * 
 * @author Tor Norbye
 */
public enum JsTokenId implements TokenId {
    ERROR(null, "error"),
    NEW("new", "keyword"),
    
    IDENTIFIER(null, "identifier"),
    GLOBAL_VAR(null, "static"),
    CONSTANT(null, "constant"),
    INT_LITERAL(null, "number"),
    REGEXP_LITERAL(null, "regexp"),
    FLOAT_LITERAL(null, "number"),
    //  CHAR_LITERAL(null, "character"),
    STRING_LITERAL(null, "string"),
    WHITESPACE(null, "whitespace"),
    EOL(null, "whitespace"),
    LINE_COMMENT(null, "comment"),
    BLOCK_COMMENT(null, "comment"),
    LPAREN("(", "separator"),
    RPAREN(")", "separator"),
    LBRACE("{", "separator"),
    RBRACE("}", "separator"),
    LBRACKET("[", "separator"),
    RBRACKET("]", "separator"),
    STRING_BEGIN(null, "string"),
    STRING_END(null, "string"),
    REGEXP_BEGIN(null, "regexp"), // or separator,
    REGEXP_END(null, "regexp"),
    // Cheating: out of laziness just map all keywords returning from JRuby
    // into a single KEYWORD token; eventually I will have separate tokens
    // for each here such that the various helper methods for formatting,
    // smart indent, brace matching etc. can refer to specific keywords
    ANY_KEYWORD(null, "keyword"),
    ANY_OPERATOR(null, "operator"),
    DOT(null, "operator"),
    THIS("this", "keyword"),
    FOR("for", "keyword"),
    IF("if", "keyword"),
    ELSE("else", "keyword"),
    WHILE("while", "keyword"),

    SEMI(";", "operator"),
    FUNCTION("function", "keyword"),
    
    // Non-unary operators which indicate a line continuation if used at the end of a line
    NONUNARY_OP(null, "operator");

    private final String fixedText;
    private final String primaryCategory;

    JsTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<JsTokenId> language =
        new LanguageHierarchy<JsTokenId>() {
                protected String mimeType() {
                    return JsMimeResolver.JAVASCRIPT_MIME_TYPE;
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
                    return JsLexer.create(info);
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<JsTokenId> token,
                    LanguagePath languagePath, InputAttributes inputAttributes) {
                    JsTokenId id = token.id();

                    if (id == STRING_LITERAL) {
                        return LanguageEmbedding.create(JsStringTokenId.language(), 0, 0);
                    } else if (id == BLOCK_COMMENT || id == LINE_COMMENT) {
                        return LanguageEmbedding.create(JsCommentTokenId.language(), 0, 0);
                    }

                    return null; // No embedding
                }
            }.language();

    public static Language<JsTokenId> language() {
        return language;
    }
}
