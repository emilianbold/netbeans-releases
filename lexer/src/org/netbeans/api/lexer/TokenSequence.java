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

import java.util.ConcurrentModificationException;
import org.netbeans.lib.lexer.EmbeddingContainer;
import org.netbeans.lib.lexer.SubSequenceTokenList;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.inc.FilterSnapshotTokenList;
import org.netbeans.lib.lexer.inc.SnapshotTokenList;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Token sequence allows to move between tokens
 * of a token hierarchy in forward/backward direction
 * and by index/offset positioning.
 * <br/>
 * It may be obtained by {@link TokenHierarchy#tokenSequence()}.
 * <br/>
 * A typical use is a forward iteration through the tokens:
 * <pre>
 *   TokenSequence ts = tokenHierarchy.tokenSequence();
 *   // Possible positioning by ts.move()
 *   while (ts.moveNext()) {
 *       Token t = ts.token();
 *       if (t.id() == ...) { ... }
 *       if (TokenUtilities.equals(t.text(), "mytext")) { ... }
 *       if (ts.offset() == ...) { ... }
 *   }
 * </pre>
 * <br/>
 * Token sequence provides correct offset information
 * for the token to which the sequence is positioned
 * (some tokens may be flyweight and do not hold the offset by themselves).
 *
 * <p>
 * This class should be used by a single thread only.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenSequence<T extends TokenId> {
    
    private TokenList<T> tokenList; // 8 + 4 = 12 bytes
    
    private AbstractToken<T> token; // 16 bytes
    
    private int tokenIndex = -1; // 20 bytes
    
    /**
     * Offset in the input at which the current token is located
     * or <code>-1</code> if the offset needs to be computed.
     */
    private int tokenOffset = -1; // 24 bytes

    /**
     * Copy of the modCount of the token list. If the token list's modCount
     * changes (by modification) this token sequence will become invalid.
     */
    private final int modCount; // 28 bytes
    
    /**
     * Parent token indexes allow to effectively determine parent tokens
     * in the tree token hierarchy.
     * <br/>
     * The first index corresponds to the top language in the hierarchy
     * and the ones that follow point to subsequent embedded levels.
     */
    private int[] parentTokenIndexes; // 32 bytes

    /**
     * Package-private constructor used by API accessor.
     */
    TokenSequence(TokenList<T> tokenList) {
        this.tokenList = tokenList;
        this.modCount = tokenList.modCount();
    }

    /**
     * Get the language describing token ids
     * used by tokens in this token sequence.
     */
    public Language<T> language() {
        return LexerUtilsConstants.mostEmbeddedLanguage(languagePath());
    }

    /**
     * Get the complete language path of the tokens contained
     * in this token sequence.
     */
    public LanguagePath languagePath() {
        return tokenList.languagePath();
    }

    /**
     * Get instance of current token to which this token sequence points to.
     * <br/>
     * It is necessary to call {@link #moveNext()} before first calling this method.
     *
     * <p>
     * The returned token instance may be flyweight
     * (returns true from {@link Token#isFlyweight()})
     * which means that its {@link Token#offset(TokenHierarchy)} will return -1.
     * <br/>
     * To find a correct offset use {@link #offset()}.
     * <br/>
     * Or if its necessary to have a non-flyweigt the {@link #offsetToken()}
     * may be used.
     * </p>
     *
     * <p>
     * The lifetime of the returned token instance may be limited for mutable inputs.
     * The token instance should not be held across the input source modifications.
     * </p>
     *
     * @return non-null token instance.
     * @see #offsetToken()
     * @throws IllegalStateException if this token sequence was not positioned
     *  to any token yet.
     */
    public Token<T> token() {
        checkToken();
        return token;
    }
    
    /**
     * Similar to {@link #token()} but always returns a non-flyweight token
     * with the appropriate offset.
     * <br/>
     * If the current token is flyweight then this method replaces it
     * with the corresponding non-flyweight token which it then returns.
     *
     * <p>
     * This method may be handy if the token instance is referenced in a standalone way
     * (e.g. in an expression node of a parse tree) and it's necessary
     * to get the appropriate offset from the token itself
     * later when a token sequence will not be available.
     * </p>
     * @throws IllegalStateException if this token sequence was not positioned
     *  to any token yet.
     */
    public Token<T> offsetToken() {
        checkToken();
        if (token.isFlyweight()) {
            token = tokenList.replaceFlyToken(tokenIndex, token, offset());
        }
        return token;
    }
    
    /**
     * Get the offset of the current token in the underlying input.
     * <br>
     * The token's offset should never be computed by a client of the token sequence
     * by adding/subtracting tokens' length to a client's variable because
     * in case of the immutable token sequences there can be gaps
     * between tokens if some tokens get filtered out.
     * <br>
     * Instead this method should always be used because it offers
     * best performance with a constant time complexity.
     *
     * @return &gt;=0 absolute offset of the current token in the underlying input.
     * @throws IllegalStateException if this token sequence was not positioned
     *  to any token yet.
     */
    public int offset() {
        checkToken();
        if (tokenOffset == -1) {
            tokenOffset = tokenList.tokenOffset(tokenIndex);
        }
        return tokenOffset;
    }
    
    /**
     * Get the index of the current token in the complete list of tokens.
     *
     * @return &gt;=0 index of the current token or <code>-1</code>
     *  if this token sequence is initially located in front of the first token.
     */
    public int index() {
        return tokenIndex;
    }

    /**
     * Get embedded token sequence if the token
     * to which this token sequence is currently positioned
     * has a language embedding.
     * <br/>
     * If there is a custom embedding created by
     * {@link #createEmbedding(Language,int,int)} it will be returned
     * instead of the default embedding
     * (the one created by <code>LanguageHierarchy.embedding()</code>
     * or <code>LanguageProvider</code>).
     *
     * @return embedded sequence or null if no embedding exists for this token.
     * @throws IllegalStateException if this token sequence was not positioned
     *  to any token yet.
     */
    public TokenSequence<? extends TokenId> embedded() {
        checkToken();
        return embeddedImpl(null);
    }
    
    private <ET extends TokenId> TokenSequence<ET> embeddedImpl(Language<ET> embeddedLanguage) {
        TokenList<ET> embeddedTokenList
                = EmbeddingContainer.getEmbedding(tokenList, tokenIndex, embeddedLanguage);
        if (embeddedTokenList != null) {
            TokenList<T> tl = tokenList;
            if (tokenList.getClass() == SubSequenceTokenList.class) {
                tl = ((SubSequenceTokenList<T>)tokenList).delegate();
            }

            if (tl.getClass() == FilterSnapshotTokenList.class) {
                embeddedTokenList = new FilterSnapshotTokenList<ET>(embeddedTokenList,
                        ((FilterSnapshotTokenList<T>)tl).tokenOffsetDiff());

            } else if (tl.getClass() == SnapshotTokenList.class) {
                embeddedTokenList = new FilterSnapshotTokenList<ET>(embeddedTokenList,
                        offset() - token().offset(null));
            }
            return new TokenSequence<ET>(embeddedTokenList);

        } else // Embedded token list does not exist
            return null;
    }

    /**
     * Get embedded token sequence if the token
     * to which this token sequence is currently positioned
     * has a language embedding.
     */
    public <ET extends TokenId> TokenSequence<ET> embedded(Language<ET> embeddedLanguage) {
        checkToken();
        return embeddedImpl(embeddedLanguage);
    }

    /**
     * Create language embedding without joining of the embedded sections.
     *
     * @see #createEmbedding(Language, int, int, boolean)
     */
    public boolean createEmbedding(Language<? extends TokenId> embeddedLanguage,
    int startSkipLength, int endSkipLength) {
        return createEmbedding(embeddedLanguage, startSkipLength, endSkipLength, false);
    }

    /**
     * Create language embedding described by the given parameters.
     * <br/>
     * If the underying text input is mutable then this method should only be called
     * within a read lock over the text input.
     *
     * @param embeddedLanguage non-null embedded language
     * @param startSkipLength &gt;=0 number of characters in an initial part of the token
     *  for which the language embedding is defined that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     * @param endSkipLength &gt;=0 number of characters at the end of the token
     *  for which the language embedding is defined that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     * @param joinSections whether sections with this embedding should be joined
     *  across the input source or whether they should stay separate.
     *  <br/>
     *  For example for HTML sections embedded in JSP this flag should be true:
     *  <pre>
     *   &lt;!-- HTML comment start
     *       &lt;% System.out.println("Hello"); %&gt;
            still in HTML comment --&lt;
     *  </pre>
     *  <br/>
     *  Only the embedded sections with the same language path can be joined.
     * @return true if the embedding was created successfully or false if an embedding
     *  with the given language already exists for this token.
     */
    public boolean createEmbedding(Language<? extends TokenId> embeddedLanguage,
    int startSkipLength, int endSkipLength, boolean joinSections) {
        checkToken();
        return EmbeddingContainer.createEmbedding(tokenList, tokenIndex,
                embeddedLanguage, startSkipLength, endSkipLength, joinSections);
    }

    /**
     * Move to the next token in this token sequence.
     * <br/>
     * The next token may not necessarily start at the offset where
     * the current token ends (there may be gaps between tokens
     * caused by use of a token id filter).
     *
     * @return true if the sequence was successfully moved to the next token
     *  or false if stays on the original token because there are no more tokens
     *  in the forward direction.
     * @throws ConcurrentModificationException if this token sequence
     *  is no longer valid because of an underlying mutable input source modification.
     */
    public boolean moveNext() {
        checkModCount();
        tokenIndex++;
        Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(tokenIndex);
        if (tokenOrEmbeddingContainer != null) {
            AbstractToken origToken = token;
            assignToken(tokenOrEmbeddingContainer);
            if (tokenOffset != -1) {
                // If the token list is continuous or the fetched token
                // is flyweight (there cannot be a gap before flyweight token)
                // the original offset can be just increased
                // by the original token's length.
                if (tokenList.isContinuous() || token.isFlyweight()) {
                    tokenOffset += origToken.length(); // advance by previous token's length
                } else // Offset must be recomputed
                    tokenOffset = -1; // mark the offset to be recomputed
            }
            return true;
        }
        tokenIndex--;
        return false;
    }

    /**
     * Move to the previous token in this token sequence.
     * <br/>
     * The next token may not necessarily end at the offset where
     * the present token starts (there may be gaps between tokens
     * caused by use of a token id filter).
     *
     * @return true if the sequence was successfully moved to the previous token
     *  or false if stayed on the original token because there are no more tokens
     *  in the backward direction.
     * @throws ConcurrentModificationException if this token sequence
     *  is no longer valid because of an underlying mutable input source modification.
     */
    public boolean movePrevious() {
        checkModCount();
        if (tokenIndex > 0) {
            AbstractToken origToken = token;
            tokenIndex--;
            assignToken();
            if (tokenOffset != -1) {
                // If the token list is continuous or the original token
                // is flyweight (there cannot be a gap before flyweight token)
                // the original offset can be just decreased
                // by the fetched token's length.
                if (tokenList.isContinuous() || origToken.isFlyweight()) {
                    tokenOffset -= token.length(); // decrease by the fetched's token length
                } else { // mark the offset to be computed upon call to offset()
                    tokenOffset = -1;
                }
            } else {
                tokenOffset = -1; // mark the offset to be computed upon call to offset()
            }
            return true;

        } // no tokens below index zero
        return false;
    }

    /**
     * Move the token sequence to point to the token with the given index.
     *
     * @param index index of the token to which this sequence
     *   should be positioned.
     * @return <code>true</code> if the sequence was moved to the token
     *   with the given index. Returns <code>false</code>
     *   if <code>index < 0</code> or <code>index < tokenCount</code>.
     *   In such case the current token sequence's position stays unchanged.
     * @throws ConcurrentModificationException if this token sequence
     *  is no longer valid because of an underlying mutable input source modification.
     */
    public boolean moveIndex(int index) {
        checkModCount();
        if (index < 0) {
            return false;
        }
        Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(index);
        if (tokenOrEmbeddingContainer != null) { // enough tokens
            this.tokenIndex = index;
            assignToken(tokenOrEmbeddingContainer);
            tokenOffset = -1;
            return true;

        } else // Token at the requested index does not exist - leave orig. index
            return false;
    }
    
    /**
     * Move the token sequence to be positioned to the token
     * that "contains" the requested offset (the offset is at the begining
     * or inside of the token).
     * <br>
     * If the offset is too big the token sequence will be positioned
     * to the last token and the return value will
     * be the distance between the requested offset
     * and the start offset of the token to which the token sequence
     * will be positioned..
     * <br>
     * If there are no tokens in the sequence then {@link Integer#MAX_VALUE}
     * will be returned.
     *
     * <p>
     * The underlying token list may contain gaps that are not covered
     * by any tokens and if the offset is contained in such gap then
     * the token sequence will be positioned to the token that precedes the gap.
     * </p>
     *
     * Example:
     * <pre>
     *   int diff = tokenSequence.move(targetOffset);
     *   // diff equals to (targetOffset - tokenSequence.token().offset())
     *   if (diff >= 0 && diff < tokenSequence.token().length()) {
     *       // Within token bounds - tokenSequence.token() starts at or "contains" targetOffset
     *
     *   } else if (diff == Integer.MAX_VALUE) {
     *       // No tokens in the token sequence at all.
     *       // Token sequence is not positioned to any token.
     *
     *   } else {
     *       // 1. diff >= tokenSequence.token().length()
     *       //   a) targetOffset is above the end of the last token in the sequence.
     *       //     Token sequence is positioned to the last token in the sequence.
     *       //   b) there are text areas not covered by any tokens
     *       //     due to skipped tokens (skipTokenIds was used
     *       //     in TokenHierarchy.create()) and the targetOffset points to such gap.
     *       //     Token sequence is positioned to the preceding token.
     *       // 
     *       // 2. diff < 0
     *       //   a) targetOffset < 0
     *       //   b) targetOffset >= 0 but there is a text area
     *       //     at the begining that is not covered by any tokens
     *       //     (skipTokenIds was used in TokenHierarchy.create())
     *       //     Token sequence is positioned to the first token in the sequence.
     *   }
     * </pre>
     * 
     *
     * @param offset absolute offset in the input to which
     *  the token sequence should be moved.
     *
     * @return difference between the reqeuested offset
     *  and the absolute starting offset of the token
     *  to which the the token sequence gets moved.
     *  <br>
     *  Returns {@link Integer#MAX_VALUE} if there are no tokens in the sequence.
     *  In such case there is no active token.
     * 
     * @throws ConcurrentModificationException if this token sequence
     *  is no longer valid because of an underlying mutable input source modification.
     */
    public int move(int offset) {
        checkModCount();
        // Token count in the list may change as possibly other threads
        // keep asking for tokens. Root token list impls create tokens lazily
        // when asked by clients.
        int tokenCount = tokenList.tokenCountCurrent(); // presently created token count
        if (tokenCount == 0) { // no tokens yet -> attempt to create at least one
            if (tokenList.tokenOrEmbeddingContainer(0) == null) { // really no tokens at all
                // In this case the token sequence could not be positioned yet
                // so no need to reset "index" or other vars
                return Integer.MAX_VALUE;
            }
            // Re-get the present token count (could be created a chunk of tokens at once)
            tokenCount = tokenList.tokenCountCurrent();
        }

        // tokenCount surely >0
        int prevTokenOffset = tokenList.tokenOffset(tokenCount - 1);
        if (offset > prevTokenOffset) { // may need to create further tokens if they do not exist
            // Force token list to create subsequent tokens
            // Cannot subtract offset by each token's length because
            // there may be gaps between tokens due to token id filter use.
            int tokenLength = LexerUtilsConstants.token(tokenList, tokenCount - 1).length();
            while (offset >= prevTokenOffset + tokenLength) { // above present token
                Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(tokenCount);
                if (tokenOrEmbeddingContainer != null) {
                    AbstractToken t = LexerUtilsConstants.token(tokenOrEmbeddingContainer);
                    if (t.isFlyweight()) { // need to use previous tokenLength
                        prevTokenOffset += tokenLength;
                    } else { // non-flyweight token - retrieve offset
                        prevTokenOffset = tokenList.tokenOffset(tokenCount);
                    }
                    tokenLength = t.length();
                    tokenCount++;

                } else { // no more tokens => break
                    break;
                }
            }
            tokenIndex = tokenCount - 1;
            // Absolute token's start offset 
            tokenOffset = prevTokenOffset;
            assignToken();
            return offset - prevTokenOffset;
        }
        
        // The offset is within the currently recognized tokens
        // Use binary search
        int low = 0;
        int high = tokenCount - 1;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            int midStartOffset = tokenList.tokenOffset(mid);
            
            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else {
                // Token starting exactly at offset found
                tokenIndex = mid;
                tokenOffset = midStartOffset;
                assignToken();
                return 0; // right at the token begining
            }
        }
        
        // Not found exactly and high + 1 == low => high < low
        // Check whether the token at "high" contains the offset
        if (high >= 0) { // could be -1
            AbstractToken t = LexerUtilsConstants.token(tokenList, high);
            prevTokenOffset = tokenList.tokenOffset(high);
        } else { // at least one token exists => use token at index 0
            high = 0;
            prevTokenOffset = tokenList.tokenOffset(0); // result may differ from 0
        }

        tokenIndex = high;
        tokenOffset = prevTokenOffset;
        assignToken();
        return offset - prevTokenOffset;
    }

    /**
     * Move to the first token in this token sequence.
     *
     * @return true if the sequence was positioned on the first token
     *  or false if there are no tokens in the sequence.
     */
    public boolean moveFirst() {
        return moveIndex(0);
    }
    
    /**
     * Move to the last token in this token sequence.
     *
     * @return true if the sequence was positioned on the last token
     *  or false if there are no tokens in the sequence.
     */
    public boolean moveLast() {
        return moveIndex(tokenCount() - 1); // Can be -1 but handled in move(index)
    }

    /**
     * Return total count of tokens in this sequence.
     * <br>
     * <b>Note:</b> Calling this method will lead
     * to creation of all the remaining tokens in the sequence
     * if they were not yet created.
     *
     * @return total number of tokens in this token sequence.
     */
    public int tokenCount() {
        checkModCount();
        return tokenList.tokenCount();
    }
    
    /**
     * Create sub sequence of this token sequence that only returns
     * tokens above the given offset.
     *
     * @param startOffset only tokens satisfying
     *  <code>tokenStartOffset + tokenLength > startOffset</code>
     *  will be present in the returned sequence.
     * @return non-null sub sequence of this token sequence.
     */
    public TokenSequence<T> subSequence(int startOffset) {
        return subSequence(startOffset, Integer.MAX_VALUE);
    }
    
    /**
     * Create sub sequence of this token sequence that only returns
     * tokens between the given offsets.
     *
     * @param startOffset only tokens satisfying
     *  <code>tokenStartOffset + tokenLength > startOffset</code>
     *  will be present in the returned sequence.
     * @param endOffset >=startOffset only tokens satisfying
     *  <code>tokenStartOffset < endOffset</code>
     *  will be present in the returned sequence.
     * @return non-null sub sequence of this token sequence.
     */
    public TokenSequence<T> subSequence(int startOffset, int endOffset) {
        checkModCount(); // Ensure subsequences on valid token sequences only
        TokenList<T> tl;
        if (tokenList.getClass() == SubSequenceTokenList.class) {
            SubSequenceTokenList<T> stl = (SubSequenceTokenList<T>)tokenList;
            tl = stl.delegate();
            startOffset = Math.max(startOffset, stl.limitStartOffset());
            endOffset = Math.min(endOffset, stl.limitEndOffset());
        } else // Regular token list
            tl = tokenList;
        return new TokenSequence<T>(new SubSequenceTokenList<T>(tl, startOffset, endOffset));
    }
    
    public String toString() {
        return LexerUtilsConstants.appendTokenList(null, tokenList, tokenIndex).toString();
    }
    
    int[] parentTokenIndexes() {
        return parentTokenIndexes;
    }

    private void assignToken(Object tokenOrEmbeddingContainer) {
        token = LexerUtilsConstants.token(tokenOrEmbeddingContainer);
    }
    
    private void assignToken() {
        assignToken(tokenList.tokenOrEmbeddingContainer(tokenIndex));
    }

    private void checkToken() {
        if (token == null) {
            throw new IllegalStateException(
                "No token fetched by moveNext() from token sequence yet: index=" + tokenIndex
            ); // NOI18N
        }
    }
    
    private void checkModCount() {
        if (tokenList.modCount() != this.modCount) {
            throw new ConcurrentModificationException(
                "This token sequence is no longer valid. Underlying token hierarchy" // NOI18N
              + " has been modified: " + this.modCount + " != " + tokenList.modCount() // NOI18N
            );
        }
    }

}