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

import java.util.Collection;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedVariable;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class UninitializedVariableRule  extends PHPRule implements VarStackReadingRule {
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
    public void visit(Variable variable) {
        ASTNode parent = context.path.get(0);
        
        if (parent instanceof Assignment) {
            Assignment assignment = (Assignment) parent;
            
            if (assignment.getLeftHandSide() == variable){
                // variable is just being initialized, do not check it 
                return;
            }
        } else if (parent instanceof FunctionName 
                || parent instanceof SingleFieldDeclaration
                || parent instanceof FieldAccess
                || parent instanceof StaticFieldAccess){
            
            return;
        }
        
        if (parent instanceof ForEachStatement) {
            ForEachStatement forEachStatement = (ForEachStatement) parent;
            
            if (forEachStatement.getExpression() != variable){
                return;
            }
        } else if (parent instanceof ArrayAccess) {
            if (context.path.size() > 1) {
                ASTNode grandpa = context.path.get(1);
                
                if (grandpa instanceof FieldAccess || grandpa instanceof StaticFieldAccess
                        // issue #157823
                        || grandpa instanceof ArrayAccess) {
                    return;
                }
            }
        }
        
        check(variable);
    }

    private void check(Variable var) {
        if (var.getName() instanceof Identifier) {
            Identifier identifier = (Identifier) var.getName();
            String varName = identifier.getName();
            
            if (varName != null && !context.variableStack.isVariableDefined(varName)) {
                // check the globals from included files
                Collection<IndexedVariable> topLevelVars = context.getIndex().getTopLevelVariables((PHPParseResult) context.parserResult,
                        "$" + varName, QuerySupport.Kind.EXACT); //NOI18N
                
                for (IndexedConstant topLevelVar : topLevelVars) {
                    if (topLevelVar.isResolved()){
                        return;
                    }
                }
                
                OffsetRange range = new OffsetRange(var.getStartOffset(), var.getEndOffset());

                Hint hint = new Hint(UninitializedVariableRule.this, getDisplayName(),
                        context.parserResult.getSnapshot().getSource().getFileObject(), range, null, 500);

                addResult(hint);
            }
        }
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UninitializedVariableRule.class, "UninitializedVariableDispName");
    }
    
    @Override
    public boolean getDefaultEnabled() {
        return false;
    }
}
