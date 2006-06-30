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

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface;

public interface EjbRelationshipRole extends CommonDDBean, DescriptionInterface {

    public static final String EJB_RELATIONSHIP_ROLE_NAME = "EjbRelationshipRoleName";	// NOI18N
    public static final String EJBRELATIONSHIPROLENAMEID = "EjbRelationshipRoleNameId";	// NOI18N
    public static final String MULTIPLICITY = "Multiplicity";	// NOI18N
    public static final String CASCADE_DELETE = "CascadeDelete";	// NOI18N
    public static final String CASCADEDELETEID = "CascadeDeleteId";	// NOI18N
    public static final String RELATIONSHIP_ROLE_SOURCE = "RelationshipRoleSource";	// NOI18N
    public static final String CMR_FIELD = "CmrField";	// NOI18N   
    public static final String MULTIPLICITY_ONE = "One"; // NOI18N  
    public static final String MULTIPLICITY_MANY = "Many"; // NOI18N  
        
    public void setEjbRelationshipRoleName(String value);

    public String getEjbRelationshipRoleName();
        
    public void setEjbRelationshipRoleNameId(java.lang.String value);

    public java.lang.String getEjbRelationshipRoleNameId();
        
    public void setMultiplicity(String value);
    
    public String getMultiplicity();
    
    public void setCascadeDelete(boolean value);
    
    public boolean isCascadeDelete();
        
    public void setRelationshipRoleSource(RelationshipRoleSource value);
    
    public RelationshipRoleSource getRelationshipRoleSource();
    
    public RelationshipRoleSource newRelationshipRoleSource();
        
    public void setCmrField(CmrField value);
    
    public CmrField getCmrField();
        
    public CmrField newCmrField();
    
}
 

