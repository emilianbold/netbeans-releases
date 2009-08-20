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
import org.mozilla.nb.javascript.Context;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;

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
    protected void setUp() throws Exception {
        SupportedBrowsers.getInstance().setLanguageVersion(Context.VERSION_DEFAULT);
    }

    @Override
    protected String describeNode(ParserResult info, Object obj, boolean includePath) throws Exception {
        Node node = (Node)obj;
        if (includePath) {
            BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(true);
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
    protected void initializeNodes(ParserResult result, List<Object> validNodes,
            Map<Object,OffsetRange> positions, List<Object> invalidNodes) throws Exception {
        //Node root = AstUtilities.getRoot(info);
        JsParseResult jspr = AstUtilities.getParseResult(result);
        assertNotNull(jspr.getRootNode());
        
        initialize(jspr.getRootNode(), validNodes, invalidNodes, positions, result);
    }

    private void initialize(Node node, List<Object> validNodes, List<Object> invalidNodes, Map<Object,
            OffsetRange> positions, ParserResult info) throws Exception {
        if (node.getSourceStart() > node.getSourceEnd()) {
            BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(true);
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

    public void testEmbeddedCode1() throws Exception {
        checkOffsets("testfiles/generated_identifiers.js");
    }

    public void testEmbeddedCode2() throws Exception {
        checkOffsets("testfiles/lbracketlist.js");
    }

    public void testEmbeddedCode3() throws Exception {
        checkOffsets("testfiles/embedding/issue136495.erb.js");
    }

    public void testEmbeddedCode4() throws Exception {
        checkOffsets("testfiles/issue120499.js");
    }

    public void testEmbeddedCode6() throws Exception {
        checkOffsets("testfiles/issue148423.js");
    }

    public void testEmbeddedCode5() throws Exception {
        checkOffsets("testfiles/issue149019.js");
    }

    public void testJavaScript17Stuff() throws Exception {
        // http://developer.mozilla.org/en/docs/New_in_JavaScript_1.7
        SupportedBrowsers.getInstance().setLanguageVersion(Context.VERSION_1_7);
        checkOffsets("testfiles/javascript17.js");
    }

    public void testFunctionNameInQuote() throws Exception {
        checkOffsets("testfiles/issue159083.js");
    }
}
