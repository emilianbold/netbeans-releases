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

/**
 * List of token lists that collects all token lists for a given language path.
 *
 * @author Miloslav Metelka
 */

public final class TokenListList extends AbstractList<TokenList<?>> {

    private final TokenHierarchyOperation operation;

    private final LanguagePath languagePath;

    private final List<TokenList<?>> tokenLists;
    
    /** Whether searching thgroughout the token hierarchy was finished. */
    private boolean closed;

    /** Explorers scanning the hierarchy. */
    private List<TokenListExplorer> explorers;
    
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
        assert (languagePath != null);
        this.operation = operation;
        this.languagePath = languagePath;

        if (languagePath.size() == 1) {
            // For top-level language path return singleton list or nothing
            if (operation.checkedTokenList().languagePath() == languagePath) {
                // Other way than through raw-type??
                List l = Collections.singletonList(operation.checkedTokenList());
                @SuppressWarnings("unchecked")
                List<TokenList<?>> tls = (List<TokenList<?>>)l;
                tokenLists = tls;
            } else {
                tokenLists = Collections.emptyList();
            }
            explorers = Collections.emptyList();
            closed = true;

        } else { // languagePath.size() > 1
            tokenLists = new ArrayList<TokenList<?>>(2);
            explorers = new ArrayList<TokenListExplorer>(languagePath.size());
            explorers.add(new TokenListExplorer(operation.checkedTokenList()));
        }
    }
    
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    public TokenList<?> get(int index) {
        if (!closed && index >= tokenLists.size())
            findTokenListWithIndex(index);
        return tokenLists.get(index); // Will fail naturally if index too high
    }

    public int size() {
        findTokenListWithIndex(Integer.MAX_VALUE);
        return tokenLists.size();
    }

    private void findTokenListWithIndex(int tokenListIndex) {
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
        closed = true;
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
