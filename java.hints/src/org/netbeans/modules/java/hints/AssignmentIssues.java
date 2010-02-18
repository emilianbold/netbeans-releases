/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPatterns;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerTreeKind;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class AssignmentIssues {

    @Hint(category = "assignment_issues", enabled = false, suppressWarnings = "AssignmentToForLoopParameter") //NOI18N
    @TriggerPatterns({
        @TriggerPattern(value = "for ($paramType $param = $init; $expr; $update) $statement;"), //NOI18N
        @TriggerPattern(value = "for ($paramType $param : $expr) $statement;") //NOI18N
    })
    public static List<ErrorDescription> assignmentToForLoopParam(HintContext context) {
        final Trees trees = context.getInfo().getTrees();
        final TreePath paramPath = context.getVariables().get("$param"); //NOI18N
        final Element param = trees.getElement(paramPath);
        if (param == null || param.getKind() != ElementKind.LOCAL_VARIABLE) {
            return null;
        }
        final TreePath stat = context.getVariables().get("$statement"); //NOI18N
        final List<TreePath> paths = new LinkedList<TreePath>();
        new AssignmentFinder(trees, param).scan(stat, paths);
        final List<ErrorDescription> ret = new ArrayList<ErrorDescription>(paths.size());
        for (TreePath path : paths) {
            ret.add(ErrorDescriptionFactory.forTree(context, path, NbBundle.getMessage(AssignmentIssues.class, "MSG_AssignmentToForLoopParam", param.getSimpleName()))); //NOI18N
        }
        return ret;
    }

    @Hint(category = "assignment_issues", enabled = false, suppressWarnings = "AssignmentToCatchBlockParameter") //NOI18N
    @TriggerTreeKind(Kind.CATCH)
    public static List<ErrorDescription> assignmentToCatchBlockParameter(HintContext context) {
        final Trees trees = context.getInfo().getTrees();
        final TreePath catchPath = context.getPath();
        final Element param = trees.getElement(TreePath.getPath(catchPath, ((CatchTree) catchPath.getLeaf()).getParameter()));
        if (param == null || param.getKind() != ElementKind.EXCEPTION_PARAMETER) {
            return null;
        }
        final TreePath block = TreePath.getPath(catchPath, ((CatchTree) catchPath.getLeaf()).getBlock());
        final List<TreePath> paths = new LinkedList<TreePath>();
        new AssignmentFinder(trees, param).scan(block, paths);
        final List<ErrorDescription> ret = new ArrayList<ErrorDescription>(paths.size());
        for (TreePath path : paths) {
            ret.add(ErrorDescriptionFactory.forTree(context, path, NbBundle.getMessage(AssignmentIssues.class, "MSG_AssignmentToCatchBlockParameter", param.getSimpleName()))); //NOI18N
        }
        return ret;
    }

    @Hint(category = "assignment_issues", enabled = false, suppressWarnings = "AssignmentToMethodParameter") //NOI18N
    @TriggerTreeKind({Kind.ASSIGNMENT, Kind.AND_ASSIGNMENT, Kind.DIVIDE_ASSIGNMENT,
        Kind.LEFT_SHIFT_ASSIGNMENT, Kind.MINUS_ASSIGNMENT, Kind.MULTIPLY_ASSIGNMENT,
        Kind.OR_ASSIGNMENT, Kind.PLUS_ASSIGNMENT, Kind.REMAINDER_ASSIGNMENT, Kind.RIGHT_SHIFT_ASSIGNMENT,
        Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT, Kind.XOR_ASSIGNMENT, Kind.PREFIX_INCREMENT,
        Kind.PREFIX_DECREMENT, Kind.POSTFIX_INCREMENT, Kind.POSTFIX_DECREMENT})
    public static ErrorDescription assignmentToMethodParam(HintContext context) {
        final TreePath path = context.getPath();
        Element element = null;
        switch (path.getLeaf().getKind()) {
            case ASSIGNMENT:
                element = context.getInfo().getTrees().getElement(TreePath.getPath(path, ((AssignmentTree) path.getLeaf()).getVariable()));
                break;
            case PREFIX_INCREMENT:
            case PREFIX_DECREMENT:
            case POSTFIX_INCREMENT:
            case POSTFIX_DECREMENT:
                element = context.getInfo().getTrees().getElement(TreePath.getPath(path, ((UnaryTree) path.getLeaf()).getExpression()));
                break;
            default:
                element = context.getInfo().getTrees().getElement(TreePath.getPath(path, ((CompoundAssignmentTree) path.getLeaf()).getVariable()));
        }
        if (element != null && element.getKind() == ElementKind.PARAMETER) {
            return ErrorDescriptionFactory.forTree(context, path, NbBundle.getMessage(AssignmentIssues.class, "MSG_AssignmentToMethodParam", element.getSimpleName())); //NOI18N
        }
        return null;
    }

    @Hint(category = "assignment_issues", enabled = false, suppressWarnings = "NestedAssignment") //NOI18N
    @TriggerTreeKind({Kind.ASSIGNMENT, Kind.AND_ASSIGNMENT, Kind.DIVIDE_ASSIGNMENT,
        Kind.LEFT_SHIFT_ASSIGNMENT, Kind.MINUS_ASSIGNMENT, Kind.MULTIPLY_ASSIGNMENT,
        Kind.OR_ASSIGNMENT, Kind.PLUS_ASSIGNMENT, Kind.REMAINDER_ASSIGNMENT, Kind.RIGHT_SHIFT_ASSIGNMENT,
        Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT, Kind.XOR_ASSIGNMENT})
    public static ErrorDescription nestedAssignment(HintContext context) {
        final TreePath path = context.getPath();
        final Kind parentKind = path.getParentPath().getLeaf().getKind();
        if (parentKind != Kind.EXPRESSION_STATEMENT && parentKind != Kind.ANNOTATION) {
            return ErrorDescriptionFactory.forTree(context, path, NbBundle.getMessage(AssignmentIssues.class, "MSG_NestedAssignment", path.getLeaf())); //NOI18N
        }
        return null;
    }

    @Hint(category = "assignment_issues", enabled = false, suppressWarnings = "ValueOfIncrementOrDecrementUsed") //NOI18N
    @TriggerTreeKind({Kind.PREFIX_INCREMENT, Kind.PREFIX_DECREMENT, Kind.POSTFIX_INCREMENT, Kind.POSTFIX_DECREMENT})
    public static ErrorDescription incrementDecrementUsed(HintContext context) {
        final TreePath path = context.getPath();
        if (path.getParentPath().getLeaf().getKind() != Kind.EXPRESSION_STATEMENT) {
            final Kind kind = path.getLeaf().getKind();
            return ErrorDescriptionFactory.forTree(context, path, NbBundle.getMessage(AssignmentIssues.class,
                    kind == Kind.PREFIX_INCREMENT || kind == Kind.POSTFIX_INCREMENT
                    ? "MSG_IncrementUsedAsExpression" : "MSG_DecrementUsedAsExpression", path.getLeaf())); //NOI18N
        }
        return null;
    }

    @Hint(category = "assignment_issues", enabled = false, suppressWarnings = "AssignmentReplaceableWithOperatorAssignment") //NOI18N
    @TriggerTreeKind(Kind.ASSIGNMENT)
    public static ErrorDescription replaceAssignWithOpAssign(HintContext context) {
        final TreePath path = context.getPath();
        final AssignmentTree at = (AssignmentTree) path.getLeaf();
        switch (at.getExpression().getKind()) {
            case AND:
            case DIVIDE:
            case LEFT_SHIFT:
            case MINUS:
            case MULTIPLY:
            case OR:
            case PLUS:
            case REMAINDER:
            case RIGHT_SHIFT:
            case UNSIGNED_RIGHT_SHIFT:
            case XOR:
                final BinaryTree bt = (BinaryTree) at.getExpression();
                final Trees trees = context.getInfo().getTrees();
                final Element element = trees.getElement(TreePath.getPath(path, at.getVariable()));
                if (element != null && element == trees.getElement(TreePath.getPath(path, bt.getLeftOperand()))) {
                    return ErrorDescriptionFactory.forTree(context, path, NbBundle.getMessage(AssignmentIssues.class, "MSG_ReplaceAssignmentWithOperatorAssignment", path.getLeaf()), //NOI18N
                            new ReplaceAssignmentFix(NbBundle.getMessage(AssignmentIssues.class, "FIX_ReplaceAssignmentWithOperatorAssignment", path.getLeaf()), TreePathHandle.create(path, context.getInfo()))); //NOI18N
                }
        }
        return null;
    }

    private static final class AssignmentFinder extends TreePathScanner<Void, List<TreePath>> {

        private final Trees trees;
        private final Element param;

        private AssignmentFinder(Trees trees, Element param) {
            this.trees = trees;
            this.param = param;
        }

        @Override
        public Void visitAssignment(AssignmentTree node, List<TreePath> p) {
            if (param == trees.getElement(TreePath.getPath(getCurrentPath(), node.getVariable()))) {
                p.add(getCurrentPath());
                return null;
            }
            return super.visitAssignment(node, p);
        }

        @Override
        public Void visitCompoundAssignment(CompoundAssignmentTree node, List<TreePath> p) {
            if (param == trees.getElement(TreePath.getPath(getCurrentPath(), node.getVariable()))) {
                p.add(getCurrentPath());
                return null;
            }
            return super.visitCompoundAssignment(node, p);
        }

        @Override
        public Void visitUnary(UnaryTree node, List<TreePath> p) {
            switch (node.getKind()) {
                case PREFIX_INCREMENT:
                case PREFIX_DECREMENT:
                case POSTFIX_INCREMENT:
                case POSTFIX_DECREMENT:
                    if (param == trees.getElement(TreePath.getPath(getCurrentPath(), node.getExpression()))) {
                        p.add(getCurrentPath());
                        return null;
                    }
            }
            return super.visitUnary(node, p);
        }

        @Override
        public Void visitClass(ClassTree node, List<TreePath> p) {
            return null;
        }
    }

    private static final class ReplaceAssignmentFix implements Fix {

        private final String text;
        private final TreePathHandle handle;

        public ReplaceAssignmentFix(String text, TreePathHandle handle) {
            this.text = text;
            this.handle = handle;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public ChangeInfo implement() throws Exception {
            JavaSource.forFileObject(handle.getFileObject()).runModificationTask(new Task<WorkingCopy>() {

                @Override
                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(Phase.RESOLVED);
                    final TreePath path = handle.resolve(wc);
                    if (path == null) {
                        return;
                    }
                    final AssignmentTree at = (AssignmentTree) path.getLeaf();
                    Kind kind = null;
                    switch (at.getExpression().getKind()) {
                        case AND:
                            kind = Kind.AND_ASSIGNMENT;
                            break;
                        case DIVIDE:
                            kind = Kind.DIVIDE_ASSIGNMENT;
                            break;
                        case LEFT_SHIFT:
                            kind = Kind.LEFT_SHIFT_ASSIGNMENT;
                            break;
                        case MINUS:
                            kind = Kind.MINUS_ASSIGNMENT;
                            break;
                        case MULTIPLY:
                            kind = Kind.MULTIPLY_ASSIGNMENT;
                            break;
                        case OR:
                            kind = Kind.OR_ASSIGNMENT;
                            break;
                        case PLUS:
                            kind = Kind.PLUS_ASSIGNMENT;
                            break;
                        case REMAINDER:
                            kind = Kind.REMAINDER_ASSIGNMENT;
                            break;
                        case RIGHT_SHIFT:
                            kind = Kind.RIGHT_SHIFT_ASSIGNMENT;
                            break;
                        case UNSIGNED_RIGHT_SHIFT:
                            kind = Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT;
                            break;
                        case XOR:
                            kind = Kind.XOR_ASSIGNMENT;
                            break;
                    }
                    if (kind == null) {
                        return;
                    }
                    final CompoundAssignmentTree cat = wc.getTreeMaker().CompoundAssignment(kind, at.getVariable(), ((BinaryTree) at.getExpression()).getRightOperand());
                    wc.rewrite(at, cat);
                }
            }).commit();
            return null;
        }
    }
}
