/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
