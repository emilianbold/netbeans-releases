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

/**
 * Structure holding a token list count that bases a JoinTokenList.
 * <br/>
 * There is also an information regarding a join token index gap inside the series
 * of contained embedded token lists.
 * <br/>
 * Join token list can be created as a wrapper around this class with a knowledge
 * of token list list and an token list index at which the join token list would start.
 * 
 * @author Miloslav Metelka
 */

public final class JoinTokenListBase {
    
    private static final int INDEX_GAP_LENGTH_INITIAL_SIZE = (Integer.MAX_VALUE >> 1);
    
    /**
     * Number of embedded token lists contained in join token list.
     */
    int tokenListCount; // 12 bytes (8-super + 4)
    
    /**
     * Total count of tokens contained in JoinTokenList.
     */
    int joinTokenCount; // 16 bytes
    
    /**
     * Index among contained embedded token lists where both index-related gaps are located.
     */
    int indexGapsIndex; // 24 bytes
    
    /**
     * Length of an index gap for computation of indexes in a JoinTokenList
     * based on ETLs.
     * <br/>
     * The above gap checking is done by checking whether the index is above gap length
     * since the initial gap length is so high that the indexes should never reach
     * its size (even decreased by added items).
     */
    int joinTokenIndexGapLength = INDEX_GAP_LENGTH_INITIAL_SIZE; // 28 bytes
    
    /**
     * Length of an index gap for computation of index of ETL in a JoinTokenList
     * which is useful for finding of a start-token-list-index of the join token list.
     * <br/>
     * The above gap checking is done by checking whether the index is above gap length
     * since the initial gap length is so high that the indexes should never reach
     * its size (even decreased by added items).
     */
    int tokenListIndexGapLength = INDEX_GAP_LENGTH_INITIAL_SIZE; // 32 bytes

    /**
     * Extra mod count to indicate a change in join token list caused by a custom embedding
     * creation/removal.
     */
    int extraModCount; // 36 bytes
    
    public JoinTokenListBase(int tokenListCount) {
        this.tokenListCount = tokenListCount;
        // Move index gap to be at the end of all contained token lists
        this.indexGapsIndex = tokenListCount;
    }

    int joinTokenIndex(int rawJoinTokenIndex) {
        return (rawJoinTokenIndex < joinTokenIndexGapLength)
                ? rawJoinTokenIndex
                : rawJoinTokenIndex - joinTokenIndexGapLength;
    }
    
    int tokenListIndex(int rawTokenListIndex) {
        return (rawTokenListIndex < tokenListIndexGapLength)
                ? rawTokenListIndex
                : rawTokenListIndex - tokenListIndexGapLength;
    }
    
    /**
     * Move both gaps in sync so that ETL.JoinInfo in an ETL at "index" is above both gaps.
     * 
     * @param tokenListList non-null TLL.
     * @param tokenListStartIndex points to first list belonging to a JTL.
     * @param index index to which the gaps should be moved.
     */
    
    public void moveIndexGap(TokenListList<?> tokenListList, int tokenListStartIndex, int index) {
        if (index < indexGapsIndex) {
            // Items above index should be moved to be above gap
            int i = index;
            do {
                EmbeddedJoinInfo joinInfo = tokenListList.get(tokenListStartIndex + i++).joinInfo;
                joinInfo.rawTokenListIndex += tokenListIndexGapLength;
                joinInfo.rawJoinTokenIndex += joinTokenIndexGapLength;
            } while (i < indexGapsIndex);
            indexGapsIndex = index;

        } else if (index > indexGapsIndex) {
            // Items below index should be moved to be below gap
            int i = index;
            do {
                EmbeddedJoinInfo joinInfo = tokenListList.get(tokenListStartIndex + --i).joinInfo;
                joinInfo.rawTokenListIndex -= tokenListIndexGapLength;
                joinInfo.rawJoinTokenIndex -= joinTokenIndexGapLength;
            } while (i > indexGapsIndex);
            indexGapsIndex = index;
        }
    }

    public void tokenListModNotify(int tokenListCountDiff) {
        // Gap assumed to be above last added token list or above
        indexGapsIndex += tokenListCountDiff; // Move gap above added 
        tokenListCount += tokenListCountDiff;
        tokenListIndexGapLength -= tokenListCountDiff;
    }

    public int joinTokenCount() {
        return joinTokenCount;
    }

    public void updateJoinTokenCount(int joinTokenCountDiff) {
        joinTokenCount += joinTokenCountDiff;
        joinTokenIndexGapLength -= joinTokenCountDiff;
    }

    public void incrementExtraModCount() {
        extraModCount++;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(70);
        sb.append("tokenListCount=").append(tokenListCount);
        sb.append(", tokenCount=").append(joinTokenCount);
        sb.append(", indexGapsIndex=").append(indexGapsIndex);
        return sb.toString();
    }

}
