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

public interface Query extends CommonDDBean, DescriptionInterface {
    
    public static final String QUERY_METHOD = "QueryMethod";	// NOI18N
    public static final String RESULT_TYPE_MAPPING = "ResultTypeMapping";	// NOI18N
    public static final String EJB_QL = "EjbQl";	// NOI18N
    public static final String EJBQLID = "EjbQlId";	// NOI18N
    public static final String RESULT_TYPE_MAPPING_REMOTE = "Remote"; // NOI18N
    public static final String RESULT_TYPE_MAPPING_LOCAL = "Local"; // NOI18N
        
    public void setQueryMethod(QueryMethod value);
    
    public QueryMethod getQueryMethod();
    
    public QueryMethod newQueryMethod();
    
    public void setResultTypeMapping(String value);
    
    public String getResultTypeMapping();
    
    public void setEjbQl(String value);
    
    public String getEjbQl();
        
}

