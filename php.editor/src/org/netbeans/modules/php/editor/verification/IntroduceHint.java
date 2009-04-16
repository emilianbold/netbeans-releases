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
import java.util.Set;
import java.util.Stack;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule.AstRule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Radek Matous
 */
public class IntroduceHint implements AstRule {

    public String getId() {
        return "introduce.fix"; //NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(IntroduceHint.class, "IntroduceHintDesc");//NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(IntroduceHint.class, "IntroduceHintDisplayName");//NOI18N
    }

    public boolean showInTasklist() {
        return false;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }

    void check(RuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        final BaseDocument doc = context.doc;
        final int caretOffset = context.caretOffset;
        int lineBegin = -1;
        int lineEnd = -1;
        try {
            lineBegin = caretOffset > 0 ? Utilities.getRowStart(doc, caretOffset) : -1;
            lineEnd = (lineBegin != -1) ? Utilities.getRowEnd(doc, caretOffset) : -1;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (lineBegin != -1 && lineEnd != -1) {
            IntroduceFixVisitor introduceFixVisitor = new IntroduceFixVisitor(doc, lineBegin, lineEnd);
            phpParseResult.getProgram().accept(introduceFixVisitor);
            InstanceCreationVariableFix variableFix = introduceFixVisitor.getInstanceCreationVariableFix();
            if (variableFix != null) {
                hints.add(new Hint(IntroduceHint.this, getDisplayName(),
                        context.parserResult.getSnapshot().getSource().getFileObject(), variableFix.getOffsetRange(),
                        Collections.<HintFix>singletonList(variableFix), 500));
            }
        }
    }

    @Override
    public Set<? extends Object> getKinds() {
        return Collections.singleton(PHPHintsProvider.INTRODUCE_HINT);
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    private class IntroduceFixVisitor extends DefaultVisitor {

        private int lineBegin;
        private int lineEnd;
        private BaseDocument doc;
        private InstanceCreationVariableFix variableFix;
        private List<Variable> variables;
        private Stack<ClassInstanceCreation> stack;

        IntroduceFixVisitor(BaseDocument doc, int lineBegin, int lineEnd) {
            this.doc = doc;
            this.lineBegin = lineBegin;
            this.lineEnd = lineEnd;
            this.variables = new ArrayList<Variable>();
            this.stack = new Stack<ClassInstanceCreation>();
        }

        @Override
        public void scan(ASTNode node) {
            if (node != null && (isBefore(node.getStartOffset(), lineEnd) || variableFix != null)) {
                super.scan(node);
            }
        }

        @Override
        public void visit(Variable node) {
            variables.add(node);
            super.visit(node);
        }

        @Override
        public void visit(Assignment node) {
            if (isInside(node.getStartOffset(), lineBegin, lineEnd)) {
                if (node.getRightHandSide() instanceof ClassInstanceCreation) {
                    this.stack.push((ClassInstanceCreation) node.getRightHandSide());
                }
            }
            super.visit(node);
        }

        @Override
        public void visit(ClassInstanceCreation node) {
            boolean assignmentExists = false;
            if (!this.stack.isEmpty()) {
                ClassInstanceCreation fromStack = this.stack.pop();
                if (node.equals(fromStack)) {
                    assignmentExists = true;
                }
            }
            if (!assignmentExists && isInside(node.getStartOffset(), lineBegin, lineEnd)) {
                variableFix = new InstanceCreationVariableFix(doc, node);
            }
            super.visit(node);
        }

        /**
         * @return or null
         */
        public InstanceCreationVariableFix getInstanceCreationVariableFix() {
            if (variableFix != null) {
                variableFix.setVariables(variables);
            }
            return variableFix;
        }
    }

    private class InstanceCreationVariableFix implements HintFix {

        private BaseDocument doc;
        private ClassInstanceCreation instanceCreation;
        private List<Variable> variables;

        InstanceCreationVariableFix(BaseDocument doc, ClassInstanceCreation instanceCreation) {
            this.doc = doc;
            this.instanceCreation = instanceCreation;
        }

        OffsetRange getOffsetRange() {
            return new OffsetRange(instanceCreation.getStartOffset(), instanceCreation.getEndOffset());
        }

        public String getDescription() {
            return IntroduceHint.this.getDescription();
        }

        public void implement() throws Exception {
            int textOffset = getTextOffset(instanceCreation);
            String variableName = getVariableName(instanceCreation);
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

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }

        private int getTextOffset(ClassInstanceCreation instanceCreation) {
            return instanceCreation.getStartOffset();
        }

        private String getVariableName(ClassInstanceCreation instanceCreation) {
            String guessName = CodeUtils.extractClassName(instanceCreation.getClassName());
            guessName = firstToLower(guessName);
            String proposedName = guessName;
            int incr = -1;
            boolean cont = true;

            while (cont) {
                if (incr != -1) {
                    proposedName = String.format("%s%d", guessName, incr);//NOI18N
                }
                cont = false;
                for (Variable variable : variables) {
                    String varName = CodeUtils.extractVariableName(variable);
                    if (varName != null) {
                        if (variable.isDollared()) {
                            varName = varName.substring(1);
                        }
                        if (proposedName.equals(varName)) {
                            incr++;
                            cont = true;
                            break;
                        }
                    }
                }
            }
            return proposedName;
        }

        /**
         * @param variables the variables to set
         */
        public void setVariables(List<Variable> variables) {
            this.variables = variables;
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
