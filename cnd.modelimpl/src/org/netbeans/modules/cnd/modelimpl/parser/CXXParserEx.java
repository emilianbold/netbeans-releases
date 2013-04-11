/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.parser;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CXXParser;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;

/**
 *
 * @author nick
 */
public class CXXParserEx extends CXXParser {
    
    private final CXXParserActionEx action;
    private final boolean trace;
    
    public CXXParserEx(TokenStream input, CXXParserActionEx action) {
        super(input, action);
        this.action = action;
        this.trace = TraceFlags.TRACE_CPP_PARSER_RULES;
    }
    
    private CsmParserProvider.ParserErrorDelegate errorDelegate;
    
    public void setErrorDelegate(CsmParserProvider.ParserErrorDelegate delegate) {
        errorDelegate = delegate;
    }

    @Override
    public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
        if(errorDelegate != null) {
            if (e instanceof MyRecognitionException) {
                MyRecognitionException ex = (MyRecognitionException) e;
                if (APTUtils.isEOF(ex.getToken())) {
                    errorDelegate.onError(new CsmParserProvider.ParserError(ex.getMessage(), -1, -1, ex.getToken().getText(), true));
                } else {
                    errorDelegate.onError(new CsmParserProvider.ParserError(ex.getMessage(), ex.getToken().getLine(), ex.getToken().getColumn(), ex.getToken().getText(), false));
                }
            } else {
                errorDelegate.onError(new CsmParserProvider.ParserError(e.getMessage(), e.line, e.charPositionInLine, e.token.getText(), e.token.getType() == -1));
            }
        }
    }

//    @Override
//    public void recover(IntStream input, RecognitionException re) {
//        //super.recover(input, re);
//        if (state.lastErrorIndex == input.index()) {
//            // uh oh, another error at same token index; must be a case
//            // where LT(1) is in the recovery token set so nothing is
//            // consumed; consume a single token so at least to prevent
//            // an infinite loop; this is a failsafe.
//            input.consume();
//        }
//        state.lastErrorIndex = input.index();
//        BitSet followSet = computeErrorRecoverySet();
//        beginResync();
//        consumeUntil(input, followSet);
//        endResync();
//    }
//
//    @Override
//    public void consumeUntil(IntStream input, BitSet set) {
//        //System.out.println("consumeUntil("+set.toString(getTokenNames())+")");
//        int ttype = input.LA(1);
//        while (ttype != org.antlr.runtime.Token.EOF && !set.member(ttype)) {
//            //System.out.println("consume during recover LA(1)="+getTokenNames()[input.LA(1)]);
//            input.consume();
//            ttype = input.LA(1);
//        }
//    }
//    
//    @Override
//    protected BitSet combineFollows(boolean exact) {
//        int top = state._fsp;
//        BitSet followSet = new BitSet();
//        for (int i = top; i >= 0; i--) {
//            BitSet localFollowSet = state.following[i];
//            /*
//             System.out.println("local follow depth "+i+"="+
//             localFollowSet.toString(getTokenNames())+")");
//             */
//            followSet.orInPlace(localFollowSet);
//            if (exact) {
//                // can we see end of rule?
//                if (localFollowSet.member(org.antlr.runtime.Token.EOR_TOKEN_TYPE)) {
//                    // Only leave EOR in set if at top (start rule); this lets
//                    // us know if have to include follow(start rule); i.e., EOF
//                    if (i > 0) {
//                        followSet.remove(org.antlr.runtime.Token.EOR_TOKEN_TYPE);
//                    }
//                } else { // can't see end of rule, quit
//                    break;
//                }
//            }
//        }
//        return followSet;
//    }

    public int backtrackingLevel() {
        return state.backtracking;
    }        
    
    // indentation based trace
    private int level = 0;

    @Override
    public void traceIn(String ruleName, int ruleIndex) {
        if (trace) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < level; i++) {
                buf.append(' ').append(' '); //NOI18N
            }
            super.traceIn(buf.toString() + ruleName, ruleIndex);
            level++;
        }
    }

    @Override
    public void traceOut(String ruleName, int ruleIndex) {
        if (trace) {
            level--;
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < level; i++) {
                buf.append(' ').append(' '); //NOI18N
            }
            buf.append(' '); //NOI18N
            super.traceOut(buf.toString() + ruleName, ruleIndex);
        }
    }
    
    public static class MyRecognitionException extends RecognitionException {
        private final String message;
        private final Token myToken;
        public MyRecognitionException(String message, Token token) {
            this.message = message;
            myToken = token;
        }

        public Token getToken() {
            return myToken;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
