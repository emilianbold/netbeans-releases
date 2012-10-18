/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import java.util.*;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
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
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.options.OptionsUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.util.Exceptions;

/**
 * Provide bracket completion for Ruby. This class provides three broad
 * services: - Identifying matching pairs (parentheses, begin/end pairs etc.),
 * which is used both for highlighting in the IDE (when the caret is on for
 * example an if statement, the corresponding end token is highlighted), and
 * navigation where you can jump between matching pairs. - Automatically
 * inserting corresponding pairs when you insert a character. For example, if
 * you insert a single quote, a corresponding ending quote is inserted - unless
 * you're typing "over" the existing quote (you should be able to type foo =
 * "hello" without having to arrow over the second quote that was inserted after
 * you typed the first one). - Automatically adjusting indentation in some
 * scenarios, for example when you type the final "d" in "end" - and readjusting
 * it back to the original indentation if you continue typing something other
 * than "end", e.g. "endian".
 *
 * The logic around inserting matching ""'s is heavily based on the Java editor
 * implementation, and probably should be rewritten to be Ruby oriented. One
 * thing they did is process the characters BEFORE the character has been
 * inserted into the document. This has some advantages - it's easy to detect
 * whether you're typing in the middle of a string since the token hierarchy has
 * not been updated yet. On the other hand, it makes it hard to identify whether
 * some characters are what we expect - is a "/" truly a regexp starter or
 * something else? The Ruby lexer has lots of logic and state to determine this.
 * I think it would be better to switch to after-insert logic for this.
 *
 * @todo Match braces within literal strings, as in #{}
 * @todo Match || in the argument list of blocks? do { |foo| etc. }
 * @todo I'm currently highlighting the indentation tokens (else, elsif, ensure,
 * etc.) by finding the corresponding begin. For "illegal" tokens, e.g. def foo;
 * else; end; this means I'll show "def" as the matching token for else, which
 * is wrong. I should make the "indentation tokens" list into a map and
 * associate them with their corresponding tokens, such that an else is only
 * lined up with an if, etc.
 * @todo Pressing newline in a parameter list doesn't work well if it's on a
 * blockdefining line - e.g. def foo(a,b => it will insert the end BEFORE the
 * closing paren!
 * @todo Pressing space in a comment beyond the textline limit should wrap text?
 * http://ruby.netbeans.org/issues/show_bug.cgi?id=11553
 * @todo Make ast-selection pick up =begin/=end documentation blocks
 *
 * @author Tor Norbye
 */
public class PHPBracketCompleter implements KeystrokeHandler {

    // XXX: this should made it to options and be supported in java for example
    /**
     * When true, continue comments if you press return in a line comment (that
     * does not also have code on the same line
     */
    static final boolean CONTINUE_COMMENTS = Boolean.getBoolean("php.cont.comment"); // NOI18N
    /**
     * Tokens which indicate that we're within a literal string
     */
    private final static PHPTokenId[] STRING_TOKENS = {
        PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING,
        PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE
    };
    /**
     * When != -1, this indicates that we previously adjusted the indentation of
     * the line to the given offset, and if it turns out that the user changes
     * that token, we revert to the original indentation
     */
    private int previousAdjustmentOffset = -1;
    /**
     * True iff we're processing bracket matching AFTER the key has been
     * inserted rather than before
     */
    private boolean isAfter;
    /**
     * The indentation to revert to when previousAdjustmentOffset is set and the
     * token changed
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

    private PHPTokenId findContextForEnd(TokenSequence<? extends PHPTokenId> ts, int offset, int[] startOfContext) {
        if (ts == null) {
            return null;
        }
        if (ts.offset() != offset) {
            ts.move(offset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return null;
            }
        }

        PHPTokenId returnValeu = null;
        PHPTokenId previousToken = null;
        if (ts.movePrevious()) {
            previousToken = ts.token().id();
            ts.moveNext();
        }

        if (previousToken == PHPTokenId.PHPDOC_COMMENT_START || previousToken == PHPTokenId.PHP_COMMENT_START) {
            return null;
        }
        // at fist there should be find a bracket  '{' or column ':'
        Token<? extends PHPTokenId> bracketColumnToken = LexUtilities.findPrevious(ts,
                Arrays.asList(PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                PHPTokenId.PHPDOC_COMMENT_START, PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END,
                PHPTokenId.PHP_LINE_COMMENT, PHPTokenId.WHITESPACE, PHPTokenId.PHP_CLOSETAG));
        if (bracketColumnToken != null
                && (bracketColumnToken.id() == PHPTokenId.PHP_CURLY_OPEN
                || (bracketColumnToken.id() == PHPTokenId.PHP_TOKEN && ":".equals(ts.token().text().toString())))) {
            startOfContext[0] = ts.offset();
            // we are interested only in adding end for { or alternative syntax :
            List<PHPTokenId> lookFor = Arrays.asList(PHPTokenId.PHP_CURLY_CLOSE, //PHPTokenId.PHP_SEMICOLON,
                    PHPTokenId.PHP_CLASS, PHPTokenId.PHP_FUNCTION,
                    PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF,
                    PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH,
                    PHPTokenId.PHP_DO, PHPTokenId.PHP_WHILE, PHPTokenId.PHP_TOKEN,
                    PHPTokenId.PHP_SWITCH, PHPTokenId.PHP_CASE, PHPTokenId.PHP_OPENTAG);
            Token<? extends PHPTokenId> keyToken = LexUtilities.findPreviousToken(ts, lookFor);
            while (keyToken.id() == PHPTokenId.PHP_TOKEN) {
                if ("?".equals(keyToken.text().toString())) { //NOI18N
                    return null;
                }
                ts.movePrevious();
                keyToken = LexUtilities.findPreviousToken(ts, lookFor);
            }
            if (keyToken.id() == PHPTokenId.PHP_CASE) {
                return null;
            }
            if (bracketColumnToken.id() == PHPTokenId.PHP_CURLY_OPEN) {
                if (keyToken.id() == PHPTokenId.PHP_CLASS || keyToken.id() == PHPTokenId.PHP_FUNCTION) {
                    returnValeu = keyToken.id();
                } else {
                    returnValeu = PHPTokenId.PHP_CURLY_OPEN;
                }
            } else {
                if (bracketColumnToken.id() == PHPTokenId.PHP_TOKEN && ":".equals(bracketColumnToken.text().toString())) {
                    if (keyToken.id() != PHPTokenId.PHP_OPENTAG
                            && keyToken.id() != PHPTokenId.PHP_CLASS
                            && keyToken.id() != PHPTokenId.PHP_FUNCTION) {
                        returnValeu = keyToken.id();
                    }
                }
            }
            if (keyToken.id() != PHPTokenId.PHP_CURLY_CLOSE && keyToken.id() != PHPTokenId.PHP_SEMICOLON) {
                startOfContext[0] = ts.offset();
            }
        }

        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }
        return returnValeu;
    }

    private boolean isEndMissing(BaseDocument doc, int offset, PHPTokenId startTokenId) throws BadLocationException {
        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
        if (ts == null) {
            return false;
        }
        ts.move(0);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }
        Token<? extends PHPTokenId> token;
        int curlyBalance = 0;
        boolean curlyProcessed = false;
        if (startTokenId == PHPTokenId.PHP_CURLY_OPEN || startTokenId == PHPTokenId.PHP_FUNCTION
                || startTokenId == PHPTokenId.PHP_CLASS) {
            boolean unfinishedComment = false;
            do {
                token = ts.token();
                if (token.id() == PHPTokenId.PHP_CURLY_CLOSE) {
                    curlyBalance--;
                    curlyProcessed = true;
                } else if (token.id() == PHPTokenId.PHP_CURLY_OPEN
                        || (token.id() == PHPTokenId.PHP_TOKEN && "${".equals(token.text().toString()))) { //NOI18N
                    curlyBalance++;
                    curlyProcessed = true;
                } else if (token.id() == PHPTokenId.PHP_COMMENT_START || token.id() == PHPTokenId.PHPDOC_COMMENT_START) {
                    unfinishedComment = true;
                } else if (token.id() == PHPTokenId.PHP_COMMENT_END || token.id() == PHPTokenId.PHPDOC_COMMENT_END) {
                    unfinishedComment = false;
                }
                if (curlyBalance == 0 && curlyProcessed && ts.offset() > offset) {
                    break;
                }
            } while (ts.moveNext());
            if (unfinishedComment) {
                curlyBalance--;
            }
        } else {
            // complete alternative syntax.
            PHPTokenId endTokenId = null;
            if (startTokenId == PHPTokenId.PHP_FOR) {
                endTokenId = PHPTokenId.PHP_ENDFOR;
            } else if (startTokenId == PHPTokenId.PHP_FOREACH) {
                endTokenId = PHPTokenId.PHP_ENDFOREACH;
            } else if (startTokenId == PHPTokenId.PHP_WHILE) {
                endTokenId = PHPTokenId.PHP_ENDWHILE;
            } else if (startTokenId == PHPTokenId.PHP_SWITCH) {
                endTokenId = PHPTokenId.PHP_ENDSWITCH;
            } else if (startTokenId == PHPTokenId.PHP_IF) {
                endTokenId = PHPTokenId.PHP_ENDIF;
            } else if (startTokenId == PHPTokenId.PHP_ELSE || startTokenId == PHPTokenId.PHP_ELSEIF) {
                startTokenId = PHPTokenId.PHP_IF;
                endTokenId = PHPTokenId.PHP_ENDIF;
            }
            ts.move(0);
            if (!ts.moveNext() && !ts.movePrevious()) {
                return false;
            }
            int balance = 0;
            boolean checkAlternativeSyntax = false;
            do {
                token = ts.token();
                if (token.id() == PHPTokenId.PHP_CURLY_CLOSE) {
                    curlyBalance--;
                } else if (token.id() == PHPTokenId.PHP_CURLY_OPEN) {
                    curlyBalance++;
                    checkAlternativeSyntax = false;
                } else if (token.id() == startTokenId) {
                    checkAlternativeSyntax = true;
                } else if (token.id() == PHPTokenId.PHP_TOKEN
                        && ":".equals(token.text().toString())
                        && checkAlternativeSyntax) {
                    balance++;
                    checkAlternativeSyntax = false;
                } else if (token.id() == endTokenId) {
                    balance--;
                }
            } while (ts.moveNext() && curlyBalance > -1 && balance > -1);
            return balance > 0;

        }
        return curlyBalance > 0;
    }

    @Override
    public int beforeBreak(Document document, int offset, JTextComponent target)
            throws BadLocationException {
        isAfter = false;

        final Caret caret = target.getCaret();
        final BaseDocument doc = (BaseDocument) document;

        boolean insertMatching = isInsertMatchingEnabled(doc);

        int lineBegin = Utilities.getRowStart(doc, offset);
        int lineEnd = Utilities.getRowEnd(doc, offset);

        if (lineBegin == offset && lineEnd == offset) {
            // Pressed return on a blank newline - do nothing
            return -1;
        }

        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);

        if (ts == null) {
            return -1;
        }
        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return -1;
        }

        Token<? extends PHPTokenId> token = ts.token();
        TokenId id = token.id();
        int tokenOffsetOnCaret = ts.offset();

        // Insert an end statement? Insert a } marker?
        int[] startOfContext = new int[1];
        PHPTokenId completeIn = insertMatching ? findContextForEnd(ts, offset, startOfContext) : null;
        boolean insert = completeIn != null && isEndMissing(doc, offset, completeIn);

        if (insert) {


            int indent = IndentUtils.lineIndent(doc, IndentUtils.lineStartOffset(document, startOfContext[0]));

            int afterLastNonWhite = Utilities.getRowLastNonWhite(doc, offset);

            // We've either encountered a further indented line, or a line that doesn't
            // look like the end we're after, so insert a matching end.
            StringBuilder sb = new StringBuilder();
            if (offset > afterLastNonWhite || id == PHPTokenId.PHP_CLOSETAG
                    || (offset < afterLastNonWhite && "?>".equals(doc.getText(afterLastNonWhite - 1, 2)))) {
                // don't put php close tag iside. see #167816
                sb.append("\n"); //NOI18N
                sb.append(IndentUtils.createIndentString(doc, countIndent(doc, offset, indent)));
            } else {
                // I'm inserting a newline in the middle of a sentence, such as the scenario in #118656
                // I should insert the end AFTER the text on the line
                String restOfLine = doc.getText(offset, Utilities.getRowEnd(doc, afterLastNonWhite) - offset);
                sb.append(restOfLine);
                sb.append("\n"); //NOI18N
                sb.append(IndentUtils.createIndentString(doc, countIndent(doc, offset, indent)));
                doc.remove(offset, restOfLine.length());
            }

            if (id == PHPTokenId.PHP_CLOSETAG && offset > tokenOffsetOnCaret) {
                token = LexUtilities.findPreviousToken(ts, Arrays.asList(PHPTokenId.PHP_OPENTAG));
                String begin = token != null ? token.text().toString() : "<?php"; //NOI18N
                sb.append(begin);
                sb.append(" "); // NOI18N
            }
            if (completeIn == PHPTokenId.PHP_CURLY_OPEN || completeIn == PHPTokenId.PHP_CLASS || completeIn == PHPTokenId.PHP_FUNCTION) {
                sb.append("}"); // NOI18N
            } else if (completeIn == PHPTokenId.PHP_IF || completeIn == PHPTokenId.PHP_ELSE || completeIn == PHPTokenId.PHP_ELSEIF) {
                sb.append("endif;"); // NOI18N
            } else if (completeIn == PHPTokenId.PHP_FOR) {
                sb.append("endfor;"); // NOI18N
            } else if (completeIn == PHPTokenId.PHP_FOREACH) {
                sb.append("endforeach;"); // NOI18N
            } else if (completeIn == PHPTokenId.PHP_WHILE) {
                sb.append("endwhile;"); // NOI18N
            } else if (completeIn == PHPTokenId.PHP_SWITCH) {
                sb.append("endswitch;"); // NOI18N
            }

            if (id == PHPTokenId.PHP_CLOSETAG && offset > tokenOffsetOnCaret) {
                sb.append(" ?>");  //NOI18N
            }

            if (id == PHPTokenId.PHP_CLOSETAG) {
                // place the close tag on the new line.
                sb.append("\n"); //NOI18N
            }
            int insertOffset = offset;
            doc.insertString(insertOffset, sb.toString(), null);
            caret.setDot(insertOffset);

            return -1;
        }

        if ((id == PHPTokenId.PHP_CURLY_CLOSE || LexUtilities.textEquals(token.text(), ']') || LexUtilities.textEquals(token.text(), ')')) // NOI18N
                /*&& (Utilities.getRowLastNonWhite(doc, offset) == offset)*/) {
            int indent = GsfUtilities.getLineIndent(doc, offset);
            StringBuilder sb = new StringBuilder();
            // the new line will not be added, if we are in middle of array declaration
            if ((LexUtilities.textEquals(token.text(), ')') || LexUtilities.textEquals(token.text(), ']')) && ts.movePrevious()) {
                Token<? extends PHPTokenId> helpToken = LexUtilities.findPrevious(ts, Arrays.asList(
                        PHPTokenId.WHITESPACE,
                        PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                        PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                        PHPTokenId.PHP_LINE_COMMENT));
                if (helpToken.id() == PHPTokenId.PHP_TOKEN
                        && (helpToken.text().charAt(0) == ',' || helpToken.text().charAt(0) == '(' || helpToken.text().charAt(0) == '[') && ts.movePrevious()) {
                    // only in array declaration we will add new line
                    if (helpToken.text().charAt(0) == '[') {
                        sb.append("\n"); // NOI18N
                    } else {
                        helpToken = LexUtilities.findPrevious(ts, Arrays.asList(
                                PHPTokenId.WHITESPACE,
                                PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                                PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                                PHPTokenId.PHP_LINE_COMMENT));
                        if (helpToken.id() == PHPTokenId.PHP_ARRAY || (helpToken.id() == PHPTokenId.PHP_TOKEN && helpToken.text().charAt(0) == '[')) { //NOI18N
                            sb.append("\n"); // NOI18N
                        }
                    }
                }
            } else {
                sb.append("\n"); // NOI18N
            }

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
                    if (id == PHPTokenId.PHP_LINE_COMMENT
                            || id == PHPTokenId.PHPDOC_COMMENT_START
                            || id == PHPTokenId.PHP_COMMENT_START) {
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
                int prevBegin = Utilities.getRowFirstNonWhite(doc, rowStart - 1);
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
                if (ts.offset() + token.length() > offset + 1) {
                    // See if the remaining text is just whitespace
                    String trailing = doc.getText(offset, Utilities.getRowEnd(doc, offset) - offset);
                    if (trailing.trim().length() != 0 && !trailing.startsWith("//")) { //NOI18N
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
                    int nextLine = Utilities.getRowEnd(doc, offset) + 1;
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
                int afterHash = begin + 2;
                String line = doc.getText(afterHash, Utilities.getRowEnd(doc, afterHash) - afterHash);
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
                    int sp = Utilities.getRowStart(doc, offset) + sb.length();
                    doc.insertString(insertOffset, sb.toString(), null);
                    caret.setDot(sp);
                    return sp;
                }
                doc.insertString(insertOffset, sb.toString(), null);
                caret.setDot(insertOffset);
                return insertOffset + sb.length() + 1;
            }
        }

        if (id == PHPTokenId.PHPDOC_COMMENT || (id == PHPTokenId.PHPDOC_COMMENT_START && offset > ts.offset()) || id == PHPTokenId.PHPDOC_COMMENT_END) {
            final Object[] ret = beforeBreakInComments(doc, ts, offset, caret,
                    PHPTokenId.PHPDOC_COMMENT_START, PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END);
            boolean isEmptyComment = (Boolean) ret[1];

            if (isEmptyComment) {
                final int indent = GsfUtilities.getLineIndent(doc, ts.offset());

                //XXX: workaround for issue #133210:
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        final long currentTimeMillis = System.currentTimeMillis();
                        try {
                            ParserManager.parse(Collections.<Source>singleton(Source.create(doc)), new UserTask() {
                                @Override
                                public void run(ResultIterator resultIterator) throws Exception {
                                    if (System.currentTimeMillis() - currentTimeMillis < 1500) {
                                        GeneratingBracketCompleter.generateDocTags(doc, (Integer) ret[0], indent);
                                        caret.setDot((Integer) ret[0]);
                                    }
                                }
                            });
                        } catch (ParseException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });


            }

            return (Integer) ret[0];
        }

        if (id == PHPTokenId.PHP_COMMENT || id == PHPTokenId.PHP_COMMENT_START || id == PHPTokenId.PHP_COMMENT_END) {
            if (!(id == PHPTokenId.PHP_COMMENT_START && offset == ts.offset())) {
                Object[] ret = beforeBreakInComments(doc, ts, offset, caret,
                        PHPTokenId.PHP_COMMENT_START, PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END);
                return (Integer) ret[0];
            }
        }

        return -1;
    }

    private static Object[] beforeBreakInComments(
            BaseDocument doc, TokenSequence<? extends PHPTokenId> ts, int offset, Caret caret,
            PHPTokenId commentStart, PHPTokenId commentBody, PHPTokenId commentEnd) throws BadLocationException {
        PHPTokenId id = ts.token().id();

        if (id == commentBody || id == commentStart) {
            int insertOffset;


            if (id == commentStart) {
                insertOffset = ts.offset() + ts.token().length();
            } else {
                insertOffset = offset;
            }

            // hofix for #174165
            if (insertOffset > doc.getLength()) {
                insertOffset = doc.getLength();
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
                String restOfLine = doc.getText(insertOffset, Utilities.getRowEnd(doc, afterLastNonWhite) - insertOffset);
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

            return new Object[]{newCaretOffset, addClosingTag};
        }

        if (id == commentEnd) {
            int insertOffset = ts.offset();

            // find comment start
            if (ts.movePrevious()) {
                assert ts.token().id() == commentBody
                        || ts.token().id() == commentStart : "PHP_COMMENT_END should not be preceeded by " + ts.token().id().name(); //NOI18N
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

            return new Object[]{newCaretOffset, false};
        }

        return new Object[]{-1, false};
    }

    // XXX: stolen from JavaKit.JavaInsertBreakAction, we should extend it to support heredoc
    private static boolean isClosedComment(CharSequence txt, int pos) {
        int length = txt.length();
        int quotation = 0;
        int simpleQuotation = 0;
        for (int i = pos; i < length; i++) {
            char c = txt.charAt(i);
            if (c == '*' && i < length - 1 && txt.charAt(i + 1) == '/') {
                if (quotation == 0 && simpleQuotation == 0 && i < length - 2) {
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
                    } else if (cc == '\'' && j < length - 1) {
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
                simpleQuotation = 0;
            } else if (c == '"' && i < length - 1 && txt.charAt(i + 1) != '\'') {
                quotation = ++quotation % 2;
            } else if (c == '\'' && i < length - 1) {
                simpleQuotation = ++simpleQuotation % 2;
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
     * Determine if an "end" or "}" is missing following the caret offset. The
     * logic used is to check the text on the current line for block initiators
     * (e.g. "def", "for", "{" etc.) and then see if a corresponding close is
     * found after the same indentation level.
     *
     * @param doc The document to be checked
     * @param offset The offset of the current line
     * @param skipJunk If false, only consider the current line (of the offset)
     * as the possible "block opener"; if true, look backwards across empty
     * lines and comment lines as well.
     * @param insertEndResult Null, or a boolean 1-element array whose first
     * element will be set to true iff this method determines that "end" should
     * be inserted
     * @param insertRBraceResult Null, or a boolean 1-element array whose first
     * element will be set to true iff this method determines that "}" should be
     * inserted
     * @param startOffsetResult Null, or an integer 1-element array whose first
     * element will be set to the starting offset of the opening block.
     * @param indentResult Null, or an integer 1-element array whose first
     * element will be set to the indentation level "end" or "}" should be
     * indented to when inserted.
     * @return true if something is missing; insertEndResult, insertRBraceResult
     * and identResult will provide the more specific return values in their
     * first elements.
     */
    static boolean isEndMissing(BaseDocument doc, int offset, boolean skipJunk,
            boolean[] insertEndResult, boolean[] insertRBraceResult, int[] startOffsetResult,
            int[] indentResult, PHPTokenId insertingEnd) throws BadLocationException {

        if (startOffsetResult != null) {
            startOffsetResult[0] = Utilities.getRowFirstNonWhite(doc, offset);
        }

        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);

        if (ts == null) {
            return false;
        }

        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<? extends PHPTokenId> token = ts.token();
        int balance = 1;
        boolean EOF = false;
        if (insertingEnd == PHPTokenId.PHP_CURLY_CLOSE) {
            while ((token.id() == PHPTokenId.PHP_CURLY_OPEN
                    || token.id() == PHPTokenId.PHP_CURLY_CLOSE
                    || token.id() == PHPTokenId.WHITESPACE)
                    && !EOF) {
                if (token.id() == PHPTokenId.PHP_CURLY_OPEN) {
                    balance++;
                } else if (token.id() == PHPTokenId.PHP_CURLY_CLOSE) {
                    balance--;
                }
                if (ts.moveNext()) {
                    token = ts.token();
                } else {
                    EOF = true;
                }
            }

            if (EOF) {
                if (balance == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBracket(final char ch) {
        return isOpeningBracket(ch) || isClosingBracket(ch);
    }

    private boolean isOpeningBracket(final char ch) {
        return ch == '(' || ch == '{' || ch == '[';
    }

    private boolean isClosingBracket(final char ch) {
        return ch == ')' || ch == '}' || ch == ']';
    }

    private boolean isQuote(final char ch) {
        return ch == '"' || ch == '\'';
    }

    private boolean isQuote(final Token<? extends PHPTokenId> token) {
        return isQuote(token.text().charAt(0));
    }

    private boolean doNotAutoComplete(final BaseDocument baseDocument, final char ch) {
        return (!isInsertMatchingEnabled(baseDocument) && isBracket(ch)) || (isQuote(ch) && !OptionsUtils.autoCompletionSmartQuotes());
    }

    @Override
    public boolean beforeCharInserted(Document document, int caretOffset, JTextComponent target, char ch)
            throws BadLocationException {
        isAfter = false;
        Caret caret = target.getCaret();
        BaseDocument doc = (BaseDocument) document;

        if (doNotAutoComplete(doc, ch)) {
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
                    doc.remove(start, end - start);
                }
                // Fall through to do normal insert matching work
            } else if (ch == '"' || ch == '\'' || ch == '(' || ch == '{' || ch == '[') {
                // Bracket the selection
                String selection = target.getSelectedText();
                if (selection != null && selection.length() > 0) {
                    char firstChar = selection.charAt(0);
                    if (firstChar != ch) {
                        int start = target.getSelectionStart();
                        int end = target.getSelectionEnd();
                        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPositionedSequence(doc, start);
                        if (ts != null && (!isStringToken(ts.token()) || firstChar == '\"' || firstChar == '\'')) {
                            int lastChar = selection.charAt(selection.length() - 1);
                            // Replace the surround-with chars?
                            if (selection.length() > 1
                                    && ((firstChar == '"' || firstChar == '\'' || firstChar == '('
                                    || firstChar == '{' || firstChar == '[')
                                    && lastChar == matching(firstChar))) {
                                doc.remove(end - 1, 1);
                                doc.insertString(end - 1, Character.toString(matching(ch)), null);
                                doc.remove(start, 1);
                                doc.insertString(start, Character.toString(ch), null);
                                target.getCaret().setDot(end);
                            } else if (selection.length() == 1 && (firstChar == '"' || firstChar == '\'')) {
                                if (ts.token().id() == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING) {
                                    String original = ts.token().text().toString();
                                    if (original.length() > 1) {
                                        start = ts.offset();
                                        end = ts.offset() + ts.token().length();
                                        doc.remove(start, end - start);
                                        doc.insertString(start, ch + original.substring(1, original.length() - 1) + ch, null);
                                        target.getCaret().setDot(caretOffset);
                                    } else {
                                        return false;
                                    }
                                }
                            } else {
                                doc.remove(start, end - start);
                                doc.insertString(start, ch + selection + matching(ch), null);
                                target.getCaret().setDot(start + selection.length() + 2);
                            }

                            return true;
                        }
                    }
                }
            }
        }

        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        ts.move(caretOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<? extends PHPTokenId> token = ts.token();
        TokenId id = token.id();
        TokenId[] stringTokens = null;

        if (id == PHPTokenId.PHP_LINE_COMMENT && target.getSelectionStart() != -1) {
            if (ch == '*' || ch == '+' || ch == '_') {
                // See if it's a comment and if so surround the text with an rdoc modifier
                // such as bold, teletype or italics
                String selection = target.getSelectedText();
                // Don't allow any spaces - you can't bracket multiple words in rdoc I think (TODO - check that)
                if (selection != null && selection.length() > 0 && selection.charAt(0) != ch && selection.indexOf(' ') == -1) {
                    int start = target.getSelectionStart();
                    doc.remove(start, target.getSelectionEnd() - start);
                    doc.insertString(start, ch + selection + matching(ch), null);
                    target.getCaret().setDot(start + selection.length() + 2);

                    return true;
                }
            }
        }

        // "/" is handled AFTER the character has been inserted since we need the lexer's help
        if (ch == '\"') {
            stringTokens = STRING_TOKENS;
        } else if (ch == '\'') {
            stringTokens = STRING_TOKENS;
        }

        if (stringTokens != null) {
            boolean inserted = completeQuote(doc, caretOffset, ch);

            if (inserted) {
                caret.setDot(caretOffset + 1);
                return true;
            } else {
                caret.setDot(caretOffset);
                return false;
            }
        }

        return false;
    }

    /**
     * A hook method called after a character was inserted into the document.
     * The function checks for special characters for completion ()[]'"{} and
     * other conditions and optionally performs changes to the doc and or caret
     * (complets braces, moves caret, etc.)
     *
     * @param document the document where the change occurred
     * @param dotPos position of the character insertion
     * @param target The target
     * @param ch the character that was inserted
     * @return Whether the insert was handled
     * @throws BadLocationException if dotPos is not correct
     */
    @Override
    public boolean afterCharInserted(Document document, int dotPos, JTextComponent target, char ch)
            throws BadLocationException {
        isAfter = true;
        Caret caret = target.getCaret();
        BaseDocument doc = (BaseDocument) document;

        // See if our automatic adjustment of indentation when typing (for example) "end" was
        // premature - if you were typing a longer word beginning with one of my adjustment
        // prefixes, such as "endian", then put the indentation back.
        if (previousAdjustmentOffset != -1) {
            if (dotPos == previousAdjustmentOffset) {
                // Revert indentation iff the character at the insert position does
                // not start a new token (e.g. the previous token that we reindented
                // was not complete)
                TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, dotPos);

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
                Token<? extends PHPTokenId> token = LexUtilities.getToken(doc, dotPos);
                if (token == null) {
                    return true;
                }
                TokenId id = token.id();

                if (((id == PHPTokenId.PHP_VARIABLE) && (token.length() == 1))
                        || (LexUtilities.textEquals(token.text(), '[')) || (LexUtilities.textEquals(token.text(), ']'))
                        || (LexUtilities.textEquals(token.text(), '(')) || (LexUtilities.textEquals(token.text(), ')'))) {
                    if (ch == ']' || ch == ')') { // || ch == '}'
                        skipClosingBracket(doc, caret, ch);
                    } else if ((ch == '[') || (ch == '(')) {//  || (ch == '{')
                        completeOpeningBracket(doc, dotPos, caret, ch);
                    }
                } else if (id == PHPTokenId.PHP_CASTING && ch == ')') {
                    skipClosingBracket(doc, caret, ch);
                }


                // Reindent blocks (won't do anything if } is not at the beginning of a line
                if (ch == '}') {
                    reindent(doc, dotPos, PHPTokenId.PHP_CURLY_CLOSE, caret);
                } else if (ch == '{') {
                    reindent(doc, dotPos, PHPTokenId.PHP_CURLY_OPEN, caret);
                }
            }
            break;
            default:
            //no-op
        }

        return true;
    }

    private void reindent(BaseDocument doc, int offset, TokenId id, Caret caret)
            throws BadLocationException {
        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);

        if (ts != null) {
            ts.move(offset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }

            Token<? extends PHPTokenId> token = ts.token();

            if ((token.id() == id)) {
                final int rowFirstNonWhite = Utilities.getRowFirstNonWhite(doc, offset);
                // Ensure that this token is at the beginning of the line
                if (ts.offset() > rowFirstNonWhite) {
                    return;
                }

                OffsetRange begin;

                if (id == PHPTokenId.PHP_CURLY_OPEN && ts.offset() == rowFirstNonWhite
                        && ts.movePrevious()) {
                    // The curly is at the first nonwhite char at the line.
                    // Do we need to indent the { according previous line?
                    int previousExprestion = PHPNewLineIndenter.findStartTokenOfExpression(ts);
                    int previousIndent = Utilities.getRowIndent(doc, previousExprestion);
                    int currentIndent = Utilities.getRowIndent(doc, offset);
                    int newIndent = countIndent(doc, offset, previousIndent);
                    if (newIndent != currentIndent) {
                        GsfUtilities.setLineIndentation(doc, offset, Math.max(newIndent, 0));
                        return;
                    }
                }

                if (id == PHPTokenId.PHP_CURLY_CLOSE) {
                    begin = LexUtilities.findBwd(doc, ts, PHPTokenId.PHP_CURLY_OPEN, '{', PHPTokenId.PHP_CURLY_CLOSE, '}');
                } else {
                    begin = OffsetRange.NONE;
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

    /**
     * Replaced by PHPBracesMatcher
     */
    @Override
    public OffsetRange findMatching(Document document, int offset /*, boolean simpleSearch*/) {
        return OffsetRange.NONE;
    }

    /**
     * Hook called after a character *ch* was backspace-deleted from *doc*. The
     * function possibly removes bracket or quote pair if appropriate.
     *
     * @param doc the document
     * @param dotPos position of the change
     * @param caret caret
     * @param ch the character that was deleted
     */
    @SuppressWarnings("fallthrough")
    @Override
    public boolean charBackspaced(Document document, int dotPos, JTextComponent target, char ch)
            throws BadLocationException {
        BaseDocument doc = (BaseDocument) document;

        switch (ch) {
            case ' ': {
                // Backspacing over "# " ? Delete the "#" too!
                TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, dotPos);
                if (ts != null) {
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
                if (((tokenAtDot == ']')
                        && (LexUtilities.getTokenBalance(doc, '[', ']', dotPos) != 0))
                        || ((tokenAtDot == ')')
                        && (LexUtilities.getTokenBalance(doc, '(', ')', dotPos) != 0))
                        || ((tokenAtDot == '}')
                        && (LexUtilities.getTokenBalance(doc, '{', '}', dotPos) != 0))) {
                    doc.remove(dotPos, 1);
                }
                break;
            }
            case '\"':
            case '\'': {
                char[] match = doc.getChars(dotPos, 1);
                if ((match != null) && (match[0] == ch)) {
                    doc.remove(dotPos, 1);
                }
                break;
            }
            default:
            //no-op
        }
        return true;
    }

    /**
     * A hook to be called after closing bracket ) or ] was inserted into the
     * document. The method checks if the bracket should stay there or be
     * removed and some exisitng bracket just skipped.
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
     * Check whether the typed bracket should stay in the document or be
     * removed. <br> This method is called by
     * <code>skipClosingBracket()</code>.
     *
     * @param doc document into which typing was done.
     * @param caretOffset
     */
    private boolean isSkipClosingBracket(BaseDocument doc, int caretOffset, char bracket)
            throws BadLocationException {
        if (caretOffset == doc.getLength()) {
            return false;
        }
        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, caretOffset);
        if (ts == null) {
            return false;
        }
        ts.move(caretOffset);
        if (!ts.moveNext()) {
            return false;
        }
        Token<? extends PHPTokenId> token = ts.token();
        boolean skipClosingBracket = false;
        // Check whether character follows the bracket is the same bracket
        if ((token != null) && (LexUtilities.textEquals(token.text(), bracket))) {
            char leftBracket = bracket == ')' ? '(' : (bracket == ']' ? '[' : '{');
            int bracketBalanceWithNewBracket = 0;
            ts.moveStart();
            if (!ts.moveNext()) {
                return false;
            }
            token = ts.token();
            while (token != null) {
                if ((LexUtilities.textEquals(token.text(), '(')) || (LexUtilities.textEquals(token.text(), '['))) {
                    if (LexUtilities.textEquals(token.text(), leftBracket)) {
                        bracketBalanceWithNewBracket++;
                    }
                } else if ((LexUtilities.textEquals(token.text(), ')')) || (LexUtilities.textEquals(token.text(), ']'))) {
                    if (LexUtilities.textEquals(token.text(), bracket)) {
                        bracketBalanceWithNewBracket--;
                    }
                }
                if (!ts.moveNext()) {
                    break;
                }
                token = ts.token();
            }
            if (bracketBalanceWithNewBracket == 0) {
                skipClosingBracket = false;
            } else {
                skipClosingBracket = true;
            }
        }

        return skipClosingBracket;
    }

    /**
     * Check for various conditions and possibly add a pairing bracket to the
     * already inserted.
     *
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
     * Check for conditions and possibly complete an already inserted quote .
     *
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
        Object[] result = findPhpSectionBoundaries(doc, dotPos, true);
        if (result == null) {
            // not in PHP section
            return false;
        }

        @SuppressWarnings("unchecked")
        TokenSequence<? extends PHPTokenId> ts = (TokenSequence<? extends PHPTokenId>) result[0];
        int sectionEnd = (Integer) result[2];
        boolean onlyWhitespaceFollows = (Boolean) result[4];

        Token<? extends PHPTokenId> token = ts.token();

        if (token == null) { // Issue #151886
            return false;
        }

        Token<? extends PHPTokenId> previousToken = ts.movePrevious() ? ts.token() : null;

        // Check if we are inside a comment
        if (token.id() == PHPTokenId.PHP_COMMENT
                || token.id() == PHPTokenId.PHP_LINE_COMMENT
                || token.id() == PHPTokenId.PHPDOC_COMMENT
                || token.id() == PHPTokenId.T_INLINE_HTML // #132981
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
                        if (!(dotPos < doc.getLength() - 1 && doc.getText(dotPos + 1, 1).charAt(0) == bracket)) {
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
                    insert = (chr == ')' || chr == ',' || chr == '+' || chr == '}' || //NOI18N
                            chr == ';' || chr == ']' || chr == '.') && !isStringToken(previousToken) && !isQuote(token); //NOI18N
                }
            }

            if (insert) {
                doc.insertString(dotPos, "" + bracket + (isAfter ? "" : matching(bracket)), null); //NOI18N
                return true;
            }
        }

        return false;
    }

    private static Object[] findPhpSectionBoundaries(BaseDocument doc, int offset, boolean currentLineOnly) {
        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
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
        return new Object[]{ts, sectionStart, sectionEnd, onlyWhitespacePreceeds, onlyWhitespaceFollows};
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
     * Checks whether dotPos is a position at which bracket and quote completion
     * is performed. Brackets and quotes are not completed everywhere but just
     * at suitable places .
     *
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

            return ((chr == ')') || (chr == ',') || (chr == '\"') || (chr == '\'') || (chr == ' ')
                    || (chr == ']') || (chr == '}') || (chr == '\n') || (chr == '\t') || (chr == ';'));
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

    /**
     * This method count new indent ofr braces and parent
     *
     * @param doc
     * @param offset - the original offset, where is cursor
     * @param currentIndent - the indnet that should be modified
     * @param previousIndent - indent of the line abot
     * @return
     */
    private int countIndent(BaseDocument doc, int offset, int previousIndent) {
        int value = previousIndent;
        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
        if (ts != null) {
            ts.move(offset);
            if (!ts.moveNext() || !ts.moveNext()) {
                return previousIndent;
            }
            Token<? extends PHPTokenId> token = ts.token();
            while (token.id() != PHPTokenId.PHP_CURLY_OPEN
                    && token.id() != PHPTokenId.PHP_SEMICOLON
                    && !(token.id() == PHPTokenId.PHP_TOKEN
                    && ("(".equals(token.text())
                    || "[".equals(token.text())))
                    && ts.movePrevious()) {
                token = ts.token();
            }
            if (token.id() == PHPTokenId.PHP_CURLY_OPEN) {
                while (token.id() != PHPTokenId.PHP_CLASS
                        && token.id() != PHPTokenId.PHP_FUNCTION
                        && token.id() != PHPTokenId.PHP_IF
                        && token.id() != PHPTokenId.PHP_ELSE
                        && token.id() != PHPTokenId.PHP_ELSEIF
                        && token.id() != PHPTokenId.PHP_FOR
                        && token.id() != PHPTokenId.PHP_FOREACH
                        && token.id() != PHPTokenId.PHP_WHILE
                        && token.id() != PHPTokenId.PHP_DO
                        && token.id() != PHPTokenId.PHP_SWITCH
                        && ts.movePrevious()) {
                    token = ts.token();
                }
                CodeStyle codeStyle = CodeStyle.get(doc);
                CodeStyle.BracePlacement bracePlacement = codeStyle.getOtherBracePlacement();
                if (token.id() == PHPTokenId.PHP_CLASS) {
                    bracePlacement = codeStyle.getClassDeclBracePlacement();
                } else if (token.id() == PHPTokenId.PHP_FUNCTION) {
                    bracePlacement = codeStyle.getMethodDeclBracePlacement();
                } else if (token.id() == PHPTokenId.PHP_IF || token.id() == PHPTokenId.PHP_ELSE || token.id() == PHPTokenId.PHP_ELSEIF) {
                    bracePlacement = codeStyle.getIfBracePlacement();
                } else if (token.id() == PHPTokenId.PHP_FOR || token.id() == PHPTokenId.PHP_FOREACH) {
                    bracePlacement = codeStyle.getForBracePlacement();
                } else if (token.id() == PHPTokenId.PHP_WHILE || token.id() == PHPTokenId.PHP_DO) {
                    bracePlacement = codeStyle.getWhileBracePlacement();
                } else if (token.id() == PHPTokenId.PHP_SWITCH) {
                    bracePlacement = codeStyle.getSwitchBracePlacement();
                }
                value = bracePlacement == CodeStyle.BracePlacement.NEW_LINE_INDENTED ? previousIndent + codeStyle.getIndentSize() : previousIndent;
            }
        }
        return value;
    }

    @Override
    public List<OffsetRange> findLogicalRanges(ParserResult info, final int caretOffset) {
        final Set<OffsetRange> ranges = new LinkedHashSet<OffsetRange>();
        final DefaultVisitor pathVisitor = new DefaultVisitor() {
            @Override
            public void scan(ASTNode node) {
                if (node != null && node.getStartOffset() <= caretOffset && caretOffset <= node.getEndOffset()) {
                    ranges.add(new OffsetRange(node.getStartOffset(), node.getEndOffset()));
                    super.scan(node);
                }
            }
        };
        if (info instanceof PHPParseResult) {
            pathVisitor.scan(((PHPParseResult) info).getProgram());
        }
        final ArrayList<OffsetRange> retval = new ArrayList<OffsetRange>(ranges);
        Collections.reverse(retval);
        return retval;
    }

    // UGH - this method has gotten really ugly after successive refinements based on unit tests - consider cleaning up
    @Override
    public int getNextWordOffset(Document document, int offset, boolean reverse) {
        BaseDocument doc = (BaseDocument) document;
        doc.readLock();
        try {
            TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
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
                    int start = ts.offset() + token.length();
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

            if (id == PHPTokenId.PHP_VARIABLE || id == PHPTokenId.PHP_STRING) {
                String s = token.text().toString();
                int length = s.length();
                int wordOffset = offset - ts.offset();
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
                                        return ts.offset() + j + 1;
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
                    int start = wordOffset + 1;
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
                                return ts.offset() + i;
                            }
                            start++;
                        }
                    }
                    for (int i = start; i < length; i++) {
                        char charAtI = s.charAt(i);
                        if (charAtI == '_' || Character.isUpperCase(charAtI)) {
                            return ts.offset() + i;
                        }
                    }
                }
            }
        } finally {
            doc.readUnlock();
        }

        // Default handling in the IDE
        return -1;
    }
}
