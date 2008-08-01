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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jruby.ast.DStrNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.IterNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeType;
import org.jruby.ast.StrNode;
import org.jruby.ast.types.INameNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @todo Lots of other methods to test!
 *  
 * @author Tor Norbye
 */
public class AstUtilitiesTest extends RubyTestBase {
    
    public AstUtilitiesTest(String testName) {
        super(testName);
    }

    public void testFindbySignature1() throws Exception {
        // Test top level methods
        Node root = getRootNode("testfiles/top_level.rb");
        Node node = AstUtilities.findBySignature(root, "Object#bar(baz)");
        assertNotNull(node);
        assertEquals("bar", ((INameNode)node).getName());
    }

    public void testFindbySignature2() throws Exception {
        Node root = getRootNode("testfiles/ape.rb");
        Node node = AstUtilities.findBySignature(root, "Ape#test_sorting(coll)");
        assertNotNull(node);
        assertEquals("test_sorting", ((INameNode)node).getName());
    }

    public void testFindbySignatureNested() throws Exception {
        Node root = getRootNode("testfiles/resolv.rb");
        Node node = AstUtilities.findBySignature(root, "Resolv::DNS::lazy_initialize");
        assertNotNull(node);
        assertEquals("lazy_initialize", ((INameNode)node).getName());
    }
    
    public void testFindbySignatureInstance() throws Exception {
        Node root = getRootNode("testfiles/ape.rb");
        Node node = AstUtilities.findBySignature(root, "Ape#@dialogs");
        assertNotNull(node);
        assertEquals(node.nodeId, NodeType.INSTASGNNODE);
        assertEquals("@dialogs", ((INameNode)node).getName());
    }

    public void testFindbySignatureClassVar() throws Exception {
        Node root = getRootNode("testfiles/ape.rb");
        Node node = AstUtilities.findBySignature(root, "Ape#@@debugging");
        assertNotNull(node);
        assertEquals(node.nodeId, NodeType.CLASSVARASGNNODE);
        assertEquals("@@debugging", ((INameNode)node).getName());
    }

    public void testFindRequires() throws Exception {
        Node root = getRootNode("testfiles/ape.rb");
        Set<String> requires = AstUtilities.getRequires(root);
        List<String> expected = Arrays.asList(new String[] {
            "rexml/document",
            "rubygems",
            "builder",
            "getter",
            "service",
            "samples",
            "entry",
            "poster",
            "collection",
            "deleter",
            "putter",
            "feed",
            "html",
            "crumbs",
            "escaper", 
            "categories",
            "names",
            "validator",
            "authent"
        });
        assertEquals(expected, requires);
    }
    
    public void testGetMethodName() {
        String testFile = "testfiles/ape.rb";
        FileObject fileObject = getTestFile(testFile);
        String text = readFile(fileObject);
        
        int offset = 0;
        String method = AstUtilities.getMethodName(fileObject, offset);
        assertNull(method);
        
        offset = text.indexOf("@w.text! lines[-1]");
        method = AstUtilities.getMethodName(fileObject, offset);
        assertEquals("report_li", method);
        
        offset = text.indexOf("step[1 .. -1].each { |li| report_li(nil, nil, li) }");
        method = AstUtilities.getMethodName(fileObject, offset);
        assertEquals("report_html", method);
    }

    public void testGetTestName() {
        String testFile = "testfiles/new_test.rb";
        FileObject fileObject = getTestFile(testFile);
        String text = readFile(fileObject);
        
        int offset = 0;
        String test = null;
        
        offset = text.indexOf("something should happen to me okay?");
        test = AstUtilities.getTestName(fileObject, offset);
        assertEquals("test_something_should_happen_to_me_okay?", test);

        offset = text.indexOf("something else should happen to me okay?");
        test = AstUtilities.getTestName(fileObject, offset);
        assertEquals("test_something_else_should_happen_to_me_okay?", test);

        offset = text.indexOf("test \"something ");
        test = AstUtilities.getTestName(fileObject, offset);
        assertEquals("test_something_should_happen_to_me_okay?", test);
    }
    
    public void testAddNodesByType() {
        Node root = getRootNode("testfiles/unused.rb");
        List<Node> result = new ArrayList<Node>();
        AstUtilities.addNodesByType(root, new NodeType[] { NodeType.ITERNODE }, result);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof IterNode);
    }
    
    public void testAddNodesByType2() {
        Node root = getRootNode("testfiles/top_level.rb");
        List<Node> result = new ArrayList<Node>();
        AstUtilities.addNodesByType(root, new NodeType[] { NodeType.DEFNNODE }, result);
        assertEquals(2, result.size());
        assertTrue(result.get(0) instanceof DefnNode);
    }
    
    private void addAllNodes(Node node, List<Node> list, Node parent, Map<Node,Node> parents) {
        try {
            node.getPosition().getStartOffset();
            node.getPosition().getEndOffset();
        } catch (UnsupportedOperationException uoe) {
            OffsetRange parentOffset = parent != null ? AstUtilities.getRange(parent) : OffsetRange.NONE;
            fail(uoe.getMessage() + "  node=" + node + " with parent" + parent + " at offset " + parentOffset.toString());
        }

        list.add(node);
        parents.put(node, parent);
        
        List<Node> children = node.childNodes();
        assertNotNull(children);

        for (Node child : children) {
            if (child.isInvisible()) {
                parents.put(child, node);
                continue;
            }
            // No null nodes as children
            assertNotNull(child);
            addAllNodes(child, list, node, parents);
        }
    }
    
    // Make sure we don't bomb out analyzing any of these files
    public void testStress() throws Exception {
        List<FileObject> files = findJRubyRubyFiles();
        for (FileObject file : files) {
            CompilationInfo info = getInfo(file);
            BaseDocument doc = (BaseDocument) info.getDocument();
            assertNotNull(doc);
            List<Node> allNodes = new ArrayList<Node>();
            Node root = AstUtilities.getRoot(info);
            if (root == null || root.isInvisible()) {
                continue;
            }
            assertNotNull(file + " had unexpected parsing errors", root);
            Map<Node,Node> parents = new IdentityHashMap<Node,Node>(1000);
            addAllNodes(root, allNodes, null, parents);
            if (root == null) {
                continue;
            }
            for (Node node : allNodes) {
                try {
                    node.getPosition().getStartOffset();
                    node.getPosition().getEndOffset();
                    
                    // Known exceptions - broken for getNameRange/getRange
                    if (node instanceof StrNode || node instanceof DStrNode) {
                        // See AstOffsetTest.testStringOffset1
                        continue;
                    }
                    
                    AstUtilities.getNameRange(node);
                    AstUtilities.getRange(node);
                } catch (Throwable t) {
                    Node parent = parents.get(node);
                    OffsetRange parentOffset = parent != null ? AstUtilities.getRange(parent) : OffsetRange.NONE;
                    fail(t.getMessage() + " while parsing " + FileUtil.getFileDisplayName(file) + " and node=" + node + " with parent" + parent + " at offset " + parentOffset.toString());
                }
            }
        }
    }
}
