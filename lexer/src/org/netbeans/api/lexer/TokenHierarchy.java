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

package org.netbeans.api.lexer;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.TokenSequenceList;
import org.netbeans.lib.lexer.inc.DocumentInput;

/**
 * Token hierarchy represents a given input source as a browsable hierarchy of tokens.
 * <br>
 * It's is an entry point into the Lexer API.
 * <br/>
 * It allows to create token sequences for hierarchy exploration
 * and watching for token changes by attaching the token hierarchy listeners.
 * <br>
 * The hierarchy may either be flat or it can be a tree if the
 * corresponding language hierarchy contains language embeddings.
 *
 * <p/>
 * Token hierarchy may also act as a snapshot of another "live" token hierarchy.
 * <br/>
 * The snapshot may be created at any time by using {@link #createSnapshot()}
 * on the live token hierarchy.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenHierarchy<I> { // "I" stands for mutable input source
    
    /**
     * Get or create mutable token hierarchy for the given swing document.
     * <br>
     * All the operations with the obtained token hierarchy
     * must be done under document's read lock (or write lock).
     *
     * @param doc swing text document for which the token hiearchy should be obtained.
     * @return token hierarchy or <code>null</code> in case the token hierarchy
     *  does not exist yet and the <code>Language.class</code>
     *  document property was not yet initialized with the valid language
     *  so the hierarchy cannot be created.
     */
    public static <D extends Document> TokenHierarchy<D> get(D doc) {
        DocumentInput<D> di = DocumentInput.get(doc);
        return di.tokenHierarchyControl().tokenHierarchy();
    }
    
    /**
     * Create token hierarchy for the given non-mutating input text (for example
     * java.lang.String).
     *
     * @see #create(CharSequence,boolean,Language,Set,InputAttributes)
     */
    public static TokenHierarchy<Void> create(CharSequence inputText,
    Language<? extends TokenId> language) {
        return create(inputText, false, language, null, null);
    }

    /**
     * Create token hierarchy for the given input text.
     *
     * @param inputText input text containing the characters to tokenize.
     * @param copyInputText <code>true</code> in case the content of the input
     *  will not be modified in the future so the created tokens can reference it.
     *  <br>
     *  <code>false</code> means that the text can change in the future
     *  and the tokens should not directly reference it. Instead copy of the necessary text
     *  from the input should be made and the original text should not be referenced.
     * @param language language defining how the input
     *  will be tokenized.
     * @param skipTokenIds set containing the token ids for which the tokens
     *  should not be created in the created token hierarchy.
     *  <br/>
     *  <code>null</code> may be passed which means that no tokens will be skipped.
     *  <br/>
     *  This applies to top level of the token hierarchy only (not to embedded tokens).
     *  <br/>
     *  The provided set should be efficient enough - ideally created by e.g.
     *  {@link Language#tokenCategoryMembers(String)}
     *  or {@link Language#merge(Collection,Collection)}.
     *
     * @param inputAttributes additional properties related to the input
     *  that may influence token creation or lexer operation
     *  for the particular language (such as version of the language to be used).
     * @return non-null token hierarchy.
     */
    public static <T extends TokenId> TokenHierarchy<Void> create(
    CharSequence inputText, boolean copyInputText,
    Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {

        return new TokenHierarchyOperation<Void,T>(inputText, copyInputText,
                language, skipTokenIds, inputAttributes).tokenHierarchy();
    }

    /**
     * Create token hierarchy for the given reader.
     *
     * @param inputReader input reader containing the characters to tokenize.
     * @param language language defining how the input
     *  will be tokenized.
     * @param skipTokenIds set containing the token ids for which the tokens
     *  should not be created in the created token hierarchy.
     *  <br/>
     *  <code>null</code> may be passed which means that no tokens will be skipped.
     *  <br/>
     *  This applies to top level of the token hierarchy only (not to embedded tokens).
     *  <br/>
     *  The provided set should be efficient enough - ideally created by e.g.
     *  {@link Language#tokenCategoryMembers(String)}
     *  or {@link Language#merge(Collection,Collection)}.
     *
     * @param inputAttributes additional properties related to the input
     *  that may influence token creation or lexer operation
     *  for the particular language (such as version of the language to be used).
     * @return non-null token hierarchy.
     */
    public static <T extends TokenId> TokenHierarchy<Void> create(
    Reader inputReader,
    Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {

        return new TokenHierarchyOperation<Void,T>(inputReader,
                language, skipTokenIds, inputAttributes).tokenHierarchy();
    }
    

    private TokenHierarchyOperation<I,?> operation;

    TokenHierarchy(TokenHierarchyOperation<I,?> operation) {
        this.operation = operation;
    }

    /**
     * Get token sequence of the top level language of the token hierarchy.
     * <br/>
     * The token sequences for inner levels of the token hierarchy can be
     * obtained by calling {@link TokenSequence#embedded()}.
     *
     * @return non-null token sequence of the top level of the token hierarchy.
     */
    public TokenSequence<? extends TokenId> tokenSequence() {
        @SuppressWarnings("unchecked")
        TokenSequence<? extends TokenId> ts = new TokenSequence<TokenId>(
                (TokenList<TokenId>)operation.checkedTokenList());
        return ts;
    }

    /**
     * Get token sequence of the top level of the language hierarchy
     * only if it's of the given language.
     *
     * @return non-null token sequence or null if the top level token sequence
     *  satisfies the condition <code>(tokenSequence().language() == language)</code>.
     *  Null is returned otherwise.
     *
     */
    public <T extends TokenId> TokenSequence<T> tokenSequence(Language<T> language) {
        TokenList<? extends TokenId> tokenList = operation.checkedTokenList();
        @SuppressWarnings("unchecked")
        TokenSequence<T> ts
                = (tokenList.languagePath().topLanguage() == language)
                    ? new TokenSequence<T>((TokenList<T>)tokenList)
                    : null;
        return ts;
    }
    
    /**
     * Get immutable list of token sequences with the given language path
     * from this hierarchy.
     * <br/>
     * For mutable token hierarchies the list should only be used
     * under read-locked input source. A new list should be
     * obtained after each modification.
     * {@link java.util.ConcurrentModificationException} may be thrown
     * when iterating over (or retrieving items) from the obsolete list.
     * <br/>
     * For forward exploration of the list the iterator is preferred over
     * index-based iteration because the list contents can be constructed lazily.
     * 
     * @param languagePath non-null language path that the obtained token sequences
     *  will all have.
     * @param startOffset starting offset of the TSs to get. Use 0 for no limit.
     *  If the particular TS ends after this offset then it will be returned.
     * @param endOffset ending offset of the TS to get. Use Integer.MAX_VALUE for no limit.
     *  If the particular TS starts before this offset then it will be returned.
     * @return non-null list of <code>TokenSequence</code>s.
     */
    public List<TokenSequence<? extends TokenId>> tokenSequenceList(
    LanguagePath languagePath, int startOffset, int endOffset) {
        if (languagePath == null)
            throw new IllegalArgumentException("languagePath cannot be null"); // NOI18N
        return TokenSequenceList.create(operation, languagePath, startOffset, endOffset);
    }

    /**
     * Gets the list of all embedded <code>TokenSequence</code>s at the given offset.
     * This method will use the top level <code>TokenSequence</code> in this
     * hierarchy to drill down through the token at the specified <code>offset</code>
     * and all its possible embedded sub-sequences.
     * 
     * <p>If the <code>offset</code>
     * lies at the border between two tokens the <code>backwardBias</code>
     * parameter will be used to choose either the token on the left hand side
     * (<code>backwardBias == true</code>) of the <code>offset</code> or
     * on the right hand side (<code>backwardBias == false</code>).
     * 
     * @param offset The offset to look at.
     * @param backwardBias If <code>true</code> the backward lying token will
     *   be used in case that the <code>offset</code> specifies position between
     *   two tokens. If <code>false</code> the forward lying token will be used.
     * 
     * @return The list of all sequences embedded at the given offset. The list
     *   has always at least one element and that is the top level
     *   <code>TokenSequence</code>. The sequences in the list are ordered from
     *   the top level sequence to the bottom one.
     * 
     * @since 1.20
     */
    public List<TokenSequence<? extends TokenId>> embeddedTokenSequences(
        int offset, boolean backwardBias
    ) {
        TokenSequence<? extends TokenId> embedded = tokenSequence();
        List<TokenSequence<? extends TokenId>> sequences = new ArrayList<TokenSequence<? extends TokenId>>();

        do {
            TokenSequence<? extends TokenId> seq = embedded;
            sequences.add(seq);
            embedded = null;

            seq.move(offset);
            if (seq.moveNext()) {
                if (seq.offset() == offset && backwardBias) {
                    if (seq.movePrevious()) {
                        embedded = seq.embedded();
                    }
                } else {
                    embedded = seq.embedded();
                }
            } else if (backwardBias && seq.movePrevious()) {
                embedded = seq.embedded();
            }
        } while (embedded != null);
        
        return sequences;
    }
    
    /**
     * Get a set of language paths used by this token hierarchy.
     * <br/>
     * The set includes "static" paths that are those reachable by traversing
     * token ids of the top language and searching for the default embeddings
     * that could be created by
     * {@link org.netbeans.spi.lexer.LanguageHierarchy#embedding(Token,LanguagePath,InputAttributes)}.
     * 
     */
    public Set<LanguagePath> languagePaths() {
        return operation.languagePaths();
    }

    /**
     * Whether input text of this token hierarchy is mutable or not.
     *
     * @return true if the input text is mutable or false otherwise.
     */
    public boolean isMutable() {
        return operation.isMutable();
    }
    
    /**
     * Get mutable input source providing text over which
     * this token hierarchy was constructed.
     * <br/>
     * For example it may be a swing text document instance
     * {@link javax.swing.text.Document} in case the token hierarchy
     * was constructed for its text.
     * <br/>
     * Snapshot will return the same input source
     * as the original mutable token hierarchy.
     *
     * @return mutable input source or null in case this token hierarchy
     *  was not created over mutable input source.
     */
    public I mutableInputSource() {
        return operation.mutableInputSource();
    }
    
    /**
     * Token hierarchy may be set inactive to release resources consumed
     * by tokens.
     * <br>
     * Only token hierarchies over a mutable input can become inactive.
     *
     * @return true if valid tokens exist for this hierarchy
     *  or false if the token hierarchy is inactive and there are currently
     *  no active tokens to represent it.
     */
    public boolean isActive() {
        return operation.isActive();
    }
    
    /**
     * Add listener for token changes inside this hierarchy.
     *
     * @param listener token change listener to be added.
     */
    public void addTokenHierarchyListener(TokenHierarchyListener listener) {
        operation.addTokenHierarchyListener(listener);
    }
    
    /**
     * Remove listener for token changes inside this hierarchy.
     *
     * @param listener token change listener to be removed.
     */
    public void removeTokenHierarchyListener(TokenHierarchyListener listener) {
        operation.removeTokenHierarchyListener(listener);
    }
    
    /**
     * Create a snapshot of the present mutable token hierarchy.
     * <br/>
     * Even with subsequent modifications to the "live" token hierarchy
     * the tokens of the snapshot will retain the original ids, texts and offsets.
     * <br/>
     * The snapshot retains the original token instances that were present
     * in the token hierarchy at time of its creation.
     * 
     * <p/>
     * The snapshot creation is cheap. With subsequent modifications
     * of the mutable input source the snapshot maintenance brings an overhead.
     * Therefore the snapshot should be released as soon as it's no longer needed.
     * Ideally the releasing should be performed by using {@link #snapshotRelease()}.
     * Another way is to forget the reference to the snapshot token hierarchy
     * but it depends on the garbage collector's releasing of the weak reference.
     * <br/>
     * As the snapshot shares information with the live hierarchy
     * its content must also be accessed under a read lock
     * in the same way like the live hierarchy.
     * 
     * <br/>
     * If a particular token in the snapshot is mutable
     * then <code>token.offset(snapshotHierarchy)</code> will give the offset
     * of the token in a snapshot while <code>token.offset(null)</code>
     * will return the offset of the token in the live hierarchy.
     * 
     * <p/>
     * The snapshot attempts to share tokens with the live token hierarchy.
     * <br/>
     * Upon a first modification in the live token hierarchy (after the snapshot creation)
     * an initial and ending areas of tokens shared between the snapshot
     * and live hierarchy get created. The tokens that were in the live hierarchy prior
     * to the modification (but which were removed from it because of the modification)
     * are captured and used by the snapshot as the "middle" area. With subsequent
     * modifications the initial and ending areas of shared tokens may be reduced
     * (and the original tokens captured for the snapshot)
     * if any of the tokens contained in them get modified.
     * 
     * <p/>
     * The overhead of the subsequent token modifications
     * for an existing snapshot in the present implementation are the following:<ul>
     *  <li> Removed token's text must be maintained which creates an overhead
     *    equal to the original token's text characters plus about 24 bytes.
     *    <br/>
     *    This is a single-time overhead per each removed token
     *    referenced by at least one snapshot.
     *  </li>
     *  <li> Token's original offset must be maintained. The overhead
     *    is about 32 bytes per token per snapshot.
     *  </li>
     * 
     * @return non-null new token hierarchy which is a snapshot
     *  of this token hierarchy. For non-mutable token hierarchies
     *  this method returns null (original token hierarchy may be used
     *  in the same way like the snapshot would be used).
     */
    public TokenHierarchy<I> createSnapshot() {
        return operation.createSnapshot();
    }

    /**
     * Check whether this token hierarchy is a snapshot.
     *
     * @return true if this is snapshot or false if not.
     */
    public boolean isSnapshot() {
        return operation.isSnapshot();
    }

    /**
     * Release snapshot - should only be called if this token hierarchy
     * is a snapshot.
     * @throws IllegalStateException if this token hierarchy was already released
     *  or it's not a snapshot.
     */
    public void snapshotRelease() {
        operation.snapshotRelease();
    }

    /**
     * Check whether this snapshot is released.
     *
     * @return true if this snapshot is already released or false if not.
     * @throws IllegalStateException if this token hierarchy is not a snapshot.
     */
    public boolean isSnapshotReleased() {
        return operation.isSnapshotReleased();
    }

    /**
     * If this token hierarchy is snapshot then return the token hierarchy
     * for which this snapshot was constructed.
     *
     * @return live token hierarchy or null if this is not a snapshot.
     */
     public TokenHierarchy<I> snapshotOf() {
         return operation.snapshotOf();
     }
     
     /**
      * Get start offset of the area where the tokens in the token hierarchy snapshot
      * have explicitly shifted offsets.
      * <br/>
      * With subsequent modifications the area where the token offsets are shifted
      * explicitly gets extended (modifications with lowest and highest offsets
      * define the area boundaries).
      * <br/>
      * Below this offset the snapshot uses all the tokens from the live token hierarchy
      * directly.
      * <br/>
      * Above this area (and below {@link #tokenShiftEndOffset()} the tokens
      * are either removed from the live token hierarchy or still present in it
      * but all of them have explicitly corrected offsets.
      * <br/>
      * The clients may get a token from the snapshot and check its offset
      * to find out whether it's below token shift start offset.
      * <br/>
      * If so then the token is present in the live token hierarchy as well
      * and it has the same offset there like in the snapshot.
      *
      * @see #tokenShiftEndOffset()
      */
     public int tokenShiftStartOffset() {
         return operation.tokenShiftStartOffset();
     }
     
     /**
      * Get end offset of the area where the tokens in the token hierarchy snapshot
      * have explicitly shifted offsets.
      * <br/>
      * The clients may get a token from the snapshot and check its offset
      * to find out whether it's above token shift end offset.
      * <br/>
      * If so then the token is present in the live token hierarchy as well
      * and its offset there can be determined by using
      * <code>Token.offset(null)</code>.
      *
      * @see #tokenShiftStartOffset()
      */
     public int tokenShiftEndOffset() {
         return operation.tokenShiftEndOffset();
     }

    /**
     * Obtaining of token hierarchy operation is only intended to be done
     * by package accessor.
     */
    TokenHierarchyOperation<I,?> operation() {
        return operation;
    }
    
    @Override
    public String toString() {
        return operation.toString();
    }

}
