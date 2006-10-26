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

package org.netbeans.api.lexer;

import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.inc.TokenListChange;

/**
 * Token change describes modification on one level of a token hierarchy.
 * <br/>
 * If there is only one token that was modified
 * and there was a language embedding in that token then
 * most of the embedded tokens can usually be retained.
 * This defines an embedded change accessible by {@link #embedded()}.
 * <br/>
 * There may possibly be multiple levels of the embedded changes.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenChange<T extends TokenId> {
    
    private final TokenListChange tokenListChange;
    
    TokenChange(TokenListChange tokenListChange) {
        this.tokenListChange = tokenListChange;
    }

    /**
     * Get embedded token change.
     * <br/>
     * If there is only one token that was modified
     * and there was a language embedding in that token then
     * there is possibility that the new token will be similar
     * to the old one and the embedded tokens can be retained
     * and just updated by another token change.
     * <br/>
     * In such case there will be an embedded token change.
     *
     * @return valid embedded token change or null if there
     *  is no embedded token change.
     */
    public TokenChange<? extends TokenId> embedded() {
        return null; // TODO
    }

    /**
     * Get embedded token change of the given type
     * only if it's of the given language.
     *
     * @return non-null token change or null if the embedded token change
     *  satisfies the condition <code>(embedded().language() == language)</code>.
     *  Null is returned otherwise.
     */
    public <T extends TokenId> TokenChange<T> embedded(Language<T> language) {
        @SuppressWarnings("unchecked")
        TokenChange<T> e = (TokenChange<T>)embedded();
        return (e != null && e.language() == language) ? e : null;
    }

    /**
     * Get the language describing token ids
     * used by tokens contained in this token change.
     */
    public Language<T> language() {
        // No need to check as the token sequence should already
        // be obtained originally for the inner language
        @SuppressWarnings("unchecked") Language<T> l
                = (Language<T>)languagePath().innerLanguage();
        return l;
    }
    
    /**
     * Get the complete language path of the tokens contained
     * in this token sequence (containing outer language levels as well).
     */
    public LanguagePath languagePath() {
        return tokenListChange.languagePath();
    }

    /**
     * Get start offset of the modification
     * that caused this token change.
     * <br/>
     * For token hierarchy rebuilds this is the start offset
     * of the area being rebuilt.
     */
    public int offset() {
        return tokenListChange.offset();
    }
    
    /**
     * Get number of characters inserted by the text modification
     * that caused this token change.
     * <br/>
     * For token hierarchy rebuilds this is the length
     * of the area being rebuilt.
     */
    public int insertedLength() {
        return tokenListChange.insertedLength();
    }
    
    /**
     * Get number of characters removed by the text modification
     * that caused this token change.
     * <br/>
     * For token hierarchy rebuilds this is the length
     * of the area being rebuilt.
     */
    public int removedLength() {
        return tokenListChange.removedLength();
    }
    
    /**
     * Get index of the first token being modified.
     */
    public int tokenIndex() {
        return tokenListChange.tokenIndex();
    }
    
    /**
     * Get number of tokens removed.
     */
    public int removedTokenCount() {
        return tokenListChange.removedTokenList().tokenCount();
    }
    
    /**
     * Get offset of the first token that was modified.
     * <br/>
     * The returned value is always equal or below the {@link #offset()} value.
     * <br/>
     * If there were any removed tokens then this is a start offset
     * of the first removed token.
     * <br/>
     * If there were only added tokens (no removed tokens)
     * then this is the start offset of the first added token.
     */
    public int modifiedTokensStartOffset() {
        return tokenListChange.modifiedTokensStartOffset();
    }
    
    /**
     * Get end offset of the last token that was removed
     * (in the original offset space before the removal was done).
     * <br/>
     * If there were no removed tokens then the result of this method
     * is equal to {@link #modifiedTokensStartOffset()}.
     */
    public int removedTokensEndOffset() {
        return tokenListChange.removedTokensEndOffset();
    }
    
    /**
     * Create token sequence over the removed tokens.
     *
     * <p>
     * There is no analogy of this method for the added tokens.
     * The {@link #currentTokenSequence()} may be used for exploration
     * of the current token sequence at this level.
     * </p>
     *
     * @return token sequence over the removed tokens
     *  or null if there were no removed tokens.
     */
    public TokenSequence<T> removedTokenSequence() {
        return new TokenSequence<T>(tokenListChange.removedTokenList());
    }
 
    /**
     * Get number of the tokens added by this token change.
     */
    public int addedTokenCount() {
        return tokenListChange.addedTokenCount();
    }
    
    /**
     * Get end offset of the last token that was added.
     * <br/>
     * If there were no added tokens then the result of this method
     * is equal to {@link #modifiedTokensStartOffset()}.
     */
    public int addedTokensEndOffset() {
        return tokenListChange.addedTokensEndOffset();
    }

    /**
     * Get the token sequence that corresponds to the current state
     * of the token hierarchy.
     * <br/>
     * If this is an embedded token change then this method returns
     * the token sequence at the corresponding embedded level.
     */
    public TokenSequence<T> currentTokenSequence() {
        return new TokenSequence<T>(tokenListChange.currentTokenList());
    }
    
    /**
     * Get token hierarchy where this change occurred.
     */
    public TokenHierarchy<?> tokenHierarchy() {
        return tokenListChange.tokenHierarchyOperation().tokenHierarchy();
    }

    TokenListChange tokenListChange() {
        return tokenListChange;
    }

}