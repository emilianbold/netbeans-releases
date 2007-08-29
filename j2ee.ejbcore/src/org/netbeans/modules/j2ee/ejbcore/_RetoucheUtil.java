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

package org.netbeans.modules.j2ee.ejbcore;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Parameters;

/**
 *
 * @author Martin Adamek
 */
public final class _RetoucheUtil {
    
    private _RetoucheUtil() {}
    
    /** never call this from javac task */
    public static String getMainClassName(final FileObject classFO) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(classFO);
        final String[] result = new String[1];
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                if (sourceUtils != null) {
                    result[0] = sourceUtils.getTypeElement().getQualifiedName().toString();
                }
            }
        }, true);
        return result[0];
    }

    /**
     * @return true if the given <code>javaClass</code> contains a feature
     * whose name is identical with the given <code>feature</code>'s name.
     */
    public static boolean containsFeature(TypeElement javaClass, Element searchedElement) {
        for (Element element : javaClass.getEnclosedElements()) {
            if (searchedElement.getSimpleName().equals(element.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static ElementHandle<TypeElement> getElementHandle(ElementHandle elementHandle) {
        if (elementHandle != null && ElementKind.CLASS == elementHandle.getKind()) {
            return (ElementHandle<TypeElement>) elementHandle;
        }
        return null;
    }
    
    public static ElementHandle<TypeElement> getJavaClassFromNode(Node node) throws IOException {
        ElementHandle<TypeElement> elementHandle = getElementHandle(node.getLookup().lookup(ElementHandle.class));
        if (elementHandle != null) {
            return elementHandle;
        }
        //TODO: RETOUCHE TypeElement from Node, this one just takes main TypeElement if ElementHandle is not found
        FileObject fileObject = node.getLookup().lookup(FileObject.class);
        if (fileObject == null || !fileObject.isValid()) {
            return null;
        }
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        if (javaSource == null) {
            return null;
        }
        final List<ElementHandle<TypeElement>> result = new ArrayList<ElementHandle<TypeElement>>();
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                if (sourceUtils != null) {
                    TypeElement typeElement = sourceUtils.getTypeElement();
                    result.add(ElementHandle.create(typeElement));
                }
            }
        }, true);
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public static ExecutableElement getMethodFromNode(Node node) {
        //TODO: RETOUCHE ExecutableElement from Node
        return null;
    }
    
    public static void generateAnnotatedField(FileObject fileObject, final String className, final String annotationType,
            final String name, final String fieldType, final Map<String, String> attributes, final boolean isStatic) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                GenerationUtils generationUtils = GenerationUtils.newInstance(workingCopy, typeElement);
                TypeElement returnTypeElement = workingCopy.getElements().getTypeElement(fieldType);
                // modifiers
                Set<Modifier> modifiers = new HashSet<Modifier>();
                modifiers.add(Modifier.PRIVATE);
                if (isStatic) {
                    modifiers.add(Modifier.STATIC);
                }
                // annotation with attributes
                List<ExpressionTree> attributesList = new ArrayList<ExpressionTree>();
                if (attributes != null) {
                    for (Map.Entry<String, String> entry : attributes.entrySet()) {
                        ExpressionTree attributeTree = generationUtils.createAnnotationArgument(entry.getKey(), entry.getValue());
                        attributesList.add(attributeTree);
                    }
                }
                AnnotationTree annotationTree = generationUtils.createAnnotation(annotationType, attributesList);
                ModifiersTree modifiersTree = treeMaker.addModifiersAnnotation(treeMaker.Modifiers(modifiers), annotationTree);
                // field itself
                VariableTree variableTree = treeMaker.Variable(
                        modifiersTree,
                        name,
                        treeMaker.QualIdent(returnTypeElement),
                        null
                        );
                // adding field to class
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = treeMaker.insertClassMember(classTree, 0, variableTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
    }

    public static boolean isInterface(FileObject fileObject, final ElementHandle<TypeElement> elementHandle) throws IOException {
        final boolean[] isInterface = new boolean[1];
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        if (javaSource == null || elementHandle == null) {
            return false;
        }
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = elementHandle.resolve(controller);
                isInterface[0] = ElementKind.INTERFACE == typeElement.getKind();
            }
        }, true);
        return isInterface[0];
    }

    /**
     * Generates unique member name in class-scope
     * @param javaClass scope for uniqueness
     * @param memberName suggested member name
     * @param defaultValue default value applied if member name cannot be converted to legal Java identifier
     * @return given member name if no such member exists or given member name without any illegal characters extended with unique number
     */
    public static String uniqueMemberName(FileObject fileObject, final String className, final String memberName, final String defaultValue) throws IOException{
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final String[] result = new String[1];
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                String newName = convertToJavaIdentifier(memberName, defaultValue);
                List<String> existingMethodNames = new ArrayList<String>();
                TypeElement typeElement = controller.getElements().getTypeElement(className);
                for (Element element : typeElement.getEnclosedElements()) {
                    existingMethodNames.add(element.getSimpleName().toString());
                }
                int uniquefier = 1;
                while (existingMethodNames.contains(newName)){
                    newName = memberName + uniquefier++;
                }
                result[0] = newName;
            }
        }, true);
        return result[0];
    }
    
    private static String convertToJavaIdentifier(String name, String defaultValue) {
        Parameters.notWhitespace("name", name);
        Parameters.notWhitespace("defaultValue", defaultValue);
        String str = name;
        while (str.length() > 0 && !Character.isJavaIdentifierStart(str.charAt(0))) {
            str = str.substring(1);
        }
        StringBuilder result = new StringBuilder();
        if (str.length() > 0) {
            char firstChar = str.charAt(0);
            firstChar = Character.toLowerCase(firstChar);
            result.append(firstChar);
            for (int i = 1; i < str.length(); i++) {
                char character = str.charAt(i);
                if (Character.isJavaIdentifierPart(character)) {
                    result.append(character);
                }
            }
        } else {
            result.append(defaultValue);
        }
        return result.toString();
    }

    /**
     * Tries to find {@link FileObject} which contains class with given className.<br>
     * This method will enter javac context for referenceFileObject to find class by its className,
     * therefore don't call this method within other javac context.
     * 
     * @param referenceFileObject helper file for entering javac context
     * @param className fully-qualified class name to resolve file for
     * @return resolved file or null if not found
     */
    public static FileObject resolveFileObjectForClass(FileObject referenceFileObject, final String className) throws IOException {
        final FileObject[] result = new FileObject[1];
        JavaSource javaSource = JavaSource.forFileObject(referenceFileObject);
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) {
                TypeElement typeElement = controller.getElements().getTypeElement(className);
                result[0] = org.netbeans.api.java.source.SourceUtils.getFile(ElementHandle.create(typeElement), controller.getClasspathInfo());
            }
        }, true);
        return result[0];
    }
    
    //TODO: RETOUCHE move/reuse in SourceUtil, or best - get from java/source!
    // package private only for unit test
    // see #90968
    public static String getTypeName(CompilationController controller, TypeMirror typeMirror) {
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
                Element componentTypeElement = controller.getTypes().asElement(arrayType.getComponentType());
                return ((TypeElement) componentTypeElement).getQualifiedName().toString() + "[]";
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
