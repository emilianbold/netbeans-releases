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
package org.netbeans.modules.j2ee.ejbverification;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 *
 * @author Tomasz.Slota
 */
public class JavaUtils {
    public static String extractClassNameFromType(TypeMirror type){
        if (type instanceof DeclaredType){
            Element elem = ((DeclaredType)type).asElement();
            
            if (elem.getKind() == ElementKind.CLASS
                    || elem.getKind() == ElementKind.INTERFACE){
                return ((TypeElement)elem).getQualifiedName().toString();
            }
        }
        
        return null;
    }
    
    public static AnnotationMirror findAnnotation(Element element, String annotationClass){
        for (AnnotationMirror ann : element.getAnnotationMirrors()){
            if (annotationClass.equals(ann.getAnnotationType().toString())){
                return ann;
            }
        }
        
        return null;
    }
    
    /**
     * @return the value of annotation attribute, null if the attribute
     * was not found or when ann was null
     */
    public static AnnotationValue getAnnotationAttrValue(AnnotationMirror ann, String attrName){
        if (ann != null){
            for (ExecutableElement attr : ann.getElementValues().keySet()){
                if (attrName.equals(attr.getSimpleName().toString())){
                    return ann.getElementValues().get(attr);
                }
            }
        }
        
        return null;
    }
    
    public static String getShortClassName(String qualifiedClassName){
        return qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".") + 1); //NOI18N
    }
}