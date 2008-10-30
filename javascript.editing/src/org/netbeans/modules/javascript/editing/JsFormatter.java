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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.javascript.editing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.csl.api.CompilationInfo;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.javascript.editing.JsPretty.Diff;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.openide.util.Exceptions;

/**
 * Formatting and indentation for JavaScript
 * 
 * @todo dojo.js.uncompressed.js:5786 indentation
 * @todo Handle block comments - similar to multiline literals but should be indented by a relative amount
 * @todo Handle XML/E4X content
 * @todo Use the Context.modifyIndent() method to change line indents instead of
 *   the current document/formatter method
 * @todo Indent block comments properly: See if the first char is "*", and if so, indent it one extra
 *   char somehow such that it lines up with the * in /*
 *
 * @author Tor Norbye
 * @author Martin Adamek
 */
public class JsFormatter implements org.netbeans.modules.csl.api.Formatter {
    private boolean embeddedJavaScript;
    private int embeddededIndent = 0;
    private int indentSize;
    private int continuationIndentSize;

    /**
     * <p>
     * Stack describing indentation of blocks defined by '{', '[' and blocks
     * with missing optional curly braces '{'. See also getBracketBalanceDelta()
     * </p>
     * For example:
     * <pre>
     * if (true)        // [ StackItem[block=true] ]
     *   if (true) {    // [ StackItem[block=true], StackItem[block=false] ]
     *     if (true)    // [ StackItem[block=true], StackItem[block=false], StackItem[block=true] ]
     *       foo();     // [ StackItem[block=true], StackItem[block=false] ]
     *     bar();       // [ StackItem[block=true], StackItem[block=false] ]
     *   }              // [ StackItem[block=true] ]
     * fooBar();        // [ ]
     * </pre>
     */
    private Stack<StackItem> stack = new Stack<StackItem>();

    public JsFormatter() {
    }
    
    public boolean needsParserResult() {
        return true;
    }

    public void reformat(Context context, CompilationInfo info) {
        indentSize = IndentUtils.indentLevelSize(context.document());
        continuationIndentSize = indentSize; // No separate setting for this yet!
        
        JsParseResult jsParseResult = null;
        if (info != null) {
            jsParseResult = AstUtilities.getParseResult(info);
        }
        
        Document document = context.document();
        if (jsParseResult == null || jsParseResult.getRootNode() == null || !(JsUtils.isJsOrJsonDocument(document))) {
            reindent(context, null, false);
            return;
        }
        final BaseDocument doc = (BaseDocument) document;
        
        int startOffset = context.startOffset();
        int endOffset = context.endOffset();
        
        final JsPretty jsPretty = new JsPretty(info, doc, startOffset, endOffset, indentSize, continuationIndentSize);
        jsPretty.format();
        
        doc.runAtomic(new Runnable() {
            public void run() {
                try {
                    ArrayList<Diff> diffs = jsPretty.getReverseDiffs();
                    Collections.sort(diffs);
                    for (int i = diffs.size()-1; i >= 0; i--) {
                        Diff diff = diffs.get(i);
                        if (diff.text != null && diff.text.length() == (diff.end-diff.start) && diff.end > diff.start) {
                            // See if we're replacing the exact same text we already have there
                            String s = doc.getText(diff.start, diff.end-diff.start);
                            if (s.equals(diff.text)) {
                                continue;
                            }
                        }
                        if (diff.end > diff.start) {
                            doc.remove(diff.start, diff.end - diff.start);
                        }
                        if (diff.text != null && diff.text.length() > 0) {
                            doc.insertString(diff.start, diff.text, null);
                        }
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
    
    public void reindent(Context context) {
        indentSize = IndentUtils.indentLevelSize(context.document());
        continuationIndentSize = indentSize; // No separate setting for this yet!

        reindent(context, null, true);
    }
    
    public int indentSize() {
        return -1; // Use IDE defaults
    }
    
    public int hangingIndentSize() {
        return -1; // Use IDE defaults
    }

    /** Compute the initial balance of brackets at the given offset. */
    private int getFormatStableStart(BaseDocument doc, int offset) {
        TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, offset);
        if (ts == null) {
            return 0;
        }

        ts.move(offset);

        if (!ts.movePrevious()) {
            return 0;
        }

        // Look backwards to find a suitable context - a class, module or method definition
        // which we will assume is properly indented and balanced
        do {
            Token<?extends JsTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == JsTokenId.FUNCTION) {
                return ts.offset();
            }
        } while (ts.movePrevious());

        if (embeddedJavaScript && !ts.movePrevious()) {
            // I may have moved to the front of an embedded JavaScript area, e.g. in
            // an attribute or in a <script> tag. If this is the end of the line,
            // go to the next line instead since the reindent code will go to the beginning
            // of the stable formatting start.
            int sequenceBegin = ts.offset();
            try {
                int lineTextEnd = Utilities.getRowLastNonWhite(doc, sequenceBegin);
                if (lineTextEnd == -1 || sequenceBegin > lineTextEnd) {
                    return Math.min(doc.getLength(), Utilities.getRowEnd(doc, sequenceBegin) + 1);
                }

            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }
        }

        return ts.offset();
    }
    
    private int getBracketBalanceDelta(TokenId id) {
        if (id == JsTokenId.LPAREN || id == JsTokenId.LBRACKET) {
            return 1;
        } else if (id == JsTokenId.RPAREN || id == JsTokenId.RBRACKET) {
            return -1;
        }
        return 0;
    }
    
    private int getTokenBalanceDelta(TokenId id, BaseDocument doc, TokenSequence<? extends JsTokenId> ts, boolean indentOnly) {
        try {
            OffsetRange range = OffsetRange.NONE;
            if (id == JsTokenId.LBRACKET || id == JsTokenId.LBRACE) {
                // block with braces, just record it to stack and return 1
                stack.push(new StackItem(false, new OffsetRange(ts.offset(), ts.offset())));
                return 1;
            } else if (id == JsTokenId.CASE || id == JsTokenId.DEFAULT) {
                
                int index = ts.index();
                
                // find colon ':'
                LexUtilities.findNextIncluding(ts, Collections.singletonList(JsTokenId.COLON));

                // skip whitespaces, comments and newlines
                Token<? extends JsTokenId> token = LexUtilities.findNextNonWsNonComment(ts);
                JsTokenId tokenId = token.id();
                
                if (tokenId == JsTokenId.CASE || tokenId == JsTokenId.DEFAULT) {
                    return 0;
                } else if (tokenId == JsTokenId.RBRACE) {
                    return -1;
                } else {
                    // look at the beginning of next line if there is case or default
                    LexUtilities.findNextIncluding(ts, Collections.singletonList(JsTokenId.EOL));
                    LexUtilities.findNextNonWsNonComment(ts);
                    if (ts.token().id() == JsTokenId.CASE || ts.token().id() == JsTokenId.DEFAULT) {
                        return 0;
                    }
                }

                ts.moveIndex(index);
                ts.moveNext();
                
                return 1;
            } else if (id == JsTokenId.RBRACKET || id == JsTokenId.RBRACE) {
                /*
                 * End of braces block.
                 * If we are not on same line where block started, try to push 
                 * all braceless blocks from stack and decrease indent for them,
                 * otherwise just decrese indent by 1.
                 * For example:
                 * if (true)
                 *   if (true)
                 *     if (true)
                 *       foo();     // we should decrease indent by 3 levels
                 * 
                 * but:
                 * if (true)
                 *   if (true)
                 *     if (map[0]) // at ']' we should decrease only by 1
                 *       foo();
                 */
                int delta = -1;
                StackItem lastPop = stack.empty() ? null : stack.pop();
                if (lastPop != null && lastPop.range.getStart() <= (doc.getLength() + 1) &&
                        Utilities.getLineOffset(doc, lastPop.range.getStart()) != Utilities.getLineOffset(doc, ts.offset())) {
                    int blocks = 0;
                    while (!stack.empty() && stack.pop().braceless) {
                        blocks++;
                    }
                    delta -= blocks;
                }
                return delta;
            } else if ((range = LexUtilities.getMultilineRange(doc, ts)) != OffsetRange.NONE) {
                // we found braceless block, let's record it in the stack
                stack.push(new StackItem(true, range));
            } else if (id == JsTokenId.EOL) {

                if (!indentOnly) {
                    TokenSequence<? extends JsTokenId> ts2 = LexUtilities.getPositionedSequence(doc, ts.offset());
                    // skip whitespaces and newlines
                    Token<? extends JsTokenId> nextToken = LexUtilities.findNextNonWsNonComment(ts2);
                    TokenId tokenId = nextToken == null ? null : nextToken.id();
                    if (tokenId == JsTokenId.RBRACE) {
                        // if it is end of 'switch'
                        OffsetRange offsetRange = LexUtilities.findBwd(doc, ts2, JsTokenId.LBRACE, JsTokenId.RBRACE);
                        if (offsetRange != OffsetRange.NONE) {
                            ts2.movePrevious();
                            if (LexUtilities.skipParenthesis(ts2, true)) {
                                Token<? extends JsTokenId> token = ts2.token();
                                token = LexUtilities.findPreviousNonWsNonComment(ts2);
                                if (token.id() == JsTokenId.SWITCH) {
                                    return -1;
                                }
                            }
                        }
                    } else if (tokenId == JsTokenId.CASE || tokenId == JsTokenId.DEFAULT) {
                        ts2 = LexUtilities.getPositionedSequence(doc, ts.offset());
                        Token<? extends JsTokenId> prevToken = LexUtilities.findPreviousNonWsNonComment(ts2);
                        if (prevToken.id() != JsTokenId.LBRACE) {
                            // it must be case or default
                            ts2 = LexUtilities.getPositionedSequence(doc, ts.offset());
                            prevToken = LexUtilities.findPreviousIncluding(ts2, 
                                    Arrays.asList(JsTokenId.CASE, JsTokenId.DEFAULT));
                            int beginLine = Utilities.getLineOffset(doc, ts2.offset());
                            int eolLine = Utilities.getLineOffset(doc, ts.offset());
                            if (beginLine != eolLine) {
                                return -1;
                            }
                        }
                    }
                }
                
                // other
                if (!stack.empty()) {
                    if (stack.peek().braceless) {
                        // end of line after braceless block start
                        OffsetRange stackOffset = stack.peek().range;
                        if (stackOffset.containsInclusive(ts.offset())) {
                            if (indentOnly) {
                                // enter pressed in braceless block
                                return 1;
                            }
                            // we are in the braceless block statement
                            int stackEndLine = Utilities.getLineOffset(doc, stackOffset.getEnd());
                            int offsetLine = Utilities.getLineOffset(doc, ts.offset());
                            if (stackEndLine == offsetLine) {
                                // if we are at the last line of braceless block statement
                                // increse indent by 1
                                return 1;
                            }
                        } else {
                            // we are not in braceless block statement,
                            // let's decrease indent for all braceless blocks in top of stack (if any)
                            int blocks = 0;
                            while (!stack.empty() && stack.peek().braceless) {
                                blocks++;
                                stack.pop();
                            }
                            return -blocks;
                        }
                    }
                }
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
        return 0;
    }
    
    @SuppressWarnings("unchecked")
    private int getTokenBalance(TokenSequence<? extends JsTokenId> ts, BaseDocument doc, final int begin, int end, boolean includeKeywords, boolean indentOnly) {
        int balance = 0;

        if (ts == null) {
            try {
                // remember indent of previous html tag
                embeddededIndent = Utilities.getRowIndent(doc, begin);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            return 0;
        }

        ts.move(begin);

        if (!ts.moveNext()) {
            return 0;
        }

        int last = begin;
        do {
            Token<?extends JsTokenId> token = ts.token();
            if (token == null) {
                break;
            }
            TokenId id = token.id();

            if (includeKeywords) {
                int delta = getTokenBalanceDelta(id, doc, ts, indentOnly);
                balance += delta;
            } else {
                balance += getBracketBalanceDelta(id);
            }
            last = ts.offset() + token.length();
        } while (ts.moveNext() && (ts.offset() < end));

        if (embeddedJavaScript && last < end) {
            // We're not done yet... find the next section...
            TokenSequence<? extends JsTokenId> ets = LexUtilities.getNextJsTokenSequence(doc, last+1, end);
            if (ets != null && ets.offset() > begin) {
                return balance + getTokenBalance(ets, doc, ets.offset(), end, includeKeywords, indentOnly);
            }
        }
        
        return balance;
    }
    
    /** 
     * Get the first token on the given line. Similar to LexUtilities.getToken(doc, lineBegin)
     * except (a) it computes the line begin from the offset itself, and more importantly,
     * (b) it handles RHTML tokens specially; e.g. if a line begins with
     * {@code
     *    <% if %>
     * }
     * then the "if" embedded token will be returned rather than the RHTML delimiter, or even
     * the whitespace token (which is the first Ruby token in the embedded sequence).
     *    
     * </pre>   
     */
    private Token<? extends JsTokenId> getFirstToken(BaseDocument doc, int offset) throws BadLocationException {
        int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);

        if (lineBegin != -1) {
            if (embeddedJavaScript) {
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, lineBegin);
                if (ts != null) {
                    ts.moveNext();
                    Token<?extends JsTokenId> token = ts.token();
                    while (token != null && token.id() == JsTokenId.WHITESPACE) {
                        if (!ts.moveNext()) {
                            return null;
                        }
                        token = ts.token();
                    }
                    return token;
                }
            } else {
                return LexUtilities.getToken(doc, lineBegin);
            }
        }
        
        return null;
    }
    
    private int isEndIndent(BaseDocument doc, int offset) throws BadLocationException {
        int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);

        if (lineBegin != -1) {
            Token<?extends JsTokenId> token = getFirstToken(doc, offset);
            
            if (token == null) {
                return 0;
            }
            
            TokenId id = token.id();

            // If the line starts with an end-marker, such as "end", "}", "]", etc.,
            // find the corresponding opening marker, and indent the line to the same
            // offset as the beginning of that line.
            if (/*(LexUtilities.isIndentToken(id) && !LexUtilities.isBeginToken(id, doc, offset)) || LexUtilities.isEndToken(id, doc, offset) ||*/
                id == JsTokenId.RBRACE || id == JsTokenId.RBRACKET || id == JsTokenId.RPAREN) {
                int indents = 1;
                
                // Check if there are multiple end markers here... if so increase indent level.
                // This should really do an iteration... for now just handling the most common
                // scenario in JavaScript where we have }) in object literals
                int lineEnd = Utilities.getRowEnd(doc, offset);
                int newOffset = offset;
                while (newOffset < lineEnd && token != null) {
                    newOffset = newOffset+token.length();
                    if (newOffset < doc.getLength()) {
                        token = LexUtilities.getToken(doc, newOffset);
                        if (token != null) {
                            id = token.id();
                            if (id == JsTokenId.WHITESPACE) {
                                continue;
                            /*} else if ((LexUtilities.isIndentToken(id) && !LexUtilities.isBeginToken(id, doc, offset)) || LexUtilities.isEndToken(id, doc, offset) ||
                                id == JsTokenId.RBRACE || id == JsTokenId.RBRACKET || id == JsTokenId.RPAREN) {
                                indents++;*/
                            } else {
                                break;
                            }
                        }
                    }
                }
                
                return indents;
            }
        }
        
        return 0;
    }
    
    private static boolean isLineContinued(BaseDocument doc, int offset, int bracketBalance) throws BadLocationException {
        // TODO RHTML - this isn't going to work for rhtml embedded strings...
        offset = Utilities.getRowLastNonWhite(doc, offset);
        if (offset == -1) {
            return false;
        }

        TokenSequence<?extends JsTokenId> ts = LexUtilities.getPositionedSequence(doc, offset);
        Token<?extends JsTokenId> token = (ts != null ? ts.token() : null);

        if (token != null) {
            TokenId id = token.id();
            
            // http://www.netbeans.org/issues/show_bug.cgi?id=115279
            boolean isContinuationOperator = (id == JsTokenId.NONUNARY_OP || id == JsTokenId.DOT);
            
            if (ts.offset() == offset && token.length() > 1 && token.text().toString().startsWith("\\")) {
                // Continued lines have different token types
                isContinuationOperator = true;
            }
            
            /* No line continuations with comma in JavaScrip - this misformats nested object literals
             * like those used in prototype and isn't necesary for real JavaScript code (since we
             * always have parentheses in parameter lists etc. to help with indentation
            if (token.length() == 1 && id == JsTokenId.IDENTIFIER && token.text().toString().equals(",")) {
                // If there's a comma it's a continuation operator, but inside arrays, hashes or parentheses
                // parameter lists we should not treat it as such since we'd "double indent" the items, and
                // NOT the first item (where there's no comma, e.g. you'd have
                //  foo(
                //    firstarg,
                //      secondarg,  # indented both by ( and hanging indent ,
                //      thirdarg)
                if (bracketBalance == 0) {
                    isContinuationOperator = true;
                }
            }
             */
            if (id == JsTokenId.NONUNARY_OP && ",".equals(token.text().toString())) {
                // If there's a comma it's a continuation operator, but inside arrays, hashes or parentheses
                // parameter lists we should not treat it as such since we'd "double indent" the items, and
                // NOT the first item (where there's no comma, e.g. you'd have
                //  foo(
                //    firstarg,
                //      secondarg,  # indented both by ( and hanging indent ,
                //      thirdarg)
                isContinuationOperator = (bracketBalance == 0);
            }
            
            if (id == JsTokenId.COLON) {
                TokenSequence<? extends JsTokenId> ts2 = LexUtilities.getPositionedSequence(doc, ts.offset());
                Token<? extends JsTokenId> foundToken = LexUtilities.findPreviousIncluding(ts2,
                        Arrays.asList(JsTokenId.CASE, JsTokenId.DEFAULT, JsTokenId.COLON));
                if (foundToken != null && (foundToken.id() == JsTokenId.CASE || foundToken.id() == JsTokenId.DEFAULT)) {
                    isContinuationOperator = false;
                } else {
                    isContinuationOperator = true;
                }
            }
            
//            if (isContinuationOperator) {
//                // Make sure it's not a case like this:
//                //    alias eql? ==
//                // or
//                //    def ==
//                token = LexUtilities.getToken(doc, Utilities.getRowFirstNonWhite(doc, offset));
//                if (token != null) {
//                    id = token.id();
//                    if (id == JsTokenId.DEF || id == JsTokenId.ANY_KEYWORD && token.text().toString().equals("alias")) { // NOI18N
//                        return false;
//                    }
//                }
//
//                return true;
//            } else if (id == JsTokenId.ANY_KEYWORD) {
//                String text = token.text().toString();
//                if ("or".equals(text) || "and".equals(text)) { // NOI18N
//                    return true;
//                }
//            }
            
            return isContinuationOperator;
        }
        
        
        return false;
    }

    private void reindent(final Context context, CompilationInfo info, final boolean indentOnly) {
        Document document = context.document();
        int startOffset = context.startOffset();
        int endOffset = context.endOffset();

        embeddedJavaScript = !JsUtils.isJsOrJsonDocument(document);
        
        try {
            final BaseDocument doc = (BaseDocument)document; // document.getText(0, document.getLength())

            if (indentOnly && embeddedJavaScript) {
                // Make sure we're not messing with indentation in HTML
                Token<? extends JsTokenId> token = LexUtilities.getToken(doc, startOffset);
                if (token == null) {
                    return;
                }
            }

            if (endOffset > doc.getLength()) {
                endOffset = doc.getLength();
            }
            
                startOffset = Utilities.getRowStart(doc, startOffset);
            final int lineStart = startOffset;//Utilities.getRowStart(doc, startOffset);
            int initialOffset = 0;
            int initialIndent = 0;
            if (startOffset > 0) {
                int prevOffset = Utilities.getRowStart(doc, startOffset-1);
                initialOffset = getFormatStableStart(doc, prevOffset);
                initialIndent = GsfUtilities.getLineIndent(doc, initialOffset);
            }
            
            // Build up a set of offsets and indents for lines where I know I need
            // to adjust the offset. I will then go back over the document and adjust
            // lines that are different from the intended indent. By doing piecemeal
            // replacements in the document rather than replacing the whole thing,
            // a lot of things will work better: breakpoints and other line annotations
            // will be left in place, semantic coloring info will not be temporarily
            // damaged, and the caret will stay roughly where it belongs.
            final List<Integer> offsets = new ArrayList<Integer>();
            final List<Integer> indents = new ArrayList<Integer>();

            // When we're formatting sections, include whitespace on empty lines; this
            // is used during live code template insertions for example. However, when
            // wholesale formatting a whole document, leave these lines alone.
            boolean indentEmptyLines = (startOffset != 0 || endOffset != doc.getLength());

            boolean includeEnd = endOffset == doc.getLength() || indentOnly;
            
            // TODO - remove initialbalance etc.
            computeIndents(doc, initialIndent, initialOffset, endOffset, info, 
                    offsets, indents, indentEmptyLines, includeEnd, indentOnly);
            
            doc.runAtomic(new Runnable() {
                public void run() {
                    try {
                        // Iterate in reverse order such that offsets are not affected by our edits
                        assert indents.size() == offsets.size();
                        for (int i = indents.size() - 1; i >= 0; i--) {
                            int indent = indents.get(i);
                            int lineBegin = offsets.get(i);

                            if (lineBegin < lineStart) {
                                // We're now outside the region that the user wanted reformatting;
                                // these offsets were computed to get the correct continuation context etc.
                                // for the formatter
                                break;
                            }

                            if (lineBegin == lineStart && i > 0) {
                                // Look at the previous line, and see how it's indented
                                // in the buffer.  If it differs from the computed position,
                                // offset my computed position (thus, I'm only going to adjust
                                // the new line position relative to the existing editing.
                                // This avoids the situation where you're inserting a newline
                                // in the middle of "incorrectly" indented code (e.g. different
                                // size than the IDE is using) and the newline position ending
                                // up "out of sync"
                                int prevOffset = offsets.get(i-1);
                                int prevIndent = indents.get(i-1);
                                int actualPrevIndent = GsfUtilities.getLineIndent(doc, prevOffset);
                                // NOTE: in embedding this is usually true as we have some nonzero initial indent,
                                // I am just not sure if it is better to add indentOnly check (as I did) or
                                // remove blank lines condition completely?
                                if (actualPrevIndent != prevIndent) {
                                    // For blank lines, indentation may be 0, so don't adjust in that case
                                    if (indentOnly || !(Utilities.isRowEmpty(doc, prevOffset) || Utilities.isRowWhite(doc, prevOffset))) {
                                        indent = actualPrevIndent + (indent-prevIndent);
                                    }
                                }
                            }

                            // Adjust the indent at the given line (specified by offset) to the given indent
                            int currentIndent = GsfUtilities.getLineIndent(doc, lineBegin);

                            if (currentIndent != indent && indent >= 0) {
                                //org.netbeans.editor.Formatter editorFormatter = doc.getFormatter();
                                //editorFormatter.changeRowIndent(doc, lineBegin, indent);
                                context.modifyIndent(lineBegin, indent);
                            }
                        }
                    } catch (BadLocationException ble) {
                        Exceptions.printStackTrace(ble);
                    }
                }
            });
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }

    public void computeIndents(BaseDocument doc, int initialIndent, int startOffset, int endOffset, CompilationInfo info,
            List<Integer> offsets,
            List<Integer> indents,
            boolean indentEmptyLines, boolean includeEnd, boolean indentOnly
        ) {
        // PENDING:
        // The reformatting APIs in NetBeans should be lexer based. They are still
        // based on the old TokenID apis. Once we get a lexer version, convert this over.
        // I just need -something- in place until that is provided.

        try {
            // Algorithm:
            // Iterate over the range.
            // Accumulate a token balance ( {,(,[, and keywords like class, case, etc. increases the balance, 
            //      },),] and "end" decreases it
            // If the line starts with an end marker, indent the line to the level AFTER the token
            // else indent the line to the level BEFORE the token (the level being the balance * indentationSize)
            // Compute the initial balance and indentation level and use that as a "base".
            // If the previous line is not "done" (ends with a comma or a binary operator like "+" etc.
            // add a "hanging indent" modifier.
            // At the end of the day, we're recording a set of line offsets and indents.
            // This can be used either to reformat the buffer, or indent a new line.

            // State:
            int offset = Utilities.getRowStart(doc, startOffset); // The line's offset
            int end = endOffset;


            // Pending - apply comment formatting too?

            // XXX Look up RHTML too
            //int indentSize = EditorOptions.get(RubyInstallation.RUBY_MIME_TYPE).getSpacesPerTab();
            //int hangingIndentSize = indentSize;
            

            // Build up a set of offsets and indents for lines where I know I need
            // to adjust the offset. I will then go back over the document and adjust
            // lines that are different from the intended indent. By doing piecemeal
            // replacements in the document rather than replacing the whole thing,
            // a lot of things will work better: breakpoints and other line annotations
            // will be left in place, semantic coloring info will not be temporarily
            // damaged, and the caret will stay roughly where it belongs.

            // The token balance at the offset
            int balance = 0;
            // The bracket balance at the offset ( parens, bracket, brace )
            int bracketBalance = 0;
            boolean continued = false;
//            boolean indentHtml = false;
//            if (embeddedJavaScript) {
//                indentHtml = codeStyle.indentHtml();
//            }
            
            //int originallockCommentIndention = 0;
            int adjustedBlockCommentIndention = 0;

            int endIndents;

            final int IN_CODE = 0;
            final int IN_LITERAL = 1;
            final int IN_BLOCK_COMMENT_START = 2;
            final int IN_BLOCK_COMMENT_MIDDLE = 3;

            while ((!includeEnd && offset < end) || (includeEnd && offset <= end)) {
                int indent; // The indentation to be used for the current line

                if (embeddedJavaScript) {
                    // now using JavaScript indent size to indent from <SCRIPT> tag; should it be HTML?
                    initialIndent = embeddededIndent + indentSize;
                }

                
                int lineType = IN_CODE;
                int pos = Utilities.getRowFirstNonWhite(doc, offset);
                TokenSequence<?extends JsTokenId> ts = null;

                if (pos != -1) {
                    // I can't look at the first position on the line, since
                    // for a string array that is indented, the indentation portion
                    // is recorded as a blank identifier
                    ts = LexUtilities.getPositionedSequence(doc, pos, false);

                    if (ts != null) {
                        TokenId id = ts.token().id();
                        // We don't have multiline string literals in JavaScript!
                        if (id == JsTokenId.BLOCK_COMMENT) {
                            if (ts.offset() == pos) {
                                lineType = IN_BLOCK_COMMENT_START;
                                //originallockCommentIndention = GsfUtilities.getLineIndent(doc, offset);
                            } else {
                                lineType =  IN_BLOCK_COMMENT_MIDDLE;
                            }
                        } else if (id == JsTokenId.NONUNARY_OP) {
                            // If a line starts with a non unary operator we can
                            // assume it's a continuation from a previous line
                            continued = true;
                        } else if (id == JsTokenId.STRING_LITERAL || id == JsTokenId.STRING_END ||
                                id == JsTokenId.REGEXP_LITERAL || id == JsTokenId.REGEXP_END) {
                            // You can get multiline literals in JavaScript by inserting a \ at the end
                            // of the line
                            lineType = IN_LITERAL;
                        }
                    } else {
                        // No ruby token -- leave the formatting alone!
                        // (Probably in an RHTML file on a line with no JavaScript)
                        lineType = IN_LITERAL;
                    }
                }
                
                int hangingIndent = continued ? (continuationIndentSize) : 0;

                if (lineType == IN_LITERAL) {
                    // Skip this line - leave formatting as it is prior to reformatting 
                    indent = GsfUtilities.getLineIndent(doc, offset);
                    
                    // No compound indent for JavaScript          
                    //                    if (embeddedJavaScript && indentHtml && balance > 0) {
                    //                        indent += balance * indentSize;
                    //                    }
                } else if (lineType == IN_BLOCK_COMMENT_MIDDLE) {
                    if (doc.getText(pos,1).charAt(0) == '*') {
                        // *-lines get indented to be flushed with the * in /*, other lines
                        // get indented to be aligned with the presumably indented text content!
                        //indent = LexUtilities.getLineIndent(doc, ts.offset())+1;
                        indent = adjustedBlockCommentIndention+1;
                    } else {
                        // Leave indentation of comment blocks alone since they probably correspond
                        // to commented out code - we don't want to lose the indentation.
                        // Possibly, I could shift the code all relative to the first line
                        // in the commented out block... A possible later enhancement.
                        // This shifts by the starting line which is wrong - should use the first comment line
                        //indent = LexUtilities.getLineIndent(doc, offset)-originallockCommentIndention+adjustedBlockCommentIndention;
                        indent = GsfUtilities.getLineIndent(doc, offset);
                    }
                } else if ((endIndents = isEndIndent(doc, offset)) > 0) {
                    indent = (balance-endIndents) * indentSize + hangingIndent + initialIndent;
                } else {
                    assert lineType == IN_CODE || lineType == IN_BLOCK_COMMENT_START;
                    indent = balance * indentSize + hangingIndent + initialIndent;
                    
//                    System.out.println("### indent " + indent + " = " + balance + " * " + indentSize + " + " + hangingIndent + " + " + initialIndent);
                    
                    if (lineType == IN_BLOCK_COMMENT_START) {
                        adjustedBlockCommentIndention = indent;
                    }
                }

                if (indent < 0) {
                    indent = 0;
                }
                
                int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);

                // Insert whitespace on empty lines too -- needed for abbreviations expansion
                if (lineBegin != -1 || indentEmptyLines) {
                    // Don't do a hanging indent if we're already indenting beyond the parent level?
                    
                    indents.add(Integer.valueOf(indent));
                    offsets.add(Integer.valueOf(offset));
                }

                int endOfLine = Utilities.getRowEnd(doc, offset) + 1;

                if (lineBegin != -1) {
                    balance += getTokenBalance(ts, doc, lineBegin, endOfLine, true, indentOnly);
                    int bracketDelta = getTokenBalance(ts, doc, lineBegin, endOfLine, false, indentOnly);
                    bracketBalance += bracketDelta;
                    continued = isLineContinued(doc, offset, bracketBalance);
                }

                offset = endOfLine;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }
    
    /**
     * One item in indent stack, see description of stack variable
     */
    private static final class StackItem {
        
        private StackItem(boolean braceless, OffsetRange range) {
            this.braceless = braceless;
            this.range = range;
        }
        
        /**
         * true for block without optional curly braces, false otherwise
         */
        private final boolean braceless;
        
        /**
         * For braceless blocks it is range from statement beginning (e.g. |if...)
         * to end of line where curly brace would be (e.g. if(...) |\n )<br>
         * For braces and brackets blocks it is offset of beginning of token for 
         * both - beginning and end of range (e.g. OffsetRange[ts.token(), ts.token()])
         */
        private final OffsetRange range;
        
        @Override
        public String toString() {
            return "StackItem[" + braceless + "," + range + "]";
        }
    }
    
}
