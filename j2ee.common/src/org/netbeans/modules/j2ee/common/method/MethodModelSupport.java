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

import com.sun.source.tree.BlockTree;
import javax.lang.model.type.ArrayType;
import org.netbeans.api.java.source.CompilationController;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.util.Parameters;

/**
 * Support class for {@link MethodModel} providing some factory and conversion methods.
 * 
 * @author Martin Adamek
 */
public final class MethodModelSupport {
    
    private MethodModelSupport() {}
    
    /**
     * Creates new instance of method model. None of the parameters can be null.
     * This method must be called from within javac context.
     * 
     * @param workingCopy controller from javac context
     * @param method method for which the model is to be created
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * @return immutable model of method
     */
    public static MethodModel createMethodModel(CompilationController controller, ExecutableElement method) {
        Parameters.notNull("controller", controller); // NOI18N
        Parameters.notNull("method", method); // NOI18N
        List<MethodModel.Variable> parameters = new ArrayList<MethodModel.Variable>();
        for (VariableElement variableElement : method.getParameters()) {
            String type = getTypeName(controller, variableElement.asType());
            String name = variableElement.getSimpleName().toString();
            parameters.add(MethodModel.Variable.create(type, name));
        }
        List<String> exceptions = new ArrayList<String>();
        for (TypeMirror typeMirror : method.getThrownTypes()) {
            exceptions.add(getTypeName(controller, typeMirror));
        }
        return MethodModel.create(
                method.getSimpleName().toString(),
                getTypeName(controller, method.getReturnType()),
                //TODO: RETOUCHE get body of method
                "",
                parameters,
                exceptions,
                method.getModifiers()
                );
    }
    
    /**
     * Creates new instance of model of class variable or method parameter
     * This method must be called from within javac context.
     * 
     * @param workingCopy controller from javac context
     * @param variableElement variable or method parameter for which the model is to be created
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * @return immutable model of variable or method parameter
     */
    public static MethodModel.Variable createVariable(CompilationController controller, VariableElement variableElement) {
        Parameters.notNull("controller", controller); //NOI18N
        Parameters.notNull("variableElement", variableElement); //NOI18N
        return MethodModel.Variable.create(
                getTypeName(controller, variableElement.asType()),
                variableElement.getSimpleName().toString(),
                variableElement.getModifiers().contains(Modifier.FINAL)
                );
    }
    
    /**
     * Creates {@link MethodTree} represented by methodModel in given javac context.
     * 
     * @param workingCopy controller from javac context
     * @param methodModel model of method
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * @return tree representing methodModel
     */
    public static MethodTree createMethodTree(WorkingCopy workingCopy, MethodModel methodModel) {
        return createMethodTree(workingCopy, methodModel, false);
    }
    
    /**
     * Creates {@link MethodTree} represented by methodModel in given javac context.
     * 
     * @param workingCopy controller from javac context
     * @param methodModel model of method
     * @param generateDefaultBody if true and body on methodModel is null or empty string, default return statement will be generated
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * @return tree representing methodModel
     */
    public static MethodTree createMethodTree(WorkingCopy workingCopy, MethodModel methodModel, boolean generateDefaultBody) {
        Parameters.notNull("workingCopy", workingCopy); //NOI18N
        Parameters.notNull("methodModel", methodModel); //NOI18N
        TreeMaker treeMaker = workingCopy.getTreeMaker();
        List<VariableTree> paramsList = new ArrayList<VariableTree>();
        if (methodModel.getParameters() != null) {
            for (MethodModel.Variable parameter : methodModel.getParameters()) {
                VariableTree variableTree = treeMaker.Variable(
                        treeMaker.Modifiers(parameter.getFinalModifier() ? Collections.<Modifier>singleton(Modifier.FINAL) : Collections.<Modifier>emptySet()),
                        parameter.getName(),
                        getTypeTree(workingCopy, parameter.getType()),
                        null
                        );
                paramsList.add(variableTree);
            }
        }
        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        for (String exceptionName : methodModel.getExceptions()) {
            throwsList.add(createQualIdent(workingCopy, exceptionName));
        }
        MethodTree result;
        String body = methodModel.getBody();
        // if passed body is null, generate default return statement (if return type is not void)
        if (generateDefaultBody && (body == null || "".equals(body.trim()))) {
            String generatedBody = getDefaultReturnValue(workingCopy, methodModel.getReturnType());
            body = generatedBody == null ? "" : "return " + generatedBody + ";";
        }
        if (body == null) {
            result = treeMaker.Method(
                    treeMaker.Modifiers(methodModel.getModifiers()),
                    methodModel.getName(),
                    getTypeTree(workingCopy, methodModel.getReturnType()),
                    Collections.<TypeParameterTree>emptyList(),
                    paramsList,
                    throwsList,
                    (BlockTree) null,
                    null
                    );
        } else {
            result = treeMaker.Method(
                    treeMaker.Modifiers(methodModel.getModifiers()),
                    methodModel.getName(),
                    getTypeTree(workingCopy, methodModel.getReturnType()),
                    Collections.<TypeParameterTree>emptyList(),
                    paramsList,
                    throwsList,
                    "{" + body + "}",
                    null
                    );
        }
        return result;
    }
    
    /**
     * Checks if signature of {@link ExecutableElement} is represented by {@link MethodModel} methodModel
     * in given javac context.
     * 
     * @param compilationInfo controller from javac context
     * @param method method existing in given javac context
     * @param methodModel model of method
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * @return true if method and methodModel have same signature, false otherwise
     */
    public static boolean isSameMethod(CompilationController controller, ExecutableElement method, MethodModel methodModel) {
        //TODO: RETOUCHE fix this method, see #90505
        Parameters.notNull("compilationInfo", controller); // NOI18N
        Parameters.notNull("method", method); // NOI18N
        Parameters.notNull("methodModel", methodModel); // NOI18N
        if (!method.getSimpleName().contentEquals(methodModel.getName())) {
            return false;
        }
        List<? extends VariableElement> methodParams = method.getParameters();
        if (methodParams.size() != methodModel.getParameters().size()) {
            return false;
        }
        for (int i = 0; i < methodParams.size(); i++) {
            VariableElement variableElement = methodParams.get(i);
            String variableElementType = getTypeName(controller, variableElement.asType());
            MethodModel.Variable variable = methodModel.getParameters().get(i);
            if (!variableElementType.equals(variable.getType())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Generates default value for provided type
     * 
     * @param workingCopy workingCopy
     * @param typeName fully-qualified type name or primitive type name or "void"
     * @return values according to JLS '4.5.5 Initial Values of Variables'
     * with exception for "char" where it returns "c"
     */
    public static String getDefaultReturnValue(WorkingCopy workingCopy, String typeName) {
        if ("boolean".equals(typeName)) {           // NOI18N
            return "false";                         // NOI18N
        } else if ("byte".equals(typeName)) {       // NOI18N
            return "0";                             // NOI18N
        } else if ("short".equals(typeName)) {      // NOI18N
            return "0";                             // NOI18N
        } else if ("int".equals(typeName)) {        // NOI18N
            return "0";                             // NOI18N
        } else if ("long".equals(typeName)) {       // NOI18N
            return "0L";                            // NOI18N
        } else if ("char".equals(typeName)) {       // NOI18N
            // should be '\u0000' which is null character, but we cannot return that
            return "'c'";                           // NOI18N
        } else if ("float".equals(typeName)) {      // NOI18N
            return "0.0f";                          // NOI18N
        } else if ("double".equals(typeName)) {     // NOI18N
            return "0.0d";                          // NOI18N
        } else if ("void".equals(typeName)) {       // NOI18N
            return null;
        } else {
            return "null";                          // NOI18N
        }
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
            typeElement = workingCopy.getElements().getTypeElement("java.lang." + typeName);
            if (typeElement == null) {
                return workingCopy.getTreeMaker().Identifier(typeName);
            }
        }
        return workingCopy.getTreeMaker().QualIdent(typeElement);
    }
    
    //TODO: RETOUCHE move/reuse in SourceUtil, or best - get from java/source!
    // package private only for unit test
    // see #90968
    static String getTypeName(CompilationController controller, TypeMirror typeMirror) {
        TypeKind typeKind = typeMirror.getKind();
        switch (typeKind) {
            case BOOLEAN : return "boolean"; // NOI18N
            case BYTE : return "byte"; // NOI18N
            case CHAR : return "char"; // NOI18N
            case DOUBLE : return "double"; // NOI18N
            case FLOAT : return "float"; // NOI18N
            case INT : return "int"; // NOI18N
            case LONG : return "long"; // NOI18N
            case SHORT : return "short"; // NOI18N
            case VOID : return "void"; // NOI18N
            case DECLARED : 
                Element element = controller.getTypes().asElement(typeMirror);
                return ((TypeElement) element).getQualifiedName().toString();
            case ARRAY : 
                ArrayType arrayType = (ArrayType) typeMirror;
                TypeMirror componentType = arrayType.getComponentType();
                return getTypeName(controller, componentType) + "[]";
            case ERROR :
            case EXECUTABLE :
            case NONE :
            case NULL :
            case OTHER :
            case PACKAGE :
            case TYPEVAR :
            case WILDCARD :
            default:break;
        }
        return null;
    }
    
}
