/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.ArithmeticUtilities;
import org.netbeans.modules.java.hints.bugs.NPECheck;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.MatcherUtilities;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.jdk.ConvertToStringSwitch", description = "#DESC_org.netbeans.modules.java.hints.jdk.ConvertToStringSwitch", category="rules15", suppressWarnings="ConvertToStringSwitch")
@NbBundle.Messages({
    "# {0} - string literal value",
    "TEXT_ChainedIfContainsSameValues=The string value `{0}'' used in String comparison appears earlier in the chained if-else-if statement. This condition never evaluates to true",
    "TEXT_ConvertToSwitch=Convert to switch",
    "# initial label for breaking out of the innermost loop",
    "LABEL_OuterGeneratedLabelInitial=OUTER",
    "# template for generated label names, must form a valid Java identifiers",
    "# {0} - unique integer",
    "LABEL_OuterGeneratedLabel=OUTER_{0}"
})
public class ConvertToStringSwitch {

    static final boolean DEF_ALSO_EQ = true;
    
    @BooleanOption(displayName = "#LBL_org.netbeans.modules.java.hints.jdk.ConvertToStringSwitch.KEY_ALSO_EQ", tooltip = "#TP_org.netbeans.modules.java.hints.jdk.ConvertToStringSwitch.KEY_ALSO_EQ", defaultValue=DEF_ALSO_EQ)
    static final String KEY_ALSO_EQ = "also-equals";
    
    private static final String[] INIT_PATTERNS = {
        "$c1.equals($c2)",
        "$c1.contentEquals($c2)"
    };

    private static final String[] INIT_PATTERNS_EQ = {
        "$c1 == $c2"
    };

    private static final String[] PATTERNS = {
        "$var.equals($constant)",
        "$constant.equals($var)",
        "$var.contentEquals($constant)",
        "$constant.contentEquals($var)"
    };
    
    private static final String[] PATTERNS_EQ = {
        "$var == $constant",
        "$constant == $var",
    };

    @TriggerPattern(value="if ($cond) $body; else $else;")
    public static List<ErrorDescription> hint(HintContext ctx) {
        if (   ctx.getPath().getParentPath().getLeaf().getKind() == Kind.IF
            || ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_7) < 0) {
            return null;
        }

        TypeElement jlString = ctx.getInfo().getElements().getTypeElement("java.lang.String"); // NOI18N

        if (jlString == null) {
            return null;
        }
        
        List<CatchDescription<TreePathHandle>> literal2Statement = new ArrayList<CatchDescription<TreePathHandle>>();
        TreePathHandle defaultStatement = null;

        Iterable<? extends TreePath> conds = linearizeOrs(ctx.getVariables().get("$cond")); // NOI18N
        Iterator<? extends TreePath> iter = conds.iterator();
        TreePath first = iter.next();
        TreePath variable = null;
        Set<String> seenLiterals = new HashSet<String>();
        Map<TreePath, String> duplicateLiterals = Collections.emptyMap();
        
        // if .equals is called on the variable, it is definitively not null.
        boolean[] varNotNull = new boolean[] { false };
        TreePath lastCondition = null;
        
        Collection<String> initPatterns = new ArrayList<String>(INIT_PATTERNS.length + INIT_PATTERNS_EQ.length);

        initPatterns.addAll(Arrays.asList(INIT_PATTERNS));

        if (ctx.getPreferences().getBoolean(KEY_ALSO_EQ, DEF_ALSO_EQ)) {
            initPatterns.addAll(Arrays.asList(INIT_PATTERNS_EQ));
        }
        
        for (String initPattern : initPatterns) {
            if (MatcherUtilities.matches(ctx, first, initPattern, true)) {
                lastCondition = first;
                TreePath c1 = ctx.getVariables().get("$c1"); // NOI18N
                TreePath c2 = ctx.getVariables().get("$c2"); // NOI18N
                TreePath body = ctx.getVariables().get("$body"); // NOI18N
                List<TreePathHandle> literals = new LinkedList<TreePathHandle>();

                Object c = ArithmeticUtilities.compute(ctx.getInfo(), c1, true, true);
                if (c instanceof String) {
                    literals.add(TreePathHandle.create(c1, ctx.getInfo()));
                    variable = c2;
                } else {
                    // c1.equals -> c1 is not null
                    c = ArithmeticUtilities.compute(ctx.getInfo(), c2, true, true);
                    if (c instanceof String) {
                        varNotNull[0] = true;
                        literals.add(TreePathHandle.create(c2, ctx.getInfo()));
                        variable = c1;
                    } else {
                        return null;
                    }
                }
                seenLiterals.add((String)c);

                TypeMirror varType = ctx.getInfo().getTrees().getTypeMirror(variable);

                if (!ctx.getInfo().getTypes().isSameType(varType, jlString.asType())) {
                    return null;
                }
                
                ctx.getVariables().put("$var", variable); //XXX: hack

                while (iter.hasNext()) {
                    TreePath lt = isStringComparison(ctx, iter.next(), varNotNull);
                    
                    if (lt == null) {
                        return null;
                    }
                    Object o = ArithmeticUtilities.compute(ctx.getInfo(), lt, true, true);
                    if (!(o instanceof String)) {
                        return null;
                    }
                    if (seenLiterals.contains((String)o)) {
                        if (duplicateLiterals.isEmpty()) {
                            duplicateLiterals = new LinkedHashMap<TreePath, String>(3);
                        }
                        duplicateLiterals.put(lt, (String)o);
                    }
                    literals.add(TreePathHandle.create(lt, ctx.getInfo()));
                }

                literal2Statement.add(new CatchDescription<TreePathHandle>(literals, TreePathHandle.create(body, ctx.getInfo())));
                break;
            }
        }

        if (variable == null) {
            return null;
        }
        
        TreePath tp = ctx.getVariables().get("$else"); // NOI18N

        while (true) {
            if (tp.getLeaf().getKind() == Kind.IF) {
                IfTree it = (IfTree) tp.getLeaf();
                List<TreePathHandle> literals = new LinkedList<TreePathHandle>();
                lastCondition = new TreePath(tp, it.getCondition());
                for (TreePath cond : linearizeOrs(lastCondition)) {
                    TreePath lt = isStringComparison(ctx, cond, varNotNull);

                    if (lt == null) {
                        return null;
                    }
                    Object o = ArithmeticUtilities.compute(ctx.getInfo(), lt, true, true);
                    if (!ArithmeticUtilities.isRealValue(o) || !(o instanceof String)) {
                        return null;
                    }
                    if (seenLiterals.contains((String)o)) {
                        if (duplicateLiterals.isEmpty()) {
                            duplicateLiterals = new LinkedHashMap<TreePath, String>(3);
                        }
                        duplicateLiterals.put(lt, (String)o);
                    }

                    literals.add(TreePathHandle.create(lt, ctx.getInfo()));
                }

                literal2Statement.add(new CatchDescription<TreePathHandle>(literals, TreePathHandle.create(new TreePath(tp, it.getThenStatement()), ctx.getInfo())));
                
                if (it.getElseStatement() == null) {
                    break;
                }

                tp = new TreePath(tp, it.getElseStatement());
            } else {
                defaultStatement = TreePathHandle.create(tp, ctx.getInfo());
                break;
            }
        }

        if (literal2Statement.size() <= 1) {
            return null;
        }
        
        if (!duplicateLiterals.isEmpty()) {
            List<ErrorDescription> descs = new ArrayList<>(duplicateLiterals.size());
            seenLiterals.clear();
            for (Map.Entry<TreePath, String> en : duplicateLiterals.entrySet()) {
                String lit = en.getValue();
                // do not report a single value more than once; confusing.
                if (!seenLiterals.add(lit)) {
                    continue;
                }
                TreePath t = en.getKey();
                descs.add(ErrorDescriptionFactory.forTree(ctx, t, Bundle.TEXT_ChainedIfContainsSameValues(lit)));
            }
            return descs;
        }

        Fix convert = new ConvertToSwitch(ctx.getInfo(),
                                 ctx.getPath(),
                                 TreePathHandle.create(variable, ctx.getInfo()),
                                 literal2Statement,
                                 defaultStatement, varNotNull[0]).toEditorFix();
        ErrorDescription ed = ErrorDescriptionFactory.forName(ctx,
                                                              ctx.getPath(),
                                                              Bundle.TEXT_ConvertToSwitch(),
                                                              convert);

        return Collections.singletonList(ed);
    }

    private static TreePath isStringComparison(HintContext ctx, TreePath tp, boolean[] varConst) {
        Tree leaf = tp.getLeaf();

        while (leaf.getKind() == Kind.PARENTHESIZED) {
            tp = new TreePath(tp, ((ParenthesizedTree) leaf).getExpression());
            leaf = tp.getLeaf();
        }

        Collection<String> patterns = new ArrayList<String>(PATTERNS.length + PATTERNS_EQ.length);

        patterns.addAll(Arrays.asList(PATTERNS));

        if (ctx.getPreferences().getBoolean(KEY_ALSO_EQ, DEF_ALSO_EQ)) {
            patterns.addAll(Arrays.asList(PATTERNS_EQ));
        }
        int i = -1;
        assert PATTERNS.length == 4; // the cycle counts with specific positions
        for (String patt : patterns) {
            ++i;
            ctx.getVariables().remove("$constant"); // NOI18N

            if (!MatcherUtilities.matches(ctx, tp, patt, true))
                continue;
            if (i % 2 == 0 && i < 4) {
                varConst[0] = true;
            }
            return ctx.getVariables().get("$constant"); // NOI18N
        }

        return null;
    }

    private static Iterable<? extends TreePath> linearizeOrs(TreePath cond) {
        List<TreePath> result = new LinkedList<TreePath>();

        while (cond.getLeaf().getKind() == Kind.CONDITIONAL_OR || cond.getLeaf().getKind() == Kind.PARENTHESIZED) {
            if (cond.getLeaf().getKind() == Kind.PARENTHESIZED) {
                cond = new TreePath(cond, ((ParenthesizedTree) cond.getLeaf()).getExpression());
                continue;
            }
            
            BinaryTree bt = (BinaryTree) cond.getLeaf();
            
            result.add(new TreePath(cond, bt.getRightOperand()));
            cond = new TreePath(cond, bt.getLeftOperand());
        }

        result.add(cond);

        Collections.reverse(result);

        return result;
    }

    private static final class ConvertToSwitch extends JavaFix {

        private final TreePathHandle value;
        private final List<CatchDescription<TreePathHandle>> literal2Statement;
        private final TreePathHandle defaultStatement;
        private boolean varNotNull;

        public ConvertToSwitch(CompilationInfo info, TreePath create, TreePathHandle value, List<CatchDescription<TreePathHandle>> literal2Statement, TreePathHandle defaultStatement, 
                boolean varNotNull) {
            super(info, create);
            this.value = value;
            this.literal2Statement = literal2Statement;
            this.defaultStatement = defaultStatement;
            this.varNotNull = varNotNull;
        }

        public String getText() {
            return NbBundle.getMessage(ConvertToStringSwitch.class, "FIX_ConvertToStringSwitch"); // NOI18N
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy copy = ctx.getWorkingCopy();
            TreePath it = ctx.getPath();
            TreeMaker make = copy.getTreeMaker();
            List<CaseTree> cases = new LinkedList<CaseTree>();
            List<CatchDescription<TreePath>> resolved = new ArrayList<CatchDescription<TreePath>>(ConvertToSwitch.this.literal2Statement.size() + 1);
            Map<TreePath, Set<Name>> catch2Declared = new IdentityHashMap<TreePath, Set<Name>>();
            Map<TreePath, Set<Name>> catch2Used = new IdentityHashMap<TreePath, Set<Name>>();
            Map<BreakTree, StatementTree> break2Target = new IdentityHashMap<BreakTree, StatementTree>();
            
            for (CatchDescription<TreePathHandle> d : ConvertToSwitch.this.literal2Statement) {
                TreePath s = d.path.resolve(copy);

                if (s == null) {
                    return ;
                }

                resolved.add(new CatchDescription<TreePath>(d.literals, s));
                catch2Declared.put(s, declaredVariables(s));
                catch2Used.put(s, usedVariables(copy, s, break2Target));
            }

            if (defaultStatement != null) {
                TreePath s = defaultStatement.resolve(copy);

                if (s == null) {
                    return ;
                }

                resolved.add(new CatchDescription<TreePath>(null, s));
                catch2Declared.put(s, declaredVariables(s));
                catch2Used.put(s, usedVariables(copy, s, break2Target));
            }

            for (CatchDescription<TreePath> d : resolved) {
                if (addCase(copy, d, cases, catch2Declared, catch2Used)) {
                    return;
                }
            }

            TreePath value = ConvertToSwitch.this.value.resolve(copy);

            if (!varNotNull) {
                varNotNull = NPECheck.isSafeToDereference(copy, value);
            }
            
            
            SwitchTree s = make.Switch((ExpressionTree) value.getLeaf(), cases);

            Utilities.copyComments(copy, it.getLeaf(), s, true);
            
            Tree nue = s;
            
            if (!varNotNull) {
                nue = make.If(
                        make.Parenthesized(
                            make.Binary(Kind.NOT_EQUAL_TO, make.Literal(null), (ExpressionTree)value.getLeaf())
                        ),
                    s, null
                );
            }
            copy.rewrite(it.getLeaf(), nue); //XXX

            TreePath topLevelMethod = Utilities.findTopLevelBlock(it);
            final Set<String> seenLabels = new HashSet<String>();

            new TreeScanner<Void, Void>() {
                @Override public Void visitLabeledStatement(LabeledStatementTree node, Void p) {
                    seenLabels.add(node.getLabel().toString());
                    return super.visitLabeledStatement(node, p);
                }
            }.scan(topLevelMethod.getLeaf(), null);

            Map<StatementTree, String> labels = new IdentityHashMap<StatementTree, String>();

            for (Entry<BreakTree, StatementTree> e : break2Target.entrySet()) {
                String label = labels.get(e.getValue());

                if (label == null) {
                    labels.put(e.getValue(), label = computeLabel(seenLabels));
                    copy.rewrite(e.getValue(), make.LabeledStatement(label, e.getValue()));
                }
                
                copy.rewrite(e.getKey(), make.Break(label));
            }
        }

        private static String computeLabel(Set<String> labels) {
            int index = 0;
            String label = Bundle.LABEL_OuterGeneratedLabelInitial();

            while (labels.contains(label)) {
                label = Bundle.LABEL_OuterGeneratedLabel(++index);
            }

            labels.add(label);

            return label;
        }

        private boolean addCase(WorkingCopy copy, CatchDescription<TreePath> desc, List<CaseTree> cases, Map<TreePath, Set<Name>> catch2Declared, Map<TreePath, Set<Name>> catch2Used) {
            TreeMaker make = copy.getTreeMaker();
            List<StatementTree> statements = new LinkedList<StatementTree>();
            Tree then = desc.path.getLeaf();

            if (then.getKind() == Kind.BLOCK) {
                Set<Name> currentDeclared = catch2Declared.get(desc.path);
                boolean keepBlock = false;

                for (Entry<TreePath, Set<Name>> e : catch2Declared.entrySet()) {
                    if (e.getKey() == desc.path) continue;
                    if (!Collections.disjoint(currentDeclared, e.getValue())) {
                        keepBlock = true;
                        break;
                    }
                }

                if (!keepBlock) {
                    for (Entry<TreePath, Set<Name>> e : catch2Used.entrySet()) {
                        if (e.getKey() == desc.path) continue;
                        if (!Collections.disjoint(currentDeclared, e.getValue())) {
                            keepBlock = true;
                            break;
                        }
                    }
                }

                boolean exitsFromAllBranches = false;

                for (Tree st : ((BlockTree) then).getStatements()) {
                    exitsFromAllBranches |= Utilities.exitsFromAllBranchers(copy, new TreePath(desc.path, st));
                }

                BlockTree block = (BlockTree) then;

                if (keepBlock) {
                    if (!exitsFromAllBranches) {
                        statements.add(make.addBlockStatement(block, make.Break(null)));
                    } else {
                        statements.add(block);
                    }
                } else {
                    statements.addAll(block.getStatements());
                    if (!exitsFromAllBranches) {
                        statements.add(make.Break(null));
                    }
                }
            } else {
                statements.add((StatementTree) then);
                if (!Utilities.exitsFromAllBranchers(copy, desc.path)) {
                    statements.add(make.Break(null));
                }
            }

            if (desc.literals == null) {
                cases.add(make.Case(null, statements));

                return false;
            }
            
            for (Iterator<TreePathHandle> it = desc.literals.iterator(); it.hasNext(); ) {
                TreePathHandle tph = it.next();
                TreePath lit = tph.resolve(copy);

                if (lit == null) {
                    //XXX: log
                    return true;
                }

                List<StatementTree> body = it.hasNext() ? Collections.<StatementTree>emptyList() : statements;

                cases.add(make.Case((ExpressionTree) lit.getLeaf(), body));
            }

            return false;
        }

        private Set<Name> declaredVariables(TreePath where) {
            Set<Name> result = new HashSet<Name>();
            Iterable<? extends Tree> statements;

            if (where.getLeaf().getKind() == Kind.BLOCK) {
                statements = ((BlockTree) where.getLeaf()).getStatements();
            } else {
                statements = Collections.singletonList(where.getLeaf());
            }

            for (Tree t : statements) {
                if (t.getKind() == Kind.VARIABLE) {
                    result.add(((VariableTree) t).getName());
                }
            }

            return result;
        }

        private Set<Name> usedVariables(final CompilationInfo info, TreePath where, final Map<BreakTree, StatementTree> break2Target) {
            final Set<Name> result = new HashSet<Name>();
            final Set<Element> declared = new HashSet<Element>();
            final Set<Tree> above = new HashSet<Tree>();

            for (Tree t : where) {
                above.add(t);
            }

            new TreePathScanner<Void, Void>() {
                @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                    if (declared.contains(info.getTrees().getElement(getCurrentPath())))
                        return null;
                    result.add(node.getName());
                    return super.visitIdentifier(node, p);
                }
                @Override public Void visitVariable(VariableTree node, Void p) {
                    declared.add(info.getTrees().getElement(getCurrentPath()));
                    return super.visitVariable(node, p);
                }
                @Override
                public Void visitBreak(BreakTree node, Void p) {
                    if (node.getLabel() == null) {
                        StatementTree target = info.getTreeUtilities().getBreakContinueTarget(getCurrentPath());

                        if (above.contains(target)) {
                            break2Target.put(node, target);
                        }
                    }
                    return super.visitBreak(node, p);
                }
            }.scan(where, null);

            return result;
        }
    }

    private static final class CatchDescription<T> {
        private final @NullAllowed Iterable<TreePathHandle> literals;
        private final @NonNull T path;

        public CatchDescription(Iterable<TreePathHandle> literals, T path) {
            this.literals = literals;
            this.path = path;
        }
    }

}
