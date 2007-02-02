/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
