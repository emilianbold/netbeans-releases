/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.common.method;

import org.netbeans.api.java.source.CompilationController;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.method.MethodModel.VariableModel;

/**
 *
 * @author Martin Adamek
 */
public final class MethodModelSupport {
    
    private MethodModelSupport() {}
    
    public static MethodModel createMethodModel(WorkingCopy workingCopy, ExecutableElement method) {
        List<VariableModel> parameters = new ArrayList<VariableModel>();
        for (VariableElement variableElement : method.getParameters()) {
            String type = getTypeName(workingCopy, variableElement.asType());
            String name = variableElement.getSimpleName().toString();
            parameters.add(new VariableModel(type, name));
        }
        List<String> exceptions = new ArrayList<String>();
        for (TypeMirror typeMirror : method.getThrownTypes()) {
            exceptions.add(getTypeName(workingCopy, typeMirror));
        }
        return createMethodModel(
                method.getSimpleName().toString(),
                getTypeName(workingCopy, method.getReturnType()),
                //TODO: RETOUCHE get body of method
                "",
                parameters,
                exceptions,
                method.getModifiers()
                );
    }
    
    public static MethodModel createMethodModel(String name, String returnType, String body,
            List<MethodModel.VariableModel> parameters, List<String> exceptions, Set<Modifier> modifiers) {
        return new MethodModel(name, returnType, body, parameters, exceptions, modifiers);
    }
    
    public static MethodModel.VariableModel createVariableModel(WorkingCopy workingCopy, VariableElement variableElement) {
        return createVariableModel(
                getTypeName(workingCopy, variableElement.asType()), 
                variableElement.getSimpleName().toString()
                );
    }
    
    public static MethodModel.VariableModel createVariableModel(String type, String name) {
        return new MethodModel.VariableModel(type, name);
    }
    
    public static MethodTree createMethodTree(WorkingCopy workingCopy, MethodModel methodModel) {
        TreeMaker treeMaker = workingCopy.getTreeMaker();
        TreeUtilities treeUtilities = workingCopy.getTreeUtilities();
        List<VariableTree> paramsList = new ArrayList<VariableTree>();
        if (methodModel.getParameters() != null) {
            int index = 0;
            for (VariableModel parameter : methodModel.getParameters()) {
                VariableTree variableTree = treeMaker.Variable(
                        treeMaker.Modifiers(Collections.<Modifier>emptySet()),
                        "arg" + index++,
                        getTypeTree(workingCopy, parameter.getType()),
                        null
                        );
                paramsList.add(variableTree);
            }
        }
        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        for (String exceptionName : methodModel.getExceptions()) {
            TypeElement element = workingCopy.getElements().getTypeElement(exceptionName);
            throwsList.add(treeMaker.QualIdent(element));
        }
        StatementTree bodyTree = treeUtilities.parseStatement(methodModel.getBody(), new SourcePositions[1]);
        return treeMaker.Method(
                treeMaker.Modifiers(methodModel.getModifiers()),
                methodModel.getName(),
                getTypeTree(workingCopy, methodModel.getReturnType()),
                Collections.<TypeParameterTree>emptyList(),
                paramsList,
                throwsList,
                //TODO: RETOUCHE add method body
                //                treeMaker.Block(Collections.<StatementTree>singletonList(bodyTree), false),
                treeMaker.Block(Collections.<StatementTree>emptyList(), false),
                null
                );
    }
    
    //TODO: RETOUCHE fix this method, see #90505
    public static boolean isSameMethod(CompilationInfo compilationInfo, TypeElement clazz, ExecutableElement method, MethodModel methodModel) {
        if (!method.getSimpleName().contentEquals(methodModel.getName())) {
            return false;
        }
        List<? extends VariableElement> methodParams = method.getParameters();
        if (methodParams.size() != methodModel.getParameters().size()) {
            return false;
        }
        for (int i = 0; i < methodParams.size(); i++) {
            VariableElement variableElement = methodParams.get(i);
            TypeMirror variableElementType = variableElement.asType();
            VariableModel variable = methodModel.getParameters().get(i);
            TypeMirror variableType = compilationInfo.getTreeUtilities().parseType(variable.getType(), clazz);
            if (!compilationInfo.getTypes().isSameType(variableElementType, variableType)) {
                return false;
            }
        }
        return true;
    }
    
    //TODO: RETOUCHE move/reuse to GenerationUtil, this one has also void type
    private static Tree getTypeTree(WorkingCopy workingCopy, String typeName) {
        TreeMaker make = workingCopy.getTreeMaker();
        TypeKind primitiveTypeKind = null;
        if ("boolean".equals(typeName)) {           // NOI18N
            primitiveTypeKind = TypeKind.BOOLEAN;
        } else if ("byte".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.BYTE;
        } else if ("short".equals(typeName)) {      // NOI18N
            primitiveTypeKind = TypeKind.SHORT;
        } else if ("int".equals(typeName)) {        // NOI18N
            primitiveTypeKind = TypeKind.INT;
        } else if ("long".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.LONG;
        } else if ("char".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.CHAR;
        } else if ("float".equals(typeName)) {      // NOI18N
            primitiveTypeKind = TypeKind.FLOAT;
        } else if ("double".equals(typeName)) {     // NOI18N
            primitiveTypeKind = TypeKind.DOUBLE;
        } else if ("void".equals(typeName)) {     // NOI18N
            primitiveTypeKind = TypeKind.VOID;
        }
        if (primitiveTypeKind != null) {
            return make.PrimitiveType(primitiveTypeKind);
        } else {
            return createQualIdent(workingCopy, typeName);
        }
    }
    
    //TODO: RETOUCHE move/reuse to GenerationUtil
    private static ExpressionTree createQualIdent(WorkingCopy workingCopy, String typeName) {
        TypeElement typeElement = workingCopy.getElements().getTypeElement(typeName);
        if (typeElement == null) {
            throw new IllegalArgumentException("Type " + typeName + " cannot be found"); // NOI18N
        }
        return workingCopy.getTreeMaker().QualIdent(typeElement);
    }
    
    //TODO: RETOUCHE move/reuse in SourceUtil
     private static String getTypeName(CompilationController controller, TypeMirror typeMirror) {
        Element element = controller.getTypes().asElement(typeMirror);
        if (ElementKind.CLASS == element.getKind()) {
            return ((TypeElement) element).getQualifiedName().toString();
        } else {
            return element.getSimpleName().toString();
        }
    }
    
}
