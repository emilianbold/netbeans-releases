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
package org.netbeans.modules.php.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LanguageProvider;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;


/**
 * @author ads
 *
 */
public enum PhpTokenId implements TokenId {
    
    HTML("html"),
    PHP("php"),
    DELIMITER("php-delimiter-pc"),
    DELIMITER1("php-delimiter-q"),
    DELIMITER2("php-delimiter-php"),
    DELIMITER_END( "php-delimiter-end" );
    
    public static String MIME_TYPE          = "text/x-php5";               // NOI18N
    
    public static String EMBED_MIME_TYPE    = "text/x-pure-php5";          // NOI18N
    
    private PhpTokenId( String category ) {
        myCategory = category ;
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.lexer.TokenId#primaryCategory()
     */
    public String primaryCategory() {
        return myCategory;
    }
    
    public static Language<PhpTokenId> language() {
        return LANGUAGE;
    }
    
    public static Language getPhpLanguage() {
        return Language.find(EMBED_MIME_TYPE);
    }
    
    private static class PhpLanguageHierarchy extends LanguageHierarchy<PhpTokenId> {
        
        protected Collection<PhpTokenId> createTokenIds() {
            return EnumSet.allOf(PhpTokenId.class);
        }
        
        protected Map<String,Collection<PhpTokenId>> createTokenCategories() {
            return null;
        }
        
        public Lexer<PhpTokenId> createLexer(LexerRestartInfo<PhpTokenId> info) {
            return new PhpLexer(info);
        }
        
        @Override
        protected LanguageEmbedding<?> embedding(
                Token<PhpTokenId> token,LanguagePath languagePath, 
                InputAttributes inputAttributes) 
        {
            switch(token.id()) {
                case HTML:
                    return LanguageEmbedding.create(
                            HTMLTokenId.language(), 0, 0, true);
                case PHP:
                    Language<?> lang = Language.find(EMBED_MIME_TYPE);
                    return LanguageEmbedding.create(lang, 0, 0, true);
                default:
                    return null;
            }
        }
        
        public String mimeType() {
            return MIME_TYPE;
        }
        
    }
    
    private static final Language<PhpTokenId> LANGUAGE = 
        new PhpLanguageHierarchy().language();
    
    private String myCategory;
}
