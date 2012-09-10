/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.model.impl;

import com.oracle.nashorn.ir.*;
import com.oracle.nashorn.ir.visitor.NodeVisitor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Petr Pisl
 */
public class PathNodeVisitor extends NodeVisitor {
    private final List<Node> treePath = new ArrayList<Node>();
    
    public List<? extends Node> getPath() {
        return treePath;
    }
    
    public void addToPath(Node node) {
        treePath.add(node);
    }
    
    public void removeFromPathTheLast() {
        treePath.remove(treePath.size() - 1);
    }

    @Override
    public Node enter(AccessNode accessNode) {
        addToPath(accessNode);
        return super.enter(accessNode);
    }

    @Override
    public Node leave(AccessNode accessNode) {
        removeFromPathTheLast();
        return super.leave(accessNode);
    }

    @Override
    public Node enter(BinaryNode binaryNode) {
        addToPath(binaryNode);
        return super.enter(binaryNode);
    }

    @Override
    public Node leave(BinaryNode binaryNode) {
        removeFromPathTheLast();
        return super.leave(binaryNode);
    }

    @Override
    public Node enter(Block block) {
        addToPath(block);
        return super.enter(block);
    }

    @Override
    public Node leave(Block block) {
        removeFromPathTheLast();
        return super.leave(block);
    }

    @Override
    public Node enter(BreakNode breakNode) {
        addToPath(breakNode);
        return super.enter(breakNode);
    }

    @Override
    public Node leave(BreakNode breakNode) {
        removeFromPathTheLast();
        return super.leave(breakNode);
    }

    @Override
    public Node enter(CallNode callNode) {
        addToPath(callNode);
        return super.enter(callNode);
    }

    @Override
    public Node leave(CallNode callNode) {
        removeFromPathTheLast();
        return super.leave(callNode);
    }

    @Override
    public Node enter(CaseNode caseNode) {
        addToPath(caseNode);
        return super.enter(caseNode);
    }

    @Override
    public Node leave(CaseNode caseNode) {
        removeFromPathTheLast();
        return super.leave(caseNode);
    }

    @Override
    public Node enter(CatchNode catchNode) {
        addToPath(catchNode);
        return super.enter(catchNode);
    }

    @Override
    public Node leave(CatchNode catchNode) {
        removeFromPathTheLast();
        return super.leave(catchNode);
    }

    @Override
    public Node enter(ContinueNode continueNode) {
        addToPath(continueNode);
        return super.enter(continueNode);
    }

    @Override
    public Node leave(ContinueNode continueNode) {
        removeFromPathTheLast();
        return super.leave(continueNode);
    }

    @Override
    public Node enter(ExecuteNode executeNode) {
        addToPath(executeNode);
        return super.enter(executeNode);
    }

    @Override
    public Node leave(ExecuteNode executeNode) {
        removeFromPathTheLast();
        return super.leave(executeNode);
    }

    @Override
    public Node enter(ForNode forNode) {
        addToPath(forNode);
        return super.enter(forNode);
    }

    @Override
    public Node leave(ForNode forNode) {
        removeFromPathTheLast();
        return super.leave(forNode);
    }

    @Override
    public Node enter(FunctionNode functionNode) {
        addToPath(functionNode);
        return super.enter(functionNode);
    }

    @Override
    public Node leave(FunctionNode functionNode) {
        removeFromPathTheLast();
        return super.leave(functionNode);
    }

    @Override
    public Node enter(IdentNode identNode) {
        addToPath(identNode);
        return super.enter(identNode);
    }

    @Override
    public Node leave(IdentNode identNode) {
        removeFromPathTheLast();
        return super.leave(identNode);
    }

    @Override
    public Node enter(IfNode ifNode) {
        addToPath(ifNode);
        return super.enter(ifNode);
    }

    @Override
    public Node leave(IfNode ifNode) {
        removeFromPathTheLast();
        return super.leave(ifNode);
    }

    @Override
    public Node enter(IndexNode indexNode) {
        addToPath(indexNode);
        return super.enter(indexNode);
    }

    @Override
    public Node leave(IndexNode indexNode) {
        removeFromPathTheLast();
        return super.leave(indexNode);
    }

    @Override
    public Node enter(LabelNode labeledNode) {
        addToPath(labeledNode);
        return super.enter(labeledNode);
    }

    @Override
    public Node leave(LabelNode labeledNode) {
        removeFromPathTheLast();
        return super.leave(labeledNode);
    }

    @Override
    public Node enter(LineNumberNode lineNumberNode) {
        addToPath(lineNumberNode);
        return super.enter(lineNumberNode);
    }

    @Override
    public Node leave(LineNumberNode lineNumberNode) {
        removeFromPathTheLast();
        return super.leave(lineNumberNode);
    }

    @Override
    public Node enter(LiteralNode literalNode) {
        addToPath(literalNode);
        return super.enter(literalNode);
    }

    @Override
    public Node leave(LiteralNode literalNode) {
        removeFromPathTheLast();
        return super.leave(literalNode);
    }

    @Override
    public Node enter(ObjectNode objectNode) {
        addToPath(objectNode);
        return super.enter(objectNode);
    }

    @Override
    public Node leave(ObjectNode objectNode) {
        removeFromPathTheLast();
        return super.leave(objectNode);
    }

    @Override
    public Node enter(PropertyNode propertyNode) {
        addToPath(propertyNode);
        return super.enter(propertyNode);
    }

    @Override
    public Node leave(PropertyNode propertyNode) {
        removeFromPathTheLast();
        return super.leave(propertyNode);
    }

    @Override
    public Node enter(ReferenceNode referenceNode) {
        addToPath(referenceNode);
        return super.enter(referenceNode);
    }

    @Override
    public Node leave(ReferenceNode referenceNode) {
        removeFromPathTheLast();
        return super.leave(referenceNode);
    }

    @Override
    public Node enter(ReturnNode returnNode) {
        addToPath(returnNode);
        return super.enter(returnNode);
    }

    @Override
    public Node leave(ReturnNode returnNode) {
        removeFromPathTheLast();
        return super.leave(returnNode);
    }

    @Override
    public Node enter(RuntimeNode runtimeNode) {
        addToPath(runtimeNode);
        return super.enter(runtimeNode);
    }

    @Override
    public Node leave(RuntimeNode runtimeNode) {
        removeFromPathTheLast();
        return super.leave(runtimeNode);
    }

    @Override
    public Node enter(SwitchNode switchNode) {
        addToPath(switchNode);
        return super.enter(switchNode);
    }

    @Override
    public Node leave(SwitchNode switchNode) {
        removeFromPathTheLast();
        return super.leave(switchNode);
    }

    @Override
    public Node enter(TernaryNode ternaryNode) {
        addToPath(ternaryNode);
        return super.enter(ternaryNode);
    }

    @Override
    public Node leave(TernaryNode ternaryNode) {
        removeFromPathTheLast();
        return super.leave(ternaryNode);
    }

    @Override
    public Node enter(ThrowNode throwNode) {
        addToPath(throwNode);
        return super.enter(throwNode);
    }

    @Override
    public Node leave(ThrowNode throwNode) {
        removeFromPathTheLast();
        return super.leave(throwNode);
    }

    @Override
    public Node enter(TryNode tryNode) {
        addToPath(tryNode);
        return super.enter(tryNode);
    }

    @Override
    public Node leave(TryNode tryNode) {
        removeFromPathTheLast();
        return super.leave(tryNode);
    }

    @Override
    public Node enter(UnaryNode unaryNode) {
        addToPath(unaryNode);
        return super.enter(unaryNode);
    }

    @Override
    public Node leave(UnaryNode unaryNode) {
        removeFromPathTheLast();
        return super.leave(unaryNode);
    }

    @Override
    public Node enter(VarNode varNode) {
        addToPath(varNode);
        return super.enter(varNode);
    }

    @Override
    public Node leave(VarNode varNode) {
        removeFromPathTheLast();
        return super.leave(varNode);
    }

    @Override
    public Node enter(WhileNode whileNode) {
        addToPath(whileNode);
        return super.enter(whileNode);
    }

    @Override
    public Node leave(WhileNode whileNode) {
        removeFromPathTheLast();
        return super.leave(whileNode);
    }

    @Override
    public Node enter(WithNode withNode) {
        addToPath(withNode);
        return super.enter(withNode);
    }

    @Override
    public Node leave(WithNode withNode) {
        removeFromPathTheLast();
        return super.leave(withNode);
    }
    
}
