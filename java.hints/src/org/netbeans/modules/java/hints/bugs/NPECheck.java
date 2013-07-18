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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnionType;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.bugs.NPECheck.State.*;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.java.hints.*;
import org.netbeans.spi.java.hints.Hint.Options;
import org.openide.util.Lookup;

/**
 *
 * @author lahvac
 */
@Hint(displayName="#DN_NPECheck", description="#DESC_NPECheck", category="bugs", options=Options.QUERY, suppressWarnings = {"null", "", "NullableProblems"})
public class NPECheck {

    static final boolean DEF_ENABLE_FOR_FIELDS = false;
    @BooleanOption(displayName = "#LBL_NPECheck.ENABLE_FOR_FIELDS", tooltip = "#TP_NPECheck.ENABLE_FOR_FIELDS", defaultValue=DEF_ENABLE_FOR_FIELDS)
    static final String KEY_ENABLE_FOR_FIELDS = "enable-for-fields";
    
    @TriggerPatterns({
        @TriggerPattern("$mods$ $type $var = $expr;"),
        @TriggerPattern("$var = $expr")
    })
    public static ErrorDescription assignment(HintContext ctx) {
        Element e = ctx.getInfo().getTrees().getElement(ctx.getVariables().get("$var"));

        if (!isVariableElement(ctx, e)) {
            return null;
        }
        
        TreePath expr = ctx.getVariables().get("$expr");
        State r = computeExpressionsState(ctx).get(expr.getLeaf());

        State elementState = getStateFromAnnotations(ctx.getInfo(), e);

        if (elementState != null && elementState.isNotNull()) {
            String key = null;

            if (r == NULL || r == NULL_HYPOTHETICAL) {
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
        State r = computeExpressionsState(ctx).get(select.getLeaf());
        
        if (r == NULL || r == NULL_HYPOTHETICAL) {
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
        Map<Tree, State> expressionsState = computeExpressionsState(ctx);

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
            if (getStateFromAnnotations(ctx.getInfo(), param) == NOT_NULL && (!ee.isVarArgs() || param != params.get(params.size() - 1))) {
                switch (paramStates.get(index)) {
                    case NULL: case NULL_HYPOTHETICAL:
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
        @TriggerPattern("null != $variable"),
    })
    public static ErrorDescription notNullTest(HintContext ctx) {
        TreePath variable = ctx.getVariables().get("$variable");
        State r = computeExpressionsState(ctx).get(variable.getLeaf());
        
        if (r != null && r.isNotNull() && !ignore(ctx, false)) {
            String displayName = NbBundle.getMessage(NPECheck.class, r == State.NOT_NULL_BE_NPE ? "ERR_NotNullWouldBeNPE" : "ERR_NotNull");
            
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
        }
        
        return null;
    }
    
    @TriggerPatterns({
        @TriggerPattern("$variable == null"),
        @TriggerPattern("null == $variable")
    })
    public static ErrorDescription nullTest(HintContext ctx) {
        TreePath variable = ctx.getVariables().get("$variable");
        State r = computeExpressionsState(ctx).get(variable.getLeaf());
        
        if (r != null && r.isNotNull() && !ignore(ctx, true)) {
            String displayName = NbBundle.getMessage(NPECheck.class, r == State.NOT_NULL_BE_NPE ? "ERR_NotNullWouldBeNPE" : "ERR_NotNull");
            
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
        }
        
        return null;
    }
    
    private static boolean ignore(HintContext ctx, boolean equalsToNull) {
        TreePath test = ctx.getPath().getParentPath();
        
        while (test != null && !StatementTree.class.isAssignableFrom(test.getLeaf().getKind().asInterface())) {
            test = test.getParentPath();
        }
        
        if (test == null) return false;
        
        if (test.getLeaf().getKind() == Kind.ASSERT && !equalsToNull) {
            return verifyConditions(ctx, ((AssertTree) test.getLeaf()).getCondition(), equalsToNull);
        } else if (test.getLeaf().getKind() == Kind.IF && equalsToNull) {
            StatementTree last;
            IfTree it = (IfTree) test.getLeaf();

            switch (it.getThenStatement().getKind()) {
                case BLOCK:
                    List<? extends StatementTree> statements = ((BlockTree) it.getThenStatement()).getStatements();
                    last = !statements.isEmpty() ? statements.get(statements.size() - 1) : null;
                    break;
                default:
                    last = it.getThenStatement();
                    break;
            }
            
            return last != null && last.getKind() == Kind.THROW && verifyConditions(ctx, ((IfTree) test.getLeaf()).getCondition(), equalsToNull);
        }
        
        return false;
    }
    
    private static boolean verifyConditions(HintContext ctx, ExpressionTree cond, boolean equalsToNull) {
        switch (cond.getKind()) {
            case PARENTHESIZED: return verifyConditions(ctx, ((ParenthesizedTree) cond).getExpression(), equalsToNull);
            case NOT_EQUAL_TO: return !equalsToNull && hasNull(ctx, (BinaryTree) cond);
            case EQUAL_TO: return equalsToNull && hasNull(ctx, (BinaryTree) cond);
            case CONDITIONAL_OR: case OR:
                return equalsToNull && verifyConditions(ctx, ((BinaryTree) cond).getLeftOperand(), equalsToNull) && verifyConditions(ctx, ((BinaryTree) cond).getRightOperand(), equalsToNull);
            case CONDITIONAL_AND: case AND:
                return !equalsToNull && verifyConditions(ctx, ((BinaryTree) cond).getLeftOperand(), equalsToNull) && verifyConditions(ctx, ((BinaryTree) cond).getRightOperand(), equalsToNull);
        }
        
        return false;
    }
    
    private static boolean hasNull(HintContext ctx, BinaryTree bt) {
        return    bt.getLeftOperand().getKind() == Kind.NULL_LITERAL
               || bt.getRightOperand().getKind() == Kind.NULL_LITERAL;
    }
    
    @TriggerPattern("return $expression;")
    public static ErrorDescription returnNull(HintContext ctx) {
        TreePath expression = ctx.getVariables().get("$expression");
        State returnState = computeExpressionsState(ctx).get(expression.getLeaf());

        if (returnState == null) return null;

        TreePath method = ctx.getPath();

        while (method != null && method.getLeaf().getKind() != Kind.METHOD && method.getLeaf().getKind() != Kind.CLASS) {
            method = method.getParentPath();
        }

        if (method == null || method.getLeaf().getKind() != Kind.METHOD) return null;

        Element el = ctx.getInfo().getTrees().getElement(method);

        if (el == null || el.getKind() != ElementKind.METHOD) return null;

        State expected = getStateFromAnnotations(ctx.getInfo(), el);
        String key = null;

        switch (returnState) {
            case NULL: case NULL_HYPOTHETICAL:
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
    private static Map<Tree, State> computeExpressionsState(HintContext ctx) {
        Map<Tree, State> result = (Map<Tree, State>) ctx.getInfo().getCachedValue(KEY_EXPRESSION_STATE);
        
        if (result != null) {
            return result;
        }
        
        VisitorImpl v = new VisitorImpl(ctx);
        
        v.scan(ctx.getInfo().getCompilationUnit(), null);
        
        ctx.getInfo().putCachedValue(KEY_EXPRESSION_STATE, result = v.expressionState, CompilationInfo.CacheClearPolicy.ON_TASK_END);
        
        return result;
    }
    
    private static State getStateFromAnnotations(CompilationInfo info, Element e) {
        return getStateFromAnnotations(info, e, State.POSSIBLE_NULL);
    }

    private static final AnnotationMirrorGetter OVERRIDE_ANNOTATIONS = Lookup.getDefault().lookup(AnnotationMirrorGetter.class);
    
    private static State getStateFromAnnotations(CompilationInfo info, Element e, State def) {
        if (e == null) return def;
        
        Iterable<? extends AnnotationMirror> mirrors = OVERRIDE_ANNOTATIONS != null ? OVERRIDE_ANNOTATIONS.getAnnotationMirrors(info, e) : null;
        
        if (mirrors == null) mirrors = e.getAnnotationMirrors();
        
        for (AnnotationMirror am : mirrors) {
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
    
    public interface AnnotationMirrorGetter {
        public Iterable<? extends AnnotationMirror> getAnnotationMirrors(CompilationInfo info, Element el);
    }
        
    private static final class VisitorImpl extends CancellableTreePathScanner<State, Void> {
        
        private final HintContext ctx;
        private final CompilationInfo info;
        private Map<VariableElement, State> variable2State = new HashMap<VariableElement, NPECheck.State>();
        private final Map<Tree, Collection<Map<VariableElement, State>>> resumeBefore = new IdentityHashMap<Tree, Collection<Map<VariableElement, State>>>();
        private final Map<Tree, Collection<Map<VariableElement, State>>> resumeAfter = new IdentityHashMap<Tree, Collection<Map<VariableElement, State>>>();
        private       Map<TypeMirror, Map<VariableElement, State>> resumeOnExceptionHandler = new IdentityHashMap<TypeMirror, Map<VariableElement, State>>();
        private final Map<Tree, State> expressionState = new IdentityHashMap<Tree, State>();
        private final List<TreePath> pendingFinally = new LinkedList<TreePath>();
        private boolean not;
        private boolean doNotRecord;

        public VisitorImpl(HintContext ctx) {
            this.ctx = ctx;
            this.info = ctx.getInfo();
        }

        @Override
        protected boolean isCanceled() {
            return ctx.isCanceled();
        }

        @Override
        public State scan(Tree tree, Void p) {
            resume(tree, resumeBefore);

            State r = super.scan(tree, p);
            
            TypeMirror currentType = tree != null ? info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), tree)) : null;
            
            if (currentType != null && currentType.getKind().isPrimitive()) {
                r = State.NOT_NULL;
            }
            
            if (r != null && !doNotRecord) {
                expressionState.put(tree, r);
            }
            
            resume(tree, resumeAfter);
            
            return r;
        }

        private void resume(Tree tree, Map<Tree, Collection<Map<VariableElement, State>>> resume) {
            Collection<Map<VariableElement, State>> toResume = resume.remove(tree);

            if (toResume != null) {
                for (Map<VariableElement, State> s : toResume) {
                    mergeIntoVariable2State(s);
                }
            }
        }

        @Override
        public State visitAssignment(AssignmentTree node, Void p) {
            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));
            Map<VariableElement, State> orig = new HashMap<VariableElement, State>(variable2State);
            State r = scan(node.getExpression(), p);
            
            scan(node.getVariable(), p);
            
            mergeHypotheticalVariable2State(orig);
            
            if (isVariableElement(e)) {
                variable2State.put((VariableElement) e, r);
            }
            
            return r;
        }

        @Override
        public State visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
            Map<VariableElement, State> orig = new HashMap<VariableElement, State>(variable2State);
            
            scan(node.getExpression(), p);
            scan(node.getVariable(), p);
            
            mergeHypotheticalVariable2State(orig);
            
            return null;
        }

        @Override
        public State visitVariable(VariableTree node, Void p) {
            Element e = info.getTrees().getElement(getCurrentPath());
            Map<VariableElement, State> orig = new HashMap<VariableElement, State>(variable2State);
            State r = scan(node.getInitializer(), p);
            
            mergeHypotheticalVariable2State(orig);
            
            if (e != null) {
                variable2State.put((VariableElement) e, r);
            }
            
            return r;
        }

        @Override
        public State visitMemberSelect(MemberSelectTree node, Void p) {
            State expr = scan(node.getExpression(), p);
            boolean wasNPE = false;
            
            if (expr == State.NULL || expr == State.NULL_HYPOTHETICAL || expr == State.POSSIBLE_NULL || expr == State.POSSIBLE_NULL_REPORT) {
                wasNPE = true;
            }
            
            Element site = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getExpression()));
            
            if (isVariableElement(site) && wasNPE && (variable2State.get(site) == null || !variable2State.get(site).isNotNull())) {
                variable2State.put((VariableElement) site, NOT_NULL_BE_NPE);
            }
            
            return getStateFromAnnotations(info, info.getTrees().getElement(getCurrentPath()));
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
            
            State condition = scan(node.getCondition(), p);
            
            scan(node.getThenStatement(), null);
            
            Map<VariableElement, State> variableStatesAfterThen = new HashMap<VariableElement, NPECheck.State>(variable2State);
            
            variable2State = new HashMap<VariableElement, NPECheck.State>(oldVariable2StateBeforeCondition);
            not = true;
            doNotRecord = true;
            scan(node.getCondition(), p);
            not = false;
            doNotRecord = false;
            
            scan(node.getElseStatement(), null);
            
            boolean thenExitsFromAllBranches = Utilities.exitsFromAllBranchers(info, new TreePath(getCurrentPath(), node.getThenStatement()));
            boolean elseExitsFromAllBranches = node.getElseStatement() != null && Utilities.exitsFromAllBranchers(info, new TreePath(getCurrentPath(), node.getElseStatement()));
            
            if (thenExitsFromAllBranches && !elseExitsFromAllBranches) {
                //already set
            } else if (!thenExitsFromAllBranches && elseExitsFromAllBranches) {
                variable2State = variableStatesAfterThen;
            } else {
                mergeIntoVariable2State(variableStatesAfterThen);
            }
            
            return null;
        }

        @Override
        public State visitBinary(BinaryTree node, Void p) {
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
                scan(node.getLeftOperand(), p);
                scan(node.getRightOperand(), p);
                
                subnodesAlreadyProcessed = true;
            }
            
            if (kind == Kind.CONDITIONAL_OR) {
                HashMap<VariableElement, State> orig = new HashMap<VariableElement, NPECheck.State>(variable2State);
                
                scan(node.getLeftOperand(), p);
                
                Map<VariableElement, State> afterLeft = variable2State;
                
                variable2State = orig;
                
                boolean oldNot = not;
                boolean oldDoNotRecord = doNotRecord;
                
                not ^= true;
                doNotRecord = true;
                scan(node.getLeftOperand(), p);
                not = oldNot;
                doNotRecord = oldDoNotRecord;
                
                scan(node.getRightOperand(), p);
                
                mergeIntoVariable2State(afterLeft);
                
                subnodesAlreadyProcessed = true;
            }
            
            State left = null;
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
                    
                    if (isVariableElement(e) && !hasDefiniteValue(e)) {
                        variable2State.put((VariableElement) e, State.NULL_HYPOTHETICAL);
                        
                        return null;
                    }
                }
                if (left == State.NULL) {
                    Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getRightOperand()));
                    
                    if (isVariableElement(e) && !hasDefiniteValue(e)) {
                        variable2State.put((VariableElement) e, State.NULL_HYPOTHETICAL);
                        
                        return null;
                    }
                }
            }
            
            if (kind == Kind.NOT_EQUAL_TO) {
                if (right == State.NULL) {
                    Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getLeftOperand()));
                    
                    if (isVariableElement(e) && !hasDefiniteValue(e)) {
                        variable2State.put((VariableElement) e, State.NOT_NULL_HYPOTHETICAL);
                        
                        return null;
                    }
                }
                if (left == State.NULL) {
                    Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getRightOperand()));
                    
                    if (isVariableElement(e) && !hasDefiniteValue(e)) {
                        variable2State.put((VariableElement) e, State.NOT_NULL_HYPOTHETICAL);
                        
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
                variable2State.put((VariableElement) e, State.NOT_NULL_HYPOTHETICAL);
            }
            
            return null;
        }

        @Override
        public State visitConditionalExpression(ConditionalExpressionTree node, Void p) {
            //TODO: handle the condition similarly to visitIf
            Map<VariableElement, State> oldVariable2State = new HashMap<VariableElement, State>(variable2State);
            
            scan(node.getCondition(), p);
            
            State thenSection = scan(node.getTrueExpression(), p);
            
            Map<VariableElement, State> variableStatesAfterThen = variable2State;
            
            variable2State = oldVariable2State;
            
            not = true;
            doNotRecord = true;
            scan(node.getCondition(), p);
            not = false;
            doNotRecord = false;
            
            State elseSection = scan(node.getFalseExpression(), p);
            
            State result = State.collect(thenSection, elseSection);
            
            mergeIntoVariable2State(variableStatesAfterThen);
                
            return result;
        }

        @Override
        public State visitNewClass(NewClassTree node, Void p) {
            scan(node.getEnclosingExpression(), p);
            scan(node.getIdentifier(), p);
            scan(node.getTypeArguments(), p);
            
            for (Tree param : node.getArguments()) {
                Map<VariableElement, State> origVariable2State = variable2State;
                variable2State = new HashMap<VariableElement, State>(variable2State);
                scan(param, p);
                mergeNonHypotheticalVariable2State(origVariable2State);
            }
            
            scan(node.getClassBody(), p);
            
            Element invoked = info.getTrees().getElement(getCurrentPath());

            if (invoked != null && invoked.getKind() == ElementKind.CONSTRUCTOR) {
                recordResumeOnExceptionHandler((ExecutableElement) invoked);
            }
            
            return State.NOT_NULL;
        }

        @Override
        public State visitMethodInvocation(MethodInvocationTree node, Void p) {
            scan(node.getTypeArguments(), p);
            scan(node.getMethodSelect(), p);
            
            for (Tree param : node.getArguments()) {
                Map<VariableElement, State> origVariable2State = variable2State;
                variable2State = new HashMap<VariableElement, State>(variable2State);
                scan(param, p);
                mergeNonHypotheticalVariable2State(origVariable2State);
            }
            
            Element e = info.getTrees().getElement(getCurrentPath());
            
            if (e == null || e.getKind() != ElementKind.METHOD) {
                return State.POSSIBLE_NULL;
            } else {
                recordResumeOnExceptionHandler((ExecutableElement) e);
                
                if (!node.getArguments().isEmpty()) {
                    String ownerFQN = ((TypeElement) e.getEnclosingElement()).getQualifiedName().toString();
                    Tree argument = null;
                    State targetState = null;

                    switch (e.getSimpleName().toString()) {
                        case "assertNotNull": targetState = State.NOT_NULL; break;
                        case "assertNull": targetState = State.NULL; break;
                    }

                    switch (ownerFQN) {
                        case "org.testng.Assert": argument = node.getArguments().get(0); break;
                        case "junit.framework.Assert":
                        case "org.junit.Assert": argument = node.getArguments().get(node.getArguments().size() - 1); break;
                    }

                    Element param = argument != null && targetState != null ? info.getTrees().getElement(new TreePath(getCurrentPath(), argument)) : null;

                    if (param != null && isVariableElement(param)) {
                        variable2State.put((VariableElement) param, targetState);
                    }
                }
            }
            
            return getStateFromAnnotations(info, e);
        }

        @Override
        public State visitIdentifier(IdentifierTree node, Void p) {
            super.visitIdentifier(node, p);
            
            Element e = info.getTrees().getElement(getCurrentPath());

            if (e == null || !isVariableElement(e)) {
                return State.POSSIBLE_NULL;
            }
            
            if (e != null) {
                State s = variable2State.get(e);
                
                if (s != null) {
                    return s;
                }
            }
            
            return getStateFromAnnotations(info, e);
        }

        @Override
        public State visitWhileLoop(WhileLoopTree node, Void p) {
            return handleGeneralizedFor(null, node.getCondition(), null, node.getStatement(), p);
        }

        @Override
        public State visitDoWhileLoop(DoWhileLoopTree node, Void p) {
            return handleGeneralizedFor(Collections.singletonList(node.getStatement()), node.getCondition(), null, node.getStatement(), p);
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
            Map<TypeMirror, Map<VariableElement, State>> oldResumeOnExceptionHandler = resumeOnExceptionHandler;

            resumeOnExceptionHandler = new IdentityHashMap<>();
            
            try {
                variable2State = new HashMap<VariableElement, NPECheck.State>();
                not = false;

                Element current = info.getTrees().getElement(getCurrentPath());

                if (current != null && (current.getKind() == ElementKind.METHOD || current.getKind() == ElementKind.CONSTRUCTOR)) {
                    for (VariableElement var : ((ExecutableElement) current).getParameters()) {
                        variable2State.put(var, getStateFromAnnotations(info, var));
                    }
                }

                while (current != null) {
                    for (VariableElement var : ElementFilter.fieldsIn(current.getEnclosedElements())) {
                        variable2State.put(var, getStateFromAnnotations(info, var));
                    }
                    current = current.getEnclosingElement();
                }

                return super.visitMethod(node, p);
            } finally {
                resumeOnExceptionHandler = oldResumeOnExceptionHandler;
            }
        }

        @Override
        public State visitForLoop(ForLoopTree node, Void p) {
            return handleGeneralizedFor(node.getInitializer(), node.getCondition(), node.getUpdate(), node.getStatement(), p);
        }

        @Override
        public State visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
            return handleGeneralizedFor(Arrays.asList(node.getVariable(), node.getExpression()), null, null, node.getStatement(), p);
        }
        
        private State handleGeneralizedFor(Iterable<? extends Tree> initializer, Tree condition, Iterable<? extends Tree> update, Tree statement, Void p) {
            scan(initializer, p);
            
            Map<VariableElement, State> oldVariable2State = new HashMap<VariableElement, State>(variable2State);

            boolean oldNot = not;
            boolean oldDoNotRecord = doNotRecord;
            
            not = true;
            doNotRecord = true;
            
            scan(condition, p);
            
            not = oldNot;
            
            Map<VariableElement, State> negConditionVariable2State = new HashMap<VariableElement, State>(variable2State);
            
                
            if (!oldDoNotRecord) {
                variable2State = new HashMap<VariableElement, State>(oldVariable2State);
                
                scan(condition, p);
                scan(statement, p);
                scan(update, p);
                
                mergeIntoVariable2State(oldVariable2State);
            } else {
                variable2State = oldVariable2State;
            }
        
            
            doNotRecord = oldDoNotRecord;
            
            scan(condition, p);
            scan(statement, p);
            scan(update, p);
            
            mergeIntoVariable2State(negConditionVariable2State);
            
            return null;
        }

        @Override
        public State visitAssert(AssertTree node, Void p) {
            scan(node.getCondition(), p);
            //XXX: todo clear hypothetical, evaluate negation?
            scan(node.getDetail(), p);
            return null;
        }

        @Override
        public State visitArrayAccess(ArrayAccessTree node, Void p) {
            super.visitArrayAccess(node, p);
            return State.POSSIBLE_NULL;
        }
        
        public State visitSwitch(SwitchTree node, Void p) {
            scan(node.getExpression(), null);

            Map<VariableElement, State> origVariable2State = new HashMap<VariableElement, State>(variable2State);

            boolean exhaustive = false;

            for (CaseTree ct : node.getCases()) {
                mergeIntoVariable2State(origVariable2State);

                if (ct.getExpression() == null) {
                    exhaustive = true;
                }

                scan(ct, null);
            }

            if (!exhaustive) {
                mergeIntoVariable2State(origVariable2State);
            }
            
            return null;
        }
        
        public State visitBreak(BreakTree node, Void p) {
            super.visitBreak(node, p);

            StatementTree target = info.getTreeUtilities().getBreakContinueTarget(getCurrentPath());
            
            resumeAfter(target, variable2State);

            variable2State = new HashMap<VariableElement, State>(); //XXX: fields?
            
            return null;
        }
        
        public State visitTry(TryTree node, Void p) {
            Map<TypeMirror, Map<VariableElement, State>> oldResumeOnExceptionHandler = resumeOnExceptionHandler;

            resumeOnExceptionHandler = new IdentityHashMap<>();
            
            try {
            if (node.getFinallyBlock() != null) {
                pendingFinally.add(0, new TreePath(getCurrentPath(), node.getFinallyBlock()));
            }
            
            scan(node.getResources(), null);

            Map<VariableElement, State> oldVariable2State = variable2State;

            variable2State = new HashMap<VariableElement, State>(oldVariable2State);

            scan(node.getBlock(), null);

            HashMap<VariableElement, State> afterBlockVariable2State = new HashMap<VariableElement, State>(variable2State);

            for (CatchTree ct : node.getCatches()) {
                Map<VariableElement, State> variable2StateBeforeCatch = variable2State;

                variable2State = new HashMap<VariableElement, State>(oldVariable2State);

                if (ct.getParameter() != null) {
                    TypeMirror caught = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), ct.getParameter()));
                    List<TypeMirror> caughtExceptions = new ArrayList<TypeMirror>();

                    if (caught != null && caught.getKind() != TypeKind.ERROR) {
                        if (caught.getKind() == TypeKind.UNION) {
                            caughtExceptions.addAll(((UnionType) caught).getAlternatives());
                        } else {
                            caughtExceptions.add(caught);
                        }
                    }
                    
                    for (TypeMirror caughtException : caughtExceptions) {
                        for (Iterator<Entry<TypeMirror, Map<VariableElement, State>>> it = resumeOnExceptionHandler.entrySet().iterator(); it.hasNext();) {
                            Entry<TypeMirror, Map<VariableElement, State>> e = it.next();

                            if (info.getTypes().isSubtype(e.getKey(), caughtException)) {
                                mergeIntoVariable2State(e.getValue());

                                it.remove();
                            }
                        }
                    }
                }
                
                scan(ct, null);

                if (Utilities.exitsFromAllBranchers(info, new TreePath(getCurrentPath(), ct))) {
                    variable2State = variable2StateBeforeCatch;
                } else {
                    mergeIntoVariable2State(variable2StateBeforeCatch);
                }
            }

            if (node.getFinallyBlock() != null) {
                pendingFinally.remove(0);
                mergeIntoVariable2State(oldVariable2State);
                mergeIntoVariable2State(afterBlockVariable2State);

                scan(node.getFinallyBlock(), null);
            }
            } finally {
                Map<TypeMirror, Map<VariableElement, State>> remainingException = resumeOnExceptionHandler;
                resumeOnExceptionHandler = oldResumeOnExceptionHandler;
                for (Entry<TypeMirror, Map<VariableElement, State>> e : remainingException.entrySet()) {
                    recordResumeOnExceptionHandler(e.getKey(), e.getValue());
                }
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
            recordResumeOnExceptionHandler(thrown, variable2State);
        }
        
        private void recordResumeOnExceptionHandler(TypeMirror thrown, Map<VariableElement, State> variable2State) {
            if (thrown == null || thrown.getKind() == TypeKind.ERROR) return;
            
            Map<VariableElement, State> r = resumeOnExceptionHandler.get(thrown);

            if (r == null) {
                resumeOnExceptionHandler.put(thrown, r = new HashMap<>());
            }

            mergeInto(r, variable2State);
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

        private void mergeIntoVariable2State(Map<VariableElement, State> other) {
            mergeInto(variable2State, other);
        }
        
        private void mergeInto(Map<VariableElement, State> target, Map<VariableElement, State> other) {
            for (Entry<VariableElement, State> e : other.entrySet()) {
                State t = e.getValue();
                
                if (target.containsKey(e.getKey())) {
                    State el = target.get(e.getKey());

                    target.put(e.getKey(), State.collect(t, el));
                } else {
                    target.put(e.getKey(), t);
                }
            }
        }
        
        private void mergeHypotheticalVariable2State(Map<VariableElement, State> original) {
            for (Entry<VariableElement, State> e : variable2State.entrySet()) {
                State t = e.getValue();
                
                if (t == State.NULL_HYPOTHETICAL || t == State.NOT_NULL_HYPOTHETICAL) {
                    State originalValue = original.get(e.getKey());
                    e.setValue(originalValue == State.POSSIBLE_NULL || originalValue == null ? State.POSSIBLE_NULL_REPORT : originalValue);
                }
            }
        }
        
        private void mergeNonHypotheticalVariable2State(Map<VariableElement, State> original) {
            Map<VariableElement, State> backup = variable2State;
            
            variable2State = original;
            
            for (Entry<VariableElement, State> e : backup.entrySet()) {
                State t = e.getValue();
                
                if (t  != null && t != State.NOT_NULL_HYPOTHETICAL && t != NULL_HYPOTHETICAL) {
                    variable2State.put(e.getKey(), t);
                }
            }
        }
        
        private boolean hasDefiniteValue(Element el) {
            State s = variable2State.get(el);
            
            return s != null && s.isNotNull();
        }
        
        private boolean isVariableElement(Element ve) {
            return NPECheck.isVariableElement(ctx, ve);
        }
        
        private void clearHypothetical() {
            for (Iterator<Entry<VariableElement, State>> it = variable2State.entrySet().iterator(); it.hasNext();) {
                Entry<VariableElement, State> e = it.next();
                
                if (e.getValue() == State.NOT_NULL_HYPOTHETICAL || e.getValue() == State.NULL_HYPOTHETICAL) {
                    it.remove();
                }
            }
        }
    }
    
    static enum State {
        NULL,
        NULL_HYPOTHETICAL,
        POSSIBLE_NULL,
        POSSIBLE_NULL_REPORT,
        NOT_NULL,
        NOT_NULL_HYPOTHETICAL,
        NOT_NULL_BE_NPE;
        
        public @CheckForNull State reverse() {
            switch (this) {
                case NULL:
                    return NOT_NULL;
                case NULL_HYPOTHETICAL:
                    return NOT_NULL_HYPOTHETICAL;
                case POSSIBLE_NULL:
                case POSSIBLE_NULL_REPORT:
                    return this;
                case NOT_NULL:
                case NOT_NULL_BE_NPE:
                    return NULL;
                case NOT_NULL_HYPOTHETICAL:
                    return NULL_HYPOTHETICAL;
                default: throw new IllegalStateException();
            }
        }
        
        public boolean isNotNull() {
            return this == NOT_NULL || this == NOT_NULL_BE_NPE || this == NOT_NULL_HYPOTHETICAL;
        }
        
        public static State collect(State s1, State s2) {
            if (s1 == s2) return s1;
            if (s1 == NULL || s2 == NULL || s1 == NULL_HYPOTHETICAL || s2 == NULL_HYPOTHETICAL) return POSSIBLE_NULL_REPORT;
            if (s1 == POSSIBLE_NULL_REPORT || s2 == POSSIBLE_NULL_REPORT) return POSSIBLE_NULL_REPORT;
            if (s1 != null && s2 != null && s1.isNotNull() && s2.isNotNull()) return NOT_NULL;
            
            return POSSIBLE_NULL;
        }
    }
    
    private static boolean isVariableElement(HintContext ctx, Element ve) {
        return ve != null && (ctx.getPreferences().getBoolean(KEY_ENABLE_FOR_FIELDS, DEF_ENABLE_FOR_FIELDS) ? VARIABLE_ELEMENT_FIELDS : VARIABLE_ELEMENT_NO_FIELDS).contains(ve.getKind());
    }
        
    private static final Set<ElementKind> VARIABLE_ELEMENT_NO_FIELDS = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
    private static final Set<ElementKind> VARIABLE_ELEMENT_FIELDS = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
    
}
