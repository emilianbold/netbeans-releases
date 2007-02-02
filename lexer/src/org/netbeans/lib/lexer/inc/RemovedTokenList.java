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
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.EmbeddingContainer;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.TextToken;

/**
 * Token list implementation holding added or removed tokens from a list.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class RemovedTokenList<T extends TokenId> implements TokenList<T> {
    
    private final LanguagePath languagePath;
    
    private Object[] tokensOrBranches;
    
    private int removedTokensStartOffset;
    
    public RemovedTokenList(LanguagePath languagePath, Object[] tokensOrBranches) {
        this.languagePath = languagePath;
        this.tokensOrBranches = tokensOrBranches;
    }
    
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    public Object tokenOrEmbeddingContainer(int index) {
        return (index < tokensOrBranches.length) ? tokensOrBranches[index] : null;
    }

    public int lookahead(int index) {
        return -1;
    }

    public Object state(int index) {
        return null;
    }

    public int tokenOffset(int index) {
        Token<? extends TokenId> token = existingToken(index);
        if (token.isFlyweight()) {
            int offset = 0;
            while (--index >= 0) {
                token = existingToken(index);
                offset += token.length();
                if (!token.isFlyweight()) {
                    // Return from here instead of break; - see code after while()
                    return offset + token.offset(null);
                }
            }
            // might remove token sequence starting with flyweight
            return removedTokensStartOffset + offset;

        } else { // non-flyweight offset
            return token.offset(null);
        }
    }

    private Token<T> existingToken(int index) {
        return LexerUtilsConstants.token(tokensOrBranches[index]);
    }

    public synchronized AbstractToken<T> replaceFlyToken(
    int index, AbstractToken<T> flyToken, int offset) {
        TextToken<T> nonFlyToken = ((TextToken<T>)flyToken).createCopy(this, offset);
        tokensOrBranches[index] = nonFlyToken;
        return nonFlyToken;
    }

    public int tokenCount() {
        return tokenCountCurrent();
    }

    public int tokenCountCurrent() {
        return tokensOrBranches.length;
    }

    public int modCount() {
        return -1;
    }
    
    public int childTokenOffset(int rawOffset) {
        // Offsets of contained tokens are absolute
        return rawOffset;
    }
    
    public char childTokenCharAt(int rawOffset, int index) {
        throw new IllegalStateException("Querying of text for removed tokens not supported"); // NOI18N
    }

    public void wrapToken(int index, EmbeddingContainer embeddingContainer) {
        throw new IllegalStateException("Branching of removed tokens not supported"); // NOI18N
    }
    
    public TokenList<? extends TokenId> root() {
        return this;
    }
    
    public TokenHierarchyOperation<?,? extends TokenId> tokenHierarchyOperation() {
        return null;
    }
    
    public InputAttributes inputAttributes() {
        return null;
    }

    public boolean isContinuous() {
        return true;
    }

    public Set<T> skipTokenIds() {
        return null;
    }

}
