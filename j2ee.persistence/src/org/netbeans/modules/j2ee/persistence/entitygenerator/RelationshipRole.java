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
package org.netbeans.modules.j2ee.persistence.entitygenerator;

/**
 * Abstract information about role in a relationship between 2 entity classes 
 * (either CMP or Java Persistence API).
 *
 * @author Chris Webster, Pavel Buzek
 */
public class RelationshipRole {
    
    private String roleName;
    private String entityName;
    private String fieldName;
    private boolean many;
    private boolean toMany;
    private boolean cascade;
    
    private EntityRelation parent;
 
    public RelationshipRole (String roleName,
            String entityName,
            String fieldName,
            boolean many,
            boolean toMany,
            boolean cascade) {
        this.setRoleName(roleName);
        this.setEntityName(entityName);
        this.setFieldName(fieldName);
        this.setMany(many);
        this.setToMany(toMany);
        this.setCascade(cascade);
    }
    
    public RelationshipRole (EntityRelation parentRelation) {
        setParent(parentRelation);
    }
    
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isMany() {
        return many;
    }

    public void setMany(boolean many) {
        this.many = many;
    }

    public boolean isToMany() {
        return toMany;
    }

    public void setToMany(boolean toMany) {
        this.toMany = toMany;
    }

    public boolean isCascade() {
        return cascade;
    }

    public void setCascade(boolean cascade) {
        this.cascade = cascade;
    }

    public EntityRelation getParent() {
        return parent;
    }

    public void setParent(EntityRelation parent) {
        this.parent = parent;
    }

}
