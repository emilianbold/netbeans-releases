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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.inc.IncTokenList;
import org.netbeans.lib.lexer.inc.MutableTokenList;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.lib.lexer.inc.TokenListChange;
import org.netbeans.lib.lexer.inc.TokenListUpdater;
import static org.netbeans.lib.lexer.LexerUtilsConstants.INVALID_STATE;

/**
 * Request for updating of token hierarchy after text modification
 * or custom embedding creation/removal.
 * <br/>
 * This class contains all the data and methods related to updating.
 *
 * @author Miloslav Metelka
 */

public final class TokenHierarchyUpdate {
    
    // -J-Dorg.netbeans.lib.lexer.TokenHierarchyUpdate.level=FINE
    static final Logger LOG = Logger.getLogger(TokenHierarchyUpdate.class.getName());

    final TokenHierarchyEventInfo eventInfo;
    
    private Map<LanguagePath,TLLInfo> path2info;
    
    /**
     * Infos ordered from higher top levels of the hierarchy to lower levels.
     * Useful for top-down updating at the end.
     */
    private List<List<TLLInfo>> levelInfos;
    
    TokenListChange<?> rootChange;

    public TokenHierarchyUpdate(TokenHierarchyEventInfo eventInfo) {
        this.eventInfo = eventInfo;
    }

    public void update(IncTokenList<?> incTokenList) {
        incTokenList.incrementModCount();
        // Update top-level token list first
        // It does not need any updateStatusImpl() since it's only for embedded token lists
        rootChange = updateTokenListByModification(incTokenList, null);
        eventInfo.setTokenChangeInfo(rootChange.tokenChangeInfo());

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("ROOT CHANGE: " + rootChange.toString(0) + "\n"); // NOI18N
        }

        // If there is an active lexer input operation (for not-yet finished
        // top-level token list lexing) refresh it because it would be damaged
        // by the performed token list update
        if (!incTokenList.isFullyLexed()) {
            incTokenList.refreshLexerInputOperation();
        }

        // Now the update goes to possibly embedded token list lists
        // based on the top-level change. If there are embeddings that join sections
        // this becomes a fairly complex thing.
        // 1. The updating must always go from upper levels to lower levels of the token hierarchy
        //    to ensure that the tokens of the possible joined embeddings get updated properly
        //    as the tokens created/removed at upper levels may contain embeddings that will
        //    need to be added/removed from token list lists on lower level.
        // 2. A single insert/remove may produce token updates at several
        //    places in the document. A top-level change of token with embedding
        //    will request the embedded token list update and that token list
        //    may be connected with another joined token list(s) with the same language path
        //    and the update may continue into these joined token lists.

        // 3. The algorithm must collect both removed and added token lists
        //    in the TLLInfo.
        // 4. For a removed token list the updating must check nested embedded token lists
        //    because some embedded tokens of the removed embedded token list might contain
        //    another embedding that might also be maintained as token list list
        //    and need to be updated.
        // 5. The parent token list lists
        //    are always maintained too which simplifies the updating algorithm
        //    and speeds it up because the token list list marks whether it has any children
        //    or not and so the deep traversing only occurs if there are any children present.
        // 6. Additions may produce nested additions too so they need to be makred
        //    similarly to removals.
        if (rootChange.isBoundsChange()) {
            processBoundsChangeEmbeddings(rootChange, null);
        } else {
            // Mark changed area based on start of first mod.token and end of last mod.token
            // of the root-level change
            eventInfo.setMinAffectedStartOffset(rootChange.offset());
            eventInfo.setMaxAffectedEndOffset(rootChange.addedEndOffset());
            processNonBoundsChange(rootChange);
        }

        processLevelInfos();
    }
    
    public void updateCreateEmbedding(EmbeddedTokenList<?> addedTokenList) {
        TLLInfo info = info(addedTokenList.languagePath());
        if (info != NO_INFO) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("THU.updateCreateEmbedding(): " + addedTokenList.toStringHeader());
            }
            info.markAdded(addedTokenList);
            processLevelInfos();
        }
    }
    
    /**
     * update-status must be called by the caller.
     * @param removedTokenList token list removed by TS.removeEmbedding().
     */
    public void updateRemoveEmbedding(EmbeddedTokenList<?> removedTokenList) {
        TLLInfo info = info(removedTokenList.languagePath());
        if (info != NO_INFO) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("THU.updateRemoveEmbedding(): " + removedTokenList.toStringHeader());
            }
            // update-status called by caller.
            info.markRemoved(removedTokenList);
            processLevelInfos();
        }
    }
    
    void processBoundsChangeEmbeddings(TokenListChange<?> change, TokenListChange<?> parentChange) {
        // Add an embedded change to the parent change (if exists)
        if (parentChange != null) {
            parentChange.tokenChangeInfo().addEmbeddedChange(change.tokenChangeInfo());
        }
        Object tokenOrEC = change.tokenChangeInfo().removedTokenList().tokenOrEmbeddingContainer(0);
        if (tokenOrEC.getClass() == EmbeddingContainer.class) {
            TLLInfo info;
            boolean hasChildren;
            if (change.languagePath().size() > 1) {
                info = info(change.languagePath());
                hasChildren = (info != NO_INFO) ? info.tokenListList().hasChildren() : false;
            } else { // root-level
                info = NO_INFO;
                hasChildren = (eventInfo.tokenHierarchyOperation().maxTokenListListPathSize() > 0);
            }
            EmbeddingContainer<?> ec = (EmbeddingContainer<?>)tokenOrEC;
            rewrapECToken(ec, change); // Includes updateStatusImpl()
            EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
            if (etl != null && etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING) {
                // Check the text length beyond modification => end skip length must not be affected
                int modRelOffset = eventInfo.modificationOffset() - change.offset();
                int beyondModLength = change.addedEndOffset() - (eventInfo.modificationOffset() + eventInfo.diffLengthOrZero());
                EmbeddedTokenList<?> prevEtl = null;
                do {
                    TLLInfo childInfo = hasChildren ? info(etl.languagePath()) : NO_INFO;
                    // Check whether the change was not in the start or end skip lengths
                    // and if so then remove the embedding
                    if (modRelOffset >= etl.embedding().startSkipLength()
                            && beyondModLength >= etl.embedding().endSkipLength()
                    ) { // Modification within embedding's bounds => embedding can stay
                        // Mark that the embedding should be updated
                        if (childInfo != NO_INFO) {
                            // update-status called by rewrap-ec-token above
                            childInfo.markBoundsChange(etl);
                        } else { // No child but want to update nested possible bounds changes
                            if (etl.isInited()) {
                                parentChange = change;
                                // Perform change in child - it surely does not join the sections
                                // since otherwise the childInfo could not be null
                                // update-status done above for the embedding container
                                change = updateTokenListByModification(etl, null);
                                if (change.isBoundsChange()) {
                                    processBoundsChangeEmbeddings(change, parentChange);
                                } else {
                                    eventInfo.setMinAffectedStartOffset(change.offset());
                                    eventInfo.setMaxAffectedEndOffset(change.addedEndOffset());
                                }
                            }
                        }
                        prevEtl = etl;
                        etl = etl.nextEmbeddedTokenList();

                    } else { // Mod in skip lengths => Remove the etl from chain
                        if (childInfo != NO_INFO) {
                            // update-status already done as part of rewrap-token
                            childInfo.markRemoved(etl);
                        }
                        // Remove embedding and get the next embedded token list (prevEtl stays the same)
                        etl = ec.removeEmbeddedTokenList(prevEtl, etl);
                    }
                } while (etl != null && etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING);
            }
        } 
    }
    
    void processNonBoundsChange(TokenListChange<?> change) {
        TLLInfo info;
        boolean hasChildren;
        if (change.languagePath().size() >= 2) {
            info = info(change.languagePath());
            hasChildren = (info != NO_INFO && info.tokenListList().hasChildren());
        } else { // root change
            info = NO_INFO;
            hasChildren = (eventInfo.tokenHierarchyOperation().maxTokenListListPathSize() > 0);
        }
        if (hasChildren) {
            // First mark the removed embeddings
            TokenList<?> removedTokenList = change.tokenChangeInfo().removedTokenList();
            if (removedTokenList != null) {
                markRemovedEmbeddings(removedTokenList);
            }

            // Now mark added embeddings
            TokenList<?> currentTokenList = change.tokenChangeInfo().currentTokenList();
            markAddedEmbeddings(currentTokenList, change.index(), change.addedTokensOrBranchesCount());
        }
    }
    
    /**
     * Collect removed embeddings for the given token list recursively
     * and nest deep enough for all maintained children
     * token list lists.
     */
    private void markRemovedEmbeddings(TokenList<?> removedTokenList) {
        int tokenCount = removedTokenList.tokenCountCurrent();
        for (int i = 0; i < tokenCount; i++) {
            Object tokenOrEC = removedTokenList.tokenOrEmbeddingContainer(i);
            if (tokenOrEC.getClass() == EmbeddingContainer.class) {
                EmbeddingContainer<?> ec = (EmbeddingContainer<?>)tokenOrEC;
                ec.updateStatusImpl(); // Update status since markRemoved() will need it
                EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
                while (etl != null && etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING) {
                    TLLInfo info = info(etl.languagePath());
                    if (info != NO_INFO) {
                        // update-status called above
                        info.markRemoved(etl);
                    }
                    etl = etl.nextEmbeddedTokenList();
                }
            }
        }
    }
    
    private void markAddedEmbeddings(TokenList<?> tokenList, int index, int addedCount) {
        for (int i = 0; i < addedCount; i++) {
            // Ensure that the default embedding gets possibly created
            EmbeddedTokenList<?> etl = EmbeddingContainer.embeddedTokenList(tokenList, index + i, null);
            if (etl != null) {
                TLLInfo info = info(etl.languagePath());
                if (info != NO_INFO) {
                    // Mark that there was a new embedded token list added
                    // There should be no updateStatusImpl() necessary since the token lists are new
                    // and the parent embedding container was surely updated by the updating process.
                    info.markAdded(etl);
                }
            }
        }
    }

    private void processLevelInfos() {
        // Now relex the changes in affected token list lists
        // i.e. fix the tokens after the token lists removals/additions.
        // The higher-level updates
        if (levelInfos != null) {
            // The list can be extended by additional items dynamically during iteration
            for (int i = 0; i < levelInfos.size(); i++) {
                List<TLLInfo> infos = levelInfos.get(i);
                // The "infos" list should not be extended by additional items dynamically during iteration
                // However an extra items can be added at the deeper levels.
                for (int j = 0; j < infos.size(); j++) {
                    infos.get(j).update();
                }
            }
        }
        
        // Assert that update was called on all infos
        if (LOG.isLoggable(Level.FINER) && levelInfos != null) {
            for (List<TLLInfo> infos : levelInfos) {
                for (TLLInfo info : infos) {
                    if (!info.updateCalled) {
                        throw new IllegalStateException("Update not called on tokenListList\n" + // NOI18N
                                info.tokenListList);
                    }
                }
            }
        }
    }
    
    <T extends TokenId> TokenListChange<T> updateTokenListByModification(
    MutableTokenList<T> tokenList, Object zeroIndexRelexState) {
        TokenListChange<T> change = new TokenListChange<T>(tokenList);
//        if (tokenList instanceof EmbeddedTokenList) {
//            ((EmbeddedTokenList)tokenList).embeddingContainer().checkStatusUpdated();
//        }
        TokenListUpdater.update(tokenList, eventInfo.modificationOffset(),
                eventInfo.insertedLength(), eventInfo.removedLength(), change, zeroIndexRelexState);
        return change;
    }

    /**
     * Return tll info or NO_INFO if the token list list is not maintained
     * for the given language path.
     */
    private TLLInfo info(LanguagePath languagePath) {
        if (path2info == null) { // Init since it will contain NO_INFO
            path2info = new HashMap<LanguagePath,TLLInfo>(4, 0.5f);
        }
        TLLInfo info = path2info.get(languagePath);
        if (info == null) {
            TokenListList tll = eventInfo.tokenHierarchyOperation().existingTokenListList(languagePath);
            if (tll != null) {
                info = new TLLInfo(this, tll);
                int index = languagePath.size() - 2;
                if (levelInfos == null) {
                    levelInfos = new ArrayList<List<TLLInfo>>(index + 1);
                }
                while (levelInfos.size() <= index) {
                    levelInfos.add(new ArrayList<TLLInfo>(2));
                }
                levelInfos.get(index).add(info);
            } else { // No token list list for the given language path
                info = NO_INFO;
            }
            path2info.put(languagePath, info);
        }
        return info;
    }
    
    private <T extends TokenId> void rewrapECToken(EmbeddingContainer<T> ec, TokenListChange<?> change) {
        @SuppressWarnings("unchecked")
        TokenListChange<T> tChange = (TokenListChange<T>)change;
        ec.reinit(tChange.addedToken(0));
        ec.updateStatusImpl();
        tChange.tokenList().wrapToken(tChange.index(), ec);
    }

    /**
     * Special constant value to avoid double map search for token list lists updating.
     */
    static final TLLInfo NO_INFO = new TLLInfo(null, null);
        
    /**
     * Information about update in a single token list list.
     */
    static final class TLLInfo {
        
        final TokenHierarchyUpdate update;
        
        final TokenListList tokenListList;

        int index;

        int removeCount;

        List<TokenList<?>> added;
        
        TokenListChange<?> change;
        
        boolean updateCalled;
        
        public TLLInfo(TokenHierarchyUpdate update, TokenListList tokenListList) {
            this.update = update;
            this.tokenListList = tokenListList;
            this.index = -1;
            this.added = Collections.emptyList();
        }
        
        public TokenListList tokenListList() {
            return tokenListList;
        }
        
        /**
         * Mark the given token list as removed in this token list list.
         * All removed token lists should be marked by their increasing offset
         * so it should be necessary to search for the index just once.
         * <br/>
         * It's expected that updateStatusImpl() was already called
         * on the corresponding embedding container.
         */
        public void markRemoved(EmbeddedTokenList<?> removedTokenList) {
            boolean indexWasMinusOne; // Used for possible exception cause debugging
//            removedTokenList.embeddingContainer().checkStatusUpdated();
            if (index == -1) {
                checkUpdateNotCalledYet();
                indexWasMinusOne = true;
                index = tokenListList.findIndexDuringUpdate(removedTokenList,
                        update.eventInfo.modificationOffset(), update.eventInfo.removedLength());
                assert (index >= 0) : "index=" + index + " < 0"; // NOI18N
            } else { // Index already initialized
                indexWasMinusOne = false;
            }
            TokenList<?> markedForRemoveTokenList = tokenListList.getOrNull(index + removeCount);
            if (markedForRemoveTokenList != removedTokenList) {
                int realIndex = tokenListList.indexOf(removedTokenList);
                throw new IllegalStateException("Removing at index=" + index + // NOI18N
                        " but real index is " + realIndex + // NOI18N
                        " (indexWasMinusOne=" + indexWasMinusOne + ").\n" + // NOI18N
                        "Wishing to remove tokenList\n" + removedTokenList + // NOI18N
                        "\nbut marked-for-remove tokenList is \n" + markedForRemoveTokenList + // NOI18N
                        "\nfrom tokenListList\n" + tokenListList + // NOI18N
                        "\n\nModification description:\n" + update.eventInfo.modificationDescription(true) // NOI18N
                );
            }
            removeCount++;
        }

        /**
         * Mark the given token list to be added to this list of token lists.
         * At the end first the token lists marked for removal will be removed
         * and then the token lists marked for addition will be added.
         * <br/>
         * It's expected that updateStatusImpl() was already called
         * on the corresponding embedding container.
         */
        public void markAdded(EmbeddedTokenList<?> addedTokenList) {
//            addedTokenList.embeddingContainer().checkStatusUpdated();
            if (added.size() == 0) {
                checkUpdateNotCalledYet();
                if (index == -1) {
                    index = tokenListList.findIndex(addedTokenList.startOffset());
                    assert (index >= 0) : "index=" + index + " < 0"; // NOI18N
                }
                added = new ArrayList<TokenList<?>>(4);
            }
            added.add(addedTokenList);
        }
        
        /**
         * Mark that a parent's token list's bounds change need to be propagated
         * into the given (child) token list.
         * <br/>
         * It's expected that updateStatusImpl() was already called
         * on the corresponding embedding container.
         */
        public void markBoundsChange(EmbeddedTokenList<?> etl) {
            assert (index == -1) : "index=" + index + " != -1"; // Should be the first one
//            etl.embeddingContainer().checkStatusUpdated();
            checkUpdateNotCalledYet();
            index = tokenListList.findIndex(etl.startOffset());
        }
        
        public void update() {
            checkUpdateNotCalledYet();
            updateCalled = true;
            // Update this level (and language path).
            // All the removed and added sections resulting from parent change(s)
            // are already marked.
            if (index == -1)
                return; // Nothing to do

            if (removeCount == 0 && added.size() == 0) { // Bounds change only
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("TLLInfo.update(): BOUNDS-CHANGE: " + tokenListList.languagePath().mimePath() + // NOI18N
                            " index=" + index + // NOI18N
                            '\n'
                    );
                }

                EmbeddedTokenList<?> etl = tokenListList.get(index);
                etl.embeddingContainer().updateStatusImpl();
                Object matchState = LexerUtilsConstants.endState(etl);
                Object relexState = tokenListList.relexState(index);
                // update-status called above
                TokenListChange<?> chng = update.updateTokenListByModification(etl, relexState);
                relexState = LexerUtilsConstants.endState(etl, relexState);
                // Prevent bounds change in case the states at the end of the section would not match
                // which leads to relexing of the next section.
                if (chng.isBoundsChange() && LexerUtilsConstants.statesEqual(relexState, matchState)) {
                    TokenListChange<?> parentChange = (tokenListList.languagePath().size() == 2)
                            ? update.rootChange
                            : update.info(tokenListList.languagePath().parent()).change;
                    update.processBoundsChangeEmbeddings(chng, parentChange);
                } else { // Regular change
                    update.processNonBoundsChange(chng);
                }
                relexAfterLastModifiedSection(index + 1, relexState, matchState);

            } else { // Non-bounds change
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("TLLInfo.update(): REPLACE: " + tokenListList.languagePath().mimePath() + // NOI18N
                            " index=" + index + // NOI18N
                            ", removeCount=" + removeCount + // NOI18N
                            ", added.size()=" + added.size() + // NOI18N
                            '\n'
                    );
                }

                TokenList<?>[] removed = tokenListList.replace(index, removeCount, added);
                // Mark embeddings of removed token lists as removed
                if (tokenListList.hasChildren()) {
                    for (int i = 0; i < removed.length; i++) {
                        TokenList<?> removedTokenList = removed[i];
                        update.markRemovedEmbeddings(removedTokenList);
                    }
                }

                Object relexState; // State from which the relexing will start
                Object matchState = INVALID_STATE; // State that needs to be reached by relexing
                if (tokenListList.joinSections()) { // Need to find the right relexState
                    // Must update the token list by incremental algorithm
                    // Find non-empty token list and take last token's state
                    relexState = tokenListList.relexState(index);
                    for (int i = removed.length - 1; i >= 0 && matchState == INVALID_STATE; i--) {
                        matchState = LexerUtilsConstants.endState((EmbeddedTokenList<?>)removed[i]);
                    }
                    // Find the start state as the previous non-empty section's last token's state
                    // for case there would be no token lists added or all the added sections
                    // would be empty.
                    if (matchState == INVALID_STATE) // None or just empty sections were removed
                        matchState = relexState;

                } else { // Not joining the sections
                    relexState = null;
                }

                // Relex all the added token lists (just by asking for tokenCount - init() will be done)
                for (int i = 0; i < added.size(); i++) {
                    EmbeddedTokenList<?> tokenList = (EmbeddedTokenList<?>)added.get(i);
                    assert (!tokenList.isInited());
                    tokenList.init(relexState);
                    if (tokenList.embedding().joinSections()) {
                        tokenListList.setJoinSections(true);
                    }
                    relexState = LexerUtilsConstants.endState((EmbeddedTokenList<?>)tokenList, relexState);
                    if (tokenListList.hasChildren()) {
                        update.markAddedEmbeddings(tokenList, 0, tokenList.tokenCount());
                    }
                    // Added token lists should not require updateStatus()
                    update.eventInfo.setMaxAffectedEndOffset(tokenList.endOffset());
                }

                if (tokenListList.joinSections()) {
                    index += added.size();
                    relexAfterLastModifiedSection(index, relexState, matchState);
                }
            }
            
//            for (EmbeddedTokenList<?> etl : tokenListList) {
//                etl.embeddingContainer().updateStatusImpl();
//                if (etl.embeddingContainer().isRemoved())
//                    throw new IllegalStateException();
//            }
            // Set index to -1 to simplify correctness checking
            index = -1;
        }
        
        void checkUpdateNotCalledYet() {
            if (updateCalled) {
                throw new IllegalStateException("Update already called on \n" + tokenListList);
            }
        }
        
        private void relexAfterLastModifiedSection(int index, Object relexState, Object matchState) {
            // Must continue relexing existing section(s) (from a different start state)
            // until the relexing will stop before the last token of the given section.
            EmbeddedTokenList<?> etl;
            while (!LexerUtilsConstants.statesEqual(relexState, matchState)
                && (etl = tokenListList.getOrNull(index)) != null
            ) {
                etl.embeddingContainer().updateStatusImpl();
                if (etl.tokenCount() > 0) {
                    // Remember state after the last token of the given section
                    matchState = etl.state(etl.tokenCount() - 1);
                    // updateStatusImpl() just called
                    TokenListChange<?> chng = updateTokenListAtStart(etl, etl.startOffset(), relexState);
                    update.processNonBoundsChange(chng);
                    // Since the section is non-empty (checked above) there should be >0 tokens
                    relexState = etl.state(etl.tokenCount() - 1);
                }
                index++;
            }
        }
        
        private <T extends TokenId> TokenListChange<T> updateTokenListAtStart(
        EmbeddedTokenList<T> etl, int offset, Object zeroIndexRelexState) {
            TokenListChange<T> chng = new TokenListChange<T>(etl);
//            etl.embeddingContainer().checkStatusUpdated();
            TokenListUpdater.update(etl, offset, 0, 0, chng, zeroIndexRelexState);
            update.eventInfo.setMaxAffectedEndOffset(chng.addedEndOffset());
            return chng;
        }
        
    }
    
}
