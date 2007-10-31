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

package org.netbeans.lib.lexer;

import java.util.List;
import java.util.Set;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.lib.editor.util.FlyOffsetGapList;
import org.netbeans.lib.lexer.inc.MutableTokenList;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.inc.TokenListChange;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.TextToken;


/**
 * Embedded token list maintains a list of tokens
 * on a particular embedded language level .
 * <br>
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

public final class EmbeddedTokenList<T extends TokenId>
extends FlyOffsetGapList<Object> implements MutableTokenList<T> {
    
    /** Flag for additional correctness checks (may degrade performance). */
    private static final boolean testing = Boolean.getBoolean("netbeans.debug.lexer.test");
    
    /**
     * Marker value that represents that an attempt to create default embedding was
     * made but was unsuccessful.
     */
    public static final EmbeddedTokenList<TokenId> NO_DEFAULT_EMBEDDING
            = new EmbeddedTokenList<TokenId>(null, null, null, null);
    
    /**
     * Embedding container carries info about the token into which this
     * token list is embedded.
     */
    private EmbeddingContainer<? extends TokenId> embeddingContainer; // 36 bytes (32-super + 4)
    
    /**
     * Language embedding for this embedded token list.
     */
    private final LanguageEmbedding<T> embedding; // 40 bytes
    
    /**
     * Language path of this token list.
     */
    private final LanguagePath languagePath; // 44 bytes
    
    /**
     * Storage for lookaheads and states.
     * <br/>
     * It's non-null only initialized for mutable token lists
     * or when in testing environment.
     */
    private LAState laState; // 48 bytes
    
    /**
     * Next embedded token list forming a single-linked list.
     */
    private EmbeddedTokenList<? extends TokenId> nextEmbeddedTokenList; // 52 bytes
    
    
    public EmbeddedTokenList(EmbeddingContainer<? extends TokenId> embeddingContainer,
    LanguagePath languagePath, LanguageEmbedding<T> embedding,
    EmbeddedTokenList<? extends TokenId> nextEmbedding) {
        this.embeddingContainer = embeddingContainer;
        this.languagePath = languagePath;
        this.embedding = embedding;
        this.nextEmbeddedTokenList = nextEmbedding;

        if (embeddingContainer != null) { // ec may be null for NO_DEFAULT_EMBEDDING only
            laState = LAState.initState();
        }
    }

    private void init() {
        if (embedding.joinSections()) {
            // Find the token list list - it should also init this token list
            TokenListList tll = root().tokenHierarchyOperation().tokenListList(languagePath);
        } else { // not joining => can lex individually
            init(null);
        }
    }
    
    public void init(Object relexState) {
        laState = (modCount() != -1 || testing) ? LAState.empty() : null;

        embeddingContainer.updateStatusImpl(); // Ensure startOffset() is up-to-date
        // Lex the whole input represented by token at once
        LexerInputOperation<T> lexerInputOperation = createLexerInputOperation(
                0, startOffset(), relexState);
        AbstractToken<T> token = lexerInputOperation.nextToken();
        while (token != null) {
            updateElementOffsetAdd(token); // must subtract startOffset()
            add(token);
            if (laState != null) {
                laState = laState.add(lexerInputOperation.lookahead(),
                        lexerInputOperation.lexerState());
            }
            token = lexerInputOperation.nextToken();
        }
        lexerInputOperation.release();
        lexerInputOperation = null;

        trimToSize(); // Compact storage
        if (laState != null)
            laState.trimToSize();
    }
    
    /**
     * Check whether this embedded token list is initialized.
     * <br/>
     * If not then the updating process should not touch it unless
     * the token list list exists for this particular language path.
     */
    public boolean isInited() {
        return (laState != LAState.initState());
    }
    
    EmbeddedTokenList<? extends TokenId> nextEmbeddedTokenList() {
        return nextEmbeddedTokenList;
    }
    
    void setNextEmbeddedTokenList(EmbeddedTokenList<? extends TokenId> nextEmbeddedTokenList) {
        this.nextEmbeddedTokenList = nextEmbeddedTokenList;
    }
    
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    public LanguageEmbedding embedding() {
        return embedding;
    }

    public int tokenCount() {
        synchronized (root()) {
            if (laState == LAState.initState())
                init();
            return size();
        }
    }
    
    public Object tokenOrEmbeddingContainer(int index) {
        synchronized (root()) {
            if (laState == LAState.initState())
                init();
            return (index < size()) ? get(index) : null;
        }
    }
    
    private Token existingToken(int index) {
        return LexerUtilsConstants.token(tokenOrEmbeddingContainer(index));
    }
    
    public int lookahead(int index) {
        return (laState != null) ? laState.lookahead(index) : -1;
    }

    public Object state(int index) {
        return (laState != null) ? laState.state(index) : null;
    }

    /**
     * Returns absolute offset of the token at the given index
     * (startOffset gets added to the child token's real offset).
     * <br/>
     * For token hierarchy snapshots the returned value is corrected
     * in the TokenSequence explicitly by adding TokenSequence.tokenOffsetDiff.
     */
    public int tokenOffset(int index) {
        return elementOffset(index);
    }

    public int childTokenOffset(int rawOffset) {
        // Need to make sure that the startOffset is up-to-date
        embeddingContainer.updateStatus();
        return childTokenOffsetNoUpdate(rawOffset);
    }
    
    public int childTokenOffsetNoUpdate(int rawOffset) {
        return embeddingContainer.tokenStartOffset() + embedding.startSkipLength()
            + childTokenRelOffset(rawOffset);
    }

    /**
     * Get difference between start offset of the particular child token
     * against start offset of the root token.
     */
    public int childTokenOffsetShift(int rawOffset) {
        // Need to make sure that the startOffsetShift is up-to-date
        embeddingContainer.updateStatus();
        return embeddingContainer.rootTokenOffsetShift() + childTokenRelOffset(rawOffset);
    }

    /**
     * Get child token's real offset which is always a relative value
     * to startOffset value.
     */
    private int childTokenRelOffset(int rawOffset) {
        return (rawOffset < offsetGapStart())
                ? rawOffset
                : rawOffset - offsetGapLength();
    }

    public char childTokenCharAt(int rawOffset, int index) {
        // Do not update the start offset shift - the token.text()
        // did it before returning its result and its contract
        // specifies that.
        // Return chars by delegating to rootToken
        return embeddingContainer.charAt(
                embedding.startSkipLength() + childTokenRelOffset(rawOffset) + index);
    }

    public int modCount() {
        // Delegate to root to have the most up-to-date value for token sequence's check.
        // Extra synchronization should not be necessary since the TokenSequence.embedded()
        // calls EmbeddingContainer.embeddedTokenList()
        // which calls which contains the synchronization and calls updateStatusImpl().
        return embeddingContainer.cachedModCount();
    }
    
    public int startOffset() { // used by FlyOffsetGapList
        return embeddingContainer.tokenStartOffset() + embedding.startSkipLength();
    }
    
    public int endOffset() {
        return embeddingContainer.tokenStartOffset() + embeddingContainer.token().length()
                - embedding.endSkipLength();
    }
    
    public TokenList<? extends TokenId> root() {
        return embeddingContainer.rootTokenList();
    }
    
    public TokenHierarchyOperation<?,? extends TokenId> tokenHierarchyOperation() {
        return root().tokenHierarchyOperation();
    }
    
    public AbstractToken<? extends TokenId> rootToken() {
        return embeddingContainer.rootToken();
    }

    protected int elementRawOffset(Object elem) {
        return (elem.getClass() == EmbeddingContainer.class)
            ? ((EmbeddingContainer)elem).token().rawOffset()
            : ((AbstractToken<? extends TokenId>)elem).rawOffset();
    }

    protected void setElementRawOffset(Object elem, int rawOffset) {
        if (elem.getClass() == EmbeddingContainer.class)
            ((EmbeddingContainer)elem).token().setRawOffset(rawOffset);
        else
            ((AbstractToken<? extends TokenId>)elem).setRawOffset(rawOffset);
    }
    
    protected boolean isElementFlyweight(Object elem) {
        // token wrapper always contains non-flyweight token
        return (elem.getClass() != EmbeddingContainer.class)
            && ((AbstractToken<? extends TokenId>)elem).isFlyweight();
    }
    
    protected int elementLength(Object elem) {
        return LexerUtilsConstants.token(elem).length();
    }
    
    public AbstractToken<T> replaceFlyToken(
    int index, AbstractToken<T> flyToken, int offset) {
        synchronized (root()) {
            TextToken<T> nonFlyToken = ((TextToken<T>)flyToken).createCopy(this, offset2Raw(offset));
            set(index, nonFlyToken);
            return nonFlyToken;
        }
    }

    public void wrapToken(int index, EmbeddingContainer embeddingContainer) {
        synchronized (root()) {
            set(index, embeddingContainer);
        }
    }

    public InputAttributes inputAttributes() {
        return root().inputAttributes();
    }

    // MutableTokenList extra methods
    public Object tokenOrEmbeddingContainerUnsync(int index) {
        return get(index);
    }

    public int tokenCountCurrent() {
        return size();
    }

    public LexerInputOperation<T> createLexerInputOperation(
    int tokenIndex, int relexOffset, Object relexState) {
        CharSequence tokenText = embeddingContainer.token().text();
        int tokenStartOffset = embeddingContainer.tokenStartOffset();
        if (tokenText == null) { // Should not normally happen - debug the state
            throw new IllegalStateException("Text of parent token is null. tokenStartOffset=" + tokenStartOffset +
                    ", tokenIndex=" + tokenIndex + ", relexOffset=" + relexOffset + ", relexState=" + relexState +
                    ", languagePath=" + languagePath() + ", inited=" + isInited()
            );
        }
        int endOffset = tokenStartOffset + tokenText.length()
            - embedding.endSkipLength();
        return new TextLexerInputOperation<T>(this, tokenIndex, relexState, tokenText,
                tokenStartOffset, relexOffset, endOffset);
    }

    public boolean isFullyLexed() {
        return true;
    }

    public void replaceTokens(TokenListChange<T> change, int removeTokenCount, int diffLength) {
        int index = change.index();
        // Remove obsolete tokens (original offsets are retained)
        Object[] removedTokensOrEmbeddingContainers = new Object[removeTokenCount];
        copyElements(index, index + removeTokenCount, removedTokensOrEmbeddingContainers, 0);
        int offset = change.offset();
        for (int i = 0; i < removeTokenCount; i++) {
            Object tokenOrEmbeddingContainer = removedTokensOrEmbeddingContainers[i];
            AbstractToken<T> token = LexerUtilsConstants.token(tokenOrEmbeddingContainer);
            if (!token.isFlyweight()) {
                updateElementOffsetRemove(token);
                token.setTokenList(null);
            }
            offset += token.length();
        }
        remove(index, removeTokenCount); // Retain original offsets
        laState.remove(index, removeTokenCount); // Remove lookaheads and states
        change.setRemovedTokens(removedTokensOrEmbeddingContainers);
        change.setRemovedEndOffset(offset);

        // Move and fix the gap according to the performed modification.
        int startOffset = startOffset(); // updateStatus() should already be called
        if (offsetGapStart() != change.offset() - startOffset) {
            // Minimum of the index of the first removed index and original computed index
            moveOffsetGap(change.offset() - startOffset, Math.min(index, change.offsetGapIndex()));
        }
        updateOffsetGapLength(-diffLength);

        // Add created tokens.
        // This should be called early when all the members are true tokens
        List<Object> addedTokensOrBranches = change.addedTokensOrBranches();
        if (addedTokensOrBranches != null) {
            for (Object tokenOrBranch : addedTokensOrBranches) {
                @SuppressWarnings("unchecked")
                AbstractToken<T> token = (AbstractToken<T>)tokenOrBranch;
                updateElementOffsetAdd(token);
            }
            addAll(index, addedTokensOrBranches);
            laState = laState.addAll(index, change.laState());
            change.syncAddedTokenCount();
            // Check for bounds change only
            if (removeTokenCount == 1 && addedTokensOrBranches.size() == 1) {
                // Compare removed and added token ids and part types
                AbstractToken<T> removedToken = LexerUtilsConstants.token(removedTokensOrEmbeddingContainers[0]);
                AbstractToken<T> addedToken = change.addedToken(0);
                if (removedToken.id() == addedToken.id()
                    && removedToken.partType() == addedToken.partType()
                ) {
                    change.markBoundsChange();
                }
            }
        }
    }

    public boolean isContinuous() {
        return true;
    }

    public Set<T> skipTokenIds() {
        return null;
    }
    
    public EmbeddingContainer<?> embeddingContainer() {
        return embeddingContainer;
    }
    
    public void setEmbeddingContainer(EmbeddingContainer<? extends TokenId> embeddingContainer) {
        this.embeddingContainer = embeddingContainer;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("EmbeddedTokenList: startOffset=").append(startOffset());
        sb.append(", endOffset=").append(endOffset());
        sb.append('\n');
        LexerUtilsConstants.appendTokenList(sb, this);
        return sb.toString();
    }

}
