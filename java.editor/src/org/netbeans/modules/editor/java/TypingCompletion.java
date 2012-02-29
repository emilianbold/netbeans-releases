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
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 * This static class groups the whole aspect of bracket completion. It is
 * defined to clearly separate the functionality and keep actions clean. The
 * methods of the class are called from different typing hooks <br/> This class
 * is similar to old BraceCompletion but works solely with the token hierarchy
 * and typing hooks context. Every method is called 
 * before anything was inserted into the document.
 */
class TypingCompletion {

    /**
     * Returns true if bracket completion is enabled in options.
     */
    static boolean isCompletionSettingEnabled() {
        Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, false);
    }

    /**
     * Check for various conditions and possibly remove two quotes.
     *
     * @param context
     * @throws BadLocationException
     */
    static void removeCompletedQuote(DeletedTextInterceptor.Context context) throws BadLocationException {
        TokenSequence<JavaTokenId> ts = javaTokenSequence(context, false);
        if (ts == null) {
            return;
        }
        char removedChar = context.getText().charAt(0);
        int caretOffset = context.isBackwardDelete() ? context.getOffset() - 1 : context.getOffset();
        if (removedChar == '\"') {
            if (ts.token().id() == JavaTokenId.STRING_LITERAL && ts.offset() == caretOffset) {
                context.getDocument().remove(caretOffset, 1);
            }
        } else if (removedChar == '\'') {
            if (ts.token().id() == JavaTokenId.CHAR_LITERAL && ts.offset() == caretOffset) {
                context.getDocument().remove(caretOffset, 1);
            }
        }
    }

    /**
     * Check for various conditions and possibly remove two brackets.
     *
     * @param context
     * @throws BadLocationException
     */
    static void removeBrackets(DeletedTextInterceptor.Context context) throws BadLocationException {
        int caretOffset = context.isBackwardDelete() ? context.getOffset() - 1 : context.getOffset();
        TokenSequence<JavaTokenId> ts = javaTokenSequence(context.getDocument(), caretOffset, false);
        if (ts == null) {
            return;
        }

        switch (ts.token().id()) {
            case RPAREN:
                if (tokenBalance(context.getDocument(), JavaTokenId.LPAREN) != 0) {
                    context.getDocument().remove(caretOffset, 1);
                }
                break;
            case RBRACKET:
                if (tokenBalance(context.getDocument(), JavaTokenId.LBRACKET) != 0) {
                    context.getDocument().remove(caretOffset, 1);
                }
                break;
        }
    }

    /**
     * Check for various conditions and possibly skip a closing bracket.
     *
     * @param context
     * @return relative caretOffset change
     * @throws BadLocationException
     */
    static int skipClosingBracket(TypedTextInterceptor.MutableContext context) throws BadLocationException {
        TokenSequence<JavaTokenId> javaTS = javaTokenSequence(context, false);
        if (javaTS == null || (javaTS.token().id() != JavaTokenId.RPAREN && javaTS.token().id() != JavaTokenId.RBRACKET)) {
            return -1;
        }

        JavaTokenId bracketId = bracketCharToId(context.getText().charAt(0));
        if (isSkipClosingBracket(context, javaTS, bracketId)) {
            context.setText("", 0);  // NOI18N
            return context.getOffset() + 1;
        }
        return -1;
    }

    /**
     * Check for various conditions and possibly add a pairing bracket.
     *
     * @param context
     * @throws BadLocationException
     */
    static void completeOpeningBracket(TypedTextInterceptor.MutableContext context) throws BadLocationException {
        char chr = context.getDocument().getText(context.getOffset(), 1).charAt(0);
        if (chr == ')' || chr == ',' || chr == '\"' || chr == '\'' || chr == ' ' || chr == ']' || chr == '}' || chr == '\n' || chr == '\t' || chr == ';') {
            char insChr = context.getText().charAt(0);
            context.setText("" + insChr + matching(insChr) , 1);  // NOI18N
        }
    }

    /**
     * Called to add semicolon after bracket for some conditions
     *
     * @param context
     * @return relative caretOffset change
     * @throws BadLocationException
     */
    static int moveSemicolon(TypedTextInterceptor.MutableContext context) throws BadLocationException {
        TokenSequence<JavaTokenId> javaTS = javaTokenSequence(context, false);
        if (javaTS == null) {
            return -1;
        }
        int lastParenPos = context.getOffset();
        int index = javaTS.index();
        // Move beyond semicolon
        while (javaTS.moveNext() && !(javaTS.token().id() == JavaTokenId.WHITESPACE && javaTS.token().text().toString().contains("\n"))) {  // NOI18N
            switch (javaTS.token().id()) {
                case RPAREN:
                    lastParenPos = javaTS.offset();
                    break;
                case WHITESPACE:
                    break;
                default:
                    return -1;
            }
        }
        // Restore javaTS position
        javaTS.moveIndex(index);
        javaTS.moveNext();
        if (isForLoopSemicolon(javaTS) || posWithinAnyQuote(context, javaTS)) {
            return -1;
        }
        context.setText("", 0); // NOI18N
        context.getDocument().insertString(lastParenPos + 1, ";", null); // NOI18N
        return lastParenPos + 2;
    }

    /**
     * Called to insert either single bracket or bracket pair. 
     *
     * @param context
     * @return relative caretOffset change
     * @throws BadLocationException
     */
    static int completeQuote(TypedTextInterceptor.MutableContext context) throws BadLocationException {
        if (isEscapeSequence(context)) {
            return -1;
        }
        // Examine token id at the caret offset
        TokenSequence<JavaTokenId> javaTS = javaTokenSequence(context, true);
        JavaTokenId id = (javaTS != null) ? javaTS.token().id() : null;


        // If caret within comment return false
        boolean caretInsideToken = (id != null)
                && (javaTS.offset() + javaTS.token().length() > context.getOffset()
                || javaTS.token().partType() == PartType.START);
        if (caretInsideToken && (id == JavaTokenId.BLOCK_COMMENT || id == JavaTokenId.JAVADOC_COMMENT || id == JavaTokenId.LINE_COMMENT)) {
            return -1;
        }

        boolean completablePosition = isQuoteCompletablePosition(context);
        boolean insideString = caretInsideToken
                && (id == JavaTokenId.STRING_LITERAL
                || id == JavaTokenId.CHAR_LITERAL);

        int lastNonWhite = org.netbeans.editor.Utilities.getRowLastNonWhite((BaseDocument) context.getDocument(), context.getOffset());
        // eol - true if the caret is at the end of line (ignoring whitespaces)
        boolean eol = lastNonWhite < context.getOffset();
        if (insideString) {
            if (eol) {
                return -1;
            } else {
                //#69524
                char chr = context.getDocument().getText(context.getOffset(), 1).charAt(0);
                if (chr == context.getText().charAt(0)) {
                    //#83044
                    if (context.getOffset() > 0) {
                        javaTS.move(context.getOffset() - 1);
                        if (javaTS.moveNext()) {
                            id = javaTS.token().id();
                            if (id == JavaTokenId.STRING_LITERAL || id == JavaTokenId.CHAR_LITERAL) {
                                context.setText("", 0); // NOI18N
                                return context.getOffset() + 1;
                            }
                        }
                    }
                }
            }
        }

        if ((completablePosition && !insideString) || eol) {
            context.setText(context.getText() + context.getText(), 1);
        }
        return -1;
    }

    private static boolean isQuoteCompletablePosition(TypedTextInterceptor.MutableContext context) throws BadLocationException {
        if (context.getOffset() == context.getDocument().getLength()) {
            return true;
        } else {
            for (int i = context.getOffset(); i < context.getDocument().getLength(); i++) {
                char chr = context.getDocument().getText(i, 1).charAt(0);
                if (chr == '\n') {
                    break;
                }
                if (!Character.isWhitespace(chr)) {
                    return (chr == ')' || chr == ',' || chr == '+' || chr == '}' || chr == ';');
                }

            }
            return false;
        }
    }

    private static boolean isEscapeSequence(TypedTextInterceptor.MutableContext context) throws BadLocationException {
        if (context.getOffset() <= 0) {
            return false;
        }

        char[] previousChars;
        for (int i = 2; context.getOffset() - i >= 0; i += 2) {
            previousChars = context.getDocument().getText(context.getOffset() - i, 2).toCharArray();
            if (previousChars[1] != '\\') {
                return false;
            }
            if (previousChars[0] != '\\') {
                return true;
            }
        }
        return context.getDocument().getText(context.getOffset() - 1, 1).charAt(0) == '\\';
    }

    private static boolean posWithinAnyQuote(TypedTextInterceptor.MutableContext context, TokenSequence<JavaTokenId> javaTS) throws BadLocationException {
        if (javaTS.token().id() == JavaTokenId.STRING_LITERAL || javaTS.token().id() == JavaTokenId.CHAR_LITERAL) {
            char chr = context.getDocument().getText(context.getOffset(), 1).charAt(0);
            return (context.getOffset() - javaTS.offset() == 1 || (chr != '"' && chr != '\''));
        }
        return false;
    }

    private static boolean isForLoopSemicolon(TokenSequence<JavaTokenId> ts) {
        int parenDepth = 0; // parenthesis depth
        int braceDepth = 0; // brace depth
        boolean semicolonFound = false; // next semicolon
        int tsOrigIndex = ts.index();
        try {
            while (ts.movePrevious()) {
                switch (ts.token().id()) {
                    case LPAREN:
                        if (parenDepth == 0) { // could be a 'for ('
                            while (ts.movePrevious()) {
                                switch (ts.token().id()) {
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
    
    private static Set<JavaTokenId> STOP_TOKENS_FOR_SKIP_CLOSING_BRACKET = EnumSet.of(JavaTokenId.LBRACE, JavaTokenId.RBRACE, JavaTokenId.SEMICOLON);

    private static boolean isSkipClosingBracket(TypedTextInterceptor.MutableContext context, TokenSequence<JavaTokenId> javaTS, JavaTokenId rightBracketId) {
        if (context.getOffset() == context.getDocument().getLength()) {
            return false;
        }

        boolean skipClosingBracket = false;

        if (javaTS != null && javaTS.token().id() == rightBracketId) {
            JavaTokenId leftBracketId = matching(rightBracketId);
            // Skip all the brackets of the same type that follow the last one
            do {
                if (STOP_TOKENS_FOR_SKIP_CLOSING_BRACKET.contains(javaTS.token().id())
                        || (javaTS.token().id() == JavaTokenId.WHITESPACE && javaTS.token().text().toString().contains("\n"))) {  // NOI18N
                    while (javaTS.token().id() != rightBracketId) {
                        boolean isPrevious = javaTS.movePrevious();
                        if (!isPrevious) {
                            break;
                        }
                    }
                    break;
                }
            } while (javaTS.moveNext());

            // token var points to the last bracket in a group of two or more right brackets
            // Attempt to find the left matching bracket for it
            // Search would stop on an extra opening left brace if found
            int braceBalance = 0; // balance of '{' and '}'
            int bracketBalance = -1; // balance of the brackets or parenthesis
            int numOfSemi = 0;
            boolean finished = false;
            while (!finished && javaTS.movePrevious()) {
                JavaTokenId id = javaTS.token().id();
                switch (id) {
                    case LPAREN:
                    case LBRACKET:
                        if (id == leftBracketId) {
                            bracketBalance++;
                            if (bracketBalance == 1) {
                                if (braceBalance != 0) {
                                    // Here the bracket is matched but it is located
                                    // inside an unclosed brace block
                                    // e.g. ... ->( } a()|)
                                    // which is in fact illegal but it's a question
                                    // of what's best to do in this case.
                                    // We chose to leave the typed bracket
                                    // by setting bracketBalance to 1.
                                    // It can be revised in the future.
                                    bracketBalance = 2;
                                }
                                finished = javaTS.offset() < context.getOffset();
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

                    case SEMICOLON:
                        numOfSemi++;
                        break;
                }
            }

            if (bracketBalance == 1 && numOfSemi < 2) {
                finished = false;
                while (!finished && javaTS.movePrevious()) {
                    switch (javaTS.token().id()) {
                        case WHITESPACE:
                        case LINE_COMMENT:
                        case BLOCK_COMMENT:
                        case JAVADOC_COMMENT:
                            break;
                        case FOR:
                            bracketBalance--;
                        default:
                            finished = true;
                            break;
                    }
                }
            }

            skipClosingBracket = bracketBalance != 1;
        }
        return skipClosingBracket;

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
                throw new IllegalArgumentException("Not a bracket char '" + bracket + '\'');  // NOI18N
        }
    }

    private static int tokenBalance(Document doc, JavaTokenId leftTokenId) {
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

    private static TokenSequence<JavaTokenId> javaTokenSequence(TypedTextInterceptor.MutableContext context, boolean backwardBias) {
        return javaTokenSequence(context.getDocument(), context.getOffset(), backwardBias);
    }

    private static TokenSequence<JavaTokenId> javaTokenSequence(DeletedTextInterceptor.Context context, boolean backwardBias) {
        return javaTokenSequence(context.getDocument(), context.getOffset(), backwardBias);
    }

    /**
     * Get token sequence positioned over a token.
     *
     * @param doc
     * @param caretOffset
     * @param backwardBias
     * @return token sequence positioned over a token that "contains" the offset
     * or null if the document does not contain any java token sequence or the
     * offset is at doc-or-section-start-and-bwd-bias or
     * doc-or-section-end-and-fwd-bias.
     */
    private static TokenSequence<JavaTokenId> javaTokenSequence(Document doc, int caretOffset, boolean backwardBias) {
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        List<TokenSequence<?>> tsList = hi.embeddedTokenSequences(caretOffset, backwardBias);
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
}
