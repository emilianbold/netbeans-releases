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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.modules.groovy.refactoring.GroovyRefactoringElement;

/**
 *
 * @author Martin Janicek
 */
public class FindMethodUsages extends AbstractFindUsages {

    public FindMethodUsages(GroovyRefactoringElement element) {
        super(element);
    }

    @Override
    protected AbstractFindUsagesVisitor getVisitor(ModuleNode moduleNode, String defClass) {
        return new FindMethodUsagesVisitor(moduleNode);
    }

    private class FindMethodUsagesVisitor extends AbstractFindUsagesVisitor {

        private final String declaringClassName;
        private final String findingMethod;

        public FindMethodUsagesVisitor(ModuleNode moduleNode) {
            super(moduleNode);
            this.declaringClassName = element.getFQN();
            this.findingMethod = element.getName();
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression methodCall) {
            Expression expression = methodCall.getObjectExpression();

            if (expression instanceof VariableExpression) {
                Variable variable = ((VariableExpression) expression).getAccessedVariable();

                // Most frequent situations like:
                // 1. String whatever = "Oops!"
                // 2. whatever.concat("")
                if (variable != null) {
                    ClassNode methodCallType = variable.getType();
                    if (!declaringClassName.equals(methodCallType.getName())) {
                        return;
                    }

                    List<MethodNode> methods;
                    if (methodCallType.isResolved()) {
                        methods = methodCallType.getMethods(findingMethod);
                    } else {
                        methods = methodCallType.redirect().getMethods(findingMethod);
                    }
                    for (MethodNode method : methods) {
                        usages.add(methodCall);
                    }
                } else {
                    if (!declaringClassName.equals(methodCall.getType().getName())) {
                        return;
                    }
                    ClassNode declaringClass = element.getDeclaringClass();
                    List<MethodNode> methods;
                    if (declaringClass.isResolved()) {
                        methods = declaringClass.getMethods(findingMethod);
                    } else {
                        methods = declaringClass.redirect().getMethods(findingMethod);
                    }
                    for (MethodNode method : methods) {
                        usages.add(methodCall);
                    }
                }
            }
            super.visitMethodCallExpression(methodCall);
        }

        @Override
        public void visitConstructorCallExpression(ConstructorCallExpression constructorCall) {
            ClassNode constructorCallType = constructorCall.getType();

            // Situations like: "new SomeClass().destroyWorldMethod()"
            if (constructorCallType != null) {
                List<MethodNode> methods;
                if (constructorCallType.isResolved()) {
                    methods = constructorCallType.getMethods(findingMethod);
                } else {
                    methods = constructorCallType.redirect().getMethods(findingMethod);
                }
                for (MethodNode method : methods) {
                    usages.add(constructorCall);
                }
            }
            super.visitConstructorCallExpression(constructorCall);
        }

        @Override
        public void visitConstantExpression(ConstantExpression expression) {
            // Method calls on this object without specifying any variable type
            // For example: "this.destroyWorldMethod()" or just "destroyWorldMethod()"
            if (findingMethod.equals(expression.getConstantName())) {
                usages.add(expression);
            }
            super.visitConstantExpression(expression);
        }
    }
}
