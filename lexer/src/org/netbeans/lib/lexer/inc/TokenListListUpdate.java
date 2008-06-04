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

package org.netbeans.lib.lexer.inc;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.JoinTokenList;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.TokenListList;

/**
 * Change of a particular TokenListList.
 *
 * @author Miloslav Metelka
 */

final class TokenListListUpdate<T extends TokenId> {
    
    /**
     * Token list list for the case when the particular language path
     * corresponds to joined.
     */
    final TokenListList<T> tokenListList;

    int modTokenListIndex;

    int removedTokenListCount;

    List<EmbeddedTokenList<T>> addedTokenLists;

    TokenListListUpdate(TokenListList<T> tokenListList) {
        this.tokenListList = tokenListList;
        this.modTokenListIndex = -1;
    }

    public boolean isTokenListsMod() { // If any ETL was removed/added
        return (removedTokenListCount != 0) || addedTokenLists.size() > 0;
    }

    public int tokenListCountDiff() {
        return addedTokenLists.size() - removedTokenListCount;
    }

    public EmbeddedTokenList<T> afterUpdateTokenList(JoinTokenList<T> jtl, int tokenListIndex) {
        EmbeddedTokenList<T> etl;
        if (tokenListIndex < modTokenListIndex) {
            etl = jtl.tokenList(tokenListIndex);
            // Update ETL's start offset. JTL.tokenStartLocalIndex() may skip down several ETLs
            //   for join tokens so this needs to be done to properly relex.
            // Only used by TLU so update without syncing.
            etl.embeddingContainer().updateStatusUnsync();
        } else if (tokenListIndex < modTokenListIndex + addedTokenLists.size()) {
            etl = addedTokenLists.get(tokenListIndex - modTokenListIndex);
        } else { // Last part after removed and added
            etl = jtl.tokenList(tokenListIndex + removedTokenListCount - addedTokenLists.size());
            // Update ETL's start offset.
            // Only used by TLU so update without syncing.
            etl.embeddingContainer().updateStatusUnsync();
        }
        return etl;
    }

    protected int afterUpdateTokenListCount(JoinTokenList<T> jtl) {
        return jtl.tokenListCount() - removedTokenListCount + addedTokenLists.size();
    }

    void markChangedMember(EmbeddedTokenList<T> changedTokenList) {
        assert (modTokenListIndex == -1);
        modTokenListIndex = tokenListList.findIndex(changedTokenList.startOffset());
        assert (tokenListList.get(modTokenListIndex) == changedTokenList);
    }

    void markChageBetween(int offset) { // Nothing added/removed and mod outside of bounds of an ETL
        assert (modTokenListIndex == -1);
        modTokenListIndex = tokenListList.findIndex(offset);
    }

    /**
     * Mark the given token list as removed in the token list list.
     * All removed token lists should be marked subsequently their increasing offset
     * so it should be necessary to search for the index just once.
     * <br/>
     * It's expected that updateStatusImpl() was already called
     * on the corresponding embedding container.
     */
    void markRemovedMember(EmbeddedTokenList<T> removedTokenList, TokenHierarchyEventInfo eventInfo) {
        boolean indexWasMinusOne; // Used for possible exception cause debugging
//            removedTokenList.embeddingContainer().checkStatusUpdated();
        if (modTokenListIndex == -1) {
            indexWasMinusOne = true;
            modTokenListIndex = tokenListList.findIndexDuringUpdate(removedTokenList, eventInfo);
            assert (modTokenListIndex >= 0) : "tokenListIndex=" + modTokenListIndex + " < 0"; // NOI18N
        } else { // tokenListIndex already initialized
            indexWasMinusOne = false;
        }
        TokenList<T> markedForRemoveTokenList = tokenListList.getOrNull(modTokenListIndex + removedTokenListCount);
        if (markedForRemoveTokenList != removedTokenList) {
            int realIndex = tokenListList.indexOf(removedTokenList);
            throw new IllegalStateException("Removing at tokenListIndex=" + modTokenListIndex + // NOI18N
                    " but real tokenListIndex is " + realIndex + // NOI18N
                    " (indexWasMinusOne=" + indexWasMinusOne + ").\n" + // NOI18N
                    "Wishing to remove tokenList\n" + removedTokenList + // NOI18N
                    "\nbut marked-for-remove tokenList is \n" + markedForRemoveTokenList + // NOI18N
                    "\nfrom tokenListList\n" + tokenListList + // NOI18N
                    "\n\nModification description:\n" + eventInfo.modificationDescription(true) // NOI18N
                    );
        }
        removedTokenListCount++;
    }

    /**
     * Mark the given token list to be added to this list of token lists.
     * At the end first the token lists marked for removal will be removed
     * and then the token lists marked for addition will be added.
     * <br/>
     * It's expected that updateStatusImpl() was already called
     * on the corresponding embedding container.
     */
    void markAddedMember(EmbeddedTokenList<T> addedTokenList) {
//            addedTokenList.embeddingContainer().checkStatusUpdated();
        if (addedTokenLists == null) {
            if (modTokenListIndex == -1) {
                modTokenListIndex = tokenListList.findIndex(addedTokenList.startOffset());
                assert (modTokenListIndex >= 0) : "tokenListIndex=" + modTokenListIndex + " < 0"; // NOI18N
            }
            addedTokenLists = new ArrayList<EmbeddedTokenList<T>>(4);
        }
        addedTokenLists.add(addedTokenList);
    }

    void addRemoveTokenLists(TokenHierarchyUpdate update, boolean tllChildrenMayExist) {
        assert (removedTokenListCount > 0 || addedTokenLists != null);
        EmbeddedTokenList<T>[] removedTokenLists = tokenListList.replace(
                modTokenListIndex, removedTokenListCount, addedTokenLists);
        if (tllChildrenMayExist) {
            for (int i = 0; i < removedTokenLists.length; i++) {
                update.collectRemovedEmbeddings(removedTokenLists[i]);
            }
            for (int i = 0; i < addedTokenLists.size(); i++) {
                EmbeddedTokenList<T> addedEtl = addedTokenLists.get(i);
                update.collectAddedEmbeddings(addedEtl, 0, addedEtl.tokenCountCurrent());
            }
        }
    }

    TokenListChange<T> createTokenListChange(EmbeddedTokenList<T> etl) {
        assert (etl != null);
        TokenListChange<T> etlTokenListChange;
        if (tokenListList.joinSections()) {
            MutableJoinTokenList<T> jtl = MutableJoinTokenList.create(tokenListList, modTokenListIndex);
            etlTokenListChange = new JoinTokenListChange<T>(jtl);
        } else { // Non-joining
            etlTokenListChange = new TokenListChange<T>(etl);
        }
        return etlTokenListChange;
    }

    TokenListChange<T> createJoinTokenListChange() {
        assert (tokenListList.joinSections());
        // In case when adding at jtl.tokenListCount() a last ETL must be used
        int etlIndex = Math.min(modTokenListIndex, tokenListList.size() - 1);
        MutableJoinTokenList<T> jtl = MutableJoinTokenList.create(tokenListList, etlIndex);
        return new JoinTokenListChange<T>(jtl);
    }

    @Override
    public String toString() {
        return " modTokenListIndex=" + modTokenListIndex + // NOI18N
                "; Rem:" + removedTokenListCount + // NOI18N
                " Add:" + addedTokenLists.size() + // NOI18N
                " Size:" + tokenListList.size(); // NOI18N
    }

}
