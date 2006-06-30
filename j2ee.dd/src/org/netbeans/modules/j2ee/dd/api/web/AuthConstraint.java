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
