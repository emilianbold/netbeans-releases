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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.api.java.source;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.SourcePositions;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.code.Symbol;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.CommentSetImpl;
import static org.netbeans.modules.java.source.save.PositionEstimator.*;
import org.netbeans.modules.java.source.save.PositionEstimator;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;

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
class TranslateIdentifier implements TreeVisitor<Tree, Void> {
    
    private final CompilationInfo info;
    private final TreeMaker make;
    private final CompilationUnitTree unit;
    private final boolean copyComments;
    private final boolean resolveImports;
    private final TokenSequence<JavaTokenId> seq;
    private final CommentHandlerService commentService;
    private int tokenIndexAlreadyAdded = -1;
    private Element rootElement;
    


    public TranslateIdentifier(final CompilationInfo info, 
            final boolean copyComments, 
            final boolean resolveImports,
            final TokenSequence<JavaTokenId> seq) 
    {
        this.info = info;
        this.make = info instanceof WorkingCopy ? ((WorkingCopy) info).getTreeMaker() : null;
        this.unit = info.getCompilationUnit();
        this.seq = seq;
        this.copyComments = copyComments;
        this.resolveImports = resolveImports;
        this.commentService = CommentHandlerService.instance(info.impl.getJavacTask().getContext());
    }

    public Tree visitAnnotation(AnnotationTree node, Void p) {
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

    public Tree visitMethodInvocation(MethodInvocationTree node, Void p) {
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

    public Tree visitAssert(AssertTree node, Void p) {
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

    public Tree visitAssignment(AssignmentTree node, Void p) {
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

    public Tree visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
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

    public Tree visitBinary(BinaryTree node, Void p) {
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

    public Tree visitBlock(BlockTree node, Void p) {
        List<? extends StatementTree> statements = translateTree(node.getStatements());
        
        if (make == null) return node;
        
        if (statements != node.getStatements()) {
            node = make.Block(statements, node.isStatic());
        }
        return node;
    }

    public Tree visitBreak(BreakTree node, Void p) {
        return node;
    }

    public Tree visitCase(CaseTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        List<? extends StatementTree> statements = translateTree(node.getStatements());
        
        if (make == null) return node;
        
        if (expression != node.getExpression() ||
            statements != node.getStatements())
        {
            node = make.Case(expression, statements);
        }
        return node;
    }

    public Tree visitCatch(CatchTree node, Void p) {
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

    public Tree visitClass(ClassTree node, Void p) {
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

    public Tree visitConditionalExpression(ConditionalExpressionTree node, Void p) {
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

    public Tree visitContinue(ContinueTree node, Void p) {
        return node;
    }

    public Tree visitDoWhileLoop(DoWhileLoopTree node, Void p) {
        StatementTree statement = (StatementTree) translateTree(node.getStatement());
        ExpressionTree condition = (ExpressionTree) translateTree(node.getCondition());
        
        if (make == null) return node;
        
        if (condition != node.getCondition() || statement != node.getStatement()) {
            node = make.DoWhileLoop(condition, statement);
        }
        return node;
    }

    public Tree visitErroneous(ErroneousTree node, Void p) {
        List<? extends Tree> errorTrees = translateTree(node.getErrorTrees());
        
        if (make == null) return node;
        
        if (errorTrees != node.getErrorTrees()) {
            node = make.Erroneous(errorTrees);
        }
        return node;
    }

    public Tree visitExpressionStatement(ExpressionStatementTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.ExpressionStatement(expression);
        }
        return node;
    }

    public Tree visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
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

    public Tree visitForLoop(ForLoopTree node, Void p) {
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

    public Tree visitIdentifier(IdentifierTree node, Void p) {
        if (!resolveImports) return node;
        if (make == null) return node;
                
        TreePath path = info.getTrees().getPath(unit, node);
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
                if ((path == null && element == rootElement)
                        || (path != null && elmPath != null && path.getCompilationUnit().getSourceFile() == elmPath.getCompilationUnit().getSourceFile())) {
                    return make.Identifier(element.getSimpleName());
                } else {
                    return make.QualIdent(element);
                }
            } 
        }
        return node;
    }
    
    public Tree visitIf(IfTree node, Void p) {
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

    public Tree visitImport(ImportTree node, Void p) {
        return node;
    }

    public Tree visitArrayAccess(ArrayAccessTree node, Void p) {
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

    public Tree visitLabeledStatement(LabeledStatementTree node, Void p) {
        StatementTree statement = (StatementTree) translateTree(node.getStatement());
        
        if (make == null) return node;
        
        if (statement != node.getStatement()) {
            node = make.LabeledStatement(node.getLabel(), statement);
        }
        return node;
    }

    public Tree visitLiteral(LiteralTree node, Void p) {
        return node;
    }

    public Tree visitMethod(MethodTree node, Void p) {
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

    public Tree visitModifiers(ModifiersTree node, Void p) {
        List<? extends AnnotationTree> annotations = translateTree(node.getAnnotations());
        
        if (make == null) return node;
        
        if (annotations != node.getAnnotations()) {
            node = make.Modifiers(node.getFlags(), annotations);
        }
        return node;
    }

    public Tree visitNewArray(NewArrayTree node, Void p) {
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

    public Tree visitNewClass(NewClassTree node, Void p) {
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

    public Tree visitParenthesized(ParenthesizedTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.Parenthesized(expression);
        }
        return node;
    }

    public Tree visitReturn(ReturnTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.Return(expression);
        }
        return node;
    }

    public Tree visitMemberSelect(MemberSelectTree node, Void p) {
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

    public Tree visitEmptyStatement(EmptyStatementTree node, Void p) {
        return node;
    }

    public Tree visitSwitch(SwitchTree node, Void p) {
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

    public Tree visitSynchronized(SynchronizedTree node, Void p) {
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

    public Tree visitThrow(ThrowTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.Throw(expression);
        }
        return node;
    }

    public Tree visitCompilationUnit(CompilationUnitTree node, Void p) {
        List<? extends Tree> typeDecls = translateTree(node.getTypeDecls());
        
        if (make == null) return node;
        
        if (typeDecls != node.getTypeDecls()) {
            node = make.CompilationUnit(
                    node.getPackageName(),
                    node.getImports(),
                    typeDecls,
                    node.getSourceFile()
            );                   
        }
        return node;
    }

    public Tree visitTry(TryTree node, Void p) {
        BlockTree block = (BlockTree) translateTree(node.getBlock());
        List<? extends CatchTree> catches = translateTree(node.getCatches());
        BlockTree finallyBlock = (BlockTree) translateTree(node.getFinallyBlock());
        
        if (make == null) return node;
        
        if (block != node.getBlock() ||
            catches != node.getCatches() ||
            finallyBlock != node.getFinallyBlock())
        {
            node = make.Try(block, catches, finallyBlock);
        }
        return node;
    }

    public Tree visitParameterizedType(ParameterizedTypeTree node, Void p) {
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

    public Tree visitArrayType(ArrayTypeTree node, Void p) {
        Tree type = translateTree(node.getType());
        
        if (make == null) return node;
        
        if (type != node.getType()) {
            node = make.ArrayType(type);
        }
        return node;
    }

    public Tree visitTypeCast(TypeCastTree node, Void p) {
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

    public Tree visitPrimitiveType(PrimitiveTypeTree node, Void p) {
        return node;
    }

    public Tree visitTypeParameter(TypeParameterTree node, Void p) {
        List<? extends Tree> bounds = translateTree(node.getBounds());
        
        if (make == null) return node;
        
        if (bounds != node.getBounds()) {
            node = make.TypeParameter(node.getName(), (List<? extends ExpressionTree>) bounds);
        }
        return node;
    }

    public Tree visitInstanceOf(InstanceOfTree node, Void p) {
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

    public Tree visitUnary(UnaryTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translateTree(node.getExpression());
        
        if (make == null) return node;
        
        if (expression != node.getExpression()) {
            node = make.Unary(node.getKind(), expression);
        }
        return node;
    }

    public Tree visitVariable(VariableTree node, Void p) {
        ModifiersTree modifiers = (ModifiersTree) translateTree(node.getModifiers());
        Tree type = translateTree(node.getType());
        ExpressionTree initializer = (ExpressionTree) translateTree(node.getInitializer());

        if (make == null) return node;
        
        if (modifiers != node.getModifiers() || type != node.getType() || initializer != node.getInitializer()) {
            node = make.Variable(modifiers, node.getName(), type, initializer);
        }
        return node;
    }

    public Tree visitWhileLoop(WhileLoopTree node, Void p) {
        StatementTree statement = (StatementTree) translateTree(node.getStatement());
        ExpressionTree condition = (ExpressionTree) translateTree(node.getCondition());
        
        if (make == null) return node;
        
        if (condition != node.getCondition() || statement != node.getStatement()) {
            node = make.WhileLoop(condition, statement);
        }
        return node;
    }

    public Tree visitWildcard(WildcardTree node, Void p) {
        Tree tree = translateTree(node.getBound());
        
        if (make == null) return node;
        
	if (tree != node.getBound()) {
	    node = make.Wildcard(node.getKind(), tree);
        }
        return node;
    }

    public Tree visitOther(Tree node, Void p) {
        return node;
    }

    ////////////////////////////////////////////////////////////////////////////
    public Tree translate(Tree tree) {
        if (tree == null) {
            return null;
        } else {
            if (copyComments) {
                mapComments(tree);
//                mapComments2(tree);
            }
            TreePath path = info.getTrees().getPath(unit, tree);
            if (path == null) {
                if (tree instanceof JCClassDecl) {
                    rootElement = ((JCClassDecl) tree).sym;
                }
            } else {
                rootElement = info.getTrees().getElement(path);
            }
            return tree.accept(this, null);
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
        if (tree == null) {
            return null;
        } else {
            if (copyComments) {
                mapComments(tree);
//                mapComments2(tree);
            }
            Tree newTree = tree.accept(this, null);
            // #144209
            commentService.copyComments(tree, newTree);
            return newTree;
        }
    }
    
    private void mapComments(Tree tree) {
        if (((JCTree) tree).pos <= 0) {
            return;
        }
        commentService.getComments(tree).commentsMapped();
        SourcePositions pos = info.getTrees().getSourcePositions();
        seq.move((int) pos.getStartPosition(null, tree));
        PositionEstimator.moveToSrcRelevant(seq, Direction.BACKWARD);
        int indent = NOPOS;
        Token<JavaTokenId> token;
        boolean b = false;
        while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
            if (seq.index() <= tokenIndexAlreadyAdded) {
                return;
            } else {
                if (!b) {
                    tokenIndexAlreadyAdded = seq.index();
                    b = true;
                }
            }
            switch (token.id()) {
                case LINE_COMMENT:
                    commentService.addComment(tree, Comment.create(Comment.Style.LINE, NOPOS, NOPOS, indent, token.toString()));
                    indent = 0;
                    break;
                case BLOCK_COMMENT:
                    commentService.addComment(tree, Comment.create(Comment.Style.BLOCK, NOPOS, NOPOS, indent, token.toString()));
                    indent = NOPOS;
                    break;
                case JAVADOC_COMMENT:
                    commentService.addComment(tree, Comment.create(Comment.Style.JAVADOC, NOPOS, NOPOS, indent, token.toString()));
                    indent = NOPOS;
                    break;
                case WHITESPACE:
                    String tokenText = token.toString();
                    commentService.addComment(tree, Comment.create(Comment.Style.WHITESPACE, NOPOS, NOPOS, NOPOS, tokenText));
                    int newLinePos = tokenText.lastIndexOf('\n');
                    if (newLinePos < 0) {
                        if (indent >= 0)
                            indent += tokenText.length();
                    } else {
                        indent = tokenText.length() - newLinePos - 1;
                    }
                    break;
            }
        }
    }
    
    private void mapComments2(Tree tree) {
        if (((JCTree) tree).pos <= 0) {
            return;
        }
        collect(tree);
    }
    
    /*
        Implementation of new gathering algorithm based on comment weighting by natural (my) aligning of comments to statements.
     */
    
    private static Logger log = Logger.getLogger(TranslateIdentifier.class.getName());
    
    private void collect(Tree tree) {
        if (isEvil(tree)) {
            return;
        }
        JCTree.JCCompilationUnit cu = (JCTree.JCCompilationUnit) info.getCompilationUnit();
        int pos = findInterestingStart((JCTree) tree);
        seq.move(pos);
        lookForPreceedings(seq, tree, cu.endPositions);
        if (tree instanceof BlockTree) {
            BlockTree blockTree = (BlockTree) tree;
            if (blockTree.getStatements().isEmpty()) {
                lookWithinEmptyBlock(seq, blockTree, cu.endPositions);
            }
        }
        int end = getBounds((JCTree) tree, cu.endPositions)[1];
        seq.move(end);        
        lookForInline(seq, tree, cu.endPositions);
        lookForTrailing(seq, tree, cu.endPositions);

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "T: " + tree + "\nC: " + commentService.getComments(tree));
        }
    }

    private void lookForInline(TokenSequence<JavaTokenId> seq, Tree tree, Map<JCTree, Integer> endPositions) {
        seq.move(getBounds((JCTree) tree, endPositions)[1]);
        CommentsCollection result = new CommentsCollection();
        while (seq.moveNext()) {
            if (seq.index() <= tokenIndexAlreadyAdded) continue;
            if (seq.token().id() == JavaTokenId.WHITESPACE) {
                if (numberOfNL(seq.token()) > 0) {
                    break;
                }
            } else if (isComment(seq.token().id())) {
                result.add(seq.token());
                tokenIndexAlreadyAdded = seq.index(); 
            } else {
                break;
            }
        }
        if (!result.isEmpty()) {
            CommentSet.RelativePosition position = CommentSet.RelativePosition.INLINE;
            attachComments(tree, result, position);
        }
    }

    private void attachComments(Tree tree, CommentsCollection result, CommentSet.RelativePosition position) {
        CommentSetImpl cs = commentService.getComments(tree);
        for (Token<JavaTokenId> token : result) {
            attachComment(position, cs, token);
        }
    }

    private boolean isEvil(Tree tree) {
        Tree.Kind kind = tree.getKind();
        switch (kind) {
            case INT_LITERAL:            
            case LONG_LITERAL:            
            case BOOLEAN_LITERAL:            
            case CHAR_LITERAL:            
            case MODIFIERS:
            case COMPILATION_UNIT:
                return true;
            default: return false;
        }
    }

    private void lookForTrailing(TokenSequence<JavaTokenId> seq, Tree tree, Map<JCTree, Integer> endPositions) {
        //TODO: [RKo] This does not work correctly... need improvemetns.
        seq.move(getBounds((JCTree) tree, endPositions)[1]);
        CommentsCollection foundComments = null;
        int newlines = 0;
        while (seq.moveNext()) {
            if (seq.index() <= tokenIndexAlreadyAdded) continue;
            Token<JavaTokenId> t = seq.token();
            if (t.id() == JavaTokenId.WHITESPACE) {
                newlines += numberOfNL(t);
            } else if (isComment(t.id())) {
                if (foundComments != null) {
                    attachComments(foundComments, tree, commentService, endPositions, seq);
                }
                foundComments = getCommentsCollection(seq, newlines);
                if (t.id() == JavaTokenId.LINE_COMMENT) {
                    newlines = 1;
                } else {
                    newlines = 0;
                }

            } else {
                skipEvil(seq);
                Tree ctree = getTree(info.getTreeUtilities(), seq);
                if (ctree != null && foundComments != null) {
                    int[] bounds = foundComments.getBounds();
                    double weight = belongsTo(bounds[0], bounds[1], seq);
                    if (tree.getKind() == Tree.Kind.COMPILATION_UNIT && weight == 0) {
                        attachComments(foundComments, tree, commentService, endPositions, seq);
                    } else if (weight >= 0) {
//                        attachComments(foundComments, ctree, commentService, endPositions, seq);
                        return;
                    } else {
                        attachComments(foundComments, tree, commentService, endPositions, seq);
                    }
                    foundComments = null;
                }
                newlines = 0;
            }

        }
    }

    private void lookWithinEmptyBlock(TokenSequence<JavaTokenId> seq, BlockTree tree, Map<JCTree, Integer> endPositions) {
        // moving into opening brace.
        if (moveTo(seq, JavaTokenId.LBRACE, true)) {
            CommentsCollection cc = getCommentsCollection(seq, Integer.MAX_VALUE);
            attachComments(tree, cc, CommentSet.RelativePosition.INNER);
        } else {
            int end = getBounds((JCTree) tree, endPositions)[1];
            seq.move(end); seq.moveNext();
        }
    }

    /**
     * Moves <code>seq</code> to first occurence of specified <code>toToken</code> if specified direction.
     * @param seq sequence of tokens
     * @param toToken token to stop on.
     * @param forward move forward if true, backward otherwise
     * @return true if token has been reached.
     */
    private boolean moveTo(TokenSequence<JavaTokenId> seq, JavaTokenId toToken, boolean forward) {
        while (forward ? seq.moveNext() : seq.movePrevious()) {
            if (toToken == seq.token().id()) {
                return true;
            } 
        }
        return false;
    }

    private void lookForPreceedings(TokenSequence<JavaTokenId> seq, Tree tree, Map<JCTree, Integer> endPositions) {
        int reset = ((JCTree) tree).pos;
        CommentsCollection cc = null;
        while (seq.moveNext() && seq.offset() < reset) {
            JavaTokenId id = seq.token().id();
            if (isComment(id)) {
                if (cc == null) {
                    cc = getCommentsCollection(seq, Integer.MAX_VALUE);
                } else {
                    cc.merge(getCommentsCollection(seq, Integer.MAX_VALUE));
                }
            }
        }
        attachComments(cc, tree, commentService, endPositions, seq);
        seq.move(reset);
        seq.moveNext();
    }

    /**
     * Looking for position where to start looking up for preceeding commnets.
     * @param tree tree to examine.
     * @return position where to start 
     */
    private int findInterestingStart(JCTree tree) {
        if (tree.pos <= 0) return 0;
        seq.move(tree.pos);
        Tree shouldBeTree = null;
        while (seq.movePrevious() && tokenIndexAlreadyAdded < seq.index()) {
            switch (seq.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case JAVADOC_COMMENT:
                case BLOCK_COMMENT:
                    continue;
                case LBRACE:
                    /*
                        we are reaching parent tree element. This tree has no siblings or is first child. We have no 
                        interest in number of NL before this kind of comments. This comments are always considered 
                        as preceeding to tree.
                    */
                    return seq.offset() + seq.token().length();
                default: {
                    shouldBeTree = getTree(info.getTreeUtilities(), seq);
                    if (shouldBeTree == null || tree.equals(shouldBeTree)) {
                        //this is some kind of creepy token which is not interesting nor tree. Just skip it.
                        continue;
                    }
                    break;
                }
            }
        }
        return seq.offset(); 
    }

    private void consumeWS(TokenSequence<JavaTokenId> seq, boolean forward) {
        while (forward ? seq.moveNext() : seq.movePrevious()) {
            switch (seq.token().id()) {
                case WHITESPACE:
                    continue;
                default: return;
            }
        }
    }

    @SuppressWarnings({"MethodWithMultipleLoops"})
    private int adjustByComments(int pos, CommentSetImpl comments) {
        List<Comment> cl = comments.getComments(CommentSet.RelativePosition.INLINE);
        if (!cl.isEmpty()) {
            for (Comment comment : cl) {
                pos = Math.max(pos, comment.endPos());
            }
        }
        cl = comments.getComments(CommentSet.RelativePosition.TRAILING);
        if (!cl.isEmpty()) {
            for (Comment comment : cl) {
                pos = Math.max(pos, comment.endPos());
            }
        }
        return pos;
    }

    private void skipEvil(TokenSequence<JavaTokenId> ts) {
        do {
            JavaTokenId id = ts.token().id();
            switch (id) {
                case PUBLIC:
                case PRIVATE:
                case PROTECTED:
                case ABSTRACT:
                case FINAL:
                case STATIC:
                case VOID:
                case VOLATILE:
                case NATIVE:
                case STRICTFP:
                case WHITESPACE:
                case INT:
                case BOOLEAN:
                case DOUBLE:
                case FLOAT:
                case BYTE:
                case CHAR:
                case SHORT:
                case CONST:
                case LONG:
                    continue;
                default:
                    return;
            }
        } while (ts.moveNext());
    }

    private double belongsTo(int startPos, int endPos, TokenSequence<JavaTokenId> ts) {
        int index = ts.index();
        double result = getForwardWeight(endPos, ts) - getBackwardWeight(startPos, ts);
        ts.moveIndex(index);
        ts.moveNext();
        return result;
    }

    private double getForwardWeight(int endPos, TokenSequence<JavaTokenId> ts) {
        double result = 0;
        ts.move(endPos);
        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.WHITESPACE) {
                int nls = numberOfNL(ts.token());
                result = nls == 0 ? 1 : (1 / nls);
            } else if (isComment(ts.token().id())) {
                if (ts.token().id() == JavaTokenId.LINE_COMMENT) {
                    return 1;
                }
                result = 0;
                break;
            } else {
                break;
            }
        }
        return result;
    }

    private double getBackwardWeight(int startPos, TokenSequence<JavaTokenId> ts) {
        double result = 0;
        ts.move(startPos);
        while (ts.movePrevious()) {
            if (ts.token().id() == JavaTokenId.WHITESPACE) {
                int nls = numberOfNL(ts.token());
                result = nls == 0 ? 0 : (1 / nls);
            } else if (isComment(ts.token().id())) {
                result = 0;
                break;
            } else {
                break;
            }
        }
        return result;
    }

    private void attachComments(CommentsCollection foundComments, Tree tree, CommentHandler ch, Map<JCTree, Integer> endPositions, TokenSequence<JavaTokenId> ts) {
        if (foundComments == null || foundComments.isEmpty()) return;
        int[] bounds = getBounds((JCTree) tree, endPositions);
        CommentSet.RelativePosition positioning;
        if (tree instanceof BlockTree) {
            BlockTree bt = (BlockTree) tree;
            if (bt.getStatements().isEmpty()
                    && bounds[0] >= foundComments.getBounds()[0]
                    && bounds[1] <= foundComments.getBounds()[1]) {
                positioning = CommentSet.RelativePosition.INNER;
            } else {
                positioning = computePositioning(bounds, foundComments, ts);
            }
        } else {
            positioning = computePositioning(bounds, foundComments, ts);
        }
        CommentSet set = createCommentSet(ch, tree);
        for (Token<JavaTokenId> comment : foundComments) {
            attachComment(positioning, set, comment);
        }
    }

    private void attachComment(CommentSet.RelativePosition positioning, CommentSet set, Token<JavaTokenId> comment) {
        Comment c = Comment.create(getStyle(comment.id()), comment.offset(null),
                getEndPos(comment), NOPOS, getText(comment));
        set.addComment(positioning, c);        
    }

    private String getText(Token<JavaTokenId> comment) {
        return String.valueOf(comment.text());
    }

    private int getEndPos(Token<JavaTokenId> comment) {
        return comment.offset(null) + comment.length();
    }

    private Comment.Style getStyle(JavaTokenId id) {
        switch (id) {
            case JAVADOC_COMMENT:
                return Comment.Style.JAVADOC;
            case LINE_COMMENT:
                return Comment.Style.LINE;
            case BLOCK_COMMENT:
                return Comment.Style.BLOCK;
            default:
                return Comment.Style.WHITESPACE;
        }
    }

    private CommentSet.RelativePosition computePositioning(int[] treeBounds, CommentsCollection cc, TokenSequence<JavaTokenId> ts) {
        int[] commentsBounds = cc.getBounds();
        if (commentsBounds[1] < treeBounds[0]) return CommentSet.RelativePosition.PRECEDING;
        if (commentsBounds[0] > treeBounds[1]) {
            TokenSequence<JavaTokenId> sequence = ts.subSequence(treeBounds[1], commentsBounds[0]);
            sequence.move(0);
//            sequence.move(treeBounds[1]);
            if (!sequence.moveNext()) return CommentSet.RelativePosition.INLINE;
            switch (sequence.token().id()) {
                case WHITESPACE: {
                    if (numberOfNL(sequence.token()) > 0) {
                        return CommentSet.RelativePosition.TRAILING;
                    } else {
                        return CommentSet.RelativePosition.INLINE;
                    }
                }
                default:
                    return CommentSet.RelativePosition.TRAILING;
            }
        }

        if (commentsBounds[0] > treeBounds[0] && commentsBounds[1] < treeBounds[1])
            return CommentSet.RelativePosition.INNER;
        return CommentSet.RelativePosition.TRAILING;
    }

    private int[] getBounds(JCTree tree, Map<JCTree, Integer> endPositions) {
        return new int[]{TreeInfo.getStartPos(tree), TreeInfo.getEndPos(tree, endPositions)};
    }


    private Tree getTree(TreeUtilities tu, TokenSequence<JavaTokenId> ts) {
        int start = ts.offset();
        if (ts.token().length() > 0) {
            start++; //going into token. This is required because token offset is not considered as start of tree :(
        }
        TreePath path = tu.pathFor(start);
        if (path != null) {
            return path.getLeaf();
        }
        return null;
    }

    private int numberOfNL(Token<JavaTokenId> t) {
        int count = 0;
        CharSequence charSequence = t.text();
        for (int i = 0; i < charSequence.length(); i++) {
            char a = charSequence.charAt(i);
            if ('\n' == a) {
                count++;
            }
        }
        return count;
    }

    private CommentsCollection getCommentsCollection(TokenSequence<JavaTokenId> ts, int maxTension) {
        CommentsCollection result = new CommentsCollection();
        Token<JavaTokenId> t = ts.token();
        result.add(t);
        boolean isLC = t.id() == JavaTokenId.LINE_COMMENT;
        int lastCommentIndex = ts.index();
        int start = ts.offset();
        int end = ts.offset() + ts.token().length();
        while (ts.moveNext()) {
            t = ts.token();
            if (isComment(t.id())) {
                result.add(t);
                start = Math.min(ts.offset(), start);
                end = Math.max(ts.offset() + t.length(), end);
                isLC = t.id() == JavaTokenId.LINE_COMMENT;
                lastCommentIndex = ts.index();
                tokenIndexAlreadyAdded = ts.index();
            } else if (t.id() == JavaTokenId.WHITESPACE) {
                if ((numberOfNL(t) + (isLC ? 1 : 0)) > maxTension) {
                    break;
                }
            } else {
                break;                
            }
        }
        ts.moveIndex(lastCommentIndex);
        ts.moveNext();
        result.setBounds(new int[]{start, end});
        System.out.println("tokenIndexAlreadyAdded = " + tokenIndexAlreadyAdded);
        return result;
    }

    private CommentSet createCommentSet(CommentHandler ch, Tree lastTree) {
        return ch.getComments(lastTree);
    }

    private boolean isComment(JavaTokenId tid) {
        switch (tid) {
            case LINE_COMMENT:
            case BLOCK_COMMENT:
            case JAVADOC_COMMENT:
                return true;
            default:
                return false;
        }
    }

    private static class CommentsCollection implements Iterable<Token<JavaTokenId>> {
        private final int[] bounds = {NOPOS, NOPOS};
        private final List<Token<JavaTokenId>> comments = new LinkedList<Token<JavaTokenId>>();

        void add(Token<JavaTokenId> comment) {
            comments.add(comment);
        }

        boolean isEmpty() {
            return comments.isEmpty();
        }

        public Iterator<Token<JavaTokenId>> iterator() {
            return comments.iterator();
        }

        void setBounds(int[] bounds) {
            this.bounds[0] = bounds[0];
            this.bounds[1] = bounds[1];
        }

        public int[] getBounds() {
            return bounds.clone();
        }

        public void merge(CommentsCollection cc) {
            comments.addAll(cc.comments);
            this.bounds[0] = Math.min(this.bounds[0], cc.bounds[0]);
            this.bounds[1] = Math.max(this.bounds[1], cc.bounds[1]);
        }
    }
}