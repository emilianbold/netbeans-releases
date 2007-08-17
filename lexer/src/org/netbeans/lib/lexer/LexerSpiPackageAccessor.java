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

package org.netbeans.lib.lexer;

import java.util.Collection;
import java.util.Map;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.CharPreprocessor;
import org.netbeans.spi.lexer.EmbeddingPresence;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.MutableTextInput;
import org.netbeans.spi.lexer.TokenFactory;
import org.netbeans.spi.lexer.TokenValidator;

/**
 * Accessor for the package-private functionality in org.netbeans.api.editor.fold.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class LexerSpiPackageAccessor {
    
    private static LexerSpiPackageAccessor INSTANCE;
    
    public static LexerSpiPackageAccessor get() {
        return INSTANCE;
    }

    /**
     * Register the accessor. The method can only be called once
     * - othewise it throws IllegalStateException.
     * 
     * @param accessor instance.
     */
    public static void register(LexerSpiPackageAccessor accessor) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already registered"); // NOI18N
        }
        INSTANCE = accessor;
    }

    public abstract <T extends TokenId> Collection<T> createTokenIds(LanguageHierarchy<T> languageHierarchy);

    public abstract <T extends TokenId> Map<String,Collection<T>> createTokenCategories(LanguageHierarchy<T> languageHierarchy);
    
    public abstract <T extends TokenId> Lexer<T> createLexer(LanguageHierarchy<T> languageHierarchy, LexerRestartInfo<T> info);
    
    public abstract <T extends TokenId> LexerRestartInfo<T> createLexerRestartInfo(
    LexerInput input, TokenFactory<T> tokenFactory, Object state,
    LanguagePath languagePath, InputAttributes inputAttributes);
    
    public abstract String mimeType(LanguageHierarchy<? extends TokenId> languageHierarchy);
    
    public abstract <T extends TokenId> LanguageEmbedding<? extends TokenId> embedding(
    LanguageHierarchy<T> languageHierarchy, Token<T> token,
    LanguagePath languagePath, InputAttributes inputAttributes);
    
    public abstract <T extends TokenId> EmbeddingPresence embeddingPresence(LanguageHierarchy<T> languageHierarchy, T id);
    
    public abstract <T extends TokenId> TokenValidator<T> createTokenValidator(LanguageHierarchy<T> languageHierarchy, T id);

    public abstract CharPreprocessor createCharPreprocessor(LanguageHierarchy<? extends TokenId> languageHierarchy);

    public abstract <T extends TokenId> boolean isRetainTokenText(LanguageHierarchy<T> languageHierarchy, T id);

    public abstract LexerInput createLexerInput(CharProvider charProvider);
    
    public abstract void init(CharPreprocessor preprocessor, CharPreprocessorOperation operation);
    
    public abstract void preprocessChar(CharPreprocessor preprocessor);
    
    public abstract Language<? extends TokenId> language(MutableTextInput<?> mti);
    
    public abstract <T extends TokenId> LanguageEmbedding<T> createLanguageEmbedding(
    Language<T> language, int startSkipLength, int endSkipLength, boolean joinSections);

    public abstract CharSequence text(MutableTextInput<?> mti);
    
    public abstract InputAttributes inputAttributes(MutableTextInput<?> mti);
    
    public abstract <I> I inputSource(MutableTextInput<I> mti);
    
    public abstract <T extends TokenId> TokenFactory<T> createTokenFactory(LexerInputOperation<T> lexerInputOperation);
    
}
