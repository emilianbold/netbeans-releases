/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s): Sebastian Hörl
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.twig.editor.lexer;

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

public enum TwigTokenId implements TokenId {

    T_TWIG_NAME( null, "twig_name" ),
    T_TWIG_STRING( null, "twig_string" ),
    T_TWIG_NUMBER( null, "twig_number" ),
    T_TWIG_OPERATOR( null, "twig_operator" ),
    T_TWIG_PUNCTUATION( null, "twig_punctuation" ),
    T_TWIG_WHITESPACE( null, "twig_whitespace" ),
    T_TWIG_FUNCTION( null, "twig_function" ),

    T_TWIG_INSTRUCTION( null, "twig_instruction" ),
    T_TWIG_VARIABLE( null, "twig_variable" ),
    T_TWIG_COMMENT( null, "twig_comment" ),

    T_TWIG_OTHER( null, "twig_other" )
    ;

    private final String fixedText;
    private final String primaryCategory;

    TwigTokenId( String fixedText, String primaryCategory ) {
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

    private static final Language<TwigTokenId> language =
            new LanguageHierarchy<TwigTokenId>() {

                @Override
                protected Collection<TwigTokenId> createTokenIds() {
                    return EnumSet.allOf( TwigTokenId.class );
                }

                @Override
                protected Map<String, Collection<TwigTokenId>> createTokenCategories() {
                    Map<String, Collection<TwigTokenId>> cats = new HashMap<String, Collection<TwigTokenId>>();
                    return cats;
                }

                @Override
                protected Lexer<TwigTokenId> createLexer(LexerRestartInfo<TwigTokenId> info) {
                    return TwigLexer.create( info );
                }

                @Override
                protected String mimeType() {
                    return "text/twig-markup";
                }

                @Override
                protected LanguageEmbedding<?> embedding( Token<TwigTokenId> token,
                        LanguagePath languagePath, InputAttributes inputAttributes ) {
                    return null;
                }
            }.language();

    public static Language<TwigTokenId> language() {
        return language;
    }
}