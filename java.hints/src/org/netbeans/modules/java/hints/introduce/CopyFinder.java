/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author Jan Lahoda
 */
public class CopyFinder extends TreePathScanner<Boolean, TreePath> {

    private final TreePath searchingFor;
    private final CompilationInfo info;
    private final List<TreePath> result = new LinkedList<TreePath>();
    private boolean allowGoDeeper = true;
    private AtomicBoolean cancel;

    private CopyFinder(TreePath searchingFor, CompilationInfo info, AtomicBoolean cancel) {
        this.searchingFor = searchingFor;
        this.info = info;
        this.cancel = cancel;
    }
    
    public static List<TreePath> computeDuplicates(CompilationInfo info, TreePath searchingFor, TreePath scope, AtomicBoolean cancel) {
        CopyFinder f = new CopyFinder(searchingFor, info, cancel);
        
        f.scan(scope, null);
        
        return f.result;
    }

    public static boolean isDuplicate(CompilationInfo info, TreePath one, TreePath second, AtomicBoolean cancel) {
        if (one.getLeaf().getKind() != second.getLeaf().getKind()) {
            return false;
        }
        
        CopyFinder f = new CopyFinder(one, info, cancel);

        f.allowGoDeeper = false;
        
        return f.scan(second, one);
    }

    @Override
    public Boolean scan(Tree node, TreePath p) {
        if (cancel.get()) {
            return false;
        }
        
        if (node == null)
            return p == null;
        
        if (p != null && node.getKind() == p.getLeaf().getKind()) {
            //maybe equivalent:
            boolean result = super.scan(node, p) == Boolean.TRUE;

            if (result) {
                if (p == searchingFor && node != searchingFor) {
                    this.result.add(new TreePath(getCurrentPath(), node));
                }
                
                return true;
            }
        }
        
        if (!allowGoDeeper)
            return false;
        
        if ((p != null && p.getLeaf() == searchingFor.getLeaf()) || node.getKind() != searchingFor.getLeaf().getKind()) {
            super.scan(node, null);
            return false;
        } else {
            //maybe equivalent:
            allowGoDeeper = false;
            
            boolean result = super.scan(node, searchingFor) == Boolean.TRUE;
            
            allowGoDeeper = true;
            
            if (result) {
                if (node != searchingFor.getLeaf()) {
                    this.result.add(new TreePath(getCurrentPath(), node));
                }
                
                return true;
            }
            
            super.scan(node, null);
            return false;
        }
    }

    private Boolean scan(Tree node, Tree p, TreePath pOrigin) {
        if (node == null || p == null)
            return node == p;
        
        return scan(node, new TreePath(pOrigin, p));
    }
    
//    public Boolean visitAnnotation(AnnotationTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitMethodInvocation(MethodInvocationTree node, TreePath p) {
        if (p == null)
            return super.visitMethodInvocation(node, p);
        
        MethodInvocationTree t = (MethodInvocationTree) p.getLeaf();
        
        if (!scan(node.getMethodSelect(), t.getMethodSelect(), p))
            return false;
        
        if (!checkLists(node.getTypeArguments(), t.getTypeArguments(), p))
            return false;
        
        return checkLists(node.getArguments(), t.getArguments(), p);
    }
    
    private <T extends Tree> boolean checkLists(List<? extends T> one, List<? extends T> other, TreePath otherOrigin) {
        if (one == null || other == null) {
            return one == other;
        }
        
        if (one.size() != other.size())
            return false;
        
        for (int cntr = 0; cntr < one.size(); cntr++) {
            if (!scan(one.get(cntr), other.get(cntr), otherOrigin))
                return false;
        }
        
        return true;
    }

//    public Boolean visitAssert(AssertTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitAssignment(AssignmentTree node, TreePath p) {
        if (p == null)
            return super.visitAssignment(node, p);
        
        AssignmentTree at = (AssignmentTree) p.getLeaf();
        
        boolean result = scan(node.getExpression(), at.getExpression(), p);
        
        return result && scan(node.getVariable(), at.getVariable(), p);
    }

    public Boolean visitCompoundAssignment(CompoundAssignmentTree node, TreePath p) {
        if (p == null) {
            super.visitCompoundAssignment(node, p);
            return false;
        }
        
        CompoundAssignmentTree bt = (CompoundAssignmentTree) p.getLeaf();
        boolean result = scan(node.getExpression(), bt.getExpression(), p);
        
        return result && scan(node.getVariable(), bt.getVariable(), p);
    }

    public Boolean visitBinary(BinaryTree node, TreePath p) {
        if (p == null) {
            super.visitBinary(node, p);
            return false;
        }
        
        BinaryTree bt = (BinaryTree) p.getLeaf();
        boolean result = scan(node.getLeftOperand(), bt.getLeftOperand(), p);
        
        return result && scan(node.getRightOperand(), bt.getRightOperand(), p);
    }

//    public Boolean visitBlock(BlockTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitBreak(BreakTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitCase(CaseTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitCatch(CatchTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitClass(ClassTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitConditionalExpression(ConditionalExpressionTree node, TreePath p) {
        if (p == null) {
            super.visitConditionalExpression(node, p);
            return false;
        }
        
        ConditionalExpressionTree t = (ConditionalExpressionTree) p.getLeaf();
        
        if (!scan(node.getCondition(), t.getCondition(), p))
            return false;
        
        if (!scan(node.getFalseExpression(), t.getFalseExpression(), p))
            return false;
        
        return scan(node.getTrueExpression(), t.getTrueExpression(), p);
    }

//    public Boolean visitContinue(ContinueTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitDoWhileLoop(DoWhileLoopTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitErroneous(ErroneousTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

//    public Boolean visitExpressionStatement(ExpressionStatementTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

//    public Boolean visitEnhancedForLoop(EnhancedForLoopTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitForLoop(ForLoopTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitIdentifier(IdentifierTree node, TreePath p) {
        if (p == null)
            return super.visitIdentifier(node, p);
        
        Element nodeEl = info.getTrees().getElement(getCurrentPath());
        Element pEl    = info.getTrees().getElement(p);

        if (nodeEl == pEl) { //covers null == null
            return true;
        }
        
        if (nodeEl == null || pEl == null)
            return false;
        
        return nodeEl.equals(pEl);
    }

//    public Boolean visitIf(IfTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitImport(ImportTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitArrayAccess(ArrayAccessTree node, TreePath p) {
        if (p == null)
            return super.visitArrayAccess(node, p);
        
        ArrayAccessTree t = (ArrayAccessTree) p.getLeaf();
        
        if (!scan(node.getExpression(), t.getExpression(), p))
            return false;
        
        return scan(node.getIndex(), t.getIndex(), p);
    }

//    public Boolean visitLabeledStatement(LabeledStatementTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitLiteral(LiteralTree node, TreePath p) {
        if (p == null)
            return super.visitLiteral(node, p);
        
        LiteralTree lt = (LiteralTree) p.getLeaf();
        Object nodeValue = node.getValue();
        Object ltValue = lt.getValue();
        
        if (nodeValue == ltValue)
            return true;
        
        if (nodeValue == null || ltValue == null)
            return false;
        return nodeValue.equals(ltValue);
    }

//    public Boolean visitMethod(MethodTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

//    public Boolean visitModifiers(ModifiersTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitNewArray(NewArrayTree node, TreePath p) {
        if (p == null)
            return super.visitNewArray(node, p);
        
        NewArrayTree t = (NewArrayTree) p.getLeaf();
        
        if (!checkLists(node.getDimensions(), t.getDimensions(), p))
            return false;
        
        if (!checkLists(node.getInitializers(), t.getInitializers(), p))
            return false;
        
        return scan(node.getType(), t.getType(), p);
    }

    public Boolean visitNewClass(NewClassTree node, TreePath p) {
        if (p == null)
            return super.visitNewClass(node, p);
        
        NewClassTree t = (NewClassTree) p.getLeaf();
        
        if (!scan(node.getIdentifier(), t.getIdentifier(), p))
            return false;
        
        if (!scan(node.getEnclosingExpression(), t.getEnclosingExpression(), p))
            return false;
        
        if (!checkLists(node.getTypeArguments(), t.getTypeArguments(), p))
            return false;
        
        if (!checkLists(node.getArguments(), t.getArguments(), p))
            return false;
        
        return scan(node.getClassBody(), t.getClassBody(), p);
    }

    public Boolean visitParenthesized(ParenthesizedTree node, TreePath p) {
        if (p == null)
            return super.visitParenthesized(node, p);
        
        ParenthesizedTree t = (ParenthesizedTree) p.getLeaf();
        
        return scan(node.getExpression(), t.getExpression(), p);
    }

//    public Boolean visitReturn(ReturnTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitMemberSelect(MemberSelectTree node, TreePath p) {
        if (p == null)
            return super.visitMemberSelect(node, p);
        
        MemberSelectTree t = (MemberSelectTree) p.getLeaf();
        
        if (!scan(node.getExpression(), t.getExpression(), p))
            return false;
        
        return node.getIdentifier().toString().equals(t.getIdentifier().toString());
    }

//    public Boolean visitEmptyStatement(EmptyStatementTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitSwitch(SwitchTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitSynchronized(SynchronizedTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitThrow(ThrowTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

//    public Boolean visitCompilationUnit(CompilationUnitTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

//    public Boolean visitTry(TryTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitParameterizedType(ParameterizedTypeTree node, TreePath p) {
        if (p == null)
            return super.visitParameterizedType(node, p);
        
        ParameterizedTypeTree t = (ParameterizedTypeTree) p.getLeaf();
        
        if (!scan(node.getType(), t.getType(), p))
            return false;
        
        return checkLists(node.getTypeArguments(), t.getTypeArguments(), p);
    }

//    public Boolean visitArrayType(ArrayTypeTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitTypeCast(TypeCastTree node, TreePath p) {
        if (p == null)
            return super.visitTypeCast(node, p);
        
        TypeCastTree t = (TypeCastTree) p.getLeaf();
        
        if (!scan(node.getType(), t.getType(), p))
            return false;
        
        return scan(node.getExpression(), t.getExpression(), p);
    }

    public Boolean visitPrimitiveType(PrimitiveTypeTree node, TreePath p) {
        if (p == null)
            return super.visitPrimitiveType(node, p);
        
        PrimitiveTypeTree t = (PrimitiveTypeTree) p.getLeaf();
        
        return node.getPrimitiveTypeKind() == t.getPrimitiveTypeKind();
    }

//    public Boolean visitTypeParameter(TypeParameterTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitInstanceOf(InstanceOfTree node, TreePath p) {
        if (p == null)
            return super.visitInstanceOf(node, p);
        
        InstanceOfTree t = (InstanceOfTree) p.getLeaf();
        
        if (!scan(node.getExpression(), t.getExpression(), p))
            return false;
        
        return scan(node.getType(), t.getType(), p);
    }

    public Boolean visitUnary(UnaryTree node, TreePath p) {
        if (p == null)
            return super.visitUnary(node, p);
        
        UnaryTree t = (UnaryTree) p.getLeaf();
        
        return scan(node.getExpression(), t.getExpression(), p);
    }

//    public Boolean visitVariable(VariableTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

//    public Boolean visitWhileLoop(WhileLoopTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitWildcard(WildcardTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitOther(Tree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
    

}
