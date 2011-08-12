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
package org.netbeans.modules.html.editor.completion;

import junit.framework.AssertionFailedError;
import org.netbeans.editor.Finder;
import org.netbeans.editor.ext.html.dtd.DTD;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.dtd.Registry;
import org.netbeans.editor.ext.html.parser.api.HtmlVersion;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.html.editor.test.TestBase;

/**
 *
 * @author marekfukala
 */
public abstract class HtmlCompletionTestBase extends TestBase {

    private static final String DATA_DIR_BASE = "testfiles/completion/";

    public static enum Match {

        EXACT, CONTAINS, DOES_NOT_CONTAIN, EMPTY, NOT_EMPTY;
    }

    public HtmlCompletionTestBase(String name) {
        super(name);
    }

    protected abstract HtmlVersion getExpectedVersion();

    protected String getPublicID() {
        return null;
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

        HtmlCompletionQuery query = new HtmlCompletionQuery(doc, pipeOffset, false);
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);

//        DTD dtd = Registry.getDTD(getPublicID(), null);
//        assertNotNull(dtd);
//        if (getPublicID() != null) {
//            assertEquals(getPublicID(), dtd.getIdentifier());
//        }

        final HtmlParserResult[] result = new HtmlParserResult[1];
        Source source = Source.create(doc);
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                result[0] = (HtmlParserResult) resultIterator.getParserResult();
            }
        });

        assertNotNull(result[0]);

        assertSame(getExpectedVersion(), result[0].getSyntaxAnalyzerResult().getHtmlVersion());

        HtmlCompletionQuery.CompletionResult completionResult = query.query(result[0]);
        
        if(type != Match.EMPTY) {
            assertNotNull("null completion query result", completionResult);
        }
        
        if (expectedItemsNames.length == 0 && completionResult == null) {
            //result may be null if we do not expect any result, nothing to test then
            return;
        }

        Collection<? extends CompletionItem> items = completionResult.getItems();
        assertNotNull(items);

        if (expectedAnchor > 0) {
            assertEquals(expectedAnchor, completionResult.getAnchor());
        }

        try {
            assertCompletionItemNames(expectedItemsNames, items, type);
        } catch (AssertionFailedError e) {
            for (CompletionItem item : items) {
                System.out.println(((HtmlCompletionItem) item).getItemText());
            }
            throw e;
        }

    }

    protected void assertCompletedText(String documentText, String itemToCompleteName, String expectedText) throws BadLocationException, ParseException {
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

        HtmlCompletionQuery query = new HtmlCompletionQuery(doc, pipeOffset, false);
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);
        component.getCaret().setDot(pipeOffset);

//        DTD dtd = Registry.getDTD(getPublicID(), null);
//        assertNotNull(dtd);
//        if (getPublicID() != null) {
//            assertEquals(getPublicID(), dtd.getIdentifier());
//        }

        final HtmlParserResult[] result = new HtmlParserResult[1];
        Source source = Source.create(doc);
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                result[0] = (HtmlParserResult) resultIterator.getParserResult();
            }
        });

        assertNotNull(result[0]);

        HtmlCompletionQuery.CompletionResult completionResult = query.query(result[0]);

        assertNotNull(completionResult);
        Collection<? extends CompletionItem> items = completionResult.getItems();
        assertNotNull(items);

        CompletionItem item = null;
        for (CompletionItem htmlci : items) {
            if (((HtmlCompletionItem) htmlci).getItemText().equals(itemToCompleteName)) {
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

    private void assertCompletionItemNames(String[] expected, Collection<? extends CompletionItem> ccresult, Match type) {
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

    protected TestSource getTestSource(String testFilePath, boolean removePipe) throws BadLocationException {
        FileObject source = getTestFile(DATA_DIR_BASE + testFilePath);
        BaseDocument doc = getDocument(source);
        StringBuilder sb = new StringBuilder(doc.getText(0, doc.getLength()));
        int pipeIndex = sb.indexOf("|");
        assertTrue(String.format("Errorneous test file %s, there is no pipe char specifying the completion offset!", testFilePath),
                pipeIndex != -1);

        if(removePipe) {
            sb.deleteCharAt(pipeIndex);
        }

        return new TestSource(sb.toString(), pipeIndex);
    }

    private void testCompletionResults(String testFile) throws IOException, BadLocationException, ParseException {
        FileObject source = getTestFile(DATA_DIR_BASE + testFile);
        BaseDocument doc = getDocument(source);
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);

        StringBuffer output = new StringBuffer();
        for (int i = 0; i < doc.getLength(); i++) {
            HtmlCompletionQuery.CompletionResult result = new HtmlCompletionQuery(doc, i, false).query();
            if (result != null) {
                Collection<? extends CompletionItem> items = result.getItems();
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
                    HtmlCompletionItem htmlci = (HtmlCompletionItem) itr.next();
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


    protected static class TestSource {
        private String code;
        private int position;

        private TestSource(String code, int position) {
            this.code = code;
            this.position = position;
        }

        public String getCode() {
            return code;
        }

        public int getPosition() {
            return position;
        }
        
    }

}
