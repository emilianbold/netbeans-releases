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

package org.netbeans.lib.lexer.test;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;

/**
 * Language provider for various lexer-related tests.
 * <br/>
 * For using it a "META-INF/services/org.netbeans.spi.lexer.LanguageProvider" file
 * should be creating containing a single line with "org.netbeans.lib.lexer.test.TestLanguageProvider".
 * <br/>
 * Then the tests should register their test languages into it.
 *
 * @author Miloslav Metelka
 */
public class TestLanguageProvider extends LanguageProvider {
    
    private static TestLanguageProvider INSTANCE;
    
    private static Map<String,Language<?>> mime2language = new HashMap<String,Language<?>>();
    
    private static Map<String,Map<TokenId,LanguageEmbedding<?>>> mime2embeddings
            = new HashMap<String,Map<TokenId,LanguageEmbedding<?>>>();
    
    private static Object LOCK = new String("TestLanguageProvider.LOCK");
    
    public static void register(Language language) {
        register(language.mimeType(), language);
    }

    public static void register(String mimePath, Language language) {
        checkInstanceExists();
        synchronized (LOCK) {
            mime2language.put(mimePath, language);
        }
        fireChange();
    }
    
    public static void registerEmbedding(String mimePath, TokenId id,
    Language<?> language, int startSkipLength, int endSkipLength, boolean joinSections) {
        registerEmbedding(mimePath, id, LanguageEmbedding.create(language, startSkipLength, endSkipLength, joinSections));
    }

    public static void registerEmbedding(String mimePath, TokenId id, LanguageEmbedding<?> embedding) {
        checkInstanceExists();
        synchronized (LOCK) {
            Map<TokenId,LanguageEmbedding<?>> id2embedding = mime2embeddings.get(mimePath);
            if (id2embedding == null) {
                id2embedding = new HashMap<TokenId,LanguageEmbedding<?>>();
                mime2embeddings.put(mimePath, id2embedding);
            }
            id2embedding.put(id, embedding);
        }
        fireChange();
    }
    
    public static void fireChange() {
        checkInstanceExists();
        INSTANCE.firePropertyChange(PROP_LANGUAGE);
        INSTANCE.firePropertyChange(PROP_EMBEDDED_LANGUAGE);
    }
    
    public TestLanguageProvider() {
        assert (INSTANCE == null) : "More than one instance of this class prohibited";
        INSTANCE = this;
    }
    
    private static void checkInstanceExists() {
        if (INSTANCE == null)
            throw new IllegalStateException("No instance of created yet.");
    }

    public Language<? extends TokenId> findLanguage(String mimePath) {
        synchronized (LOCK) {
            return mime2language.get(mimePath);
        }
    }

    public LanguageEmbedding<? extends TokenId> findLanguageEmbedding(
    Token<? extends TokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
        Map<TokenId,LanguageEmbedding<?>> id2embedding = mime2embeddings.get(languagePath.mimePath());
        return (id2embedding != null) ? id2embedding.get(token.id()) : null;
    }
    
}
