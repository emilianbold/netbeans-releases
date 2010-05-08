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

package org.netbeans.modules.ruby;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.SourcePosition;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;

/**
 * Check offsets for the JRuby AST
 * 
 * @author Tor Norbye
 */
public class AstOffsetTest extends RubyTestBase {
    
    public AstOffsetTest(String testName) {
        super(testName);
    }            
    
    @Override
    protected String describeNode(ParserResult parserResult, Object obj, boolean includePath) throws Exception {
        Node node = (Node)obj;
        if (includePath) {
            AstPath path = new AstPath(AstUtilities.getRoot(parserResult), node);
            Iterator<Node> it = path.leafToRoot();
            BaseDocument doc = RubyUtils.getDocument(parserResult);
            assertNotNull(doc);
            String s = null;
            while (it.hasNext()) {
                node = it.next();
                int line = Utilities.getLineOffset(doc, node.getPosition().getStartOffset());
                int offset = node.getPosition().getStartOffset()-Utilities.getRowStart(doc, node.getPosition().getStartOffset());
                String offsetDesc = line + ":" + offset;
                String n = node.getNodeType().name() + "[" + offsetDesc + "]";
                if (s != null) {
                    s = n + ":" + s;
                } else {
                    s = n;
                }
            }

            return s;
        } else {
            return node.getNodeType().name();
        }
    }

    @Override
    protected void initializeNodes(ParserResult result, List<Object> validNodes,
            Map<Object, OffsetRange> positions, List<Object> invalidNodes) throws Exception {
        Node root = AstUtilities.getRoot(result);
        assertNotNull(root);
        
        initialize(root, validNodes, invalidNodes, positions, null/*doc*/);
    }

    private void initialize(Node node, List<Object> validNodes, List<Object> invalidNodes, Map<Object,
            OffsetRange> positions, BaseDocument doc) throws Exception {
        
        if (node.getNodeType() != NodeType.NEWLINENODE) { // Skipping newline nodes since they're everywhere
            SourcePosition pos = node.getPosition();
            OffsetRange range = new OffsetRange(pos.getStartOffset(), pos.getEndOffset());
            if (range.getStart() != 0 || range.getEnd() != 0) { // Don't include 0-0 nodes, these are errors
                if (!validNodes.contains(node)) {
                    validNodes.add(node);
                    positions.put(node, range);
                }
            } else if (!invalidNodes.contains(node)) {
                invalidNodes.add(node);
            }
        }
        
        List<Node> children = node.childNodes();
        if (children.size() > 0) {
            for (Node child : children) {
                if (child.isInvisible()) {
                    continue;
                }
                assert child != null;
                initialize(child, validNodes, invalidNodes, positions, doc);
            }
        }
    }

    public void testAnalysis2() throws Exception {
        checkOffsets("testfiles/ape.rb");
    }

    public void testAnalysis() throws Exception {
        failsDueToIssue182494(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                checkOffsets("testfiles/postgresql_adapter.rb");
                return null;
            }
        });

    }

    public void testAnalysis3() throws Exception {
        failsDueToIssue182494(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                checkOffsets("testfiles/date.rb");
                return null;
            }
        });
    }

    public void testAnalysis4() throws Exception {
        checkOffsets("testfiles/resolv.rb");
    }

    public void testUnused() throws Exception {
        checkOffsets("testfiles/unused.rb");
    }

    public void testRails1() throws Exception {
        checkOffsets("testfiles/action_controller.rb");
    }

    public void testJapanese() throws Exception {
        checkOffsets("testfiles/japanese_spec.rb");
    }

    // These tests fail!!
    //    public void testStringOffset1() throws Exception {
    //        // AstUtilities generated an assertion for this
    //        checkOffsets("testfiles/attribute_accessors.rb");
    //    }
    //
    //    public void testStringOffset2() throws Exception {
    //        // AstUtilities generated an assertion for this
    //        checkOffsets("testfiles/aliasing.rb");
    //    }
}
