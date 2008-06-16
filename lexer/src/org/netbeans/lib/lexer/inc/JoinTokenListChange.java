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
import org.netbeans.lib.lexer.EmbeddedJoinInfo;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.JoinLexerInputOperation;
import org.netbeans.lib.lexer.TokenOrEmbedding;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.JoinToken;
import org.netbeans.lib.lexer.token.PartToken;

/**
 * Token list change for join token lists.
 *
 * @author Miloslav Metelka
 */
final class JoinTokenListChange<T extends TokenId> extends TokenListChange<T> {
    
    /** ETL where character modification occurred. */
    EmbeddedTokenList<T> charModTokenList;
    
    private TokenListListUpdate<T> tokenListListUpdate;

    private int startRelexTokenListIndex;

    private List<RelexTokenListChange<T>> relexChanges;
    
    private JoinLexerInputOperation<T> joinLexerInputOperation;
    
    public JoinTokenListChange(MutableJoinTokenList<T> tokenList) {
        super(tokenList);
    }

    public List<? extends TokenListChange<T>> relexChanges() {
        return relexChanges;
    }

    public TokenListListUpdate<T> tokenListListUpdate() {
        return tokenListListUpdate;
    }
    
    public void setTokenListListUpdate(TokenListListUpdate<T> tokenListListUpdate) {
        this.tokenListListUpdate = tokenListListUpdate;
    }
    
    public void setStartInfo(JoinLexerInputOperation<T> joinLexerInputOperation, int localIndex) {
        this.joinLexerInputOperation = joinLexerInputOperation;
        this.startRelexTokenListIndex = joinLexerInputOperation.activeTokenListIndex();
        this.relexChanges = new ArrayList<RelexTokenListChange<T>>(
                tokenListListUpdate.addedTokenLists.size() + 3);
        // Add first change now to incorporate starting modified token index
        RelexTokenListChange<T> firstChange = new RelexTokenListChange<T>(
                joinLexerInputOperation.tokenList(startRelexTokenListIndex));
        // Set index in ETL to properly do replaceTokens() in ETL
        // Setting both index and offset is BTW necessary in order to properly move offset gap in ETL
        firstChange.setIndex(localIndex);
        int relexOffset = joinLexerInputOperation.lastTokenEndOffset();
        firstChange.setOffset(relexOffset);
        firstChange.setMatchOffset(relexOffset); // Due to removeLastAddedToken() and etc.
        relexChanges.add(firstChange);
    }

    @Override
    public void addToken(AbstractToken<T> token, int lookahead, Object state) {
        // Check if lexer-input-operation advanced to next list and possibly add corresponding relex change(s)
        int activeTokenListIndex = joinLexerInputOperation.activeTokenListIndex();
        while (startRelexTokenListIndex + relexChanges.size() <= activeTokenListIndex) {
            // Use JLIO.tokenList() since it already contains the removed/added ETLs.
            EmbeddedTokenList<T> etl = joinLexerInputOperation.tokenList(
                    startRelexTokenListIndex + relexChanges.size());
            RelexTokenListChange<T> relexChange = new RelexTokenListChange<T>(etl);
            int startOffset = etl.startOffset();
            relexChange.setOffset(startOffset);
            relexChanges.add(relexChange);
        }
        int relexChangeIndex = activeTokenListIndex - startRelexTokenListIndex;
        if (token.getClass() == JoinToken.class) {
            JoinToken<T> joinToken = (JoinToken<T>) token;
            List<PartToken<T>> joinedParts = joinToken.joinedParts();
            int extraTokenListSpanCount = joinToken.extraTokenListSpanCount();
            int startRelexChangeIndex = relexChangeIndex - extraTokenListSpanCount;
            int joinedPartIndex = 0;
            // Only add without the last part (will be added normally outside the loop)
            // The last ETL can not be empty (must contain the last non-empty token part)
            for (int i = 0; i < extraTokenListSpanCount; i++) {
                RelexTokenListChange<T> relexChange = relexChanges.get(startRelexChangeIndex + i);
                // Check whether token list is non-empty by checking a text length that it covers.
                // Do not use etl.tokenCount() since the tokens are just being added into ETL.
                EmbeddedTokenList<T> etl = (EmbeddedTokenList<T>) relexChange.tokenList();
                if (etl.textLength() > 0) {
                    PartToken<T> partToken = joinedParts.get(joinedPartIndex++);
                    relexChange.addToken(partToken, 0, null);
                }
                relexChange.joinTokenLastPartShift = extraTokenListSpanCount - i;
            }
            // Last part will be added normally by subsequent code
            token = joinedParts.get(joinedPartIndex); // Should be (joinedParts.size()-1)
        }
        RelexTokenListChange<T> relexChange = relexChanges.get(relexChangeIndex);
        relexChange.addToken(token, lookahead, state);
        addedEndOffset = relexChange.addedEndOffset;
    }
    
    @Override
    public int increaseMatchIndex() {
        MutableJoinTokenList<T> jtl = (MutableJoinTokenList<T>) tokenList();
        AbstractToken<T> token = jtl.tokenOrEmbeddingUnsync(matchIndex).token();
        // matchOffset needs to be set to end of token at matchIndex
        if (token.getClass() == JoinToken.class) {
            matchOffset = ((JoinToken<T>) token).endOffset();
        } else { // Check whether the matchIndex points to begining of ETL
            if (matchIndex == jtl.activeStartJoinIndex()) { // First in ETL
                // Cannot use previous value of matchOffset since it pointed to end of previous ETL
                // Token is not join token so use its natural length()
                matchOffset = jtl.activeTokenList().startOffset() + token.length();
            } else {
                matchOffset += token.length();
            }
        }
        matchIndex++;
        return matchOffset;
    }

    @Override
    public AbstractToken<T> removeLastAddedToken() {
        RelexTokenListChange<T> lastRelexChange = relexChanges.get(relexChanges.size() - 1);
        AbstractToken<T> lastRemovedToken = lastRelexChange.removeLastAddedToken();
        if (lastRemovedToken.getClass() == PartToken.class) { // Join token
            // Remove extra parts - the relex changes
            int extraCount = ((PartToken<T>) lastRemovedToken).joinToken().extraTokenListSpanCount();
            for (int i = extraCount - 1; i >= 0; i--) {
                relexChanges.remove(relexChanges.size() - 1);
            }
            lastRelexChange = relexChanges.get(relexChanges.size() - 1);
            lastRemovedToken = lastRelexChange.removeLastAddedToken();
        }
        if (lastRelexChange.addedTokenOrEmbeddings().size() == 0) { // Empty change
            // Use addedEndOffset of the previous change and remove this one
            relexChanges.remove(relexChanges.size() - 1);
            
        }
        addedEndOffset = lastRelexChange.addedEndOffset;
        return lastRemovedToken;
    }
    
    void replaceTokenLists() {
        MutableJoinTokenList<T> jtl = (MutableJoinTokenList<T>) tokenList();
        // Move gap after last ETL that was relexed (obsolete ETLs still not removed)
        jtl.moveIndexGap(tokenListListUpdate.modTokenListIndex + tokenListListUpdate.removedTokenListCount);
        // Do physical ETLs replace
        jtl.tokenListList().replace(jtl.tokenListStartIndex() + tokenListListUpdate.modTokenListIndex,
                tokenListListUpdate.removedTokenListCount, tokenListListUpdate.addedTokenLists);
        jtl.base().tokenListModNotify(tokenListListUpdate.tokenListCountDiff());
    }
    
    public void replaceTokens(int diffLength) {
        // Determine position of matchIndex in token lists
        // if matchIndex == jtl.tokenCount() the token list index will be the last list
        //   and endLocalIndex will be its tokenCount(). Because of this
        //   there must be a check whether token list index is not among removed ETLs.
        MutableJoinTokenList<T> jtl = (MutableJoinTokenList<T>) tokenList();
        int localMatchIndex;
        if (matchIndex < jtl.tokenCountCurrent()) {
            localMatchIndex = jtl.tokenStartLocalIndex(matchIndex);
            if (localMatchIndex == 0) { // In fact end of previous ETL
                jtl.setPrevActiveTokenListIndex();
                localMatchIndex = jtl.activeTokenList().tokenCountCurrent();
            }
        } else { // matchIndex == tokenCount
            if (jtl.tokenListCount() > 0) {
                jtl.setActiveTokenListIndex(jtl.tokenListCount() - 1);
                localMatchIndex = jtl.activeTokenList().tokenCountCurrent(); // Index at end (can't be join tokens)
            } else { // Empty JTL
                localMatchIndex = 0;
            }
        }
        int matchTokenListIndex = jtl.activeTokenListIndex();
        if (matchTokenListIndex >= tokenListListUpdate.modTokenListIndex + tokenListListUpdate.removedTokenListCount) {
            // Project into relexChanges
            matchTokenListIndex += tokenListListUpdate.tokenListCountDiff();
            relexChanges.get(matchTokenListIndex - startRelexTokenListIndex).setMatchIndex(localMatchIndex);
            int afterAddIndex = tokenListListUpdate.modTokenListIndex + tokenListListUpdate.addedTokenLists.size();
            while (--matchTokenListIndex >= afterAddIndex) {
                TokenListChange<T> change = relexChanges.get(matchTokenListIndex - startRelexTokenListIndex);
                change.setMatchIndex(change.tokenList().tokenCountCurrent());
            }
        }
        // Fill in the below-mod-ETLs area
        int index = tokenListListUpdate.modTokenListIndex;
        while (--index >= startRelexTokenListIndex) {
            TokenListChange<T> change = relexChanges.get(index - startRelexTokenListIndex);
            change.setMatchIndex(change.tokenList().tokenCountCurrent());
        }

        // Physically replace the token lists
        if (tokenListListUpdate.isTokenListsMod()) {
            replaceTokenLists();
        }
        jtl.moveIndexGap(startRelexTokenListIndex + relexChanges.size());

        // Remember join token count right before the first relexed ETL
        int joinTokenIndex;
        if (startRelexTokenListIndex > 0) {
            EmbeddedTokenList<T> etl = jtl.tokenList(startRelexTokenListIndex - 1);
            joinTokenIndex = etl.joinInfo.joinTokenIndex() + etl.joinTokenCount(); // Physical removal already performed
        } else {
            joinTokenIndex = 0;
        }
        // Now process each relex change and update join token count etc.
        int relexChangesSizeM1 = relexChanges.size() - 1;
        int i;
        for (i = 0; i <= relexChangesSizeM1; i++) {
            RelexTokenListChange<T> change = relexChanges.get(i);
            //assert (change.laState().size() == change.addedTokenOrEmbeddingsCount());
            EmbeddedTokenList<T> etl = (EmbeddedTokenList<T>) change.tokenList();
            if (etl.joinInfo == null) {
                etl.joinInfo = new EmbeddedJoinInfo(jtl.base(), joinTokenIndex, startRelexTokenListIndex + i);
            } else {
                etl.joinInfo.setRawJoinTokenIndex(joinTokenIndex);
            }
            // Set new joinTokenLastPartShift before calling etl.joinTokenCount()
            // Only set LPS for non-last change and in case the removal was till end
            // of ETL.
            if (i < relexChangesSizeM1 || change.index() + change.removedTokenCount() == etl.tokenCountCurrent()) {
                etl.joinInfo.setJoinTokenLastPartShift(change.joinTokenLastPartShift);
            }
            // Replace tokens in the individual ETL
            int realDiffLength = (etl == charModTokenList) ? diffLength : 0;
            etl.replaceTokens(change, realDiffLength);
            // Fix join token count
            joinTokenIndex += etl.joinTokenCount();
        }
        
        // Now fix the total join token count
        i += startRelexTokenListIndex;
        int origJoinTokenIndex = (i < jtl.tokenListCount())
                ? jtl.tokenList(i).joinInfo.joinTokenIndex()
                : jtl.base().joinTokenCount();
        int joinTokenCountDiff = joinTokenIndex - origJoinTokenIndex;
        jtl.base().updateJoinTokenCount(joinTokenCountDiff);
        
        // Possibly mark this change as bound change
        if (relexChangesSizeM1 == 0 && !tokenListListUpdate.isTokenListsMod()) { // Only change inside single ETL
            if (relexChanges.get(0).isBoundsChange()) {
                markBoundsChange(); // Joined change treated as bounds change too
            }
        }
        
        // The jtl cannot be used without jtl.resetActiveAfterUpdate() since it may cache
        //   an obsolete ETL as activeTokenList
//        jtl.resetActiveAfterUpdate();
//        assert (jtl.checkConsistency() == null) : jtl.checkConsistency();
    }

    @Override
    public String toString() {
        return super.toString() + ", tokenListListUpdate=" + tokenListListUpdate + // NOI18N
                ", startRelexTokenListIndex=" + startRelexTokenListIndex + // NOI18N
                ", relexChanges.size()=" + relexChanges.size();
    }

    @Override
    public String toStringMods(int indent) {
        StringBuilder sb = new StringBuilder(100);
        for (RelexTokenListChange change : relexChanges) {
            sb.append(change.toStringMods(indent));
            sb.append('\n');
        }
        return sb.toString();
    }
    
    static final class RelexTokenListChange<T extends TokenId> extends TokenListChange<T> {
        
        int joinTokenLastPartShift; // New value for EmbeddedJoinInfo.joinTokenLastPartShift during relex

        RelexTokenListChange(EmbeddedTokenList<T> tokenList) {
            super(tokenList);
        }

        @Override
        public String toString() {
            return super.toString() + ", lps=" + joinTokenLastPartShift;
        }
        
    }

}