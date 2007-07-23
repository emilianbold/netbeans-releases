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
import java.util.HashMap;
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
import org.netbeans.lib.lexer.inc.TokenListChange;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Token hierarchy operation services tasks of its associated token hierarchy.
 * <br/>
 * There is one-to-one relationship between token hierarchy and its operation.
 * <br/>
 * Token hierarchy may be a snapshot of an original 
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
    
    private Map<LanguagePath,TokenListList> tokenListListMap;

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
    
    public synchronized TokenListList tokenListList(LanguagePath languagePath) {
        if (tokenListListMap == null)
            tokenListListMap = new HashMap<LanguagePath,TokenListList>();

        TokenListList tll = tokenListListMap.get(languagePath);
        if (tll == null) {
            tll = new TokenListList(this, languagePath);
            tokenListListMap.put(languagePath, tll);
        }
        return tll;
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

            incTokenList.replaceTokens(eventInfo, change, incTokenList.tokenCountCurrent());
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
            updateCaches();
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
            IncTokenList<T> incTokenList = (IncTokenList<T>)tokenList;
            incTokenList.incrementModCount();
            TokenListChange<T> change = new TokenListChange<T>(incTokenList);

            if (LOG.isLoggable(Level.FINEST)) {
                // Display current state of the hierarchy by faking its text
                // through original text
                if (tokenList != null) {
                    CharSequence text = incTokenList.text();
                    assert (text != null);
                    incTokenList.setText(new OriginalText(text, offset, removedText, insertedLength));
                    // Dump all contents
                    LOG.log(Level.FINEST, toString());
                    // Return the original text
                    incTokenList.setText(text);
                }
            }

            TokenListUpdater.update(incTokenList, eventInfo, change);
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("<<<<<<<<<<<<<<<<<< LEXER CHANGE START ------------------\n"); // NOI18N
                LOG.fine("ROOT CHANGE: " + change.toString(0) + "\n"); // NOI18N
            }
            if (!incTokenList.isFullyLexed())
                incTokenList.refreshLexerInputOperation();
            
            synchronized (snapshotRefs) {
                for (int i = snapshotRefs.size() - 1; i >= 0; i--) {
                    TokenHierarchyOperation<I,T> op = snapshotRefs.get(i).get();
                    
                    if (op != null) {
                        ((SnapshotTokenList<T>)op.tokenList()).update(eventInfo, change);
                    }
                }
            }
            
            eventInfo.setTokenChangeInfo(change.tokenChangeInfo());

            // Create nested changes if necessary
            if (change.isBoundsChange()) {
                // Use modification boundaries first (for possibly having just
                // boundary change
                eventInfo.setAffectedStartOffset(eventInfo.modificationOffset());
                eventInfo.setAffectedEndOffset(eventInfo.modificationOffset()
                        + Math.max(0, eventInfo.insertedLength() - eventInfo.removedLength()));
                addNestedChanges(eventInfo, change, 1);
            } else { // Not bounds-only change
                eventInfo.setAffectedStartOffset(change.offset());
                eventInfo.setAffectedEndOffset(change.addedEndOffset());
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
            updateCaches();
            fireTokenHierarchyChanged(
                LexerApiPackageAccessor.get().createTokenChangeEvent(eventInfo));
        } // not active - no changes fired
    }
    
    private void updateCaches() {
        if (tokenListListMap != null)
            tokenListListMap.clear();
    }
    
    /**
     * Add a nested changes to the original change for the given embedding token list
     * recursively traversing next linked embedded token lists.
     * 
     * @param change non-null change. It must be bounds change.
     * @param etl non-null embedding token list
     */
    private <TX extends TokenId> void addNestedChanges(
    TokenHierarchyEventInfo eventInfo, TokenListChange<TX> change, int level) {
        EmbeddedTokenList<? extends TokenId> etl = EmbeddingContainer.getEmbeddingIfExists(
                change.tokenChangeInfo().removedTokenList().tokenOrEmbeddingContainer(0));
        if (etl != null) {
            int tokenOffset = change.tokenList().tokenOffset(change.index());
            int modRelOffset = eventInfo.modificationOffset() - tokenOffset;
            int beyondModLength = (tokenOffset + LexerUtilsConstants.token(change.tokenList(), change.index()).length()) 
                    - (modRelOffset + Math.max(0, eventInfo.insertedLength() - eventInfo.removedLength()));
            // All embedded token lists need to be re-routed to a new embedding container
            // and then updated by the incremental algorithm
            @SuppressWarnings("unchecked")
            EmbeddingContainer<TX> newEC = new EmbeddingContainer<TX>(
                    (AbstractToken<TX>)change.addedTokensOrBranches().get(0));
            newEC.setFirstEmbeddedTokenList(etl);
            change.tokenList().wrapToken(change.index(), newEC);
            EmbeddedTokenList<? extends TokenId> prevEtl = null;
            do {
                // Check whether the change was not in the start or end skip lengths
                if (modRelOffset >= etl.embedding().startSkipLength()
                        && beyondModLength >= etl.embedding().endSkipLength()
                ) {
                    etl.setEmbeddingContainer(newEC);

                    @SuppressWarnings("unchecked")
                    TokenListChange<TokenId> nestedChange = new TokenListChange<TokenId>(
                            (EmbeddedTokenList<TokenId>)etl);
                    @SuppressWarnings("unchecked")
                    EmbeddedTokenList<TokenId> etlT = (EmbeddedTokenList<TokenId>)etl;
                    TokenListUpdater.update(etlT, eventInfo, nestedChange);
                    if (LOG.isLoggable(Level.FINE)) {
                        StringBuilder sb = new StringBuilder();
                        ArrayUtilities.appendSpaces(sb, level << 2);
                        sb.append("NESTED CHANGE at level="); // NOI18N
                        sb.append(level);
                        sb.append(": ");
                        sb.append(nestedChange.toString(level << 2));
                        sb.append('\n');
                        LOG.fine(sb.toString());
                    }
                    change.tokenChangeInfo().addEmbeddedChange(nestedChange.tokenChangeInfo());
                    if (nestedChange.isBoundsChange()) { // Attempt to find more nested
                        addNestedChanges(eventInfo, nestedChange, level + 1);
                    } else { // Not a bounds change
                        eventInfo.setMinAffectedStartOffset(nestedChange.offset());
                        eventInfo.setMaxAffectedEndOffset(nestedChange.addedEndOffset());
                    }
                    prevEtl = etl;
                    etl = etl.nextEmbeddedTokenList();

                } else { // Mod in skip lengths => Remove the etl from chain
                    etl = etl.nextEmbeddedTokenList();
                    if (prevEtl == null) {
                        newEC.setFirstEmbeddedTokenList(etl);
                    } else {
                        prevEtl.setNextEmbeddedTokenList(etl);
                    }
                }
            } while (etl != null);
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
            LanguageOperation<T> langOp = LexerUtilsConstants.languageOperation(language());
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
            tokenList = EmbeddingContainer.getEmbedding(tokenList, indexes[i], languagePath.language(i));
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

}
