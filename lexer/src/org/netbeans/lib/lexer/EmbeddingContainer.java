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

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.inc.TokenChangeInfo;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.lib.lexer.token.AbstractToken;
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

public final class EmbeddingContainer<T extends TokenId> {
    
    /**
     * Get embedded token list.
     *
     * @param tokenList non-null token list in which the token for which the embedding
     *  should be obtained resides.
     * @param index &gt;=0 index of the token in the token list where the embedding
     *  should be obtained.
     * @param language whether only language embeddding of the particular language
     *  was requested. It may be null if any embedding should be returned.
     */
    public static <T extends TokenId, ET extends TokenId> EmbeddedTokenList<ET> embeddedTokenList(
    TokenList<T> tokenList, int index, Language<ET> embeddedLanguage) {
        EmbeddingContainer<T> ec;
        AbstractToken<T> token;
        Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(index);
        EmbeddingPresence ep;
        if (tokenOrEmbeddingContainer.getClass() == EmbeddingContainer.class) {
            // Embedding container exists
            @SuppressWarnings("unchecked")
            EmbeddingContainer<T> ecUC = (EmbeddingContainer<T>)tokenOrEmbeddingContainer;
            ec = ecUC;
            token = ec.token();
            ep = null;

        } else { // No embedding was created yet
            ec = null;
            @SuppressWarnings("unchecked")
            AbstractToken<T> t = (AbstractToken<T>)tokenOrEmbeddingContainer;
            token = t;
            // Check embedding presence
            ep = LexerUtilsConstants.innerLanguageOperation(tokenList.languagePath()).embeddingPresence(token.id());
            if (ep == EmbeddingPresence.NONE) {
                return null;
            }
        }

        // Now either ec == null for no embedding yet or linked list of embedded token lists of ec
        // need to be processed to find the embedded token list for requested language.
        TokenList<?> rootTokenList = tokenList.root();
        synchronized (rootTokenList) {
            EmbeddedTokenList<? extends TokenId> prevEtl;
            if (ec != null) {
                ec.updateStatusImpl();
                EmbeddedTokenList<? extends TokenId> etl = ec.firstEmbeddedTokenList();
                prevEtl = null;
                while (etl != null) {
                    if (embeddedLanguage == null || etl.languagePath().innerLanguage() == embeddedLanguage) {
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
                CharSequence text = token.text(); // Should not be null here but rather check
                if (text == null || embedding.startSkipLength() + embedding.endSkipLength() > text.length()) {
                    return null;
                }
                if (ec == null) {
                    ec = new EmbeddingContainer<T>(token, rootTokenList);
                    tokenList.wrapToken(index, ec);
                }
                LanguagePath embeddedLanguagePath = LanguagePath.get(languagePath,
                        embedding.language());
                EmbeddedTokenList<ET> etl = new EmbeddedTokenList<ET>(ec,
                        embeddedLanguagePath, embedding, null);
                // Preceding code should ensure that (prevEtl.nextEmbeddedTokenList == null)
                // so no need to call etl.setNextEmbeddedTokenList(prevEtl.nextEmbeddedTokenList())
                ec.addEmbeddedTokenList(prevEtl, etl, true);
                return (embeddedLanguage == null || embeddedLanguage == embedding.language()) ? etl : null;
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
        TokenHierarchyOperation<?,?> tokenHierarchyOperation = tokenList.tokenHierarchyOperation();
        if (tokenHierarchyOperation == null) {
            return false;
        }
        TokenList<?> rootTokenList = tokenList.root();
        // Only create embedddings for valid operations so not e.g. for removed token list
        Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(index);
        AbstractToken<T> token;
        EmbeddingContainer<T> ec;
        synchronized (rootTokenList) {
            if (tokenOrEmbeddingContainer.getClass() == EmbeddingContainer.class) {
                // Embedding container exists
                @SuppressWarnings("unchecked")
                EmbeddingContainer<T> ecUC = (EmbeddingContainer<T>)tokenOrEmbeddingContainer;
                ec = ecUC;
                EmbeddedTokenList<? extends TokenId> etl = ec.firstEmbeddedTokenList();
                while (etl != null) {
                    if (embeddedLanguage == etl.languagePath().innerLanguage()) {
                        return false; // already exists
                    }
                    etl = etl.nextEmbeddedTokenList();
                }
                token = ec.token();
            } else {
                @SuppressWarnings("unchecked")
                AbstractToken<T> t = (AbstractToken<T>)tokenOrEmbeddingContainer;
                token = t;
                if (token.isFlyweight()) { // embedding cannot exist for this flyweight token
                    return false;
                }
                ec = new EmbeddingContainer<T>(token, rootTokenList);
                tokenList.wrapToken(index, ec);
            }
        }
        
        // Token is now wrapped with the EmbeddingContainer and the embedding can be added

        EmbeddedTokenList<ET> etl;
        LanguageEmbedding<ET> embedding;
        synchronized (rootTokenList) {
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
                    ec, embeddedLanguagePath, embedding, ec.firstEmbeddedTokenList());
            ec.addEmbeddedTokenList(null, etl, false);
        }

        // Fire the embedding creation to the clients
        // Threading model may need to be changed if necessary
        int aOffset = ec.tokenStartOffset();
        TokenHierarchyEventInfo eventInfo = new TokenHierarchyEventInfo(
                tokenHierarchyOperation,
                TokenHierarchyEventType.EMBEDDING_CREATED,
                aOffset, 0, "", 0
        
        
        
        );
        eventInfo.setMaxAffectedEndOffset(aOffset + token.length());
        // Construct outer token change info
        TokenChangeInfo<T> info = new TokenChangeInfo<T>(tokenList);
        info.setIndex(index);
        info.setOffset(aOffset);
        //info.setAddedTokenCount(0);
        eventInfo.setTokenChangeInfo(info);

        TokenChangeInfo<ET> embeddedInfo = new TokenChangeInfo<ET>(etl);
        embeddedInfo.setIndex(0);
        embeddedInfo.setOffset(aOffset + embedding.startSkipLength());
        // Should set number of added tokens directly?
        //  - would prevent further lazy embedded lexing so leave to zero for now
        //info.setAddedTokenCount(0);
        info.addEmbeddedChange(embeddedInfo);

        // Check presence of token list list for the embedded language path
        // When joining sections ensure that the token list list gets created
        // and the embedded tokens get created because they must exist
        // before possible next updating of the token list.
        TokenListList tll = tokenHierarchyOperation.existingTokenListList(etl.languagePath());
        if (tll != null) {
            // Update tll by embedding creation
            new TokenHierarchyUpdate(eventInfo).updateCreateEmbedding(etl);
        } else { // tll == null
            if (embedding.joinSections()) {
                // Force token list list creation only when joining sections
                tll = tokenHierarchyOperation.tokenListList(etl.languagePath());
            }
        }

        // Fire the change
        tokenHierarchyOperation.fireTokenHierarchyChanged(
                    LexerApiPackageAccessor.get().createTokenChangeEvent(eventInfo));
        return true;
    }
    
    public static <T extends TokenId, ET extends TokenId> boolean removeEmbedding(
    TokenList<T> tokenList, int index, Language<ET> embeddedLanguage) {
        TokenHierarchyOperation<?,?> tokenHierarchyOperation = tokenList.tokenHierarchyOperation();
        if (tokenHierarchyOperation == null) {
            return false;
        }
        TokenList<? extends TokenId> rootTokenList = tokenList.root();
        // Only create embedddings for valid operations so not e.g. for removed token list
        Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(index);
        EmbeddingContainer<T> ec;
        synchronized (rootTokenList) {
            if (tokenOrEmbeddingContainer.getClass() == EmbeddingContainer.class) {
                // Embedding container exists
                @SuppressWarnings("unchecked")
                EmbeddingContainer<T> ecUC = (EmbeddingContainer<T>)tokenOrEmbeddingContainer;
                ec = ecUC;
                ec.updateStatusImpl();
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
                        ec.invalidateChildren();

                        // Fire the embedding creation to the clients
                        int startOffset = ec.tokenStartOffset();
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
                            new TokenHierarchyUpdate(eventInfo).updateRemoveEmbedding(etl);
                        }

                        // Fire the change
                        tokenHierarchyOperation.fireTokenHierarchyChanged(
                                    LexerApiPackageAccessor.get().createTokenChangeEvent(eventInfo));
                        return true;
                    }
                    etl = etl.nextEmbeddedTokenList();
                }
            }
        }
        return false;
    }

    private AbstractToken<T> token; // 12 bytes (8-super + 4)
    
    /**
     * Cached modification count allows to determine whether the start offset
     * needs to be recomputed.
     */
    private int cachedModCount; // 16 bytes

    /**
     * Root token list of the hierarchy.
     * 
     */
    private final TokenList<? extends TokenId> rootTokenList; // 20 bytes
    
    /**
     * The root embedding container to which this embedding container relates.
     * <br/>
     * It's used for getting of the start offset of the contained tokens
     * and for getting of their text.
     */
    private AbstractToken<? extends TokenId> rootToken; // 24 bytes
    
    /**
     * Cached start offset of the token for which this embedding container
     * was created.
     */
    private int tokenStartOffset; // 28 bytes

    /**
     * First embedded token list in the single-linked list.
     */
    private EmbeddedTokenList<? extends TokenId> firstEmbeddedTokenList; // 32 bytes

    /**
     * Difference between start offset of the first token in this token list
     * against the start offset of the root token.
     * <br>
     * The offset gets refreshed upon <code>updateStartOffset()</code>.
     */
    private int offsetShiftFromRootToken; // 36 bytes
    
    /**
     * Embedded token list that represents the default embedding.
     * It may be <code>EmbeddedTokenList.NO_DEFAULT_EMBEDDING</code>
     * for failed attempt to create a default embedding.
     */
    private EmbeddedTokenList<? extends TokenId> defaultEmbeddedTokenList; // 40 bytes
    
    EmbeddingContainer(AbstractToken<T> token, TokenList<?> rootTokenList) {
        this.token = token;
        this.rootTokenList = rootTokenList;
        this.rootToken = token; // Has to be non-null since updateStatusImpl() would not update null rootToken
        // cachedModCount must differ from root's one to sync offsets
        // Root mod count can be >= 0 or -1 for non-incremental token lists
        this.cachedModCount = -2;
        // Update the tokenStartOffset etc. - this assumes that the token
        // is already parented till the root token list.
        updateStatusImpl();
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
        this(ec.token(), ec.rootTokenList()); // Force init of tokenStartOffset and rootTokenOffsetShift
        invalidate();
    }
    
    private void invalidate() {
        this.rootToken = null;
        // Set cachedModCount to -2 which should not occur for regular cases
        // which should force existing token sequences to be invalidated.
        this.cachedModCount = -2;
    }
    
    void invalidateChildren() {
        EmbeddedTokenList etl = firstEmbeddedTokenList;
        while (etl != null && etl != EmbeddedTokenList.NO_DEFAULT_EMBEDDING) {
            for (int i = etl.tokenCountCurrent() - 1; i >= 0; i--) {
                Object tokenOrEC = etl.tokenOrEmbeddingContainerUnsync(i);
                if (tokenOrEC.getClass() == EmbeddingContainer.class) {
                    ((EmbeddingContainer)tokenOrEC).invalidateChildren();
                }
            }
            etl = etl.nextEmbeddedTokenList();
        }
    }
    
    public int cachedModCount() {
        return cachedModCount;
    }

    public AbstractToken<T> token() {
        return token;
    }
    
    /**
     * Make this container serve a different token.
     * The updateStatusImpl() should be called afterwards to update tokenStartOffset etc.
     */
    public void setToken(AbstractToken<T> token) {
        this.token = token;
    }
    
    public TokenList<? extends TokenId> rootTokenList() {
        return rootTokenList;
    }
    
    public AbstractToken<? extends TokenId> rootToken() {
        return rootToken;
    }

    public int tokenStartOffset() {
        return tokenStartOffset;
    }
    
    public int rootTokenOffsetShift() {
        return offsetShiftFromRootToken;
    }
    
    public char charAt(int tokenRelOffset) {
        return rootToken.charAt(offsetShiftFromRootToken + tokenRelOffset);
    }
    
    public EmbeddedTokenList<? extends TokenId> firstEmbeddedTokenList() {
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
    
    public EmbeddedTokenList<? extends TokenId> defaultEmbeddedTokenList() {
        return defaultEmbeddedTokenList;
    }
    
    public boolean updateStatus() {
        synchronized (rootTokenList) {
            return (updateStatusImpl() != null);
        }
    }
    
    /**
     * Check whether this embedding container is no longer present
     * in the token hierarchy.
     * <br/>
     * This method should only be called after updateStatusImpl() was called
     * (it updates rootToken variable).
     */
    public boolean isRemoved() {
        return (rootToken == null);
    }
    
    /**
     * Update and return root token corresponding to this embedding container.
     */
    AbstractToken<? extends TokenId> updateStatusImpl() {
        if (rootToken == null)
            return null; // Removed from hierarchy
        int rootModCount;
        if (cachedModCount != (rootModCount = rootTokenList.modCount())) {
            cachedModCount = rootModCount;
            TokenList<?> tl = token.tokenList();
            if (tl == null) {
                rootToken = null;
            } else if (tl.getClass() == EmbeddedTokenList.class) {
                EmbeddedTokenList<?> etl = (EmbeddedTokenList<?>)tl;
                rootToken = etl.embeddingContainer().updateStatusImpl();
                if (rootToken != null) {
                    tokenStartOffset = etl.childTokenOffsetNoUpdate(token.rawOffset());
                    offsetShiftFromRootToken = tokenStartOffset - rootToken.offset(null);
                }
            } else { // parent is a root token list: rootToken == token
                rootToken = token;
                tokenStartOffset = token.offset(null);
                offsetShiftFromRootToken = 0;
            }
        }
        return rootToken;
    }

}
