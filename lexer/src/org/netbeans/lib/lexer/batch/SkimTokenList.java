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

package org.netbeans.lib.lexer.batch;

import java.util.Set;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.lib.lexer.EmbeddingContainer;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Filtering token list constructed over character array with an independent
 * start offset value.
 * <br>
 * It is constructed for batch inputs and it implements
 * a token list but it only implements translation of raw offsets
 * into real offsets and retrieving of the characters of token bodies.
 * <br>
 * Other operations are delegated to an original
 * token list that really holds the tokens.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class SkimTokenList<T extends TokenId> implements TokenList<T> {
    
    private CopyTextTokenList<T> tokenList;
    
    private int startOffset;
    
    private char[] text;
    
    
    public SkimTokenList(CopyTextTokenList<T> tokenList, int startOffset, char[] text) {
        this.tokenList = tokenList;
        this.startOffset = startOffset;
        this.text = text;
    }

    public CopyTextTokenList<T> getTokenList() {
        return tokenList;
    }
    
    public int getStartOffset() {
        return startOffset;
    }
    
    char[] getText() {
        return text;
    }
    
    void setText(char[] text) {
        this.text = text;
    }

    public int childTokenOffset(int rawOffset) {
        int offsetShift = (rawOffset >> 16);
        return startOffset + (rawOffset & 0xFFFF) + offsetShift;
    }

    public char childTokenCharAt(int rawOffset, int index) {
        return text[((rawOffset + index) & 0xFFFF)];
    }

    public int modCount() {
        return 0;
    }

    public Object tokenOrEmbeddingContainer(int index) {
        return tokenList.tokenOrEmbeddingContainer(index);
    }
    
    public AbstractToken<T> replaceFlyToken(
    int index, AbstractToken<T> flyToken, int offset) {
        return tokenList.replaceFlyToken(index, flyToken, offset);
    }
    

    public int lookahead(int index) {
        return tokenList.lookahead(index);
    }

    public Object state(int index) {
        return tokenList.state(index);
    }

    public int tokenOffset(int index) {
        return tokenList.tokenOffset(index);
    }

    public int tokenCount() {
        return tokenList.tokenCount();
    }
    
    public int tokenCountCurrent() {
        return tokenList.tokenCountCurrent();
    }

    public TokenList<? extends TokenId> root() {
        return tokenList.root();
    }

    public TokenHierarchyOperation<?,? extends TokenId> tokenHierarchyOperation() {
        return tokenList.tokenHierarchyOperation();
    }
    
    public LanguagePath languagePath() {
        return tokenList.languagePath();
    }

    public void wrapToken(int index, EmbeddingContainer embeddingContainer) {
        tokenList.wrapToken(index, embeddingContainer);
    }

    public InputAttributes inputAttributes() {
        return tokenList.inputAttributes();
    }
    
    public boolean isContinuous() {
        return tokenList.isContinuous();
    }

    public Set<T> skipTokenIds() {
        return tokenList.skipTokenIds();
    }

}
