/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.editor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;

/**
 *
 * @author Martin Adamek
 */
public final class VariableScopeVisitor extends ClassCodeVisitorSupport {

    private final SourceUnit sourceUnit;
    private final AstPath path;
    private final ASTNode leaf;
    private final ASTNode leafParent;
    private final Set<ASTNode> occurrences = new HashSet<ASTNode>();
    private ASTNode scope;

    public VariableScopeVisitor(SourceUnit sourceUnit, AstPath path) {
        this.sourceUnit = sourceUnit;
        this.path = path;
        this.leaf = path.leaf();
        this.leafParent = path.leafParent();
    }

    public Set<ASTNode> getOccurrences() {
        return occurrences;
    }

    public ASTNode getScope() {
        return scope;
    }

    public void collect() {
        for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
            scope = it.next();
            if (leaf instanceof Variable) {
                if (collectVariable()) {
                    return;
                }
            } else {
                collectFromWholeModule();
            }
        }
    }

    private boolean collectVariable() {
        if (isDeclaringVariable(scope, (Variable) leaf)) {
            if (scope instanceof MethodNode) {
                MethodNode methodNode = (MethodNode) scope;
                String name = ((Variable) leaf).getName();
                for (Parameter parameter : methodNode.getParameters()) {
                    if (name.equals(parameter.getName())) {
                        occurrences.add(parameter);
                    }
                }
                // we checked parameters, now let's go to method body,
                // but use parent's method, as our impl is refusing to
                // get in method if there is parameter with such name
                super.visitMethod((MethodNode) scope);
                return true;
            } else if (scope instanceof ClassNode) {
                visitClass((ClassNode) scope);
                return true;
            } else {
                scope.visit(this);
                return true;
            }
        } else {
            // nobody is declaring this variable, so it is probably
            // inherited from super class
            if (scope instanceof ClassNode) {
                visitClass((ClassNode) scope);
                return true;
            }
        }
        return false;
    }

    private boolean collectFromWholeModule() {
        ModuleNode moduleNode = (ModuleNode) path.root();
        for (Object object : moduleNode.getClasses()) {
            visitClass((ClassNode) object);
        }
        return false;
    }

    private static boolean isDeclaringVariable(ASTNode parent, Variable variable) {
        String name = variable.getName();
        if (parent instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) parent;
            for (Parameter parameter : methodNode.getParameters()) {
                if (name.equals(parameter.getName())) {
                    return true;
                }
            }
        } else if (parent instanceof ClassNode) {
            ClassNode classNode = (ClassNode) parent;
            for (Object object : classNode.getFields()) {
                FieldNode fieldNode = (FieldNode) object;
                if (name.equals(fieldNode.getName())) {
                    return true;
                }
            }
            for (Object object : classNode.getProperties()) {
                PropertyNode propertyNode = (PropertyNode) object;
                if (name.equals(propertyNode.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isSameMethod(MethodNode methodNode, MethodCallExpression methodCall) {
        if (methodNode.getName().equals(methodCall.getMethodAsString())) {
            ArgumentListExpression argumentList = (ArgumentListExpression) methodCall.getArguments();
            // not comparing parameter types for now, only their count
            // is it even possible to make some check for parameter types?
            if (argumentList.getExpressions().size() == methodNode.getParameters().length) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSameMethod(MethodNode methodNode1, MethodNode methodNode2) {
        if (methodNode1.getName().equals(methodNode2.getName())) {
            // not comparing parameter types for now, only their count
            // is it even possible to make some check for parameter types?
            if (methodNode1.getParameters().length == methodNode2.getParameters().length) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSameMethod(MethodCallExpression methodCall1, MethodCallExpression methodCall2) {
        if (methodCall1.getMethodAsString().equals(methodCall2.getMethodAsString())) {
            int size1 = getParameterCount(methodCall1);
            int size2 = getParameterCount(methodCall2);
            // not comparing parameter types for now, only their count
            // is it even possible to make some check for parameter types?
            if (size1 >= 0 && size1 == size2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tries to calculate number of method parameters.
     *
     * @param methodCall called method
     * @return number of method parameters,
     * 1 in case of named parameters represented by map,
     * or -1 if it is unknown
     */
    private static int getParameterCount(MethodCallExpression methodCall) {
        Expression expression = methodCall.getArguments();
        if (expression instanceof ArgumentListExpression) {
            return ((ArgumentListExpression) expression).getExpressions().size();
        } else if (expression instanceof NamedArgumentListExpression) {
            // this is in fact map acting as named parameters
            // lets return size 1
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    @Override
    public void visitVariableExpression(VariableExpression variableExpression) {
        if (leaf instanceof Variable && ((Variable) leaf).getName().equals(variableExpression.getName())) {
            occurrences.add(variableExpression);
        }
        super.visitVariableExpression(variableExpression);
    }

    @Override
    public void visitField(FieldNode fieldNode) {
        if (leaf instanceof Variable && ((Variable) leaf).getName().equals(fieldNode.getName())) {
            occurrences.add(fieldNode);
        }
        super.visitField(fieldNode);
    }

    @Override
    public void visitProperty(PropertyNode propertyNode) {
        if (leaf instanceof Variable && ((Variable) leaf).getName().equals(propertyNode.getName())) {
            occurrences.add(propertyNode);
        }
        super.visitProperty(propertyNode);
    }

    @Override
    public void visitMethod(MethodNode methodNode) {
        if (leaf instanceof Variable) {
            String name = ((Variable) leaf).getName();
            for (Parameter parameter : methodNode.getParameters()) {
                if (name.equals(parameter.getName())) {
                    return;
                }
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof MethodCallExpression) {
            MethodCallExpression methodCallExpression = (MethodCallExpression) leafParent;
            if (isSameMethod(methodNode, methodCallExpression)) {
                occurrences.add(methodNode);
            }
        } else if (leaf instanceof MethodNode) {
            if (isSameMethod(methodNode, (MethodNode) leaf)) {
                occurrences.add(methodNode);
            }
        }
        super.visitMethod(methodNode);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression methodCall) {

        if (leaf instanceof MethodNode) {
            MethodNode method = (MethodNode) leaf;
            if (isSameMethod(method, methodCall)) {
                occurrences.add(methodCall);
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof MethodCallExpression) {
            if (isSameMethod(methodCall, (MethodCallExpression) leafParent)) {
                occurrences.add(methodCall);
            }
        }
        super.visitMethodCallExpression(methodCall);
    }

    @Override
    public void visitClassExpression(ClassExpression clazz) {
        if (leaf instanceof ClassNode) {
            ClassNode classNode = (ClassNode) leaf;
            if (clazz.getText().equals(classNode.getName())) {
                occurrences.add(clazz);
            }
        } else if (leaf instanceof ClassExpression) {
            if (clazz.getText().equals(((ClassExpression) leaf).getText())) {
                occurrences.add(clazz);
            }
        }
        super.visitClassExpression(clazz);
    }

    @Override
    public void visitClass(ClassNode classNode) {
        if (leaf instanceof ClassExpression) {
            if (classNode.getName().equals(((ClassExpression) leaf).getText())) {
                occurrences.add(classNode);
            }
        } else if (leaf instanceof ClassNode) {
            if (classNode.getName().equals(((ClassNode) leaf).getName())) {
                occurrences.add(classNode);
            }
        }
        super.visitClass(classNode);
    }



}
