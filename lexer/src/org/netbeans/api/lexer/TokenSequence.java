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

import java.util.ConcurrentModificationException;
import org.netbeans.lib.lexer.EmbeddingContainer;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.SubSequenceTokenList;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Token sequence allows to iterate between tokens
 * of a token hierarchy.
 * <br/>
 * Token sequence for top-level language of a token hierarchy
 * may be obtained by {@link TokenHierarchy#tokenSequence()}.
 * 
 * <p>
 * Use of token sequence is a two-step operation:
 * <ol>
 *   <li>
 *     Position token sequence before token that should first be retrieved
 *     (or behind desired token when iterating backwards).
 *     <br/>
 *     One of the following ways may be used:
 *     <ul>
 *       <li> {@link #move(int)} positions TS before token that either starts
 *           at the given offset or "contains" it.
 *       </li>
 *       <li> {@link #moveIndex(int)} positions TS before n-th token in the underlying
 *           token list.
 *       </li>
 *       <li> {@link #moveStart()} positions TS before the first token. </li>
 *       <li> {@link #moveEnd()} positions TS behind the last token. </li>
 *       <li> Do nothing - TS is positioned before the first token automatically by default. </li>
 *     </ul>
 *     Token sequence will always be positioned between tokens
 *     when using one of the operations above
 *     ({@link #token()} will return <code>null</code> to signal between-tokens location).
 *     <br/>
 *   </li>
 * 
 *   <li>
 *     Start iterating through the tokens in forward/backward direction
 *     by using {@link #moveNext()} or {@link #movePrevious()}.
 *     <br/>
 *     If <code>moveNext()</code> or <code>movePrevious()</code> returned
 *     <code>true</code> then TS is positioned
 *     over a concrete token retrievable by {@link #token()}.
 *     <br/>
 *     Its offset can be retrieved by {@link #offset()}.
 *   </li>
 * </ol>
 * </p>
 * 
 * <p>
 * An example of forward iteration through the tokens:
 * <pre>
 *   TokenSequence ts = tokenHierarchy.tokenSequence();
 *   // Possible positioning by ts.move(offset) or ts.moveIndex(index)
 *   while (ts.moveNext()) {
 *       Token t = ts.token();
 *       if (t.id() == ...) { ... }
 *       if (TokenUtilities.equals(t.text(), "mytext")) { ... }
 *       if (ts.offset() == ...) { ... }
 *   }
 * </pre>
 * </p>
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
    
    private int tokenIndex; // 20 bytes
    
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
        return LexerUtilsConstants.innerLanguage(languagePath());
    }

    /**
     * Get the complete language path of the tokens contained
     * in this token sequence.
     */
    public LanguagePath languagePath() {
        return tokenList.languagePath();
    }

    /**
     * Get token to which this token sequence points to or null
     * if TS is positioned between tokens
     * ({@link #moveNext()} or {@link #movePrevious()} were not called yet).
     * <br/>
     * A typical iteration usage:
     * <pre>
     *   TokenSequence ts = tokenHierarchy.tokenSequence();
     *   // Possible positioning by ts.move(offset) or ts.moveIndex(index)
     *   while (ts.moveNext()) {
     *       Token t = ts.token();
     *       if (t.id() == ...) { ... }
     *       if (TokenUtilities.equals(t.text(), "mytext")) { ... }
     *       if (ts.offset() == ...) { ... }
     *   }
     * </pre>
     *
     * The returned token instance may be flyweight
     * ({@link Token#isFlyweight()} returns true)
     * which means that its {@link Token#offset(TokenHierarchy)} will return -1.
     * <br/>
     * To find a correct offset use {@link #offset()}.
     * <br/>
     * Or if its necessary to revert to a regular non-flyweigt token
     * the {@link #offsetToken()} may be used.
     * </p>
     *
     * <p>
     * The lifetime of the returned token instance may be limited for mutable inputs.
     * The token instance should not be held across the input source modifications.
     * </p>
     *
     * @return token instance to which this token sequence is currently positioned
     *  or null if this token sequence is not positioned to any token which may
     *  happen after TS creation or after use of {@link #move(int)} or {@link #moveIndex(int)}.
     * 
     * @see #offsetToken()
     */
    public Token<T> token() {
        return token;
    }
    
    /**
     * Similar to {@link #token()} but always returns a non-flyweight token
     * with the appropriate offset.
     * <br/>
     * If the current token is flyweight then this method replaces it
     * with the corresponding non-flyweight token which it then returns.
     * <br/>
     * Subsequent calls to {@link #token()} will also return this non-flyweight token.
     *
     * <p>
     * This method may be handy if the token instance is referenced in a standalone way
     * (e.g. in an expression node of a parse tree) and it's necessary
     * to get the appropriate offset from the token itself
     * later when a token sequence will not be available.
     * </p>
     * @throws IllegalStateException if {@link #token()} returns null.
     */
    public Token<T> offsetToken() {
        checkTokenNotNull();
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
     * @throws IllegalStateException if {@link #token()} returns null.
     */
    public int offset() {
        checkTokenNotNull();
        if (tokenOffset == -1) {
            tokenOffset = tokenList.tokenOffset(tokenIndex);
        }
        return tokenOffset;
    }
    
    /**
     * Get an index of token to which (or before which) this TS is currently positioned.
     * <br/>
     * <p>
     * Initially or after {@link #move(int)} or {@link #moveIndex(int)}
     * token sequence is positioned between tokens:
     * <pre>
     *          Token[0]   Token[1]   ...   Token[n]
     *        ^          ^                ^
     * Index: 0          1                n
     * </pre>
     * </p>
     * 
     * <p>
     * After use of {@link #moveNext()} or {@link #movePrevious()}
     * the token sequence is positioned over one of the actual tokens:
     * <pre>
     *          Token[0]   Token[1]   ...   Token[n]
     *             ^          ^                ^
     * Index:      0          1                n
     * </pre>
     * </p>
     * 
     * @return &gt;=0 index of token to which (or before which) this TS is currently positioned.
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
     * @throws IllegalStateException if {@link #token()} returns null.
     */
    public TokenSequence<? extends TokenId> embedded() {
        checkTokenNotNull();
        return embeddedImpl(null);
    }
    
    private <ET extends TokenId> TokenSequence<ET> embeddedImpl(Language<ET> embeddedLanguage) {
        if (token.isFlyweight())
            return null;
        TokenList<ET> embeddedTokenList = LexerUtilsConstants.embeddedTokenList(
                tokenList, tokenIndex, embeddedLanguage);
        return (embeddedTokenList != null)
            ? new TokenSequence<ET>(embeddedTokenList)
            : null;
    }

    /**
     * Get embedded token sequence if the token
     * to which this token sequence is currently positioned
     * has a language embedding.
     * 
     * @throws IllegalStateException if {@link #token()} returns null.
     */
    public <ET extends TokenId> TokenSequence<ET> embedded(Language<ET> embeddedLanguage) {
        checkTokenNotNull();
        return embeddedImpl(embeddedLanguage);
    }

    /**
     * Create language embedding without joining of the embedded sections.
     *
     * @throws IllegalStateException if {@link #token()} returns null.
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
     * within a write lock over the text input.
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
     * @throws IllegalStateException if {@link #token()} returns null.
     */
    public boolean createEmbedding(Language<? extends TokenId> embeddedLanguage,
    int startSkipLength, int endSkipLength, boolean joinSections) {
        checkTokenNotNull();
        return EmbeddingContainer.createEmbedding(tokenList, tokenIndex,
                embeddedLanguage, startSkipLength, endSkipLength, joinSections);
    }

    /**
     * Move to the next token in this token sequence.
     * 
     * <p>
     * The next token may not necessarily start at the offset where
     * the previous token ends (there may be gaps between tokens
     * caused by token filtering). {@link #offset()} should be used
     * for offset retrieval.
     * </p>
     *
     * @return true if the sequence was successfully moved to the next token
     *  or false if it was not moved before there are no more tokens
     *  in the forward direction.
     * @throws ConcurrentModificationException if this token sequence
     *  is no longer valid because of an underlying mutable input source modification.
     */
    public boolean moveNext() {
        checkModCount();
        if (token != null) // Token already fetched
            tokenIndex++;
        Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(tokenIndex);
        if (tokenOrEmbeddingContainer != null) {
            AbstractToken origToken = token;
            token = LexerUtilsConstants.token(tokenOrEmbeddingContainer);
            // If origToken == null then the right offset might already be pre-computed from move()
            if (tokenOffset != -1) {
                if (origToken != null) {
                    // If the token list is continuous or the fetched token
                    // is flyweight (there cannot be a gap before flyweight token)
                    // the original offset can be just increased
                    // by the original token's length.
                    if (tokenList.isContinuous() || token.isFlyweight()) {
                        tokenOffset += origToken.length(); // advance by previous token's length
                    } else // Offset must be recomputed
                        tokenOffset = -1; // mark the offset to be recomputed
                } else // Not valid token previously
                    tokenOffset = -1;
            }
            return true;
        }
        if (token != null) // Unsuccessful move from existing token
            tokenIndex--;
        return false;
    }

    /**
     * Move to a previous token in this token sequence.
     *
     * <p>
     * The previous token may not necessarily end at the offset where
     * the previous token started (there may be gaps between tokens
     * caused by token filtering). {@link #offset()} should be used
     * for offset retrieval.
     * </p>
     *
     * @return true if the sequence was successfully moved to the previous token
     *  or false if it was not moved because there are no more tokens
     *  in the backward direction.
     * @throws ConcurrentModificationException if this token sequence
     *  is no longer valid because of an underlying mutable input source modification.
     */
    public boolean movePrevious() {
        checkModCount();
        if (tokenIndex > 0) {
            AbstractToken origToken = token;
            tokenIndex--;
            token = LexerUtilsConstants.token(tokenList.tokenOrEmbeddingContainer(tokenIndex));
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
            }
            return true;

        } // no tokens below index zero
        return false;
    }

    /**
     * Position token sequence between <code>index-1</code>
     * and <code>index</code> tokens.
     * <br/>
     * TS will be positioned in the following way:
     * <pre>
     *          Token[0]   ...   Token[index-1]   Token[index] ...
     *        ^                ^                ^
     * Index: 0             index-1           index
     * </pre>
     * 
     * <p>
     * Subsequent {@link #moveNext()} or {@link #movePrevious()} is needed to fetch
     * a concrete token in the desired direction.
     * <br/>
     * Subsequent {@link #moveNext()} will position TS over <code>Token[index]</code>
     * (or {@link #movePrevious()} will position TS over <code>Token[index-1]</code>)
     * so that <code>{@link #token()} != null</code>.
     *
     * @param index index of the token to which this sequence
     *   should be positioned.
     *   <br/>
     *   If <code>index >= {@link #tokenCount()}</code>
     *   then the TS will be positioned to {@link #tokenCount()}.
     *   <br/>
     *   If <code>index < 0</code> then the TS will be positioned to index 0.
     * 
     * @return difference between requested index and the index to which TS
     *   is really set.
     * @throws ConcurrentModificationException if this token sequence
     *  is no longer valid because of an underlying mutable input source modification.
     */
    public int moveIndex(int index) {
        checkModCount();
        if (index >= 0) {
            Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(index);
            if (tokenOrEmbeddingContainer != null) { // enough tokens
                resetTokenIndex(index);
            } else // Token at the requested index does not exist - leave orig. index
                resetTokenIndex(tokenCount());
        } else // index < 0
            resetTokenIndex(0);
        return index - tokenIndex;
    }
    
    /**
     * Move the token sequence to be positioned before the first token.
     * <br/>
     * This is equivalent to <code>moveIndex(0)</code>.
     */
    public void moveStart() {
        moveIndex(0);
    }
    
    /**
     * Move the token sequence to be positioned behind the last token.
     * <br/>
     * This is equivalent to <code>moveIndex(tokenCount())</code>.
     */
    public void moveEnd() {
        moveIndex(tokenCount());
    }
    
    /**
     * Move token sequence to be positioned between <code>index-1</code>
     * and <code>index</code> tokens where Token[index] either starts at offset
     * or "contains" the offset.
     * <br/>
     * <pre>
     *        +----------+-----+----------------+--------------+------
     *        | Token[0] | ... | Token[index-1] | Token[index] | ...
     *        | "public" | ... | "static"       | "int"        | ...
     *        +----------+-----+----------------+--------------+------
     *        ^                ^                ^
     * Index: 0             index-1           index
     * Offset:                                  ---^ (if offset points to 'i','n' or 't')
     * </pre>
     * 
     * <p>
     * Subsequent {@link #moveNext()} or {@link #movePrevious()} is needed to fetch
     * a concrete token.
     * <br/>
     * If the offset is too big then the token sequence will be positioned
     * behind the last token.
     * </p>
     * 
     * <p>
     * If token filtering is used there may be gaps that are not covered
     * by any tokens and if the offset is contained in such gap then
     * the token sequence will be positioned before the token that follows the gap.
     * </p>
     *
     *
     * @param offset absolute offset to which the token sequence should be moved.
     * @return difference between the reqeuested offset
     *  and the start offset of the token
     *  before which the the token sequence gets positioned.
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
                resetTokenIndex(0);
                return offset;
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

                } else { // no more tokens => position behind last token
                    resetTokenIndex(tokenCount);
                    tokenOffset = prevTokenOffset + tokenLength; // May assign the token's offset in advance
                    return offset - tokenOffset;
                }
            }
            resetTokenIndex(tokenCount - 1);
            tokenOffset = prevTokenOffset; // May assign the token's offset in advance
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
                resetTokenIndex(mid);
                tokenOffset = midStartOffset;
                return 0; // right at the token begining
            }
        }
        
        // Not found exactly and high + 1 == low => high < low
        // BTW there may be gaps between tokens; if offset is in gap then position to higher token
        if (high >= 0) { // could be -1
            AbstractToken t = LexerUtilsConstants.token(tokenList, high);
            prevTokenOffset = tokenList.tokenOffset(high);
            // If gaps allowed check whether the token at "high" contains the offset
            if (!tokenList.isContinuous() && offset > prevTokenOffset + t.length()) {
                // Offset in the gap above the "high" token
                high++;
                prevTokenOffset += t.length();
            }
        } else { // at least one token exists => use token at index 0
            high = 0;
            prevTokenOffset = tokenList.tokenOffset(0); // result may differ from 0
        }
        resetTokenIndex(high);
        tokenOffset = prevTokenOffset;
        return offset - prevTokenOffset;
    }
    
    /**
     * Check whether this TS contains zero tokens.
     * <br/>
     * This check is strongly preferred over <code>tokenCount() == 0</code>.
     * 
     * @see #tokenCount()
     */
    public boolean isEmpty() {
        return (tokenIndex == 0 && tokenList.tokenOrEmbeddingContainer(0) == null);
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

    private void resetTokenIndex(int index) {
        // Position to the given index e.g. by move() and moveIndex()
        tokenIndex = index;
        token = null;
        tokenOffset = -1;
    }

    private void checkTokenNotNull() {
        if (token == null) {
            throw new IllegalStateException(
                "Caller of TokenSequence forgot to call moveNext(): tokenIndex=" + tokenIndex
            ); // NOI18N
        }
    }
    
    private void checkModCount() {
        if (tokenList.modCount() != this.modCount) {
            throw new ConcurrentModificationException(
                "Caller uses token sequence which is no longer valid. Underlying token hierarchy" // NOI18N
              + " has been modified: " + this.modCount + " != " + tokenList.modCount() // NOI18N
            );
        }
    }

}