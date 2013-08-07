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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.typinghooks;

import java.io.IOException;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import junit.framework.TestSuite;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.csl.PHPBracketCompleter;
import org.netbeans.modules.php.editor.indent.PHPFormatter;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.csl.PHPNavTestBase;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Jan Lahoda
 */
public class GeneratingBracketCompleterTest extends PHPNavTestBase {

    public GeneratingBracketCompleterTest(String testName) {
        super(testName);
    }

    public static TestSuite suite() {
        TestSuite ts = new TestSuite();

        ts.addTest(new GeneratingBracketCompleterTest("testFoo"));

        return ts;
    }

    public void testFoo() throws Exception {}

    public void testFunctionDocumentationParam() throws Exception {
        performInsertBreak( "<?php\n" +
                            "/**^\n" +
                            "function foo($i) {\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "/**\n" +
                            " * ^\n" +
                            " * @param " + GeneratingBracketCompleter.TYPE_PLACEHOLDER + " $i\n" +
                            " */\n" +
                            "function foo($i) {\n" +
                            "}\n" +
                            "?>\n");
    }

    public void testFunctionDocumentationGlobalVar() throws Exception {
        performInsertBreak( "<?php\n" +
                            "$r = 1;\n" +
                            "/**^\n" +
                            "function foo() {\n" +
                            "    global $r;\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "$r = 1;\n" +
                            "/**\n" +
                            " * ^\n" +
                            " * @global " + GeneratingBracketCompleter.TYPE_PLACEHOLDER + " $r\n" +
                            " */\n" +
                            "function foo() {\n" +
                            "    global $r;\n" +
                            "}\n" +
                            "?>\n");
    }

    public void testFunctionDocumentationStaticVar() throws Exception {
        performInsertBreak( "<?php\n" +
                            "/**^\n" +
                            "function foo() {\n" +
                            "    static $r;\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "/**\n" +
                            " * ^\n" +
                            " * @staticvar " + GeneratingBracketCompleter.TYPE_PLACEHOLDER + " $r\n" +
                            " */\n" +
                            "function foo() {\n" +
                            "    static $r;\n" +
                            "}\n" +
                            "?>\n");
    }

    public void testFunctionDocumentationReturn() throws Exception {
        performInsertBreak( "<?php\n" +
                            "/**^\n" +
                            "function foo() {\n" +
                            "    return \"\";\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "/**\n" +
                            " * ^\n" +
                            " * @return " + GeneratingBracketCompleter.TYPE_PLACEHOLDER + "\n" +
                            " */\n" +
                            "function foo() {\n" +
                            "    return \"\";\n" +
                            "}\n" +
                            "?>\n");
    }

    public void testGlobalVariableDocumentation() throws Exception {
        performInsertBreak( "<?php\n" +
                            "/**^\n" +
                            "$GLOBALS['test'] = \"\";\n" +
                            "?>\n",
                            "<?php\n" +
                            "/**\n" +
                            " * ^\n" +
                            " * @global " + GeneratingBracketCompleter.TYPE_PLACEHOLDER + " $GLOBALS['test']\n" +
                            " * @name $test\n" +
                            " */\n" +
                            "$GLOBALS['test'] = \"\";\n" +
                            "?>\n");
    }

    public void testFieldDocumentation() throws Exception {
        performInsertBreak( "<?php\n" +
                            "class foo {\n" +
                            "    /**^\n" +
                            "    var $bar = \"\";\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "class foo {\n" +
                            "    /**\n" +
                            "     * ^\n" +
                            "     * @var " + GeneratingBracketCompleter.TYPE_PLACEHOLDER + "\n" +
                            "     */\n" +
                            "    var $bar = \"\";\n" +
                            "}\n" +
                            "?>\n");
    }

    public void testMethodDocumentation() throws Exception {
        performInsertBreak( "<?php\n" +
                            "class foo {\n" +
                            "    /**^\n" +
                            "    function bar($par) {\n" +
                            "    }\n" +
                            "}\n" +
                            "?>\n",
                            "<?php\n" +
                            "class foo {\n" +
                            "    /**\n" +
                            "     * ^\n" +
                            "     * @param " + GeneratingBracketCompleter.TYPE_PLACEHOLDER + " $par\n" +
                            "     */\n" +
                            "    function bar($par) {\n" +
                            "    }\n" +
                            "}\n" +
                            "?>\n");
    }

    private void performInsertBreak(final String original, final String expected) throws Exception {
        final int insertOffset = original.indexOf('^');
        final int finalCaretPos = expected.indexOf('^');
        final String originalFin = original.substring(0, insertOffset) + original.substring(insertOffset + 1);
        final String expectedFin = expected.substring(0, finalCaretPos) + expected.substring(finalCaretPos + 1);
        performTest(new String[] {originalFin}, new UserTask() {
            public void cancel() {}

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ParserResult parameter = (ParserResult) resultIterator.getParserResult();
                if (parameter != null) {
                    insertBreak(parameter, originalFin, expectedFin, insertOffset, finalCaretPos);
                }
            }
        });
    }

    private void insertBreak(ParserResult info, String original, String expected, int insertOffset, int finalCaretPos) throws BadLocationException, DataObjectNotFoundException, IOException {

        BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);//PHPBracketCompleterTest.getDocument(original);
        assertNotNull(doc);

        doc.putProperty(org.netbeans.api.lexer.Language.class, PHPTokenId.language());
        doc.putProperty("mimeType", FileUtils.PHP_MIME_TYPE);
//        doc.putProperty(Document.StreamDescriptionProperty, DataObject.find(info.getFileObject()));

        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(insertOffset);

        Reformat f = Reformat.get(doc);
        assertNotNull(f);

        f.lock();
        try {
            doc.atomicLock();
            try {
                DocumentUtilities.setTypingModification(doc, true);
                try {
                    PHPBracketCompleter bc = new PHPBracketCompleter();
                    int newOffset = bc.beforeBreak(doc, insertOffset, ta);
                    doc.insertString(caret.getDot(), "\n", null);
                    // Indent the new line
                    PHPFormatter formatter = new PHPFormatter();
                    //ParserResult result = parse(fo);

                    int startPos = caret.getDot()+1;
                    int endPos = startPos+1;

                    //ParserResult result = parse(fo);
                    f.reformat(startPos, endPos);

                    int indent = GsfUtilities.getLineIndent(doc, insertOffset+1);

                    //bc.afterBreak(doc, insertOffset, caret);
                    String formatted = doc.getText(0, doc.getLength());
                    assertEquals(expected, formatted);
                    if (newOffset != -1) {
                        caret.setDot(newOffset);
                    } else {
                        caret.setDot(insertOffset+1+indent);
                    }
                    if (finalCaretPos != -1) {
                        assertEquals(finalCaretPos, caret.getDot());
                    }
                } finally {
                    DocumentUtilities.setTypingModification(doc, false);
                }
            } finally {
                doc.atomicUnlock();
            }
        } finally {
            f.unlock();
        }
    }
}
