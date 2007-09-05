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
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.lexer.inc.IncTokenList;
import org.netbeans.lib.lexer.inc.MutableTokenList;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.lib.lexer.inc.TokenListChange;
import org.netbeans.lib.lexer.inc.TokenListUpdater;

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

    public TokenHierarchyUpdate(TokenHierarchyEventInfo eventInfo) {
        this.eventInfo = eventInfo;
    }

    public void update(IncTokenList<?> incTokenList) {
        incTokenList.incrementModCount();
        // Update top-level token list first
        TokenListChange<?> change = updateTokenList(incTokenList);
        eventInfo.setTokenChangeInfo(change.tokenChangeInfo());

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("<<<<<<<<<<<<<<<<<< LEXER CHANGE START ------------------\n"); // NOI18N
            LOG.fine("ROOT CHANGE: " + change.toString(0) + "\n"); // NOI18N
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
        processChange(change);

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

    /**
     * Process performed token list change checking whether it was bounds change or not.
     */
    void processChange(TokenListChange<?> change) {
        if (change.isBoundsChange()) {
            processBoundsChangeEmbeddings(change);
        } else {
            // Mark changed area based on start of first mod.token and end of last mod.token
            // of the top-level change
            eventInfo.setMinAffectedStartOffset(change.offset());
            eventInfo.setMaxAffectedEndOffset(change.addedEndOffset());
            processNonBoundsChange(change);
        }
    }
    
    /**
     * Add a nested changes to the original change for the given embedding token list
     * recursively traversing next linked embedded token lists.
     * 
     * @param change non-null change. It must be bounds change.
     * @param etl non-null embedding token list
     */
    void processBoundsChangeEmbeddings(TokenListChange<?> change) {
        Object tokenOrEC = change.tokenChangeInfo().removedTokenList().tokenOrEmbeddingContainer(0);
        if (tokenOrEC.getClass() == EmbeddingContainer.class) {
            EmbeddingContainer<?> ec = (EmbeddingContainer<?>)tokenOrEC;
            rewrapECToken(ec, change);
            EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
            if (etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING) {
                // Check the text length beyond modification => end skip length must not be affected
                int modRelOffset = eventInfo.modificationOffset() - change.offset();
                int beyondModLength = change.addedEndOffset() - (eventInfo.modificationOffset() + eventInfo.diffLengthOrZero());
                EmbeddedTokenList<?> prevEtl = null;

                do {
                    // Check whether the change was not in the start or end skip lengths
                    if (modRelOffset >= etl.embedding().startSkipLength()
                            && beyondModLength >= etl.embedding().endSkipLength()
                    ) {
                        // Check whether the change is not in the mandatory token list list that joins sections
                        @SuppressWarnings("unchecked")
                        EmbeddedTokenList<TokenId> etlT = (EmbeddedTokenList<TokenId>)etl;
                        new TokenListChange<TokenId>(etlT);
                        // If there are joined sections then check that the update
                        // did not span till end of a section and if so that the ending state
                        // is the same as before. Otherwise next sections may need to be relexed.
                        TLLInfo info = info(etl.languagePath());
                        boolean joinSections = (info != NO_INFO && info.tokenListList().isJoinSections());
                        Object lastState = joinSections ? etl.state(etlT.tokenCountCurrent() - 1) : null;

                        TokenListChange<?> nestedChange = updateTokenList(etl);
                        // Check whether the change affects next sections
                        if (joinSections && !LexerUtilsConstants.statesEqual(lastState, etl.state(etl.tokenCountCurrent() - 1))) {
                            info.markEndChange(etl);
                            if (LOG.isLoggable(Level.FINE)) {
                                int updateLevel = etl.languagePath().size();
                                LOG.fine("End join section change: " + nestedChange.toString(
                                        updateLevel << 2));
                            }
                        }
                        change.tokenChangeInfo().addEmbeddedChange(nestedChange.tokenChangeInfo());
                        if (LOG.isLoggable(Level.FINE)) {
                            int updateLevel = etl.languagePath().size();
                            StringBuilder sb = new StringBuilder();
                            ArrayUtilities.appendSpaces(sb, updateLevel << 2);
                            sb.append("NESTED CHANGE at level="); // NOI18N
                            sb.append(updateLevel);
                            sb.append(": ");
                            sb.append(nestedChange.toString(updateLevel << 2));
                            sb.append('\n');
                            LOG.fine(sb.toString());
                        }
                        processChange(nestedChange);
                        prevEtl = etl;
                        etl = etl.nextEmbeddedTokenList();

                    } else { // Mod in skip lengths => Remove the etl from chain
                        etl = etl.nextEmbeddedTokenList();
                        if (prevEtl != null) {
                            prevEtl.setNextEmbeddedTokenList(etl);
                        }
                    }
                } while (etl != null && etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING);
            } // etl == NO_DEFAULT_EMBEDDING
        } 
    }
    
    void processNonBoundsChange(TokenListChange<?> change) {
        // First mark the removed embeddings
        TokenList<?> removedTokens = change.tokenChangeInfo().removedTokenList();
        if (removedTokens != null) {
            markRemovedEmbeddings(removedTokens);
        }

        // For top-level change the added tokens need to be checked for embeddings
        // in case at least one token list list is maintained.
        // Embedded token lists of the added tokens should be marked as added
        // in a corresponding update info.
        // For non-top-level change a similar process has to be done
        // if there is a token list list at the given level that has some children.
        //
        // All the marked changes will be processed later altogether
        // in top-down manner. There can be multiple added tokens with possible embeddings
        // with the same language paths that need to be joined so first all must
        // be found and then lexed to properly make the sections joining.
        TokenList<?> currentTokenList = change.tokenChangeInfo().currentTokenList();
        LanguagePath languagePath = currentTokenList.languagePath();
        boolean topLevelChange = (languagePath.size() == 1);
        TLLInfo info;
        if ((topLevelChange && eventInfo.tokenHierarchyOperation().maxTokenListListPathSize() > 0)
            || (!topLevelChange && (info = info(languagePath)) != NO_INFO)
        ) {
            int addedTokenCount = change.addedTokensOrBranchesCount();
            for (int i = change.index(); i < addedTokenCount; i++) {
                Object tokenOrEC = currentTokenList.tokenOrEmbeddingContainer(i);
                if (tokenOrEC.getClass() == EmbeddingContainer.class) {
                    EmbeddingContainer<?> ec = (EmbeddingContainer<?>)tokenOrEC;
                    EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
                    if (etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING) {
                        do {
                            info = info(etl.languagePath());
                            if (info != NO_INFO) {
                                // Mark that there was a new embedded token list added
                                info.markAdded(etl);
                            }
                            etl = etl.nextEmbeddedTokenList();
                        } while (etl != null && etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING);
                    }
                }
            }
        }
    }
    
    /**
     * Collect removed embeddings for the given token list recursively
     * and nest deep enough for all maintained children
     * token list lists.
     */
    private void markRemovedEmbeddings(TokenList<?> tokenList) {
        int tokenCount = tokenList.tokenCountCurrent();
        for (int i = 0; i < tokenCount; i++) {
            Object tokenOrEC = tokenList.tokenOrEmbeddingContainer(i);
            if (tokenOrEC.getClass() == EmbeddingContainer.class) {
                EmbeddingContainer<?> ec = (EmbeddingContainer<?>)tokenOrEC;
                EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
                while (etl != null && etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING) {
                    TLLInfo info = info(etl.languagePath());
                    if (info != NO_INFO) {
                        info.markRemoved(etl);
                        if (info.tokenListList().hasChildren()) {
                            // If the token list list has children then nest - should be safe
                            // since the nested changes should not interfere
                            markRemovedEmbeddings(etl);
                        }
                    }
                    etl = etl.nextEmbeddedTokenList();
                }
            }
        }
    }
    
    private <T extends TokenId> TokenListChange<T> updateTokenList(MutableTokenList<T> tokenList) {
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
                int index = languagePath.size() - 1;
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
        
        boolean prevJoinSectionRelexedTillEnd;
        
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
                    assert (index >= 0);
                }
                assert (tokenListList.getExistingOrNull(index + removeCount) == removedTokenList);
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
                        assert (index >= 0);
                    }
                    added = new ArrayList<TokenList<?>>(4);
                }
                added.add(addedTokenList);
            }
        }
        
        public List<TokenList<?>> added() {
            return added;
        }
        
        public void markEndChange(TokenList<?> tokenList) {
            // Checking of marking not necessary since doing for isJoinSections() only
            if (index == -1) {
                // Take index + 1 since relexing the next section
                index = tokenListList.findIndex(tokenList.startOffset()) + 1;
            }
            prevJoinSectionRelexedTillEnd = true;
        }
        
        public void update() {
            // First scan sections added to parent token list list
            LanguagePath languagePath = tokenListList.languagePath();
            TLLInfo parentInfo = update.info(languagePath.parent());
            for (TokenList<?> tokenList : parentInfo.added()) {
                int tokenCount = tokenList.tokenCount(); // Force full lexing
                Language<?> embeddedLanguage = languagePath.innerLanguage();
                for (int i = 0; i < tokenCount; i++) {
                    EmbeddedTokenList<?> etl = EmbeddingContainer.embeddedTokenList(tokenList, i, embeddedLanguage);
                    if (etl != null && etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING) {
                        markAdded(etl);
                    }
                }
            }

            if (index == -1) // Nothing added or removed (possible for bounds only change processing)
                return;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("TokenListList " + tokenListList.languagePath() +
                        " replace: index=" + index +
                        ", removeCount=" + removeCount +
                        ", added.size()=" + added.size()
                );
            }

            TokenList<?>[] removed = tokenListList.replace(index, removeCount, added);
            if (tokenListList.isJoinSections()) {
                // Must update the token list by incremental algorithm
                Object matchState = null;
                // Find non-empty token list and take last token's state
                if (removed.length > 0) {
                    for (int i = removed.length - 1; i >= 0; i--) {
                        TokenList<?> tokenList = removed[i];
                        if (tokenList.tokenCount() > 0) {
                            matchState = tokenList.state(tokenList.tokenCount() - 1);
                            break;
                        }
                    }
                }
                // Find the start state as the previous non-empty section's last token's state
                // for case there would be no token lists added or all the added sections
                // would be empty.
                Object state = null;
                for (int i = index - 1; i >= 0; i--) {
                    TokenList<?> tokenList = tokenListList.get(i);
                    if (tokenList.tokenCount() > 0) {
                        state = tokenList.state(tokenList.tokenCount() - 1);
                        break;
                    }
                }
                // Relex all the added token lists (just by asking for tokenCount - init() will be done)
                for (int i = 0; i < added.size(); i++) {
                    TokenList<?> tokenList = added.get(i);
                    if (tokenList.tokenCount() > 0) { // Take state at the end of lexing
                        state = tokenList.state(tokenList.tokenCount() - 1);
                    }
                }
                if (!LexerUtilsConstants.statesEqual(state, matchState) || prevJoinSectionRelexedTillEnd) {
                    index += added.size();
                    // Must continue relexing existing section(s) (from a different start state)
                    // until the relexing will stop before the last token of the given section.
                    while (true) {
                        EmbeddedTokenList<?> etl = (EmbeddedTokenList<?>)tokenListList.getOrNull(index);
                        if (etl == null)
                            break;
                        if (etl.tokenCount() > 0) {
                            etl.embeddingContainer().updateStatusImpl();
                            // Remember state after the last token of the given section
                            matchState = etl.state(etl.tokenCount() - 1);
                            TokenListChange<?> change = updateTokenListAtStart(etl, etl.startOffset());
                            update.processChange(change);
                            if (change.addedEndOffset() == etl.endOffset()) { // Modified till the end
                                // Since the section is non-empty (checked above) there should be >0 tokens
                                // If the states don't match after the last token then continue with next section
                                if (LexerUtilsConstants.statesEqual(etl.state(etl.tokenCount() - 1), matchState))
                                    break;
                            } else {
                                break;
                            }
                        }
                        index++;
                    }
                }
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
            TokenListChange<T> change = new TokenListChange<T>(tokenList);
            TokenListUpdater.update(tokenList, offset, 0, 0, change);
            return change;
        }
        
    }
    
}
