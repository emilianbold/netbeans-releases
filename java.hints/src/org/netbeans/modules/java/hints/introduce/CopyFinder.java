/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007-2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
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
import com.sun.source.tree.Scope;
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
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.NonNull;
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
    private Set<VariableElement> variablesWithAllowedRemap = Collections.emptySet();
    private State bindState = State.empty();
    private boolean allowVariablesRemap = false;
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

    public static Map<TreePath, VariableAssignments> computeDuplicates(final CompilationInfo info, TreePath searchingFor, TreePath scope, boolean fullElementVerify, AtomicBoolean cancel, Map<String, TypeMirror> designedTypeHack) {
        CopyFinder f =   fullElementVerify
                       ? new CopyFinder(searchingFor, info, cancel)
                       : new CopyFinder(searchingFor, info, cancel) {
            @Override
            protected VerifyResult verifyElements(TreePath node, TreePath p) {
                return getSimpleName(node.getLeaf()).contentEquals(getSimpleName(p.getLeaf())) ? VerifyResult.MATCH : VerifyResult.NO_MATCH_CONTINUE;
            }
            @Override
            protected Iterable<? extends TreePath> prepareThis(TreePath tp) {
                ExpressionTree thisTree = info.getTreeUtilities().parseExpression("this", new SourcePositions[1]);

                return Collections.singleton(new TreePath(tp, thisTree));
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
            return new VariableAssignments(f.bindState);
        }

        return null;
    }

    public static boolean isDuplicate(CompilationInfo info, TreePath one, TreePath second, AtomicBoolean cancel) {
        return isDuplicate(info, one, second, true, cancel);
    }

    public static boolean isDuplicate(CompilationInfo info, TreePath one, TreePath second, boolean fullElementVerify, AtomicBoolean cancel) {
        return isDuplicate(info, one, second, fullElementVerify, null, false, cancel);
    }

    public static boolean isDuplicate(final CompilationInfo info, TreePath one, TreePath second, boolean fullElementVerify, HintContext inVariables, boolean fillInVariables, AtomicBoolean cancel) {
        if (one.getLeaf().getKind() != second.getLeaf().getKind()) {
            return false;
        }

        CopyFinder f =   fullElementVerify
                       ? new CopyFinder(one, info, cancel)
                       : new CopyFinder(one, info, cancel) {
            @Override
            protected VerifyResult verifyElements(TreePath node, TreePath p) {
                return getSimpleName(node.getLeaf()).contentEquals(getSimpleName(p.getLeaf())) ? VerifyResult.MATCH : VerifyResult.NO_MATCH_CONTINUE;
            }
            @Override
            protected Iterable<? extends TreePath> prepareThis(TreePath tp) {
                ExpressionTree thisTree = info.getTreeUtilities().parseExpression("this", new SourcePositions[1]);

                return Collections.singleton(new TreePath(tp, thisTree));
            }
        };

        if (inVariables != null) {
            if (fillInVariables) {
                f.bindState = State.from(inVariables.getVariables(), inVariables.getMultiVariables(), inVariables.getVariableNames());
            } else {
                f.bindState.variables.putAll(inVariables.getVariables());
                f.bindState.variables2Names.putAll(inVariables.getVariableNames());
                f.bindState.multiVariables.putAll(inVariables.getMultiVariables());
            }
        }

        f.allowGoDeeper = false;

        return f.scan(second, one);
    }

    //TODO: does not currently support variables:
    public static Collection<? extends MethodDuplicateDescription> computeDuplicatesAndRemap(CompilationInfo info, Collection<? extends TreePath> searchingFor, TreePath scope, Collection<VariableElement> variablesWithAllowedRemap, AtomicBoolean cancel) {
        TreePath first = searchingFor.iterator().next();
        List<MethodDuplicateDescription> result = new LinkedList<MethodDuplicateDescription>();

        CopyFinder firstStatementSearcher = new CopyFinder(first, info, cancel);

        firstStatementSearcher.designedTypeHack = Collections.<String, TypeMirror>emptyMap();
        firstStatementSearcher.variablesWithAllowedRemap = new HashSet<VariableElement>(variablesWithAllowedRemap);
        firstStatementSearcher.allowVariablesRemap = true;

        firstStatementSearcher.scan(scope, null);

        OUTER: for (Entry<TreePath, VariableAssignments> e : firstStatementSearcher.result.entrySet()) {
            TreePath firstOccurrence = e.getKey();
            List<? extends StatementTree> statements = getStatements(firstOccurrence);
            int occurrenceIndex = statements.indexOf(firstOccurrence.getLeaf());

            if (occurrenceIndex + searchingFor.size() > statements.size()) {
                continue;
            }

            int currentIndex = occurrenceIndex;
            Iterator<? extends TreePath> toProcess = searchingFor.iterator();
            Map<Element, Element> remapElements = new HashMap<Element, Element>(e.getValue().variablesRemapToElement);
            Map<Element, TreePath> remapTrees = new HashMap<Element, TreePath>(e.getValue().variablesRemapToTrees);

            toProcess.next();

            while (toProcess.hasNext()) {
                currentIndex++;

                TreePath currentToProcess = toProcess.next();
                CopyFinder ver = new CopyFinder(currentToProcess, info, cancel);

                ver.designedTypeHack = Collections.<String, TypeMirror>emptyMap();
                ver.allowGoDeeper = false;
                ver.variablesWithAllowedRemap = new HashSet<VariableElement>(variablesWithAllowedRemap);
                ver.bindState = State.from(remapElements, remapTrees);
                ver.allowVariablesRemap = true;

                if (!ver.scan(new TreePath(firstOccurrence.getParentPath(), statements.get(currentIndex)), currentToProcess)) {
                    continue OUTER;
                }
            }

            result.add(new MethodDuplicateDescription(firstOccurrence, occurrenceIndex, currentIndex, e.getValue().variablesRemapToElement, e.getValue().variablesRemapToTrees));
        }

        return result;
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
                if (bindState.variables2Names.containsKey(ident)) {
                    if (node.getKind() == Kind.IDENTIFIER)
                        return ((IdentifierTree) node).getName().toString().equals(bindState.variables2Names.get(ident));
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
                    TreePath original = bindState.variables.get(ident);

                    if (original == null) {
                        bindState.variables.put(ident, currentPath);
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

            //TODO: remap with qualified name?
            Element remappable = info.getTrees().getElement(p);

            if (variablesWithAllowedRemap.contains(remappable)) {
                TreePath existing = bindState.variablesRemapToTrees.get(remappable);

                if (existing != null) {
                    boolean oldAllowGoDeeper = allowGoDeeper;

                    try {
                        allowGoDeeper = false;
                        return superScan(node, existing);
                    } finally {
                        allowGoDeeper = oldAllowGoDeeper;
                   }
                }

                TreePath currPath = new TreePath(getCurrentPath(), node);
                TypeMirror currType = info.getTrees().getTypeMirror(currPath);
                TypeMirror pType = ((VariableElement) remappable).asType();

                if (isSameTypeForVariableRemap(currType, pType)) {
                    bindState.variablesRemapToTrees.put(remappable, currPath);
                    return true;
                }

                return false;
            }
        }

        if (p != null && Utilities.getWildcardTreeName(p.getLeaf()) != null) {
            String ident = Utilities.getWildcardTreeName(p.getLeaf()).toString();

            if (ident.startsWith("$") && StatementTree.class.isAssignableFrom(node.getKind().asInterface())) {
                TreePath original = bindState.variables.get(ident);

                if (original == null) {
                    TreePath currentPath = new TreePath(getCurrentPath(), node);

                    bindState.variables.put(ident, currentPath);
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
                    this.result.put(new TreePath(getCurrentPath(), node), new VariableAssignments(bindState));
                    bindState = State.empty();
                }

                return true;
            }
        }

        if (!allowGoDeeper)
            return false;

        if ((p != null && p.getLeaf() == searchingFor.getLeaf()) || !sameKind(node, searchingFor.getLeaf())) {
            if (    bindState.multiVariables.isEmpty()
                 || bindState.variables.isEmpty()
                 || bindState.variables2Names.isEmpty()
                 || bindState.variablesRemapToElement.isEmpty()
                 || bindState.variablesRemapToTrees.isEmpty()) {
                bindState = State.empty();
            }
            superScan(node, null);
            return false;
        } else {
            //maybe equivalent:
            allowGoDeeper = false;

            boolean result = superScan(node, searchingFor) == Boolean.TRUE;

            allowGoDeeper = true;

            if (result) {
                if (node != searchingFor.getLeaf()) {
                    this.result.put(new TreePath(getCurrentPath(), node), new VariableAssignments(bindState));
                    bindState = State.empty();
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
        Collection<? extends TreePath> original = this.bindState.multiVariables.get(name);

        if (original == null) {
            this.bindState.multiVariables.put(name, tps);
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
                State backup = State.copyOf(bindState);

                if (checkListsWithMultistatementTrees(real, realOffset, pattern, patternOffset + 1, p)) {
                    return validateMultiVariable(pattern.get(patternOffset), tps);
                }

                bindState = backup;

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

        switch (verifyElements(getCurrentPath(), p)) {
            case MATCH_CHECK_DEEPER:
                if (node.getKind() == p.getLeaf().getKind()) {
                    return true;
                }

                for (TreePath thisPath : prepareThis(getCurrentPath())) {
                    State origState = State.copyOf(bindState);
                    try {
                        MemberSelectTree t = (MemberSelectTree) p.getLeaf();

                        if (scan(thisPath.getLeaf(), t.getExpression(), p) == Boolean.TRUE) {
                            return true;
                        }
                    } finally {
                        bindState = origState;
                    }
                }

                return false;
            case MATCH:
                return true;
            default:
            case NO_MATCH:
            case NO_MATCH_CONTINUE:
                return false;
        }
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
            switch (verifyElements(getCurrentPath(), p)) {
                case MATCH_CHECK_DEEPER:
                    if (node.getKind() == p.getLeaf().getKind()) {
                        //to bind any free variables inside:
                        MemberSelectTree t = (MemberSelectTree) p.getLeaf();

                        return scan(node.getExpression(), t.getExpression(), p) == Boolean.TRUE;
                    } else {
                        //TODO: what to do here?
                        return true;
                    }
                case MATCH:
                    return true;
                case NO_MATCH:
                    return false;
            }
        }

        if (node.getKind() != p.getLeaf().getKind()) {
            return false;
        }

        MemberSelectTree t = (MemberSelectTree) p.getLeaf();

        if (!scan(node.getExpression(), t.getExpression(), p))
            return false;

        String ident = t.getIdentifier().toString();

        if (ident.startsWith("$")) { //XXX: there should be a utility method for this check
            if (bindState.variables2Names.containsKey(ident)) {
                return node.getIdentifier().contentEquals(bindState.variables2Names.get(ident));
            } else {
                bindState.variables2Names.put(ident, node.getIdentifier().toString());
            }
            return true;
        }

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
            String existingName = bindState.variables2Names.get(name);
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
                bindState.variables.put(name, getCurrentPath());
                bindState.variables2Names.put(name, currentName);
            }
        }

        if (allowVariablesRemap) {
            VariableElement nodeEl = (VariableElement) info.getTrees().getElement(getCurrentPath());
            VariableElement pEl = (VariableElement) info.getTrees().getElement(p);

            if (nodeEl != null && pEl != null && isSameTypeForVariableRemap(nodeEl.asType(), pEl.asType())) {
                bindState.variablesRemapToElement.put(pEl, nodeEl);
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


    private enum VerifyResult {
        MATCH_CHECK_DEEPER,
        MATCH,
        NO_MATCH_CONTINUE,
        NO_MATCH;
    }
    
    protected @NonNull VerifyResult verifyElements(TreePath node, TreePath p) {
        Element nodeEl = info.getTrees().getElement(node);
        Element pEl    = info.getTrees().getElement(p);

        if (nodeEl == null) {
            return pEl == null ? VerifyResult.MATCH : VerifyResult.NO_MATCH_CONTINUE; //TODO: correct? shouldn't be MATCH_CHECK_DEEPER?
        }

        VerifyResult matchingResult;
        
        if (!nodeEl.getModifiers().contains(Modifier.STATIC)) {
            if (nodeEl.getKind().isClass() && info.getElementUtilities().enclosingTypeElement(nodeEl) == null) {
                //top-level class:
                matchingResult = VerifyResult.MATCH;
            } else {
                matchingResult = VerifyResult.MATCH_CHECK_DEEPER;
            }
        } else {
            matchingResult = VerifyResult.MATCH;
        }

        if (nodeEl == pEl) {
            return matchingResult;
        }

        if (nodeEl == null || pEl == null)
            return VerifyResult.NO_MATCH;

        if (nodeEl.getKind() == pEl.getKind() && nodeEl.getKind() == ElementKind.METHOD) {
            if (info.getElements().overrides((ExecutableElement) nodeEl, (ExecutableElement) pEl, (TypeElement) nodeEl.getEnclosingElement())) {
                return VerifyResult.MATCH_CHECK_DEEPER;
            }
        }

        if (nodeEl.equals(pEl)) {
            return matchingResult;
        }

        if (allowVariablesRemap && nodeEl.equals(bindState.variablesRemapToElement.get(pEl))) {
            return matchingResult;
        }

        TypeMirror nodeTM = info.getTrees().getTypeMirror(node);

        if (nodeTM == null || nodeTM.getKind() == TypeKind.ERROR) {
            return VerifyResult.NO_MATCH_CONTINUE;
        }

        TypeMirror pTM = info.getTrees().getTypeMirror(p);

        if (pTM == null || pTM.getKind() == TypeKind.ERROR) {
            return VerifyResult.NO_MATCH_CONTINUE;
        }

        return VerifyResult.NO_MATCH;
    }

    protected Iterable<? extends TreePath> prepareThis(TreePath tp) {
        //XXX: is there a faster way to do this?
        Collection<TreePath> result = new LinkedList<TreePath>();
        Scope scope = info.getTrees().getScope(tp);
        TypeElement lastClass = null;

        while (scope != null && scope.getEnclosingClass() != null) {
            if (lastClass != scope.getEnclosingClass()) {
                ExpressionTree thisTree = info.getTreeUtilities().parseExpression("this", new SourcePositions[1]);

                info.getTreeUtilities().attributeTree(thisTree, scope);

                result.add(new TreePath(tp, thisTree));
            }
            
            scope = scope.getEnclosingScope();
        }

        return result;
    }

    private boolean isSameTypeForVariableRemap(TypeMirror nodeType, TypeMirror pType) {
        //TODO: subtypes could be OK for remap?
        return info.getTypes().isSameType(nodeType, pType);
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

    public static List<? extends StatementTree> getStatements(TreePath firstLeaf) {
        switch (firstLeaf.getParentPath().getLeaf().getKind()) {
            case BLOCK:
                return ((BlockTree) firstLeaf.getParentPath().getLeaf()).getStatements();
            case CASE:
                return ((CaseTree) firstLeaf.getParentPath().getLeaf()).getStatements();
            default:
                return Collections.singletonList((StatementTree) firstLeaf.getLeaf());
        }
    }

    public static final class VariableAssignments {
        public final Map<String, TreePath> variables;
        public final Map<String, Collection<? extends TreePath>> multiVariables;
        public final Map<String, String> variables2Names;
               final Map<Element, Element> variablesRemapToElement;
               final Map<Element, TreePath> variablesRemapToTrees;

        public VariableAssignments(Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variables2Names) {
            this.variables = variables;
            this.multiVariables = multiVariables;
            this.variables2Names = variables2Names;
            this.variablesRemapToElement = null;
            this.variablesRemapToTrees = null;
        }

        VariableAssignments(State state) {
            this.variables = state.variables;
            this.multiVariables = state.multiVariables;
            this.variables2Names = state.variables2Names;
            this.variablesRemapToElement = state.variablesRemapToElement;
            this.variablesRemapToTrees = state.variablesRemapToTrees;
        }
    }

    public static final class MethodDuplicateDescription {
        public final TreePath firstLeaf;
        public final int dupeStart;
        public final int dupeEnd;
        public final Map<Element, Element> variablesRemapToElement;
        public final Map<Element, TreePath> variablesRemapToTrees;
        public MethodDuplicateDescription(TreePath firstLeaf, int dupeStart, int dupeEnd, Map<Element, Element> variablesRemapToElement, Map<Element, TreePath> variablesRemapToTrees) {
            this.firstLeaf = firstLeaf;
            this.dupeStart = dupeStart;
            this.dupeEnd = dupeEnd;
            this.variablesRemapToElement = variablesRemapToElement;
            this.variablesRemapToTrees = variablesRemapToTrees;
        }
    }

    private static final class State {
        final Map<String, TreePath> variables;
        final Map<String, Collection<? extends TreePath>> multiVariables;
        final Map<String, String> variables2Names;
        final Map<Element, Element> variablesRemapToElement;
        final Map<Element, TreePath> variablesRemapToTrees;

        private State(Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variables2Names, Map<Element, Element> variablesRemapToElement, Map<Element, TreePath> variablesRemapToTrees) {
            this.variables = variables;
            this.multiVariables = multiVariables;
            this.variables2Names = variables2Names;
            this.variablesRemapToElement = variablesRemapToElement;
            this.variablesRemapToTrees = variablesRemapToTrees;
        }
        public static State empty() {
            return new State(new HashMap<String, TreePath>(), new HashMap<String, Collection<? extends TreePath>>(), new HashMap<String, String>(), new HashMap<Element, Element>(), new HashMap<Element, TreePath>());
        }

        public static State copyOf(State original) {
            return new State(new HashMap<String, TreePath>(original.variables), new HashMap<String, Collection<? extends TreePath>>(original.multiVariables), new HashMap<String, String>(original.variables2Names), new HashMap<Element, Element>(original.variablesRemapToElement), new HashMap<Element, TreePath>(original.variablesRemapToTrees));
        }

        public static State from(Map<Element, Element> variablesRemapToElement, Map<Element, TreePath> variablesRemapToTrees) {
            return new State(new HashMap<String, TreePath>(), new HashMap<String, Collection<? extends TreePath>>(), new HashMap<String, String>(), variablesRemapToElement, variablesRemapToTrees);
        }

        public static State from(Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variables2Names) {
            return new State(variables, multiVariables, variables2Names, null, null);
        }
    }
}
