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

package org.netbeans.cnd.api.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.cnd.lexer.CppLexer;
import org.netbeans.modules.cnd.lexer.PreprocLexer;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids of C/C++ languages defined as enum.
 *
 * @author Vladimir Voskresensky
 * @version 1.00
 */
public enum CppTokenId implements TokenId {
    
    // make sure token category names are the same used in the string
    // constants below
    
    ERROR(null, "error"),
    IDENTIFIER(null, "identifier"),

    // C/C++ keywords
    ASM("asm", "keyword-directive"), // gcc and C++
    AUTO("auto", "keyword"), 
    BOOL("bool", "keyword"), // C++
    BREAK("break", "keyword-directive"),
    CASE("case", "keyword-directive"),
    CATCH("catch", "keyword-directive"), //C++
    CHAR("char", "keyword"),
    CLASS("class", "keyword"), //C++
    CONST("const", "keyword"),
    CONST_CAST("const_cast", "keyword"), // C++
    CONTINUE("continue", "keyword-directive"),
    DEFAULT("default", "keyword-directive"),
    DELETE("delete", "keyword"), // C++
    DO("do", "keyword-directive"),
    DOUBLE("double", "keyword"),
    DYNAMIC_CAST("dynamic_cast", "keyword"), // C++
    ELSE("else", "keyword-directive"),
    ENUM("enum", "keyword"),
    EXPLICIT("explicit", "keyword"), // C++
    EXPORT("export", "keyword"), // C++
    EXTERN("extern", "keyword"),
    FINALLY("finally", "keyword-directive"), //C++
    FLOAT("float", "keyword"),
    FOR("for", "keyword-directive"),
    FRIEND("friend", "keyword"), // C++
    GOTO("goto", "keyword-directive"),
    IF("if", "keyword-directive"),
    INLINE("inline", "keyword"), // now in C also
    INT("int", "keyword"),
    LONG("long", "keyword"),
    MUTABLE("mutable", "keyword"), // C++
    NAMESPACE("namespace", "keyword"), //C++
    NEW("new", "keyword"), //C++
    OPERATOR("operator", "keyword"), // C++
    PRIVATE("private", "keyword"), //C++
    PROTECTED("protected", "keyword"), //C++
    PUBLIC("public", "keyword"), // C++
    REGISTER("register", "keyword"),
    REINTERPRET_CAST("reinterpret_cast", "keyword"), //C++
    RESTRICT("restrict", "keyword"), // C
    RETURN("return", "keyword-directive"),
    SHORT("short", "keyword"),
    SIZNED("signed", "keyword"),
    SIZEOF("sizeof", "keyword"),
    STATIC("static", "keyword"),
    STATIC_CAST("static_cast", "keyword"), // C++
    STRUCT("struct", "keyword"),
    SWITCH("switch", "keyword-directive"),
    TEMPLATE("template", "keyword"), //C++
    THIS("this", "keyword"), // C++
    THROW("throw", "keyword-directive"), //C++
    TRY("try", "keyword-directive"), // C++
    TYPEDEF("typedef", "keyword"),
    TYPEID("typeid", "keyword"), //C++
    TYPENAME("typename", "keyword"), //C++
    TYPEOF("typeof", "keyword"), // gcc, C++
    UNION("union", "keyword"),
    UNSIGNED("unsigned", "keyword"),
    USING("using", "keyword"), //C++
    VIRTUAL("virtual", "keyword"), //C++
    VOID("void", "keyword"),
    VOLATILE("volatile", "keyword"),
    WCHAR_T("wchar_t", "keyword"), // C++
    WHILE("while", "keyword-directive"),
    _BOOL("_Bool", "keyword"), // C 
    _COMPLEX("_Complex", "keyword"), // C
    _IMAGINARY("_Imaginary", "keyword"), // C
    
    INT_LITERAL(null, "number"),
    LONG_LITERAL(null, "number"),
    FLOAT_LITERAL(null, "number"),
    DOUBLE_LITERAL(null, "number"),
    UNSIGNED_LITERAL(null, "number"),
    CHAR_LITERAL(null, "character"),
    STRING_LITERAL(null, "string"),
    
    TRUE("true", "literal"), // C++
    FALSE("false", "literal"), // C++
    NULL("null", "literal"),
    
    LPAREN("(", "separator"),
    RPAREN(")", "separator"),
    LBRACE("{", "separator"),
    RBRACE("}", "separator"),
    LBRACKET("[", "separator"),
    RBRACKET("]", "separator"),
    SEMICOLON(";", "separator"),
    COMMA(",", "separator"),
    DOT(".", "separator"),
    DOTMBR(".*", "separator"),
    SCOPE("::", "separator"),
    ARROW("->", "separator"),
    ARROWMBR("->*", "separator"),
    
    EQ("=", "operator"),
    GT(">", "operator"),
    LT("<", "operator"),
    NOT("!", "operator"),
    TILDE("~", "operator"),
    QUESTION("?", "operator"),
    COLON(":", "operator"),
    EQEQ("==", "operator"),
    LTEQ("<=", "operator"),
    GTEQ(">=", "operator"),
    NOTEQ("!=","operator"),
    AMPAMP("&&", "operator"),
    BARBAR("||", "operator"),
    PLUSPLUS("++", "operator"),
    MINUSMINUS("--","operator"),
    PLUS("+", "operator"),
    MINUS("-", "operator"),
    STAR("*", "operator"),
    SLASH("/", "operator"),
    AMP("&", "operator"),
    BAR("|", "operator"),
    CARET("^", "operator"),
    PERCENT("%", "operator"),
    LTLT("<<", "operator"),
    GTGT(">>", "operator"),
    PLUSEQ("+=", "operator"),
    MINUSEQ("-=", "operator"),
    STAREQ("*=", "operator"),
    SLASHEQ("/=", "operator"),
    AMPEQ("&=", "operator"),
    BAREQ("|=", "operator"),
    CARETEQ("^=", "operator"),
    PERCENTEQ("%=", "operator"),
    LTLTEQ("<<=", "operator"),
    GTGTEQ(">>=", "operator"),
    
    ELLIPSIS("...", "special"),
    AT("@", "special"),
    DOLLAR("$", "special"),
    SHARP("#", "special"),
    DBL_SHARP("##", "special"),
    BACK_SLASH("\\", "special"),
            
    WHITESPACE(null, "whitespace"), // all spaces except new line
    ESCAPED_LINE(null, "whitespace"), // line escape with \
    NEW_LINE(null, "whitespace"), // new line \n or \r
    LINE_COMMENT(null, "comment"),
    BLOCK_COMMENT(null, "comment"),
    DOXYGEN_COMMENT(null, "comment"),
    
    // Prerpocessor 
    //   - on top level
    PREPROCESSOR_DIRECTIVE(null, "preprocessor"),
    //   - tokens
    PREPROCESSOR_START("#", "preprocessor"),
    PREPROCESSOR_IF("if", "preprocessor-keyword-directive"),
    PREPROCESSOR_IFDEF("ifdef", "preprocessor-keyword-directive"),
    PREPROCESSOR_IFNDEF("ifndef", "preprocessor-keyword-directive"),
    PREPROCESSOR_ELSE("else", "preprocessor-keyword-directive"),
    PREPROCESSOR_ELIF("elif", "preprocessor-keyword-directive"),
    PREPROCESSOR_ENDIF("endif", "preprocessor-keyword-directive"),
    PREPROCESSOR_DEFINE("define", "preprocessor-keyword-directive"),
    PREPROCESSOR_UNDEF("undef", "preprocessor-keyword-directive"),
    PREPROCESSOR_INCLUDE("include", "preprocessor-keyword-directive"),
    PREPROCESSOR_INCLUDE_NEXT("include_next", "preprocessor-keyword-directive"),
    PREPROCESSOR_LINE("line", "preprocessor-keyword-directive"),
    PREPROCESSOR_PRAGMA("pragma", "preprocessor-keyword-directive"),
    PREPROCESSOR_WARNING("warning", "preprocessor-keyword-directive"),
    PREPROCESSOR_ERROR("error", "preprocessor-keyword-directive"),
    PREPROCESSOR_DEFINED("defined", "preprocessor-keyword"),
    
    PREPROCESSOR_USER_INCLUDE(null, "preprocessor-user-include-literal"),
    PREPROCESSOR_SYS_INCLUDE(null, "preprocessor-system-include-literal"),
    PREPROCESSOR_IDENTIFIER(null, "preprocessor-identifier"),
    
    // Errors
    INVALID_COMMENT_END("*/", "error"),
    FLOAT_LITERAL_INVALID(null, "number");    
    
    // make sure string names are the same used in the tokenIds above
    public static final String WHITESPACE_CATEGORY = "whitespace"; // NOI18N
    public static final String COMMENT_CATEGORY = "comment"; // NOI18N
    public static final String KEYWORD_CATEGORY = "keyword"; // NOI18N
    public static final String KEYWORD_DIRECTIVE_CATEGORY = "keyword-directive"; // NOI18N
    public static final String ERROR_CATEGORY = "error"; // NOI18N
    public static final String NUMBER_CATEGORY = "number"; // NOI18N
    public static final String LITERAL_CATEGORY = "literal"; // NOI18N
    public static final String CHAR_CATEGORY = "character"; // NOI18N
    public static final String STRING_CATEGORY = "string"; // NOI18N
    public static final String SEPARATOR_CATEGORY = "separator"; // NOI18N
    public static final String OPERATOR_CATEGORY = "operator"; // NOI18N
    public static final String SPECIAL_CATEGORY = "special"; // NOI18N
    public static final String PREPROCESSOR_CATEGORY = "preprocessor"; // NOI18N
    public static final String PREPROCESSOR_KEYWORD_CATEGORY = "preprocessor-keyword"; // NOI18N
    public static final String PREPROCESSOR_KEYWORD_DIRECTIVE_CATEGORY = "preprocessor-keyword-directive"; // NOI18N
    public static final String PREPROCESSOR_IDENTIFIER_CATEGORY = "preprocessor-identifier"; // NOI18N
    public static final String PREPROCESSOR_USER_INCLUDE_CATEGORY = "preprocessor-user-include-literal"; // NOI18N
    public static final String PREPROCESSOR_SYS_INCLUDE_CATEGORY = "preprocessor-system-include-literal"; // NOI18N
  
    private final String fixedText;

    private final String primaryCategory;

    CppTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<CppTokenId> languageC;
    private static final Language<CppTokenId> languageCpp;
    private static final Language<CppTokenId> languagePreproc;
    
    static {
        languageC = CppHierarchy.createCLanguage();
        languageCpp = CppHierarchy.createCppLanguage();
        languagePreproc = CppHierarchy.createPreprocLanguage();
    }

    public static Language<CppTokenId> languageC() {
        return languageC;
    }

    public static Language<CppTokenId> languageCpp() {
        return languageCpp;
    }
    
    public static Language<CppTokenId> languagePreproc() {
        return languagePreproc;
    }

    private static final class CppHierarchy extends LanguageHierarchy<CppTokenId> {
        private final boolean cpp;
        private final boolean preproc;
        private CppHierarchy(boolean cpp, boolean preproc) {
            this.cpp = cpp;
            this.preproc = preproc;
        }
        
        private static Language<CppTokenId> createCppLanguage() {
            return new CppHierarchy(true, false).language();
        }
        
        private static Language<CppTokenId> createCLanguage() {
            return new CppHierarchy(false, false).language();
        }
        
        private static Language<CppTokenId> createPreprocLanguage() {
            return new CppHierarchy(false/*meaning less*/, true).language();
        }
        
        @Override
        protected String mimeType() {
            if (this.preproc) {
                return CndLexerUtilities.PREPROC_MIME_TYPE;
            } else {
                return this.cpp ? CndLexerUtilities.CPLUSPLUS_MIME_TYPE : CndLexerUtilities.C_MIME_TYPE;
            }
        }

        @Override
        protected Collection<CppTokenId> createTokenIds() {
            return EnumSet.allOf(CppTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<CppTokenId>> createTokenCategories() {
            Map<String,Collection<CppTokenId>> cats = new HashMap<String,Collection<CppTokenId>>();
            // Additional literals being a lexical error
            cats.put(ERROR_CATEGORY, EnumSet.of(
                CppTokenId.FLOAT_LITERAL_INVALID
            ));
            // Literals category
            EnumSet<CppTokenId> l = EnumSet.of(
                CppTokenId.INT_LITERAL,
                CppTokenId.LONG_LITERAL,
                CppTokenId.FLOAT_LITERAL,
                CppTokenId.DOUBLE_LITERAL,
                CppTokenId.UNSIGNED_LITERAL,
                CppTokenId.CHAR_LITERAL,
                CppTokenId.STRING_LITERAL
            );
            cats.put(LITERAL_CATEGORY, l);

            // Preprocessor category
//            EnumSet<CppTokenId> p = EnumSet.of(
//                CppTokenId.PREPROCESSOR_DEFINE,
//                CppTokenId.PREPROCESSOR_DEFINED,
//                CppTokenId.PREPROCESSOR_DIRECTIVE,
//                CppTokenId.LONG_LITERAL,
//                CppTokenId.FLOAT_LITERAL,
//                CppTokenId.DOUBLE_LITERAL,
//                CppTokenId.UNSIGNED_LITERAL,
//                CppTokenId.CHAR_LITERAL,
//                CppTokenId.STRING_LITERAL
//            );
//            cats.put("preprocessor", p);            
            return cats;
        }

        @Override
        protected Lexer<CppTokenId> createLexer(LexerRestartInfo<CppTokenId> info) {
            if (this.preproc) {
                return new PreprocLexer(CndLexerUtilities.getDefatultFilter(true), info);
            } else {
                return new CppLexer(CndLexerUtilities.getDefatultFilter(this.cpp), info);
            }
        }

        @Override
        protected LanguageEmbedding<?> embedding(
        Token<CppTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            // Test language embedding in the block comment and string literal
            switch (token.id()) {
                case DOXYGEN_COMMENT:
                    return LanguageEmbedding.create(DoxygenTokenId.language(), 3,
                            (token.partType() == PartType.COMPLETE) ? 2 : 0);
                case STRING_LITERAL:
                    return LanguageEmbedding.create(CppStringTokenId.languageDouble(), 1,
                            (token.partType() == PartType.COMPLETE) ? 1 : 0);
                case CHAR_LITERAL:
                    return LanguageEmbedding.create(CppStringTokenId.languageSingle(), 1,
                            (token.partType() == PartType.COMPLETE) ? 1 : 0);
                case PREPROCESSOR_DIRECTIVE:
                    return LanguageEmbedding.create(languagePreproc, 0, 0);
            }
            return null; // No embedding
        }
    }
}
