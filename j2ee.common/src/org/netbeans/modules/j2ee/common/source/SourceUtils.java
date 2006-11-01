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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CancellableTask;
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
    protected final TypeElement mainTypeElement;

    public SourceUtils(CompilationController controller) throws IOException, IllegalStateException {
        this.controller = controller;
        mainTypeElement = findMainTypeElement();
        if (mainTypeElement == null) {
            throw new IllegalStateException("Cannot find the main type element"); // NOI18N
        }
    }

    // static methods ----------------------------------------------------------
    
    public static boolean hasMainMethod(FileObject fileObject) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final boolean[] result = new boolean[] {false};
        javaSource.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController cc) throws Exception {
                SourceUtils sourceUtils = new SourceUtils(cc);
                result[0] = sourceUtils.hasMainMethod();
            }
        }, true);
        return result[0];
    }
    
    // instance methods --------------------------------------------------------
    
//    public TypeElement getMainTypeElement() {
//        return mainTypeElement;
//    }
    
    // private methods ---------------------------------------------------------
    
    private TypeElement findMainTypeElement() throws IOException {
        controller.toPhase(Phase.ELEMENTS_RESOLVED);

        FileObject fo = controller.getFileObject();
        final String mainElementName = fo.getName();

        // XXX maybe
        // return controller.getElements().getTypeElement(mainElementName);
        Iterator<? extends TypeElement> globalTypes = controller.getElementUtilities().getGlobalTypes(new ElementUtilities.ElementAcceptor() {
            public boolean accept(Element e, TypeMirror type) {
                return mainElementName.equals(e.getSimpleName().toString());
            }
        }).iterator();
        if (!globalTypes.hasNext()) {
            throw new IllegalStateException("Could not find the main type element in " + fo); // NOI18N
        }
        return globalTypes.next();
    }

    // TODO: will be replaced by Tomas's implementation from J2SE Project
    private boolean hasMainMethod() throws IOException {
        controller.toPhase(Phase.ELEMENTS_RESOLVED);
        TypeElement typeElement = findMainTypeElement();
        for (ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if (isMainMethod(method)) {
                return true;
            }
        }
        return false;
    }

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

}
