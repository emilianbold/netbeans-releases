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
        rootChange = updateTokenListByModification(incTokenList);
        eventInfo.setTokenChangeInfo(rootChange.tokenChangeInfo());

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("<<<<<<<<<<<<<<<<<< LEXER CHANGE START ------------------\n"); // NOI18N
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
        //    in the UpdateInfo.
        // 4. For a removed token list it must check nested embedded token lists
        //    because some embedded tokens of the removed embedded token list migth contain
        //    another embedding that might also be maintained as token list list
        //    and need to be updated. The parent token list lists
        //    are always maintained too which simplifies the updating algorithm
        //    and speeds it up because the token list list marks whether it has any children
        //    or not and so the deep traversing only occurs if there are any children present.
        //    Unlike additions marking the marking of removed tokens lists can nest immediately upon
        //    discovery of the upper level removed token list.
        // 5. For additions of embedded token lists the situation is different.
        //    They must be processed (updated) strictly from top-level down because
        //    their updating may produce tokens with embeddings that may be part
        //    of an existing joined token list.
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
            info.markAdded(addedTokenList);
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
                hasChildren = info.tokenListList().hasChildren();
            } else { // root-level
                info = NO_INFO;
                hasChildren = (eventInfo.tokenHierarchyOperation().maxTokenListListPathSize() > 0);
            }
            EmbeddingContainer<?> ec = (EmbeddingContainer<?>)tokenOrEC;
            rewrapECToken(ec, change);
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
                            childInfo.markBoundsChange(etl);
                        } else { // No child but want to update nested possible bounds changes
                            parentChange = change;
                            change = updateTokenListByModification(etl);
                            if (change.isBoundsChange()) {
                                processBoundsChangeEmbeddings(change, parentChange);
                            } else {
                                eventInfo.setMinAffectedStartOffset(change.offset());
                                eventInfo.setMaxAffectedEndOffset(change.addedEndOffset());
                            }
                        }
                        prevEtl = etl;
                        etl = etl.nextEmbeddedTokenList();

                    } else { // Mod in skip lengths => Remove the etl from chain
                        if (childInfo != NO_INFO) {
                            childInfo.markRemoved(etl);
                        }
                        etl = etl.nextEmbeddedTokenList();
                        if (prevEtl != null) {
                            prevEtl.setNextEmbeddedTokenList(etl);
                        }
                    }
                } while (etl != null && etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING);
            }
        } 
    }
    
    void processNonBoundsChange(TokenListChange<?> change) {
        TLLInfo info;
        boolean hasChildren;
        if (change.languagePath().size() > 1) {
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
                EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
                while (etl != null && etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING) {
                    TLLInfo info = info(etl.languagePath());
                    if (info != NO_INFO) {
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
    }
    
    private <T extends TokenId> TokenListChange<T> updateTokenListByModification(MutableTokenList<T> tokenList) {
        TokenListChange<T> change = new TokenListChange<T>(tokenList);
        TokenListUpdater.update(tokenList, eventInfo.modificationOffset(),
                eventInfo.insertedLength(), eventInfo.removedLength(), change);
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
            TokenListList tll = eventInfo.tokenHierarchyOperation().tokenListListOrNull(languagePath);
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
        ec.setToken(tChange.addedToken(0));
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
        
        Boolean lexedBeyondModPoint;
        
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
         */
        public void markRemoved(TokenList<?> removedTokenList) {
            if (doMarking(removedTokenList)) {
                if (index == -1) {
                    index = tokenListList.findIndex(removedTokenList.startOffset());
                    assert (index >= 0) : "index=" + index + " < 0"; // NOI18N
                }
                TokenList<?> expected = tokenListList.getOrNull(index + removeCount);
                if (expected != removedTokenList) {
                    throw new IllegalStateException("Expected to remove " + expected + // NOI18N
                            "\nbut removing " + removedTokenList + // NOI18N
                            "\nfrom tokenListList=" + tokenListList);
                }
                removeCount++;
            }
        }

        /**
         * Mark the given token list to be added to this list of token lists.
         * At the end first the token lists marked for removal will be removed
         * and then the token lists marked for addition will be added.
         */
        public void markAdded(TokenList<?> addedTokenList) {
            if (doMarking(addedTokenList)) {
                if (added.size() == 0) {
                    if (index == -1) {
                        index = tokenListList.findIndex(addedTokenList.startOffset());
                        assert (index >= 0) : "index=" + index + " < 0"; // NOI18N
                    }
                    added = new ArrayList<TokenList<?>>(4);
                }
                added.add(addedTokenList);
            }
        }
        
        public void markBoundsChange(TokenList<?> tokenList) {
            if (doMarking(tokenList)) {
                assert (index == -1) : "index=" + index + " != -1"; // Should be the first one
                index = tokenListList.findIndex(tokenList.startOffset());
                // No additional removals or addings
            }
        }
        
        public void update() {
            // Update this level (and language path).
            // All the removed and added sections resulting from parent change(s)
            // are already marked.
            if (index == -1)
                return; // Nothing to do

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("TokenListList " + tokenListList.languagePath() +
                        " replace: index=" + index +
                        ", removeCount=" + removeCount +
                        ", added.size()=" + added.size()
                );
            }

            if (removeCount == 0 && added.size() == 0) { // Bounds change only
                EmbeddedTokenList<?> etl = (EmbeddedTokenList<?>)tokenListList.get(index);
                // Should certainly be non-empty
                assert (etl.tokenCountCurrent() > 0);
                Object endState = LexerUtilsConstants.endState(etl);
                etl.embeddingContainer().updateStatusImpl();
                Object matchState = LexerUtilsConstants.endState(etl);
                TokenListChange<?> chng = update.updateTokenListByModification(etl);
                if (chng.isBoundsChange()) {
                    TokenListChange<?> parentChange = (tokenListList.languagePath().size() == 2)
                            ? update.rootChange
                            : update.info(tokenListList.languagePath().parent()).change;
                    update.processBoundsChangeEmbeddings(chng, parentChange);
                } else { // Regular change
                    update.processNonBoundsChange(chng);
                }
                Object relexState = LexerUtilsConstants.endState(etl);
                relexAfterLastModifiedSection(index + 1, relexState, matchState);

            } else { // Non-bounds change
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
                if (tokenListList.isJoinSections()) { // Need to find the right relexState
                    // Must update the token list by incremental algorithm
                    // Find non-empty token list and take last token's state
                    relexState = INVALID_STATE;
                    for (int i = removed.length - 1; i >= 0 && matchState == INVALID_STATE; i--) {
                        matchState = LexerUtilsConstants.endState(removed[i]);
                    }
                    // Find the start state as the previous non-empty section's last token's state
                    // for case there would be no token lists added or all the added sections
                    // would be empty.
                    for (int i = index - 1; i >= 0 && relexState == INVALID_STATE; i--) {
                        relexState = LexerUtilsConstants.endState(tokenListList.get(i));
                    }
                    if (relexState == INVALID_STATE)
                        relexState = null;
                    if (matchState == INVALID_STATE) // None or just empty sections were removed
                        matchState = relexState;

                } else { // Not joining the sections
                    relexState = null;
                }

                // Relex all the added token lists (just by asking for tokenCount - init() will be done)
                for (int i = 0; i < added.size(); i++) {
                    TokenList<?> tokenList = added.get(i);
                    relexState = LexerUtilsConstants.endState(tokenList, relexState);
                    if (tokenListList.hasChildren()) {
                        update.markAddedEmbeddings(tokenList, 0, tokenList.tokenCount());
                    }
                    update.eventInfo.setMaxAffectedEndOffset(tokenList.endOffset());
                }

                if (tokenListList.isJoinSections()) {
                    index += added.size();
                    relexAfterLastModifiedSection(index, relexState, matchState);
                }
            }
        }
        
        private void relexAfterLastModifiedSection(int index, Object relexState, Object matchState) {
            // Must continue relexing existing section(s) (from a different start state)
            // until the relexing will stop before the last token of the given section.
            EmbeddedTokenList<?> etl;
            while (!LexerUtilsConstants.statesEqual(relexState, matchState) 
                && (etl = (EmbeddedTokenList<?>)tokenListList.getOrNull(index)) != null
            ) {
                if (etl.tokenCount() > 0) {
                    etl.embeddingContainer().updateStatusImpl();
                    // Remember state after the last token of the given section
                    matchState = etl.state(etl.tokenCount() - 1);
                    TokenListChange<?> chng = updateTokenListAtStart(etl, etl.startOffset());
                    update.processNonBoundsChange(chng);
                    // Since the section is non-empty (checked above) there should be >0 tokens
                    relexState = etl.state(etl.tokenCount() - 1);
                }
                index++;
            }
        }
        
        private boolean doMarking(TokenList<?> tokenList) {
            if (lexedBeyondModPoint == null) { // Check whether lexed till this point
                if (tokenListList.isComplete()) {
                    lexedBeyondModPoint = Boolean.TRUE;
                } else { // Not complete yet
//                    if (tokenListList.isMandatory()) { // Should never happen
//                        // Such list should only be non-mandatory
//                        throw new IllegalStateException("Mandatory list " + // NOI18N
//                                tokenListList.languagePath() + " is not fully lexed"); // NOI18N
//                    }
                    TokenList<?> lastTokenList;
                    lexedBeyondModPoint = tokenListList.size() > 0 
                            && (lastTokenList = tokenListList.getExistingOrNull(tokenListList.size() - 1)) != null
                            && (lastTokenList.endOffset() >= tokenList.endOffset());
                }
            }
            return lexedBeyondModPoint.booleanValue();
        }
        
        private <T extends TokenId> TokenListChange<T> updateTokenListAtStart(MutableTokenList<T> tokenList, int offset) {
            TokenListChange<T> chng = new TokenListChange<T>(tokenList);
            TokenListUpdater.update(tokenList, offset, 0, 0, chng);
            update.eventInfo.setMaxAffectedEndOffset(chng.addedEndOffset());
            return chng;
        }
        
    }
    
}
