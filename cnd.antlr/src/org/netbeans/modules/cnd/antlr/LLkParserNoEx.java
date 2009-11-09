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
 *  The same as LLkParser but no recognition exception thrown
 * @author gorrus
 */
public class LLkParserNoEx extends LLkParser {
    public LLkParserNoEx(int k_) {
        super(k_);
    }

    public LLkParserNoEx(TokenBuffer tokenBuf, int k_) {
        super(tokenBuf, k_);
    }

    public LLkParserNoEx(TokenStream lexer, int k_) {
        super(lexer, k_);
    }
    
    public LLkParserNoEx(TokenStream lexer, int k_, int initialBufferCapacity) {
        super(lexer, k_, initialBufferCapacity);
    }

    @Override
    public void match(int t) {
        if (LA(1) == t) {
            consume();
        } else {
            if (guessing == 0) {
                matchException = new MismatchedTokenException(tokenNames, LT(1), t, false, getFilename());
            }
            matchError=true;
        }
    }

    @Override
    public void match(BitSet b) {
        if (b.member(LA(1))) {
            consume();
        } else {
            if (guessing == 0) {
                matchException = new MismatchedTokenException(tokenNames, LT(1), b, false, getFilename());
            }
            matchError=true;
        }
    }

    @Override
    public void matchNot(int t) {
        if (LA(1) != t) {
            consume();
        } else {
            if (guessing == 0) {
                matchException = new MismatchedTokenException(tokenNames, LT(1), t, true, getFilename());
            }
            matchError=true;
        }
    }
}
