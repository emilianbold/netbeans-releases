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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.GapList;

/**
 * List of token lists that collects all token lists for a given language path.
 *
 * @author Miloslav Metelka
 */

public final class TokenListList extends AbstractList<TokenList<?>> {

    private final TokenHierarchyOperation operation;

    private final LanguagePath languagePath;

    private final GapList<TokenList<?>> tokenLists;

    /** Whether searching thgroughout the token hierarchy was finished. */
    private boolean complete;

    /** Explorers scanning the hierarchy. */
    private List<TokenListExplorer> explorers;

    /**
     * Whether this token list is holding joint sections embeddings.
     * <br/>
     * If so it is mandatorily maintained.
     */
    private boolean joinSections;
    
    /**
     * Total count of children. It's maintained to quickly resolve
     * whether the list may be released.
     */
    private int childrenCount;

    public static List<TokenSequence<? extends TokenId>> createTokenSequenceList(
    TokenHierarchyOperation operation, LanguagePath languagePath, int startOffset, int endOffset) {
        TokenListList tll = operation.tokenListList(languagePath);
        // Only choose the token lists in the requested area
        int low = 0;
        int high = tll.size() - 1;
        // Find the tl which has the end offset above or equal to the requested startOffset
        // and take the first token list above it.
        if (startOffset > 0) {
            while (low <= high) {
                int mid = (low + high) / 2;
                TokenList<?> tl = tll.get(mid);
                int tlEndOffset = tl.endOffset();
                if (tlEndOffset < startOffset) {
                    low = mid + 1;
                } else if (tlEndOffset > startOffset) {
                    high = mid - 1;
                } else { // tl ends exactly at start offset
                    low = mid + 1; // take the first above this
                    break;
                }
            }
        }
        // Not found exactly -> take the higher one (low variable)
        int startIndex = low;

        low = 0;
        high = tll.size() - 1;
        if (startIndex <= high) { // startIndex < tll.size()
            // Find the tl which has the start offset above or equal to the requested endOffset
            // and take the first token list above it.
            if (endOffset < Integer.MAX_VALUE) {
                while (low <= high) {
                    int mid = (low + high) / 2;
                    TokenList<?> tl = tll.get(mid);
                    int tlStartOffset = tl.startOffset();
                    if (tlStartOffset < endOffset) {
                        low = mid + 1;
                    } else if (tlStartOffset > endOffset) {
                        high = mid - 1;
                    } else { // tl ends exactly at end offset
                        high = mid; // take the first above this
                        break;
                    }
                }
            }
            // Not found exactly -> take the high plus one as end index
            int endIndex = high + 1;

            assert (startIndex <= endIndex);
            if (endIndex - startIndex > 0) {
                @SuppressWarnings("unchecked")
                TokenSequence<? extends TokenId>[] tss = new TokenSequence[endIndex - startIndex];
                TokenList<?> startTokenList = tll.get(startIndex);
                // Wrap the start token list if the startOffset lies inside it
                boolean wrapStart = (startTokenList.startOffset() < startOffset)
                        && (startOffset < startTokenList.endOffset());
                TokenList<?> endTokenList = tll.get(endIndex - 1);
                // Wrap the end token list if the endOffset lies inside it
                boolean wrapEnd = (endTokenList.startOffset() < endOffset)
                        && (endOffset < endTokenList.endOffset());
                if (endIndex == startIndex + 1) { // Exactly one token list
                    if (wrapStart || wrapEnd)
                        startTokenList = SubSequenceTokenList.create(
                                startTokenList, startOffset, endOffset);
                } else { // tss.length >= 2
                    if (wrapStart)
                        startTokenList = SubSequenceTokenList.create(
                                startTokenList, startOffset, Integer.MAX_VALUE);
                    if (wrapEnd)
                        endTokenList = SubSequenceTokenList.create(endTokenList, 0, endOffset);
                    tss[tss.length - 1] = LexerApiPackageAccessor.get().createTokenSequence(endTokenList);
                    for (int i = 1; i < tss.length - 1; i++) {
                        tss[i] = LexerApiPackageAccessor.get().createTokenSequence(tll.get(startIndex + i));
                    }

                }
                tss[0] = LexerApiPackageAccessor.get().createTokenSequence(startTokenList);
                return ArrayUtilities.unmodifiableList(tss);

            } // endIndex == startIndex => empty
        } // startIndex >= tll.size()
        // Return empty list
        return Collections.<TokenSequence<? extends TokenId>>emptyList();
    }

    public TokenListList(TokenHierarchyOperation<?,?> operation, LanguagePath languagePath) {
        this.operation = operation;
        this.languagePath = languagePath;

        tokenLists = new GapList<TokenList<?>>(2);
        if (languagePath.size() == 1) { // Either top-level path or something not present
            if (languagePath == operation.tokenList().languagePath()) {
                tokenLists.add(operation.tokenList());
            }
            complete = true;

        } else {
            explorers = new ArrayList<TokenListExplorer>(languagePath.size());
            explorers.add(new TokenListExplorer(operation.checkedTokenList()));
        }
    }

    public LanguagePath languagePath() {
        return languagePath;
    }
    
    /**
     * Whether the list already contains all the appropriate token lists
     * from the whole hierarchy.
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Return true if this list is mandatorily updated because there is
     * one or more embeddings that join sections.
     */
    public boolean isJoinSections() {
        return joinSections;
    }
    
    public void setJoinSections(boolean joinSections) {
        this.joinSections = joinSections;
    }
    
    public void increaseChildrenCount() {
        childrenCount++;
    }
    
    public void decreaseChildrenCount() {
        childrenCount--;
    }
    
    public boolean hasChildren() {
        return (childrenCount > 0);
    }

    public TokenList<?> get(int index) {
        findTokenListWithIndex(index);
        return tokenLists.get(index); // Will fail naturally if index too high
    }

    /**
     * Return a valid token list or null if the index is too high.
     */
    public TokenList<?> getOrNull(int index) {
        findTokenListWithIndex(index);
        return getExistingOrNull(index);
    }
    
    public TokenList<?> getExistingOrNull(int index) {
        return (index < tokenLists.size()) ? tokenLists.get(index) : null;
    }
    
    private static final TokenList<?>[] EMPTY_TOKEN_LIST_ARRAY = new TokenList<?>[0];

    public TokenList<?>[] replace(int index, int removeCount, List<TokenList<?>> addTokenLists) {
        TokenList<?>[] removed;
        if (removeCount > 0) {
            removed = new TokenList<?>[removeCount];
            tokenLists.copyElements(index, index + removeCount, removed, 0);
            tokenLists.remove(index, removeCount);
        } else {
            removed = EMPTY_TOKEN_LIST_ARRAY;
        }
        tokenLists.addAll(index, addTokenLists);
        return removed;
    }

    public int size() {
        findTokenListWithIndex(Integer.MAX_VALUE);
        return tokenLists.size();
    }

    public int sizeCurrent() {
        return tokenLists.size();
    }

    /**
     * Find the previous sections with at least one token.
     * <br/>
     * Force creation of token lists below the offset if they do not exist yet.
     */
    public int findPreviousNonEmptySectionIndex(int offset) {
        int high = sizeCurrent() - 1;
        if (!complete) {
            if (high == -1) {
                getOrNull(0); // Force init of first item
                return findPreviousNonEmptySectionIndex(offset);
            }
            TokenList<?> tokenList = get(high); // Should be non-null
            if (tokenList.endOffset() < offset) {
                while ((tokenList = getOrNull(++high)) != null && tokenList.endOffset() < offset) { }
                return findPreviousNonEmptySectionIndex(offset);
            }
        }
        int low = 0;
        while (low <= high) {
            int mid = (low + high) >> 1;
            int cmp = tokenLists.get(mid).endOffset() - offset;
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else { // cmp == 0 -> take the previous one
                high = mid;
                break;
            }
        }
        // Use 'high' which == low - 1
        while (high >= 0 && tokenLists.get(high).tokenCount() == 0) {
            high--; // Skip all empty sections
        }
        return high;
    }

    void childAdded() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    TokenHierarchyOperation operation() {
        return operation;
    }

    int modCount() {
        return operation.modCount();
    }

    public int findIndex(int offset) {
        int high = sizeCurrent() - 1;
        int low = 0;
        while (low <= high) {
            int mid = (low + high) >> 1;
            int cmp = tokenLists.get(mid).startOffset() - offset;
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else { // cmp == 0 -> take the previous one
                low = mid;
                break;
            }
        }
        return low;
    }

    private void findTokenListWithIndex(int tokenListIndex) {
        if (complete || tokenListIndex < tokenLists.size())
            return;

        while (explorers.size() > 0) {
            int explorerIndex = explorers.size() - 1;
            TokenListExplorer explorer = explorers.get(explorerIndex);
            int tokenCount = explorer.tokenCount();
            Language<?> language = languagePath.language(explorerIndex + 1);
            while (explorer.index < tokenCount) {
                TokenList<?> embeddedTL = LexerUtilsConstants.embeddedTokenList(
                        explorer.tokenList, explorer.index, language);
                explorer.index++;
                if (embeddedTL != null) {
                    if (explorerIndex + 2 == languagePath.size()) { // Found the one
                        tokenLists.add(embeddedTL);
                        if (tokenLists.size() > tokenListIndex) // Return if reached requested index
                            return;
                    } else { // Explore this one
                        explorer = new TokenListExplorer(embeddedTL);
                        explorers.add(explorer);
                        explorerIndex++;
                        tokenCount = explorer.tokenCount();
                        language = languagePath.language(explorerIndex + 1);
                    }
                }
            }
            // Finished searching the most embedded list
            explorers.remove(explorerIndex);
        }
        complete = true;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder("TokenListList for ");
        sb.append(languagePath().mimePath());
        if (!isComplete()) {
            sb.append(", incomplete");
        }
        if (isJoinSections()) {
            sb.append(", joinSections");
        }
        if (hasChildren()) {
            sb.append(", hasChildren");
        }
        sb.append('\n');
        int digitCount = ArrayUtilities.digitCount(tokenLists.size());
        for (int i = 0; i < tokenLists.size(); i++) {
            TokenList<?> tokenList = tokenLists.get(i);
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            sb.append("range:[").append(tokenList.startOffset()).append(",").
                    append(tokenList.endOffset()).append(']');
            sb.append(", IHC=").append(System.identityHashCode(tokenList));
            sb.append('\n');
            LexerUtilsConstants.appendTokenListIndented(sb, tokenList, 4);
        }
        return sb.toString();
    }

    private static final class TokenListExplorer {

        final TokenList<?> tokenList;

        int index;

        TokenListExplorer(TokenList<?> tokenList) {
            this.tokenList = tokenList;
        }

        int tokenCount() {
            return tokenList.tokenCount();
        }

    }

}
