/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.editor.cplusplus;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport;

/**
 * This is the org.netbeans.modules.editor.java.JavaBracesMatcher
 * with IZ118206 fixed to have consistency with Java matcher.
 *
 * @author sg155630
 */
public class CppBracesMatcher implements BracesMatcher, BracesMatcherFactory {

    private static final char [] PAIRS = new char [] { '(', ')' //NOI18N
                                                     , '[', ']' //NOI18N
                                                     , '{', '}' //NOI18N
                                                     //, '<', '>' //NOI18N
                                                     };
    private static final CppTokenId [] PAIR_TOKEN_IDS = new CppTokenId [] { 
          CppTokenId.LPAREN, CppTokenId.RPAREN
        , CppTokenId.LBRACKET, CppTokenId.RBRACKET
        , CppTokenId.LBRACE, CppTokenId.RBRACE
        //, CppTokenId.LT, CppTokenId.GT
        };

    private final MatcherContext context;

    private boolean preprocConditionKeyword;
    private int originOffset;
    private char originChar;
    private char matchingChar;
    private boolean backward;
    private List<TokenSequence<?>> sequences;

    public CppBracesMatcher() {
        this(null);
    }

    private CppBracesMatcher(MatcherContext context){
        this.context = context;
    }

    public int[] findOrigin() throws BadLocationException, InterruptedException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            int [] origin = BracesMatcherSupport.findChar(
                context.getDocument(), 
                context.getSearchOffset(), 
                context.getLimitOffset(), 
                PAIRS
            );

            if (origin != null) {
                originOffset = origin[0];
                originChar = PAIRS[origin[1]];
                matchingChar = PAIRS[origin[1] + origin[2]];
                backward = origin[2] < 0;
                // Filter out block and line comments
                TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());
                sequences = th.embeddedTokenSequences(originOffset, backward);
                if (!sequences.isEmpty()) {
                    // Check special tokens
                    TokenSequence<?> seq = getTokenSequence();
                    if (seq == null) {
                        return null;
                    }
                }
                return new int [] { originOffset, originOffset + 1 };
            } else if (!MatcherContext.isTaskCanceled()) {
                TokenSequence<CppTokenId> ts = BracketCompletion.cppTokenSequence(context.getDocument(), context.getSearchOffset(), false);
                if (ts != null && ts.language() == CppTokenId.languagePreproc()) {
                    Token<CppTokenId> token = ts.token();
                    switch (token.id()) {
                        case PREPROCESSOR_IF:
                        case PREPROCESSOR_IFDEF:
                        case PREPROCESSOR_IFNDEF:
                        case PREPROCESSOR_ELIF:
                        case PREPROCESSOR_ELSE:
                        case PREPROCESSOR_ENDIF:
                            // valid token
                            originOffset = ts.offset();
                            preprocConditionKeyword = true;
                            return new int[] { originOffset, originOffset + token.length() };
                    }
                }
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    private static final class ConditionalBlock {
        private final List<Integer> directivePositions = new ArrayList<Integer>(4);
        private final List<ConditionalBlock> nested = new ArrayList<ConditionalBlock>(4);
        private final ConditionalBlock parent;

        public ConditionalBlock(ConditionalBlock parent) {
            this.parent = parent;
        }
        
        public void addDirective(TokenSequence<CppTokenId> ppTS) {
            directivePositions.add(ppTS.offset());
            directivePositions.add(ppTS.offset() + ppTS.token().length());
        }
        
        public ConditionalBlock startNestedBlock(TokenSequence<CppTokenId> ppTS) {
            ConditionalBlock nestedBlock = new ConditionalBlock(this);
            nestedBlock.addDirective(ppTS);
            nested.add(nestedBlock);
            return nestedBlock;
        }

        public ConditionalBlock getParent() {
            return parent;
        }
        
        public int[] toIntArray() {
            int[] out = new int[directivePositions.size()];
            for (int index = 0; index < directivePositions.size(); index++) {
                out[index] = directivePositions.get(index).intValue();
            }
            return out;
        }
    }

    private int[] findPreprocConditionBlocks() {
        TokenSequence<CppTokenId> origPreprocTS = BracketCompletion.cppTokenSequence(context.getDocument(), context.getSearchOffset(), false);
        if (origPreprocTS == null || origPreprocTS.language() != CppTokenId.languagePreproc()) {   
            return null;
        }     
        TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());
        int max = Math.min(context.getDocument().getLength() - 1, context.getSearchOffset() + context.getLimitOffset() - 1);
        List<TokenSequence<?>> ppSequences = th.tokenSequenceList(origPreprocTS.languagePath(), 0, max);
        ConditionalBlock file = new ConditionalBlock(null);
        ConditionalBlock current = new ConditionalBlock(file);
        ConditionalBlock offsetContainer = null;
        int searchOffset = context.getSearchOffset();
        for (TokenSequence<?> ts : ppSequences) {
            if (MatcherContext.isTaskCanceled()) {
                return null;
            }
            @SuppressWarnings("unchecked")
            TokenSequence<CppTokenId> ppTS = (TokenSequence<CppTokenId>) ts;
            if (moveToPreprocConditionalBlockKeyword(ppTS)) {
                switch (ppTS.token().id()) {
                    case PREPROCESSOR_IF:
                    case PREPROCESSOR_IFDEF:
                    case PREPROCESSOR_IFNDEF:
                        current = current.startNestedBlock(ppTS);
                        break;
                    case PREPROCESSOR_ELIF:
                    case PREPROCESSOR_ELSE:
                    case PREPROCESSOR_ENDIF:
                        current.addDirective(ppTS);
                        break;
                    default:
                        assert false : "unexpected token " + ts.token();
                }
                if (offsetContainer == null && isInToken(ppTS, searchOffset)) {
                    offsetContainer = current;
                }
                if (ppTS.token().id() == CppTokenId.PREPROCESSOR_ENDIF) {
                    current = current.getParent();
                    if (current == null) {
                        // unbalanced
                        return offsetContainer == null ? null : offsetContainer.toIntArray();
                    }
                }
            }
        }
        return offsetContainer == null ? null : offsetContainer.toIntArray();
    }

    private boolean isInToken(TokenSequence<CppTokenId> ppTS, int searchOffset) {
        return ppTS.offset() <= searchOffset && searchOffset <= ppTS.offset() + ppTS.token().length();
    }

    private boolean moveToPreprocConditionalBlockKeyword(TokenSequence<CppTokenId> ts) {
        ts.moveStart();
        while (ts.moveNext()) {
            switch (ts.token().id()) {
                case PREPROCESSOR_START:
                case WHITESPACE:
                case BLOCK_COMMENT:
                case ESCAPED_LINE:
                case ESCAPED_WHITESPACE:
                    // skip them
                    break;
                case PREPROCESSOR_IF:
                case PREPROCESSOR_IFDEF:
                case PREPROCESSOR_IFNDEF:
                case PREPROCESSOR_ELIF:
                case PREPROCESSOR_ELSE:
                case PREPROCESSOR_ENDIF:
                    // found
                    return true;
                default:
                    // not found interested directive
                    return false;
            }
        }
        return false;
    }
    
    private TokenSequence<?> getTokenSequence(){
        if (sequences.isEmpty()) {
            return null;
        }
        TokenSequence<?> seq = sequences.get(sequences.size() - 1);
        seq.move(originOffset);
        if (!seq.moveNext()) {
            if (sequences.size()>1) {
                seq = sequences.get(sequences.size() - 2);
                seq.move(originOffset);
                if (seq.moveNext()){
                    if (seq.token().id() == CppTokenId.BLOCK_COMMENT ||
                        seq.token().id() == CppTokenId.LINE_COMMENT) {
                        return null;
                    }
                }
            } else {
                return null;
            }
        } else {
            if (seq.token().id() == CppTokenId.BLOCK_COMMENT ||
                seq.token().id() == CppTokenId.LINE_COMMENT) {
                return null;
            }
        }
        return seq;
    }
    
    public int[] findMatches() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            if (preprocConditionKeyword) {
                return findPreprocConditionBlocks();
            }
            TokenSequence<?> seq = getTokenSequence();
            if (seq == null) {
                return null;
            }
            // Check special tokens
            seq.move(originOffset);
            if (seq.moveNext()) {
                if (seq.token().id() == CppTokenId.STRING_LITERAL) {
                    int offset = BracesMatcherSupport.matchChar(
                        context.getDocument(),
                        backward ? originOffset : originOffset + 1,
                        backward ? seq.offset() : seq.offset() + seq.token().length(),
                        originChar,
                        matchingChar);
                    if (offset != -1) {
                        return new int [] { offset, offset + 1 };
                    } else {
                        return null;
                    }
                }
            }
            // We are in plain c/c++
            CppTokenId originId = getTokenId(originChar);
            CppTokenId lookingForId = getTokenId(matchingChar);
            seq.move(originOffset);
            int counter = 0;
            if (backward) {
                while(seq.movePrevious()) {
                    if (originId == seq.token().id()) {
                        counter++;
                    } else if (lookingForId == seq.token().id()) {
                        if (counter == 0) {
                            return new int [] { seq.offset(), seq.offset() + seq.token().length() };
                        } else {
                            counter--;
                        }
                    }
                }
            } else {
                seq.moveNext();
                while(seq.moveNext()) {
                    if (originId == seq.token().id()) {
                        counter++;
                    } else if (lookingForId == seq.token().id()) {
                        if (counter == 0) {
                            return new int [] { seq.offset(), seq.offset() + seq.token().length() };
                        } else {
                            counter--;
                        }
                    }
                }
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    private CppTokenId getTokenId(char ch) {
        for(int i = 0; i < PAIRS.length; i++) {
            if (PAIRS[i] == ch) {
                return PAIR_TOKEN_IDS[i];
            }
        }
        return null;
    }

    public BracesMatcher createMatcher(MatcherContext context) {
        return new CppBracesMatcher(context);
    }
}