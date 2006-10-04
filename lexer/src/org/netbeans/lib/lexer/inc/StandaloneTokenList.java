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

import java.util.Set;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.BranchTokenList;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.token.AbstractToken;


/**
 * Single token list maintains a text for a single token.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class StandaloneTokenList implements TokenList {

    private char[] tokenText;

    private LanguagePath languagePath;
    
    public StandaloneTokenList(LanguagePath languagePath, char[] tokenText) {
        this.languagePath = languagePath;
        this.tokenText = tokenText;
    }
    
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    public Object tokenOrBranch(int index) {
        throw new IllegalStateException("Not expected to be called"); // NOI18N
    }

    public <T extends TokenId> AbstractToken<T> createNonFlyToken(
    int index, AbstractToken<T> flyToken, int offset) {
        throw new IllegalStateException("Not expected to be called"); // NOI18N
    }

    public int lookahead(int index) {
        return -1;
    }

    public Object state(int index) {
        return null;
    }

    public int tokenOffset(int index) {
        throw new IllegalStateException("Not expected to be called"); // NOI18N
    }
    
    public int tokenCount() {
        return 1;
    }

    public int tokenCountCurrent() {
        return 1;
    }

    public int modCount() {
        return -1;
    }
    
    public int childTokenOffset(int rawOffset) {
        // Offset of the standalone token is absolute
        return rawOffset;
    }
    
    public char childTokenCharAt(int rawOffset, int index) {
        return tokenText[index];
    }

    public void wrapToken(int index, BranchTokenList wrapper) {
        throw new IllegalStateException("Branching of standalone tokens not supported"); // NOI18N
    }
    
    public TokenList root() {
        return this;
    }
    
    public InputAttributes inputAttributes() {
        throw new IllegalStateException("Not expected to be called"); // NOI18N
    }
    
    public boolean isContinuous() {
        return true;
    }

    public Set<? extends TokenId> skipTokenIds() {
        return null;
    }

}
