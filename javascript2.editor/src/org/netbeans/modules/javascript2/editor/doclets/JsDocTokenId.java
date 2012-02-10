/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.doclets;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.*;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * JSDoc TokenId class.
 * 
 * @author Martin Fousek <marfous@netbeans.org>
 */
public enum JsDocTokenId implements TokenId {

    // IMPORTANT - Categories of JavaScript tokenIds should be shared across
    //  all JavaScript documentation tools to preserve coloring settings.
    KEYWORD("COMMENT_KEYWORD"), //NOI18N
    COMMENT_START("COMMENT"), //NOI18N
    COMMENT_BLOCK("COMMENT"), //NOI18N
    COMMENT_NOCODE_BEGIN("COMMENT_NOCODE"), //NOI18N
    COMMENT_NOCODE_END("COMMENT_NOCODE"), //NOI18N
    COMMENT_LINE("COMMENT"), //NOI18N
    COMMENT_END("COMMENT"), //NOI18N
    COMMENT_CODE("COMMENT"), //NOI18N
    COMMENT_SHARED_END("COMMENT"), //NOI18N
    COMMENT_SHARED_BEGIN("COMMENT"), //NOI18N
    UNKNOWN("UNKNOWN"); //NOI18N

    public static final String JSDOC_MIME_TYPE = "text/javascript-comment"; //NOI18N
    private final String primaryCategory;

    JsDocTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<JsDocTokenId> LANGUAGE =
        new LanguageHierarchy<JsDocTokenId>() {
                @Override
                protected String mimeType() {
                    return JsDocTokenId.JSDOC_MIME_TYPE;
                }

                @Override
                protected Collection<JsDocTokenId> createTokenIds() {
                    return EnumSet.allOf(JsDocTokenId.class);
                }

                @Override
                protected Map<String, Collection<JsDocTokenId>> createTokenCategories() {
            Map<String, Collection<JsDocTokenId>> cats = new HashMap<String, Collection<JsDocTokenId>>();
                    return cats;
                }

                @Override
                protected Lexer<JsDocTokenId> createLexer(LexerRestartInfo<JsDocTokenId> info) {
                    return JsDocLexer.create(info);
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<JsDocTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
                    
                    // No embedding
                    return null; 
                }
            }.language();

     public static Language<JsDocTokenId> language() {
        return LANGUAGE;
    }

}
