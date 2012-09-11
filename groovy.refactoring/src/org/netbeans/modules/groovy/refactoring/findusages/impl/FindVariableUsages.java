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

package org.netbeans.modules.groovy.refactoring.findusages.impl;

import java.util.List;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.refactoring.GroovyRefactoringElement;

/**
 *
 * @author Martin Janicek
 */
public class FindVariableUsages extends AbstractFindUsages {

    public FindVariableUsages(GroovyRefactoringElement element) {
        super(element);
    }

    @Override
    protected List<AbstractFindUsagesVisitor> getVisitors(ModuleNode moduleNode, String defClass) {
        return singleVisitor(new FindVariableUsagesVisitor(moduleNode, defClass));
    }

    @Override
    protected ElementKind getElementKind() {
        return ElementKind.VARIABLE;
    }


    private class FindVariableUsagesVisitor extends AbstractFindUsagesVisitor {

        private final String declaringClassName;
        private final String variableName;

        
        public FindVariableUsagesVisitor(ModuleNode moduleNode, String declaringClass) {
           super(moduleNode);
           this.declaringClassName = element.getDeclaringClassName();
           this.variableName = element.getName();
        }

        @Override
        public void visitField(FieldNode field) {
            if (!field.isSynthetic() && variableName.equals(field.getName())) {
                usages.add(field);
            }
            super.visitField(field);
        }

        @Override
        public void visitProperty(PropertyNode property) {
            if (!property.isSynthetic() && variableName.equals(property.getName())) {
                usages.add(property.getField());
            }
            super.visitProperty(property);
        }

        @Override
        public void visitVariableExpression(VariableExpression expression) {
            VariableExpression variableExpression = ((VariableExpression) expression);
            Variable variable = variableExpression.getAccessedVariable();
            if (variable != null) {
                // FIXME: we have to check also variable declaration type somehow
                // So far if there are two classes containing both field with the
                // same name, the result won't be correct
                if (variableName.equals(variable.getName())) {
                    usages.add(expression);
                }
            }
            super.visitVariableExpression(expression);
        }
    }
}
