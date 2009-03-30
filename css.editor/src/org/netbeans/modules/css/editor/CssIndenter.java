/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.css.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.css.formatting.api.support.AbstractIndenter;
import org.netbeans.modules.css.formatting.api.support.IndenterContextData;
import org.netbeans.modules.css.formatting.api.support.IndentCommand;
import org.netbeans.modules.css.formatting.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.css.formatting.api.LexUtilities;
import org.netbeans.modules.css.lexer.api.CssTokenId;
import org.netbeans.modules.editor.indent.spi.Context;

public class CssIndenter extends AbstractIndenter<CssTokenId> {

    private Stack<CssStackItem> stack = null;
    private int preservedLineIndentation = -1;

    public CssIndenter(Context context) {
        super(CssTokenId.language(), context);
    }

    private Stack<CssStackItem> getStack() {
        return stack;
    }

    @Override
    protected boolean isWhiteSpaceToken(Token<CssTokenId> token) {
        String text = token.text().toString().trim();
        return token.id() == CssTokenId.S && !text.startsWith("/*") && !text.endsWith("*/");
    }

    private boolean isCommentToken(Token<CssTokenId> token) {
        String text = token.text().toString().trim();
        return token.id() == CssTokenId.S && text.startsWith("/*") && text.endsWith("*/");
    }

    @Override
    protected void reset() {
        stack = new Stack<CssStackItem>();
    }

    @Override
    protected int getFormatStableStart(JoinedTokenSequence<CssTokenId> ts, int startOffset, int endOffset,
            AbstractIndenter.OffsetRanges rangesToIgnore) {
        ts.move(startOffset);

        if (!ts.movePrevious()) {
            return LexUtilities.getTokenSequenceStartOffset(ts);
        }

        // Look backwards to find a suitable context - beginning of a rule
        do {
            Token<CssTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == CssTokenId.IDENT) {
                int index = ts.index();
                ts.moveNext();
                Token tk = LexUtilities.findNext(ts, Arrays.asList(CssTokenId.S));
                ts.moveIndex(index);
                ts.moveNext();
                if (tk != null && tk.id() == CssTokenId.LBRACE) {
                    if (ts.movePrevious()) {
                        tk = LexUtilities.findPrevious(ts, Arrays.asList(CssTokenId.S, CssTokenId.IDENT));
                        if (tk != null) {
                            ts.moveNext();
                            tk = LexUtilities.findNext(ts, Arrays.asList(CssTokenId.S));
                        }
                    }
                    return ts.offset();
                }
            }
        } while (ts.movePrevious());

        return LexUtilities.getTokenSequenceStartOffset(ts);
    }

    private void getIndentFromState(List<IndentCommand> iis, boolean updateState, int lineStartOffset) {
        Stack<CssStackItem> blockStack = getStack();
        // decide on preliminary indent of current line based on current stack content;
        // it is preliminary as it can be still adjusted when iterating over current line
        // for example if current line contains "}" then current line should be de-indented
        if (!blockStack.empty()) {
            CssStackItem item = blockStack.peek();
            if (item.state == StackItemState.IN_VALUE) {
                // first have a look if IN_RULE was processed
                if (blockStack.size() > 1) {
                    CssStackItem prevItem = blockStack.get(blockStack.size()-2);
                    assert prevItem.state == StackItemState.IN_RULE;
                    if (prevItem.processed == Boolean.FALSE) {
                        // handle this one first:
                        IndentCommand ii = new IndentCommand(IndentCommand.Type.INDENT, lineStartOffset);
                        if (prevItem.indent != -1) {
                            ii.setFixedIndentSize(prevItem.indent);
                        }
                        iis.add(ii);
                        if (updateState) {
                            prevItem.processed = Boolean.TRUE;
                        }
                    }
                }
                IndentCommand ii = new IndentCommand(IndentCommand.Type.CONTINUE, lineStartOffset);
                if (item.indent != -1) {
                    ii.setFixedIndentSize(item.indent);
                }
                iis.add(ii);
            } else if (item.state == StackItemState.IN_RULE) {
                if (item.processed == Boolean.FALSE) {
                    IndentCommand ii = new IndentCommand(IndentCommand.Type.INDENT, lineStartOffset);
                    if (item.indent != -1) {
                        ii.setFixedIndentSize(item.indent);
                    }
                    iis.add(ii);
                    if (updateState) {
                        item.processed = Boolean.TRUE;
                    }
                }
            } else if (item.state == StackItemState.RULE_FINISHED) {
                iis.add(new IndentCommand(IndentCommand.Type.RETURN, lineStartOffset));
                if (updateState) {
                    blockStack.pop();
                }
            }
        }
    }

    @Override
    protected List<IndentCommand> getLineIndent(IndenterContextData<CssTokenId> context,
            List<IndentCommand> preliminaryNextLineIndent) throws BadLocationException {
        Stack<CssStackItem> blockStack = getStack();
        List<IndentCommand> iis = new ArrayList<IndentCommand>();
        getIndentFromState(iis, true, context.getLineStartOffset());

        JoinedTokenSequence<CssTokenId> ts = context.getJoinedTokenSequences();
        ts.move(context.getLineStartOffset());

        boolean ruleWasDefined = false;
        int lastLBrace = -1;
        // iterate over tokens on the line and push to stack any changes
        while (!context.isBlankLine() && ts.moveNext() &&
            ((ts.isCurrentTokenSequenceVirtual() && ts.offset() < context.getLineEndOffset()) ||
                    ts.offset() <= context.getLineEndOffset()) ) {
            Token<CssTokenId> token = ts.token();
            if (token == null || ts.embedded() != null) {
                continue;
            }

            if (lastLBrace != -1 && token.id() != CssTokenId.S) {
                CssStackItem state = blockStack.peek();
                assert state.state == StackItemState.IN_RULE;
                state.indent = ts.offset() - context.getLineNonWhiteStartOffset();
                lastLBrace = -1;
            }
            if (token.id() == CssTokenId.LBRACE) {
                if (!isInState(blockStack, StackItemState.IN_RULE)) {
                    CssStackItem state = new CssStackItem(StackItemState.IN_RULE);
                    lastLBrace = ts.offset();
                    state.processed = Boolean.FALSE;
                    blockStack.push(state);
                    ruleWasDefined = true;
                }
            } else if (token.id() == CssTokenId.COLON) {
                if (!isInState(blockStack, StackItemState.IN_VALUE) && isInState(blockStack, StackItemState.IN_RULE)) {
                    blockStack.push(new CssStackItem(StackItemState.IN_VALUE));
                }
            } else if (token.id() == CssTokenId.SEMICOLON) {
                if (isInState(blockStack, StackItemState.IN_VALUE)) {
                    CssStackItem item = blockStack.pop();
                    assert item.state == StackItemState.IN_VALUE;
                }
            } else if (token.id() == CssTokenId.RBRACE) {
                if (isInState(blockStack, StackItemState.IN_RULE)) {
                    CssStackItem item = blockStack.pop();
                    if (item.state == StackItemState.IN_VALUE) {
                        // in cases like:
                        //
                        //  .rcol {
                        //    width:249px
                        //  }
                        //
                        item = blockStack.pop();
                    }
                    assert item.state == StackItemState.IN_RULE : item;
                    if (ts.offset() == context.getLineNonWhiteStartOffset()) {
                        // if "}" is first character on line then it changes line's indentation:
                        iis.add(new IndentCommand(IndentCommand.Type.RETURN, context.getLineStartOffset()));
                    } else {
                        // eg. "blue; }" - this does not have impact on indentation of current line but
                        // will need to be addressed on next line; pushing state RULE_FINISHED.
                        // but only if rule was not defined on line completely, eg. "a {b:c}" in which
                        // case nothing needs to be done.
                        if (!ruleWasDefined) {
                            blockStack.push(new CssStackItem(StackItemState.RULE_FINISHED));
                        }
                    }
                }
            } else if (isCommentToken(token)) {
                    int start = context.getLineStartOffset();
                    if (start < ts.offset()) {
                        start = ts.offset();
                    }
                    int commentEndOffset = ts.offset()+ts.token().text().toString().trim().length()-1;
                    int end = context.getLineEndOffset();
                    if (end > commentEndOffset) {
                        end = commentEndOffset;
                    }
                    if (start > end) {
                        assert !isInState(blockStack, StackItemState.IN_COMMENT) : "token="+token.text()+" start="+start+" end="+end;
                        // do nothing
                    } else if (start == ts.offset()) {
                        if (end < commentEndOffset) {
                            // if comment ends on next line put formatter to IN_COMMENT state
                            blockStack.push(new CssStackItem(StackItemState.IN_COMMENT));
                            int lineStart = Utilities.getRowStart(getDocument(), ts.offset());
                            preservedLineIndentation = start - lineStart;
                        }
                    } else if (end == commentEndOffset) {
                        String text = getDocument().getText(start, end-start).trim();
                        if (!text.startsWith("*/")) {
                            // if line does not start with '*/' then treat it as unformattable
                            IndentCommand ic = new IndentCommand(IndentCommand.Type.PRESERVE_INDENTATION, context.getLineStartOffset());
                            ic.setFixedIndentSize(preservedLineIndentation);
                            iis.add(ic);
                        }
                        assert isInState(blockStack, StackItemState.IN_COMMENT) : "token="+token.text()+" start="+start+" end="+end;
                        blockStack.pop();
                        preservedLineIndentation = -1;
                    } else {
                        assert isInState(blockStack, StackItemState.IN_COMMENT) : "token="+token.text()+" start="+start+" end="+end;
                        IndentCommand ic = new IndentCommand(IndentCommand.Type.PRESERVE_INDENTATION, context.getLineStartOffset());
                        ic.setFixedIndentSize(preservedLineIndentation);
                        iis.add(ic);
                    }
            }
        }

        if (iis.isEmpty()) {
            iis.add(new IndentCommand(IndentCommand.Type.NO_CHANGE, context.getLineStartOffset()));
        }

        if (context.getNextLineStartOffset() != -1) {
            getIndentFromState(preliminaryNextLineIndent, false, context.getNextLineStartOffset());
            if (preliminaryNextLineIndent.size() == 0) {
                preliminaryNextLineIndent.add(new IndentCommand(IndentCommand.Type.NO_CHANGE, context.getNextLineStartOffset()));
            }
        }

        return iis;
    }

    private boolean isInState(Stack<CssStackItem> stack, StackItemState state) {
        for (CssStackItem item : stack) {
            if (item.state == state) {
                return true;
            }
        }
        return false;
    }

    private static enum StackItemState {
        IN_RULE,
        IN_VALUE,
        RULE_FINISHED,
        IN_COMMENT,
        ;
    }

    private static class CssStackItem  {
        private StackItemState state;
        private Boolean processed;
        private int indent;

        private CssStackItem(StackItemState state) {
            this.state = state;
            this.indent = -1;
        }

        @Override
        public String toString() {
            return "CssStackItem[state="+state+",indent="+indent+",processed="+processed+"]";
        }

    }

}
