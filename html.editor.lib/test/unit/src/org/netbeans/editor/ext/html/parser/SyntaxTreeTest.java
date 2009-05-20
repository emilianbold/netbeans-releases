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
package org.netbeans.editor.ext.html.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.HtmlSyntaxSupport;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.Registry;
import org.netbeans.editor.ext.html.dtd.Utils;
import org.netbeans.editor.ext.html.parser.AstNode.Description;
import org.netbeans.editor.ext.html.test.TestBase;
import org.netbeans.junit.MockServices;

/**
 *
 * @author tomslot
 */
public class SyntaxTreeTest extends TestBase {

    private static final LanguagePath languagePath = LanguagePath.get(HTMLTokenId.language());

    public SyntaxTreeTest() {
        super("SyntaxTreeTest");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(MockMimeLookup.class);
    }

//    public void testTrivialCase() throws Exception {
//        testSyntaxTree("trivial.html");
//    }
//
//    public void testEmptyTags() throws Exception {
//        testSyntaxTree("emptyTags.html");
//    }
//
//    public void testList() throws Exception {
//        testSyntaxTree("list.html");
//    }
//
//    public void testTable() throws Exception {
//        testSyntaxTree("table.html");
//    }
//
//    public void testTagCrossing() throws Exception {
//        testSyntaxTree("tagCrossing.html");
//    }
//
//    public void testMissingEndTag() throws Exception {
//        testSyntaxTree("missingEndTag.html");
//    }
//
////    //this test "is supposed to fail" since there is a bug in the code
////    //see the issue description for more information
////    public void testIssue145821() throws Exception{
////        testSyntaxTree("issue145821.html");
////    }
//    public void testIssue127786() throws Exception {
//        testSyntaxTree("issue127786.html");
//    }
//
//    public void testIssue129347() throws Exception {
//        testSyntaxTree("issue129347.html");
//    }
//
//    public void testIssue129654() throws Exception {
//        testSyntaxTree("issue129654.html");
//    }
//
//
    public void testAST() throws Exception {
        assertAST("<p>one\n<p>two</p>");
        assertAST("<p></p><div>", 1); //last DIV is unmatched
//        assertAST("<p><p><p>");
        assertAST("<html><head><title></title><script></script></head><body></body></html>");
    }
//
//    public void testEmptyFileWithOpenTag() throws Exception {
//        assertAST("<div>", 1);
//        assertAST("</div>", 1);
//    }
//
//    public void testNamespaceTag() throws Exception {
//        assertAST("<div> <ul> <wicket:link> <li>item</li> </wicket:link> </ul> </div>", 0);
//    }
//
//    public void testUnresolvedTagContent() throws Exception {
//        //missing TITLE
//        assertAST("<html><head></head><body></body></html>",
//                desc(SyntaxTree.UNRESOLVED_TAG_KEY, 6,12, Description.ERROR));
//        //         0123456789012345678901234567890123456789
//        //         0         1         2         3
//
//        //missing BODY
//        assertAST("<html><head><title></title></head></html>",
//                desc(SyntaxTree.UNRESOLVED_TAG_KEY, 0,6, Description.ERROR));
//        //         0123456789012345678901234567890123456789
//        //         0         1         2         3
//
//        //unresolved HTML - missing HEAD + unexpected BODY - missing HEAD
//        assertAST("<html><body></body></html>",
//                desc(SyntaxTree.UNRESOLVED_TAG_KEY, 0, 6, Description.ERROR),
//                desc(SyntaxTree.UNEXPECTED_TAG_KEY, 6, 12, Description.ERROR));
//    }
//
//    public void testUnmatchedTagBecauseOfOptionalEndTag() throws Exception {
//        //last </P> end tag is unmatched
//        assertAST("<p><p></p></p>",
//                desc(SyntaxTree.UNMATCHED_TAG, 10,14, Description.WARNING));
//        //         0123456789012345678901234567890123456789
//        //         0         1         2         3
//
//        assertAST("<p><div></div></p>",
//                desc(SyntaxTree.UNMATCHED_TAG, 14, 18, Description.WARNING));
//    }
//
//    public void testOptionalEndTag() throws Exception {
//        //HEAD has optional end tag
//        assertAST("<html><head><title></title><body></body></html>");
//
//    }
//
//    public void testUnexpectedTag() throws Exception {
//        //TR cannot contain TR - needs (TD|TH)+
//        assertAST("<table><tr><tr></table>",
//                desc(SyntaxTree.UNEXPECTED_TAG_KEY, 11, 15, Description.ERROR ));
//
//        //STYLE is not allowed in BODY; Issue 164903
//        AstNode.Description[] expectedErrors = new AstNode.Description[]{
//            desc(SyntaxTree.UNEXPECTED_TAG_KEY, 40, 47, Description.ERROR)
//        };
//        assertAST("<html><head><title></title></head><body><style></style></body></html>", expectedErrors);
//        //         0123456789012345678901234567890123456789012345678901234567890123456789
//        //         0         1         2         3         4         5         6
//
//    }
//
//    public void testIssue162576() throws Exception {
//        String code = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"" +
//                "\"http://www.w3.org/TR/html4/strict.dtd\">" +
//                "<form>" +
//                "<fieldset title=\"requestMethod\">" +
//                "<legend>requestMethod</legend>" +
//                "<input>" +
//                "</fieldset>" +
//                "</form>";
//
////        SyntaxTree.DEBUG = true;
//        assertAST(code);
//
//    }
//
//    public void testIssue163146() throws Exception {
//        //I am not sure what is the problem here, keep it for later debugging
//
//        String code =
//        "<html>"+
//            "<head>"+
//                "<title></title>"+
//            "</head>"+
//            "<body>"+
//                "<div>"+
//                    "<table>"+
//                        "<tr>"+
//                            "<td>"+
//                                "</div>"+
//                            "</td>"+
//                        "</tr>"+
//                    "</table>"+
//                "</div>"+
//            "</body>"+
//        "</html>";
//
//        assertAST(code);
//
//    }
//    public void testUnknownTags() throws Exception {
//        assertAST("<xx></xx>",
//                desc(SyntaxTree.UNKNOWN_TAG_KEY, 0, 4, Description.ERROR));
//    }
//
//    public void testUnknownTagsMatching() throws Exception {
//        assertAST("<xx>",
//                desc(SyntaxTree.UNKNOWN_TAG_KEY, 0, 4, Description.ERROR),
//                desc(SyntaxTree.UNMATCHED_TAG, 0, 4, Description.WARNING));
//    }
//
//    public void testEmptyXhtmlTags() throws Exception{
//        assertAST("<html><head><meta></meta><title></title></head><body></body></html>", Utils.XHTML_STRINCT_PUBLIC_ID);
//    }

    private void testSyntaxTree(String testCaseName) throws Exception {
        String documentContent = readStringFromFile(new File(getTestFilesDir(), testCaseName));

        BaseDocument doc = createDocument();
        doc.insertString(0, documentContent, null);
        HtmlSyntaxSupport sup = HtmlSyntaxSupport.get(doc);
        assertNotNull(sup);
        DTD dtd = sup.getDTD();
        assertNotNull(dtd);
        SyntaxParser parser = SyntaxParser.get(doc, languagePath);
        parser.forceParse();
        AstNode root = SyntaxTree.makeTree(parser.elements(), dtd);
        getRef().print(root.toString());
        compareReferenceFiles();
    }

    private AstNode.Description desc(String key, int from, int to, int type) {
        return AstNode.Description.create(key, null, type, from, to);
    }

    private String errorsAsString(List<AstNode.Description> errors) {
        StringBuffer buf = new StringBuffer();
        for(Iterator<AstNode.Description> i = errors.listIterator(); i.hasNext() ;) {
            buf.append(i.next().dump(null));
            if(i.hasNext()) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }

    private void assertAST(final String code) throws Exception {
        assertAST(code,0);
    }

    private void assertAST(final String code, int expectedErrorsNumber) throws Exception {
        assertAST(code, null, expectedErrorsNumber);
    }

    private void assertAST(final String code, String publicId, int expectedErrorsNumber) throws Exception {
        AstNode root = parse(code, publicId);

        final int[] errors = new int[1];
        errors[0] = 0;
        final List<AstNode.Description> errorslist = new ArrayList<AstNode.Description>();
        AstNodeVisitor visitor = new AstNodeVisitor() {
            public void visit(AstNode node) {
                for(Description d : node.getDescriptions()) {
                    errorslist.add(d);
                    errors[0]++;
                }
            }
        };
        AstNodeUtils.visitChildren(root, visitor);

        assertEquals("Unexpected number of errors, current errors: " + errorsAsString(errorslist) ,expectedErrorsNumber, errors[0]);

    }

    private void assertAST(final String code, AstNode.Description... expectedErrors) throws Exception {
        assertAST(code, null, expectedErrors);
    }

    private void assertAST(final String code, String publicId, AstNode.Description... expectedErrors) throws Exception {
        AstNode root = parse(code, publicId);

        final Iterator<AstNode.Description> errorsItr = Arrays.asList(expectedErrors).listIterator();
        AstNodeVisitor visitor = new AstNodeVisitor() {
            public void visit(AstNode node) {
                for(Description d : node.getDescriptions()) {
                    assertTrue("Unexpected error description: " + d.dump(code), errorsItr.hasNext());
                    assertEquals(errorsItr.next(), d);
                }
            }
        };

        AstNodeUtils.visitChildren(root, visitor);

        List<AstNode.Description> missing = new ArrayList<Description>();
        while(errorsItr.hasNext()) {
            missing.add(errorsItr.next());
        }
        assertEquals("Some expected errors are missing: " + errorsAsString(missing), expectedErrors.length, expectedErrors.length - missing.size());

    }

    private AstNode parse(String code, String publicId) throws BadLocationException {
        BaseDocument doc = createDocument();
        doc.insertString(0, code, null);
        HtmlSyntaxSupport sup = HtmlSyntaxSupport.get(doc);
        assertNotNull(sup);

        DTD dtd;
        if(publicId == null) {
            dtd = sup.getDTD();
        } else {
            dtd = Registry.getDTD(publicId, null);
            assertEquals(publicId, dtd.getIdentifier());
        }

        assertNotNull(dtd);
        SyntaxParser parser = SyntaxParser.get(doc, languagePath);
        parser.forceParse();
        return SyntaxTree.makeTree(parser.elements(), dtd);
    }

    private String readStringFromFile(File file) throws IOException {
        StringBuffer buff = new StringBuffer();

        BufferedReader rdr = new BufferedReader(new FileReader(file));

        String line;

        try {
            while ((line = rdr.readLine()) != null) {
                buff.append(line + "\n");
            }
        } finally {
            rdr.close();
        }

        return buff.toString();
    }

    private File getTestFilesDir() {
        return new File(new File(getDataDir(), "input"), "SyntaxTreeTest");
    }
}
