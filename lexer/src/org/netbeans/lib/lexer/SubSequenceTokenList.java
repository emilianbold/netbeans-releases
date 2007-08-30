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
    
    public static <T extends TokenId> SubSequenceTokenList<T> create(
    TokenList<T> tokenList, int limitStartOffset, int limitEndOffset) {
        return new SubSequenceTokenList<T>(tokenList, limitStartOffset, limitEndOffset);
    }
    
    /**
     * Token list to which this filtering token list delegates.
     */
    private TokenList<T> tokenList;
    
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
    
    /**
     * Create new subsequence token list
     * @param tokenList non-null token list over which the subsequence gets created.
     * @param limitStartOffset lower offset bound offset or 0 for none.
     * @param limitEndOffset upper offset bound or Integer.MAX_VALUE for none.
     */
    public SubSequenceTokenList(TokenList<T> tokenList, int limitStartOffset, int limitEndOffset) {
        this.tokenList = tokenList;
        this.limitStartOffset = limitStartOffset;
        this.limitEndOffset = limitEndOffset;

        if (limitEndOffset == Integer.MAX_VALUE) { // No limit
            // No upper bound for end index so use tokenCount() (can be improved if desired)
            limitEndIndex = tokenList.tokenCount();
        } else { // Valid limit end offset
            limitEndIndex = tokenList.tokenCountCurrent(); // presently created token count
            if (limitEndIndex == 0) { // no tokens yet -> attempt to create at least one
                if (tokenList.tokenOrEmbeddingContainer(0) != null) { // some tokens exist
                    // Re-get the present token count (could be created a chunk of tokens at once)
                    limitEndIndex = tokenList.tokenCountCurrent();
                }
            }
            
            if (limitEndIndex > 0) {
                // tokenCount surely >0
                int tokenOffset = tokenList.tokenOffset(limitEndIndex - 1);
                if (limitEndOffset > tokenOffset) { // may need to create further tokens if they do not exist
                    // Force token list to create subsequent tokens
                    // Cannot subtract offset by each token's length because
                    // there may be gaps between tokens due to token id filter use.
                    AbstractToken<?> token = token(limitEndIndex - 1);
                    int tokenLength = token.length();
                    while (limitEndOffset >= tokenOffset + tokenLength) { // above present token
                        Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(limitEndIndex);
                        if (tokenOrEmbeddingContainer != null) {
                            token = LexerUtilsConstants.token(tokenOrEmbeddingContainer);
                            if (tokenList.isContinuous() || token.isFlyweight()) {
                                tokenOffset += tokenLength;
                            } else { // retrieve offset
                                tokenOffset = tokenList.tokenOffset(limitEndIndex);
                            }
                            tokenLength = token.length();
                            limitEndIndex++;
                        } else { // no more tokens => break
                            break;
                        }
                    }

                } else { // end index within existing tokens
                    // The offset is within the currently recognized tokens
                    // Use binary search
                    int low = 0;
                    limitEndIndex--;

                    while (low <= limitEndIndex) {
                        int mid = (low + limitEndIndex) / 2;
                        int midStartOffset = tokenList.tokenOffset(mid);

                        if (midStartOffset < limitEndOffset) {
                            low = mid + 1;
                        } else if (midStartOffset > limitEndOffset) {
                            limitEndIndex = mid - 1;
                        } else { // Token starting exactly at offset found
                            limitEndIndex = mid - 1;
                            break;
                        }
                    }
                    limitEndIndex++; // Increase from 'high' to end index
                }
            }
        }
            
        // Compute limitStartIndex (currently == 0)
        if (limitEndIndex > 0 && limitStartOffset > 0) {
            int high = limitEndIndex - 1;
            while (limitStartIndex <= high) {
                int mid = (limitStartIndex + high) / 2;
                int midStartOffset = tokenList.tokenOffset(mid);

                if (midStartOffset < limitStartOffset) {
                    limitStartIndex = mid + 1;
                } else if (midStartOffset > limitStartOffset) {
                    high = mid - 1;
                } else { // Token starting exactly at offset found
                    limitStartIndex = mid + 1;
                    break;
                }
            }
            // Include previous token if it "includes" limitStartOffset (also handles gaps between tokens properly)
            if (tokenList.tokenOffset(limitStartIndex - 1) + token(limitStartIndex - 1).length() > limitStartOffset) {
                limitStartIndex--;
            }
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
        index += limitStartIndex;
        return (index < limitEndIndex)
            ? tokenList.tokenOrEmbeddingContainer(index)
            : null;
    }

    public int tokenOffset(int index) {
        index += limitStartIndex;
        if (index >= limitEndIndex)
            throw new IndexOutOfBoundsException("index=" + index + " >= limitEndIndex=" + limitEndIndex);
        return tokenList.tokenOffset(index);
    }

    public int tokenCount() {
        return limitEndIndex - limitStartIndex;
    }

    public int tokenCountCurrent() {
        return limitEndIndex - limitStartIndex;
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
    
    public int startOffset() {
        if (tokenCountCurrent() > 0 || tokenCount() > 0)
            return tokenOffset(0);
        return limitStartOffset;
    }

    public int endOffset() {
        int cntM1 = tokenCount() - 1;
        if (cntM1 >= 0)
            return tokenOffset(cntM1) + token(cntM1).length();
        return limitStartOffset;
    }

    private AbstractToken<T> token(int index) {
        return LexerUtilsConstants.token(tokenList, index);
    }
    
}