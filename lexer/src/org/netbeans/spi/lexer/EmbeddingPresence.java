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

package org.netbeans.spi.lexer;

/**
 * Defines whether a default embedding can be present for the given token id or not.
 * <br/>
 * It allows to speed up <code>TokenSequence.embedded()</code> calls considerably in most cases.
 * <br/>
 * This only affects the default embedding creation. Custom embedding creation
 * can always be performed by <code>TokenSequene.createEmbedding()</code>.
 *
 * @author Miloslav Metelka
 */

public enum EmbeddingPresence {

    /**
     * Creation of the default embedding for the particular {@link org.netbeans.api.lexer.TokenId}
     * will be attempted for the first time but if there will be no embedding 
     * created then there will be no other attempts for embedding creation
     * for any tokens with the same token id.
     * <br/>
     * This corresponds to the most usual case that the embedding presence
     * only depends on a token id.
     * <br/>
     * This is the default for {@link LanguageHierarchy#embeddingPresence(org.netbeans.api.lexer.TokenId)}.
     */
    CACHED_FIRST_QUERY,
    
    /**
     * Default embedding creation will always be attempted for each token since
     * the embedding presence varies (it may depend on token's text or other token properties).
     * <br/>
     * For example if a string literal token would only qualify for an embedding
     * if it would contain a '\' character but not otherwise then this method
     * should return true for string literal token id.
     * <br/>
     * This option presents no performance improvement.
     */
    ALWAYS_QUERY,

    /**
     * There is no default embedding for the given {@link org.netbeans.api.lexer.TokenId}
     * and its creation will not be attempted.
     * <br/>
     * This is useful e.g. for keywords and operators.
     */
    NONE,

}
