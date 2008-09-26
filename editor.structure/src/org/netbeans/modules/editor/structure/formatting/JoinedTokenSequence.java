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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.structure.formatting;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class JoinedTokenSequence {

    private TokenSequence[] tokenSequences;
    private TextBounds[] tokenSequenceBounds;
    private int currentTokenSequence = -1;

    public JoinedTokenSequence(TokenSequence[] tokenSequences, TextBounds[] tokenSequenceBounds) {
        this.tokenSequences = tokenSequences;
        this.tokenSequenceBounds = tokenSequenceBounds;
    }

    public Token token() {
        return currentTokenSequence().token();
    }

    public TokenSequence currentTokenSequence() {
        return tokenSequences[currentTokenSequence];
    }

    public void moveStart() {
        currentTokenSequence = 0;
        currentTokenSequence().moveStart();
    }

    public boolean moveNext() {
        boolean moreTokens = currentTokenSequence().moveNext();

        if (!moreTokens) {
            if (currentTokenSequence + 1 < tokenSequences.length) {
                currentTokenSequence++;
                currentTokenSequence().moveStart();
                moveNext();
            } else {
                return false;
            }
        }

        return true;
    }

    public boolean movePrevious() {
        boolean moreTokens = currentTokenSequence().movePrevious();

        if (!moreTokens) {
            if (currentTokenSequence > 0) {
                currentTokenSequence--;
                currentTokenSequence().moveEnd();
                movePrevious();
            } else {
                return false;
            }
        }

        return true;
    }

    public int move(int offset) {
        for (int i = 0; i < tokenSequences.length; i++) {
            if (tokenSequenceBounds[i].getAbsoluteStart() <= offset && tokenSequenceBounds[i].getAbsoluteEnd() > offset) {

                currentTokenSequence = i;
                return currentTokenSequence().move(offset);
            }
        }

        return Integer.MIN_VALUE;
    }

    boolean isJustAfterGap(){
        boolean justAfterGap = !currentTokenSequence().movePrevious();

        if (justAfterGap){
            currentTokenSequence().moveStart();
        }
        
        currentTokenSequence().moveNext();
        return justAfterGap;
    }

    public int offset() {
        return currentTokenSequence().offset();
    }

    public TokenSequence embedded() {
        return currentTokenSequence().embedded();
    }
}