/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007-2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.impl.Utilities;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;

/**
 *
 * @author Jan Lahoda
 */
public class CopyFinder extends TreeScanner<Boolean, TreePath> {

    private final TreePath searchingFor;
    private final CompilationInfo info;
    private final Map<TreePath, VariableAssignments> result = new LinkedHashMap<TreePath, VariableAssignments>();
    private boolean allowGoDeeper = true;
    private Map<String, TreePath> variables = new HashMap<String, TreePath>(); //XXX
    private Map<String, Collection<? extends TreePath>> multiVariables = new HashMap<String, Collection<? extends TreePath>>(); //XXX
    private Map<String, String> variables2Names = new HashMap<String, String>(); //XXX
    private AtomicBoolean cancel;


    private Map<String, TypeMirror> designedTypeHack;

    private CopyFinder(TreePath searchingFor, CompilationInfo info, AtomicBoolean cancel) {
        this.searchingFor = searchingFor;
        this.info = info;
        this.cancel = cancel;
    }

    public static Map<TreePath, VariableAssignments> computeDuplicates(CompilationInfo info, TreePath searchingFor, TreePath scope, AtomicBoolean cancel, Map<String, TypeMirror> designedTypeHack) {
        return computeDuplicates(info, searchingFor, scope, true, cancel, designedTypeHack);
    }

    public static Map<TreePath, VariableAssignments> computeDuplicates(CompilationInfo info, TreePath searchingFor, TreePath scope, boolean fullElementVerify, AtomicBoolean cancel, Map<String, TypeMirror> designedTypeHack) {
        CopyFinder f =   fullElementVerify
                       ? new CopyFinder(searchingFor, info, cancel)
                       : new CopyFinder(searchingFor, info, cancel) {
            @Override
            protected boolean verifyElements(TreePath node, TreePath p) {
                return getSimpleName(node.getLeaf()).contentEquals(getSimpleName(p.getLeaf()));
            }
        };

        f.designedTypeHack = designedTypeHack;

        f.scan(scope, null);

        return f.result;
    }

    public static VariableAssignments computeVariables(CompilationInfo info, TreePath searchingFor, TreePath scope, AtomicBoolean cancel, Map<String, TypeMirror> designedTypeHack) {
        if (!sameKind(scope.getLeaf(), searchingFor.getLeaf())) {
            return null;
        }

        CopyFinder f = new CopyFinder(searchingFor, info, cancel);

        f.allowGoDeeper = false;


        f.designedTypeHack = designedTypeHack;

        if (f.scan(scope, searchingFor)) {
            return new VariableAssignments(f.variables, f.multiVariables, f.variables2Names);
        }

        return null;
    }

    public static boolean isDuplicate(CompilationInfo info, TreePath one, TreePath second, AtomicBoolean cancel) {
        return isDuplicate(info, one, second, true, cancel);
    }

    public static boolean isDuplicate(CompilationInfo info, TreePath one, TreePath second, boolean fullElementVerify, AtomicBoolean cancel) {
        return isDuplicate(info, one, second, fullElementVerify, null, false, cancel);
    }

    public static boolean isDuplicate(CompilationInfo info, TreePath one, TreePath second, boolean fullElementVerify, HintContext inVariables, boolean fillInVariables, AtomicBoolean cancel) {
        if (one.getLeaf().getKind() != second.getLeaf().getKind()) {
            return false;
        }

        CopyFinder f =   fullElementVerify
                       ? new CopyFinder(one, info, cancel)
                       : new CopyFinder(one, info, cancel) {
            @Override
            protected boolean verifyElements(TreePath node, TreePath p) {
                return getSimpleName(node.getLeaf()).contentEquals(getSimpleName(p.getLeaf()));
            }
        };

        if (inVariables != null) {
            if (fillInVariables) {
                f.variables = inVariables.getVariables();
                f.variables2Names = inVariables.getVariableNames();
                f.multiVariables = inVariables.getMultiVariables();
            } else {
                f.variables.putAll(inVariables.getVariables());
                f.variables2Names.putAll(inVariables.getVariableNames());
                f.multiVariables.putAll(inVariables.getMultiVariables());
            }
        }

        f.allowGoDeeper = false;

        return f.scan(second, one);
    }

    private static boolean sameKind(Tree t1, Tree t2) {
        Kind k1 = t1.getKind();
        Kind k2 = t2.getKind();

        if (k1 == k2) {
            return true;
        }

        if (isSingleStatemenBlockAndStatement(t1, t2) || isSingleStatemenBlockAndStatement(t2, t1)) {
            return true;
        }

        if (k2 == Kind.BLOCK && StatementTree.class.isAssignableFrom(k1.asInterface())) {
            BlockTree bt = (BlockTree) t2;

            if (bt.isStatic()) {
                return false;
            }

            switch (bt.getStatements().size()) {
                case 1:
                    return true;
                case 2:
                    return    Utilities.isMultistatementWildcardTree(bt.getStatements().get(0))
                           || Utilities.isMultistatementWildcardTree(bt.getStatements().get(1));
                case 3:
                    return    Utilities.isMultistatementWildcardTree(bt.getStatements().get(0))
                           || Utilities.isMultistatementWildcardTree(bt.getStatements().get(2));
            }

            return false;
        }

        if (    (k1 != Kind.MEMBER_SELECT && k1 != Kind.IDENTIFIER)
             || (k2 != Kind.MEMBER_SELECT && k2 != Kind.IDENTIFIER)) {
            return false;
        }

        return Utilities.isPureMemberSelect(t1, true) && Utilities.isPureMemberSelect(t2, true);
    }

    private static boolean isSingleStatemenBlockAndStatement(Tree t1, Tree t2) {
        Kind k1 = t1.getKind();
        Kind k2 = t2.getKind();

        if (k1 == Kind.BLOCK && ((BlockTree) t1).getStatements().size() == 1 && !((BlockTree) t1).isStatic()) {
            return StatementTree.class.isAssignableFrom(k2.asInterface());
        }

        return false;
    }

    private static final Set<TypeKind> IGNORE_KINDS = EnumSet.of(TypeKind.EXECUTABLE, TypeKind.PACKAGE);

    private TreePath currentPath;

    protected TreePath getCurrentPath() {
        return currentPath;
    }

    protected Boolean scan(TreePath path, TreePath param) {
        currentPath = path.getParentPath();
        try {
            return scan(path.getLeaf(), param);
        } finally {
            currentPath = null;
        }
    }

    @Override
    public Boolean scan(Tree node, TreePath p) {
        if (cancel.get()) {
            return false;
        }

        if (node == null)
            return p == null;

        if (p != null && p.getLeaf().getKind() == Kind.IDENTIFIER) {
            String ident = ((IdentifierTree) p.getLeaf()).getName().toString();

            if (ident.startsWith("$")) {
                if (variables2Names.containsKey(ident)) {
                    if (node.getKind() == Kind.IDENTIFIER)
                        return ((IdentifierTree) node).getName().toString().equals(variables2Names.get(ident));
                    else
                        return false; //correct?
                }

                TreePath currentPath = new TreePath(getCurrentPath(), node);
                TypeMirror designed = designedTypeHack != null ? designedTypeHack.get(ident) : null;//info.getTrees().getTypeMirror(p);

                boolean bind = true;

                if (designed != null && designed.getKind() != TypeKind.ERROR) {
                    TypeMirror real = info.getTrees().getTypeMirror(currentPath);

                    if (real != null && !IGNORE_KINDS.contains(real.getKind()))
                        bind = info.getTypes().isAssignable(real, designed);
                    else
                        bind = false;
                }

                if (bind) {
                    TreePath original = variables.get(ident);

                    if (original == null) {
                        variables.put(ident, currentPath);
                        return true;
                    } else {
                        boolean oldAllowGoDeeper = allowGoDeeper;

                        try {
                            return scan(node, original);
                        } finally {
                            allowGoDeeper = oldAllowGoDeeper;
                        }
                    }
                } else {
                    return false;
                }
            }
        }

        if (p != null && Utilities.getWildcardTreeName(p.getLeaf()) != null) {
            String ident = Utilities.getWildcardTreeName(p.getLeaf()).toString();

            if (ident.startsWith("$") && StatementTree.class.isAssignableFrom(node.getKind().asInterface())) {
                TreePath original = variables.get(ident);

                if (original == null) {
                    TreePath currentPath = new TreePath(getCurrentPath(), node);

                    variables.put(ident, currentPath);
                    return true;
                } else {
                    boolean oldAllowGoDeeper = allowGoDeeper;

                    try {
                        return scan(node, original);
                    } finally {
                        allowGoDeeper = oldAllowGoDeeper;
                    }
                }
            }
        }

        if (p != null && sameKind(node, p.getLeaf())) {
            //maybe equivalent:
            boolean result = superScan(node, p) == Boolean.TRUE;

            if (result) {
                if (p == searchingFor && node != searchingFor && allowGoDeeper) {
                    this.result.put(new TreePath(getCurrentPath(), node), new VariableAssignments(variables, multiVariables, variables2Names));
                    variables = new HashMap<String, TreePath>();
                    multiVariables = new HashMap<String, Collection<? extends TreePath>>();
                    variables2Names = new HashMap<String, String>();
                }

                return true;
            }
        }

        if (!allowGoDeeper)
            return false;

        if ((p != null && p.getLeaf() == searchingFor.getLeaf()) || !sameKind(node, searchingFor.getLeaf())) {
            superScan(node, null);
            return false;
        } else {
            //maybe equivalent:
            allowGoDeeper = false;

            boolean result = superScan(node, searchingFor) == Boolean.TRUE;

            allowGoDeeper = true;

            if (result) {
                if (node != searchingFor.getLeaf()) {
                    this.result.put(new TreePath(getCurrentPath(), node), new VariableAssignments(variables, multiVariables, variables2Names));
                    variables = new HashMap<String, TreePath>();
                    multiVariables = new HashMap<String, Collection<? extends TreePath>>();
                    variables2Names = new HashMap<String, String>();
                }

                return true;
            }

            superScan(node, null);
            return false;
        }
    }

    private Boolean superScan(Tree node, TreePath p) {
        if (p == null) {
            return doSuperScan(node, p);
        }

        if (p.getLeaf().getKind() == Kind.IDENTIFIER) {
            String ident = ((IdentifierTree) p.getLeaf()).getName().toString();

            if (ident.startsWith("$")) {
                return scan(node, p);
            }
        }

        if (p.getLeaf().getKind() == Kind.BLOCK && node.getKind() != Kind.BLOCK /*&& p.getLeaf() != searchingFor.getLeaf()*/) {
            BlockTree bt = (BlockTree) p.getLeaf();

            switch (bt.getStatements().size()) {
                case 1:
                    if (Utilities.isMultistatementWildcardTree(bt.getStatements().get(0))) {
                        if (!validateMultiVariable(bt.getStatements().get(0), Collections.singletonList(new TreePath(getCurrentPath(), node))))
                                return false;
                        return true;
                    }

                    p = new TreePath(p, bt.getStatements().get(0));
                    break;
                case 2:
                    if (Utilities.isMultistatementWildcardTree(bt.getStatements().get(0))) {
                        if (!validateMultiVariable(bt.getStatements().get(0), Collections.<TreePath>emptyList()))
                                return false;
                        p = new TreePath(p, bt.getStatements().get(1));
                        break;
                    }
                    if (Utilities.isMultistatementWildcardTree(bt.getStatements().get(1))) {
                        if (!validateMultiVariable(bt.getStatements().get(1), Collections.<TreePath>emptyList()))
                                return false;
                        p = new TreePath(p, bt.getStatements().get(0));
                        break;
                    }
                    throw new UnsupportedOperationException();
                case 3:
                    if (   Utilities.isMultistatementWildcardTree(bt.getStatements().get(0))
                        && Utilities.isMultistatementWildcardTree(bt.getStatements().get(2))) {
                        if (!validateMultiVariable(bt.getStatements().get(0), Collections.<TreePath>emptyList()))
                                return false;
                        if (!validateMultiVariable(bt.getStatements().get(2), Collections.<TreePath>emptyList()))
                                return false;
                        p = new TreePath(p, bt.getStatements().get(1));
                        break;
                    }
                    throw new UnsupportedOperationException();
            }
        }

        if (!sameKind(node, p.getLeaf())) {
            return false;
        }

        return doSuperScan(node, p);
    }

    private Boolean doSuperScan(Tree node, TreePath p) {
        if (node == null) return null;
        TreePath prev = currentPath;
        try {
            currentPath = new TreePath(currentPath, node);
            return super.scan(node, p);
        } finally {
            currentPath = prev;
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

        if (Utilities.containsMultistatementTrees(other)) {
            return checkListsWithMultistatementTrees(one, 0, other, 0, otherOrigin);
        }

        if (one.size() != other.size())
            return false;

        for (int cntr = 0; cntr < one.size(); cntr++) {
            if (!scan(one.get(cntr), other.get(cntr), otherOrigin))
                return false;
        }

        return true;
    }

    public Boolean visitAssert(AssertTree node, TreePath p) {
        if (p == null) {
            super.visitAssert(node, p);
            return false;
        }

        AssertTree at = (AssertTree) p.getLeaf();

        if (!scan(node.getCondition(), at.getCondition(), p)) {
            return false;
        }

        return scan(node.getDetail(), at.getDetail(), p);
    }

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

    private boolean validateMultiVariable(Tree t, List<? extends TreePath> tps) {
        String name = Utilities.getWildcardTreeName(t).toString();
        Collection<? extends TreePath> original = this.multiVariables.get(name);

        if (original == null) {
            this.multiVariables.put(name, tps);
            return true;
        } else {
            if (tps.size() != original.size()) {
                return false;
            }

            Iterator<? extends TreePath> orig = original.iterator();
            Iterator<? extends TreePath> current = tps.iterator();

            while (orig.hasNext() && current.hasNext()) {
                if (!scan(current.next(), orig.next())) {
                    return false;
                }
            }

            return true;
        }
    }

    //TODO: currently, only the first matching combination is found:
    private boolean checkListsWithMultistatementTrees(List<? extends Tree> real, int realOffset, List<? extends Tree> pattern, int patternOffset, TreePath p) {
        while (realOffset < real.size() && patternOffset < pattern.size() && !Utilities.isMultistatementWildcardTree(pattern.get(patternOffset))) {
            if (!scan(real.get(realOffset), pattern.get(patternOffset), p)) {
                return false;
            }

            realOffset++;
            patternOffset++;
        }

        if (realOffset == real.size() && patternOffset == pattern.size()) {
            return true;
        }

        if (Utilities.isMultistatementWildcardTree(pattern.get(patternOffset))) {
            if (patternOffset + 1 == pattern.size()) {
                List<TreePath> tps = new LinkedList<TreePath>();

                for (Tree t : real.subList(realOffset, real.size())) {
                    tps.add(new TreePath(getCurrentPath(), t));
                }

                return validateMultiVariable(pattern.get(patternOffset), tps);
            }

            List<TreePath> tps = new LinkedList<TreePath>();

            while (realOffset < real.size()) {
                Map<String, TreePath> variables = this.variables;
                Map<String, Collection<? extends TreePath>> multiVariables = this.multiVariables;
                Map<String, String> variables2Names = this.variables2Names;

                this.variables = new HashMap<String, TreePath>(variables);
                this.multiVariables = new HashMap<String, Collection<? extends TreePath>>(multiVariables);
                this.variables2Names = new HashMap<String, String>(variables2Names);

                if (checkListsWithMultistatementTrees(real, realOffset, pattern, patternOffset + 1, p)) {
                    return validateMultiVariable(pattern.get(patternOffset), tps);
                }

                this.variables = variables;
                this.multiVariables = multiVariables;
                this.variables2Names = variables2Names;

                tps.add(new TreePath(getCurrentPath(), real.get(realOffset)));

                realOffset++;
            }

            return false;
        }

        return false;
    }

    public Boolean visitBlock(BlockTree node, TreePath p) {
        if (p == null) {
            super.visitBlock(node, p);
            return false;
        }

        if (p.getLeaf().getKind() != Kind.BLOCK) {
            //single-statement blocks are considered to be equivalent to statements
            //TODO: some parents may need to be more strict, esp. synchronized and do-while
            assert node.getStatements().size() == 1;
            assert !node.isStatic();

            if (p.getLeaf() == searchingFor.getLeaf())
                return false;

            return checkLists(node.getStatements(), Collections.singletonList(p.getLeaf()), p.getParentPath());
        }

        BlockTree at = (BlockTree) p.getLeaf();

        if (node.isStatic() != at.isStatic()) {
            return false;
        }

        return checkLists(node.getStatements(), at.getStatements(), p);
    }

//    public Boolean visitBreak(BreakTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitCase(CaseTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitCatch(CatchTree node, TreePath p) {
        if (p == null) {
            super.visitCatch(node, p);
            return false;
        }

        CatchTree ef = (CatchTree) p.getLeaf();

        if (!scan(node.getParameter(), ef.getParameter(), p))
            return false;

        return scan(node.getBlock(), ef.getBlock(), p);
    }

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

    public Boolean visitDoWhileLoop(DoWhileLoopTree node, TreePath p) {
        if (p == null) {
            super.visitDoWhileLoop(node, p);
            return false;
        }

        DoWhileLoopTree t = (DoWhileLoopTree) p.getLeaf();

        if (!scan(node.getStatement(), t.getStatement(), p))
            return false;

        return scan(node.getCondition(), t.getCondition(), p);
    }

//    public Boolean visitErroneous(ErroneousTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitExpressionStatement(ExpressionStatementTree node, TreePath p) {
        if (p == null) {
            super.visitExpressionStatement(node, p);
            return false;
        }

        ExpressionStatementTree et = (ExpressionStatementTree) p.getLeaf();

        return scan(node.getExpression(), et.getExpression(), p);
    }

    public Boolean visitEnhancedForLoop(EnhancedForLoopTree node, TreePath p) {
        if (p == null) {
            super.visitEnhancedForLoop(node, p);
            return false;
        }

        EnhancedForLoopTree ef = (EnhancedForLoopTree) p.getLeaf();

        if (!scan(node.getVariable(), ef.getVariable(), p))
            return false;

        if (!scan(node.getExpression(), ef.getExpression(), p))
            return false;

        return scan(node.getStatement(), ef.getStatement(), p);
    }

    public Boolean visitForLoop(ForLoopTree node, TreePath p) {
        if (p == null)
            return super.visitForLoop(node, p);

        ForLoopTree t = (ForLoopTree) p.getLeaf();

        if (!checkLists(node.getInitializer(), t.getInitializer(), p)) {
            return false;
        }

        if (!scan(node.getCondition(), t.getCondition(), p))
            return false;

        if (!checkLists(node.getUpdate(), t.getUpdate(), p))
            return false;

        return scan(node.getStatement(), t.getStatement(), p);
    }

    public Boolean visitIdentifier(IdentifierTree node, TreePath p) {
        if (p == null)
            return super.visitIdentifier(node, p);

        return verifyElements(getCurrentPath(), p);
    }

    public Boolean visitIf(IfTree node, TreePath p) {
        if (p == null)
            return super.visitIf(node, p);

        IfTree t = (IfTree) p.getLeaf();

        if (!scan(node.getCondition(), t.getCondition(), p))
            return false;

        if (!scan(node.getThenStatement(), t.getThenStatement(), p))
            return false;

        return scan(node.getElseStatement(), t.getElseStatement(), p);
    }

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

    public Boolean visitModifiers(ModifiersTree node, TreePath p) {
        if (p == null)
            return super.visitModifiers(node, p);

        ModifiersTree t = (ModifiersTree) p.getLeaf();

        if (!checkLists(node.getAnnotations(), t.getAnnotations(), p))
            return false;

        return node.getFlags().equals(t.getFlags());
    }

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

    public Boolean visitReturn(ReturnTree node, TreePath p) {
        if (p == null) {
            super.visitReturn(node, p);
            return false;
        }

        ReturnTree at = (ReturnTree) p.getLeaf();

        return scan(node.getExpression(), at.getExpression(), p);
    }

    public Boolean visitMemberSelect(MemberSelectTree node, TreePath p) {
        if (p == null)
            return super.visitMemberSelect(node, p);

        if (Utilities.isPureMemberSelect(node, true) && Utilities.isPureMemberSelect(p.getLeaf(), true)) {
            boolean ret = verifyElements(getCurrentPath(), p);

            if (ret) {
                if (node.getKind() == p.getLeaf().getKind()) {
                    //to bind any free variables inside:
                    MemberSelectTree t = (MemberSelectTree) p.getLeaf();

                    scan(node.getExpression(), t.getExpression(), p);
                }

                return ret;
            }
        }

        if (node.getKind() != p.getLeaf().getKind()) {
            return false;
        }

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
    public Boolean visitSynchronized(SynchronizedTree node, TreePath p) {
        if (p == null) {
            super.visitSynchronized(node, p);
            return false;
        }

        SynchronizedTree at = (SynchronizedTree) p.getLeaf();

        if (!scan(node.getExpression(), at.getExpression(), p)) {
            return false;
        }

        return scan(node.getBlock(), at.getBlock(), p);
    }

    public Boolean visitThrow(ThrowTree node, TreePath p) {
        if (p == null) {
            super.visitThrow(node, p);
            return false;
        }

        ThrowTree at = (ThrowTree) p.getLeaf();

        return scan(node.getExpression(), at.getExpression(), p);
    }

//    public Boolean visitCompilationUnit(CompilationUnitTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitTry(TryTree node, TreePath p) {
        if (p == null) {
            super.visitTry(node, p);
            return false;
        }

        TryTree at = (TryTree) p.getLeaf();

        if (!scan(node.getBlock(), at.getBlock(), p)) {
            return false;
        }

        if (!checkLists(node.getCatches(), at.getCatches(), p)) {
            return false;
        }

        return scan(node.getFinallyBlock(), at.getFinallyBlock(), p);
    }

    public Boolean visitParameterizedType(ParameterizedTypeTree node, TreePath p) {
        if (p == null)
            return super.visitParameterizedType(node, p);

        ParameterizedTypeTree t = (ParameterizedTypeTree) p.getLeaf();

        if (!scan(node.getType(), t.getType(), p))
            return false;

        return checkLists(node.getTypeArguments(), t.getTypeArguments(), p);
    }

    public Boolean visitArrayType(ArrayTypeTree node, TreePath p) {
        if (p == null) {
            super.visitArrayType(node, p);
            return false;
        }

        ArrayTypeTree at = (ArrayTypeTree) p.getLeaf();

        return scan(node.getType(), at.getType(), p);
    }

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

    public Boolean visitVariable(VariableTree node, TreePath p) {
        if (p == null) {
            return super.visitVariable(node, p);
        }

        VariableTree t = (VariableTree) p.getLeaf();

        if (!scan(node.getModifiers(), t.getModifiers(), p))
            return false;

        if (!scan(node.getType(), t.getType(), p))
            return false;

        String name = t.getName().toString();

        if (name.startsWith("$")) { //XXX: there should be a utility method for this check
            String existingName = variables2Names.get(name);
            String currentName = node.getName().toString();

            if (existingName != null) {
                if (!existingName.equals(name)) {
                    return false;
                }
            } else {
                //XXX: putting the variable into both variables and variable2Names.
                //variables is needed by the declarative hints to support conditions like
                //referencedIn($variable, $statements$):
                //causes problems in JavaFix, see visitIdentifier there.
                variables.put(name, getCurrentPath());
                variables2Names.put(name, currentName);
            }
        }

        return scan(node.getInitializer(), t.getInitializer(), p);
    }

    public Boolean visitWhileLoop(WhileLoopTree node, TreePath p) {
        if (p == null)
            return super.visitWhileLoop(node, p);

        WhileLoopTree t = (WhileLoopTree) p.getLeaf();

        if (!scan(node.getCondition(), t.getCondition(), p))
            return false;

        return scan(node.getStatement(), t.getStatement(), p);
    }
///
//    public Boolean visitWildcard(WildcardTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Boolean visitOther(Tree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }


    protected boolean verifyElements(TreePath node, TreePath p) {
        Element nodeEl = info.getTrees().getElement(node);
        Element pEl    = info.getTrees().getElement(p);

        if (nodeEl == pEl) { //covers null == null
            return true;
        }

        if (nodeEl == null || pEl == null)
            return false;

        if (nodeEl.getKind() == pEl.getKind() && nodeEl.getKind() == ElementKind.METHOD) {
            if (info.getElements().overrides((ExecutableElement) nodeEl, (ExecutableElement) pEl, (TypeElement) nodeEl.getEnclosingElement())) {
                return true;
            }
        }

        return nodeEl.equals(pEl);
    }

    private static Name getSimpleName(Tree t) {
        if (t.getKind() == Kind.IDENTIFIER) {
            return ((IdentifierTree) t).getName();
        }
        if (t.getKind() == Kind.MEMBER_SELECT) {
            return ((MemberSelectTree) t).getIdentifier();
        }

        throw new UnsupportedOperationException();
    }

    public static final class VariableAssignments {
        public final Map<String, TreePath> variables;
        public final Map<String, Collection<? extends TreePath>> multiVariables;
        public final Map<String, String> variables2Names;

        public VariableAssignments(Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variables2Names) {
            this.variables = variables;
            this.multiVariables = multiVariables;
            this.variables2Names = variables2Names;
        }

    }
}
