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
        EjbRelationshipRole roleA = newRole();
        relation.setEjbRelationshipRole(roleA);
        EjbRelationshipRole roleB = newRole();
        relation.setEjbRelationshipRole2(roleB);
        singleRelationships.addEjbRelation(relation);
        this.roleA = new RelationshipRoleHelper(roleA);
        this.roleB = new RelationshipRoleHelper(roleB);
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
