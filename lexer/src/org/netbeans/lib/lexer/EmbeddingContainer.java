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
    
    /** Flag for additional correctness checks (may degrade performance). */
    private static final boolean testing = Boolean.getBoolean("netbeans.debug.lexer.test");
    
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
        TokenList<?> root = tokenList.root();
        synchronized (root) {
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
                if (ec == null) {
                    ec = new EmbeddingContainer<T>(token, root);
                    tokenList.wrapToken(index, ec);
                }
                LanguagePath embeddedLanguagePath = LanguagePath.get(languagePath,
                        embedding.language());
                // When joining sections ensure that the token list list gets created
                // Even possibly existing token list list needs to be marked as mandatory
                // since there is at least one embedding that joins the sections.
                TokenHierarchyOperation hi;
                if (embedding.joinSections() && (hi = tokenList.tokenHierarchyOperation()) != null) {
                    TokenListList tll = hi.tokenListList(embeddedLanguagePath);
                    tll.setJoinSections(true);
                }
                EmbeddedTokenList<ET> etl = new EmbeddedTokenList<ET>(ec,
                        embeddedLanguagePath, embedding, null);
                if (prevEtl != null) {
                    prevEtl.setNextEmbeddedTokenList(etl);
                } else {
                    ec.setFirstEmbeddedTokenList(etl);
                }
                ec.setDefaultEmbeddedTokenList(etl);
                if (ep == EmbeddingPresence.CACHED_FIRST_QUERY) {
                    LexerUtilsConstants.innerLanguageOperation(tokenList.languagePath()).
                            setEmbeddingPresence(token.id(), EmbeddingPresence.ALWAYS_QUERY);
                }
                return (embeddedLanguage == null || embeddedLanguage == embedding.language()) ? etl : null;
            }
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
        TokenList<? extends TokenId> root = tokenList.root();
        // Only create embedddings for valid operations so not e.g. for removed token list
        Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(index);
        AbstractToken<T> token;
        EmbeddingContainer<T> ec;
        synchronized (root) {
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
                ec = new EmbeddingContainer<T>(token, root);
                tokenList.wrapToken(index, ec);
            }
        }
        
        // Token is now wrapped with the EmbeddingContainer and the embedding can be added

        EmbeddedTokenList<ET> etl;
        LanguageEmbedding<ET> embedding;
        synchronized (root) {
            if (startSkipLength + endSkipLength > token.length()) // Check for appropriate size
                return false;
            // Add the new embedding as the first one in the single-linked list
            embedding = LanguageEmbedding.create(embeddedLanguage,
                startSkipLength, endSkipLength, joinSections);
            LanguagePath languagePath = tokenList.languagePath();
            LanguagePath embeddedLanguagePath = LanguagePath.get(languagePath, embeddedLanguage);
            tokenHierarchyOperation.addLanguagePath(embeddedLanguagePath);
            // When joining sections ensure that the token list list gets created
            // Even possibly existing token list list needs to be marked as mandatory
            // since there is at least one embedding that joins the sections.
            if (embedding.joinSections()) {
                TokenListList tll = tokenHierarchyOperation.tokenListList(embeddedLanguagePath);
                tll.setJoinSections(true);
            }
            // Make the embedded token list to be the first in the list
            etl = new EmbeddedTokenList<ET>(
                    ec, embeddedLanguagePath, embedding, ec.firstEmbeddedTokenList());
            ec.setFirstEmbeddedTokenList(etl);
            // Increment mod count and since the addition may produce tokens
            // for joined sections then this needs to be processed
            // similarly as a regular text modification.
            // TBD - implement embedded sections handling
        }

        // Fire the embedding creation to the clients
        // Threading model may need to be changed if necessary
        int aOffset = ec.tokenStartOffset();
        TokenHierarchyEventInfo eventInfo = new TokenHierarchyEventInfo(
                tokenHierarchyOperation,
                TokenHierarchyEventType.EMBEDDING,
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

        // Fire the change
        tokenHierarchyOperation.fireTokenHierarchyChanged(
                    LexerApiPackageAccessor.get().createTokenChangeEvent(eventInfo));
        return true;
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
    
    public EmbeddingContainer(AbstractToken<T> token, TokenList<?> rootTokenList) {
        this.token = token;
        this.rootTokenList = rootTokenList;
        this.rootToken = token; // Has to be non-null since updateStatusImpl() would not update null rootToken
        // cachedModCount must differ from root's one to sync offsets
        // Root mod count can be >= 0 or -1 for non-incremental token lists
        // It also cannot be -2 which means that this container is no longer
        // attached to the token hierarchy.
        this.cachedModCount = -3;
        // Update the tokenStartOffset etc. - this assumes that the token
        // is already parented till the root token list.
        updateStatusImpl();
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
    
    void setFirstEmbeddedTokenList(EmbeddedTokenList<? extends TokenId> firstEmbeddedTokenList) {
        this.firstEmbeddedTokenList = firstEmbeddedTokenList;
    }
    
    public EmbeddedTokenList<? extends TokenId> defaultEmbeddedTokenList() {
        return defaultEmbeddedTokenList;
    }
    
    void setDefaultEmbeddedTokenList(EmbeddedTokenList<? extends TokenId> defaultEmbeddedTokenList) {
        this.defaultEmbeddedTokenList = defaultEmbeddedTokenList;
    }
    
    public boolean updateStatus() {
        synchronized (rootTokenList) {
            return (updateStatusImpl() != null);
        }
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
