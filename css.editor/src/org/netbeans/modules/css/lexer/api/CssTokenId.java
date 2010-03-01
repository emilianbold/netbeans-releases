/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * or only the GPL Version , indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.css.lexer.api;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.css.gsf.CssLanguage;
import org.netbeans.modules.css.lexer.CssLexer;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids of CSS language
 *
 * @author Marek Fukala
 */
public enum CssTokenId implements TokenId {

    /* Defined categories:
     * -------------------
     * others
     * brace
     * separator
     * hash
     * operator
     * string
     * url
     * keyword
     * number
     * rgb
     * identifier
     * function
     * whitespace
     * comment
     */
    
    EOF("others"),
    S("whitespace"),
    COMMENT("comment"),
    MSE("ms_expression"),
    LBRACE("brace"),
    RBRACE("brace"),
    COMMA("separator"),
    DOT("operator"),
    SEMICOLON("separator"),
    COLON("separator"),
    ASTERISK("operator"),
    SLASH("operator"),
    PLUS("operator"),
    MINUS("operator"),
    EQUALS("operator"),
    GT("operator"),
    LSQUARE("brace"),
    RSQUARE("brace"),
    HASH("hash"),
    STRING("string"),
    RROUND("brace"),
    URL("url"),
    URI("url"),
    GENERATED("others"),
    CDO("whitespace"),
    CDC("whitespace"),
    INCLUDES("operator"),
    DASHMATCH("operator"),
    IMPORT_SYM("keyword"),
    PAGE_SYM("keyword"),
    MEDIA_SYM("keyword"),
    FONT_FACE_SYM("keyword"),
    CHARSET_SYM("keyword"),
    ATKEYWORD("keyword"),
    IMPORTANT_SYM("keyword"),
    INHERIT("keyword"),
    EMS("number"),
    EXS("number"),
    LENGTH_PX("number"),
    LENGTH_CM("number"),
    LENGTH_MM("number"),
    LENGTH_IN("number"),
    LENGTH_PT("number"),
    LENGTH_PC("number"),
    ANGLE_DEG("number"),
    ANGLE_RAD("number"),
    ANGLE_GRAD("number"),
    TIME_MS("number"),
    TIME_S("number"),
    FREQ_HZ("number"),
    FREQ_KHZ("number"),
    DIMEN("number"),
    PERCENTAGE("number"),
    NUMBER("number"),
    RGB("others"),
    FUNCTION("function"),
    IDENT("identifier"),
    NAME("others"),
    NUM("number"),
    UNICODERANGE("others"),
    RANGE("others"),
    Q16("others"),
    Q15("others"),
    Q14("others"),
    Q13("others"),
    Q12("others"),
    Q11("others"),
    NMSTART("others"),
    NMCHAR("others"),
    SELECTOR_NMSTART("others"),
    SELECTOR_NMCHAR("others"),
    STRING1("string"),
    STRING2("string"),
    NONASCII("others"),
    ESCAPE("others"),
    NL("others"),
    UNICODE("others"),
    HNUM("others"),
    H("others"),
    UNKNOWN("unknown"); //NOI18N 
    
    
    private final String primaryCategory;

    private static final String JAVASCRIPT_MIMETYPE = "text/javascript";//NOI18N
    
    CssTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }
    private static final Language<CssTokenId> language = new LanguageHierarchy<CssTokenId>() {

        @Override
        protected Collection<CssTokenId> createTokenIds() {
            return EnumSet.allOf(CssTokenId.class);
        }

        @Override
        protected Map<String, Collection<CssTokenId>> createTokenCategories() {
            Map<String,Collection<CssTokenId>> cats = new HashMap<String,Collection<CssTokenId>>();
            // Additional literals being a lexical error
            
            //TODO Add all tokens to some meaningful categories otherwise the options will look messy
            
            return cats;
        }

        @Override
        protected Lexer<CssTokenId> createLexer(LexerRestartInfo<CssTokenId> info) {
            return new CssLexer(info);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected LanguageEmbedding embedding(
                Token<CssTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            if(token.text() == null) {
                return null;
            }
            if (token.id() == MSE) {
                Language lang = Language.find(JAVASCRIPT_MIMETYPE);
                if (lang == null) {
                    return null; //no language found
                } else {
                    //expression(
                    //01234567890
                    String tokenImage = token.text().toString();
                    int lastParenthesisIndex = tokenImage.lastIndexOf(')');
                    return LanguageEmbedding.create(lang, 11, token.length() - lastParenthesisIndex, false);
                }
            }
            return null;
        }

        @Override
        protected String mimeType() {
            return CssLanguage.CSS_MIME_TYPE;
        }
    }.language();

    /** Gets a LanguageDescription describing a set of token ids
     * that comprise the given language.
     *
     * @return non-null LanguageDescription
     */
    public static Language<CssTokenId> language() {
        return language;
    }

    /**
     * Get name of primary token category into which this token belongs.
     * <br/>
     * Other token categories for this id can be defined in the language hierarchy.
     *
     * @return name of the primary token category into which this token belongs
     *  or null if there is no primary category for this token.
     */
    public String primaryCategory() {
        return primaryCategory;
    }
}
