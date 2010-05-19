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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.jca.base.generator.api;

import org.netbeans.modules.soa.jca.base.generator.impl.AddVariableTask;
import org.netbeans.modules.soa.jca.base.generator.impl.JavacTreeModelImpl;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.util.Exceptions;

/**
 *
 * @author echou
 */
public class GeneratorUtil {


    public static JavacTreeModel createJavacTreeModel(JTextComponent target) {
        try {
            final JavacTreeModelImpl model = new JavacTreeModelImpl();
            JavaSource javaSource = JavaSource.forDocument(target.getDocument());
            javaSource.runUserActionTask(new CancellableTask<CompilationController> () {
                public void cancel() {}
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    model.setCompilationController(cc);
                    model.scan(cc.getCompilationUnit(), null);
                }
            }, true);

            return model;
        } catch (IOException ioe) {
            return null;
        }
    }

    public static void runModificationTask(JTextComponent target, ModificationTask<WorkingCopy> task) throws Throwable {

        JavaSource javaSource = JavaSource.forDocument(target.getDocument());

        ModificationResult result = null;
        try {
            result = javaSource.runModificationTask(task);
        } catch (IOException ioe) {
            Exception taskException = task.getException();
            throw new Exception("error adding variable to JavacTreeModel", taskException);  // NOI18N
        }
        result.commit();
    }

    public static void addVariable(JTextComponent target, String varType, String varName,
            String annotationType, Map<String, Object> annotationArguments) throws Throwable {

        JavaSource javaSource = JavaSource.forDocument(target.getDocument());
        ModificationTask<WorkingCopy> task = new AddVariableTask<WorkingCopy> (
                varType, varName, annotationType, annotationArguments);

        ModificationResult result = null;
        try {
            result = javaSource.runModificationTask(task);
        } catch (IOException ioe) {
            Exception taskException = task.getException();
            throw new Exception("error adding variable to JavacTreeModel", taskException);  // NOI18N
        }
        result.commit();
    }


    public static boolean isJavaIdentifier(String s) {
        if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0))) {
            return false;
        }
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDuplicateLocalVariable(TreePath methodTree, String variableName) {
        final List<Name> localVariables = new ArrayList<Name> ();
        (new TreePathScanner<Void, Void> () {

            private int blockLevel = 0;

            @Override
            public Void visitVariable(VariableTree t, Void v) {
                super.visitVariable(t, v);
                if (blockLevel <= 1) {
                    localVariables.add(t.getName());
                }
                return null;
            }

            @Override
            public Void visitBlock(BlockTree t, Void v) {
                blockLevel++;
                super.visitBlock(t, v);
                blockLevel--;
                return null;
            }

        }).scan(methodTree, null);

        for (Name name : localVariables) {
            if (name.contentEquals(variableName)) {
                return true;
            }
        }
        return false;
    }

    public static List<VariableTree> getMethodLevelVariables(TreePath methodTree) {
        final List<VariableTree> variables = new ArrayList<VariableTree> ();
        (new TreePathScanner<Void, Void> () {

            @Override
            public Void visitVariable(VariableTree t, Void v) {
                super.visitVariable(t, v);
                variables.add(t);
                return null;
            }

        }).scan(methodTree, null);

        return variables;
    }

    public static AnnotationMirror findAnnotation(Element element, String annotationClass) {
        for (AnnotationMirror ann : element.getAnnotationMirrors()) {
            if (annotationClass.equals(ann.getAnnotationType().toString())) {
                return ann;
            }
        }

        return null;
    }

    public static AnnotationValue getAnnotationAttrValue(AnnotationMirror ann, String attrName) {
        if (ann != null) {
            for (ExecutableElement attr : ann.getElementValues().keySet()) {
                if (attrName.equals(attr.getSimpleName().toString())) {
                    return ann.getElementValues().get(attr);
                }
            }
        }

        return null;
    }

    // ********************* RetoucheUtil *********************************

    public static Tree createType(TreeMaker make, WorkingCopy workingCopy, String typeName) throws Exception {
        TypeKind primitiveTypeKind = null;
        if ("void".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.VOID;
        } else if ("boolean".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.BOOLEAN;
        } else if ("byte".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.BYTE;
        } else if ("short".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.SHORT;
        } else if ("int".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.INT;
        } else if ("long".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.LONG;
        } else if ("char".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.CHAR;
        } else if ("float".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.FLOAT;
        } else if ("double".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.DOUBLE;
        }
        if (primitiveTypeKind != null) {
            return make.PrimitiveType(primitiveTypeKind);
        }
        Tree typeTree = createQualIdent(make, workingCopy, typeName);

        return typeTree;
    }

    public static ModifiersTree createModifiers(TreeMaker make, Modifier modifier,
            AnnotationTree annotation) {
        return make.Modifiers(EnumSet.of(modifier), Collections.<AnnotationTree>singletonList(annotation));
    }

    public static AnnotationTree createAnnotation(TreeMaker make, WorkingCopy workingCopy,
            String annotationType, List<? extends ExpressionTree> arguments) throws Exception {
        ExpressionTree annotationTypeTree = createQualIdent(make, workingCopy, annotationType);
        return make.Annotation(annotationTypeTree, arguments);
    }

    public static ExpressionTree createAnnotationArgument(TreeMaker make, String argumentName,
            Object argumentValue) {
        ExpressionTree argumentValueTree = make.Literal(argumentValue);
        return make.Assignment(make.Identifier(argumentName), argumentValueTree);
    }

    public static VariableTree createField(TreeMaker make, WorkingCopy workingCopy,
            ModifiersTree modifiersTree, String fieldName,
            String fieldType, ExpressionTree expressionTree) throws Exception {
        return make.Variable(
                modifiersTree,
                fieldName,
                createType(make, workingCopy, fieldType),
                expressionTree);
    }

    public static IdentifierTree createQualIdent(TreeMaker make, WorkingCopy workingCopy,
            String typeName) throws Exception {
        //TypeElement typeElement = workingCopy.getElements().getTypeElement(typeName);
        //if (typeElement == null) {
        //    throw new Exception("Cannot resolve type: " + typeName);
        //}
        return make.Identifier(typeName);
    }

    public static ExecutableElement findMethodByName(WorkingCopy workingCopy, String methodName) {
        List<? extends TypeElement> elements = workingCopy.getTopLevelElements();
        if (elements.size() == 0) {
            return null;
        }
        TypeElement topElement = elements.get(0);
        for (Element element : topElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                if (method.getSimpleName().toString().equals(methodName)) {
                    return method;
                }
            }
        }

        return null;
    }

    public static void addLibrary(String libName, Project prj) {
        Library lib = LibraryManager.getDefault().getLibrary(
                libName);
        Sources srcs = getSources(prj);
        if (srcs != null) {
            SourceGroup[] srg = srcs.getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA);
            if ((srg != null) && (srg.length > 0)) {
                try {
                    ProjectClassPathModifier.addLibraries(
                            new Library[]{lib}, srg[0].getRootFolder(),
                            ClassPath.COMPILE);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    public static Sources getSources(Project p) {
        Sources s = p.getLookup().lookup(Sources.class);
        if (s != null) {
            return s;
        } else {
            return GenericSources.genericOnly(p);
        }
    }
}
