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
 * SecurityMap.java
 *
 * Created on November 21, 2004, 2:33 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;
/**
 * @author Nitya Doraisamy
 */
public interface SecurityMap {

        public static final String NAME = "Name";	// NOI18N
	public static final String PRINCIPAL = "Principal";	// NOI18N
	public static final String USER_GROUP = "UserGroup";	// NOI18N
	public static final String BACKEND_PRINCIPAL = "BackendPrincipal";	// NOI18N
	public static final String BACKENDPRINCIPALUSERNAME = "BackendPrincipalUserName";	// NOI18N
	public static final String BACKENDPRINCIPALPASSWORD = "BackendPrincipalPassword";	// NOI18N
        
	public void setName(java.lang.String value);

	public java.lang.String getName();

	public void setPrincipal(int index, String value);

	public String getPrincipal(int index);

	public int sizePrincipal();

	public void setPrincipal(String[] value);

	public String[] getPrincipal();

	public int addPrincipal(String value);

	public int removePrincipal(String value);

	public void setUserGroup(int index, String value);

	public String getUserGroup(int index);

	public int sizeUserGroup();

	public void setUserGroup(String[] value);

	public String[] getUserGroup();

	public int addUserGroup(String value);

	public int removeUserGroup(String value);

	public void setBackendPrincipal(boolean value);

	public boolean isBackendPrincipal();

	public void setBackendPrincipalUserName(java.lang.String value);

	public java.lang.String getBackendPrincipalUserName();

	public void setBackendPrincipalPassword(java.lang.String value);

	public java.lang.String getBackendPrincipalPassword();

}
