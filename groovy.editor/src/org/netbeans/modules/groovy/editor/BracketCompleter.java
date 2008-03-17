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

package org.netbeans.modules.groovy.editor;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.BracketCompletion;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.EditorOptions;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.lexer.LexUtilities;

/** 
 * 
 * @author Tor Norbye
 * @author Martin Adamek
 */
public class BracketCompleter implements BracketCompletion {
    /** When true, automatically reflows comments that are being edited according to the rdoc
     * conventions as well as the right hand side margin
     */
    private static final boolean REFLOW_COMMENTS = Boolean.getBoolean("groovy.autowrap.comments"); // NOI18N

    /** When true, continue comments if you press return in a line comment (that does not
     * also have code on the same line 
     */
    //static final boolean CONTINUE_COMMENTS = !Boolean.getBoolean("groovy.no.cont.comment"); // NOI18N
    static final boolean CONTINUE_COMMENTS = Boolean.getBoolean("groovy.cont.comment"); // NOI18N

    /** Tokens which indicate that we're within a literal string */
    private final static TokenId[] STRING_TOKENS = // XXX What about GroovyTokenId.STRING_BEGIN or QUOTED_STRING_BEGIN?
        {
            GroovyTokenId.STRING_LITERAL, GroovyTokenId.QUOTED_STRING_LITERAL, GroovyTokenId.CHAR_LITERAL,
            GroovyTokenId.STRING_END, GroovyTokenId.QUOTED_STRING_END
        };

    /** Tokens which indicate that we're within a regexp string */
    // XXX What about GroovyTokenId.REGEXP_BEGIN?
    private static final TokenId[] REGEXP_TOKENS = { GroovyTokenId.REGEXP_LITERAL, GroovyTokenId.REGEXP_END };

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

    public BracketCompleter() {
    }
    
    public boolean isInsertMatchingEnabled(BaseDocument doc) {
        // The editor options code is calling methods on BaseOptions instead of looking in the settings map :(
        //Boolean b = ((Boolean)Settings.getValue(doc.getKitClass(), SettingsNames.PAIR_CHARACTERS_COMPLETION));
        //return b == null || b.booleanValue();
        EditorOptions options = EditorOptions.get("text/x-groovy");
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
        
        // Look for an unterminated heredoc string
        if (lineBegin != -1 && lineEnd != -1) {
            TokenSequence<?extends GroovyTokenId> lineTs = LexUtilities.getGroovyTokenSequence(doc, offset);
            if (lineTs != null) {
                lineTs.move(lineBegin);
                StringBuilder sb = new StringBuilder();
                while (lineTs.moveNext() && lineTs.offset() <= lineEnd) {
                    Token<?extends GroovyTokenId> token = lineTs.token();
                    TokenId id = token.id();
                    
                    if (id == GroovyTokenId.STRING_BEGIN) {
                        String text = token.text().toString();
                        if (text.startsWith("<<") && insertMatching) {
                            StringBuilder markerBuilder = new StringBuilder();

                            for (int i = 2, n = text.length(); i < n; i++) {
                                char c = text.charAt(i);

                                if ((c == '\n') || (c == '\r')) {
                                    break;
                                }

                                markerBuilder.append(c);
                            }

                            String marker = markerBuilder.toString();

                            // Handle indented heredoc
                            if (marker.startsWith("-")) {
                                marker = marker.substring(1);
                            }
                            
                            if ((marker.startsWith("'") && marker.endsWith("'")) ||
                                    ((marker.startsWith("\"") && marker.endsWith("\"")))){
                                marker = marker.substring(1, marker.length()-2);
                            }

                            
                            // Next token should be string contents or a string end marker
                            //boolean addEndMarker = true;

                            TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);
                            ts.move(offset);
                            // XXX No, this is bogus, find a better way to detect whether the string is matched,
                            // perhaps using "find matching?"
                            
                            OffsetRange range = LexUtilities.findHeredocEnd(ts, token);
                            if (range == OffsetRange.NONE) {
                                sb.append("\n");
                                sb.append(marker);
                                //sb.append("\n");
                            }
                        }
                    }
                }
                
                if (sb.length() > 0) {
                    if (lineEnd == doc.getLength()) {
                        // At the end of the buffer we need a newline after the end
                        // marker. On other lines, we don't.
                        sb.append("\n");
                    }

                    doc.insertString(lineEnd, sb.toString(), null);
                    caret.setDot(lineEnd);

                    return -1;
                }
            }
        }
        
        TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);

        if (ts == null) {
            return -1;
        }

        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return -1;
        }

        Token<?extends GroovyTokenId> token = ts.token();
        TokenId id = token.id();

        // Is it an umatched =begin token?
//        if (insertMatching && (((id == GroovyTokenId.ERROR) && (ts.offset() == (offset - 6)) && 
//                token.text().toString().startsWith(EQ_BEGIN)) || 
//                (id == GroovyTokenId.BEGIN && ts.offset() == Utilities.getRowStart(doc, offset) + 1 &&
//                EQ_BEGIN.equals(doc.getText(ts.offset() - 1, EQ_BEGIN.length()))) || 
//                (id == GroovyTokenId.NONUNARY_OP && ts.offset() + EQ_BEGIN.length() <= doc.getLength() &&
//                EQ_BEGIN.equals(doc.getText(ts.offset(), EQ_BEGIN.length()))))) {
//            // NOI18N
//            doc.insertString(offset, "\n=end", null); // NOI18N
//            caret.setDot(offset);
//
//            return -1;
//        }

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
                LexUtilities.indent(sb, indent);
            } else {
                // I'm inserting a newline in the middle of a sentence, such as the scenario in #118656
                // I should insert the end AFTER the text on the line
                String restOfLine = doc.getText(offset, Utilities.getRowEnd(doc, afterLastNonWhite)-offset);
                sb.append(restOfLine);
                sb.append("\n");
                LexUtilities.indent(sb, indent);
                doc.remove(offset, restOfLine.length());
            }
            
            assert insertRBrace;
            sb.append("}"); // NOI18N

            int insertOffset = offset;
            doc.insertString(insertOffset, sb.toString(), null);
            caret.setDot(insertOffset);
            
            return -1;
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
        if ((id == GroovyTokenId.RBRACE || id == GroovyTokenId.RBRACKET) && (Utilities.getRowLastNonWhite(doc, offset) == offset)) {
            int indent = LexUtilities.getLineIndent(doc, offset);
            StringBuilder sb = new StringBuilder();
            // XXX On Windows, do \r\n?
            sb.append("\n"); // NOI18N
            LexUtilities.indent(sb, indent);

            int insertOffset = offset; // offset < length ? offset+1 : offset;
            doc.insertString(insertOffset, sb.toString(), null);
            caret.setDot(insertOffset);
        }
        
        if (id == GroovyTokenId.WHITESPACE) {
            // Pressing newline in the whitespace before a comment
            // should be identical to pressing newline with the caret
            // at the beginning of the comment
            int begin = Utilities.getRowFirstNonWhite(doc, offset);
            if (begin != -1 && offset < begin) {
                ts.move(begin);
                if (ts.moveNext()) {
                    id = ts.token().id();
                    if (id == GroovyTokenId.LINE_COMMENT) {
                        offset = begin;
                    }
                }
            }
        }
        
        if (id == GroovyTokenId.LINE_COMMENT) {
            // Only do this if the line only contains comments OR if there is content to the right on this line,
            // or if the next line is a comment!

            boolean continueComment = false;
            int begin = Utilities.getRowFirstNonWhite(doc, offset);

            // We should only continue comments if the previous line had a comment
            // (and a comment from the beginning, not a trailing comment)
            boolean previousLineWasComment = false;
            int rowStart = Utilities.getRowStart(doc, offset);
            if (rowStart > 0) {                
                int prevBegin = Utilities.getRowFirstNonWhite(doc, rowStart-1);
                if (prevBegin != -1) {
                    Token<? extends GroovyTokenId> firstToken = LexUtilities.getToken(doc, prevBegin);
                    if (firstToken.id() == GroovyTokenId.LINE_COMMENT) {
                        previousLineWasComment = true;
                    }                
                }
            }
            
            // See if we have more input on this comment line (to the right
            // of the inserted newline); if so it's a "split" operation on
            // the comment
            if (previousLineWasComment || offset > begin) {
                if (ts.offset()+token.length() > offset+1) {
                    // See if the remaining text is just whitespace
                    String trailing = doc.getText(offset,Utilities.getRowEnd(doc, offset)-offset);
                    if (trailing.trim().length() != 0) {
                        continueComment = true;
                    }
                } else if (CONTINUE_COMMENTS) {
                    // See if the "continue comments" options is turned on, and this is a line that
                    // contains only a comment (after leading whitespace)
                    Token<? extends GroovyTokenId> firstToken = LexUtilities.getToken(doc, begin);
                    if (firstToken.id() == GroovyTokenId.LINE_COMMENT) {
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
                            Token<? extends GroovyTokenId> firstToken = LexUtilities.getToken(doc, nextLineFirst);
                            if (firstToken != null && firstToken.id() == GroovyTokenId.LINE_COMMENT) {
                                continueComment = true;
                            }
                        }
                    }
                }
            }
                
            if (continueComment) {
                // Line comments should continue
                int indent = LexUtilities.getLineIndent(doc, offset);
                StringBuilder sb = new StringBuilder();
                LexUtilities.indent(sb, indent);
                sb.append("//"); // NOI18N
                // Copy existing indentation
                int afterHash = begin+1;
                String line = doc.getText(afterHash, Utilities.getRowEnd(doc, afterHash)-afterHash);
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
     * @param insertEndResult Null, or a boolean 1-element array whose first
     *   element will be set to true iff this method determines that "end" should
     *   be inserted
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
    static boolean isEndMissing(BaseDocument doc, int offset, boolean skipJunk,
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

        int beginEndBalance = LexUtilities.getBeginEndLineBalance(doc, offset, true);
        int braceBalance =
            LexUtilities.getLineBalance(doc, offset, GroovyTokenId.LBRACE, GroovyTokenId.RBRACE);

        if ((beginEndBalance == 1) || (braceBalance == 1)) {
            // There is one more opening token on the line than a corresponding
            // closing token.  (If there's is more than one we don't try to help.)
            int indent = LexUtilities.getLineIndent(doc, offset);

            // Look for the next nonempty line, and if its indent is > indent,
            // or if its line balance is -1 (e.g. it's an end) we're done
            boolean insertEnd = beginEndBalance > 0;
            int next = Utilities.getRowEnd(doc, offset) + 1;

            for (; next < length; next = Utilities.getRowEnd(doc, next) + 1) {
                if (Utilities.isRowEmpty(doc, next) || Utilities.isRowWhite(doc, next) ||
                        LexUtilities.isCommentOnlyLine(doc, next)) {
                    continue;
                }

                int nextIndent = LexUtilities.getLineIndent(doc, next);

                if (nextIndent > indent) {
                    insertEnd = false;
                } else if (nextIndent == indent) {
                    if (insertEnd) {
                        if (LexUtilities.getBeginEndLineBalance(doc, next, false) < 0) {
                            insertEnd = false;
                        } else {
                            // See if I have a structure word like "else", "ensure", etc.
                            // (These are indent words that are not also begin words)
                            // and if so refrain from inserting the end
                            int lineBegin = Utilities.getRowFirstNonWhite(doc, next);

                            Token<?extends GroovyTokenId> token =
                                LexUtilities.getToken(doc, lineBegin);

                            if ((token != null) && LexUtilities.isIndentToken(token.id()) &&
                                    !LexUtilities.isBeginToken(token.id(), doc, lineBegin)) {
                                insertEnd = false;
                            }
                        }
                    } else if (insertEnd &&
                            (LexUtilities.getLineBalance(doc, next, GroovyTokenId.LBRACE,
                                GroovyTokenId.RBRACE) < 0)) {
                        insertEnd = false;
                    }
                }

                break;
            }

            if (insertRBraceResult != null) {
                insertRBraceResult[0] = insertEnd;
            }

            if (indentResult != null) {
                indentResult[0] = indent;
            }

            return insertEnd;
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

        // Gotta look for the string begin pair in tokens since ANY character can
        // be used in Ruby string like the %x!! form.
        if (caretOffset == 0) {
            return false;
        }

        if (target.getSelectionStart() != -1) {
            if (NbUtilities.isCodeTemplateEditing(doc)) {
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
            } else if (ch == '$' && 
                    (LexUtilities.isInsideQuotedString(doc, target.getSelectionStart()) &&
                    LexUtilities.isInsideQuotedString(doc, target.getSelectionEnd())) ||
                    (LexUtilities.isInsideRegexp(doc, target.getSelectionStart()) &&
                    LexUtilities.isInsideRegexp(doc, target.getSelectionEnd()))) {
                String selection = target.getSelectedText();
                if (selection != null && selection.length() > 0 && selection.charAt(0) != ch) {
                    int start = target.getSelectionStart();
                    doc.remove(start, target.getSelectionEnd()-start);
                    doc.insertString(start, "${" + selection + "}", null);
                    target.getCaret().setDot(start+selection.length()+3);
                    return true;
                }
            }
        }

        TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        ts.move(caretOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<?extends GroovyTokenId> token = ts.token();
        TokenId id = token.id();
        TokenId[] stringTokens = null;
        TokenId beginTokenId = null;
        
        if (id == GroovyTokenId.LINE_COMMENT && target.getSelectionStart() != -1) {
            if (ch == '*' || ch == '+' || ch == '_') {
                // See if it's a comment and if so surround the text with an rdoc modifier
                // such as bold, teletype or italics
                String selection = target.getSelectedText();
                // Don't allow any spaces - you can't bracket multiple words in rdoc I think (TODO - check that)
                if (selection != null && selection.length() > 0 && selection.charAt(0) != ch && selection.indexOf(' ') == -1) {
                    int start = target.getSelectionStart();
                    doc.remove(start, target.getSelectionEnd()-start);
                    doc.insertString(start, ch + selection + matching(ch), null);
                    target.getCaret().setDot(start+selection.length()+2);
                
                    return true;
                }
            }
        }

        // "/" is handled AFTER the character has been inserted since we need the lexer's help
        if (ch == '\"') {
            stringTokens = STRING_TOKENS;
            beginTokenId = GroovyTokenId.QUOTED_STRING_BEGIN;
        } else if (ch == '\'') {
            stringTokens = STRING_TOKENS;
            beginTokenId = GroovyTokenId.STRING_BEGIN;
        } else if (id == GroovyTokenId.ERROR) {
            String text = token.text().toString();

            if (text.equals("%")) {
                // Depending on the character we're going to continue
                if (!Character.isLetter(ch)) { // %q, %x, etc. Only %[], %!!, %<space> etc. is allowed
                    stringTokens = STRING_TOKENS;
                    beginTokenId = GroovyTokenId.QUOTED_STRING_BEGIN;
                }
            } else if ((text.length() == 2) && (text.charAt(0) == '%') &&
                    Character.isLetter(text.charAt(1))) {
                char c = text.charAt(1);

                switch (c) {
                case 'q':
                    stringTokens = STRING_TOKENS;
                    beginTokenId = GroovyTokenId.STRING_BEGIN;

                    break;

                case 'Q':
                    stringTokens = STRING_TOKENS;
                    beginTokenId = GroovyTokenId.QUOTED_STRING_BEGIN;

                    break;

                case 'r':
                    stringTokens = REGEXP_TOKENS;
                    beginTokenId = GroovyTokenId.REGEXP_BEGIN;

                    break;

                default:
                    // ?
                    stringTokens = STRING_TOKENS;
                    beginTokenId = GroovyTokenId.QUOTED_STRING_BEGIN;
                }
            } else {
                ts.movePrevious();

                TokenId prevId = ts.token().id();

                if ((prevId == GroovyTokenId.STRING_BEGIN) ||
                        (prevId == GroovyTokenId.QUOTED_STRING_BEGIN)) {
                    stringTokens = STRING_TOKENS;
                    beginTokenId = prevId;
                } else if (prevId == GroovyTokenId.REGEXP_BEGIN) {
                    stringTokens = REGEXP_TOKENS;
                    beginTokenId = GroovyTokenId.REGEXP_BEGIN;
                }
            }
        } else if (((((id == GroovyTokenId.STRING_BEGIN) || (id == GroovyTokenId.QUOTED_STRING_BEGIN)) &&
                (caretOffset == (ts.offset() + 1))))) {
            if (!Character.isLetter(ch)) { // %q, %x, etc. Only %[], %!!, %<space> etc. is allowed
                stringTokens = STRING_TOKENS;
                beginTokenId = id;
            }
        } else if (((id == GroovyTokenId.STRING_BEGIN) && (caretOffset == (ts.offset() + 2))) ||
                (id == GroovyTokenId.STRING_END)) {
            stringTokens = STRING_TOKENS;
            beginTokenId = GroovyTokenId.STRING_BEGIN;
        } else if (((id == GroovyTokenId.QUOTED_STRING_BEGIN) && (caretOffset == (ts.offset() + 2))) ||
                (id == GroovyTokenId.QUOTED_STRING_END)) {
            stringTokens = STRING_TOKENS;
            beginTokenId = GroovyTokenId.QUOTED_STRING_BEGIN;
        } else if (((id == GroovyTokenId.REGEXP_BEGIN) && (caretOffset == (ts.offset() + 2))) ||
                (id == GroovyTokenId.REGEXP_END)) {
            stringTokens = REGEXP_TOKENS;
            beginTokenId = GroovyTokenId.REGEXP_BEGIN;
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
    //    TokenSequence< ?extends GroovyTokenId> ts = LexUtilities.getTokenSequence(doc);
    //
    //    System.out.println("Dumping tokens for dot=" + dot);
    //    int prevOffset = -1;
    //    if (ts != null) {
    //        ts.moveFirst();
    //        int index = 0;
    //        do {
    //            Token<? extends GroovyTokenId> token = ts.token();
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

        if (REFLOW_COMMENTS) {
//            Token<?extends GroovyTokenId> token = LexUtilities.getToken(doc, dotPos);
//            if (token != null) {
//                TokenId id = token.id();
//                if (id == GroovyTokenId.LINE_COMMENT || id == GroovyTokenId.DOCUMENTATION) {
//                    new ReflowParagraphAction().reflowEditedComment(target);
//                }
//            }
        }
        
        // See if our automatic adjustment of indentation when typing (for example) "end" was
        // premature - if you were typing a longer word beginning with one of my adjustment
        // prefixes, such as "endian", then put the indentation back.
        if (previousAdjustmentOffset != -1) {
            if (dotPos == previousAdjustmentOffset) {
                // Revert indentation iff the character at the insert position does
                // not start a new token (e.g. the previous token that we reindented
                // was not complete)
                TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, dotPos);

                if (ts != null) {
                    ts.move(dotPos);

                    if (ts.moveNext() && (ts.offset() < dotPos)) {
                        LexUtilities.setLineIndentation(doc, dotPos, previousAdjustmentIndent);
                    }
                }
            }

            previousAdjustmentOffset = -1;
        }

        //dumpTokens(doc, dotPos);
        switch (ch) {
        case '$': {
            // Automatically insert ${^} when typing "$" in a quoted string or regexp
            Token<?extends GroovyTokenId> token = LexUtilities.getToken(doc, dotPos);
            if (token == null) {
                return true;
            }
            TokenId id = token.id();

            if (id == GroovyTokenId.QUOTED_STRING_LITERAL || id == GroovyTokenId.REGEXP_LITERAL) {
                document.insertString(dotPos+1, "{}", null);
                // Skip the "{" to place the caret between { and }
                caret.setDot(dotPos+2);
            }
            break;
        }
        case '}':
        case '{':
        case ')':
        case ']':
        case '(':
        case '[': {
            
            if (!isInsertMatchingEnabled(doc)) {
                return false;
            }

            
            Token<?extends GroovyTokenId> token = LexUtilities.getToken(doc, dotPos);
            if (token == null) {
                return true;
            }
            TokenId id = token.id();

            if ((ch == '{') && (id == GroovyTokenId.ERROR && dotPos > 0)) {
                Token<? extends GroovyTokenId> prevToken = LexUtilities.getToken(doc, dotPos-1);
                if (prevToken != null) {
                    TokenId prevId = prevToken.id();
                    if (prevId == GroovyTokenId.STRING_LITERAL || prevId == GroovyTokenId.REGEXP_LITERAL) {
                        // Avoid case where typing "${" ends up as ${{ if user
                        // isn't used to the ${^} auto-insertion
                        if (dotPos > 1) {
                            String s = doc.getText(dotPos-2, 2);
                            if ("${".equals(s)) { // NOI18N
                                doc.remove(dotPos, 1);
                                caret.setDot(dotPos); // skip closing bracket
                                return true;
                            }
                        }
                    }
                }
            }
             
            if (ch == '}' && (id == GroovyTokenId.QUOTED_STRING_LITERAL || id == GroovyTokenId.REGEXP_LITERAL)) {
                Token<? extends GroovyTokenId> prevToken = LexUtilities.getToken(doc, dotPos-1);
                if (prevToken != null) {
                    TokenId prevId = prevToken.id();
                    if (prevId == GroovyTokenId.EMBEDDED_GROOVY) {
                        if (dotPos < doc.getLength()-1) {
                            char c = doc.getText(dotPos+1, 1).charAt(0);
                            if (c == '}') {
                                doc.remove(dotPos, 1);
                                caret.setDot(dotPos+1); // skip closing bracket
                                return true;
                            }
                        }
                    }
                }
            }

            // This only kicks in when ${} has no content
            if ((ch == '}') && id == GroovyTokenId.EMBEDDED_GROOVY) {
                // Support type-through of } when we have ${^}
                if (dotPos < doc.getLength()-1) {
                    char c = doc.getText(dotPos+1, 1).charAt(0);
                    if (c == '}') {
                        doc.remove(dotPos, 1);
                        caret.setDot(dotPos+1); // skip closing bracket
                        return true;
                    }
                }
            }

            if (id == GroovyTokenId.ANY_OPERATOR) {
                int length = token.length();
                String s = token.text().toString();
                if ((length == 2) && "[]".equals(s) || "[]=".equals(s)) { // Special case
                    skipClosingBracket(doc, caret, ch, GroovyTokenId.RBRACKET);

                    return true;
                }
            }

            if (((id == GroovyTokenId.IDENTIFIER) && (token.length() == 1)) ||
                    (id == GroovyTokenId.LBRACKET) || (id == GroovyTokenId.RBRACKET) ||
                    (id == GroovyTokenId.LBRACE) || (id == GroovyTokenId.RBRACE) ||
                    (id == GroovyTokenId.LPAREN) || (id == GroovyTokenId.RPAREN)) {
                if (ch == ']') {
                    skipClosingBracket(doc, caret, ch, GroovyTokenId.RBRACKET);
                } else if (ch == ')') {
                    skipClosingBracket(doc, caret, ch, GroovyTokenId.RPAREN);
                } else if (ch == '}') {
                    skipClosingBracket(doc, caret, ch, GroovyTokenId.RBRACE);
                } else if ((ch == '[') || (ch == '(') || (ch == '{')) {
                    completeOpeningBracket(doc, dotPos, caret, ch);
                }
            }

            // Reindent blocks (won't do anything if } is not at the beginning of a line
            if (ch == '}') {
                reindent(doc, dotPos, GroovyTokenId.RBRACE, caret);
            } else if (ch == ']') {
                reindent(doc, dotPos, GroovyTokenId.RBRACKET, caret);
            }
        }

        break;

        case '/': {
            if (!isInsertMatchingEnabled(doc)) {
                return false;
            }

            // Bracket matching for regular expressions has to be done AFTER the
            // character is inserted into the document such that I can use the lexer
            // to determine whether it's a division (e.g. x/y) or a regular expression (/foo/)
            Token<?extends GroovyTokenId> token = LexUtilities.getToken(doc, dotPos);
            if (token != null) {
                TokenId id = token.id();

                if (id == GroovyTokenId.REGEXP_BEGIN || id == GroovyTokenId.REGEXP_END) {
                    TokenId[] stringTokens = REGEXP_TOKENS;
                    TokenId beginTokenId = GroovyTokenId.REGEXP_BEGIN;

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
        case '|': {
            if (!isInsertMatchingEnabled(doc)) {
                return false;
            }

            Token<?extends GroovyTokenId> token = LexUtilities.getToken(doc, dotPos);
            if (token == null) {
                return true;
            }

            TokenId id = token.id();

            // Ensure that we're not in a comment, strings etc.
            if (id == GroovyTokenId.NONUNARY_OP && token.length() == 2 && "||".equals(token.text().toString())) {
                // Type through: |^| and type |
                // See if we're in a do or { block
                if (isBlockDefinition(doc, dotPos)) {
                    // It's a block so this should be a variable declaration of the form { |foo| }
                    // Did you type "|" in the middle? If so, should type through!
                    // TODO - check that we were typing in the middle, not in the front!
                    doc.remove(dotPos, 1);
                    caret.setDot(dotPos+1);
                        
                    return true;
                }
                
            } else if (id == GroovyTokenId.IDENTIFIER && token.length() == 1 && "|".equals(token.text().toString())) {
                // Only insert a matching | if there aren't any others on this line AND we're in a block
                if (isBlockDefinition(doc, dotPos)) {
                    boolean found = false;
                    int lineEnd = Utilities.getRowEnd(doc, dotPos);
                    if (lineEnd > dotPos+1) {
                        TokenSequence<? extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, dotPos+1);
                        ts.move(dotPos+1);
                        while (ts.moveNext() && ts.offset() < lineEnd) {
                            Token<? extends GroovyTokenId> t = ts.token();
                            if (t.id() == GroovyTokenId.IDENTIFIER && t.length() == 1 && "|".equals(t.text().toString())) {
                                found = true;
                                break;
                            }
                        }
                    }

                    if (!found) {
                        doc.insertString(dotPos+1, "|", null);
                        caret.setDot(dotPos + 1);
                    }
                }
                
                return true;
            }
            break;
        }
        }

        return true;
    }
    
    private boolean isBlockDefinition(BaseDocument doc, int dotPos) throws BadLocationException {
        TokenSequence<? extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, dotPos);
        int lineStart = Utilities.getRowStart(doc, dotPos);
        ts.move(dotPos+1);
        while (ts.movePrevious() && ts.offset() >= lineStart) {
            TokenId tid = ts.token().id();
            if (tid == GroovyTokenId.LBRACE) {
                return true;
//                    } else if (tid == GroovyTokenId.IDENTIFIER && ts.token().length() == 1 && "|".equals(ts.token().text().toString())) {
//                        continue;
//                    } else if (tid == GroovyTokenId.NONUNARY_OP && "||".equals(ts.token().text().toString())) {
//                        continue;
            } else if (tid == GroovyTokenId.RBRACE) {
                break;
            }
        }
        
        return false;
    }

    private void reindent(BaseDocument doc, int offset, TokenId id, Caret caret)
        throws BadLocationException {
        TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);

        if (ts != null) {
            ts.move(offset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }

            Token<?extends GroovyTokenId> token = ts.token();

            if ((token.id() == id)) {
                final int rowFirstNonWhite = Utilities.getRowFirstNonWhite(doc, offset);
                // Ensure that this token is at the beginning of the line
                if (ts.offset() > rowFirstNonWhite) {
//                    if (GroovyUtils.isRhtmlDocument(doc)) {
//                        // Allow "<%[whitespace]*" to preceed
//                        String s = doc.getText(rowFirstNonWhite, ts.offset()-rowFirstNonWhite);
//                        if (!s.matches("<%\\s*")) {
//                            return;
//                        }
//                    } else {
                        return;
//                    }
                }

                OffsetRange begin;

                if (id == GroovyTokenId.RBRACE) {
                    begin = LexUtilities.findBwd(doc, ts, GroovyTokenId.LBRACE, GroovyTokenId.RBRACE);
                } else if (id == GroovyTokenId.RBRACKET) {
                    begin = LexUtilities.findBwd(doc, ts, GroovyTokenId.LBRACKET, GroovyTokenId.RBRACKET);
                } else {
                    begin = LexUtilities.findBegin(doc, ts);
                }

                if (begin != OffsetRange.NONE) {
                    int beginOffset = begin.getStart();
                    int indent = LexUtilities.getLineIndent(doc, beginOffset);
                    previousAdjustmentIndent = LexUtilities.getLineIndent(doc, offset);
                    LexUtilities.setLineIndentation(doc, offset, indent);
                    previousAdjustmentOffset = caret.getDot();
                }
            }
        }
    }

    public OffsetRange findMatching(Document document, int offset /*, boolean simpleSearch*/) {
        BaseDocument doc = (BaseDocument)document;

        TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);

        if (ts != null) {
            ts.move(offset);

            if (!ts.moveNext()) {
                return OffsetRange.NONE;
            }

            Token<?extends GroovyTokenId> token = ts.token();

            if (token == null) {
                return OffsetRange.NONE;
            }

            TokenId id = token.id();

            if (id == GroovyTokenId.WHITESPACE) {
                // ts.move(offset) gives the token to the left of the caret.
                // If you have the caret right at the beginning of a token, try
                // the token to the right too - this means that if you have
                //  "   |def" it will show the matching "end" for the "def".
                offset++;
                ts.move(offset);

                if (ts.moveNext() && (ts.offset() <= offset)) {
                    token = ts.token();
                    id = token.id();
                }
            }

            if (id == GroovyTokenId.QUOTED_STRING_BEGIN) {
                // Heredocs should be treated specially
                if (token.text().toString().startsWith("<<")) {
                    return LexUtilities.findHeredocEnd(ts, token);
                }
                return LexUtilities.findFwd(doc, ts, GroovyTokenId.QUOTED_STRING_BEGIN,
                    GroovyTokenId.QUOTED_STRING_END);
            } else if (id == GroovyTokenId.QUOTED_STRING_END) {
                String s = token.text().toString();
                if (!"\"".equals(s) && !"\'".equals(s) && !")".equals(s)) {
                    OffsetRange r = LexUtilities.findHeredocBegin(ts, token);
                    if (r != OffsetRange.NONE) {
                        return r;
                    }
                    ts.move(offset);
                }
                return LexUtilities.findBwd(doc, ts, GroovyTokenId.QUOTED_STRING_BEGIN,
                    GroovyTokenId.QUOTED_STRING_END);
            } else if (id == GroovyTokenId.STRING_BEGIN) {
                // Heredocs should be treated specially
                if (token.text().toString().startsWith("<<")) {
                    return LexUtilities.findHeredocEnd(ts, token);
                }
                return LexUtilities.findFwd(doc, ts, GroovyTokenId.STRING_BEGIN, GroovyTokenId.STRING_END);
            } else if (id == GroovyTokenId.STRING_END) {
                String s = token.text().toString();
                if (!"\"".equals(s) && !"\'".equals(s) && !")".equals(s)) {
                    OffsetRange r = LexUtilities.findHeredocBegin(ts, token);
                    if (r != OffsetRange.NONE) {
                        return r;
                    }
                    ts.move(offset);
                }
                return LexUtilities.findBwd(doc, ts, GroovyTokenId.STRING_BEGIN, GroovyTokenId.STRING_END);
            } else if (id == GroovyTokenId.REGEXP_BEGIN) {
                return LexUtilities.findFwd(doc, ts, GroovyTokenId.REGEXP_BEGIN, GroovyTokenId.REGEXP_END);
            } else if (id == GroovyTokenId.REGEXP_END) {
                return LexUtilities.findBwd(doc, ts, GroovyTokenId.REGEXP_BEGIN, GroovyTokenId.REGEXP_END);
            } else if (id == GroovyTokenId.LPAREN) {
                return LexUtilities.findFwd(doc, ts, GroovyTokenId.LPAREN, GroovyTokenId.RPAREN);
            } else if (id == GroovyTokenId.RPAREN) {
                return LexUtilities.findBwd(doc, ts, GroovyTokenId.LPAREN, GroovyTokenId.RPAREN);
            } else if (id == GroovyTokenId.LBRACE) {
                return LexUtilities.findFwd(doc, ts, GroovyTokenId.LBRACE, GroovyTokenId.RBRACE);
            } else if (id == GroovyTokenId.RBRACE) {
                return LexUtilities.findBwd(doc, ts, GroovyTokenId.LBRACE, GroovyTokenId.RBRACE);
            } else if (id == GroovyTokenId.LBRACKET) {
                return LexUtilities.findFwd(doc, ts, GroovyTokenId.LBRACKET, GroovyTokenId.RBRACKET);
//            } else if (id == GroovyTokenId.DO && !LexUtilities.isEndmatchingDo(doc, ts.offset())) {
//                // No matching dot for "do" used in conditionals etc.
//                return OffsetRange.NONE;
            } else if (id == GroovyTokenId.RBRACKET) {
                return LexUtilities.findBwd(doc, ts, GroovyTokenId.LBRACKET, GroovyTokenId.RBRACKET);
            } else if (id.primaryCategory().equals("keyword")) {
                if (LexUtilities.isBeginToken(id, doc, ts)) {
                    return LexUtilities.findEnd(doc, ts);
//                } else if ((id == GroovyTokenId.END) || LexUtilities.isIndentToken(id)) { // Find matching block
//
//                    return LexUtilities.findBegin(doc, ts);
                }
            }
        }

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
        // Backspacing over "$ " ? Delete the "$" too!
            TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, dotPos);
            if (ts != null) {
                ts.move(dotPos);
                if ((ts.moveNext() || ts.movePrevious()) && (ts.offset() == dotPos-1 && ts.token().id() == GroovyTokenId.LINE_COMMENT)) {
                    doc.remove(dotPos-1, 1);
                    target.getCaret().setDot(dotPos-1);

                    return true;
                }
            }
            break;
        }

        case '{': {
            // Attempt to fix ${} in chars
            Token<? extends GroovyTokenId> token = LexUtilities.getToken(doc, dotPos-1);
            if (token != null && (token.id() == GroovyTokenId.QUOTED_STRING_LITERAL || token.id() == GroovyTokenId.REGEXP_LITERAL)) {
                String s = document.getText(dotPos-1, 2);
                if ("$}".equals(s)) {
                    // We have just deleted a ${} segment
                    doc.remove(dotPos, 1);
                    target.getCaret().setDot(dotPos);
                }
            }
            
            // Fall through to handle '{' typethrough as well
        }

        case '(':
        case '[': { // and '{' via fallthrough
            char tokenAtDot = LexUtilities.getTokenChar(doc, dotPos);

            if (((tokenAtDot == ']') &&
                    (LexUtilities.getTokenBalance(doc, GroovyTokenId.LBRACKET, GroovyTokenId.RBRACKET, dotPos) != 0)) ||
                    ((tokenAtDot == ')') &&
                    (LexUtilities.getTokenBalance(doc, GroovyTokenId.LPAREN, GroovyTokenId.RPAREN, dotPos) != 0)) ||
                    ((tokenAtDot == '}') &&
                    (LexUtilities.getTokenBalance(doc, GroovyTokenId.LBRACE, GroovyTokenId.RBRACE, dotPos) != 0))) {
                doc.remove(dotPos, 1);
            }
            break;
        }
        case '|':
        case '\"':
        case '\'':
        case '/': {
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

        TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        // XXX BEGIN TOR MODIFICATIONS
        //ts.move(caretOffset+1);
        ts.move(caretOffset);

        if (!ts.moveNext()) {
            return false;
        }

        Token<?extends GroovyTokenId> token = ts.token();

        // Check whether character follows the bracket is the same bracket
        if ((token != null) && (token.id() == bracketId)) {
            int bracketIntId = bracketId.ordinal();
            int leftBracketIntId =
                (bracketIntId == GroovyTokenId.RPAREN.ordinal()) ? GroovyTokenId.LPAREN.ordinal()
                                                               : GroovyTokenId.LBRACKET.ordinal();

            // Skip all the brackets of the same type that follow the last one
            ts.moveNext();

            Token<?extends GroovyTokenId> nextToken = ts.token();

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
            Token<?extends GroovyTokenId> lastRBracket = token;
            ts.movePrevious();
            token = ts.token();

            boolean finished = false;

            while (!finished && (token != null)) {
                int tokenIntId = token.id().ordinal();

                if ((token.id() == GroovyTokenId.LPAREN) || (token.id() == GroovyTokenId.LBRACKET)) {
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
                } else if ((token.id() == GroovyTokenId.RPAREN) ||
                        (token.id() == GroovyTokenId.RBRACKET)) {
                    if (tokenIntId == bracketIntId) {
                        bracketBalance--;
                    }
                } else if (token.id() == GroovyTokenId.LBRACE) {
                    braceBalance++;

                    if (braceBalance > 0) { // stop on extra left brace
                        finished = true;
                    }
                } else if (token.id() == GroovyTokenId.RBRACE) {
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
                    if ((token.id() == GroovyTokenId.LPAREN) || (token.id() == GroovyTokenId.LBRACKET)) {
                        if (token.id().ordinal() == leftBracketIntId) {
                            bracketBalance++;
                        }
                    } else if ((token.id() == GroovyTokenId.RPAREN) ||
                            (token.id() == GroovyTokenId.RBRACKET)) {
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
                    } else if (token.id() == GroovyTokenId.LBRACE) {
                        braceBalance++;
                    } else if (token.id() == GroovyTokenId.RBRACE) {
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

        TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, dotPos);

        if (ts == null) {
            return false;
        }

        ts.move(dotPos);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<?extends GroovyTokenId> token = ts.token();
        Token<?extends GroovyTokenId> previousToken = null;

        if (ts.movePrevious()) {
            previousToken = ts.token();
        }

        int lastNonWhite = Utilities.getRowLastNonWhite(doc, dotPos);

        // eol - true if the caret is at the end of line (ignoring whitespaces)
        boolean eol = lastNonWhite < dotPos;

        if ((token.id() == GroovyTokenId.BLOCK_COMMENT) || (token.id() == GroovyTokenId.LINE_COMMENT) ||
                (token.id() == GroovyTokenId.DOCUMENTATION)) { // 105419
            return false;
        } else if ((token.id() == GroovyTokenId.WHITESPACE) && eol && ((dotPos - 1) > 0)) {
            // check if the caret is at the very end of the line comment
            token = LexUtilities.getToken(doc, dotPos - 1);

            if (token.id() == GroovyTokenId.LINE_COMMENT) {
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

        if ((id == GroovyTokenId.ERROR) && (previousToken != null) &&
                (previousToken.id() == beginToken)) {
            insideString = true;
        }

        if (!insideString) {
            // check if the caret is at the very end of the line and there
            // is an unterminated string literal
            if ((token.id() == GroovyTokenId.WHITESPACE) && eol) {
                if ((dotPos - 1) > 0) {
                    token = LexUtilities.getToken(doc, dotPos - 1);
                    // XXX TODO use language embedding to handle this
                    insideString = (token.id() == GroovyTokenId.STRING_LITERAL) ||
                        (token.id() == GroovyTokenId.CHAR_LITERAL);
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

    public List<OffsetRange> findLogicalRanges(CompilationInfo info, int caretOffset) {
        List<OffsetRange> ranges = new ArrayList<OffsetRange>();
        return ranges;
    }

    // UGH - this method has gotten really ugly after successive refinements based on unit tests - consider cleaning up
    public int getNextWordOffset(Document document, int offset, boolean reverse) {
        BaseDocument doc = (BaseDocument)document;
        TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);
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

        Token<? extends GroovyTokenId> token = ts.token();
        TokenId id = token.id();

        if (id == GroovyTokenId.WHITESPACE) {
            // Just eat up the space in the normal IDE way
            if ((reverse && ts.offset() < offset) || (!reverse && ts.offset() > offset)) {
                return ts.offset();
            }
            while (id == GroovyTokenId.WHITESPACE) {
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

        if (id == GroovyTokenId.IDENTIFIER || id == GroovyTokenId.TYPE_SYMBOL ||
                id == GroovyTokenId.CONSTANT ||
                id == GroovyTokenId.GLOBAL_VAR || id == GroovyTokenId.INSTANCE_VAR) {
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
