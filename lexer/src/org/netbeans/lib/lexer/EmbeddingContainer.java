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

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.lexer.inc.TokenHierarchyUpdate;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.inc.TokenChangeInfo;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.JoinToken;
import org.netbeans.spi.lexer.EmbeddingPresence;
import org.netbeans.spi.lexer.LanguageHierarchy;


/**
 * Embedding info contains information about all the embeddings
 * for a particular token in a token list.
 * <br>
 * There can be one or more {@link EmbeddedTokenList} instances for each
 * cotnained embedding.
 * <p>
 * There is an intent to not degrade performance significantly
 * with each extra language embedding level so the token list maintains direct
 * link to the root level.
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class EmbeddingContainer<T extends TokenId> implements TokenOrEmbedding<T> {

    // -J-Dorg.netbeans.lib.lexer.EmbeddingContainer.level=FINE
    private static final Logger LOG = Logger.getLogger(EmbeddingContainer.class.getName());

    /**
     * Get embedded token list.
     *
     * @param tokenList non-null token list in which the token for which the embedding
     *  should be obtained resides.
     * @param index &gt;=0 index of the token in the token list where the embedding
     *  should be obtained.
     * @param language whether only language embeddding of the particular language
     *  was requested. It may be null if any embedding should be returned.
     * @param initTokensInNew true if tokens should be created when a new ETL gets created.
     *  False in case this is called from TokenListList to grab ETLs for sections joining.
     */
    public static <T extends TokenId, ET extends TokenId> EmbeddedTokenList<ET> embeddedTokenList(
    TokenList<T> tokenList, int index, Set<Language<?>> embeddedLanguages, boolean initTokensInNew) {
        TokenList<?> rootTokenList = tokenList.rootTokenList();
        synchronized (rootTokenList) {
            TokenOrEmbedding<T> tokenOrEmbedding = tokenList.tokenOrEmbedding(index);
            EmbeddingContainer<T> ec = tokenOrEmbedding.embedding();
            AbstractToken<T> token = tokenOrEmbedding.token();  
            if (token.getClass() == JoinToken.class) {
                // Currently do not allow to create embedding over token that is physically joined
                return null;
            }
            EmbeddingPresence ep;
            if (ec != null) {
                ep = null;
            } else { // No embedding was created yet
                // Check embedding presence
                ep = LexerUtilsConstants.innerLanguageOperation(tokenList.languagePath()).embeddingPresence(token.id());
                if (ep == EmbeddingPresence.NONE) {
                    return null;
                }
            }

            // Now either ec == null for no embedding yet or linked list of embedded token lists of ec
            // need to be processed to find the embedded token list for requested language.
            EmbeddedTokenList<?> prevEtl;
            if (ec != null) {
                ec.updateStatusUnsync();
                EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
                prevEtl = null;
                while (etl != null) {
                    if (embeddedLanguages == null || embeddedLanguages.contains(etl.languagePath().innerLanguage())) {
                        @SuppressWarnings("unchecked")
                        EmbeddedTokenList<ET> etlUC = (EmbeddedTokenList<ET>)etl;
                        return etlUC;
                    }
                    prevEtl = etl;
                    etl = etl.nextEmbeddedTokenList();
                }
                // Requested etl not found
                if (ec.defaultEmbeddedTokenList() != null) { // Already created or NO_DEFAULT_EMBEDDING
                    return null;
                }

            } else { // No embedding yet
                prevEtl = null;
            }
            
            // If the tokenList is removed then do not create the embedding
            if (tokenList.isRemoved())
                return null;

            // Attempt to create the default embedding
            LanguagePath languagePath = tokenList.languagePath();
            LanguageHierarchy<T> languageHierarchy = LexerUtilsConstants.innerLanguageHierarchy(languagePath);
            @SuppressWarnings("unchecked")
            LanguageEmbedding<ET> embedding = (LanguageEmbedding<ET>) LexerUtilsConstants.findEmbedding(
                    languageHierarchy, token, languagePath, tokenList.inputAttributes());
            if (ep == null) { // Needs to be retrieved to check for CACHED_FIRST_QUERY
                ep = LexerUtilsConstants.innerLanguageOperation(tokenList.languagePath()).
                        embeddingPresence(token.id());
            }
            if (embedding != null) {
                // Update the embedding presence ALWAYS_QUERY
                if (ep == EmbeddingPresence.CACHED_FIRST_QUERY) {
                    LexerUtilsConstants.innerLanguageOperation(tokenList.languagePath()).
                            setEmbeddingPresence(token.id(), EmbeddingPresence.ALWAYS_QUERY);
                }

                // Check whether the token contains enough text to satisfy embedding's start and end skip lengths
                if ((embeddedLanguages != null && !embeddedLanguages.contains(embedding.language())) ||
                    token.isRemoved() || embedding.startSkipLength() + embedding.endSkipLength() > token.length()
                ) {
                    return null;
                }
                if (ec == null) {
                    ec = new EmbeddingContainer<T>(token, rootTokenList);
                    tokenList.wrapToken(index, ec);
                }
                LanguagePath embeddedLanguagePath = LanguagePath.get(languagePath,
                        embedding.language());
                EmbeddedTokenList<ET> etl = new EmbeddedTokenList<ET>(ec,
                        embeddedLanguagePath, embedding);
                // Preceding code should ensure that (prevEtl.nextEmbeddedTokenList == null)
                // so no need to call etl.setNextEmbeddedTokenList(prevEtl.nextEmbeddedTokenList())
                ec.addEmbeddedTokenList(prevEtl, etl, true);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("@@@@@@@@@@ NATURAL-EMBEDDING-CREATED: " + etl.dumpInfo(null) + '\n');
                }

                if (initTokensInNew) {
                    TokenHierarchyOperation operation = rootTokenList.tokenHierarchyOperation();
                    if (embedding.joinSections()) {
                        // Init corresponding TokenListList
                        operation.tokenListList(embeddedLanguagePath);
                    } else { // sections not joined
                        // Check that there is no TLL in this case.
                        // If there would be one it would already have to run through its constructor
                        // which should have collected all the ETLs already (with initTokensInNew==false)
                        // and init tokens explicitly.
                        // Thus the following assert should always pass.
                        assert (operation.existingTokenListList(embeddedLanguagePath) == null);
                        etl.initAllTokens();
                    }
                }
                return etl;
            }
            // Update embedding presence to NONE
            if (ep == EmbeddingPresence.CACHED_FIRST_QUERY) {
                LexerUtilsConstants.innerLanguageOperation(tokenList.languagePath()).
                        setEmbeddingPresence(token.id(), EmbeddingPresence.NONE);
            }
            return null;
        }
    }
    
    /**
     * Create custom embedding.
     *
     * @param tokenList non-null token list in which the token for which the embedding
     *  should be created resides.
     * @param index &gt;=0 index of the token in the token list where the embedding
     *  should be created.
     * @param embeddedLanguage non-null embedded language.
     * @param startSkipLength &gt;=0 number of characters in an initial part of the token
     *  for which the language embedding is being create that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     * @param endSkipLength &gt;=0 number of characters at the end of the token
     *  for which the language embedding is defined that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     */
    public static <T extends TokenId, ET extends TokenId> boolean createEmbedding(
    TokenList<T> tokenList, int index, Language<ET> embeddedLanguage,
    int startSkipLength, int endSkipLength, boolean joinSections) {
        TokenList<?> rootTokenList = tokenList.rootTokenList();
        // Only create embedddings for valid operations so not e.g. for removed token list
        AbstractToken<T> token;
        EmbeddingContainer<T> ec;
        EmbeddedTokenList<ET> etl;
        LanguageEmbedding<ET> embedding;
        TokenHierarchyOperation<?,?> tokenHierarchyOperation;
        int tokenStartOffset;
        TokenHierarchyEventInfo eventInfo;
        synchronized (rootTokenList) {
            // Check TL.isRemoved() under syncing of rootTokenList
            if (tokenList.isRemoved()) // Do not create embedding for removed TLs
                return false;
            // If not removed then THO should be non-null
            tokenHierarchyOperation = tokenList.tokenHierarchyOperation();
            tokenHierarchyOperation.ensureWriteLocked();

            TokenOrEmbedding<T> tokenOrEmbedding = tokenList.tokenOrEmbedding(index);
            ec = tokenOrEmbedding.embedding();
            token = tokenOrEmbedding.token();
            if (ec != null) {
                EmbeddedTokenList etl2 = ec.firstEmbeddedTokenList();
                while (etl2 != null) {
                    if (embeddedLanguage == etl2.languagePath().innerLanguage()) {
                        return false; // already exists
                    }
                    etl2 = etl2.nextEmbeddedTokenList();
                }
            } else {
                if (token.isFlyweight()) { // embedding cannot exist for this flyweight token
                    return false;
                }
                ec = new EmbeddingContainer<T>(token, rootTokenList);
                tokenList.wrapToken(index, ec);
            }
        
            // Token is now wrapped with the EmbeddingContainer and the embedding can be added
            if (startSkipLength + endSkipLength > token.length()) // Check for appropriate size
                return false;
            // Add the new embedding as the first one in the single-linked list
            embedding = LanguageEmbedding.create(embeddedLanguage,
                startSkipLength, endSkipLength, joinSections);
            LanguagePath languagePath = tokenList.languagePath();
            LanguagePath embeddedLanguagePath = LanguagePath.get(languagePath, embeddedLanguage);
            tokenHierarchyOperation.addLanguagePath(embeddedLanguagePath);
            // Make the embedded token list to be the first in the list
            etl = new EmbeddedTokenList<ET>(
                    ec, embeddedLanguagePath, embedding);
            ec.addEmbeddedTokenList(null, etl, false);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("@@@@@@@@@@ EXPLICIT-EMBEDDING-CREATED: " + etl.dumpInfo(null) + '\n');
            }

            
            // Fire the embedding creation to the clients
            // Threading model may need to be changed if necessary
            tokenStartOffset = ec.branchTokenStartOffset();
            eventInfo = new TokenHierarchyEventInfo(
                    tokenHierarchyOperation,
                    TokenHierarchyEventType.EMBEDDING_CREATED,
                    tokenStartOffset, 0, "", 0);
            eventInfo.setMaxAffectedEndOffset(tokenStartOffset + token.length());

            // Check presence of token list list for the embedded language path
            // When joining sections ensure that the token list list gets created
            // and the embedded tokens get created because they must exist
            // before possible next updating of the token list.
            TokenListList<ET> tll = tokenHierarchyOperation.existingTokenListList(etl.languagePath());
            if (!embedding.joinSections()) {
                etl.initAllTokens();
            }
            if (tll != null) {
                // Update tll by embedding creation
                new TokenHierarchyUpdate(eventInfo).updateCreateOrRemoveEmbedding(etl, true);
            } else { // tll == null
                if (embedding.joinSections()) {
                    // Force token list list creation only when joining sections
                    tll = tokenHierarchyOperation.tokenListList(etl.languagePath());
                }
            }
        }

        // Construct outer token change info
        TokenChangeInfo<T> info = new TokenChangeInfo<T>(tokenList);
        info.setIndex(index);
        info.setOffset(tokenStartOffset);
        //info.setAddedTokenCount(0);
        eventInfo.setTokenChangeInfo(info);

        TokenChangeInfo<ET> embeddedInfo = new TokenChangeInfo<ET>(etl);
        embeddedInfo.setIndex(0);
        embeddedInfo.setOffset(tokenStartOffset + embedding.startSkipLength());
        // Should set number of added tokens directly?
        //  - would prevent further lazy embedded lexing so leave to zero for now
        //info.setAddedTokenCount(0);
        info.addEmbeddedChange(embeddedInfo);

        // Fire the change
        tokenHierarchyOperation.fireTokenHierarchyChanged(eventInfo);
        return true;
    }
    
    public static <T extends TokenId, ET extends TokenId> boolean removeEmbedding(
    TokenList<T> tokenList, int index, Language<ET> embeddedLanguage) {
        TokenList<?> rootTokenList = tokenList.rootTokenList();
        // Only create embedddings for valid operations so not e.g. for removed token list
        EmbeddingContainer<T> ec;
        synchronized (rootTokenList) {
            // Check TL.isRemoved() under syncing of rootTokenList
            if (tokenList.isRemoved()) // Do not remove embedding for removed TLs
                return false;
            // If TL is not removed then THO should be non-null
            TokenHierarchyOperation<?,?> tokenHierarchyOperation = tokenList.tokenHierarchyOperation();
            tokenHierarchyOperation.ensureWriteLocked();

            TokenOrEmbedding<T> tokenOrEmbedding = tokenList.tokenOrEmbedding(index);
            ec = tokenOrEmbedding.embedding();
            if (ec != null) {
                ec.updateStatusUnsync();
                EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
                EmbeddedTokenList<?> prevEtl = null;
                while (etl != null) {
                    if (embeddedLanguage == etl.languagePath().innerLanguage()) {
                        // The embedding with the given language exists
                        // Remove it from the chain
                        ec.removeEmbeddedTokenList(prevEtl, etl);
                        // Do not increase the version of the hierarchy since
                        // all the existing token sequences would be invalidated.
                        // Instead invalidate only TSes for the etl only and all its children.
                        // Construct special EC just for the removed token list.
                        ec = new EmbeddingContainer<T>(ec);
                        // State that the removed embedding was not default - should not matter anyway
                        ec.addEmbeddedTokenList(null, etl, false);
                        etl.setEmbeddingContainer(ec);
                        ec.markChildrenRemovedDeep();

                        // Fire the embedding creation to the clients
                        int startOffset = ec.branchTokenStartOffset();
                        TokenHierarchyEventInfo eventInfo = new TokenHierarchyEventInfo(
                                tokenHierarchyOperation,
                                TokenHierarchyEventType.EMBEDDING_REMOVED,
                                startOffset, 0, "", 0
                        
                        
                        
                        );
                        eventInfo.setMaxAffectedEndOffset(startOffset + ec.token().length());
                        // Construct outer token change info
                        TokenChangeInfo<T> info = new TokenChangeInfo<T>(tokenList);
                        info.setIndex(index);
                        info.setOffset(startOffset);
                        //info.setAddedTokenCount(0);
                        eventInfo.setTokenChangeInfo(info);

                        @SuppressWarnings("unchecked")
                        EmbeddedTokenList<ET> etlET = (EmbeddedTokenList<ET>)etl;
                        TokenChangeInfo<ET> embeddedInfo = new TokenChangeInfo<ET>(etlET);
                        embeddedInfo.setIndex(0);
                        embeddedInfo.setOffset(startOffset + etl.embedding().startSkipLength());
                        // For now do not set the removed contents (requires RemovedTokenList)
                        info.addEmbeddedChange(embeddedInfo);

                        // Check for presence of etl in a token list list
                        TokenListList tll = tokenHierarchyOperation.existingTokenListList(etl.languagePath());
                        if (tll != null) {
                            // update-status already called
                            new TokenHierarchyUpdate(eventInfo).updateCreateOrRemoveEmbedding(etl, false);
                        }

                        // Fire the change
                        tokenHierarchyOperation.fireTokenHierarchyChanged(eventInfo);
                        return true;
                    }
                    etl = etl.nextEmbeddedTokenList();
                }
            }
        }
        return false;
    }

    /**
     * Token wrapped by this EC.
     */
    private AbstractToken<T> branchToken; // 12 bytes (8-super + 4)
    
    /**
     * Root token list of the hierarchy should never be null and is final.
     * 
     */
    private final TokenList<?> rootTokenList; // 16 bytes
    
    /**
     * Cached modification count allows to determine whether the start offset
     * needs to be recomputed.
     */
    private int cachedModCount; // 20 bytes

    /**
     * Cached start offset of the token for which this embedding container
     * was created.
     * <br/>
     * Its value may be shared by multiple embedded token lists.
     */
    private int branchTokenStartOffset; // 24 bytes

    /**
     * First embedded token list in the single-linked list.
     */
    private EmbeddedTokenList<?> firstEmbeddedTokenList; // 28 bytes

    /**
     * Embedded token list that represents the default embedding.
     * It may be <code>EmbeddedTokenList.NO_DEFAULT_EMBEDDING</code>
     * for failed attempt to create a default embedding.
     */
    private EmbeddedTokenList<?> defaultEmbeddedTokenList; // 32 bytes
    
    EmbeddingContainer(AbstractToken<T> branchToken, TokenList<?> rootTokenList) {
        if (branchToken == null)
            throw new IllegalArgumentException("branchToken cannot be null");
        if (rootTokenList == null)
            throw new IllegalArgumentException("rootTokenList cannot be null");
        this.branchToken = branchToken;
        this.rootTokenList = rootTokenList;
        // cachedModCount must differ from root's one to sync offsets
        // Root mod count can be >= 0 or -1 for non-incremental token lists
        this.cachedModCount = LexerUtilsConstants.MOD_COUNT_EMBEDDED_INITIAL;
        // Update the tokenStartOffset etc. - this assumes that the token
        // is already parented till the root token list.
        updateStatusUnsync();
    }
    
    /**
     * Constructor used when a custom embedding gets removed.
     * Such removal does not increase token hierarchy version
     * (to not destroy existing token sequences) but need to invalidate
     * token sequences over the removed embedded token list and all its children.
     * <br/>
     * A new special embedding container gets created in such case
     * that will carry null root token since begining and will have a special modCount
     * so that the token sequences become invalid.
     * 
     * @param ec non-null existing embedding container.
     */
    EmbeddingContainer(EmbeddingContainer<T> ec) {
        this(ec.token(), ec.rootTokenList()); // Force init of tokenStartOffset
        markRemoved();
    }
    
    private void markRemoved() {
        // Set cachedModCount to LexerUtilsConstants.MOD_COUNT_REMOVED which should not occur
        // for regular cases which should force existing token sequences to be invalidated.
        cachedModCount = LexerUtilsConstants.MOD_COUNT_REMOVED;
        // Also clear extraModCount of all contained ETLs so that their extraModCount
        // does not increase the cachedModCount in a way that the total count would match
        // some token-sequence's modCount.
        EmbeddedTokenList etl = firstEmbeddedTokenList;
        while (etl != null && etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING) {
            etl.resetExtraModCount();
            etl = etl.nextEmbeddedTokenList();
        }
    }
    
    public void markRemoved(int branchTokenStartOffset) {
        this.branchTokenStartOffset = branchTokenStartOffset;
        markRemoved();
    }
    
    void markChildrenRemovedDeep() { // Used by custom embedding removal
        EmbeddedTokenList etl = firstEmbeddedTokenList;
        while (etl != null && etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING) {
            for (int i = etl.tokenCountCurrent() - 1; i >= 0; i--) {
                EmbeddingContainer ec = etl.tokenOrEmbeddingUnsync(i).embedding();
                if (ec != null) {
                    ec.updateStatusUnsync(); // First update the status
                    ec.markChildrenRemovedDeep();
                    ec.markRemoved(); // Mark removed with the correctly updated offsets
                }
            }
            etl = etl.nextEmbeddedTokenList();
        }
    }
    
    public int cachedModCount() {
        return cachedModCount;
    }
    
    public final AbstractToken<T> token() {
        return branchToken;
    }
    
    public final EmbeddingContainer<T> embedding() {
        return this;
    }
    
    /**
     * Make this container serve a different token.
     * The updateStatusImpl() should be called afterwards to update tokenStartOffset etc.
     */
    public void reinit(AbstractToken<T> token) {
        this.branchToken = token;
        cachedModCount = LexerUtilsConstants.MOD_COUNT_EMBEDDED_INITIAL;
        updateStatusUnsync();
    }
    
    public TokenList<?> rootTokenList() {
        return rootTokenList;
    }
    
    public int branchTokenStartOffset() {
//        checkStatusUpdated();
        return branchTokenStartOffset;
    }
    
    public EmbeddedTokenList<?> firstEmbeddedTokenList() {
        return firstEmbeddedTokenList;
    }
    
    /**
     * Add a new embedded token list to this container.
     * 
     * @param prevEtl token list preceding the place of addition.
     *  Null means that the added one will be first in the chain.
     * @param etl non-null token list to be added.
     * @param defaultEmbedding whether the added etl is default embedding or not.
     */
    public void addEmbeddedTokenList(EmbeddedTokenList<?> prevEtl, EmbeddedTokenList<?> etl, boolean defaultEmbedding) {
        if (prevEtl != null) {
            etl.setNextEmbeddedTokenList(prevEtl.nextEmbeddedTokenList());
            prevEtl.setNextEmbeddedTokenList(etl);
        } else { // prevEtl is null
            etl.setNextEmbeddedTokenList(firstEmbeddedTokenList);
            firstEmbeddedTokenList = etl;
        }
        if (defaultEmbedding) {
            defaultEmbeddedTokenList = etl;
        }
    }
    
    /**
     * Remove embedded token list from this container.
     * Clear reference to next item in the removed token list.
     * 
     * @param prevEtl token list preceding the place of removal.
     *  Null means that the removed one is first in the chain.
     * @param etl non-null token list to be removed.
     * @return next token list linked originally to etl.
     */
    public EmbeddedTokenList<?> removeEmbeddedTokenList(EmbeddedTokenList<?> prevEtl, EmbeddedTokenList<?> etl) {
        EmbeddedTokenList<?> next = etl.nextEmbeddedTokenList();
        if (prevEtl != null) {
            prevEtl.setNextEmbeddedTokenList(next);
        } else {
            firstEmbeddedTokenList = next;
        }
        etl.setNextEmbeddedTokenList(null);
        if (defaultEmbeddedTokenList == etl) {
            defaultEmbeddedTokenList = null;
        }
        return next;
    }
    
    public EmbeddedTokenList<?> defaultEmbeddedTokenList() {
        return defaultEmbeddedTokenList;
    }
    
    /**
     * Check whether this embedding container is no longer present
     * in the token hierarchy.
     * <br/>
     * This method should only be called after updateStatusImpl() was called
     * (it updates rootToken variable).
     */
    public boolean isRemoved() {
//        checkStatusUpdated();
        return (cachedModCount == LexerUtilsConstants.MOD_COUNT_REMOVED);
    }
    
    /**
     * Update status of this container in a synchronized way
     * ensuring that no other thread will interfere - this is suitable
     * for cases when there may be multiple concurrent readers
     * using a token hierarchy and calling Token.offset() for example.
     * <br/>
     * Status updating fixes value of cached start offset of wrapped branch token
     * (calling branch token(s) on upper level(s) for multiple embeddings' nesting).
     * 
     * @return true if token is still part of token hierarchy or false
     *  if it was removed.
     */
    public void updateStatus() {
        synchronized (rootTokenList) {
            updateStatusUnsync();
        }
    }

    /**
     * Unsynced synchronization of container - this method should only be used
     * when there may be only a single thread accessing token hierarchy i.e. during
     * token hierarchy modifications upon mutable input source modifications.
     * 
     * @return true if token is still part of token hierarchy or false
     *  if it was removed.
     * @see #updateStatus()
     */
    public void updateStatusUnsync() {
        updateStatusImpl(rootTokenList.modCount());
    }

    /**
     * Update the status of this embedding container when current mod count
     * of a root token list is given.
     *
     * @param rootModCount modCount of a root token list. The token list either
     *  updates to it or to LexerUtilsConstants.MOD_COUNT_REMOVED if it's removed
     *  from a token hierarchy. If called by nested embeddings they should finally
     *  update to the same value.
     * @return current modCount of this container.
     */
    protected int updateStatusImpl(int rootModCount) {
        if (cachedModCount != LexerUtilsConstants.MOD_COUNT_REMOVED &&
            cachedModCount != rootModCount
        ) {
            TokenList<T> parentTokenList = branchToken.tokenList();
            if (parentTokenList == null) { // branch token removed from its parent token list
                markRemoved();
            } else if (parentTokenList.getClass() == EmbeddedTokenList.class) { // deeper level embedding
                EmbeddedTokenList<T> parentEtl = (EmbeddedTokenList<T>)parentTokenList;
                cachedModCount = parentEtl.embeddingContainer().updateStatusImpl(rootModCount);
                // After status of parent(s) was updated get the current branch token's offset
                branchTokenStartOffset = parentEtl.tokenOffset(branchToken);
            } else { // parent of branch token is a non-null root token list.
                cachedModCount = rootModCount;
                branchTokenStartOffset = parentTokenList.tokenOffset(branchToken);
            }
        }
        return cachedModCount;
    }

    /**
     * Check if this embedding container is up-to-date (updateStatusImpl() was called on it)
     * which is useful for missing-update-status checks.
     * Method declared to return true so it can be used in assert stmts.
     */
    public boolean checkStatusUpdated() {
        if (cachedModCount != LexerUtilsConstants.MOD_COUNT_REMOVED
                && cachedModCount != rootTokenList.modCount()
                && !checkStatusUpdatedThrowingException
        ) {
            // Prevent OOME because of nested throwing of exc
            checkStatusUpdatedThrowingException = true;
            String excMsg = "!!!INTERNAL ERROR!!! Status not updated on " +
                    this + "\nin token hierarchy\n" + rootTokenList.tokenHierarchyOperation();
            checkStatusUpdatedThrowingException = false;
            throw new IllegalStateException(excMsg);
        }
        return true;
    }
    private static boolean checkStatusUpdatedThrowingException;

}
