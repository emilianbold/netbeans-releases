/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.el.lexer.api;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.el.lexer.ELLexer;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token IDs of Expression Language
 *
 * @author Petr Pisl
 * @author Marek.Fukala@Sun.COM
 */
public enum ELTokenId implements TokenId {
    
    //operators
    LT("<", ELTokenCategories.OPERATORS.categoryName),
    GT(">", ELTokenCategories.OPERATORS.categoryName),
    DOT(".", ELTokenCategories.OPERATORS.categoryName),
    COMMA(",", ELTokenCategories.OPERATORS.categoryName),
    QUESTION("?", ELTokenCategories.OPERATORS.categoryName),
    PLUS("+", ELTokenCategories.OPERATORS.categoryName),
    MINUS("-", ELTokenCategories.OPERATORS.categoryName),
    MUL("*", ELTokenCategories.OPERATORS.categoryName),
    DIV("/", ELTokenCategories.OPERATORS.categoryName),
    MOD("%", ELTokenCategories.OPERATORS.categoryName),
    EQ_EQ("==", ELTokenCategories.OPERATORS.categoryName),
    LT_EQ("<=", ELTokenCategories.OPERATORS.categoryName),
    GT_EQ(">=", ELTokenCategories.OPERATORS.categoryName),
    NOT_EQ("!=", ELTokenCategories.OPERATORS.categoryName),
    AND_AND("&&", ELTokenCategories.OPERATORS.categoryName),
    OR_OR("||", ELTokenCategories.OPERATORS.categoryName),
    COLON(":", ELTokenCategories.OPERATORS.categoryName),
    NOT("!", ELTokenCategories.OPERATORS.categoryName),
    LPAREN("(", ELTokenCategories.OPERATORS.categoryName),
    RPAREN(")", ELTokenCategories.OPERATORS.categoryName),
    LBRACKET("[", ELTokenCategories.OPERATORS.categoryName),
    RBRACKET("]", ELTokenCategories.OPERATORS.categoryName),
    
    //keywords
    AND_KEYWORD("and", ELTokenCategories.KEYWORDS.categoryName),
    DIV_KEYWORD("div", ELTokenCategories.KEYWORDS.categoryName),
    EMPTY_KEYWORD("empty", ELTokenCategories.KEYWORDS.categoryName),
    EQ_KEYWORD("eq", ELTokenCategories.KEYWORDS.categoryName),
    FALSE_KEYWORD("false", ELTokenCategories.KEYWORDS.categoryName),
    GE_KEYWORD("ge", ELTokenCategories.KEYWORDS.categoryName),
    GT_KEYWORD("gt", ELTokenCategories.KEYWORDS.categoryName),
    INSTANCEOF_KEYWORD("instanceof", ELTokenCategories.KEYWORDS.categoryName),
    LE_KEYWORD("le", ELTokenCategories.KEYWORDS.categoryName),
    LT_KEYWORD("lt", ELTokenCategories.KEYWORDS.categoryName),
    MOD_KEYWORD("mod", ELTokenCategories.KEYWORDS.categoryName),
    NE_KEYWORD("ne", ELTokenCategories.KEYWORDS.categoryName),
    NOT_KEYWORD("not", ELTokenCategories.KEYWORDS.categoryName),
    NULL_KEYWORD("null", ELTokenCategories.KEYWORDS.categoryName),
    OR_KEYWORD("or", ELTokenCategories.KEYWORDS.categoryName),
    TRUE_KEYWORD("true", ELTokenCategories.KEYWORDS.categoryName),
    
    //literals
    WHITESPACE(null, "el-whitespace"),
    EOL("\n", "el-eol"),
    STRING_LITERAL(null, "el-string"),
    TAG_LIB_PREFIX(null, "el-tag-lib-prefix"),
    IDENTIFIER(null, "el-identifier"),
    CHAR_LITERAL(null, "el-char-literal"),
    
    //numeric literals
    /** Java integer literal e.g. 1234 */
    INT_LITERAL(null, "el-int-literal"),
    /** Java long literal e.g. 12L */
    LONG_LITERAL(null, "el-long-literal"),
    /** Java hexadecimal literal e.g. 0x5a */
    HEX_LITERAL(null, "el-hex-literal"),
    /** Java octal literal e.g. 0123 */
    OCTAL_LITERAL(null, "el-octal-literal"),
    /** Java float literal e.g. 1.5e+20f */
    FLOAT_LITERAL(null, "el-float-literal"),
    /** Java double literal e.g. 1.5e+20 */
    DOUBLE_LITERAL(null, "el-double-literal"),
    // Incomplete and error token-ids
    INVALID_OCTAL_LITERAL(null, "el-invalid-octal-literal"),
    INVALID_CHAR(null, "el-invalid-char");
    
    
    /** EL token categories enum.*/
    public static enum ELTokenCategories {
        
        /** Token category for EL keywords like and, false etc. */
        KEYWORDS("el-keyword"),
        /** Token category for EL operators like ==, => etc. */
        OPERATORS("el-operators"),
        /** Token category for EL numeric literals. */
        NUMERIC_LITERALS("el-numeric-literals"),
        /** Token category for EL errors. */
        ERRORS("el-error");
        
        private final String categoryName;
        
        ELTokenCategories(String categoryName) {
            this.categoryName = categoryName;
        }
        
    }
    
    private final String fixedText; // Used by lexer for production of flyweight tokens
    
    private final String primaryCategory;
    
    ELTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }
    
    /** Get fixed text of the token. */
    public String fixedText() {
        return fixedText;
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
    
    private static final Language<ELTokenId> language = new LanguageHierarchy<ELTokenId>() {
        @Override
        protected Collection<ELTokenId> createTokenIds() {
            return EnumSet.allOf(ELTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<ELTokenId>> createTokenCategories() {
            Map<String,Collection<ELTokenId>> cats = new HashMap<String,Collection<ELTokenId>>();
            
            cats.put(ELTokenCategories.NUMERIC_LITERALS.categoryName, EnumSet.of(
                    ELTokenId.INT_LITERAL,
                    ELTokenId.LONG_LITERAL,
                    ELTokenId.HEX_LITERAL,
                    ELTokenId.OCTAL_LITERAL,
                    ELTokenId.FLOAT_LITERAL,
                    ELTokenId.DOUBLE_LITERAL));
            
            cats.put(ELTokenCategories.ERRORS.categoryName, EnumSet.of(
                    ELTokenId.INVALID_OCTAL_LITERAL,
                    ELTokenId.INVALID_CHAR));
            
            return cats;
        }
        
        @Override
        protected Lexer<ELTokenId> createLexer(LexerRestartInfo<ELTokenId> info) {
            return new ELLexer(info);
        }
        
        @Override
        public LanguageEmbedding<? extends TokenId> embedding(
                Token<ELTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }
        
        @Override
        protected String mimeType() {
            return "text/x-el"; //???
        }
    }.language();
    
    /** Gets a LanguageDescription describing a set of token ids
     * that comprise the given language.
     *
     * @return non-null LanguageDescription
     */
    public static Language<ELTokenId> language() {
        return language;
    }
    
    
}
