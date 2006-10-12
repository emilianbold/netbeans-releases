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
 * SunApplication.java
 *
 * Created on November 21, 2004, 12:47 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.app;

import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;

public interface SunApplication extends org.netbeans.modules.j2ee.sun.dd.api.RootInterface {

        public static final String VERSION_5_0_0 = "5.00"; //NOI18N
        public static final String VERSION_1_4_0 = "1.40"; //NOI18N
        public static final String VERSION_1_3_0 = "1.30"; //NOI18N
        
        public static final String WEB = "Web";	// NOI18N
	public static final String PASS_BY_REFERENCE = "PassByReference";	// NOI18N
	public static final String UNIQUE_ID = "UniqueId";	// NOI18N
	public static final String SECURITY_ROLE_MAPPING = "SecurityRoleMapping";	// NOI18N
	public static final String REALM = "Realm";	// NOI18N
        
	public void setWeb(int index, Web value);
	public Web getWeb(int index);
	public int sizeWeb();
	public void setWeb(Web[] value);
	public Web[] getWeb();
	public int addWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web value);
	public int removeWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web value);
	public Web newWeb();

        /** Setter for pass-by-reference property
        * @param value property value
        */
	public void setPassByReference(String value);
        /** Getter for pass-by-reference property.
        * @return property value
        */
	public String getPassByReference();
        /** Setter for unique-id property
        * @param value property value
        */
	public void setUniqueId(String value);
        /** Getter for unique-id property.
        * @return property value
        */
	public String getUniqueId();

	public void setSecurityRoleMapping(int index, SecurityRoleMapping value);
	public SecurityRoleMapping getSecurityRoleMapping(int index);
	public int sizeSecurityRoleMapping();
	public void setSecurityRoleMapping(SecurityRoleMapping[] value);
	public SecurityRoleMapping[] getSecurityRoleMapping();
	public int addSecurityRoleMapping(SecurityRoleMapping value);
	public int removeSecurityRoleMapping(SecurityRoleMapping value);
	public SecurityRoleMapping newSecurityRoleMapping();

        /** Setter for realm property
        * @param value property value
        */
	public void setRealm(String value);
        /** Getter for realm property.
        * @return property value
        */
	public String getRealm();

}
