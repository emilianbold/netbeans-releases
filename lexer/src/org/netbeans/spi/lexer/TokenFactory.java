/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.spi.lexer;

import java.util.Set;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.lexer.LanguageOperation;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.TokenIdImpl;
import org.netbeans.lib.lexer.token.CustomTextToken;
import org.netbeans.lib.lexer.token.DefaultToken;
import org.netbeans.lib.lexer.token.ComplexToken;
import org.netbeans.lib.lexer.token.ComplexToken;
import org.netbeans.lib.lexer.token.PropertyToken;
import org.netbeans.lib.lexer.token.TextToken;

/**
 * Lexer should delegate all the token instances creation to this class.
 * <br/>
 * It's not allowed to create empty tokens.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenFactory<T extends TokenId> {

    /** Flag for additional correctness checks (may degrade performance). */
    private static final boolean testing = Boolean.getBoolean("netbeans.debug.lexer.test");

    /**
     * Token instance that should be returned by the lexer
     * if there is an active filtering of certain token ids
     * and the just recognized token-id should be skipped.
     */
    public static final Token SKIP_TOKEN
        = new TextToken<TokenId>(
            new TokenIdImpl("skip-token-id; special id of TokenFactory.SKIP_TOKEN; " + // NOI18N
                    " It should never be part of token sequence", 0, null), // NOI18N
            "" // empty skip token text NOI18N
        );
    
    private final LexerInputOperation<T> operation;
    
    TokenFactory(LexerInputOperation<T> operation) {
        this.operation = operation;
    }

    /**
     * Create token with token length corresponding
     * to the number of characters read from the lexer input.
     *
     * @see #createToken(TokenId, int)
     */
    public Token<T> createToken(T id) {
        return createToken(id, operation.readIndex());
    }

    /**
     * Create regular token instance with an explicit length.
     *
     * @param id non-null token id recognized by the lexer.
     * @param length >=0 length of the token to be created. The length must not
     *  exceed the number of characters read from the lexer input.
     * @return non-null regular token instance.
     *  <br/>
     *  {@link #SKIP_TOKEN} will be returned
     *  if tokens for the given token id should be skipped
     *  because of token id filter.
     */
    public Token<T> createToken(T id, int length) {
        if (isSkipToken(id)) {
            operation.tokenRecognized(length, true);
            return skipToken();
        } else { // Do not skip the token
            if (operation.tokenRecognized(length, false)) { // Create preprocessed token
//                return new PreprocessedTextToken<T>(id, operation.tokenLength());
                return new DefaultToken<T>(id, operation.tokenLength());
            } else {
                return new DefaultToken<T>(id, operation.tokenLength());
            }
        }
    }

    /**
     * Create regular token instance with an explicit length and part type.
     *
     * @param id non-null token id recognized by the lexer.
     * @param length >=0 length of the token to be created. The length must not
     *  exceed the number of characters read from the lexer input.
     * @param partType whether this token is complete token or a part of a complete token.
     * @return non-null regular token instance.
     *  <br/>
     *  {@link #SKIP_TOKEN} will be returned
     *  if tokens for the given token id should be skipped
     *  because of token id filter.
     */
    public Token<T> createToken(T id, int length, PartType partType) {
        checkPartTypeNonNull(partType);
        if (partType == PartType.COMPLETE)
            return createToken(id, length);

        if (isSkipToken(id)) {
            operation.tokenRecognized(length, true);
            return skipToken();
        } else { // Do not skip the token
            if (operation.tokenRecognized(length, false)) { // Create preprocessed token
//                return new ComplexToken<T>(id, operation.tokenLength(), null, partType, null);
                return new PropertyToken<T>(id, operation.tokenLength(), null, partType);
            } else {
                return new PropertyToken<T>(id, operation.tokenLength(), null, partType);
            }
        }
    }

    /**
     * Get flyweight token for the given arguments.
     * <br/>
     * <b>Note:</b> The returned token will not be flyweight under certain
     * conditions - see return value description.
     *
     * @param id non-null token id.
     * @param text non-null text that the flyweight token should carry.
     * @return non-null flyweight token instance.
     *  <br/>
     *  For performance reasons there is a limit for number of successive
     *  flyweight tokens. If this limit would be exceeded a single non-flyweight
     *  token gets created instead of flyweight one.
     *  <br/>
     *  {@link #SKIP_TOKEN} will be returned
     *  if tokens for the given token id should be skipped
     *  because of token id filter.
     */
    public Token<T> getFlyweightToken(T id, String text) {
        assert (text.length() <= operation.readIndex());
        // Compare each recognized char with the corresponding char in text
        if (testing) {
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) != operation.readExisting(i)) {
                    throw new IllegalArgumentException("Flyweight text in " + // NOI18N
                            "TokenFactory.getFlyweightToken(" + id + ", \"" + // NOI18N
                            CharSequenceUtilities.debugText(text) + "\") " + // NOI18N
                            "differs from recognized text: '" + // NOI18N
                            CharSequenceUtilities.debugChar(operation.readExisting(i)) +
                            "' != '" + CharSequenceUtilities.debugChar(text.charAt(i)) + // NOI18N
                            "' at index=" + i // NOI18N
                    );
                }
            }
        }

        // Check whether token with given id should be created
        if (isSkipToken(id)) {
            operation.tokenRecognized(text.length(), true);
            return skipToken();
        } else { // Do not skip the token
            if (operation.tokenRecognized(text.length(), false)) { // Create preprocessed token
//                return new PreprocessedTextToken<T>(id, operation.tokenLength());
                return new DefaultToken<T>(id, operation.tokenLength());
            } else if (operation.isFlyTokenAllowed()) {
                LanguageOperation<T> langOp = operation.languageOperation();
                return langOp.getFlyweightToken(id, text);
            } else { // return non-flyweight token
                return new DefaultToken<T>(id, operation.tokenLength());
            }
        }
    }
    
    /**
     * Create token with properties.
     *
     * @param id non-null token id.
     * @param length >=0 length of the token to be created. The length must not
     *  exceed the number of characters read from the lexer input.
     * @param propertyProvider non-null token property provider.
     * @param partType whether this token is complete or just a part of complete token.
     *  See {@link TokenPropertyProvider} for examples how this parameter may be used.
     * @return non-null property token instance.
     *  <br/>
     *  {@link #SKIP_TOKEN} will be returned
     *  if tokens for the given token id should be skipped
     *  because of token id filter.
     */
    public Token<T> createPropertyToken(T id, int length,
    TokenPropertyProvider propertyProvider, PartType partType) {
        checkPartTypeNonNull(partType);
        if (isSkipToken(id)) {
            operation.tokenRecognized(length, true);
            return skipToken();
        } else { // Do not skip the token
            if (operation.tokenRecognized(length, false)) { // Create preprocessed token
//                return new ComplexToken<T>(id, operation.tokenLength(),
//                    propertyProvider, null, partType);
                return new PropertyToken<T>(id, operation.tokenLength(),
                    propertyProvider, partType);
            } else {
                return new PropertyToken<T>(id, operation.tokenLength(),
                    propertyProvider, partType);
            }
        }
    }

    /**
     * Create token with a custom text that possibly differs from the text
     * represented by the token in the input text.
     */
    public Token<T> createCustomTextToken(T id, CharSequence text, int length, PartType partType) {
        checkPartTypeNonNull(partType);
        if (isSkipToken(id)) {
            operation.tokenRecognized(length, true);
            return skipToken();
        } else { // Do not skip the token
            if (operation.tokenRecognized(length, false)) { // Create preprocessed token
                return new CustomTextToken<T>(id, operation.tokenLength(), text, partType);
//                return new ComplexToken<T>(id, operation.tokenLength(), null, text, partType);
            } else {
                return new CustomTextToken<T>(id, operation.tokenLength(), text, partType);
            }
        }
    }
    
    private boolean isSkipToken(T id) {
        Set<? extends TokenId> skipTokenIds = operation.skipTokenIds();
        return (skipTokenIds != null) && skipTokenIds.contains(id);
    }

    @SuppressWarnings("unchecked") // NOI18N
    private Token<T> skipToken() {
        return SKIP_TOKEN;
    }
    
    private void checkPartTypeNonNull(PartType partType) {
        if (partType == null)
            throw new IllegalArgumentException("partType must be non-null");
    }
    
}
