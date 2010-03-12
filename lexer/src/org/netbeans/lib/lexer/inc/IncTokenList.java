/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.lib.lexer.inc;

import java.util.List;
import java.util.Set;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.lib.lexer.LAState;
import org.netbeans.lib.lexer.LexerSpiPackageAccessor;
import org.netbeans.lib.lexer.TextLexerInputOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.editor.util.FlyOffsetGapList;
import org.netbeans.lib.lexer.EmbeddingContainer;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.TextToken;
import org.netbeans.lib.lexer.TokenOrEmbedding;
import org.netbeans.spi.lexer.MutableTextInput;


/**
 * Incremental token list maintains a list of tokens
 * at the root language level.
 * <br/>
 * The physical storage contains a gap to speed up list modifications
 * during typing in a document when tokens are typically added/removed
 * at the same index in the list.
 *
 * <p>
 * There is an intent to not degrade performance significantly
 * with each extra language embedding level so the token list maintains direct
 * link to the root level.
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class IncTokenList<T extends TokenId>
extends FlyOffsetGapList<TokenOrEmbedding<T>> implements MutableTokenList<T> {
    
    private final TokenHierarchyOperation<?,T> tokenHierarchyOperation;

    private LanguagePath languagePath;
    
    private CharSequence inputSourceText;
    
    /**
     * Lexer input operation used for lexing of the input.
     */
    private LexerInputOperation<T> lexerInputOperation;
    
    private int rootModCount;

    private LAState laState;
    
    
    public IncTokenList(TokenHierarchyOperation<?,T> tokenHierarchyOperation) {
        this.tokenHierarchyOperation = tokenHierarchyOperation;
    }
    
    /**
     * Activate this list internally if it's currently active (its languagePath() != null)
     * or deactivate if LP == null.
     */
    public void reinit() {
        if (languagePath != null) {
            MutableTextInput input = tokenHierarchyOperation.mutableTextInput();
            this.inputSourceText = LexerSpiPackageAccessor.get().text(input);
            this.lexerInputOperation = new TextLexerInputOperation<T>(this);
        } else {
            this.inputSourceText = null;
            releaseLexerInputOperation();
        }
        this.laState = LAState.empty();
    }
    
    public void releaseLexerInputOperation() {
        if (lexerInputOperation != null) {
            lexerInputOperation.release();
            lexerInputOperation = null;
        }
    }

    public LanguagePath languagePath() {
        return languagePath;
    }
    
    public void setLanguagePath(LanguagePath languagePath) {
        this.languagePath = languagePath;
    }

    public boolean updateLanguagePath() {
        Language<?> language = LexerSpiPackageAccessor.get().language(tokenHierarchyOperation.mutableTextInput());
        if (language != null) {
            setLanguagePath(LanguagePath.get(language));
            return true;
        }
        return false;
    }
    
    public synchronized int tokenCount() {
        if (lexerInputOperation != null) { // still lexing
            tokenOrEmbeddingImpl(Integer.MAX_VALUE);
        }
        return size();
    }

    public int tokenOffset(AbstractToken<T> token) {
        int rawOffset = token.rawOffset();
        return (rawOffset < offsetGapStart()
                ? rawOffset
                : rawOffset - offsetGapLength());
    }

    public int tokenOffset(int index) {
        return elementOffset(index);
    }
    
    public int[] tokenIndex(int offset) {
        return LexerUtilsConstants.tokenIndexLazyTokenCreation(this, offset);
    }

    /**
     * Get modification count for which this token list was last updated
     * (mainly its cached start offset).
     */
    public synchronized int modCount() {
        return rootModCount;
    }
    
    public void incrementModCount() {
        rootModCount++;
    }
    
    public synchronized TokenOrEmbedding<T> tokenOrEmbedding(int index) {
        return tokenOrEmbeddingImpl(index);
    }
    
    private TokenOrEmbedding<T> tokenOrEmbeddingImpl(int index) {
        while (lexerInputOperation != null && index >= size()) {
            AbstractToken<T> token = lexerInputOperation.nextToken();
            if (token != null) { // lexer returned valid token
                if (!token.isFlyweight())
                    token.setTokenList(this);
                updateElementOffsetAdd(token);
                add(token);
                laState = laState.add(lexerInputOperation.lookahead(),
                        lexerInputOperation.lexerState());
            } else { // no more tokens from lexer
                releaseLexerInputOperation();
                trimToSize();
                laState.trimToSize();
            }
        }
        return (index < size()) ? get(index) : null;
    }
    
    public synchronized AbstractToken<T> replaceFlyToken(
    int index, AbstractToken<T> flyToken, int offset) {
        TextToken<T> nonFlyToken = ((TextToken<T>)flyToken).createCopy(this, offset2Raw(offset));
        set(index, nonFlyToken);
        return nonFlyToken;
    }

    public synchronized void wrapToken(int index, EmbeddingContainer<T> embeddingContainer) {
        set(index, embeddingContainer);
    }

    public InputAttributes inputAttributes() {
        return LexerSpiPackageAccessor.get().inputAttributes(tokenHierarchyOperation.mutableTextInput());
    }
    
    protected int elementRawOffset(TokenOrEmbedding<T> elem) {
        return elem.token().rawOffset();
    }
 
    protected void setElementRawOffset(TokenOrEmbedding<T> elem, int rawOffset) {
        elem.token().setRawOffset(rawOffset);
    }
    
    protected boolean isElementFlyweight(TokenOrEmbedding<T> elem) {
        // token wrapper always contains non-flyweight token
        return (elem.embedding() == null)
            && elem.token().isFlyweight();
    }
    
    protected int elementLength(TokenOrEmbedding<T> elem) {
        return elem.token().length();
    }
    
    public TokenOrEmbedding<T> tokenOrEmbeddingUnsync(int index) {
        // Solely for token list updater or token hierarchy snapshots
        // having single-threaded exclusive write access
        return get(index);
    }

    public int lookahead(int index) {
        return laState.lookahead(index);
    }

    public Object state(int index) {
        return laState.state(index);
    }

    public int tokenCountCurrent() {
        return size();
    }

    public TokenList<?> rootTokenList() {
        return this;
    }

    public CharSequence inputSourceText() {
        return inputSourceText;
    }

    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return tokenHierarchyOperation;
    }
    
    public LexerInputOperation<T> createLexerInputOperation(
    int tokenIndex, int relexOffset, Object relexState) {
        // Possibly release unfinished lexing - will be restarted in replaceTokens()
        // Releasing the lexer now allows to share a single backing lexer's impl instance better.
        // Do not assign null to lexerInputOperation since the replaceTokens() would not know
        // that the lexing was unfinished.
        if (lexerInputOperation != null)
            lexerInputOperation.release();

        // Used for mutable lists only so maintain LA and state
        return new TextLexerInputOperation<T>(this, tokenIndex, relexState,
                relexOffset, inputSourceText.length());
    }

    public boolean isFullyLexed() {
        return (lexerInputOperation == null);
    }

    public void replaceTokens(TokenListChange<T> change, TokenHierarchyEventInfo eventInfo, boolean modInside) {
        int index = change.index();
        // Remove obsolete tokens (original offsets are retained)
        int removeTokenCount = change.removedTokenCount();
        AbstractToken<T> firstRemovedToken = null;
        if (removeTokenCount > 0) {
            @SuppressWarnings("unchecked")
            TokenOrEmbedding<T>[] removedTokensOrEmbeddings = new TokenOrEmbedding[removeTokenCount];
            copyElements(index, index + removeTokenCount, removedTokensOrEmbeddings, 0);
            firstRemovedToken = removedTokensOrEmbeddings[0].token();
            for (int i = 0; i < removeTokenCount; i++) {
                TokenOrEmbedding<T> tokenOrEmbedding = removedTokensOrEmbeddings[i];
                // It's necessary to update-status of all removed tokens' contained embeddings
                // since otherwise (if they would not be up-to-date) they could not be updated later
                // as they lose their parent token list which the update-status relies on.
                AbstractToken<T> token = tokenOrEmbedding.token();
                if (!token.isFlyweight()) {
                    updateElementOffsetRemove(token);
                    token.setTokenList(null);
                    EmbeddingContainer<T> ec = tokenOrEmbedding.embedding();
                    if (ec != null) {
                        // Assert that the modCount of root token list (which should already be updated)
                        // is already updated while the children embeddings were not "touched" and they still hold
                        // before-mod mod counts because otherwise their offsets would not reflect
                        // proper original values.
                        assert (ec.cachedModCount() != rootModCount) : "ModCount=" + rootModCount + // NOI18N
                                " already updated in child embedding."; // NOI18N
                        ec.markRemoved(token.rawOffset());
                    }
                }
            }
            remove(index, removeTokenCount); // Retain original offsets
            laState.remove(index, removeTokenCount); // Remove lookaheads and states
            change.setRemovedTokens(removedTokensOrEmbeddings);
        } else {
            change.setRemovedTokensEmpty();
        }

        // Move and fix the gap according to the performed modification.
        // Instead of modOffset the gap is located at first relexed token's start
        // because then the already precomputed index corresponding to the given offset
        // can be reused. Otherwise there would have to be another binary search for index.
        if (offsetGapStart() != change.offset()) {
            // Minimum of the index of the first removed index and original computed index
            moveOffsetGap(change.offset(), change.index());
        }
        updateOffsetGapLength(-eventInfo.diffLength());

        // Add created tokens.
        List<TokenOrEmbedding<T>> addedTokensOrEmbeddings = change.addedTokenOrEmbeddings();
        if (addedTokensOrEmbeddings != null && addedTokensOrEmbeddings.size() > 0) {
            for (TokenOrEmbedding<T> tokenOrEmbedding : addedTokensOrEmbeddings) {
                AbstractToken<T> token = tokenOrEmbedding.token();
                if (!token.isFlyweight())
                    token.setTokenList(this);
                updateElementOffsetAdd(token);
            }
            addAll(index, addedTokensOrEmbeddings);
            laState = laState.addAll(index, change.laState());
            change.syncAddedTokenCount();
            // Check for bounds change only
            if (removeTokenCount == 1 && addedTokensOrEmbeddings.size() == 1) {
                // Compare removed and added token ids and part types
                AbstractToken<T> addedToken = change.addedTokenOrEmbeddings().get(0).token();
                if (firstRemovedToken.id() == addedToken.id()
                    && firstRemovedToken.partType() == addedToken.partType()
                ) {
                    change.markBoundsChange();
                }
            }
        }

        // Possibly restart unfinished lexing
        if (this.lexerInputOperation != null) { // Lexing was not finished before update
            int tokenCount = tokenCountCurrent();
            lexerInputOperation = createLexerInputOperation(tokenCount, elementOrEndOffset(tokenCount),
                (tokenCount > 0) ? state(tokenCount - 1) : null);
        }
    }
    
    public boolean isContinuous() {
        return true;
    }

    public Set<T> skipTokenIds() {
        return null;
    }

    @Override
    public int startOffset() {
        return 0;
    }

    public int endOffset() {
        return (inputSourceText != null) ? inputSourceText.length() : 0;
    }

    public boolean isRemoved() {
        return false; // Should never become removed
    }

    public void setInputSourceText(CharSequence text) {
        this.inputSourceText = text;
    }

    @Override
    public String toString() {
        return LexerUtilsConstants.appendTokenList(null, this).toString();
    }
    
}
