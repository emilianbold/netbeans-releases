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

package org.netbeans.lib.lexer.inc;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.lib.lexer.LAState;
import org.netbeans.lib.lexer.LexerSpiPackageAccessor;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Description of the change in a token list.
 * <br/>
 * The change is expressed as a list of removed tokens
 * plus the current list and index and number of the tokens
 * added to the current list.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenListChange {
    
    private static final AbstractToken[] EMPTY_TOKEN_ARRAY = new AbstractToken[0];

    private SharedInfo sharedInfo;

    private LanguagePath languagePath;
    
    private RemovedTokenList removedTokenList;
    
    private TokenList currentTokenList;
    
    private List<AbstractToken> addedTokens;

    private LAState laState;

    /**
     * Offset of the first removed token.
     */
    private int removedTokensOffset;
    
    private int addedTokenCount;

    private int tokenIndex;

    private int modifiedTokensStartOffset;

    private int removedTokensEndOffset;

    private int addedTokensEndOffset;
    
    private int offsetGapIndex;

    private TokenListChange child;


    public TokenListChange(TokenHierarchyOperation tokenHierarchyOperation, TokenHierarchyEvent.Type type,
    int offset, int removedLength, CharSequence removedText, int insertedLength) {
        
        // Initial checks
        if (offset < 0) {
            throw new IllegalArgumentException("offset=" + offset + " < 0"); // NOI18N
        }
        if (removedLength < 0) {
            throw new IllegalArgumentException("removedLength=" + removedLength + " < 0"); // NOI18N
        }
        if (insertedLength < 0) {
            throw new IllegalArgumentException("insertedLength=" + insertedLength + " < 0"); // NOI18N
        }

        sharedInfo = new SharedInfo(tokenHierarchyOperation, type, offset, removedLength, removedText, insertedLength);
    }

    public TokenListChange(TokenListChange parentChange) {
        this.sharedInfo = parentChange.sharedInfo();
        parentChange.setChild(this);
    }

    public TokenHierarchyOperation tokenHierarchyOperation() {
        return sharedInfo.tokenHierarchyOperation();
    }
    
    public TokenHierarchyEvent.Type type() {
        return sharedInfo.type();
    }
    
    public int offset() {
        return sharedInfo.offset();
    }
    
    public int removedLength() {
        return sharedInfo.removedLength();
    }
    
    public CharSequence removedText() {
        return sharedInfo.removedText();
    }

    public OriginalText originalText() {
        return sharedInfo.originalText();
    }
    
    public int insertedLength() {
        return sharedInfo.insertedLength();
    }
    
    public CharSequence insertedText() {
        return null;
    }
    
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    public void setLanguagePath(LanguagePath languagePath) {
        this.languagePath = languagePath;
    }
    
    public int removedTokenCount() {
        return removedTokenList.tokenCount();
    }
    
    public int addedTokenCount() {
        return addedTokenCount;
    }

    public void setAddedTokenCount(int addedTokenCount) {
        this.addedTokenCount = addedTokenCount;
    }
    
    public TokenList currentTokenList() {
        return currentTokenList;
    }
    
    public void setCurrentTokenList(TokenList tokenList) {
        this.currentTokenList = tokenList;
    }

    public int tokenIndex() {
        return tokenIndex;
    }

    public void setTokenIndex(int tokenIndex) {
        this.tokenIndex = tokenIndex;
    }
    
    public RemovedTokenList removedTokenList() {
        return removedTokenList;
    }
    
    public void initRemovedTokenList(Object[] removedTokensOrBranches) {
        removedTokenList = new RemovedTokenList(this, removedTokensOrBranches);
    }
    
    public void addToken(AbstractToken token, int lookahead, Object state) {
        if (addedTokens == null) {
            addedTokens = new ArrayList<AbstractToken>(2);
            laState = LAState.empty();
        }
        addedTokens.add(token);
        laState = laState.add(lookahead, state);
    }
    
    public List<AbstractToken> addedTokens() {
        return addedTokens;
    }

    public LAState laState() {
        return laState;
    }

    public void clearAddedTokens() {
        addedTokens = null;
        laState = null;
    }

    public void noChange(TokenList tokenList) {
        this.currentTokenList = tokenList;
        this.removedTokenList = new RemovedTokenList(this, EMPTY_TOKEN_ARRAY);
        setTokenIndex(-1);
    }

    public int modifiedTokensStartOffset() {
        return modifiedTokensStartOffset;
    }

    public void setModifiedTokensStartOffset(int modifiedTokensStartOffset) {
        this.modifiedTokensStartOffset = modifiedTokensStartOffset;
    }

    public int removedTokensEndOffset() {
        return removedTokensEndOffset;
    }

    public void setRemovedTokensEndOffset(int removedTokensEndOffset) {
        this.removedTokensEndOffset = removedTokensEndOffset;
    }

    public int addedTokensEndOffset() {
        return addedTokensEndOffset;
    }

    public void setAddedTokensEndOffset(int addedTokensEndOffset) {
        this.addedTokensEndOffset = addedTokensEndOffset;
    }

    public int offsetGapIndex() {
        return offsetGapIndex;
    }

    public void setOffsetGapIndex(int offsetGapIndex) {
        this.offsetGapIndex = offsetGapIndex;
    }

    public TokenListChange child() {
        return child;
    }

    void setChild(TokenListChange child) {
        assert (this.child == null);
        this.child = child;
    }

    SharedInfo sharedInfo() {
        return sharedInfo;
    }



    private static final class SharedInfo {

        private final TokenHierarchyOperation tokenHierarchyOperation;
        
        private final TokenHierarchyEvent.Type type;
        
        private final int offset;
        
        private final int removedLength;
        
        private final CharSequence removedText;
        
        private final int insertedLength;
        
        private OriginalText originalText;

        SharedInfo(TokenHierarchyOperation tokenHierarchyOperation, TokenHierarchyEvent.Type type,
        int offset, int removedLength, CharSequence removedText, int insertedLength) {
            this.tokenHierarchyOperation = tokenHierarchyOperation;
            this.type = type;
            this.offset = offset;
            this.removedLength = removedLength;
            this.removedText = removedText;
            this.insertedLength = insertedLength;
        }

        public TokenHierarchyOperation tokenHierarchyOperation() {
            return tokenHierarchyOperation;
        }
        
        public TokenHierarchyEvent.Type type() {
            return type;
        }
        
        public int offset() {
            return offset;
        }
        
        public int removedLength() {
            return removedLength;
        }
        
        public CharSequence removedText() {
            return removedText;
        }
        
        public int insertedLength() {
            return insertedLength;
        }
        
        public OriginalText originalText() {
            if (originalText == null) {
                if (removedLength != 0 && removedText == null) {
                    throw new IllegalStateException("Cannot obtain removed text for " // NOI18N
                            + tokenHierarchyOperation.mutableInputSource()
                            + " which breaks token snapshots operation and" // NOI18N
                            + " token text retaining after token's removal." // NOI18N
                            + " Valid removedText in TokenHierarchyControl.textModified()" // NOI18N
                            + " should be provided." // NOI18N
                            );
                }
                originalText = new OriginalText(
                        LexerSpiPackageAccessor.get().text(tokenHierarchyOperation.mutableTextInput()),
                        offset, removedText, insertedLength
                        );
            }
            return originalText;
        }
 }

}