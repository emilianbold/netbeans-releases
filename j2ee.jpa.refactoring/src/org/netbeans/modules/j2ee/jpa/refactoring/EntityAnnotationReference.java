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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.jpa.refactoring;

import org.netbeans.api.java.source.TreePathHandle;

/**
 * This class represents an annotation reference to an entity.
 *
 * @author Erno Mononen
 */
public class EntityAnnotationReference {
    
    /**
     * The entity that has the feature with the referencing annotation.
     */
    private final String entity;
    /**
     * The FQN of the referencing annotation.
     */
    private final String annotation;
    /**
     * The referencing annotation attribute.
     */ 
    private final String attribute;
    /**
     * The value for the referencing annotation attribute.
     */ 
    private final String attributeValue;
    /**
     * The handle for the property that has the referencing annotation.
     */ 
    private final TreePathHandle handle;
    /**
     * Creates a new instance of EntityAssociation
     * @param referenced the entity that is referenced.
     * @param referring the entity that has the property with referencing annotation.
     * @param property the property that hat the referencing annotation.
     * @param annotation the referencing annotation
     * @param attributeValue the attribute value of the annotation that references other entity
     */
    public EntityAnnotationReference(String entity, String annotation, 
            String attribute, String attributeValue, TreePathHandle handle) {
        this.entity = entity;
        this.annotation = annotation;
        this.attribute = attribute;
        this.attributeValue = attributeValue;
        this.handle = handle;
    }

    /**
     *@see #entity
     */ 
    public String getEntity() {
        return entity;
    }

    /**
     *@see #annotation
     */ 
    public String getAnnotation() {
        return annotation;
    }

    /**
     *@see #attribute
     */ 
    public String getAttribute() {
        return attribute;
    }

    /**
     *@see #attributeValue
     */ 
    public String getAttributeValue() {
        return attributeValue;
    }

    /**
     *@see #handle
     */ 
    public TreePathHandle getHandle() {
        return handle;
    }
    
}
