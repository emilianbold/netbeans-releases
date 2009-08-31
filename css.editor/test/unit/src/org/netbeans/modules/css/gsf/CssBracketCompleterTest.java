/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.css.gsf;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.test.TestBase;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * @author marek.fukala@sun.com
 */
public class CssBracketCompleterTest extends TestBase {

    private Document doc;
    private JEditorPane pane;
    private BaseAction defaultKeyTypedAction;
    private BaseAction backspaceAction;

    public CssBracketCompleterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupEditor();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanUpEditor();
    }

    private void setupEditor() throws IOException {
        // this doesn't work since the JTextPane doesn't like our Kits since they aren't StyleEditorKits.
        //            Document doc = createDocument();
        //            JTextPane pane = new JTextPane((StyledDocument)doc);
        //            EditorKit kit = CloneableEditorSupport.getEditorKit("text/x-css");
        //            pane.setEditorKit(kit);

        File tmpFile = new File(getWorkDir(), "bracketCompleterTest.css");
        tmpFile.createNewFile();
        FileObject fo = FileUtil.createData(tmpFile);
        DataObject dobj = DataObject.find(fo);
        EditorCookie ec = dobj.getCookie(EditorCookie.class);
        this.doc = ec.openDocument();
        ec.open();
        this.pane = ec.getOpenedPanes()[0];

        this.defaultKeyTypedAction = (BaseAction) pane.getActionMap().get(NbEditorKit.defaultKeyTypedAction);
        this.backspaceAction = (BaseAction) pane.getActionMap().get(NbEditorKit.deletePrevCharAction);
    }

    private void cleanUpEditor() {
        this.pane.setVisible(false);
        this.pane = null;
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testCurlyBrackets() throws BadLocationException, IOException {
        //test pair autocomplete
        type('{');
        assertEquals("{}", getText());

        //test generated pair autodelete
//        backspace();
//        assertEquals("", getText());

        clear(doc);

        //test not generated pair NOT deleted
        doc.insertString(0, "{}", null);
        //                   01
        pane.setCaretPosition(1);
        backspace();
//        assertEquals("}", getText());

        clear(doc);

        //test skipping closing curly bracket
        String text = "h1 { color: red; } ";
        //             0123456789012345678        
        doc.insertString(0, text , null);

        pane.setCaretPosition(17);
        type('}');

        assertEquals(text, getText()); //no change in the text
        assertEquals(18, pane.getCaretPosition()); //+1

    }

    public void testLogicalRanges() throws ParseException {
        assertLogicalRanges("h1 { col|or: red; }", new int[][]{{5,10}, {5,15}, {0,18}});
        //                   01234567 89012345678

        assertLogicalRanges("h1 { color: re|d; }", new int[][]{{12,15}, {5,15}, {0,18}});
        //                   01234567890123 45678

        assertLogicalRanges("h1 {| color: red; }", new int[][]{{0,18}});
        //                   0123 456789012345678

        assertLogicalRanges("@media page { h1 { col|or: red; } }", new int[][]{{19, 24}, {19, 29}, {14, 32}, {14, 33}, {0, 34}});
        //                   0123456789012345678901 234567890123456789
        //                   0         1         2          3
        
        assertLogicalRanges("h1, h|2 { color: red; }", new int[][]{{4, 6}, {4, 7}, {0, 7}, {0, 22}});
        //                   01234 567890123456789012

        assertLogicalRanges("@me|dia page { }", new int[][]{{0,15}});
        //                   012 3456789012345

    }


    private void assertLogicalRanges(String sourceText, int[][] expectedRangesLeaveToRoot) throws ParseException {
         //find caret position in the source text
        StringBuffer content = new StringBuffer(sourceText);

        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0 : "define caret position by pipe character in the document source!";

        //remove the pipe
        content.deleteCharAt(pipeOffset);
        sourceText = content.toString();

        //fatal parse error on such input, AST root == null
        Document document = getDocument(sourceText);
        Source source = Source.create(document);
        final Result[] _result = new Result[1];
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                _result[0] = resultIterator.getParserResult();
            }
        });

        Result result = _result[0];
        assertNotNull(result);
        assertTrue(result instanceof CssParserResult);

        CssParserResult cssResult = (CssParserResult)result;
        assertNotNull(cssResult.root());
        assertEquals(0, cssResult.getDiagnostics().size()); //no errors

        KeystrokeHandler handler = getPreferredLanguage().getKeystrokeHandler();
        assertNotNull(handler);

        List<OffsetRange> ranges = handler.findLogicalRanges(cssResult, pipeOffset);
        assertNotNull(ranges);

        String expectedRanges = expectedRangesToString(ranges);
        assertEquals("Unexpected number of logical ranges; existing ranges=" + expectedRanges , expectedRangesLeaveToRoot.length, ranges.size());

        for(int i = 0; i < ranges.size(); i++) {
            OffsetRange or = ranges.get(i);

            int expectedStart = expectedRangesLeaveToRoot[i][0];
            int expectedEnd = expectedRangesLeaveToRoot[i][1];

            assertEquals("Invalid logical range (" + or.toString() + ") start offset", expectedStart, or.getStart());
            assertEquals("Invalid logical range (" + or.toString() + ") end offset", expectedEnd, or.getEnd());
        }

    }


    //------- utilities -------

    private String expectedRangesToString(List<OffsetRange> ranges) {
        StringBuffer buf = new StringBuffer();
        for(OffsetRange range : ranges) {
            buf.append("{" + range.getStart() + ", " + range.getEnd() + "}, ");
        }
        return buf.toString();
    }

    private void type(char ch) {
        ActionEvent ae = new ActionEvent(doc, 0, ""+ch);
        defaultKeyTypedAction.actionPerformed(ae, pane);
    }

    private void backspace() {
        backspaceAction.actionPerformed(new ActionEvent(doc, 0, null));
    }

    private String getText() throws BadLocationException {
        return doc.getText(0, doc.getLength());
    }

    private void clear(Document doc) throws BadLocationException {
        doc.remove(0, doc.getLength());
    }
    
}
