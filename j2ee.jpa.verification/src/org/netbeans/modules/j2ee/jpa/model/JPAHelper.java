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


package org.netbeans.modules.j2ee.jpa.model;

import java.util.Collection;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;

/**
 * Utility methods for discovering various facts
 * about JPA model
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public class JPAHelper {
    
    /**
     * Utility method to find out if any member is annotated as Id or
     * EmbeddedId in this class? It does not check any of the inheritted
     * members.
     *
     * @param javaClass JavaClass whose members will be inspected.
     * @return returns true if atleast one member is annotated as Id or EmbeddedId
     */
    public static boolean isAnyMemberAnnotatedAsIdOrEmbeddedId(TypeElement javaClass) {
        for (Element classElement : javaClass.getEnclosedElements()) {
            
            if (Utilities.findAnnotation(classElement, JPAAnnotations.ID) != null){
                return true;
            }
            
            if (Utilities.findAnnotation(classElement, JPAAnnotations.EMBEDDED_ID) != null){
                return true;
            }
        }
        return false;
    }
    
    /**
     * @return name of the primary table that will be mapped to given entity class
     */
    public static String getPrimaryTableName(TypeElement entityClass){
        String name = null;
        AnnotationMirror annTable = Utilities.findAnnotation(entityClass, JPAAnnotations.TABLE);
        AnnotationValue nameAttrValue = Utilities.getAnnotationAttrValue(annTable, JPAAnnotations.NAME_ATTR);
        
        if (nameAttrValue != null){
            name = nameAttrValue.getValue().toString();
        }
        else {
            AnnotationMirror annEntity = Utilities.findAnnotation(entityClass, JPAAnnotations.ENTITY);
            nameAttrValue = Utilities.getAnnotationAttrValue(annEntity, JPAAnnotations.NAME_ATTR);
            
            if (nameAttrValue == null){
                name = entityClass.getSimpleName().toString();
            } else{
                name = nameAttrValue.getValue().toString();
            }
        }
        
        assert name != null;
        
        return name;
    }
    
    public static AnnotationMirror getFirstAnnotationFromGivenSet(Element element,
            Collection<String> searchedAnnotations){
        
        for (AnnotationMirror ann : element.getAnnotationMirrors()){
            String annType = ann.getAnnotationType().toString();
            
            if (searchedAnnotations.contains(annType)){
                return ann;
            }
        }
        
        return null;
    }
    
    /**
     *
     */
    public static AccessType findAccessType(TypeElement entityClass){
        AccessType accessType = AccessType.INDETERMINED;
        
        // look for the first element annotated with a JPA field annotation
        for (Element element : entityClass.getEnclosedElements()){
            if (element.getKind() == ElementKind.FIELD || element.getKind() == ElementKind.METHOD){
                AnnotationMirror ann = getFirstAnnotationFromGivenSet(element, JPAAnnotations.MEMBER_LEVEL);
                
                if (ann != null){
                    accessType = element.getKind() == ElementKind.FIELD ?
                        AccessType.FIELD : AccessType.PROPERTY;
                    
                    break;
                }
            }
        }
        
        if (accessType.isDetermined()){
            // check if access type is consistent
            Collection<? extends Element> otherElems = null;
            
            if (accessType == AccessType.FIELD){
                otherElems = ElementFilter.methodsIn(entityClass.getEnclosedElements());
            }
            else{
                otherElems = ElementFilter.fieldsIn(entityClass.getEnclosedElements());
            }
            
            for (Element element : otherElems){
                AnnotationMirror ann = getFirstAnnotationFromGivenSet(element, JPAAnnotations.MEMBER_LEVEL);
                
                if (ann != null){
                    accessType = AccessType.INCONSISTENT;
                    break;
                }
            }
        }
        
        return accessType;
    }
}
