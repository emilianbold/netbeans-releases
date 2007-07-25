/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.util.Exceptions;


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
 *
 * @author Tor Norbye
 */
public class BracketCompleter implements org.netbeans.api.gsf.BracketCompletion {
    /** When true, continue comments if you press return in a line comment (that does not
     * also have code on the same line 
     */
    //static final boolean CONTINUE_COMMENTS = !Boolean.getBoolean("ruby.no.cont.comment"); // NOI18N
    static final boolean CONTINUE_COMMENTS = Boolean.getBoolean("ruby.cont.comment"); // NOI18N
    
    /** Tokens which indicate that we're within a literal string */
    private final static TokenId[] STRING_TOKENS = // XXX What about RubyTokenId.STRING_BEGIN or QUOTED_STRING_BEGIN?
        {
            RubyTokenId.STRING_LITERAL, RubyTokenId.QUOTED_STRING_LITERAL, RubyTokenId.CHAR_LITERAL,
            RubyTokenId.STRING_END, RubyTokenId.QUOTED_STRING_END
        };

    /** Tokens which indicate that we're within a regexp string */
    // XXX What about RubyTokenId.REGEXP_BEGIN?
    private static final TokenId[] REGEXP_TOKENS = { RubyTokenId.REGEXP_LITERAL, RubyTokenId.REGEXP_END };

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
        return true; // TODO - look up
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
            TokenSequence<?extends GsfTokenId> lineTs = LexUtilities.getRubyTokenSequence(doc, offset);
            if (lineTs != null) {
                lineTs.move(lineBegin);
                StringBuilder sb = new StringBuilder();
                while (lineTs.moveNext() && lineTs.offset() <= lineEnd) {
                    Token<?extends GsfTokenId> token = lineTs.token();
                    TokenId id = token.id();
                    
                    if (id == RubyTokenId.STRING_BEGIN) {
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

                            TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, offset);
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
        
        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, offset);

        if (ts == null) {
            return -1;
        }

        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return -1;
        }

        Token<?extends GsfTokenId> token = ts.token();
        TokenId id = token.id();

        // Is it an umatched =begin token?
        if ((id == RubyTokenId.ERROR) && (ts.offset() == (offset - 6)) &&
                token.text().toString().startsWith("=begin") && insertMatching) {
            doc.insertString(offset, "\n=end", null);
            caret.setDot(offset);

            return -1;
        }

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

            // We've either encountered a further indented line, or a line that doesn't
            // look like the end we're after, so insert a matching end.
            StringBuilder sb = new StringBuilder();
            sb.append("\n"); // XXX On Windows, do \r\n?
            LexUtilities.indent(sb, indent);

            if (insertEnd) {
                sb.append("end"); // NOI18N
            } else {
                assert insertRBrace;
                sb.append("}"); // NOI18N
            }

            int insertOffset = offset; // offset < length ? offset+1 : offset;
            doc.insertString(insertOffset, sb.toString(), null);
            caret.setDot(insertOffset);
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
        if ((id == RubyTokenId.RBRACE || id == RubyTokenId.RBRACKET) && (Utilities.getRowLastNonWhite(doc, offset) == offset)) {
            int indent = LexUtilities.getLineIndent(doc, offset);
            StringBuilder sb = new StringBuilder();
            // XXX On Windows, do \r\n?
            sb.append("\n"); // NOI18N
            LexUtilities.indent(sb, indent);

            int insertOffset = offset; // offset < length ? offset+1 : offset;
            doc.insertString(insertOffset, sb.toString(), null);
            caret.setDot(insertOffset);
        }
        
        if (CONTINUE_COMMENTS && id == RubyTokenId.LINE_COMMENT) {
            // Only do this if the line only contains comments
            int begin = Utilities.getRowFirstNonWhite(doc, offset);
            token = LexUtilities.getToken(doc, begin);

            if (token.id() == RubyTokenId.LINE_COMMENT) {
                // Line comments should continue
                int indent = LexUtilities.getLineIndent(doc, offset);
                StringBuilder sb = new StringBuilder();
                LexUtilities.indent(sb, indent);
                sb.append("#"); // NOI18N
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

        int beginEndBalance = LexUtilities.getBeginEndLineBalance(doc, offset);
        int braceBalance =
            LexUtilities.getLineBalance(doc, offset, RubyTokenId.LBRACE, RubyTokenId.RBRACE);

        if ((beginEndBalance == 1) || (braceBalance == 1)) {
            // There is one more opening token on the line than a corresponding
            // closing token.  (If there's is more than one we don't try to help.)
            int indent = LexUtilities.getLineIndent(doc, offset);

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

                int nextIndent = LexUtilities.getLineIndent(doc, next);

                if (nextIndent > indent) {
                    insertEnd = false;
                    insertRBrace = false;
                } else if (nextIndent == indent) {
                    if (insertEnd) {
                        if (LexUtilities.getBeginEndLineBalance(doc, next) < 0) {
                            insertEnd = false;
                        } else {
                            // See if I have a structure word like "else", "ensure", etc.
                            // (These are indent words that are not also begin words)
                            // and if so refrain from inserting the end
                            int lineBegin = Utilities.getRowFirstNonWhite(doc, next);

                            Token<?extends GsfTokenId> token =
                                LexUtilities.getToken(doc, lineBegin);

                            if ((token != null) && LexUtilities.isIndentToken(token.id()) &&
                                    !LexUtilities.isBeginToken(token.id(), doc, lineBegin)) {
                                insertEnd = false;
                            }
                        }
                    } else if (insertRBrace &&
                            (LexUtilities.getLineBalance(doc, next, RubyTokenId.LBRACE,
                                RubyTokenId.RBRACE) < 0)) {
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
            if (ch == '"' || ch == '\'' || ch == '(' || ch == '{' || ch == '[') {
                // Bracket the selection
                String selection = target.getSelectedText();
                if (selection != null && selection.length() > 0 && selection.charAt(0) != ch) {
                    int start = target.getSelectionStart();
                    doc.remove(start, target.getSelectionEnd()-start);
                    doc.insertString(start, ch + selection + matching(ch), null);
                    target.getCaret().setDot(start+selection.length()+2);
                
                    return true;
                }
            }
        }

        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        ts.move(caretOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<?extends GsfTokenId> token = ts.token();
        TokenId id = token.id();
        TokenId[] stringTokens = null;
        TokenId beginTokenId = null;
        
        if (id == RubyTokenId.LINE_COMMENT && target.getSelectionStart() != -1) {
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
            beginTokenId = RubyTokenId.QUOTED_STRING_BEGIN;
        } else if (ch == '\'') {
            stringTokens = STRING_TOKENS;
            beginTokenId = RubyTokenId.STRING_BEGIN;
        } else if (id == RubyTokenId.ERROR) {
            String text = token.text().toString();

            if (text.equals("%")) {
                // Depending on the character we're going to continue
                if (!Character.isLetter(ch)) { // %q, %x, etc. Only %[], %!!, %<space> etc. is allowed
                    stringTokens = STRING_TOKENS;
                    beginTokenId = RubyTokenId.QUOTED_STRING_BEGIN;
                }
            } else if ((text.length() == 2) && (text.charAt(0) == '%') &&
                    Character.isLetter(text.charAt(1))) {
                char c = text.charAt(1);

                switch (c) {
                case 'q':
                    stringTokens = STRING_TOKENS;
                    beginTokenId = RubyTokenId.STRING_BEGIN;

                    break;

                case 'Q':
                    stringTokens = STRING_TOKENS;
                    beginTokenId = RubyTokenId.QUOTED_STRING_BEGIN;

                    break;

                case 'r':
                    stringTokens = REGEXP_TOKENS;
                    beginTokenId = RubyTokenId.REGEXP_BEGIN;

                    break;

                default:
                    // ?
                    stringTokens = STRING_TOKENS;
                    beginTokenId = RubyTokenId.QUOTED_STRING_BEGIN;
                }
            } else {
                ts.movePrevious();

                TokenId prevId = ts.token().id();

                if ((prevId == RubyTokenId.STRING_BEGIN) ||
                        (prevId == RubyTokenId.QUOTED_STRING_BEGIN)) {
                    stringTokens = STRING_TOKENS;
                    beginTokenId = prevId;
                } else if (prevId == RubyTokenId.REGEXP_BEGIN) {
                    stringTokens = REGEXP_TOKENS;
                    beginTokenId = RubyTokenId.REGEXP_BEGIN;
                }
            }
        } else if (((((id == RubyTokenId.STRING_BEGIN) || (id == RubyTokenId.QUOTED_STRING_BEGIN)) &&
                (caretOffset == (ts.offset() + 1))))) {
            if (!Character.isLetter(ch)) { // %q, %x, etc. Only %[], %!!, %<space> etc. is allowed
                stringTokens = STRING_TOKENS;
                beginTokenId = id;
            }
        } else if (((id == RubyTokenId.STRING_BEGIN) && (caretOffset == (ts.offset() + 2))) ||
                (id == RubyTokenId.STRING_END)) {
            stringTokens = STRING_TOKENS;
            beginTokenId = RubyTokenId.STRING_BEGIN;
        } else if (((id == RubyTokenId.QUOTED_STRING_BEGIN) && (caretOffset == (ts.offset() + 2))) ||
                (id == RubyTokenId.QUOTED_STRING_END)) {
            stringTokens = STRING_TOKENS;
            beginTokenId = RubyTokenId.QUOTED_STRING_BEGIN;
        } else if (((id == RubyTokenId.REGEXP_BEGIN) && (caretOffset == (ts.offset() + 2))) ||
                (id == RubyTokenId.REGEXP_END)) {
            stringTokens = REGEXP_TOKENS;
            beginTokenId = RubyTokenId.REGEXP_BEGIN;
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
    //    TokenSequence< ?extends GsfTokenId> ts = LexUtilities.getTokenSequence(doc);
    //
    //    System.out.println("Dumping tokens for dot=" + dot);
    //    int prevOffset = -1;
    //    if (ts != null) {
    //        ts.moveFirst();
    //        int index = 0;
    //        do {
    //            Token<? extends GsfTokenId> token = ts.token();
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

        // See if our automatic adjustment of indentation when typing (for example) "end" was
        // premature - if you were typing a longer word beginning with one of my adjustment
        // prefixes, such as "endian", then put the indentation back.
        if (previousAdjustmentOffset != -1) {
            if (dotPos == previousAdjustmentOffset) {
                // Revert indentation iff the character at the insert position does
                // not start a new token (e.g. the previous token that we reindented
                // was not complete)
                TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, dotPos);

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
        case '#': {
            // Automatically insert #{^} when typing "#" in a quoted string
            Token<?extends GsfTokenId> token = LexUtilities.getToken(doc, dotPos);
            TokenId id = token.id();

            if (id == RubyTokenId.QUOTED_STRING_LITERAL) {
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
            Token<?extends GsfTokenId> token = LexUtilities.getToken(doc, dotPos);
            TokenId id = token.id();

            if (id == RubyTokenId.IDENTIFIER) {
                int length = token.length();

                if ((length == 2) && "[]".equals(token.text().toString())) { // Special case
                    skipClosingBracket(doc, caret, ch, RubyTokenId.RBRACKET);

                    return true;
                }
            }

            if (((id == RubyTokenId.IDENTIFIER) && (token.length() == 1)) ||
                    (id == RubyTokenId.LBRACKET) || (id == RubyTokenId.RBRACKET) ||
                    (id == RubyTokenId.LBRACE) || (id == RubyTokenId.RBRACE) ||
                    (id == RubyTokenId.LPAREN) || (id == RubyTokenId.RPAREN)) {
                if (ch == ']') {
                    skipClosingBracket(doc, caret, ch, RubyTokenId.RBRACKET);
                } else if (ch == ')') {
                    skipClosingBracket(doc, caret, ch, RubyTokenId.RPAREN);
                } else if (ch == '}') {
                    skipClosingBracket(doc, caret, ch, RubyTokenId.RBRACE);
                } else if ((ch == '[') || (ch == '(') || (ch == '{')) {
                    completeOpeningBracket(doc, dotPos, caret, ch);
                }
            }

            // Reindent blocks (won't do anything if } is not at the beginning of a line
            if (ch == '}') {
                reindent(doc, dotPos, RubyTokenId.RBRACE, caret);
            } else if (ch == ']') {
                reindent(doc, dotPos, RubyTokenId.RBRACKET, caret);
            }
        }

        break;

        case 'd':
            // See if it's the end of an "end" - if so, reindent
            reindent(doc, dotPos, RubyTokenId.END, caret);

            break;

        case 'e':
            // See if it's the end of an "else" or an "ensure" - if so, reindent
            reindent(doc, dotPos, RubyTokenId.ELSE, caret);
            reindent(doc, dotPos, RubyTokenId.ENSURE, caret);
            reindent(doc, dotPos, RubyTokenId.RESCUE, caret);

            break;

        case 'f':
            // See if it's the end of an "else" - if so, reindent
            reindent(doc, dotPos, RubyTokenId.ELSIF, caret);

            break;

        case 'n':
            // See if it's the end of an "when" - if so, reindent
            reindent(doc, dotPos, RubyTokenId.WHEN, caret);
            
            break;
            
        case '/': {
            // Bracket matching for regular expressions has to be done AFTER the
            // character is inserted into the document such that I can use the lexer
            // to determine whether it's a division (e.g. x/y) or a regular expression (/foo/)
            Token<?extends GsfTokenId> token = LexUtilities.getToken(doc, dotPos);
            TokenId id = token.id();

            if (id == RubyTokenId.REGEXP_BEGIN || id == RubyTokenId.REGEXP_END) {
                TokenId[] stringTokens = REGEXP_TOKENS;
                TokenId beginTokenId = RubyTokenId.REGEXP_BEGIN;

                boolean inserted =
                    completeQuote(doc, dotPos, caret, ch, stringTokens, beginTokenId);

                if (inserted) {
                    caret.setDot(dotPos + 1);
                }
                
                return inserted;
            }
        }
        }

        return true;
    }

    private void reindent(BaseDocument doc, int offset, TokenId id, Caret caret)
        throws BadLocationException {
        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, offset);

        if (ts != null) {
            ts.move(offset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }

            Token<?extends GsfTokenId> token = ts.token();

            if ((token.id() == id)) {
                // Ensure that this token is at the beginning of the line
                if (ts.offset() > Utilities.getRowFirstNonWhite(doc, offset)) {
                    return;
                }

                OffsetRange begin;

                if (id == RubyTokenId.RBRACE) {
                    begin = LexUtilities.findBwd(doc, ts, RubyTokenId.LBRACE, RubyTokenId.RBRACE);
                } else if (id == RubyTokenId.RBRACKET) {
                    begin = LexUtilities.findBwd(doc, ts, RubyTokenId.LBRACKET, RubyTokenId.RBRACKET);
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

        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, offset);

        if (ts != null) {
            ts.move(offset);

            if (!ts.moveNext()) {
                return OffsetRange.NONE;
            }

            Token<?extends GsfTokenId> token = ts.token();

            if (token == null) {
                return OffsetRange.NONE;
            }

            TokenId id = token.id();

            if (id == RubyTokenId.WHITESPACE) {
                // ts.move(offset) gives the token to the left of the caret.
                // If you have the caret right at the beginning of a token, try
                // the token to the right too - this means that if you have
                //  "   |def" it will show the matching "end" for the "def".
                ts.move(offset + 1);

                if (ts.moveNext() && (ts.offset() <= (offset + 1))) {
                    token = ts.token();
                    id = token.id();
                }
            }

            if (id == RubyTokenId.QUOTED_STRING_BEGIN) {
                // Heredocs should be treated specially
                if (token.text().toString().startsWith("<<")) {
                    return LexUtilities.findHeredocEnd(ts, token);
                }
                return LexUtilities.findFwd(doc, ts, RubyTokenId.QUOTED_STRING_BEGIN,
                    RubyTokenId.QUOTED_STRING_END);
            } else if (id == RubyTokenId.QUOTED_STRING_END) {
                String s = token.text().toString();
                if (!"\"".equals(s) && !"\'".equals(s) && !")".equals(s)) {
                    OffsetRange r = LexUtilities.findHeredocBegin(ts, token);
                    if (r != OffsetRange.NONE) {
                        return r;
                    }
                }
                return LexUtilities.findBwd(doc, ts, RubyTokenId.QUOTED_STRING_BEGIN,
                    RubyTokenId.QUOTED_STRING_END);
            } else if (id == RubyTokenId.STRING_BEGIN) {
                // Heredocs should be treated specially
                if (token.text().toString().startsWith("<<")) {
                    return LexUtilities.findHeredocEnd(ts, token);
                }
                return LexUtilities.findFwd(doc, ts, RubyTokenId.STRING_BEGIN, RubyTokenId.STRING_END);
            } else if (id == RubyTokenId.STRING_END) {
                String s = token.text().toString();
                if (!"\"".equals(s) && !"\'".equals(s) && !")".equals(s)) {
                    OffsetRange r = LexUtilities.findHeredocBegin(ts, token);
                    if (r != OffsetRange.NONE) {
                        return r;
                    }
                }
                return LexUtilities.findBwd(doc, ts, RubyTokenId.STRING_BEGIN, RubyTokenId.STRING_END);
            } else if (id == RubyTokenId.REGEXP_BEGIN) {
                return LexUtilities.findFwd(doc, ts, RubyTokenId.REGEXP_BEGIN, RubyTokenId.REGEXP_END);
            } else if (id == RubyTokenId.REGEXP_END) {
                return LexUtilities.findBwd(doc, ts, RubyTokenId.REGEXP_BEGIN, RubyTokenId.REGEXP_END);
            } else if (id == RubyTokenId.LPAREN) {
                return LexUtilities.findFwd(doc, ts, RubyTokenId.LPAREN, RubyTokenId.RPAREN);
            } else if (id == RubyTokenId.RPAREN) {
                return LexUtilities.findBwd(doc, ts, RubyTokenId.LPAREN, RubyTokenId.RPAREN);
            } else if (id == RubyTokenId.LBRACE) {
                return LexUtilities.findFwd(doc, ts, RubyTokenId.LBRACE, RubyTokenId.RBRACE);
            } else if (id == RubyTokenId.RBRACE) {
                return LexUtilities.findBwd(doc, ts, RubyTokenId.LBRACE, RubyTokenId.RBRACE);
            } else if (id == RubyTokenId.LBRACKET) {
                return LexUtilities.findFwd(doc, ts, RubyTokenId.LBRACKET, RubyTokenId.RBRACKET);
            } else if (id == RubyTokenId.DO && !LexUtilities.isEndmatchingDo(doc, ts.offset())) {
                // No matching dot for "do" used in conditionals etc.
                return OffsetRange.NONE;
            } else if (id == RubyTokenId.RBRACKET) {
                return LexUtilities.findBwd(doc, ts, RubyTokenId.LBRACKET, RubyTokenId.RBRACKET);
            } else if (id.primaryCategory().equals("keyword")) {
                if (LexUtilities.isBeginToken(id, doc, ts)) {
                    return LexUtilities.findEnd(doc, ts);
                } else if ((id == RubyTokenId.END) || LexUtilities.isIndentToken(id)) { // Find matching block

                    return LexUtilities.findBegin(doc, ts);
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
    public boolean charBackspaced(Document document, int dotPos, JTextComponent target, char ch)
        throws BadLocationException {
        BaseDocument doc = (BaseDocument)document;
        
        // Backspacing over "# " ? Delete the "#" too!
        if (/*CONTINUE_COMMENTS && */ ch == ' ') {
            Token token = LexUtilities.getToken(doc, dotPos);
            TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, dotPos);
            ts.move(dotPos);
            if (ts.movePrevious() && ts.offset() == dotPos-1 && ts.token().id() == RubyTokenId.LINE_COMMENT) {
                doc.remove(dotPos-1, 1);
                
                return true;
            }
        }

        if ((ch == '(') || (ch == '[')) {
            char tokenAtDot = LexUtilities.getTokenChar(doc, dotPos);

            if (((tokenAtDot == ']') &&
                    (LexUtilities.getTokenBalance(doc, RubyTokenId.LBRACKET, RubyTokenId.RBRACKET, dotPos) != 0)) ||
                    ((tokenAtDot == ')') &&
                    (LexUtilities.getTokenBalance(doc, RubyTokenId.LPAREN, RubyTokenId.RPAREN, dotPos) != 0))) {
                doc.remove(dotPos, 1);
            }
        } else if (ch == '\"') {
            char[] match = doc.getChars(dotPos, 1);

            if ((match != null) && (match[0] == '\"')) {
                doc.remove(dotPos, 1);
            }
        } else if (ch == '\'') {
            char[] match = doc.getChars(dotPos, 1);

            if ((match != null) && (match[0] == '\'')) {
                doc.remove(dotPos, 1);
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

        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        // XXX BEGIN TOR MODIFICATIONS
        //ts.move(caretOffset+1);
        ts.move(caretOffset);

        if (!ts.moveNext()) {
            return false;
        }

        Token<?extends GsfTokenId> token = ts.token();

        // Check whether character follows the bracket is the same bracket
        if ((token != null) && (token.id() == bracketId)) {
            int bracketIntId = bracketId.ordinal();
            int leftBracketIntId =
                (bracketIntId == RubyTokenId.RPAREN.ordinal()) ? RubyTokenId.LPAREN.ordinal()
                                                               : RubyTokenId.LBRACKET.ordinal();

            // Skip all the brackets of the same type that follow the last one
            ts.moveNext();

            Token<?extends GsfTokenId> nextToken = ts.token();

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
            Token<?extends GsfTokenId> lastRBracket = token;
            ts.movePrevious();
            token = ts.token();

            boolean finished = false;

            while (!finished && (token != null)) {
                int tokenIntId = token.id().ordinal();

                if ((token.id() == RubyTokenId.LPAREN) || (token.id() == RubyTokenId.LBRACKET)) {
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
                } else if ((token.id() == RubyTokenId.RPAREN) ||
                        (token.id() == RubyTokenId.RBRACKET)) {
                    if (tokenIntId == bracketIntId) {
                        bracketBalance--;
                    }
                } else if (token.id() == RubyTokenId.LBRACE) {
                    braceBalance++;

                    if (braceBalance > 0) { // stop on extra left brace
                        finished = true;
                    }
                } else if (token.id() == RubyTokenId.RBRACE) {
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
                    if ((token.id() == RubyTokenId.LPAREN) || (token.id() == RubyTokenId.LBRACKET)) {
                        if (token.id().ordinal() == leftBracketIntId) {
                            bracketBalance++;
                        }
                    } else if ((token.id() == RubyTokenId.RPAREN) ||
                            (token.id() == RubyTokenId.RBRACKET)) {
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
                    } else if (token.id() == RubyTokenId.LBRACE) {
                        braceBalance++;
                    } else if (token.id() == RubyTokenId.RBRACE) {
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
    private boolean completeQuote(BaseDocument doc, int dotPos, Caret caret, char bracket,
        TokenId[] stringTokens, TokenId beginToken) throws BadLocationException {
        if (isEscapeSequence(doc, dotPos)) { // \" or \' typed

            return false;
        }

        // Examine token at the caret offset
        if (doc.getLength() < dotPos) {
            return false;
        }

        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, dotPos);

        if (ts == null) {
            return false;
        }

        ts.move(dotPos);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<?extends GsfTokenId> token = ts.token();
        Token<?extends GsfTokenId> previousToken = null;

        if (ts.movePrevious()) {
            previousToken = ts.token();
        }

        int lastNonWhite = Utilities.getRowLastNonWhite(doc, dotPos);

        // eol - true if the caret is at the end of line (ignoring whitespaces)
        boolean eol = lastNonWhite < dotPos;

        if ((token.id() == RubyTokenId.BLOCK_COMMENT) || (token.id() == RubyTokenId.LINE_COMMENT) ||
                (token.id() == RubyTokenId.DOCUMENTATION)) { // 105419
            return false;
        } else if ((token.id() == RubyTokenId.WHITESPACE) && eol && ((dotPos - 1) > 0)) {
            // check if the caret is at the very end of the line comment
            token = LexUtilities.getToken(doc, dotPos - 1);

            if (token.id() == RubyTokenId.LINE_COMMENT) {
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

        if ((id == RubyTokenId.ERROR) && (previousToken != null) &&
                (previousToken.id() == beginToken)) {
            insideString = true;
        }

        if (!insideString) {
            // check if the caret is at the very end of the line and there
            // is an unterminated string literal
            if ((token.id() == RubyTokenId.WHITESPACE) && eol) {
                if ((dotPos - 1) > 0) {
                    token = LexUtilities.getToken(doc, dotPos - 1);
                    // XXX TODO use language embedding to handle this
                    insideString = (token.id() == RubyTokenId.STRING_LITERAL) ||
                        (token.id() == RubyTokenId.CHAR_LITERAL);
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
        Node root = AstUtilities.getRoot(info);

        if (root == null) {
            return Collections.emptyList();
        }

        AstPath path = new AstPath(root, caretOffset);
        List<OffsetRange> ranges = new ArrayList<OffsetRange>();

        // Check if the caret is within a comment, and if so insert a new
        // leaf "node" which contains the comment line and then comment block
        try {
            BaseDocument doc = (BaseDocument)info.getDocument();
            Token<?extends GsfTokenId> token = LexUtilities.getToken(doc, caretOffset);

            if ((token != null) && (token.id() == RubyTokenId.LINE_COMMENT)) {
                // First add a range for the current line
                int begin = Utilities.getRowStart(doc, caretOffset);
                int end = Utilities.getRowEnd(doc, caretOffset);

                if (LexUtilities.isCommentOnlyLine(doc, caretOffset)) {
                    ranges.add(new OffsetRange(begin, end));

                    int lineBegin = begin;
                    int lineEnd = end;

                    while (true) {
                        int newBegin = Utilities.getRowStart(doc, begin - 1);

                        if ((newBegin <= 0) || !LexUtilities.isCommentOnlyLine(doc, newBegin)) {
                            break;
                        }

                        begin = newBegin;
                    }

                    int length = doc.getLength();

                    while (true) {
                        int newEnd = Utilities.getRowEnd(doc, end + 1);

                        if ((newEnd >= length) || !LexUtilities.isCommentOnlyLine(doc, newEnd)) {
                            break;
                        }

                        end = newEnd;
                    }

                    if ((lineBegin > begin) || (lineEnd < end)) {
                        ranges.add(new OffsetRange(begin, end));
                    }
                } else {
                    // It's just a line comment next to some code; select the comment
                    TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
                    int offset = token.offset(th);
                    ranges.add(new OffsetRange(offset, offset + token.length()));
                }
            } else if (token != null && token.id() == GsfTokenId.DOCUMENTATION) {
                // Select the whole token block
                TokenHierarchy<BaseDocument> th = TokenHierarchy.get(doc);
                int begin = token.offset(th);
                int end = begin + token.length();
                ranges.add(new OffsetRange(begin, end));
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        Iterator<Node> it = path.leafToRoot();

        while (it.hasNext()) {
            Node node = it.next();

            // Filter out some uninteresting nodes
            if (node instanceof NewlineNode) {
                continue;
            }

            OffsetRange range = AstUtilities.getRange(node);

            // The contains check should be unnecessary, but I end up getting
            // some weird positions for some JRuby AST nodes
            if (range.containsInclusive(caretOffset)) {
                ranges.add(range);
            }
        }

        return ranges;
    }

    public int getNextWordOffset(Document document, int offset, boolean reverse) {
        BaseDocument doc = (BaseDocument)document;
        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, offset);
        if (ts == null) {
            return -1;
        }
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return -1;
        }

        Token<? extends GsfTokenId> token = ts.token();
        TokenId id = token.id();

        while (id == RubyTokenId.WHITESPACE) {
            if (reverse && !ts.movePrevious()) {
                return -1;
            } else if (!reverse && !ts.moveNext()) {
                return -1;
            }

            token = ts.token();
            id = token.id();

            if (reverse) {
                offset = ts.offset();
            } else {
                offset = ts.offset()+token.length();
            }
        }

        if (id == RubyTokenId.IDENTIFIER || id == RubyTokenId.TYPE_SYMBOL ||
                id == RubyTokenId.CONSTANT ||
                id == RubyTokenId.GLOBAL_VAR || id == RubyTokenId.INSTANCE_VAR) {
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
                        if (charAtI == '_' || !Character.isUpperCase(charAtI)) {
                            // return offset of previous uppercase char in the identifier
                            return ts.offset() + i + 1;
                        }
                    }
                    return ts.offset();
                } else {
                    for (int i = offsetInImage - 1; i >= 0; i--) {
                        char charAtI = s.charAt(i);
                        if (charAtI == '_') {
                            return ts.offset() + i;
                        }
                        if (Character.isUpperCase(charAtI)) {
                            // now skip over previous uppercase chars in the identifier
                            for (int j = i; j >= 0; j--) {
                                char charAtJ = s.charAt(j);
                                if (charAtJ == '_') {
                                    return ts.offset() + j;
                                }
                                if (!Character.isUpperCase(charAtJ)) {
                                    // return offset of previous uppercase char in the identifier
                                    return ts.offset() + j + 1;
                                }
                            }
                            return ts.offset();
                        }
                    }
                }
            } else {
                // Find next
                int start = wordOffset+1;
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
