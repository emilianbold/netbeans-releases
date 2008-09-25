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

package org.netbeans.modules.javascript.editing;

import java.util.List;
import java.util.Map;
import org.mozilla.nb.javascript.FunctionNode;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.EditHistory;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;

/**
 * Check offsets for the JavaScript AST
 * 
 * @author Tor Norbye
 */
public class AstOffsetTest extends JsTestBase {
    
    public AstOffsetTest(String testName) {
        super(testName);
    }            
    
    @Override
    protected String describeNode(CompilationInfo info, Object obj, boolean includePath) throws Exception {
        Node node = (Node)obj;
        if (includePath) {
            BaseDocument doc = LexUtilities.getDocument(info, false);
            String s = null;
            while (node != null) {
                int line = Utilities.getLineOffset(doc, node.getSourceStart());
                int offset = node.getSourceStart()-Utilities.getRowStart(doc, node.getSourceStart());
                String offsetDesc = line + ":" + offset;
                String n = Token.fullName(node.getType()) + "[" + offsetDesc + "]";
                if (s != null) {
                    s = n + ":" + s;
                } else {
                    s = n;
                }
                node = node.getParentNode();
            }

            return s;
        } else {
            return Token.fullName(node.getType());
        }
    }
    
    @Override
    protected void initializeNodes(CompilationInfo info, ParserResult result, List<Object> validNodes,
            Map<Object,OffsetRange> positions, List<Object> invalidNodes) throws Exception {
        //Node root = AstUtilities.getRoot(info);
        Node root = AstUtilities.getRoot(result);
        assertNotNull(root);
        
        initialize(root, validNodes, invalidNodes, positions, info);
    }

    private void initialize(Node node, List<Object> validNodes, List<Object> invalidNodes, Map<Object,
            OffsetRange> positions, CompilationInfo info) throws Exception {
        if (node.getSourceStart() > node.getSourceEnd()) {
            BaseDocument doc = LexUtilities.getDocument(info, false);
            assertTrue(describeNode(info, node, true) + "; node=" + node.toString() + " at line " + org.netbeans.editor.Utilities.getLineOffset(doc, node.getSourceStart()), false);
        }
        OffsetRange range = new OffsetRange(node.getSourceStart(), node.getSourceEnd());
        if (range.getStart() != 0 || range.getEnd() != 0) { // Don't include 0-0 nodes, these are errors
            validNodes.add(node);
            positions.put(node, range);
        } else {
            invalidNodes.add(node);
        }
        
        if (node.hasChildren()) {
            for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                assert child != null;
                initialize(child, validNodes, invalidNodes, positions, info);
            }
        }
    }

    @Override
    protected void assertEquals(String message, BaseDocument doc, ParserResult expected, ParserResult actual) throws Exception {
        Node expectedRoot = ((JsParseResult)expected).getRootNode();
        Node actualRoot = ((JsParseResult)actual).getRootNode();
        assertEquals(doc, expectedRoot, actualRoot);
    }

    private boolean assertEquals(BaseDocument doc, Node expected, Node actual) throws Exception {
        assertEquals(expected.hasChildren(), actual.hasChildren());
        if (expected.getType() != actual.getType() ||
                expected.hasChildren() != actual.hasChildren() /* ||
                expected.getSourceStart() != actual.getSourceStart() ||
                expected.getSourceEnd() != actual.getSourceEnd()*/
                ) {
            String s = null;
            Node curr = expected;
            while (curr != null) {
                String desc = curr.toString();
                int start = curr.getSourceStart();
                int line = Utilities.getLineOffset(doc, start);
                desc = desc + " (line " + line + ")";
                if (curr.getType() == Token.FUNCTION) {
                    String name = null;
                    Node label = ((FunctionNode)curr).labelNode;
                    if (label != null) {
                        name = label.getString();
                    } else {
                        for (Node child = curr.getFirstChild(); child != null; child = child.getNext()) {
                            if (child.getType() == Token.FUNCNAME) {
                                desc = child.getString();
                                break;
                            }
                        }
                    }
                    if (name != null) {
                        desc = desc + " : " + name + "()";
                    }
                } else if (curr.getType() == Token.OBJECTLIT) {
                    String[] names = AstUtilities.getObjectLitFqn(curr);
                    if (names != null) {
                        desc = desc + " : " + names[0];
                    }
                }
                if (s == null) {
                    s = desc;
                } else {
                    s = desc + " - " + s;
                }
                curr = curr.getParentNode();
            }
            fail("node mismatch: Expected=" + expected + ", Actual=" + actual + "; path=" + s);
        }

        if (expected.hasChildren()) {
            for (Node expectedChild = expected.getFirstChild(),
                    actualChild = actual.getFirstChild();
                    expectedChild != null; expectedChild = expectedChild.getNext(), actualChild = actualChild.getNext()) {
                assertEquals(expectedChild.getNext() != null, actualChild.getNext() != null);
                assertEquals(doc, expectedChild, actualChild);
            }
        }

        return true;
    }

    @Override
    protected void verifyIncremental(ParserResult result, EditHistory history, ParserResult oldResult) {
        JsParseResult pr = (JsParseResult)result;
        assertNotNull(pr.getIncrementalParse());
        assertNotNull(pr.getIncrementalParse().newFunction);
    }
    
    public void testOffsets1() throws Exception {
        checkOffsets("testfiles/semantic1.js");
    }

    public void testOffsets2() throws Exception {
        checkOffsets("testfiles/semantic2.js");
    }

    public void testOffsets3() throws Exception {
        checkOffsets("testfiles/semantic3.js");
    }

    public void testOffsets4() throws Exception {
        checkOffsets("testfiles/semantic4.js");
    }

    public void testOffsets5() throws Exception {
        checkOffsets("testfiles/semantic5.js");
    }

    public void testOffsets6() throws Exception {
        checkOffsets("testfiles/semantic6.js");
    }

    public void testOffsets7() throws Exception {
        checkOffsets("testfiles/semantic7.js");
    }

    public void testOffsets8() throws Exception {
        checkOffsets("testfiles/semantic8.js", "new^");
    }

    public void testOffsets9() throws Exception {
        checkOffsets("testfiles/semantic9.js");
    }

    public void testOffsetsE4x() throws Exception {
        checkOffsets("testfiles/e4x.js", "order^");
    }

    public void testOffsetsE4x2() throws Exception {
        checkOffsets("testfiles/e4x2.js", "order^");
    }

    public void testOffsetsE4x3() throws Exception {
        checkOffsets("testfiles/e4x3.js");
    }


    public void testOffsetsTryCatch() throws Exception {
        checkOffsets("testfiles/tryblocks.js");
    }

    public void testOffsetsPrototype() throws Exception {
        checkOffsets("testfiles/prototype.js");
    }

    public void testOffsetsPrototypeNew() throws Exception {
        checkOffsets("testfiles/prototype-new.js");
    }

    public void testOffsetsSwitches() throws Exception {
        checkOffsets("testfiles/switches.js");
    }

    public void testOffsets136162() throws Exception {
        checkOffsets("testfiles/rename2.js");
    }

    public void testFunctionExpressions() throws Exception {
        checkOffsets("testfiles/functions.js");
    }

//    public void testDestructuringAssignment() throws Exception {
//        // http://developer.mozilla.org/en/docs/New_in_JavaScript_1.7#Destructuring_assignment
//        checkOffsets("testfiles/destructuring_assignment.js");
//    }

    public void testIncremental1() throws Exception {
        checkIncremental("testfiles/dragdrop.js",
                1.7d, // Expect it to be at least twice as fast as non-incremental
                "for (i = 1; i < ^drops.length; ++i)", INSERT+"target",
                "if (Element.isPa^rent", REMOVE+"re"
                );
    }

    public void testIncremental2() throws Exception {
        checkIncremental("testfiles/rename.js",
                0.0d, // small file: no expectation for it to be faster
                "bbb: function(^ppp)", REMOVE+"pp"
                );
    }
    public void testIncremental3() throws Exception {
        checkIncremental("testfiles/semantic3.js",
                0.0d, // small file: no expectation for it to be faster
                "document.createElement(\"option\");^", INSERT+"\nfoo = 5;\n"
                );
    }
}
