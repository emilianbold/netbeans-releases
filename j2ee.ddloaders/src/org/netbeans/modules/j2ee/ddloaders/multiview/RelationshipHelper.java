/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;

/**
 * @author pfiala
 */
public class RelationshipHelper {
    private static final String MULTIPLICITY_MANY = "Many";
    private static final String MULTIPLICITY_ONE = "One";

    private final EjbRelation relation;
    private final EjbRelationshipRole role;
    private final EjbRelationshipRole role2;

    public RelationshipHelper(EjbRelation relation) {
        this.relation = relation;
        role = relation.getEjbRelationshipRole();
        role2 = relation.getEjbRelationshipRole2();
    }

    public RelationshipHelper(Relationships singleRelationships) {
        relation = singleRelationships.newEjbRelation();
        role = relation.newEjbRelationshipRole();
        role.setRelationshipRoleSource(role.newRelationshipRoleSource());
        relation.setEjbRelationshipRole(role);
        role2 = relation.newEjbRelationshipRole();
        role2.setRelationshipRoleSource(role2.newRelationshipRoleSource());
        relation.setEjbRelationshipRole2(role2);
        singleRelationships.addEjbRelation(relation);
    }

    public String getRelationName() {
        return relation.getEjbRelationName();
    }

    public void setRelationName(String relationName) {
        relation.setEjbRelationName(relationName);
    }

    public boolean isMultiple() {
        return MULTIPLICITY_MANY.equals(role.getMultiplicity());
    }

    public void setMultiple(boolean multiple) {
        role.setMultiplicity(multiple ? MULTIPLICITY_MANY : MULTIPLICITY_ONE);
    }

    public boolean isMultiple2() {
        return MULTIPLICITY_MANY.equals(role2.getMultiplicity());
    }

    public void setMultiple2(boolean multiple) {
        role2.setMultiplicity(multiple ? MULTIPLICITY_MANY : MULTIPLICITY_ONE);
    }

    public String getEjbName() {
        return role.getRelationshipRoleSource().getEjbName();
    }

    public void setEjbName(String ejbName) {
        role.getRelationshipRoleSource().setEjbName(ejbName);
    }

    public String getEjbName2() {
        return role2.getRelationshipRoleSource().getEjbName();
    }

    public void setEjbName2(String ejbName) {
        role2.getRelationshipRoleSource().setEjbName(ejbName);
    }

    public String getRoleName() {
        return role.getEjbRelationshipRoleName();
    }

    public void setRoleName(String roleName) {
        role.setEjbRelationshipRoleName(roleName);
    }

    public String getRoleName2() {
        return role2.getEjbRelationshipRoleName();
    }

    public void setRoleName2(String roleName) {
        role.setEjbRelationshipRoleName(roleName);
    }

    public String getFieldName() {
        CmrField field = role.getCmrField();
        return field == null ? null : field.getCmrFieldName();
    }

    //todo: remove
    public void setFieldName(String fieldName) {
        role.getCmrField().setCmrFieldName(fieldName);
    }

    public String getFieldName2() {
        CmrField field = role2.getCmrField();
        return field == null ? null : field.getCmrFieldName();
    }

    //todo: remove
    public void setFieldName2(String fieldName) {
        role2.getCmrField().setCmrFieldName(fieldName);
    }

    public String getFieldType() {
        CmrField field = role.getCmrField();
        return field == null ? null : field.getCmrFieldType();
    }

    //todo: remove
    public void setFieldType(String fieldType) {
        role.getCmrField().setCmrFieldType(fieldType);
    }

    public String getFieldType2() {
        CmrField field = role2.getCmrField();
        return field == null ? null : field.getCmrFieldType();
    }

    //todo: remove
    public void setFieldType2(String fieldType) {
        role2.getCmrField().setCmrFieldType(fieldType);
    }

    public String getDescription() {
        return relation.getDefaultDescription();
    }

    public void setDescription(String description) {
        relation.setDescription(description);
    }

    public boolean isCascadeDelete() {
        return role.isCascadeDelete();

    }

    public void setCascadeDelete(boolean cascadeDelete) {
        role.setCascadeDelete(cascadeDelete);
    }

    public boolean isCascadeDelete2() {
        return role2.isCascadeDelete();
    }

    public void setCascadeDelete2(boolean cascadeDelete) {
        role2.setCascadeDelete(cascadeDelete);
    }

    public CmrField getCmrField() {
        return role.getCmrField();
    }

    public CmrField getCmrField2() {
        return role2.getCmrField();
    }

    public void setCmrField(CmrField cmrField) {
        role.setCmrField(cmrField);
    }

    public void setCmrField2(CmrField cmrField) {
        role2.setCmrField(cmrField);
    }

    public void setCmrField(String fieldName, String fieldType) {
        CmrField field = role.getCmrField();
        if (field == null) {
            role.setCmrField(field = role.newCmrField());
        }
        field.setCmrFieldName(fieldName);
        field.setCmrFieldType(fieldType);
    }

    public void setCmrField2(String fieldName, String fieldType) {
        CmrField field = role2.getCmrField();
        if (field == null) {
            role2.setCmrField(field = role2.newCmrField());
        }
        field.setCmrFieldName(fieldName);
        field.setCmrFieldType(fieldType);
    }
}
