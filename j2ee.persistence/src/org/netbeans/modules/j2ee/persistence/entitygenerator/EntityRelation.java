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
 * Abstract information about relationship between 2 entity classes
 * (either CMP or Java Persistence API).
 *
 * @author Chris Webster, Pavel Buzek
 */
public class EntityRelation {
    
    private RelationshipRole[] roles;
    private String relationName;
    
    public EntityRelation(RelationshipRole roleA, RelationshipRole roleB) {
        roles = new RelationshipRole[] {roleA, roleB};
        roleA.setParent(this);
        roleB.setParent(this);
    }
    
    public void makeRoleNamesUnique() {
        if (getRoleA().getRoleName().equals(
                getRoleB().getRoleName())) {
            
            String roleBName = getRoleB().getRoleName() + '1';
            getRoleB().setRoleName(roleBName);
        }
    }
    
    public RelationshipRole getRoleA() {
        return roles[0];
    }
    
    public RelationshipRole getRoleB() {
        return roles[1];
    }
    
    public void setRoleA(RelationshipRole roleA) {
        roles[0] = roleA;
    }
    
    public void setRoleB(RelationshipRole roleB) {
        roles[1] = roleB;
    }
    
    public String toString() {
        return "\nrelation name " + getRelationName() + // NOI18N
                "\nroleA = \n\t" + getRoleA() + // NOI18N
                "\nroleB = \n\t" + getRoleB(); // NOI18N
    }
    
    public String getRelationName() {
        return relationName;
    }
    
    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }
    
    public RelationshipRole[] getRoles() {
        return roles;
    }
}
