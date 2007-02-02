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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LAState;
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

public final class TokenListChange<T extends TokenId> {
    
    private final TokenChangeInfo<T> tokenChangeInfo;
    
    private List<AbstractToken<T>> addedTokens;

    private LAState laState;

    private int offsetGapIndex;
    
    private int removedEndOffset;
    
    private int addedEndOffset;
    
    public TokenListChange(TokenList<T> tokenList) {
        tokenChangeInfo = new TokenChangeInfo<T>(tokenList);
    }
    
    public TokenChangeInfo<T> tokenChangeInfo() {
        return tokenChangeInfo;
    }
    
    public LanguagePath languagePath() {
        return tokenChangeInfo.currentTokenList().languagePath();
    }

    public int index() {
        return tokenChangeInfo.index();
    }

    public void setIndex(int tokenIndex) {
        tokenChangeInfo.setIndex(tokenIndex);
    }
    
    public int offset() {
        return tokenChangeInfo.offset();
    }

    public void setOffset(int offset) {
        tokenChangeInfo.setOffset(offset);
    }
    
    public int offsetGapIndex() {
        return offsetGapIndex;
    }

    public void setOffsetGapIndex(int offsetGapIndex) {
        this.offsetGapIndex = offsetGapIndex;
    }

    public void addToken(AbstractToken<T> token, int lookahead, Object state) {
        if (addedTokens == null) {
            addedTokens = new ArrayList<AbstractToken<T>>(2);
            laState = LAState.empty();
        }
        addedTokens.add(token);
        laState = laState.add(lookahead, state);
    }
    
    public List<AbstractToken<T>> addedTokens() {
        return addedTokens;
    }
    
    public void syncAddedTokenCount() {
        tokenChangeInfo.setAddedTokenCount(addedTokens.size());
    }

    public void setRemovedTokens(Object[] removedTokensOrBranches) {
        tokenChangeInfo.setRemovedTokenList(new RemovedTokenList<T>(
                languagePath(), removedTokensOrBranches));
    }
    
    public int removedEndOffset() {
        return removedEndOffset;
    }
    
    public void setRemovedEndOffset(int removedEndOffset) {
        this.removedEndOffset = removedEndOffset;
    }
    
    public int addedEndOffset() {
        return addedEndOffset;
    }
    
    public void setAddedEndOffset(int addedEndOffset) {
        this.addedEndOffset = addedEndOffset;
    }
    
    public LAState laState() {
        return laState;
    }

}