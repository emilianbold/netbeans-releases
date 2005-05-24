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
 * WebProperty.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

public interface WebProperty extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
        public static final String NAME = "Name";	// NOI18N
	public static final String VALUE = "Value";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N
        
        /** Setter for name attribute
         * @param value attribute value
         */
	public void setName(java.lang.String value);
        /** Getter for name attribute.
         * @return attribute value
         */
	public java.lang.String getName();
        /** Setter for value attribute
         * @param value attribute value
         */
	public void setValue(java.lang.String value);
        /** Getter for value attribute.
         * @return attribute value
         */
	public java.lang.String getValue();
        /** Setter for description property
         * @param value property value
         */
	public void setDescription(String value);
        /** Getter for description property.
         * @return property value
         */
	public String getDescription();

}
