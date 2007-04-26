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
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Token identifications of the simple plain language.
 *
 * @author mmetelka
 */
public enum TestSaveTokensInLATokenId implements TokenId {
    
    A, // matches "a"; also checks for "b" and "c"
    B, // matches "b"; also checks for "c"
    C, // matches "c"
    TEXT; // other text

    TestSaveTokensInLATokenId() {
    }

    public String primaryCategory() {
        return null;
    }

    private  static final Language<TestSaveTokensInLATokenId> language
    = new LanguageHierarchy<TestSaveTokensInLATokenId>() {

        @Override
        protected Collection<TestSaveTokensInLATokenId> createTokenIds() {
            return EnumSet.allOf(TestSaveTokensInLATokenId.class);
        }
        
        @Override
        public Lexer<TestSaveTokensInLATokenId> createLexer(LexerRestartInfo<TestSaveTokensInLATokenId> info) {
            return new LexerImpl(info);
        }

        @Override
        protected LanguageEmbedding<? extends TokenId> embedding(
        Token<TestSaveTokensInLATokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }

        @Override
        protected String mimeType() {
            return "text/x-simple-plain";
        }
        
    }.language();

    public static Language<TestSaveTokensInLATokenId> language() {
        return language;
    }

    private static final class LexerImpl implements Lexer<TestSaveTokensInLATokenId> {

        private static final int EOF = LexerInput.EOF;

        private LexerInput input;

        private TokenFactory<TestSaveTokensInLATokenId> tokenFactory;

        public LexerImpl(LexerRestartInfo<TestSaveTokensInLATokenId> info) {
            this.input = info.input();
            this.tokenFactory = info.tokenFactory();
            assert (info.state() == null); // passed argument always null
        }

        public Object state() {
            return null;
        }

        public Token<TestSaveTokensInLATokenId> nextToken() {
            int ch = input.read();
            switch (ch) {
                case 'a': // check for 'b' and 'c'
                    if (input.read() == 'b') {
                        if (input.read() == 'c') { // just check for "c"
                        }
                        input.backup(1);
                    }
                    input.backup(1);
                    return tokenFactory.createToken(A);

                case 'b':
                    if (input.read() == 'c') { // just check for "c"
                    }
                    input.backup(1);
                    return tokenFactory.createToken(B);
                    
                case 'c':
                    return tokenFactory.createToken(C);

                case EOF:
                    return null;

                default:
                    return tokenFactory.createToken(TEXT);
            }
        }

        private Token<TestSaveTokensInLATokenId> token(TestSaveTokensInLATokenId id) {
            return tokenFactory.createToken(id);
        }

        public void release() {
        }

    }

}
