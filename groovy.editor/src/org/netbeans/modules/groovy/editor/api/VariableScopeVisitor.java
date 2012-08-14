/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.groovy.editor.api;

import java.util.HashSet;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.groovy.editor.api.AstUtilities.FakeASTNode;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;

/**
 * Visitor for finding occurrences of the class types, variables and methods.
 *
 * @author Martin Adamek
 * @author Martin Janicek
 */
public final class VariableScopeVisitor extends TypeVisitor {

    private final Set<ASTNode> occurrences;
    private final ASTNode leafParent;


    public VariableScopeVisitor(SourceUnit sourceUnit, AstPath path, BaseDocument doc, int cursorOffset) {
        super(sourceUnit, path, doc, cursorOffset, true);
        this.occurrences = new HashSet<ASTNode>();
        this.leafParent = path.leafParent();
    }

    public Set<ASTNode> getOccurrences() {
        return occurrences;
    }

    @Override
    public void visitArrayExpression(ArrayExpression visitedArray) {
        ClassNode visitedType = visitedArray.getElementType();
        String visitedName = removeParentheses(visitedType.getName());

        if (leaf instanceof FieldNode) {
            if (helper.isCaretOnFieldType((FieldNode) leaf)) {
                addOccurrences(visitedType, ((FieldNode) leaf).getType());
            }
        } else if (leaf instanceof PropertyNode) {
            if (helper.isCaretOnFieldType(((PropertyNode) leaf).getField())) {
                addOccurrences(visitedType, ((PropertyNode) leaf).getField().getType());
            }
        } else if (leaf instanceof Parameter) {
            if (helper.isCaretOnParamType((Parameter) leaf)) {
                addOccurrences(visitedType, ((Parameter) leaf).getType());
            }
        } else if (leaf instanceof Variable) {
            String varName = removeParentheses(((Variable) leaf).getName());
            if (varName.equals(visitedName)) {
                occurrences.add(new FakeASTNode(visitedArray.getElementType(), visitedName));
            }
        } else if (leaf instanceof MethodNode) {
            if (helper.isCaretOnReturnType((MethodNode) leaf)) {
                addOccurrences(visitedType, ((MethodNode) leaf).getReturnType());
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            PropertyExpression property = (PropertyExpression) leafParent;
            if (visitedName.equals(property.getPropertyAsString())) {
                occurrences.add(new FakeASTNode(visitedArray.getElementType(), visitedName));
            }
        } else if (leaf instanceof DeclarationExpression) {
            DeclarationExpression declarationExpression = (DeclarationExpression) leaf;
            if (!declarationExpression.isMultipleAssignmentDeclaration()) {
                addOccurrences(visitedType, declarationExpression.getVariableExpression().getType());
            }
        } else if (leaf instanceof ClassExpression) {
            addOccurrences(visitedType, ((ClassExpression) leaf).getType());
        } else if (leaf instanceof ArrayExpression) {
            addOccurrences(visitedType, ((ArrayExpression) leaf).getElementType());
        } else if (leaf instanceof ForStatement) {
            addOccurrences(visitedType, ((ForStatement) leaf).getVariableType());
        }
        super.visitArrayExpression(visitedArray);
    }

    @Override
    protected void visitParameters(Parameter[] parameters, Variable variable) {
        // method is declaring given variable, let's visit only the method,
        // but we need to check also parameters as those are not part of method visit
        for (Parameter parameter : parameters) {
            if (helper.isCaretOnParamType(parameter)) {
                occurrences.add(new FakeASTNode(parameter.getType(), parameter.getType().getNameWithoutPackage()));
            } else {
                if (parameter.getName().equals(variable.getName())) {
                    occurrences.add(parameter);
                    break;
                }
            }
        }
        super.visitParameters(parameters, variable);
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        if (expression.isParameterSpecified() && (leaf instanceof Variable)) {
            visitParameters(expression.getParameters(), (Variable) leaf);
        }
        super.visitClosureExpression(expression);
    }


    @Override
    protected boolean isValidToken(Token<? extends GroovyTokenId> currentToken, Token<? extends GroovyTokenId> previousToken) {
        // cursor must be positioned on identifier, otherwise occurences doesn't make sense
        // second check is here because we want to have occurences also at the end of the identifier (see issue #155574)
        return currentToken.id() == GroovyTokenId.IDENTIFIER || previousToken.id() == GroovyTokenId.IDENTIFIER;
    }

    @Override
    public void visitVariableExpression(VariableExpression variableExpression) {
        if (leaf instanceof FieldNode) {
            addVariableExpressionOccurrences(variableExpression, (FieldNode) leaf);
        } else if (leaf instanceof PropertyNode) {
            addVariableExpressionOccurrences(variableExpression, ((PropertyNode) leaf).getField());
        } else if (leaf instanceof Parameter) {
            if (!helper.isCaretOnParamType(((Parameter) leaf)) && ((Parameter) leaf).getName().equals(variableExpression.getName())) {
                occurrences.add(variableExpression);
            }
        } else if (leaf instanceof Variable) {
            if (((Variable) leaf).getName().equals(variableExpression.getName())) {
                occurrences.add(variableExpression);
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            PropertyExpression property = (PropertyExpression) leafParent;
            if (variableExpression.getName().equals(property.getPropertyAsString())) {
                occurrences.add(variableExpression);
                return;
            }
        }
        super.visitVariableExpression(variableExpression);
    }

    private void addVariableExpressionOccurrences(VariableExpression visited, FieldNode findingNode) {
        if (helper.isCaretOnFieldType(findingNode)) {
            addOccurrences(visited.getType(), findingNode.getType());
        } else {
            final String visitedVariableName = visited.getName();
            final String fieldName = removeParentheses(findingNode.getName());
            if (visitedVariableName.equals(fieldName)) {
                occurrences.add(visited);
            }
        }
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        if (leaf instanceof FieldNode) {
            addDeclarationExpressionOccurrences(expression, (FieldNode) leaf);
        } else if (leaf instanceof PropertyNode) {
            addDeclarationExpressionOccurrences(expression, ((PropertyNode) leaf).getField());
        } else if (leaf instanceof Parameter) {
            if (helper.isCaretOnParamType((Parameter) leaf)) {
                addDeclarationExpressionOccurrences(expression, ((Parameter) leaf).getType());
            }
        } else if (leaf instanceof DeclarationExpression) {
            DeclarationExpression declaration = ((DeclarationExpression) leaf);
            if (!declaration.isMultipleAssignmentDeclaration()) {
                addDeclarationExpressionOccurrences(expression, declaration.getVariableExpression().getType());
            } else {
                addDeclarationExpressionOccurrences(expression, declaration.getTupleExpression().getType());
            }
        } else if (leaf instanceof ClassExpression) {
            VariableExpression variable = expression.getVariableExpression();
            if (!variable.isDynamicTyped()) {
                addDeclarationExpressionOccurrences(expression, ((ClassExpression) leaf).getType());
            }
        } else if (leaf instanceof ArrayExpression) {
            addDeclarationExpressionOccurrences(expression, ((ArrayExpression) leaf).getElementType());
        } else if (leaf instanceof ClassNode) {
            ClassNode clazz = (ClassNode) leaf;

            if (!expression.isMultipleAssignmentDeclaration()) {
                VariableExpression variable = expression.getVariableExpression();
                if (!variable.isDynamicTyped()) {
                    if (clazz.getName().equals(variable.getType().getName())) {
                        occurrences.add(new FakeASTNode(variable.getType(), clazz.getNameWithoutPackage()));
                    }
                }
            }
        } else if (leaf instanceof MethodNode) {
            MethodNode method = (MethodNode) leaf;
            if (helper.isCaretOnReturnType(method) && !method.isDynamicReturnType()) {
                addDeclarationExpressionOccurrences(expression, method.getReturnType());
            }
        } else if (leaf instanceof ForStatement) {
            addDeclarationExpressionOccurrences(expression, ((ForStatement) leaf).getVariableType());
        }
        super.visitDeclarationExpression(expression);
    }

    private void addDeclarationExpressionOccurrences(DeclarationExpression visited, FieldNode findingNode) {
        if (helper.isCaretOnFieldType(findingNode)) {
            addDeclarationExpressionOccurrences(visited, findingNode.getType());
        } else if (helper.isCaretOnGenericType(findingNode.getType())) {
            addDeclarationExpressionOccurrences(visited, helper.getGenericType(findingNode.getType()));
        }
    }

    private void addDeclarationExpressionOccurrences(DeclarationExpression visited, ClassNode type) {
        if (!visited.isMultipleAssignmentDeclaration()) {
            addOccurrences(visited.getVariableExpression().getType(), type);
        } else {
            addOccurrences(visited.getTupleExpression().getType(), type);
        }
    }

    @Override
    public void visitField(FieldNode fieldNode) {
        final ClassNode visitedType = fieldNode.getType();
        if (leaf instanceof FieldNode) {
            addFieldOccurrences(fieldNode, (FieldNode) leaf);
        } else if (leaf instanceof PropertyNode) {
            addFieldOccurrences(fieldNode, ((PropertyNode) leaf).getField());
        } else if (leaf instanceof Parameter) {
            if (helper.isCaretOnParamType(((Parameter) leaf))) {
                addOccurrences(visitedType, ((Parameter) leaf).getType());
            }
        } else if (leaf instanceof Variable && ((Variable) leaf).getName().equals(fieldNode.getName())) {
            occurrences.add(fieldNode);
        } else if (leaf instanceof MethodNode) {
            if (helper.isCaretOnReturnType((MethodNode) leaf)) {
                addOccurrences(visitedType, ((MethodNode) leaf).getReturnType());
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            PropertyExpression property = (PropertyExpression) leafParent;
            if (fieldNode.getName().equals(property.getPropertyAsString())) {
                occurrences.add(fieldNode);
            }
        } else if (leaf instanceof DeclarationExpression) {
            DeclarationExpression declarationExpression = (DeclarationExpression) leaf;
            if (!declarationExpression.isMultipleAssignmentDeclaration()) {
                addOccurrences(visitedType, declarationExpression.getVariableExpression().getType());
            } else {
                addOccurrences(visitedType, declarationExpression.getTupleExpression().getType());
            }
        } else if (leaf instanceof ClassExpression) {
            addOccurrences(visitedType, ((ClassExpression) leaf).getType());
        } else if (leaf instanceof ArrayExpression) {
            addOccurrences(visitedType, ((ArrayExpression) leaf).getElementType());
        } else if (leaf instanceof ForStatement) {
            addOccurrences(visitedType, ((ForStatement) leaf).getVariableType());
        }
        super.visitField(fieldNode);
    }

    private void addFieldOccurrences(FieldNode visitedField, FieldNode findingNode) {
        if (helper.isCaretOnFieldType(findingNode)) {
            addOccurrences(visitedField.getType(), findingNode.getType());
        } else if (helper.isCaretOnGenericType(findingNode.getType())) {
            addOccurrences(visitedField.getType(), helper.getGenericType(findingNode.getType()));
        } else {
            if (visitedField.getName().equals(findingNode.getName())) {
                occurrences.add(visitedField);
            }
        }
    }

    @Override
    public void visitMethod(MethodNode methodNode) {
        VariableScope variableScope = methodNode.getVariableScope();
        if (leaf instanceof Parameter) {
            if (helper.isCaretOnParamType(((Parameter) leaf))) {
                addMethodOccurrences(methodNode, ((Parameter) leaf).getType());
            } else {
                String name = ((Variable) leaf).getName();
                // This check is here because we can have method parameter with the same
                // name hidding property/field and we don't want to show occurences of these
                if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                    return;

                }
            }
        } else  if (leaf instanceof Variable) {
            String name = ((Variable) leaf).getName();
            // This check is here because we can have method parameter with the same
            // name hidding property/field and we don't want to show occurences of these
            if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                return;
            }
        }

        if (leaf instanceof FieldNode) {
            final ClassNode fieldType = ((FieldNode) leaf).getType();
            if (helper.isCaretOnFieldType((FieldNode) leaf)) {
                addMethodOccurrences(methodNode, fieldType);
            } else if (helper.isCaretOnGenericType(fieldType)) {
                addMethodOccurrences(methodNode, helper.getGenericType(fieldType));
            }
        } else if (leaf instanceof PropertyNode) {
            FieldNode field = ((PropertyNode) leaf).getField();
            if (helper.isCaretOnFieldType(field)) {
                addMethodOccurrences(methodNode, field.getType());
            } else if (helper.isCaretOnGenericType(field.getType())) {
                addMethodOccurrences(methodNode, helper.getGenericType(field.getType()));
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof MethodCallExpression) {
            MethodCallExpression methodCallExpression = (MethodCallExpression) leafParent;
            if (Methods.isSameMethod(methodNode, methodCallExpression)) {
                occurrences.add(methodNode);
            }
        } else if (leaf instanceof MethodNode) {
            MethodNode method = (MethodNode) leaf;

            if (helper.isCaretOnReturnType(method)) {
                addMethodOccurrences(methodNode, method.getReturnType());   // We have caret on the return type and we want to add all occurrences
            } else if (Methods.isSameMethod(methodNode, method)) {
                occurrences.add(methodNode);    // We are on method name looking for method calls
            }
        } else if (leaf instanceof ClassExpression) {
            addMethodOccurrences(methodNode, ((ClassExpression) leaf).getType());
        } else if (leaf instanceof DeclarationExpression) {
            VariableExpression variable = ((DeclarationExpression) leaf).getVariableExpression();
            if (!variable.isDynamicTyped() && !methodNode.isDynamicReturnType()) {
                addMethodOccurrences(methodNode, variable.getType());
            }
        } else if (leaf instanceof ArrayExpression) {
            addMethodOccurrences(methodNode, ((ArrayExpression) leaf).getElementType());
        } else if (leaf instanceof ClassNode) {
            if (!methodNode.isDynamicReturnType()) {
                addMethodOccurrences(methodNode, (ClassNode) leaf);
            }
        } else if (leaf instanceof ForStatement) {
            addMethodOccurrences(methodNode, ((ForStatement) leaf).getVariableType());
        }
        super.visitMethod(methodNode);
    }

    private void addMethodOccurrences(MethodNode visitedMethod, ClassNode findingNode) {
        // If the caret location is on generic type, change findingNode
        ClassNode genericTypeOnCaret = helper.getGenericType(findingNode);
        if (genericTypeOnCaret != null) {
            findingNode = genericTypeOnCaret;
        }

        // Check return type
        addOccurrences(visitedMethod.getReturnType(), findingNode);

        // Check method parameters
        for (Parameter parameter : visitedMethod.getParameters()) {
            addOccurrences(parameter.getType(), findingNode);
        }
    }

    @Override
    public void visitConstructor(ConstructorNode constructor) {
        VariableScope variableScope = constructor.getVariableScope();
        if (leaf instanceof Parameter) {
            if (helper.isCaretOnParamType(((Parameter) leaf))) {
                addConstructorOccurrences(constructor, ((Parameter) leaf).getType());
            } else {
                String name = ((Variable) leaf).getName();
                if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                    return;

                }
            }
        } else if (leaf instanceof Variable) {
            String name = ((Variable) leaf).getName();
            if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                return;
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            String name = ((ConstantExpression) leaf).getText();
            if (variableScope != null && variableScope.getDeclaredVariable(name) != null) {
                return;
            }
        }

        if (leaf instanceof FieldNode) {
            if (helper.isCaretOnFieldType((FieldNode) leaf)) {
                addMethodOccurrences(constructor, ((FieldNode) leaf).getType());
            }
        } else if (leaf instanceof PropertyNode) {
            FieldNode field = ((PropertyNode) leaf).getField();
            if (helper.isCaretOnFieldType(field)) {
                addConstructorOccurrences(constructor, field.getType());
            }
        } else if (leaf instanceof ConstructorNode) {
            if (Methods.isSameConstructor(constructor, (ConstructorNode) leaf)) {
                occurrences.add(constructor);
            }
        } else if (leaf instanceof MethodNode) {
            MethodNode method = (MethodNode) leaf;
            if (helper.isCaretOnReturnType(method)) {
                addConstructorOccurrences(constructor, method.getReturnType());   // We have caret on the return type and we want to add all occurrences
            }
        } else if (leaf instanceof ForStatement) {
            addConstructorOccurrences(constructor, ((ForStatement) leaf).getVariableType());
        } else if (leaf instanceof ClassExpression) {
            addConstructorOccurrences(constructor, ((ClassExpression) leaf).getType());
        } else if (leaf instanceof DeclarationExpression) {
            VariableExpression variable = ((DeclarationExpression) leaf).getVariableExpression();
            if (!variable.isDynamicTyped() && !constructor.isDynamicReturnType()) {
                addConstructorOccurrences(constructor, variable.getType());
            }
        } else if (leaf instanceof ConstructorCallExpression) {
            ConstructorCallExpression methodCallExpression = (ConstructorCallExpression) leaf;
            if (Methods.isSameConstructor(constructor, methodCallExpression)) {
                occurrences.add(constructor);
            }
        } else if (leaf instanceof ArrayExpression) {
            addConstructorOccurrences(constructor, ((ArrayExpression) leaf).getElementType());
        }
        super.visitConstructor(constructor);
    }

    private void addConstructorOccurrences(ConstructorNode constructor, ClassNode findingNode) {
        // If the caret location is on generic type, change findingNode
        ClassNode genericTypeOnCaret = helper.getGenericType(findingNode);
        if (genericTypeOnCaret != null) {
            findingNode = genericTypeOnCaret;
        }
        
        for (Parameter parameter : constructor.getParameters()) {
            addOccurrences(parameter.getType(), findingNode);
        }
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression methodCall) {
        if (leaf instanceof MethodNode) {
            MethodNode method = (MethodNode) leaf;
            if (Methods.isSameMethod(method, methodCall) && !helper.isCaretOnReturnType(method)) {
                occurrences.add(methodCall);
            }
        } else if (leaf instanceof ConstantExpression && leafParent instanceof MethodCallExpression) {
            if (Methods.isSameMethod(methodCall, (MethodCallExpression) leafParent)) {
                occurrences.add(methodCall);
            }
        }
        super.visitMethodCallExpression(methodCall);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        if (leaf instanceof ConstructorNode) {
            ConstructorNode constructor = (ConstructorNode) leaf;
            if (Methods.isSameConstructor(constructor, call)) {
                occurrences.add(call);
            }
        } else if (leaf instanceof ConstructorCallExpression) {
            if (Methods.isSameConstuctor(call, (ConstructorCallExpression) leaf)) {
                occurrences.add(call);
            }
        }
        super.visitConstructorCallExpression(call);
    }

    @Override
    public void visitClassExpression(ClassExpression clazz) {
        if (leaf instanceof FieldNode) {
            ClassNode fieldType = ((FieldNode) leaf).getType();
            if (helper.isCaretOnFieldType((FieldNode) leaf)) {
                addClassExpressionOccurrences(clazz, fieldType);
            } else if (helper.isCaretOnGenericType(fieldType)) {
                addClassExpressionOccurrences(clazz, helper.getGenericType(fieldType));
            }
        } else if (leaf instanceof PropertyNode) {
            FieldNode field = ((PropertyNode) leaf).getField();
            if (helper.isCaretOnFieldType(field)) {
                addClassExpressionOccurrences(clazz, field.getType());
            } else if (helper.isCaretOnGenericType(field.getType())) {
                addClassExpressionOccurrences(clazz, helper.getGenericType(field.getType()));
            }
        } else if (leaf instanceof Parameter) {
            if (helper.isCaretOnParamType(((Parameter) leaf))) {
                addClassExpressionOccurrences(clazz, ((Parameter) leaf).getType());
            }
        } else if (leaf instanceof ClassNode) {
            addClassExpressionOccurrences(clazz, (ClassNode) leaf);
        } else if (leaf instanceof ClassExpression) {
            addClassExpressionOccurrences(clazz, ((ClassExpression) leaf).getType());
        } else if (leaf instanceof ArrayExpression) {
            addClassExpressionOccurrences(clazz, ((ArrayExpression) leaf).getElementType());
        } else if (leaf instanceof DeclarationExpression) {
            DeclarationExpression declaration = (DeclarationExpression) leaf;
            VariableExpression variable = declaration.getVariableExpression();
            if (!variable.isDynamicTyped()) {
                addClassExpressionOccurrences(clazz, variable.getType());
            }
        } else if (leaf instanceof MethodNode) {
            MethodNode method = (MethodNode) leaf;
            if (helper.isCaretOnReturnType(method)) {
                addClassExpressionOccurrences(clazz, method.getReturnType());
            }
        } else if (leaf instanceof ForStatement) {
            addClassExpressionOccurrences(clazz, ((ForStatement) leaf).getVariableType());
        }
        super.visitClassExpression(clazz);
    }

    private void addClassExpressionOccurrences(ClassExpression clazz, ClassNode findingNode) {
        final String visitedName = removeParentheses(clazz.getType().getName());
        final String findingName = removeParentheses(findingNode.getName());
        if (visitedName.equals(findingName)) {
            occurrences.add(clazz);
        }
    }

    @Override
    public void visitClass(ClassNode classNode) {
        if (leaf instanceof ClassExpression) {
            if (classNode.getName().equals(((ClassExpression) leaf).getText())) {
                occurrences.add(classNode);
            }
        } else if (leaf instanceof ClassNode) {
            checkClassNode(classNode);
            checkClassNode(classNode.getSuperClass());
            for (ClassNode interfaceNode : classNode.getInterfaces()) {
                checkClassNode(interfaceNode);
            }
        } else if (leaf instanceof DeclarationExpression) {
            DeclarationExpression declaration = (DeclarationExpression) leaf;
            VariableExpression variable = declaration.getVariableExpression();
            if (!variable.isDynamicTyped() && classNode.getName().equals(variable.getType().getName())) {
                occurrences.add(classNode);
            }
        } else if (leaf instanceof MethodNode) {
            MethodNode method = (MethodNode) leaf;
            OffsetRange range = helper.getMethodReturnType(method);
            if (range != OffsetRange.NONE && classNode.getName().equals(method.getReturnType().getName())) {
                occurrences.add(classNode);
            }
        }
        super.visitClass(classNode);
    }

    private void checkClassNode(ClassNode visitedClass) {
        String visitedName = visitedClass.getName();
        String visitedNameWithoutPkg = visitedClass.getNameWithoutPackage();

        ClassNode classNode = ((ClassNode) leaf);
        ClassNode superClass = classNode.getUnresolvedSuperClass(false);
        ClassNode[] interfaces = classNode.getInterfaces();

        // Check if the caret is on the ClassNode itself
        if (helper.isCaretOnClassNode(classNode)) {
            if (visitedName.equals(classNode.getName())) {
                occurrences.add(new FakeASTNode(classNode, visitedNameWithoutPkg));
            }
        }

        // Check if the caret is on the parent type
        if (superClass.getLineNumber() > 0 && superClass.getColumnNumber() > 0) {
            if (helper.isCaretOnClassNode(superClass)) {
                if (visitedName.equals(superClass.getName())) {
                    occurrences.add(new FakeASTNode(superClass, visitedNameWithoutPkg));
                }
            }
        }

        // Check all implemented interfaces
        for (ClassNode interfaceNode : interfaces) {
            if (interfaceNode.getLineNumber() > 0 && interfaceNode.getColumnNumber() > 0) {
                if (helper.isCaretOnClassNode(interfaceNode)) {
                    if (visitedName.equals(interfaceNode.getName())) {
                        occurrences.add(new FakeASTNode(interfaceNode, visitedNameWithoutPkg));
                    }
                }
            }
        }
    }

    @Override
    public void visitPropertyExpression(PropertyExpression node) {
        Expression property = node.getProperty();
        if (leaf instanceof Variable && ((Variable) leaf).getName().equals(node.getPropertyAsString())) {
            occurrences.add(property);
        } else if (leaf instanceof ConstantExpression && leafParent instanceof PropertyExpression) {
            PropertyExpression propertyUnderCursor = (PropertyExpression) leafParent;
            String nodeAsString = node.getPropertyAsString();
            if (nodeAsString != null && nodeAsString.equals(propertyUnderCursor.getPropertyAsString())) {
                occurrences.add(property);
            }
        }
        super.visitPropertyExpression(node);
    }

    @Override
    public void visitForLoop(ForStatement forLoop) {
        final ClassNode forLoopVariable = forLoop.getVariableType();
        if (leaf instanceof FieldNode) {
            ClassNode fieldType = ((FieldNode) leaf).getType();
            if (helper.isCaretOnFieldType((FieldNode) leaf)) {
                addOccurrences(forLoopVariable, ((FieldNode) leaf).getType());
            } else if (helper.isCaretOnGenericType(fieldType)) {
                addOccurrences(forLoopVariable, helper.getGenericType(fieldType));
            }
        } else if (leaf instanceof PropertyNode) {
            FieldNode field = ((PropertyNode) leaf).getField();
            if (helper.isCaretOnFieldType(field)) {
                addOccurrences(forLoopVariable, field.getType());
            } else if (helper.isCaretOnGenericType(field.getType())) {
                addOccurrences(forLoopVariable, helper.getGenericType(field.getType()));
            }
        } else if (leaf instanceof MethodNode) {
            MethodNode method = (MethodNode) leaf;
            if (helper.isCaretOnReturnType(method)) {
                addOccurrences(forLoopVariable, method.getReturnType()); // We have caret on the return type and we want to add all occurrences
            }
        } else if (leaf instanceof ClassExpression) {
            addOccurrences(forLoopVariable, ((ClassExpression) leaf).getType());
        } else if (leaf instanceof DeclarationExpression) {
            VariableExpression variable = ((DeclarationExpression) leaf).getVariableExpression();
            if (!variable.isDynamicTyped()) {
                addOccurrences(forLoopVariable, variable.getType());
            }
        } else if (leaf instanceof ArrayExpression) {
            addOccurrences(forLoopVariable, ((ArrayExpression) leaf).getElementType());
        } else if (leaf instanceof ForStatement) {
            addOccurrences(forLoopVariable, ((ForStatement) leaf).getVariableType());
        } else if (leaf instanceof Parameter) {
            if (helper.isCaretOnParamType(((Parameter) leaf))) {
                addOccurrences(forLoopVariable, ((Parameter) leaf).getType());
            }
        }
        super.visitForLoop(forLoop);
    }

    private void addOccurrences(ClassNode visitedType, ClassNode findingType) {
        final String visitedTypeName = removeParentheses(visitedType.getName());
        final String findingName = removeParentheses(findingType.getName());
        final String findingNameWithoutPkg = removeParentheses(findingType.getNameWithoutPackage());

        if (visitedTypeName.equals(findingName)) {
            occurrences.add(new FakeASTNode(visitedType, findingNameWithoutPkg));
        }
        addGenericsOccurrences(visitedType, findingType);
    }

    private void addGenericsOccurrences(ClassNode visitedType, ClassNode findingNode) {
        final String findingTypeName = removeParentheses(findingNode.getName());
        final GenericsType[] genericsTypes = visitedType.getGenericsTypes();

        if (genericsTypes != null && genericsTypes.length > 0) {
            for (GenericsType genericsType : genericsTypes) {
                final String genericTypeName = genericsType.getType().getName();
                final String genericTypeNameWithoutPkg = genericsType.getName();

                if (genericTypeName.equals(findingTypeName)) {
                    occurrences.add(new FakeASTNode(genericsType, genericTypeNameWithoutPkg));
                }
            }
        }
    }

    /**
     * Removes [] parentheses.
     *
     * @param name where we want to strip parentheses off
     * @return name without [] parentheses
     */
    private String removeParentheses(String name) {
        if (name.endsWith("[]")) { // NOI18N
            name = name.substring(0, name.length() - 2);
        }
        return name;
    }
}
