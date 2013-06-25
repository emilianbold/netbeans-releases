/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import junit.framework.TestCase;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.test.TestBase;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;

/**
 *
 * @author marekfukala
 */
public class HtmlAutoCompletionTest extends TestBase {

    public HtmlAutoCompletionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HtmlAutoCompletion.adjust_quote_type_after_eq = false;
    }

    public void testHandleEmptyTagCloseSymbol() throws InterruptedException, InvocationTargetException, Exception {
        final JEditorPane pane = Mutex.EVENT.readAccess(new Mutex.Action<JEditorPane>() {
            @Override
            public JEditorPane run() {
                try {
                    return getPane("");
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            }
        });

        final BaseDocument doc = (BaseDocument) pane.getDocument();
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    doc.insertString(0, "<div/", null);
                    //                   01234
                    HtmlAutoCompletion.charInserted(doc, 4, pane.getCaret(), '/');
                    assertEquals("<div/>", doc.getText(0, doc.getLength()));

                    doc.remove(0, doc.getLength());
                    doc.insertString(0, "<div /", null);
                    //                   01234
                    HtmlAutoCompletion.charInserted(doc, 5, pane.getCaret(), '/');
                    assertEquals("<div />", doc.getText(0, doc.getLength()));

                    doc.remove(0, doc.getLength());
                    doc.insertString(0, "<div align='center'/", null);
                    //                   012345678901234567890
                    HtmlAutoCompletion.charInserted(doc, 19, pane.getCaret(), '/');
                    assertEquals("<div align='center'/>", doc.getText(0, doc.getLength()));
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    public void testQuoteAutocompletionInHtmlAttribute() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a href=\"javascript:bigpic(|)\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a href=\"javascript:bigpic(\"|)\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a href=\"javascript:bigpic(\"\"|)\">");
    }

    public void testSkipClosingQuote() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a href=\"|\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a href=\"\"|>");
    }

    public void testDoubleQuoteAutocompleteAfterEQ() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a href|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a href=\"|\"");
        ctx.typeChar('v');
        ctx.typeChar('a');
        ctx.typeChar('l');
        ctx.assertDocumentTextEquals("<a href=\"val|\"");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a href=\"val\"|");
    }

    public void testDoubleQuoteAutocompleteAfterEQInCSSAttribute() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a class|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a class=\"|\"");
        ctx.typeChar('v');
        ctx.typeChar('a');
        ctx.typeChar('l');
        ctx.assertDocumentTextEquals("<a class=\"val|\"");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a class=\"val\"|");
    }

    public void testDoubleQuoteAfterQuotedClassAttribute() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a class=\"val|");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a class=\"val\"|");
    }

    public void testDoubleQuoteAfterUnquotedClassAttribute() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a class=val|");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a class=val\"|");
    }

    public void testSingleQuoteAutocompleteAfterEQ() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a href=|");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a href='|'");
        ctx.typeChar('v');
        ctx.typeChar('a');
        ctx.typeChar('l');
        ctx.assertDocumentTextEquals("<a href='val|'");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a href='val'|");
    }

    public void testQuoteChange() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a href|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a href=\"|\"");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a href='|'");
        ctx.typeChar('v');
        ctx.typeChar('a');
        ctx.typeChar('l');
        ctx.assertDocumentTextEquals("<a href='val|'");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a href='val'|");
    }

    public void testTypeSingleQuoteInUnquoteClassAttr() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a class=|");
        ctx.typeChar('v');
        ctx.typeChar('a');
        ctx.typeChar('l');
        ctx.assertDocumentTextEquals("<a class=val|");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a class=val'|");
    }

    public void testAutocompleteDoubleQuoteOnlyAfterEQ() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a class|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a class=\"|\"");
        ctx.typeChar('x');
        ctx.assertDocumentTextEquals("<a class=\"x|\"");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a class=\"x\"|");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a class=\"x\"\"|");
    }

    public void testAutocompleteSingleQuoteOnlyAfterEQ() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a class|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a class=\"|\"");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a class='|'");
        ctx.typeChar('x');
        ctx.assertDocumentTextEquals("<a class='x|'");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a class='x'|");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a class='x''|");
    }

    public void testDeleteAutocompletedQuote() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a class|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a class=\"|\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=");
    }

    public void testDeleteQuote() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a class=\"|\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=");
    }

    public void testDeleteQuoteWithWSAfter() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a class=\"|\" ");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class= ");
    }

    public void testDeleteSingleQuote() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a class='|'");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=");

        //but do not delete if there's a text after the caret
        ctx = new Context(new HtmlKit(), "<a class='|x'");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=x'");

    }

    public void testDoNotAutocompleteQuoteInValue() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a x=\"|test\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a x=|test\"");
        ctx.typeChar('"');

        //do not autocomplete in this case
        ctx.assertDocumentTextEquals("<a x=\"|test\"");

        //different quotes
        ctx = new Context(new HtmlKit(), "<a x=\"|test\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a x=|test\"");
        ctx.typeChar('\'');

        //do not autocomplete in this case
        ctx.assertDocumentTextEquals("<a x=\'|test\"");

        //no closing quote
        ctx = new Context(new HtmlKit(), "<a x=\"|test");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a x=|test");
        ctx.typeChar('\'');

        //do not autocomplete in this case
        ctx.assertDocumentTextEquals("<a x=\'|test");

    }

    public void testInClassDoNotAutocompleteQuoteInValue() throws InterruptedException, InvocationTargetException, Exception {
        Context ctx = new Context(new HtmlKit(), "<a class=\"|test\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=|test\"");
        ctx.typeChar('"');

        //do not autocomplete in this case
        ctx.assertDocumentTextEquals("<a class=\"|test\"");

        //different quotes
        ctx = new Context(new HtmlKit(), "<a class=\"|test\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=|test\"");
        ctx.typeChar('\'');

        //do not autocomplete in this case
        ctx.assertDocumentTextEquals("<a class=\'|test\"");

        //no closing quote
        ctx = new Context(new HtmlKit(), "<a class=\"|test");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=|test");
        ctx.typeChar('\'');

        //do not autocomplete in this case
        ctx.assertDocumentTextEquals("<a class=\'|test");

    }

    public void testAdjustQuoteTypeAfterEQ() throws InterruptedException, InvocationTargetException, Exception {
        HtmlAutoCompletion.adjust_quote_type_after_eq = true;
        try {
            //default type
            assertEquals('"', HtmlAutoCompletion.default_quote_char_after_eq);

            Context ctx = new Context(new HtmlKit(), "<a class|");
            ctx.typeChar('=');
            ctx.assertDocumentTextEquals("<a class=\"|\"");
            ctx.typeChar('\'');

            //now should be switched to single quote type
            assertEquals('\'', HtmlAutoCompletion.default_quote_char_after_eq);

            ctx = new Context(new HtmlKit(), "<a class|");
            ctx.typeChar('=');
            ctx.assertDocumentTextEquals("<a class='|'");
            ctx.typeChar('"');
            
            //now should be switched back to the default double quote type
            assertEquals('"', HtmlAutoCompletion.default_quote_char_after_eq);

        } finally {
            HtmlAutoCompletion.adjust_quote_type_after_eq = false;
        }
    }

    private static final class Context {

        private JEditorPane pane;

        public Context(final EditorKit kit, final String textWithPipe) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        pane = new JEditorPane();
                        pane.setEditorKit(kit);
                        Document doc = pane.getDocument();
                        // Required by Java's default key typed
                        doc.putProperty(Language.class, HTMLTokenId.language());
                        doc.putProperty("mimeType", "text/html");
                        int caretOffset = textWithPipe.indexOf('|');
                        String text;
                        if (caretOffset != -1) {
                            text = textWithPipe.substring(0, caretOffset) + textWithPipe.substring(caretOffset + 1);
                        } else {
                            text = textWithPipe;
                        }
                        pane.setText(text);
                        pane.setCaretPosition((caretOffset != -1) ? caretOffset : doc.getLength());
                    }
                });
            } catch (InterruptedException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }

        public JEditorPane pane() {
            return pane;
        }

        public Document document() {
            return pane.getDocument();
        }

        public void typeChar(final char ch) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        KeyEvent keyEvent;
                        switch (ch) {
                            case '\n':
                                keyEvent = new KeyEvent(pane, KeyEvent.KEY_PRESSED,
                                        EventQueue.getMostRecentEventTime(),
                                        0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED); // Simulate pressing of Enter
                                break;
                            case '\b':
                                keyEvent = new KeyEvent(pane, KeyEvent.KEY_PRESSED,
                                        EventQueue.getMostRecentEventTime(),
                                        0, KeyEvent.VK_BACK_SPACE, KeyEvent.CHAR_UNDEFINED); // Simulate pressing of BackSpace
                                break;
                            case '\f':
                                keyEvent = new KeyEvent(pane, KeyEvent.KEY_PRESSED,
                                        EventQueue.getMostRecentEventTime(),
                                        0, KeyEvent.VK_DELETE, KeyEvent.CHAR_UNDEFINED); // Simulate pressing of Delete
                                break;
                            default:
                                keyEvent = new KeyEvent(pane, KeyEvent.KEY_TYPED,
                                        EventQueue.getMostRecentEventTime(),
                                        0, KeyEvent.VK_UNDEFINED, ch);
                        }
                        SwingUtilities.processKeyBindings(keyEvent);
                    }
                });
            } catch (InterruptedException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }

        public void typeText(String text) {
            for (int i = 0; i < text.length(); i++) {
                typeChar(text.charAt(i));
            }
        }

        public void assertDocumentTextEquals(final String textWithPipe) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        int caretOffset = textWithPipe.indexOf('|');
                        String text;
                        if (caretOffset != -1) {
                            text = textWithPipe.substring(0, caretOffset) + textWithPipe.substring(caretOffset + 1);
                        } else {
                            text = textWithPipe;
                        }
                        try {
                            // Use debug text to prefix special chars for easier readability
                            text = CharSequenceUtilities.debugText(text);
                            String docText = document().getText(0, document().getLength());
                            docText = CharSequenceUtilities.debugText(docText);
                            if (!text.equals(docText)) {
                                int diffIndex = 0;
                                int minLen = Math.min(docText.length(), text.length());
                                while (diffIndex < minLen) {
                                    if (text.charAt(diffIndex) != docText.charAt(diffIndex)) {
                                        break;
                                    }
                                    diffIndex++;
                                }
                                TestCase.fail("Invalid document text - diff at index " + diffIndex
                                        + "\nExpected: \"" + text
                                        + "\"\n  Actual: \"" + docText + "\"");
                            }
                        } catch (BadLocationException e) {
                            throw new IllegalStateException(e);
                        }
                        if (caretOffset != -1) {
                            TestCase.assertEquals("Invalid caret offset", caretOffset, pane.getCaretPosition());
                        }
                    }
                });
            } catch (InterruptedException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
