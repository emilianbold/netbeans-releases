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
    
    ERROR(null, "error"), // NOI18N
    IDENTIFIER(null, "identifier"), // NOI18N

    // C/C++ keywords
    ASM("asm", "keyword-directive"), // NOI18N // gcc and C++
    AUTO("auto", "keyword"), // NOI18N 
    BOOL("bool", "keyword"), // NOI18N // C++
    BREAK("break", "keyword-directive"), // NOI18N
    CASE("case", "keyword-directive"), // NOI18N
    CATCH("catch", "keyword-directive"), // NOI18N //C++
    CHAR("char", "keyword"), // NOI18N
    CLASS("class", "keyword"), // NOI18N //C++
    CONST("const", "keyword"), // NOI18N
    CONST_CAST("const_cast", "keyword"), // NOI18N // C++
    CONTINUE("continue", "keyword-directive"), // NOI18N
    DEFAULT("default", "keyword-directive"), // NOI18N
    DELETE("delete", "keyword"), // NOI18N // C++
    DO("do", "keyword-directive"), // NOI18N
    DOUBLE("double", "keyword"), // NOI18N
    DYNAMIC_CAST("dynamic_cast", "keyword"), // NOI18N // C++
    ELSE("else", "keyword-directive"), // NOI18N
    ENUM("enum", "keyword"), // NOI18N
    EXPLICIT("explicit", "keyword"), // NOI18N // C++
    EXPORT("export", "keyword"), // NOI18N // C++
    EXTERN("extern", "keyword"), // NOI18N
    FINALLY("finally", "keyword-directive"), // NOI18N //C++
    FLOAT("float", "keyword"), // NOI18N
    FOR("for", "keyword-directive"), // NOI18N
    FRIEND("friend", "keyword"), // NOI18N // C++
    GOTO("goto", "keyword-directive"), // NOI18N
    IF("if", "keyword-directive"), // NOI18N
    INLINE("inline", "keyword"), // NOI18N // now in C also
    INT("int", "keyword"), // NOI18N
    LONG("long", "keyword"), // NOI18N
    MUTABLE("mutable", "keyword"), // NOI18N // C++
    NAMESPACE("namespace", "keyword"), // NOI18N //C++
    NEW("new", "keyword"), // NOI18N //C++
    OPERATOR("operator", "keyword"), // NOI18N // C++
    PRIVATE("private", "keyword"), // NOI18N //C++
    PROTECTED("protected", "keyword"), // NOI18N //C++
    PUBLIC("public", "keyword"), // NOI18N // C++
    REGISTER("register", "keyword"), // NOI18N
    REINTERPRET_CAST("reinterpret_cast", "keyword"), // NOI18N //C++
    RESTRICT("restrict", "keyword"), // NOI18N // C
    RETURN("return", "keyword-directive"), // NOI18N
    SHORT("short", "keyword"), // NOI18N
    SIZNED("signed", "keyword"), // NOI18N
    SIZEOF("sizeof", "keyword"), // NOI18N
    STATIC("static", "keyword"), // NOI18N
    STATIC_CAST("static_cast", "keyword"), // NOI18N // C++
    STRUCT("struct", "keyword"), // NOI18N
    SWITCH("switch", "keyword-directive"), // NOI18N
    TEMPLATE("template", "keyword"), // NOI18N //C++
    THIS("this", "keyword"), // NOI18N // C++
    THROW("throw", "keyword-directive"), // NOI18N //C++
    TRY("try", "keyword-directive"), // NOI18N // C++
    TYPEDEF("typedef", "keyword"), // NOI18N
    TYPEID("typeid", "keyword"), // NOI18N //C++
    TYPENAME("typename", "keyword"), // NOI18N //C++
    TYPEOF("typeof", "keyword"), // NOI18N // gcc, C++
    UNION("union", "keyword"), // NOI18N
    UNSIGNED("unsigned", "keyword"), // NOI18N
    USING("using", "keyword"), // NOI18N //C++
    VIRTUAL("virtual", "keyword"), // NOI18N //C++
    VOID("void", "keyword"), // NOI18N
    VOLATILE("volatile", "keyword"), // NOI18N
    WCHAR_T("wchar_t", "keyword"), // NOI18N // C++
    WHILE("while", "keyword-directive"), // NOI18N
    _BOOL("_Bool", "keyword"), // NOI18N // C 
    _COMPLEX("_Complex", "keyword"), // NOI18N // C
    _IMAGINARY("_Imaginary", "keyword"), // NOI18N // C
    
    INT_LITERAL(null, "number"), // NOI18N
    LONG_LITERAL(null, "number"), // NOI18N
    FLOAT_LITERAL(null, "number"), // NOI18N
    DOUBLE_LITERAL(null, "number"), // NOI18N
    UNSIGNED_LITERAL(null, "number"), // NOI18N
    CHAR_LITERAL(null, "character"), // NOI18N
    STRING_LITERAL(null, "string"), // NOI18N
    
    TRUE("true", "literal"), // NOI18N // C++
    FALSE("false", "literal"), // NOI18N // C++
    NULL("null", "literal"), // NOI18N
    
    LPAREN("(", "separator"), // NOI18N
    RPAREN(")", "separator"), // NOI18N
    LBRACE("{", "separator"), // NOI18N
    RBRACE("}", "separator"), // NOI18N
    LBRACKET("[", "separator"), // NOI18N
    RBRACKET("]", "separator"), // NOI18N
    SEMICOLON(";", "separator"), // NOI18N
    COMMA(",", "separator"), // NOI18N
    DOT(".", "separator"), // NOI18N
    DOTMBR(".*", "separator"), // NOI18N
    SCOPE("::", "separator"), // NOI18N
    ARROW("->", "separator"), // NOI18N
    ARROWMBR("->*", "separator"), // NOI18N
    
    EQ("=", "operator"), // NOI18N
    GT(">", "operator"), // NOI18N
    LT("<", "operator"), // NOI18N
    NOT("!", "operator"), // NOI18N
    TILDE("~", "operator"), // NOI18N
    QUESTION("?", "operator"), // NOI18N
    COLON(":", "operator"), // NOI18N
    EQEQ("==", "operator"), // NOI18N
    LTEQ("<=", "operator"), // NOI18N
    GTEQ(">=", "operator"), // NOI18N
    NOTEQ("!=","operator"), // NOI18N
    AMPAMP("&&", "operator"), // NOI18N
    BARBAR("||", "operator"), // NOI18N
    PLUSPLUS("++", "operator"), // NOI18N
    MINUSMINUS("--","operator"), // NOI18N
    PLUS("+", "operator"), // NOI18N
    MINUS("-", "operator"), // NOI18N
    STAR("*", "operator"), // NOI18N
    SLASH("/", "operator"), // NOI18N
    AMP("&", "operator"), // NOI18N
    BAR("|", "operator"), // NOI18N
    CARET("^", "operator"), // NOI18N
    PERCENT("%", "operator"), // NOI18N
    LTLT("<<", "operator"), // NOI18N
    GTGT(">>", "operator"), // NOI18N
    PLUSEQ("+=", "operator"), // NOI18N
    MINUSEQ("-=", "operator"), // NOI18N
    STAREQ("*=", "operator"), // NOI18N
    SLASHEQ("/=", "operator"), // NOI18N
    AMPEQ("&=", "operator"), // NOI18N
    BAREQ("|=", "operator"), // NOI18N
    CARETEQ("^=", "operator"), // NOI18N
    PERCENTEQ("%=", "operator"), // NOI18N
    LTLTEQ("<<=", "operator"), // NOI18N
    GTGTEQ(">>=", "operator"), // NOI18N
    
    ELLIPSIS("...", "special"), // NOI18N
    AT("@", "special"), // NOI18N
    DOLLAR("$", "special"), // NOI18N
    SHARP("#", "special"), // NOI18N
    DBL_SHARP("##", "special"), // NOI18N
    BACK_SLASH("\\", "special"), // NOI18N
            
    WHITESPACE(null, "whitespace"), // NOI18N // all spaces except new line
    ESCAPED_LINE(null, "whitespace"), // NOI18N // line escape with \
    ESCAPED_WHITESPACE(null, "whitespace"), // NOI18N // whitespace escape with \ inside it
    NEW_LINE(null, "whitespace"), // NOI18N // new line \n or \r
    LINE_COMMENT(null, "comment"), // NOI18N
    BLOCK_COMMENT(null, "comment"), // NOI18N
    DOXYGEN_COMMENT(null, "comment"), // NOI18N
    
    // Prerpocessor 
    //   - on top level
    PREPROCESSOR_DIRECTIVE(null, "preprocessor"), // NOI18N
    //   - tokens
    PREPROCESSOR_START("#", "preprocessor"), // NOI18N
    PREPROCESSOR_IF("if", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_IFDEF("ifdef", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_IFNDEF("ifndef", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_ELSE("else", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_ELIF("elif", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_ENDIF("endif", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_DEFINE("define", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_UNDEF("undef", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_INCLUDE("include", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_INCLUDE_NEXT("include_next", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_LINE("line", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_IDENT("ident", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_PRAGMA("pragma", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_WARNING("warning", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_ERROR("error", "preprocessor-keyword-directive"), // NOI18N
    PREPROCESSOR_DEFINED("defined", "preprocessor-keyword"), // NOI18N
    
    PREPROCESSOR_USER_INCLUDE(null, "preprocessor-user-include-literal"), // NOI18N
    PREPROCESSOR_SYS_INCLUDE(null, "preprocessor-system-include-literal"), // NOI18N
    PREPROCESSOR_IDENTIFIER(null, "preprocessor-identifier"), // NOI18N
    
    // Errors
    INVALID_COMMENT_END("*/", "error"), // NOI18N
    FLOAT_LITERAL_INVALID(null, "number"); // NOI18N
    
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
