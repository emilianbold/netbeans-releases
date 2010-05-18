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

package org.netbeans.modules.editor.java;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import junit.framework.TestCase;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;


/**
 * Test java block comment completion.
 * Emulates real typing and tests the resulting state of the document.
 * 
 * Based on JavaBraceCompletionUnitTest
 *
 * Issue #84764
 *
 * @autor Marek Slama
 */
public class JavaBlockCommentCompletionUnitTest extends NbTestCase {

    public JavaBlockCommentCompletionUnitTest(String testMethodName) {
        super(testMethodName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    /**
     * Close block comment
     */
    public void testCompleteBlockComment1 () {
        Context ctx = new Context(new JavaKit(),
                "/*|"
        );
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "/*\n" +
                " * |\n" +
                " */"
        );
    }

    /**
     * Do not close block comment
     */
    public void testCompleteBlockComment2 () {
        Context ctx = new Context(new JavaKit(),
                "/*|\n" +
                " */"
        );
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "/*\n" +
                " * |\n" +
                " */"
        );
    }

    /**
     * Close block comment
     */
    public void testCompleteBlockComment3 () {
        Context ctx = new Context(new JavaKit(),
                "/*|\n" +
                "/*\n" +
                " */"
        );
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "/*\n" +
                " * |\n" +
                " */\n" +
                "/*\n" +
                " */"
        );
    }

    /**
     * Close block comment
     */
    public void testCompleteBlockComment4 () {
        Context ctx = new Context(new JavaKit(),
                "/*|\n" +
                "\n" +
                "/*\n" +
                " */"
        );
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "/*\n" +
                " * |\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " */"
        );
    }

    /**
     * Do not close block comment
     */
    public void testCompleteBlockComment5 () {
        Context ctx = new Context(new JavaKit(),
                "/*| a"
        );
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "/*\n" +
                " |a"
        );
    }

    /**
     * Close block comment
     */
    public void testCompleteBlockComment6 () {
        Context ctx = new Context(new JavaKit(),
                "a/*|"
        );
        ctx.typeChar('\n');
        ctx.assertDocumentTextEquals(
                "a/*\n" +
                "  * |\n" +
                "  */"
        );
    }
    
    private static final class Context {
        
        private JEditorPane pane;

        public Context(final EditorKit kit, final String textWithPipe) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        pane = new JEditorPane();
                        pane.setEditorKit(kit);
                        Document doc = pane.getDocument();
                        // Required by Java's default key typed
                        doc.putProperty(Language.class, JavaTokenId.language());
                        doc.putProperty("mimeType", "text/x-java");
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
            } catch (Exception e) {
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
                    public void run() {
                        KeyEvent keyEvent;
                        if (ch != '\n') {
                            keyEvent = new KeyEvent(pane, KeyEvent.KEY_TYPED,
                                EventQueue.getMostRecentEventTime(),
                                0, KeyEvent.VK_UNDEFINED, ch);
                        } else { // Simulate pressing of Enter
                            keyEvent = new KeyEvent(pane, KeyEvent.KEY_PRESSED,
                                EventQueue.getMostRecentEventTime(),
                                0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
                        }
                        SwingUtilities.processKeyBindings(keyEvent);
                    }
                });
            } catch (Exception e) {
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
                                TestCase.fail("Invalid document text - diff at index " + diffIndex +
                                        "\nExpected: \"" + text +
                                        "\"\n  Actual: \"" + docText + "\""
                                );
                            }
                        } catch (BadLocationException e) {
                            throw new IllegalStateException(e);
                        }
                        if (caretOffset != -1) {
                            TestCase.assertEquals("Invalid caret offset", caretOffset, pane.getCaretPosition());
                        }
                    }
                });
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        public String dumpDocumentText() {
            final StringBuffer sb = new StringBuffer();
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        try {
                            String text = document().getText(0, document().getLength());
                            text = CharSequenceUtilities.debugText(text);
                            sb.append(text);
                        } catch (BadLocationException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                });
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return sb.toString();
        }
    }

}
