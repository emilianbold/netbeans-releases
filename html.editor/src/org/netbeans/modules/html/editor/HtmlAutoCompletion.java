/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.html.editor;

import javax.swing.SwingUtilities;
import javax.swing.text.Position;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.css.formatting.api.LexUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.html.editor.xhtml.XhtmlElTokenId;
import org.openide.util.Exceptions;

/**
 * This static class groups the whole aspect of bracket
 * completion. It is defined to clearly separate the functionality
 * and keep actions clean.
 * The methods of the class are called from different actions as
 * KeyTyped, DeletePreviousChar.
 */
public class HtmlAutoCompletion {

    private static DocumentInsertIgnore insertIgnore;

    /** Hook for before char inserted actions
     *
     * <b>Runs under document atomic lock.</b>
     * <b>Always runs in AWT.</b>
     *
     * @return false if the char should be inserted, true otherwise
     */
    public static boolean beforeCharInserted(BaseDocument doc,
            int dotPos,
            Caret caret,
            char ch) throws BadLocationException {
        try {
            if (insertIgnore != null) {
                if (insertIgnore.getOffset() == dotPos && insertIgnore.getChar() == ch) {
                    //move the caret to specified position if needed
                    if(insertIgnore.getMoveCaretTo() != -1) {
                        caret.setDot(insertIgnore.moveCaretTo);
                        //also close the completion window
                        Completion.get().hideAll();
                    }
                    return true;
                }
            }
        } finally {
            insertIgnore = null;
        }

        return false;

    }

    /**
     * A hook method called after a character was inserted into the
     * document. The function checks for special characters for
     * completion ()[]'"{} and other conditions and optionally performs
     * changes to the doc and or caret (complets braces, moves caret,
     * etc.)
     *
     * <b>Runs under document atomic lock.</b>
     * <b>Always runs in AWT.</b>
     *
     * @param doc the document where the change occurred
     * @param dotPos position of the character insertion
     * @param caret caret
     * @param ch the character that was inserted
     * @throws BadLocationException
     */
    public static void charInserted(BaseDocument doc,
            int dotPos,
            Caret caret,
            char ch) throws BadLocationException {
        if (ch == '=') { //NOI18N
            if(HtmlPreferences.autocompleteQuotesAfterEqualSign()) {
                completeQuotes(doc, dotPos, caret);
            }
        } else if (ch == '"') { //NOI18N
            //user has pressed quotation mark
            if(HtmlPreferences.autocompleteQuotes()) {
                handleQuotationMark(doc, dotPos, caret);
            }
        } else if (ch == '{') { //NOI18N
            //user has pressed quotation mark
            handleEL(doc, dotPos, caret);
        } else if (ch == '/') { //NOI18N
            handleEmptyTagCloseSymbol(doc, dotPos, caret);
        } else if (ch == '>') { //NOI18N
            handleTagClosingSymbol(doc, dotPos, ch);
        }
    }

    private static void handleTagClosingSymbol(final BaseDocument doc, final int dotPos, char lastChar)
            throws BadLocationException {

        TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.get(doc);
        for (final LanguagePath languagePath : tokenHierarchy.languagePaths()) {
            if (languagePath.innerLanguage() == HTMLTokenId.language()) {
                TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence((BaseDocument) doc, dotPos, HTMLTokenId.language());
                if (ts == null) {
                    return;
                }
                ts.move(dotPos);
                boolean found = false;
                while (ts.movePrevious()) {
                    if (ts.token().id() == HTMLTokenId.TAG_OPEN_SYMBOL) {
                        found = true;
                        break;
                    }
                    if (ts.token().id() != HTMLTokenId.ARGUMENT &&
                            ts.token().id() != HTMLTokenId.OPERATOR &&
                            ts.token().id() != HTMLTokenId.VALUE &&
                            ts.token().id() != HTMLTokenId.VALUE_CSS &&
                            ts.token().id() != HTMLTokenId.VALUE_JAVASCRIPT &&
                            ts.token().id() != HTMLTokenId.WS &&
                            ts.token().id() != HTMLTokenId.TAG_CLOSE &&
                            ts.token().id() != HTMLTokenId.TAG_OPEN) {
                        break;
                    }
                }
                if (!found) {
                    return;
                }
                try {
                    int lineStart = Utilities.getRowFirstNonWhite((BaseDocument) doc, ts.offset());
                    if (lineStart != ts.offset()) {
                        return;
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

                //ok, the user just type tag closing symbol, lets reindent the line
                //since the code runs under document atomic lock, we cannot lock the
                //indentation infrastructure directly. Instead of that create a new
                //AWT task and post it for later execution.
                final Position from = doc.createPosition(Utilities.getRowStart(doc, dotPos));
                final Position to = doc.createPosition(Utilities.getRowEnd(doc, dotPos));
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        final Indent indent = Indent.get(doc);
                        indent.lock();
                        try {
                            doc.runAtomic(new Runnable() {

                                public void run() {
                                    try {
                                        indent.reindent(from.getOffset(), to.getOffset());
                                    } catch (BadLocationException ex) {
                                        //ignore
                                    }
                                }
                            });
                        } finally {
                            indent.unlock();
                        }
                    }
                });

                return; //exit the loop
            }
        }




    }

    private static void handleEmptyTagCloseSymbol(final BaseDocument doc, final int dotPos, final Caret caret) throws BadLocationException {
        TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence((BaseDocument) doc, dotPos, HTMLTokenId.language());
        if (ts == null) {
            return; //no html ts at the caret position
        }
        ts.move(dotPos);
        if (!ts.moveNext()) {
            return; //no token
        }

        Token<HTMLTokenId> token = ts.token();
        if (token.id() == HTMLTokenId.ERROR) {
            if (ts.movePrevious() && (ts.token().id() == HTMLTokenId.TAG_OPEN ||
                    ts.token().id() == HTMLTokenId.WS ||
                    ts.token().id() == HTMLTokenId.VALUE)) {
                // slash typed just after open tag name => autocomplete the > symbol
                doc.insertString(dotPos + 1, ">", null);

                //ignore next &gt; char if typed
                insertIgnore = new DocumentInsertIgnore(dotPos + 2, '>', -1);
            }
        }
    }

    private static void handleQuotationMark(final BaseDocument doc, final int dotPos, final Caret caret) throws BadLocationException {
        //test whether the user typed an ending quotation in the attribute value
        TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence((BaseDocument) doc, dotPos, HTMLTokenId.language());
        if (ts == null) {
            return; //no html ts at the caret position
        }
        int diff = ts.move(dotPos);
        if (!ts.moveNext()) {
            return; //no token
        }

        Token<HTMLTokenId> token = ts.token();
        if (token.id() == HTMLTokenId.VALUE) {
            //test if the user inserted the qutation in an attribute value and before
            //an already existing end quotation
            //the text looks following in such a situation:
            //
            //  atrname="abcd|"", where offset of the | == dotPos
            try {
                if ("\"\"".equals(doc.getText(dotPos, 2))) {
                    doc.remove(dotPos, 1);
                    caret.setDot(dotPos + 1);
                } else if (diff == 0 && token.text().charAt(0) == '"') {
                    //user typed quation just after equal sign after tag attribute name => complete the second quote
                    doc.insertString(dotPos, "\"", null);
                    caret.setDot(dotPos + 1);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static void completeQuotes(final BaseDocument doc, final int dotPos, final Caret caret) {
        TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence((BaseDocument) doc, dotPos, HTMLTokenId.language());
        if (ts == null) {
            return; //no html ts at the caret position
        }
        ts.move(dotPos);
        if (!ts.moveNext()) {
            return; //no token
        }

        Token<HTMLTokenId> token = ts.token();

        int dotPosAfterTypedChar = dotPos + 1;
        if (token != null && token.id() == HTMLTokenId.OPERATOR) {
            try {
                doc.insertString(dotPosAfterTypedChar, "\"\"", null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            caret.setDot(dotPosAfterTypedChar + 1);
        }
    }

    //autocomplete ${ "}" and moves the caret inside the brackets
    private static void handleEL(final BaseDocument doc, final int dotPos, final Caret caret) {
        TokenHierarchy<?> th = TokenHierarchy.get(doc);
        TokenSequence<XhtmlElTokenId> ts = th.tokenSequence(XhtmlElTokenId.language());
        if (ts == null) {
            return;
        }
        int diff = ts.move(dotPos);
        if (diff == 0) {
            return; // ${ - the token diff must be > 0
        }

        if (!ts.moveNext()) {
            return; //no token
        }

        Token token = ts.token();
        int dotPosAfterTypedChar = dotPos + 1;
        if (token.id() == XhtmlElTokenId.EL) {
            char charBefore = token.text().charAt(diff - 1);
            if (charBefore == '$' || charBefore == '#') {
                try {
                    doc.insertString(dotPosAfterTypedChar, "}", null);
                    caret.setDot(dotPosAfterTypedChar);
                    //open completion
                    Completion.get().showCompletion();

                    //ignore '}' char
                    insertIgnore = new DocumentInsertIgnore(dotPosAfterTypedChar, '}', dotPosAfterTypedChar + 1);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        }
    }

    private static class DocumentInsertIgnore {

        private int offset;
        private char ch;
        private int moveCaretTo;

        public DocumentInsertIgnore(int offset, char ch, int moveCaretTo) {
            this.offset = offset;
            this.ch = ch;
            this.moveCaretTo = moveCaretTo;
        }

        public char getChar() {
            return ch;
        }

        public int getOffset() {
            return offset;
        }

        public int getMoveCaretTo() {
            return moveCaretTo;
        }
    }
}
