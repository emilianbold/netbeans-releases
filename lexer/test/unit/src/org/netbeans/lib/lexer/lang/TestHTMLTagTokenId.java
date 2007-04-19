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

package org.netbeans.lib.lexer.lang;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids for simple javadoc language 
 * - copied from HTMLTagTokenId.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public enum TestHTMLTagTokenId implements TokenId {

    LT("lt"),
    TEXT("text"),
    GT("gt");

    private final String primaryCategory;

    TestHTMLTagTokenId() {
        this(null);
    }

    TestHTMLTagTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<TestHTMLTagTokenId> language = new LanguageHierarchy<TestHTMLTagTokenId>() {
        @Override
        protected Collection<TestHTMLTagTokenId> createTokenIds() {
            return EnumSet.allOf(TestHTMLTagTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<TestHTMLTagTokenId>> createTokenCategories() {
            return null; // no extra categories
        }

        @Override
        protected Lexer<TestHTMLTagTokenId> createLexer(LexerRestartInfo<TestHTMLTagTokenId> info) {
            return new TestHTMLTagLexer(info);
        }

        @Override
        protected String mimeType() {
            return "text/x-test-html-tag";
        }
    }.language();

    public static Language<TestHTMLTagTokenId> language() {
        return language;
    }

}
