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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
