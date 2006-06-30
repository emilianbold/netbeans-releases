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
