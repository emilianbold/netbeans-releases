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
package org.netbeans.modules.websvc.rest.model.impl;

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;

/**
 *
 * @author Peter Liu
 */
public class RestServiceDescriptionImpl extends PersistentObject implements RestServiceDescription {
    
    private String name;
    private String uriTemplate;
    
    public RestServiceDescriptionImpl(AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        
        this.name = typeElement.getSimpleName().toString();
        this.uriTemplate = getUriTemplate(typeElement);
    }
    
    private String getUriTemplate(TypeElement element) {
        for (AnnotationMirror annotation: element.getAnnotationMirrors()) {
            if (annotation.getAnnotationType().toString().equals("javax.ws.rs.UriTemplate")) {
                for (ExecutableElement key : annotation.getElementValues().keySet()) {
                    //System.out.println("key = " + key.getSimpleName());
                    if (key.getSimpleName().toString().equals("value")) {
                        uriTemplate = annotation.getElementValues().get(key).toString();
                        uriTemplate = uriTemplate.substring(1, uriTemplate.length()-1);
                        
                        return uriTemplate;
                    }
                }
            }
        }
        
        return "";
    }
    
    public String getName() {
        return name;
    }
    
    public String getUriTemplate() {
        return uriTemplate;
    }
    
    boolean refresh(TypeElement typeElement) {
        if (typeElement.getKind() == ElementKind.INTERFACE) {
            // don't consider interfaces (SEI classes)
            return false;
        }
        AnnotationModelHelper helper = getHelper();
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(typeElement.getAnnotationMirrors());
        if (annByType.get("javax.ws.rs.UriTemplate") != null) {
            return true;// NOI18N
        }
        
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                if (helper.hasAnnotation(element.getAnnotationMirrors(), "javax.ws.rs.HttpMethod")) {    //NOI18N
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
    public String toString() {
        return name + "[" + uriTemplate + "]";   //NOI18N
    }
}
