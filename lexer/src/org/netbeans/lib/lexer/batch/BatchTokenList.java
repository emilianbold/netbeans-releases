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

package org.netbeans.lib.lexer.batch;

import java.util.ArrayList;
import java.util.Set;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.lib.lexer.EmbeddingContainer;
import org.netbeans.lib.lexer.LAState;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.TextToken;


/**
 * Token list used for root list for immutable inputs.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class BatchTokenList<T extends TokenId>
extends ArrayList<Object> implements TokenList<T> {
    
    /** Flag for additional correctness checks (may degrade performance). */
    private static final boolean testing = Boolean.getBoolean("netbeans.debug.lexer.test");
    
    private static boolean maintainLAState;
    
    /**
     * Check whether lookaheads and states are stored for testing purposes.
     */
    public static boolean isMaintainLAState() {
        return maintainLAState;
    }
    
    public static void setMaintainLAState(boolean maintainLAState) {
        BatchTokenList.maintainLAState = maintainLAState;
    }
    
    private final TokenHierarchyOperation<?,T> tokenHierarchyOperation;
    
    private final LanguagePath languagePath;
    
    private final Set<T> skipTokenIds;
    
    private final InputAttributes inputAttributes;
    
    /**
     * Lexer input used for lexing of the input.
     */
    private LexerInputOperation<T> lexerInputOperation;

    private LAState laState;
    
    private boolean inited;
    
    
    public BatchTokenList(TokenHierarchyOperation<?,T> tokenHierarchyOperation,
    Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {
        this.tokenHierarchyOperation = tokenHierarchyOperation;
        this.languagePath = LanguagePath.get(language);
        this.skipTokenIds = skipTokenIds;
        this.inputAttributes = inputAttributes;
        if (testing) { // Maintain lookaheads and states when in test environment
            laState = LAState.empty();
        }
    }

    public abstract char childTokenCharAt(int rawOffset, int index);

    protected abstract LexerInputOperation<T> createLexerInputOperation();

    protected void init() {
        lexerInputOperation = createLexerInputOperation();
    }
    
    public TokenList<? extends TokenId> root() {
        return this; // this list should always be the root list of the token hierarchy
    }
    
    public TokenHierarchyOperation<?,? extends TokenId> tokenHierarchyOperation() {
        return tokenHierarchyOperation;
    }
    
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    public synchronized int tokenCount() {
        if (!inited) {
            init();
            inited = true;
        }
        if (lexerInputOperation != null) { // still lexing
            tokenOrEmbeddingContainerImpl(Integer.MAX_VALUE);
        }
        return size();
    }
    
    public int tokenCountCurrent() {
        return size();
    }

    public int childTokenOffset(int rawOffset) {
        // Children offsets should be absolute
        return rawOffset;
    }

    public int tokenOffset(int index) {
        Token<T> token = existingToken(index);
        int offset;
        if (token.isFlyweight()) {
            offset = 0;
            while (--index >= 0) {
                token = existingToken(index);
                offset += token.length();
                if (!token.isFlyweight()) {
                    offset += token.offset(null);
                    break;
                }
            }
        } else { // non-flyweight offset
            offset = token.offset(null);
        }
        return offset;
    }

    public synchronized Object tokenOrEmbeddingContainer(int index) {
        return tokenOrEmbeddingContainerImpl(index);
    }
    
    private Object tokenOrEmbeddingContainerImpl(int index) {
        if (!inited) {
            init();
            inited = true;
        }
        while (lexerInputOperation != null && index >= size()) {
            Token<T> token = lexerInputOperation.nextToken();
            if (token != null) { // lexer returned valid token
                add(token);
                if (laState != null) { // maintaining lookaheads and states
                    laState = laState.add(lexerInputOperation.lookahead(),
                            lexerInputOperation.lexerState());
                }
            } else { // no more tokens from lexer
                lexerInputOperation.release();
                lexerInputOperation = null;
                trimToSize();
            }
        }
        return (index < size()) ? get(index) : null;
    }
    
    private Token<T> existingToken(int index) {
        return LexerUtilsConstants.token(get(index));
    }

    public int lookahead(int index) {
        return (laState != null) ? laState.lookahead(index) : -1;
    }

    public Object state(int index) {
        return (laState != null) ? laState.state(index) : null;
    }

    public int modCount() {
        return -1; // immutable input
    }
    
    public synchronized AbstractToken<T> replaceFlyToken(
    int index, AbstractToken<T> flyToken, int offset) {
        TextToken<T> nonFlyToken = ((TextToken<T>)flyToken).createCopy(this, offset);
        set(index, nonFlyToken);
        return nonFlyToken;
    }

    public void wrapToken(int index, EmbeddingContainer embeddingContainer) {
        set(index, embeddingContainer);
    }

    public InputAttributes inputAttributes() {
        return inputAttributes;
    }
    
    public boolean isContinuous() {
        return (skipTokenIds == null);
    }
    
    public Set<T> skipTokenIds() {
        return skipTokenIds;
    }

}
