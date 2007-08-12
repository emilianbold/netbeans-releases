/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.debug;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class TreeNode extends AbstractNode implements OffsetProvider {
    
    private TreePath tree;
    private CompilationInfo info;
    private boolean synthetic;
    
    public static Node getTree(CompilationInfo info, TreePath tree) {
        List<Node> result = new ArrayList<Node>();
        
        new FindChildrenTreeVisitor(info).scan(tree, result);
        
        return result.get(0);
    }

    /** Creates a new instance of TreeNode */
    public TreeNode(CompilationInfo info, TreePath tree, List<Node> nodes) {
        super(nodes.isEmpty() ? Children.LEAF: new NodeChilren(nodes));
        this.tree = tree;
        this.info = info;
        this.synthetic = info.getTreeUtilities().isSynthetic(tree);
        setDisplayName(tree.getLeaf().getKind().toString() + ":" + tree.getLeaf().toString()); //NOI18N
        setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/tree.png"); //NOI18N
    }

    @Override
    public String getHtmlDisplayName() {
        if (synthetic) {
            return "<html><font color='#808080'>" + translate(getDisplayName()); //NOI18N
        }
        
        return null;
    }
            
    private static String[] c = new String[] {"&", "<", ">", "\""}; // NOI18N
    private static String[] tags = new String[] {"&amp;", "&lt;", "&gt;", "&quot;"}; // NOI18N
    
    private String translate(String input) {
        for (int cntr = 0; cntr < c.length; cntr++) {
            input = input.replaceAll(c[cntr], tags[cntr]);
        }
        
        return input;
    }
    
    public int getStart() {
        return (int)info.getTrees().getSourcePositions().getStartPosition(tree.getCompilationUnit(), tree.getLeaf());
    }

    public int getEnd() {
        return (int)info.getTrees().getSourcePositions().getEndPosition(tree.getCompilationUnit(), tree.getLeaf());
    }

    private static final class NodeChilren extends Children.Keys {
        
        public NodeChilren(List<Node> nodes) {
            setKeys(nodes);
        }
        
        protected Node[] createNodes(Object key) {
            return new Node[] {(Node) key};
        }
        
    }
    
    private static class FindChildrenTreeVisitor extends TreePathScanner<Void, List<Node>> {
        
        private CompilationInfo info;
        
        public FindChildrenTreeVisitor(CompilationInfo info) {
            this.info = info;
        }
        
        @Override
        public Void visitAnnotation(AnnotationTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            //???
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitAnnotation(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitMethodInvocation(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitAssert(AssertTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitAssert(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitAssignment(AssignmentTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitAssignment(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitCompoundAssignment(CompoundAssignmentTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitCompoundAssignment(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitBinary(BinaryTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitBinary(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitBlock(BlockTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitBlock(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitBreak(BreakTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitBreak(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitCase(CaseTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitCase(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitCatch(CatchTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitCatch(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitClass(ClassTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitClass(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitConditionalExpression(ConditionalExpressionTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitConditionalExpression(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitContinue(ContinueTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitContinue(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitDoWhileLoop(DoWhileLoopTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitDoWhileLoop(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitErroneous(ErroneousTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            scan(((com.sun.tools.javac.tree.JCTree.JCErroneous)tree).errs, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitExpressionStatement(ExpressionStatementTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitExpressionStatement(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitEnhancedForLoop(EnhancedForLoopTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitEnhancedForLoop(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitForLoop(ForLoopTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitForLoop(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitIdentifier(IdentifierTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitIdentifier(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitIf(IfTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitIf(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitImport(ImportTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitImport(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitArrayAccess(ArrayAccessTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitArrayAccess(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitLabeledStatement(LabeledStatementTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitLabeledStatement(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitLiteral(LiteralTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitLiteral(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitMethod(MethodTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitMethod(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitModifiers(ModifiersTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitModifiers(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitNewArray(NewArrayTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitNewArray(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitNewClass(NewClassTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitNewClass(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitParenthesized(ParenthesizedTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitParenthesized(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitReturn(ReturnTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitReturn(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitMemberSelect(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitEmptyStatement(EmptyStatementTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitEmptyStatement(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitSwitch(SwitchTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitSwitch(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitSynchronized(SynchronizedTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitSynchronized(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitThrow(ThrowTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitThrow(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitCompilationUnit(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitTry(TryTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitTry(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitParameterizedType(ParameterizedTypeTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitParameterizedType(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitArrayType(ArrayTypeTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitArrayType(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitTypeCast(TypeCastTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitTypeCast(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitPrimitiveType(PrimitiveTypeTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitPrimitiveType(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitTypeParameter(TypeParameterTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitTypeParameter(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitInstanceOf(InstanceOfTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitInstanceOf(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitUnary(UnaryTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitUnary(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitVariable(VariableTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitVariable(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitWhileLoop(WhileLoopTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitWhileLoop(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitWildcard(WildcardTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitWildcard(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }
        
        private void addCorrespondingElement(List<Node> below) {
            Element el = info.getTrees().getElement(getCurrentPath());
            
            if (el != null) {
                below.add(new ElementNode(info, el, Collections.EMPTY_LIST));
            } else {
                below.add(new NotFoundElementNode(NbBundle.getMessage(TreeNode.class, "Cannot_Resolve_Element")));
            }
        }

        private void addCorrespondingType(List<Node> below) {
            TypeMirror tm = info.getTrees().getTypeMirror(getCurrentPath());
            
            if (tm != null) {
                below.add(new TypeNode(tm));
            } else {
                below.add(new NotFoundTypeNode(NbBundle.getMessage(TreeNode.class, "Cannot_Resolve_Type")));
            }
        }
        
        private void addCorrespondingComments(List<Node> below) {
            below.add(new CommentsNode(NbBundle.getMessage(TreeNode.class, "NM_Preceding_Comments"), info.getTreeUtilities().getComments(getCurrentPath().getLeaf(), true)));
            below.add(new CommentsNode(NbBundle.getMessage(TreeNode.class, "NM_Trailing_Comments"), info.getTreeUtilities().getComments(getCurrentPath().getLeaf(), false)));
        }
    }
    
    private static class NotFoundElementNode extends AbstractNode {
        
        public NotFoundElementNode(String name) {
            super(Children.LEAF);
            setName(name);
            setDisplayName(name);
            setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/element.png"); //NOI18N
        }
        
    }
    
    private static class TypeNode extends AbstractNode {
        
        public TypeNode(TypeMirror type) {
            super(Children.LEAF);
            setDisplayName(type.getKind().toString() + ":" + type.toString()); //NOI18N
            setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/type.png"); //NOI18N
        }
        
    }
    
    private static class NotFoundTypeNode extends AbstractNode {
        
        public NotFoundTypeNode(String name) {
            super(Children.LEAF);
            setName(name);
            setDisplayName(name);
            setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/type.png"); //NOI18N
        }
        
    }    
}
