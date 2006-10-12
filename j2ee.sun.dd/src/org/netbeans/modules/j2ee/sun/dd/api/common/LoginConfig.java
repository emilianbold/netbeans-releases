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
/*
 * LoginConfig.java
 *
 * Created on November 18, 2004, 10:27 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface LoginConfig extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String VERSION_SERVER_8_1 = "Server 8.1";
    public static final String VERSION_SERVER_9_0 = "Server 9.0";
    
    public static final String AUTH_METHOD = "AuthMethod";	// NOI18N
    public static final String REALM = "Realm";	// NOI18N
    
    /** Setter for auth-method property
     * @param value property value
     */
    public void setAuthMethod(java.lang.String value);
    public java.lang.String getAuthMethod();
    
    // This property supported for AS 9.0+ EJB hosted endpoints only.
    public void setRealm(java.lang.String value) throws VersionNotSupportedException;
    public java.lang.String getRealm() throws VersionNotSupportedException;
    
}
