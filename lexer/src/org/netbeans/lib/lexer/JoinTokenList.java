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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.JoinToken;
import org.netbeans.lib.lexer.token.PartToken;


/**
 * Join token list over certain range of ETLs of a TokenListList.
 * <br/>
 * There must always be at least one ETL in a JTL since otherwise there would be nobody
 * holding EmbeddedJoinInfo that holds JoinTokenListBase which is crucial for JTL.
 * <br/>
 * It does not have any physical storage for tokens. Regular tokens
 * are stored in individual ETLs. Tokens split across multiple ETLs
 * are represented as PartToken in each ETL referencing a JoinToken.
 * The only "countable" part is a last part of a JoinToken.
 * <br/>
 * Lookaheads and states are assigned to a last part of the JoinToken
 * and it's stored normally in ETL like for regular tokens.
 * 
 * @author Miloslav Metelka
 */

public class JoinTokenList<T extends TokenId> implements TokenList<T> {
    
    // -J-Dorg.netbeans.lib.lexer.JoinTokenList.level=FINE
    private static final Logger LOG = Logger.getLogger(JoinTokenList.class.getName());
    
    /**
     * Create join token list over an uninitialized set of embedded token lists
     * and this method will perform initial lexing of the contained embedded token lists.
     * 
     * @param tokenListList non-null tokenListList
     * @param tokenListStartIndex index of first ETL contained in the desired JTL.
     * @param tokenListCount total number of ETLs contained in the created JTL.
     * @return non-null JTL.
     */
    public static <T extends TokenId> JoinTokenList<T> create(
            TokenListList<T> tokenListList, int tokenListStartIndex, int tokenListCount
    ) {
        assert (tokenListCount > 0) : "tokenListCount must be >0";
        JoinTokenListBase base = new JoinTokenListBase(tokenListCount);
        // Create join token list - just init first ETL's join info (read by JTL's constructor)
        JoinTokenList<T> jtl = new JoinTokenList<T>(tokenListList, base, tokenListStartIndex);
        jtl.init();
        return jtl;
    }

    /** Backing token list list that holds ETLs. */
    protected final TokenListList<T> tokenListList; // 16 bytes
    
    /** Info about token list count and join token index gap. */
    protected final JoinTokenListBase base; // 12 bytes (8-super + 4)
    
    /** Start index of ETLs in TLL used by this JTL. */
    protected final int tokenListStartIndex; // 20 bytes
    
    /**
     * Index of active token list - the one used for operations like tokenOrEmbedding() etc.
     * The index may have value from zero to tokenListCount() including.
     * When set to tokenListCount() the activeStartJoinIndex == activeEndJoinIndex == tokenCount()
     * and activeTokenList == null.
     */
    protected int activeTokenListIndex; // 24 bytes

    /** Token list currently servicing requests. */
    protected  EmbeddedTokenList<T> activeTokenList; // 28 bytes
    
    /** Start join index of activeTokenList */
    protected int activeStartJoinIndex; // 32 bytes
    
    /** End join index of activeTokenList */
    protected int activeEndJoinIndex; // 36 bytes

    public JoinTokenList(TokenListList<T> tokenListList, JoinTokenListBase base, int tokenListStartIndex) {
        this.tokenListList = tokenListList;
        this.base = base;
        this.tokenListStartIndex = tokenListStartIndex;
        // Use -1 as invalid value and activeStartJoinIndex == activeEndJoinIndex == 0
        // will force fetching of appropriate ETL.
        this.activeTokenListIndex = -1;
    }

    public LanguagePath languagePath() {
        return tokenListList.languagePath();
    }

    public TokenListList<T> tokenListList() {
        return tokenListList;
    }

    public JoinTokenListBase base() {
        return base;
    }

    public int tokenListStartIndex() {
        return tokenListStartIndex;
    }

    /**
     * Get token list contained in this join token list.
     * 
     * @param index >=0 index of the token list in this joined token list.
     * @return non-null embedded token list at the given index.
     */
    public EmbeddedTokenList<T> tokenList(int index) {
        if (index < 0)
            throw new IndexOutOfBoundsException("index=" + index + " < 0"); // NOI18N
        if (index >= base.tokenListCount)
            throw new IndexOutOfBoundsException("index=" + index + " >= size()=" + base.tokenListCount); // NOI18N
        return tokenListList.get(tokenListStartIndex + index);
    }
    
    public int tokenListCount() {
        return base.tokenListCount;
    }
    
    
    public int tokenCountCurrent() {
        return base.joinTokenCount;
    }

    public int tokenCount() {
        return tokenCountCurrent();
    }
    
    public int activeStartJoinIndex() { // Use by TS.embeddedImpl()
        return activeStartJoinIndex;
    }

    public int activeEndJoinIndex() { // Use by TokenListUpdater
        return activeEndJoinIndex;
    }

    public int activeTokenListIndex() {
        return activeTokenListIndex;
    }

    public void setActiveTokenListIndex(int activeTokenListIndex) { // Used by ETL.joinTokenList()
        if (this.activeTokenListIndex != activeTokenListIndex) {
            this.activeTokenListIndex = activeTokenListIndex;
            fetchActiveTokenListData();
        }
    }

    public EmbeddedTokenList<T> activeTokenList() {
        return activeTokenList;
    }

    public TokenOrEmbedding<T> tokenOrEmbedding(int index) {
        locateTokenListByIndex(index);
        TokenOrEmbedding<T> tokenOrEmbedding = (activeTokenList != null)
                ? activeTokenList.tokenOrEmbedding(index - activeStartJoinIndex)
                : null;
        // Need to return complete token in case a token part was retrieved
        AbstractToken<T> token;
        if (index == activeStartJoinIndex && // token part can only be the first in ETL
            tokenOrEmbedding != null && // could be beyond end?
            (token = tokenOrEmbedding.token()).getClass() == PartToken.class
        ) {
            tokenOrEmbedding = ((PartToken<T>)token).joinTokenOrEmbedding();
        }
        return tokenOrEmbedding;
    }

    public int tokenOffset(AbstractToken<T> token) {
        // Should never be called for any token instances
        throw new IllegalStateException("Internal error - should never be called");
    }

    public int tokenOffset(int index) {
        locateTokenListByIndex(index);
        // Need to treat specially token parts - return offset of complete token
        AbstractToken<T> token;
        if (index == activeStartJoinIndex && // token part can only be the first in ETL
            (token = activeTokenList.tokenOrEmbedding(index - activeStartJoinIndex).token()).getClass() == PartToken.class
        ) {
            return ((PartToken<T>)token).joinToken().offset(null);
        }
        return activeTokenList.tokenOffset(index - activeStartJoinIndex);
    }

    public int tokenListIndex(int offset, int startIndex, int endIndex) {
        // First find the right ETL for the given offset and store it in activeTokenListIndex
        // Use binary search
        int low = startIndex;
        int high = endIndex - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midStartOffset = tokenList(mid).startOffset();
            
            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else {
                // Token starting exactly at ETL.startOffset()
                high = mid;
                break;
            }
        }
        // Use lower index => high
        return high; // May return -1
    }

    public int[] tokenIndex(int offset) {
        // Check if the current active token list covers the given offset.
        // If not covered then only search below/above the current active ETL.
        // It not only improves performance but it is NECESSARY for proper functionality
        // of TokenListUpdater.updateJoined() since it may skip removed ETLs
        // by manually using setActiveTokenListIndex() in the area below/above the removed ETLs.
        boolean activeStartsBelowOffset = ((offset >= activeTokenList.startOffset()) || activeTokenListIndex == 0);
        if (activeStartsBelowOffset) {
            if (offset < activeTokenList.endOffset() ||
                (activeTokenListIndex + 1 == tokenListCount() ||
                    offset < tokenList(activeTokenListIndex + 1).startOffset())
            ) {
                // Current active ETL covers the area
            } else if (activeTokenListIndex + 1 < tokenListCount()) { // Search above
                activeTokenListIndex = tokenListIndex(offset, activeTokenListIndex + 1, tokenListCount());
                fetchActiveTokenListData();
            }
        } else if (activeTokenListIndex > 0) { // Search below
            activeTokenListIndex = tokenListIndex(offset, 0, activeTokenListIndex);
            if (activeTokenListIndex < 0) {
                activeTokenListIndex = 0;
            }
            fetchActiveTokenListData();
        }

        // Now search within a single ETL by binary search
        EmbeddedJoinInfo joinInfo = activeTokenList.joinInfo;
        int joinTokenLastPartShift = joinInfo.joinTokenLastPartShift();
        int searchETLTokenCount = activeTokenList.joinTokenCount();
        int[] indexAndTokenOffset = LexerUtilsConstants.tokenIndexBinSearch(activeTokenList, offset, searchETLTokenCount);
        int etlIndex = indexAndTokenOffset[0]; // Index inside etl
        indexAndTokenOffset[0] += joinInfo.joinTokenIndex(); // Make the index joinIndex
        if (etlIndex == searchETLTokenCount && joinTokenLastPartShift > 0) { // Must move activeTokenList to last part
            // Get last part and find out how much forward is the last part
            activeTokenListIndex += joinTokenLastPartShift;
            fetchActiveTokenListData();
            PartToken<T> lastPartToken = (PartToken<T>) activeTokenList.tokenOrEmbeddingUnsync(0).token();
            indexAndTokenOffset[1] = lastPartToken.joinToken().offset(null);
            
        } else if (etlIndex == 0) { // Possibly last part of a join token
            AbstractToken<T> token = activeTokenList.tokenOrEmbedding(0).token();
            if (token.getClass() == PartToken.class) {
                // indexAndTokenOffset[0] is already ok - just fix token's offset
                indexAndTokenOffset[1] = ((PartToken<T>)token).joinToken().offset(null);
            }
        }
        return indexAndTokenOffset;
    }

    public AbstractToken<T> replaceFlyToken(int index, AbstractToken<T> flyToken, int offset) {
        locateTokenListByIndex(index);
        return activeTokenList.replaceFlyToken(index - activeStartJoinIndex, flyToken, offset);
    }

    public void wrapToken(int index, EmbeddingContainer<T> embeddingContainer) {
        locateTokenListByIndex(index);
        // !!! TBD - must not wrap complete tokens of join token list.
        // Instead wrap all part tokens with another join token list
        activeTokenList.wrapToken(index - activeStartJoinIndex, embeddingContainer);
    }

    public final int modCount() {
        return rootTokenList().modCount() + base.extraModCount;
    }
    
    public InputAttributes inputAttributes() {
        return rootTokenList().inputAttributes();
    }
    
    public int lookahead(int index) {
        // Locate embedded token list for the last token part (only that one stores the LA)
        locateTokenListByIndex(index);
        return activeTokenList.lookahead(index - activeStartJoinIndex);
    }

    public Object state(int index) {
        // Locate embedded token list for the last token part (only that one stores the state)
        locateTokenListByIndex(index);
        return activeTokenList.state(index - activeStartJoinIndex);
    }

    public final TokenList<?> rootTokenList() {
        return tokenListList.rootTokenList();
    }

    public CharSequence inputSourceText() {
        return rootTokenList().inputSourceText();
    }

    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return rootTokenList().tokenHierarchyOperation();
    }
    
    public boolean isContinuous() {
        return false; // TBD can be partially continuous - could be improved
    }

    public Set<T> skipTokenIds() {
        return null; // Not a top-level list -> no skip token ids
    }

    public int startOffset() {
        if (activeTokenListIndex == 0) {
            // Status already up-to-date
            return activeTokenList.startOffset();
        } else {
            EmbeddedTokenList<T> firstEtl = tokenList(0);
            updateStatus(firstEtl);
            return firstEtl.startOffset();
        }
    }

    public int endOffset() {
        int tokenListCountM1 = tokenListCount() - 1;
        if (tokenListCountM1 < 0) { // No token lists contained
            return 0;
        }
        if (activeTokenListIndex == tokenListCountM1) {
            // Status already up-to-date
            return activeTokenList.endOffset();
        } else {
            EmbeddedTokenList<T> lastEtl = tokenList(tokenListCountM1);
            updateStatus(lastEtl);
            return lastEtl.endOffset();
        }
    }
    
    public boolean isRemoved() {
        return false; // Should never be parented
    }

    /**
     * Get index of token list where a token for a particular join index starts
     * and index where it's located.
     *
     * @param index join index in JTL lower than tokenCountCurrent().
     * @return local index in ETL determined by activeTokenListIndex().
     */
    public int tokenStartLocalIndex(int index) {
        int tokenCount = tokenCount();
        if (index != tokenCount) {
            locateTokenListByIndex(index);
            AbstractToken<T> token = activeTokenList.tokenOrEmbeddingUnsync(index - activeStartJoinIndex).token();
            if (token.getClass() == PartToken.class) { // Last part of join token
                PartToken<T> partToken = (PartToken<T>) token;
                activeTokenListIndex -= partToken.joinToken().extraTokenListSpanCount();
                fetchActiveTokenListData();
                // The first part of join token is last in the active ETL
                return activeTokenList.tokenCountCurrent() - 1;
            }
            return index - activeStartJoinIndex;
        } else { // index == tokenCount
            setActiveTokenListIndex(tokenListCount());
            return 0;
        }
    }

    /**
     * Locate the right activeTokenList to service the requested join index.
     *
     * @param joinIndex index in a join token list.
     * @throws IndexOutOfBoundsException for joinIndex below zero.
     */
    protected final void locateTokenListByIndex(int joinIndex) {
        if (joinIndex < activeStartJoinIndex) {
            if (joinIndex < 0)
                throw new IndexOutOfBoundsException("index=" + joinIndex + " < 0");
            // Must be lower segment - first try the one below
            activeTokenListIndex--;
            fetchActiveTokenListData();
            if (joinIndex < activeStartJoinIndex) { // Still not covered
                // Do binary search on <0, activeTokenListIndex - 1>
                positionToJoinIndex(joinIndex, 0, activeTokenListIndex - 1);
            }

        } else if (joinIndex == activeEndJoinIndex) {
            int lps = activeTokenList.joinInfo.joinTokenLastPartShift();
            if (lps > 0) { // join token
                activeTokenListIndex += lps;
                fetchActiveTokenListData();
            } else { // Move to next ETL
                if (activeTokenListIndex + 1 < tokenListCount()) {
                    activeTokenListIndex++;
                    fetchActiveTokenListData();
                    // Use first token but check for empty ETL and join token
                    adjustJoinedOrSkipEmpty();
                }
            }
            
        } else if (joinIndex > activeEndJoinIndex) { // joinIndex > activeEndJoinIndex
            if (activeTokenListIndex + 1 < tokenListCount()) {
                activeTokenListIndex++;
                fetchActiveTokenListData();
                if (joinIndex >= activeEndJoinIndex) { // Still too high
                    // Do binary search on <activeTokenListIndex + 1, tokenListCount-1>
                    positionToJoinIndex(joinIndex, activeTokenListIndex + 1, base.tokenListCount - 1);
                }
            }
        }
        // The index is within bounds of activeTokenList or above its token count (which equals
        // to the fact that joinIndex is above total token count).
    }

    private void positionToJoinIndex(int joinIndex, int low, int high) {
        while (low <= high) {
            activeTokenListIndex = (low + high) >>> 1;
            fetchActiveTokenListData();
            if (activeStartJoinIndex < joinIndex) {
                low = activeTokenListIndex + 1;
            } else if (activeStartJoinIndex > joinIndex) {
                high = activeTokenListIndex - 1;
            } else { // first token of the active token list
                // The current list could be empty or possibly a part of a join token
                //   so adjust to join token's end or skip to a next non-empty ETL.
                adjustJoinedOrSkipEmpty();
                return;
            }
        }
        // low == high + 1
        // Use ETL at lower index i.e. "high". That ETL will hold the joinIndex "inside"
        // i.e. it will not be empty (because otherwise the bin-search would naturally
        // relocate to the ETL that starts exactly with joinIndex so it would be catched
        // with an exact match clause above and returned earlier).
        // For the same reason it's not necessary to check whether the token at joinIndex
        // is not a last in the ETL and check for join token possibility.
        if (activeTokenListIndex != high) {
            activeTokenListIndex = high;
            fetchActiveTokenListData();
        }
    }

    private void adjustJoinedOrSkipEmpty() {
        if (activeStartJoinIndex == activeEndJoinIndex) {
            int lps = activeTokenList.joinInfo.joinTokenLastPartShift();
            if (lps > 0) { // join token
                activeTokenListIndex += lps;
                fetchActiveTokenListData();
            } else { // Not a join token ETL
                // If current is empty go to a next non-empty ETL which will contain the joinIndex
                while (activeTokenList.tokenCountCurrent() == 0) {
                    activeTokenListIndex++;
                    fetchActiveTokenListData();
                    if (activeTokenListIndex == tokenListCount()) {
                        // All ETLs were empty including last one - the JTL.tokenOrEmbedding()
                        // should return null in this case.
                        return;
                    }
                }
                // Now located on first non-empty ETL - its first token
                // is either regular or a joined token in which case
                // it should be relocated to the last part's index to comply
                // with the JTL contract.
                if (activeStartJoinIndex == activeEndJoinIndex) { // surely non-empty; and contains single joined token
                    lps = activeTokenList.joinInfo.joinTokenLastPartShift();
                    activeTokenListIndex += lps;
                    fetchActiveTokenListData();
                }
            }
        }
    }

    protected final void fetchActiveTokenListData() {
        if (activeTokenListIndex != tokenListCount()) {
            activeTokenList = tokenList(activeTokenListIndex);
            updateStatus(activeTokenList);
            activeStartJoinIndex = activeTokenList.joinInfo.joinTokenIndex();
            activeEndJoinIndex = activeStartJoinIndex + activeTokenList.joinTokenCount();
        } else { // index at tokenListCount()
            activeTokenList = null;
            activeStartJoinIndex = activeEndJoinIndex = tokenCount();
        }
    }

    protected void updateStatus(EmbeddedTokenList<T> etl) {
        etl.embeddingContainer().updateStatus();
    }

    private void init() {
        // Notify will initialize all ETL.joinInfo except the first one (already inited)
        JoinLexerInputOperation<T> lexerInputOperation = new JoinLexerInputOperation<T>(this, 0, null, 0,
                tokenListList.get(tokenListStartIndex).startOffset());
        lexerInputOperation.init();
        AbstractToken<T> token;
        int joinTokenCount = 0;
        int tokenListCount = tokenListCount();
        if (tokenListCount > 0) {
            boolean loggable = LOG.isLoggable(Level.FINE);
            int tokenListIndex = 0;
            EmbeddedTokenList<T> tokenList = initTokenList(tokenListIndex, joinTokenCount);
            while ((token = lexerInputOperation.nextToken()) != null) {
                int skipTokenListCount;
                if ((skipTokenListCount = lexerInputOperation.skipTokenListCount()) > 0) {
                    while (--skipTokenListCount >= 0) {
                        tokenList = initTokenList(++tokenListIndex, joinTokenCount);
                    }
                    lexerInputOperation.clearSkipTokenListCount();
                }
                if (token.getClass() == JoinToken.class) {
                    // ETL for last part
                    JoinToken<T> joinToken = (JoinToken<T>) token;
                    List<PartToken<T>> joinedParts = joinToken.joinedParts();
                    // Index for active list of addition
                    // There may be ETLs so token list count may differ from part count
                    int extraTokenListSpanCount = joinToken.extraTokenListSpanCount();
                    int joinedPartIndex = 0;
                    // Only add without the last part (will be added normally outside the loop)
                    // The last ETL can not be empty (must contain the last non-empty token part)
                    for (int i = 0; i < extraTokenListSpanCount; i++) {
                        tokenList.joinInfo.setJoinTokenLastPartShift(extraTokenListSpanCount - i);
                        if (tokenList.textLength() > 0) {
                            tokenList.addToken(joinedParts.get(joinedPartIndex++), 0, null);
                        }
                        tokenList = initTokenList(++tokenListIndex, joinTokenCount);
                    }
                    // Last part will be added normally by subsequent code
                    token = joinedParts.get(joinedPartIndex); // Should be (joinedParts.size()-1)
                }
                tokenList.addToken(token, lexerInputOperation);
                if (loggable) {
                    StringBuilder sb = new StringBuilder(50);
                    ArrayUtilities.appendBracketedIndex(sb, joinTokenCount, 2);
                    token.dumpInfo(sb, null, true, true, 0);
                    sb.append('\n');
                    LOG.fine(sb.toString());
                }
                joinTokenCount++; // Increase after possible logging to start from index zero
            }
            if (loggable) {
                LOG.fine("JoinTokenList created for " + tokenListList.languagePath() + // NOI18N
                        " with " + joinTokenCount + " tokens\n"); // NOI18N
            }
            // Init possible empty ETL at the end
            while (++tokenListIndex < tokenListCount) {
                // There may be empty ETLs that contain no tokens
                tokenList = initTokenList(tokenListIndex, joinTokenCount);
            }
            // Trim storage of all ETLs to their current size
            for (int i = tokenListCount - 1; i >= 0; i--) {
                EmbeddedTokenList etl = tokenListList.get(tokenListStartIndex + i);
                etl.trimStorageToSize();
                assert (etl.joinInfo != null);
            }
        }
        base.joinTokenCount = joinTokenCount;
        // Could possibly subtract gap lengths but should not be necessary
    }

    private EmbeddedTokenList<T> initTokenList(int tokenListIndex, int joinTokenCount) {
        EmbeddedTokenList<T> tokenList = tokenList(tokenListIndex);
        if (!tokenList.embedding().joinSections()) {
            throw new IllegalStateException(
                    "Embedding " + tokenList.embedding() + " not declared to join sections. " +
                    tokenList.dumpInfo(null)
            );
        }
                
        if (tokenList.tokenCountCurrent() > 0) {
            // Clear all tokens so that it can be initialized by joined lexing.
            // This situation may arise when there would be mixed joining and non-joining ETLs
            // (see also TokenListList's constructor and scanTokenList()).
            tokenList.clear();
//                throw new IllegalStateException(
//                        "Non-empty embedded token list in JoinTokenList initialization. " +
//                        tokenList.dumpInfo(null) + "\n" + tokenListList
//                );
        }
        assert (tokenList.joinInfo == null) : "Non-null joinInfo in tokenList " +
                tokenList.dumpInfo(null) + "\n" + tokenListList;
        tokenList.joinInfo = new EmbeddedJoinInfo(base, joinTokenCount, tokenListIndex);
        return tokenList;
    }
    
    public String checkConsistency() {
        // Check regular consistency without checking embeddings
        String error = LexerUtilsConstants.checkConsistencyTokenList(this, false);
        if (error == null) {
            // Check individual ETLs and their join infos
            int joinTokenCount = 0;
            JoinToken<T> activeJoinToken = null;
            int joinedPartCount = 0;
            int nextCheckPartIndex = 0;
            for (int tokenListIndex = 0; tokenListIndex < tokenListCount(); tokenListIndex++) {
                EmbeddedTokenList<T> etl = tokenList(tokenListIndex);
                error = LexerUtilsConstants.checkConsistencyTokenList(etl, false);
                if (error != null)
                    return error;

                if (etl.joinInfo == null) {
                    return "Null joinInfo for ETL at token-list-index " + tokenListIndex; // NOI18N
                }
                if (joinTokenCount != etl.joinInfo.joinTokenIndex()) {
                    return "joinTokenIndex=" + joinTokenCount + " != etl.joinInfo.joinTokenIndex()=" + // NOI18N
                            etl.joinInfo.joinTokenIndex() + " at token-list-index " + tokenListIndex; // NOI18N
                }
                if (tokenListIndex != etl.joinInfo.tokenListIndex()) {
                    return "token-list-index=" + tokenListIndex + " != etl.joinInfo.tokenListIndex()=" + // NOI18N
                            etl.joinInfo.tokenListIndex();
                }

                int etlTokenCount = etl.tokenCount();
                int etlJoinTokenCount = etlTokenCount;
                if (etlTokenCount > 0) {
                    AbstractToken<T> token = etl.tokenOrEmbeddingUnsync(0).token();
                    int startCheckIndex = 0;
                    // Check first token (may also be the last token)
                    if (activeJoinToken != null) {
                        if (token.getClass() != PartToken.class) {
                            return "Unfinished joinToken at token-list-index=" + tokenListIndex; // NOI18N
                        }
                        error = checkConsistencyJoinToken(activeJoinToken, token, nextCheckPartIndex++, tokenListIndex);
                        if (error != null) {
                            return error;
                        }
                        if (nextCheckPartIndex == joinedPartCount) {
                            activeJoinToken = null; // activeJoinToken ended
                        } else { // For non-last there must be no other tokens in the list
                            if (etlTokenCount > 1) {
                                return "More than one token and non-last part of unfinished join token" +  // NOI18N
                                        " at token-list-index " + tokenListIndex; // NOI18N
                            }
                            // etlTokenCount so the first token is last too
                            // and this is an ETL with single token part that continues activeJoinToken
                            etlJoinTokenCount--;
                        }
                        startCheckIndex = 1;
                    }
                    // Check last token
                    if (etlTokenCount > startCheckIndex) {
                        assert (activeJoinToken == null);
                        token = etl.tokenOrEmbeddingUnsync(etlTokenCount - 1).token();
                        if (token.getClass() == PartToken.class) {
                            etlJoinTokenCount--;
                            activeJoinToken = ((PartToken<T>) token).joinToken();
                            joinedPartCount = activeJoinToken.joinedParts().size();
                            nextCheckPartIndex = 0;
                            if (joinedPartCount < 2) {
                                return "joinedPartCount=" + joinedPartCount + " < 2";
                            }
                            error = checkConsistencyJoinToken(activeJoinToken, token, nextCheckPartIndex++, tokenListIndex);
                            if (error != null)
                                return error;
                        }
                    }
                    // Check that no other token are part tokens than the relevant ones
                    for (int j = startCheckIndex; j < etlJoinTokenCount; j++) {
                        if (etl.tokenOrEmbeddingUnsync(j).token().getClass() == PartToken.class) {
                            return "Inside PartToken at index " + j + "; joinTokenCount=" + etlJoinTokenCount; // NOI18N
                        }
                    }
                }
                if (etlJoinTokenCount != etl.joinTokenCount()) {
                    return "joinTokenCount=" + etlJoinTokenCount + " != etl.joinTokenCount()=" + // NOI18N
                            etl.joinTokenCount() + " at token-list-index " + tokenListIndex; // NOI18N
                }
                joinTokenCount += etlJoinTokenCount;
            } // end-of-for over ETLs
            if (activeJoinToken != null) {
                return "Unfinished join token at end";
            }
            if (joinTokenCount != base.joinTokenCount) {
                return "joinTokenCount=" + joinTokenCount + " != base.joinTokenCount=" + base.joinTokenCount; // NOI18N
            }
        }
        // Check placement of index gap
        return error;
    }

    private String checkConsistencyJoinToken(JoinToken<T> joinToken, AbstractToken<T> token, int partIndex, int tokenListIndex) {
        PartToken<T> partToken = (PartToken<T>) token;
        if (joinToken.joinedParts().get(partIndex) != token) {
            return "activeJoinToken.joinedParts().get(" + partIndex + // NOI18N
                    ") != token at token-list-index " + tokenListIndex; // NOI18N
        }
        if (partToken.joinToken() != joinToken) {
            return "Invalid join token of part at partIndex " + partIndex + // NOI18N
                    " at token-list-index " + tokenListIndex; // NOI18N
        }
        EmbeddedTokenList<T> etl = tokenList(tokenListIndex);
        int lps = etl.joinInfo.joinTokenLastPartShift();
        if (lps < 0) {
            return "lps=" + lps + " < 0";
        }

        if (tokenListIndex + lps >= tokenListCount()) {
            return "Invalid lps=" + lps + // NOI18N
                    " at token-list-index " + tokenListIndex + // NOI18N
                    "; tokenListCount=" + tokenListCount(); // NOI18N
        }
        AbstractToken<T> lastPart = tokenList(tokenListIndex + lps).tokenOrEmbeddingUnsync(0).token();
        if (lastPart.getClass() != PartToken.class) {
            return "Invalid lps: lastPart not PartToken " + lastPart.dumpInfo(null, null, true, true, 0) + // NOI18N
                    " at token-list-index " + tokenListIndex; // NOI18N
        }
        if (((PartToken<T>)lastPart).joinToken().lastPart() != lastPart) {
            return "Invalid lps: Not last part " + lastPart.dumpInfo(null, null, true, true, 0) + // NOI18N
                    " at token-list-index " + tokenListIndex; // NOI18N
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        int tokenListCount = tokenListCount();
        int digitCount = String.valueOf(tokenListCount - 1).length();
        for (int i = 0; i < tokenListCount; i++) {
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            tokenList(i).dumpInfo(sb);
            sb.append('\n');
        }
        return LexerUtilsConstants.appendTokenList(sb, this).toString();
    }

}

