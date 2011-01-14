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
package org.netbeans.editor.ext.html.parser.api;

import org.netbeans.editor.ext.html.parser.SyntaxAnalyzer;
import org.netbeans.editor.ext.html.parser.spi.AstNodeVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.parser.api.AstNode.NodeFilter;
import org.netbeans.editor.ext.html.parser.api.AstNode.NodeType;
import org.netbeans.editor.ext.html.test.TestBase;

/**
 *
 * @author mfukala@netbeans.org
 */
public class AstNodeUtilsTest extends TestBase {

    public static enum Match {

        EXACT, CONTAINS, DOES_NOT_CONTAIN, EMPTY, NOT_EMPTY;
    }

    public AstNodeUtilsTest(String testName) {
        super(testName);
    }

    
    @Override
    protected void setUp() throws Exception {
        HtmlVersionTest.setDefaultHtmlVersion(HtmlVersion.HTML41_TRANSATIONAL);
        super.setUp();
    }

    public static Test Xsuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new AstNodeUtilsTest("testFindClosestNodeBackward"));
        return suite;
    }

    public void testFindClosestNodeBackward() throws Exception {
        String code = "<p><a>text</a><b>xxx</b></p>";
        //             0123456789012345678901234567

        AstNode root = parse(code, null);
        assertNotNull(root);

        AstNode a = AstNodeUtils.query(root, "p/a");
        assertNotNull(a);
        AstNode b = AstNodeUtils.query(root, "p/b");
        assertNotNull(b);
        AstNode p = AstNodeUtils.query(root, "p");
        assertNotNull(p);

        NodeFilter filter = new NodeFilter() {
            @Override
            public boolean accepts(AstNode node) {
                return node.type() == NodeType.OPEN_TAG;
            }
        };

        assertEquals(a, AstNodeUtils.getClosestNodeBackward(root, 7, filter));
        assertEquals(a, AstNodeUtils.getClosestNodeBackward(root, 5, filter));
        assertEquals(a, AstNodeUtils.getClosestNodeBackward(root, 4, filter));
        assertEquals(a, AstNodeUtils.getClosestNodeBackward(root, 14, filter));

        assertEquals(b, AstNodeUtils.getClosestNodeBackward(root, 15, filter));
        assertEquals(b, AstNodeUtils.getClosestNodeBackward(root, 17, filter));
        assertEquals(b, AstNodeUtils.getClosestNodeBackward(root, 19, filter));
        assertEquals(b, AstNodeUtils.getClosestNodeBackward(root, 20, filter));
        assertEquals(b, AstNodeUtils.getClosestNodeBackward(root, 26, filter));

        assertEquals(p, AstNodeUtils.getClosestNodeBackward(root, 3, filter));
        assertEquals(p, AstNodeUtils.getClosestNodeBackward(root, 1, filter));
        assertEquals(root, AstNodeUtils.getClosestNodeBackward(root, 0, filter));


    }

    public void testFindDescendant() throws Exception {
        String code = "<p><a>text</a></p>";
        //             0123456789012345678

        AstNode root = parse(code, null);
        assertNotNull(root);

        assertDescendant(root, 0, "p", NodeType.OPEN_TAG, 0, 18);
        assertDescendant(root, 4, "a", NodeType.OPEN_TAG, 3, 14);
//        assertDescendant(root, 8, null, NodeType.TEXT, 6, 10);
        AstNode node = assertDescendant(root, 12, "a", NodeType.OPEN_TAG, 3, 14);
        AstNode adjusted = AstNodeUtils.getTagNode(node, 12);

        assertNotNull(adjusted);
        assertEquals(10, adjusted.startOffset());
        assertEquals(14, adjusted.endOffset());
        assertEquals(AstNode.NodeType.ENDTAG, adjusted.type());

        assertDescendant(root, 17, "p", NodeType.OPEN_TAG, 0, 18);

    }

    public void testFindDescendantInASTWithVirtualNodes() throws Exception {
        String code = "<!doctype html><p><a>text</a></p>";
        //             0123456789012345678901234567890123
        //             0         1         2         3


        AstNode root = parse(code, null);
        assertNotNull(root);

//        AstNodeUtils.dumpTree(root);

        assertDescendant(root, 15, "p", NodeType.OPEN_TAG, 15, 33);
        assertDescendant(root, 18, "a", NodeType.OPEN_TAG, 18, 29);
    }

    public void testQuery() throws Exception {
        String code = "<html><body><table><tr></tr><tr><td></tr></body></html>";
        //             0123456789012345678

        AstNode root = parse(code, null);
        assertNotNull(root);

        AstNode node = AstNodeUtils.query(root, "html");
        assertNotNull(node);
        assertEquals("html", node.name());

        node = AstNodeUtils.query(root, "html/body");
        assertNotNull(node);
        assertEquals("body", node.name());

        node = AstNodeUtils.query(root, "html/body/table");
        assertNotNull(node);
        assertEquals("table", node.name());

        node = AstNodeUtils.query(root, "html/body/table/tr");
        assertNotNull(node);
        assertEquals("tr", node.name());

        node = AstNodeUtils.query(root, "html/body/table/tr|1");
        assertNotNull(node);
        assertEquals("tr", node.name());

        node = AstNodeUtils.query(root, "html/body/table/tr|1/td");
        assertNotNull(node);
        assertEquals("td", node.name());
    }

     public void testNodeVisitors() throws Exception {
        String code = "<html><body><table><tr></tr><tr><td></tr></body></html>";
        //             0123456789012345678

        AstNode root = parse(code, null);
        assertNotNull(root);

//        AstNodeUtils.dumpTree(root);

        final int nodes[] = new int[1];
        AstNodeUtils.visitChildren(root, new AstNodeVisitor() {
            public void visit(AstNode node) {
                nodes[0]++;
            }
        });

        assertEquals(10, nodes[0]);

        nodes[0] = 0;
        AstNodeUtils.visitChildren(root, new AstNodeVisitor() {
            public void visit(AstNode node) {
                nodes[0]++;
            }
        }, AstNode.NodeType.OPEN_TAG);

        assertEquals(6, nodes[0]);

    }

    public void testGetPossibleOpenTagElements() throws BadLocationException, ParseException {
        String code = "<html><head><title></title></head><body>...<p>...</body></html>";
        //             0123456789012345678901234567890123456789012345678901234
        //             0         1         2         3         4         5

        AstNode root = parse(code, null);
        assertNotNull(root);

//        AstNodeUtils.dumpTree(root);

        //root node allows all dtd elements
        assertPossibleElements(root, 0, arr("a", "abbr", "html"), Match.CONTAINS);

        //inside html tag - nothing is offered inside the tag itself
        assertPossibleElements(root, 1, arr(), Match.EMPTY);

        //just after html tag
        assertPossibleElements(root, 6, arr("head"), Match.CONTAINS);

        //at the beginning of head tag
        assertPossibleElements(root, 12, arr("title", "meta"), Match.CONTAINS);
        //after title in head tag
        assertPossibleElements(root, 27, arr("meta"), Match.CONTAINS);
//        assertPossibleElements(root, 27, arr("title"), Match.DOES_NOT_CONTAIN);

        //just before body
        assertPossibleElements(root, 34, arr("body"), Match.CONTAINS);
        //inside body
        assertPossibleElements(root, 41, arr("p", "div"), Match.CONTAINS);

        //p can contain another p - will close the previous one with opt. end
        assertPossibleElements(root, 47, arr("p"), Match.CONTAINS);


    }

    public void testIssue169206() throws BadLocationException, ParseException {
        String code = "<html><head><title></title></head><body><table> </table></body></html>";
        //             0123456789012345678901234567890123456789012345678901234
        //             0         1         2         3         4         5

        AstNode root = parse(code, null);
        assertNotNull(root);

        assertPossibleElements(root, 47, arr("thead","tbody","tr"), Match.CONTAINS);


    }

    public void testIssue185837() throws BadLocationException, ParseException {
        String code = "<html><head><title></title></head><body><b><del>xxx</del></b></body></html>";
        //             0123456789012345678901234567890123456789012345678901234
        //             0         1         2         3         4         5

        AstNode root = parse(code, null);
        assertNotNull(root);

//        AstNodeUtils.dumpTree(root);

        //root node allows all dtd elements
        assertPossibleElements(root, 40, arr("del", "ins"), Match.CONTAINS);
        assertPossibleElements(root, 43, arr("del", "ins"), Match.CONTAINS);

    }

    public void testFindNodeVirtualNodes() throws BadLocationException, ParseException {
        String code = "<!doctype html><title>hi</title><div>buddy</div>";
        //             0123456789012345678901234567890123456789012345678901234
        //             0         1         2         3         4         5

        AstNode root = parse(code, null);
        assertNotNull(root);

//        AstNodeUtils.dumpTree(root);

        AstNode title = AstNodeUtils.query(root, "html/head/title");
        assertNotNull(title);

        //non logical range
        assertSame(title, AstNodeUtils.findNode(root, 17, true, false)); //middle
        assertSame(title, AstNodeUtils.findNode(root, 15, true, false)); //fw
        assertSame(title, AstNodeUtils.findNode(root, 22, false, false)); //bw

        //logical range
        assertSame(title, AstNodeUtils.findNode(root, 23, false, false));

        AstNode div = AstNodeUtils.query(root, "html/body/div");
        assertNotNull(div);
        //non logical range
        assertSame(div, AstNodeUtils.findNode(root, 35, true, false)); //middle
        assertSame(div, AstNodeUtils.findNode(root, 32, true, false)); //fw
        assertSame(div, AstNodeUtils.findNode(root, 37, false, false)); //bw

        //logical range
        assertSame(div, AstNodeUtils.findNode(root, 40, false, false));

    }

    public void testFindNodeByPhysicalRange() throws BadLocationException, ParseException {
        String code = "<html>  <body> nazdar </body> <div></html>";
        //             0123456789012345678901234567890123456789012345678901234
        //             0         1         2         3         4         5
        AstNode root = parse(code, null);
        assertNotNull(root);

//        AstNodeUtils.dumpTree(root);

        AstNode html = AstNodeUtils.query(root, "html");
        assertNotNull(html);
        AstNode htmlEnd = html.getMatchingTag();
        assertNotNull(htmlEnd);
        AstNode body = AstNodeUtils.query(root, "html/body");
        assertNotNull(body);
        AstNode bodyEnd = body.getMatchingTag();
        assertNotNull(bodyEnd);
        AstNode div = AstNodeUtils.query(root, "html/div");
        assertNotNull(div);

        assertNull(AstNodeUtils.findNode(root,7, true, true));

        //html open tag
        assertNull(AstNodeUtils.findNode(root,6, true, true)); //behind, look forward
        assertEquals(html, AstNodeUtils.findNode(root,6, false, true)); //behind, bw

        assertEquals(html, AstNodeUtils.findNode(root,0, true, true)); //before, fw
        assertNull(AstNodeUtils.findNode(root,0, false, true)); //before, look backward

        assertEquals(html, AstNodeUtils.findNode(root,3, true, true)); //middle, fw
        assertEquals(html, AstNodeUtils.findNode(root,3, false, true)); //middle, bw

        //body open tag
        assertNull(AstNodeUtils.findNode(root,14, true, true)); //behind, look forward
        assertEquals(body, AstNodeUtils.findNode(root,14, false, true)); //behind, bw

        assertEquals(body, AstNodeUtils.findNode(root,8, true, true)); //before, fw
        assertNull(AstNodeUtils.findNode(root,8, false, true)); //before, look backward

        assertEquals(body, AstNodeUtils.findNode(root,10, true, true)); //middle, fw
        assertEquals(body, AstNodeUtils.findNode(root,10, false, true)); //middle, bw

        //body end tag
        assertNull(AstNodeUtils.findNode(root,29, true, true)); //behind, look forward
        assertEquals(bodyEnd, AstNodeUtils.findNode(root,29, false, true)); //behind, bw

        assertEquals(bodyEnd, AstNodeUtils.findNode(root,22, true, true)); //before, fw
        assertNull(AstNodeUtils.findNode(root,22, false, true)); //before, look backward

        assertEquals(bodyEnd, AstNodeUtils.findNode(root,25, true, true)); //middle, fw
        assertEquals(bodyEnd, AstNodeUtils.findNode(root,25, false, true)); //middle, bw

        //div open tag
        assertNotNull(AstNodeUtils.findNode(root,35, true, true)); //behind, look forward //</html>
        assertEquals(div, AstNodeUtils.findNode(root,35, false, true)); //behind, bw

        assertEquals(div, AstNodeUtils.findNode(root,30, true, true)); //before, fw
        assertNull(AstNodeUtils.findNode(root,30, false, true)); //before, look backward

        assertEquals(div, AstNodeUtils.findNode(root,32, true, true)); //middle, fw
        assertEquals(div, AstNodeUtils.findNode(root,32, false, true)); //middle, bw

        //html end tag
        assertNull(AstNodeUtils.findNode(root,42, true, true)); //behind, look forward
        assertEquals(htmlEnd, AstNodeUtils.findNode(root,42, false, true)); //behind, bw

        assertEquals(htmlEnd, AstNodeUtils.findNode(root,35, true, true)); //before, fw
        assertNotNull(AstNodeUtils.findNode(root,35, false, true)); //before, look backward //<div>

        assertEquals(htmlEnd, AstNodeUtils.findNode(root,40, true, true)); //middle, fw
        assertEquals(htmlEnd, AstNodeUtils.findNode(root,40, false, true)); //middle, bw

        //out of content
        assertNull(AstNodeUtils.findNode(root,100, true, true));
        assertNull(AstNodeUtils.findNode(root,100, false, true));


    }

    private AstNode assertDescendant(AstNode searchedNode, int searchOffset, String name, AstNode.NodeType type, int from, int to) {
        return assertDescendant(searchedNode, searchOffset, true, name, type, from, to);
    }
    private AstNode assertDescendant(AstNode searchedNode, int searchOffset, boolean forward, String name, AstNode.NodeType type, int from, int to) {
        AstNode node = AstNodeUtils.findNode(searchedNode, searchOffset, forward, false);
        assertNotNull(node);
        assertEquals(name, node.name());
        assertEquals(type, node.type());
        int[] range = node.getLogicalRange();
        assertNotNull(range);
        assertEquals(from, range[0]);
        assertEquals(to, range[1]);

        return node;
    }

    private AstNode parse(String code, String publicId) throws BadLocationException, ParseException {
        HtmlSource source = new HtmlSource(code);
        SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();
        return result.parseHtml().root();
    }

    private void assertPossibleElements(AstNode rootNode, int offset, String[] expected, Match type) {
        Collection<DTD.Element> possible = AstNodeUtils.getPossibleOpenTagElements(rootNode, offset);
        assertNotNull(possible); 
        assertDTDElements(expected, possible, type);
    }

    private void assertDTDElements(String[] expected, Collection<DTD.Element> elements, Match type) {
        List<String> real = new ArrayList<String>();
        for (DTD.Element ccp : elements) {
            real.add(ccp.getName().toLowerCase());
        }
        List<String> exp = new ArrayList<String>(Arrays.asList(expected));

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

    private String[] arr(String... args) {
        return args;
    }
}
