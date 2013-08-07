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
package org.netbeans.modules.html.editor.typinghooks;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import junit.framework.TestCase;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.test.TestBase;

/**
 *
 * @author marekfukala
 */
public class HtmlTypingHooksTest extends TestBase {

    public HtmlTypingHooksTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HtmlTypedTextInterceptor.adjust_quote_type_after_eq = false;
    }

    public void testHandleEmptyTagCloseSymbol()  {
       Context ctx = new Context(new HtmlKit(), "<div|");
       ctx.typeChar('/');
       ctx.assertDocumentTextEquals("<div/>|");
       ctx.typeChar('>');
       ctx.assertDocumentTextEquals("<div/>|");
    }
    
    public void testHandleEmptyTagCloseSymbolAfterWS()  {
       Context ctx = new Context(new HtmlKit(), "<div |");
       ctx.typeChar('/');
       ctx.assertDocumentTextEquals("<div />|");
       ctx.typeChar('>');
       ctx.assertDocumentTextEquals("<div />|");
    }
    
    public void testHandleEmptyTagCloseSymbolAfterAttribute()  {
       Context ctx = new Context(new HtmlKit(), "<div align='center'|");
       ctx.typeChar('/');
       ctx.assertDocumentTextEquals("<div align='center'/>|");
       ctx.typeChar('>');
       ctx.assertDocumentTextEquals("<div align='center'/>|");
    }
    
    public void testQuoteAutocompletionInHtmlAttribute() {
        Context ctx = new Context(new HtmlKit(), "<a href=\"javascript:bigpic(|)\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a href=\"javascript:bigpic(\"|)\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a href=\"javascript:bigpic(\"\"|)\">");
    }

    public void testSkipClosingQuoteInEmptyAttr() {
        Context ctx = new Context(new HtmlKit(), "<a href=\"|\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a href=\"\"|>");
    }

     public void testSkipClosingQuoteInNonEmpty() {
        Context ctx = new Context(new HtmlKit(), "<a href=\"x|\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a href=\"x\"|>");
    }
     
     public void testSkipClosingQuoteInEmptyClassAndId() {
        Context ctx = new Context(new HtmlKit(), "<a class=\"|\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a class=\"\"|>");
        
        ctx = new Context(new HtmlKit(), "<a id=\"|\">");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a id=\"\"|>");
    }

     //XXX fix me - css intercepts here
//     public void testSkipClosingQuoteInNonEmptyClassAndId() {
//        Context ctx = new Context(new HtmlKit(), "<a class=\"xx|\">");
//        ctx.typeChar('"');
//        ctx.assertDocumentTextEquals("<a class=\"xx\"|>");
//        
//        ctx = new Context(new HtmlKit(), "<a id=\"yy|\">");
//        ctx.typeChar('"');
//        ctx.assertDocumentTextEquals("<a id=\"yy\"|>");
//    }
     
     //XXX fixme - <div + "> => will autopopup the closing tag, but once completed,
     //the closing tag is not indented properly -- fix in HtmlTypedBreakInterceptor
      
    public void testDoubleQuoteAutocompleteAfterEQ() {
        Context ctx = new Context(new HtmlKit(), "<a href|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a href=\"|\"");
        ctx.typeChar('v');
        ctx.typeChar('a');
        ctx.typeChar('l');
        ctx.assertDocumentTextEquals("<a href=\"val|\"");
    }

    public void testDoubleQuoteAutocompleteAfterEQInCSSAttribute() {
        Context ctx = new Context(new HtmlKit(), "<a class|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a class=\"|\"");
        ctx.typeChar('v');
        ctx.typeChar('a');
        ctx.typeChar('l');
        ctx.assertDocumentTextEquals("<a class=\"val|\"");
    }

    public void testDoubleQuoteAfterQuotedClassAttribute() {
        Context ctx = new Context(new HtmlKit(), "<a class=\"val|");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a class=\"val\"|");
    }

    public void testDoubleQuoteAfterUnquotedClassAttribute() {
        Context ctx = new Context(new HtmlKit(), "<a class=val|");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a class=val\"|");
    }

    public void testSingleQuoteAutocompleteAfterEQ() {
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

    public void testQuoteChange() {
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

    public void testTypeSingleQuoteInUnquoteClassAttr() {
        Context ctx = new Context(new HtmlKit(), "<a class=|");
        ctx.typeChar('v');
        ctx.typeChar('a');
        ctx.typeChar('l');
        ctx.assertDocumentTextEquals("<a class=val|");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a class=val'|");
    }
    
    public void testAutocompleteDoubleQuoteOnlyAfterEQ() {
        Context ctx = new Context(new HtmlKit(), "<a align|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a align=\"|\"");
        ctx.typeChar('x');
        ctx.assertDocumentTextEquals("<a align=\"x|\"");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a align=\"x\"|");
        ctx.typeChar('"');
        ctx.assertDocumentTextEquals("<a align=\"x\"\"|");
    }


    //XXX fix me - css intercepts here
//    public void testAutocompleteDoubleQuoteOnlyAfterEQInClass() {
//        Context ctx = new Context(new HtmlKit(), "<a class|");
//        ctx.typeChar('=');
//        ctx.assertDocumentTextEquals("<a class=\"|\"");
//        ctx.typeChar('x');
//        ctx.assertDocumentTextEquals("<a class=\"x|\"");
//        ctx.typeChar('"');
//        ctx.assertDocumentTextEquals("<a class=\"x\"|");
//        ctx.typeChar('"');
//        ctx.assertDocumentTextEquals("<a class=\"x\"\"|");
//    }

    public void testAutocompleteSingleQuoteOnlyAfterEQ() {
        Context ctx = new Context(new HtmlKit(), "<a align|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a align=\"|\"");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a align='|'");
        ctx.typeChar('x');
        ctx.assertDocumentTextEquals("<a align='x|'");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a align='x'|");
        ctx.typeChar('\'');
        ctx.assertDocumentTextEquals("<a align='x''|");
    }

    //XXX fix me - css intercepts here
//     public void testAutocompleteSingleQuoteOnlyAfterEQInClass() {
//        Context ctx = new Context(new HtmlKit(), "<a class|");
//        ctx.typeChar('=');
//        ctx.assertDocumentTextEquals("<a class=\"|\"");
//        ctx.typeChar('\'');
//        ctx.assertDocumentTextEquals("<a class='|'");
//        ctx.typeChar('x');
//        ctx.assertDocumentTextEquals("<a class='x|'");
//        ctx.typeChar('\'');
//        ctx.assertDocumentTextEquals("<a class='x'|");
//        ctx.typeChar('\'');
//        ctx.assertDocumentTextEquals("<a class='x''|");
//    }

    
    public void testDeleteAutocompletedQuote() {
        Context ctx = new Context(new HtmlKit(), "<a class|");
        ctx.typeChar('=');
        ctx.assertDocumentTextEquals("<a class=\"|\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=");
    }

    public void testDeleteQuote() {
        Context ctx = new Context(new HtmlKit(), "<a class=\"|\"");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=");
    }

    public void testDeleteQuoteWithWSAfter() {
        Context ctx = new Context(new HtmlKit(), "<a class=\"|\" ");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class= ");
    }

    public void testDeleteSingleQuote() {
        Context ctx = new Context(new HtmlKit(), "<a class='|'");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=");

        //but do not delete if there's a text after the caret
        ctx = new Context(new HtmlKit(), "<a class='|x'");
        ctx.typeChar('\b');
        ctx.assertDocumentTextEquals("<a class=x'");

    }

    public void testDoNotAutocompleteQuoteInValue() {
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

    public void testInClassDoNotAutocompleteQuoteInValue() {
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

    public void testAdjustQuoteTypeAfterEQ()  {
        HtmlTypedTextInterceptor.adjust_quote_type_after_eq = true;
        try {
            //default type
            assertEquals('"', HtmlTypedTextInterceptor.default_quote_char_after_eq);

            Context ctx = new Context(new HtmlKit(), "<a class|");
            ctx.typeChar('=');
            ctx.assertDocumentTextEquals("<a class=\"|\"");
            ctx.typeChar('\'');

            //now should be switched to single quote type
            assertEquals('\'', HtmlTypedTextInterceptor.default_quote_char_after_eq);

            ctx = new Context(new HtmlKit(), "<a class|");
            ctx.typeChar('=');
            ctx.assertDocumentTextEquals("<a class='|'");
            ctx.typeChar('"');
            
            //now should be switched back to the default double quote type
            assertEquals('"', HtmlTypedTextInterceptor.default_quote_char_after_eq);

        } finally {
            HtmlTypedTextInterceptor.adjust_quote_type_after_eq = false;
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

        public void typeText(String text) {
            for (int i = 0; i < text.length(); i++) {
                typeChar(text.charAt(i));
            }
        }

        public void assertDocumentTextEquals(final String textWithPipe) {
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

    }
}
