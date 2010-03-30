/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jrubyparser.ast.AliasNode;
import org.jrubyparser.ast.ArrayNode;
import org.jrubyparser.ast.AssignableNode;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.ClassNode;
import org.jrubyparser.ast.Colon2Node;
import org.jrubyparser.ast.DStrNode;
import org.jrubyparser.ast.DefnNode;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.FalseNode;
import org.jrubyparser.ast.FixnumNode;
import org.jrubyparser.ast.IterNode;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.ReturnNode;
import org.jrubyparser.ast.StrNode;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.NilNode;
import org.jrubyparser.ast.OrNode;
import org.jrubyparser.ast.TrueNode;
import org.jrubyparser.ast.VCallNode;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @todo Lots of other methods to test!
 *  
 * @author Tor Norbye
 */
public class AstUtilitiesTest extends RubyTestBase {
    
    public AstUtilitiesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // makes the indexer to index also top level methods in tests
        RubyIndexer.userSourcesTest = true;
    }


    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return rubyTestsClassPath();
    }

    public void testFindBySignature1() throws Exception {
        // Test top level methods
        Node root = getRootNode("testfiles/top_level.rb");
        Node node = AstUtilities.findBySignature(root, "Object#bar(baz)");
        assertNotNull(node);
        assertEquals("bar", ((INameNode)node).getName());
    }

    public void testFindBySignature2() throws Exception {
        Node root = getRootNode("testfiles/ape.rb");
        Node node = AstUtilities.findBySignature(root, "Ape#test_sorting(coll)");
        assertNotNull(node);
        assertEquals("test_sorting", ((INameNode)node).getName());
    }

    public void testFindMethodBySignatureNested() throws Exception {
        Node root = getRootNode("testfiles/resolv.rb");
        Node node = AstUtilities.findBySignature(root, "Resolv::DNS#lazy_initialize");
        assertNotNull(node);
        assertEquals("lazy_initialize", ((INameNode)node).getName());
    }

    public void testFindConstantBySignatureNested() throws Exception {
        Node root = getRootNode("testfiles/constants.rb");
        Node node = AstUtilities.findBySignature(root, "Colors::Converter#VERSION");
        assertNotNull(node);
        assertEquals("VERSION", ((INameNode)node).getName());
    }

    public void testFindConstantBySignature() throws Exception {
        Node root = getRootNode("testfiles/constants.rb");
        Node node = AstUtilities.findBySignature(root, "Colors#RED");
        assertNotNull(node);
        assertEquals("RED", ((INameNode)node).getName());
    }

    public void testFindBySignatureInstance() throws Exception {
        Node root = getRootNode("testfiles/ape.rb");
        Node node = AstUtilities.findBySignature(root, "Ape#@dialogs");
        assertNotNull(node);
        assertEquals(node.getNodeType(), NodeType.INSTASGNNODE);
        assertEquals("@dialogs", ((INameNode)node).getName());
    }

    public void testFindBySignatureClassVar() throws Exception {
        Node root = getRootNode("testfiles/ape.rb");
        Node node = AstUtilities.findBySignature(root, "Ape#@@debugging");
        assertNotNull(node);
        assertEquals(node.getNodeType(), NodeType.CLASSVARASGNNODE);
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

    public void testExitPointsFoo() {
        Node root = getRootNode("testfiles/exit_points.rb");
        MethodDefNode methodDef = (MethodDefNode) AstUtilities.findBySignature(root, "Dummy#foo");
        Set<Node> exits = new LinkedHashSet<Node>();
        AstUtilities.findExitPoints(methodDef, exits);
        assertEquals("two exit points, was: " + exits, 2, exits.size());
        Iterator<Node> it = exits.iterator();
        assertTrue("return node", it.next() instanceof ReturnNode);
        assertTrue("return node", it.next() instanceof ArrayNode);
    }

    public void testExitPointsBoo() {
        Node root = getRootNode("testfiles/exit_points.rb");
        MethodDefNode methodDef = (MethodDefNode) AstUtilities.findBySignature(root, "Dummy#boo(unusedparam,unusedparam2,usedparam)");
        Set<Node> exits = new LinkedHashSet<Node>();
        AstUtilities.findExitPoints(methodDef, exits);
        assertEquals("one exit point, was: " + exits, 1, exits.size());
        Iterator<Node> it = exits.iterator();
        assertTrue("return node", it.next() instanceof FCallNode);
    }

    public void testExitPointsBaz() {
        Node root = getRootNode("testfiles/exit_points.rb");
        MethodDefNode methodDef = (MethodDefNode) AstUtilities.findBySignature(root, "Dummy#baz");
        List<Node> exits = new ArrayList<Node>();
        AstUtilities.findExitPoints(methodDef, exits);
        assertEquals("two exit points, was: " + exits, 2, exits.size());
        assertTrue(exits.get(0) instanceof StrNode);
        assertTrue(exits.get(1) instanceof FixnumNode);
    }

    public void testExitPointsBar() {
        Node root = getRootNode("testfiles/exit_points.rb");
        MethodDefNode methodDef = (MethodDefNode) AstUtilities.findBySignature(root, "Dummy#bar");
        List<Node> exits = new ArrayList<Node>();
        AstUtilities.findExitPoints(methodDef, exits);
        assertEquals("Was: " + exits, 3, exits.size());
        assertEquals("if", AstUtilities.getNameOrValue(exits.get(0)));
        assertEquals("elsif", AstUtilities.getNameOrValue(exits.get(1)));
        assertEquals("else", AstUtilities.getNameOrValue(exits.get(2)));
    }

    public void testExitPointsQux() {
        Node root = getRootNode("testfiles/exit_points.rb");
        MethodDefNode methodDef = (MethodDefNode) AstUtilities.findBySignature(root, "Dummy#qux");
        List<Node> exits = new ArrayList<Node>();
        AstUtilities.findExitPoints(methodDef, exits);
        assertEquals("Was: " + exits, 3, exits.size());
        assertEquals("1", AstUtilities.getNameOrValue(exits.get(0)));
        assertEquals("2", AstUtilities.getNameOrValue(exits.get(1)));
        assertTrue(exits.get(2) instanceof FixnumNode);
    }

    public void testExitPointsThud() {
        Node root = getRootNode("testfiles/exit_points.rb");
        MethodDefNode methodDef = (MethodDefNode) AstUtilities.findBySignature(root, "Dummy#thud");
        List<Node> exits = new ArrayList<Node>();
        AstUtilities.findExitPoints(methodDef, exits);
        assertEquals("Was: " + exits, 1, exits.size());
        assertEquals("2", AstUtilities.getNameOrValue(exits.get(0)));
    }

    public void testExitPointsCorge() {
        Node root = getRootNode("testfiles/exit_points.rb");
        MethodDefNode methodDef = (MethodDefNode) AstUtilities.findBySignature(root, "Dummy#corge(z)");
        List<Node> exits = new ArrayList<Node>();
        AstUtilities.findExitPoints(methodDef, exits);
        assertEquals("Was: " + exits, 2, exits.size());
        assertEquals("a", AstUtilities.getNameOrValue(exits.get(0)));
        assertTrue(exits.get(1) instanceof NilNode);
    }

    public void testExitPointsQuux() {
        Node root = getRootNode("testfiles/exit_points.rb");
        MethodDefNode methodDef = (MethodDefNode) AstUtilities.findBySignature(root, "Dummy#quux");
        List<Node> exits = new ArrayList<Node>();
        AstUtilities.findExitPoints(methodDef, exits);
        assertEquals("Was: " + exits, 4, exits.size());
        assertTrue(exits.get(0) instanceof TrueNode);
        assertTrue(exits.get(1) instanceof FalseNode);
        assertTrue(exits.get(2) instanceof VCallNode);
        assertTrue(exits.get(3) instanceof CallNode);
    }

    public void testExitPointsFred() {
        Node root = getRootNode("testfiles/exit_points.rb");
        MethodDefNode methodDef = (MethodDefNode) AstUtilities.findBySignature(root, "Dummy#fred");
        List<Node> exits = new ArrayList<Node>();
        AstUtilities.findExitPoints(methodDef, exits);
        assertEquals("Was: " + exits, 1, exits.size());
        Node node = exits.get(0);
        assertTrue("Was " + node, node instanceof CallNode);
    }

    public void testExitPointsBarry() {
        Node root = getRootNode("testfiles/exit_points.rb");
        MethodDefNode methodDef = (MethodDefNode) AstUtilities.findBySignature(root, "Dummy#barry(p)");
        List<Node> exits = new ArrayList<Node>();
        AstUtilities.findExitPoints(methodDef, exits);
        assertEquals("Was: " + exits, 3, exits.size());
        assertEquals("raise", AstUtilities.getNameOrValue(exits.get(0)));
        assertTrue(exits.get(1) instanceof FixnumNode);
        assertEquals("77", AstUtilities.getNameOrValue(exits.get(2)));
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

    public void testGetTestNameForShouldaTest() {
        String testFile = "testfiles/shoulda_test.rb";
        FileObject fileObject = getTestFile(testFile);
        String text = readFile(fileObject);

        int offset = 0;
        String test = null;

        offset = text.indexOf("be empty");
        test = AstUtilities.getTestName(fileObject, offset);
        assertEquals("Queue should be empty", test);

        offset = text.indexOf("work  ");
        test = AstUtilities.getTestName(fileObject, offset);
        assertEquals("A Queue instance with a space at the end should work  ", test);

        offset = text.indexOf("respond to :push");
        test = AstUtilities.getTestName(fileObject, offset);
        assertEquals("A Queue instance should respond to :push", test);

        offset = text.indexOf("return that element on :pop");
        test = AstUtilities.getTestName(fileObject, offset);
        assertEquals("A Queue instance with a single element should return that element on :pop", test);
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

    public void testGuessName() throws Exception {
        //public static String guessName(ParserResult info, OffsetRange lexRange, OffsetRange astRange) {
        Parser.Result parserResult = getParserResult("testfiles/arguments.rb");
        String text = getText(parserResult);

        int caretOffset = getCaretOffset(text, "call1(^x)");
        OffsetRange range = new OffsetRange(caretOffset, caretOffset);
        String name = AstUtilities.guessName(parserResult, range, range);
        assertEquals("foo", name);

        caretOffset = getCaretOffset(text, "call2(^y)");
        range = new OffsetRange(caretOffset, caretOffset);
        name = AstUtilities.guessName(parserResult, range, range);
        assertEquals("foo", name);

        caretOffset = getCaretOffset(text, "call3(^x,y,z)");
        range = new OffsetRange(caretOffset, caretOffset);
        name = AstUtilities.guessName(parserResult, range, range);
        assertEquals("a", name);

        caretOffset = getCaretOffset(text, "call3(x,^y,z)");
        range = new OffsetRange(caretOffset, caretOffset);
        name = AstUtilities.guessName(parserResult, range, range);
        assertEquals("b", name);

        caretOffset = getCaretOffset(text, "call4(^x,y,z,w)");
        range = new OffsetRange(caretOffset, caretOffset);
        name = AstUtilities.guessName(parserResult, range, range);
        assertEquals("a", name);

        caretOffset = getCaretOffset(text, "call4(x,^y,z,w)");
        range = new OffsetRange(caretOffset, caretOffset);
        name = AstUtilities.guessName(parserResult, range, range);
        assertEquals("b", name);

        caretOffset = getCaretOffset(text, "call4(x,y,^z,w)");
        range = new OffsetRange(caretOffset, caretOffset);
        name = AstUtilities.guessName(parserResult, range, range);
        assertEquals("c", name);

        caretOffset = getCaretOffset(text, "call4(x,y,z,^w)");
        range = new OffsetRange(caretOffset, caretOffset);
        name = AstUtilities.guessName(parserResult, range, range);
        assertEquals("d", name);
    }


    // Make sure we don't bomb out analyzing any of these files
    public void testStress() throws Throwable {
        List<FileObject> files = findJRubyRubyFiles();
        for (FileObject file : files) {
            Parser.Result parserResult = getParserResult(file);
            BaseDocument doc = RubyUtils.getDocument(parserResult, true);
            assertNotNull("Document for file: " + file.getPath() + " must not be null.", doc);
            List<Node> allNodes = new ArrayList<Node>();
            Node root = AstUtilities.getRoot(parserResult);
            if (root == null || root.isInvisible()) {
                continue;
            }
            assertNotNull(file + " had unexpected parsing errors", root);
            Map<Node,Node> parents = new IdentityHashMap<Node,Node>(1000);
            addAllNodes(root, allNodes, null, parents);
            if (root == null) {
                continue;
            }

            AstUtilities.getClasses(root);
            AstUtilities.getRequires(root);

            for (Node node : allNodes) {
                try {
                    node.getPosition().getStartOffset();
                    node.getPosition().getEndOffset();

                    // Known exceptions - broken for getNameRange/getRange
                    if (node instanceof StrNode || node instanceof DStrNode) {
                        // See AstOffsetTest.testStringOffset1
                        continue;
                    }

                    AstUtilities.getFunctionNameRange(node);
                    AstUtilities.getNameRange(node);
                    OffsetRange nodeRange = AstUtilities.getRange(node);

                    // 147800
                    if (AstUtilities.isCall(node)) {
                        AstUtilities.getCallRange(node);
                        AstUtilities.getCallName(node);

                        for (int offset = nodeRange.getStart(); offset <= nodeRange.getEnd(); offset++) {
                            AstUtilities.findArgumentIndex(node, offset);
                        }
                    }

                    if (node instanceof AliasNode) {
                        AliasNode an = (AliasNode)node;
                        AstUtilities.getAliasOldRange(an);
                        AstUtilities.getAliasNewRange(an);
                    }

                    if (AstUtilities.isAttr(node)) {
                        AstUtilities.getAttrSymbols(node);
                    }

                    if (node instanceof MethodDefNode) {
                        AstUtilities.getDefName(node);
                    }

                    if (node instanceof Colon2Node) {
                        AstUtilities.getFqn((Colon2Node)node);
                    }

                    if (node instanceof AssignableNode) {
                        AstUtilities.getLValueRange((AssignableNode)node);
                    }

                    if (node instanceof ClassNode) {
                        AstUtilities.getSuperclass((ClassNode)node);
                    }
                } catch (Throwable t) {
                    Node parent = parents.get(node);
                    OffsetRange parentOffset = parent != null ? AstUtilities.getRange(parent) : OffsetRange.NONE;
                    fail(t.getMessage() + " while parsing " + FileUtil.getFileDisplayName(file) + " and node=" + node + " with parent" + parent + " at offset " + parentOffset.toString());
                }
            }
            //ParserResult result = info.getEmbeddedResult(RubyInstallation.RUBY_MIME_TYPE, 0);
            //LinkedList<AstElement> list = new LinkedList<AstElement>();
            //RubyParseResult rpr = (RubyParseResult)result;
            //list.addAll(rpr.getStructure().getElements());
            //while (!list.isEmpty()) {
            //    AstElement el = list.removeFirst();
            //    String signature = el.getFqn();
            //    if (signature == null) {
            //        signature = el.getIn() + "." + el.getName();
            //    }
            //    if (signature.length() > 0 && !".".equals(signature)) {
            //        assertNotNull(el.toString(), signature);
            //        AstUtilities.findBySignature(root, signature);
            //    }
            //    list.addAll(el.getChildren());
            //}
        }
    }

    public void testFindArguments1() throws Exception {
        Parser.Result parserResult = getParserResult("testfiles/ape.rb");
        String text = getText(parserResult);
        int caretOffset = getCaretOffset(text, "might_^fail(uri, requested_e_coll, requested_m_coll)");
        Node root = AstUtilities.getRoot(parserResult);
        AstPath path = new AstPath(root, caretOffset);
        Node call = path.leaf();
        assertTrue(AstUtilities.isCall(call));

        caretOffset = getCaretOffset(text, "might_fail(^uri, requested_e_coll, requested_m_coll)");
        assertEquals(0, AstUtilities.findArgumentIndex(call, caretOffset));

        caretOffset = getCaretOffset(text, "might_fail(uri^, requested_e_coll, requested_m_coll)");
        assertEquals(0, AstUtilities.findArgumentIndex(call, caretOffset));

        caretOffset = getCaretOffset(text, "might_fail(uri,^ requested_e_coll, requested_m_coll)");
        assertEquals(1, AstUtilities.findArgumentIndex(call, caretOffset));

        caretOffset = getCaretOffset(text, "might_fail(uri, requested_e_coll^, requested_m_coll)");
        assertEquals(1, AstUtilities.findArgumentIndex(call, caretOffset));

        caretOffset = getCaretOffset(text, "might_fail(uri, requested_e_coll,^ requested_m_coll)");
        assertEquals(2, AstUtilities.findArgumentIndex(call, caretOffset));

        caretOffset = getCaretOffset(text, "might_fail(uri, requested_e_coll, requested_m_coll^)");
        assertEquals(2, AstUtilities.findArgumentIndex(call, caretOffset));

        caretOffset = getCaretOffset(text, "might_fail(uri, requested_e_coll, requested_m_coll)^");
        assertEquals(-1, AstUtilities.findArgumentIndex(call, caretOffset));
    }

    public void testFindArguments2() throws Exception {
        Parser.Result parserResult = getParserResult("testfiles/rubygems.rb");
        String text = getText(parserResult);
        //int caretOffset = 2755; // "new" call from failed earlier test
        int caretOffset = getCaretOffset(text, "MUTEX = Mutex.^new");

        Node root = AstUtilities.getRoot(parserResult);
        AstPath path = new AstPath(root, caretOffset);
        Node call = path.leaf();
        assertTrue(AstUtilities.isCall(call));

        assertEquals(-1, AstUtilities.findArgumentIndex(call, caretOffset));
        assertEquals(-1, AstUtilities.findArgumentIndex(call, caretOffset+3));
    }

    public void testFindArguments3() throws Exception {
        Parser.Result parserResult = getParserResult("testfiles/rubygems.rb");
        String text = getText(parserResult);
        //int caretOffset = 2755; // "new" call from failed earlier test
        int caretOffset = getCaretOffset(text, "Gem.ac^tivate(gem_name, *version_requirements)");

        Node root = AstUtilities.getRoot(parserResult);
        AstPath path = new AstPath(root, caretOffset);
        Node call = path.leaf();
        assertTrue(AstUtilities.isCall(call));

        caretOffset = getCaretOffset(text, "Gem.^activate(gem_name, *version_requirements)");
        assertEquals(-1, AstUtilities.findArgumentIndex(call, caretOffset));

        caretOffset = getCaretOffset(text, "Gem.activate(^gem_name, *version_requirements)");
        assertEquals(0, AstUtilities.findArgumentIndex(call, caretOffset));

        caretOffset = getCaretOffset(text, "Gem.activate(gem_name^, *version_requirements)");
        assertEquals(0, AstUtilities.findArgumentIndex(call, caretOffset));

        caretOffset = getCaretOffset(text, "Gem.activate(gem_name,^ *version_requirements)");
        assertEquals(1, AstUtilities.findArgumentIndex(call, caretOffset));

        caretOffset = getCaretOffset(text, "Gem.activate(gem_name, ^*version_requirements)");
        assertEquals(1, AstUtilities.findArgumentIndex(call, caretOffset));

        caretOffset = getCaretOffset(text, "Gem.activate(gem_name, *^version_requirements)");
        assertEquals(1, AstUtilities.findArgumentIndex(call, caretOffset));

        caretOffset = getCaretOffset(text, "Gem.activate(gem_name, *version_requirements^)");
        assertEquals(1, AstUtilities.findArgumentIndex(call, caretOffset));


        caretOffset = getCaretOffset(text, "Gem::Dependency.^new(gem, version_requirements)");
        caretOffset = getCaretOffset(text, "Gem::ConfigFile.^new []");
        caretOffset = getCaretOffset(text, "StringIO.^new data");
        caretOffset = getCaretOffset(text, "Gem::Dependency.^new gem, requirements");

        // TODO - make sure I add up nested calls correctly, e.g.
        //   foo(bar, baz(boo(bazy)))
        // TODO - test argscatnode - missing fallthrough node!
    }
}
