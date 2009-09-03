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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.Registry;
import org.netbeans.editor.ext.html.parser.AstNode.NodeType;
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

    public static Test xsuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new AstNodeUtilsTest("testGetPossibleOpenTagElements"));
        return suite;
    }

    public void testFindDescendant() throws Exception {
        String code = "<p><a>text</a></p>";
        //             0123456789012345678

        AstNode root = parse(code, null);
        assertNotNull(root);

        assertDescendant(root, 0, "p", NodeType.OPEN_TAG, 0, 18);
        assertDescendant(root, 4, "a", NodeType.OPEN_TAG, 3, 14);
        assertDescendant(root, 8, null, NodeType.TEXT, 6, 10);
        AstNode node = assertDescendant(root, 12, "a", NodeType.OPEN_TAG, 3, 14);
        AstNode adjusted = AstNodeUtils.getTagNode(node, 12);

        assertNotNull(adjusted);
        assertEquals(10, adjusted.startOffset());
        assertEquals(14, adjusted.endOffset());
        assertEquals(AstNode.NodeType.ENDTAG, adjusted.type());

        assertDescendant(root, 17, "p", NodeType.OPEN_TAG, 0, 18);

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

    public void testGetPossibleOpenTagElements() throws BadLocationException {
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
        assertPossibleElements(root, 6, arr("head"), Match.EXACT);

        //at the beginning of head tag
        assertPossibleElements(root, 12, arr("title", "meta"), Match.CONTAINS);
        //after title in head tag
        assertPossibleElements(root, 27, arr("meta"), Match.CONTAINS);
        assertPossibleElements(root, 27, arr("title"), Match.DOES_NOT_CONTAIN);

        //just before body
        assertPossibleElements(root, 34, arr("body"), Match.CONTAINS);
        //inside body
        assertPossibleElements(root, 41, arr("p", "div"), Match.CONTAINS);

        //p can contain another p - will close the previous one with opt. end
        assertPossibleElements(root, 47, arr("p"), Match.CONTAINS);


    }

    private AstNode assertDescendant(AstNode searchedNode, int searchOffset, String name, AstNode.NodeType type, int from, int to) {
        AstNode node = AstNodeUtils.findDescendant(searchedNode, searchOffset);
        assertNotNull(node);
        assertEquals(name, node.name());
        assertEquals(type, node.type());
        int[] range = node.getLogicalRange();
        assertNotNull(range);
        assertEquals(from, range[0]);
        assertEquals(to, range[1]);

        return node;
    }

    private AstNode parse(String code, String publicId) throws BadLocationException {
        SyntaxParserResult result = SyntaxParser.parse(code);
        DTD dtd;
        if (publicId == null) {
            dtd = result.getDTD();
        } else {
            dtd = Registry.getDTD(publicId, null);
            assertEquals(publicId, dtd.getIdentifier());
        }

        assertNotNull(dtd);
        return SyntaxTree.makeTree(result.getElements(), dtd);
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
