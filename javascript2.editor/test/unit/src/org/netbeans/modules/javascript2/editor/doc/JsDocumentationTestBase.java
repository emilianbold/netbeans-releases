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
import com.oracle.nashorn.ir.LabeledNode;
import com.oracle.nashorn.ir.LineNumberNode;
import com.oracle.nashorn.ir.LiteralNode;
import com.oracle.nashorn.ir.Node;
import com.oracle.nashorn.ir.ObjectNode;
import com.oracle.nashorn.ir.PhiNode;
import com.oracle.nashorn.ir.PropertyNode;
import com.oracle.nashorn.ir.ReferenceNode;
import com.oracle.nashorn.ir.ReturnNode;
import com.oracle.nashorn.ir.RuntimeNode;
import com.oracle.nashorn.ir.SplitNode;
import com.oracle.nashorn.ir.SwitchNode;
import com.oracle.nashorn.ir.TernaryNode;
import com.oracle.nashorn.ir.ThrowNode;
import com.oracle.nashorn.ir.TryNode;
import com.oracle.nashorn.ir.UnaryNode;
import com.oracle.nashorn.ir.VarNode;
import com.oracle.nashorn.ir.WhileNode;
import com.oracle.nashorn.ir.WithNode;
import com.oracle.nashorn.ir.visitor.NodeVisitor;
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
    public JsDocumentationHolder getDocumentationHolder(JsParserResult parserResult) {
        return JsDocumentationSupport.getDocumentationHolder(parserResult);
    }

    /**
     * Gets {@code DocumentationProvider} for given parse result.
     *
     * @param parserResult parser result of the JS file
     * @param provider which provider should be used to create the {@code JsDocumentationHolder}
     * @return requested type of {@code JsDocumentationHolder}
     */
    public JsDocumentationHolder getDocumentationHolder(JsParserResult parserResult, JsDocumentationProvider provider) {
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

        private void processNode(Node node) {
            if (offset >= node.getStart() && offset <= node.getFinish()) {
                nodes.add(node);
            }
        }

        public List<Node> getNodes() {
            return nodes;
        }

        @Override
        public Node enter(AccessNode accessNode) {
            processNode(accessNode);
            return super.enter(accessNode);
        }

        @Override
        public Node enter(Block block) {
            processNode(block);
            return super.enter(block);
        }

        @Override
        public Node enter(BinaryNode binaryNode) {
            processNode(binaryNode);
            return super.enter(binaryNode);
        }

        @Override
        public Node enter(BreakNode breakNode) {
            processNode(breakNode);
            return super.enter(breakNode);
        }

        @Override
        public Node enter(CallNode callNode) {
            processNode(callNode);
            return super.enter(callNode);
        }

        @Override
        public Node enter(CaseNode caseNode) {
            processNode(caseNode);
            return super.enter(caseNode);
        }

        @Override
        public Node enter(CatchNode catchNode) {
            processNode(catchNode);
            return super.enter(catchNode);
        }

        @Override
        public Node enter(ContinueNode continueNode) {
            processNode(continueNode);
            return super.enter(continueNode);
        }

        @Override
        public Node enter(DoWhileNode doWhileNode) {
            processNode(doWhileNode);
            return super.enter(doWhileNode);
        }

        @Override
        public Node enter(EmptyNode emptyNode) {
            processNode(emptyNode);
            return super.enter(emptyNode);
        }

        @Override
        public Node enter(ExecuteNode executeNode) {
            processNode(executeNode);
            return super.enter(executeNode);
        }

        @Override
        public Node enter(ForNode forNode) {
            processNode(forNode);
            return super.enter(forNode);
        }

        @Override
        public Node enter(FunctionNode functionNode) {
            processNode(functionNode);
            return super.enter(functionNode);
        }

        @Override
        public Node enter(IdentNode identNode) {
            processNode(identNode);
            return super.enter(identNode);
        }

        @Override
        public Node enter(IfNode ifNode) {
            processNode(ifNode);
            return super.enter(ifNode);
        }

        @Override
        public Node enter(IndexNode indexNode) {
            processNode(indexNode);
            return super.enter(indexNode);
        }

        @Override
        public Node enter(LabelNode labeledNode) {
            processNode(labeledNode);
            return super.enter(labeledNode);
        }

        @Override
        public Node enter(LabeledNode labeledNode) {
            processNode(labeledNode);
            return super.enter(labeledNode);
        }

        @Override
        public Node enter(LineNumberNode lineNumberNode) {
            processNode(lineNumberNode);
            return super.enter(lineNumberNode);
        }

        @Override
        public Node enter(LiteralNode literalNode) {
            processNode(literalNode);
            return super.enter(literalNode);
        }

        @Override
        public Node enter(ObjectNode objectNode) {
            processNode(objectNode);
            return super.enter(objectNode);
        }

        @Override
        public Node enter(PhiNode phiNode) {
            processNode(phiNode);
            return super.enter(phiNode);
        }

        @Override
        public Node enter(PropertyNode propertyNode) {
            processNode(propertyNode);
            return super.enter(propertyNode);
        }

        @Override
        public Node enter(ReferenceNode referenceNode) {
            processNode(referenceNode);
            return super.enter(referenceNode);
        }

        @Override
        public Node enter(ReturnNode returnNode) {
            processNode(returnNode);
            return super.enter(returnNode);
        }

        @Override
        public Node enter(RuntimeNode runtimeNode) {
            processNode(runtimeNode);
            return super.enter(runtimeNode);
        }

        @Override
        public Node enter(SplitNode splitNode) {
            processNode(splitNode);
            return super.enter(splitNode);
        }

        @Override
        public Node enter(SwitchNode switchNode) {
            processNode(switchNode);
            return super.enter(switchNode);
        }

        @Override
        public Node enter(TernaryNode ternaryNode) {
            processNode(ternaryNode);
            return super.enter(ternaryNode);
        }

        @Override
        public Node enter(ThrowNode throwNode) {
            processNode(throwNode);
            return super.enter(throwNode);
        }

        @Override
        public Node enter(TryNode tryNode) {
            processNode(tryNode);
            return super.enter(tryNode);
        }

        @Override
        public Node enter(UnaryNode unaryNode) {
            processNode(unaryNode);
            return super.enter(unaryNode);
        }

        @Override
        public Node enter(VarNode varNode) {
            processNode(varNode);
            return super.enter(varNode);
        }

        @Override
        public Node enter(WhileNode whileNode) {
            processNode(whileNode);
            return super.enter(whileNode);
        }

        @Override
        public Node enter(WithNode withNode) {
            processNode(withNode);
            return super.enter(withNode);
        }
    }
}
