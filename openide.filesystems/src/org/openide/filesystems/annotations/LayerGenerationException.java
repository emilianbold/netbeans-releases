/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.openide.filesystems.annotations;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;

/**
 * Exception thrown when a layer entry cannot be generated due to erroneous sources.
 * @since XXX
 */
public class LayerGenerationException extends Exception {

    final Element erroneousElement;
    final AnnotationMirror erroneousAnnotation;
    final AnnotationValue erroneousAnnotationValue;

    /**
     * An exception with no associated element.
     * @param message a detail message which could be reported to the user
     * @see Messager#printMessage(javax.tools.Diagnostic.Kind, CharSequence)
     */
    public LayerGenerationException(String message) {
        this(message, null, null, null);
    }

    /**
     * An exception with an associated element.
     * @param message a detail message which could be reported to the user
     * @param erroneousElement the associated element
     * @see Messager#printMessage(javax.tools.Diagnostic.Kind, CharSequence, Element)
     */
    public LayerGenerationException(String message, Element erroneousElement) {
        this(message, erroneousElement, null, null);
    }

    /**
     * An exception with an associated annotation.
     * @param message a detail message which could be reported to the user
     * @param erroneousElement the associated element
     * @param erroneousAnnotation the annotation on the element
     * @see Messager#printMessage(javax.tools.Diagnostic.Kind, CharSequence, Element, AnnotationMirror)
     */
    public LayerGenerationException(String message, Element erroneousElement, AnnotationMirror erroneousAnnotation) {
        this(message, erroneousElement, erroneousAnnotation, null);
    }

    /**
     * An exception with an associated annotation value.
     * @param message a detail message which could be reported to the user
     * @param erroneousElement the associated element
     * @param erroneousAnnotation the annotation on the element
     * @param erroneousAnnotationValue the value of that annotation
     * @see Messager#printMessage(javax.tools.Diagnostic.Kind, CharSequence, Element, AnnotationMirror, AnnotationValue)
     */
    public LayerGenerationException(String message, Element erroneousElement, AnnotationMirror erroneousAnnotation, AnnotationValue erroneousAnnotationValue) {
        super(message);
        this.erroneousElement = erroneousElement;
        this.erroneousAnnotation = erroneousAnnotation;
        this.erroneousAnnotationValue = erroneousAnnotationValue;
    }

}
