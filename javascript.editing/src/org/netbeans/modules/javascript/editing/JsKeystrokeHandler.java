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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.mozilla.nb.javascript.Node;
import org.netbeans.modules.csl.api.EditorOptions;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.openide.util.Exceptions;


/** 
 * Provide bracket completion for JavaScript.
 * This class provides three broad services:
 *  - Identifying matching pairs (parentheses, begin/end pairs etc.), which
 *    is used both for highlighting in the IDE (when the caret is on for example
 *    an if statement, the corresponding end token is highlighted), and navigation
 *    where you can jump between matching pairs.
 *  - Automatically inserting corresponding pairs when you insert a character.
 *    For example, if you insert a single quote, a corresponding ending quote
 *    is inserted - unless you're typing "over" the existing quote (you should
 *    be able to type foo = "hello" without having to arrow over the second
 *    quote that was inserted after you typed the first one).
 *  - Automatically adjusting indentation in some scenarios, for example
 *    when you type the final "d" in "end" - and readjusting it back to the
 *    original indentation if you continue typing something other than "end",
 *    e.g. "endian".
 *
 * @todo Pressing space in a comment beyond the textline limit should wrap text?
 *    http://ruby.netbeans.org/issues/show_bug.cgi?id=11553
 * @todo Pressing ; before a )\n should move it to after the ).
 *
 * @author Tor Norbye
 */
public class JsKeystrokeHandler implements KeystrokeHandler {
    /** When true, automatically reflows comments that are being edited according to the rdoc
     * conventions as well as the right hand side margin
     */
    //private static final boolean REFLOW_COMMENTS = Boolean.getBoolean("js.autowrap.comments"); // NOI18N

    /** When true, continue comments if you press return in a line comment (that does not
     * also have code on the same line 
     */
    static final boolean CONTINUE_COMMENTS = Boolean.getBoolean("js.cont.comment"); // NOI18N

    /** Tokens which indicate that we're within a literal string */
    private final static TokenId[] STRING_TOKENS = // XXX What about JsTokenId.STRING_BEGIN or QUOTED_STRING_BEGIN?
        {
            JsTokenId.STRING_LITERAL, JsTokenId.STRING_END
        };

    /** Tokens which indicate that we're within a regexp string */
    // XXX What about JsTokenId.REGEXP_BEGIN?
    private static final TokenId[] REGEXP_TOKENS = { JsTokenId.REGEXP_LITERAL, JsTokenId.REGEXP_END };

    /** When != -1, this indicates that we previously adjusted the indentation of the
     * line to the given offset, and if it turns out that the user changes that token,
     * we revert to the original indentation
     */
    private int previousAdjustmentOffset = -1;

    /** True iff we're processing bracket matching AFTER the key has been inserted rather than before  */
    private boolean isAfter;

    /**
     * The indentation to revert to when previousAdjustmentOffset is set and the token
     * changed
     */
    private int previousAdjustmentIndent;

    public JsKeystrokeHandler() {
    }
    
    public boolean isInsertMatchingEnabled(BaseDocument doc) {
        // The editor options code is calling methods on BaseOptions instead of looking in the settings map :(
        //Boolean b = ((Boolean)Settings.getValue(doc.getKitClass(), SettingsNames.PAIR_CHARACTERS_COMPLETION));
        //return b == null || b.booleanValue();
        EditorOptions options = EditorOptions.get(JsTokenId.JAVASCRIPT_MIME_TYPE);
        if (options != null) {
            return options.getMatchBrackets();
        }
        
        return true;
    }

    public int beforeBreak(Document document, int offset, JTextComponent target)
        throws BadLocationException {
        isAfter = false;
        
        Caret caret = target.getCaret();
        BaseDocument doc = (BaseDocument)document;
        
        boolean insertMatching = isInsertMatchingEnabled(doc);
        
        int lineBegin = Utilities.getRowStart(doc,offset);
        int lineEnd = Utilities.getRowEnd(doc,offset);
        
        if (lineBegin == offset && lineEnd == offset) {
            // Pressed return on a blank newline - do nothing
            return -1;
        }
        
        TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, offset);

        if (ts == null) {
            return -1;
        }

        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return -1;
        }

        Token<?extends JsTokenId> token = ts.token();
        TokenId id = token.id();

        // Insert an end statement? Insert a } marker?
        boolean[] insertRBraceResult = new boolean[1];
        int[] indentResult = new int[1];
        boolean insert = insertMatching &&
            isEndMissing(doc, offset, false, insertRBraceResult, null, indentResult);

        if (insert) {
            boolean insertRBrace = insertRBraceResult[0];
            int indent = indentResult[0];

            int afterLastNonWhite = Utilities.getRowLastNonWhite(doc, offset);

            // We've either encountered a further indented line, or a line that doesn't
            // look like the end we're after, so insert a matching end.
            StringBuilder sb = new StringBuilder();
            if (offset > afterLastNonWhite) {
                sb.append("\n"); // XXX On Windows, do \r\n?
                sb.append(IndentUtils.createIndentString(doc, indent));
            } else {
                // I'm inserting a newline in the middle of a sentence, such as the scenario in #118656
                // I should insert the end AFTER the text on the line
                String restOfLine = doc.getText(offset, Utilities.getRowEnd(doc, afterLastNonWhite)-offset);
                sb.append(restOfLine);
                sb.append("\n");
                sb.append(IndentUtils.createIndentString(doc, indent));
                doc.remove(offset, restOfLine.length());
            }
            
            if (insertRBrace) {
                sb.append("}"); // NOI18N
            }

            int insertOffset = offset;
            doc.insertString(insertOffset, sb.toString(), null);
            caret.setDot(insertOffset);
            
            return -1;
        }
        
        if (id == JsTokenId.ERROR) {
            // See if it's a block comment opener
            String text = token.text().toString();
            if (text.startsWith("/*") && ts.offset() == Utilities.getRowFirstNonWhite(doc, offset)) {
                int indent = GsfUtilities.getLineIndent(doc, offset);
                StringBuilder sb = new StringBuilder();
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append(" * "); // NOI18N
                int offsetDelta = sb.length()+1;
                sb.append("\n"); // NOI18N
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append(" */"); // NOI18N
                // TODO - possibly populate associated types in JS-doc style!
                //if (text.startsWith("/**")) {
                //    
                //}
                doc.insertString(offset, sb.toString(), null);
                caret.setDot(offset);
                return offset+offsetDelta;
            }
        }
        
        if (id == JsTokenId.STRING_LITERAL || 
                (id == JsTokenId.STRING_END) && offset < ts.offset()+ts.token().length()) {
            // Instead of splitting a string "foobar" into "foo"+"bar", just insert a \ instead!
            //int indent = GsfUtilities.getLineIndent(doc, offset);
            //int delimiterOffset = id == JsTokenId.STRING_END ? ts.offset() : ts.offset()-1;
            //char delimiter = doc.getText(delimiterOffset,1).charAt(0);
            //doc.insertString(offset, delimiter + " + " + delimiter, null);
            //caret.setDot(offset+3);
            //return offset + 5 + indent;
            String str = (id != JsTokenId.STRING_LITERAL || offset > ts.offset()) ? "\\n\\"  : "\\";
            doc.insertString(offset, str, null);
            caret.setDot(offset+str.length());
            return offset + 1 + str.length();
        }

        

        if (id == JsTokenId.REGEXP_LITERAL || 
                (id == JsTokenId.REGEXP_END) && offset < ts.offset()+ts.token().length()) {
            // Instead of splitting a string "foobar" into "foo"+"bar", just insert a \ instead!
            //int indent = GsfUtilities.getLineIndent(doc, offset);
            //doc.insertString(offset, "/ + /", null);
            //caret.setDot(offset+3);
            //return offset + 5 + indent;
            String str = (id != JsTokenId.REGEXP_LITERAL || offset > ts.offset()) ? "\\n\\"  : "\\";
            doc.insertString(offset, str, null);
            caret.setDot(offset+str.length());
            return offset + 1 + str.length();
        }
        
        // Special case: since I do hash completion, if you try to type
        //     y = Thread.start {
        //         code here
        //     }
        // you end up with
        //     y = Thread.start {|}
        // If you hit newline at this point, you end up with
        //     y = Thread.start {
        //     |}
        // which is not as helpful as it would be if we were not doing hash-matching
        // (in that case we'd notice the brace imbalance, and insert the closing
        // brace on the line below the insert position, and indent properly.
        // Catch this scenario and handle it properly.
        if ((id == JsTokenId.RBRACE || id == JsTokenId.RBRACKET) && offset > 0) {
            Token<? extends JsTokenId> prevToken = LexUtilities.getToken(doc, offset - 1);
            if (prevToken != null) {
                JsTokenId prevTokenId = prevToken.id();
                if (id == JsTokenId.RBRACE && prevTokenId == JsTokenId.LBRACE ||
                        id == JsTokenId.RBRACKET && prevTokenId == JsTokenId.LBRACKET) {
                    int indent = GsfUtilities.getLineIndent(doc, offset);
                    StringBuilder sb = new StringBuilder();
                    // XXX On Windows, do \r\n?
                    sb.append("\n"); // NOI18N
                    sb.append(IndentUtils.createIndentString(doc, indent));

                    int insertOffset = offset; // offset < length ? offset+1 : offset;
                    doc.insertString(insertOffset, sb.toString(), null);
                    caret.setDot(insertOffset);
                }
            }
        }
        
        if (id == JsTokenId.WHITESPACE) {
            // Pressing newline in the whitespace before a comment
            // should be identical to pressing newline with the caret
            // at the beginning of the comment
            int begin = Utilities.getRowFirstNonWhite(doc, offset);
            if (begin != -1 && offset < begin) {
                ts.move(begin);
                if (ts.moveNext()) {
                    id = ts.token().id();
                    if (id == JsTokenId.LINE_COMMENT) {
                        offset = begin;
                    }
                }
            }
        }
        
        if (id == JsTokenId.BLOCK_COMMENT && offset > ts.offset() && offset < ts.offset()+ts.token().length()) {
            // Continue *'s
            int begin = Utilities.getRowFirstNonWhite(doc, offset);
            int end = Utilities.getRowEnd(doc, offset)+1;
            if (begin == -1) {
                begin = end;
            }
            String line = doc.getText(begin, end-begin);
            boolean isBlockStart = line.startsWith("/*") || (begin != -1 && begin < ts.offset());
            if (isBlockStart || line.startsWith("*")) {
                int indent = GsfUtilities.getLineIndent(doc, offset);
                StringBuilder sb = new StringBuilder();
                if (isBlockStart) {
                    indent++;
                }
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append("*"); // NOI18N
                // Copy existing indentation
                int afterStar = isBlockStart ? begin+2 : begin+1;
                line = doc.getText(afterStar, Utilities.getRowEnd(doc, afterStar)-afterStar);
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c == ' ' || c == '\t') {
                        sb.append(c);
                    } else {
                        break;
                    }
                }

                int insertOffset = offset; // offset < length ? offset+1 : offset;
                if (offset == begin && insertOffset > 0) {
                    insertOffset = Utilities.getRowStart(doc, offset);                    
                    int sp = Utilities.getRowStart(doc, offset)+sb.length();
                    doc.insertString(insertOffset, sb.toString(), null);
                    caret.setDot(sp);
                    return sp;
                }
                doc.insertString(insertOffset, sb.toString(), null);
                caret.setDot(insertOffset);
                return insertOffset+sb.length()+1;
            }
        }
        
        boolean isComment = id == JsTokenId.LINE_COMMENT;
        if (id == JsTokenId.EOL) {
            if (ts.movePrevious() && ts.token().id() == JsTokenId.LINE_COMMENT) {
                //ts.moveNext();
                isComment = true;
            }
        }
        
        if (isComment) {
            // Only do this if the line only contains comments OR if there is content to the right on this line,
            // or if the next line is a comment!

            boolean continueComment = false;
            int begin = Utilities.getRowFirstNonWhite(doc, offset);

            // We should only continue comments if the previous line had a comment
            // (and a comment from the beginning, not a trailing comment)
            boolean previousLineWasComment = false;
            boolean nextLineIsComment = false;
            int rowStart = Utilities.getRowStart(doc, offset);
            if (rowStart > 0) {                
                int prevBegin = Utilities.getRowFirstNonWhite(doc, rowStart-1);
                if (prevBegin != -1) {
                    Token<? extends JsTokenId> firstToken = LexUtilities.getToken(doc, prevBegin);
                    if (firstToken != null && firstToken.id() == JsTokenId.LINE_COMMENT) {
                        previousLineWasComment = true;
                    }                
                }
            }
            int rowEnd = Utilities.getRowEnd(doc, offset);
            if (rowEnd < doc.getLength()) {
                int nextBegin = Utilities.getRowFirstNonWhite(doc, rowEnd+1);
                if (nextBegin != -1) {
                    Token<? extends JsTokenId> firstToken = LexUtilities.getToken(doc, nextBegin);
                    if (firstToken != null && firstToken.id() == JsTokenId.LINE_COMMENT) {
                        nextLineIsComment = true;
                    }                
                }
            }
            
            // See if we have more input on this comment line (to the right
            // of the inserted newline); if so it's a "split" operation on
            // the comment
            if (previousLineWasComment || nextLineIsComment || 
                    (offset > ts.offset() && offset < ts.offset()+ts.token().length())) {
                if (ts.offset()+token.length() > offset+1) {
                    // See if the remaining text is just whitespace
                    String trailing = doc.getText(offset,Utilities.getRowEnd(doc, offset)-offset);
                    if (trailing.trim().length() != 0) {
                        continueComment = true;
                    }
                } else if (CONTINUE_COMMENTS) {
                    // See if the "continue comments" options is turned on, and this is a line that
                    // contains only a comment (after leading whitespace)
                    Token<? extends JsTokenId> firstToken = LexUtilities.getToken(doc, begin);
                    if (firstToken.id() == JsTokenId.LINE_COMMENT) {
                        continueComment = true;
                    }
                }
                if (!continueComment) {
                    // See if the next line is a comment; if so we want to continue
                    // comments editing the middle of the comment
                    int nextLine = Utilities.getRowEnd(doc, offset)+1;
                    if (nextLine < doc.getLength()) {
                        int nextLineFirst = Utilities.getRowFirstNonWhite(doc, nextLine);
                        if (nextLineFirst != -1) {
                            Token<? extends JsTokenId> firstToken = LexUtilities.getToken(doc, nextLineFirst);
                            if (firstToken != null && firstToken.id() == JsTokenId.LINE_COMMENT) {
                                continueComment = true;
                            }
                        }
                    }
                }
            }
                
            if (continueComment) {
                // Line comments should continue
                int indent = GsfUtilities.getLineIndent(doc, offset);
                StringBuilder sb = new StringBuilder();
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append("//"); // NOI18N
                // Copy existing indentation
                int afterSlash = begin+2;
                String line = doc.getText(afterSlash, Utilities.getRowEnd(doc, afterSlash)-afterSlash);
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c == ' ' || c == '\t') {
                        sb.append(c);
                    } else {
                        break;
                    }
                }

                int insertOffset = offset; // offset < length ? offset+1 : offset;
                if (offset == begin && insertOffset > 0) {
                    insertOffset = Utilities.getRowStart(doc, offset);                    
                    int sp = Utilities.getRowStart(doc, offset)+sb.length();
                    doc.insertString(insertOffset, sb.toString(), null);
                    caret.setDot(sp);
                    return sp;
                }
                doc.insertString(insertOffset, sb.toString(), null);
                caret.setDot(insertOffset);
                return insertOffset+sb.length()+1;
            }
        }

        return -1;
    }

    /**
     * Determine if an "end" or "}" is missing following the caret offset.
     * The logic used is to check the text on the current line for block initiators
     * (e.g. "def", "for", "{" etc.) and then see if a corresponding close is
     * found after the same indentation level.
     *
     * @param doc The document to be checked
     * @param offset The offset of the current line
     * @param skipJunk If false, only consider the current line (of the offset)
     *   as the possible "block opener"; if true, look backwards across empty
     *   lines and comment lines as well.
     * @param insertRBraceResult Null, or a boolean 1-element array whose first
     *   element will be set to true iff this method determines that "}" should
     *   be inserted
     * @param startOffsetResult Null, or an integer 1-element array whose first
     *   element will be set to the starting offset of the opening block.
     * @param indentResult Null, or an integer 1-element array whose first
     *   element will be set to the indentation level "end" or "}" should be
     *   indented to when inserted.
     * @return true if something is missing; insertEndResult, insertRBraceResult
     *   and identResult will provide the more specific return values in their
     *   first elements.
     */
    private static boolean isEndMissing(BaseDocument doc, int offset, boolean skipJunk,
        boolean[] insertRBraceResult, int[] startOffsetResult,
        int[] indentResult) throws BadLocationException {
        int length = doc.getLength();

        // Insert an end statement? Insert a } marker?
        // Do so if the current line contains an unmatched begin marker,
        // AND a "corresponding" marker does not exist.
        // This will be determined as follows: Look forward, and check
        // that we don't have "indented" code already (tokens at an
        // indentation level higher than the current line was), OR that
        // there is no actual end or } coming up.
        if (startOffsetResult != null) {
            startOffsetResult[0] = Utilities.getRowFirstNonWhite(doc, offset);
        }

        int braceBalance =
            LexUtilities.getLineBalance(doc, offset, JsTokenId.LBRACE, JsTokenId.RBRACE);

        int parenBalance =
            LexUtilities.getLineBalance(doc, offset, JsTokenId.LPAREN, JsTokenId.RPAREN);

        if ((braceBalance == 1) && parenBalance >= 0) {
            // There is one more opening token on the line than a corresponding
            // closing token.  (If there's is more than one we don't try to help.)
            int indent = GsfUtilities.getLineIndent(doc, offset);

            // Look for the next nonempty line, and if its indent is > indent,
            // or if its line balance is -1 (e.g. it's an end) we're done
            boolean insertRBrace = braceBalance > 0;
            int next = Utilities.getRowEnd(doc, offset) + 1;

            for (; next < length; next = Utilities.getRowEnd(doc, next) + 1) {
                if (Utilities.isRowEmpty(doc, next) || Utilities.isRowWhite(doc, next) ||
                        LexUtilities.isCommentOnlyLine(doc, next)) {
                    continue;
                }

                int nextIndent = GsfUtilities.getLineIndent(doc, next);

                if (nextIndent > indent) {
                    insertRBrace = false;
                } else if (nextIndent == indent) {
                    if (insertRBrace &&
                            (LexUtilities.getLineBalance(doc, next, JsTokenId.LBRACE,
                                JsTokenId.RBRACE) < 0)) {
                        insertRBrace = false;
                    }
                }

                break;
            }

            if (insertRBraceResult != null) {
                insertRBraceResult[0] = insertRBrace;
            }

            if (indentResult != null) {
                indentResult[0] = indent;
            }

            return insertRBrace;
        }

        return false;
    }

    public boolean beforeCharInserted(Document document, int caretOffset, JTextComponent target, char ch)
        throws BadLocationException {
        isAfter = false;
        Caret caret = target.getCaret();
        BaseDocument doc = (BaseDocument)document;

        if (!isInsertMatchingEnabled(doc)) {
            return false;
        }
        
        //dumpTokens(doc, caretOffset);

        if (target.getSelectionStart() != -1) {
            if (GsfUtilities.isCodeTemplateEditing(doc)) {
                int start = target.getSelectionStart();
                int end = target.getSelectionEnd();
                if (start < end) {
                    target.setSelectionStart(start);
                    target.setSelectionEnd(start);
                    caretOffset = start;
                    caret.setDot(caretOffset);
                    doc.remove(start, end-start);
                }
                // Fall through to do normal insert matching work
            } else if (ch == '"' || ch == '\'' || ch == '(' || ch == '{' || ch == '[' || ch == '/') {
                // Bracket the selection
                String selection = target.getSelectedText();
                if (selection != null && selection.length() > 0) {
                    char firstChar = selection.charAt(0);
                    if (firstChar != ch) {
                        int start = target.getSelectionStart();
                        int end = target.getSelectionEnd();
                        TokenSequence<? extends JsTokenId> ts = LexUtilities.getPositionedSequence(doc, start);
                        if (ts != null && ts.token().id() != JsTokenId.STRING_LITERAL) { // Not inside strings!
                            int lastChar = selection.charAt(selection.length()-1);
                            // Replace the surround-with chars?
                            if (selection.length() > 1 && 
                                    ((firstChar == '"' || firstChar == '\'' || firstChar == '(' || 
                                    firstChar == '{' || firstChar == '[' || firstChar == '/') &&
                                    lastChar == matching(firstChar))) {
                                doc.remove(end-1, 1);
                                doc.insertString(end-1, ""+matching(ch), null);
                                doc.remove(start, 1);
                                doc.insertString(start, ""+ch, null);
                                target.getCaret().setDot(end);
                            } else {
                                // No, insert around
                                doc.remove(start,end-start);
                                doc.insertString(start, ch + selection + matching(ch), null);
                                target.getCaret().setDot(start+selection.length()+2);
                            }

                            return true;
                        }
                    }
                }
            }
        }

        TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        ts.move(caretOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<?extends JsTokenId> token = ts.token();
        TokenId id = token.id();
        TokenId[] stringTokens = null;
        TokenId beginTokenId = null;

        if (ch == '*' && id == JsTokenId.LINE_COMMENT && caretOffset == ts.offset()+1) {
            // Just typed "*" inside a "//" -- the user has typed "/", which automatched to
            // "//" and now they're typing "*" (e.g. to type "/*", but ended up with "/*/".
            // Remove the auto-matched /.
            doc.remove(caretOffset, 1);
            return false; // false: continue to insert the "*"
        }

        // "/" is handled AFTER the character has been inserted since we need the lexer's help
        if (ch == '\"' || ch == '\'') {
            stringTokens = STRING_TOKENS;
            beginTokenId = JsTokenId.STRING_BEGIN;
        } else if (id == JsTokenId.ERROR) {
            //String text = token.text().toString();

            ts.movePrevious();

            TokenId prevId = ts.token().id();

            if (prevId == JsTokenId.STRING_BEGIN) {
                stringTokens = STRING_TOKENS;
                beginTokenId = prevId;
            } else if (prevId == JsTokenId.REGEXP_BEGIN) {
                stringTokens = REGEXP_TOKENS;
                beginTokenId = JsTokenId.REGEXP_BEGIN;
            }
        } else if ((id == JsTokenId.STRING_BEGIN) &&
                (caretOffset == (ts.offset() + 1))) {
            if (!Character.isLetter(ch)) { // %q, %x, etc. Only %[], %!!, %<space> etc. is allowed
                stringTokens = STRING_TOKENS;
                beginTokenId = id;
            }
        } else if (((id == JsTokenId.STRING_BEGIN) && (caretOffset == (ts.offset() + 2))) ||
                (id == JsTokenId.STRING_END)) {
            stringTokens = STRING_TOKENS;
            beginTokenId = JsTokenId.STRING_BEGIN;
        } else if (((id == JsTokenId.REGEXP_BEGIN) && (caretOffset == (ts.offset() + 2))) ||
                (id == JsTokenId.REGEXP_END)) {
            stringTokens = REGEXP_TOKENS;
            beginTokenId = JsTokenId.REGEXP_BEGIN;
        }

        if (stringTokens != null) {
            boolean inserted =
                completeQuote(doc, caretOffset, caret, ch, stringTokens, beginTokenId);

            if (inserted) {
                caret.setDot(caretOffset + 1);

                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    // For debugging purposes
    // Probably obsolete - see the tokenspy utility in gsf debugging tools for better help
    //private void dumpTokens(BaseDocument doc, int dot) {
    //    TokenSequence< ?extends JsTokenId> ts = LexUtilities.getTokenSequence(doc);
    //
    //    System.out.println("Dumping tokens for dot=" + dot);
    //    int prevOffset = -1;
    //    if (ts != null) {
    //        ts.moveFirst();
    //        int index = 0;
    //        do {
    //            Token<? extends JsTokenId> token = ts.token();
    //            int offset = ts.offset();
    //            String id = token.id().toString();
    //            String text = token.text().toString().replaceAll("\n", "\\\\n");
    //            if (prevOffset < dot && dot <= offset) {
    //                System.out.print(" ===> ");
    //            }
    //            System.out.println("Token " + index + ": offset=" + offset + ": id=" + id + ": text=" + text);
    //            index++;
    //            prevOffset = offset;
    //        } while (ts.moveNext());
    //    }
    //}

    /**
     * A hook method called after a character was inserted into the
     * document. The function checks for special characters for
     * completion ()[]'"{} and other conditions and optionally performs
     * changes to the doc and or caret (complets braces, moves caret,
     * etc.)
     * @param document the document where the change occurred
     * @param dotPos position of the character insertion
     * @param target The target
     * @param ch the character that was inserted
     * @return Whether the insert was handled
     * @throws BadLocationException if dotPos is not correct
     */
    public boolean afterCharInserted(Document document, int dotPos, JTextComponent target, char ch)
        throws BadLocationException {
        isAfter = true;
        Caret caret = target.getCaret();
        BaseDocument doc = (BaseDocument)document;

//        if (REFLOW_COMMENTS) {
//            Token<?extends JsTokenId> token = LexUtilities.getToken(doc, dotPos);
//            if (token != null) {
//                TokenId id = token.id();
//                if (id == JsTokenId.LINE_COMMENT || id == JsTokenId.DOCUMENTATION) {
//                    new ReflowParagraphAction().reflowEditedComment(target);
//                }
//            }
//        }
        
        // See if our automatic adjustment of indentation when typing (for example) "end" was
        // premature - if you were typing a longer word beginning with one of my adjustment
        // prefixes, such as "endian", then put the indentation back.
        if (previousAdjustmentOffset != -1) {
            if (dotPos == previousAdjustmentOffset) {
                // Revert indentation iff the character at the insert position does
                // not start a new token (e.g. the previous token that we reindented
                // was not complete)
                TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, dotPos);

                if (ts != null) {
                    ts.move(dotPos);

                    if (ts.moveNext() && (ts.offset() < dotPos)) {
                        GsfUtilities.setLineIndentation(doc, dotPos, previousAdjustmentIndent);
                    }
                }
            }

            previousAdjustmentOffset = -1;
        }

        //dumpTokens(doc, dotPos);
        switch (ch) {
//        case '#': {
//            // Automatically insert #{^} when typing "#" in a quoted string or regexp
//            Token<?extends JsTokenId> token = LexUtilities.getToken(doc, dotPos);
//            if (token == null) {
//                return true;
//            }
//            TokenId id = token.id();
//
//            if (id == JsTokenId.QUOTED_STRING_LITERAL || id == JsTokenId.REGEXP_LITERAL) {
//                document.insertString(dotPos+1, "{}", null);
//                // Skip the "{" to place the caret between { and }
//                caret.setDot(dotPos+2);
//            }
//            break;
//        }
        case '}':
        case '{':
        case ')':
        case ']':
        case '(':
        case '[': {
            
            if (!isInsertMatchingEnabled(doc)) {
                return false;
            }

            
            Token<?extends JsTokenId> token = LexUtilities.getToken(doc, dotPos);
            if (token == null) {
                return true;
            }
            TokenId id = token.id();

            if (id == JsTokenId.ANY_OPERATOR) {
                int length = token.length();
                String s = token.text().toString();
                if ((length == 2) && "[]".equals(s) || "[]=".equals(s)) { // Special case
                    skipClosingBracket(doc, caret, ch, JsTokenId.RBRACKET);

                    return true;
                }
            }

            if (((id == JsTokenId.IDENTIFIER) && (token.length() == 1)) ||
                    (id == JsTokenId.LBRACKET) || (id == JsTokenId.RBRACKET) ||
                    (id == JsTokenId.LBRACE) || (id == JsTokenId.RBRACE) ||
                    (id == JsTokenId.LPAREN) || (id == JsTokenId.RPAREN)) {
                if (ch == ']') {
                    skipClosingBracket(doc, caret, ch, JsTokenId.RBRACKET);
                } else if (ch == ')') {
                    skipClosingBracket(doc, caret, ch, JsTokenId.RPAREN);
                } else if (ch == '}') {
                    skipClosingBracket(doc, caret, ch, JsTokenId.RBRACE);
                } else if ((ch == '[') || (ch == '(') || (ch == '{')) {
                    completeOpeningBracket(doc, dotPos, caret, ch);
                }
            }

            // Reindent blocks (won't do anything if } is not at the beginning of a line
            if (ch == '}') {
                reindent(doc, dotPos, JsTokenId.RBRACE, caret);
            } else if (ch == ']') {
                reindent(doc, dotPos, JsTokenId.RBRACKET, caret);
            }
        }

        break;

//        case 'e':
//            // See if it's the end of an "else" or an "ensure" - if so, reindent
//            reindent(doc, dotPos, JsTokenId.ELSE, caret);
//            reindent(doc, dotPos, JsTokenId.ENSURE, caret);
//            reindent(doc, dotPos, JsTokenId.RESCUE, caret);
//
//            break;
//
//        case 'f':
//            // See if it's the end of an "else" - if so, reindent
//            reindent(doc, dotPos, JsTokenId.ELSIF, caret);
//
//            break;
//
//        case 'n':
//            // See if it's the end of an "when" - if so, reindent
//            reindent(doc, dotPos, JsTokenId.WHEN, caret);
//            
//            break;
            
        case '/': {
            if (!isInsertMatchingEnabled(doc)) {
                return false;
            }

            // Bracket matching for regular expressions has to be done AFTER the
            // character is inserted into the document such that I can use the lexer
            // to determine whether it's a division (e.g. x/y) or a regular expression (/foo/)
            TokenSequence<?extends JsTokenId> ts = LexUtilities.getPositionedSequence(doc, dotPos);
            if (ts != null) {
                Token token = ts.token();
                TokenId id = token.id();

                if (id == JsTokenId.LINE_COMMENT) {
                    // Did you just type "//" - make sure this didn't turn into ///
                    // where typing the first "/" inserted "//" and the second "/" appended
                    // another "/" to make "///"
                    if (dotPos == ts.offset()+1 && dotPos+1 < doc.getLength() &&
                            doc.getText(dotPos+1,1).charAt(0) == '/') {
                        doc.remove(dotPos, 1);
                        caret.setDot(dotPos+1);
                        return true;
                    }
                }
                if (id == JsTokenId.REGEXP_BEGIN || id == JsTokenId.REGEXP_END) {
                    TokenId[] stringTokens = REGEXP_TOKENS;
                    TokenId beginTokenId = JsTokenId.REGEXP_BEGIN;

                    boolean inserted =
                        completeQuote(doc, dotPos, caret, ch, stringTokens, beginTokenId);

                    if (inserted) {
                        caret.setDot(dotPos + 1);
                    }

                    return inserted;
                }
            }
            break;
        }
        }

        return true;
    }
    
    private void reindent(BaseDocument doc, int offset, TokenId id, Caret caret)
        throws BadLocationException {
        TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, offset);

        if (ts != null) {
            ts.move(offset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }

            Token<?extends JsTokenId> token = ts.token();

            if ((token.id() == id)) {
                final int rowFirstNonWhite = Utilities.getRowFirstNonWhite(doc, offset);
                // Ensure that this token is at the beginning of the line
                if (ts.offset() > rowFirstNonWhite) {
//                    if (RubyUtils.isRhtmlDocument(doc)) {
//                        // Allow "<%[whitespace]*" to preceed
//                        String s = doc.getText(rowFirstNonWhite, ts.offset()-rowFirstNonWhite);
//                        if (!s.matches("<%\\s*")) {
//                            return;
//                        }
//                    } else {
                        return;
//                    }
                }

                OffsetRange begin = OffsetRange.NONE;

                if (id == JsTokenId.RBRACE) {
                    begin = LexUtilities.findBwd(doc, ts, JsTokenId.LBRACE, JsTokenId.RBRACE);
                } else if (id == JsTokenId.RBRACKET) {
                    begin = LexUtilities.findBwd(doc, ts, JsTokenId.LBRACKET, JsTokenId.RBRACKET);
                }

                if (begin != OffsetRange.NONE) {
                    int beginOffset = begin.getStart();
                    int indent = GsfUtilities.getLineIndent(doc, beginOffset);
                    previousAdjustmentIndent = GsfUtilities.getLineIndent(doc, offset);
                    GsfUtilities.setLineIndentation(doc, offset, indent);
                    previousAdjustmentOffset = caret.getDot();
                }
            }
        }
    }
    
    /** Replaced by JsBracesMatcher */
    public OffsetRange findMatching(Document document, int offset /*, boolean simpleSearch*/) {
        return OffsetRange.NONE;
    }
    
    /**
    * Hook called after a character *ch* was backspace-deleted from
    * *doc*. The function possibly removes bracket or quote pair if
    * appropriate.
    * @param doc the document
    * @param dotPos position of the change
    * @param caret caret
    * @param ch the character that was deleted
    */
    @SuppressWarnings("fallthrough")
    public boolean charBackspaced(Document document, int dotPos, JTextComponent target, char ch)
        throws BadLocationException {
        BaseDocument doc = (BaseDocument)document;
        
        switch (ch) {
        case ' ': {
            // Backspacing over "// " ? Delete the "//" too!
            TokenSequence<?extends JsTokenId> ts = LexUtilities.getPositionedSequence(doc, dotPos);
            if (ts != null && ts.token().id() == JsTokenId.LINE_COMMENT) {
                if (ts.offset() == dotPos-2) {
                    doc.remove(dotPos-2, 2);
                    target.getCaret().setDot(dotPos-2);
                
                    return true;
                }
            }
            break;
        }

        case '{':
        case '(':
        case '[': { // and '{' via fallthrough
            char tokenAtDot = LexUtilities.getTokenChar(doc, dotPos);

            if (((tokenAtDot == ']') &&
                    (LexUtilities.getTokenBalance(doc, JsTokenId.LBRACKET, JsTokenId.RBRACKET, dotPos) != 0)) ||
                    ((tokenAtDot == ')') &&
                    (LexUtilities.getTokenBalance(doc, JsTokenId.LPAREN, JsTokenId.RPAREN, dotPos) != 0)) ||
                    ((tokenAtDot == '}') &&
                    (LexUtilities.getTokenBalance(doc, JsTokenId.LBRACE, JsTokenId.RBRACE, dotPos) != 0))) {
                doc.remove(dotPos, 1);
            }
            break;
        }
        
        case '/': {
            // Backspacing over "//" ? Delete the whole "//"
            TokenSequence<?extends JsTokenId> ts = LexUtilities.getPositionedSequence(doc, dotPos);
            if (ts != null && ts.token().id() == JsTokenId.REGEXP_BEGIN) {
                if (ts.offset() == dotPos-1) {
                    doc.remove(dotPos-1, 1);
                    target.getCaret().setDot(dotPos-1);
                
                    return true;
                }
            }
            // Fallthrough for match-deletion
        }
        case '|':
        case '\"':
        case '\'': {
            char[] match = doc.getChars(dotPos, 1);

            if ((match != null) && (match[0] == ch)) {
                doc.remove(dotPos, 1);
            }
        } // TODO: Test other auto-completion chars, like %q-foo-
        }
        return true;
    }

    /**
     * A hook to be called after closing bracket ) or ] was inserted into
     * the document. The method checks if the bracket should stay there
     * or be removed and some exisitng bracket just skipped.
     *
     * @param doc the document
     * @param dotPos position of the inserted bracket
     * @param caret caret
     * @param bracket the bracket character ']' or ')'
     */
    private void skipClosingBracket(BaseDocument doc, Caret caret, char bracket, TokenId bracketId)
        throws BadLocationException {
        int caretOffset = caret.getDot();

        if (isSkipClosingBracket(doc, caretOffset, bracketId)) {
            doc.remove(caretOffset - 1, 1);
            caret.setDot(caretOffset); // skip closing bracket
        }
    }

    /**
     * Check whether the typed bracket should stay in the document
     * or be removed.
     * <br>
     * This method is called by <code>skipClosingBracket()</code>.
     *
     * @param doc document into which typing was done.
     * @param caretOffset
     */
    private boolean isSkipClosingBracket(BaseDocument doc, int caretOffset, TokenId bracketId)
        throws BadLocationException {
        // First check whether the caret is not after the last char in the document
        // because no bracket would follow then so it could not be skipped.
        if (caretOffset == doc.getLength()) {
            return false; // no skip in this case
        }

        boolean skipClosingBracket = false; // by default do not remove

        TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        // XXX BEGIN TOR MODIFICATIONS
        //ts.move(caretOffset+1);
        ts.move(caretOffset);

        if (!ts.moveNext()) {
            return false;
        }

        Token<?extends JsTokenId> token = ts.token();

        // Check whether character follows the bracket is the same bracket
        if ((token != null) && (token.id() == bracketId)) {
            int bracketIntId = bracketId.ordinal();
            int leftBracketIntId =
                (bracketIntId == JsTokenId.RPAREN.ordinal()) ? JsTokenId.LPAREN.ordinal()
                                                               : JsTokenId.LBRACKET.ordinal();

            // Skip all the brackets of the same type that follow the last one
            ts.moveNext();

            Token<?extends JsTokenId> nextToken = ts.token();

            while ((nextToken != null) && (nextToken.id() == bracketId)) {
                token = nextToken;

                if (!ts.moveNext()) {
                    break;
                }

                nextToken = ts.token();
            }

            // token var points to the last bracket in a group of two or more right brackets
            // Attempt to find the left matching bracket for it
            // Search would stop on an extra opening left brace if found
            int braceBalance = 0; // balance of '{' and '}'
            int bracketBalance = -1; // balance of the brackets or parenthesis
            Token<?extends JsTokenId> lastRBracket = token;
            ts.movePrevious();
            token = ts.token();

            boolean finished = false;

            while (!finished && (token != null)) {
                int tokenIntId = token.id().ordinal();

                if ((token.id() == JsTokenId.LPAREN) || (token.id() == JsTokenId.LBRACKET)) {
                    if (tokenIntId == bracketIntId) {
                        bracketBalance++;

                        if (bracketBalance == 0) {
                            if (braceBalance != 0) {
                                // Here the bracket is matched but it is located
                                // inside an unclosed brace block
                                // e.g. ... ->( } a()|)
                                // which is in fact illegal but it's a question
                                // of what's best to do in this case.
                                // We chose to leave the typed bracket
                                // by setting bracketBalance to 1.
                                // It can be revised in the future.
                                bracketBalance = 1;
                            }

                            finished = true;
                        }
                    }
                } else if ((token.id() == JsTokenId.RPAREN) ||
                        (token.id() == JsTokenId.RBRACKET)) {
                    if (tokenIntId == bracketIntId) {
                        bracketBalance--;
                    }
                } else if (token.id() == JsTokenId.LBRACE) {
                    braceBalance++;

                    if (braceBalance > 0) { // stop on extra left brace
                        finished = true;
                    }
                } else if (token.id() == JsTokenId.RBRACE) {
                    braceBalance--;
                }

                if (!ts.movePrevious()) {
                    break;
                }

                token = ts.token();
            }

            if (bracketBalance != 0) { // not found matching bracket
                                       // Remove the typed bracket as it's unmatched
                skipClosingBracket = true;
            } else { // the bracket is matched
                     // Now check whether the bracket would be matched
                     // when the closing bracket would be removed
                     // i.e. starting from the original lastRBracket token
                     // and search for the same bracket to the right in the text
                     // The search would stop on an extra right brace if found
                braceBalance = 0;
                bracketBalance = 1; // simulate one extra left bracket

                //token = lastRBracket.getNext();
                TokenHierarchy<BaseDocument> th = TokenHierarchy.get(doc);

                int ofs = lastRBracket.offset(th);

                ts.move(ofs);
                ts.moveNext();
                token = ts.token();
                finished = false;

                while (!finished && (token != null)) {
                    //int tokenIntId = token.getTokenID().getNumericID();
                    if ((token.id() == JsTokenId.LPAREN) || (token.id() == JsTokenId.LBRACKET)) {
                        if (token.id().ordinal() == leftBracketIntId) {
                            bracketBalance++;
                        }
                    } else if ((token.id() == JsTokenId.RPAREN) ||
                            (token.id() == JsTokenId.RBRACKET)) {
                        if (token.id().ordinal() == bracketIntId) {
                            bracketBalance--;

                            if (bracketBalance == 0) {
                                if (braceBalance != 0) {
                                    // Here the bracket is matched but it is located
                                    // inside an unclosed brace block
                                    // which is in fact illegal but it's a question
                                    // of what's best to do in this case.
                                    // We chose to leave the typed bracket
                                    // by setting bracketBalance to -1.
                                    // It can be revised in the future.
                                    bracketBalance = -1;
                                }

                                finished = true;
                            }
                        }
                    } else if (token.id() == JsTokenId.LBRACE) {
                        braceBalance++;
                    } else if (token.id() == JsTokenId.RBRACE) {
                        braceBalance--;

                        if (braceBalance < 0) { // stop on extra right brace
                            finished = true;
                        }
                    }

                    //token = token.getPrevious(); // done regardless of finished flag state
                    if (!ts.movePrevious()) {
                        break;
                    }

                    token = ts.token();
                }

                // If bracketBalance == 0 the bracket would be matched
                // by the bracket that follows the last right bracket.
                skipClosingBracket = (bracketBalance == 0);
            }
        }

        return skipClosingBracket;
    }

    /**
     * Check for various conditions and possibly add a pairing bracket
     * to the already inserted.
     * @param doc the document
     * @param dotPos position of the opening bracket (already in the doc)
     * @param caret caret
     * @param bracket the bracket that was inserted
     */
    private void completeOpeningBracket(BaseDocument doc, int dotPos, Caret caret, char bracket)
        throws BadLocationException {
        if (isCompletablePosition(doc, dotPos + 1)) {
            String matchingBracket = "" + matching(bracket);
            doc.insertString(dotPos + 1, matchingBracket, null);
            caret.setDot(dotPos + 1);
        }
    }

    // XXX TODO Use embedded string sequence here and see if it
    // really is escaped. I know where those are!
    // TODO Adjust for JavaScript
    private boolean isEscapeSequence(BaseDocument doc, int dotPos)
        throws BadLocationException {
        if (dotPos <= 0) {
            return false;
        }

        char previousChar = doc.getChars(dotPos - 1, 1)[0];

        return previousChar == '\\';
    }

    /**
     * Check for conditions and possibly complete an already inserted
     * quote .
     * @param doc the document
     * @param dotPos position of the opening bracket (already in the doc)
     * @param caret caret
     * @param bracket the character that was inserted
     */
    private boolean completeQuote(BaseDocument doc, int dotPos, Caret caret, char bracket,
        TokenId[] stringTokens, TokenId beginToken) throws BadLocationException {
        if (isEscapeSequence(doc, dotPos)) { // \" or \' typed

            return false;
        }

        // Examine token at the caret offset
        if (doc.getLength() < dotPos) {
            return false;
        }

        TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, dotPos);

        if (ts == null) {
            return false;
        }

        ts.move(dotPos);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<?extends JsTokenId> token = ts.token();
        Token<?extends JsTokenId> previousToken = null;

        if (ts.movePrevious()) {
            previousToken = ts.token();
        }

        int lastNonWhite = Utilities.getRowLastNonWhite(doc, dotPos);

        // eol - true if the caret is at the end of line (ignoring whitespaces)
        boolean eol = lastNonWhite < dotPos;

        if ((token.id() == JsTokenId.BLOCK_COMMENT) || (token.id() == JsTokenId.LINE_COMMENT)) {
            return false;
        } else if ((token.id() == JsTokenId.WHITESPACE) && eol && ((dotPos - 1) > 0)) {
            // check if the caret is at the very end of the line comment
            token = LexUtilities.getToken(doc, dotPos - 1);

            if (token.id() == JsTokenId.LINE_COMMENT) {
                return false;
            }
        }

        boolean completablePosition = isQuoteCompletablePosition(doc, dotPos);

        boolean insideString = false;
        TokenId id = token.id();

        for (TokenId currId : stringTokens) {
            if (id == currId) {
                insideString = true;
                break;
            }
        }

        if ((id == JsTokenId.ERROR) && (previousToken != null) &&
                (previousToken.id() == beginToken)) {
            insideString = true;
        }
        
        if (id == JsTokenId.EOL && previousToken != null) {
            if (previousToken.id() == beginToken) {
                insideString = true;
            } else if (previousToken.id() == JsTokenId.ERROR) {
                if (ts.movePrevious()) {
                    if (ts.token().id() == beginToken) {
                        insideString = true;
                    }
                }
            }
        }

        if (!insideString) {
            // check if the caret is at the very end of the line and there
            // is an unterminated string literal
            if ((token.id() == JsTokenId.WHITESPACE) && eol) {
                if ((dotPos - 1) > 0) {
                    token = LexUtilities.getToken(doc, dotPos - 1);
                    // XXX TODO use language embedding to handle this
                    insideString = (token.id() == JsTokenId.STRING_LITERAL);
                }
            }
        }

        if (insideString) {
            if (eol) {
                return false; // do not complete
            } else {
                //#69524
                char chr = doc.getChars(dotPos, 1)[0];

                if (chr == bracket) {
                    if (!isAfter) {
                        doc.insertString(dotPos, "" + bracket, null); //NOI18N
                    } else {
                        if (!(dotPos < doc.getLength()-1 && doc.getText(dotPos+1,1).charAt(0) == bracket)) {
                            return true;
                        }
                    }
 
                    doc.remove(dotPos, 1);

                    return true;
                }
            }
        }

        if ((completablePosition && !insideString) || eol) {
            doc.insertString(dotPos, "" + bracket + (isAfter ? "" : matching(bracket)), null); //NOI18N

            return true;
        }

        return false;
    }
    
    /**
     * Checks whether dotPos is a position at which bracket and quote
     * completion is performed. Brackets and quotes are not completed
     * everywhere but just at suitable places .
     * @param doc the document
     * @param dotPos position to be tested
     */
    private boolean isCompletablePosition(BaseDocument doc, int dotPos)
        throws BadLocationException {
        if (dotPos == doc.getLength()) { // there's no other character to test

            return true;
        } else {
            // test that we are in front of ) , " or '
            char chr = doc.getChars(dotPos, 1)[0];

            return ((chr == ')') || (chr == ',') || (chr == '\"') || (chr == '\'') || (chr == ' ') ||
            (chr == ']') || (chr == '}') || (chr == '\n') || (chr == '\t') || (chr == ';'));
        }
    }

    private boolean isQuoteCompletablePosition(BaseDocument doc, int dotPos)
        throws BadLocationException {
        if (dotPos == doc.getLength()) { // there's no other character to test

            return true;
        } else {
            // test that we are in front of ) , " or ' ... etc.
            int eol = Utilities.getRowEnd(doc, dotPos);

            if ((dotPos == eol) || (eol == -1)) {
                return false;
            }

            int firstNonWhiteFwd = Utilities.getFirstNonWhiteFwd(doc, dotPos, eol);

            if (firstNonWhiteFwd == -1) {
                return false;
            }

            char chr = doc.getChars(firstNonWhiteFwd, 1)[0];

//            if (chr == '%' && RubyUtils.isRhtmlDocument(doc)) {
//                return true;
//            }

            return ((chr == ')') || (chr == ',') || (chr == '+') || (chr == '}') || (chr == ';') ||
               (chr == ']') || (chr == '/'));
        }
    }

    /**
     * Returns for an opening bracket or quote the appropriate closing
     * character.
     */
    private char matching(char bracket) {
        switch (bracket) {
        case '(':
            return ')';

        case '/':
            return '/';

        case '[':
            return ']';

        case '\"':
            return '\"'; // NOI18N

        case '\'':
            return '\'';

        case '{':
            return '}';

        case '}':
            return '{';

        default:
            return bracket;
        }
    }

    public List<OffsetRange> findLogicalRanges(ParserResult info, int caretOffset) {
        JsParseResult jspr = AstUtilities.getParseResult(info);
        if (jspr == null || jspr.getRootNode() == null) {
            return Collections.emptyList();
        }

        int astOffset = AstUtilities.getAstOffset(info, caretOffset);
        if (astOffset == -1) {
            return Collections.emptyList();
        }

        AstPath path = new AstPath(jspr.getRootNode(), astOffset);
        List<OffsetRange> ranges = new ArrayList<OffsetRange>();
        
        // Check if the caret is within a comment, and if so insert a new
        // leaf "node" which contains the comment line and then comment block
        try {
            TokenSequence<?extends JsTokenId> ts = LexUtilities.getPositionedSequence(info.getSnapshot(), astOffset);
            if (ts != null) {
                Token<?extends JsTokenId> token = ts.token();

                if (token != null && token.id() == JsTokenId.BLOCK_COMMENT) {
                    // First add a range for the current line
                    int begin = ts.offset();
                    int end = begin+token.length();
                    ranges.add(new OffsetRange(begin, end));
                } else if ((token != null) && (token.id() == JsTokenId.LINE_COMMENT)) {
                    CharSequence text = info.getSnapshot().getText();

                    // First add a range for the current line
                    int begin = GsfUtilities.getRowStart(text, astOffset);
                    int end = GsfUtilities.getRowEnd(text, astOffset);

                    if (LexUtilities.isCommentOnlyLine(info.getSnapshot(), astOffset)) {
                        ranges.add(new OffsetRange(
                            GsfUtilities.getRowFirstNonWhite(text, begin),
                            GsfUtilities.getRowLastNonWhite(text, end)+1)
                        );

                        int lineBegin = begin;
                        int lineEnd = end;

                        while (begin > 0) {
                            int newBegin = GsfUtilities.getRowStart(text, begin - 1);

                            if ((newBegin < 0) || !LexUtilities.isCommentOnlyLine(info.getSnapshot(), newBegin)) {
                                begin = GsfUtilities.getRowFirstNonWhite(text, begin);
                                break;
                            }

                            begin = newBegin;
                        }

                        while (true) {
                            int newEnd = GsfUtilities.getRowEnd(text, end + 1);

                            if ((newEnd >= text.length()) || !LexUtilities.isCommentOnlyLine(info.getSnapshot(), newEnd)) {
                                end = GsfUtilities.getRowLastNonWhite(text, end)+1;
                                break;
                            }

                            end = newEnd;
                        }

                        if ((lineBegin > begin) || (lineEnd < end)) {
                            ranges.add(new OffsetRange(begin, end));
                        }
                    } else {
                        // It's just a line comment next to some code; select the comment
                        int offset = token.offset(info.getSnapshot().getTokenHierarchy());
                        ranges.add(new OffsetRange(offset, offset + token.length()));
                    }
                }
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
            return ranges;
        }

        Iterator<Node> it = path.leafToRoot();

        OffsetRange previous = OffsetRange.NONE;
        while (it.hasNext()) {
            Node node = it.next();

//            // Filter out some uninteresting nodes
//            if (node instanceof NewlineNode) {
//                continue;
//            }

            OffsetRange range = AstUtilities.getRange(node);
            
            // The contains check should be unnecessary, but I end up getting
            // some weird positions for some Rhino AST nodes
            if (range.containsInclusive(astOffset) && !range.equals(previous)) {
                range = LexUtilities.getLexerOffsets(jspr, range);
                if (range != OffsetRange.NONE) {
                    ranges.add(range);
                    previous = range;
                }
            }
        }

        return ranges;
    }

    // UGH - this method has gotten really ugly after successive refinements based on unit tests - consider cleaning up
    public int getNextWordOffset(Document document, int offset, boolean reverse) {
        BaseDocument doc = (BaseDocument)document;
        TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, offset);
        if (ts == null) {
            return -1;
        }
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return -1;
        }
        if (reverse && ts.offset() == offset) {
            if (!ts.movePrevious()) {
                return -1;
            }
        }

        Token<? extends JsTokenId> token = ts.token();
        TokenId id = token.id();

        if (id == JsTokenId.WHITESPACE) {
            // Just eat up the space in the normal IDE way
            if ((reverse && ts.offset() < offset) || (!reverse && ts.offset() > offset)) {
                return ts.offset();
            }
            while (id == JsTokenId.WHITESPACE) {
                if (reverse && !ts.movePrevious()) {
                    return -1;
                } else if (!reverse && !ts.moveNext()) {
                    return -1;
                }

                token = ts.token();
                id = token.id();
            }
            if (reverse) {
                int start = ts.offset()+token.length();
                if (start < offset) {
                    return start;
                }
            } else {
                int start = ts.offset();
                if (start > offset) {
                    return start;
                }
            }
            
        }

        if (id == JsTokenId.IDENTIFIER) {
            String s = token.text().toString();
            int length = s.length();
            int wordOffset = offset-ts.offset();
            if (reverse) {
                // Find previous
                int offsetInImage = offset - 1 - ts.offset(); 
                if (offsetInImage < 0) {
                    return -1;
                }
                if (offsetInImage < length && Character.isUpperCase(s.charAt(offsetInImage))) {
                    for (int i = offsetInImage - 1; i >= 0; i--) {
                        char charAtI = s.charAt(i);
                        if (charAtI == '_') {
                            // return offset of previous uppercase char in the identifier
                            return ts.offset() + i + 1;
                        } else if (!Character.isUpperCase(charAtI)) {
                            // return offset of previous uppercase char in the identifier
                            return ts.offset() + i + 1;
                        }
                    }
                    return ts.offset();
                } else {
                    for (int i = offsetInImage - 1; i >= 0; i--) {
                        char charAtI = s.charAt(i);
                        if (charAtI == '_') {
                            return ts.offset() + i + 1;
                        }
                        if (Character.isUpperCase(charAtI)) {
                            // now skip over previous uppercase chars in the identifier
                            for (int j = i; j >= 0; j--) {
                                char charAtJ = s.charAt(j);
                                if (charAtJ == '_') {
                                    return ts.offset() + j+1;
                                }
                                if (!Character.isUpperCase(charAtJ)) {
                                    // return offset of previous uppercase char in the identifier
                                    return ts.offset() + j + 1;
                                }
                            }
                            return ts.offset();
                        }
                    }
                    
                    return ts.offset();
                }
            } else {
                // Find next
                int start = wordOffset+1;
                if (wordOffset < 0 || wordOffset >= s.length()) {
                    // Probably the end of a token sequence, such as this:
                    // <%s|%>
                    return -1;
                }
                if (Character.isUpperCase(s.charAt(wordOffset))) { 
                    // if starting from a Uppercase char, first skip over follwing upper case chars
                    for (int i = start; i < length; i++) {
                        char charAtI = s.charAt(i);
                        if (!Character.isUpperCase(charAtI)) {
                            break;
                        }
                        if (s.charAt(i) == '_') {
                            return ts.offset()+i;
                        }
                        start++;
                    }
                }
                for (int i = start; i < length; i++) {
                    char charAtI = s.charAt(i);
                    if (charAtI == '_' || Character.isUpperCase(charAtI)) {
                        return ts.offset()+i;
                    }
                }
            }
        }
        
        // Default handling in the IDE
        return -1;
    }
}
