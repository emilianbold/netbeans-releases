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
 * SessionManager.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

public interface SessionManager extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
        
        public static final String PERSISTENCETYPE = "PersistenceType";	// NOI18N
	public static final String MANAGER_PROPERTIES = "ManagerProperties";	// NOI18N
	public static final String STORE_PROPERTIES = "StoreProperties";	// NOI18N

        
        /** Setter for persistence-type attribute
         * @param value attribute value
         */
	public void setPersistenceType(java.lang.String value);
        /** Getter for persistence-type attribute.
         * @return attribute value
         */
	public java.lang.String getPersistenceType();
        /** Setter for manager-properties property
         * @param value property value
         */
	public void setManagerProperties(ManagerProperties value);
        /** Getter for manager-properties property.
         * @return property value
         */
	public ManagerProperties getManagerProperties();

	public ManagerProperties newManagerProperties();
        /** Setter for store-properties property
         * @param value property value
         */
	public void setStoreProperties(StoreProperties value);
        /** Getter for store-properties property.
         * @return property value
         */
	public StoreProperties getStoreProperties();

	public StoreProperties newStoreProperties(); 

}
