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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 *
 * @author Peter Liu
 */
public class Utils {

    private static final String URI_TEMPLATE = "javax.ws.rs.UriTemplate"; //NOI18N
    private static final String HTTP_METHOD = "javax.ws.rs.HttpMethod"; //NOI18N
    private static final String CONSUME_MIME = "javax.ws.rs.ConsumeMime"; //NOI18N
    private static final String PRODUCE_MIME = "javax.ws.rs.ProduceMime"; //NOI18N
    private static final String VALUE = "value";        //NOI18N

    public static String getUriTemplate(Element element) {
        return getAnnotationValue(element, URI_TEMPLATE, VALUE);
    }

    public static String getConsumeMime(Element element) {
        return getAnnotationValue(element, CONSUME_MIME, VALUE);
    }

    public static String getProduceMime(Element element) {
        return getAnnotationValue(element, PRODUCE_MIME, VALUE);
    }

    public static String getHttpMethod(Element element) {
        return getAnnotationValue(element, HTTP_METHOD, VALUE);
    }

    public static boolean hasUriTemplate(Element element) {
        return hasAnnotationType(element, URI_TEMPLATE);
    }
    
    public static boolean hasHttpMethod(Element element) {
        return hasAnnotationType(element, HTTP_METHOD);
    }
    
    public static boolean hasConsumeMime(Element element) {
        return hasAnnotationType(element, CONSUME_MIME);
    }
    
    public static boolean hasProduceMime(Element element) {
        return hasAnnotationType(element, PRODUCE_MIME);
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
}
