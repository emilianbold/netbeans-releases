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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.AssertTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.Pair;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.openide.util.NbBundle;

/**
 *
 * @author Ralph Ruijs
 */
public class InlineMethodTransformer extends RefactoringVisitor {

    private Trees trees;
    private BlockTree body;
    private MethodTree methodTree;
    private Tree returnType;
    private boolean hasReturn;
    private boolean hasParameters;
    private IdentityHashMap<Tree, Tree> changes = new IdentityHashMap<Tree, Tree>();
    private boolean initialized = false;
    private InlineAbstractTransformer imtv;
    private Problem problem;

    public InlineMethodTransformer() {
    }

    @Override
    public Tree scan(Tree tree, Element p) {
        if (!initialized) {
            initialized = true;
            trees = workingCopy.getTrees();
            methodTree = (MethodTree) trees.getTree(p);
            returnType = methodTree.getReturnType();
            hasReturn = true;
            if (returnType.getKind().equals(Tree.Kind.PRIMITIVE_TYPE)) {
                if (((PrimitiveTypeTree) returnType).getPrimitiveTypeKind().equals(TypeKind.VOID)) {
                    hasReturn = false;
                }
            }
            hasParameters = methodTree.getParameters().size() > 0;
            imtv = new InlineAbstractTransformer(make) {

                @Override
                protected List<StatementTree> replaceStatement(StatementTree statementTree, ExpressionTree node, boolean needBlock) {
                    List<StatementTree> newStatementList = new LinkedList<StatementTree>();
                    if (hasReturn) {

                        // Add statements up to the last statement (return)
                        for (int i = 0; i < body.getStatements().size() - 1; i++) {
                            newStatementList.add(body.getStatements().get(i));
                        }

                        switch (statementTree.getKind()) {
                            case ASSERT:
                                AssertTree assertTree = (AssertTree) statementTree;
                                ExpressionTree assertCondition = assertTree.getCondition();
                                if (assertCondition != null) {
                                    if (assertCondition.equals(node)) {
                                        assertCondition = replaceExpression(assertCondition);
                                    } else {
                                        assertCondition = replacesExpression(assertCondition, node);
                                    }
                                }
                                ExpressionTree assertDetail = assertTree.getDetail();
                                if (assertDetail != null) {
                                    if (assertDetail.equals(node)) {
                                        assertDetail = replaceExpression(assertDetail);
                                    } else {
                                        assertDetail = replacesExpression(assertDetail, node);
                                    }
                                }
                                newStatementList.add(make.Assert(assertCondition, assertDetail));
                                break;
                            case DO_WHILE_LOOP:
                                DoWhileLoopTree doWhileLoopTree = (DoWhileLoopTree) statementTree;
                                ExpressionTree doWhileCondition = doWhileLoopTree.getCondition();
                                if (doWhileCondition != null) {
                                    if (doWhileCondition.equals(node)) {
                                        doWhileCondition = replaceExpression(doWhileCondition);
                                    } else {
                                        doWhileCondition = replacesExpression(doWhileCondition, node);
                                    }
                                }
                                newStatementList.add(make.DoWhileLoop(doWhileCondition, doWhileLoopTree.getStatement()));
                                break;
                            case ENHANCED_FOR_LOOP:
                                EnhancedForLoopTree enhancedForLoopTree = (EnhancedForLoopTree) statementTree;
                                ExpressionTree enhancedForLoopExpression = enhancedForLoopTree.getExpression();
                                if (enhancedForLoopExpression != null) {
                                    if (enhancedForLoopExpression.equals(node)) {
                                        enhancedForLoopExpression = replaceExpression(enhancedForLoopExpression);
                                    } else {
                                        enhancedForLoopExpression = replacesExpression(enhancedForLoopExpression, node);
                                    }
                                }
                                newStatementList.add(make.EnhancedForLoop(enhancedForLoopTree.getVariable(),
                                        enhancedForLoopExpression, enhancedForLoopTree.getStatement()));
                                break;
                            case EXPRESSION_STATEMENT:
                                ExpressionStatementTree expressionStatementTree = (ExpressionStatementTree) statementTree;
                                ExpressionTree expression = expressionStatementTree.getExpression();
                                if (expression != null) {
                                    if (expression.equals(node)) {
                                        expression = replaceExpression(expression);
                                    } else {
                                        expression = replacesExpression(expression, node);
                                    }
                                }
                                newStatementList.add(make.ExpressionStatement(expression));
                                break;
                            case FOR_LOOP:
                                ForLoopTree forLoopTree = (ForLoopTree) statementTree;
                                ExpressionTree forLoopCondition = forLoopTree.getCondition();
                                if (forLoopCondition != null) {
                                    if (forLoopCondition.equals(node)) {
                                        forLoopCondition = replaceExpression(forLoopCondition);
                                    } else {
                                        forLoopCondition = replacesExpression(forLoopCondition, node);
                                    }
                                }
                                newStatementList.add(make.ForLoop(forLoopTree.getInitializer(),
                                        forLoopCondition, forLoopTree.getUpdate(), forLoopTree.getStatement()));
                                break;
                            case IF:
                                IfTree ifTree = (IfTree) statementTree;
                                ExpressionTree ifTreeCondition = ifTree.getCondition();
                                if (ifTreeCondition != null) {
                                    if (ifTreeCondition.equals(node)) {
                                        ifTreeCondition = replaceExpression(ifTreeCondition);
                                    } else {
                                        ifTreeCondition = replacesExpression(ifTreeCondition, node);
                                    }
                                }
                                newStatementList.add(make.If(ifTreeCondition, ifTree.getThenStatement(), ifTree.getElseStatement()));
                                break;
                            case RETURN:
                                ReturnTree returnTree = (ReturnTree) statementTree;
                                ExpressionTree returnExpression = returnTree.getExpression();
                                if (returnExpression != null) {
                                    if (returnExpression.equals(node)) {
                                        returnExpression = replaceExpression(returnExpression);
                                    } else {
                                        returnExpression = replacesExpression(returnExpression, node);
                                    }
                                }
                                newStatementList.add(make.Return(returnExpression));
                                break;
                            case SWITCH:
                                SwitchTree switchTree = (SwitchTree) statementTree;
                                ExpressionTree switchTreeExpression = switchTree.getExpression();
                                if (switchTreeExpression != null) {
                                    if (switchTreeExpression.equals(node)) {
                                        switchTreeExpression = replaceExpression(switchTreeExpression);
                                    } else {
                                        switchTreeExpression = replacesExpression(switchTreeExpression, node);
                                    }
                                }
                                newStatementList.add(make.Switch(switchTreeExpression, switchTree.getCases()));
                                break;
                            case SYNCHRONIZED:
                                SynchronizedTree synchronizedTree = (SynchronizedTree) statementTree;
                                ExpressionTree synchronizedTreeExpression = synchronizedTree.getExpression();
                                if (synchronizedTreeExpression != null) {
                                    if (synchronizedTreeExpression.equals(node)) {
                                        synchronizedTreeExpression = replaceExpression(synchronizedTreeExpression);
                                    } else {
                                        synchronizedTreeExpression = replacesExpression(synchronizedTreeExpression, node);
                                    }
                                }
                                newStatementList.add(make.Synchronized(synchronizedTreeExpression, synchronizedTree.getBlock()));
                                break;
                            case THROW:
                                ThrowTree throwTree = (ThrowTree) statementTree;
                                ExpressionTree throwExpression = throwTree.getExpression();
                                if (throwExpression != null) {
                                    if (throwExpression.equals(node)) {
                                        throwExpression = replaceExpression(throwExpression);
                                    } else {
                                        throwExpression = replacesExpression(throwExpression, node);
                                    }
                                }
                                newStatementList.add(make.Throw(throwExpression));
                                break;
                            case VARIABLE:
                                VariableTree variableTree = (VariableTree) statementTree;
                                ExpressionTree variableInitializer = variableTree.getInitializer();
                                if (variableInitializer != null) {
                                    if (variableInitializer.equals(node)) {
                                        variableInitializer = replaceExpression(variableInitializer);
                                    } else {
                                        variableInitializer = replacesExpression(variableInitializer, node);
                                    }
                                }
                                newStatementList.add(make.Variable(variableTree.getModifiers(), variableTree.getName(), variableTree.getType(), variableInitializer));
                                break;
                            case WHILE_LOOP:
                                WhileLoopTree whileLoopTree = (WhileLoopTree) statementTree;
                                ExpressionTree whileLoopCondition = whileLoopTree.getCondition();
                                if (whileLoopCondition != null) {
                                    if (whileLoopCondition.equals(node)) {
                                        whileLoopCondition = replaceExpression(whileLoopCondition);
                                    } else {
                                        whileLoopCondition = replacesExpression(whileLoopCondition, node);
                                    }
                                }
                                newStatementList.add(make.WhileLoop(whileLoopCondition, whileLoopTree.getStatement()));
                                break;
                        }
                    } else {
                        switch (statementTree.getKind()) {
                            case EXPRESSION_STATEMENT:
                                for (StatementTree bodyStatement : body.getStatements()) {
                                    newStatementList.add(bodyStatement);
                                }
                                break;
                        }
                    }
                    if (needBlock) {
                        BlockTree block = make.Block(newStatementList, false);
                        ArrayList<StatementTree> list = new ArrayList<StatementTree>(1);
                        list.add(block);
                        return list;
                    } else {
                        return newStatementList;
                    }
                }

                @Override
                protected ExpressionTree replaceExpression(ExpressionTree expressionTree) {
                    ExpressionTree expressionBody = ((ReturnTree) body.getStatements().get(body.getStatements().size() - 1)).getExpression();

                    boolean parenthesize = OperatorPrecedence.needsParentheses(getCurrentPath(), expressionTree, expressionBody, workingCopy);
                    if (parenthesize) {
                        expressionBody = make.Parenthesized((ExpressionTree) expressionBody);
                    }

                    return expressionBody;
                }
            };
        }
        return super.scan(tree, p);
    }

    @Override
    public Tree visitMethod(MethodTree node, Element p) {
        Element el = workingCopy.getTrees().getElement(getCurrentPath());
        if (p.equals(el)) {
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            ClassTree removeClassMember = make.removeClassMember((ClassTree) parent, node);
            if (removeClassMember != null) {
                rewrite(parent, removeClassMember);
            }
        }
        return super.visitMethod(node, p);
    }

    @Override
    public Tree visitMethodInvocation(MethodInvocationTree node, Element p) {
        final TreePath currentPath = getCurrentPath();
        Element el = trees.getElement(currentPath);
        if (p.equals(el)) {
            // Find corressponding Statement
            TreePath statementPath = currentPath;
            boolean foundStatement = false;
            while (!foundStatement) {
                statementPath = statementPath.getParentPath();
                switch (statementPath.getLeaf().getKind()) {
                    case ASSERT:
                    case BLOCK:
                    case BREAK:
                    case CLASS:
                    case CONTINUE:
                    case DO_WHILE_LOOP:
                    case EMPTY_STATEMENT:
                    case ENHANCED_FOR_LOOP:
                    case EXPRESSION_STATEMENT:
                    case FOR_LOOP:
                    case IF:
                    case LABELED_STATEMENT:
                    case RETURN:
                    case SWITCH:
                    case SYNCHRONIZED:
                    case THROW:
                    case TRY:
                    case VARIABLE:
                    case WHILE_LOOP:
                        foundStatement = true;
                }
            }
            // Find enclosing block
            TreePath blockPath = statementPath;
            boolean foundEnclosingBlock = false;
            while (!foundEnclosingBlock) {
                switch (blockPath.getLeaf().getKind()) {
                    case BLOCK:
                    case FOR_LOOP:
                    case ENHANCED_FOR_LOOP:
                    case IF:
                    case WHILE_LOOP:
                    case DO_WHILE_LOOP:
                        foundEnclosingBlock = true;
                }
                if (!foundEnclosingBlock) {
                    blockPath = blockPath.getParentPath();
                }
            }
            // Find most upper block
            TreePath upperBlockPath = blockPath;
            boolean foundUpperBlock = false;
            while (!foundUpperBlock) {
                switch (upperBlockPath.getLeaf().getKind()) {
                    case METHOD:
                        foundUpperBlock = true;
                }
                if (!foundUpperBlock) {
                    upperBlockPath = upperBlockPath.getParentPath();
                }
            }
            BlockTree upperBlock = ((MethodTree) upperBlockPath.getLeaf()).getBody();
            // Get block from changes
            BlockTree cachedBlock = (BlockTree) changes.get(upperBlock);
            if (cachedBlock == null) {
                cachedBlock = upperBlock;
                changes.put(upperBlock, cachedBlock);
            }

            body = methodTree.getBody();
            if (hasParameters) {
                final Map<ExpressionTree, ExpressionTree> original2Translated = new HashMap<ExpressionTree, ExpressionTree>();

                TreeScanner<Void, Pair<Element, ExpressionTree>> idScan = new TreeScanner<Void, Pair<Element, ExpressionTree>>() {

                    @Override
                    public Void visitIdentifier(IdentifierTree node, Pair<Element, ExpressionTree> p) {
                        TreePath currentPath = trees.getPath(workingCopy.getCompilationUnit(), node);
                        Element el = trees.getElement(currentPath);
                        if (p.first.equals(el)) {
                            original2Translated.put(node, p.second);
                        }
                        return super.visitIdentifier(node, p);
                    }
                };
                for (int i = 0; i < methodTree.getParameters().size(); i++) {
                    VariableTree variable = methodTree.getParameters().get(i);
                    ExpressionTree argument = node.getArguments().get(i);
                    TreePath path = trees.getPath(workingCopy.getCompilationUnit(), variable);
                    Element element = trees.getElement(path);
                    final Pair<Element, ExpressionTree> pair = Pair.of(element, argument);
                    idScan.scan(body, pair);
                }
                body = (BlockTree) workingCopy.getTreeUtilities().translate(body, original2Translated);
            }
            
            TreeScanner nameClashScanner = new TreeScanner() {

                @Override
                public Object visitIdentifier(IdentifierTree node, Object p) {
                    String msg = RetoucheUtils.variableClashes(node.getName().toString(), currentPath, workingCopy);
                    if (msg != null) {
                        problem = MoveTransformer.createProblem(problem, true, NbBundle.getMessage(InlineRefactoringPlugin.class,
                                "ERR_InlineMethodNameClash", msg)); // NOI18N
                    }
                    return super.visitIdentifier(node, p);
                }
            };
            
            nameClashScanner.scan(body, p);
            
            BlockTree newBlock = imtv.replaceStatement(cachedBlock, statementPath, node);
            changes.put(upperBlock, newBlock);
            rewrite(upperBlock, newBlock);
        }
        return super.visitMethodInvocation(node, p);
    }

    public Problem getProblem() {
        return problem;
    }
}
