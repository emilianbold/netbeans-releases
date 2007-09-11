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

import java.util.List;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.GapList;
import static org.netbeans.lib.lexer.LexerUtilsConstants.INVALID_STATE;

/**
 * List of token lists that collects all token lists for a given language path.
 * <br/>
 * There can be both lists with/without joining of the embedded sections.
 * <br/>
 * Initial implementation attempted to initialize the list of token lists lazily
 * upon asking for it by client. However there was a problem with fixing
 * of token list explorers' state when the list is partially initialized
 * and there is an update of the token hierarchy. Sometimes there were inconsistencies
 * that a particular token list appeared twice in the token list list.
 * <br/>
 * Current impl is non-lazy so once the list becomes created it gets fully initialized
 * by traversing the parent token lists's tokens for embeddings of the particular language.
 * <br/>
 * Advantages:
 * <ul>
 *   <li> Easier updating - no issues with incomplete exploration.
 *   <li> More errorsafe approach with joinSections - if any of the scanned lists is joinSections
 *        then the whole token list list becomes joinSections from the begining.
 *   <li> It's disputable how much time the lazy impl has been saving.
 *   <li> More deterministic behavior - helps to diagnose errors.
 * </ul>
 * 
 * <p>
 * GapList is used for faster updates and there can be either single top-level
 * non-EmbeddedTokenList token list or zero or more nested EmbeddedTokenList(s).
 * </p>
 * 
 * <p>
 * joinSections approach:
 * <br/>
 * Non-joining embedded token lists' contents will be lexed without token list list assistance.
 * <br/>
 * Joining embedded TLs will need TLL assistance so TLL instance gets created for them.
 * <br/>
 * If there are mixed joining/non-joining language embedding instances for the same
 * language path then the non-joining ones can possibly become initialized (lexed)
 * without TLL if they are asked individually. Later when first joining embedding is found
 * the token list list will be created and contain both joining and non-joining
 * embeddings but the joinSections will be respected for individual lexing.
 * Non-joining sections will be lexed individually and the join sections will be lexed as joined.
 * </p>
 *
 * @author Miloslav Metelka
 */

public final class TokenListList extends GapList<TokenList<?>> {

    private final TokenHierarchyOperation operation;

    private final LanguagePath languagePath;

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


    public TokenListList(TokenHierarchyOperation<?,?> operation, LanguagePath languagePath) {
        super(4);
        this.operation = operation;
        this.languagePath = languagePath;

        if (languagePath.size() == 1) { // Either top-level path or something not present
            if (languagePath == operation.rootTokenList().languagePath()) {
                add(operation.rootTokenList());
            }

        } else { // languagePath has size >= 2
            Language<?> language = languagePath.innerLanguage();
            if (languagePath.size() > 2) {
                Object relexState = null;
                TokenListList parentTokenList = operation.tokenListList(languagePath.parent());
                for (int parentIndex = 0; parentIndex < parentTokenList.size(); parentIndex++) {
                    TokenList<?> tokenList = parentTokenList.get(parentIndex);
                    relexState = scanTokenList(tokenList, language, relexState);
                }
            } else { // Parent is root token list
                scanTokenList(operation.validRootTokenList(), language, null);
            }
        }
    }
    
    private Object scanTokenList(TokenList<?> tokenList, Language<?> language, Object relexState) {
        int tokenCount = tokenList.tokenCount();
        for (int i = 0; i < tokenCount; i++) {
            EmbeddedTokenList<?> etl = EmbeddingContainer.embeddedTokenList(
                tokenList, i, language);
            if (etl != null) {
                add(etl);
                if (etl.embedding().joinSections()) {
                    joinSections = true;
                    if (!etl.isInited()) {
                        etl.init(relexState);
                        relexState = LexerUtilsConstants.endState(etl, relexState);
                    }
                } else { // Not joining sections -> next section starts with null state
                    relexState = null;
                }
            }
        }
        return relexState;
    }
    
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    /**
     * Return true if this list is mandatorily updated because there is
     * one or more embeddings that join sections.
     */
    public boolean joinSections() {
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
    
    public Object relexState(int index) {
        // Find the previous non-empty section or non-joining section
        Object relexState = INVALID_STATE;
        for (int i = index - 1; i >= 0 && relexState == INVALID_STATE; i--) {
            relexState = LexerUtilsConstants.endState((EmbeddedTokenList<?>)get(i));
        }
        if (relexState == INVALID_STATE) // Start from real begining
            relexState = null;
        return relexState;
    }

    /**
     * Return a valid token list or null if the index is too high.
     */
    public TokenList<?> getOrNull(int index) {
        return (index < size()) ? get(index) : null;
    }
    
    private static final TokenList<?>[] EMPTY_TOKEN_LIST_ARRAY = new TokenList<?>[0];

    public TokenList<?>[] replace(int index, int removeCount, List<TokenList<?>> addTokenLists) {
        TokenList<?>[] removed;
        if (removeCount > 0) {
            removed = new TokenList<?>[removeCount];
            copyElements(index, index + removeCount, removed, 0);
            remove(index, removeCount);
        } else {
            removed = EMPTY_TOKEN_LIST_ARRAY;
        }
        addAll(index, addTokenLists);
        return removed;
    }

    /**
     * Find the previous sections with at least one token.
     * <br/>
     * Force creation of token lists below the offset if they do not exist yet.
     */
    public int findPreviousNonEmptySectionIndex(int offset) {
        int high = size() - 1;
        int low = 0;
        while (low <= high) {
            int mid = (low + high) >> 1;
            int cmp = get(mid).endOffset() - offset;
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
        while (high >= 0 && get(high).tokenCount() == 0) {
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
        int high = size() - 1;
        int low = 0;
        while (low <= high) {
            int mid = (low + high) >> 1;
            int cmp = get(mid).startOffset() - offset;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TokenListList for ");
        sb.append(languagePath().mimePath());
        if (joinSections()) {
            sb.append(", joinSections");
        }
        if (hasChildren()) {
            sb.append(", hasChildren");
        }
        sb.append('\n');
        int digitCount = ArrayUtilities.digitCount(size());
        for (int i = 0; i < size(); i++) {
            TokenList<?> tokenList = get(i);
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            sb.append("range:[").append(tokenList.startOffset()).append(",").
                    append(tokenList.endOffset()).append(']');
            sb.append(", IHC=").append(System.identityHashCode(tokenList));
            sb.append('\n');
            LexerUtilsConstants.appendTokenListIndented(sb, tokenList, 4);
        }
        return sb.toString();
    }

}
