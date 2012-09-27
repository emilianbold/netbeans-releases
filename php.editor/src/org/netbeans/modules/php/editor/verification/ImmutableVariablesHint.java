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

import java.util.*;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment.Type;
import org.netbeans.modules.php.editor.parser.astnodes.*;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.editor.verification.PHPHintsProvider.Kind;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ImmutableVariablesHint extends AbstractRule implements PHPRuleWithPreferences {

    private static final String HINT_ID = "Immutable.Variables.Hint"; //NOI18N
    private static final String NUMBER_OF_ALLOWED_ASSIGNMENTS = "php.verification.number.of.allowed.assignments"; //NOI18N
    private static final int DEFAULT_NUMBER_OF_ALLOWED_ASSIGNMENTS = 1;
    private static final List<String> UNCHECKED_VARIABLES = new LinkedList<String>();
    private Preferences preferences;

    static {
        UNCHECKED_VARIABLES.add("this"); //NOI18N
        UNCHECKED_VARIABLES.add("GLOBALS"); //NOI18N
        UNCHECKED_VARIABLES.add("_SERVER"); //NOI18N
        UNCHECKED_VARIABLES.add("_GET"); //NOI18N
        UNCHECKED_VARIABLES.add("_POST"); //NOI18N
        UNCHECKED_VARIABLES.add("_FILES"); //NOI18N
        UNCHECKED_VARIABLES.add("_COOKIE"); //NOI18N
        UNCHECKED_VARIABLES.add("_SESSION"); //NOI18N
        UNCHECKED_VARIABLES.add("_REQUEST"); //NOI18N
        UNCHECKED_VARIABLES.add("_ENV"); //NOI18N
    }

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
        private final Stack<ASTNode> parentNodes = new Stack<ASTNode>();
        private final Map<ASTNode, Map<String, List<Variable>>> assignments = new HashMap<ASTNode, Map<String, List<Variable>>>();
        private final List<Hint> hints = new LinkedList<Hint>();
        private boolean variableAssignment;
        private final int numberOfAllowedAssignments;

        public CheckVisitor(FileObject fileObject) {
            this.fileObject = fileObject;
            this.numberOfAllowedAssignments = getNumberOfAllowedAssignments(preferences);
        }

        public List<Hint> getHints() {
            for (ASTNode scopeNode : assignments.keySet()) {
                Map<String, List<Variable>> names = assignments.get(scopeNode);
                checkNamesInScope(names);
            }
            return hints;
        }

        private void checkNamesInScope(Map<String, List<Variable>> names) {
            for (Entry<String, List<Variable>> entry : names.entrySet()) {
                checkAllowedAssignments(entry.getValue());
            }
        }

        private void checkAllowedAssignments(List<Variable> variables) {
            int variablesSize = variables.size();
            if (variablesSize > numberOfAllowedAssignments) {
                createHints(variables);
            }
        }

        @Messages({
            "# {0} - Number of allowed assignments",
            "# {1} - Number of assignments",
            "# {2} - Variable name",
            "ImmutableVariablesHintCustom=You should use only:\n{0} assignment(s) ({1} used)\nto a variable:\n${2}\nto avoid accidentally overwriting it and make your code easier to read."
        })
        private void createHints(List<Variable> variables) {
            for (Variable variable : variables) {
                int start = variable.getStartOffset() + 1;
                int end = variable.getEndOffset();
                OffsetRange offsetRange = new OffsetRange(start, end);
                Identifier variableIdentifier = getIdentifier(variable);
                String variableName = variableIdentifier == null ? "?" : variableIdentifier.getName(); //NOI18N
                hints.add(new Hint(ImmutableVariablesHint.this, Bundle.ImmutableVariablesHintCustom(numberOfAllowedAssignments, variables.size(), variableName), fileObject, offsetRange, null, 500));
            }
        }

        @Override
        public void visit(Program node) {
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(NamespaceDeclaration node) {
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(FunctionDeclaration node) {
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(LambdaFunctionDeclaration node) {
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(IfStatement node) {
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(CatchClause node) {
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(Block node) {
            if (parentNodes.peek() instanceof IfStatement) {
                parentNodes.push(node);
                super.visit(node);
                parentNodes.pop();
            } else {
                super.visit(node);
            }
        }

        @Override
        public void visit(ForStatement node) {
            parentNodes.push(node);
            scan(node.getInitializers());
            scan(node.getConditions());
            scan(node.getBody());
            parentNodes.pop();
        }

        @Override
        public void visit(ForEachStatement node) {
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(DoStatement node) {
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(WhileStatement node) {
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(SwitchCase node) {
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(StaticFieldAccess node) {
            // intentionally
        }

        @Override
        public void visit(Variable node) {
            if (variableAssignment) {
                processVariableAssignment(node);
            }
        }

        private void processVariableAssignment(Variable node) {
            ASTNode parentNode = parentNodes.peek();
            Map<String, List<Variable>> names = getNames(parentNode);
            Identifier identifier = getIdentifier(node);
            if (identifier != null) {
                addValidVariable(identifier, names, node);
            }
        }

        private Map<String, List<Variable>> getNames(ASTNode parentNode) {
            Map<String, List<Variable>> names = assignments.get(parentNode);
            if (names == null) {
                names = new HashMap<String, List<Variable>>();
                assignments.put(parentNode, names);
            }
            return names;
        }

        private void addValidVariable(Identifier identifier, Map<String, List<Variable>> names, Variable node) {
            String name = identifier.getName();
            if (!UNCHECKED_VARIABLES.contains(name)) {
                List<Variable> variables = getVariables(names, name);
                variables.add(node);
            }
        }

        private List<Variable> getVariables(Map<String, List<Variable>> names, String name) {
            List<Variable> variables = names.get(name);
            if (variables == null) {
                variables = new LinkedList<Variable>();
                names.put(name, variables);
            }
            return variables;
        }

        @Override
        public void visit(Assignment node) {
            if (node.getOperator().equals(Type.EQUAL)) {
                if (parentNodes.peek() instanceof IfStatement) {
                    parentNodes.push(node);
                    processEqualAssignment(node);
                    parentNodes.pop();
                } else {
                    processEqualAssignment(node);
                }
            }
        }

        private void processEqualAssignment(Assignment node) {
            if (!(node.getRightHandSide() instanceof InfixExpression)
                    || (node.getRightHandSide() instanceof InfixExpression && !containsConcatOperator((InfixExpression) node.getRightHandSide()))) {
                variableAssignment = true;
                scan(node.getLeftHandSide());
                variableAssignment = false;
            }
        }

        private boolean containsConcatOperator(InfixExpression infixExpression) {
            boolean retval = false;
            if (infixExpression.getOperator().equals(InfixExpression.OperatorType.CONCAT)) {
                retval = true;
            } else if (infixExpression.getLeft() instanceof InfixExpression) {
                retval = containsConcatOperator((InfixExpression) infixExpression.getLeft());
            } else if (infixExpression.getLeft() instanceof InfixExpression) {
                retval = containsConcatOperator((InfixExpression) infixExpression.getRight());
            }
            return retval;
        }

        @Override
        public void visit(ArrayAccess node) {
            // intentionally
        }

        @Override
        public void visit(ArrayCreation node) {
            // intentionally
        }

        @Override
        public void visit(FieldAccess node) {
            // intentionally
        }

        @CheckForNull
        private Identifier getIdentifier(Variable variable) {
            Identifier retval = null;
            if (variable != null && variable.isDollared()) {
                retval = separateIdentifier(variable);
            }
            return retval;
        }

        @CheckForNull
        private Identifier separateIdentifier(Variable variable) {
            Identifier retval = null;
            if (variable.getName() instanceof Identifier) {
                retval = (Identifier) variable.getName();
            }
            return retval;
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("ImmutableVariableHintDesc=Checks a number of assignments into a variable in a block.")
    public String getDescription() {
        return Bundle.ImmutableVariableHintDesc();
    }

    @Override
    @Messages("ImmutableVariableHintDispName=Immutable Variables")
    public String getDisplayName() {
        return Bundle.ImmutableVariableHintDispName();
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
        return new ImmutableVariablesCustomizer(preferences, this);
    }

    public void setNumberOfAllowedAssignments(Preferences preferences, Integer value) {
        preferences.putInt(NUMBER_OF_ALLOWED_ASSIGNMENTS, value);
    }

    public int getNumberOfAllowedAssignments(Preferences preferences) {
        return preferences.getInt(NUMBER_OF_ALLOWED_ASSIGNMENTS, DEFAULT_NUMBER_OF_ALLOWED_ASSIGNMENTS);
    }

}
