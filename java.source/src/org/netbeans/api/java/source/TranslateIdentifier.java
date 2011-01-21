/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.api.java.source;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.code.Symbol;

import java.util.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Replaces identifiers representing all used types with the new ones - imports
 * for them will be solved throughout new commit phase.
 * 
 * This is provided because of refactoring, which wants to take tree from
 * one compilation unit and add it to another one and wants to have all
 * types resolved.
 *
 * @author Pavel Flaska
 */
class TranslateIdentifier implements TreeVisitor<Tree, Boolean> {
    
    private final @NonNull CompilationInfo info;
    private final @NonNull TreeMaker make;
    private final @NullAllowed CompilationUnitTree unit;
    private Element rootElement;
    
    public TranslateIdentifier(@NonNull final WorkingCopy copy) {
        this.info = copy;
        this.make = copy.getTreeMaker();
        this.unit = copy.getFileObject() != null ? copy.getCompilationUnit() : null;
    }

    public Tree visitAnnotation(AnnotationTree node, Boolean p) {
        Tree annotationType = translateTree(node.getAnnotationType());
        List<? extends ExpressionTree> arguments = translateTree(node.getArguments());
        
        if (make == null) return node;
        
        if (annotationType != node.getAnnotationType() ||
            arguments != node.getArguments()) 
        {
            node = make.Annotation(annotationType, arguments);
        }
        return node;
    }

    public Tree visitMethodInvocation(MethodInvocationTree node, Boolean p) {
        List<? extends ExpressionTree> arguments = translateTree(node.getArguments());
        ExpressionTree methodSelect = (ExpressionTree) translateTree(node.getMethodSelect());
        List<? extends Tree> typeArguments = translateTree(node.getTypeArguments());
        
        if (make == null) return node;
        
        if (arguments != node.getArguments() ||
            methodSelect != node.getMethodSelect() ||
            typeArguments != node.getTypeArguments())
        {
            node = make.MethodInvocation((List<? extends ExpressionTree>) typeArguments, methodSelect, arguments);
        }
        return node;
    }

    public Tree visitAssert(AssertTree node, Boolean p) {
        ExpressionTree condition = (ExpressionTree) translateTree(node.getCondition());
        ExpressionTree detail = (ExpressionTree) translateTree(node.getDetail());
        
        if (make == null) return node;
        
        if (condition != node.getCondition() ||
            detail != node.getDetail())
        {
            node = make.Assert(condition, detail);
        }
        return node;
    }

    public Tree visitAssignment(AssignmentTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        ExpressionTree variable = (ExpressionTree) translateTree(node.getVariable());
        
        if (make == null) return node;
        
        if (expression != node.getExpression() ||
            variable != node.getVariable()) 
        {
            node = make.Assignment(variable, expression);
        }
        return node;
    }

    public Tree visitCompoundAssignment(CompoundAssignmentTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        ExpressionTree variable = (ExpressionTree) translateTree(node.getVariable());
        
        if (make == null) return node;
        
        if (expression != node.getExpression() ||
            variable != node.getVariable()) 
        {
            node = make.CompoundAssignment(node.getKind(), variable, expression);
        }
        return node;
    }

    public Tree visitBinary(BinaryTree node, Boolean p) {
        ExpressionTree leftOperand = (ExpressionTree) translateTree(node.getLeftOperand());
        ExpressionTree rightOperand = (ExpressionTree) translateTree(node.getRightOperand());
        
        if (make == null) return node;
        
        if (leftOperand != node.getLeftOperand() ||
            rightOperand != node.getRightOperand())
        {
            node = make.Binary(node.getKind(), leftOperand, rightOperand);
        }   
        return node;
    }

    public Tree visitBlock(BlockTree node, Boolean p) {
        List<? extends StatementTree> statements = translateTree(node.getStatements());
        
        if (make == null) return node;
        
        if (statements != node.getStatements()) {
            node = make.Block(statements, node.isStatic());
        }
        return node;
    }

    public Tree visitBreak(BreakTree node, Boolean p) {
        return node;
    }

    public Tree visitCase(CaseTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression(), true);
        List<? extends StatementTree> statements = translateTree(node.getStatements());
        
        if (make == null) return node;
        
        if (expression != node.getExpression() ||
            statements != node.getStatements())
        {
            node = make.Case(expression, statements);
        }
        return node;
    }

    public Tree visitCatch(CatchTree node, Boolean p) {
        BlockTree block = (BlockTree) translateTree(node.getBlock());
        VariableTree parameter = (VariableTree) translateTree(node.getParameter());
        
        if (make == null) return node;
        
        if (block != node.getBlock() ||
            parameter != node.getParameter()) 
        {
            node = make.Catch(parameter, block);
        }
        return node;
    }

    public Tree visitClass(ClassTree node, Boolean p) {
        Tree extendsClause = translateTree(node.getExtendsClause());
        List<? extends Tree> implementsClause = translateTree(node.getImplementsClause());
        List<? extends Tree> members = translateTree(node.getMembers());
        ModifiersTree modifiers = (ModifiersTree) translateTree(node.getModifiers());
        List<? extends TypeParameterTree> typeParameters = translateTree(node.getTypeParameters());
        
        if (make == null) return node;
        
        if (extendsClause != node.getExtendsClause() ||
            implementsClause != node.getImplementsClause() ||
            members != node.getMembers() ||
            modifiers != node.getModifiers() ||
            typeParameters != node.getTypeParameters())
        {
            node = make.Class(modifiers, node.getSimpleName(), typeParameters, extendsClause, implementsClause, members);
        }
        return node;
    }

    public Tree visitConditionalExpression(ConditionalExpressionTree node, Boolean p) {
        ExpressionTree condition = (ExpressionTree) translateTree(node.getCondition());
        ExpressionTree falseExpression = (ExpressionTree) translateTree(node.getFalseExpression());
        ExpressionTree trueExpression = (ExpressionTree) translateTree(node.getTrueExpression());
        
        if (make == null) return node;
        
        if (condition != node.getCondition() ||
            falseExpression != node.getFalseExpression() ||
            trueExpression != node.getTrueExpression())
        {
            node = make.ConditionalExpression(condition, trueExpression, falseExpression);
        }
        return node;
    }

    public Tree visitContinue(ContinueTree node, Boolean p) {
        return node;
    }

    @Override
    public Tree visitDisjunctiveType(DisjunctiveTypeTree node, Boolean p) {
        List<? extends Tree> typeComponents = translateTree(node.getTypeAlternatives());

        if (make == null) return node;

        if (typeComponents != node.getTypeAlternatives())
        {
            node = make.DisjunctiveType(typeComponents);
        }
        return node;
    }

    public Tree visitDoWhileLoop(DoWhileLoopTree node, Boolean p) {
        StatementTree statement = (StatementTree) translateTree(node.getStatement());
        ExpressionTree condition = (ExpressionTree) translateTree(node.getCondition());
        
        if (make == null) return node;
        
        if (condition != node.getCondition() || statement != node.getStatement()) {
            node = make.DoWhileLoop(condition, statement);
        }
        return node;
    }

    public Tree visitErroneous(ErroneousTree node, Boolean p) {
        List<? extends Tree> errorTrees = translateTree(node.getErrorTrees());
        
        if (make == null) return node;
        
        if (errorTrees != node.getErrorTrees()) {
            node = make.Erroneous(errorTrees);
        }
        return node;
    }

    public Tree visitExpressionStatement(ExpressionStatementTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.ExpressionStatement(expression);
        }
        return node;
    }

    public Tree visitEnhancedForLoop(EnhancedForLoopTree node, Boolean p) {
        StatementTree statement = (StatementTree) translateTree(node.getStatement());
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        VariableTree variable = (VariableTree) translateTree(node.getVariable());
        
        if (make == null) return node;
        
        if (statement != node.getStatement() ||
            expression != node.getExpression() ||
            variable != node.getVariable()) 
        {
            node = make.EnhancedForLoop(variable, expression, statement);
        }
        return node;
    }

    public Tree visitForLoop(ForLoopTree node, Boolean p) {
        StatementTree statement = (StatementTree) translateTree(node.getStatement());
        ExpressionTree condition = (ExpressionTree) translateTree(node.getCondition());
        List<? extends StatementTree> initializer = translateTree(node.getInitializer());
        List<? extends ExpressionStatementTree> update = translateTree(node.getUpdate());
        
        if (make == null) return node;
        
        if (statement != node.getStatement() ||
            condition != node.getCondition() ||
            initializer != node.getInitializer() ||
            update != node.getUpdate()) 
        {
            node = make.ForLoop(initializer, condition, update, statement);
        }
        return node;
    }

    public Tree visitIdentifier(IdentifierTree node, Boolean p) {
        if (make == null) return node;
                
        TreePath path = unit != null ? info.getTrees().getPath(unit, node) : null;
        Element element;
        if (path == null) {
            element = ((JCIdent) node).sym;
        } else {
            element = info.getTrees().getElement(path);
        }
        if (element != null) {
            // solve the imports only when declared type!!!
            if (element.getKind().isClass() || element.getKind().isInterface()
                    || (element.getKind().isField() && ((Symbol) element).isStatic())) {
                TreePath elmPath = info.getTrees().getPath(element);
                boolean en = p == Boolean.TRUE && element.getKind() == ElementKind.ENUM_CONSTANT;
                if ((path == null && element == rootElement)
                        || (path != null && elmPath != null && path.getCompilationUnit().getSourceFile() == elmPath.getCompilationUnit().getSourceFile())
                        || en) {
                    return make.Identifier(element.getSimpleName());
                } else {
                    return make.QualIdent(element);
                }
            } 
        }
        return node;
    }
    
    public Tree visitIf(IfTree node, Boolean p) {
        ExpressionTree condition = (ExpressionTree) translateTree(node.getCondition());
        StatementTree elseStatement = (StatementTree) translateTree(node.getElseStatement());
        StatementTree thenStatement = (StatementTree) translateTree(node.getThenStatement());
        
        if (make == null) return node;
        
        if (condition != node.getCondition() ||
            elseStatement != node.getElseStatement() ||
            thenStatement != node.getThenStatement())
        {
            node = make.If(condition, thenStatement, elseStatement);
        }
        return node;
    }

    public Tree visitImport(ImportTree node, Boolean p) {
        return node;
    }

    public Tree visitArrayAccess(ArrayAccessTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        ExpressionTree index = (ExpressionTree) translateTree(node.getIndex());
        
        if (make == null) return node;
        
        if (expression != node.getExpression() ||
            index != node.getIndex())
        {
            node = make.ArrayAccess(expression, index);
        }
        return node;
    }

    public Tree visitLabeledStatement(LabeledStatementTree node, Boolean p) {
        StatementTree statement = (StatementTree) translateTree(node.getStatement());
        
        if (make == null) return node;
        
        if (statement != node.getStatement()) {
            node = make.LabeledStatement(node.getLabel(), statement);
        }
        return node;
    }

    public Tree visitLiteral(LiteralTree node, Boolean p) {
        return node;
    }

    public Tree visitMethod(MethodTree node, Boolean p) {
        BlockTree body = (BlockTree) translateTree(node.getBody());
        Tree defaultValue = translateTree(node.getDefaultValue());
        List<? extends VariableTree> parameters = translateTree(node.getParameters());
        ModifiersTree modifiers = (ModifiersTree) translateTree(node.getModifiers());
        Tree returnType = translateTree(node.getReturnType());
        List<? extends ExpressionTree> aThrows = translateTree(node.getThrows());
        List<? extends TypeParameterTree> typeParameters = translateTree(node.getTypeParameters());
        
        if (make == null) return node;
        
        if (body != node.getBody() ||
            defaultValue != node.getDefaultValue() ||
            parameters != node.getParameters() ||
            modifiers != node.getModifiers() ||
            returnType != node.getReturnType() ||
            aThrows != node.getThrows() ||
            typeParameters != node.getTypeParameters()) 
        {
            node = make.Method(modifiers,
                    node.getName(),
                    returnType,
                    typeParameters,
                    parameters,
                    aThrows,
                    body,
                    (ExpressionTree) defaultValue
            );
        }
        return node;
    }

    public Tree visitModifiers(ModifiersTree node, Boolean p) {
        List<? extends AnnotationTree> annotations = translateTree(node.getAnnotations());
        
        if (make == null) return node;
        
        if (annotations != node.getAnnotations()) {
            node = make.Modifiers(node.getFlags(), annotations);
        }
        return node;
    }

    public Tree visitNewArray(NewArrayTree node, Boolean p) {
        List<? extends ExpressionTree> initializers = translateTree(node.getInitializers());
        List<? extends ExpressionTree> dimensions = translateTree(node.getDimensions());
        Tree type = translateTree(node.getType());
        
        if (make == null) return node;
        
        if (initializers != node.getInitializers() ||
            dimensions != node.getDimensions() ||
            type != node.getType()) 
        {
            node = make.NewArray(type, dimensions, initializers);
        }
        return node;
    }

    public Tree visitNewClass(NewClassTree node, Boolean p) {
        List<? extends ExpressionTree> arguments = translateTree(node.getArguments());
        ClassTree classBody = (ClassTree) translateTree(node.getClassBody());
        ExpressionTree enclosingExpression = (ExpressionTree) translateTree(node.getEnclosingExpression());
        ExpressionTree identifier = (ExpressionTree) translateTree(node.getIdentifier());
        List<? extends Tree> typeArguments = translateTree(node.getTypeArguments());
        
        if (make == null) return node;
        
        if (arguments != node.getArguments() ||
            classBody != node.getClassBody() ||
            enclosingExpression != node.getEnclosingExpression() ||
            identifier != node.getIdentifier() ||
            typeArguments != node.getTypeArguments())
        {
            node = make.NewClass(enclosingExpression, (List<? extends ExpressionTree>) typeArguments, identifier, arguments, classBody);
        }
        return node;
    }

    public Tree visitParenthesized(ParenthesizedTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.Parenthesized(expression);
        }
        return node;
    }

    public Tree visitReturn(ReturnTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.Return(expression);
        }
        return node;
    }

    public Tree visitMemberSelect(MemberSelectTree node, Boolean p) {
        if (make == null) return node;
        
        TypeElement e = info.getElements().getTypeElement(node.toString());
        if (e != null) {
            return make.QualIdent(e);
        } else {
            ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());

            if (expression != node.getExpression()) {
                node = make.MemberSelect(expression, node.getIdentifier());
            }
            return node;
        }
    }

    public Tree visitEmptyStatement(EmptyStatementTree node, Boolean p) {
        return node;
    }

    public Tree visitSwitch(SwitchTree node, Boolean p) {
        List<? extends CaseTree> cases = translateTree(node.getCases());
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (cases != node.getCases() ||
            expression != node.getExpression()) 
        {
            node = make.Switch(expression, cases);
        }
        return node;
    }

    public Tree visitSynchronized(SynchronizedTree node, Boolean p) {
        BlockTree block = (BlockTree) translateTree(node.getBlock());
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (block != node.getBlock() ||
            expression != node.getExpression())
        {
            node = make.Synchronized(expression, block);
        }
        return node;
    }

    public Tree visitThrow(ThrowTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.Throw(expression);
        }
        return node;
    }

    public Tree visitCompilationUnit(CompilationUnitTree node, Boolean p) {
        List<? extends Tree> typeDecls = translateTree(node.getTypeDecls());
        
        if (make == null) return node;
        
        if (typeDecls != node.getTypeDecls()) {
            node = make.CompilationUnit(
                    node.getPackageAnnotations(),
                    node.getPackageName(),
                    node.getImports(),
                    typeDecls,
                    node.getSourceFile()
            );                   
        }
        return node;
    }

    public Tree visitTry(TryTree node, Boolean p) {
        List<? extends Tree> resources = translateTree(node.getResources());
        BlockTree block = (BlockTree) translateTree(node.getBlock());
        List<? extends CatchTree> catches = translateTree(node.getCatches());
        BlockTree finallyBlock = (BlockTree) translateTree(node.getFinallyBlock());
        
        if (make == null) return node;
        
        if (resources != node.getResources() ||
            block != node.getBlock() ||
            catches != node.getCatches() ||
            finallyBlock != node.getFinallyBlock())
        {
            node = make.Try(block, catches, finallyBlock);
        }
        return node;
    }

    public Tree visitParameterizedType(ParameterizedTypeTree node, Boolean p) {
        Tree type = translateTree(node.getType());
        List<? extends Tree> typeArguments = translateTree(node.getTypeArguments());
        
        if (make == null) return node;
        
        if (type != node.getType() ||
            typeArguments != node.getTypeArguments())
        {
            node = make.ParameterizedType(type, typeArguments);
        }
        return node;
    }

    public Tree visitArrayType(ArrayTypeTree node, Boolean p) {
        Tree type = translateTree(node.getType());
        
        if (make == null) return node;
        
        if (type != node.getType()) {
            node = make.ArrayType(type);
        }
        return node;
    }

    public Tree visitTypeCast(TypeCastTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        Tree type = translateTree(node.getType());
        
        if (make == null) return node;
        
        if (expression != node.getExpression() ||
            type != node.getType()) 
        {
            node = make.TypeCast(type, expression);
        }
        return node;
    }

    public Tree visitPrimitiveType(PrimitiveTypeTree node, Boolean p) {
        return node;
    }

    public Tree visitTypeParameter(TypeParameterTree node, Boolean p) {
        List<? extends Tree> bounds = translateTree(node.getBounds());
        
        if (make == null) return node;
        
        if (bounds != node.getBounds()) {
            node = make.TypeParameter(node.getName(), (List<? extends ExpressionTree>) bounds);
        }
        return node;
    }

    public Tree visitInstanceOf(InstanceOfTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        Tree type = translateTree(node.getType());
        
        if (make == null) return node;
        
        if (expression != node.getExpression() ||
            type != node.getType())
        {
            node = make.InstanceOf(expression, type);
        }
        return node;
    }

    public Tree visitUnary(UnaryTree node, Boolean p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.Unary(node.getKind(), expression);
        }
        return node;
    }

    public Tree visitVariable(VariableTree node, Boolean p) {
        ModifiersTree modifiers = (ModifiersTree) translateTree(node.getModifiers());
        Tree type = translateTree(node.getType());
        ExpressionTree initializer = (ExpressionTree) translateTree(node.getInitializer());

        if (make == null) return node;
        
        if (modifiers != node.getModifiers() || type != node.getType() || initializer != node.getInitializer()) {
            node = make.Variable(modifiers, node.getName(), type, initializer);
        }
        return node;
    }

    public Tree visitWhileLoop(WhileLoopTree node, Boolean p) {
        StatementTree statement = (StatementTree) translateTree(node.getStatement());
        ExpressionTree condition = (ExpressionTree) translateTree(node.getCondition());
        
        if (make == null) return node;
        
        if (condition != node.getCondition() || statement != node.getStatement()) {
            node = make.WhileLoop(condition, statement);
        }
        return node;
    }

    public Tree visitWildcard(WildcardTree node, Boolean p) {
        Tree tree = translateTree(node.getBound());
        
        if (make == null) return node;
        
	if (tree != node.getBound()) {
	    node = make.Wildcard(node.getKind(), tree);
        }
        return node;
    }

    public Tree visitOther(Tree node, Boolean p) {
        return node;
    }

    ////////////////////////////////////////////////////////////////////////////
    public Tree translate(Tree tree) {
        if (tree == null) {
            return null;
        } else {
            TreePath path = unit != null ? info.getTrees().getPath(unit, tree) : null;
            if (path == null) {
                if (tree instanceof JCClassDecl) {
                    rootElement = ((JCClassDecl) tree).sym;
                }
            } else {
                rootElement = info.getTrees().getElement(path);
            }
            Tree res = tree.accept(this, null);

            return res;
        }
    }
    
    private <T extends Tree> List<T> translateTree(List<T> trees) {
        if (trees == null || trees.isEmpty()) {
            return trees;
        }
        List<T> newTrees = new ArrayList<T>();
        boolean changed = false;
        for (T t : trees) {
            T newT = (T) translateTree(t);
            if (newT != t) {
                changed = true;
            }
            if (newT != null) {
                newTrees.add(newT);
            }
        }
        return changed ? newTrees : trees;
    }

    private Tree translateTree(Tree tree) {
        return translateTree(tree, null);
    }

    private Tree translateTree(Tree tree, Boolean p) {
        if (tree == null) {
            return null;
        } else {
            Tree newTree = tree.accept(this, p);
            return newTree;
        }
    }
        
}
