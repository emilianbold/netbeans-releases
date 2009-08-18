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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.Registry;
import org.netbeans.editor.ext.html.parser.AstNode.Description;
import org.netbeans.editor.ext.html.test.TestBase;
import org.netbeans.modules.html.editor.NbReaderProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mfukala@netbeans.org
 */
public class SyntaxTreeTest extends TestBase {

    private static final String DATA_DIR_BASE = "testfiles/syntaxtree/";

    public SyntaxTreeTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        NbReaderProvider.setupReaders();
    }

    public static Test xsuite(){
	TestSuite suite = new TestSuite();
        suite.addTest(new SyntaxTreeTest("testIssue169209"));
        return suite;
    }

    public void testTrivialCase() throws Exception {
        testSyntaxTree("trivial.html");
    }

    public void testList() throws Exception {
        testSyntaxTree("list.html");
    }

    public void testTable() throws Exception {
        testSyntaxTree("table.html");
    }

    public void testTagCrossing() throws Exception {
        testSyntaxTree("tagCrossing.html");
    }

    public void testMissingEndTag() throws Exception {
        testSyntaxTree("missingEndTag.html");
    }

    public void testIssue145821() throws Exception{
        testSyntaxTree("issues145821.html");
    }

    public void testUncheckedAST() throws BadLocationException {
        String code = "<div><a><b></a></b></div>";
        //             01234567
        AstNode root = parseUnchecked(code);

//        System.out.println(AstNodeUtils.dumpTree(root));

        AstNode div = AstNodeUtils.query(root, "div");
        assertNotNull(div);

        AstNode a = AstNodeUtils.query(root, "div/a");
        assertNotNull(a);

        AstNode b = AstNodeUtils.query(root, "div/a/b");
        assertNotNull(b);

        assertEquals(3, div.children().size()); //<a>,</a> and unmatched </b>
        assertEquals(a, div.children().get(0));

        assertEquals(1, a.children().size()); //<b>
        assertEquals(b, a.children().get(0));
        

    }

    
    public void testAST() throws Exception {
        assertAST("<div><div>text</div></div>");
        assertAST("<div>text</div>");
        assertAST("<p>one\n<p>two</p>");
        assertAST("<p></p><div>", 1); //last DIV is unmatched
        assertAST("<p><p><p>");
        assertAST("<html><head><title></title><script type=''></script></head><body></body></html>");
    }

    public void testEmptyFileWithOpenTag() throws Exception {
        assertAST("<div>", 1);
        assertAST("</div>", 1);
    }

    //fails now
//    public void testIssue169209() throws Exception {
//         AstNode root = parse("<html><head><title></title><s| </head>", null);
//         AstNodeUtils.dumpTree(root);
//         assertNotNull(AstNodeUtils.query(root, "html/head/s"));
//    }

    public void testNamespaceTag() throws Exception {
        assertAST("<div> <ul> <wicket:link> <li>item</li> </wicket:link> </ul> </div>", 0);
    }

    public void testUnresolvedTagContent() throws Exception {
        //missing TITLE
        assertAST("<html><head></head><body></body></html>",
                desc(SyntaxTree.UNRESOLVED_TAG_KEY, 6,12, Description.ERROR));
        //         0123456789012345678901234567890123456789
        //         0         1         2         3

        //missing BODY
        assertAST("<html><head><title></title></head></html>",
                desc(SyntaxTree.UNRESOLVED_TAG_KEY, 0,6, Description.ERROR));
        //         0123456789012345678901234567890123456789
        //         0         1         2         3

        //unresolved HTML - missing HEAD + unexpected BODY - missing HEAD
        assertAST("<html><body></body></html>",
                desc(SyntaxTree.UNRESOLVED_TAG_KEY, 0, 6, Description.ERROR),
                desc(SyntaxTree.UNEXPECTED_TAG_KEY, 6, 12, Description.ERROR));
    }

    public void testUnmatchedTagBecauseOfOptionalEndTag() throws Exception {
        //last </P> end tag is unmatched
        assertAST("<p><p></p></p>",
                desc(SyntaxTree.UNMATCHED_TAG, 10,14, Description.WARNING));
        //         0123456789012345678901234567890123456789
        //         0         1         2         3

        assertAST("<p><div></div></p>",
                desc(SyntaxTree.UNMATCHED_TAG, 14, 18, Description.WARNING));
    }

    public void testOptionalEndTag() throws Exception {
        //HEAD has optional end tag
        assertAST("<html><head><title></title><body></body></html>");

        //test invalid element after optional end
        assertAST("<html><head><title></title><div><body></body></html>",
                desc(SyntaxTree.UNEXPECTED_TAG_KEY, 27, 32, Description.ERROR));
    }

    public void testUnexpectedTag() throws Exception {
        //TR cannot contain TR - needs (TD|TH)+
        assertAST("<table><tr><tr></table>",
                desc(SyntaxTree.UNRESOLVED_TAG_KEY, 7, 11, Description.ERROR),
                desc(SyntaxTree.UNEXPECTED_TAG_KEY, 11, 15, Description.ERROR),
                desc(SyntaxTree.UNRESOLVED_TAG_KEY, 11, 15, Description.ERROR)
                );

        //STYLE is not allowed in BODY; Issue 164903
        AstNode.Description[] expectedErrors = new AstNode.Description[]{
            desc(SyntaxTree.UNEXPECTED_TAG_KEY, 40, 46, Description.ERROR),
            desc(SyntaxTree.UNMATCHED_TAG, 55, 63, Description.WARNING)
        };
        assertAST("<html><head><title></title></head><body><style type=''></style></body></html>", expectedErrors);
        //         0123456789012345678901234567890123456789012345678901234567890123456789
        //         0         1         2         3         4         5         6

    }

//     public void testUnallowedEmptyTag() throws Exception {
//        //<stype> cannot be empty - issue #166042
//        assertAST("<html><head><title></title><style type=\"text/javascript\"/></head><body></body></html>",
//                // 0123456789012345678901234567890123456789
//                // 0         1         2         3
//                desc(SyntaxTree.TAG_CANNOT_BE_EMPTY, 27, 33, Description.ERROR)
//                );
//
//    }

    public void testIssue162576() throws Exception {
        String code = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" " +
                "\"http://www.w3.org/TR/html4/strict.dtd\">" +
                "<form action=''>" +
                "<fieldset title=\"requestMethod\">" +
                "<legend>requestMethod</legend>" +
                "<input>" +
                "</fieldset>" +
                "</form>";

//        SyntaxTree.DEBUG = true;
        assertAST(code);

    }

    public void testIssue163146() throws Exception {
        //I am not sure what is the problem here, keep it for later debugging

        String code =
        "<html>"+
            "<head>"+
                "<title></title>"+
            "</head>"+
            "<body>"+
                "<div>"+
                    "<table>"+
                        "<tr>"+
                            "<td>"+
                                "</div>"+
                            "</td>"+
                        "</tr>"+
                    "</table>"+
                "</div>"+
            "</body>"+
        "</html>";

        assertAST(code,
                desc(SyntaxTree.MISSING_REQUIRED_END_TAG, 45, 52, Description.ERROR),
                desc(SyntaxTree.UNMATCHED_TAG, 66, 71, Description.WARNING),
                desc(SyntaxTree.UNMATCHED_TAG, 71, 76, Description.WARNING),
                desc(SyntaxTree.UNMATCHED_TAG, 76, 84, Description.WARNING),
                desc(SyntaxTree.UNMATCHED_TAG, 84, 90, Description.WARNING)
                );

    }
    public void testUnknownTags() throws Exception {
        assertAST("<xx></xx>",
                desc(SyntaxTree.UNKNOWN_TAG_KEY, 0, 4, Description.WARNING),
                desc(SyntaxTree.UNKNOWN_TAG_KEY, 4, 9, Description.WARNING));
    }

    public void testUnknownTagsMatching() throws Exception {
        assertAST("<xx>",
                desc(SyntaxTree.UNKNOWN_TAG_KEY, 0, 4, Description.WARNING));
    }

    public void testOptionalStartTag() throws Exception {
        //missing optional open tags for html, head and body
        assertAST("<title></title></head><div></div></body></html>");
        //missing optional open tags for html and body
        assertAST("<head><title></title></head><div></div></body></html>");
        //missing optional open tags for head and body
        assertAST("<html><title></title></head><div></div></body></html>");
        //missing optional open tag for tbody
        assertAST("<html><head><title></title></head><body><table><tr><td>xxx</td></tbody></table></body></html>");
    }

    public void testHtmlOptionalStartTag() throws Exception {
        //missing optional open tag for body
//        assertAST("<html><head><title></title></head><div></div></body></html>");
    }
    
    public void testIssue165396() throws Exception {
        //<p> is marked as unmatched but should not be
        assertAST("<html><head><title></title></head><body><p></body></html>");
    }

    public void testEmptyTags() throws Exception{
        assertAST("<html><head><meta content=''></meta><title></title></head><body></body></html>",
                desc(SyntaxTree.UNMATCHED_TAG, 29, 36, Description.WARNING));
    }

    public void testEmptyXhtmlTags() throws Exception{
        assertAST("<html><head><meta content=''></meta><title></title></head><body></body></html>", "-//W3C//DTD XHTML 1.0 Strict//EN");
    }

    public void testOptinalEndTagsInTable() throws Exception{
        assertAST("<html><head><title></title></head><body>" +
                "<table><tr><td>r1c1<tr><td>r2c2</table>" +
                "</body></html>");

        //error: unresolved last <tr> tag
        assertAST("<html><head><title></title></head><body>" +
                "<table><tr><td>r1c1<tr><td>r2c2<tr></table>" +
                "</body></html>",
                desc(SyntaxTree.UNRESOLVED_TAG_KEY, 71, 75, Description.ERROR));
    }

    public void testOptinalHtmlEndTags() throws Exception{
        String code = "<html><head><title></title></head><body></body>";
        AstNode root = assertAST(code);

        //check the logical range e.g. end offset which should be the end of the code
        //(html unterminated, but optional end tag)
        assertLogicalRange(root, "html", 0, code.length());
        
    }

    public void testLogicalRanges() throws Exception {
        String code = "<html><head><title></title></head><body><table><tr><td><tr><td></table></body></html>";
        //             012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
        //             0         1         2         3         4         5         6         7         8
        AstNode root = assertAST(code);
        AstNodeUtils.dumpTree(root);

        assertLogicalRange(root, "html", 0, code.length());
        assertLogicalRange(root, "html/head", 6, 34);
        assertLogicalRange(root, "html/body", 34, 78);
        assertLogicalRange(root, "html/body/table", 40, 71);
        assertLogicalRange(root, "html/body/table/tr", 47, 55); //closed by next <tr> open tag
        assertLogicalRange(root, "html/body/table/tr|1", 55, 63); //closed by next </table> open tag

    }

    public void testLogicalRangesOfBrokenSource() throws Exception {
        String code = "<html><head><title></title></head><body><table><tr><td><p><style></style><tr></table></body></html>";
        //             012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
        //             0         1         2         3         4         5         6         7         8
        AstNode root = assertAST(code,
                desc(SyntaxTree.MISSING_REQUIRED_ATTRIBUTES, 58, 65, Description.WARNING), //style attr type required
                desc(SyntaxTree.UNEXPECTED_TAG_KEY, 58, 65, Description.ERROR), //style should be here
                desc(SyntaxTree.UNMATCHED_TAG, 65, 73, Description.WARNING), // </style> is unmatched
                desc(SyntaxTree.UNRESOLVED_TAG_KEY, 73, 77, Description.ERROR)); //<tr> doesn't contain required content
        
        AstNodeUtils.dumpTree(root);

        assertLogicalRange(root, "html", 0, code.length());
        assertLogicalRange(root, "html/head", 6, 34);
        assertLogicalRange(root, "html/body", 34, 92);
        assertLogicalRange(root, "html/body/table", 40, 85);
        assertLogicalRange(root, "html/body/table/tr", 47, 73); //closed by next <tr> open tag
        assertLogicalRange(root, "html/body/table/tr/td", 51, 73); //closed by next <tr> open tag
        assertLogicalRange(root, "html/body/table/tr|1", 73, 77); //closed by next </table> open tag

    }

    public void testTable2() throws Exception {
        String code = "<table><tr><td>r1c1<tr><td>r2c1</table>";

        assertAST(code);
    }

    //issue 165680, currently failing
    public void testUnexpectedContentAfterBody() throws Exception {
        String code = "<html><head><title></title></head><body>" +
                "</body><tr><td></tr></html>";

//        assertAST(code, 1);
    }


    public void testXhtmlNamespaceAttrs() throws Exception {
        assertAST("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:ui="+
                "\"http://java.sun.com/jsf/facelets\"><head><meta content=\"\"></meta>"+
                "<title></title></head><body></body></html>",
                "-//W3C//DTD XHTML 1.0 Strict//EN");
    }

    public void testMissingRequiredAttribute() throws Exception{
        //missing content attribute of meta tag
        assertAST("<html><head><title></title><meta></head><body>" +
                "<table><tr><td>r1c1<tr><td>r2c2</table>" +
                "</body></html>",
                desc(SyntaxTree.MISSING_REQUIRED_ATTRIBUTES, 27, 33, Description.WARNING));
    }

    public void testUnknownAttribute() throws Exception{
        assertAST("<html><head><title></title><body dummy='value'></body></html>",
                desc(SyntaxTree.UNKNOWN_ATTRIBUTE_KEY, 33, 38, Description.WARNING));

        //try it also in optional end tag
        assertAST("<html><head><title></title><body><table><tr><td dummy='value'></table></body></html>",
                desc(SyntaxTree.UNKNOWN_ATTRIBUTE_KEY, 48, 53, Description.WARNING));
    }

    public void testLogicalEndOfUnclosedTag() throws BadLocationException {
        String code = "<div></";
        //             01234567
        AstNode root = parse(code, null);

        AstNode divNode = root.children().get(0);
        AstNode errorNode = divNode.children().get(0);

        assertEquals(5, errorNode.startOffset());
        assertEquals(7, errorNode.endOffset());

        //check div logical end
        assertEquals(7, divNode.getLogicalRange()[1]);
    }

    public void testErrorDescriptionsOnOpenTags() throws Exception {
        //error descriptions attached to open tags should span either the whole
        //tag area if there are no attributes or just the opening symbol and
        //tag name if there are some.
        assertAST("<title>",
                desc(SyntaxTree.UNMATCHED_TAG, 0, 7, Description.WARNING));
        //         01234567
        assertAST("<title lang=''>",
                desc(SyntaxTree.UNMATCHED_TAG, 0, 6, Description.WARNING));
    }

    public void testTagsMatching() throws Exception {
        String code = "<table><tr><td></tr></table>";
        AstNode root = parse(code, null);

        AstNode otable = root.children().get(0);
        AstNode ctable = root.children().get(1);

        assertEquals(ctable, otable.getMatchingTag());
        assertEquals(otable, ctable.getMatchingTag());

        assertTrue(otable.needsToHaveMatchingTag());
        assertTrue(ctable.needsToHaveMatchingTag());

        List<AstNode> tch = otable.children();
        AstNode otr = tch.get(0);
        AstNode ctr = tch.get(1);

        assertEquals(otr, ctr.getMatchingTag());
        assertEquals(ctr, otr.getMatchingTag());

        assertFalse(otr.needsToHaveMatchingTag());
        assertTrue(ctr.needsToHaveMatchingTag());

        AstNode otd = otr.children().get(0);

        assertNull(otd.getMatchingTag());
        assertFalse(otd.needsToHaveMatchingTag());

    }


    //------------------------ private methods ---------------------------

    
    private void testSyntaxTree(String testFile) throws Exception {
        FileObject source = getTestFile(DATA_DIR_BASE + testFile);
        BaseDocument doc = getDocument(source);
        String code = doc.getText(0, doc.getLength());
        AstNode root = SyntaxParser.parse(code).getASTRoot();
        StringBuffer output = new StringBuffer();
        AstNodeUtils.dumpTree(root, output);
        assertDescriptionMatches(source, output.toString(), false, ".pass", true);
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

    private void assertLogicalRange(AstNode base, String pathToTheNode, int logicalStart, int logicalEnd) {
        AstNode node = AstNodeUtils.query(base, pathToTheNode);
        assertNotNull("Node " + pathToTheNode + " couldn't be found!", node);

        //probably not correct assumption, but ok for testing
        assertEquals(AstNode.NodeType.OPEN_TAG, node.type()); 

        int[] logicalRange = node.getLogicalRange();
        assertNotNull(logicalRange);        
        assertEquals(logicalStart, logicalRange[0]); //assert the start offset
        assertEquals(logicalEnd, logicalRange[1]); //assert the end offset
    }

    private AstNode assertAST(final String code) throws Exception {
        return assertAST(code,0);
    }

    private AstNode assertAST(final String code, int expectedErrorsNumber) throws Exception {
        return assertAST(code, null, expectedErrorsNumber);
    }

    private AstNode assertAST(final String code, String publicId, int expectedErrorsNumber) throws Exception {
        AstNode root = parse(code, publicId);
//        System.out.println("AST for code: " + code);
//        AstNodeUtils.dumpTree(root);

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

        return root;
    }

    private AstNode assertAST(final String code, AstNode.Description... expectedErrors) throws Exception {
        return assertAST(code, null, expectedErrors);
    }

    private AstNode assertAST(final String code, String publicId, AstNode.Description... expectedErrors) throws Exception {
        AstNode root = parse(code, publicId);
//        System.out.println("AST for code: " + code);
//        AstNodeUtils.dumpTree(root);

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

        return root;

    }

    private AstNode parse(String code, String publicId) throws BadLocationException {
        SyntaxParserResult result = SyntaxParser.parse(code);
        DTD dtd;
        if(publicId == null) {
            dtd = result.getDTD();
            assertNotNull("Likely invalid public id: " + result.getPublicID(), dtd);
        } else {
            dtd = Registry.getDTD(publicId, null);
            assertEquals(publicId, dtd.getIdentifier());
        }
        assertNotNull(dtd);
        return SyntaxTree.makeTree(result.getElements(), dtd);
    }

    private AstNode parseUnchecked(String code) throws BadLocationException {
        SyntaxParserResult result = SyntaxParser.parse(code);
        assertNotNull(result);
        return SyntaxTree.makeTree(result.getElements(), null);
    }

}
