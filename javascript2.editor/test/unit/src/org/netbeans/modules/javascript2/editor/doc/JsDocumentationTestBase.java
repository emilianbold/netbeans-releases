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

import com.oracle.truffle.js.parser.nashorn.internal.ir.AccessNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.BinaryNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.Block;
import com.oracle.truffle.js.parser.nashorn.internal.ir.BlockStatement;
import com.oracle.truffle.js.parser.nashorn.internal.ir.BreakNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.CallNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.CaseNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.CatchNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ClassNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ContinueNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.DebuggerNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.EmptyNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ErrorNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ExpressionStatement;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ForNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.FunctionNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.IdentNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.IfNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.IndexNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.JoinPredecessorExpression;
import com.oracle.truffle.js.parser.nashorn.internal.ir.LabelNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.LexicalContext;
import com.oracle.truffle.js.parser.nashorn.internal.ir.LiteralNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.Node;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ObjectNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.PropertyNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ReturnNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.RuntimeNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.SwitchNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.TernaryNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.ThrowNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.TryNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.UnaryNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.VarNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.WhileNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.WithNode;
import com.oracle.truffle.js.parser.nashorn.internal.ir.visitor.NodeVisitor;
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
            super(new LexicalContext());
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
        public boolean enterClassNode(ClassNode classNode) {
            processNode(classNode);
            return super.enterClassNode(classNode);
        }

        @Override
        public boolean enterWithNode(WithNode withNode) {
            processNode(withNode);
            return super.enterWithNode(withNode);
        }

        @Override
        public boolean enterWhileNode(WhileNode whileNode) {
            processNode(whileNode);
            return super.enterWhileNode(whileNode);
        }

        @Override
        public boolean enterVarNode(VarNode varNode) {
            processNode(varNode);
            return super.enterVarNode(varNode);
        }

        @Override
        public boolean enterJoinPredecessorExpression(JoinPredecessorExpression expr) {
            processNode(expr);
            return super.enterJoinPredecessorExpression(expr);
        }

        @Override
        public boolean enterUnaryNode(UnaryNode unaryNode) {
            processNode(unaryNode);
            return super.enterUnaryNode(unaryNode);
        }

        @Override
        public boolean enterTryNode(TryNode tryNode) {
            processNode(tryNode);
            return super.enterTryNode(tryNode);
        }

        @Override
        public boolean enterThrowNode(ThrowNode throwNode) {
            processNode(throwNode);
            return super.enterThrowNode(throwNode);
        }

        @Override
        public boolean enterTernaryNode(TernaryNode ternaryNode) {
            processNode(ternaryNode);
            return super.enterTernaryNode(ternaryNode);
        }

        @Override
        public boolean enterSwitchNode(SwitchNode switchNode) {
            processNode(switchNode);
            return super.enterSwitchNode(switchNode);
        }

        @Override
        public boolean enterRuntimeNode(RuntimeNode runtimeNode) {
            processNode(runtimeNode);
            return super.enterRuntimeNode(runtimeNode);
        }

        @Override
        public boolean enterReturnNode(ReturnNode returnNode) {
            processNode(returnNode);
            return super.enterReturnNode(returnNode);
        }

        @Override
        public boolean enterPropertyNode(PropertyNode propertyNode) {
            processNode(propertyNode);
            return super.enterPropertyNode(propertyNode);
        }

        @Override
        public boolean enterObjectNode(ObjectNode objectNode) {
            processNode(objectNode);
            return super.enterObjectNode(objectNode);
        }

        @Override
        public boolean enterLiteralNode(LiteralNode literalNode) {
            processNode(literalNode);
            return super.enterLiteralNode(literalNode);
        }

        @Override
        public boolean enterLabelNode(LabelNode labelNode) {
            processNode(labelNode);
            return super.enterLabelNode(labelNode);
        }

        @Override
        public boolean enterIndexNode(IndexNode indexNode) {
            processNode(indexNode);
            return super.enterIndexNode(indexNode);
        }

        @Override
        public boolean enterIfNode(IfNode ifNode) {
            processNode(ifNode);
            return super.enterIfNode(ifNode);
        }

        @Override
        public boolean enterIdentNode(IdentNode identNode) {
            processNode(identNode);
            return super.enterIdentNode(identNode);
        }

        @Override
        public boolean enterFunctionNode(FunctionNode functionNode) {
            processNode(functionNode);
            return super.enterFunctionNode(functionNode);
        }

        @Override
        public boolean enterForNode(ForNode forNode) {
            processNode(forNode);
            return super.enterForNode(forNode);
        }

        @Override
        public boolean enterBlockStatement(BlockStatement blockStatement) {
            processNode(blockStatement);
            return super.enterBlockStatement(blockStatement);
        }

        @Override
        public boolean enterExpressionStatement(ExpressionStatement expressionStatement) {
            processNode(expressionStatement);
            return super.enterExpressionStatement(expressionStatement);
        }

        @Override
        public boolean enterErrorNode(ErrorNode errorNode) {
            processNode(errorNode);
            return super.enterErrorNode(errorNode);
        }

        @Override
        public boolean enterEmptyNode(EmptyNode emptyNode) {
            processNode(emptyNode);
            return super.enterEmptyNode(emptyNode);
        }

        @Override
        public boolean enterDebuggerNode(DebuggerNode debuggerNode) {
            processNode(debuggerNode);
            return super.enterDebuggerNode(debuggerNode);
        }

        @Override
        public boolean enterContinueNode(ContinueNode continueNode) {
            processNode(continueNode);
            return super.enterContinueNode(continueNode);
        }

        @Override
        public boolean enterCatchNode(CatchNode catchNode) {
            processNode(catchNode);
            return super.enterCatchNode(catchNode);
        }

        @Override
        public boolean enterCaseNode(CaseNode caseNode) {
            processNode(caseNode);
            return super.enterCaseNode(caseNode);
        }

        @Override
        public boolean enterCallNode(CallNode callNode) {
            processNode(callNode);
            return super.enterCallNode(callNode);
        }

        @Override
        public boolean enterBreakNode(BreakNode breakNode) {
            processNode(breakNode);
            return super.enterBreakNode(breakNode);
        }

        @Override
        public boolean enterBinaryNode(BinaryNode binaryNode) {
            processNode(binaryNode);
            return super.enterBinaryNode(binaryNode);
        }

        @Override
        public boolean enterBlock(Block block) {
            processNode(block);
            return super.enterBlock(block);
        }

        @Override
        public boolean enterAccessNode(AccessNode accessNode) {
            processNode(accessNode);
            return super.enterAccessNode(accessNode);
        }
    }
}
