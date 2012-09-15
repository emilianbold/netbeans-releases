/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditorOptions;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 *
 * @author Petr Hejl
 */
public class JsTypedTextInterceptor implements TypedTextInterceptor {

    /** Tokens which indicate that we're within a regexp string */
    // XXX What about JsTokenId.REGEXP_BEGIN?
    private static final TokenId[] REGEXP_TOKENS = { JsTokenId.REGEXP, JsTokenId.REGEXP_END };

    /** Tokens which indicate that we're within a literal string */
    private final static TokenId[] STRING_TOKENS = { JsTokenId.STRING, JsTokenId.STRING_END };

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

    @Override
    public void afterInsert(final Context context) throws BadLocationException {
        isAfter = true;
        BaseDocument doc = (BaseDocument) context.getDocument();
        doc.runAtomicAsUser(new Runnable() {

            @Override
            public void run() {

            }
        });

        int dotPos = context.getOffset();
        Caret caret = context.getComponent().getCaret();
        char ch = context.getText().charAt(0);

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
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, dotPos);

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
            Token<? extends JsTokenId> token = LexUtilities.getToken(doc, dotPos);
            if (token == null) {
                return;
            }
            TokenId id = token.id();

//            if (id == JsTokenId.ANY_OPERATOR) {
//                int length = token.length();
//                String s = token.text().toString();
//                if ((length == 2) && "[]".equals(s) || "[]=".equals(s)) { // Special case
//                    skipClosingBracket(doc, caret, ch, JsTokenId.BRACKET_RIGHT_BRACKET);
//
//                    return;
//                }
//            }

            if (((id == JsTokenId.IDENTIFIER) && (token.length() == 1)) ||
                    (id == JsTokenId.BRACKET_LEFT_BRACKET) || (id == JsTokenId.BRACKET_RIGHT_BRACKET) ||
                    (id == JsTokenId.BRACKET_LEFT_CURLY) || (id == JsTokenId.BRACKET_RIGHT_CURLY) ||
                    (id == JsTokenId.BRACKET_LEFT_PAREN) || (id == JsTokenId.BRACKET_RIGHT_PAREN)) {
                if (ch == ']') {
                    skipClosingBracket(doc, caret, ch, JsTokenId.BRACKET_RIGHT_BRACKET);
                } else if (ch == ')') {
                    skipClosingBracket(doc, caret, ch, JsTokenId.BRACKET_RIGHT_PAREN);
                } else if (ch == '}') {
                    skipClosingBracket(doc, caret, ch, JsTokenId.BRACKET_RIGHT_CURLY);
                } else if ((ch == '[') || (ch == '(') || (ch == '{')) {
                    completeOpeningBracket(doc, dotPos, caret, ch);
                }
            }

            // Reindent blocks (won't do anything if } is not at the beginning of a line
            if (ch == '}') {
                reindent(doc, dotPos, JsTokenId.BRACKET_RIGHT_CURLY, caret);
            } else if (ch == ']') {
                reindent(doc, dotPos, JsTokenId.BRACKET_RIGHT_BRACKET, caret);
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
            // Bracket matching for regular expressions has to be done AFTER the
            // character is inserted into the document such that I can use the lexer
            // to determine whether it's a division (e.g. x/y) or a regular expression (/foo/)
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsPositionedSequence(doc, dotPos);
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
                        return;
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

                    return;
                }
            }
            break;
        }
        }
    }

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        isAfter = false;
        JTextComponent target = context.getComponent();
        Caret caret = target.getCaret();
        int caretOffset = context.getOffset();
        char ch = context.getText().charAt(0);
        BaseDocument doc = (BaseDocument) context.getDocument();

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
                        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsPositionedSequence(doc, start);
                        if (ts != null
                                && ts.token().id() != JsTokenId.LINE_COMMENT
                                && ts.token().id() != JsTokenId.DOC_COMMENT
                                && ts.token().id() != JsTokenId.BLOCK_COMMENT // not inside comments
                                && ts.token().id() != JsTokenId.STRING) { // not inside strings!
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

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        ts.move(caretOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<? extends JsTokenId> token = ts.token();
        JsTokenId id = token.id();
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
        } else if (id.isError()) {
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

    @Override
    public void insert(MutableContext context) throws BadLocationException {
    }

    @Override
    public void cancelled(Context context) {
    }

    private void reindent(BaseDocument doc, int offset, TokenId id, Caret caret)
        throws BadLocationException {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, offset);

        if (ts != null) {
            ts.move(offset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }

            Token<? extends JsTokenId> token = ts.token();

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

                if (id == JsTokenId.BRACKET_RIGHT_CURLY) {
                    begin = LexUtilities.findBwd(doc, ts, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY);
                } else if (id == JsTokenId.BRACKET_RIGHT_BRACKET) {
                    begin = LexUtilities.findBwd(doc, ts, JsTokenId.BRACKET_LEFT_BRACKET, JsTokenId.BRACKET_RIGHT_BRACKET);
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

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, dotPos);

        if (ts == null) {
            return false;
        }

        ts.move(dotPos);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<? extends JsTokenId> token = ts.token();
        Token<? extends JsTokenId> previousToken = null;

        if (ts.movePrevious()) {
            previousToken = ts.token();
        }

        int lastNonWhite = Utilities.getRowLastNonWhite(doc, dotPos);

        // eol - true if the caret is at the end of line (ignoring whitespaces)
        boolean eol = lastNonWhite < dotPos;

        if ((token.id() == JsTokenId.BLOCK_COMMENT)
                || (token.id() == JsTokenId.DOC_COMMENT)
                || (token.id() == JsTokenId.LINE_COMMENT)
                || (previousToken != null && previousToken.id() == JsTokenId.LINE_COMMENT && token.id() == JsTokenId.EOL)) {
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
        JsTokenId id = token.id();

        for (TokenId currId : stringTokens) {
            if (id == currId) {
                insideString = true;
                break;
            }
        }

        if (id.isError() && (previousToken != null)
                && (previousToken.id() == beginToken)) {
            insideString = true;
        }

        if (id == JsTokenId.EOL && previousToken != null) {
            if (previousToken.id() == beginToken) {
                insideString = true;
            } else if (previousToken.id().isError()) {
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
                    insideString = (token.id() == JsTokenId.STRING);
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

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        // XXX BEGIN TOR MODIFICATIONS
        //ts.move(caretOffset+1);
        ts.move(caretOffset);

        if (!ts.moveNext()) {
            return false;
        }

        Token<? extends JsTokenId> token = ts.token();

        // Check whether character follows the bracket is the same bracket
        if ((token != null) && (token.id() == bracketId)) {
            int bracketIntId = bracketId.ordinal();
            int leftBracketIntId =
                (bracketIntId == JsTokenId.BRACKET_RIGHT_PAREN.ordinal()) ? JsTokenId.BRACKET_LEFT_PAREN.ordinal()
                                                               : JsTokenId.BRACKET_LEFT_BRACKET.ordinal();

            // Skip all the brackets of the same type that follow the last one
            ts.moveNext();

            Token<? extends JsTokenId> nextToken = ts.token();
            boolean endOfJs = false;
            while ((nextToken != null) && (nextToken.id() == bracketId)) {
                token = nextToken;

                if (!ts.moveNext()) {
                    endOfJs = true;
                    break;
                }

                nextToken = ts.token();
            }

            // token var points to the last bracket in a group of two or more right brackets
            // Attempt to find the left matching bracket for it
            // Search would stop on an extra opening left brace if found
            int braceBalance = 0; // balance of '{' and '}'
            int bracketBalance = 0; // balance of the brackets or parenthesis
            Token<? extends JsTokenId> lastRBracket = token;
            if (!endOfJs) {
                // move on the las bracket || parent
                ts.movePrevious();
            }
            token = ts.token();

            boolean finished = false;

            while (!finished && (token != null)) {
                int tokenIntId = token.id().ordinal();

                if ((token.id() == JsTokenId.BRACKET_LEFT_PAREN) || (token.id() == JsTokenId.BRACKET_LEFT_BRACKET)) {
                    if (tokenIntId == leftBracketIntId) {
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
                } else if ((token.id() == JsTokenId.BRACKET_RIGHT_PAREN) ||
                        (token.id() == JsTokenId.BRACKET_RIGHT_BRACKET)) {
                    if (tokenIntId == bracketIntId) {
                        bracketBalance--;
                    }
                } else if (token.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                    braceBalance++;

                    if (braceBalance > 0) { // stop on extra left brace
                        finished = true;
                    }
                } else if (token.id() == JsTokenId.BRACKET_RIGHT_CURLY) {
                    braceBalance--;
                }

                if (!ts.movePrevious()) {
                    break;
                }

                token = ts.token();
            }

            if (bracketBalance != 0
                    || (bracketId ==  JsTokenId.BRACKET_RIGHT_CURLY && braceBalance < 0)) { // not found matching bracket
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
                    if ((token.id() == JsTokenId.BRACKET_LEFT_PAREN) || (token.id() == JsTokenId.BRACKET_LEFT_BRACKET)) {
                        if (token.id().ordinal() == leftBracketIntId) {
                            bracketBalance++;
                        }
                    } else if ((token.id() == JsTokenId.BRACKET_RIGHT_PAREN) ||
                            (token.id() == JsTokenId.BRACKET_RIGHT_BRACKET)) {
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
                    } else if (token.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                        braceBalance++;
                    } else if (token.id() == JsTokenId.BRACKET_RIGHT_CURLY) {
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
                //skipClosingBracket = (bracketBalance == 0);
            }
        }

        return skipClosingBracket;
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

    @MimeRegistration(mimeType = JsTokenId.JAVASCRIPT_MIME_TYPE, service = TypedTextInterceptor.Factory.class)
    public static class Factory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(org.netbeans.api.editor.mimelookup.MimePath mimePath) {
            return new JsTypedTextInterceptor();
        }

    }
}
