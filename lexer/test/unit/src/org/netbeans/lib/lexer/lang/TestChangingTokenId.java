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
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Language that changes dynamically to test Language.refresh().
 *
 * @author Miloslav Metelka
 */
public enum TestChangingTokenId implements TokenId {
    
    TEXT, // any text
    A; // "a"; added after change only

    private final String primaryCategory;

    TestChangingTokenId() {
        this(null);
    }

    TestChangingTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }
    
    private static boolean changed;

    private static Language<TestChangingTokenId> createLanguage() {
        return new LanguageHierarchy<TestChangingTokenId>() {
            @Override
            protected Collection<TestChangingTokenId> createTokenIds() {
                return changed
                        ? EnumSet.allOf(TestChangingTokenId.class)
                        : EnumSet.of(TEXT);
            }

            @Override
            protected Map<String,Collection<TestChangingTokenId>> createTokenCategories() {
                Map<String,Collection<TestChangingTokenId>> cats = null;
                if (changed) {
                    cats = new HashMap<String,Collection<TestChangingTokenId>>();
                    cats.put("test",EnumSet.of(A));
                }
                return cats;
            }

            @Override
            protected Lexer<TestChangingTokenId> createLexer(LexerRestartInfo<TestChangingTokenId> info) {
                return null;
            }

            @Override
            protected String mimeType() {
                return MIME_TYPE;
            }
        }.language();
    }

    private static Language<TestChangingTokenId> language;

    public static Language<TestChangingTokenId> language() {
        if (language == null)
            language = createLanguage();
        return language;
    }
    
    public static void change() {
        changed = true;
        language = null;
    }

    public static final String MIME_TYPE = "text/x-changing";
    
}
