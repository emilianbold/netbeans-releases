/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.doc;

import com.oracle.nashorn.ir.AccessNode;
import com.oracle.nashorn.ir.BinaryNode;
import com.oracle.nashorn.ir.Block;
import com.oracle.nashorn.ir.BreakNode;
import com.oracle.nashorn.ir.CallNode;
import com.oracle.nashorn.ir.CaseNode;
import com.oracle.nashorn.ir.CatchNode;
import com.oracle.nashorn.ir.ContinueNode;
import com.oracle.nashorn.ir.DoWhileNode;
import com.oracle.nashorn.ir.EmptyNode;
import com.oracle.nashorn.ir.ExecuteNode;
import com.oracle.nashorn.ir.ForNode;
import com.oracle.nashorn.ir.FunctionNode;
import com.oracle.nashorn.ir.IdentNode;
import com.oracle.nashorn.ir.IfNode;
import com.oracle.nashorn.ir.IndexNode;
import com.oracle.nashorn.ir.LabelNode;
import com.oracle.nashorn.ir.LineNumberNode;
import com.oracle.nashorn.ir.LiteralNode;
import com.oracle.nashorn.ir.Node;
import com.oracle.nashorn.ir.NodeVisitor;
import com.oracle.nashorn.ir.ObjectNode;
import com.oracle.nashorn.ir.PhiNode;
import com.oracle.nashorn.ir.PropertyNode;
import com.oracle.nashorn.ir.ReferenceNode;
import com.oracle.nashorn.ir.ReturnNode;
import com.oracle.nashorn.ir.RuntimeNode;
import com.oracle.nashorn.ir.SwitchNode;
import com.oracle.nashorn.ir.TernaryNode;
import com.oracle.nashorn.ir.ThrowNode;
import com.oracle.nashorn.ir.TryNode;
import com.oracle.nashorn.ir.UnaryNode;
import com.oracle.nashorn.ir.VarNode;
import com.oracle.nashorn.ir.WhileNode;
import com.oracle.nashorn.ir.WithNode;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.editor.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.editor.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.editor.doc.spi.JsDocumentationProvider;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.Source;

/**
 * Base of class for doc unit tests.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public abstract class JsDocumentationTestBase extends JsTestBase {

    public JsDocumentationTestBase(String testName) {
        super(testName);
    }

    /**
     * Gets {@code DocumentationProvider} for given parse result.
     *
     * @param parserResult parser result of the JS file
     * @return appropriate {@code JsDocumentationHolder} to given source
     */
    public static JsDocumentationHolder getDocumentationHolder(JsParserResult parserResult) {
        return JsDocumentationSupport.getDocumentationHolder(parserResult);
    }

    /**
     * Gets {@code DocumentationProvider} for given parse result.
     *
     * @param parserResult parser result of the JS file
     * @param provider which provider should be used to create the {@code JsDocumentationHolder}
     * @return requested type of {@code JsDocumentationHolder}
     */
    public static JsDocumentationHolder getDocumentationHolder(JsParserResult parserResult, JsDocumentationProvider provider) {
        return provider.createDocumentationHolder(parserResult.getSnapshot());
    }

    /**
     * Gets node for given offset.
     *
     * @param parserResult parser result of the JS file
     * @param offset offset of examined node
     * @return {@code Node} which correspond to given offset
     */
    public Node getNodeForOffset(JsParserResult parserResult, int offset) {
        Node nearestNode = null;
        int nearestNodeDistance = Integer.MAX_VALUE;
        FunctionNode root = parserResult.getRoot();
        OffsetVisitor offsetVisitor = new OffsetVisitor(offset);
        root.accept(offsetVisitor);
        for (Node node : offsetVisitor.getNodes()) {
            if (offset - node.getStart() < nearestNodeDistance) {
                nearestNodeDistance = offset - node.getStart();
                nearestNode = node;
            }
        }
        return nearestNode;
    }

    /**
     * Return the offset of the given position, indicated by ^ in the line fragment from the text got from given Source.
     *
     * @param source source for counting the offset
     * @param caretLine line
     * @return offset of ^ in the given source
     */
    public int getCaretOffset(Source source, String caretLine) {
        return getCaretOffset(source.createSnapshot().getText().toString(), caretLine);
    }


    private static class OffsetVisitor extends NodeVisitor {

        private final int offset;
        private final List<Node> nodes = new LinkedList<Node>();

        public OffsetVisitor(int offset) {
            this.offset = offset;
        }

        private void processNode(Node node, boolean onset) {
            if (onset) {
                if (offset >= node.getStart() && offset <= node.getFinish()) {
                    nodes.add(node);
                }
            }
        }

        public List<Node> getNodes() {
            return nodes;
        }

        @Override
        public Node visit(AccessNode accessNode, boolean onset) {
            processNode(accessNode, onset);
            return super.visit(accessNode, onset);
        }

        @Override
        public Node visit(Block block, boolean onset) {
            processNode(block, onset);
            return super.visit(block, onset);
        }

        @Override
        public Node visit(BinaryNode binaryNode, boolean onset) {
            processNode(binaryNode, onset);
            return super.visit(binaryNode, onset);
        }

        @Override
        public Node visit(BreakNode breakNode, boolean onset) {
            processNode(breakNode, onset);
            return super.visit(breakNode, onset);
        }

        @Override
        public Node visit(CallNode callNode, boolean onset) {
            processNode(callNode, onset);
            return super.visit(callNode, onset);
        }

        @Override
        public Node visit(CaseNode caseNode, boolean onset) {
            processNode(caseNode, onset);
            return super.visit(caseNode, onset);
        }

        @Override
        public Node visit(CatchNode catchNode, boolean onset) {
            processNode(catchNode, onset);
            return super.visit(catchNode, onset);
        }

        @Override
        public Node visit(ContinueNode continueNode, boolean onset) {
            processNode(continueNode, onset);
            return super.visit(continueNode, onset);
        }

        @Override
        public Node visit(DoWhileNode doWhileNode, boolean onset) {
            processNode(doWhileNode, onset);
            return super.visit(doWhileNode, onset);
        }

        @Override
        public Node visit(EmptyNode emptyNode, boolean onset) {
            processNode(emptyNode, onset);
            return super.visit(emptyNode, onset);
        }

        @Override
        public Node visit(ExecuteNode executeNode, boolean onset) {
            processNode(executeNode, onset);
            return super.visit(executeNode, onset);
        }

        @Override
        public Node visit(ForNode forNode, boolean onset) {
            processNode(forNode, onset);
            return super.visit(forNode, onset);
        }

        @Override
        public Node visit(FunctionNode functionNode, boolean onset) {
            processNode(functionNode, onset);
            return super.visit(functionNode, onset);
        }

        @Override
        public Node visit(IdentNode identNode, boolean onset) {
            processNode(identNode, onset);
            return super.visit(identNode, onset);
        }

        @Override
        public Node visit(IfNode ifNode, boolean onset) {
            processNode(ifNode, onset);
            return super.visit(ifNode, onset);
        }

        @Override
        public Node visit(IndexNode indexNode, boolean onset) {
            processNode(indexNode, onset);
            return super.visit(indexNode, onset);
        }

        @Override
        public Node visit(LabelNode labeledNode, boolean onset) {
            processNode(labeledNode, onset);
            return super.visit(labeledNode, onset);
        }

        @Override
        public Node visit(LineNumberNode lineNumberNode, boolean onset) {
            processNode(lineNumberNode, onset);
            return super.visit(lineNumberNode, onset);
        }

        @Override
        public Node visit(LiteralNode literalNode, boolean onset) {
            processNode(literalNode, onset);
            return super.visit(literalNode, onset);
        }

        @Override
        public Node visit(ObjectNode objectNode, boolean onset) {
            processNode(objectNode, onset);
            return super.visit(objectNode, onset);
        }

        @Override
        public Node visit(PhiNode phiNode, boolean onset) {
            processNode(phiNode, onset);
            return super.visit(phiNode, onset);
        }

        @Override
        public Node visit(PropertyNode propertyNode, boolean onset) {
            processNode(propertyNode, onset);
            return super.visit(propertyNode, onset);
        }

        @Override
        public Node visit(ReferenceNode referenceNode, boolean onset) {
            processNode(referenceNode, onset);
            return super.visit(referenceNode, onset);
        }

        @Override
        public Node visit(ReturnNode returnNode, boolean onset) {
            processNode(returnNode, onset);
            return super.visit(returnNode, onset);
        }

        @Override
        public Node visit(RuntimeNode runtimeNode, boolean onset) {
            processNode(runtimeNode, onset);
            return super.visit(runtimeNode, onset);
        }

        @Override
        public Node visit(SwitchNode switchNode, boolean onset) {
            processNode(switchNode, onset);
            return super.visit(switchNode, onset);
        }

        @Override
        public Node visit(TernaryNode ternaryNode, boolean onset) {
            processNode(ternaryNode, onset);
            return super.visit(ternaryNode, onset);
        }

        @Override
        public Node visit(ThrowNode throwNode, boolean onset) {
            processNode(throwNode, onset);
            return super.visit(throwNode, onset);
        }

        @Override
        public Node visit(TryNode tryNode, boolean onset) {
            processNode(tryNode, onset);
            return super.visit(tryNode, onset);
        }

        @Override
        public Node visit(UnaryNode unaryNode, boolean onset) {
            processNode(unaryNode, onset);
            return super.visit(unaryNode, onset);
        }

        @Override
        public Node visit(VarNode varNode, boolean onset) {
            processNode(varNode, onset);
            return super.visit(varNode, onset);
        }

        @Override
        public Node visit(WhileNode whileNode, boolean onset) {
            processNode(whileNode, onset);
            return super.visit(whileNode, onset);
        }

        @Override
        public Node visit(WithNode withNode, boolean onset) {
            processNode(withNode, onset);
            return super.visit(withNode, onset);
        }
    }
}
