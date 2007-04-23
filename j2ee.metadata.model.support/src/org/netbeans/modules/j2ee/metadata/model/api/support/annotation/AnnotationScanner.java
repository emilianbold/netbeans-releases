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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.TypeAnnotationHandler;

/**
 *
 * @author Andrei Badea
 */
public class AnnotationScanner {

    // XXX perhaps should be merged with AnnotationModelHelper

    private static final Logger LOGGER = Logger.getLogger(AnnotationScanner.class.getName());

    private final AnnotationModelHelper helper;

    public AnnotationScanner(AnnotationModelHelper helper) {
        this.helper = helper;
    }

    public void findAnnotatedTypes(final String searchedTypeName, final TypeAnnotationHandler handler) {
        LOGGER.log(Level.FINE, "findAnnotatedTypes called with {0}", searchedTypeName); // NOI18N
        CompilationController controller = helper.getCompilationController();
        TypeElement searchedType = controller.getElements().getTypeElement(searchedTypeName);
        if (searchedType == null) {
            LOGGER.log(Level.WARNING, "findAnnotatedTypes: could not find type {0}", searchedTypeName); // NOI18N
            return;
        }
        ElementHandle<TypeElement> searchedTypeHandle = ElementHandle.create(searchedType);
        final Set<ElementHandle<TypeElement>> elementHandles = helper.getClasspathInfo().getClassIndex().getElements(searchedTypeHandle, EnumSet.of(SearchKind.TYPE_REFERENCES), EnumSet.of(SearchScope.SOURCE, SearchScope.DEPENDENCIES));
        TypeMirror searchedTypeMirror = searchedType.asType();
        for (ElementHandle<TypeElement> elementHandle : elementHandles) {
            LOGGER.log(Level.FINE, "found element {0}", elementHandle.getQualifiedName()); // NOI18N
            TypeElement element = elementHandle.resolve(controller);
            if (element == null) {
                continue;
            }
            List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
            for (AnnotationMirror annotationMirror : annotationMirrors) {
                TypeMirror annotationType = annotationMirror.getAnnotationType();
                // XXX or should compare annotation type names here?
                if (controller.getTypes().isSameType(searchedTypeMirror, annotationType)) {
                    LOGGER.log(Level.FINE, "notifying element {0}, annotation {1}", new Object[] { element.getQualifiedName(), annotationMirror }); // NOI18N
                    handler.typeAnnotation(element, annotationMirror);
                }
            }
        }
    }
}
