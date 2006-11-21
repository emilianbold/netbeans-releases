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
 * Token ids of Expression Language
 *
 * @author Petr Pisl
 * @author Marek Fukala
 */
public enum ELTokenId implements TokenId {
    
    //operators
    LT("<", "operator"),
    GT(">", "operator"),
    DOT(".", "operator"),
    COMMA(",", "operator"),
    QUESTION("?", "operator"),
    PLUS("+", "operator"),
    MINUS("-", "operator"),
    MUL("*", "operator"),
    DIV("/", "operator"),
    MOD("%", "operator"),
    EQ_EQ("==", "operator"),
    LT_EQ("<=", "operator"),
    GT_EQ(">=", "operator"),
    NOT_EQ("!=", "operator"),
    AND_AND("&&", "operator"),
    OR_OR("||", "operator"),
    COLON(":", "operator"),
    NOT("!", "operator"),
    LPAREN("(", "operator"),
    RPAREN(")", "operator"),
    LBRACKET("[", "operator"),
    RBRACKET("]", "operator"),
    
    //keywords
    AND_KEYWORD("and", "keyword"),
    DIV_KEYWORD("div", "keyword"),
    EMPTY_KEYWORD("empty", "keyword"),
    EQ_KEYWORD("eq", "keyword"),
    FALSE_KEYWORD("false", "keyword"),
    GE_KEYWORD("ge", "keyword"),
    GT_KEYWORD("gt", "keyword"),
    INSTANCEOF_KEYWORD("instanceof", "keyword"),
    LE_KEYWORD("le", "keyword"),
    LT_KEYWORD("lt", "keyword"),
    MOD_KEYWORD("mod", "keyword"),
    NE_KEYWORD("ne", "keyword"),
    NOT_KEYWORD("not", "keyword"),
    NULL_KEYWORD("null", "keyword"),
    OR_KEYWORD("or", "keyword"),
    TRUE_KEYWORD("true", "keyword"),
    
    //literals
    WHITESPACE(null, "whitespace"),
    EOL("\n", null),
    EL_DELIM(null, "el-delimiter"),
    STRING_LITERAL(null, "string"),
    TAG_LIB_PREFIX(null, "tag-lib-prefix"),
    IDENTIFIER(null, "identifier"),
    CHAR_LITERAL(null, "char-literal"),
    
    //numeric literals
    /** Java integer literal e.g. 1234 */
    INT_LITERAL(null, "int-literal"),
    /** Java long literal e.g. 12L */
    LONG_LITERAL(null, "long-literal"),
    /** Java hexadecimal literal e.g. 0x5a */
    HEX_LITERAL(null, "hex-literal"),
    /** Java octal literal e.g. 0123 */
    OCTAL_LITERAL(null, "octal-literal"),
    /** Java float literal e.g. 1.5e+20f */
    FLOAT_LITERAL(null, "float-literal"),
    /** Java double literal e.g. 1.5e+20 */
    DOUBLE_LITERAL(null, "double-literal"),
    // Incomplete and error token-ids
    INVALID_OCTAL_LITERAL(null, "invalid-octal-literal"),
    INVALID_CHAR(null, "invalid-char");
    
    
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
        protected Collection<ELTokenId> createTokenIds() {
            return EnumSet.allOf(ELTokenId.class);
        }
        
        protected Map<String,Collection<ELTokenId>> createTokenCategories() {
            Map<String,Collection<ELTokenId>> cats = new HashMap<String,Collection<ELTokenId>>();
            
            cats.put("numeric-literals", EnumSet.of(
                    ELTokenId.INT_LITERAL,
                    ELTokenId.LONG_LITERAL,
                    ELTokenId.HEX_LITERAL,
                    ELTokenId.OCTAL_LITERAL,
                    ELTokenId.FLOAT_LITERAL,
                    ELTokenId.DOUBLE_LITERAL));
            
            return cats;
        }
        
        public Lexer<ELTokenId> createLexer(LexerRestartInfo<ELTokenId> info) {
            return new ELLexer(info);
        }
        
        public LanguageEmbedding embedding(
                Token<ELTokenId> token, boolean tokenComplete,
                LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }
        
        public String mimeType() {
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
