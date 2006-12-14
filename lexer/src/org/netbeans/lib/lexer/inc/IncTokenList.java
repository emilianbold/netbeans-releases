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
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.spi.lexer.MutableTextInput;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.TextToken;


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
extends FlyOffsetGapList<Object> implements MutableTokenList<T> {
    
    private final TokenHierarchyOperation<?,T> tokenHierarchyOperation;

    private final MutableTextInput<?> mutableTextInput;
    
    private final LanguagePath languagePath;
    
    private final InputAttributes inputAttributes;
    
    private CharSequence text;
    
    /**
     * Lexer input operation used for lexing of the input.
     */
    private LexerInputOperation<T> lexerInputOperation;
    
    private boolean inited;
    
    private int rootModCount;

    private LAState laState;
    
    
    public IncTokenList(TokenHierarchyOperation<?,T> tokenHierarchyOperation,
    MutableTextInput<?> mutableTextInput) {
        this.tokenHierarchyOperation = tokenHierarchyOperation;
        this.mutableTextInput = mutableTextInput;
        this.languagePath = LanguagePath.get(
                LexerSpiPackageAccessor.get().language(mutableTextInput));
        this.inputAttributes = LexerSpiPackageAccessor.get().inputAttributes(mutableTextInput);
        this.text = LexerSpiPackageAccessor.get().text(mutableTextInput);
        this.laState = LAState.empty();
    }
    
    private void init() {
        lexerInputOperation = new TextLexerInputOperation<T>(this, text);
    }
    
    public LanguagePath languagePath() {
        return languagePath;
    }

    public synchronized int tokenCount() {
        if (!inited) {
            init();
            inited = true;
        }
        if (lexerInputOperation != null) { // still lexing
            tokenOrEmbeddingContainerImpl(Integer.MAX_VALUE);
        }
        return size();
    }

    public char childTokenCharAt(int rawOffset, int index) {
        return text.charAt(childTokenOffset(rawOffset) + index);
    }
    
    public int childTokenOffset(int rawOffset) {
        return (rawOffset < offsetGapStart()
                ? rawOffset
                : rawOffset - offsetGapLength());
    }
    
    public int tokenOffset(int index) {
        return elementOffset(index);
    }

    /**
     * Get modification count for which this token list was last updated
     * (mainly its cached start offset).
     */
    public int modCount() {
        return rootModCount;
    }
    
    public void incrementModCount() {
        rootModCount++;
    }
    
    public synchronized Object tokenOrEmbeddingContainer(int index) {
        return tokenOrEmbeddingContainerImpl(index);
    }
    
    private Object tokenOrEmbeddingContainerImpl(int index) {
        if (!inited) {
            init();
            inited = true;
        }
        while (lexerInputOperation != null && index >= size()) {
            Token token = lexerInputOperation.nextToken();
            if (token != null) { // lexer returned valid token
                updateElementOffsetAdd(token);
                add(token);
                laState = laState.add(lexerInputOperation.lookahead(),
                        lexerInputOperation.lexerState());
            } else { // no more tokens from lexer
                lexerInputOperation.release();
                lexerInputOperation = null;
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

    public synchronized void wrapToken(int index, EmbeddingContainer embeddingContainer) {
        set(index, embeddingContainer);
    }

    public InputAttributes inputAttributes() {
        return inputAttributes;
    }
    
    protected int elementRawOffset(Object elem) {
        return LexerUtilsConstants.token(elem).rawOffset();
    }
 
    protected void setElementRawOffset(Object elem, int rawOffset) {
        LexerUtilsConstants.token(elem).setRawOffset(rawOffset);
    }
    
    protected boolean isElementFlyweight(Object elem) {
        // token wrapper always contains non-flyweight token
        return (elem.getClass() != EmbeddingContainer.class)
            && ((AbstractToken)elem).isFlyweight();
    }
    
    protected int elementLength(Object elem) {
        return LexerUtilsConstants.token(elem).length();
    }
    
    private AbstractToken<T> existingToken(int index) {
        // Must use synced tokenOrEmbeddingContainer() because of possible change
        // of the underlying list impl when adding lazily requested tokens
        return LexerUtilsConstants.token(tokenOrEmbeddingContainer(index));
    }

    public Object tokenOrEmbeddingContainerUnsync(int index) {
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

    public TokenList<? extends TokenId> root() {
        return this;
    }

    public TokenHierarchyOperation<?,? extends TokenId> tokenHierarchyOperation() {
        return tokenHierarchyOperation;
    }
    
    public LexerInputOperation<T> createLexerInputOperation(
    int tokenIndex, int relexOffset, Object relexState) {
        // Used for mutable lists only so maintain LA and state
        return new TextLexerInputOperation<T>(this, tokenIndex, relexState,
                text, 0, relexOffset, text.length());
    }

    public boolean isFullyLexed() {
        return inited && (lexerInputOperation == null);
    }

    public void replaceTokens(TokenHierarchyEventInfo eventInfo,
    TokenListChange<T> change, int removeTokenCount) {
        int index = change.index();
        // Remove obsolete tokens (original offsets are retained)
        Object[] removedTokensOrBranches = new Object[removeTokenCount];
        copyElements(index, index + removeTokenCount, removedTokensOrBranches, 0);
        int offset = change.offset();
        for (int i = 0; i < removeTokenCount; i++) {
            Object tokenOrEmbeddingContainer = removedTokensOrBranches[i];
            AbstractToken<T> token = LexerUtilsConstants.token(tokenOrEmbeddingContainer);
            if (!token.isFlyweight()) {
                updateElementOffsetRemove(token);
                token.setTokenList(null);
            }
            offset += token.length();
        }
        remove(index, removeTokenCount); // Retain original offsets
        laState.remove(index, removeTokenCount); // Remove lookaheads and states
        change.setRemovedTokens(removedTokensOrBranches);
        change.setRemovedEndOffset(offset);

        // Move and fix the gap according to the performed modification.
        int diffLength = eventInfo.insertedLength() - eventInfo.removedLength();
        if (offsetGapStart() != change.offset()) {
            // Minimum of the index of the first removed index and original computed index
            moveOffsetGap(change.offset(), Math.min(index, change.offsetGapIndex()));
        }
        updateOffsetGapLength(-diffLength);

        // Add created tokens.
        List<AbstractToken<T>> addedTokens = change.addedTokens();
        if (addedTokens != null) {
            for (int i = 0; i < addedTokens.size(); i++) {
                AbstractToken<T> token = addedTokens.get(i);
                updateElementOffsetAdd(token);
            }
            addAll(index, addedTokens);
            laState = laState.addAll(index, change.laState());
            change.syncAddedTokenCount();
        }
    }
    
    public void refreshLexerInputOperation() {
        // Only called when !isFullyLexed() but "inited" might be false => must check "!= null"
        if (lexerInputOperation != null)
            lexerInputOperation.release();
        int lastTokenIndex = tokenCountCurrent() - 1;
        int endOffset = (lastTokenIndex >= 0)
                ? tokenOffset(lastTokenIndex) + existingToken(lastTokenIndex).length()
                : 0;
        lexerInputOperation = createLexerInputOperation(
                lastTokenIndex + 1,
                endOffset,
                (lastTokenIndex >= 0) ? state(lastTokenIndex) : null
        );
    }
    
    public boolean isContinuous() {
        return true;
    }

    public Set<T> skipTokenIds() {
        return null;
    }

    public String toString() {
        return LexerUtilsConstants.appendTokenList(null, this, -1).toString();
    }

}
