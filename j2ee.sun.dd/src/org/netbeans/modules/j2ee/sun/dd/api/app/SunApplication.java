/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
                
        public static final int STATE_VALID=0;
        public static final int STATE_INVALID_PARSABLE=1;
        public static final int STATE_INVALID_UNPARSABLE=2;
        public static final String PROPERTY_STATUS="dd_status"; //NOI18N
        public static final String PROPERTY_VERSION="dd_version"; //NOI18N
        
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

        /** Setter for version property.
         * Warning : Only the upgrade from lower to higher version is supported.
         * @param version ejb-jar version value
         */
        public void setVersion(java.math.BigDecimal version);
        /** Getter for version property.
         * @return property value
         */
        public java.math.BigDecimal getVersion();
}
