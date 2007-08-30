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

import java.io.Reader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.lib.lexer.batch.CopyTextTokenList;
import org.netbeans.lib.lexer.batch.TextTokenList;
import org.netbeans.lib.lexer.inc.IncTokenList;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.lib.lexer.inc.TokenListUpdater;
import org.netbeans.spi.lexer.MutableTextInput;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.lexer.inc.OriginalText;
import org.netbeans.lib.lexer.inc.SnapshotTokenList;
import org.netbeans.lib.lexer.inc.TokenChangeInfo;
import org.netbeans.lib.lexer.inc.TokenListChange;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Token hierarchy operation services tasks of its associated token hierarchy.
 * <br/>
 * There is one-to-one relationship between token hierarchy and its operation.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenHierarchyOperation<I, T extends TokenId> { // "I" stands for input
    
    // -J-Dorg.netbeans.lib.lexer.TokenHierarchyOperation.level=FINE
    private static final Logger LOG = Logger.getLogger(TokenHierarchyOperation.class.getName());

    /**
     * The token hierarchy delegating to this operation.
     * <br>
     * There is one-to-one relationship between token hierarchy and its operation.
     */
    private TokenHierarchy<I> tokenHierarchy;
    
    /**
     * Mutable text input for mutable token hierarchy or null otherwise.
     */
    private MutableTextInput<I> mutableTextInput;
    
    private TokenList<T> tokenList;
    
    /**
     * The hierarchy can be made inactive to release the tokens
     * and the memory that they consume temporarily.
     * <br>
     * By default the hierarchy is active so the tokens
     * will be created lazily for it.
     */
    private boolean active = true;

    /**
     * Primary token hierarchy for snapshot.
     */
    private TokenHierarchyOperation<I,T> liveTokenHierarchyOperation;
    
    /**
     * References to active snapshots.
     */
    private List<SnapshotRef> snapshotRefs;

    /**
     * Listener list solely for token change listeners.
     */
    private EventListenerList listenerList;
    
    private boolean snapshotReleased;
    
    private Set<LanguagePath> languagePaths;
    
    private Set<Language<? extends TokenId>> exploredLanguages;

    /**
     * Mapping of language path to token list lists.
     * <br/>
     * If a token list list is contained then all its parents
     * with the shorter language path are also mandatorily maintained.
     */
    private Map<LanguagePath,TokenListList> path2tokenListList;
    
    private int maxTokenListListPathSize;
    

    /**
     * Constructor for reader as input.
     */
    public TokenHierarchyOperation(Reader inputReader,
    Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {
        this.tokenList = new CopyTextTokenList<T>(this, inputReader,
                language, skipTokenIds, inputAttributes);
        init();
    }

    /**
     * Constructor for character sequence as input.
     */
    public TokenHierarchyOperation(CharSequence inputText, boolean copyInputText,
    Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {
        this.tokenList = copyInputText
                ? new CopyTextTokenList<T>(this, inputText,
                        language, skipTokenIds, inputAttributes)
                : new TextTokenList<T>(this, inputText,
                        language, skipTokenIds, inputAttributes);
        init();
    }

    /**
     * Constructor for mutable input.
     */
    public TokenHierarchyOperation(MutableTextInput<I> mutableTextInput,
    Language<T> language) {
        this.mutableTextInput = mutableTextInput;
        this.tokenList = new IncTokenList<T>(this, mutableTextInput);
        init();
    }

    public TokenHierarchyOperation(TokenHierarchyOperation<I,T> liveTokenHierarchy) {
        this.liveTokenHierarchyOperation = liveTokenHierarchy;
        this.tokenList = new SnapshotTokenList<T>(this);
        init();
    }

    private void init() {
        assert (tokenHierarchy == null);
        tokenHierarchy = LexerApiPackageAccessor.get().createTokenHierarchy(this);
        // Create listener list even for non-mutable hierarchies as there may be
        // custom embeddings created that need to be notified
        listenerList = new EventListenerList();
        if (isMutable()) {
            snapshotRefs = new ArrayList<SnapshotRef>(1);
        }
    }
    
    public TokenHierarchy<I> tokenHierarchy() {
        return tokenHierarchy;
    }
    
    public TokenList<T> tokenList() {
        return tokenList;
    }

    public TokenList<T> checkedTokenList() {
        checkSnapshotNotReleased();
        return tokenList();
    }
    
    public int modCount() {
        return tokenList.modCount();
    }

    public boolean isMutable() {
        return (mutableTextInput != null);
    }

    public MutableTextInput mutableTextInput() {
        return mutableTextInput;
    }
    
    public I mutableInputSource() {
        return isMutable()
            ? LexerSpiPackageAccessor.get().inputSource(mutableTextInput)
            : null;
    }
    
    public void setActive(boolean active) {
        assert (isMutable());
        if (this.active != active) {
            this.active = active;
        }
    }
    
    public boolean isActive() {
        return active;
    }
    
    /**
     * Get the token list list for the given language path.
     * <br/>
     * If the list needs to be created or it was non-mandatory.
     */
    public synchronized TokenListList tokenListList(LanguagePath languagePath) {
        TokenListList tll = path2tokenListList().get(languagePath);
        if (tll == null) {
            tll = new TokenListList(this, languagePath);
            path2tokenListList.put(languagePath, tll);
            maxTokenListListPathSize = Math.max(languagePath.size(), maxTokenListListPathSize);
            // Also create parent token list lists if they don't exist yet
            if (languagePath.size() >= 3) { // Top-level token list list not maintained
                tokenListList(languagePath.parent()).increaseChildrenCount();
            }
        }
        return tll;
    }
    
    private Map<LanguagePath,TokenListList> path2tokenListList() {
        if (path2tokenListList == null) {
            path2tokenListList = new HashMap<LanguagePath,TokenListList>();
        }
        return path2tokenListList;
    }
    
    public synchronized TokenListList tokenListListOrNull(LanguagePath languagePath) {
        return (path2tokenListList != null) ? path2tokenListList.get(languagePath) : null;
    }
    
    public void rebuild() {
        if (isSnapshot()) // Do nothing for snapshot
            return;
        if (active) {
            IncTokenList<T> incTokenList = (IncTokenList<T>)tokenList;
            incTokenList.incrementModCount();
            TokenListChange<T> change = new TokenListChange<T>(incTokenList);
            CharSequence text = LexerSpiPackageAccessor.get().text(mutableTextInput);
            int endOffset = incTokenList.existingTokensEndOffset();
            TokenHierarchyEventInfo eventInfo = new TokenHierarchyEventInfo(
                this, TokenHierarchyEventType.REBUILD, 0, 0, text, 0);
            change.setIndex(0);
            change.setOffset(0);
            change.setAddedEndOffset(0); // Tokens will be recreated lazily

            incTokenList.replaceTokens(change, incTokenList.tokenCountCurrent(), 0);
            incTokenList.restartLexing(); // Will relex tokens lazily
            incTokenList.incrementModCount();
            
            synchronized (snapshotRefs) {
                for (int i = snapshotRefs.size() - 1; i >= 0; i--) {
                    TokenHierarchyOperation<I,T> op = snapshotRefs.get(i).get();
                    if (op != null) {
                        ((SnapshotTokenList<T>)op.tokenList()).update(eventInfo, change);
                    }
                }
            }
            eventInfo.setTokenChangeInfo(change.tokenChangeInfo());
            eventInfo.setAffectedStartOffset(0);
            eventInfo.setAffectedEndOffset(text.length());
            
            path2tokenListList = null; // Drop all token list lists
            fireTokenHierarchyChanged(
                LexerApiPackageAccessor.get().createTokenChangeEvent(eventInfo));
        } // not active - no changes fired
    }
    
    public void fireTokenHierarchyChanged(TokenHierarchyEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        int listenersLength = listeners.length;
        for (int i = 1; i < listenersLength; i += 2) {
            ((TokenHierarchyListener)listeners[i]).tokenHierarchyChanged(evt);
        }
    }
    
    public void addTokenHierarchyListener(TokenHierarchyListener listener) {
        listenerList.add(TokenHierarchyListener.class, listener);
    }
    
    public void removeTokenHierarchyListener(TokenHierarchyListener listener) {
        listenerList.remove(TokenHierarchyListener.class, listener);
    }

    public void textModified(int offset, int removedLength, CharSequence removedText, int insertedLength) {
        TokenHierarchyEventInfo eventInfo = new TokenHierarchyEventInfo(
                this, TokenHierarchyEventType.MODIFICATION,
                offset, removedLength, removedText, insertedLength);
        if (active) {
            // First a top-level token list will be updated then the embedded ones.
            IncTokenList<T> incTokenList = (IncTokenList<T>)tokenList;
            incTokenList.incrementModCount();
            TokenListChange<T> tlChange = new TokenListChange<T>(incTokenList);

            if (LOG.isLoggable(Level.FINEST)) {
                // Display current state of the hierarchy by faking its text
                // through original text
                CharSequence text = incTokenList.text();
                assert (text != null);
                incTokenList.setText(new OriginalText(text, offset, removedText, insertedLength));
                // Dump all contents
                LOG.log(Level.FINEST, toString());
                // Return the original text
                incTokenList.setText(text);
            }

            // Update top-level token list first
            TokenListUpdater.update(incTokenList, eventInfo.modificationOffset(),
                    eventInfo.insertedLength(), eventInfo.removedLength(), tlChange);
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("<<<<<<<<<<<<<<<<<< LEXER CHANGE START ------------------\n"); // NOI18N
                LOG.fine("ROOT CHANGE: " + tlChange.toString(0) + "\n"); // NOI18N
            }
            // If there is an active lexer input operation (for not-yet finished
            // top-level token list lexing) refresh it because it would be damaged
            // by the performed token list update
            if (!incTokenList.isFullyLexed())
                incTokenList.refreshLexerInputOperation();
            
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
            
            // Update snapshots - needs to be improved
//            synchronized (snapshotRefs) {
//                for (int i = snapshotRefs.size() - 1; i >= 0; i--) {
//                    TokenHierarchyOperation<I,T> op = snapshotRefs.get(i).get();
//                    
//                    if (op != null) {
//                        ((SnapshotTokenList<T>)op.tokenList()).update(eventInfo, change);
//                    }
//                }
//            }
            
            int diffLengthOrZero = Math.max(0, eventInfo.insertedLength() - eventInfo.removedLength());
            TokenChangeInfo<?> change = tlChange.tokenChangeInfo();
            eventInfo.setTokenChangeInfo(change);
            Map<LanguagePath,UpdateInfo> path2updateInfo =
                     new HashMap<LanguagePath,UpdateInfo>();

            // Affected boundaries only modification bounds => nested changes will possibly extend them
            if (change.isBoundsChange()) {
                eventInfo.setAffectedStartOffset(eventInfo.modificationOffset());
                eventInfo.setAffectedEndOffset(eventInfo.modificationOffset() + diffLengthOrZero);
                processBoundsChangesNested(eventInfo, tlChange, path2updateInfo, diffLengthOrZero, 0);
                        
            } else {
                // Mark changed area based on start of first mod.token and end of last mod.token
                // of the top-level change
                eventInfo.setAffectedStartOffset(tlChange.offset());
                eventInfo.setAffectedEndOffset(tlChange.addedEndOffset());
                processTokenChange(path2updateInfo, change);
            }
            
            // Now relex the changes in affected token list lists
            // i.e. fix the tokens after the token lists removals/additions.
            // Pick the valid update infos
            if (path2updateInfo.size() > 0) {
                List<UpdateInfo> updateInfoList = new ArrayList<UpdateInfo>(path2updateInfo.size());
                for (UpdateInfo updateInfo : path2updateInfo.values()) {
                    if (updateInfo != NO_UPDATE_INFO) {
                        updateInfoList.add(updateInfo);
                    }
                }
                if (updateInfoList.size() > 0) {
                    Collections.sort(updateInfoList, updateInfoComparator);
                    for (UpdateInfo updateInfo : updateInfoList) {
                        updateInfo.update(eventInfo);
                    }
                }
            }
            

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("EVENT: " + eventInfo + "\n"); // NOI18N
                String extraMsg = "";
                if (LOG.isLoggable(Level.FINER)) {
                    // Check consistency of the whole token hierarchy
                    String error = checkConsistency();
                    if (error != null)
                        LOG.finer("!!!CONSISTENCY-ERROR!!!: " + error + "\n");
                    else
                        extraMsg = "(TokenHierarchy Check OK) ";
                }
                LOG.fine(">>>>>>>>>>>>>>>>>> LEXER CHANGE END " + extraMsg + "------------------\n"); // NOI18N
            }

            // Fix token list list cache
            fireTokenHierarchyChanged(
                LexerApiPackageAccessor.get().createTokenChangeEvent(eventInfo));
        } // not active - no changes fired
    }
    
    /**
     * Add a nested changes to the original change for the given embedding token list
     * recursively traversing next linked embedded token lists.
     * 
     * @param change non-null change. It must be bounds change.
     * @param etl non-null embedding token list
     */
    private <TX extends TokenId> void processBoundsChangesNested(
        TokenHierarchyEventInfo eventInfo, TokenListChange<TX> tlChange,
        Map<LanguagePath,UpdateInfo> path2updateInfo,
        int diffLengthOrZero, int updateLevel
    ) {
        Object tokenOrEC = tlChange.tokenChangeInfo().removedTokenList().tokenOrEmbeddingContainer(0);
        if (tokenOrEC.getClass() == EmbeddingContainer.class) {
            @SuppressWarnings("unchecked")
            EmbeddingContainer<TX> ec = (EmbeddingContainer<TX>)tokenOrEC;
            ec.setToken(tlChange.addedToken(0));
            ec.updateStatus();
            EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
            if (etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING) {
                int tokenOffset = tlChange.offset();
                // Check the text length beyond modification => end skip length must not be affected
                int modRelOffset = eventInfo.modificationOffset() - tlChange.offset();
                int beyondModLength = tlChange.addedEndOffset() - (eventInfo.modificationOffset() + diffLengthOrZero);
                EmbeddedTokenList<?> prevEtl = null;

                do {
                    // Check whether the change was not in the start or end skip lengths
                    if (modRelOffset >= etl.embedding().startSkipLength()
                            && beyondModLength >= etl.embedding().endSkipLength()
                    ) {
                        // Check whether the change is not in the mandatory token list list that joins sections
                        @SuppressWarnings("unchecked")
                        EmbeddedTokenList<TokenId> etlT = (EmbeddedTokenList<TokenId>)etl;
                        TokenListChange<TokenId> nestedTLChange = new TokenListChange<TokenId>(etlT);
                        TokenListUpdater.update(etlT, eventInfo.modificationOffset(),
                                eventInfo.insertedLength(), eventInfo.removedLength(), nestedTLChange);
                        if (LOG.isLoggable(Level.FINE)) {
                            StringBuilder sb = new StringBuilder();
                            ArrayUtilities.appendSpaces(sb, updateLevel << 2);
                            sb.append("NESTED CHANGE at level="); // NOI18N
                            sb.append(updateLevel);
                            sb.append(": ");
                            sb.append(nestedTLChange.toString(updateLevel << 2));
                            sb.append('\n');
                            LOG.fine(sb.toString());
                        }
                        tlChange.tokenChangeInfo().addEmbeddedChange(nestedTLChange.tokenChangeInfo());
                        if (nestedTLChange.isBoundsChange()) { // Attempt to find more nested
                            processBoundsChangesNested(eventInfo, nestedTLChange, path2updateInfo,
                                    diffLengthOrZero, updateLevel + 1);
                        } else { // Not a bounds change
                            processTokenChange(path2updateInfo, nestedTLChange.tokenChangeInfo());
                            eventInfo.setMinAffectedStartOffset(nestedTLChange.offset());
                            eventInfo.setMaxAffectedEndOffset(nestedTLChange.addedEndOffset());
                        }
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
    
    private void processTokenChange(
        Map<LanguagePath,UpdateInfo> path2updateInfo, TokenChangeInfo<?> change
    ) {
        // First mark the removed embeddings
        TokenList<?> removed = change.removedTokenList();
        if (removed != null) {
            markRemovedTokensEmbeddingsNested(path2updateInfo, removed);
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
        TokenList<?> currentTokenList = change.currentTokenList();
        LanguagePath languagePath = currentTokenList.languagePath();
        boolean topLevelChange = (languagePath.size() == 1);
        UpdateInfo updateInfo;
        if ((topLevelChange && maxTokenListListPathSize > 0)
            || (!topLevelChange && (updateInfo = findUpdateInfo(path2updateInfo, languagePath)) != null)
        ) {
            int addedTokenCount = change.addedTokenCount();
            for (int i = change.index(); i < addedTokenCount; i++) {
                Object tokenOrEC = currentTokenList.tokenOrEmbeddingContainer(i);
                if (tokenOrEC.getClass() == EmbeddingContainer.class) {
                    EmbeddingContainer<?> ec = (EmbeddingContainer<?>)tokenOrEC;
                    EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
                    if (etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING) {
                        do {
                            updateInfo = findUpdateInfo(path2updateInfo, etl.languagePath());
                            if (updateInfo != NO_UPDATE_INFO) {
                                // Mark that there was a new embedded token list added
                                updateInfo.markAdded(etl);
                            }
                            etl = etl.nextEmbeddedTokenList();
                        } while (etl != null && etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING);
                    }
                }
            }
        }
    }
    
    /**
     * Return update info or NO_INFO if the token list list is not maintained
     * for the given language path.
     */
    private UpdateInfo findUpdateInfo(
        Map<LanguagePath,UpdateInfo> path2updateInfo, LanguagePath languagePath
    ) {
        UpdateInfo updateInfo = path2updateInfo.get(languagePath);
        if (updateInfo == null) {
            TokenListList tll = tokenListListOrNull(languagePath);
            updateInfo = (tll != null) ? new UpdateInfo(tll) : NO_UPDATE_INFO;
            path2updateInfo.put(languagePath, updateInfo);
        }
        return updateInfo;
    }
    
    /**
     * Collect removed embeddings and nest deep enough for all maintained children
     * token list lists.
     */
    private void markRemovedTokensEmbeddingsNested(
        Map<LanguagePath,UpdateInfo> path2updateInfo, TokenList<?> tokenList
    ) {
        int tokenCount = tokenList.tokenCountCurrent();
        for (int i = 0; i < tokenCount; i++) {
            Object tokenOrEC = tokenList.tokenOrEmbeddingContainer(i);
            if (tokenOrEC.getClass() == EmbeddingContainer.class) {
                EmbeddingContainer<?> ec = (EmbeddingContainer<?>)tokenOrEC;
                EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
                if (etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING) {
                    do {
                        UpdateInfo updateInfo = findUpdateInfo(path2updateInfo, etl.languagePath());
                        if (updateInfo != NO_UPDATE_INFO && updateInfo.tokenListList().hasChildren()) {
                            // If the token list list has children then nest - should be safe
                            // since the nested changes should not interfere
                            markRemovedTokensEmbeddingsNested(path2updateInfo, etl);
                        }
                        etl = etl.nextEmbeddedTokenList();
                    } while (etl != null && etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING);
                }
            }
        }
    }
    
    private Language<T> language() {
        TokenList<T> tl = tokenList();
        Language<? extends TokenId> l;
        if (tl != null) {
            l = tokenList.languagePath().topLanguage();
        } else {
            assert (mutableTextInput != null);
            l = LexerSpiPackageAccessor.get().language(mutableTextInput);
        }
        @SuppressWarnings("unchecked")
        Language<T> language = (Language<T>)l;
        return language;
    }
    
    public Set<LanguagePath> languagePaths() {
        Set<LanguagePath> lps;
        synchronized (this) {
            lps = languagePaths;
        }
        if (lps == null) {
            LanguageOperation<T> langOp = LexerApiPackageAccessor.get().languageOperation(language());
            @SuppressWarnings("unchecked")
            Set<LanguagePath> clps = (Set<LanguagePath>)
                    ((HashSet<LanguagePath>)langOp.languagePaths()).clone();
            lps = clps;

            @SuppressWarnings("unchecked")
            Set<Language<? extends TokenId>> cel = (Set<Language<? extends TokenId>>)
                    ((HashSet<Language<? extends TokenId>>)langOp.exploredLanguages()).clone();
            synchronized (this) {
                languagePaths = lps;
                exploredLanguages = cel;
            }
        }
        return lps;
    }
    
    public void addLanguagePath(LanguagePath lp) {
        Set<LanguagePath> elps = languagePaths(); // init if not inited yet
        if (!elps.contains(lp)) {
            // Add the new language path
            Set<LanguagePath> lps = new HashSet<LanguagePath>();
            LanguageOperation.findLanguagePaths(elps, lps, exploredLanguages, lp);
            elps.addAll(lps);
            // Fire the token hierarchy change event
        }
    }
            
    public boolean isSnapshot() {
        return (liveTokenHierarchyOperation != null);
    }

    public TokenHierarchy<I> snapshotOf() {
        return (isSnapshot() ? liveTokenHierarchyOperation.tokenHierarchy() : null);
    }

    private void checkIsSnapshot() {
        if (!isSnapshot()) {
            throw new IllegalStateException("Not a snapshot");
        }
    }

    private void checkSnapshotNotReleased() {
        if (snapshotReleased) {
            throw new IllegalStateException("Snapshot already released"); // NOI18N
        }
    }

    public TokenHierarchy<I> createSnapshot() {
        if (isMutable()) {
            TokenHierarchyOperation<I,T> snapshot = new TokenHierarchyOperation<I,T>(this);
            snapshotRefs.add(new SnapshotRef(snapshot));
            return snapshot.tokenHierarchy();
        }
        return null;
    }

    public void snapshotRelease() {
        checkIsSnapshot();
        checkSnapshotNotReleased();

        snapshotReleased = true;
        if (liveTokenHierarchyOperation != null) { // only when "real" snapshot for mutable hierarchies
            // Remove the reference from the snapshots array
            liveTokenHierarchyOperation.removeSnapshot(this);
        }
    }

    public boolean isSnapshotReleased() {
        return snapshotReleased;
    }

    void removeSnapshot(SnapshotRef snapshotRef) {
        synchronized (snapshotRefs) {
            snapshotRefs.remove(snapshotRef);
        }
    }
    
    void removeSnapshot(TokenHierarchyOperation<I,T> snapshot) {
        synchronized (snapshotRefs) {
            for (int i = snapshotRefs.size() - 1; i >= 0; i--) {
                Reference ref = (Reference)snapshotRefs.get(i);
                if (ref.get() == snapshot) {
                    snapshotRefs.remove(i);
                    break;
                }
            }
        }
    }

    private int snapshotCount() {
        synchronized (snapshotRefs) {
            return snapshotRefs.size();
        }
    }

    public boolean canModifyToken(int index, AbstractToken token) {
        synchronized (snapshotRefs) {
            for (int i = snapshotCount() - 1; i >= 0; i--) {
                TokenHierarchyOperation op = snapshotRefs.get(i).get();
                
                if (op != null && ((SnapshotTokenList) op.tokenList()).canModifyToken(index, token)) {
                    return false;
                }
            }
        }
        return true;
    }

    public TokenHierarchyOperation<I,T> liveTokenHierarchyOperation() {
        return liveTokenHierarchyOperation;
    }

    public <TT extends TokenId> int tokenOffset(AbstractToken<TT> token, TokenList<TT> tokenList, int rawOffset) {
        if (this.tokenList.getClass() == SnapshotTokenList.class) {
            if (tokenList != null) {
                @SuppressWarnings("unchecked")
                SnapshotTokenList<TT> tlUC = (SnapshotTokenList<TT>)this.tokenList;
                return tlUC.tokenOffset(token, tokenList, rawOffset);
            } else { // passed tokenList is null => token removed from EmbeddedTokenList
                return rawOffset;
            }
        } else { // not a snapshot - regular situation
            return (tokenList != null)
                    ? tokenList.childTokenOffset(rawOffset)
                    : rawOffset;
        }
    }

    public int tokenShiftStartOffset() {
        return isSnapshot() ? ((SnapshotTokenList)tokenList).tokenShiftStartOffset() : -1;
    }

    public int tokenShiftEndOffset() {
        return isSnapshot() ? ((SnapshotTokenList)tokenList).tokenShiftEndOffset() : -1;
    }
    
    public String toString() {
        return "TOKEN HIERARCHY" // NOI18N
                + (mutableInputSource() != null ? " for " + mutableInputSource() : "") // NOI18N
                + ":\n" // NOI18N
                + LexerUtilsConstants.appendTokenList(null, tokenList, -1).toString()
                + '\n';
    }
    
    /**
     * Check consistency of the whole token hierarchy.
     * @return string describing the problem or null if the hierarchy is consistent.
     */
    public String checkConsistency() {
        return checkConsistencyTokenList(checkedTokenList(), ArrayUtilities.emptyIntArray(), 0);
    }
    
    private String checkConsistencyTokenList(TokenList<? extends TokenId> tokenList,
    int[] parentIndexes, int firstTokenOffset) {
        int tokenCountCurrent = tokenList.tokenCountCurrent();
        int[] indexes = ArrayUtilities.intArray(parentIndexes, parentIndexes.length + 1);
        boolean continuous = tokenList.isContinuous();
        int lastOffset = firstTokenOffset;
        for (int i = 0; i < tokenCountCurrent; i++) {
            Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(i);
            if (tokenOrEmbeddingContainer == null) {
                return dumpContext("Null token", tokenList, i, parentIndexes); // NOI18N
            }
            Token<?> token = LexerUtilsConstants.token(tokenOrEmbeddingContainer);
            int offset = (token.isFlyweight()) ? lastOffset : token.offset(null);
            if (offset < 0) {
                return dumpContext("Token offset=" + offset + " < 0", tokenList, i, parentIndexes); // NOI18N
            }
            if (offset < lastOffset) {
                return dumpContext("Token offset=" + offset + " < lastOffset=" + lastOffset,
                        tokenList, i, parentIndexes);
            }
            if (offset > lastOffset && continuous) {
                return dumpContext("Gap between tokens; offset=" + offset + ", lastOffset=" + lastOffset,
                        tokenList, i, parentIndexes);
            }
            lastOffset = offset + token.length();
            if (tokenOrEmbeddingContainer.getClass() == EmbeddingContainer.class) {
                EmbeddingContainer<?> ec = (EmbeddingContainer<?>)tokenOrEmbeddingContainer;
                EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
                while (etl != null) {
                    String error = checkConsistencyTokenList(etl, indexes, offset + etl.embedding().startSkipLength());
                    if (error != null)
                        return error;
                    etl = etl.nextEmbeddedTokenList();
                }
            }
        }
        return null;
    }
    
    private String dumpContext(String msg, TokenList<?> tokenList, int index, int[] parentIndexes) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        sb.append(" at index="); // NOI18N
        sb.append(index);
        sb.append(" of tokens of language "); // NOI18N
        sb.append(tokenList.languagePath().innerLanguage().mimeType());
        sb.append('\n');
        LexerUtilsConstants.appendTokenList(sb, tokenList, index, index - 2, index + 3, false, 0);
        sb.append("\nParents:\n"); // NOI18N
        sb.append(tracePath(parentIndexes, tokenList.languagePath()));
        return sb.toString();
    }
    
    private String tracePath(int[] indexes, LanguagePath languagePath) {
        StringBuilder sb  = new StringBuilder();
        TokenList<?> tokenList = checkedTokenList();
        for (int i = 0; i < indexes.length; i++) {
            LexerUtilsConstants.appendTokenInfo(sb, tokenList, i,
                    tokenHierarchy(), false, 0);
            tokenList = EmbeddingContainer.embeddedTokenList(tokenList, indexes[i], languagePath.language(i));
        }
        return sb.toString();
    }
    
    private final class SnapshotRef extends WeakReference<TokenHierarchyOperation<I,T>> implements Runnable {
        
        SnapshotRef(TokenHierarchyOperation<I,T> snapshot) {
            super(snapshot, org.openide.util.Utilities.activeReferenceQueue());
        }

        public void run() {
            if (liveTokenHierarchyOperation != null) {
                // Remove the reference from the snapshots array
                liveTokenHierarchyOperation.removeSnapshot(this);
            }
        }

    }

    /**
     * Special value to avoid double map search for token list lists updating.
     */
    public static final UpdateInfo NO_UPDATE_INFO = new UpdateInfo(null);
        
    /**
     * Information about update in the related token list list because of a text modification .
     */
    public static final class UpdateInfo {
        
        final TokenListList tokenListList;

        int index;

        int removeCount;

        List<TokenList<?>> added;
        
        int firstModifiedStartTokenIndex;

        int lastModifiedEndTokenIndex;
        
        Boolean lexedBeyondModPoint;
        
        public UpdateInfo(TokenListList tokenListList) {
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
            if (added.size() == 0) {
                if (index == -1) {
                    index = tokenListList.findIndex(addedTokenList.startOffset());
                }
                added = new ArrayList<TokenList<?>>(4);
            }
            added.add(addedTokenList);
        }
        
        public void update(TokenHierarchyEventInfo eventInfo) {
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
                if (state != matchState) {
                    index += added.size();
                    // Must continue relexing existing section(s) (from a different start state)
                    // until the relexing will stop before the last token of the given section.
                    while (true) {
                        @SuppressWarnings("unchecked")
                        EmbeddedTokenList<TokenId> tokenList = (EmbeddedTokenList<TokenId>)tokenListList.getOrNull(index);
                        if (tokenList == null)
                            break;
                        if (tokenList.tokenCount() > 0) {
                            // Remember state after the last token of the given section
                            matchState = tokenList.state(tokenList.tokenCount() - 1);
                            TokenListChange<TokenId> tlChange = new TokenListChange<TokenId>(tokenList);
                            TokenListUpdater.update(tokenList, tokenList.startOffset(), 0, 0, tlChange);
                            if (tlChange.addedEndOffset() == tokenList.endOffset()) { // Modified till the end
                                // Since the section is non-empty (checked above) there should be >0 tokens
                                // If the states don't match after the last token then continue with next section
                                if (tokenList.state(tokenList.tokenCount() - 1) == matchState)
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
                    if (tokenListList.size() == 0 
                            || (lastTokenList = tokenListList.getExistingOrNull(tokenListList.size() - 1)) == null
                            || (lastTokenList.endOffset() < tokenList.endOffset())
                    ) {
                        lexedBeyondModPoint = Boolean.FALSE;
                        
                    }
                }
            }
            return lexedBeyondModPoint.booleanValue();
        }

    }
    
    private static final Comparator<UpdateInfo> updateInfoComparator = new Comparator<UpdateInfo>() {
        public int compare(UpdateInfo ui1, UpdateInfo ui2) {
            return ui1.tokenListList.languagePath().size() - ui2.tokenListList.languagePath().size();
        }
    };

}
