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
            path2tokenListList = new HashMap<LanguagePath,TokenListList>(4, 0.5f);
        }
        return path2tokenListList;
    }
    
    public synchronized TokenListList tokenListListOrNull(LanguagePath languagePath) {
        return (path2tokenListList != null) ? path2tokenListList.get(languagePath) : null;
    }

    int maxTokenListListPathSize() {
        return maxTokenListListPathSize;
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
            eventInfo.setMaxAffectedEndOffset(text.length());
            
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
        if (active) {
            TokenHierarchyEventInfo eventInfo = new TokenHierarchyEventInfo(
                    this, TokenHierarchyEventType.MODIFICATION,
                    offset, removedLength, removedText, insertedLength);
            // First a top-level token list will be updated then the embedded ones.
            IncTokenList<T> incTokenList = (IncTokenList<T>)tokenList;

            if (TokenHierarchyUpdate.LOG.isLoggable(Level.FINEST)) {
                // Display current state of the hierarchy by faking its text
                // through original text
                CharSequence text = incTokenList.text();
                assert (text != null);
                incTokenList.setText(new OriginalText(text, offset, removedText, insertedLength));
                // Dump all contents
                TokenHierarchyUpdate.LOG.log(Level.FINEST, toString());
                // Return the original text
                incTokenList.setText(text);
            }
            
            new TokenHierarchyUpdate(eventInfo).update(incTokenList);
            
            if (TokenHierarchyUpdate.LOG.isLoggable(Level.FINE)) {
                TokenHierarchyUpdate.LOG.fine("EVENT: " + eventInfo + "\n"); // NOI18N
                String extraMsg = "";
                if (TokenHierarchyUpdate.LOG.isLoggable(Level.FINER)) {
                    // Check consistency of the whole token hierarchy
                    String error = checkConsistency();
                    if (error != null)
                        TokenHierarchyUpdate.LOG.finer("!!!CONSISTENCY-ERROR!!!: " + error + "\n");
                    else
                        extraMsg = "(TokenHierarchy Check OK) ";
                }
                TokenHierarchyUpdate.LOG.fine(">>>>>>>>>>>>>>>>>> LEXER CHANGE END " + extraMsg + "------------------\n"); // NOI18N
            }

            fireTokenHierarchyChanged(LexerApiPackageAccessor.get().createTokenChangeEvent(eventInfo));
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TOKEN HIERARCHY"); // NOI18N
        if (mutableInputSource() != null) {
            sb.append(" for " + mutableInputSource());
        }
        sb.append(":\n"); // NOI18N
        LexerUtilsConstants.appendTokenList(sb, tokenList);
        if (path2tokenListList != null && path2tokenListList.size() > 0) {
            sb.append(path2tokenListList.size());
            sb.append(" TokenListList(s) maintained:\n"); // NOI18N
            for (TokenListList tll : path2tokenListList.values()) {
                sb.append(tll).append('\n');
            }
        }
        return sb.toString();
    }
    
    /**
     * Check consistency of the whole token hierarchy.
     * @return string describing the problem or null if the hierarchy is consistent.
     */
    public String checkConsistency() {
        String error = checkConsistencyTokenList(checkedTokenList(), ArrayUtilities.emptyIntArray(), 0);
        if (error == null && path2tokenListList != null) { // Check token sequences lists
            for (TokenListList tll : path2tokenListList.values()) {
                for (TokenList<?> tl : tll) {
                    error = checkConsistencyTokenList(tl, ArrayUtilities.emptyIntArray(), tl.startOffset());
                    if (error != null) {
                        return error;
                    }
                }
            }
        }
        return error;
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
            AbstractToken<?> token = LexerUtilsConstants.token(tokenOrEmbeddingContainer);
            // Check whether tokenList.startOffset() corresponds to the start of first token
            if (i == 0 && continuous && tokenCountCurrent > 0 && !token.isFlyweight()) {
                if (token.offset(null) != tokenList.startOffset()) {
                    return dumpContext("firstToken.offset()=" + token.offset(null) +
                            " != tokenList.startOffset()=" + tokenList.startOffset(),
                            tokenList, i, parentIndexes);
                }
            }
            if (token.tokenList() != tokenList) {
                return dumpContext("Invalid token.tokenList()=" + token.tokenList(),
                        tokenList, i, parentIndexes);
            }
            if (token.text() == null) {
                return dumpContext("Null token.text()=" + token.tokenList(),
                        tokenList, i, parentIndexes);
            }
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
    
    public String findTokenContext(AbstractToken<?> token) {
        return findTokenContext(token, checkedTokenList(), ArrayUtilities.emptyIntArray());
    }

    private String findTokenContext(AbstractToken<?> token, TokenList<?> tokenList, int[] parentIndexes) {
        int tokenCountCurrent = tokenList.tokenCountCurrent();
        int[] indexes = ArrayUtilities.intArray(parentIndexes, parentIndexes.length + 1);
        for (int i = 0; i < tokenCountCurrent; i++) {
            Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(i);
            if (tokenOrEmbeddingContainer == null) {
                continue;
            }
            if (tokenOrEmbeddingContainer.getClass() == EmbeddingContainer.class) {
                EmbeddingContainer<?> ec = (EmbeddingContainer<?>)tokenOrEmbeddingContainer;
                if (ec.token() == token) {
                    return dumpContext("Token found.", tokenList, i, indexes);
                }
                EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
                while (etl != null) {
                    String context = findTokenContext(token, etl, indexes);
                    if (context != null)
                        return context;
                    etl = etl.nextEmbeddedTokenList();
                }

            } else if (tokenOrEmbeddingContainer == token) {
                return dumpContext("Token found.", tokenList, i, indexes);
            }
        }
        return null;
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

}
