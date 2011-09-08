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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.parser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.editor.ext.html.parser.api.AstNode.Attribute;
import org.netbeans.editor.ext.html.parser.SyntaxAnalyzer;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.api.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.api.HtmlSource;
import org.netbeans.editor.ext.html.parser.api.ParseException;
import org.netbeans.editor.ext.html.parser.spi.AstNodeVisitor;
import org.netbeans.editor.ext.html.parser.spi.HelpItem;
import org.netbeans.editor.ext.html.parser.spi.HtmlModel;
import org.netbeans.editor.ext.html.parser.spi.HtmlParseResult;
import org.netbeans.editor.ext.html.parser.spi.HtmlTag;
import org.netbeans.editor.ext.html.parser.spi.HtmlTagAttribute;
import org.netbeans.editor.ext.html.parser.spi.HtmlTagType;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.parser.model.ElementDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
public class Html5ParserTest extends NbTestCase {

    public Html5ParserTest(String name) {
        super(name);
    }

    public static Test Xsuite() {
        String testName = "testIsAttributeQuoted";

        System.err.println("Only " + testName + " test is going to be run!!!!");
        System.err.println("******************************************************\n");

//        AstNodeTreeBuilder.DEBUG = true;
//        AstNodeTreeBuilder.DEBUG_STATES = true;
        TestSuite suite = new TestSuite();
        suite.addTest(new Html5ParserTest(testName));
        return suite;
    }

    public void testaParseErrorneousHeadContent() throws ParseException {
        //the &lt; char after the title close tag makes the parse tree really bad
        String code = "<!doctype html>\n"
                + "<html>\n"
                + "<head>\n"
                + "<title>\n"
                + "</title> < \n"
                + "</head>\n"
                + "<body>\n"
                + "</body>\n"
                + "</html>\n";

//        AstNodeTreeBuilder.DEBUG_STATES = true;
        HtmlParseResult result = parse(code);
        AstNode root = result.root();
        assertNotNull(root);
//        AstNodeUtils.dumpTree(result.root());

//        Collection<ProblemDescription> problems = result.getProblems();
//        for(ProblemDescription pd : problems) {
//            System.out.println(pd);
//        }

    }

    public void testSimpleDocument() throws ParseException {
        String code = "<!doctype html><html><head><title>x</title></head><body><div onclick=\"alert();\"/></body></html>";
        //             012345678901234567890123456789012345678901234567890123456789012345678 901234567 8901234567890123456789
        //             0         1         2         3         4         5         6          7          8         9

//        AstNodeTreeBuilder.DEBUG_STATES = true;
        HtmlParseResult result = parse(code);
        AstNode root = result.root();
        assertNotNull(root);
//        AstNodeUtils.dumpTree(result.root());

        AstNode html = AstNodeUtils.query(root, "html");
        assertEquals("html", html.name());
        assertEquals(15, html.startOffset());
        assertEquals(21, html.endOffset());
        assertEquals(15, html.logicalStartOffset());
        assertEquals(95, html.logicalEndOffset());

        AstNode body = AstNodeUtils.query(root, "html/body");
        assertEquals("body", body.name());
        assertEquals(50, body.startOffset());
        assertEquals(56, body.endOffset());
        assertEquals(50, body.logicalStartOffset());
        assertEquals(88, body.logicalEndOffset());

        AstNode bodyEndTag = body.getMatchingTag();
        assertNotNull(bodyEndTag);
        assertSame(body, bodyEndTag.getMatchingTag());
        assertSame(bodyEndTag, body.getMatchingTag());

        AstNode title = AstNodeUtils.query(root, "html/head/title");
        assertEquals("title", title.name());
        assertEquals(27, title.startOffset());
        assertEquals(34, title.endOffset());
        assertEquals(27, title.logicalStartOffset());
        assertEquals(43, title.logicalEndOffset());

        AstNode titleEndTag = title.getMatchingTag();
        assertNotNull(titleEndTag);
        assertSame(title, titleEndTag.getMatchingTag());
        assertSame(titleEndTag, title.getMatchingTag());



    }

    public void testBasic() throws SAXException, IOException, ParseException {
        HtmlParseResult result = parse("<!doctype html><section><div></div></section>");
        AstNode root = result.root();
        assertNotNull(root);
        assertNotNull(AstNodeUtils.query(root, "html/body/section/div")); //html/body are generated

    }

    public void testHtmlAndBodyTags() throws ParseException {
        HtmlParseResult result = parse("<!DOCTYPE html><html><head><title>hello</title></head><body><div>ahoj</div></body></html>");
        AstNode root = result.root();
//        AstNodeUtils.dumpTree(root);
        assertNotNull(root);
        assertNotNull(AstNodeUtils.query(root, "html"));
        assertNotNull(AstNodeUtils.query(root, "html/head"));
        assertNotNull(AstNodeUtils.query(root, "html/head/title"));
        assertNotNull(AstNodeUtils.query(root, "html/body"));
        assertNotNull(AstNodeUtils.query(root, "html/body/div"));
    }

    public void testAttributes() throws ParseException {
        HtmlParseResult result = parse("<!DOCTYPE html><html><head><title>hello</title></head><body onclick=\"alert()\"></body></html>");
        AstNode root = result.root();
//        AstNodeUtils.dumpTree(root);
        assertNotNull(root);
        AstNode body = AstNodeUtils.query(root, "html/body");
        assertNotNull(body);

        assertEquals(1, body.getAttributes().size());

        Attribute attr = body.getAttributes().iterator().next();
        assertNotNull(attr);
        assertEquals("onclick", attr.name());
        assertEquals("\"alert()\"", attr.value());
    }

//    public void testProblemsReporting() throws ParseException {
//        HtmlParseResult result = parse("<!DOCTYPE html></section>");
//        //                              012345678901234567890123456789
//        //                              0         1         2
//        Collection<ProblemDescription> problems = result.getProblems();
//
//        assertEquals(1, problems.size());
//        ProblemDescription p = problems.iterator().next();
//
//        assertEquals(ProblemDescription.ERROR, p.getType());
//        assertEquals("nokey", p.getKey()); //XXX fix that
//        assertEquals("Stray end tag “section”.", p.getText());
//        assertEquals(15, p.getFrom());
//        assertEquals(25, p.getTo());
//
//    }
    public void testStyle() throws ParseException {
        String code = "<!DOCTYPE html>\n<style type=\"text/css\">\n@import \"resources2/ezcompik/newcss2moje.css\";\n</style>\n";
        //             0123456789012345 67890123456 7890123456 789 012345678 90123456789012345678 90123456789012 345678901 23456789
        //             0         1          2          3           4          5         6          7          8           9

//        AstNodeTreeBuilder.DEBUG = true;
//        AstNodeTreeBuilder.DEBUG_STATES = true;
        HtmlParseResult result = parse(code);
        AstNode root = result.root();
        assertNotNull(root);
//        AstNodeUtils.dumpTree(result.root());

        AstNode head = AstNodeUtils.query(root, "html/head");
        assertNotNull(head);
        assertEquals(2, head.children().size());
        AstNode styleOpenTag = head.children().get(0);

        assertNotNull(styleOpenTag);
        assertEquals(16, styleOpenTag.startOffset());
        assertEquals(38, styleOpenTag.endOffset());

        AstNode styleEndTag = head.children().get(1);
        assertNotNull(styleEndTag);
        assertEquals(87, styleEndTag.startOffset());
        assertEquals(95, styleEndTag.endOffset());

        assertSame(styleEndTag, styleOpenTag.getMatchingTag());
//        assertEquals(95, styleOpenTag.getLogicalRange()[1]);

    }

    public void testParseUnfinishedCode() throws ParseException {
        String code = "<!DOCTYPE HTML>"
                + "<html>"
                + "<head>"
                + "<title>"
                + "</title>"
                + "</head>"
                + "<body>"
                + "</table>"
                + "</html>";

        HtmlParseResult result = parse(code);
        AstNode root = result.root();

        assertNotNull(root);

//        AstNodeUtils.dumpTree(root);

        AstNode html = AstNodeUtils.query(root, "html");
        assertNotNull(html);

        Collection<AstNode> children = html.children();
        assertEquals(4, children.size()); // <head>, </head>,<body>,</table>
        Iterator<AstNode> childernItr = children.iterator();

        assertTrue(childernItr.hasNext());
        AstNode child = childernItr.next();
        assertEquals("head", child.name());
        assertEquals(AstNode.NodeType.OPEN_TAG, child.type());
        assertTrue(childernItr.hasNext());
        child = childernItr.next();
        assertEquals("head", child.name());
        assertEquals(AstNode.NodeType.ENDTAG, child.type());
        assertTrue(childernItr.hasNext());
        child = childernItr.next();
        assertEquals("body", child.name());
        assertEquals(AstNode.NodeType.OPEN_TAG, child.type());
        assertTrue(childernItr.hasNext());
        child = childernItr.next();
        assertEquals("table", child.name());
        assertEquals(AstNode.NodeType.ENDTAG, child.type());


//        AstNodeUtils.dumpTree(root);

    }

    public void testLogicalRangesOfUnclosedOpenTags() throws ParseException {
        HtmlParseResult result = parse("<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<title>hello</title>"
                + "</head>"
                + "<body>"
                + "<table>"
                + "</html>");
        AstNode root = result.root();

//        AstNodeUtils.dumpTree(root);

        assertNotNull(root);
        AstNode htmlOpen = AstNodeUtils.query(root, "html");
        assertNotNull(htmlOpen);
        AstNode htmlEnd = htmlOpen.getMatchingTag();
        assertNotNull(htmlEnd);

        assertNotNull(AstNodeUtils.query(root, "html/head"));
        assertNotNull(AstNodeUtils.query(root, "html/head/title"));
        AstNode body = AstNodeUtils.query(root, "html/body");
        assertNotNull(body);
        AstNode table = AstNodeUtils.query(root, "html/body/table");
        assertNotNull(table);

        //both body and table should be logically closed at the beginning of the html end tag
        assertEquals(htmlEnd.startOffset(), body.logicalEndOffset());
        assertEquals(htmlEnd.startOffset(), table.logicalEndOffset());
    }

    public void testGetPossibleOpenTagsInContext() throws ParseException {
        HtmlParseResult result = parse("<!DOCTYPE html><html><head><title>hello</title></head><body><div>ahoj</div></body></html>");

        assertNotNull(result.root());

        AstNode body = AstNodeUtils.query(result.root(), "html/body");
        Collection<HtmlTag> possible = result.getPossibleTagsInContext(body, true);

        assertTrue(!possible.isEmpty());

        HtmlTag divTag = new HtmlTagImpl("div");
        HtmlTag headTag = new HtmlTagImpl("head");

        assertTrue(possible.contains(divTag));
        assertFalse(possible.contains(headTag));

        AstNode head = AstNodeUtils.query(result.root(), "html/head");
        possible = result.getPossibleTagsInContext(head, true);

        assertTrue(!possible.isEmpty());

        HtmlTag titleTag = new HtmlTagImpl("title");
        assertTrue(possible.contains(titleTag));
        assertFalse(possible.contains(headTag));

        AstNode html = AstNodeUtils.query(result.root(), "html");
        possible = result.getPossibleTagsInContext(html, true);
        assertTrue(!possible.isEmpty());
        assertTrue(possible.contains(divTag));

    }

    public void testGetPossibleEndTagsInContext() throws ParseException {
        HtmlParseResult result = parse("<!DOCTYPE html><html><head><title>hello</title></head><body><div>ahoj</div></body></html>");

        assertNotNull(result.root());

        AstNode body = AstNodeUtils.query(result.root(), "html/body");
        Collection<HtmlTag> possible = result.getPossibleTagsInContext(body, false);

        assertTrue(!possible.isEmpty());

        HtmlTag htmlTag = new HtmlTagImpl("html");
        HtmlTag headTag = new HtmlTagImpl("head");
        HtmlTag bodyTag = new HtmlTagImpl("body");
        HtmlTag divTag = new HtmlTagImpl("div");

        Iterator<HtmlTag> possibleItr = possible.iterator();
        assertEquals(bodyTag, possibleItr.next());
        assertEquals(htmlTag, possibleItr.next());

        assertFalse(possible.contains(divTag));

        AstNode head = AstNodeUtils.query(result.root(), "html/head");
        possible = result.getPossibleTagsInContext(head, true);

        assertTrue(!possible.isEmpty());

        HtmlTag titleTag = new HtmlTagImpl("title");
        assertTrue(possible.contains(titleTag));
        assertFalse(possible.contains(headTag));

    }

    public void testAllowDialogInDiv() throws ParseException {
        HtmlParseResult result = parse("<!doctype html>"
                + "<html>\n"
                + "<title>title</title>\n"
                + "<body>\n"
                + "<div>\n"
                + "</div>\n"
                + "</body>\n"
                + "</html>\n");

        assertNotNull(result.root());

        AstNode div = AstNodeUtils.query(result.root(), "html/body/div");
        Collection<HtmlTag> possible = result.getPossibleTagsInContext(div, true);

        assertTrue(!possible.isEmpty());

        HtmlTag divTag = new HtmlTagImpl("div");
        HtmlTag dialogTag = new HtmlTagImpl("dialog");

        assertTrue(possible.contains(divTag));

        //fails - bug
//        assertFalse(possible.contains(dialogTag));

    }

    public void testParseUnfinishedTagFollowedByChars() throws ParseException {
        String code = "<!doctype html> \n"
                + "<html>    \n"
                + "<title>dd</title>\n"
                + "<b\n" //the tag is unfinished during typing
                + "      a\n" //this text is considered as the tag's attribute (correctly)
                + "</body>\n"
                + "</html> ";

        HtmlParseResult result = parse(code);
        AstNode root = result.root();

        assertNotNull(root);

//        AstNodeUtils.dumpTree(root);
    }

    public void testParseNotMatchingBodyTags() throws ParseException {
        String code = "<!doctype html>\n"
                + "<html>\n"
                + "<title></title>\n"
                + "<body>\n"
                + "</body>\n"
                + "</html>\n";

        HtmlParseResult result = parse(code);
        AstNode root = result.root();

        assertNotNull(root);

//        AstNodeUtils.dumpTree(root);
    }

    public void testParseFileLongerThan2048chars() throws ParseException {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 2048 * 3; i++) {
            b.append('*');
        }

        String code = "<!doctype html>\n"
                + "<html>\n"
                + "<title></title>\n"
                + "<body>\n"
                + b.toString()
                + "</body>\n"
                + "</html>\n";


//        AstNodeTreeBuilder.DEBUG_STATES = true;
        HtmlParseResult result = parse(code);
        AstNode root = result.root();

        assertNotNull(root);
//        AstNodeUtils.dumpTree(root);

        AstNode body = AstNodeUtils.query(result.root(), "html/body");
        assertNotNull(body);

        AstNode bodyEnd = body.getMatchingTag();
        assertNotNull(bodyEnd);

        assertEquals(6190, bodyEnd.startOffset());
        assertEquals(6197, bodyEnd.endOffset());

    }

    public void test_A_TagProblem() throws ParseException {
        //
        //java.lang.AssertionError
        //at org.netbeans.modules.html.parser.AstNodeTreeBuilder.elementPopped(AstNodeTreeBuilder.java:106)
        //

        //such broken source really confuses the html parser
        String code = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "     <title>HTML5 Demo: geolocation</title>\n"
                + "     <body>\n"
                + "         <a>  \n"
                + "         <footer>\n"
                + "             <a>\n"
                + "         </footer> \n"
                + "     </body>\n" //from some reason the tree builder pushes <a> open tag node here?!?!?
                + "</html>  \n";

        HtmlParseResult result = parse(code);
        AstNode root = result.root();

        assertNotNull(root);

//        AstNodeUtils.dumpTree(root);
    }

    public void testParseTreeAfterOpenedTag() throws ParseException {
        String code = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<title>HTML5 Demo: geolocation</title>\n"
                + "<body>\n"
                + "<div></\n"
                + "</body>\n"
                + "</html>\n";

        HtmlParseResult result = parse(code);
        AstNode root = result.root();

        assertNotNull(root);

//        AstNodeUtils.dumpTree(root);
    }

    public void testBodyTagHasNoParent() throws ParseException {
        String code = "<!doctype html> "
                + "<html> "
                + "<body >"
                + "      "
                + "<       "
                + "</body>"
                + "</html>";

        HtmlParseResult result = parse(code);
        AstNode root = result.root();

        assertNotNull(root);

        AstNode body = AstNodeUtils.query(result.root(), "html/body");
        assertNotNull(body);

        assertNotNull(body.parent());

//        AstNodeUtils.dumpTree(root);
    }

    public void testUnclosedTitleTag() throws ParseException {
        //this is causing all the nodes missing their logical ranges
        //the body tag is parsed as virtual only regardless the open and end
        //tag is present in the code.

        //looks like caused by the CDATA content of the unclosed title tag
        //which causes the parser consume everything after as unparseable code :-(
        String code = "<!doctype html><html><head><title></head><body></body></html>";
        //             0123456789012345678901234567890123456789012345678901234567890123456789
        //             0         1         2         3         4         5         6

        HtmlParseResult result = parse(code);
        AstNode root = result.root();

        assertNotNull(root);

        AstNode title = AstNodeUtils.query(result.root(), "html/head/title");
        assertNotNull(title);

//        AstNodeUtils.dumpTree(root);

        //FAILING - http://netbeans.org/bugzilla/show_bug.cgi?id=190183

//        assertTrue(title.logicalEndOffset() != -1);

    }

    public void testOnlyDivInFile() throws ParseException {
        String code = "<!doctype html><html><head><title>x</title></head><body><di </body></html>";
        HtmlParseResult result = parse(code);
        AstNode root = result.root();

        assertNotNull(root);

//        AstNodeUtils.dumpTree(root);
    }

    public void testMaskTemplatingMarksPattern() throws ParseException {
        String code = "@@@<div> @@@ </div>";
        assertEquals("   <div>     </div>", Html5Parser.maskTemplatingMarks(code));
    }

    public void testParseFileTest1() throws ParseException {
        parse(getTestFile("testfiles/test1.html"));
    }

    public void testParseFileTest2() throws ParseException {
        parse(getTestFile("testfiles/test2.html"));
    }

    public void testParseFileTest3() throws ParseException {
        parse(getTestFile("testfiles/test3.html"));
    }

    public void testParseFileTest4() throws ParseException {
        parse(getTestFile("testfiles/test4.html"));
    }

    public void testParseFileTest5() throws ParseException {
        parse(getTestFile("testfiles/test5.html"));
    }

    protected FileObject getTestFile(String relFilePath) {
        File wholeInputFile = new File(getDataDir(), relFilePath);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(wholeInputFile);
        assertNotNull(fo);

        return fo;
    }

    public void testHtml5Model() throws ParseException {
        String code = "<!doctype html><title>hi</title>";
        HtmlParseResult result = parse(code);

        HtmlModel model = result.model();
        assertNotNull(model);
        assertEquals("html5model", model.getModelId());

        Collection<HtmlTag> all = model.getAllTags();
        assertNotNull(all);
        assertEquals(ElementDescriptor.values().length, all.size());

        HtmlTag table = HtmlTagProvider.getTagForElement("table");
        assertNotNull(table);

        assertTrue(all.contains(table));

        //try to modify the unmodifiable collection
        try {
            all.remove(table);
            assertTrue("The tags collection can be modified!", false);
        } catch (UnsupportedOperationException t) {
            //ok
        }


    }

    //unknown node 't' has no parent set
    public void testParseUnknownElementInTable() throws ParseException {
        String code = "<!doctype html>"
                + "<html>"
                + "<head><title></title></head>"
                + "<body>"
                + "<table>"
                + "<t>"
                + "</table>"
                + "</body>"
                + "</html>";
//      AstNodeTreeBuilder.DEBUG = true;
        HtmlParseResult result = parse(code);
        AstNode root = result.root();

//      AstNodeUtils.dumpTree(root);

        //the 't' node is foster parented, so it goes to the table's parent, not table itself
        AstNode t = AstNodeUtils.query(root, "html/body/t");
        assertNotNull(t);
        AstNode body = AstNodeUtils.query(root, "html/body");
        assertNotNull(body);

        assertEquals(body, t.parent());

    }

    public void testDivLogicalEndAtTheEOF() throws ParseException {
        String code = "<!doctype html><div><div></div>";
        //             0123456789012345678901234567890123456789
        //                                           ^
//        AstNodeTreeBuilder.DEBUG = true;
        HtmlParseResult result = parse(code);
        AstNode root = result.root();

//        AstNodeUtils.dumpTree(root);

        //the 't' node is foster parented, so it goes to the table's parent, not table itself
        AstNode div = AstNodeUtils.query(root, "html/body/div");
        assertNotNull(div);

        assertEquals(30, div.logicalEndOffset());

        code = "<!doctype html><div><div></</div>";
        //      0123456789012345678901234567890123456789
        //                                      ^
//        AstNodeTreeBuilder.DEBUG = true;
        result = parse(code);
        root = result.root();

//        AstNodeUtils.dumpTree(root);

        //the 't' node is foster parented, so it goes to the table's parent, not table itself
        div = AstNodeUtils.query(root, "html/body/div");
        assertNotNull(div);

        assertEquals(32, div.logicalEndOffset());

        code = "<!doctype html><div></";
        //        0123456789012345678901234567890123456789
        //                             ^
//        AstNodeTreeBuilder.DEBUG = true;
        result = parse(code);
        root = result.root();

//        AstNodeUtils.dumpTree(root);

        //the 't' node is foster parented, so it goes to the table's parent, not table itself
        div = AstNodeUtils.query(root, "html/body/div");
        assertNotNull(div);

        assertEquals(21, div.logicalEndOffset());

    }

    //Bug 191873 - IllegalStateException: Stack's top root:ROOT(0-186){} is not the same as <*head>(34-40/47){}
    public void testNodePopWithoutPush() throws ParseException {
        String code = "<!doctype html>"
                + "<head></head>"
                + "<meta charset=\"utf-8\" />";

//        AstNodeTreeBuilder.setLoggerLevel(Level.FINER);
        HtmlParseResult result = parse(code);
        AstNode root = result.root();

        assertNotNull(root);
//        AstNodeUtils.dumpTree(root);

    }

    //Bug 193268 - AssertionError: Unexpected node type ENDTAG
    public void testScriptTagInBody() throws ParseException {
        String scriptOpenTag = "<script type=\"text/javascript\" src=\"test.js\">";
        //                   0123456789012 3456789012345678 901234 56789012 345678901234567890123456789
        //                   0         1          2          3          4          5
        String code = "<!doctype html>"
                + "<html>"
                + "<head>"
                + "<title></title>"
                + "</head>"
                + "<body>"
                + "<canvas>"
                + "<a/>"
                + "</canvas>"
                + scriptOpenTag + "</script>"
                + "</body>"
                + "</html>";

        AstNodeTreeBuilder.setLoggerLevel(Level.FINER);
        HtmlParseResult result = parse(code);
        AstNode root = result.root();

        assertNotNull(root);
//        AstNodeUtils.dumpTree(root);

        AstNode scriptOpen = AstNodeUtils.query(root, "html/body/script");
        assertNotNull(scriptOpen);

        assertEquals(76, scriptOpen.startOffset());
        assertEquals(76 + 45, scriptOpen.endOffset());

        AstNode scriptEnd = scriptOpen.getMatchingTag();
        assertEquals(121, scriptEnd.startOffset());
        assertEquals(130, scriptEnd.endOffset());

    }

    //[Bug 195103] Refactoring changes a changed filename incorrectly in the html <script> tag
    public void testIsAttributeQuoted() throws ParseException {
        String code = "<!doctype html>"
                + "<html>"
                + "<head>"
                + "<title></title>"
                + "</head>"
                + "<body>"
                + "<div onclick=\"alert()\">x</div>"
                + "<p onclick='alert()'>x</p>"
                + "<a onclick=alert>x</a>"
                + "</body>"
                + "</html>";

        HtmlParseResult result = parse(code);
        AstNode root = result.root();

        assertNotNull(root);
//        AstNodeUtils.dumpTree(root);

        AstNode div = AstNodeUtils.query(root, "html/body/div");
        assertNotNull(div);

        AstNode.Attribute attr = div.getAttribute("onclick");
        assertNotNull(attr);
        assertTrue(attr.isValueQuoted());

        AstNode p = AstNodeUtils.query(root, "html/body/p");
        assertNotNull(p);

        attr = p.getAttribute("onclick");
        assertNotNull(attr);
        assertTrue(attr.isValueQuoted());

        AstNode a = AstNodeUtils.query(root, "html/body/a");
        assertNotNull(a);

        attr = a.getAttribute("onclick");
        assertNotNull(attr);
        assertFalse(attr.isValueQuoted());

    }

    //fails
//     //Bug 194037 - AssertionError at nu.validator.htmlparser.impl.TreeBuilder.endTag
//    public void testIssue194037() throws ParseException {
//        String code = "<FRAMESET></FRAMESET></HTML><FRAMESET></FRAMESET>";
//
//        AstNodeTreeBuilder.setLoggerLevel(Level.FINER);
//        HtmlParseResult result = parse(code);
//        AstNode root = result.root();
//
//        assertNotNull(root);
//        AstNodeUtils.dumpTree(root);
//
//    }
//    public void test_parse_safari_css_properties_spec() throws ParseException {
//        //source:
//        //http://developer.apple.com/library/safari/#documentation/appleapplications/reference/SafariCSSRef/Articles/StandardCSSProperties.html#//apple_ref/doc/uid/%28null%29-SW1
//        File file = new File("/Users/marekfukala/parse.html");
//        HtmlSource source = new HtmlSource(FileUtil.toFileObject(file));
//        final CharSequence code = source.getSourceCode();
//        HtmlParseResult result = SyntaxAnalyzer.create(source).analyze().parseHtml();
//
//        AstNode root = result.root();
//
//        AstNodeVisitor v = new AstNodeVisitor() {
//
//            public void visit(AstNode node) {
//                if (node.name().contains("section")) {
//                    AstNode h3 = AstNodeUtils.query(node, "h3");
//                    if (h3 == null) {
//                        return;
//                    }
//                    AstNode h3end = h3.getMatchingTag();
//                    System.out.print(code.subSequence(h3.endOffset(), h3end.startOffset()));
//                } else if (node.name().equals("dl")) {
//                    AstNode.Attribute clazzattr = node.getAttribute("class");
//                    if (clazzattr != null && "content_text".equals(clazzattr.unquotedValue())) {
//                        AstNode strong = AstNodeUtils.query(node, "b/strong");
//                        if(strong != null && "Support Level".contains(getTextContent(code, strong))) {
//                            AstNode p = AstNodeUtils.query(node, "dd/p");
//                            if(p != null) {
//                                System.out.println("=" + getTextContent(code, p));
//                            }
//                        }
//                    }
//                }
//            }
//        };
//                
//        AstNodeUtils.visitChildren(root, v);
//
//    }
//    
//    private CharSequence getTextContent(CharSequence source, AstNode openTag) {
//        AstNode endTag = openTag.getMatchingTag();
//        if(endTag != null) {
//            return source.subSequence(openTag.endOffset(), endTag.startOffset());
//        }
//        
//        return null;
//    }

    private HtmlParseResult parse(FileObject file) throws ParseException {
        HtmlSource source = new HtmlSource(file);
        HtmlParseResult result = SyntaxAnalyzer.create(source).analyze().parseHtml();

        assertNotNull(result);

        return result;
    }

    private HtmlParseResult parse(CharSequence code) throws ParseException {
        HtmlSource source = new HtmlSource(code);
        HtmlParseResult result = SyntaxAnalyzer.create(source).analyze().parseHtml();

        assertNotNull(result);

        return result;
    }

    private static class HtmlTagImpl implements HtmlTag {

        private String name;

        public HtmlTagImpl(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof HtmlTag)) {
                return false;
            }
            final HtmlTag other = (HtmlTag) obj;
            if ((this.name == null) ? (other.getName() != null) : !this.name.equals(other.getName())) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "HtmlTagImpl{" + "name=" + name + '}';
        }

        public Collection<HtmlTagAttribute> getAttributes() {
            return Collections.emptyList();
        }

        public boolean isEmpty() {
            return false;
        }

        public boolean hasOptionalOpenTag() {
            return false;
        }

        public boolean hasOptionalEndTag() {
            return false;
        }

        public HtmlTagAttribute getAttribute(String name) {
            return null;
        }

        public HtmlTagType getTagClass() {
            return HtmlTagType.HTML;
        }

        public Collection<HtmlTag> getChildren() {
            return Collections.emptyList();
        }

        public HelpItem getHelp() {
            return null;
        }
    }
}
