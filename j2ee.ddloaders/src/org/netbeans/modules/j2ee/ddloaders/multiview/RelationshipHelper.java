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

import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;

/**
 * @author pfiala
 */
public class RelationshipHelper {
    private static final String MULTIPLICITY_MANY = "Many";
    private static final String MULTIPLICITY_ONE = "One";

    private final EjbRelation relation;

    public static class RelationshipRoleHelper {
        private final EjbRelationshipRole role;

        public RelationshipRoleHelper(EjbRelationshipRole role) {
            this.role = role;
        }

        public boolean isMultiple() {
            return MULTIPLICITY_MANY.equals(role.getMultiplicity());
        }

        public void setMultiple(boolean multiple) {
            role.setMultiplicity(multiple ? MULTIPLICITY_MANY : MULTIPLICITY_ONE);
        }

        public String getEjbName() {
            return role.getRelationshipRoleSource().getEjbName();
        }

        public void setEjbName(String ejbName) {
            role.getRelationshipRoleSource().setEjbName(ejbName);
        }

        public String getRoleName() {
            return role.getEjbRelationshipRoleName();
        }

        public void setRoleName(String roleName) {
            role.setEjbRelationshipRoleName(roleName);
        }

        public String getFieldName() {
            CmrField field = role.getCmrField();
            return field == null ? null : field.getCmrFieldName();
        }

        public String getFieldType() {
            CmrField field = role.getCmrField();
            return field == null ? null : field.getCmrFieldType();
        }

        public boolean isCascadeDelete() {
            return role.isCascadeDelete();
        }

        public void setCascadeDelete(boolean cascadeDelete) {
            role.setCascadeDelete(cascadeDelete);
        }

        public CmrField getCmrField() {
            return role.getCmrField();
        }

        public void setCmrField(CmrField cmrField) {
            role.setCmrField(cmrField);
        }

        public void setCmrField(String fieldName, String fieldType) {
            CmrField field = role.getCmrField();
            if (field == null) {
                role.setCmrField(field = role.newCmrField());
            }
            field.setCmrFieldName(fieldName);
            field.setCmrFieldType(fieldType);
        }

    }

    public final RelationshipRoleHelper roleA;
    public final RelationshipRoleHelper roleB;

    public RelationshipHelper(EjbRelation relation) {
        this.relation = relation;
        roleA = new RelationshipRoleHelper(relation.getEjbRelationshipRole());
        roleB = new RelationshipRoleHelper(relation.getEjbRelationshipRole2());
    }

    public RelationshipHelper(Relationships singleRelationships) {
        relation = singleRelationships.newEjbRelation();
        EjbRelationshipRole role = newRole();
        relation.setEjbRelationshipRole(role);
        EjbRelationshipRole role2 = newRole();
        relation.setEjbRelationshipRole2(role2);
        singleRelationships.addEjbRelation(relation);
        roleA = new RelationshipRoleHelper(role);
        roleB = new RelationshipRoleHelper(role2);
    }

    private EjbRelationshipRole newRole() {
        EjbRelationshipRole role = relation.newEjbRelationshipRole();
        role.setRelationshipRoleSource(role.newRelationshipRoleSource());
        return role;
    }

    public String getRelationName() {
        return relation.getEjbRelationName();
    }

    public void setRelationName(String relationName) {
        relation.setEjbRelationName(relationName);
    }

    public String getDescription() {
        return relation.getDefaultDescription();
    }

    public void setDescription(String description) {
        relation.setDescription(description);
    }

}
