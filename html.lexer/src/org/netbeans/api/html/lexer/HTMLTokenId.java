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
package org.netbeans.api.html.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.html.lexer.HTMLLexer;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids of HTML language
 *
 * @author Jan Lahoda, Miloslav Metelka, Marek Fukala
 */
public enum HTMLTokenId implements TokenId {
    
    /** HTML text */
    TEXT("html-text"), 
    /** HTML script e.g. javascript. */
    SCRIPT("html-script"), 
    /** Whitespace in a tag: <code> &lt;BODY" "bgcolor=red&gt;</code>. */
    WS("html-ws"),
    /** Error token - returned in various erroneous situations. */
    ERROR("html-error"),
    /** HTML open tag name: <code>&lt;"BODY"/&gt;</code>.*/
    TAG_OPEN("html-tag"),
    /** HTML close tag name: <code>&lt;/"BODY"&gt;</code>.*/
    TAG_CLOSE("html-tag"),
    /** HTML tag attribute name: <code> &lt;BODY "bgcolor"=red&gt;</code>.*/
    ARGUMENT("html-argument"),
    /** Equals sign in HTML tag: <code> &lt;BODY bgcolor"="red&gt;</code>.*/
    OPERATOR("html-operator"),
    /** Attribute value in HTML tag: <code> &lt;BODY bgcolor="red"&gt;</code>.*/
    VALUE("html-value"),
    /** HTML block comment: <code> &lt;!-- xxx --&gt; </code>.*/
    BLOCK_COMMENT("html-block-comment"),
    /** HTML/SGML comment.*/
    SGML_COMMENT("html-sgml-comment"),
    /** HTML/SGML declaration: <code> &lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"&gt; </code>.*/
    DECLARATION("html-sgml-declaration"),
    /** Character reference: <code> &amp;amp; </code>.*/
    CHARACTER("html-character"),
    /** End of line.*/
    EOL("html-text"),
    /** HTML open tag symbol: <code> "&lt;"BODY&gt; </code>.*/
    TAG_OPEN_SYMBOL("html-tag"),
    /** HTML close tag symbol: <code> "&lt;/"BODY&gt; </code>.*/
    TAG_CLOSE_SYMBOL("html-tag");
    
    private final String primaryCategory;

    HTMLTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    private static final Language<HTMLTokenId> language = new LanguageHierarchy<HTMLTokenId>() {
        @Override
        protected Collection<HTMLTokenId> createTokenIds() {
            return EnumSet.allOf(HTMLTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<HTMLTokenId>> createTokenCategories() {
            //Map<String,Collection<HTMLTokenId>> cats = new HashMap<String,Collection<HTMLTokenId>>();
            // Additional literals being a lexical error
            //cats.put("error", EnumSet.of());
            return null;
        }
        
        @Override
        protected Lexer<HTMLTokenId> createLexer(LexerRestartInfo<HTMLTokenId> info) {
            return new HTMLLexer(info);
        }
        
        @Override
        protected LanguageEmbedding embedding(
        Token<HTMLTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }
        
        @Override
        protected String mimeType() {
            return "text/html";
        }
    }.language();
    
    /** Gets a LanguageDescription describing a set of token ids
     * that comprise the given language.
     *
     * @return non-null LanguageDescription
     */
    public static Language<HTMLTokenId> language() {
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
