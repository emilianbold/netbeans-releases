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
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public interface Session extends EntityAndSession {

    public static final String SERVICE_ENDPOINT = "ServiceEndpoint";	// NOI18N
    public static final String SESSION_TYPE = "SessionType";	// NOI18N
    public static final String TRANSACTION_TYPE = "TransactionType";	// NOI18N
    public static final String SESSION_TYPE_STATEFUL = "Stateful"; // NOI18N
    public static final String SESSION_TYPE_STATELESS = "Stateless"; // NOI18N
    public static final String TRANSACTION_TYPE_BEAN = "Bean"; // NOI18N
    public static final String TRANSACTION_TYPE_CONTAINER = "Container"; // NOI18N
    
    public String getSessionType();
    
    public void setSessionType(String value);

    public String getTransactionType(); 
    
    public void setTransactionType(String value);

    //2.1
        
    public void setServiceEndpoint(java.lang.String value) throws VersionNotSupportedException;
    
    public java.lang.String getServiceEndpoint() throws VersionNotSupportedException;
    
}

