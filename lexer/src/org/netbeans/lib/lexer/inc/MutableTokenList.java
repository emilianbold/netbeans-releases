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

package org.netbeans.lib.lexer.inc;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.TokenList;

/**
 * Token list that allows mutating by token list mutator.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface MutableTokenList<T extends TokenId> extends TokenList<T> {

    /**
     * Return token or branch token list at the requested index
     * but do not synchronize the access - there should only be one thread
     * accessing the token list at this time.
     * Also do not perform any checks regarding index validity
     * - only items below {@link #tokenCountCurrent()} will be requested.
     */
    Object tokenOrEmbeddingContainerUnsync(int index);
    
    /**
     * Create lexer input operation used for relexing of the input.
     */
    LexerInputOperation<T> createLexerInputOperation(
    int tokenIndex, int relexOffset, Object relexState);
    
    /**
     * Check whether the whole input was tokenized or not.
     * <br/>
     * Incremental algorithm uses this information to determine
     * whether it should relex the input till the end or not.
     */
    boolean isFullyLexed();
    
    /**
     * Update the token list by replacing tokens according to the given change.
     */
    void replaceTokens(TokenListChange<T> change, int removeTokenCount, int diffLength);
    
}
