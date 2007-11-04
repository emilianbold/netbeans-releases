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

public final class TokenListList extends GapList<EmbeddedTokenList<?>> {

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

        // languagePath has size >= 2
        assert (languagePath.size() >= 2);
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
            relexState = LexerUtilsConstants.endState(get(i));
        }
        if (relexState == INVALID_STATE) // Start from real begining
            relexState = null;
        return relexState;
    }

    /**
     * Return a valid token list or null if the index is too high.
     */
    public EmbeddedTokenList<?> getOrNull(int index) {
        return (index < size()) ? get(index) : null;
    }
    
    private static final EmbeddedTokenList<?>[] EMPTY_TOKEN_LIST_ARRAY = new EmbeddedTokenList<?>[0];

    public EmbeddedTokenList<?>[] replace(int index, int removeCount, List<? extends TokenList<?>> addTokenLists) {
        EmbeddedTokenList<?>[] removed;
        if (removeCount > 0) {
            removed = new EmbeddedTokenList<?>[removeCount];
            copyElements(index, index + removeCount, removed, 0);
            remove(index, removeCount);
        } else {
            removed = EMPTY_TOKEN_LIST_ARRAY;
        }
        @SuppressWarnings("unchecked")
        List<EmbeddedTokenList<?>> etlLists = (List<EmbeddedTokenList<?>>)addTokenLists;
        addAll(index, etlLists);
        return removed;
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
            EmbeddedTokenList<?> etl = get(mid);
            // Ensure that the startOffset() will be updated
            etl.embeddingContainer().updateStatus();
            int cmp = etl.startOffset() - offset;
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
    
    /**
     * Find an index during update of the token list.
     * <br>
     * If there was a removal performed and some of the contained token lists
     * were removed then these TLs then the token lists beyond the modification point
     * will be forced to update itself which may 
     */
    public int findIndexDuringUpdate(EmbeddedTokenList targetEtl, int modOffset, int removedLength) {
        int high = size() - 1;
        int low = 0;
        int targetStartOffset = targetEtl.startOffset();
        if (targetStartOffset > modOffset && targetEtl.embeddingContainer().isRemoved()) {
            targetStartOffset = Math.max(targetStartOffset - removedLength, modOffset);
        }
        while (low <= high) {
            int mid = (low + high) >> 1;
            EmbeddedTokenList<?> etl = get(mid);
            // Ensure that the startOffset() will be updated
            etl.embeddingContainer().updateStatusImpl();
            int startOffset = etl.startOffset();
            if (startOffset > modOffset && etl.embeddingContainer().isRemoved()) {
                startOffset = Math.max(startOffset - removedLength, modOffset);
            }
            int cmp = startOffset - targetStartOffset;
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else {
                low = mid;
                // Now it's also possible that there was a larger remove when multiple token lists
                // inside the removed area were removed and they all have startOffset being modOffset.
                // In such case these need to be searched by linear search in both directions
                // from the found one.
                if (etl != targetEtl) {
                    low--;
                    while (low >= 0) {
                        etl = get(low);
                        if (etl == targetEtl) { // Quick check for match
                            return low;
                        }
                        // Check whether this was appropriate attempt for match
                        etl.embeddingContainer().updateStatusImpl();
                        startOffset = etl.startOffset();
                        if (startOffset > modOffset && etl.embeddingContainer().isRemoved()) {
                            startOffset = Math.max(startOffset - removedLength, modOffset);
                        }
                        if (startOffset != modOffset)
                            break;
                        low--;
                    }
                    
                    // Go up from mid
                    low = mid + 1;
                    while (low < size()) {
                        etl = get(low);
                        if (etl == targetEtl) { // Quick check for match
                            return low;
                        }
                        // Check whether this was appropriate attempt for match
                        etl.embeddingContainer().updateStatusImpl();
                        startOffset = etl.startOffset();
                        if (startOffset > modOffset && etl.embeddingContainer().isRemoved()) {
                            startOffset = Math.max(startOffset - removedLength, modOffset);
                        }
                        if (startOffset != modOffset)
                            break;
                        low++;
                    }
                }
                break;
            }
        }
        return low;
    }
    
    public String checkConsistency() {
        // Check whether the token lists are in a right order
        int lastEndOffset = 0;
        for (int i = 0; i < size(); i++) {
            EmbeddedTokenList<?> etl = get(i);
            etl.embeddingContainer().updateStatusImpl();
            if (etl.startOffset() < lastEndOffset) {
                return "TOKEN-LIST-LIST Invalid start offset at index=" + i +
                        ": etl[" + i + "].startOffset()=" + etl.startOffset() +
                        " < lastEndOffset=" + lastEndOffset +
                        "\n" + this;
            }
            if (etl.startOffset() > etl.endOffset()) {
                return "TOKEN-LIST-LIST Invalid end offset at index=" + i +
                        ": etl[" + i + "].startOffset()=" + etl.startOffset() +
                        " > etl[" + i + "].endOffset()="+ etl.endOffset() +
                        "\n" + this;
            }
            if (etl.embeddingContainer() == null) {
                return "TOKEN-LIST-LIST Null ec at index=" + i + "\n" + this;
            }
            if (etl.embeddingContainer().isRemoved()) {
                return "TOKEN-LIST-LIST Removed ec fat index=" + i + "\n" + this;
            }
            lastEndOffset = etl.endOffset();
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(2048);
        sb.append("TokenListList for ");
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
            EmbeddedTokenList<?> etl = get(i);
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            etl.embeddingContainer().updateStatus();
            sb.append(etl.toStringHeader());
            EmbeddingContainer ec = etl.embeddingContainer();
            if (ec != null && ec.isRemoved()) {
                sb.append(", <--REMOVED-->");
            }
            sb.append('\n');
            LexerUtilsConstants.appendTokenListIndented(sb, etl, 4);
        }
        return sb.toString();
    }

}
