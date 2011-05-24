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

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.source.TreeMaker;

/**
 *
 * @author Ralph Ruijs
 */
public abstract class InlineAbstractTransformer {

    protected TreeMaker make;

    public InlineAbstractTransformer(TreeMaker make) {
        this.make = make;
    }

    protected CaseTree replaceCaseTree(CaseTree caseTree, TreePath statementPath, ExpressionTree node) {
        List<StatementTree> newStatementList = new ArrayList<StatementTree>(caseTree.getStatements().size());
        for (StatementTree statementTree : caseTree.getStatements()) {
            if (statementTree.equals(statementPath.getLeaf())) {
                List<StatementTree> recStatementList = replaceStatement(statementTree, node, false);
                for (StatementTree receivedStatement : recStatementList) {
                    newStatementList.add(receivedStatement);
                }
            } else {
                newStatementList.add(replacesStatement(statementTree, statementPath, node));
            }
        }
        CaseTree newCaseTree = make.Case(caseTree.getExpression(), newStatementList);
        return newCaseTree;
    }

    protected CatchTree replaceCatchTree(CatchTree catchTree, TreePath statementPath, ExpressionTree node) {
        BlockTree block = catchTree.getBlock();
        if (block != null) {
            block = replaceStatement(block, statementPath, node);
        }
        CatchTree newCatchTree = make.Catch(catchTree.getParameter(), block);
        return newCatchTree;
    }

    protected abstract ExpressionTree replaceExpression(ExpressionTree expressionTree);

    protected ExpressionTree replaceExpression(ArrayAccessTree arrayAccessTree, ExpressionTree node) {
        ExpressionTree arrayAccessExpression = arrayAccessTree.getExpression();
        if (arrayAccessExpression != null) {
            if (arrayAccessExpression.equals(node)) {
                arrayAccessExpression = replaceExpression(arrayAccessExpression);
            } else {
                arrayAccessExpression = replacesExpression(arrayAccessExpression, node);
            }
        }
        ExpressionTree arrayAccesIndex = arrayAccessTree.getIndex();
        if (arrayAccesIndex != null) {
            if (arrayAccesIndex.equals(node)) {
                arrayAccesIndex = replaceExpression(arrayAccesIndex);
            } else {
                arrayAccesIndex = replacesExpression(arrayAccesIndex, node);
            }
        }
        ArrayAccessTree newArrayAccess = make.ArrayAccess(arrayAccessExpression, arrayAccesIndex);
        return newArrayAccess;
    }

    protected ExpressionTree replaceExpression(AssignmentTree assignmentTree, ExpressionTree node) {
        ExpressionTree assignmentExpression = assignmentTree.getExpression();
        if (assignmentExpression != null) {
            if (assignmentExpression.equals(node)) {
                assignmentExpression = replaceExpression(assignmentExpression);
            } else {
                assignmentExpression = replacesExpression(assignmentExpression, node);
            }
        }
        ExpressionTree assignmentVariable = assignmentTree.getVariable();
        if (assignmentVariable != null) {
            if (assignmentVariable.equals(node)) {
                assignmentVariable = replaceExpression(assignmentVariable);
            } else {
                assignmentVariable = replacesExpression(assignmentVariable, node);
            }
        }
        AssignmentTree newAssignment = make.Assignment(assignmentVariable, assignmentExpression);
        return newAssignment;
    }

    protected ExpressionTree replaceExpression(BinaryTree binaryTree, ExpressionTree node) {
        ExpressionTree binaryLeftOperand = binaryTree.getLeftOperand();
        if (binaryLeftOperand != null) {
            if (binaryLeftOperand.equals(node)) {
                binaryLeftOperand = replaceExpression(binaryLeftOperand);
            } else {
                binaryLeftOperand = replacesExpression(binaryLeftOperand, node);
            }
        }
        ExpressionTree binaryRightOperand = binaryTree.getRightOperand();
        if (binaryRightOperand != null) {
            if (binaryRightOperand.equals(node)) {
                binaryRightOperand = replaceExpression(binaryRightOperand);
            } else {
                binaryRightOperand = replacesExpression(binaryRightOperand, node);
            }
        }
        BinaryTree newBinary = make.Binary(binaryTree.getKind(), binaryLeftOperand, binaryRightOperand);
        return newBinary;
    }

    protected ExpressionTree replaceExpression(CompoundAssignmentTree compoundAssignmentTree, ExpressionTree node) {
        ExpressionTree assignmentExpression = compoundAssignmentTree.getExpression();
        if (assignmentExpression != null) {
            if (assignmentExpression.equals(node)) {
                assignmentExpression = replaceExpression(assignmentExpression);
            } else {
                assignmentExpression = replacesExpression(assignmentExpression, node);
            }
        }
        ExpressionTree assignmentVariable = compoundAssignmentTree.getVariable();
        if (assignmentVariable != null) {
            if (assignmentVariable.equals(node)) {
                assignmentVariable = replaceExpression(assignmentVariable);
            } else {
                assignmentVariable = replacesExpression(assignmentVariable, node);
            }
        }
        CompoundAssignmentTree newAssignment = make.CompoundAssignment(compoundAssignmentTree.getKind(), assignmentVariable, assignmentExpression);
        return newAssignment;
    }

    protected ExpressionTree replaceExpression(ConditionalExpressionTree conditionalExpressionTree, ExpressionTree node) {
        ExpressionTree conditionalExpressionCondition = conditionalExpressionTree.getCondition();
        if (conditionalExpressionCondition != null) {
            if (conditionalExpressionCondition.equals(node)) {
                conditionalExpressionCondition = replaceExpression(conditionalExpressionCondition);
            } else {
                conditionalExpressionCondition = replacesExpression(conditionalExpressionCondition, node);
            }
        }
        ExpressionTree conditionalTrueExpression = conditionalExpressionTree.getTrueExpression();
        if (conditionalTrueExpression != null) {
            if (conditionalTrueExpression.equals(node)) {
                conditionalTrueExpression = replaceExpression(conditionalTrueExpression);
            } else {
                conditionalTrueExpression = replacesExpression(conditionalTrueExpression, node);
            }
        }
        ExpressionTree conditionalFalseExpression = conditionalExpressionTree.getFalseExpression();
        if (conditionalFalseExpression != null) {
            if (conditionalFalseExpression.equals(node)) {
                conditionalFalseExpression = replaceExpression(conditionalFalseExpression);
            } else {
                conditionalFalseExpression = replacesExpression(conditionalFalseExpression, node);
            }
        }
        ConditionalExpressionTree newConditional = make.ConditionalExpression(conditionalExpressionCondition, conditionalTrueExpression, conditionalFalseExpression);
        return newConditional;
    }

    protected ExpressionTree replaceExpression(InstanceOfTree instanceOfTree, ExpressionTree node) {
        ExpressionTree instanceOfExpression = instanceOfTree.getExpression();
        if (instanceOfExpression != null) {
            if (instanceOfExpression.equals(node)) {
                instanceOfExpression = replaceExpression(instanceOfExpression);
            } else {
                instanceOfExpression = replacesExpression(instanceOfExpression, node);
            }
        }
        InstanceOfTree newInstanceOf = make.InstanceOf(instanceOfExpression, instanceOfTree.getType());
        return newInstanceOf;
    }

    protected ExpressionTree replaceExpression(MemberSelectTree memberSelectTree, ExpressionTree node) {
        ExpressionTree memberSelectExpression = memberSelectTree.getExpression();
        if (memberSelectExpression != null) {
            if (memberSelectExpression.equals(node)) {
                memberSelectExpression = replaceExpression(memberSelectExpression);
            } else {
                memberSelectExpression = replacesExpression(memberSelectExpression, node);
            }
        }
        MemberSelectTree newMemberSelect = make.MemberSelect(memberSelectExpression, memberSelectTree.getIdentifier());
        return newMemberSelect;
    }

    protected ExpressionTree replaceExpression(MethodInvocationTree methodInvocationTree, ExpressionTree node) {
        ExpressionTree methodInvocationMethodSelect = methodInvocationTree.getMethodSelect();
        if (methodInvocationMethodSelect != null) {
            if (methodInvocationMethodSelect.equals(node)) {
                methodInvocationMethodSelect = replaceExpression(methodInvocationMethodSelect);
            } else {
                methodInvocationMethodSelect = replacesExpression(methodInvocationMethodSelect, node);
            }
        }
        List<ExpressionTree> newArguments = new ArrayList<ExpressionTree>(methodInvocationTree.getArguments().size());
        for (ExpressionTree expressionTree : methodInvocationTree.getArguments()) {
            if (expressionTree.equals(node)) {
                newArguments.add(replaceExpression(expressionTree));
            } else {
                newArguments.add(replacesExpression(expressionTree, node));
            }
        }
        MethodInvocationTree newMethodInvocationTree = make.MethodInvocation((List<? extends ExpressionTree>) methodInvocationTree.getTypeArguments(), methodInvocationMethodSelect, newArguments);
        return newMethodInvocationTree;
    }

    protected ExpressionTree replaceExpression(NewArrayTree newArrayTree, ExpressionTree node) {
        List<ExpressionTree> newDimensions = new ArrayList<ExpressionTree>(newArrayTree.getDimensions().size());
        for (ExpressionTree expressionTree : newArrayTree.getDimensions()) {
            if (expressionTree != null) {
                if (expressionTree.equals(node)) {
                    newDimensions.add(replaceExpression(expressionTree));
                } else {
                    newDimensions.add(replacesExpression(expressionTree, node));
                }
            }
        }
        List<ExpressionTree> newInitializers = new ArrayList<ExpressionTree>(newArrayTree.getInitializers().size());
        for (ExpressionTree expressionTree : newArrayTree.getInitializers()) {
            if (expressionTree != null) {
                if (expressionTree.equals(node)) {
                    newInitializers.add(replaceExpression(expressionTree));
                } else {
                    newInitializers.add(replacesExpression(expressionTree, node));
                }
            }
        }
        NewArrayTree newNewArray = make.NewArray(newArrayTree.getType(), newDimensions, newInitializers);
        return newNewArray;
    }

    protected ExpressionTree replaceExpression(NewClassTree newClassTree, ExpressionTree node) {
        List<ExpressionTree> newArguments = new ArrayList<ExpressionTree>(newClassTree.getArguments().size());
        for (ExpressionTree expressionTree : newClassTree.getArguments()) {
            if (expressionTree != null) {
                if (expressionTree.equals(node)) {
                    newArguments.add(replaceExpression(expressionTree));
                } else {
                    newArguments.add(replacesExpression(expressionTree, node));
                }
            }
        }
        ExpressionTree enclosingExpression = newClassTree.getEnclosingExpression();
        if (enclosingExpression != null) {
            if (enclosingExpression.equals(node)) {
                enclosingExpression = replaceExpression(enclosingExpression);
            } else {
                enclosingExpression = replacesExpression(enclosingExpression, node);
            }
        }
        ClassTree classBody = newClassTree.getClassBody();
        ClassTree newClassBody = null;
        if (classBody != null) {
            List<Tree> newMembers = new ArrayList<Tree>(classBody.getMembers().size());
            for (ExpressionTree expressionTree : newClassTree.getArguments()) {
                if (expressionTree != null) {
                    if (expressionTree.equals(node)) {
                        newMembers.add(replaceExpression(expressionTree));
                    } else {
                        newMembers.add(replacesExpression(expressionTree, node));
                    }
                }
            }
            newClassBody = make.Class(classBody.getModifiers(), classBody.getSimpleName(), classBody.getTypeParameters(), classBody.getExtendsClause(), classBody.getImplementsClause(), newMembers);
        }
        return make.NewClass(enclosingExpression, (List<? extends ExpressionTree>) newClassTree.getTypeArguments(), newClassTree.getIdentifier(), newArguments, newClassBody);
    }

    protected ExpressionTree replaceExpression(ParenthesizedTree parenthesizedTree, ExpressionTree node) {
        ExpressionTree parenthesizedExpression = parenthesizedTree.getExpression();
        if (parenthesizedExpression != null) {
            if (parenthesizedExpression.equals(node)) {
                parenthesizedExpression = replaceExpression(parenthesizedExpression);
            } else {
                parenthesizedExpression = replacesExpression(parenthesizedExpression, node);
            }
        }
        ParenthesizedTree newParenthesizedTree = make.Parenthesized(parenthesizedExpression);
        return newParenthesizedTree;
    }

    protected ExpressionTree replaceExpression(TypeCastTree typeCastTree, ExpressionTree node) {
        ExpressionTree typeCastExpression = typeCastTree.getExpression();
        if (typeCastExpression != null) {
            if (typeCastExpression.equals(node)) {
                typeCastExpression = replaceExpression(typeCastExpression);
            } else {
                typeCastExpression = replacesExpression(typeCastExpression, node);
            }
        }
        TypeCastTree newTypeCast = make.TypeCast(typeCastTree.getType(), typeCastExpression);
        return newTypeCast;
    }

    protected ExpressionTree replaceExpression(UnaryTree unaryTree, ExpressionTree node) {
        ExpressionTree unaryExpression = unaryTree.getExpression();
        if (unaryExpression != null) {
            if (unaryExpression.equals(node)) {
                unaryExpression = replaceExpression(unaryExpression);
            } else {
                unaryExpression = replacesExpression(unaryExpression, node);
            }
        }
        UnaryTree newUnary = make.Unary(unaryTree.getKind(), unaryExpression);
        return newUnary;
    }

    protected BlockTree replaceStatement(BlockTree blockTree, TreePath statementPath, ExpressionTree node) {
        List<StatementTree> newStatementList = new ArrayList<StatementTree>(blockTree.getStatements().size());
        for (StatementTree statementTree : blockTree.getStatements()) {
            if (statementTree.equals(statementPath.getLeaf())) {
                List<StatementTree> recStatementList = replaceStatement(statementTree, node, false);
                for (StatementTree receivedStatement : recStatementList) {
                    newStatementList.add(receivedStatement);
                }
            } else {
                newStatementList.add(replacesStatement(statementTree, statementPath, node));
            }
        }
        BlockTree newBlock = make.Block(newStatementList, false);
        return newBlock;
    }

    protected SynchronizedTree replaceStatement(SynchronizedTree synchronizedTree, TreePath statementPath, ExpressionTree node) {
        BlockTree blockStatement = synchronizedTree.getBlock();
        if (blockStatement != null) {
            blockStatement = replaceStatement(blockStatement, statementPath, node);
        }
        SynchronizedTree newSynchronized = make.Synchronized(synchronizedTree.getExpression(), blockStatement);
        return newSynchronized;
    }

    protected TryTree replaceStatement(TryTree tryTree, TreePath statementPath, ExpressionTree node) {
        List<CatchTree> newCatches = new ArrayList<CatchTree>(tryTree.getCatches().size());
        for (CatchTree catchTree : tryTree.getCatches()) {
            newCatches.add(replaceCatchTree(catchTree, statementPath, node));
        }
        BlockTree tryBlock = tryTree.getBlock();
        if (tryBlock != null) {
            tryBlock = replaceStatement(tryBlock, statementPath, node);
        }
        BlockTree finallyBlock = tryTree.getFinallyBlock();
        if (finallyBlock != null) {
            finallyBlock = replaceStatement(finallyBlock, statementPath, node);
        }
        TryTree newTry = make.Try(tryBlock, newCatches, finallyBlock);
        return newTry;
    }

    protected IfTree replaceStatement(IfTree ifTree, TreePath statementPath, ExpressionTree node) {
        StatementTree thenStatement = ifTree.getThenStatement();
        if (thenStatement != null) {
            if (thenStatement.equals(statementPath.getLeaf())) {
                thenStatement = replaceStatement(thenStatement, node, true).get(0);
            } else {
                thenStatement = replacesStatement(thenStatement, statementPath, node);
            }
        }
        StatementTree elseStatement = ifTree.getElseStatement();
        if (elseStatement != null) {
            if (elseStatement.equals(statementPath.getLeaf())) {
                elseStatement = replaceStatement(elseStatement, node, true).get(0);
            } else {
                elseStatement = replacesStatement(elseStatement, statementPath, node);
            }
        }
        IfTree newIf = make.If(ifTree.getCondition(), thenStatement, elseStatement);
        return newIf;
    }

    protected SwitchTree replaceStatement(SwitchTree switchTree, TreePath statementPath, ExpressionTree node) {
        List<CaseTree> newCases = new ArrayList<CaseTree>(switchTree.getCases().size());
        for (CaseTree caseTree : switchTree.getCases()) {
            newCases.add(replaceCaseTree(caseTree, statementPath, node));
        }
        SwitchTree newSwitch = make.Switch(switchTree.getExpression(), newCases);
        return newSwitch;
    }

    protected ForLoopTree replaceStatement(ForLoopTree forLoopTree, TreePath statementPath, ExpressionTree node) {
        StatementTree forLoopStatement = forLoopTree.getStatement();
        if (forLoopStatement != null) {
            if (forLoopStatement.equals(statementPath.getLeaf())) {
                forLoopStatement = replaceStatement(forLoopStatement, node, true).get(0);
            } else {
                forLoopStatement = replacesStatement(forLoopStatement, statementPath, node);
            }
        }
        ForLoopTree newFor = make.ForLoop(forLoopTree.getInitializer(), forLoopTree.getCondition(), forLoopTree.getUpdate(), forLoopStatement);
        return newFor;
    }

    protected EnhancedForLoopTree replaceStatement(EnhancedForLoopTree enhancedForLoop, TreePath statementPath, ExpressionTree node) {
        StatementTree forLoopStatement = enhancedForLoop.getStatement();
        if (forLoopStatement != null) {
            if (forLoopStatement.equals(statementPath.getLeaf())) {
                forLoopStatement = replaceStatement(forLoopStatement, node, true).get(0);
            } else {
                forLoopStatement = replacesStatement(forLoopStatement, statementPath, node);
            }
        }
        EnhancedForLoopTree newFor = make.EnhancedForLoop(enhancedForLoop.getVariable(), enhancedForLoop.getExpression(), forLoopStatement);
        return newFor;
    }

    protected WhileLoopTree replaceStatement(WhileLoopTree whileLoopTree, TreePath statementPath, ExpressionTree node) {
        StatementTree whileLoopStatement = whileLoopTree.getStatement();
        if (whileLoopStatement != null) {
            if (whileLoopStatement.equals(statementPath.getLeaf())) {
                whileLoopStatement = replaceStatement(whileLoopStatement, node, true).get(0);
            } else {
                whileLoopStatement = replacesStatement(whileLoopStatement, statementPath, node);
            }
        }
        WhileLoopTree newWhile = make.WhileLoop(whileLoopTree.getCondition(), whileLoopStatement);
        return newWhile;
    }

    protected DoWhileLoopTree replaceStatement(DoWhileLoopTree doWhileLoopTree, TreePath statementPath, ExpressionTree node) {
        StatementTree doWhileLoopStatement = doWhileLoopTree.getStatement();
        if (doWhileLoopStatement != null) {
            if (doWhileLoopStatement.equals(statementPath.getLeaf())) {
                doWhileLoopStatement = replaceStatement(doWhileLoopStatement, node, true).get(0);
            } else {
                doWhileLoopStatement = replacesStatement(doWhileLoopStatement, statementPath, node);
            }
        }
        DoWhileLoopTree newWhile = make.DoWhileLoop(doWhileLoopTree.getCondition(), doWhileLoopStatement);
        return newWhile;
    }

    protected abstract List<StatementTree> replaceStatement(StatementTree statementTree, ExpressionTree node, boolean needBlock);

    protected ExpressionTree replacesExpression(ExpressionTree expressionTree, ExpressionTree node) {
        ExpressionTree returnExpression;
        switch (expressionTree.getKind()) {
            /* Expression Types */
            case ARRAY_ACCESS:
                returnExpression = replaceExpression((ArrayAccessTree) expressionTree, node);
                break;
            case ASSIGNMENT:
                returnExpression = replaceExpression((AssignmentTree) expressionTree, node);
                break;
            // Binary
            case AND:
            case CONDITIONAL_AND:
            case CONDITIONAL_OR:
            case DIVIDE:
            case EQUAL_TO:
            case GREATER_THAN:
            case GREATER_THAN_EQUAL:
            case LEFT_SHIFT:
            case LESS_THAN:
            case LESS_THAN_EQUAL:
            case MINUS:
            case MULTIPLY:
            case NOT_EQUAL_TO:
            case OR:
            case PLUS:
            case REMAINDER:
            case RIGHT_SHIFT:
            case UNSIGNED_RIGHT_SHIFT:
            case XOR:
                returnExpression = replaceExpression((BinaryTree) expressionTree, node);
                break;
            // Compound Assignemnt
            case AND_ASSIGNMENT:
            case DIVIDE_ASSIGNMENT:
            case LEFT_SHIFT_ASSIGNMENT:
            case MINUS_ASSIGNMENT:
            case MULTIPLY_ASSIGNMENT:
            case OR_ASSIGNMENT:
            case PLUS_ASSIGNMENT:
            case REMAINDER_ASSIGNMENT:
            case RIGHT_SHIFT_ASSIGNMENT:
            case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
            case XOR_ASSIGNMENT:
                returnExpression = replaceExpression((CompoundAssignmentTree) expressionTree, node);
                break;
            // -----------------
            case CONDITIONAL_EXPRESSION:
                returnExpression = replaceExpression((ConditionalExpressionTree) expressionTree, node);
                break;
            case INSTANCE_OF:
                returnExpression = replaceExpression((InstanceOfTree) expressionTree, node);
                break;
            // -----------------
            case MEMBER_SELECT:
                returnExpression = replaceExpression((MemberSelectTree) expressionTree, node);
                break;
            case METHOD_INVOCATION:
                returnExpression = replaceExpression((MethodInvocationTree) expressionTree, node);
                break;
            case NEW_ARRAY:
                returnExpression = replaceExpression((NewArrayTree) expressionTree, node);
                break;
            case NEW_CLASS:
                returnExpression = replaceExpression((NewClassTree) expressionTree, node);
                break;
            case PARENTHESIZED:
                returnExpression = replaceExpression((ParenthesizedTree) expressionTree, node);
                break;
            case TYPE_CAST:
                returnExpression = replaceExpression((TypeCastTree) expressionTree, node);
                break;
            // Unary
            case BITWISE_COMPLEMENT:
            case LOGICAL_COMPLEMENT:
            case POSTFIX_DECREMENT:
            case POSTFIX_INCREMENT:
            case PREFIX_DECREMENT:
            case PREFIX_INCREMENT:
            case UNARY_MINUS:
            case UNARY_PLUS:
                returnExpression = replaceExpression((UnaryTree) expressionTree, node);
                break;
            case ANNOTATION:
            default:
                returnExpression = expressionTree;
        }
        return returnExpression;
    }

    protected StatementTree replacesStatement(StatementTree statement, TreePath statementPath, ExpressionTree node) {
        StatementTree returnStatement;
        switch (statement.getKind()) {
            case BLOCK:
                returnStatement = replaceStatement((BlockTree) statement, statementPath, node);
                break;
            case IF:
                returnStatement = replaceStatement((IfTree) statement, statementPath, node);
                break;
            case FOR_LOOP:
                returnStatement = replaceStatement((ForLoopTree) statement, statementPath, node);
                break;
            case ENHANCED_FOR_LOOP:
                returnStatement = replaceStatement((EnhancedForLoopTree) statement, statementPath, node);
                break;
            case WHILE_LOOP:
                returnStatement = replaceStatement((WhileLoopTree) statement, statementPath, node);
                break;
            case DO_WHILE_LOOP:
                returnStatement = replaceStatement((DoWhileLoopTree) statement, statementPath, node);
                break;
            case SWITCH:
                returnStatement = replaceStatement((SwitchTree) statement, statementPath, node);
                break;
            case SYNCHRONIZED:
                returnStatement = replaceStatement((SynchronizedTree) statement, statementPath, node);
                break;
            case TRY:
                returnStatement = replaceStatement((TryTree) statement, statementPath, node);
                break;
            default:
                returnStatement = statement;
        }
        return returnStatement;
    }
}
