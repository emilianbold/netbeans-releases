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

package org.netbeans.modules.j2ee.dd.api.ejb;

// 
// This interface has all of the bean info accessor methods.
// 
import org.netbeans.api.web.dd.common.CommonDDBean;
import org.netbeans.api.web.dd.common.DescriptionInterface;

public interface EjbRelationshipRole extends CommonDDBean, DescriptionInterface {
    
    public static final String EJB_RELATIONSHIP_ROLE_NAME = "EjbRelationshipRoleName";	// NOI18N
    public static final String EJBRELATIONSHIPROLENAMEID = "EjbRelationshipRoleNameId";	// NOI18N
    public static final String MULTIPLICITY = "Multiplicity";	// NOI18N
    public static final String CASCADE_DELETE = "CascadeDelete";	// NOI18N
    public static final String CASCADEDELETEID = "CascadeDeleteId";	// NOI18N
    public static final String RELATIONSHIP_ROLE_SOURCE = "RelationshipRoleSource";	// NOI18N
    public static final String CMR_FIELD = "CmrField";	// NOI18N   
    
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
 

