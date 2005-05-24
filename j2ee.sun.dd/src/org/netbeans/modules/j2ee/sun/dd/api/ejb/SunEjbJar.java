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
 * SunEjbJar.java
 *
 * Created on November 17, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;
import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
/**
 *
 * @author  Nitya Doraisamy
 */
public interface SunEjbJar extends org.netbeans.modules.j2ee.sun.dd.api.RootInterface {
    public static final String VERSION_2_1_1 = "2.11"; //NOI18N
    public static final String VERSION_2_1_0 = "2.10"; //NOI18N
    public static final String VERSION_2_0_0 = "2.00"; //NOI18N
    
    public static final int STATE_VALID=0;
    public static final int STATE_INVALID_PARSABLE=1;
    public static final int STATE_INVALID_UNPARSABLE=2;
    public static final String PROPERTY_STATUS="dd_status"; //NOI18N
    public static final String PROPERTY_VERSION="dd_version"; //NOI18N
    
    public static final String SECURITY_ROLE_MAPPING = "SecurityRoleMapping";	// NOI18N
    public static final String ENTERPRISE_BEANS = "EnterpriseBeans";	// NOI18N
        
    public SecurityRoleMapping[] getSecurityRoleMapping();
    public SecurityRoleMapping getSecurityRoleMapping(int index);
    public void setSecurityRoleMapping(SecurityRoleMapping[] value);
    public void setSecurityRoleMapping(int index, SecurityRoleMapping value);
    public int addSecurityRoleMapping(SecurityRoleMapping value);
    public int removeSecurityRoleMapping(SecurityRoleMapping value);
    public int sizeSecurityRoleMapping();
    
    public void setEnterpriseBeans(EnterpriseBeans value);
    public EnterpriseBeans getEnterpriseBeans();
    public EnterpriseBeans newEnterpriseBeans();
    
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
