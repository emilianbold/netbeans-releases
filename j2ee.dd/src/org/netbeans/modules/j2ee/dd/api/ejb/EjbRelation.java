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
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface;

public interface EjbRelation extends CommonDDBean, DescriptionInterface {
    
    public static final String EJB_RELATION_NAME = "EjbRelationName";	// NOI18N
    public static final String EJBRELATIONNAMEID = "EjbRelationNameId";	// NOI18N
    public static final String EJB_RELATIONSHIP_ROLE = "EjbRelationshipRole";	// NOI18N
    public static final String EJB_RELATIONSHIP_ROLE2 = "EjbRelationshipRole2";	// NOI18N
        
    public void setEjbRelationName(String value);
    
    public String getEjbRelationName();
    
    public void setEjbRelationNameId(java.lang.String value);

    public java.lang.String getEjbRelationNameId();
    
    public void setEjbRelationshipRole(EjbRelationshipRole value);
    
    public EjbRelationshipRole getEjbRelationshipRole();
    
    public EjbRelationshipRole newEjbRelationshipRole();
    
    public void setEjbRelationshipRole2(EjbRelationshipRole value);
    
    public EjbRelationshipRole getEjbRelationshipRole2();
        
}

