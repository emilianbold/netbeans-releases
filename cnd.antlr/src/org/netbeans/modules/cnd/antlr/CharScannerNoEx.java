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
