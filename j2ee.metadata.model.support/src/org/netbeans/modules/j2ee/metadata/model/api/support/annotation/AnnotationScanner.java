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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.openide.util.Parameters;

/**
 * An utility class that can be used to find elements (types, methods, etc.)
 * annotated with a given annotation.
 *
 * @author Andrei Badea, Tomas Mysik
 */
public class AnnotationScanner {

    // XXX perhaps should be merged with AnnotationModelHelper
    // XXX method findAnnotatedTypes() should be removed
    
    private static final Logger LOGGER = Logger.getLogger(AnnotationScanner.class.getName());
    private static final Set<ElementKind> TYPE_KINDS = EnumSet.of(ElementKind.CLASS, ElementKind.INTERFACE, ElementKind.ENUM, ElementKind.ANNOTATION_TYPE);

    private final AnnotationModelHelper helper;

    AnnotationScanner(AnnotationModelHelper helper) {
        this.helper = helper;
    }

    /**
     * Finds all types annotated with the given annotation. This methods gets
     * the name of the searched annotation and an instance of the
     * {@link TypeAnnotationHandler} interface which will be used to
     * pass the found annotation types back to the caller.
     *
     * @param  searchedTypeName the fully-qualified name of the annotation
     *         to be searched for. Cannot be null.
     * @param  handler a {@link TypeAnnotationHandler}. Its <code>typeAnnotation</code>
     *         method will be invoked once for each type annotated with the annotation
     *         passed in the <code>searchedTypeName</code> parameter, with
     *         the <code>type</code> parameter set to the annotated type, and the
     *         <code>annotation</code> parameter set to an {@link AnnotationMirror}
     *         (of type <code>searchedTypeName</code>) which that type is annotated with.
     *         Cannot be null.
     * @throws InterruptedException when the search was interrupted (for 
     *         example because {@link org.netbeans.api.java.source.ClassIndex#getElements}
     *         was interrupted).
     */
    public void findAnnotatedTypes(final String searchedTypeName, final TypeAnnotationHandler handler) throws InterruptedException {
        Parameters.notNull("searchedTypeName", searchedTypeName); // NOI18N
        Parameters.notNull("handler", handler); // NOI18N
        LOGGER.log(Level.FINE, "findAnnotatedTypes called with {0}", searchedTypeName); // NOI18N
        CompilationController controller = helper.getCompilationController();
        TypeElement searchedType = controller.getElements().getTypeElement(searchedTypeName);
        if (searchedType == null) {
            LOGGER.log(Level.WARNING, "findAnnotatedTypes: could not find type {0}", searchedTypeName); // NOI18N
            return;
        }
        ElementHandle<TypeElement> searchedTypeHandle = ElementHandle.create(searchedType);
        final Set<ElementHandle<TypeElement>> elementHandles = helper.getClasspathInfo().getClassIndex().getElements(searchedTypeHandle, EnumSet.of(SearchKind.TYPE_REFERENCES), EnumSet.of(SearchScope.SOURCE, SearchScope.DEPENDENCIES));
        if (elementHandles == null) {
            throw new InterruptedException("ClassIndex.getElements() was interrupted"); // NOI18N
        }
        TypeMirror searchedTypeMirror = searchedType.asType();
        for (ElementHandle<TypeElement> elementHandle : elementHandles) {
            LOGGER.log(Level.FINE, "found element {0}", elementHandle.getQualifiedName()); // NOI18N
            TypeElement element = elementHandle.resolve(controller);
            if (element == null) {
                continue;
            }
            List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
            for (AnnotationMirror annotationMirror : annotationMirrors) {
                DeclaredType annotationType = annotationMirror.getAnnotationType();
                String annotationTypeName = helper.getAnnotationTypeName(annotationType);
                // issue 110819: need to compare the real type names, since the annotation can be @<any>
                if (searchedTypeName.equals(annotationTypeName)) {
                    LOGGER.log(Level.FINE, "notifying element {0}, annotation {1}", new Object[] { element.getQualifiedName(), annotationMirror }); // NOI18N
                    handler.typeAnnotation(element, annotationMirror);
                } else {
                    LOGGER.log(Level.FINE, "type name mismatch, ignoring element {0}, annotation {1}", new Object[] { element.getQualifiedName(), annotationMirror }); // NOI18N
                }
            }
        }
    }
    
    /**
     * Finds all elements annotated with the given annotation. This methods gets
     * the name of the searched annotation and an instance of the
     * {@link ElementAnnotationHandler} interface which will be used to
     * pass the found annotation elements back to the caller.
     *
     * @param  searchedTypeName the fully-qualified name of the annotation
     *         to be searched for. Cannot be <code>null</code>.
     * @param  kinds the set of kinds to be searched for.
     *         Cannot be neither <code>null</code> nor empty.
     * @param  handler a {@link ElementAnnotationHandler}. Its <code>elementAnnotation</code>
     *         method will be invoked once for each element annotated with the annotation
     *         passed in the <code>searchedTypeName</code> parameter, with
     *         the <code>element</code> parameter set to the annotated element, and the
     *         <code>annotation</code> parameter set to an {@link AnnotationMirror}
     *         (of type <code>searchedTypeName</code>) which that type is annotated with.
     *         Cannot be null.
     * @throws InterruptedException when the search was interrupted (for 
     *         example because {@link org.netbeans.api.java.source.ClassIndex#getElements}
     *         was interrupted).
     */
    public void findAnnotations(final String searchedTypeName, Set<ElementKind> kinds, final AnnotationHandler handler) throws InterruptedException {
        Parameters.notNull("searchedTypeName", searchedTypeName); // NOI18N
        Parameters.notNull("kinds", kinds); // NOI18N
        Parameters.notNull("handler", handler); // NOI18N
        LOGGER.log(Level.FINE, "findAnnotations called with {0} for {1}", new Object[] { searchedTypeName, kinds }); // NOI18N
        if (kinds.isEmpty()) {
            LOGGER.log(Level.WARNING, "findAnnotations: no kinds given"); // NOI18N
            return;
        }
        CompilationController controller = helper.getCompilationController();
        TypeElement searchedType = controller.getElements().getTypeElement(searchedTypeName);
        if (searchedType == null) {
            LOGGER.log(Level.WARNING, "findAnnotations: could not find type {0}", searchedTypeName); // NOI18N
            return;
        }
        ElementHandle<TypeElement> searchedTypeHandle = ElementHandle.create(searchedType);
        final Set<ElementHandle<TypeElement>> elementHandles = helper.getClasspathInfo().getClassIndex().getElements(
                searchedTypeHandle,
                EnumSet.of(SearchKind.TYPE_REFERENCES),
                EnumSet.of(SearchScope.SOURCE, SearchScope.DEPENDENCIES));
        if (elementHandles == null) {
            throw new InterruptedException("ClassIndex.getElements() was interrupted"); // NOI18N
        }
        TypeMirror searchedTypeMirror = searchedType.asType();
        Set<ElementKind> nonTypeKinds = EnumSet.copyOf(kinds);
        nonTypeKinds.removeAll(TYPE_KINDS);
        Set<ElementKind> typeKinds = EnumSet.copyOf(kinds);
        typeKinds.retainAll(TYPE_KINDS);
        for (ElementHandle<TypeElement> elementHandle : elementHandles) {
            LOGGER.log(Level.FINE, "found element {0}", elementHandle.getQualifiedName()); // NOI18N
            TypeElement typeElement = elementHandle.resolve(controller);
            if (typeElement == null) {
                continue;
            }
            
            // class etc.
            if (!typeKinds.isEmpty()) {
                handleAnnotation(handler, typeElement, typeElement, searchedTypeName);
            }
            
            // methods & fields
            if (!nonTypeKinds.isEmpty()) {
                for (Element element : typeElement.getEnclosedElements()) {
                    if (nonTypeKinds.contains(element.getKind())) {
                        handleAnnotation(handler, typeElement, element, searchedTypeName);
                    }
                }
            }
        }
    }
    
    private void handleAnnotation(final AnnotationHandler handler, 
            final TypeElement typeElement, final Element element, 
            final String searchedTypeName) {
        
        List<? extends AnnotationMirror> fieldAnnotationMirrors = element.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : fieldAnnotationMirrors) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            String annotationTypeName = helper.getAnnotationTypeName(annotationType);
            // issue 110819: need to compare the real type names, since the annotation can be @<any>
            if (searchedTypeName.equals(annotationTypeName)) {
                LOGGER.log(
                        Level.FINE,
                        "notifying type {0}, element {1}, annotation {2}", // NOI18N
                        new Object[] { typeElement.getQualifiedName(), element.getSimpleName(), annotationMirror });
                handler.handleAnnotation(typeElement, element, annotationMirror);
            } else {
                LOGGER.log(
                        Level.FINE,
                        "type name mismatch, ignoring type {0}, element {1}, annotation {2}", // NOI18N
                        new Object[] { typeElement.getQualifiedName(), element.getSimpleName(), annotationMirror });
            }
        }
    }
}
