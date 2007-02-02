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

import java.util.Set;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.EmbeddingContainer;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Filtering token list for token hierarchy snapshots.
 * <br/>
 * It holds an offset diff between offset of a token related to particular snapshot
 * and a "natural" offset of a token (related to null token hierarchy).
 * <br/>
 * For non-snapshots it's always zero.
 * <br/>
 * It's used for token sequences over embedded token lists because
 * they are not SnapshotTokenList (only used for root token list) instances
 * so the embedded token lists need an extra relocation.
 *
 * <p>
 * It also ensures that the modCount will be -1 to eliminate up-to-date checking
 * for snapshot embedded branches.
 * </p>
 *
 * <p>
 * This list assumes single-threaded use only.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FilterSnapshotTokenList<T extends TokenId> implements TokenList<T> {
    
    /** Original token list. */
    private TokenList<T> tokenList;
    
    /**
     * Difference of the offsets retrieved from tokenList.offset(index)
     * from the reality - there is a non-zero shift because of the snapshot use.
     */
    private int tokenOffsetDiff;
    
    public FilterSnapshotTokenList(TokenList<T> tokenList, int tokenOffsetDiff) {
        this.tokenList = tokenList;
        this.tokenOffsetDiff = tokenOffsetDiff;
    }
    
    public TokenList delegate() {
        return tokenList;
    }
    
    public int tokenOffsetDiff() {
        return tokenOffsetDiff;
    }
    
    public Object tokenOrEmbeddingContainer(int index) {
        return tokenList.tokenOrEmbeddingContainer(index);
    }

    public AbstractToken<T> replaceFlyToken(int index, AbstractToken<T> flyToken, int offset) {
        return tokenList.replaceFlyToken(index, flyToken, offset);
    }

    public int tokenOffset(int index) {
        return tokenOffsetDiff + tokenList.tokenOffset(index);
    }

    public int modCount() {
        return -1;
    }

    public int tokenCount() {
        return tokenList.tokenCount();
    }

    public int tokenCountCurrent() {
        return tokenList.tokenCountCurrent();
    }

    public LanguagePath languagePath() {
        return tokenList.languagePath();
    }

    public int childTokenOffset(int rawOffset) {
        throw new IllegalStateException("Unexpected call.");
    }

    public char childTokenCharAt(int rawOffset, int index) {
        throw new IllegalStateException("Unexpected call.");
    }

    public void wrapToken(int index, EmbeddingContainer<T> embeddingContainer) {
        tokenList.wrapToken(index, embeddingContainer);
    }

    public TokenList<? extends TokenId> root() {
        return tokenList.root();
    }
    
    public TokenHierarchyOperation<?,? extends TokenId> tokenHierarchyOperation() {
        return tokenList.tokenHierarchyOperation();
    }
    
    public InputAttributes inputAttributes() {
        return tokenList.inputAttributes();
    }

    public int lookahead(int index) {
        // Can be used by LexerTestUtilities.lookahead()
        return tokenList.lookahead(index);
    }

    public Object state(int index) {
        return tokenList.state(index);
    }

    public boolean isContinuous() {
        return tokenList.isContinuous();
    }

    public Set<T> skipTokenIds() {
        return tokenList.skipTokenIds();
    }
    
}