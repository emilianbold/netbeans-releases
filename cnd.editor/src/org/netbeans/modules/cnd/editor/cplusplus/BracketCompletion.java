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

import java.util.Stack;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndAbstractTokenProcessor;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenProcessor;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.utils.MIMENames;

/**
 * This static class groups the whole aspect of bracket
 * completion. It is defined to clearly separate the functionality
 * and keep actions clean.
 * The methods of the class are called from different actions as
 * KeyTyped, DeletePreviousChar.
 */
public class BracketCompletion {

    /**
     * A hook method called after a character was inserted into the
     * document. The function checks for special characters for
     * completion ()[]'"{} and other conditions and optionally performs
     * changes to the doc and or caret (complets braces, moves caret,
     * etc.)
     * @param doc the document where the change occurred
     * @param dotPos position of the character insertion
     * @param caret caret
     * @param ch the character that was inserted
     * @throws BadLocationException if dotPos is not correct
     */
    static void charInserted(BaseDocument doc,
            int dotPos,
            Caret caret,
            char ch) throws BadLocationException {
        if (!completionSettingEnabled()) {
            return;
        }
        TokenItem<CppTokenId> tokenAtDot = CndTokenUtilities.getToken(doc, dotPos, true);
        if (tokenAtDot == null) {
            return;
        }
        if (ch == ')' || ch == ']' || ch == '(' || ch == '[') {
            switch (tokenAtDot.id()) {
                case RBRACKET:
                case RPAREN:
                    skipClosingBracket(doc, caret, ch);
                    break;
                case LBRACKET:
                case LPAREN:
                    completeOpeningBracket(doc, dotPos, caret, ch);
                    break;
            }
        } else if (ch == '\"' || ch == '\'') {
            completeQuote(doc, dotPos, caret, ch);
        } else if (ch == ';') {
            moveSemicolon(doc, dotPos, caret);
        } else if (ch == '<') {
            if (tokenAtDot.id() == CppTokenId.PREPROCESSOR_SYS_INCLUDE &&
                    tokenAtDot.partType() == PartType.START) {
                completeOpeningBracket(doc, dotPos, caret, ch);
            }
        } else if (ch == '>') {
            if (tokenAtDot.id() == CppTokenId.PREPROCESSOR_SYS_INCLUDE) {
                char match[] = doc.getChars(dotPos + 1, 1);
                if (match != null && match[0] == '>') {
                    doc.remove(dotPos + 1, 1);
                }
            }
        } else if (ch == '.') {
            if (dotPos > 0) {
                tokenAtDot = CndTokenUtilities.getToken(doc, dotPos - 1, true);
                if (tokenAtDot.id() == CppTokenId.THIS) {
                    doc.remove(dotPos, 1);
                    doc.insertString(dotPos, "->", null);// NOI18N
                    caret.setDot(dotPos + 2);
                }
            }
        }
    }

    private static void moveSemicolon(BaseDocument doc, int dotPos, Caret caret) throws BadLocationException {
        int eolPos = Utilities.getRowEnd(doc, dotPos);
        TokenSequence<CppTokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, dotPos);
        if (cppTokenSequence == null || !cppTokenSequence.moveNext()) {
            return;
        }
        int lastParenPos = dotPos;
        while (cppTokenSequence.moveNext() && cppTokenSequence.offset() < eolPos) {
            Token<CppTokenId> token = cppTokenSequence.token();
            if (token.id() == CppTokenId.RPAREN) {
                lastParenPos = cppTokenSequence.offset();
            } else if (!CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory())) {
                return;
            }
        }
        if (posWithinAnyQuote(doc, dotPos) || isForLoopSemicolon(doc, dotPos)) {
            return;
        }
        // may be check offsets?
//        if (lastParenPos != dotPos) {
        doc.remove(dotPos, 1);
        doc.insertString(lastParenPos, ";", null); // NOI18N
        caret.setDot(lastParenPos + 1);
//        }
    }

    private static boolean isForLoopSemicolon(BaseDocument doc, int dotPos) {
        TokenSequence<CppTokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, dotPos);
        if (ts == null || !ts.moveNext() || ts.token().id() != CppTokenId.SEMICOLON) {
            return false;
        }
        int parDepth = 0; // parenthesis depth
        int braceDepth = 0; // brace depth
        boolean semicolonFound = false; // next semicolon
        while (ts.movePrevious()) {
            Token<CppTokenId> token = ts.token();
            if (token.id() == CppTokenId.LPAREN) {
                if (parDepth == 0) { // could be a 'for ('
                    while (ts.movePrevious()) {
                        token = ts.token();
                        String category = token.id().primaryCategory();
                        if (!CppTokenId.WHITESPACE_CATEGORY.equals(category) && !CppTokenId.COMMENT_CATEGORY.equals(category)) {
                            break;
                        }
                    }
                    if (token.id() == CppTokenId.FOR) {
                        return true;
                    }
                    return false;
                } else { // non-zero depth
                    parDepth--;
                }
            } else if (token.id() == CppTokenId.RPAREN) {
                parDepth++;
            } else if (token.id() == CppTokenId.LBRACE) {
                if (braceDepth == 0) { // unclosed left brace
                    return false;
                }
                braceDepth--;
            } else if (token.id() == CppTokenId.RBRACE) {
                braceDepth++;
            } else if (token.id() == CppTokenId.SEMICOLON) {
                if (semicolonFound) { // one semicolon already found
                    return false;
                }
                semicolonFound = true;
            }
        }
        return false;
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
    static void charBackspaced(BaseDocument doc,
            int dotPos,
            Caret caret,
            char ch) throws BadLocationException {
        if (completionSettingEnabled()) {
            if (ch == '(' || ch == '[') {
                TokenItem<CppTokenId> token = CndTokenUtilities.getToken(doc, dotPos, true);
                if ((token.id() == CppTokenId.RBRACKET && tokenBalance(doc, CppTokenId.LBRACKET, CppTokenId.RBRACKET) != 0)
                        || (token.id() == CppTokenId.RPAREN && tokenBalance(doc, CppTokenId.LPAREN, CppTokenId.RPAREN) != 0)) {
                    doc.remove(dotPos, 1);
                }
            } else if (ch == '\"') {
                char match[] = doc.getChars(dotPos, 1);
                if (match != null && match[0] == '\"') {
                    doc.remove(dotPos, 1);
                }
            } else if (ch == '\'') {
                char match[] = doc.getChars(dotPos, 1);
                if (match != null && match[0] == '\'') {
                    doc.remove(dotPos, 1);
                }
            } else if (ch == '<') {
                char match[] = doc.getChars(dotPos, 1);
                if (match != null && match[0] == '>' && dotPos > 0) {
                    TokenItem<CppTokenId> token = CndTokenUtilities.getFirstNonWhiteBwd(doc, dotPos - 1);
                    switch (token.id()) {
                        case PREPROCESSOR_INCLUDE:
                        case PREPROCESSOR_INCLUDE_NEXT:
                            doc.remove(dotPos, 1);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static TokenSequence<CppTokenId> getInnerTS(BaseDocument doc, int offset) {
        TokenSequence<?> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, offset);
        if (cppTokenSequence == null) {
            return null;
        }
        cppTokenSequence.move(offset);
        if (!cppTokenSequence.moveNext() && !cppTokenSequence.movePrevious()) {
            return null;
        }
        Token<CppTokenId> token = (Token<CppTokenId>) cppTokenSequence.token();
        if (token.id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
            cppTokenSequence = cppTokenSequence.embedded();
        }
        cppTokenSequence.move(offset);
        return (TokenSequence<CppTokenId>) cppTokenSequence;
    }

    /**
     * Resolve whether pairing right curly should be added automatically
     * at the caret position or not.
     * <br>
     * There must be only whitespace or line comment or block comment
     * between the caret position
     * and the left brace and the left brace must be on the same line
     * where the caret is located.
     * <br>
     * The caret must not be "contained" in the opened block comment token.
     *
     * @param doc document in which to operate.
     * @param caretOffset offset of the caret.
     * @return true if a right brace '}' should be added
     *  or false if not.
     */
    static boolean isAddRightBrace(BaseDocument doc, int caretOffset) throws BadLocationException {
        boolean addRightBrace = false;
        if (completionSettingEnabled()) {
            if (caretOffset > 0) {
                // Check whether line ends with '{' ignoring any whitespace
                // or comments
                int tokenOffset = caretOffset;
                TokenSequence<CppTokenId> cppTokenSequence = getInnerTS(doc, tokenOffset);
                if (cppTokenSequence == null) {
                    return false;
                }
                cppTokenSequence.move(tokenOffset);
                Token<CppTokenId> token = null;
                int tsOffset = doc.getLength();
                if (cppTokenSequence.moveNext()) {
                    token = cppTokenSequence.token();
                    tsOffset = cppTokenSequence.offset();
                }
                addRightBrace = true; // suppose that right brace should be added

                // Disable right brace adding if caret not positioned within whitespace
                // or line comment
                if ((tsOffset < caretOffset) && (token != null)
                    && (caretOffset < (tsOffset + token.length()))) {// caret contained in token
                    switch (token.id()) {
                        case WHITESPACE:
                        case LINE_COMMENT:
                        case ESCAPED_LINE:
                        case ESCAPED_WHITESPACE:
                            break;// the above tokens are OK
                        default:
                            // Disable brace adding for the remaining ones
                            addRightBrace = false;
                    }
                }

                if (addRightBrace) { // still candidate for adding
                    int caretRowStartOffset = Utilities.getRowStart(doc, caretOffset);

                    // Check whether there are only whitespace or comment tokens
                    // between caret and left brace and check only on the line
                    // with the caret
                    while (cppTokenSequence.movePrevious() && cppTokenSequence.offset() >= caretRowStartOffset) {
                        boolean ignore = false;
                        token = cppTokenSequence.token();
                        // Assuming java token context here
                        switch (token.id()) {
                            case WHITESPACE:
                            case BLOCK_COMMENT:
                            case LINE_COMMENT:
                                // skip
                                ignore = true;
                                break;
                        }

                        if (!ignore) { // break on the current token
                            break;
                        }
                    }

                    if (token == null || token.id() != CppTokenId.LBRACE // must be left brace
                            || cppTokenSequence.offset() < caretRowStartOffset // on the same line as caret
                            ) {
                        addRightBrace = false;
                    }

                }

                if (addRightBrace) { // Finally check the brace balance whether there are any missing right braces
                    addRightBrace = (braceBalance(doc) > 0);
                }
            }
        }
        return addRightBrace;
    }

    /** Return the position of the last command separator before
     * the given position.
     */
    static int getLastCommandSeparator(BaseDocument doc, int pos) throws BadLocationException {
        int stLine = Utilities.getRowFirstNonWhite(doc, pos);
        if (stLine != -1 && stLine < pos) {
            return stLine;
        }
        return pos;
    }
    /**
     * Returns position of the first unpaired closing paren/brace/bracket from the caretOffset
     * till the end of caret row. If there is no such element, position after the last non-white
     * character on the caret row is returned.
     */
    @SuppressWarnings("unchecked")
    static int getRowOrBlockEnd(BaseDocument doc, int caretOffset) throws BadLocationException {
        int rowEnd = Utilities.getRowLastNonWhite(doc, caretOffset);
        if (rowEnd == -1 || caretOffset >= rowEnd) {
            return caretOffset;
        }
        rowEnd += 1;
        int parenBalance = 0;
        int braceBalance = 0;
        int bracketBalance = 0;

        TokenSequence<CppTokenId> cppTokenSequence = getInnerTS(doc, caretOffset);
        if (cppTokenSequence == null) {
            return caretOffset;
        }
        while (cppTokenSequence.moveNext() && cppTokenSequence.offset() < rowEnd) {
            switch (cppTokenSequence.token().id()) {
                case LPAREN:
                    parenBalance++;
                    break;
                case RPAREN:
                    if (parenBalance-- == 0) {
                        return cppTokenSequence.offset();
                    }
                    break;
                case LBRACE:
                    braceBalance++;
                    break;
                case RBRACE:
                    if (braceBalance-- == 0) {
                        return cppTokenSequence.offset();
                    }
                    break;
                case LBRACKET:
                    bracketBalance++;
                    break;
                case RBRACKET:
                    if (bracketBalance-- == 0) {
                        return cppTokenSequence.offset();
                    }
                    break;
            }
        }
        return rowEnd;
    }

    /**
     * Counts the number of braces starting at dotPos to the end of the
     * document. Every occurence of { increses the count by 1, every
     * occurrence of } decreses the count by 1. The result is returned.
     * @return The number of { - number of } (>0 more { than } ,<0 more } than {)
     */
    private static int braceBalance(BaseDocument doc)
            throws BadLocationException {
        return tokenBalance(doc, CppTokenId.LBRACE, CppTokenId.RBRACE);
    }

    /**
     * The same as braceBalance but generalized to any pair of matching
     * tokens.
     * @param open the token that increses the count
     * @param close the token that decreses the count
     */
    private static int tokenBalance(BaseDocument doc, CppTokenId open, CppTokenId close)
            throws BadLocationException {
        BalanceTokenProcessor tp = new BalanceTokenProcessor(open, close);
        CndTokenUtilities.processTokens(tp, doc, 0, doc.getLength());
        return tp.getBalance();
    }

    /**
     * A hook to be called after closing bracket ) or ] was inserted into
     * the document. The method checks if the bracket should stay there
     * or be removed and some exisitng bracket just skipped.
     *
     * @param doc the document
     * @param dotPos position of the inserted bracket
     * @param caret caret
     * @param theBracket the bracket character ']' or ')'
     */
    private static void skipClosingBracket(BaseDocument doc, Caret caret, char theBracket)
            throws BadLocationException {
        CppTokenId bracketId = (theBracket == ')')
                ? CppTokenId.RPAREN
                : CppTokenId.RBRACKET;
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
    static boolean isSkipClosingBracket(BaseDocument doc, int caretOffset, CppTokenId bracketId)
            throws BadLocationException {
        // First check whether the caret is not after the last char in the document
        // because no bracket would follow then so it could not be skipped.
        if (caretOffset == doc.getLength()) {
            return false; // no skip in this case
        }

        boolean skipClosingBracket = false; // by default do not remove
        // Examine token at the caret offset
        TokenSequence<CppTokenId> ts = getInnerTS(doc, caretOffset);
        if (ts == null) {
            return false;
        }
        // Check whether character follows the bracket is the same bracket
        if (ts.moveNext() && ts.token().id() == bracketId) {
//            Token<CppTokenId> token = ts.token();
            CppTokenId leftBracketId = (ts.token().id() == CppTokenId.RPAREN) ? CppTokenId.LPAREN : CppTokenId.LBRACKET;

            // Skip all the brackets of the same type that follow the last one
            int lastRBracketIndex = ts.index();
            while (ts.moveNext() && ts.token().id() == bracketId) {
                lastRBracketIndex = ts.index();
            }
            // token var points to the last bracket in a group of two or more right brackets
            // Attempt to find the left matching bracket for it
            // Search would stop on an extra opening left brace if found
            int braceBalance = 0; // balance of '{' and '}'
            int bracketBalance = -1; // balance of the brackets or parenthesis
//            org.netbeans.editor.TokenItem lastRBracket = token;

//            token = token.getPrevious();
            boolean finished = false;
            while (!finished && ts.movePrevious()) {
                CppTokenId id = ts.token().id();
                switch (id) {
                    case LPAREN:
                    case LBRACKET:
                        if (id == bracketId) {
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
                        break;

                    case RPAREN:
                    case RBRACKET:
                        if (id == bracketId) {
                            bracketBalance--;
                        }
                        break;
                    case LBRACE:
                        braceBalance++;
                        if (braceBalance > 0) { // stop on extra left brace
                            finished = true;
                        }
                        break;

                    case RBRACE:
                        braceBalance--;
                        break;

                }
                // done regardless of finished flag state
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
                ts.moveIndex(lastRBracketIndex);
                ts.moveNext();
//                token = lastRBracket.getNext();
                ts.moveNext(); // ???
                finished = false;
                while (!finished && ts.movePrevious()) {
                    CppTokenId id = ts.token().id();
                    switch (id) {
                        case LPAREN:
                        case LBRACKET:
                            if (id == leftBracketId) {
                                bracketBalance++;
                            }
                            break;

                        case RPAREN:
                        case RBRACKET:
                            if (id == bracketId) {
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
                            break;

                        case LBRACE:
                            braceBalance++;
                            break;

                        case RBRACE:
                            braceBalance--;
                            if (braceBalance < 0) { // stop on extra right brace
                                finished = true;
                            }
                            break;

                    }
                    // done regardless of finished flag state
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
     * @param theBracket the bracket that was inserted
     */
    private static void completeOpeningBracket(BaseDocument doc,
            int dotPos,
            Caret caret,
            char theBracket) throws BadLocationException {
        if (isCompletablePosition(doc, dotPos + 1)) {
            String matchinBracket = "" + matching(theBracket);
            doc.insertString(dotPos + 1, matchinBracket, null);
            caret.setDot(dotPos + 1);
        }
    }

    private static boolean isEscapeSequence(BaseDocument doc, int dotPos) throws BadLocationException {
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
     * @param theBracket the character that was inserted
     */
    private static void completeQuote(BaseDocument doc, int dotPos, Caret caret,
            char theBracket)
            throws BadLocationException {
        if (isEscapeSequence(doc, dotPos)) {
            return;
        }
        TokenID[] tokenIDs = theBracket == '\"' ? new TokenID[]{CCTokenContext.STRING_LITERAL, CCTokenContext.INCOMPLETE_USR_INCLUDE}
                : new TokenID[]{CCTokenContext.CHAR_LITERAL};
        if ((posWithinQuotes(doc, dotPos + 1, theBracket, tokenIDs) && isCompletablePosition(doc, dotPos + 1)) &&
                (isUnclosedStringAtLineEnd(doc, dotPos, tokenIDs) &&
                ((doc.getLength() == dotPos + 1) ||
                (doc.getLength() != dotPos + 1 && doc.getChars(dotPos + 1, 1)[0] != theBracket)))) {
            doc.insertString(dotPos + 1, "" + theBracket, null);
            caret.setDot(dotPos + 1);
        } else {
            char[] charss = doc.getChars(dotPos + 1, 1);
            // System.out.println("NOT Within string, " + new String(charss));
            if (charss != null && charss[0] == theBracket) {
                doc.remove(dotPos + 1, 1);
            }
        }
    }

    /**
     * Checks whether dotPos is a position at which bracket and quote
     * completion is performed. Brackets and quotes are not completed
     * everywhere but just at suitable places .
     * @param doc the document
     * @param dotPos position to be tested
     */
    private static boolean isCompletablePosition(BaseDocument doc, int dotPos)
            throws BadLocationException {
        if (dotPos == doc.getLength()) {// there's no other character to test
            return true;
        } else {
            // test that we are in front of ) , " or '
            char chr = doc.getChars(dotPos, 1)[0];
            return (chr == ')' ||
                    chr == ',' ||
                    chr == '\"' ||
                    chr == '\'' ||
                    chr == ' ' ||
                    chr == '-' ||
                    chr == '+' ||
                    chr == '|' ||
                    chr == '&' ||
                    chr == ']' ||
                    chr == '}' ||
                    chr == '\n' ||
                    chr == '\t' ||
                    chr == ';');
        }
    }

    /**
     * Returns true if bracket completion is enabled in options.
     */
    private static boolean completionSettingEnabled() {
        Preferences prefs = MimeLookup.getLookup(MIMENames.CPLUSPLUS_MIME_TYPE).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, true);
    }

    /**
     * Returns for an opening bracket or quote the appropriate closing
     * character.
     */
    private static char matching(char theBracket) {
        switch (theBracket) {
            case '(':
                return ')';
            case '[':
                return ']';
            case '\"':
                return '\"'; // NOI18N
            case '\'':
                return '\'';
            case '<':
                return '>';

            default:
                return ' ';
        }
    }

    /**
     * posWithinString(doc, pos) iff position *pos* is within a string
     * literal in document doc.
     * @param doc the document
     * @param dotPos position to be tested
     */
    static boolean posWithinString(BaseDocument doc, int dotPos) {
        return posWithinQuotes(doc, dotPos, '\"',
                new TokenID[]{CCTokenContext.STRING_LITERAL, CCTokenContext.USR_INCLUDE});
    }

    /**
     * Generalized posWithingString to any token and delimiting
     * character. It works for tokens are delimited by *quote* and
     * extend up to the other *quote* or whitespace in case of an
     * incomplete token.
     * @param doc the document
     * @param dotPos position to be tested
     */
    static boolean posWithinQuotes(BaseDocument doc, int dotPos, char quote, TokenID[] tokenIDs) {
        try {
            MyTokenProcessor proc = new MyTokenProcessor();
            doc.getSyntaxSupport().tokenizeText(proc,
                    dotPos - 1,
                    doc.getLength(), true);
            if (matchIDs(proc.tokenID, tokenIDs)) {
                return (dotPos - proc.tokenStart == 1 || doc.getChars(dotPos - 1, 1)[0] != quote);
            }
            return false;
        } catch (BadLocationException ex) {
            return false;
        }
    }

    static boolean posWithinAnyQuote(BaseDocument doc, int dotPos) {
        try {
            MyTokenProcessor proc = new MyTokenProcessor();
            doc.getSyntaxSupport().tokenizeText(proc,
                    dotPos - 1,
                    doc.getLength(), true);
            if (proc.tokenID == CCTokenContext.STRING_LITERAL ||
                    proc.tokenID == CCTokenContext.CHAR_LITERAL ||
                    proc.tokenID == CCTokenContext.SYS_INCLUDE ||
                    proc.tokenID == CCTokenContext.USR_INCLUDE) {
                char[] ch = doc.getChars(dotPos - 1, 1);
                return dotPos - proc.tokenStart == 1 || (ch[0] != '\"' && ch[0] != '\'');
            }
            return false;
        } catch (BadLocationException ex) {
            return false;
        }
    }

    static boolean isUnclosedStringAtLineEnd(BaseDocument doc, int dotPos, TokenID[] tokenIDs) {
        try {
            MyTokenProcessor proc = new MyTokenProcessor();
            doc.getSyntaxSupport().tokenizeText(proc, Utilities.getRowLastNonWhite(doc, dotPos), doc.getLength(), true);
            return matchIDs(proc.tokenID, tokenIDs);
        } catch (BadLocationException ex) {
            return false;
        }
    }

    static boolean matchIDs(TokenID toCheck, TokenID[] checkWith) {
        for (int i = checkWith.length - 1; i >= 0; i--) {
            if (toCheck == checkWith[i]) {
                return true;
            }
        }
        return false;
    }

    static boolean matchIDs(Token<CppTokenId> toCheck, Token<CppTokenId>[] checkWith) {
        for (int i = checkWith.length - 1; i >= 0; i--) {
            if (toCheck == checkWith[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * A token processor used to find out the length of a token.
     */
    static class MyTokenProcessor implements TokenProcessor {

        public TokenID tokenID = null;
        public int tokenStart = -1;

        public boolean token(TokenID tokenID, TokenContextPath tcp,
                int tokBuffOffset, int tokLength) {
            this.tokenStart = tokenBuffer2DocumentOffset(tokBuffOffset);
            this.tokenID = tokenID;
            // System.out.println("token " + tokenID.getName() + " at " + tokenStart + " (" +
            //		 tokBuffOffset + ") len:" + tokLength);
            return false;
        }

        public int eot(int offset) { // System.out.println("EOT");
            return 0;
        }

        public void nextBuffer(char[] buffer, int offset, int len, int startPos, int preScan, boolean lastBuffer) {
            // System.out.println("nextBuffer "+ new String(buffer) + "," + offset + "len: " + len + " startPos:"+startPos + " preScan:" + preScan + " lastBuffer:" + lastBuffer);
            this.bufferStartPos = startPos - offset;
        }
        private int bufferStartPos = 0;

        private int tokenBuffer2DocumentOffset(int offs) {
            return offs + bufferStartPos;
        }
    }

    /**
     * Token processor for finding of balance of brackets and braces.
     */
    private static class BalanceTokenProcessor extends CndAbstractTokenProcessor<Token<CppTokenId>> {

        private CppTokenId leftTokenID;
        private CppTokenId rightTokenID;
        private Stack<Integer> stack = new Stack<Integer>();
        private int balance;
        private boolean isDefine;
        private boolean inPPDirective = false;

        BalanceTokenProcessor(CppTokenId leftTokenID, CppTokenId rightTokenID) {
            this.leftTokenID = leftTokenID;
            this.rightTokenID = rightTokenID;
        }

        @Override
        public boolean token(Token<CppTokenId> token, int tokenOffset) {
            if (token.id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
                inPPDirective = true;
                return true;
            }
            switch (token.id()) {
                case NEW_LINE:
                    inPPDirective = false;
                    isDefine = false;
                    break;
                case PREPROCESSOR_DEFINE:
                    isDefine = true;
                    break;
                case PREPROCESSOR_IF:
                case PREPROCESSOR_IFDEF:
                case PREPROCESSOR_IFNDEF:
                    stack.push(balance);
                    break;
                case PREPROCESSOR_ELSE:
                case PREPROCESSOR_ELIF:
                    if (!stack.empty()) {
                        balance = stack.peek();
                    }
                    break;
                case PREPROCESSOR_ENDIF:
                    if (!stack.empty()) {
                        stack.pop();
                    }
                    break;
                default:
                    if (!isDefine) {
                        if (token.id() == leftTokenID) {
                            balance++;
                        } else if (token.id() == rightTokenID) {
                            balance--;
                        }
                    }
            }
            return false;
        }

        private int getBalance() {
            return balance;
        }
    }
}
