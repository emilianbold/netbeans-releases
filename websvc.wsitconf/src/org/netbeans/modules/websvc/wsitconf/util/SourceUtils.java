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

package org.netbeans.modules.websvc.wsitconf.util;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import java.io.IOException;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.openide.util.Parameters;

/**
 *
 * @author Andrei Badea, Martin Adamek
 */
public class SourceUtils {

    // TODO we could probably also have a SourceUtils(CompilationController, TypeElement) factory method

    /**
     * The compilation controller this instance works with.
     */
    private final CompilationController controller;

    /**
     * The type element this instance works with. Do not use directly, use
     * {@link #getTypeElement} instead.
     */
    private TypeElement typeElement;

    /**
     * The class tree corresponding to {@link #typeElement}. Do not use directly,
     * use {@link #getClassTree} instead.
     */
    private ClassTree classTree;

    // <editor-fold defaultstate="collapsed" desc="Constructors and factory methods">

    SourceUtils(CompilationController controller, TypeElement typeElement) {
        this.controller = controller;
        this.typeElement = typeElement;
    }

    SourceUtils(CompilationController controller, ClassTree classTree) {
        this.controller = controller;
        this.classTree = classTree;
    }

    public static SourceUtils newInstance(CompilationController controller, TypeElement typeElement) {
        Parameters.notNull("controller", controller); // NOI18N
        Parameters.notNull("typeElement", typeElement); // NOI18N

        return new SourceUtils(controller, typeElement);
    }

    public static SourceUtils newInstance(CompilationController controller, ClassTree classTree) {
        Parameters.notNull("controller", controller); // NOI18N
        Parameters.notNull("classTree", classTree); // NOI18N

        return new SourceUtils(controller, classTree);
    }

    public static SourceUtils newInstance(CompilationController controller) throws IOException {
        Parameters.notNull("controller", controller); // NOI18N

        ClassTree classTree = findPublicTopLevelClass(controller);
        if (classTree != null) {
            return newInstance(controller, classTree);
        }
        return null;
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
    static ClassTree findPublicTopLevelClass(CompilationController controller) throws IOException {
        controller.toPhase(Phase.ELEMENTS_RESOLVED);

        final String mainElementName = controller.getFileObject().getName();
        for (Tree tree : controller.getCompilationUnit().getTypeDecls()) {
            if (tree.getKind() != Tree.Kind.CLASS) {
                continue;
            }
            ClassTree classTree = (ClassTree)tree;
            if (!classTree.getSimpleName().contentEquals(mainElementName)) {
                continue;
            }
            if (!classTree.getModifiers().getFlags().contains(Modifier.PUBLIC)) {
                continue;
            }
            return classTree;
        }
        return null;
    }

    // </editor-fold>

    // <editor-fold desc="Public methods">

    /**
     * Returns the type element that this instance works with
     * (corresponding to {@link #getClassTree}.
     *
     * @return the type element that this instance works with; never null.
     */
    public TypeElement getTypeElement() {
        if (typeElement == null) {
            assert classTree != null;
            TreePath classTreePath = controller.getTrees().getPath(getCompilationController().getCompilationUnit(), classTree);
            typeElement = (TypeElement)controller.getTrees().getElement(classTreePath);
        }
        return typeElement;
    }

    /**
     * Returns the class tree that this instance works with
     * (corresponding to {@link #getTypeElement}.
     *
     * @return the class tree that this instance works with; never null.
     */
    public ClassTree getClassTree() {
        if (classTree == null) {
            assert typeElement != null;
            classTree = controller.getTrees().getTree(typeElement);
        }
        return classTree;
    }

    /**
     * Returns true if {@link #getTypeElement} is a subtype of the given type.
     *
     * @param  type the string representation of a type. The type will be parsed
     *         in the context of {@link #getTypeElement}.
     * @return true {@link #getTypeElement} is a subtype of the given type,
     *         false otherwise.
     */
    public boolean isSubtype(String type) {
        Parameters.notNull("type", type); // NOI18N

        TypeMirror typeMirror = getCompilationController().getTreeUtilities().parseType(type, getTypeElement());
        if (typeMirror != null) {
            return getCompilationController().getTypes().isSubtype(getTypeElement().asType(), typeMirror);
        }
        return false;
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
     * Returns the non-synthetic no-arg constructor of the main type element.
     */
    ExecutableElement getNoArgConstructor() throws IOException {
        controller.toPhase(Phase.ELEMENTS_RESOLVED);

        ElementUtilities elementUtils = controller.getElementUtilities();
        for (Element element : getTypeElement().getEnclosedElements()) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement)element;
                if (constructor.getParameters().size() == 0 && !elementUtils.isSynthetic(constructor)) {
                    return constructor;
                }
            }
        }
        return null;
    }

    // </editor-fold>
}
