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

package org.netbeans.lib.lexer;

import java.util.Set;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Filtering token list used by a token sub sequence.
 * <br/>
 * As the tokens are created lazily this list won't call tokenList.tokenCount()
 * until tokenCount() is called on itself.
 *
 * <p>
 * This list assumes single-threaded use only.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class SubSequenceTokenList<T extends TokenId> implements TokenList<T> {
    
    /**
     * Token list to which this filtering token list delegates.
     */
    private TokenList<T> tokenList;
    
    /**
     * Last retrieved token's end offset.
     */
    private AbstractToken<T> lastToken;
    
    /**
     * Last retrieved token index.
     */
    private int lastTokenIndex;
    
    /**
     * Last retrieved token's offset.
     */
    private int lastTokenOffset;
    
    /**
     * Limit of start offset under which the token sequence cannot move.
     * Integer.MIN_VALUE for no limit.
     */
    private final int limitStartOffset;
    
    /**
     * Limit of the end offset under which the token sequence cannot move.
     * Integer.MAX_VALUE for no limit.
     */
    private final int limitEndOffset;

    /**
     * Index of a first token in the underlying token list that this list provides.
     */
    private int limitStartIndex;
    
    /**
     * Initially Integer.MAX_VALUE to be computed lazily.
     */
    private int limitEndIndex;
    
    public SubSequenceTokenList(TokenList<T> tokenList, int limitStartOffset, int limitEndOffset) {
        this.tokenList = tokenList;
        this.limitStartOffset = limitStartOffset;
        this.limitEndOffset = limitEndOffset;
        
        // Compute limitStartIndex
        if (limitStartOffset > 0) {
            int diff = move(limitStartOffset);
            if (diff != Integer.MAX_VALUE) { // some tokens exist
                if (diff >= lastToken.length()) { // lastToken initialized in move()
                    lastTokenIndex++;
                    Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(lastTokenIndex);
                    if (tokenOrEmbeddingContainer != null &&
                        (lastTokenOffset = tokenList.tokenOffset(lastTokenIndex)) < limitEndOffset
                    ) {
                        lastToken = LexerUtilsConstants.token(tokenOrEmbeddingContainer);
                        limitStartIndex = lastTokenIndex;
                        limitEndIndex = Integer.MAX_VALUE; // To be computed later
                    } // Otherwise limitStartIndex and limitEndIndex remain zero => no tokens

                } else { // Check if the token is not below end offset limit
                    if (limitEndOffset == Integer.MAX_VALUE || lastTokenOffset < limitEndOffset) {
                        limitStartIndex = lastTokenIndex;
                        limitEndIndex = Integer.MAX_VALUE; // To be computed later
                    } // Otherwise limitStartIndex and limitEndIndex remain zero => no tokens
                }
            }  // Otherwise limitStartIndex and limitEndIndex remain zero => no tokens

        } else {// Lower bound is zero => limitStartIndex is zero
            // Check first token (done here for simpler tokenCount() etc.)
            Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(0);
            if (tokenOrEmbeddingContainer != null && (lastTokenOffset = tokenList.tokenOffset(0)) < limitEndOffset) {
                lastToken = LexerUtilsConstants.token(tokenOrEmbeddingContainer); // lastTokenIndex remains zero
                limitEndIndex = Integer.MAX_VALUE;
            } // Otherwise limitEndIndex remain zero => no tokens
        }
    }

    public TokenList<T> delegate() {
        return tokenList;
    }
    
    public int limitStartOffset() {
        return limitStartOffset;
    }
    
    public int limitEndOffset() {
        return limitEndOffset;
    }
    
    public Object tokenOrEmbeddingContainer(int index) {
        if (limitStartIndex == -1) // No tokens
            return null;
        index += limitStartIndex; // Shift to underlying tokenList indices
        if (limitEndIndex == Integer.MAX_VALUE) { // Not initialized yet
            switch (index - lastTokenIndex) {
                case -1: // Prev to lastToken - must exist
                    if (index < limitStartIndex)
                        return null;
                    Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(index);
                    AbstractToken<T> token = LexerUtilsConstants.token(tokenOrEmbeddingContainer);
                    lastTokenIndex = index;
                    // If the token list is continuous or the original token
                    // is flyweight (there cannot be a gap before flyweight token)
                    // the original offset can be just decreased
                    // by the fetched token's length.
                    if (tokenList.isContinuous() || lastToken.isFlyweight())
                        lastTokenOffset = lastTokenOffset - token.length();
                    else // Compute offset through tokenList
                        lastTokenOffset = tokenList.tokenOffset(index);
                    lastToken = token;
                    return tokenOrEmbeddingContainer;

                case 0: // Last token
                    return lastToken;

                case 1: // Next to lastToken
                    tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(index);
                    if (tokenOrEmbeddingContainer != null) {
                        token = LexerUtilsConstants.token(tokenOrEmbeddingContainer);
                        // If the token list is continuous or the fetched token
                        // is flyweight (there cannot be a gap before flyweight token)
                        // the original offset can be just increased
                        // by the original token's length.
                        int tokenOffset;
                        if (tokenList.isContinuous() || token.isFlyweight())
                            tokenOffset = lastTokenOffset + lastToken.length();
                        else // Offset must be recomputed
                            tokenOffset = tokenList.tokenOffset(index);
                        // Check the offset to be below upper bound
                        if (tokenOffset < limitEndOffset) { // below upper bound
                            lastToken = token;
                            lastTokenIndex = index;
                            lastTokenOffset = tokenOffset;
                            return tokenOrEmbeddingContainer;
                        } // above upper bound
                    }
                    limitEndIndex = index; // lastToken at prev index was valid so may assign this
                    return null;

                default: // Not related to lastToken
                    tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(index);
                    if (tokenOrEmbeddingContainer != null) {
                        int tokenOffset = tokenList.tokenOffset(index);
                        // Check the offset to be below upper bound
                        if (tokenOffset < limitEndOffset) { // below upper offset bound
                            lastToken = LexerUtilsConstants.token(tokenOrEmbeddingContainer);
                            lastTokenIndex = index;
                            lastTokenOffset = tokenOffset;
                            return tokenOrEmbeddingContainer;
                        } // >=limitEndOffset
                    } // index too high
                    // As the null gets returned all the tokens that could
                    // possibly be lazily created would already got initialized anyway.
                    // Call tokenCount() to initialize limitEndIndex and not duplicate
                    // optimizations similar to the ones in TokenSequence
                    // for offset retrieval here.
                    tokenCount();
                    return null;
            }

        } else { // limitEndIndex already inited (won't be -1 - checked above)
            // As limitEndIndex is inited it will no longer use lastToken caching
            // because TokenSequence will use its own similar caching for token offsets.
            return (index < limitEndIndex)
                ? tokenList.tokenOrEmbeddingContainer(index)
                : null;
        }
    }

    public int tokenOffset(int index) {
        index += limitStartIndex;
        if (index == lastTokenIndex) {
            return lastTokenOffset;
        }
        return tokenList.tokenOffset(index);
    }

    public int tokenCount() {
        if (limitEndIndex == Integer.MAX_VALUE) { // Not computed yet
            // Position to lower offset but retain diff against exact limitEndOffset
            int diff = move(limitEndOffset - 1);
            assert (diff != Integer.MAX_VALUE); // Should already be handled in constructor
            limitEndIndex = lastTokenIndex + 1; // add extra 1 to become end index
        }
        return limitEndIndex - limitStartIndex;
    }

    public int tokenCountCurrent() {
        if (limitEndIndex != Integer.MAX_VALUE) // Handle no tokens properly
            return tokenCount();
        int tcc = tokenList.tokenCountCurrent(); // cannot be < limitStartIndex due to constructor
        if (tokenOffset(tcc - 1 - limitStartIndex) >= limitEndOffset) // Above limit
            return tokenCount();
        return tcc - limitStartIndex;
    }

    public AbstractToken<T> replaceFlyToken(int index, AbstractToken<T> flyToken, int offset) {
        return tokenList.replaceFlyToken(index + limitStartIndex, flyToken, offset);
    }

    public int modCount() {
        return tokenList.modCount();
    }

    public LanguagePath languagePath() {
        return tokenList.languagePath();
    }

    public int childTokenOffset(int rawOffset) {
        throw new IllegalStateException("Unexpected call.");
    }

    public char childTokenCharAt(int rawOffset, int index) {
        throw new IllegalStateException("Unexpected call.");
    }

    public void wrapToken(int index, EmbeddingContainer<T> embeddingContainer) {
        tokenList.wrapToken(limitStartIndex + index, embeddingContainer);
    }

    public TokenList<? extends TokenId> root() {
        return tokenList.root();
    }

    public TokenHierarchyOperation<?,? extends TokenId> tokenHierarchyOperation() {
        return tokenList.tokenHierarchyOperation();
    }
    
    public InputAttributes inputAttributes() {
        return tokenList.inputAttributes();
    }

    public int lookahead(int index) {
        // Can be used by LexerTestUtilities.lookahead()
        return tokenList.lookahead(index);
    }

    public Object state(int index) {
        return tokenList.state(index);
    }

    public boolean isContinuous() {
        return tokenList.isContinuous();
    }

    public Set<T> skipTokenIds() {
        return tokenList.skipTokenIds();
    }
    
    private AbstractToken<T> token(int index) {
        return LexerUtilsConstants.token(tokenList, index);
    }
    
    /**
     * Find the token index for the given offset and place it into lastTokenIndex.
     * <br/>
     * Diff between requested offset and offset of the token at lastTokenIndex
     * is returned.
     * <br/>
     * Returns Integer.MAX_VALUE if there are no tokens in the underlying
     * token list.
     */
    private int move(int offset) {
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
        lastTokenOffset = tokenList.tokenOffset(tokenCount - 1);
        if (offset > lastTokenOffset) { // may need to create further tokens if they do not exist
            // Force token list to create subsequent tokens
            // Cannot subtract offset by each token's length because
            // there may be gaps between tokens due to token id filter use.
            lastToken = token(tokenCount - 1);
            int tokenLength = lastToken.length();
            while (offset >= lastTokenOffset + tokenLength) { // above present token
                Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(tokenCount);
                if (tokenOrEmbeddingContainer != null) {
                    lastToken = LexerUtilsConstants.token(tokenOrEmbeddingContainer);
                    if (lastToken.isFlyweight()) { // need to use previous tokenLength
                        lastTokenOffset += tokenLength;
                    } else { // non-flyweight token - retrieve offset
                        lastTokenOffset = tokenList.tokenOffset(tokenCount);
                    }
                    tokenLength = lastToken.length();
                    tokenCount++;

                } else { // no more tokens => break
                    break;
                }
            }
            lastTokenIndex = tokenCount - 1;
            return offset - lastTokenOffset;
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
            } else { // Token starting exactly at offset found
                lastToken = token(mid);
                lastTokenIndex = mid;
                lastTokenOffset = midStartOffset;
                return 0; // right at the token begining
            }
        }
        
        // Not found exactly and high + 1 == low => high < low
        // Check whether the token at "high" contains the offset
        if (high < 0) { // could be -1
            high = 0;
        }
        lastToken = token(high);
        lastTokenOffset = tokenList.tokenOffset(high);
        lastTokenIndex = high;
        return offset - lastTokenOffset;
    }

}