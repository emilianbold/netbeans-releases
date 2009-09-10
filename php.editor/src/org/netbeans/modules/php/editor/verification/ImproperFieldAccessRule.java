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
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression.OperatorType;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.util.NbBundle;

/**
 *
 * @author Radek Matous
 */
public class ImproperFieldAccessRule extends PHPRule implements VarStackReadingRule {
    private String insideClsName = "";//NOI18N 

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public String getId() {
        return "improper.field.access"; //NOI18N
    }
    
    @Override
    public void visit(ClassDeclaration classDeclaration) {
        insideClsName = CodeUtils.extractClassName(classDeclaration);//NOI18N
        super.visit(classDeclaration);
    }

    @Override
    public void leavingClassDeclaration(ClassDeclaration classDeclaration) {
        super.leavingClassDeclaration(classDeclaration);
        insideClsName = "";//NOI18N 
    }
    
    @Override
    public void visit(InfixExpression infixExpression) {
        OperatorType operator = infixExpression.getOperator();
        if (operator.equals(OperatorType.MINUS) || operator.equals(OperatorType.LGREATER)) {
            Expression left = infixExpression.getLeft();
            if (left instanceof Variable) {
                Variable var = (Variable) left;
                if (var.isDollared() && "this".equals(extractVariableName(var))) {//NOI18N
                    addHint(var);
                }
            }
        }
        super.visit(infixExpression);
    }

    @Override
    public void visit(Program program) {
        insideClsName = "";//NOI18N 
        super.visit(program);
    }

    @Override
    public void visit(StaticFieldAccess staticFieldAccess) {
        Variable field = staticFieldAccess.getField();        
        String className = CodeUtils.extractUnqualifiedClassName(staticFieldAccess);
        if (className.equals("self")) {//NOI18N
            className = insideClsName;
        }
        int modifiers = (insideClsName.equals(className)) ? PHPIndex.ANY_ATTR : 
            BodyDeclaration.Modifier.PUBLIC;

        Collection<IndexedConstant> flds = null;        
        for (IndexedElement indexedElement : context.getIndex().getClassAncestors(null, className)) {
            String clazzName = indexedElement.getName();
            flds = getFields(context.getIndex(), clazzName, field, modifiers);
            if (!flds.isEmpty()) {
                break;
            } else {
                if (insideClsName.equals(clazzName)) {
                    modifiers = BodyDeclaration.Modifier.PUBLIC | BodyDeclaration.Modifier.PROTECTED;
                } else {
                    modifiers = BodyDeclaration.Modifier.PUBLIC;
                }
            }
        }
        
        if (flds == null || flds.isEmpty()) {
            addHint(field);
        }
        super.visit(staticFieldAccess);
    }
    
    @Override
    public void visit(FieldAccess fieldAccess) {
        super.visit(fieldAccess);
        Variable field = fieldAccess.getField();
        if (field.isDollared()) {
            if (!context.variableStack.isVariableDefined(extractVariableName(field))) {
                addHint(field);
            }
        } 
        super.visit(fieldAccess);
    }

    private static String extractVariableName(Variable var) {
        if (var.getName() instanceof Identifier) {
            Identifier id = (Identifier) var.getName();
            return id.getName();
        } else {
            if (var.getName() instanceof Variable) {
                Variable name = (Variable) var.getName();
                return extractVariableName(name);
            }
        }

        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(ImproperFieldAccessRule.class, "ImproperFieldAccessDesc");//NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ImproperFieldAccessRule.class, "ImproperFieldAccessDispName");//NOI18N
    }

    private void addHint(Variable field) {
        OffsetRange range = new OffsetRange(field.getStartOffset(), field.getEndOffset());
        Hint hint = new Hint(ImproperFieldAccessRule.this, getDisplayName(), context.parserResult.getSnapshot().getSource().getFileObject(), range, null, 500);
        addResult(hint);
    }

    private Collection<IndexedConstant> getFields(PHPIndex index, String clsName, Variable field, int modifiers) {
        Collection<IndexedConstant> retval = new ArrayList<IndexedConstant>();
        final String varName = extractVariableName(field);
        Collection<IndexedConstant> flds = index.getFields(null, clsName,varName, QuerySupport.Kind.PREFIX, modifiers);
        for (IndexedConstant indexedConstant : flds) {
            String fldName = indexedConstant.getName();
            fldName = fldName.charAt(0) == '$' ? fldName.substring(1) : fldName;//NOI18N
            if (varName.equals(fldName)) {
                retval.add(indexedConstant);
            }            
        }
        return retval;
    }
    
    @Override
    public boolean getDefaultEnabled() {
        return false;
    }
}