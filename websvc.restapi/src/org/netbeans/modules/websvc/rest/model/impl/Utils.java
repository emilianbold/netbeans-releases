/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.websvc.rest.model.impl;

import java.io.IOException;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Peter Liu
 */
public class Utils {

    private static final String VALUE = "value";        //NOI18N

    public static String getUriTemplate(Element element) {
        return getAnnotationValue(element, RestConstants.PATH, VALUE);
    }

    public static String getConsumeMime(Element element) {
        return getAnnotationValue(element, RestConstants.CONSUME_MIME, VALUE);
    }

    public static String getProduceMime(Element element) {
        return getAnnotationValue(element, RestConstants.PRODUCE_MIME, VALUE);
    }

    public static String getHttpMethod(Element element) {
        if (hasAnnotationType(element, RestConstants.GET)) {
            return RestConstants.GET_ANNOTATION;
        } else if (hasAnnotationType(element, RestConstants.POST)) {
            return RestConstants.POST_ANNOTATION;
        } else if (hasAnnotationType(element, RestConstants.PUT)) {
            return RestConstants.PUT_ANNOTATION;
        } else if (hasAnnotationType(element, RestConstants.DELETE)) {
            return RestConstants.DELETE_ANNOTATION;
        }
        return null;
    }

    public static boolean hasUriTemplate(Element element) {
        return hasAnnotationType(element, RestConstants.PATH);
    }
    
    public static boolean hasHttpMethod(Element element) {
        return element.getKind() == ElementKind.METHOD && 
              (hasAnnotationType(element, RestConstants.GET) ||
               hasAnnotationType(element, RestConstants.POST) ||
               hasAnnotationType(element, RestConstants.PUT) ||
               hasAnnotationType(element, RestConstants.DELETE));
    }
    
    public static boolean hasConsumeMime(Element element) {
        return hasAnnotationType(element, RestConstants.CONSUME_MIME);
    }
    
    public static boolean hasProduceMime(Element element) {
        return hasAnnotationType(element, RestConstants.PRODUCE_MIME);
    }

    private static String getAnnotationValue(Element element, String annotationType, String paramName) {
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            if (annotation.getAnnotationType().toString().equals(annotationType)) {
                for (ExecutableElement key : annotation.getElementValues().keySet()) {
                    //System.out.println("key = " + key.getSimpleName());
                    if (key.getSimpleName().toString().equals(paramName)) {
                        String value = annotation.getElementValues().get(key).toString();
                        value = stripQuotes(value);

                        return value;
                    }
                }
            }
        }

        return "";
    }

    private static boolean hasAnnotationType(Element element, String annotationType) {
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            if (annotation.getAnnotationType().toString().equals(annotationType)) {
                return true;
            }
        }

        return false;
    }

    private static String stripQuotes(String value) {
        return value.substring(value.indexOf("\"") + 1, value.lastIndexOf("\""));
    }
    
    static boolean checkForJsr311Bootstrap(TypeElement element, Project project, AnnotationModelHelper helper) {
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        if (restSupport != null && ! restSupport.isRestSupportOn() && 
                isRest(element, helper)) {
            try {
                restSupport.ensureRestDevelopmentReady();
                return true;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }
    
    public static ClasspathInfo getClassPathInfo(RestSupport restSupport) {
        FileObject root = restSupport.findSourceRoot();
        if (root != null) {
            return ClasspathInfo.create(root);
        }
        return null;
    }
    
    static boolean isRest(TypeElement type, AnnotationModelHelper helper) {
        boolean isRest = false;
        if (type.getKind() != ElementKind.INTERFACE) { // don't consider interfaces

            if (helper.hasAnnotation(type.getAnnotationMirrors(), RestConstants.PATH)) { // NOI18N
                isRest = true;
            } else {
                for (Element element : type.getEnclosedElements()) {
                    if (Utils.hasHttpMethod(element)) {
                        isRest = true;
                        break;
                    }
                }
            }
        }
        return isRest;
    }

}
