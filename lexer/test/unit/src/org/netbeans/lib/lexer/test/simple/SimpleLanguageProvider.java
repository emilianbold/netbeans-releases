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

package org.netbeans.lib.lexer.test.simple;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguageDescription;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.lib.lexer.*;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LanguageProvider;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.TokenFactory;

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

    public LanguageDescription findLanguage(String mimePath) {
        if (LanguageManagerTest.MIME_TYPE_KNOWN.equals(mimePath)) {
            return new LH().language();
        } else {
            return null;
        }
    }

    public LanguageDescription findEmbeddedLanguage(LanguagePath tokenLanguage, Token token, InputAttributes inputAttributes) {
        if ("text/x-simple-plain".equals(tokenLanguage.mimePath()) && token.id().name().equals("WORD")) {
            return SimpleCharLanguage.description();
        } else {
            return null;
        }
    }
    
    private static final class LH extends LanguageHierarchy {
        protected Collection createTokenIds() {
            return Collections.EMPTY_LIST;
        }

        protected Lexer createLexer(LexerInput input, TokenFactory tokenFactory, Object state, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null;
        }

        protected String mimeType() {
            return LanguageManagerTest.MIME_TYPE_KNOWN;
        }

        protected LanguageEmbedding embedding(Token token, boolean tokenComplete, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null;
        }
        
    } // End of LD class
}
