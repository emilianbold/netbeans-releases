/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.antlr;

import org.netbeans.modules.cnd.antlr.collections.impl.BitSet;

/**
 *
 * @author gorrus
 */
public abstract class CharScannerNoEx extends CharScanner {
    public CharScannerNoEx(InputBuffer cb) { // SAS: use generic buffer
        super(cb);
    }

    /*public CharScannerNoEx(LexerSharedInputState sharedState) {
        super(sharedState);
    }*/
    
    @Override
    public void match(char c) {
        if (LA(1) == c) {
            consume();
        } else {
            if (guessing == 0) {
                matchException = new MismatchedCharException(LA(1), c, false, this);
            }
            matchError = true;
        }
    }

    @Override
    public void match(BitSet b) {
        if (b.member(LA(1))) {
            consume();
        } else {
            if (guessing == 0) {
                matchException = new MismatchedCharException(LA(1), b, false, this);
            }
            matchError = true;
        }
    }

    @Override
    public void match(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (LA(1) != s.charAt(i)) {
                if (guessing == 0) {
                    matchException = new MismatchedCharException(LA(1), s.charAt(i), false, this);
                }
                matchError = true;
                return;
            }
            consume();
        }
    }

    @Override
    public void matchNot(char c) {
        if (LA(1) != c) {
            consume();
        } else {
            if (guessing == 0) {
                matchException = new MismatchedCharException(LA(1), c, true, this);
            }
            matchError = true;
        }
    }

    @Override
    public void matchRange(char c1, char c2) {
        char LA1 = LA(1);
        if (LA1 < c1 || LA1 > c2) {
            if (guessing == 0) {
                matchException = new MismatchedCharException(LA(1), c1, c2, false, this);
            }
            matchError = true;
        } else {
            consume();
        }
    }

    @Override
    public void setCaseSensitive(boolean t) {
        if (t != true) {
            throw new UnsupportedOperationException("In this version only case sensitive grammars supported");
        }
        super.setCaseSensitive(t);
    }
    
    @Override
    public void consume() {
        if (guessing == 0) {
            char c = LA(1);
            append(c);
            if (c == '\t') {
                tab();
            } else {
                inputState.column++;
            }
        }
        input.consume();
    }

    @Override
    public char LA(int i) {
        return input.LA(i);
    }
}
