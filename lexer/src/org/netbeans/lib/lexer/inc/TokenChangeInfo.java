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

import java.util.List;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LAState;
import org.netbeans.lib.lexer.LexerApiPackageAccessor;
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

public final class TokenChangeInfo<T extends TokenId> {
    
    private static final TokenChange<? extends TokenId>[] EMPTY_EMBEDDED_CHANGES
            = (TokenChange<? extends TokenId>[])new TokenChange[0];

    private TokenChange<? extends TokenId>[] embeddedChanges = EMPTY_EMBEDDED_CHANGES;
    
    private final TokenList<T> currentTokenList;
    
    private RemovedTokenList<T> removedTokenList;
    
    private int addedTokenCount;

    private int index;

    private int offset;


    public TokenChangeInfo(TokenList<T> currentTokenList) {
        this.currentTokenList = currentTokenList;
    }

    public TokenChange<? extends TokenId>[] embeddedChanges() {
        return embeddedChanges;
    }
    
    public void addEmbeddedChange(TokenChangeInfo<? extends TokenId> change) {
        TokenChange<? extends TokenId>[] tmp = (TokenChange<? extends TokenId>[])
                new TokenChange[embeddedChanges.length + 1];
        System.arraycopy(embeddedChanges, 0, tmp, 0, embeddedChanges.length);
        tmp[embeddedChanges.length] = LexerApiPackageAccessor.get().createTokenChange(change);
        embeddedChanges = tmp;
    }
    
    public int index() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
    public int offset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public RemovedTokenList<T> removedTokenList() {
        return removedTokenList;
    }
    
    public void setRemovedTokenList(RemovedTokenList<T> removedTokenList) {
        this.removedTokenList = removedTokenList;
    }
    
    public int addedTokenCount() {
        return addedTokenCount;
    }

    public void setAddedTokenCount(int addedTokenCount) {
        this.addedTokenCount = addedTokenCount;
    }
    
    public TokenList<T> currentTokenList() {
        return currentTokenList;
    }
    
}