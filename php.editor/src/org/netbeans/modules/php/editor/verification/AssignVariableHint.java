/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Radek Matous
 */
public class AssignVariableHint extends AbstractRule {

    public String getId() {
        return "assign.variable.hint"; //NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(AssignVariableHint.class, "AssignVariableHintDesc");//NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(AssignVariableHint.class, "AssignVariableHintDisplayName");//NOI18N
    }

    @Override
    void computeHintsImpl(PHPRuleContext context, List<Hint> hints, PHPHintsProvider.Kind kind) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        int lineBegin = -1;
        int lineEnd = -1;
        try {
            lineBegin = context.caretOffset > 0 ? Utilities.getRowStart(context.doc, context.caretOffset) : -1;
            lineEnd = (lineBegin != -1) ? Utilities.getRowEnd(context.doc, context.caretOffset) : -1;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (lineBegin != -1 && lineEnd != -1 && context.caretOffset > lineBegin) {
            IntroduceFixVisitor introduceFixVisitor = new IntroduceFixVisitor(context.doc, lineBegin, lineEnd);
            phpParseResult.getProgram().accept(introduceFixVisitor);
            IntroduceFix variableFix = introduceFixVisitor.getIntroduceFix();
            if (variableFix != null) {
                hints.add(new Hint(AssignVariableHint.this, getDisplayName(),
                        context.parserResult.getSnapshot().getSource().getFileObject(), variableFix.getOffsetRange(),
                        Collections.<HintFix>singletonList(variableFix), 500));
            }
        }
    }

    private class IntroduceFixVisitor extends DefaultVisitor {

        private int lineBegin;
        private int lineEnd;
        private BaseDocument doc;
        private IntroduceFix fix;
        private List<Variable> variables;

        IntroduceFixVisitor(BaseDocument doc, int lineBegin, int lineEnd) {
            this.doc = doc;
            this.lineBegin = lineBegin;
            this.lineEnd = lineEnd;
            this.variables = new ArrayList<Variable>();
        }

        @Override
        public void scan(ASTNode node) {
            if (node != null && (isBefore(node.getStartOffset(), lineEnd) || fix != null)) {
                super.scan(node);
            }
        }

        @Override
        public void visit(ExpressionStatement node) {
            if (isInside(node.getStartOffset(), lineBegin, lineEnd)) {
                Expression expression = node.getExpression();
                if (expression instanceof ClassInstanceCreation) {
                    fix = new InstanceCreationVariableFix(doc, (ClassInstanceCreation) expression);
                } else if (expression instanceof MethodInvocation) {
                    fix = new MethodInvocationVariableFix(doc, (MethodInvocation) expression);
                } else if (expression instanceof FunctionInvocation) {
                    fix = new FunctionInvocationVariableFix(doc, (FunctionInvocation) expression);
                } else if (expression instanceof StaticMethodInvocation) {
                    fix = new StaticMethodInvocationVariableFix(doc, (StaticMethodInvocation) expression);
                }
            }
            super.visit(node);
        }

        @Override
        public void visit(Variable node) {
            variables.add(node);
            super.visit(node);
        }

        /**
         * @return or null
         */
        public IntroduceFix getIntroduceFix() {
            if (fix != null) {
                fix.setVariables(variables);
            }
            return fix;
        }
    }

    private abstract class IntroduceFix implements HintFix {

        BaseDocument doc;
        ASTNode node;
        List<Variable> variables;

        public IntroduceFix(BaseDocument doc, ASTNode node) {
            this.doc = doc;
            this.node = node;
        }

        OffsetRange getOffsetRange() {
            return new OffsetRange(node.getStartOffset(), node.getEndOffset());
        }

        public boolean isInteractive() {
            return false;
        }

        public boolean isSafe() {
            return true;
        }

        public void setVariables(List<Variable> variables) {
            this.variables = variables;
        }

        public String getDescription() {
            return AssignVariableHint.this.getDescription();
        }

        public void implement() throws Exception {
            int textOffset = getTextOffset();
            String variableName = getVariableName();
            EditList edits = new EditList(doc);
            edits.replace(textOffset, 0, String.format("$%s = ", variableName), true, 0);//NOI18N
            edits.apply();
            JTextComponent target = GsfUtilities.getOpenPane();
            if (target != null) {
                int selectStart = textOffset + 1;//after $
                int selectEnd = selectStart + variableName.length();
                target.select(selectStart, selectEnd);
            }
        }

        protected int getTextOffset() {
            return node.getStartOffset();
        }

        abstract String getVariableName();

        String adjustName(String name) {
            if (name == null) {
                return null;
            }

            String shortName = null;

            if (name.startsWith("get") && name.length() > 3) {
                shortName = name.substring(3);
            }

            if (name.startsWith("is") && name.length() > 2) {
                shortName = name.substring(2);
            }

            if (shortName != null) {
                return firstToLower(shortName);
            }

            return name;
        }

        String getVariableName(String guessName) {
            guessName = adjustName(firstToLower(guessName));
            guessName = guessName != null ? guessName : "variable";//NOI18N
            String proposedName = guessName;
            int incr = -1;
            boolean cont = true;
            while (cont) {
                if (incr != -1) {
                    proposedName = String.format("%s%d", guessName, incr); //NOI18N
                }
                cont = false;
                for (Variable variable : variables) {
                    String varName = CodeUtils.extractVariableName(variable);
                    if (varName != null) {
                        if (variable.isDollared()) {
                            varName = varName.substring(1);
                            if (proposedName.equals(varName)) {
                                incr++;
                                cont = true;
                                break;
                            }
                        }
                    }
                }
            }
            return proposedName;
        }
    }

    private class InstanceCreationVariableFix extends IntroduceFix {

        InstanceCreationVariableFix(BaseDocument doc, ClassInstanceCreation instanceCreation) {
            super(doc, instanceCreation);
        }

        protected String getVariableName() {
            ClassInstanceCreation instanceCreation = (ClassInstanceCreation) node;
            String guessName = CodeUtils.extractClassName(instanceCreation.getClassName());
            return getVariableName(guessName);
        }
    }

    private class StaticMethodInvocationVariableFix extends IntroduceFix {
        StaticMethodInvocationVariableFix(BaseDocument doc, StaticMethodInvocation methodInvocation) {
            super(doc, methodInvocation);
        }

        protected String getVariableName() {
            StaticMethodInvocation methodInvocation = (StaticMethodInvocation) node;
            String guessName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
            return getVariableName(guessName);
        }
    }

    private class MethodInvocationVariableFix extends IntroduceFix {

        MethodInvocationVariableFix(BaseDocument doc, MethodInvocation methodInvocation) {
            super(doc, methodInvocation);
        }

        protected String getVariableName() {
            MethodInvocation methodInvocation = (MethodInvocation) node;
            String guessName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
            return getVariableName(guessName);
        }
    }

    private class FunctionInvocationVariableFix extends IntroduceFix {

        FunctionInvocationVariableFix(BaseDocument doc, FunctionInvocation functionInvocation) {
            super(doc, functionInvocation);
        }

        protected String getVariableName() {
            FunctionInvocation functionInvocation = (FunctionInvocation) node;
            String guessName = CodeUtils.extractFunctionName(functionInvocation);
            return getVariableName(guessName);
        }
    }

    private static boolean isInside(int carret, int left, int right) {
        return carret >= left && carret <= right;
    }

    private static boolean isBefore(int carret, int margin) {
        return carret <= margin;
    }

    private static String firstToLower(String name) {
        if (name.length() == 0) {
            return null;
        }

        String cand = Character.toLowerCase(name.charAt(0)) + name.substring(1);

        /*if (isKeyword(cand)) {
        cand = "a" + name;
        }*/
        return cand;
    }
}
