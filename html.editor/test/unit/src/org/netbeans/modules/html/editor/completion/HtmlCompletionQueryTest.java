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
package org.netbeans.modules.html.editor.completion;

import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.Registry;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.test.TestBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileObject;

/**Html completion test
 * This class extends TestBase class which provides access to the html editor module layer
 *
 * @author Marek Fukala
 */
public class HtmlCompletionQueryTest extends TestBase {

    private static final String DATA_DIR_BASE = "testfiles/completion/";

    public static enum Match {

        EXACT, CONTAINS, DOES_NOT_CONTAIN, EMPTY, NOT_EMPTY;
    }


    public HtmlCompletionQueryTest(String name) throws IOException, BadLocationException {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(MockMimeLookup.class);
    }

    public static Test xsuite() throws IOException, BadLocationException {
	TestSuite suite = new TestSuite();
        suite.addTest(new HtmlCompletionQueryTest("testSimpleEndTag"));
        return suite;
    }

    //test methods -----------
//    public void testIndexHtml() throws IOException, BadLocationException {
//        testCompletionResults("index.html");
//    }
//
//    public void testNetbeansFrontPageHtml() throws IOException, BadLocationException {
//        testCompletionResults("netbeans.org.html");
//    }



    public void testSimpleEndTag() throws BadLocationException, ParseException {
        assertItems("<div></|", arr("div"), Match.CONTAINS, 7);
        //           01234567
    }

    public void testSimpleEndTagBeforeText() throws BadLocationException, ParseException {
        assertItems("<div></| text ", arr("div"), Match.CONTAINS, 7);
        //           01234567
    }

    public void testOpenTagWithCommonPrefix() throws BadLocationException, ParseException {
        assertItems("<a| ", arr("abbr"), Match.CONTAINS, 1);
        //           01234567
    }

    public void testOpenTagJustAfterText() throws BadLocationException, ParseException {
        assertItems("text<| ", arr("abbr"), Match.CONTAINS, 5);
        //           01234567
    }

    //causes: java.lang.AssertionError: content is null after sibling 'meta'
    public void testOpenTagAtMetaTagEnd() throws BadLocationException, ParseException {
        assertItems("<html><head><meta><title></title><| </head></html>", arr("link"), Match.CONTAINS);
        //           01234567
    }


    public void testTags() throws BadLocationException, ParseException {
        assertItems("<|", arr("div"), Match.CONTAINS, 1);
        assertItems("<|", arr("jindra"), Match.DOES_NOT_CONTAIN);
        assertItems("<d|", arr("div"), Match.CONTAINS, 1);
        assertItems("<div|", arr("div"), Match.EXACT, 1);

        //           01234567
        assertItems("<div></|", arr("div"), Match.CONTAINS, 7);
        assertItems("<div></d|", arr("div"), Match.CONTAINS, 7);
        assertItems("<div></div|", arr("div"), Match.EXACT, 7);
    }

    public void testCompleteTags() throws BadLocationException, ParseException {
        assertCompletedText("<|", "div", "<div>|");
        assertCompletedText("<di|", "div", "<div>|");
        assertCompletedText("<div|", "div", "<div>|");

        assertCompletedText("<div></|", "div", "<div></div>|");
        assertCompletedText("<div></d|", "div", "<div></div>|");
        assertCompletedText("<div></div|", "div", "<div></div>|");
    }
    
    public void testCompleteTagsBeforeText() throws BadLocationException, ParseException {
        assertCompletedText("<| ", "div", "<div>| ");
        assertCompletedText("<di| ", "div", "<div>| ");
        assertCompletedText("<div| ", "div", "<div>| ");

        assertCompletedText("<div></| ", "div", "<div></div>| ");
        assertCompletedText("<div></d| ", "div", "<div></div>| ");
        assertCompletedText("<div></div| ", "div", "<div></div>| ");
    }

    public void testTagsBeforeText() throws BadLocationException, ParseException {
        assertItems("<| ", arr("div"), Match.CONTAINS, 1);
        assertItems("<| ", arr("jindra"), Match.DOES_NOT_CONTAIN);
        assertItems("<d| ", arr("div"), Match.CONTAINS, 1);
        assertItems("<div| ", arr("div"), Match.EXACT, 1);

        //           01234567
        assertItems("<div></| ", arr("div"), Match.CONTAINS, 7);
        assertItems("<div></d| ", arr("div"), Match.CONTAINS, 7);
        assertItems("<div></div| ", arr("div"), Match.EXACT, 7);
    }


    public void testTagAttributes() throws BadLocationException, ParseException {
        //           012345
        assertItems("<col |", arr("align"), Match.CONTAINS, 5);
        assertItems("<col |", arr("jindra"), Match.DOES_NOT_CONTAIN);
        assertItems("<col a|", arr("align"), Match.CONTAINS, 5);
    }

    public void testCompleteTagAttributes() throws BadLocationException, ParseException {
        assertCompletedText("<col |", "align", "<col align=\"|\"");
        assertCompletedText("<col a|", "align", "<col align=\"|\"");
    }

    public void testTagAttributeValues() throws BadLocationException, ParseException {
        assertItems("<col align=\"|\"", arr("center"), Match.CONTAINS, 12);
        //           01234567890 12
        assertItems("<col align=\"ce|\"", arr("center"), Match.CONTAINS, 12);
        assertItems("<col align=\"center|\"", arr("center"), Match.EXACT, 12);
    }

    public void testCompleteTagAttributeValues() throws BadLocationException, ParseException {
        assertCompletedText("<col align=\"|\"", "center", "<col align=\"center|\"");
        assertCompletedText("<col align=\"ce|\"", "center", "<col align=\"center|\"");

        //regression test - issue #161852
        assertCompletedText("<col align=\"|\"", "left", "<col align=\"left|\"");

        //test single quote
        assertCompletedText("<col align='|'", "center", "<col align='center'|");

        //test values cc without quotation
        assertCompletedText("<col align=|", "left", "<col align=left|");
        assertCompletedText("<col align=ri|", "right", "<col align=right|");
    }

    public void testCharacterReferences() throws BadLocationException, ParseException {
        assertItems("&|", arr("amp"), Match.CONTAINS, 1);
        assertItems("&a|", arr("amp"), Match.CONTAINS, 1);
        assertItems("&amp|", arr("amp"), Match.EXACT, 1);
        assertItems("|&amp;", arr("amp"), Match.CONTAINS, 1);

        assertCompletedText("&|", "amp", "&amp;|");
        assertCompletedText("&am|", "amp", "&amp;|");
    }

    public void testBooleanAttributes() throws BadLocationException, ParseException {
        assertItems("<input d|", arr("disabled"), Match.CONTAINS, 7);
        //           01234567
        assertCompletedText("<input d|", "disabled", "<input disabled|");
    }

    public void testFileAttrValue() throws BadLocationException, ParseException {
        String code = "<a href='|'";
        //             01234567890
        //we need a fileobject backed document here
        Document doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../", "another.html", "image.png"), Match.CONTAINS, 9);

        //double quotes
        code = "<a href=\"|\"";
        //             01234567890
        //we need a fileobject backed document here
        doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../", "another.html", "image.png"), Match.CONTAINS, 9);

        //unquoted
        code = "<a href=|";
        //             01234567890
        //we need a fileobject backed document here
        doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../", "another.html", "image.png"), Match.CONTAINS, 8);
    }

     public void testFileAttrValueAllTags() throws BadLocationException, ParseException {
        String code = "<link href='|'";
        //             01234567890
        //we need a fileobject backed document here
        Document doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../", "another.html", "image.png"), Match.CONTAINS, 12);

        code = "<base href='|'";
        //             01234567890
        //we need a fileobject backed document here
        doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../", "another.html", "image.png"), Match.CONTAINS, 12);

     }

    public void testFileAttrValueFolders() throws BadLocationException, ParseException {
        String code = "<a href='|'";
        //             01234567890
        //we need a fileobject backed document here
        Document doc = createDocuments("test.html", "folder1/another.html", "images/image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("folder1/", "images/", "../"), Match.CONTAINS, 9);

        //complete in folder
        code = "<a href='folder1/|'";
        //             01234567890
        //we need a fileobject backed document here
        doc = createDocuments("test.html", "folder1/another.html", "images/image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("another.html"), Match.CONTAINS, 9);
    }

    public void testFileAttrValueWithPrefix() throws BadLocationException, ParseException {
        String code = "<a href='ima|'";
        //             01234567890
        //we need a fileobject backed document here
        Document doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("image.png"), Match.CONTAINS, 9);

        //unquoted
        code = "<a href=ima|";
        //             01234567890
        //we need a fileobject backed document here
        doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("image.png"), Match.CONTAINS, 8);
    }

    public void testFileAttrValueUppercase() throws BadLocationException, ParseException {
        String code = "<A HREF='|'";
        //             01234567890
        //we need a fileobject backed document here
        Document doc = createDocuments("test.html", "another.html", "image.png")[0]; //use test.html
        doc.insertString(0, code, null);
        assertItems(doc, arr("../", "another.html", "image.png"), Match.CONTAINS, 9);
    }

    public void testCompleteAttributesInUnknownTag() throws BadLocationException, ParseException {
        assertItems("<gggg |", arr(), Match.EMPTY);
        assertItems("<gggg hhh=|", arr(), Match.EMPTY);
        assertItems("<gggg hhh='|", arr(), Match.EMPTY);
    }

    public void testEndTagAutocompletion() throws BadLocationException, ParseException {
        assertItems("<div>|", arr("div"), Match.EXACT, 5);
        //test end tag ac for unknown tags
        assertItems("<div><bla>|", arr(), Match.EMPTY, 0);
    }


    //helper methods ------------

    //test HTML 4.01
    protected String getPublicID() {
        return "-//W3C//DTD HTML 4.01 Transitional//EN";
    }

    protected void assertItems(String documentText, final String[] expectedItemsNames, final Match type) throws BadLocationException, ParseException {
        assertItems(documentText, expectedItemsNames, type, -1);
    }

    protected void assertItems(String documentText, final String[] expectedItemsNames, final Match type, int expectedAnchor) throws BadLocationException, ParseException {
        assertItems(getDocument(documentText), expectedItemsNames, type, expectedAnchor);
    }

    protected void assertItems(Document doc, final String[] expectedItemsNames, final Match type, int expectedAnchor) throws BadLocationException, ParseException {
        String content = doc.getText(0, doc.getLength());

        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0 : "define caret position by pipe character in the document source!";

        //remove the pipe
        doc.remove(pipeOffset, 1);

        HtmlCompletionQuery query = new HtmlCompletionQuery(doc, pipeOffset);
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);

        DTD dtd = Registry.getDTD(getPublicID(), null);
        assertNotNull(dtd);
        if(getPublicID() != null) {
            assertEquals(getPublicID(), dtd.getIdentifier());
        }

        final HtmlParserResult[] result = new HtmlParserResult[1];
        Source source = Source.create(doc);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                result[0] = (HtmlParserResult)resultIterator.getParserResult();
            }
        });

        assertNotNull(result[0]);

        HtmlCompletionQuery.CompletionResult completionResult = query.query(result[0], dtd);
        if(expectedItemsNames.length == 0 && completionResult == null) {
            //result may be null if we do not expect any result, nothing to test then
            return ;
        }

        Collection<CompletionItem> items = completionResult.getItems();
        assertNotNull(items);

        if(expectedAnchor > 0) {
            assertEquals(expectedAnchor, completionResult.getAnchor());
        }

        try {
            assertCompletionItemNames(expectedItemsNames, items, type);
        } catch(AssertionFailedError e) {
            for(CompletionItem item : items) {
                System.out.println(((HtmlCompletionItem)item).getItemText());
            }
            throw e;
        }

    }

    private void assertCompletedText(String documentText, String itemToCompleteName, String expectedText) throws BadLocationException, ParseException {
        StringBuffer content = new StringBuffer(documentText);
        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0 : "define caret position by pipe character in the document source!";
        //remove the pipe
        content.deleteCharAt(pipeOffset);

        StringBuffer expectedContent = new StringBuffer(expectedText);
        final int expectedPipeOffset = expectedContent.indexOf("|");
        assert expectedPipeOffset >= 0 : "define caret position by pipe character in the expected text!";
        //remove the pipe
        expectedContent.deleteCharAt(expectedPipeOffset);

        Document doc = getDocument(content.toString());

        HtmlCompletionQuery query = new HtmlCompletionQuery(doc, pipeOffset);
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);
        component.getCaret().setDot(pipeOffset);

        DTD dtd = Registry.getDTD(getPublicID(), null);
        assertNotNull(dtd);
        if(getPublicID() != null) {
            assertEquals(getPublicID(), dtd.getIdentifier());
        }

         final HtmlParserResult[] result = new HtmlParserResult[1];
        Source source = Source.create(doc);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                result[0] = (HtmlParserResult)resultIterator.getParserResult();
            }
        });

        assertNotNull(result[0]);

        HtmlCompletionQuery.CompletionResult completionResult = query.query(result[0], dtd);

        assertNotNull(result);
        Collection<CompletionItem> items = completionResult.getItems();
        assertNotNull(items);

        CompletionItem item = null;
        for (CompletionItem htmlci : items) {
            if (((HtmlCompletionItem)htmlci).getItemText().equals(itemToCompleteName)) {
                item = htmlci; //found
                break;
            }
        }


        assertNotNull(item);
        assertTrue(item instanceof HtmlCompletionItem);

        item.defaultAction(component); //complete

        assertEquals(expectedContent.toString(), doc.getText(0, doc.getLength()));
//        assertEquals(expectedPipeOffset, component.getCaret().getDot());

    }

    private void assertCompletionItemNames(String[] expected, Collection<CompletionItem> ccresult, Match type) {
        Collection<String> real = new ArrayList<String>();
        for (CompletionItem ccp : ccresult) {
            //check only html items
            if (ccp instanceof HtmlCompletionItem) {
                HtmlCompletionItem htmlci = (HtmlCompletionItem) ccp;
                real.add(htmlci.getItemText());
            }
        }
        Collection<String> exp = new ArrayList<String>(Arrays.asList(expected));

        if (type == Match.EXACT) {
            assertEquals(exp, real);
        } else if (type == Match.CONTAINS) {
            exp.removeAll(real);
            assertEquals(exp, Collections.EMPTY_LIST);
        } else if (type == Match.EMPTY) {
            assertEquals(0, real.size());
        } else if (type == Match.NOT_EMPTY) {
            assertTrue(real.size() > 0);
        } else if (type == Match.DOES_NOT_CONTAIN) {
            int originalRealSize = real.size();
            real.removeAll(exp);
            assertEquals(originalRealSize, real.size());
        }

    }

    protected String[] arr(String... args) {
        return args;
    }

    private void testCompletionResults(String testFile) throws IOException, BadLocationException, ParseException {
        FileObject source = getTestFile(DATA_DIR_BASE + testFile);
        BaseDocument doc = getDocument(source);
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);

        StringBuffer output = new StringBuffer();
        for (int i = 0; i < doc.getLength(); i++) {
            HtmlCompletionQuery.CompletionResult result = new HtmlCompletionQuery(doc, i).query();
            if (result != null) {
                Collection<CompletionItem> items = result.getItems();
                output.append(i + ":");
                output.append('[');
                List<CompletionItem> itemsList = new ArrayList<CompletionItem>(items);
                //sort the collection according to the sort text.
                //normally the html completion infrastr. does this
                Collections.sort(itemsList, new Comparator<CompletionItem>() {
                    public int compare(CompletionItem o1, CompletionItem o2) {
                        return o1.getSortText().toString().compareTo(o2.getSortText().toString());
                    }
                });
                Iterator<CompletionItem> itr = itemsList.iterator();
                while (itr.hasNext()) {
                    HtmlCompletionItem htmlci = (HtmlCompletionItem)itr.next();
                    output.append(htmlci.getItemText());
                    if (itr.hasNext()) {
                        output.append(',');
                    }
                }
                output.append(']');
                output.append('\n');
            }
        }

        assertDescriptionMatches(source, output.toString(), false, ".pass", true);

    }
}
