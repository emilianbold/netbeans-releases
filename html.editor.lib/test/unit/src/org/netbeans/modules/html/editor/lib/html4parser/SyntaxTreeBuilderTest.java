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
package org.netbeans.modules.html.editor.lib.html4parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.html.editor.lib.api.*;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor;
import org.netbeans.modules.html.editor.lib.test.TestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mfukala@netbeans.org
 */
public class SyntaxTreeBuilderTest extends TestBase {

    private static final String DATA_DIR_BASE = "testfiles/syntaxtree/";

    public SyntaxTreeBuilderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        HtmlVersionTest.setDefaultHtmlVersion(HtmlVersion.HTML41_TRANSATIONAL);
        super.setUp();
    }



    public static Test xsuite(){
	TestSuite suite = new TestSuite();
        suite.addTest(new SyntaxTreeBuilderTest("testUnclosedDivTag_Issue191286"));
        return suite;
    }

//    public void testTrivialCase() throws Exception {
//        testSyntaxTree("trivial.html");
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
//    public void testIssue145821() throws Exception{
//        testSyntaxTree("issues145821.html");
//    }

//    public void testUncheckedAST() throws BadLocationException {
//        String code = "<div><a><b></a></b></div>text";
//        //             01234567
//        AstNode root = parseUnchecked(code);
//
////        System.out.println(AstNodeUtils.dumpTree(root));
//
//        AstNode div = AstNodeUtils.query(root, "div");
//        assertNotNull(div);
//
//        AstNode a = AstNodeUtils.query(root, "div/a");
//        assertNotNull(a);
//
//        AstNode b = AstNodeUtils.query(root, "div/a/b");
//        assertNotNull(b);
//
//        assertEquals(3, div.children().size()); //<a>,</a> and unmatched </b>
//        assertEquals(a, div.children().get(0));
//
//        assertEquals(1, a.children().size()); //<b>
//        assertEquals(b, a.children().get(0));
//
//
//    }

    
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
                desc(SyntaxTreeBuilder.UNRESOLVED_TAG_KEY, 6,12, ProblemDescription.ERROR));
        //         0123456789012345678901234567890123456789
        //         0         1         2         3

        //missing BODY
        assertAST("<html><head><title></title></head></html>",
                desc(SyntaxTreeBuilder.UNRESOLVED_TAG_KEY, 0,6, ProblemDescription.ERROR));
        //         0123456789012345678901234567890123456789
        //         0         1         2         3

        //unresolved HTML - missing HEAD + unexpected BODY - missing HEAD
        assertAST("<html><body></body></html>",
                desc(SyntaxTreeBuilder.UNRESOLVED_TAG_KEY, 0, 6, ProblemDescription.ERROR),
                desc(SyntaxTreeBuilder.UNEXPECTED_TAG_KEY, 6, 12, ProblemDescription.ERROR));
    }

    public void testUnmatchedTagBecauseOfOptionalEndTag() throws Exception {
        //last </P> end tag is unmatched
        assertAST("<p><p></p></p>",
                desc(SyntaxTreeBuilder.UNMATCHED_TAG, 10,14, ProblemDescription.ERROR));
        //         0123456789012345678901234567890123456789
        //         0         1         2         3

        assertAST("<p><div></div></p>",
                desc(SyntaxTreeBuilder.UNMATCHED_TAG, 14, 18, ProblemDescription.ERROR));
    }

    public void testOptionalEndTag() throws Exception {
        //HEAD has optional end tag
        assertAST("<html><head><title></title><body></body></html>");

        //test invalid element after optional end
        assertAST("<html><head><title></title><div><body></body></html>",
                desc(SyntaxTreeBuilder.UNEXPECTED_TAG_KEY, 27, 32, ProblemDescription.ERROR));
    }

    public void testUnexpectedTag() throws Exception {
        //TR cannot contain TR - needs (TD|TH)+
        assertAST("<table><tr><tr></table>",
                desc(SyntaxTreeBuilder.UNRESOLVED_TAG_KEY, 7, 11, ProblemDescription.ERROR),
                desc(SyntaxTreeBuilder.UNEXPECTED_TAG_KEY, 11, 15, ProblemDescription.ERROR),
                desc(SyntaxTreeBuilder.UNRESOLVED_TAG_KEY, 11, 15, ProblemDescription.ERROR)
                );

        //STYLE is not allowed in BODY; Issue 164903
        ProblemDescription[] expectedErrors = new ProblemDescription[]{
            desc(SyntaxTreeBuilder.UNEXPECTED_TAG_KEY, 40, 46, ProblemDescription.ERROR),
            desc(SyntaxTreeBuilder.UNMATCHED_TAG, 55, 63, ProblemDescription.ERROR)
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
                desc(SyntaxTreeBuilder.MISSING_REQUIRED_END_TAG, 45, 52, ProblemDescription.ERROR),
                desc(SyntaxTreeBuilder.UNMATCHED_TAG, 66, 71, ProblemDescription.ERROR),
                desc(SyntaxTreeBuilder.UNMATCHED_TAG, 71, 76, ProblemDescription.ERROR),
                desc(SyntaxTreeBuilder.UNMATCHED_TAG, 76, 84, ProblemDescription.ERROR),
                desc(SyntaxTreeBuilder.UNMATCHED_TAG, 84, 90, ProblemDescription.ERROR)
                );

    }
    public void testUnknownTags() throws Exception {
        assertAST("<xx></xx>",
                desc(SyntaxTreeBuilder.UNKNOWN_TAG_KEY, 0, 4, ProblemDescription.WARNING),
                desc(SyntaxTreeBuilder.UNKNOWN_TAG_KEY, 4, 9, ProblemDescription.WARNING));
    }

    public void testUnknownTagsMatching() throws Exception {
        assertAST("<xx>",
                desc(SyntaxTreeBuilder.UNKNOWN_TAG_KEY, 0, 4, ProblemDescription.WARNING));
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
                desc(SyntaxTreeBuilder.UNMATCHED_TAG, 29, 36, ProblemDescription.ERROR));
    }

    public void testEmptyXhtmlTags() throws Exception{
        assertAST("<!doctype public \"-//W3C//DTD XHTML 1.0 Strict//EN\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta content=''></meta><title></title></head><body></body></html>");
    }

    public void testOptinalEndTagsInTable() throws Exception{
        assertAST("<html><head><title></title></head><body>" +
                "<table><tr><td>r1c1<tr><td>r2c2</table>" +
                "</body></html>");

        //error: unresolved last <tr> tag
        assertAST("<html><head><title></title></head><body>" +
                "<table><tr><td>r1c1<tr><td>r2c2<tr></table>" +
                "</body></html>",
                desc(SyntaxTreeBuilder.UNRESOLVED_TAG_KEY, 71, 75, ProblemDescription.ERROR));
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
//        AstNodeUtils.dumpTree(root);

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
                desc(SyntaxTreeBuilder.MISSING_REQUIRED_ATTRIBUTES, 58, 65, ProblemDescription.WARNING), //style attr type required
                desc(SyntaxTreeBuilder.UNEXPECTED_TAG_KEY, 58, 65, ProblemDescription.ERROR), //style should be here
                desc(SyntaxTreeBuilder.UNMATCHED_TAG, 65, 73, ProblemDescription.ERROR), // </style> is unmatched
                desc(SyntaxTreeBuilder.UNRESOLVED_TAG_KEY, 73, 77, ProblemDescription.ERROR)); //<tr> doesn't contain required content
        
//        AstNodeUtils.dumpTree(root);

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
        assertAST("<!doctype public \"-//W3C//DTD XHTML 1.0 Strict//EN\"><html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:ui="+
                "\"http://java.sun.com/jsf/facelets\"><head><meta content=\"\"></meta>"+
                "<title></title></head><body></body></html>",
                "-//W3C//DTD XHTML 1.0 Strict//EN");
    }

    public void testMissingRequiredAttribute() throws Exception{
        //missing content attribute of meta tag
        assertAST("<html><head><title></title><meta></head><body>" +
                "<table><tr><td>r1c1<tr><td>r2c2</table>" +
                "</body></html>",
                desc(SyntaxTreeBuilder.MISSING_REQUIRED_ATTRIBUTES, 27, 33, ProblemDescription.WARNING));
    }

    public void testUnknownAttribute() throws Exception{
        assertAST("<html><head><title></title><body dummy='value'></body></html>",
                desc(SyntaxTreeBuilder.UNKNOWN_ATTRIBUTE_KEY, 33, 38, ProblemDescription.WARNING));

        //try it also in optional end tag
        assertAST("<html><head><title></title><body><table><tr><td dummy='value'></table></body></html>",
                desc(SyntaxTreeBuilder.UNKNOWN_ATTRIBUTE_KEY, 48, 53, ProblemDescription.WARNING));
    }

    public void testLogicalEndOfUnclosedTag() throws BadLocationException, ParseException {
        String code = "<div></";
        //             01234567
        AstNode root = parse(code, null);

        AstNode divNode = (AstNode)root.children().iterator().next();
        AstNode errorNode = (AstNode)divNode.children().iterator().next();

        assertEquals(5, errorNode.from());
        assertEquals(7, errorNode.to());

        //check div logical end
        assertEquals(7, divNode.getLogicalRange()[1]);
    }

    public void testErrorDescriptionsOnOpenTags() throws Exception {
        //error descriptions attached to open tags should span either the whole
        //tag area if there are no attributes or just the opening symbol and
        //tag name if there are some.
        assertAST("<title>",
                desc(SyntaxTreeBuilder.UNMATCHED_TAG, 0, 7, ProblemDescription.ERROR));
        //         01234567
        assertAST("<title lang=''>",
                desc(SyntaxTreeBuilder.UNMATCHED_TAG, 0, 6, ProblemDescription.ERROR));
    }

    public void testTagsMatching() throws Exception {
        String code = "<table><tr><td></tr></table>";
        AstNode root = parse(code, null);

        Iterator<Node> ch = root.children().iterator();
        AstNode otable = (AstNode)ch.next();
        AstNode ctable = (AstNode)ch.next();

        assertEquals(ctable, otable.getMatchingTag());
        assertEquals(otable, ctable.getMatchingTag());

        assertTrue(otable.needsToHaveMatchingTag());
        assertTrue(ctable.needsToHaveMatchingTag());

        Iterator<Node> tch = otable.children().iterator();
        AstNode otr = (AstNode)tch.next();
        AstNode ctr = (AstNode)tch.next();

        assertEquals(otr, ctr.getMatchingTag());
        assertEquals(ctr, otr.getMatchingTag());

        assertFalse(otr.needsToHaveMatchingTag());
        assertTrue(ctr.needsToHaveMatchingTag());

        AstNode otd = (AstNode)otr.children().iterator().next();

        assertNull(otd.getMatchingTag());
        assertFalse(otd.needsToHaveMatchingTag());

    }

//    public void testBigFile() throws Exception {
//        testSyntaxTree("big.html");
//    }

    public void testComment() throws BadLocationException, ParseException {
        String code = "<!-- comment -->";
        AstNode root = parse(code, null);

        assertEquals(1, root.children().size());
        AstNode commentNode = (AstNode)root.children().iterator().next();

        assertEquals(ElementType.COMMENT, commentNode.type());
        assertEquals(commentNode.logicalStartOffset(), 0);
        assertEquals(commentNode.logicalEndOffset(), code.length());
    }

    public void testIssue185837() throws Exception {
        String code = "<html><head><title></title></head><body><b><del></del></b></body></html>";
        assertAST(code);
    }

    public void testUnclosedDivTag_Issue191286() throws Exception {
        //         0123456789
        assertAST("<div</div>",
                desc(SyntaxAnalyzer.UNEXPECTED_SYMBOL_IN_OPEN_TAG, 4, 6, ProblemDescription.ERROR));

        //         0123456789
        assertAST("<div </div>",
                desc(SyntaxAnalyzer.UNEXPECTED_SYMBOL_IN_OPEN_TAG, 5, 7, ProblemDescription.ERROR));

        //         01234567890
        assertAST("<div name</div>",
                desc(SyntaxAnalyzer.UNEXPECTED_SYMBOL_IN_OPEN_TAG, 9, 11, ProblemDescription.ERROR));

        //         012345678901
        assertAST("<div id=</div>",
                desc(SyntaxAnalyzer.UNEXPECTED_SYMBOL_IN_OPEN_TAG, 9, 11, ProblemDescription.ERROR),
                desc(SyntaxTreeBuilder.UNMATCHED_TAG, 0, 4, ProblemDescription.ERROR));

    }

    //------------------------ private methods ---------------------------

    
    private void testSyntaxTree(String testFile) throws Exception {
        FileObject source = getTestFile(DATA_DIR_BASE + testFile);
        BaseDocument doc = getDocument(source);
        String code = doc.getText(0, doc.getLength());

        HtmlSource htmlsource = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(htmlsource).analyze();

        AstNode root = (AstNode)result.parseHtml().root();

        StringBuffer output = new StringBuffer();
        ElementUtils.dumpTree(root, output);
        assertDescriptionMatches(source, output.toString(), false, ".pass", true);
    }

    private ProblemDescription desc(String key, int from, int to, int type) {
        return ProblemDescription.create(key, null, type, from, to);
    }

    private String errorsAsString(List<ProblemDescription> errors) {
        StringBuilder buf = new StringBuilder();
        for(Iterator<ProblemDescription> i = errors.listIterator(); i.hasNext() ;) {
            buf.append(i.next().dump(null));
            if(i.hasNext()) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }

    private void assertLogicalRange(AstNode base, String pathToTheNode, int logicalStart, int logicalEnd) {
        AstNode node = (AstNode)ElementUtils.query(base, pathToTheNode);
        assertNotNull("Node " + pathToTheNode + " couldn't be found!", node);

        //probably not correct assumption, but ok for testing
        assertEquals(ElementType.OPEN_TAG, node.type()); 

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
        final List<ProblemDescription> errorslist = new ArrayList<ProblemDescription>();
        ElementVisitor visitor = new ElementVisitor() {
            public void visit(Node node) {
                for(ProblemDescription d : node.problems()) {
                    errorslist.add(d);
                    errors[0]++;
                }
            }
        };
        ElementUtils.visitChildren(root, visitor);

        assertEquals("Unexpected number of errors, current errors: " + errorsAsString(errorslist) ,expectedErrorsNumber, errors[0]);

        return root;
    }

    private AstNode assertAST(final String code, ProblemDescription... expectedErrors) throws Exception {
        return assertAST(code, null, expectedErrors);
    }

    private AstNode assertAST(final String code, String publicId, ProblemDescription... expectedErrors) throws Exception {
        AstNode root = parse(code, publicId);
//        System.out.println("AST for code: " + code);
//        AstNodeUtils.dumpTree(root);

        final Iterator<ProblemDescription> errorsItr = Arrays.asList(expectedErrors).listIterator();
        ElementVisitor visitor = new ElementVisitor() {
            public void visit(Node node) {
                for(ProblemDescription d : node.problems()) {
                    assertTrue("Unexpected error description: " + d.dump(code), errorsItr.hasNext());
                    assertEquals(errorsItr.next(), d);
                }
            }
        };

        ElementUtils.visitChildren(root, visitor);

        List<ProblemDescription> missing = new ArrayList<ProblemDescription>();
        while(errorsItr.hasNext()) {
            missing.add(errorsItr.next());
        }
        assertEquals("Some expected errors are missing: " + errorsAsString(missing), expectedErrors.length, expectedErrors.length - missing.size());

        return root;

    }

    private AstNode parse(String code, String publicId) throws BadLocationException, ParseException {
        HtmlSource source = new HtmlSource(code);
//        HtmlVersion version = HtmlVersion.findByPublicId(publicId);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();
        return (AstNode)result.parseHtml().root();
    }

//    private AstNode parseUnchecked(String code) throws BadLocationException {
//        SyntaxParserContext context = SyntaxParserContext.createContext(code);
//        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(context);
//        assertNotNull(result);
//        assertNull(context.getDTD());
//        return SyntaxTreeBuilder.makeTree(context);
//    }

}
