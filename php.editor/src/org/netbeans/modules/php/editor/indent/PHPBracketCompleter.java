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
package org.netbeans.modules.php.editor.indent;

import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;
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
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.api.EditorOptions;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;


/** 
 * Provide bracket completion for Ruby.
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
 * The logic around inserting matching ""'s is heavily based on the Java editor
 * implementation, and probably should be rewritten to be Ruby oriented.
 * One thing they did is process the characters BEFORE the character has been
 * inserted into the document. This has some advantages - it's easy to detect
 * whether you're typing in the middle of a string since the token hierarchy
 * has not been updated yet. On the other hand, it makes it hard to identify
 * whether some characters are what we expect - is a "/" truly a regexp starter
 * or something else? The Ruby lexer has lots of logic and state to determine
 * this. I think it would be better to switch to after-insert logic for this.
 *
 * @todo Match braces within literal strings, as in #{}
 * @todo Match || in the argument list of blocks? do { |foo| etc. }
 * @todo I'm currently highlighting the indentation tokens (else, elsif, ensure, etc.)
 *   by finding the corresponding begin. For "illegal" tokens, e.g. def foo; else; end;
 *   this means I'll show "def" as the matching token for else, which is wrong.
 *   I should make the "indentation tokens" list into a map and associate them
 *   with their corresponding tokens, such that an else is only lined up with an if,
 *   etc.
 * @todo Pressing newline in a parameter list doesn't work well if it's on a blockdefining
 *    line - e.g. def foo(a,b => it will insert the end BEFORE the closing paren!
 * @todo Pressing space in a comment beyond the textline limit should wrap text?
 *    http://ruby.netbeans.org/issues/show_bug.cgi?id=11553
 * @todo Make ast-selection pick up =begin/=end documentation blocks
 *
 * @author Tor Norbye
 */
public class PHPBracketCompleter implements KeystrokeHandler {
//    /** When true, automatically reflows comments that are being edited according to the rdoc
//     * conventions as well as the right hand side margin
//     */
//    private static final boolean REFLOW_COMMENTS = Boolean.getBoolean("ruby.autowrap.comments"); // NOI18N

    // XXX: this should made it to options and be supported in java for example
    /** When true, continue comments if you press return in a line comment (that does not
     * also have code on the same line 
     */
    static final boolean CONTINUE_COMMENTS = Boolean.getBoolean("php.cont.comment"); // NOI18N

    /** Tokens which indicate that we're within a literal string */
    private final static PHPTokenId [] STRING_TOKENS = {
        PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING,
        PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE
    };

//    /** Tokens which indicate that we're within a regexp string */
//    // XXX What about PHPTokenId.PHP_REGEXP_BEGIN?
//    private static final TokenId[] REGEXP_TOKENS = { PHPTokenId.PHP_REGEXP_LITERAL, PHPTokenId.PHP_REGEXP_END };
//
//    /** Used in =begin/=end completion */
//    private final static String EQ_BEGIN = "=begin"; // NOI18N
//    
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

    public PHPBracketCompleter() {
    }
    
    public boolean isInsertMatchingEnabled(BaseDocument doc) {
        // The editor options code is calling methods on BaseOptions instead of looking in the settings map :(
        //Boolean b = ((Boolean)Settings.getValue(doc.getKitClass(), SettingsNames.PAIR_CHARACTERS_COMPLETION));
        //return b == null || b.booleanValue();
        EditorOptions options = EditorOptions.get(FileUtils.PHP_MIME_TYPE);
        if (options != null) {
            return options.getMatchBrackets();
        }
        
        return true;
    }

    public int beforeBreak(Document document, int offset, JTextComponent target)
        throws BadLocationException {
        isAfter = false;
        
        Caret caret = target.getCaret();
        final BaseDocument doc = (BaseDocument)document;

        boolean insertMatching = isInsertMatchingEnabled(doc);
        
        int lineBegin = Utilities.getRowStart(doc,offset);
        int lineEnd = Utilities.getRowEnd(doc,offset);

        if (lineBegin == offset && lineEnd == offset) {
            // Pressed return on a blank newline - do nothing
            return -1;
        }
// XXX: heredoc        
//        // Look for an unterminated heredoc string
//        if (lineBegin != -1 && lineEnd != -1) {
//            TokenSequence<?extends PHPTokenId> lineTs = LexUtilities.getPHPTokenSequence(doc, offset);
//            if (lineTs != null) {
//                lineTs.move(lineBegin);
//                StringBuilder sb = new StringBuilder();
//                while (lineTs.moveNext() && lineTs.offset() <= lineEnd) {
//                    Token<?extends PHPTokenId> token = lineTs.token();
//                    TokenId id = token.id();
//                    
//                    if (id == PHPTokenId.PHP_STRING_BEGIN) {
//                        String text = token.text().toString();
//                        if (text.startsWith("<<") && insertMatching) {
//                            StringBuilder markerBuilder = new StringBuilder();
//
//                            for (int i = 2, n = text.length(); i < n; i++) {
//                                char c = text.charAt(i);
//
//                                if ((c == '\n') || (c == '\r')) {
//                                    break;
//                                }
//
//                                markerBuilder.append(c);
//                            }
//
//                            String marker = markerBuilder.toString();
//
//                            // Handle indented heredoc
//                            if (marker.startsWith("-")) {
//                                marker = marker.substring(1);
//                            }
//                            
//                            if ((marker.startsWith("'") && marker.endsWith("'")) ||
//                                    ((marker.startsWith("\"") && marker.endsWith("\"")))){
//                                marker = marker.substring(1, marker.length()-2);
//                            }
//
//                            
//                            // Next token should be string contents or a string end marker
//                            //boolean addEndMarker = true;
//
//                            TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
//                            ts.move(offset);
//                            // XXX No, this is bogus, find a better way to detect whether the string is matched,
//                            // perhaps using "find matching?"
//                            
//                            OffsetRange range = LexUtilities.findHeredocEnd(ts, token);
//                            if (range == OffsetRange.NONE) {
//                                sb.append("\n");
//                                sb.append(marker);
//                                //sb.append("\n");
//                            }
//                        }
//                    }
//                }
//                
//                if (sb.length() > 0) {
//                    if (lineEnd == doc.getLength()) {
//                        // At the end of the buffer we need a newline after the end
//                        // marker. On other lines, we don't.
//                        sb.append("\n");
//                    }
//
//                    doc.insertString(lineEnd, sb.toString(), null);
//                    caret.setDot(lineEnd);
//
//                    return -1;
//                }
//            }
//        }

        TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);

        if (ts == null) {
            return -1;
        }

        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return -1;
        }

        Token<?extends PHPTokenId> token = ts.token();
        TokenId id = token.id();

        // Insert an end statement? Insert a } marker?
        boolean[] insertEndResult = new boolean[1];
        boolean[] insertRBraceResult = new boolean[1];
        int[] indentResult = new int[1];
        boolean insert = insertMatching &&
            isEndMissing(doc, offset, false, insertEndResult, insertRBraceResult, null, indentResult);

        if (insert) {
            boolean insertEnd = insertEndResult[0];
            boolean insertRBrace = insertRBraceResult[0];
            int indent = indentResult[0];

            int afterLastNonWhite = Utilities.getRowLastNonWhite(doc, offset);

            // We've either encountered a further indented line, or a line that doesn't
            // look like the end we're after, so insert a matching end.
            StringBuilder sb = new StringBuilder();
            if (offset > afterLastNonWhite) {
                sb.append("\n"); //NOI18N
                sb.append(IndentUtils.createIndentString(doc, indent));
                
            } else {
                // I'm inserting a newline in the middle of a sentence, such as the scenario in #118656
                // I should insert the end AFTER the text on the line
                String restOfLine = doc.getText(offset, Utilities.getRowEnd(doc, afterLastNonWhite)-offset);
                sb.append(restOfLine);
                sb.append("\n"); //NOI18N
                sb.append(IndentUtils.createIndentString(doc, indent));
                doc.remove(offset, restOfLine.length());
            }
            
            if (insertEnd) {
                sb.append("end"); // NOI18N
            } else {
                assert insertRBrace;
                sb.append("}"); // NOI18N
            }

            int insertOffset = offset;
            doc.insertString(insertOffset, sb.toString(), null);
            caret.setDot(insertOffset);
            
            return -1;
        }

        // XXX: this may not be neccessary, at least not for }
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
        if ((id == PHPTokenId.PHP_CURLY_CLOSE || LexUtilities.textEquals(token.text(), ']') || LexUtilities.textEquals(token.text(), ')')) // NOI18N
                && (Utilities.getRowLastNonWhite(doc, offset) == offset)) {
            int indent = GsfUtilities.getLineIndent(doc, offset);
            StringBuilder sb = new StringBuilder();
            sb.append("\n"); // NOI18N
            sb.append(IndentUtils.createIndentString(doc, indent));

            int insertOffset = offset; // offset < length ? offset+1 : offset;
            doc.insertString(insertOffset, sb.toString(), null);
            caret.setDot(insertOffset);
        }

        // Support continual line comments
        if (id == PHPTokenId.WHITESPACE) {
            // Pressing newline in the whitespace before a comment
            // should be identical to pressing newline with the caret
            // at the beginning of the comment
            int begin = Utilities.getRowFirstNonWhite(doc, offset);
            if (begin != -1 && offset < begin) {
                ts.move(begin);
                if (ts.moveNext()) {
                    id = ts.token().id();
                    if (id == PHPTokenId.PHP_LINE_COMMENT) {
                        offset = begin;
                    }
                }
            }
        }
        
        if (id == PHPTokenId.PHP_LINE_COMMENT) {
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
                    Token<? extends PHPTokenId> firstToken = LexUtilities.getToken(doc, prevBegin);
                    if (firstToken != null && firstToken.id() == PHPTokenId.PHP_LINE_COMMENT) {
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
                    Token<? extends PHPTokenId> firstToken = LexUtilities.getToken(doc, begin);
                    if (firstToken != null && firstToken.id() == PHPTokenId.PHP_LINE_COMMENT) {
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
                            Token<? extends PHPTokenId> firstToken = LexUtilities.getToken(doc, nextLineFirst);
                            if (firstToken != null && firstToken.id() == PHPTokenId.PHP_LINE_COMMENT) {
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

        if (id == PHPTokenId.PHPDOC_COMMENT || id == PHPTokenId.PHPDOC_COMMENT_START || id == PHPTokenId.PHPDOC_COMMENT_END) {
            final Object [] ret = beforeBreakInComments(doc, ts, offset, caret, 
                PHPTokenId.PHPDOC_COMMENT_START, PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END);
            boolean isEmptyComment = (Boolean) ret[1];
            
            if (isEmptyComment) {
                final int indent = GsfUtilities.getLineIndent(doc, ts.offset());
                
                //XXX: workaround for issue #133210:
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        GeneratingBracketCompleter.generateDocTags(doc, (Integer) ret[0], indent);
                    }
                });
            }
            
            return (Integer) ret[0];
        }
        
        if (id == PHPTokenId.PHP_COMMENT || id == PHPTokenId.PHP_COMMENT_START || id == PHPTokenId.PHP_COMMENT_END) {
            Object [] ret = beforeBreakInComments(doc, ts, offset, caret, 
                PHPTokenId.PHP_COMMENT_START, PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END);
            return (Integer) ret[0];
        }
        
        return -1;
    }
    
    private static Object [] beforeBreakInComments(
        BaseDocument doc, TokenSequence<? extends PHPTokenId> ts, int offset, Caret caret,
        PHPTokenId commentStart, PHPTokenId commentBody, PHPTokenId commentEnd
    ) throws BadLocationException {
        PHPTokenId id = ts.token().id();
        
        if (id == commentBody || id == commentStart) {
            int insertOffset;
            

            if (id == commentStart) {
                insertOffset = ts.offset() + ts.token().length();
            } else {
                insertOffset = offset;
            }

            int indent = GsfUtilities.getLineIndent(doc, ts.offset());
            int afterLastNonWhite = Utilities.getRowLastNonWhite(doc, insertOffset);

            // find comment end
            boolean addClosingTag = !isClosedComment(DocumentUtilities.getText(doc), insertOffset);
            
            // We've either encountered a further indented line, or a line that doesn't
            // look like the end we're after, so insert a matching end.
            int newCaretOffset;
            StringBuilder sb = new StringBuilder();
            if (offset > afterLastNonWhite) {
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append(" * "); // NOI18N
                newCaretOffset = insertOffset + sb.length() + 1;
            } else {
                // I'm inserting a newline in the middle of a sentence, such as the scenario in #118656
                // I should insert the end AFTER the text on the line
                String restOfLine = doc.getText(insertOffset, Utilities.getRowEnd(doc, afterLastNonWhite)-insertOffset);
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append(" * "); // NOI18N
                newCaretOffset = insertOffset + sb.length() + 1;
                sb.append(restOfLine);
                doc.remove(insertOffset, restOfLine.length());
            }
            
            if (addClosingTag) {
                // add the closing tag
                sb.append("\n");
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append(" */"); // NOI18N
            }
            
            doc.insertString(insertOffset, sb.toString(), null);
            caret.setDot(insertOffset);
            
            return new Object [] { newCaretOffset, addClosingTag };
        }

        if (id == commentEnd) {
            int insertOffset = ts.offset();
            
            // find comment start
            if (ts.movePrevious()) {
                assert ts.token().id() == commentBody || 
                       ts.token().id() == commentStart : "PHP_COMMENT_END should not be preceeded by " + ts.token().id().name(); //NOI18N
            } else {
                assert false : "PHP_COMMENT_END without PHP_COMMENT or PHP_COMMENT_START"; //NOI18N
            }
            
            int indent = GsfUtilities.getLineIndent(doc, ts.offset());
            int beforeFirstNonWhite = Utilities.getRowFirstNonWhite(doc, insertOffset);
            int rowStart = Utilities.getRowStart(doc, insertOffset);
            int newCaretOffset = insertOffset;
            
            StringBuilder sb = new StringBuilder();
            if (beforeFirstNonWhite >= insertOffset) {
                // only whitespace in front of */
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append(" * ");
                newCaretOffset = rowStart + sb.length();
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append(" "); //NOI18N
                doc.remove(rowStart, insertOffset - rowStart);
                insertOffset = rowStart;
            } else {
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append(" "); //NOI18N
            }
            
            doc.insertString(insertOffset, sb.toString(), null);
            caret.setDot(newCaretOffset);
            
            return new Object[] { newCaretOffset, false };
        }
        
        return new Object[] { -1, false };
    }
    
    // XXX: stolen from JavaKit.JavaInsertBreakAction, we should extend it to support heredoc
    private static boolean isClosedComment(CharSequence txt, int pos) {
        int length = txt.length();
        int quotation = 0;
        for (int i = pos; i < length; i++) {
            char c = txt.charAt(i);
            if (c == '*' && i < length - 1 && txt.charAt(i + 1) == '/') {
                if (quotation == 0 || i < length - 2) {
                    return true;
                }
                // guess it is not just part of some text constant
                boolean isClosed = true;
                for (int j = i + 2; j < length; j++) {
                    char cc = txt.charAt(j);
                    if (cc == '\n') {
                        break;
                    } else if (cc == '"' && j < length - 1 && txt.charAt(j + 1) != '\'') {
                        isClosed = false;
                        break;
                    }
                }

                if (isClosed) {
                    return true;
                }
            } else if (c == '/' && i < length - 1 && txt.charAt(i + 1) == '*') {
                // start of another comment block
                return false;
            } else if (c == '\n') {
                quotation = 0;
            } else if (c == '"' && i < length - 1 && txt.charAt(i + 1) != '\'') {
                quotation = ++quotation % 2;
            }
        }

        return false;
    }
    public static enum LineBalance {
        PLAIN,
        UP_FIRST,// } keyword {
        DOWN_FIRST
    };

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
        boolean[] insertEndResult, boolean[] insertRBraceResult, int[] startOffsetResult,
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
            LexUtilities.getLineBalance(doc, offset, PHPTokenId.PHP_CURLY_OPEN, PHPTokenId.PHP_CURLY_CLOSE,
            LineBalance.UP_FIRST);

        if ((beginEndBalance == 1) || (braceBalance == 1)) {
            // There is one more opening token on the line than a corresponding
            // closing token.  (If there's is more than one we don't try to help.)
            int indent = GsfUtilities.getLineIndent(doc, offset);

            // Look for the next nonempty line, and if its indent is > indent,
            // or if its line balance is -1 (e.g. it's an end) we're done
            boolean insertEnd = beginEndBalance > 0;
            boolean insertRBrace = braceBalance > 0;
            int next = Utilities.getRowEnd(doc, offset) + 1;

            for (; next < length; next = Utilities.getRowEnd(doc, next) + 1) {
                if (Utilities.isRowEmpty(doc, next) || Utilities.isRowWhite(doc, next) ||
                        LexUtilities.isCommentOnlyLine(doc, next)) {
                    continue;
                }

                int nextIndent = GsfUtilities.getLineIndent(doc, next);

                if (nextIndent > indent) {
                    insertEnd = false;
                    insertRBrace = false;
                } else if (nextIndent == indent) {
                    if (insertEnd) {
                        if (LexUtilities.getBeginEndLineBalance(doc, next, false) < 0) {
                            insertEnd = false;
                        } else {
                            // See if I have a structure word like "else", "ensure", etc.
                            // (These are indent words that are not also begin words)
                            // and if so refrain from inserting the end
                            int lineBegin = Utilities.getRowFirstNonWhite(doc, next);

                            Token<?extends PHPTokenId> token =
                                LexUtilities.getToken(doc, lineBegin);

                            if ((token != null) && LexUtilities.isIndentEndToken(token.id())) {
                                insertEnd = false;
                            }
                        }
                    } else if (insertRBrace &&
                            (LexUtilities.getLineBalance(doc, next, PHPTokenId.PHP_CURLY_OPEN,
                                PHPTokenId.PHP_CURLY_CLOSE, LineBalance.DOWN_FIRST) < 0)) {
                        insertRBrace = false;
                    }
                }

                break;
            }

            if (insertEndResult != null) {
                insertEndResult[0] = insertEnd;
            }

            if (insertRBraceResult != null) {
                insertRBraceResult[0] = insertRBrace;
            }

            if (indentResult != null) {
                indentResult[0] = indent;
            }

            return insertEnd || insertRBrace;
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
                        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPositionedSequence(doc, start);
                        if (ts != null && (!isStringToken(ts.token()) || firstChar == '\"' || firstChar == '\'')) {
                            int lastChar = selection.charAt(selection.length()-1);
                            // Replace the surround-with chars?
                            if (selection.length() > 1 && 
                                    ((firstChar == '"' || firstChar == '\'' || firstChar == '(' || 
                                    firstChar == '{' || firstChar == '[' || firstChar == '/') &&
                                    lastChar == matching(firstChar))) {
                                doc.remove(end-1, 1);
                                doc.insertString(end-1, Character.toString(matching(ch)), null);
                                doc.remove(start, 1);
                                doc.insertString(start, Character.toString(ch), null);
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

        TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        ts.move(caretOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<?extends PHPTokenId> token = ts.token();
        TokenId id = token.id();
        TokenId[] stringTokens = null;
        TokenId beginTokenId = null;
        
        if (id == PHPTokenId.PHP_LINE_COMMENT && target.getSelectionStart() != -1) {
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
            beginTokenId = PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
        } else if (ch == '\'') {
            stringTokens = STRING_TOKENS;
            beginTokenId = PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
//        } else if (id == PHPTokenId.UNKNOWN_TOKEN) {
//            String text = token.text().toString();
//
//            if (text.equals("%")) {
//                // Depending on the character we're going to continue
//                if (!Character.isLetter(ch)) { // %q, %x, etc. Only %[], %!!, %<space> etc. is allowed
//                    stringTokens = STRING_TOKENS;
//                    beginTokenId = PHPTokenId.PHP_QUOTED_STRING_BEGIN;
//                }
//            } else if ((text.length() == 2) && (text.charAt(0) == '%') &&
//                    Character.isLetter(text.charAt(1))) {
//                char c = text.charAt(1);
//
//                switch (c) {
//                case 'q':
//                    stringTokens = STRING_TOKENS;
//                    beginTokenId = PHPTokenId.PHP_STRING_BEGIN;
//
//                    break;
//
//                case 'Q':
//                    stringTokens = STRING_TOKENS;
//                    beginTokenId = PHPTokenId.PHP_QUOTED_STRING_BEGIN;
//
//                    break;
//
//                case 'r':
//                    stringTokens = REGEXP_TOKENS;
//                    beginTokenId = PHPTokenId.PHP_REGEXP_BEGIN;
//
//                    break;
//
//                default:
//                    // ?
//                    stringTokens = STRING_TOKENS;
//                    beginTokenId = PHPTokenId.PHP_QUOTED_STRING_BEGIN;
//                }
//            } else {
//                ts.movePrevious();
//
//                TokenId prevId = ts.token().id();
//
//                if ((prevId == PHPTokenId.PHP_STRING_BEGIN) ||
//                        (prevId == PHPTokenId.PHP_QUOTED_STRING_BEGIN)) {
//                    stringTokens = STRING_TOKENS;
//                    beginTokenId = prevId;
//                } else if (prevId == PHPTokenId.PHP_REGEXP_BEGIN) {
//                    stringTokens = REGEXP_TOKENS;
//                    beginTokenId = PHPTokenId.PHP_REGEXP_BEGIN;
//                }
//            }
//        } else if (((((id == PHPTokenId.PHP_STRING_BEGIN) || (id == PHPTokenId.PHP_QUOTED_STRING_BEGIN)) &&
//                (caretOffset == (ts.offset() + 1))))) {
//            if (!Character.isLetter(ch)) { // %q, %x, etc. Only %[], %!!, %<space> etc. is allowed
//                stringTokens = STRING_TOKENS;
//                beginTokenId = id;
//            }
//        } else if (((id == PHPTokenId.PHP_STRING_BEGIN) && (caretOffset == (ts.offset() + 2))) ||
//                (id == PHPTokenId.PHP_STRING_END)) {
//            stringTokens = STRING_TOKENS;
//            beginTokenId = PHPTokenId.PHP_STRING_BEGIN;
//        } else if (((id == PHPTokenId.PHP_QUOTED_STRING_BEGIN) && (caretOffset == (ts.offset() + 2))) ||
//                (id == PHPTokenId.PHP_QUOTED_STRING_END)) {
//            stringTokens = STRING_TOKENS;
//            beginTokenId = PHPTokenId.PHP_QUOTED_STRING_BEGIN;
//        } else if (((id == PHPTokenId.PHP_REGEXP_BEGIN) && (caretOffset == (ts.offset() + 2))) ||
//                (id == PHPTokenId.PHP_REGEXP_END)) {
//            stringTokens = REGEXP_TOKENS;
//            beginTokenId = PHPTokenId.PHP_REGEXP_BEGIN;
        }

        if (stringTokens != null) {
            boolean inserted = completeQuote(doc, caretOffset, ch);

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
    //    TokenSequence< ?extends PHPTokenId> ts = LexUtilities.getTokenSequence(doc);
    //
    //    System.out.println("Dumping tokens for dot=" + dot);
    //    int prevOffset = -1;
    //    if (ts != null) {
    //        ts.moveFirst();
    //        int index = 0;
    //        do {
    //            Token<? extends PHPTokenId> token = ts.token();
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
// XXX: reflow comments
//        if (REFLOW_COMMENTS) {
//            Token<?extends PHPTokenId> token = LexUtilities.getToken(doc, dotPos);
//            if (token != null) {
//                TokenId id = token.id();
//                if (id == PHPTokenId.PHP_LINE_COMMENT || id == PHPTokenId.PHP_DOCUMENTATION) {
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
                TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, dotPos);

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
        case '}':
        case '{':
        case ')':
        case ']':
        case '(':
        case '[': {
            
            if (!isInsertMatchingEnabled(doc)) {
                return false;
            }

            
            Token<?extends PHPTokenId> token = LexUtilities.getToken(doc, dotPos);
            if (token == null) {
                return true;
            }
            TokenId id = token.id();

            if (((id == PHPTokenId.PHP_VARIABLE) && (token.length() == 1)) ||
                    (LexUtilities.textEquals(token.text(), '[')) || (LexUtilities.textEquals(token.text(), ']')) ||
//                    (id == PHPTokenId.PHP_CURLY_OPEN) || (id == PHPTokenId.PHP_CURLY_CLOSE) ||
                    (LexUtilities.textEquals(token.text(), '(')) || (LexUtilities.textEquals(token.text(), ')'))) {
                if (ch == ']' || ch == ')') { // || ch == '}'
                    skipClosingBracket(doc, caret, ch);
                } else if ((ch == '[') || (ch == '(')) {//  || (ch == '{')
                    completeOpeningBracket(doc, dotPos, caret, ch);
                }
            }

            // Reindent blocks (won't do anything if } is not at the beginning of a line
            if (ch == '}') {
                reindent(doc, dotPos, PHPTokenId.PHP_CURLY_CLOSE, caret);
            }
        }

        break;
// XXX: ruby specific, but we may need something similar
//        case 'd':
//            // See if it's the end of an "end" - if so, reindent
//            reindent(doc, dotPos, PHPTokenId.PHP_END, caret);
//
//            break;
//
//        case 'e':
//            // See if it's the end of an "else" or an "ensure" - if so, reindent
//            reindent(doc, dotPos, PHPTokenId.PHP_ELSE, caret);
//            reindent(doc, dotPos, PHPTokenId.PHP_ENSURE, caret);
//            reindent(doc, dotPos, PHPTokenId.PHP_RESCUE, caret);
//
//            break;
//
//        case 'f':
//            // See if it's the end of an "else" - if so, reindent
//            reindent(doc, dotPos, PHPTokenId.PHP_ELSIF, caret);
//
//            break;
//
//        case 'n':
//            // See if it's the end of an "when" - if so, reindent
//            reindent(doc, dotPos, PHPTokenId.PHP_WHEN, caret);
//            
//            break;
//            
//        case '/': {
//            if (!isInsertMatchingEnabled(doc)) {
//                return false;
//            }
//
//            // Bracket matching for regular expressions has to be done AFTER the
//            // character is inserted into the document such that I can use the lexer
//            // to determine whether it's a division (e.g. x/y) or a regular expression (/foo/)
//            Token<?extends PHPTokenId> token = LexUtilities.getToken(doc, dotPos);
//            if (token != null) {
//                TokenId id = token.id();
//
//                if (id == PHPTokenId.PHP_REGEXP_BEGIN || id == PHPTokenId.PHP_REGEXP_END) {
//                    TokenId[] stringTokens = REGEXP_TOKENS;
//                    TokenId beginTokenId = PHPTokenId.PHP_REGEXP_BEGIN;
//
//                    boolean inserted =
//                        completeQuote(doc, dotPos, caret, ch, stringTokens, beginTokenId);
//
//                    if (inserted) {
//                        caret.setDot(dotPos + 1);
//                    }
//
//                    return inserted;
//                }
//            }
//            break;
//        }
//        case '|': {
//            if (!isInsertMatchingEnabled(doc)) {
//                return false;
//            }
//
//            Token<?extends PHPTokenId> token = LexUtilities.getToken(doc, dotPos);
//            if (token == null) {
//                return true;
//            }
//
//            TokenId id = token.id();
//
//            // Ensure that we're not in a comment, strings etc.
//            if (id == PHPTokenId.PHP_NONUNARY_OP && token.length() == 2 && "||".equals(token.text().toString())) {
//                // Type through: |^| and type |
//                // See if we're in a do or { block
//                if (isBlockDefinition(doc, dotPos)) {
//                    // It's a block so this should be a variable declaration of the form { |foo| }
//                    // Did you type "|" in the middle? If so, should type through!
//                    // TODO - check that we were typing in the middle, not in the front!
//                    doc.remove(dotPos, 1);
//                    caret.setDot(dotPos+1);
//                        
//                    return true;
//                }
//                
//            } else if (id == PHPTokenId.PHP_IDENTIFIER && token.length() == 1 && "|".equals(token.text().toString())) {
//                // Only insert a matching | if there aren't any others on this line AND we're in a block
//                if (isBlockDefinition(doc, dotPos)) {
//                    boolean found = false;
//                    int lineEnd = Utilities.getRowEnd(doc, dotPos);
//                    if (lineEnd > dotPos+1) {
//                        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, dotPos+1);
//                        ts.move(dotPos+1);
//                        while (ts.moveNext() && ts.offset() < lineEnd) {
//                            Token<? extends PHPTokenId> t = ts.token();
//                            if (t.id() == PHPTokenId.PHP_IDENTIFIER && t.length() == 1 && "|".equals(t.text().toString())) {
//                                found = true;
//                                break;
//                            }
//                        }
//                    }
//
//                    if (!found) {
//                        doc.insertString(dotPos+1, "|", null);
//                        caret.setDot(dotPos + 1);
//                    }
//                }
//                
//                return true;
//            }
//            break;
//        }
        }

        return true;
    }

    private void reindent(BaseDocument doc, int offset, TokenId id, Caret caret)
        throws BadLocationException {
        TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);

        if (ts != null) {
            ts.move(offset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }

            Token<?extends PHPTokenId> token = ts.token();

            if ((token.id() == id)) {
                final int rowFirstNonWhite = Utilities.getRowFirstNonWhite(doc, offset);
                // Ensure that this token is at the beginning of the line
                if (ts.offset() > rowFirstNonWhite) {
                    return;
                }

                OffsetRange begin;

                if (id == PHPTokenId.PHP_CURLY_CLOSE) {
                    begin = LexUtilities.findBwd(doc, ts, '{', '}');
                } else {
                    begin = LexUtilities.findBegin(doc, ts);
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

    /** Replaced by PHPBracesMatcher */
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
        BaseDocument doc = (BaseDocument) document;

        switch (ch) {
            case ' ': {
                // Backspacing over "# " ? Delete the "#" too!
                TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, dotPos);

                if (ts != null){
                    ts.move(dotPos);
                    if ((ts.moveNext() || ts.movePrevious()) && (ts.offset() == dotPos - 1 && ts.token().id() == PHPTokenId.PHP_LINE_COMMENT)) {
                        doc.remove(dotPos - 1, 1);
                        target.getCaret().setDot(dotPos - 1);

                        return true;
                    }
                }
                break;
            }

            case '{':
            case '(':
            case '[': {
                char tokenAtDot = LexUtilities.getTokenChar(doc, dotPos);

                if (((tokenAtDot == ']') &&
                        (LexUtilities.getTokenBalance(doc, '[', ']', dotPos) != 0)) ||
                        ((tokenAtDot == ')') &&
                        (LexUtilities.getTokenBalance(doc, '(', ')', dotPos) != 0)) ||
                        ((tokenAtDot == '}') &&
                        (LexUtilities.getTokenBalance(doc, '{', '}', dotPos) != 0))) {
                    doc.remove(dotPos, 1);
                }
                break;
            }
//        case '|':
//        case '/': 
            case '\"':
            case '\'': {
                char[] match = doc.getChars(dotPos, 1);

                if ((match != null) && (match[0] == ch)) {
                    doc.remove(dotPos, 1);
                }
            }
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
    private void skipClosingBracket(BaseDocument doc, Caret caret, char bracket)
        throws BadLocationException {
        int caretOffset = caret.getDot();

        if (isSkipClosingBracket(doc, caretOffset, bracket)) {
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
    private boolean isSkipClosingBracket(BaseDocument doc, int caretOffset, char bracket)
        throws BadLocationException {
        // First check whether the caret is not after the last char in the document
        // because no bracket would follow then so it could not be skipped.
        if (caretOffset == doc.getLength()) {
            return false; // no skip in this case
        }

        boolean skipClosingBracket = false; // by default do not remove

        TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        // XXX BEGIN TOR MODIFICATIONS
        //ts.move(caretOffset+1);
        ts.move(caretOffset);

        if (!ts.moveNext()) {
            return false;
        }

        Token<?extends PHPTokenId> token = ts.token();

        // Check whether character follows the bracket is the same bracket
        if ((token != null) && (LexUtilities.textEquals(token.text(), bracket))) {
//            int bracketIntId = bracketId.ordinal();
//            int leftBracketIntId =
//                (bracketIntId == PHPTokenId.PHP_RPAREN.ordinal()) ? PHPTokenId.PHP_LPAREN.ordinal()
//                                                               : PHPTokenId.PHP_LBRACKET.ordinal();

            char leftBracket = bracket == ')' ? '(' : (bracket == ']' ? '[' : '{');
            
            // Skip all the brackets of the same type that follow the last one
            ts.moveNext();

            Token<?extends PHPTokenId> nextToken = ts.token();

            while ((nextToken != null) && (LexUtilities.textEquals(nextToken.text(), bracket))) {
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
            Token<?extends PHPTokenId> lastRBracket = token;
            ts.movePrevious();
            token = ts.token();

            boolean finished = false;

            while (!finished && (token != null)) {
                if ((LexUtilities.textEquals(token.text(), '(')) || (LexUtilities.textEquals(token.text(), '['))) {
                    if (LexUtilities.textEquals(token.text(), leftBracket)) {
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
                } else if ((LexUtilities.textEquals(token.text(), ']')) || (LexUtilities.textEquals(token.text(), ']'))) {
                    if (LexUtilities.textEquals(token.text(), bracket)) {
                        bracketBalance--;
                    }
                } else if (token.id() == PHPTokenId.PHP_CURLY_OPEN) {
                    braceBalance++;

                    if (braceBalance > 0) { // stop on extra left brace
                        finished = true;
                    }
                } else if (token.id() == PHPTokenId.PHP_CURLY_CLOSE) {
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
                    if ((LexUtilities.textEquals(token.text(), '(')) || (LexUtilities.textEquals(token.text(), '['))) {
                        if (LexUtilities.textEquals(token.text(), leftBracket)) {
                            bracketBalance++;
                        }
                    } else if ((LexUtilities.textEquals(token.text(), ')')) || (LexUtilities.textEquals(token.text(), ']'))) {
                        if (LexUtilities.textEquals(token.text(), bracket)) {
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
                    } else if (token.id() == PHPTokenId.PHP_CURLY_OPEN) {
                        braceBalance++;
                    } else if (token.id() == PHPTokenId.PHP_CURLY_CLOSE) {
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
    // TODO Adjust for Ruby
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
    private boolean completeQuote(BaseDocument doc, int dotPos, char bracket) throws BadLocationException {
        // No chars completion when escaping, eg \" or \' typed
        if (isEscapeSequence(doc, dotPos)) {
            return false;
        }

        // Find the token sequence and look at what token is under the caret
        Object [] result = findPhpSectionBoundaries(doc, dotPos, true);
        if (result == null) {
            // not in PHP section
            return false;
        }

        @SuppressWarnings("unchecked")
        TokenSequence<? extends PHPTokenId> ts = (TokenSequence<? extends PHPTokenId>)result[0];
        int sectionStart = (Integer) result[1];
        int sectionEnd = (Integer) result[2];
        boolean onlyWhitespacePreceeds = (Boolean) result[3];
        boolean onlyWhitespaceFollows = (Boolean) result[4];
        
        Token<?extends PHPTokenId> token = ts.token();

        if (token == null){ // Issue #151886
            return false;
        }

        Token<?extends PHPTokenId> previousToken = ts.movePrevious() ? ts.token() : null;

        // Check if we are inside a comment
        if (token.id() == PHPTokenId.PHP_COMMENT || 
            token.id() == PHPTokenId.PHP_LINE_COMMENT ||
            token.id() == PHPTokenId.PHPDOC_COMMENT ||
            token.id() == PHPTokenId.T_INLINE_HTML // #132981
        ) {
            return false;
        }

        // Check if we are inside a string
        boolean insideString = isStringToken(token);
        if (!insideString) {
            if (onlyWhitespaceFollows && previousToken != null && isStringToken(previousToken)) {
                // The same as for the line comment above. We could be at the EOL
                // of a string literal, token is the EOL whitespace,
                // but the previous token is PHP string
                insideString = true;
            }
        }

        if (insideString) {
            if (onlyWhitespaceFollows) {
                return false;
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
        } else {
            boolean insert = onlyWhitespaceFollows;
            if (!insert) {
                int firstNonWhiteFwd = Utilities.getFirstNonWhiteFwd(doc, dotPos, sectionEnd);
                if (firstNonWhiteFwd != -1) {
                    char chr = doc.getChars(firstNonWhiteFwd, 1)[0];
                    insert = chr == ')' || chr == ',' || chr == '+' || chr == '}' || //NOI18N
                             chr == ';' || chr == ']' || chr == '.'; //NOI18N
                }
            }
            
            if (insert) {
                doc.insertString(dotPos, "" + bracket + (isAfter ? "" : matching(bracket)), null); //NOI18N
                return true;
            }
        }

        return false;
    }
    
    private static Object [] findPhpSectionBoundaries(BaseDocument doc, int offset, boolean currentLineOnly) {
        TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
        if (ts == null) {
            return null;
        }

        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }
        
        // determine the row boundaries
        int lowest = 0;
        int highest = doc.getLength();
        if (currentLineOnly) {
            lowest = doc.getParagraphElement(offset).getStartOffset();
            highest = Math.max(doc.getParagraphElement(offset).getEndOffset() - 1, lowest);
        }
        
        // find the section end
        int sectionEnd = highest;
        boolean onlyWhitespaceFollows = true;
        do {
            if (highest < ts.offset()) {
                break;
            }
            
            if (ts.token().id() == PHPTokenId.PHP_CLOSETAG) {
                sectionEnd = ts.offset();
                break;
            } else if (ts.token().id() != PHPTokenId.WHITESPACE) {
                onlyWhitespaceFollows = false;
            }
        } while (ts.moveNext());

        // find the section start
        int sectionStart = lowest;
        boolean onlyWhitespacePreceeds = true;
        while (ts.movePrevious()) {
            if (lowest > ts.offset()) {
                break;
            }
            
            if (ts.token().id() == PHPTokenId.PHP_OPENTAG) {
                sectionStart = ts.offset();
                break;
            } else if (ts.token().id() != PHPTokenId.WHITESPACE) {
                onlyWhitespacePreceeds = false;
            }
        }
        
        // re-position the sequence
        ts.move(offset);
        if (!ts.moveNext()) {
            assert ts.movePrevious();
        }
        
        assert sectionStart != -1 && sectionEnd != -1 : "sectionStart=" + sectionStart + ", sectionEnd=" + sectionEnd; //NOI18N
        return new Object [] { ts, sectionStart, sectionEnd, onlyWhitespacePreceeds, onlyWhitespaceFollows };
    }
    
    private static boolean isStringToken(Token<? extends PHPTokenId> token) {
        for (PHPTokenId stringTokenId : STRING_TOKENS) {
            if (token.id() == stringTokenId) {
                return true;
            }
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

//    private boolean isQuoteCompletablePosition(BaseDocument doc, int dotPos) throws BadLocationException {
//        if (dotPos == doc.getLength()) { // there's no other character to test
//
//            return true;
//        } else {
//            // test that we are in front of ) , " or ' ... etc.
//            int eol = LexUtilities.findPhpSectionEnd(doc, dotPos, true, 0);
//
//            if ((dotPos == eol) || (eol == -1)) {
//                return false;
//            }
//
//            int firstNonWhiteFwd = Utilities.getFirstNonWhiteFwd(doc, dotPos, eol);
//
//            if (firstNonWhiteFwd == -1) {
//                return false;
//            }
//
//            char chr = doc.getChars(firstNonWhiteFwd, 1)[0];
//
//            return ((chr == ')') || (chr == ',') || (chr == '+') || (chr == '}') || (chr == ';') ||
//               (chr == ']') || (chr == '/'));
//        }
//    }

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
//        Node root = AstUtilities.getRoot(info);
//
//        if (root == null) {
//            return Collections.emptyList();
//        }
//
//        int astOffset = AstUtilities.getAstOffset(info, caretOffset);
//        if (astOffset == -1) {
//            return Collections.emptyList();
//        }
//
//        AstPath path = new AstPath(root, astOffset);
//        List<OffsetRange> ranges = new ArrayList<OffsetRange>();
//        
//        /** Furthest we can go back in the buffer (in RHTML documents, this
//         * may be limited to the surrounding &lt;% starting tag
//         */
//        int min = 0;
//        int max = Integer.MAX_VALUE;
//        int length;
//
//        // Check if the caret is within a comment, and if so insert a new
//        // leaf "node" which contains the comment line and then comment block
//        try {
//            BaseDocument doc = (BaseDocument)info.getDocument();
//            length = doc.getLength();
//
//            if (RubyUtils.isRhtmlDocument(doc)) {
//                TokenHierarchy th = TokenHierarchy.get(doc);
//                TokenSequence ts = th.tokenSequence();
//                ts.move(caretOffset);
//                if (ts.moveNext() || ts.movePrevious()) {
//                    Token t = ts.token();
//                    if (t.id().primaryCategory().startsWith("ruby")) { // NOI18N
//                        min = ts.offset();
//                        max = min+t.length();
//                        // Try to extend with delimiters too
//                        if (ts.movePrevious()) {
//                            t = ts.token();
//                            if ("ruby-delimiter".equals(t.id().primaryCategory())) { // NOI18N
//                                min = ts.offset();
//                                if (ts.moveNext() && ts.moveNext()) {
//                                    t = ts.token();
//                                    if ("ruby-delimiter".equals(t.id().primaryCategory())) { // NOI18N
//                                        max = ts.offset()+t.length();
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            Token<?extends PHPTokenId> token = LexUtilities.getToken(doc, caretOffset);
//            
//            if ((token != null) && (token.id() == PHPTokenId.PHP_LINE_COMMENT)) {
//                // First add a range for the current line
//                int begin = Utilities.getRowStart(doc, caretOffset);
//                int end = Utilities.getRowEnd(doc, caretOffset);
//
//                if (LexUtilities.isCommentOnlyLine(doc, caretOffset)) {
//                    ranges.add(new OffsetRange(Utilities.getRowFirstNonWhite(doc, begin), 
//                            Utilities.getRowLastNonWhite(doc, end)+1));
//
//                    int lineBegin = begin;
//                    int lineEnd = end;
//
//                    while (begin > 0) {
//                        int newBegin = Utilities.getRowStart(doc, begin - 1);
//
//                        if ((newBegin < 0) || !LexUtilities.isCommentOnlyLine(doc, newBegin)) {
//                            begin = Utilities.getRowFirstNonWhite(doc, begin);
//                            break;
//                        }
//
//                        begin = newBegin;
//                    }
//
//                    while (true) {
//                        int newEnd = Utilities.getRowEnd(doc, end + 1);
//
//                        if ((newEnd >= length) || !LexUtilities.isCommentOnlyLine(doc, newEnd)) {
//                            end = Utilities.getRowLastNonWhite(doc, end)+1;
//                            break;
//                        }
//
//                        end = newEnd;
//                    }
//
//                    if ((lineBegin > begin) || (lineEnd < end)) {
//                        ranges.add(new OffsetRange(begin, end));
//                    }
//                } else {
//                    // It's just a line comment next to some code; select the comment
//                    TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
//                    int offset = token.offset(th);
//                    ranges.add(new OffsetRange(offset, offset + token.length()));
//                }
//            } else if (token != null && token.id() == PHPTokenId.PHP_DOCUMENTATION) {
//                // Select the whole token block
//                TokenHierarchy<BaseDocument> th = TokenHierarchy.get(doc);
//                int begin = token.offset(th);
//                int end = begin + token.length();
//                ranges.add(new OffsetRange(begin, end));
//            }
//        } catch (BadLocationException ble) {
//            Exceptions.printStackTrace(ble);
//            return ranges;
//        } catch (IOException ioe) {
//            Exceptions.printStackTrace(ioe);
//            return ranges;
//        }
//
//        Iterator<Node> it = path.leafToRoot();
//
//        OffsetRange previous = OffsetRange.NONE;
//        while (it.hasNext()) {
//            Node node = it.next();
//
//            // Filter out some uninteresting nodes
//            if (node instanceof NewlineNode) {
//                continue;
//            }
//
//            OffsetRange range = AstUtilities.getRange(node);
//            
//            // The contains check should be unnecessary, but I end up getting
//            // some weird positions for some JRuby AST nodes
//            if (range.containsInclusive(astOffset) && !range.equals(previous)) {
//                range = LexUtilities.getLexerOffsets(info, range);
//                if (range != OffsetRange.NONE) {
//                    if (range.getStart() < min) {
//                        ranges.add(new OffsetRange(min, max));
//                        ranges.add(new OffsetRange(0, length));
//                        break;
//                    }
//                    ranges.add(range);
//                    previous = range;
//                }
//            }
//        }
//
        return Collections.<OffsetRange>emptyList();
    }

    // UGH - this method has gotten really ugly after successive refinements based on unit tests - consider cleaning up
    public int getNextWordOffset(Document document, int offset, boolean reverse) {
        BaseDocument doc = (BaseDocument)document;
        TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
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

        Token<? extends PHPTokenId> token = ts.token();
        TokenId id = token.id();

        if (id == PHPTokenId.WHITESPACE) {
            // Just eat up the space in the normal IDE way
            if ((reverse && ts.offset() < offset) || (!reverse && ts.offset() > offset)) {
                return ts.offset();
            }
            while (id == PHPTokenId.WHITESPACE) {
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

        if (id == PHPTokenId.PHP_VARIABLE) {
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
