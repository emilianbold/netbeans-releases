/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.editor.java;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * This static class groups the whole aspect of bracket
 * completion. It is defined to clearly separate the functionality
 * and keep actions clean. 
 * The methods of the class are called from different actions as
 * KeyTyped, DeletePreviousChar.
 * <br/>
 * This class is similar to old BracketCompletion but works solely
 * with the token hierarchy.
 */
class BraceCompletion {

    /**
     * A hook method called after a character was inserted into the
     * document. The function checks for special characters for
     * completion ()[]'"{} and other conditions and optionally performs
     * changes to the doc and or caret (complets braces, moves caret,
     * etc.)
     * @param doc the document where the change occurred
     * @param caretOffset position of the character that was inserted.
     * @param caret caret
     * @param ch the character that was inserted
     * @throws BadLocationException if caretOffset is not correct
     */
    static void charInserted(BaseDocument doc, int caretOffset, Caret caret, char ch) throws BadLocationException {
        if (!completionSettingEnabled()) {
          return;
        }

        if (ch == ')' || ch == ']' || ch == '(' || ch == '[' || ch == ';') {
            TokenSequence<JavaTokenId> javaTS = javaTokenSequence(doc, caretOffset, false);
            if (javaTS != null) {
                switch (javaTS.token().id()) {
                    case RPAREN:
                    case RBRACKET:
                        skipClosingBracket(doc, caret, ch, javaTS);
                        break;
                    case LPAREN:
                    case LBRACKET:
                        completeOpeningBracket(doc, caretOffset, caret, ch, javaTS);
                        break;
                    case SEMICOLON:
                        moveSemicolon(doc, caretOffset, caret, javaTS);
                }
            }
        }
    }

    private static void moveSemicolon(BaseDocument doc, int caretOffset, Caret caret,
    TokenSequence<JavaTokenId> ts) throws BadLocationException {
        int eolOffset = Utilities.getRowEnd(doc, caretOffset);
        int lastParenPos = caretOffset;
        int index = ts.index();
        // Move beyond semicolon
        while (ts.moveNext() && ts.offset() <= eolOffset) {
            Token<JavaTokenId> token = ts.token();
            switch (token.id()) {
                case RPAREN:
                    lastParenPos = ts.offset();
                    break;
                case WHITESPACE:
                    break;
                default:
                    return; //
            }
        }
        // Restore ts position
        ts.moveIndex(index);
        ts.moveNext();
        if (isForLoopSemicolon(ts) || posWithinAnyQuote(doc, caretOffset)) {
            return;
        }
        doc.remove(caretOffset, 1);
        doc.insertString(lastParenPos, ";", null); // NOI18N
        caret.setDot(lastParenPos + 1);
    }

    private static boolean isForLoopSemicolon(TokenSequence<JavaTokenId> ts) {
        Token<JavaTokenId> token = ts.token();
        if (token == null || token.id() != JavaTokenId.SEMICOLON) {
            return false;
        }
        int parenDepth = 0; // parenthesis depth
        int braceDepth = 0; // brace depth
        boolean semicolonFound = false; // next semicolon
        int tsOrigIndex = ts.index();
        try {
            while (ts.movePrevious()) {
                token = ts.token();
                switch (token.id()) {
                    case LPAREN:
                        if (parenDepth == 0) { // could be a 'for ('
                            while (ts.movePrevious()) {
                                token = ts.token();
                                switch (token.id()) {
                                    case WHITESPACE:
                                    case BLOCK_COMMENT:
                                    case JAVADOC_COMMENT:
                                    case LINE_COMMENT:
                                        break; // skip
                                    case FOR:
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                            return false;
                        } else { // non-zero depth
                            parenDepth--;
                        }
                        break;

                    case RPAREN:
                        parenDepth++;
                        break;

                    case LBRACE:
                        if (braceDepth == 0) { // unclosed left brace
                            return false;
                        }
                        braceDepth--;
                        break;

                    case RBRACE:
                        braceDepth++;
                        break;

                    case SEMICOLON:
                        if (semicolonFound) { // one semicolon already found
                            return false;
                        }
                        semicolonFound = true;
                        break;
                }
            }
        } finally {
            // Restore orig TS's location
            ts.moveIndex(tsOrigIndex);
            ts.moveNext();
        }
        return false;
    }

    /**
     * Hook called after a character *ch* was backspace-deleted from
     * *doc*. The function possibly removes bracket or quote pair if
     * appropriate.
     * @param doc the document
     * @param caretOffset position of the change
     * @param caret caret
     * @param ch the character that was deleted
     */
    static void charBackspaced(BaseDocument doc, int caretOffset, Caret caret, char ch) throws BadLocationException {
        if (!completionSettingEnabled()) {
            return;
        }
        // Get token (forward bias) behind removed char
        TokenSequence<JavaTokenId> ts = javaTokenSequence(doc, caretOffset, false);
        if (ts == null) {
            return;
        }
        if (ch == '(' || ch == '[') {
            switch (ts.token().id()) {
                case RPAREN:
                    if (tokenBalance(doc, JavaTokenId.LPAREN) != 0) {
                        doc.remove(caretOffset, 1);
                    }
                    break;
                case RBRACKET:
                    if (tokenBalance(doc, JavaTokenId.LBRACKET) != 0) {
                        doc.remove(caretOffset, 1);
                    }
                    break;
            }
        } else if (ch == '\"') {
            if (ts.token().id() == JavaTokenId.STRING_LITERAL && ts.offset() == caretOffset) {
                doc.remove(caretOffset, 1);
            }
        } else if (ch == '\'') {
            if (ts.token().id() == JavaTokenId.CHAR_LITERAL && ts.offset() == caretOffset) {
                doc.remove(caretOffset, 1);
            }
        }
    }

    static int tokenBalance(BaseDocument doc, JavaTokenId leftTokenId) {
        TokenBalance tb = TokenBalance.get(doc);
        if (!tb.isTracked(JavaTokenId.language())) {
            tb.addTokenPair(JavaTokenId.language(), JavaTokenId.LPAREN, JavaTokenId.RPAREN);
            tb.addTokenPair(JavaTokenId.language(), JavaTokenId.LBRACKET, JavaTokenId.RBRACKET);
            tb.addTokenPair(JavaTokenId.language(), JavaTokenId.LBRACE, JavaTokenId.RBRACE);
        }
        int balance = tb.balance(JavaTokenId.language(), leftTokenId);
        assert (balance != Integer.MAX_VALUE);
        return balance;
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
    static boolean isAddRightBrace(BaseDocument doc, int caretOffset)
            throws BadLocationException
    {
        if (!completionSettingEnabled()) {
            return false;
        }
        if (tokenBalance(doc, JavaTokenId.LBRACE) <= 0) {
            return false;
        }
        int caretRowStartOffset = Utilities.getRowStart(doc, caretOffset);
        TokenSequence<JavaTokenId> ts = javaTokenSequence(doc, caretOffset, true);
        if (ts == null) {
            return false;
        }
        boolean first = true;
        do {
            if (ts.offset() < caretRowStartOffset) {
                return false;
            }
            switch (ts.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                    break;
                case BLOCK_COMMENT:
                case JAVADOC_COMMENT:
                    if (first && caretOffset > ts.offset() && caretOffset < ts.offset() + ts.token().length()) {
                        // Caret contained within block comment -> do not add anything
                        return false;
                    }
                    break; // Skip
                case LBRACE:
                    return true;
            }
            first = false;
        } while (ts.movePrevious());
        return false;
    }

    /**
     * Returns position of the first unpaired closing paren/brace/bracket from the caretOffset
     * till the end of caret row. If there is no such element, position after the last non-white
     * character on the caret row is returned.
     */
    static int getRowOrBlockEnd(BaseDocument doc, int caretOffset, boolean[] insert) throws BadLocationException {
        int rowEnd = Utilities.getRowLastNonWhite(doc, caretOffset);
        if (rowEnd == -1 || caretOffset >= rowEnd) {
            return caretOffset;
        }
        rowEnd += 1;
        int parenBalance = 0;
        int braceBalance = 0;
        int bracketBalance = 0;
        TokenSequence<JavaTokenId> ts = javaTokenSequence(doc, caretOffset, false);
        if (ts == null) {
            return caretOffset;
        }
        while (ts.offset() < rowEnd) {
            switch (ts.token().id()) {
                case SEMICOLON:
                    return ts.offset() + 1;
                case LPAREN:
                    parenBalance++;
                    break;
                case RPAREN:
                    if (parenBalance-- == 0) {
                        return ts.offset();
                    }
                case LBRACE:
                    braceBalance++;
                    break;
                case RBRACE:
                    if (braceBalance-- == 0) {
                        return ts.offset();
                    }
                case LBRACKET:
                    bracketBalance++;
                    break;
                case RBRACKET:
                    if (bracketBalance-- == 0) {
                        return ts.offset();
                    }
            }
            if (!ts.moveNext())
                break;
        }

        insert[0] = false;
        return rowEnd;
    }

    /**
     * Get token sequence positioned over a token.
     *
     * @param doc
     * @param offset
     * @param backwardBias
     * @return token sequence positioned over a token that "contains" the offset
     *  or null if the document does not contain any java token sequence
     *  or the offset is at doc-or-section-start-and-bwd-bias
     *  or doc-or-section-end-and-fwd-bias.
     */
    private static TokenSequence<JavaTokenId> javaTokenSequence(Document doc, int offset, boolean backwardBias) {
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        List<TokenSequence<?>> tsList = hi.embeddedTokenSequences(offset, backwardBias);
        // Go from inner to outer TSes
        for (int i = tsList.size() - 1; i >= 0; i--) {
            TokenSequence<?> ts = tsList.get(i);
            if (ts.languagePath().innerLanguage() == JavaTokenId.language()) {
                TokenSequence<JavaTokenId> javaInnerTS = (TokenSequence<JavaTokenId>) ts;
                return javaInnerTS;
            }
        }
        return null;
    }

    /** 
     * Counts the number of braces starting at caretOffset to the end of the
     * document. Every occurence of { increses the count by 1, every
     * occurrence of } decreses the count by 1. The result is returned.
     * @return The number of { - number of } (>0 more { than } ,<0 more } than {)
     */
    private static int braceBalance(BaseDocument doc)
            throws BadLocationException {
        return tokenBalance(doc, JavaTokenId.LBRACE);
    }

    /**
     * A hook to be called after closing bracket ) or ] was inserted into
     * the document. The method checks if the bracket should stay there
     * or be removed and some exisitng bracket just skipped.
     *
     * @param doc the document
     * @param caretOffset position of the inserted bracket
     * @param caret caret
     * @param bracket the bracket character ']' or ')'
     */
    private static void skipClosingBracket(BaseDocument doc, Caret caret, char rightBracketChar,
            TokenSequence<JavaTokenId> innerTS) throws BadLocationException
    {
        JavaTokenId bracketId = bracketCharToId(rightBracketChar);
        int caretOffset = caret.getDot();
        if (isSkipClosingBracket(doc, caretOffset, bracketId)) {
            doc.remove(caretOffset - 1, 1);
            caret.setDot(caretOffset); // skip closing bracket
        }
    }

    private static Set<JavaTokenId> STOP_TOKENS_FOR_SKIP_CLOSING_BRACKET = EnumSet.of(JavaTokenId.LBRACE, JavaTokenId.RBRACE, JavaTokenId.SEMICOLON);
    
    /**
     * Check whether the typed bracket should stay in the document
     * or be removed.
     * <br>
     * This method is called by <code>skipClosingBracket()</code>.
     *
     * @param doc document into which typing was done.
     * @param caretOffset
     */
    static boolean isSkipClosingBracket(BaseDocument doc, int caretOffset, JavaTokenId rightBracketId) throws BadLocationException {
        // First check whether the caret is not after the last char in the document
        // because no bracket would follow then so it could not be skipped.
        if (caretOffset == doc.getLength()) {
            return false; // no skip in this case
        }
        boolean skipClosingBracket = false; // by default do not remove
        // Examine token at the caret offset
        TokenSequence<JavaTokenId> javaTS = javaTokenSequence(doc, caretOffset, false);
        if (javaTS != null && javaTS.token().id() == rightBracketId) {
            JavaTokenId leftBracketId = matching(rightBracketId);
            // Skip all the brackets of the same type that follow the last one
            do {
                if (   STOP_TOKENS_FOR_SKIP_CLOSING_BRACKET.contains(javaTS.token().id())
                    || (javaTS.token().id() == JavaTokenId.WHITESPACE && javaTS.token().text().toString().contains("\n"))) {
                    while (javaTS.token().id() != rightBracketId && javaTS.movePrevious())
                        ;
                    break;
                }
            } while (javaTS.moveNext());
            // token var points to the last bracket in a group of two or more right brackets
            // Attempt to find the left matching bracket for it
            // Search would stop on an extra opening left brace if found
            int braceBalance = 0; // balance of '{' and '}'
            int bracketBalance = -1; // balance of the brackets or parenthesis
            int lastRBracketIndex = javaTS.index();
            boolean finished = false;
            while (!finished && javaTS.movePrevious()) {
                JavaTokenId id = javaTS.token().id();
                switch (id) {
                    case LPAREN:
                    case LBRACKET:
                        if (id == leftBracketId) {
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
                                finished = javaTS.offset() < caretOffset;
                            }
                        }
                        break;

                    case RPAREN:
                    case RBRACKET:
                        if (id == rightBracketId) {
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
            }

            skipClosingBracket = bracketBalance != 0;

            //commented out due to #147683:
//            if (bracketBalance != 0) { // not found matching bracket
//                // Remove the typed bracket as it's unmatched
//                skipClosingBracket = true;
//
//            } else { // the bracket is matched
//                // Now check whether the bracket would be matched
//                // when the closing bracket would be removed
//                // i.e. starting from the original lastRBracketIndex token
//                // and search for the same bracket to the right in the text
//                // The search would stop on an extra right brace if found
//                braceBalance = 0;
//                bracketBalance = 1; // simulate one extra left bracket
//                finished = false;
//                // Relocate behind original rbracket and one token to right
//                if (lastRBracketIndex + 2 <= javaTS.tokenCount()) {
//                    javaTS.moveIndex(lastRBracketIndex + 2);
//                } else { // mark as finished
//                    finished = true;
//                }
//                while (!finished && javaTS.movePrevious()) {
//                    JavaTokenId id = javaTS.token().id();
//                    switch (id) {
//                        case LPAREN:
//                        case LBRACKET:
//                            if (id == leftBracketId) {
//                                bracketBalance++;
//                            }
//                            break;
//
//                        case RPAREN:
//                        case RBRACKET:
//                            if (id == rightBracketId) {
//                                bracketBalance--;
//                                if (bracketBalance == 0) {
//                                    if (braceBalance != 0) {
//                                        // Here the bracket is matched but it is located
//                                        // inside an unclosed brace block
//                                        // which is in fact illegal but it's a question
//                                        // of what's best to do in this case.
//                                        // We chose to leave the typed bracket
//                                        // by setting bracketBalance to -1.
//                                        // It can be revised in the future.
//                                        bracketBalance = -1;
//                                    }
//                                    finished = true;
//                                }
//                            }
//                            break;
//
//                        case LBRACE:
//                            braceBalance++;
//                            break;
//
//                        case RBRACE:
//                            braceBalance--;
//                            if (braceBalance < 0) { // stop on extra right brace
//                                finished = true;
//                            }
//                            break;
//                    }
//                }
//                // If bracketBalance == 0 the bracket would be matched
//                // by the bracket that follows the last right bracket.
//                skipClosingBracket = (bracketBalance == 0);
//            }
        }
        return skipClosingBracket;
    }

    /**
     * Check for various conditions and possibly add a pairing bracket.
     * to the already inserted.
     * @param doc the document
     * @param caretOffset position of the opening bracket (already in the doc)
     * @param caret caret
     * @param bracket the bracket that was inserted
     */
    private static void completeOpeningBracket(BaseDocument doc, int caretOffset, Caret caret, char bracketChar,
        TokenSequence<JavaTokenId> innerTS) throws BadLocationException
    {
        if (isCompletablePosition(doc, caretOffset + 1)) {
            doc.insertString(caretOffset + 1, String.valueOf(matching(bracketChar)), null);
            caret.setDot(caretOffset + 1);
        }
    }

    private static boolean isEscapeSequence(BaseDocument doc, int caretOffset) throws BadLocationException {
        if (caretOffset <= 0) {
            return false;
        }

        if (caretOffset == 1) {
            char previousChar = doc.getChars(caretOffset - 1, 1)[0];

            return previousChar == '\\';
        }

        char[] previousChars = doc.getChars(caretOffset - 2, 2);

        return previousChars[0] != '\\' && previousChars[1] == '\\';
    }

    /** 
     * Called to decide whether either single bracket should be inserted
     * or whether bracket pair should be inserted instead.
     * It's called before anything was inserted into the document.
     *
     * @param doc the document
     * @param caretOffset position of the opening bracket (already in the doc)
     * @param caret caret
     * @param bracket the character that was inserted
     * @return true if the method serviced insertions or removals or false
     *  if there's nothing special to be done and the parent should handle the insertion regularly.
     */
    static boolean completeQuote(BaseDocument doc, int caretOffset, Caret caret, char bracket)
        throws BadLocationException
    {
        if (!completionSettingEnabled()) {
            return false;
        }
        if (isEscapeSequence(doc, caretOffset)) { // \" or \' typed
            return false;
        }
        // Examine token id at the caret offset
        TokenSequence<JavaTokenId> javaTS = javaTokenSequence(doc, caretOffset, true);
        JavaTokenId id = (javaTS != null) ? javaTS.token().id() : null;
        int lastNonWhite = Utilities.getRowLastNonWhite(doc, caretOffset);
        // eol - true if the caret is at the end of line (ignoring whitespaces)
        boolean eol = lastNonWhite < caretOffset;

        // If caret within comment return false
        boolean caretInsideToken = (id != null) &&
                (javaTS.offset() + javaTS.token().length() > caretOffset ||
                 javaTS.token().partType() == PartType.START);
        if (caretInsideToken && (id == JavaTokenId.BLOCK_COMMENT ||
                id == JavaTokenId.JAVADOC_COMMENT ||
                id == JavaTokenId.LINE_COMMENT)
        ) {
            return false;
        }
        boolean completablePosition = isQuoteCompletablePosition(doc, caretOffset);
        boolean insideString = caretInsideToken &&
                (id == JavaTokenId.STRING_LITERAL ||
                 id == JavaTokenId.CHAR_LITERAL);
        if (insideString) {
            if (eol) {
                return false; // do not complete
            } else {
                //#69524
                char chr = doc.getChars(caretOffset, 1)[0];
                if (chr == bracket) {
                    //#83044
                    if (caretOffset > 0) {
                        javaTS.move(caretOffset - 1);
                        if (javaTS.moveNext()) {
                            id = javaTS.token().id();
                            if (id == JavaTokenId.STRING_LITERAL ||
                                id == JavaTokenId.CHAR_LITERAL)
                            {
                                doc.insertString(caretOffset, String.valueOf(bracket), null); //NOI18N
                                doc.remove(caretOffset, 1);
                                return true;
                            }
                        }
                    }
                //end of #83044
                }
            //end of #69524
            }
        }

        if ((completablePosition && !insideString) || eol) {
            doc.insertString(caretOffset, String.valueOf(bracket) + bracket, null); //NOI18N
            return true;
        }

        return false;
    }

    /** 
     * Checks whether caretOffset is a position at which bracket and quote
     * completion is performed. Brackets and quotes are not completed
     * everywhere but just at suitable places .
     * @param doc the document
     * @param caretOffset position to be tested
     */
    private static boolean isCompletablePosition(BaseDocument doc, int caretOffset)
            throws BadLocationException
    {
        if (caretOffset == doc.getLength()) // there's no other character to test
        {
            return true;
        } else {
            // test that we are in front of ) , " or '
            char chr = doc.getChars(caretOffset, 1)[0];
            return (chr == ')' ||
                    chr == ',' ||
                    chr == '\"' ||
                    chr == '\'' ||
                    chr == ' ' ||
                    chr == ']' ||
                    chr == '}' ||
                    chr == '\n' ||
                    chr == '\t' ||
                    chr == ';');
        }
    }

    private static boolean isQuoteCompletablePosition(BaseDocument doc, int caretOffset)
            throws BadLocationException
    {
        if (caretOffset == doc.getLength()) { // there's no other character to test
            return true;
        } else {
            // test that we are in front of ) , " or ' ... etc.
            int eolOffset = Utilities.getRowEnd(doc, caretOffset);
            if (caretOffset == eolOffset || eolOffset == -1) {
                return false;
            }
            int firstNonWhiteFwdOffset = Utilities.getFirstNonWhiteFwd(doc, caretOffset, eolOffset);
            if (firstNonWhiteFwdOffset == -1) {
                return false;
            }
            char chr = doc.getChars(firstNonWhiteFwdOffset, 1)[0];
            return (chr == ')' ||
                    chr == ',' ||
                    chr == '+' ||
                    chr == '}' ||
                    chr == ';');
        }
    }

    /** 
     * Returns true if bracket completion is enabled in options.
     */
    static boolean completionSettingEnabled() {
        Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, false);
    }

    /**
     * Returns for an opening bracket or quote the appropriate closing
     * character.
     */
    private static char matching(char bracket) {
        switch (bracket) {
            case '(':
                return ')';
            case '[':
                return ']';
            case '\"':
                return '\"'; // NOI18N
            case '\'':
                return '\'';
            default:
                return ' ';
        }
    }

    private static JavaTokenId matching(JavaTokenId id) {
        switch (id) {
            case LPAREN:
                return JavaTokenId.RPAREN;
            case LBRACKET:
                return JavaTokenId.RBRACKET;
            case RPAREN:
                return JavaTokenId.LPAREN;
            case RBRACKET:
                return JavaTokenId.LBRACKET;
            default:
                return null;
        }
    }

    private static JavaTokenId bracketCharToId(char bracket) {
        switch (bracket) {
            case '(':
                return JavaTokenId.LPAREN;
            case ')':
                return JavaTokenId.RPAREN;
            case '[':
                return JavaTokenId.LBRACKET;
            case ']':
                return JavaTokenId.RBRACKET;
            case '{':
                return JavaTokenId.LBRACE;
            case '}':
                return JavaTokenId.RBRACE;
            default:
                throw new IllegalArgumentException("Not a bracket char '" + bracket + '\'');
        }
    }

    /**
     * posWithinString(doc, pos) iff position *pos* is within a string
     * literal in document doc.
     * @param doc the document
     * @param caretOffset position to be tested (before '\n' gets inserted into doc.
     */
    static boolean posWithinString(BaseDocument doc, int caretOffset) {
        return posWithinQuotes(doc, caretOffset, '"', JavaTokenId.STRING_LITERAL);
    }

    /**
     * Generalized posWithingString to any token and delimiting
     * character. It works for tokens are delimited by *quote* and
     * extend up to the other *quote* or whitespace in case of an
     * incomplete token.
     * @param doc the document
     * @param caretOffset position of typed quote
     */
    static boolean posWithinQuotes(BaseDocument doc, int caretOffset, char quote, JavaTokenId tokenId) {
        TokenSequence<JavaTokenId> javaTS = javaTokenSequence(doc, caretOffset - 1, false);
        if (javaTS != null) {
            return javaTS.token().id() == tokenId &&
                    (caretOffset - javaTS.offset() == 1 ||
                     !(DocumentUtilities.getText(doc).charAt(caretOffset - 1) == quote && DocumentUtilities.getText(doc).charAt(caretOffset - 2) != '\\'));
        }
        return false;
    }

    static boolean posWithinAnyQuote(BaseDocument doc, int caretOffset) {
        TokenSequence<JavaTokenId> javaTS = javaTokenSequence(doc, caretOffset - 1, false);
        if (javaTS != null) {
            JavaTokenId id = javaTS.token().id();
            if (id == JavaTokenId.STRING_LITERAL ||
                id == JavaTokenId.CHAR_LITERAL
            ) {
                char ch = DocumentUtilities.getText(doc).charAt(caretOffset - 1);
                return (caretOffset - javaTS.offset() == 1 || (ch != '"' && ch != '\''));
            }
        }
        return false;
    }

    static boolean isUnclosedStringAtLineEnd(BaseDocument doc, int caretOffset) {
        int lastNonWhiteOffset;
        try {
            lastNonWhiteOffset = Utilities.getRowLastNonWhite(doc, caretOffset);
        } catch (BadLocationException e) {
            return false;
        }
        TokenSequence<JavaTokenId> javaTS = javaTokenSequence(doc, lastNonWhiteOffset, true);
        if (javaTS != null) {
            return (javaTS.token().id() == JavaTokenId.STRING_LITERAL);
        }
        return false;
    }

}
