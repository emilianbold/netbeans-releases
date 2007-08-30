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

package org.netbeans.api.lexer;

import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.inc.TokenChangeInfo;

/**
 * Token change describes modification on one level of a token hierarchy.
 * <br/>
 * If there is only one token that was modified
 * and there was a language embedding in that token then
 * most of the embedded tokens can usually be retained.
 * This defines an embedded change accessible by {@link #embeddedChange(int)}.
 * <br/>
 * There may possibly be multiple levels of the embedded changes.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenChange<T extends TokenId> {
    
    private final TokenChangeInfo<T> info;
    
    TokenChange(TokenChangeInfo<T> info) {
        this.info = info;
    }

    /**
     * Get number of embedded changes contained in this change.
     *
     * @return >=0 number of embedded changes.
     */
    public int embeddedChangeCount() {
        return info.embeddedChanges().length;
    }
    
    /**
     * Get embedded change at the given index.
     *
     * @param index 0 &lt;= index &lt;= embeddedChangeCount() index of the embedded change.
     * @return non-null embedded token change.
     */
    public TokenChange<? extends TokenId> embeddedChange(int index) {
        return info.embeddedChanges()[index];
    }

    /**
     * Get the language describing token ids
     * used by tokens contained in this token change.
     */
    public Language<T> language() {
        return LexerUtilsConstants.innerLanguage(languagePath());
    }
    
    /**
     * Get the complete language path of the tokens contained
     * in this token sequence (containing outer language levels as well).
     */
    public LanguagePath languagePath() {
        return info.currentTokenList().languagePath();
    }

    /**
     * Get index of the first token being modified.
     */
    public int index() {
        return info.index();
    }
    
    /**
     * Get offset of the first token that was modified.
     * <br/>
     * If there were any added/removed tokens then this is a start offset
     * of the first added/removed token.
     */
    public int offset() {
        return info.offset();
    }
    
    /**
     * Get number of removed tokens contained in this token change.
     */
    public int removedTokenCount() {
        TokenList<? extends TokenId> rtl = info.removedTokenList();
        return (rtl != null) ? rtl.tokenCount() : 0;
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
        return new TokenSequence<T>(info.removedTokenList());
    }
 
    /**
     * Get number of the tokens added by this token change.
     */
    public int addedTokenCount() {
        return info.addedTokenCount();
    }
    
    /**
     * Get the token sequence that corresponds to the current state
     * of the token hierarchy.
     * <br/>
     * The token sequence will be positioned at the {@link #index()}.
     * <br/>
     * If this is an embedded token change then this method returns
     * the token sequence at the corresponding embedded level.
     */
    public TokenSequence<T> currentTokenSequence() {
        TokenSequence<T> ts = new TokenSequence<T>(info.currentTokenList());
        ts.moveIndex(index());
        return ts;
    }
    
    /**
     * Whether this change only modifies bounds of a single token.
     * <br/>
     * This flag is only set if there was a single token removed and a new single token
     * added with the same token id in terms of this change.
     * <br/>
     * For bounds changes the affected offsets of the event will only
     * cover the modified characters (not the modified tokens boundaries).
     */
    public boolean isBoundsChange() {
        return info.isBoundsChange();
    }
    

    /**
     * Used by package-private accessor.
     */
    TokenChangeInfo<T> info() {
        return info;
    }

}