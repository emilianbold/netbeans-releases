/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.dd.api.web;
import org.netbeans.modules.j2ee.dd.api.common.*;
/**
 * Generated interface for AuthConstraint element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface AuthConstraint extends CommonDDBean, DescriptionInterface {
        /** Setter for role-name property.
         * @param index index of role-name element
         * @param value value for role-name element 
         */
	public void setRoleName(int index, java.lang.String value);
        /** Getter for role-name property.
         * @param index index of role-name element
         * @return role-name element value 
         */
	public java.lang.String getRoleName(int index);
        /** Setter for role-name property.
         * @param value array of role-name values
         */
	public void setRoleName(java.lang.String[] value);
        /** Getter for role-name property.
         * @return string array of role-name elements 
         */
	public java.lang.String[] getRoleName();
        /** Returns size of role-name elements.
         * @return size of role-names 
         */
	public int sizeRoleName();
        /** Adds role-name element.
         * @param value value for role-name element
         * @return index of new role-name
         */
	public int addRoleName(java.lang.String value);
        /** Removes role-name element.
         * @param value role-name to be removed
         * @return index of the removed role-name
         */
	public int removeRoleName(java.lang.String value);

}
