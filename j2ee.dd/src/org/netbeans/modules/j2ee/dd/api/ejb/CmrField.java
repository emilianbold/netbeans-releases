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

public interface CmrField extends CommonDDBean, DescriptionInterface {
    
    public static final String CMR_FIELD_NAME = "CmrFieldName";	// NOI18N
    public static final String CMRFIELDNAMEID = "CmrFieldNameId";	// NOI18N
    public static final String CMR_FIELD_TYPE = "CmrFieldType";	// NOI18N
        
    public void setCmrFieldName(String value);
    
    public String getCmrFieldName();
 
    public void setCmrFieldType(String value);
    
    public String getCmrFieldType();
    
    public void setCmrFieldNameId(java.lang.String value);

    public java.lang.String getCmrFieldNameId();
    
}

