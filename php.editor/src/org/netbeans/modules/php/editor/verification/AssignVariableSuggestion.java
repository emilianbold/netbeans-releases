/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.IgnoreError;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Radek Matous
 */
public class AssignVariableSuggestion extends AbstractSuggestion {

    @Override
    public String getId() {
        return "assign.variable.hint"; //NOI18N
    }

    @Override
    @Messages("AssignVariableHintDesc=Assign Return Value To New Variable")
    public String getDescription() {
        return Bundle.AssignVariableHintDesc();
    }

    @Override
    @Messages("AssignVariableHintDisplayName=Introduce Variable")
    public String getDisplayName() {
        return Bundle.AssignVariableHintDisplayName();
    }

    @Override
    void compute(PHPRuleContext context, List<Hint> hints, int caretOffset) throws BadLocationException {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        int lineBegin = caretOffset > 0 ? Utilities.getRowStart(context.doc, caretOffset) : -1;
        int lineEnd = (lineBegin != -1) ? Utilities.getRowEnd(context.doc, caretOffset) : -1;
        if (lineBegin != -1 && lineEnd != -1 && caretOffset > lineBegin) {
            IntroduceFixVisitor introduceFixVisitor = new IntroduceFixVisitor(context.doc, lineBegin, lineEnd);
            phpParseResult.getProgram().accept(introduceFixVisitor);
            IntroduceFix variableFix = introduceFixVisitor.getIntroduceFix();
            if (variableFix != null) {
                hints.add(new Hint(AssignVariableSuggestion.this, getDisplayName(),
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
                if (expression instanceof IgnoreError) {
                    expression = ((IgnoreError)expression).getExpression();
                }
                String guessName = null;
                if (expression instanceof ClassInstanceCreation) {
                    ClassInstanceCreation instanceCreation = (ClassInstanceCreation) expression;
                    guessName = CodeUtils.extractClassName(instanceCreation.getClassName());
                } else if (expression instanceof MethodInvocation) {
                    MethodInvocation methodInvocation = (MethodInvocation) expression;
                    guessName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
                } else if (expression instanceof FunctionInvocation) {
                    FunctionInvocation functionInvocation = (FunctionInvocation) expression;
                    guessName = CodeUtils.extractFunctionName(functionInvocation);
                } else if (expression instanceof StaticMethodInvocation) {
                    StaticMethodInvocation methodInvocation = (StaticMethodInvocation) expression;
                    guessName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
                }

                if (guessName != null) {
                    fix = new IntroduceFixImpl(doc, node, guessName);
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

        @Override
        public boolean isInteractive() {
            return false;
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        public void setVariables(List<Variable> variables) {
            this.variables = variables;
        }

        @Override
        public String getDescription() {
            return AssignVariableSuggestion.this.getDescription();
        }

        @Override
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

    private class IntroduceFixImpl extends IntroduceFix {
        private final String guessName;
        IntroduceFixImpl(final BaseDocument doc, final ASTNode node, final String variable) {
            super(doc, node);
            this.guessName = variable;
        }

        @Override
        protected String getVariableName() {
            return super.getVariableName(guessName);
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
