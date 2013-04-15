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

import org.antlr.runtime.BitSet;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CXXParser;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;

/**
 *
 * @author nick
 */
public class CXXParserEx extends CXXParser {
    
    private static final int RECOVERY_LIMIT = 20;
    private static final BitSet stopSet = new BitSet();
    static {
        stopSet.add(LCURLY);
        stopSet.add(RCURLY);
        stopSet.add(RPAREN);
        stopSet.add(LPAREN);
    }
        
    private final CXXParserActionEx action;
    private final boolean trace;
    private int level = 0; // indentation based trace
    private int recoveryCounter = 0;

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
                String hdr = getSourceName();
                if (APTUtils.isEOF(ex.getToken())) {
                    errorDelegate.onError(new CsmParserProvider.ParserError(hdr+" "+ex.getMessage(), -1, -1, ex.getToken().getText(), true));
                } else {
                    errorDelegate.onError(new CsmParserProvider.ParserError(hdr+":"+ex.getToken().getLine()+": error: "+ex.getMessage(), ex.getToken().getLine(), ex.getToken().getColumn(), ex.getToken().getText(), false)); // NOI18N
                }
            } else {
                String hdr = getSourceName();
                String msg = getErrorMessage(e, tokenNames);
                errorDelegate.onError(new CsmParserProvider.ParserError(hdr+":"+e.line+": error: "+msg, e.line, e.charPositionInLine, e.token.getText(), e.token.getType() == -1)); // NOI18N
            }
        }
    }

    @Override
    public String getSourceName() {
        CsmFile currentFile = action.getCurrentFile();
        if (currentFile != null) {
            return currentFile.getAbsolutePath().toString();
        }
        return ""; // NOI18N
    }

    public int backtrackingLevel() {
        return state.backtracking;
    }        
     
    /**
     * Recover from an error found on the input stream. This is for NoViableAlt
     * and mismatched symbol exceptions. If you enable single token insertion
     * and deletion, this will usually not handle mismatched symbol exceptions
     * but there could be a mismatched token that the match() routine could not
     * recover from.
     */
    @Override
    public void recover(IntStream input, RecognitionException re) {
        BitSet followSet = computeErrorRecoverySet();
        if (state.lastErrorIndex == input.index()) {
            //<editor-fold defaultstate="collapsed" desc="Original Implementation">
            // uh oh, another error at same token index; must be a case
            // where LT(1) is in the recovery token set so nothing is
            // consumed; consume a single token so at least to prevent
            // an infinite loop; this is a failsafe.
            //input.consume();
            //</editor-fold>
            // our solution:
            if (recoveryCounter > RECOVERY_LIMIT) {
                input.consume();
                recoveryCounter = 0;
                //followSet.orInPlace(stopSet);
            } else {
                recoveryCounter++;
            }
        } else {
            recoveryCounter = 0;
        }
        state.lastErrorIndex = input.index();
        beginResync();
        consumeUntil(input, followSet);
        endResync();
    }
    
//    @Override
//    public void consumeUntil(IntStream input, int tokenType) {
//        //System.out.println("consumeUntil "+tokenType);
//        int ttype = input.LA(1);
//        while (ttype != org.antlr.runtime.Token.EOF && ttype != tokenType) {
//            input.consume();
//            ttype = input.LA(1);
//        }
//    }
//
//    /**
//     * Consume tokens until one matches the given token set
//     */
//    @Override
//    public void consumeUntil(IntStream input, BitSet set) {
//        //System.out.println("consumeUntil("+set.toString(getTokenNames())+")");
//        int ttype = input.LA(1);
//        while (ttype != org.antlr.runtime.Token.EOF && !set.member(ttype)) {
//            System.out.println("consume during recover LA(1)="+getTokenNames()[input.LA(1)]);
//            input.consume();
//            ttype = input.LA(1);
//        }
//    }

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
