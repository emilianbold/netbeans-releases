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

import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.antlr.collections.impl.BitSet;


/**
 *
 * @author gorrus
 */
public class TreeParserNoEx extends TreeParser {
    
    /** Creates a new instance of TreeParserNoEx */
    public TreeParserNoEx() {
        super();
    }
    
    @Override
    protected void match(AST t, int ttype) {
        if (t == null || t == ASTNULL || t.getType() != ttype) {
            if (inputState.guessing == 0) {
                matchException = new MismatchedTokenException(getTokenNames(), t, ttype, false);
            }
            matchError = true;
        }
    }

    @Override
    public void match(AST t, BitSet b) {
        if (t == null || t == ASTNULL || !b.member(t.getType())) {
            if (inputState.guessing == 0) {
                matchException = new MismatchedTokenException(getTokenNames(), t, b, false);
            }
            matchError = true;
        }
    }

    @Override
    protected void matchNot(AST t, int ttype) {
        if (t == null || t == ASTNULL || t.getType() == ttype) {
            if (inputState.guessing == 0) {
                matchException = new MismatchedTokenException(getTokenNames(), t, ttype, true);
            }
            matchError = true;
        }
    }
}
