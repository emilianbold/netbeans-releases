/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2012 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007-2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.bugs.NPECheck.State.*;
import org.netbeans.spi.java.hints.*;
import org.netbeans.spi.java.hints.Hint.Options;

/**XXX: null initializer to a non-null variable!
 *
 * @author lahvac
 */
@Hint(displayName="#DN_NPECheck", description="#DESC_NPECheck", category="bugs", options=Options.QUERY)
public class NPECheck {

    @TriggerPattern("$var = $expr")
    public static ErrorDescription assignment(HintContext ctx) {
        Element e = ctx.getInfo().getTrees().getElement(ctx.getVariables().get("$var"));

        if (e == null || !VARIABLE_ELEMENT.contains(e.getKind())) {
            return null;
        }
        
        TreePath expr = ctx.getVariables().get("$expr");
        State r = computeExpressionsState(ctx.getInfo()).get(expr.getLeaf());

        State elementState = getStateFromAnnotations(e);

        if (elementState != null && elementState.isNotNull()) {
            String key = null;

            if (r == NULL) {
                key = "ERR_AssigningNullToNotNull";
            }

            if (r == POSSIBLE_NULL_REPORT) {
                key = "ERR_PossibleAssigingNullToNotNull";
            }

            if (key != null) {
                return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), NbBundle.getMessage(NPECheck.class, key));
            }
        }

        return null;
    }
    
    @TriggerPattern("$select.$variable")
    public static ErrorDescription memberSelect(HintContext ctx) {
        TreePath select = ctx.getVariables().get("$select");
        State r = computeExpressionsState(ctx.getInfo()).get(select.getLeaf());
        
        if (r == State.NULL) {
            String displayName = NbBundle.getMessage(NPECheck.class, "ERR_DereferencingNull");
            
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
        }

        if (r == State.POSSIBLE_NULL_REPORT) {
            String displayName = NbBundle.getMessage(NPECheck.class, "ERR_PossiblyDereferencingNull");
            
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
        }
        
        return null;
    }
    
    @TriggerTreeKind(Kind.METHOD_INVOCATION)
    public static List<ErrorDescription> methodInvocation(HintContext ctx) {
        MethodInvocationTree mit = (MethodInvocationTree) ctx.getPath().getLeaf();
        List<State> paramStates = new ArrayList<State>(mit.getArguments().size());
        Map<Tree, State> expressionsState = computeExpressionsState(ctx.getInfo());

        for (Tree param : mit.getArguments()) {
            State r = expressionsState.get(param);
            paramStates.add(r != null ? r : State.POSSIBLE_NULL);
        }

        Element e = ctx.getInfo().getTrees().getElement(ctx.getPath());

        if (e == null || e.getKind() != ElementKind.METHOD) {
            return null;
        }

        ExecutableElement ee = (ExecutableElement) e;
        int index = 0;
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        List<? extends VariableElement> params = ee.getParameters();

        for (VariableElement param : params) {
            if (getStateFromAnnotations(param) == NOT_NULL && (!ee.isVarArgs() || param != params.get(params.size() - 1))) {
                switch (paramStates.get(index)) {
                    case NULL:
                        result.add(ErrorDescriptionFactory.forTree(ctx, mit.getArguments().get(index), NbBundle.getMessage(NPECheck.class, "ERR_NULL_TO_NON_NULL_ARG")));
                        break;
                    case POSSIBLE_NULL_REPORT:
                        result.add(ErrorDescriptionFactory.forTree(ctx, mit.getArguments().get(index), NbBundle.getMessage(NPECheck.class, "ERR_POSSIBLENULL_TO_NON_NULL_ARG")));
                        break;
                }
            }
            index++;
        }
        
        return result;
    } 
    
    @TriggerPatterns({
        @TriggerPattern("$variable != null"),
        @TriggerPattern("null != $variable")
    })
    public static ErrorDescription notNullWouldBeNPE(HintContext ctx) {
        TreePath variable = ctx.getVariables().get("$variable");
        State r = computeExpressionsState(ctx.getInfo()).get(variable.getLeaf());
        
        if (r == State.NOT_NULL_BE_NPE) {
            String displayName = NbBundle.getMessage(NPECheck.class, "ERR_NotNullWouldBeNPE");
            
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
        }
        
        return null;
    }
    
    @TriggerPattern("return $expression;")
    public static ErrorDescription returnNull(HintContext ctx) {
        TreePath expression = ctx.getVariables().get("$expression");
        State returnState = computeExpressionsState(ctx.getInfo()).get(expression.getLeaf());

        if (returnState == null) return null;

        TreePath method = ctx.getPath();

        while (method != null && method.getLeaf().getKind() != Kind.METHOD && method.getLeaf().getKind() != Kind.CLASS) {
            method = method.getParentPath();
        }

        if (method == null || method.getLeaf().getKind() != Kind.METHOD) return null;

        Element el = ctx.getInfo().getTrees().getElement(method);

        if (el == null || el.getKind() != ElementKind.METHOD) return null;

        State expected = getStateFromAnnotations(el);
        String key = null;

        switch (returnState) {
            case NULL:
                if (expected.isNotNull()) key = "ERR_ReturningNullFromNonNull";
                break;
            case POSSIBLE_NULL_REPORT:
                if (expected.isNotNull()) key = "ERR_ReturningPossibleNullFromNonNull";
                break;
        }

        if (key != null) {
            String displayName = NbBundle.getMessage(NPECheck.class, key);
            return ErrorDescriptionFactory.forName(ctx, expression, displayName);
        }
        
        return null;
    }
    
    private static final Object KEY_EXPRESSION_STATE = new Object();
    //Cancelling:
    private static Map<Tree, State> computeExpressionsState(CompilationInfo info) {
        Map<Tree, State> result = (Map<Tree, State>) info.getCachedValue(KEY_EXPRESSION_STATE);
        
        if (result != null) {
            return result;
        }
        
        VisitorImpl v = new VisitorImpl(info);
        
        v.scan(info.getCompilationUnit(), null);
        
        info.putCachedValue(KEY_EXPRESSION_STATE, result = v.expressionState, CompilationInfo.CacheClearPolicy.ON_TASK_END);
        
        return result;
    }
    
    private static State getStateFromAnnotations(Element e) {
        return getStateFromAnnotations(e, State.POSSIBLE_NULL);
    }

    private static State getStateFromAnnotations(Element e, State def) {
        for (AnnotationMirror am : e.getAnnotationMirrors()) {
            String simpleName = ((TypeElement) am.getAnnotationType().asElement()).getSimpleName().toString();

            if ("Nullable".equals(simpleName) || "NullAllowed".equals(simpleName)) {
                return State.POSSIBLE_NULL_REPORT;
            }

            if ("CheckForNull".equals(simpleName)) {
                return State.POSSIBLE_NULL_REPORT;
            }

            if ("NotNull".equals(simpleName) || "NonNull".equals(simpleName) || "Nonnull".equals(simpleName)) {
                return State.NOT_NULL;
            }
        }

        return def;
    }
        
    private static final class VisitorImpl extends TreePathScanner<State, Void> {
        
        private final CompilationInfo info;
        private Map<VariableElement, State> variable2State = new HashMap<VariableElement, NPECheck.State>();
        private Map<VariableElement, State> testedTo = new HashMap<VariableElement, NPECheck.State>();
        private final Map<Tree, State> expressionState = new IdentityHashMap<Tree, State>();
        private boolean not;

        public VisitorImpl(CompilationInfo info) {
            this.info = info;
        }

        @Override
        public State scan(Tree tree, Void p) {
            State r = super.scan(tree, p);
            
            if (r != null) {
                expressionState.put(tree, r);
            }
            
            return r;
        }

        @Override
        public State visitAssignment(AssignmentTree node, Void p) {
            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));
            
            if (e == null || !VARIABLE_ELEMENT.contains(e.getKind())) {
                return super.visitAssignment(node, p);
            }
            
            State r = scan(node.getExpression(), p);
            
            variable2State.put((VariableElement) e, r);
            
            scan(node.getVariable(), p);
            
            return r;
        }

        @Override
        public State visitVariable(VariableTree node, Void p) {
            Element e = info.getTrees().getElement(getCurrentPath());
            
            if (e == null) {
                return super.visitVariable(node, p);
            }
            
            State r = scan(node.getInitializer(), p);
            
            variable2State.put((VariableElement) e, r);
            
            return r;
        }

        @Override
        public State visitMemberSelect(MemberSelectTree node, Void p) {
            State expr = scan(node.getExpression(), p);
            boolean wasNPE = false;
            
            if (expr == State.NULL || expr == State.POSSIBLE_NULL || expr == State.POSSIBLE_NULL_REPORT) {
                wasNPE = true;
            }
            
            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getExpression()));
            
            if (isVariableElement(e)) {
                State r = getStateFromAnnotations(e);
                
                if (wasNPE) {
                    variable2State.put((VariableElement) e, NOT_NULL_BE_NPE);
                }
                
                return r;
            }
            
            return State.POSSIBLE_NULL;
        }

        @Override
        public State visitLiteral(LiteralTree node, Void p) {
            if (node.getValue() == null) {
                return State.NULL;
            } else {
                return State.NOT_NULL;
            }
        }

        @Override
        public State visitIf(IfTree node, Void p) {
            Map<VariableElement, State> oldVariable2StateBeforeCondition = new HashMap<VariableElement, State>(variable2State);
            Map<VariableElement, State> oldTestedTo = testedTo;
            
            testedTo = new HashMap<VariableElement, NPECheck.State>();
            
            scan(node.getCondition(), p);
            
            Map<VariableElement, State> oldVariable2State = variable2State;
            
            variable2State = new HashMap<VariableElement, NPECheck.State>(oldVariable2State);
            variable2State.putAll(testedTo);
            
            scan(node.getThenStatement(), null);
            
            Map<VariableElement, State> variableStatesAfterThen = new HashMap<VariableElement, NPECheck.State>(variable2State);
            Map<VariableElement, State> testedToAfterThen =  new HashMap<VariableElement, NPECheck.State>(testedTo);
            
            variable2State = new HashMap<VariableElement, NPECheck.State>(oldVariable2StateBeforeCondition);
            testedTo = new HashMap<VariableElement, NPECheck.State>();
            not = true;
            scan(node.getCondition(), p);
            not = false;
            
            variable2State = new HashMap<VariableElement, NPECheck.State>(oldVariable2State);
            variable2State.putAll(testedTo);
            
            Map<VariableElement, State> negTestedTo = new HashMap<VariableElement, State>(testedTo);

            scan(node.getElseStatement(), null);
            
            Map<VariableElement, State> variableStatesAfterElse = new HashMap<VariableElement, NPECheck.State>(variable2State);
            
            variable2State = oldVariable2State;
            
            Set<VariableElement> uncertain = new HashSet<VariableElement>();
            
            if (node.getElseStatement() == null) {
                for (Entry<VariableElement, State> e : variableStatesAfterThen.entrySet()) {
                    if (testedToAfterThen.get(e.getKey()) == State.NULL) {
                        if (e.getValue() != null) {
                            switch (e.getValue()) {
                                case NOT_NULL:
                                case NOT_NULL_BE_NPE:
                                    variable2State.put(e.getKey(), e.getValue());
                                    break;
                                case POSSIBLE_NULL:
                                    variable2State.put(e.getKey(), POSSIBLE_NULL);
                                    uncertain.add(e.getKey());
                                    break;
                            }
                        }
                    } else {
                        State c = collect(variable2State.get(e.getKey()), e.getValue());
                        
                        variable2State.put(e.getKey(), c);
                    }
                }
            } else {
                for (Entry<VariableElement, State> e : variableStatesAfterThen.entrySet()) {
                    State t = e.getValue();
                    State el = variableStatesAfterElse.get(e.getKey());
                    
                    if (t == el) {
                        variable2State.put(e.getKey(), t);
                    } else {
                        if (t == State.NULL && el == State.NOT_NULL) {
                            variable2State.put(e.getKey(), State.POSSIBLE_NULL_REPORT);
                        }
                        if (el == State.NULL && t == State.NOT_NULL) {
                            variable2State.put(e.getKey(), State.POSSIBLE_NULL_REPORT);
                        }
                    }
                }
            }
            
            boolean thenExitsFromAllBranches = new ExitsFromAllBranches(info).scan(new TreePath(getCurrentPath(), node.getThenStatement()), null) == Boolean.TRUE;
            
            if (!thenExitsFromAllBranches) {
                for (Entry<VariableElement, State> test : testedTo.entrySet()) {
                    if ((variable2State.get(test.getKey()) == POSSIBLE_NULL || variable2State.get(test.getKey()) == null) && !uncertain.contains(test.getKey())) {
                        variable2State.put(test.getKey(), POSSIBLE_NULL_REPORT);
                    }
                }
            } else {
                variable2State.putAll(negTestedTo);
            }
            
            testedTo = oldTestedTo;
            
            return null;
        }

        @Override
        public State visitBinary(BinaryTree node, Void p) {
            State left = null;
            boolean subnodesAlreadyProcessed = false;
            Kind kind = node.getKind();
            
            if (not) {
                switch (kind) {
                    case CONDITIONAL_AND: kind = Kind.CONDITIONAL_OR; break;
                    case CONDITIONAL_OR: kind = Kind.CONDITIONAL_AND; break;
                    case EQUAL_TO: kind = Kind.NOT_EQUAL_TO; break;
                    case NOT_EQUAL_TO: kind = Kind.EQUAL_TO; break;
                }
            }
            
            if (kind == Kind.CONDITIONAL_AND) {
                left = scan(node.getLeftOperand(), p);
                
                Map<VariableElement, State> oldVariable2State = variable2State;
                Map<VariableElement, State> oldTestedTo = testedTo;
                
                variable2State = new HashMap<VariableElement, NPECheck.State>(variable2State);
                variable2State.putAll(testedTo);
                
                testedTo = new HashMap<VariableElement, State>();
                
                scan(node.getRightOperand(), p);
                variable2State = oldVariable2State;
                
                Map<VariableElement, State> o = new HashMap<VariableElement, NPECheck.State>(oldTestedTo);

                o.putAll(testedTo);

                testedTo = o;
                
                subnodesAlreadyProcessed = true;
            }
            
            if (kind == Kind.CONDITIONAL_OR) {
                boolean oldNot = not;
                
                not ^= true;
                left = scan(node.getLeftOperand(), p);
                not = oldNot;
                
                Map<VariableElement, State> oldVariable2State = variable2State;
                Map<VariableElement, State> oldTestedTo = testedTo;
                
                variable2State = new HashMap<VariableElement, NPECheck.State>(variable2State);
                variable2State.putAll(testedTo);
                
                testedTo = new HashMap<VariableElement, State>();
                
                scan(node.getRightOperand(), p);
                variable2State = oldVariable2State;
                
                Map<VariableElement, State> o = new HashMap<VariableElement, NPECheck.State>(oldTestedTo);

                o.putAll(testedTo);

                testedTo = o;
                
                subnodesAlreadyProcessed = true;
            }
            
            State right = null;
            
            if (!subnodesAlreadyProcessed) {
                boolean oldNot = not;

                not = false;
                left = scan(node.getLeftOperand(), p);
                right = scan(node.getRightOperand(), p);
                not = oldNot;
            }
            
            if (kind == Kind.EQUAL_TO) {
                if (right == State.NULL) {
                    Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getLeftOperand()));
                    
                    if (isVariableElement(e)) {
                        testedTo.put((VariableElement) e, State.NULL);
                        
                        return null;
                    }
                }
                if (left == State.NULL) {
                    Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getRightOperand()));
                    
                    if (isVariableElement(e)) {
                        testedTo.put((VariableElement) e, State.NULL);
                        
                        return null;
                    }
                }
            }
            
            if (kind == Kind.NOT_EQUAL_TO) {
                if (right == State.NULL) {
                    Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getLeftOperand()));
                    
                    if (isVariableElement(e) && variable2State.get(e) != State.NOT_NULL_BE_NPE) {
                        testedTo.put((VariableElement) e, State.NOT_NULL);
                        
                        return null;
                    }
                }
                if (left == State.NULL) {
                    Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getRightOperand()));
                    
                    if (isVariableElement(e) && variable2State.get(e) != State.NOT_NULL_BE_NPE) {
                        testedTo.put((VariableElement) e, State.NOT_NULL);
                        
                        return null;
                    }
                }
            }
            
            return null;
        }

        @Override
        public State visitInstanceOf(InstanceOfTree node, Void p) {
            super.visitInstanceOf(node, p);
            
            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getExpression()));

            if (isVariableElement(e) && (variable2State.get(e) == null || !variable2State.get(e).isNotNull()) && !not) {
                testedTo.put((VariableElement) e, State.NOT_NULL);
            }
            
            return null;
        }

        @Override
        public State visitConditionalExpression(ConditionalExpressionTree node, Void p) {
            //TODO: handle the condition similarly to visitIf
            scan(node.getCondition(), p);
                
            Map<VariableElement, State> oldVariable2State = variable2State;

            variable2State = new HashMap<VariableElement, State>(variable2State);
            
            variable2State.putAll(testedTo);
                
            State thenSection = scan(node.getTrueExpression(), p);
            
            variable2State = new HashMap<VariableElement, State>(variable2State);
            
            for (Entry<VariableElement, State> e : testedTo.entrySet()) {
                State reversed = e.getValue().reverse();
                if (reversed != null)
                    variable2State.put(e.getKey(), reversed);
            }
            
            State elseSection = scan(node.getFalseExpression(), p);
            
            variable2State = oldVariable2State;
                
            State result;
            
            if (thenSection == elseSection) {
                result = thenSection;
            } else {
                result = NPECheck.State.NOT_NULL.POSSIBLE_NULL;
            }
            
            for (Entry<VariableElement, State> test : testedTo.entrySet()) {
                if (variable2State.get(test.getKey()) == POSSIBLE_NULL || variable2State.get(test.getKey()) == null) {
                    variable2State.put(test.getKey(), POSSIBLE_NULL_REPORT);
                }
            }
                
            return result;
        }

        @Override
        public State visitNewClass(NewClassTree node, Void p) {
            super.visitNewClass(node, p);
            
            return State.NOT_NULL;
        }

        @Override
        public State visitMethodInvocation(MethodInvocationTree node, Void p) {
            scan(node.getTypeArguments(), p);
            scan(node.getMethodSelect(), p);
            
            for (Tree param : node.getArguments()) {
                scan(param, p);
            }
            
            Element e = info.getTrees().getElement(getCurrentPath());
            
            if (e == null || e.getKind() != ElementKind.METHOD) {
                return State.POSSIBLE_NULL;
            }
            
            return getStateFromAnnotations(e);
        }

        @Override
        public State visitIdentifier(IdentifierTree node, Void p) {
            super.visitIdentifier(node, p);
            
            Element e = info.getTrees().getElement(getCurrentPath());

            if (e == null) {
                return State.POSSIBLE_NULL;
            }
            
            if (e != null) {
                State s = variable2State.get(e);
                
                if (s != null) {
                    return s;
                }
            }
            
            return getStateFromAnnotations(e);
        }

        @Override
        public State visitWhileLoop(WhileLoopTree node, Void p) {
            Map<VariableElement, State> oldTestedTo = testedTo;
            
            testedTo = new HashMap<VariableElement, State>();
            
            Map<VariableElement, State> oldVariable2State = variable2State;

            variable2State = new HashMap<VariableElement, State>(variable2State);
            
            scan(node.getCondition(), p);
            
            variable2State.putAll(testedTo);
            
            scan(node.getStatement(), p);
            
            testedTo = new HashMap<VariableElement, State>();
            
            scan(node.getCondition(), p);
            
            variable2State.putAll(testedTo);
            
            scan(node.getStatement(), p);
            
            variable2State = new HashMap<VariableElement, State>(oldVariable2State);
            
            for (Entry<VariableElement, State> e : testedTo.entrySet()) {
                State o = variable2State.get(e.getKey());
                
                if (e.getValue() == NOT_NULL && (o == POSSIBLE_NULL || o == null)) {
                    variable2State.put(e.getKey(), POSSIBLE_NULL_REPORT);
                }
            }
            
            testedTo = oldTestedTo;
            
            return null;
        }

        @Override
        public State visitUnary(UnaryTree node, Void p) {
            boolean oldNot = not;
            
            not ^= node.getKind() == Kind.LOGICAL_COMPLEMENT;
            
            State res = scan(node.getExpression(), p);
            
            not = oldNot;
            
            return res;
        }

        @Override
        public State visitMethod(MethodTree node, Void p) {
            variable2State = new HashMap<VariableElement, NPECheck.State>();
            testedTo = new HashMap<VariableElement, NPECheck.State>();
            not = false;
            return super.visitMethod(node, p);
        }

        @Override
        public State visitForLoop(ForLoopTree node, Void p) {
            scan(node.getInitializer(), p);
            
            scan(node.getStatement(), p);
            scan(node.getUpdate(), p);
            
            scan(node.getCondition(), p);
            scan(node.getStatement(), p);
            scan(node.getUpdate(), p);
            return null;
        }

    }
    
    static enum State {
        NULL,
        POSSIBLE_NULL,
        POSSIBLE_NULL_REPORT,
        NOT_NULL,
        NOT_NULL_BE_NPE;
        
        public @CheckForNull State reverse() {
            switch (this) {
                case NULL:
                    return NOT_NULL;
                case POSSIBLE_NULL:
                case POSSIBLE_NULL_REPORT:
                    return this;
                case NOT_NULL:
                case NOT_NULL_BE_NPE:
                    return NULL;
                default: throw new IllegalStateException();
            }
        }
        
        public boolean isNotNull() {
            return this == NOT_NULL || this == NOT_NULL_BE_NPE;
        }
        
        public static State collect(State s1, State s2) {
            if (s1 == s2) return s1;
            if (s1 == NULL && s2 != null && s2.isNotNull()) return POSSIBLE_NULL_REPORT;
            if (s1 != null && s1.isNotNull() && s2 == NULL) return POSSIBLE_NULL_REPORT;
            if (s1 == POSSIBLE_NULL_REPORT && s2 != POSSIBLE_NULL) return POSSIBLE_NULL_REPORT;
            if (s1 != POSSIBLE_NULL && s2 == POSSIBLE_NULL_REPORT) return POSSIBLE_NULL_REPORT;
            
            return POSSIBLE_NULL;
        }
    }
    
    //XXX copied from IntroduceHint:
    private static final class ExitsFromAllBranches extends TreePathScanner<Boolean, Void> {
        
        private CompilationInfo info;
        private Set<Tree> seenTrees = new HashSet<Tree>();

        public ExitsFromAllBranches(CompilationInfo info) {
            this.info = info;
        }

        @Override
        public Boolean scan(Tree tree, Void p) {
            seenTrees.add(tree);
            return super.scan(tree, p);
        }

        @Override
        public Boolean visitIf(IfTree node, Void p) {
            return scan(node.getThenStatement(), null) == Boolean.TRUE && scan(node.getElseStatement(), null) == Boolean.TRUE;
        }

        @Override
        public Boolean visitReturn(ReturnTree node, Void p) {
            return true;
        }

        @Override
        public Boolean visitBreak(BreakTree node, Void p) {
            return !seenTrees.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()));
        }

        @Override
        public Boolean visitContinue(ContinueTree node, Void p) {
            return !seenTrees.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()));
        }

        @Override
        public Boolean visitClass(ClassTree node, Void p) {
            return false;
        }

        @Override
        public Boolean visitThrow(ThrowTree node, Void p) {
            return true; //XXX: simplification
        }
        
    }
    
    private static boolean isVariableElement(Element ve) {
        return ve != null && VARIABLE_ELEMENT.contains(ve.getKind());
    }
    
    private static final Set<ElementKind> VARIABLE_ELEMENT = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
    
}
