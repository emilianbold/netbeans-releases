/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.hints.introduce;

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
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.java.hints.HintContext;

/**
 *
 * @author lahvac
 */
public class Flow {

    public static FlowResult assignmentsForUse(CompilationInfo info, AtomicBoolean cancel) {
        return assignmentsForUse(info, new AtomicBooleanCancel(cancel));
    }

    public static FlowResult assignmentsForUse(CompilationInfo info, TreePath from, AtomicBoolean cancel) {
        return assignmentsForUse(info, from, new AtomicBooleanCancel(cancel));
    }

    public static FlowResult assignmentsForUse(final HintContext ctx) {
        return Flow.assignmentsForUse(ctx.getInfo(), new Cancel() {
            @Override
            public boolean isCanceled() {
                return ctx.isCanceled();
            }
        });
    }
    
    private static final Object KEY_FLOW = new Object();
    
    public static FlowResult assignmentsForUse(CompilationInfo info, Cancel cancel) {
        FlowResult result = (FlowResult) info.getCachedValue(KEY_FLOW);
        
        if (result == null) {
            result = assignmentsForUse(info, new TreePath(info.getCompilationUnit()), cancel);
            
            if (result != null && !cancel.isCanceled()) {
                info.putCachedValue(KEY_FLOW, result, CacheClearPolicy.ON_TASK_END);
            }
        }
        
        return result;
    }
    
    public static FlowResult assignmentsForUse(CompilationInfo info, TreePath from, Cancel cancel) {
        Map<Tree, Iterable<? extends TreePath>> result = new HashMap<Tree, Iterable<? extends TreePath>>();
        VisitorImpl v = new VisitorImpl(info, cancel);

        v.scan(from, null);

        if (cancel.isCanceled()) return null;

        for (Entry<Tree, State> e : v.use2Values.entrySet()) {
            result.put(e.getKey(), e.getValue() != null ? e.getValue().assignments : Collections.<TreePath>emptyList());
        }

        v.deadBranches.remove(null);
        
        Set<VariableElement> finalCandidates = v.finalCandidates;
        
        finalCandidates.removeAll(v.usedWhileUndefined);

        return new FlowResult(Collections.unmodifiableMap(result), Collections.unmodifiableSet(v.deadBranches), Collections.unmodifiableSet(finalCandidates));
    }

    public static final class FlowResult {
        private final Map<Tree, Iterable<? extends TreePath>> assignmentsForUse;
        private final Set<? extends Tree> deadBranches;
        private final Set<VariableElement> finalCandidates;
        private FlowResult(Map<Tree, Iterable<? extends TreePath>> assignmentsForUse, Set<Tree> deadBranches, Set<VariableElement> finalCandidates) {
            this.assignmentsForUse = assignmentsForUse;
            this.deadBranches = deadBranches;
            this.finalCandidates = finalCandidates;
        }
        public Map<Tree, Iterable<? extends TreePath>> getAssignmentsForUse() {
            return assignmentsForUse;
        }
        public Set<? extends Tree> getDeadBranches() {
            return deadBranches;
        }
        public Set<VariableElement> getFinalCandidates() {
            return finalCandidates;
        }
    }

    public interface Cancel {
        public boolean isCanceled();
    }

    public static final class AtomicBooleanCancel implements Cancel {

        private final AtomicBoolean cancel;

        public AtomicBooleanCancel(AtomicBoolean cancel) {
            this.cancel = cancel;
        }

        @Override
        public boolean isCanceled() {
            return cancel.get();
        }

    }

    public static boolean definitellyAssigned(CompilationInfo info, VariableElement var, Iterable<? extends TreePath> trees, AtomicBoolean cancel) {
        return definitellyAssigned(info, var, trees, new AtomicBooleanCancel(cancel));
    }

    public static boolean definitellyAssigned(CompilationInfo info, VariableElement var, Iterable<? extends TreePath> trees, Cancel cancel) {
        VisitorImpl v = new VisitorImpl(info, cancel);

        v.variable2State.put(var, State.create(null, false));

        for (TreePath tp : trees) {
            if (cancel.isCanceled()) return false;
            
            v.scan(tp, null);
            
            TreePath toResume = tp;
            
            while (toResume != null) {
                v.resume(toResume.getLeaf(), v.resumeAfter);
                toResume = toResume.getParentPath();
            }

            if (!v.variable2State.get(var).assignments.contains(null)) return true;
        }

        return false;
    }

    private static final class VisitorImpl extends CancellableTreePathScanner<Boolean, ConstructorData> {
        
        private final CompilationInfo info;
        
        private Map<VariableElement, State> variable2State = new HashMap<VariableElement, Flow.State>();
        private Map<Tree, State> use2Values = new IdentityHashMap<Tree, State>();
        private Map<Tree, Collection<Map<VariableElement, State>>> resumeBefore = new IdentityHashMap<Tree, Collection<Map<VariableElement, State>>>();
        private Map<Tree, Collection<Map<VariableElement, State>>> resumeAfter = new IdentityHashMap<Tree, Collection<Map<VariableElement, State>>>();
        private Map<TypeMirror, Collection<Map<VariableElement, State>>> resumeOnExceptionHandler = new IdentityHashMap<TypeMirror, Collection<Map<VariableElement, State>>>();
        private boolean inParameters;
        private Tree nearestMethod;
        private Set<VariableElement> currentMethodVariables = Collections.newSetFromMap(new IdentityHashMap<VariableElement, Boolean>());
        private final Set<Tree> deadBranches = new HashSet<Tree>();
        private final List<TreePath> pendingFinally = new LinkedList<TreePath>();
        private final Cancel cancel;
        private boolean doNotRecord;
        private /*Map<ClassTree, */Set<VariableElement> finalCandidates = new HashSet<>();
        private final Set<VariableElement> usedWhileUndefined = new HashSet<VariableElement>();

        public VisitorImpl(CompilationInfo info, Cancel cancel) {
            super();
            this.info = info;
            this.cancel = cancel;
        }

        @Override
        protected boolean isCanceled() {
            return cancel.isCanceled();
        }

        @Override
        public Boolean scan(Tree tree, ConstructorData p) {
            resume(tree, resumeBefore);
            
            Boolean result = super.scan(tree, p);

            resume(tree, resumeAfter);

            return result;
        }

        private void resume(Tree tree, Map<Tree, Collection<Map<VariableElement, State>>> resume) {
            Collection<Map<VariableElement, State>> toResume = resume.remove(tree);

            if (toResume != null) {
                for (Map<VariableElement, State> s : toResume) {
                    variable2State = mergeOr(variable2State, s);
                }
            }
        }

        @Override
        public Boolean visitAssignment(AssignmentTree node, ConstructorData p) {
            switch (node.getVariable().getKind()) {
                case MEMBER_SELECT:
                    scan(((MemberSelectTree) node.getVariable()).getExpression(), null); //XXX: this will not create a correct TreePath
                    break;
                case ARRAY_ACCESS:
                    scan(node.getVariable(), null);
                    break;
                case IDENTIFIER:
                    break;
                default:
                    //#198233: ignore
            }

            scan(node.getExpression(), p);

            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));
            
            if (e != null && SUPPORTED_VARIABLES.contains(e.getKind())) {
                variable2State.put((VariableElement) e, State.create(new TreePath(getCurrentPath(), node.getExpression()), variable2State.get(e)));
            }
            
            return null;
        }

        @Override
        public Boolean visitCompoundAssignment(CompoundAssignmentTree node, ConstructorData p) {
            switch (node.getVariable().getKind()) {
                case MEMBER_SELECT:
                    scan(((MemberSelectTree) node.getVariable()).getExpression(), null); //XXX: this will not create a correct TreePath
                    break;
                case ARRAY_ACCESS:
                    scan(node.getVariable(), null);
                    break;
                case IDENTIFIER:
                    break;
                default:
                    //#198975: ignore
            }

            scan(node.getExpression(), p);

            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));

            if (e != null && SUPPORTED_VARIABLES.contains(e.getKind())) {
                VariableElement ve = (VariableElement) e;
                State prevState = variable2State.get(ve);
                
                if (LOCAL_VARIABLES.contains(e.getKind())) {
                    use2Values.put(node.getVariable(), prevState); //XXX
                } else if (e.getKind() == ElementKind.FIELD && prevState != null && prevState.hasUnassigned() && !finalCandidates.contains(ve)) {
                    usedWhileUndefined.add(ve);
                }
                
                variable2State.put(ve, State.create(getCurrentPath(), prevState));
            }

            return null;
        }

        @Override
        public Boolean visitVariable(VariableTree node, ConstructorData p) {
            super.visitVariable(node, p);

            Element e = info.getTrees().getElement(getCurrentPath());
            
            if (e != null && SUPPORTED_VARIABLES.contains(e.getKind())) {
                variable2State.put((VariableElement) e, State.create(node.getInitializer() != null ? new TreePath(getCurrentPath(), node.getInitializer()) : inParameters ? getCurrentPath() : null, variable2State.get(e)));
                currentMethodVariables.add((VariableElement) e);
            }
            
            return null;
        }

        @Override
        public Boolean visitMemberSelect(MemberSelectTree node, ConstructorData p) {
            super.visitMemberSelect(node, p);
            handleCurrentAccess();
            return null;
        }
        
        private void handleCurrentAccess() {
            Element e = info.getTrees().getElement(getCurrentPath());

            if (e != null && SUPPORTED_VARIABLES.contains(e.getKind())) {
                VariableElement ve = (VariableElement) e;
                State prevState = variable2State.get(ve);
                
                if (LOCAL_VARIABLES.contains(e.getKind())) {
                    use2Values.put(getCurrentPath().getLeaf(), prevState);
                } else if (e.getKind() == ElementKind.FIELD && (prevState == null || prevState.hasUnassigned()) && !finalCandidates.contains(ve)) {
                    usedWhileUndefined.add(ve);
                }
            }
        }

        @Override
        public Boolean visitLiteral(LiteralTree node, ConstructorData p) {
            Object val = node.getValue();

            if (val instanceof Boolean) {
                return (Boolean) val;
            } else {
                return null;
            }
        }

        @Override
        public Boolean visitIf(IfTree node, ConstructorData p) {
            generalizedIf(node.getCondition(), node.getThenStatement(), node.getElseStatement() != null ? Collections.singletonList(node.getElseStatement()) : Collections.<Tree>emptyList(), true);
            return null;
        }
        
        public void generalizedIf(Tree condition, Tree thenSection, Iterable<? extends Tree> elseSection, boolean realElse) {
            Boolean result = scan(condition, null);

            if (result != null) {
                if (result) {
                    scan(thenSection, null);
                    if (realElse && elseSection.iterator().hasNext())
                        deadBranches.add(elseSection.iterator().next());
                } else {
                    scan(elseSection, null);
                    deadBranches.add(thenSection);
                }

                return ;
            }

            Map<VariableElement, State> oldVariable2State = variable2State;
            
            variable2State = new HashMap<VariableElement, Flow.State>(oldVariable2State);

            scan(thenSection, null);
            
            Map<VariableElement, State> variableStatesAfterThen = new HashMap<VariableElement, Flow.State>(variable2State);

            variable2State = new HashMap<VariableElement, Flow.State>(oldVariable2State);

            scan(elseSection, null);

            variable2State = mergeOr(variable2State, variableStatesAfterThen);
        }

        @Override
        public Boolean visitBinary(BinaryTree node, ConstructorData p) {
            Boolean left = scan(node.getLeftOperand(), p);

            if (left != null && (node.getKind() == Kind.CONDITIONAL_AND || node.getKind() == Kind.CONDITIONAL_OR)) {
                if (left) {
                    if (node.getKind() == Kind.CONDITIONAL_AND) {
                        return scan(node.getRightOperand(), p);
                    } else {
                        return true;
                    }
                } else {
                    if (node.getKind() == Kind.CONDITIONAL_AND) {
                        return false;
                    } else {
                        return scan(node.getRightOperand(), p);
                    }
                }
            }

            Map<VariableElement, State> oldVariable2State = variable2State;

            variable2State = new HashMap<VariableElement, Flow.State>(oldVariable2State);
            
            Boolean right = scan(node.getRightOperand(), p);

            variable2State = mergeOr(variable2State, oldVariable2State);

            if (left == null || right == null) {
                return null;
            }

            switch (node.getKind()) {
                case AND: case CONDITIONAL_AND: return left && right;
                case OR: case CONDITIONAL_OR: return left || right;
                case EQUAL_TO: return left == right;
                case NOT_EQUAL_TO: return left != right;
            }
            
            return null;
        }

        @Override
        public Boolean visitConditionalExpression(ConditionalExpressionTree node, ConstructorData p) {
            Boolean result = scan(node.getCondition(), p);

            if (result != null) {
                if (result) {
                    scan(node.getTrueExpression(), null);
                } else {
                    scan(node.getFalseExpression(), null);
                }

                return null;
            }

            Map<VariableElement, State> oldVariable2State = variable2State;

            variable2State = new HashMap<VariableElement, Flow.State>(oldVariable2State);

            scan(node.getTrueExpression(), null);

            if (node.getFalseExpression() != null) {
                Map<VariableElement, State> variableStatesAfterThen = new HashMap<VariableElement, Flow.State>(variable2State);

                variable2State = new HashMap<VariableElement, Flow.State>(oldVariable2State);

                scan(node.getFalseExpression(), null);

                variable2State = mergeOr(variable2State, variableStatesAfterThen);
            } else {
                variable2State = mergeOr(variable2State, oldVariable2State);
            }

            return null;
        }

        @Override
        public Boolean visitIdentifier(IdentifierTree node, ConstructorData p) {
            super.visitIdentifier(node, p);
            handleCurrentAccess();
            return null;
        }

        @Override
        public Boolean visitUnary(UnaryTree node, ConstructorData p) {
            Boolean val = super.visitUnary(node, p);

            if (val != null && node.getKind() == Kind.LOGICAL_COMPLEMENT) {
                return !val;
            }

            if (    node.getKind() == Kind.PREFIX_DECREMENT
                 || node.getKind() == Kind.PREFIX_INCREMENT
                 || node.getKind() == Kind.POSTFIX_DECREMENT
                 || node.getKind() == Kind.POSTFIX_INCREMENT) {
                Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getExpression()));

                if (e != null && SUPPORTED_VARIABLES.contains(e.getKind())) {
                    VariableElement ve = (VariableElement) e;
                    State prevState = variable2State.get(ve);

                    if (LOCAL_VARIABLES.contains(e.getKind())) {
                        use2Values.put(node.getExpression(), prevState);
                    } else if (e.getKind() == ElementKind.FIELD && prevState != null && prevState.hasUnassigned() && !finalCandidates.contains(ve)) {
                        usedWhileUndefined.add(ve);
                    }
                    
                    variable2State.put(ve, State.create(getCurrentPath(), prevState));
                }
            }


            return null;
        }

        @Override
        public Boolean visitMethod(MethodTree node, ConstructorData p) {
            Tree oldNearestMethod = nearestMethod;
            Set<VariableElement> oldCurrentMethodVariables = currentMethodVariables;
            Map<TypeMirror, Collection<Map<VariableElement, State>>> oldResumeOnExceptionHandler = resumeOnExceptionHandler;
            Map<VariableElement, State> oldVariable2State = variable2State;

            nearestMethod = node;
            currentMethodVariables = Collections.newSetFromMap(new IdentityHashMap<VariableElement, Boolean>());
            resumeOnExceptionHandler = new IdentityHashMap<TypeMirror, Collection<Map<VariableElement, State>>>();
            variable2State = new HashMap<>(variable2State);
            
            for (Iterator<Entry<VariableElement, State>> it = variable2State.entrySet().iterator(); it.hasNext();) {
                Entry<VariableElement, State> e = it.next();
                
                if (e.getKey().getKind().isField()) it.remove();
            }
            
            try {
                scan(node.getModifiers(), null);
                scan(node.getReturnType(), null);
                scan(node.getTypeParameters(), null);

                inParameters = true;

                try {
                    scan(node.getParameters(), null);
                } finally {
                    inParameters = false;
                }

                scan(node.getThrows(), null);
                
                List<Tree> additionalTrees = p != null ? p.initializers : Collections.<Tree>emptyList();
                handleInitializers(additionalTrees);
                
                scan(node.getBody(), null);
                scan(node.getDefaultValue(), null);
            
                //constructor check:
                boolean isConstructor = isConstructor(getCurrentPath());
                Set<VariableElement> definitellyAssignedOnce = new HashSet<VariableElement>();
                Set<VariableElement> assigned = new HashSet<VariableElement>();

                for (Iterator<Entry<VariableElement, State>> it = variable2State.entrySet().iterator(); it.hasNext();) {
                    Entry<VariableElement, State> e = it.next();

                    if (e.getKey().getKind() == ElementKind.FIELD) {
                        if (isConstructor && !e.getValue().hasUnassigned() && !e.getValue().reassigned && !e.getKey().getModifiers().contains(Modifier.STATIC)) {
                            definitellyAssignedOnce.add(e.getKey());
                        }

                        assigned.add(e.getKey());

                        it.remove();
                    }
                }

                if (isConstructor) {
                    assert p != null;
                    if (p.first) {
                        finalCandidates.addAll(definitellyAssignedOnce);
                    } else {
                        finalCandidates.retainAll(definitellyAssignedOnce);
                    }
                    
                    for (VariableElement var : assigned) {
                        if (var.getModifiers().contains(Modifier.STATIC)) {
                            finalCandidates.remove(var);
                        }
                    }
                } else {
                    finalCandidates.removeAll(assigned);
                }
            } finally {
                nearestMethod = oldNearestMethod;
                currentMethodVariables = oldCurrentMethodVariables;
                resumeOnExceptionHandler = oldResumeOnExceptionHandler;
                variable2State = mergeOr(variable2State, oldVariable2State, false);
            }
            
            return null;
        }
        
        private boolean isConstructor(TreePath what) {
            return what.getLeaf().getKind() == Kind.METHOD && ((MethodTree) what.getLeaf()).getReturnType() == null; //TODO: not really a proper way to detect constructors
        }

        @Override
        public Boolean visitWhileLoop(WhileLoopTree node, ConstructorData p) {
            return handleGeneralizedForLoop(null, node.getCondition(), null, node.getStatement(), node.getCondition(), p);
        }

        @Override
        public Boolean visitDoWhileLoop(DoWhileLoopTree node, ConstructorData p) {
            Map<VariableElement, State> beforeLoop = variable2State;

            variable2State = new HashMap<VariableElement, Flow.State>(beforeLoop);

            scan(node.getStatement(), null);
            Boolean condValue = scan(node.getCondition(), null);

            if (condValue != null) {
                if (condValue) {
                    //XXX: handle possibly infinite loop
                } else {
                    //will not run more than once, skip:
                    return null;
                }
            }

            variable2State = mergeOr(beforeLoop, variable2State);

            if (!doNotRecord) {
                boolean oldDoNotRecord = doNotRecord;
                doNotRecord = true;
                
                beforeLoop = new HashMap<VariableElement, State>(variable2State);
                scan(node.getStatement(), null);
                scan(node.getCondition(), null);
                
                doNotRecord = oldDoNotRecord;
                variable2State = beforeLoop;
            }

            return null;
        }

        @Override
        public Boolean visitForLoop(ForLoopTree node, ConstructorData p) {
            return handleGeneralizedForLoop(node.getInitializer(), node.getCondition(), node.getUpdate(), node.getStatement(), node.getCondition(), p);
        }
        
        private Boolean handleGeneralizedForLoop(Iterable<? extends Tree> initializer, Tree condition, Iterable<? extends Tree> update, Tree statement, Tree resumeOn, ConstructorData p) {
            scan(initializer, null);
            
            Map<VariableElement, State> beforeLoop = variable2State;

            variable2State = new HashMap<VariableElement, Flow.State>(beforeLoop);

            Boolean condValue = scan(condition, null);

            if (condValue != null) {
                if (condValue) {
                    //XXX: handle possibly infinite loop
                } else {
                    //will not run at all, skip:
                    return null;
                }
            }
            
            if (!doNotRecord) {
                boolean oldDoNotRecord = doNotRecord;
                doNotRecord = true;

                scan(statement, null);
                scan(update, null);

                variable2State = mergeOr(beforeLoop, variable2State);
                resume(resumeOn, resumeBefore);
                beforeLoop = new HashMap<VariableElement, State>(variable2State);
                scan(condition, null);
                doNotRecord = oldDoNotRecord;
            }

            scan(statement, null);
            scan(update, null);

            variable2State = mergeOr(beforeLoop, variable2State);

            return null;
        }

        public Boolean visitTry(TryTree node, ConstructorData p) {
            if (node.getFinallyBlock() != null) {
                pendingFinally.add(0, new TreePath(getCurrentPath(), node.getFinallyBlock()));
            }
            
            scan(node.getResources(), null);

            Map<VariableElement, State> oldVariable2State = variable2State;

            variable2State = new HashMap<VariableElement, Flow.State>(oldVariable2State);

            scan(node.getBlock(), null);

            HashMap<VariableElement, State> afterBlockVariable2State = new HashMap<VariableElement, Flow.State>(variable2State);

            for (CatchTree ct : node.getCatches()) {
                Map<VariableElement, State> variable2StateBeforeCatch = variable2State;

                variable2State = new HashMap<VariableElement, Flow.State>(oldVariable2State);

                if (ct.getParameter() != null) {
                    TypeMirror caught = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), ct.getParameter()));

                    if (caught != null && caught.getKind() != TypeKind.ERROR) {
                        for (Iterator<Entry<TypeMirror, Collection<Map<VariableElement, State>>>> it = resumeOnExceptionHandler.entrySet().iterator(); it.hasNext();) {
                            Entry<TypeMirror, Collection<Map<VariableElement, State>>> e = it.next();

                            if (info.getTypes().isSubtype(e.getKey(), caught)) {
                                for (Map<VariableElement, State> s : e.getValue()) {
                                    variable2State = mergeOr(variable2State, s);
                                }

                                it.remove();
                            }
                        }
                    }
                }
                
                scan(ct, null);

                variable2State = mergeOr(variable2StateBeforeCatch, variable2State);
            }

            if (node.getFinallyBlock() != null) {
                pendingFinally.remove(0);
                variable2State = mergeOr(mergeOr(oldVariable2State, variable2State), afterBlockVariable2State);

                scan(node.getFinallyBlock(), null);
            }
            
            return null;
        }

        public Boolean visitReturn(ReturnTree node, ConstructorData p) {
            super.visitReturn(node, p);
            variable2State = new HashMap<VariableElement, State>(variable2State);

            if (pendingFinally.isEmpty()) {
                //performance: limit amount of held variables and their mapping:
                for (VariableElement ve : currentMethodVariables) {
                    variable2State.remove(ve);
                }
            }
            
            resumeAfter(nearestMethod, variable2State);
            
            variable2State = new HashMap<VariableElement, State>(variable2State);
            for (Iterator<VariableElement> it = variable2State.keySet().iterator(); it.hasNext();) {
                VariableElement k = it.next();
                
                if (!k.getKind().isField()) it.remove();
            }
            
            return null;
        }

        public Boolean visitBreak(BreakTree node, ConstructorData p) {
            super.visitBreak(node, p);

            StatementTree target = info.getTreeUtilities().getBreakContinueTarget(getCurrentPath());
            
            resumeAfter(target, variable2State);

            variable2State = new HashMap<VariableElement, State>();
            
            return null;
        }

        public Boolean visitSwitch(SwitchTree node, ConstructorData p) {
            scan(node.getExpression(), null);

            Map<VariableElement, State> origVariable2State = new HashMap<VariableElement, State>(variable2State);

            variable2State = new HashMap<VariableElement, State>();

            boolean exhaustive = false;

            for (CaseTree ct : node.getCases()) {
                variable2State = mergeOr(variable2State, origVariable2State);

                if (ct.getExpression() == null) {
                    exhaustive = true;
                }

                scan(ct, null);
            }

            if (!exhaustive) {
                variable2State = mergeOr(variable2State, origVariable2State);
            }
            
            return null;
        }

        public Boolean visitEnhancedForLoop(EnhancedForLoopTree node, ConstructorData p) {
            return handleGeneralizedForLoop(Arrays.asList(node.getVariable(), node.getExpression()), null, null, node.getStatement(), node.getStatement(), p);
        }

        @Override
        public Boolean visitAssert(AssertTree node, ConstructorData p) {
            Map<VariableElement, State> oldVariable2State = variable2State;

            variable2State = new HashMap<VariableElement, Flow.State>(oldVariable2State);

            scan(node.getCondition(), null);

            if (node.getDetail() != null) {
                Map<VariableElement, State> beforeDetailState = new HashMap<VariableElement, Flow.State>(variable2State);

                scan(node.getDetail(), null);

                variable2State = mergeOr(variable2State, beforeDetailState);
            }
            
            variable2State = mergeOr(variable2State, oldVariable2State);

            recordResumeOnExceptionHandler("java.lang.AssertionError");
            return null;
        }

        public Boolean visitContinue(ContinueTree node, ConstructorData p) {
            StatementTree loop = info.getTreeUtilities().getBreakContinueTarget(getCurrentPath());

            if (loop == null) {
                super.visitContinue(node, p);
                return null;
            }

            Tree resumePoint;

            if (loop.getKind() == Kind.LABELED_STATEMENT) {
                loop = ((LabeledStatementTree) loop).getStatement();
            }
            
            switch (loop.getKind()) {
                case WHILE_LOOP:
                    resumePoint = ((WhileLoopTree) loop).getCondition();
                    break;
                case FOR_LOOP:
                    resumePoint = ((ForLoopTree) loop).getCondition();
                    if (resumePoint == null) {
                        resumePoint = ((ForLoopTree) loop).getStatement();
                    }
                    break;
                case DO_WHILE_LOOP:
                    resumePoint = ((DoWhileLoopTree) loop).getCondition();
                    break;
                case ENHANCED_FOR_LOOP:
                    resumePoint = ((EnhancedForLoopTree) loop).getStatement();
                    break;
                default:
                    resumePoint = null;
                    break;
            }

            if (resumePoint != null) {
                recordResume(resumeBefore, resumePoint, variable2State);
            }

            variable2State = new HashMap<VariableElement, State>();

            super.visitContinue(node, p);
            return null;
        }

        public Boolean visitThrow(ThrowTree node, ConstructorData p) {
            super.visitThrow(node, p);

            if (node.getExpression() != null) {
                TypeMirror thrown = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getExpression()));

                recordResumeOnExceptionHandler(thrown);
            }

            return null;
        }

        public Boolean visitMethodInvocation(MethodInvocationTree node, ConstructorData p) {
            super.visitMethodInvocation(node, p);

            Element invoked = info.getTrees().getElement(getCurrentPath());

            if (invoked != null && invoked.getKind() == ElementKind.METHOD) {
                recordResumeOnExceptionHandler((ExecutableElement) invoked);
            }

            return null;
        }

        public Boolean visitNewClass(NewClassTree node, ConstructorData p) {
            super.visitNewClass(node, p);

            Element invoked = info.getTrees().getElement(getCurrentPath());

            if (invoked != null && invoked.getKind() == ElementKind.CONSTRUCTOR) {
                recordResumeOnExceptionHandler((ExecutableElement) invoked);
            }

            return null;
        }

        public Boolean visitClass(ClassTree node, ConstructorData p) {
            List<Tree> staticInitializers = new ArrayList<Tree>(node.getMembers().size());
            List<Tree> instanceInitializers = new ArrayList<Tree>(node.getMembers().size());
            List<MethodTree> constructors = new ArrayList<MethodTree>(node.getMembers().size());
            List<Tree> others = new ArrayList<Tree>(node.getMembers().size());
            
            for (Tree member : node.getMembers()) {
                if (member.getKind() == Kind.BLOCK) {
                    if (((BlockTree) member).isStatic()) {
                        staticInitializers.add(member);
                    } else {
                        instanceInitializers.add(member);
                    }
                } else if (member.getKind() == Kind.VARIABLE && ((VariableTree) member).getInitializer() != null) {
                    if (((VariableTree) member).getModifiers().getFlags().contains(Modifier.STATIC)) {
                        staticInitializers.add((VariableTree) member);
                    } else {
                        instanceInitializers.add((VariableTree) member);
                    }
                } else if (isConstructor(new TreePath(getCurrentPath(), member))) {
                    constructors.add((MethodTree) member);
                } else {
                    others.add(member);
                }
            }
            
            Map<VariableElement, State> oldVariable2State = variable2State;

            variable2State = new HashMap<>(variable2State);
            
            for (Iterator<Entry<VariableElement, State>> it = variable2State.entrySet().iterator(); it.hasNext();) {
                Entry<VariableElement, State> e = it.next();
                
                if (e.getKey().getKind().isField()) it.remove();
            }
            
            try {
                handleInitializers(staticInitializers);
            
                //constructor check:
                Set<VariableElement> definitellyAssignedOnce = new HashSet<VariableElement>();
                Set<VariableElement> assigned = new HashSet<VariableElement>();

                for (Iterator<Entry<VariableElement, State>> it = variable2State.entrySet().iterator(); it.hasNext();) {
                    Entry<VariableElement, State> e = it.next();

                    if (e.getKey().getKind() == ElementKind.FIELD) {
                        if (!e.getValue().hasUnassigned() && !e.getValue().reassigned && e.getKey().getModifiers().contains(Modifier.STATIC)) {
                            definitellyAssignedOnce.add(e.getKey());
                        }

                        assigned.add(e.getKey());

                        it.remove();
                    }
                }

                finalCandidates.addAll(definitellyAssignedOnce);
                //TODO: support for erroneous source code, we should prevent marking instance fields written in static blocks as final-able (i.e. none of "assigned" - "definitellyAssignedOnce" should ever be final candidates
            } finally {
                variable2State = mergeOr(variable2State, oldVariable2State, false);
            }

            boolean first = true;
            
            for (MethodTree constructor : constructors) {
                scan(constructor, new ConstructorData(first, instanceInitializers));
                first = false;
            }
            
            scan(others, p);

            
            return null;
        }

        public Boolean visitBlock(BlockTree node, ConstructorData p) {
            List<? extends StatementTree> statements = new ArrayList<StatementTree>(node.getStatements());
            
            for (int i = 0; i < statements.size(); i++) {
                StatementTree st = statements.get(i);
                
                if (st.getKind() == Kind.IF) {
                    IfTree it = (IfTree) st; 
                    if (it.getElseStatement() == null && Utilities.exitsFromAllBranchers(info, new TreePath(new TreePath(getCurrentPath(), it), it.getThenStatement()))) {
                        generalizedIf(it.getCondition(), it.getThenStatement(), statements.subList(i + 1, statements.size()), false);
                        break;
                    }
                }
                
                scan(st, null);
            }
            
            return null;
        }

        private void recordResumeOnExceptionHandler(ExecutableElement invoked) {
            for (TypeMirror tt : invoked.getThrownTypes()) {
                recordResumeOnExceptionHandler(tt);
            }

            recordResumeOnExceptionHandler("java.lang.RuntimeException");
            recordResumeOnExceptionHandler("java.lang.Error");
        }

        private void recordResumeOnExceptionHandler(String exceptionTypeFQN) {
            TypeElement exc = info.getElements().getTypeElement(exceptionTypeFQN);

            if (exc == null) return;

            recordResumeOnExceptionHandler(exc.asType());
        }

        private void recordResumeOnExceptionHandler(TypeMirror thrown) {
            if (thrown == null || thrown.getKind() == TypeKind.ERROR) return;
            
            Collection<Map<VariableElement, State>> r = resumeOnExceptionHandler.get(thrown);

            if (r == null) {
                resumeOnExceptionHandler.put(thrown, r = new ArrayList<Map<VariableElement, State>>());
            }

            r.add(new HashMap<VariableElement, State>(variable2State));
        }

        public Boolean visitParenthesized(ParenthesizedTree node, ConstructorData p) {
            return super.visitParenthesized(node, p);
        }

        private void resumeAfter(Tree target, Map<VariableElement, State> state) {
            for (TreePath tp : pendingFinally) {
                boolean shouldBeRun = false;

                for (Tree t : tp) {
                    if (t == target) {
                        shouldBeRun = true;
                        break;
                    }
                }

                if (shouldBeRun) {
                    recordResume(resumeBefore, tp.getLeaf(), state);
                } else {
                    break;
                }
            }

            recordResume(resumeAfter, target, state);
        }

        private static void recordResume(Map<Tree, Collection<Map<VariableElement, State>>> resume, Tree target, Map<VariableElement, State> state) {
            Collection<Map<VariableElement, State>> r = resume.get(target);

            if (r == null) {
                resume.put(target, r = new ArrayList<Map<VariableElement, State>>());
            }

            r.add(new HashMap<VariableElement, State>(state));
        }

        public Boolean visitWildcard(WildcardTree node, ConstructorData p) {
            super.visitWildcard(node, p);
            return null;
        }

        public Boolean visitUnionType(UnionTypeTree node, ConstructorData p) {
            super.visitUnionType(node, p);
            return null;
        }

        public Boolean visitTypeParameter(TypeParameterTree node, ConstructorData p) {
            super.visitTypeParameter(node, p);
            return null;
        }

        public Boolean visitTypeCast(TypeCastTree node, ConstructorData p) {
            super.visitTypeCast(node, p);
            return null;
        }

        public Boolean visitSynchronized(SynchronizedTree node, ConstructorData p) {
            super.visitSynchronized(node, p);
            return null;
        }

        public Boolean visitPrimitiveType(PrimitiveTypeTree node, ConstructorData p) {
            super.visitPrimitiveType(node, p);
            return null;
        }

        public Boolean visitParameterizedType(ParameterizedTypeTree node, ConstructorData p) {
            super.visitParameterizedType(node, p);
            return null;
        }

        public Boolean visitOther(Tree node, ConstructorData p) {
            super.visitOther(node, p);
            return null;
        }

        public Boolean visitNewArray(NewArrayTree node, ConstructorData p) {
            super.visitNewArray(node, p);
            return null;
        }

        public Boolean visitModifiers(ModifiersTree node, ConstructorData p) {
            super.visitModifiers(node, p);
            return null;
        }

        public Boolean visitLabeledStatement(LabeledStatementTree node, ConstructorData p) {
            super.visitLabeledStatement(node, p);
            return null;
        }

        public Boolean visitInstanceOf(InstanceOfTree node, ConstructorData p) {
            super.visitInstanceOf(node, p);
            return null;
        }

        public Boolean visitImport(ImportTree node, ConstructorData p) {
            super.visitImport(node, p);
            return null;
        }

        public Boolean visitExpressionStatement(ExpressionStatementTree node, ConstructorData p) {
            super.visitExpressionStatement(node, p);
            return null;
        }

        public Boolean visitErroneous(ErroneousTree node, ConstructorData p) {
            super.visitErroneous(node, p);
            return null;
        }

        public Boolean visitEmptyStatement(EmptyStatementTree node, ConstructorData p) {
            super.visitEmptyStatement(node, p);
            return null;
        }

        public Boolean visitCompilationUnit(CompilationUnitTree node, ConstructorData p) {
            super.visitCompilationUnit(node, p);
            return null;
        }

        public Boolean visitCatch(CatchTree node, ConstructorData p) {
            super.visitCatch(node, p);
            return null;
        }

        public Boolean visitCase(CaseTree node, ConstructorData p) {
            super.visitCase(node, p);
            return null;
        }

        public Boolean visitArrayType(ArrayTypeTree node, ConstructorData p) {
            super.visitArrayType(node, p);
            return null;
        }

        public Boolean visitArrayAccess(ArrayAccessTree node, ConstructorData p) {
            super.visitArrayAccess(node, p);
            return null;
        }

        public Boolean visitAnnotation(AnnotationTree node, ConstructorData p) {
            super.visitAnnotation(node, p);
            return null;
        }

        private Map<VariableElement, State> mergeOr(Map<VariableElement, State> into, Map<VariableElement, State> what) {
            return mergeOr(into, what, true);
        }
        
        private Map<VariableElement, State> mergeOr(Map<VariableElement, State> into, Map<VariableElement, State> what, boolean markMissingAsUnassigned) {
            for (Entry<VariableElement, State> e : what.entrySet()) {
                State stt = into.get(e.getKey());

                if (stt != null) {
                    into.put(e.getKey(), stt.merge(e.getValue()));
                } else if (e.getKey().getKind() == ElementKind.FIELD && markMissingAsUnassigned) {
                    into.put(e.getKey(), e.getValue().merge(UNASSIGNED));
                } else {
                    into.put(e.getKey(), e.getValue());
                }
            }
            
            if (markMissingAsUnassigned) {
                for (Entry<VariableElement, State> e : into.entrySet()) {
                    if (e.getKey().getKind() == ElementKind.FIELD && !what.containsKey(e.getKey())) {
                        into.put(e.getKey(), e.getValue().merge(UNASSIGNED));
                    }
                }
            }

            return into;
        }

        private void handleInitializers(List<Tree> additionalTrees) {
            for (Tree additionalTree : additionalTrees) {
                switch (additionalTree.getKind()) {
                    case BLOCK:
                        Tree oldNearestMethod = nearestMethod;
                        Set<VariableElement> oldCurrentMethodVariables = currentMethodVariables;
                        Map<TypeMirror, Collection<Map<VariableElement, State>>> oldResumeOnExceptionHandler = resumeOnExceptionHandler;

                        nearestMethod = additionalTree;
                        currentMethodVariables = Collections.newSetFromMap(new IdentityHashMap<VariableElement, Boolean>());
                        resumeOnExceptionHandler = new IdentityHashMap<TypeMirror, Collection<Map<VariableElement, State>>>();

                        try {
                            scan(((BlockTree) additionalTree).getStatements(), null);
                        } finally {
                            nearestMethod = oldNearestMethod;
                            currentMethodVariables = oldCurrentMethodVariables;
                            resumeOnExceptionHandler = oldResumeOnExceptionHandler;
                        }
                        break;
                    case VARIABLE: scan(additionalTree, null); break;
                    default: assert false : additionalTree.getKind(); break;
                }
            }
        }
    }
    
    private static final Set<ElementKind> SUPPORTED_VARIABLES = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER, ElementKind.FIELD);
    private static final Set<ElementKind> LOCAL_VARIABLES = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
    
    static class State {
        private final Set<TreePath> assignments;
        private final boolean reassigned;
        private State(Set<TreePath> assignments, boolean reassigned) {
            this.assignments = assignments;
            this.reassigned = reassigned;
        }
        public static State create(TreePath assignment, boolean reassigned) {
            return new State(Collections.singleton(assignment), reassigned);
        }
        public static State create(TreePath assignment, State previous) {
            return new State(Collections.singleton(assignment), previous != null && (previous.assignments.size() > 1 || !previous.assignments.contains(null)));
        }
        public State merge(State value) {
            @SuppressWarnings("LocalVariableHidesMemberVariable")
            Set<TreePath> assignments = new HashSet<TreePath>(this.assignments);

            assignments.addAll(value.assignments);

            return new State(assignments, this.reassigned || value.reassigned);
        }
        
        public boolean hasUnassigned() {
            return assignments.contains(null);
        }
    }
    
    static class ConstructorData {
        final boolean first;
        final List<Tree> initializers;
        public ConstructorData(boolean first, List<Tree> initializers) {
            this.first = first;
            this.initializers = initializers;
        }
    }
    
    private static final State UNASSIGNED = State.create(null, false);

}
