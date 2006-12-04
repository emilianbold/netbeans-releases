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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.inc.TokenChangeInfo;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.lib.lexer.token.AbstractToken;


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
    public static <T extends TokenId, ET extends TokenId> EmbeddedTokenList<ET> getEmbedding(
    TokenList<T> tokenList, int index, Language<ET> language) {
        EmbeddingContainer<T> ec;
        AbstractToken<T> token;
        EmbeddedTokenList<? extends TokenId> lastEtl = null;
        synchronized (tokenList.root()) {
            Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(index);
            if (tokenOrEmbeddingContainer.getClass() == EmbeddingContainer.class) {
                // Embedding container exists
                @SuppressWarnings("unchecked")
                EmbeddingContainer<T> ecUC = (EmbeddingContainer<T>)tokenOrEmbeddingContainer;
                ec = ecUC;
                ec.updateOffsets();

                EmbeddedTokenList<? extends TokenId> etl = ec.firstEmbedding();
                while (etl != null) {
                    if (language == null || etl.languagePath().innerLanguage() == language) {
                        @SuppressWarnings("unchecked")
                        EmbeddedTokenList<ET> etlUC = (EmbeddedTokenList<ET>)etl;
                        return etlUC;
                    }
                    lastEtl = etl;
                    etl = etl.nextEmbedding();
                }
                token = ec.token();
            } else {
                ec = null;
                @SuppressWarnings("unchecked")
                AbstractToken<T> t = (AbstractToken<T>)tokenOrEmbeddingContainer;
                token = t;
                if (token.isFlyweight()) { // embedding cannot exist for this flyweight token
                    return null;
                }
            }

            // Attempt to find default embedding
            LanguagePath languagePath = tokenList.languagePath();
            @SuppressWarnings("unchecked")
            LanguageEmbedding<ET> embedding = (LanguageEmbedding<ET>) LexerUtilsConstants.findEmbedding(
                    token, languagePath, tokenList.inputAttributes());
            if (embedding != null && (language == null || language == embedding.language())) {
                if (ec == null) {
                    ec = new EmbeddingContainer<T>(token);
                    tokenList.wrapToken(index, ec);
                }
                LanguagePath embeddedLanguagePath = LanguagePath.get(languagePath,
                        embedding.language());
                EmbeddedTokenList<ET> etl = new EmbeddedTokenList<ET>(ec,
                        embeddedLanguagePath, embedding, null);
                if (lastEtl != null)
                    lastEtl.setNextEmbedding(etl);
                else
                    ec.setFirstEmbedding(etl);
                return etl;
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
        synchronized (tokenList.root()) {
            TokenHierarchyOperation<?,?> tokenHierarchyOperation = tokenList.tokenHierarchyOperation();
            // Only create embedddings for valid operations so not e.g. for removed token list
            if (tokenHierarchyOperation == null) {
                return false;
            }
            Object tokenOrEmbeddingContainer = tokenList.tokenOrEmbeddingContainer(index);
            EmbeddingContainer<T> ec;
            AbstractToken<T> token;
            if (tokenOrEmbeddingContainer.getClass() == EmbeddingContainer.class) {
                // Embedding container exists
                @SuppressWarnings("unchecked")
                EmbeddingContainer<T> ecUC = (EmbeddingContainer<T>)tokenOrEmbeddingContainer;
                ec = ecUC;
                EmbeddedTokenList<? extends TokenId> etl = ec.firstEmbedding();
                while (etl != null) {
                    if (embeddedLanguage == etl.languagePath().innerLanguage()) {
                        return false; // already exists
                    }
                    etl = etl.nextEmbedding();
                }
                token = ec.token();
            } else {
                @SuppressWarnings("unchecked")
                AbstractToken<T> t = (AbstractToken<T>)tokenOrEmbeddingContainer;
                token = t;
                if (token.isFlyweight()) { // embedding cannot exist for this flyweight token
                    return false;
                }
                ec = new EmbeddingContainer<T>(token);
                tokenList.wrapToken(index, ec);
            }

            // Add the new embedding as the first one in the single-linked list
            LanguageEmbedding<ET> embedding = LanguageEmbedding.create(embeddedLanguage,
                startSkipLength, endSkipLength, joinSections);
            LanguagePath languagePath = tokenList.languagePath();
            LanguagePath embeddedLanguagePath = LanguagePath.get(languagePath, embeddedLanguage);
            // Make the embedded token list to be the first in the list
            EmbeddedTokenList<ET> etl = new EmbeddedTokenList<ET>(
                    ec, embeddedLanguagePath, embedding, ec.firstEmbedding());
            ec.setFirstEmbedding(etl);
            // Increment mod count? - not in this case

            // Fire the embedding creation to the clients
            // Threading model may need to be changed if necessary
            int aOffset = ec.tokenStartOffset();
            TokenHierarchyEventInfo eventInfo = new TokenHierarchyEventInfo(
                    tokenHierarchyOperation,
                    TokenHierarchyEventType.EMBEDDING,
                    aOffset, 0, "", 0
            );
            eventInfo.setAffectedStartOffset(aOffset);
            eventInfo.setAffectedEndOffset(aOffset + token.length());
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
        }
        return true;
    }

    private final AbstractToken<T> token; // 12 bytes (8-super + 4)
    
    /**
     * Cached modification count allows to determine whether the start offset
     * needs to be recomputed.
     */
    private int cachedModCount; // 16 bytes

    /**
     * For mutable environment this field contains root token list of the hierarchy.
     * 
     */
    private final TokenList<? extends TokenId> rootTokenList; // 20 bytes
    
    /**
     * The token in the root token list to which this embedding container relates.
     * <br/>
     * For first-level embedding it is the same like value of branchToken variable
     * but for deeper embeddings it points to the corresponding branch token
     * in the root token list.
     * <br/>
     * It's used for getting of the start offset of the contained tokens
     * and for getting of their text.
     */
    private final AbstractToken<? extends TokenId> rootToken; // 24 bytes
    
    /**
     * Cached start offset of the token for which this embedding container
     * was created.
     */
    private int tokenStartOffset; // 28 bytes

    /**
     * First embedded token list in the single-linked list.
     */
    private EmbeddedTokenList<? extends TokenId> firstEmbedding; // 32 bytes

    /**
     * Difference between start offset of the first token in this token list
     * against the start offset of the root token.
     * <br>
     * The offset gets refreshed upon <code>updateStartOffset()</code>.
     */
    private int rootTokenOffsetShift; // 52 bytes


    public EmbeddingContainer(AbstractToken<T> token) {
        this.token = token;
        TokenList<T> embeddedTokenList = token.tokenList();
        this.rootTokenList = embeddedTokenList.root();
        this.rootToken = (embeddedTokenList.getClass() == EmbeddedTokenList.class)
                ? ((EmbeddedTokenList<? extends TokenId>)embeddedTokenList).rootToken()
                : token;
        this.cachedModCount = -2; // must differ from root's one to sync offsets
        updateOffsets();
    }

    public void updateOffsets() {
        synchronized (rootTokenList) {
            if (cachedModCount != rootTokenList.modCount()) {
                cachedModCount = rootTokenList.modCount();
                tokenStartOffset = token.offset(null);
                rootTokenOffsetShift = tokenStartOffset - rootToken.offset(null);
            }
        }
    }

    public AbstractToken<T> token() {
        return token;
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
        return rootTokenOffsetShift;
    }
    
    public char charAt(int tokenRelOffset) {
        return rootToken.charAt(rootTokenOffsetShift + tokenRelOffset);
    }
    
    public EmbeddedTokenList<? extends TokenId> firstEmbedding() {
        return firstEmbedding;
    }
    
    void setFirstEmbedding(EmbeddedTokenList<? extends TokenId> firstEmbedding) {
        this.firstEmbedding = firstEmbedding;
    }

}
