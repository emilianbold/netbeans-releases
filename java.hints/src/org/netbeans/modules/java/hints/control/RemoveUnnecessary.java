/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.control;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Messages({
    "DN_org.netbeans.modules.java.hints.RemoveUnnecessaryReturn=Remove Unnecessary Return Statement",
    "DESC_org.netbeans.modules.java.hints.RemoveUnnecessaryReturn=Remove Unnecessary Return Statement",
    "ERR_UnnecessaryReturnStatement=Unnecessary return statement",
    "FIX_UnnecessaryReturnStatement=Remove unnecessary return statement",
    "DN_RemoveUnnecessaryContinue=Remove Unnecessary Continue Statement",
    "DESC_RemoveUnnecessaryContinue=Remove Unnecessary Continue Statement",
    "ERR_UnnecessaryContinueStatement=Unnecessary continue statement",
    "FIX_UnnecessaryContinueStatement=Remove unnecessary continue statement",
    "DN_RemoveUnnecessaryContinueLabel=Remove Unnecessary Label in continue",
    "DESC_RemoveUnnecessaryContinueLabel=Remove Unnecessary Label in continue statement",
    "ERR_UnnecessaryContinueStatementLabel=Unnecessary label in continue",
    "FIX_UnnecessaryContinueStatementLabel=Remove unnecessary label from continue",
    "DN_RemoveUnnecessaryBreakLabel=Remove Unnecessary Label in break",
    "DESC_RemoveUnnecessaryBreakLabel=Remove Unnecessary Label in break statement",
    "ERR_UnnecessaryBreakStatementLabel=Unnecessary label in break",
    "FIX_UnnecessaryBreakStatementLabel=Remove unnecessary label from break"
})
public class RemoveUnnecessary {

    @Hint(id="org.netbeans.modules.java.hints.RemoveUnnecessaryReturn", displayName = "#DN_org.netbeans.modules.java.hints.RemoveUnnecessaryReturn", description = "#DESC_org.netbeans.modules.java.hints.RemoveUnnecessaryReturn", category="general", suppressWarnings="UnnecessaryReturnStatement")
    @TriggerPattern("return $val$;")
    public static ErrorDescription unnecessaryReturn(HintContext ctx) {
        return unnecessaryReturnContinue(ctx, null, "UnnecessaryReturnStatement");
    }
    
    @Hint(displayName="#DN_RemoveUnnecessaryContinue", description="#DESC_RemoveUnnecessaryContinue", category="general", suppressWarnings="UnnecessaryContinue")
    @TriggerPattern("continue $val$;")
    public static ErrorDescription unnecessaryContinue(HintContext ctx) {
        return unnecessaryReturnContinue(ctx, ctx.getInfo().getTreeUtilities().getBreakContinueTarget(ctx.getPath()), "UnnecessaryContinueStatement");
    }
    
    private static ErrorDescription unnecessaryReturnContinue(HintContext ctx, StatementTree targetLoop, String key) {
        TreePath tp = ctx.getPath();

        OUTER: while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
            Tree current = tp.getLeaf();
            List<? extends StatementTree> statements;

            tp = tp.getParentPath();

            switch (tp.getLeaf().getKind()) {
                case METHOD: {
                    if (targetLoop != null) return null; //TODO: unnecessary continue - can happen?
                    MethodTree mt = (MethodTree) tp.getLeaf();

                    if (mt.getReturnType() == null) {
                        if (mt.getName().contentEquals("<init>"))
                            break OUTER;//constructor
                        else
                            return null; //a method without a return type - better ignore
                    }
                    
                    TypeMirror tm = ctx.getInfo().getTrees().getTypeMirror(new TreePath(tp, mt.getReturnType()));

                    if (tm == null || tm.getKind() != TypeKind.VOID) return null;
                    break OUTER;
                }
                case LAMBDA_EXPRESSION: {
                    if (targetLoop != null) return null; //TODO: unnecessary continue - can happen?
                    TypeMirror functionalType = ctx.getInfo().getTrees().getTypeMirror(tp);
                    if (functionalType == null || functionalType.getKind() != TypeKind.DECLARED) return null; //unknown, ignore
                    ExecutableType descriptorType = ctx.getInfo().getTypeUtilities().getDescriptorType((DeclaredType) functionalType);
                    TypeMirror returnType = descriptorType != null ? descriptorType.getReturnType() : null;

                    if (returnType == null || returnType.getKind() != TypeKind.VOID) return null;
                    break OUTER;
                }
                case BLOCK: statements = ((BlockTree) tp.getLeaf()).getStatements(); break;
                case CASE: {
                    if (tp.getParentPath().getLeaf().getKind() == Kind.SWITCH) {
                        List<? extends CaseTree> cases = ((SwitchTree) tp.getParentPath().getLeaf()).getCases();
                        List<StatementTree> locStatements = new ArrayList<StatementTree>();

                        for (int i = cases.indexOf(tp.getLeaf()); i < cases.size(); i++) {
                            locStatements.addAll(cases.get(i).getStatements());
                        }

                        statements = locStatements;
                    } else {
                        //???
                        statements = ((CaseTree) tp.getLeaf()).getStatements();
                    }
                    break;
                }
                case DO_WHILE_LOOP:
                case ENHANCED_FOR_LOOP:
                case FOR_LOOP:
                case WHILE_LOOP:
                    if (tp.getLeaf() != targetLoop) return null;
                    else break OUTER;
                case TRY:
                    if (((TryTree) tp.getLeaf()).getFinallyBlock() == current) return null;
                default: continue OUTER;
            }

            assert !statements.isEmpty();

            int i = statements.indexOf(current);

            if (i == (-1)) {
                //XXX: should not happen?
                return null;
            }

            while (i + 1 < statements.size()) {
                StatementTree next = statements.get(i + 1);

                if (next.getKind() == Kind.EMPTY_STATEMENT) {
                    i++;
                    continue;
                }

                if (next.getKind() == Kind.BLOCK) {
                    statements = ((BlockTree) next).getStatements();
                    i = -1;
                    continue;
                }

                if (next.getKind() == Kind.BREAK) {
                    StatementTree target = ctx.getInfo().getTreeUtilities().getBreakContinueTarget(new TreePath(tp, next));
                    
                    if (target == null) return null;
                    
                    tp = TreePath.getPath(ctx.getInfo().getCompilationUnit(), target);
                    continue OUTER;
                }

                return null;
            }
        }

        String displayName = NbBundle.getMessage(RemoveUnnecessary.class, "ERR_" + key);
        String fixDisplayName = NbBundle.getMessage(RemoveUnnecessary.class, "FIX_" + key);
        
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, JavaFixUtilities.removeFromParent(ctx, fixDisplayName, ctx.getPath()));
    }

    @Hint(id="unnecessaryContinueLabel", displayName="#DN_RemoveUnnecessaryContinueLabel", description="#DESC_RemoveUnnecessaryContinueLabel", category="general", suppressWarnings="UnnecessaryLabelOnContinueStatement")
    @TriggerPattern("continue $val;")
    public static ErrorDescription unnecessaryContinueLabel(HintContext ctx) {
        return unnecessaryLabel(ctx, false);
    }
    
    @Hint(id="unnecessaryBreakLabel", displayName="#DN_RemoveUnnecessaryBreakLabel", description="#DESC_RemoveUnnecessaryBreakLabel", category="general", suppressWarnings="UnnecessaryLabelOnBreakStatement")
    @TriggerPattern("break $val;")
    public static ErrorDescription unnecessaryBreakLabel(HintContext ctx) {
        return unnecessaryLabel(ctx, true);
    }
    
    private static ErrorDescription unnecessaryLabel(HintContext ctx, boolean brk) {
        TreePath loop = ctx.getPath();
        
        while (loop != null && !LOOP_KINDS.contains(loop.getLeaf().getKind()) && (!brk || loop.getLeaf().getKind() != Kind.SWITCH)) {
            loop = loop.getParentPath();
        }
        
        if (loop == null) return null;
        
        if (ctx.getInfo().getTreeUtilities().getBreakContinueTarget(ctx.getPath()) != loop.getParentPath().getLeaf()) return null;
        
        Fix fix = JavaFixUtilities.rewriteFix(ctx, brk ? Bundle.FIX_UnnecessaryBreakStatementLabel() : Bundle.FIX_UnnecessaryContinueStatementLabel(), ctx.getPath(), brk ? "break;" : "continue;");
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), brk ? Bundle.ERR_UnnecessaryBreakStatementLabel() : Bundle.ERR_UnnecessaryContinueStatementLabel(), fix);
    }
    
    private static final Set<Kind> LOOP_KINDS = EnumSet.of(Kind.DO_WHILE_LOOP, Kind.ENHANCED_FOR_LOOP, Kind.FOR_LOOP, Kind.WHILE_LOOP);
}
