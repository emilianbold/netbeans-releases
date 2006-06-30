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
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
/**
 * Generated interface for FilterMapping element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface FilterMapping extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {
        /** Setter for filter-name property.
         * @param value property value
         */
	public void setFilterName(java.lang.String value);
        /** Getter for filter-name property.
         * @return property value 
         */
	public java.lang.String getFilterName();
        /** Setter for url-pattern property.
         * @param value property value
         */
	public void setUrlPattern(java.lang.String value);
        /** Getter for url-pattern property.
         * @return property value 
         */
	public java.lang.String getUrlPattern();
        /** Setter for servlet-name property.
         * @param value property value
         */
	public void setServletName(java.lang.String value);
        /** Getter for servlet-name property.
         * @return property value 
         */
	public java.lang.String getServletName();
        /** Setter for dispatcher property.
         * @param index position in the array of dispatchers
         * @param value property value 
         */
	public void setDispatcher(int index, java.lang.String value) throws VersionNotSupportedException;
        /** Getter for dispatcher property.
         * @param index position in the array of dispatchers
         * @return property value 
         */
	public java.lang.String getDispatcher(int index) throws VersionNotSupportedException;
        /** Setter for dispatcher property.
         * @param index position in the array of dispatchers
         * @param value array of dispatcher properties
         */
	public void setDispatcher(java.lang.String[] value) throws VersionNotSupportedException;
        /** Getter for dispatcher property.
         * @return array of dispatcher properties
         */
	public java.lang.String[] getDispatcher() throws VersionNotSupportedException;
        /** Returns size of dispatcher properties.
         * @return number of dispatcher properties 
         */
	public int sizeDispatcher() throws VersionNotSupportedException;
        /** Adds dispatcher property.
         * @param value dispatcher property
         * @return index of new dispatcher
         */
	public int addDispatcher(java.lang.String value) throws VersionNotSupportedException;
        /** Removes dispatcher property.
         * @param value dispatcher property
         * @return index of the removed dispatcher
         */
	public int removeDispatcher(java.lang.String value) throws VersionNotSupportedException;

}
