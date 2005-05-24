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
 * AsContext.java
 *
 * Created on November 18, 2004, 10:03 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface AsContext extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String AUTH_METHOD = "AuthMethod";	// NOI18N
    public static final String REALM = "Realm";	// NOI18N
    public static final String REQUIRED = "Required";	// NOI18N
    
    /** Setter for auth-method property
     * @param value property value
     */
    public void setAuthMethod(java.lang.String value);
    /** Getter for auth-method property.
     * @return property value
     */
    public java.lang.String getAuthMethod();
    /** Setter for realm property
     * @param value property value
     */
    public void setRealm(java.lang.String value);
    /** Getter for realm property.
     * @return property value
     */
    public java.lang.String getRealm();
    /** Setter for required property
     * @param value property value
     */
    public void setRequired(java.lang.String value);
    /** Getter for required property.
     * @return property value
     */
    public java.lang.String getRequired();
   
}
