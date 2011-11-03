/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.modules.java.hints.introduce.Flow;
import org.netbeans.modules.java.hints.introduce.Flow.Cancel;
import org.netbeans.modules.java.hints.introduce.Flow.FlowResult;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerTreeKind;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class UnusedAssignmentOrBranch {
    
    private static final String UNUSED_ASSIGNMENT_ID = "org.netbeans.modules.java.hints.bugs.UnusedAssignmentOrBranch.unusedAssignment";
    private static final String DEAD_BRANCH_ID = "org.netbeans.modules.java.hints.bugs.UnusedAssignmentOrBranch.deadBranch";

    private static FlowResult runFlow(final HintContext ctx) {
        FlowResult flow = Flow.assignmentsForUse(ctx.getInfo(), new TreePath(ctx.getInfo().getCompilationUnit()), new Cancel() {
            @Override
            public boolean isCanceled() {
                return ctx.isCanceled();
            }
        });

        if (flow == null || ctx.isCanceled()) return null;
        else return flow;
    }

    private static final Set<ElementKind> LOCAL_VARIABLES = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);

    @Hint(category="bugs", id=UNUSED_ASSIGNMENT_ID, options={Options.QUERY})
    @TriggerTreeKind(Tree.Kind.COMPILATION_UNIT)
    public static List<ErrorDescription> unusedAssignment(final HintContext ctx) {
        final String unusedAssignmentLabel = NbBundle.getMessage(UnusedAssignmentOrBranch.class, "LBL_UNUSED_ASSIGNMENT_LABEL");
        final CompilationInfo info = ctx.getInfo();
        FlowResult flow = runFlow(ctx);

        if (flow == null) return null;

        final Set<Tree> usedAssignments = new HashSet<Tree>();

        for (Iterable<? extends TreePath> i : flow.getAssignmentsForUse().values()) {
            for (TreePath tp : i) {
                if (tp == null) continue;

                usedAssignments.add(tp.getLeaf());
            }
        }

        final Set<Element> usedVariables = new HashSet<Element>();

        new CancellableTreePathScanner<Void, Void>() {
            @Override public Void visitAssignment(AssignmentTree node, Void p) {
                Element var = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));

                if (var != null && LOCAL_VARIABLES.contains(var.getKind()) && !usedAssignments.contains(node.getExpression())) {
                    scan(node.getExpression(), null);
                    return null;
                }

                return super.visitAssignment(node, p);
            }
            @Override public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
                Element var = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));

                if (var != null && LOCAL_VARIABLES.contains(var.getKind()) && !usedAssignments.contains(node.getExpression())) {
                    scan(node.getExpression(), null);
                    return null;
                }

                return super.visitCompoundAssignment(node, p);
            }
            @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                Element var = info.getTrees().getElement(getCurrentPath());

                if (var != null && LOCAL_VARIABLES.contains(var.getKind())) {
                    usedVariables.add(var);
                }
                return super.visitIdentifier(node, p);
            }
            @Override protected boolean isCanceled() {
                return ctx.isCanceled();
            }
        }.scan(info.getCompilationUnit(), null);

        if (ctx.isCanceled()) return null;

        final List<ErrorDescription> result = new ArrayList<ErrorDescription>();

        new CancellableTreePathScanner<Void, Void>() {
            @Override public Void visitAssignment(AssignmentTree node, Void p) {
                Element var = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));

                if (var != null && LOCAL_VARIABLES.contains(var.getKind()) && !usedAssignments.contains(node.getExpression()) && usedVariables.contains(var)) {
                    unusedValue(node.getExpression());
                }
                return super.visitAssignment(node, p);
            }
            @Override public Void visitVariable(VariableTree node, Void p) {
                Element var = info.getTrees().getElement(getCurrentPath());

                if (var != null && LOCAL_VARIABLES.contains(var.getKind()) && node.getInitializer() != null && !usedAssignments.contains(node.getInitializer()) && usedVariables.contains(var)) {
                    unusedValue(node.getInitializer());
                }
                return super.visitVariable(node, p);
            }
            @Override protected boolean isCanceled() {
                return ctx.isCanceled();
            }
            private void unusedValue(Tree t) {
                result.add(ErrorDescriptionFactory.forTree(ctx, t, unusedAssignmentLabel));
            }

        }.scan(info.getCompilationUnit(), null);

        return result;
    }

    @Hint(category="bugs", id=DEAD_BRANCH_ID, options={Options.NO_BATCH, Options.QUERY})
    @TriggerTreeKind(Tree.Kind.COMPILATION_UNIT)
    public static List<ErrorDescription> deadBranch(HintContext ctx) {
        String deadBranchLabel = NbBundle.getMessage(UnusedAssignmentOrBranch.class, "LBL_DEAD_BRANCH");
        FlowResult flow = runFlow(ctx);

        if (flow == null) return null;

        List<ErrorDescription> result = new ArrayList<ErrorDescription>();

        for (Tree t : flow.getDeadBranches()) {
            if (ctx.isCanceled()) return null;
            result.add(ErrorDescriptionFactory.forTree(ctx, t, deadBranchLabel));
        }

        return result;
    }

}
