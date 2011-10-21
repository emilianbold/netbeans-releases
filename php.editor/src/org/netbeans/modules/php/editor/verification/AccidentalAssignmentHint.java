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
package org.netbeans.modules.php.editor.verification;

import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ContinueStatement;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.GotoLabel;
import org.netbeans.modules.php.editor.parser.astnodes.GotoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocStaticAccessType;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPVarComment;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.editor.verification.PHPHintsProvider.Kind;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class AccidentalAssignmentHint extends AbstractRule implements PHPRuleWithPreferences {

    private static final String HINT_ID = "Accidental.Assignment.Hint"; //NOI18N
    private static final String CHECK_ASSIGNMENTS_IN_SUB_STATEMENTS = "php.verification.check.assignments.in.sub.statements"; //NOI18N
    private static final boolean CHECK_ASSIGNMENTS_IN_SUB_STATEMENTS_DEFAULT = false;
    private static final String CHECK_ASSIGNMENTS_IN_WHILE_STATEMENTS = "php.verification.check.assignments.in.while.statements"; //NOI18N
    private static final boolean CHECK_ASSIGNMENTS_IN_WHILE_STATEMENTS_DEFAULT = false;
    private Preferences preferences;

    @Override
    void computeHintsImpl(PHPRuleContext context, List<Hint> hints, Kind kind) throws BadLocationException {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        CheckVisitor checkVisitor = new CheckVisitor(fileObject);
        phpParseResult.getProgram().accept(checkVisitor);
        hints.addAll(checkVisitor.getHints());
    }

    private class CheckVisitor extends DefaultVisitor {

        private final FileObject fileObject;
        private final List<Hint> hints = new LinkedList<Hint>();
        private final List<Assignment> accidentalAssignments = new LinkedList<Assignment>();

        public CheckVisitor(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        public List<Hint> getHints() {
            for (Assignment assignment : accidentalAssignments) {
                createHint(assignment);
            }
            return hints;
        }

        @Messages("AccidentalAssignmentHintCustom=Accidental assignment in a condition {0}")
        private void createHint(Assignment assignment) {
            OffsetRange offsetRange = new OffsetRange(assignment.getStartOffset(), assignment.getEndOffset());
            hints.add(new Hint(AccidentalAssignmentHint.this, Bundle.AccidentalAssignmentHintCustom(asText(assignment)), fileObject, offsetRange, null, 500));
        }

        private String asText(Assignment assignment) {
            StringBuilder retval = new StringBuilder();
            VariableBase leftHandSide = assignment.getLeftHandSide();
            if (leftHandSide instanceof Variable) {
                Variable variable = (Variable) leftHandSide;
                retval.append(asText(variable)).append(" "); //NOI18N
                retval.append(assignment.getOperator().toString()).append(" ..."); //NOI18N
            }
            return retval.toString();
        }

        private String asText(Variable variable) {
            StringBuilder retval = new StringBuilder();
            Expression name = variable.getName();
            if (name instanceof Identifier) {
                Identifier identifier = (Identifier) name;
                if (variable.isDollared()) {
                    retval.append("$"); //NOI18N
                }
                retval.append(identifier.getName());
            } else {
                retval.append("UNKNOWN"); //NOI18N
            }
            return retval.toString();
        }

        private void processCondition(Expression node) {
            if (node instanceof Assignment) {
                processAssignment((Assignment) node);
            }
        }

        private void processAssignment(Assignment assignment) {
            if (checkAssignmentsInSubStatements(preferences)) {
                processSubAssignments(assignment);
            } else {
                accidentalAssignments.add(assignment);
            }
        }

        private void processSubAssignments(Assignment assignment) {
            assignment.accept(new DefaultVisitor() {

                @Override
                public void visit(Assignment node) {
                    accidentalAssignments.add(node);
                    scan(node.getRightHandSide());
                }

            });
        }

        @Override
        public void visit(DoStatement node) {
            if (checkAssignmentsInWhileStatements(preferences)) {
                processCondition(node.getCondition());
            }
            scan(node.getBody());
        }

        @Override
        public void visit(IfStatement node) {
            processCondition(node.getCondition());
            scan(node.getTrueStatement());
            scan(node.getFalseStatement());
        }

        @Override
        public void visit(ForStatement node) {
            for (Expression condition : node.getConditions()) {
                processCondition(condition);
            }
            scan(node.getInitializers());
            scan(node.getUpdaters());
            scan(node.getBody());
        }

        @Override
        public void visit(WhileStatement node) {
            if (checkAssignmentsInWhileStatements(preferences)) {
                processCondition(node.getCondition());
            }
            scan(node.getBody());
        }

        @Override
        public void visit(ConstantDeclaration node) {
            // intentionally
        }

        @Override
        public void visit(ContinueStatement node) {
            // intentionally
        }

        @Override
        public void visit(FieldsDeclaration node) {
            // intentionally
        }

        @Override
        public void visit(GotoLabel node) {
            // intentionally
        }

        @Override
        public void visit(GotoStatement node) {
            // intentionally
        }

        @Override
        public void visit(InterfaceDeclaration node) {
            // intentionally
        }

        @Override
        public void visit(PHPDocBlock node) {
            // intentionally
        }

        @Override
        public void visit(PHPDocTypeTag node) {
            // intentionally
        }

        @Override
        public void visit(PHPDocMethodTag node) {
            // intentionally
        }

        @Override
        public void visit(PHPDocVarTypeTag node) {
            // intentionally
        }

        @Override
        public void visit(PHPDocStaticAccessType node) {
            // intentionally
        }

        @Override
        public void visit(PHPVarComment node) {
            // intentionally
        }

        @Override
        public void visit(SingleFieldDeclaration node) {
            // intentionally
        }

        @Override
        public void visit(StaticFieldAccess node) {
            // intetionally
        }

        @Override
        public void visit(UseStatement node) {
            // intentionally
        }

        @Override
        public void visit(UseStatementPart node) {
            // intentionally
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("AccidentalAssignmentHintDesc=Using an assignment operator (=) instead of comparison operator (===) is a frequent cause of bugs. Therefore assignments in conditional clauses should be avoided.")
    public String getDescription() {
        return Bundle.AccidentalAssignmentHintDesc();
    }

    @Override
    @Messages("AccidentalAssignmentHintDispName=Accidental Assignments")
    public String getDisplayName() {
        return Bundle.AccidentalAssignmentHintDispName();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    @Override
    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public JComponent getCustomizer(Preferences preferences) {
        return new AccidentalAssignmentCustomizer(preferences, this);
    }

    public void setCheckAssignmentsInSubStatements(Preferences preferences, boolean isEnabled) {
        preferences.putBoolean(CHECK_ASSIGNMENTS_IN_SUB_STATEMENTS, isEnabled);
    }

    public boolean checkAssignmentsInSubStatements(Preferences preferences) {
        return preferences.getBoolean(CHECK_ASSIGNMENTS_IN_SUB_STATEMENTS, CHECK_ASSIGNMENTS_IN_SUB_STATEMENTS_DEFAULT);
    }

    public void setCheckAssignmentsInWhileStatements(Preferences preferences, boolean isEnabled) {
        preferences.putBoolean(CHECK_ASSIGNMENTS_IN_WHILE_STATEMENTS, isEnabled);
    }

    public boolean checkAssignmentsInWhileStatements(Preferences preferences) {
        return preferences.getBoolean(CHECK_ASSIGNMENTS_IN_WHILE_STATEMENTS, CHECK_ASSIGNMENTS_IN_WHILE_STATEMENTS_DEFAULT);
    }

}
