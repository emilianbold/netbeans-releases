/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
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
import javax.swing.JButton;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.Union2;

/**
 * Refactored from IntroduceFix originally by lahvac
 *
 * @author sdedic
 */
public final class IntroduceMethodFix extends IntroduceFixBase implements Fix {

    static Fix computeIntroduceMethod(CompilationInfo info, int start, int end, Map<IntroduceKind, String> errorMessage, AtomicBoolean cancel) {
        int[] statements = new int[2];
        TreePathHandle h = validateSelectionForIntroduceMethod(info, start, end, statements);
        if (h == null) {
            errorMessage.put(IntroduceKind.CREATE_METHOD, "ERR_Invalid_Selection");
            return null;
        }
        TreePath block = h.resolve(info);
        TreePath method = TreeUtils.findMethod(block);
        if (method == null) {
            errorMessage.put(IntroduceKind.CREATE_METHOD, "ERR_Invalid_Selection");
            return null;
        }
        if (method.getLeaf().getKind() == Tree.Kind.METHOD && ((MethodTree) method.getLeaf()).getParameters().contains(block.getLeaf())) {
            errorMessage.put(IntroduceKind.CREATE_METHOD, "ERR_Invalid_Selection");
            return null;
        }
        Map<TypeMirror, TreePathHandle> typeVar2Def = new HashMap<TypeMirror, TreePathHandle>();
        List<TreePathHandle> typeVars = new LinkedList<TreePathHandle>();
        IntroduceHint.prepareTypeVars(method, info, typeVar2Def, typeVars);
        Element methodEl = info.getTrees().getElement(method);
        List<? extends StatementTree> parentStatements = IntroduceHint.getStatements(block);
        List<? extends StatementTree> statementsToWrap = parentStatements.subList(statements[0], statements[1] + 1);
        Flow.FlowResult flow = Flow.assignmentsForUse(info, method, cancel);
        if (flow == null || cancel.get()) {
            return null;
        }
        Map<Tree, Iterable<? extends TreePath>> assignmentsForUse = flow.getAssignmentsForUse();
        ScanStatement scanner = new ScanStatement(info, statementsToWrap.get(0), statementsToWrap.get(statementsToWrap.size() - 1), typeVar2Def, assignmentsForUse, cancel);
        Set<TypeMirror> exceptions = new HashSet<TypeMirror>();
        int index = 0;
        TypeMirror methodReturnType = info.getTypes().getNoType(TypeKind.VOID);
        if (methodEl != null && (methodEl.getKind() == ElementKind.METHOD || methodEl.getKind() == ElementKind.CONSTRUCTOR)) {
            ExecutableElement ee = (ExecutableElement) methodEl;
            scanner.localVariables.addAll(ee.getParameters());
            methodReturnType = ee.getReturnType();
        }
        scanner.scan(method, null);
        List<TreePath> pathsOfStatementsToWrap = new LinkedList<TreePath>();
        for (StatementTree s : parentStatements) {
            TreePath path = new TreePath(block, s);
            if (index >= statements[0] && index <= statements[1]) {
                exceptions.addAll(info.getTreeUtilities().getUncaughtExceptions(path));
                pathsOfStatementsToWrap.add(path);
            }
            index++;
        }
        boolean exitsFromAllBranches = Utilities.exitsFromAllBranchers(info, new TreePath(block, statementsToWrap.get(statementsToWrap.size() - 1)));
        String exitsError = scanner.verifyExits(exitsFromAllBranches);
        if (exitsError != null) {
            errorMessage.put(IntroduceKind.CREATE_METHOD, exitsError);
            return null;
        }
        Map<VariableElement, Boolean> mergedVariableUse = new LinkedHashMap<VariableElement, Boolean>(scanner.usedLocalVariables);
        for (Map.Entry<VariableElement, Boolean> e : scanner.usedAfterSelection.entrySet()) {
            if (cancel.get()) {
                return null;
            }
            Boolean usedLocal = mergedVariableUse.get(e.getKey());
            if (usedLocal == null && Flow.definitellyAssigned(info, e.getKey(), pathsOfStatementsToWrap, cancel)) {
                mergedVariableUse.put(e.getKey(), true);
            } else {
                mergedVariableUse.put(e.getKey(), !(usedLocal == Boolean.FALSE) && e.getValue());
            }
        }
        if (cancel.get()) {
            return null;
        }
        Set<VariableElement> additionalLocalVariables = new LinkedHashSet<VariableElement>();
        Set<VariableElement> paramsVariables = new LinkedHashSet<VariableElement>();
        for (Map.Entry<VariableElement, Boolean> e : mergedVariableUse.entrySet()) {
            if (e.getValue() == null || e.getValue()) {
                additionalLocalVariables.add(e.getKey());
            } else {
                paramsVariables.add(e.getKey());
                additionalLocalVariables.remove(e.getKey());
            }
        }
        List<TreePathHandle> params = new LinkedList<TreePathHandle>();
        for (VariableElement ve : paramsVariables) {
            params.add(TreePathHandle.create(info.getTrees().getPath(ve), info));
        }
        additionalLocalVariables.removeAll(paramsVariables);
        additionalLocalVariables.removeAll(scanner.selectionLocalVariables);
        List<TypeMirrorHandle> additionaLocalTypes = new LinkedList<TypeMirrorHandle>();
        List<String> additionaLocalNames = new LinkedList<String>();
        for (VariableElement ve : additionalLocalVariables) {
            additionaLocalTypes.add(TypeMirrorHandle.create(ve.asType()));
            additionaLocalNames.add(ve.getSimpleName().toString());
        }
        List<TreePathHandle> exits = null;
        Tree lastStatement = statementsToWrap.get(statementsToWrap.size() - 1);
        if (parentStatements.get(parentStatements.size() - 1) == lastStatement) {
            TreePath search = block.getParentPath();
            Tree last = block.getLeaf();
            OUTTER:
            while (search != null) {
                switch (search.getLeaf().getKind()) {
                    case BLOCK:
                        List<? extends StatementTree> thisBlockStatements = ((BlockTree) search.getLeaf()).getStatements();
                        if (thisBlockStatements.get(thisBlockStatements.size() - 1) == last) {
                            break;
                        } else {
                            break OUTTER;
                        }
                    case IF:
                        break;
                    case METHOD:
                        Tree returnType = ((MethodTree) search.getLeaf()).getReturnType();
                        if (returnType == null || (returnType.getKind() == Tree.Kind.PRIMITIVE_TYPE && ((PrimitiveTypeTree) returnType).getPrimitiveTypeKind() == TypeKind.VOID)) {
                            exits = Collections.emptyList();
                        }
                        break OUTTER;
                    default:
                        break OUTTER;
                }
                last = search.getLeaf();
                search = search.getParentPath();
            }
        }
        if (exits == null) {
            exits = new LinkedList<TreePathHandle>();
            for (TreePath tp : scanner.selectionExits) {
                if (isInsideSameClass(tp, method)) {
                    exits.add(TreePathHandle.create(tp, info));
                }
            }
        }
        TypeMirror returnType;
        TreePathHandle returnAssignTo;
        boolean declareVariableForReturnValue;
        Pattern p = Pattern.createPatternWithRemappableVariables(pathsOfStatementsToWrap, scanner.usedLocalVariables.keySet(), true);
        Collection<? extends Occurrence> duplicates = Matcher.create(info).setCancel(cancel).match(p);
        int duplicatesCount = duplicates.size();
        if (!scanner.usedAfterSelection.isEmpty()) {
            VariableElement result = scanner.usedAfterSelection.keySet().iterator().next();
            returnType = result.asType();
            returnAssignTo = TreePathHandle.create(info.getTrees().getPath(result), info);
            declareVariableForReturnValue = scanner.selectionLocalVariables.contains(result);
        } else {
            if (!exits.isEmpty() && !exitsFromAllBranches) {
                returnType = info.getTypes().getPrimitiveType(TypeKind.BOOLEAN);
                returnAssignTo = null;
                declareVariableForReturnValue = false;
            } else {
                if (exitsFromAllBranches && scanner.hasReturns) {
                    returnType = methodReturnType;
                    returnAssignTo = null;
                    declareVariableForReturnValue = false;
                } else {
                    returnType = info.getTypes().getNoType(TypeKind.VOID);
                    returnAssignTo = null;
                    declareVariableForReturnValue = false;
                }
            }
        }
        Set<TypeMirrorHandle> exceptionHandles = new HashSet<TypeMirrorHandle>();
        for (TypeMirror tm : exceptions) {
            exceptionHandles.add(TypeMirrorHandle.create(tm));
        }
        typeVars.retainAll(scanner.usedTypeVariables);
        List<TargetDescription> targets = IntroduceExpressionBasedMethodFix.computeViableTargets(info, block, statementsToWrap, duplicates, cancel);
        return new IntroduceMethodFix(info.getJavaSource(), h, params, additionaLocalTypes, additionaLocalNames, TypeMirrorHandle.create(returnType), returnAssignTo, declareVariableForReturnValue, exceptionHandles, exits, exitsFromAllBranches, statements[0], statements[1], duplicatesCount, typeVars, end, targets);
    }

    static boolean isInsideSameClass(TreePath one, TreePath two) {
        ClassTree oneClass = null;
        ClassTree twoClass = null;
        while (one.getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT && one.getLeaf().getKind() != null) {
            Tree t = one.getLeaf();
            if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                oneClass = (ClassTree) t;
                break;
            }
            one = one.getParentPath();
        }
        while (two.getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT && two.getLeaf().getKind() != null) {
            Tree t = two.getLeaf();
            if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                twoClass = (ClassTree) t;
                break;
            }
            two = two.getParentPath();
        }
        if (oneClass != null && oneClass.equals(twoClass)) {
            return true;
        }
        return false;
    }

    /**
     * Checks that the selection contains entire statements. First it tries to extend from the selection range up the
     * tree to find the exact matching (single) statement. If that does not work, the block that contains the selection
     * is searched to find the first and last statements fully covered by the selection. The first statement's TreePath is
     * returned; the first and last statement indexes within their parent block are returned in statementsSpan array
     * @param ci context
     * @param start start of selection 
     * @param end end of selection
     * @param statementsSpan out; indexes of first and last statement within their parent Block or Case.
     * @return TreePath for the first statement for valid selections, {@code null} otherwise
     */
    public static TreePathHandle validateSelectionForIntroduceMethod(CompilationInfo ci, int start, int end, int[] statementsSpan) {
        int[] span = TreeUtils.ignoreWhitespaces(ci, Math.min(start, end), Math.max(start, end));
        start = span[0];
        end = span[1];
        if (start >= end) {
            return null;
        }
        TreePath tp = ci.getTreeUtilities().pathFor((start + end) / 2 + 1);
        // finds and returns a TreePath to a statement, which precisely matches the non-whitespace area in the selection
        for (; tp != null; tp = tp.getParentPath()) {
            Tree leaf = tp.getLeaf();
            if (!StatementTree.class.isAssignableFrom(leaf.getKind().asInterface())) {
                continue;
            }
            long treeStart = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), leaf);
            long treeEnd = ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), leaf);
            if (treeStart != start || treeEnd != end) {
                continue;
            }
            List<? extends StatementTree> statements = IntroduceHint.getStatements(tp);
            statementsSpan[0] = statements.indexOf(tp.getLeaf());
            statementsSpan[1] = statementsSpan[0];
            return TreePathHandle.create(tp, ci);
        }
        TreePath tpStart = ci.getTreeUtilities().pathFor(start);
        TreePath tpEnd = ci.getTreeUtilities().pathFor(end);
        if (tpStart.getLeaf() != tpEnd.getLeaf() || (tpStart.getLeaf().getKind() != Tree.Kind.BLOCK && tpStart.getLeaf().getKind() != Tree.Kind.CASE)) {
            //??? not in the same block:
            return null;
        }
        int from = -1;
        int to = -1;
        List<? extends StatementTree> statements = tpStart.getLeaf().getKind() == Tree.Kind.BLOCK ? ((BlockTree) tpStart.getLeaf()).getStatements() : ((CaseTree) tpStart.getLeaf()).getStatements();
        int index = 0;
        for (StatementTree s : statements) {
            long sStart = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), s);
            if (sStart == start && from == (-1)) {
                from = index;
            }
            if (end < sStart && to == (-1)) {
                to = index - 1;
            }
            index++;
        }
        if (from == (-1)) {
            return null;
        }
        if (to == (-1)) {
            to = statements.size() - 1;
        }
        if (to < from) {
            return null;
        }
        statementsSpan[0] = from;
        statementsSpan[1] = to;
        return TreePathHandle.create(new TreePath(tpStart, statements.get(from)), ci);
    }
    private final List<TreePathHandle> parameters;
    private final List<TypeMirrorHandle> additionalLocalTypes;
    private final List<String> additionalLocalNames;
    private final TypeMirrorHandle returnType;
    private final TreePathHandle returnAssignTo;
    private final boolean declareVariableForReturnValue;
    private final Set<TypeMirrorHandle> thrownTypes;
    private final List<TreePathHandle> exists;
    private final boolean exitsFromAllBranches;
    private final int from;
    private final int to;
    private final List<TreePathHandle> typeVars;
    private final List<TargetDescription> targets;

    public IntroduceMethodFix(JavaSource js, TreePathHandle parentBlock, List<TreePathHandle> parameters, List<TypeMirrorHandle> additionalLocalTypes, List<String> additionalLocalNames, TypeMirrorHandle returnType, TreePathHandle returnAssignTo, boolean declareVariableForReturnValue, Set<TypeMirrorHandle> thrownTypes, List<TreePathHandle> exists, boolean exitsFromAllBranches, int from, int to, int duplicatesCount, List<TreePathHandle> typeVars, int offset, List<TargetDescription> targets) {
        super(js, parentBlock, duplicatesCount, offset);
        this.parameters = parameters;
        this.additionalLocalTypes = additionalLocalTypes;
        this.additionalLocalNames = additionalLocalNames;
        this.returnType = returnType;
        this.returnAssignTo = returnAssignTo;
        this.declareVariableForReturnValue = declareVariableForReturnValue;
        this.thrownTypes = thrownTypes;
        this.exists = exists;
        this.exitsFromAllBranches = exitsFromAllBranches;
        this.from = from;
        this.to = to;
        this.typeVars = typeVars;
        this.targets = targets;
    }

    public String getText() {
        return NbBundle.getMessage(IntroduceHint.class, "FIX_IntroduceMethod");
    }

    public String toDebugString(CompilationInfo info) {
        return "[IntroduceMethod:" + from + ":" + to + "]"; // NOI18N
    }

    public ChangeInfo implement() throws Exception {
        JButton btnOk = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Ok"));
        JButton btnCancel = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Cancel"));
        IntroduceMethodPanel panel = new IntroduceMethodPanel("", duplicatesCount, targets); //NOI18N
        panel.setOkButton(btnOk);
        String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_IntroduceMethod");
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
        if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
            return null; //cancel
        }
        final String name = panel.getMethodName();
        final Set<Modifier> access = panel.getAccess();
        final boolean replaceOther = panel.getReplaceOther();
        final TargetDescription target = panel.getSelectedTarget();
        js.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                TreePath firstStatement = handle.resolve(copy);
                TypeMirror returnType = IntroduceMethodFix.this.returnType.resolve(copy);
                if (firstStatement == null || returnType == null) {
                    return; //TODO...
                }
                GeneratorUtilities.get(copy).importComments(firstStatement.getParentPath().getLeaf(), copy.getCompilationUnit());
                List<? extends StatementTree> statements = IntroduceHint.getStatements(firstStatement);
                List<StatementTree> nueStatements = new LinkedList<StatementTree>();
                nueStatements.addAll(statements.subList(0, from));
                final TreeMaker make = copy.getTreeMaker();
                List<VariableElement> parameters = IntroduceHint.resolveVariables(copy, IntroduceMethodFix.this.parameters);
                List<ExpressionTree> realArguments = IntroduceHint.realArguments(make, parameters);
                List<StatementTree> methodStatements = new LinkedList<StatementTree>();
                Iterator<TypeMirrorHandle> additionalType = additionalLocalTypes.iterator();
                Iterator<String> additionalName = additionalLocalNames.iterator();
                while (additionalType.hasNext() && additionalName.hasNext()) {
                    TypeMirror tm = additionalType.next().resolve(copy);
                    if (tm == null) {
                        //XXX:
                        return;
                    }
                    Tree type = make.Type(tm);
                    methodStatements.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), additionalName.next(), type, null));
                }
                if (from == to && statements.get(from).getKind() == Tree.Kind.BLOCK) {
                    methodStatements.addAll(((BlockTree) statements.get(from)).getStatements());
                } else {
                    methodStatements.addAll(statements.subList(from, to + 1));
                }
                Tree returnTypeTree = make.Type(returnType);
                ExpressionTree invocation = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier(name), realArguments);
                boolean alreadyInvoked = false;
                Callable<ReturnTree> ret = null;
                final VariableElement returnAssignTo;
                if (IntroduceMethodFix.this.returnAssignTo != null) {
                    returnAssignTo = (VariableElement) IntroduceMethodFix.this.returnAssignTo.resolveElement(copy);
                    if (returnAssignTo == null) {
                        return; //TODO...
                    }
                } else {
                    returnAssignTo = null;
                }
                if (returnAssignTo != null) {
                    ret = new Callable<ReturnTree>() {
                        @Override
                        public ReturnTree call() throws Exception {
                            return make.Return(make.Identifier(returnAssignTo.getSimpleName()));
                        }
                    };
                    if (declareVariableForReturnValue) {
                        nueStatements.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), returnAssignTo.getSimpleName(), returnTypeTree, invocation));
                        alreadyInvoked = true;
                    } else {
                        invocation = make.Assignment(make.Identifier(returnAssignTo.getSimpleName()), invocation);
                    }
                }
                if (!exists.isEmpty()) {
                    TreePath handle = null;
                    handle = exists.iterator().next().resolve(copy);
                    if (handle == null) {
                        return; //TODO...
                    }
                    assert handle != null;
                    if (exitsFromAllBranches && handle.getLeaf().getKind() == Tree.Kind.RETURN && returnAssignTo == null && returnType.getKind() != TypeKind.VOID) {
                        nueStatements.add(make.Return(invocation));
                    } else {
                        if (ret == null) {
                            ret = new Callable<ReturnTree>() {
                                @Override
                                public ReturnTree call() throws Exception {
                                    if (exitsFromAllBranches) {
                                        return make.Return(null);
                                    } else {
                                        return make.Return(make.Literal(true));
                                    }
                                }
                            };
                        }
                        for (TreePathHandle h : exists) {
                            TreePath resolved = h.resolve(copy);
                            if (resolved == null) {
                                return; //TODO...
                            }
                            ReturnTree r = ret.call();
                            GeneratorUtilities.get(copy).copyComments(resolved.getLeaf(), r, false);
                            GeneratorUtilities.get(copy).copyComments(resolved.getLeaf(), r, true);
                            copy.rewrite(resolved.getLeaf(), r);
                        }
                        StatementTree branch = null;
                        switch (handle.getLeaf().getKind()) {
                            case BREAK:
                                branch = make.Break(((BreakTree) handle.getLeaf()).getLabel());
                                break;
                            case CONTINUE:
                                branch = make.Continue(((ContinueTree) handle.getLeaf()).getLabel());
                                break;
                            case RETURN:
                                branch = make.Return(((ReturnTree) handle.getLeaf()).getExpression());
                                break;
                        }
                        if (returnAssignTo != null || exitsFromAllBranches) {
                            nueStatements.add(make.ExpressionStatement(invocation));
                            nueStatements.add(branch);
                        } else {
                            nueStatements.add(make.If(make.Parenthesized(invocation), branch, null));
                            methodStatements.add(make.Return(make.Literal(false)));
                        }
                    }
                    alreadyInvoked = true;
                } else {
                    if (ret != null) {
                        methodStatements.add(ret.call());
                    }
                }
                if (!alreadyInvoked) {
                    nueStatements.add(make.ExpressionStatement(invocation));
                }
                nueStatements.addAll(statements.subList(to + 1, statements.size()));
                Map<Tree, Tree> rewritten = new IdentityHashMap<Tree, Tree>();
                IntroduceHint.doReplaceInBlockCatchSingleStatement(copy, rewritten, firstStatement, nueStatements);
                TypeElement targetType = target.type.resolve(copy);
                TreePath pathToClass = targetType != null ? copy.getTrees().getPath(targetType) : null;
                if (pathToClass == null) {
                    pathToClass = TreeUtils.findClass(firstStatement);
                }
                assert pathToClass != null;
                boolean isStatic = IntroduceHint.needsStaticRelativeTo(copy, pathToClass, firstStatement);
                if (replaceOther) {
                    //handle duplicates
                    Document doc = copy.getDocument();
                    List<TreePath> statementsPaths = new LinkedList<TreePath>();
                    for (StatementTree t : statements.subList(from, to + 1)) {
                        statementsPaths.add(new TreePath(firstStatement.getParentPath(), t));
                    }
                    Pattern p = Pattern.createPatternWithRemappableVariables(statementsPaths, parameters, true);
                    for (Occurrence desc : Matcher.create(copy).setCancel(new AtomicBoolean()).match(p)) {
                        TreePath firstLeaf = desc.getOccurrenceRoot();
                        List<? extends StatementTree> parentStatements = IntroduceHint.getStatements(new TreePath(new TreePath(firstLeaf.getParentPath().getParentPath(), IntroduceHint.resolveRewritten(rewritten, firstLeaf.getParentPath().getLeaf())), firstLeaf.getLeaf()));
                        int dupeStart = parentStatements.indexOf(firstLeaf.getLeaf());
                        int startOff = (int) copy.getTrees().getSourcePositions().getStartPosition(copy.getCompilationUnit(), parentStatements.get(dupeStart));
                        int endOff = (int) copy.getTrees().getSourcePositions().getEndPosition(copy.getCompilationUnit(), parentStatements.get(dupeStart + statementsPaths.size() - 1));
                        if (!IntroduceHint.shouldReplaceDuplicate(doc, startOff, endOff)) {
                            continue;
                        }
                        List<StatementTree> newStatements = new LinkedList<StatementTree>();
                        newStatements.addAll(parentStatements.subList(0, dupeStart));
                        //XXX:
                        List<Union2<VariableElement, TreePath>> dupeParameters = new LinkedList<Union2<VariableElement, TreePath>>();
                        for (VariableElement ve : parameters) {
                            if (desc.getVariablesRemapToTrees().containsKey(ve)) {
                                dupeParameters.add(Union2.<VariableElement, TreePath>createSecond(desc.getVariablesRemapToTrees().get(ve)));
                            } else {
                                dupeParameters.add(Union2.<VariableElement, TreePath>createFirst(ve));
                            }
                        }
                        List<ExpressionTree> dupeRealArguments = IntroduceHint.realArgumentsForTrees(make, dupeParameters);
                        ExpressionTree dupeInvocation = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier(name), dupeRealArguments);
                        if (returnAssignTo != null) {
                            TreePath remappedTree = desc.getVariablesRemapToTrees().containsKey(returnAssignTo) ? desc.getVariablesRemapToTrees().get(returnAssignTo) : null;
                            VariableElement remappedElement = desc.getVariablesRemapToElement().containsKey(returnAssignTo) ? (VariableElement) desc.getVariablesRemapToElement().get(returnAssignTo) : null;
                            //                                VariableElement dupeReturnAssignTo = mdd.variablesRemapToTrees.containsKey(returnAssignTo) ? (VariableElement) mdd.variablesRemapToTrees.get(returnAssignTo) : returnAssignTo;
                            if (declareVariableForReturnValue) {
                                assert remappedElement != null || remappedTree == null;
                                Name name = remappedElement != null ? remappedElement.getSimpleName() : returnAssignTo.getSimpleName();
                                newStatements.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, returnTypeTree, invocation));
                                dupeInvocation = null;
                            } else {
                                ExpressionTree sel = remappedTree != null ? (ExpressionTree) remappedTree.getLeaf() : remappedElement != null ? make.Identifier(remappedElement.getSimpleName()) : make.Identifier(returnAssignTo.getSimpleName());
                                dupeInvocation = make.Assignment(sel, dupeInvocation);
                            }
                        }
                        if (dupeInvocation != null) {
                            newStatements.add(make.ExpressionStatement(dupeInvocation));
                        }
                        newStatements.addAll(parentStatements.subList(dupeStart + statementsPaths.size(), parentStatements.size()));
                        IntroduceHint.doReplaceInBlockCatchSingleStatement(copy, rewritten, firstLeaf, newStatements);
                        isStatic |= IntroduceHint.needsStaticRelativeTo(copy, pathToClass, firstLeaf);
                    }
                    IntroduceHint.introduceBag(doc).clear();
                    //handle duplicates end
                }
                Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
                if (isStatic) {
                    modifiers.add(Modifier.STATIC);
                }
                modifiers.addAll(access);
                ModifiersTree mods = make.Modifiers(modifiers);
                List<VariableTree> formalArguments = IntroduceHint.createVariables(copy, parameters);
                if (formalArguments == null) {
                    return; //XXX
                }
                List<ExpressionTree> thrown = IntroduceHint.typeHandleToTree(copy, thrownTypes);
                if (thrownTypes == null) {
                    return; //XXX
                }
                List<TypeParameterTree> typeVars = new LinkedList<TypeParameterTree>();
                for (TreePathHandle tph : IntroduceMethodFix.this.typeVars) {
                    typeVars.add((TypeParameterTree) tph.resolve(copy).getLeaf());
                }
                MethodTree method = make.Method(mods, name, returnTypeTree, typeVars, formalArguments, thrown, make.Block(methodStatements, false), null);
                ClassTree nueClass = IntroduceHint.INSERT_CLASS_MEMBER.insertClassMember(copy, (ClassTree) pathToClass.getLeaf(), method, offset);
                copy.rewrite(pathToClass.getLeaf(), nueClass);
            }
        }).commit();
        return null;
    }
    
}
