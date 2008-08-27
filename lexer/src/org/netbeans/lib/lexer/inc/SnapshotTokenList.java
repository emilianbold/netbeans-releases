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

import java.util.Set;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CompactMap;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.EmbeddingContainer;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.TokenOrEmbedding;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.TextToken;


/**
 * Token list used by token hierarchy snapshot.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class SnapshotTokenList<T extends TokenId> implements TokenList<T> {

    /** Due to debugging purposes - dumpInfo() use. */
    private TokenHierarchyOperation<?,T> snapshot;

    private IncTokenList<T> liveTokenList;

    private int liveTokenGapStart = -1;

    private int liveTokenGapEnd;

    private int liveTokenGapStartOffset;

    private int liveTokenOffsetDiff;

    /** Captured original tokens or branches. */
    private TokenOrEmbedding<T>[] origTokenOrEmbeddings;

    /** Original token's offsets. The array is occupied
     * and maintained in the same way like origTokensOrBranches.
     */
    private int[] origOffsets;

    /** Index where tokens start in tokens array. */
    private int origTokenStartIndex;

    /** Number of original tokens. */
    private int origTokenCount;

    /** Overrides of tokens' offset. */
    private CompactMap<AbstractToken<T>, Token2OffsetEntry<T>> token2offset;

    public int liveTokenGapStart() {
        return liveTokenGapStart;
    }

    public int liveTokenGapEnd() {
        return liveTokenGapEnd;
    }
    
    public SnapshotTokenList(TokenHierarchyOperation<?,T> snapshot) {
        this.snapshot = snapshot;
//        this.liveTokenList = (IncTokenList<T>)snapshot.
//                liveTokenHierarchyOperation().rootTokenList();
        token2offset = new CompactMap<AbstractToken<T>,Token2OffsetEntry<T>>();
    }

    public TokenHierarchyOperation<?,T> snapshot() {
        return snapshot;
    }
    
    public LanguagePath languagePath() {
        return liveTokenList.languagePath();
    }
    
    public TokenOrEmbedding<T> tokenOrEmbedding(int index) {
        if (liveTokenGapStart == -1 || index < liveTokenGapStart) {
            return liveTokenList.tokenOrEmbedding(index);
        }
        index -= liveTokenGapStart;
        if (index < origTokenCount) {
            return origTokenOrEmbeddings[origTokenStartIndex + index];
        }
        return liveTokenList.tokenOrEmbedding(liveTokenGapEnd + index - origTokenCount);
    }

    public int lookahead(int index) {
        // Lookahead not supported for certain snapshot's tokens
        // so better don't return it for any of them.
        return -1;
    }

    public Object state(int index) {
        // Lookahead not supported for certain snapshot's tokens
        // so better don't return it for any of them.
        return null;
    }

    public int tokenOffset(int index) {
        if (liveTokenGapStart == -1 || index < liveTokenGapStart) {
            return liveTokenList.tokenOffset(index);
        }
        index -= liveTokenGapStart;
        if (index < origTokenCount) {
            return origOffsets[origTokenStartIndex + index];
        }
        index -= origTokenCount;

        AbstractToken<T> token = liveTokenList.tokenOrEmbeddingUnsync(liveTokenGapEnd + index).token();
        int offset;
        if (token.isFlyweight()) {
            offset = token.length();
            while (--index >= 0) {
                token = liveTokenList.tokenOrEmbeddingUnsync(liveTokenGapEnd + index).token();
                if (token.isFlyweight()) {
                    offset += token.length();
                } else { // non-flyweight element
                    offset += tokenOffset(token, liveTokenList);
                    break;
                }
            }
            if (index == -1) { // below the boundary of above-gap live tokens
                index += liveTokenGapStart + origTokenCount;
                if (index >= 0) {
                    offset += tokenOffset(index);
                }
            }
            
        } else { // non-flyweight
            offset = tokenOffset(token, liveTokenList);
        }
        return offset;
    }

    public int[] tokenIndex(int offset) {
        return LexerUtilsConstants.tokenIndexLazyTokenCreation(this, offset);
    }

    /**
     * @param token non-null token for which the offset is being computed.
     * @param tokenList non-null token list to which the token belongs.
     * @param rawOffset raw offset of the token.
     * @return offset for the particular token.
     */
    public <TT extends TokenId> int tokenOffset(
    AbstractToken<TT> token, TokenList<TT> tokenList) {
        // The following situations can happen:
        // 1. Token instance is contained in token2offset map so the token's
        //    offset is overriden by the information in the map.
        // 2. Token instance is not contained in token2offset map
        //    and the token's tokenList is IncTokenList. In that case
        //    it needs to be checked whether the regularly computed offset
        //    is above liveTokenGapStartOffset and if so then
        //    liveTokenOffsetDiff must be added to it.
        // 3. Token instance is not contained in token2offset map
        //    and the token's tokenList is not IncTokenList.
        //    It happens for removed tokens that were removed but
        //    do not need the offset correction.
        //    In that case the computed offset is simply returned.
        // 4. Token from branch token list is passed.
        //    In this case the offset of the corresponding rootBranchToken
        //    needs to be corrected if necessary.
        if (tokenList.getClass() == EmbeddedTokenList.class) {
            EmbeddedTokenList<TT> etl = (EmbeddedTokenList<TT>)tokenList;
            AbstractToken<?> rootBranchToken = null; // originally etl.rootToken();
            Token2OffsetEntry<T> entry = token2offset.get(rootBranchToken);
            if (entry != null) {
                return entry.offset();// used to be: + etl.childTokenOffsetShift(rawOffset);
            } else { // no special entry => check whether the regular offset is below liveTokenGapStartOffset
                int offset = etl.tokenOffset(token);
                TokenList rootTokenList = etl.rootTokenList();
                if (rootTokenList != null && rootTokenList.getClass() == IncTokenList.class) {
                    if (offset >= liveTokenGapStartOffset) {
                        offset += liveTokenOffsetDiff;
                    }
                }
                return offset;
            }

        } else { // queried token list is the root list genericsed by <T>
            @SuppressWarnings("unchecked")
            Token2OffsetEntry<T> entry = token2offset.get((AbstractToken<T>)token);
            if (entry != null) {
                return entry.offset();
            } else {
                int offset = tokenList.tokenOffset(token);
                if (tokenList.getClass() == IncTokenList.class) {
                    if (offset >= liveTokenGapStartOffset) {
                        offset += liveTokenOffsetDiff;
                    }
                }
                return offset;
            }
        }
    }
    
    public int tokenCount() {
        return (liveTokenGapStart == -1)
                ? liveTokenList.tokenCount()
                : liveTokenList.tokenCount() - (liveTokenGapEnd - liveTokenGapStart)
                    + origTokenCount;
    }

    public int tokenCountCurrent() {
        return (liveTokenGapStart == -1)
                ? liveTokenList.tokenCountCurrent()
                : liveTokenList.tokenCountCurrent() - (liveTokenGapEnd - liveTokenGapStart)
                    + origTokenCount;
    }

    public int modCount() {
        return LexerUtilsConstants.MOD_COUNT_IMMUTABLE_INPUT;
    }
    
    public int tokenOffset(AbstractToken<T> token) {
        int rawOffset = token.rawOffset();
        // Offset of the standalone token is absolute
        return rawOffset;
    }
    
    public char charAt(int offset) {
        // No tokens expected to be parented to this token list
        throw new IllegalStateException("Not expected to be called"); // NOI18N
    }

    public void wrapToken(int index, EmbeddingContainer<T> embeddingContainer) {
        // Allow branching
        if (liveTokenGapStart == -1 || index < liveTokenGapStart) {
            liveTokenList.wrapToken(index, embeddingContainer);
        } else {
            index -= liveTokenGapStart;
            if (index < origTokenCount) {
                origTokenOrEmbeddings[origTokenStartIndex + index] = embeddingContainer;
            } else {
                liveTokenList.wrapToken(liveTokenGapEnd + index - origTokenCount, embeddingContainer);
            }
        }
    }

    public AbstractToken<T> replaceFlyToken(int index, AbstractToken<T> flyToken, int offset) {
        AbstractToken<T> nonFlyToken;
        if (liveTokenGapStart == -1 || index < liveTokenGapStart) {
            nonFlyToken = liveTokenList.replaceFlyToken(index, flyToken, offset);
        } else {
            index -= liveTokenGapStart;
            if (index < origTokenCount) {
                nonFlyToken = ((TextToken<T>)flyToken).createCopy(this, offset);
                origTokenOrEmbeddings[origTokenStartIndex + index] = nonFlyToken;
            } else {
                nonFlyToken = liveTokenList.replaceFlyToken(
                        liveTokenGapEnd + index - origTokenCount,
                        flyToken, offset - liveTokenOffsetDiff);
            }
        }
        return nonFlyToken;
    }
    
    public TokenList<?> rootTokenList() {
        return this;
    }

    public CharSequence inputSourceText() {
        return rootTokenList().inputSourceText();
    }

    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return snapshot;
    }
    
    public InputAttributes inputAttributes() {
        return liveTokenList.inputAttributes();
    }

    public boolean isContinuous() {
        return true;
    }

    public Set<T> skipTokenIds() {
        return null;
    }

    public int startOffset() {
        if (tokenCountCurrent() > 0 || tokenCount() > 0)
            return tokenOffset(0);
        return 0;
    }

    public int endOffset() {
        int cntM1 = tokenCount() - 1;
        if (cntM1 >= 0)
            return tokenOffset(cntM1) + tokenOrEmbedding(cntM1).token().length();
        return 0;
    }
    
    public boolean isRemoved() {
        return false;
    }

    public boolean canModifyToken(int index, AbstractToken token) {
        return liveTokenGapStart != -1
                && index >= liveTokenGapStart
                && index < liveTokenGapEnd
                && !token2offset.containsKey(token);
    }

    public void update(TokenHierarchyEventInfo eventInfo, TokenListChange<T> change) {
        TokenList<T> removedTokenList = change.tokenChangeInfo().removedTokenList();
        int startRemovedIndex = change.index();
        int endRemovedIndex = startRemovedIndex + removedTokenList.tokenCount();
        if (liveTokenGapStart == -1) { // no modifications yet
            liveTokenGapStart = startRemovedIndex;
            liveTokenGapEnd = startRemovedIndex;
            liveTokenGapStartOffset = change.offset();
            @SuppressWarnings("unchecked")
            TokenOrEmbedding<T>[] tokenOrEmbeddings = new TokenOrEmbedding[removedTokenList.tokenCount()];
            origTokenOrEmbeddings = tokenOrEmbeddings;
            origOffsets = new int[origTokenOrEmbeddings.length];
        }

        int liveTokenIndexDiff = change.tokenChangeInfo().addedTokenCount()
                - removedTokenList.tokenCount();
        if (startRemovedIndex < liveTokenGapStart) { // will affect initial shared tokens
            int extraOrigTokenCount = liveTokenGapStart - startRemovedIndex;
            ensureOrigTokensStartCapacity(extraOrigTokenCount);
            origTokenStartIndex -= extraOrigTokenCount;
            origTokenCount += extraOrigTokenCount;

            int bound = Math.min(endRemovedIndex, liveTokenGapStart);
            int index;
            int offset = change.offset();
            liveTokenGapStartOffset = offset;
            for (index = startRemovedIndex; index < bound; index++) {
                TokenOrEmbedding<T> tokenOrEmbedding = removedTokenList.tokenOrEmbedding(index - startRemovedIndex);
                AbstractToken<T> token = tokenOrEmbedding.token();
                if (!token.isFlyweight()) {
                    TokenList<T> tokenList = token.tokenList();
                    if (tokenList == null) {
                        tokenList = null; // new StandaloneTokenList<T>(change.languagePath(),
                                // eventInfo.originalText().toCharArray(offset, offset + token.length()));
                        if (!token.isFlyweight())
                            token.setTokenList(tokenList);
                    }
                }
                origOffsets[origTokenStartIndex] = offset;
                origTokenOrEmbeddings[origTokenStartIndex++] = tokenOrEmbedding;
                offset += token.length();
            }

            while (index < liveTokenGapStart) {
                TokenOrEmbedding<T> tokenOrEmbedding = liveTokenList.tokenOrEmbeddingUnsync(index + liveTokenIndexDiff);
                AbstractToken<T> t = tokenOrEmbedding.token();
                if (!t.isFlyweight()) {
                    token2offset.putEntry(new Token2OffsetEntry<T>(t, offset));
                }
                origOffsets[origTokenStartIndex] = offset;
                origTokenOrEmbeddings[origTokenStartIndex++] = tokenOrEmbedding;
                offset += t.length();
                index++;
            }
            liveTokenGapStart = startRemovedIndex;
        }

        if (endRemovedIndex > liveTokenGapEnd) { // will affect ending shared tokens
            int extraOrigTokenCount = endRemovedIndex - liveTokenGapEnd;
            ensureOrigTokensEndCapacity(extraOrigTokenCount);
            origTokenCount += extraOrigTokenCount;
            int origTokenIndex = origTokenStartIndex + origTokenCount - 1;

            int bound = Math.max(startRemovedIndex, liveTokenGapEnd);
            int index = endRemovedIndex;
            int offset = change.removedEndOffset();
            for (index = endRemovedIndex - 1; index >= bound; index--) {
                TokenOrEmbedding<T> tokenOrEmbedding = removedTokenList.tokenOrEmbedding(index - startRemovedIndex);
                AbstractToken<T> token = tokenOrEmbedding.token();
                offset -= token.length();
                if (!token.isFlyweight()) {
                    TokenList<T> tokenList = token.tokenList();
                    if (tokenList == null) {
                        tokenList = null; // new StandaloneTokenList<T>(change.languagePath(),
                                // eventInfo.originalText().toCharArray(offset, offset + token.length()));
                        if (!token.isFlyweight())
                            token.setTokenList(tokenList);
                    }
                }
                origOffsets[origTokenIndex] = offset + liveTokenOffsetDiff;
                // If the token's offset had to be diff-ed already then a map entry is necessary
                if (liveTokenOffsetDiff != 0) {
                    token2offset.putEntry(new Token2OffsetEntry<T>(token, origOffsets[origTokenIndex]));
                }
                origTokenOrEmbeddings[origTokenIndex--] = tokenOrEmbedding;
            }

            while (index >= liveTokenGapEnd) {
                TokenOrEmbedding<T> tokenOrEmbedding = liveTokenList.tokenOrEmbeddingUnsync(index + liveTokenIndexDiff);
                AbstractToken<T> token = tokenOrEmbedding.token();
                offset -= token.length();
                if (!token.isFlyweight()) {
                    token2offset.putEntry(new Token2OffsetEntry<T>(token, offset));
                }
                origOffsets[origTokenIndex] = offset + liveTokenOffsetDiff;
                token2offset.putEntry(new Token2OffsetEntry<T>(token, origOffsets[origTokenIndex]));
                origTokenOrEmbeddings[origTokenIndex--] = tokenOrEmbedding;
                index--;
            }
            liveTokenGapEnd = endRemovedIndex;
        }

        liveTokenOffsetDiff += eventInfo.removedLength() - eventInfo.insertedLength();
        liveTokenGapEnd += liveTokenIndexDiff;
    }

    private void ensureOrigTokensStartCapacity(int extraOrigTokenCount) {
        if (extraOrigTokenCount > origTokenOrEmbeddings.length - origTokenCount) { // will need to reallocate
            // Could check for maximum possible token count (origTokenCount + below-and-above live token counts)
            // but would cause init of live tokens above gap which is undesirable
            @SuppressWarnings("unchecked")
            TokenOrEmbedding<T>[] newOrigTokensOrBranches = new TokenOrEmbedding[
                    (origTokenOrEmbeddings.length * 3 / 2) + extraOrigTokenCount];
            int[] newOrigOffsets = new int[newOrigTokensOrBranches.length];
            int newIndex = Math.max(extraOrigTokenCount, (newOrigTokensOrBranches.length
                    - (origTokenCount + extraOrigTokenCount)) / 2);
            System.arraycopy(origTokenOrEmbeddings, origTokenStartIndex,
                    newOrigTokensOrBranches, newIndex, origTokenCount);
            System.arraycopy(origOffsets, origTokenStartIndex,
                    newOrigOffsets, newIndex, origTokenCount);
            origTokenOrEmbeddings = newOrigTokensOrBranches;
            origOffsets = newOrigOffsets;
            origTokenStartIndex = newIndex;

        } else if (extraOrigTokenCount > origTokenStartIndex) { // only move
            // Move to the end of the array
            int newIndex = origTokenOrEmbeddings.length - origTokenCount;
            System.arraycopy(origTokenOrEmbeddings, origTokenStartIndex,
                    origTokenOrEmbeddings, newIndex, origTokenCount);
            System.arraycopy(origOffsets, origTokenStartIndex,
                    origOffsets, newIndex, origTokenCount);
            origTokenStartIndex = origTokenOrEmbeddings.length - origTokenCount;
        }
    }
    
    private void ensureOrigTokensEndCapacity(int extraOrigTokenCount) {
        if (extraOrigTokenCount > origTokenOrEmbeddings.length - origTokenCount) { // will need to reallocate
            // Could check for maximum possible token count (origTokenCount + below-and-above live token counts)
            // but would cause init of live tokens above gap which is undesirable
            @SuppressWarnings("unchecked")
            TokenOrEmbedding<T>[] newOrigTokensOrBranches = new TokenOrEmbedding[
                    (origTokenOrEmbeddings.length * 3 / 2) + extraOrigTokenCount];
            int[] newOrigOffsets = new int[newOrigTokensOrBranches.length];
            int newIndex = (newOrigTokensOrBranches.length
                    - (origTokenCount + extraOrigTokenCount)) / 2;
            System.arraycopy(origTokenOrEmbeddings, origTokenStartIndex,
                    newOrigTokensOrBranches, newIndex, origTokenCount);
            System.arraycopy(origOffsets, origTokenStartIndex,
                    newOrigOffsets, newIndex, origTokenCount);
            origTokenOrEmbeddings = newOrigTokensOrBranches;
            origOffsets = newOrigOffsets;
            origTokenStartIndex = newIndex;

        } else if (extraOrigTokenCount > origTokenOrEmbeddings.length - origTokenCount - origTokenStartIndex) { // only move
            // Move to the end of the array
            System.arraycopy(origTokenOrEmbeddings, origTokenStartIndex,
                    origTokenOrEmbeddings, 0, origTokenCount);
            System.arraycopy(origOffsets, origTokenStartIndex,
                    origOffsets, 0, origTokenCount);
            origTokenStartIndex = 0;
        }
    }

    @Override
    public String toString() {
        return "liveTokenGapStart=" + liveTokenGapStart +
                ", liveTokenGapEnd=" + liveTokenGapEnd +
                ", liveTokenGapStartOffset=" + liveTokenGapStartOffset +
                ", liveTokenOffsetDiff=" + liveTokenOffsetDiff +
                ",\n origTokenStartIndex=" + origTokenStartIndex +
                ", origTokenCount=" + origTokenCount +
                ", token2offset: " + token2offset;

    }

    public int tokenShiftStartOffset() {
        return liveTokenGapStartOffset;
    }
    
    public int tokenShiftEndOffset() {
        return liveTokenGapStartOffset + liveTokenOffsetDiff;
    }

    private static final class Token2OffsetEntry<T extends TokenId>
    extends CompactMap.MapEntry<AbstractToken<T>,Token2OffsetEntry<T>> {
        
        private final AbstractToken<T> token; // 20 bytes (16-super + 4)
        
        private final int offset; // 24 bytes
        
        Token2OffsetEntry(AbstractToken<T> token, int offset) {
            this.token = token;
            this.offset = offset;
        }
        
        public AbstractToken<T> getKey() {
            return token;
        }

        public Token2OffsetEntry<T> getValue() {
            return this;
        }
        
        protected int valueHashCode() {
            return offset;
        }

        protected boolean valueEquals(Object value2) {
            // In fact the second entry would have to be of <T> because
            // the tokens (as keys) must be the same objects to be equal
            return (value2 instanceof Token2OffsetEntry
                    && ((Token2OffsetEntry<?>)value2).offset() == offset());
        }
        
        public int offset() {
            return offset;
        }

        public Token2OffsetEntry<T> setValue(Token2OffsetEntry<T> value) {
            throw new IllegalStateException("Prohibited"); // NOI18N
        }

        @Override
        public String toString() {
            // Debug the offset being held
            return String.valueOf(offset);
        }

    }
    
}
