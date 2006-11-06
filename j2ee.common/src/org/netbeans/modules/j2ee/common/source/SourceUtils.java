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

package org.netbeans.modules.j2ee.common.source;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Andrei Badea
 * @author Martin Adamek
 */
public class SourceUtils {

    private final CompilationController controller;
    private final TypeElement typeElement;

    // <editor-fold defaultstate="collapsed" desc="Constructors and factory methods">

    SourceUtils(CompilationController controller, TypeElement typeElement) {
        this.controller = controller;
        this.typeElement = typeElement;
    }

    public static SourceUtils newInstance(CompilationController controller, TypeElement typeElement) {
        if (controller == null) {
            throw new IllegalArgumentException("The controller argument cannot be null"); // NOI18N
        }
        if (typeElement == null) {
            throw new IllegalArgumentException("The mainTypeElement argument cannot be null"); // NOI18N
        }
        return new SourceUtils(controller, typeElement);
    }

    public static SourceUtils newInstance(CompilationController controller) throws IOException {
        if (controller == null) {
            throw new IllegalArgumentException("The controller argument cannot be null"); // NOI18N
        }
        TypeElement topLevelTypeElement = findPublicTopLevelTypeElement(controller);
        if (topLevelTypeElement != null) {
            return newInstance(controller, topLevelTypeElement);
        }
        return null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Public static methods">

    /**
     * Returns true if the public top-level element (if any) in the given
     * file contains a <code>public static void main(String[])</code> method.
     *
     * @param  fileObject the file to search for a main method.
     * @return true if a main method was found, false otherwise.
     */
    public static boolean hasMainMethod(FileObject fileObject) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final boolean[] result = { false };
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController cc) throws Exception {
                SourceUtils sourceUtils = SourceUtils.newInstance(cc);
                result[0] = sourceUtils.hasMainMethod();
            }
        }, true);
        return result[0];
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Non-public static methods">

    /**
     * Finds the first public top-level type in the compilation unit given by the
     * given <code>CompilationController</code>.
     *
     * This method assumes the restriction that there is at most a public
     * top-level type declaration in a compilation unit, as described in the
     * section 7.6 of the JLS.
     */
    static TypeElement findPublicTopLevelTypeElement(CompilationController controller) throws IOException {
        controller.toPhase(Phase.ELEMENTS_RESOLVED);

        FileObject fo = controller.getFileObject();
        final String mainElementName = fo.getName();

        CompilationUnitTree compilationUnit = controller.getCompilationUnit();
        for (Tree tree : compilationUnit.getTypeDecls()) {
            if (tree.getKind() != Tree.Kind.CLASS) {
                continue;
            }
            ClassTree clazz = (ClassTree)tree;
            if (!clazz.getSimpleName().contentEquals(mainElementName)) {
                continue;
            }
            if (!clazz.getModifiers().getFlags().contains(Modifier.PUBLIC)) {
                continue;
            }
            TreePath clazzPath = controller.getTrees().getPath(compilationUnit, clazz);
            return (TypeElement)controller.getTrees().getElement(clazzPath);
        }
        return null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Public methods">
    /**
     * Returns the type element that this instance works with.
     *
     * @return the type element that this instance works with; never null.
     */
    public TypeElement getTypeElement() {
        return typeElement;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Non-public methods">

    /**
     * Returns the <code>CompilationController</code> that this instance
     * works with.
     */
    CompilationController getCompilationController() {
        return controller;
    }

    /**
     * Returns the non-synthetic constructor of the main type element.
     */
    ExecutableElement getDefaultConstructor() throws IOException {
        controller.toPhase(Phase.ELEMENTS_RESOLVED);

        ElementUtilities elementUtils = controller.getElementUtilities();
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement)element; // XXX is casting allowed after getKind()?
                if (constructor.getParameters().size() == 0 && !elementUtils.isSyntetic(constructor)) {
                    return constructor;
                }
            }
        }
        return null;
    }

    // TODO: will be replaced by Tomas's implementation from J2SE Project
    /**
     * Returns true if {@link #getTypeElement} has a main method.
     */
    private boolean hasMainMethod() throws IOException {
        controller.toPhase(Phase.ELEMENTS_RESOLVED);

        for (ExecutableElement method : ElementFilter.methodsIn(getTypeElement().getEnclosedElements())) {
            if (isMainMethod(method)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given method is a main method.
     */
    private boolean isMainMethod(ExecutableElement method) {
        // check method name
        if (!method.getSimpleName().contentEquals("main")) {
            return false;
        }
        // check modifiers
        Set<Modifier> modifiers = method.getModifiers();
        if (!modifiers.contains(Modifier.PUBLIC) || !modifiers.contains(Modifier.STATIC)) {
            return false;
        }
        // check return type
        if (TypeKind.VOID != method.getReturnType().getKind()) {
            return false;
        }
        // check parameters
        // there must be just one parameter
        List<? extends VariableElement> params = method.getParameters();
        if (params.size() != 1) {
            return false;
        }
        VariableElement param = params.get(0); // it is ok to take first item, it was tested before
        TypeMirror paramType = param.asType();
        // parameter must be an array
        if (TypeKind.ARRAY != paramType.getKind()) {
            return false;
        }
        ArrayType arrayType = (ArrayType) paramType;
        TypeElement stringTypeElement = controller.getElements().getTypeElement(String.class.getName());
        // array must be array of Strings
        if (!controller.getTypes().isSameType(stringTypeElement.asType(), arrayType.getComponentType())) {
            return false;
        }
        return true;
    }

    // </editor-fold>
}
