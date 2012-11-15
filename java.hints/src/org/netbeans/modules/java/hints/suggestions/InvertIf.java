/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_InvertIf", description = "#DESC_InvertIf", category = "suggestions", hintKind= Hint.Kind.ACTION)
@Messages({
    "DN_InvertIf=Invert If",
    "DESC_InvertIf=Will invert an if statement; negate the condition and switch the statements from the then and else sections."
})
public class InvertIf {

    @TriggerPattern(value = "if ($cond) $then; else $else;")
    @Messages({"ERR_InvertIf=Invert If",
               "FIX_InvertIf=Invert If"})
    public static ErrorDescription computeWarning(HintContext ctx) {
        TreePath cond = ctx.getVariables().get("$cond");
        long conditionEnd = ctx.getInfo().getTrees().getSourcePositions().getEndPosition(cond.getCompilationUnit(), cond.getParentPath().getLeaf());
        if (ctx.getCaretLocation() > conditionEnd) return null;
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_InvertIf(), new FixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }
    
    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_InvertIf();
        }

        @Override
        protected void performRewrite(final TransformationContext ctx) throws Exception {
            IfTree toRewrite = (IfTree) ctx.getPath().getLeaf();
            
            ctx.getWorkingCopy().rewrite(toRewrite, ctx.getWorkingCopy().getTreeMaker().If(toRewrite.getCondition(), toRewrite.getElseStatement(), toRewrite.getThenStatement()));
            negate(ctx.getWorkingCopy(), toRewrite.getCondition(), toRewrite);
        }
        
        //TODO: should be done automatically:
        private void negate(WorkingCopy copy, ExpressionTree original, Tree parent) {
            TreeMaker make = copy.getTreeMaker();
            ExpressionTree newTree;
            switch (original.getKind()) {
                case PARENTHESIZED:
                    ExpressionTree expr = ((ParenthesizedTree) original).getExpression();
                    negate(copy, expr, original);
                    return ;
                case LOGICAL_COMPLEMENT:
                    newTree = ((UnaryTree) original).getExpression();
                    while (newTree.getKind() == Kind.PARENTHESIZED && !JavaFixUtilities.requiresParenthesis(((ParenthesizedTree) newTree).getExpression(), original, parent)) {
                        newTree = ((ParenthesizedTree) newTree).getExpression();
                    }
                    break;
                case NOT_EQUAL_TO:
                    newTree = negateBinaryOperator(copy, original, Kind.EQUAL_TO, false);
                    break;
                case EQUAL_TO:
                    newTree = negateBinaryOperator(copy, original, Kind.NOT_EQUAL_TO, false);
                    break;
                case BOOLEAN_LITERAL:
                    newTree = make.Literal(!(Boolean) ((LiteralTree) original).getValue());
                    break;
                case CONDITIONAL_AND:
                    newTree = negateBinaryOperator(copy, original, Kind.CONDITIONAL_OR, true);
                    break;
                case CONDITIONAL_OR:
                    newTree = negateBinaryOperator(copy, original, Kind.CONDITIONAL_AND, true);
                    break;
                case LESS_THAN:
                    newTree = negateBinaryOperator(copy, original, Kind.GREATER_THAN_EQUAL, false);
                    break;
                case LESS_THAN_EQUAL:
                    newTree = negateBinaryOperator(copy, original, Kind.GREATER_THAN, false);
                    break;
                case GREATER_THAN:
                    newTree = negateBinaryOperator(copy, original, Kind.LESS_THAN_EQUAL, false);
                    break;
                case GREATER_THAN_EQUAL:
                    newTree = negateBinaryOperator(copy, original, Kind.LESS_THAN, false);
                    break;
                default:
                    newTree = make.Unary(Kind.LOGICAL_COMPLEMENT, original);
                    break;
            }
         
            if (JavaFixUtilities.requiresParenthesis(newTree, original, parent)) {
                newTree = make.Parenthesized(newTree);
            }
            
            copy.rewrite(original, newTree);
        }
        
        private ExpressionTree negateBinaryOperator(WorkingCopy copy, Tree original, Kind newKind, boolean negateOperands) {
            BinaryTree bt = (BinaryTree) original;
            if (negateOperands) {
                negate(copy, bt.getLeftOperand(), original);
                negate(copy, bt.getRightOperand(), original);
            }
            return copy.getTreeMaker().Binary(newKind, bt.getLeftOperand(), bt.getRightOperand());
        }
        
    }

}
