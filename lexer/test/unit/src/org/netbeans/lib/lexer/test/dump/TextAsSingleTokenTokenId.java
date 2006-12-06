/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer.test.dump;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * @author mmetelka
 */
public enum TextAsSingleTokenTokenId implements TokenId {
    
    /** Token covering whole input till the end. */
    TEXT(null);
    
    private String primaryCategory;

    private TextAsSingleTokenTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }
    
    public String primaryCategory() {
        return primaryCategory;
    }
    
    private static final Language<TextAsSingleTokenTokenId> lang = new LanguageHierarchy<TextAsSingleTokenTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-eof-mark";
        }

        @Override
        protected Collection<TextAsSingleTokenTokenId> createTokenIds() {
            return EnumSet.allOf(TextAsSingleTokenTokenId.class);
        }

        @Override
        protected Lexer<TextAsSingleTokenTokenId> createLexer(LexerRestartInfo<TextAsSingleTokenTokenId> info) {
            return new TextAsSingleTokenLexer(info);
        }
        
    }.language();
    
    public static Language<TextAsSingleTokenTokenId> language() {
        return lang;
    }
    
}
