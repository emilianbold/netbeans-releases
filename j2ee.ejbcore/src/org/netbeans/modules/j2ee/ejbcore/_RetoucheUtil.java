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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
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
                result[0] = sourceUtils.getTypeElement().getQualifiedName().toString();
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

    public static ElementHandle<TypeElement> getJavaClassFromNode(Node node) throws IOException {
        //TODO: RETOUCHE TypeElement from Node, this one just takes main TypeElement
        FileObject fileObject = node.getLookup().lookup(FileObject.class);
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final List<ElementHandle<TypeElement>> result = new ArrayList<ElementHandle<TypeElement>>();
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.newInstance(controller).getTypeElement();
                result.add(ElementHandle.create(typeElement));
            }
        }, true);
        return result.get(0);
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
                // annotation woth attributes
                List<ExpressionTree> attributesList = new ArrayList<ExpressionTree>();
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    ExpressionTree attributeTree = generationUtils.createAnnotationArgument(entry.getKey(), entry.getValue());
                    attributesList.add(attributeTree);
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
                ClassTree newClassTree = treeMaker.addClassMember(classTree, variableTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
    }
    
}
