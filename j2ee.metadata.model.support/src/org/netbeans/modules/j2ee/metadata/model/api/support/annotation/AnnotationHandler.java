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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * A callback interface for annotated elements. Implementations of this
 * interface are used by annotation scanners such as
 * {@link AnnotationScanner} to pass annotations back to the caller.
 *
 * @author Andrei Badea, Tomas Mysik
 */
public interface AnnotationHandler {

    /**
     * This method allows implementors to process annotated elements. Typically
     * this method will be called once for each element annotated with the
     * specified annotation.
     *
     * @param type        the type in which annotated element can be found. Never <code>null</code>.
     * @param element     the element annotated with the annotation specified
     *                    by the <code>annotation</code> parameter. Never <code>null</code>.
     *                    The same as <code>type</code> param for class.
     * @param annotation  an annotation mirror.
     */
    void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation);
}
