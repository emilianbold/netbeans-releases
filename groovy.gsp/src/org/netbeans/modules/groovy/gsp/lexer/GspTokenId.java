/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.groovy.gsp.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token Ids for Embedded Groovy (GSP).
 *
 * @todo Worry about trim mode - See section 22.1 of Agile Web Development With Rails
 *
 * @author Marek Fukala
 * @author Tor Norbye
 * @author Martin Adamek
 */

public enum GspTokenId implements TokenId {

    HTML("html"),
    GROOVYCOMMENT("comment"),
    GROOVY_EXPR("groovy"),
    GROOVY("groovy"),
    DELIMITER("groovy-delimiter"), // Note - referenced in LexUtilities
    GTAG("gtag"), // <g: and </g:
    ERROR("gsp_error");

    public static final String MIME_TYPE = "text/x-gsp"; // NOI18N
    private final String primaryCategory;


    private GspTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public static boolean isGroovy(TokenId id) {
        return id == GROOVY || id == GROOVY_EXPR || id == GROOVYCOMMENT;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<GspTokenId> LANGUAGE = new LanguageHierarchy<GspTokenId>() {
        @Override
        protected Collection<GspTokenId> createTokenIds() {
            return EnumSet.allOf(GspTokenId.class);
        }

        @Override
        protected Map<String, Collection<GspTokenId>> createTokenCategories() {
            return null;
        }

        @Override
        public Lexer<GspTokenId> createLexer(LexerRestartInfo<GspTokenId> info) {
            return new GspLexer(info);
        }

        @Override
        protected LanguageEmbedding<? extends TokenId> embedding(Token<GspTokenId> token,
                                  LanguagePath languagePath, InputAttributes inputAttributes) {
            switch(token.id()) {
                case HTML:
                case GTAG:
                    return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);
                case GROOVY_EXPR:
                case GROOVY:
                    return LanguageEmbedding.create(GroovyTokenId.language(), 0, 0, false);
                default:
                    return null;
            }
        }

        @Override
        public String mimeType() {
            return GspTokenId.MIME_TYPE;
        }
    }.language();

    public static Language<GspTokenId> language() {
        return LANGUAGE;
    }
}
