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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.JavaContextListener;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class EmbeddableImpl extends PersistentObject implements Embeddable, JavaContextListener {
    
    private final EntityMappingsImpl root;
    
    // persistent
    private String class2;
    
    // transient: set to null in javaContextLeft()
    private EmbeddableAttributesImpl attributes;
    
    public EmbeddableImpl(AnnotationModelHelper helper, EntityMappingsImpl root, TypeElement typeElement) {
        super(helper, typeElement);
        this.root = root;
        helper.addJavaContextListener(this);
        boolean valid = refresh(typeElement);
        assert valid;
    }
    
    boolean refresh(TypeElement typeElement) {
        class2 = typeElement.getQualifiedName().toString();
        AnnotationModelHelper helper = getHelper();
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(typeElement.getAnnotationMirrors());
        AnnotationMirror embeddableAnn = annByType.get("javax.persistence.Embeddable"); // NOI18N
        return embeddableAnn != null;
    }
    
    EntityMappingsImpl getRoot() {
        return root;
    }
    
    public void javaContextLeft() {
        attributes = null;
    }

    public void setClass2(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getClass2() {
        return class2;
    }

    public void setAccess(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getAccess() {
        return getAttributes().hasFieldAccess() ? FIELD_ACCESS : PROPERTY_ACCESS;
    }

    public void setMetadataComplete(boolean value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public boolean isMetadataComplete() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setDescription(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getDescription() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setAttributes(EmbeddableAttributes value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmbeddableAttributesImpl getAttributes() {
        if (attributes == null) {
            attributes = new EmbeddableAttributesImpl(this);
        }
        return attributes;
    }

    public EmbeddableAttributes newEmbeddableAttributes() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
    
    public String toString() {
        return "EmbeddableImpl[class2='" + class2 + "']"; // NOI18N
    }
}
