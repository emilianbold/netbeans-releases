/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class UninitializedVariableRule  extends PHPRule {
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public String getId() {
        return "unitialized.variable"; //NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(UninitializedVariableRule.class, "UninitializedVariableDesc");
    }

    @Override
    public void visit(Assignment assignment) {
        doAst(assignment.getRightHandSide());
    }

    @Override
    public void visit(FunctionInvocation functionInvocation) {
        for (Expression expr : functionInvocation.getParameters()){
            doAst(expr);
        }
    }
    
    private void doAst(ASTNode node) {
        VariableExtractor extractor = new VariableExtractor();
        node.accept(extractor);

        for (Variable var : extractor.variables) {
            check(var);
        }
    }

    private void check(Variable var) {
        if (var.getName() instanceof Identifier) {
            Identifier identifier = (Identifier) var.getName();
            String varName = identifier.getName();
            
            if (varName != null && !context.variableStack.isVariableDefined(varName)) {
                OffsetRange range = new OffsetRange(var.getStartOffset(), var.getEndOffset());

                Hint hint = new Hint(UninitializedVariableRule.this, getDescription(),
                        context.compilationInfo.getFileObject(), range, null, 500);

                addResult(hint);
            }
        }
    }

    public String getDisplayName() {
        return getDescription();
    }
    
    private class VariableExtractor extends DefaultVisitor{
        Collection<Variable> variables = new ArrayList<Variable>();

        @Override
        public void visit(Variable node) {
            variables.add(node);
        }
    }
}
