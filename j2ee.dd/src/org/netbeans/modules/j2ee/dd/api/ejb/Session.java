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
import org.netbeans.api.web.dd.common.VersionNotSupportedException;

public interface Session extends EntityAndSession {
     
    public static final String SERVICE_ENDPOINT = "ServiceEndpoint";	// NOI18N
    public static final String SESSION_TYPE = "SessionType";	// NOI18N
    public static final String TRANSACTION_TYPE = "TransactionType";	// NOI18N
    
    public String getSessionType();
    
    public void setSessionType(String value);

    public String getTransactionType(); 
    
    public void setTransactionType(String value);

    //2.1
        
    public void setServiceEndpoint(java.lang.String value) throws VersionNotSupportedException;
    
    public java.lang.String getServiceEndpoint() throws VersionNotSupportedException;
    
}

