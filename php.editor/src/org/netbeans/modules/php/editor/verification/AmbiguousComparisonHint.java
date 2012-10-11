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
package org.netbeans.modules.php.editor.verification;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.*;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression.OperatorType;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class AmbiguousComparisonHint extends AbstractHint {

    private static final String HINT_ID = "Ambiguous.Comparison.Hint"; //NOI18N

    @Override
    void compute(final PHPRuleContext context, final List<Hint> hints) {
        final PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        final FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        final CheckVisitor checkVisitor = new CheckVisitor(fileObject, context.doc);
        phpParseResult.getProgram().accept(checkVisitor);
        hints.addAll(checkVisitor.getHints());
    }

    private class CheckVisitor extends DefaultTreePathVisitor {
        private final FileObject fileObject;
        private final BaseDocument doc;
        private final List<InfixExpression> expressions = new LinkedList<InfixExpression>();
        private final List<Hint> hints = new LinkedList<Hint>();

        public CheckVisitor(final FileObject fileObject, final BaseDocument doc) {
            this.fileObject = fileObject;
            this.doc = doc;
        }

        public List<Hint> getHints() {
            for (InfixExpression infixExpression : expressions) {
                createHint(infixExpression);
            }
            return hints;
        }

        @Messages("AmbiguousComparisonHintCustom=Possible ambiguous comparison")
        private void createHint(final InfixExpression node) {
            final OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            hints.add(new Hint(AmbiguousComparisonHint.this, Bundle.AmbiguousComparisonHintCustom(), fileObject, offsetRange, Collections.<HintFix>singletonList(new AssignmentHintFix(doc, node)), 500));
        }

        @Override
        public void visit(final InfixExpression node) {
            final OperatorType operator = node.getOperator();
            if (OperatorType.IS_EQUAL.equals(operator) || OperatorType.IS_IDENTICAL.equals(operator)) {
                checkPathForNode(node);
            }
            super.visit(node);
        }

        private void checkPathForNode(final InfixExpression node) {
            final List<ASTNode> path = getPath();
            if (path.isEmpty() || !isValidContext(path)) {
                expressions.add(node);
            }
        }

        private boolean isValidContext(final List<ASTNode> path) {
            boolean result = false;
            for (ASTNode node : path) {
                if (isConditionalNode(node) || node instanceof Assignment || node instanceof ReturnStatement) {
                    result = true;
                    break;
                } else if (node instanceof Block) {
                    result = false;
                    break;
                }
            }
            return result;
        }

        private boolean isConditionalNode(final ASTNode node) {
            return node instanceof IfStatement
                    || node instanceof WhileStatement
                    || node instanceof DoStatement
                    || node instanceof ForStatement
                    || node instanceof ForEachStatement
                    || node instanceof ConditionalExpression;
        }

    }

    private static class AssignmentHintFix implements HintFix {
        private final BaseDocument doc;
        private final InfixExpression expression;
        private static final String ASSIGNMENT = " = "; //NOI18N

        public AssignmentHintFix(final BaseDocument doc, final InfixExpression expression) {
            this.doc = doc;
            this.expression = expression;
        }

        @Override
        @Messages("AssignmentHintFixDisp=Change Comparison to Assignment")
        public String getDescription() {
            return Bundle.AssignmentHintFixDisp();
        }

        @Override
        public void implement() throws Exception {
            final EditList edits = new EditList(doc);
            final OffsetRange offsetRange = new OffsetRange(expression.getLeft().getEndOffset(), expression.getRight().getStartOffset());
            edits.replace(offsetRange.getStart(), offsetRange.getLength(), ASSIGNMENT, true, 0);
            edits.apply();
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("AmbiguousComparisonHintDescName=Tries to reveal typos in assignments (assignments with more than one assignment operator).")
    public String getDescription() {
        return Bundle.AmbiguousComparisonHintDescName();
    }

    @Override
    @Messages("AmbiguousComparisonHintDispName=Ambiguous Comparison")
    public String getDisplayName() {
        return Bundle.AmbiguousComparisonHintDispName();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

}
