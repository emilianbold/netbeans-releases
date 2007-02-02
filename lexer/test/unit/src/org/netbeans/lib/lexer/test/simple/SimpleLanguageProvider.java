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

package org.netbeans.lib.lexer.test.simple;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.*;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LanguageProvider;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author vita
 */
public class SimpleLanguageProvider extends LanguageProvider {
    
    private static SimpleLanguageProvider instance = null;
    
    public static void fireLanguageChange() {
        assert instance != null : "There is no SimpleLanguageProvider instance.";
        instance.firePropertyChange(PROP_LANGUAGE);
    }
    
    public static void fireTokenLanguageChange() {
        assert instance != null : "There is no SimpleLanguageProvider instance.";
        instance.firePropertyChange(PROP_EMBEDDED_LANGUAGE);
    }
    
    /** Creates a new instance of SimpleLanguageProvider */
    public SimpleLanguageProvider() {
        assert instance == null : "Multiple instances of DummyLanguageProvider detected";
        instance = this;
    }

    public Language<? extends TokenId> findLanguage(String mimePath) {
        if (LanguageManagerTest.MIME_TYPE_KNOWN.equals(mimePath)) {
            return new LH().language();
        } else {
            return null;
        }
    }

    public LanguageEmbedding<? extends TokenId> findLanguageEmbedding(
    Token<? extends TokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
        if ("text/x-simple-plain".equals(languagePath.mimePath()) && token.id().name().equals("WORD")) {
            return LanguageEmbedding.create(SimpleCharTokenId.language(), 0, 0);
        } else {
            return null;
        }
    }
    
    private static final class LH extends LanguageHierarchy<TokenId> {
        protected Collection<TokenId> createTokenIds() {
            return Collections.emptyList();
        }

        protected Lexer<TokenId> createLexer(LexerRestartInfo<TokenId> info) {
            return null;
        }

        protected String mimeType() {
            return LanguageManagerTest.MIME_TYPE_KNOWN;
        }

    } // End of LD class
}
