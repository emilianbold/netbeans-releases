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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.java.lexer.JavaStringLexer;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids for java string language
 * (embedded in java string or character literals).
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public enum JavaStringTokenId implements TokenId {

    TEXT("string"),
    BACKSPACE("string-escape"),
    FORM_FEED("string-escape"),
    NEWLINE("string-escape"),
    CR("string-escape"),
    TAB("string-escape"),
    SINGLE_QUOTE("string-escape"),
    DOUBLE_QUOTE("string-escape"),
    BACKSLASH("string-escape"),
    OCTAL_ESCAPE("string-escape"),
    OCTAL_ESCAPE_INVALID("string-escape-invalid"),
    ESCAPE_SEQUENCE_INVALID("string-escape-invalid");

    private final String primaryCategory;

    JavaStringTokenId() {
        this(null);
    }

    JavaStringTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<JavaStringTokenId> language = new LanguageHierarchy<JavaStringTokenId>() {
        @Override
        protected Collection<JavaStringTokenId> createTokenIds() {
            return EnumSet.allOf(JavaStringTokenId.class);
        }
        
        @Override
        protected Map<String, Collection<JavaStringTokenId>> createTokenCategories() {
            return null; // no extra categories
        }

        @Override
        protected Lexer<JavaStringTokenId> createLexer(LexerRestartInfo<JavaStringTokenId> info) {
            return new JavaStringLexer(info);
        }

        @Override
        protected String mimeType() {
            return "text/x-java-string";
        }
    }.language();

    public static Language<JavaStringTokenId> language() {
        return language;
    }

}
