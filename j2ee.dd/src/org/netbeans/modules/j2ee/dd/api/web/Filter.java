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
/**
 * Generated interface for Filter element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface Filter extends org.netbeans.modules.j2ee.dd.api.common.ComponentInterface  {
        /** Setter for filter-name property.
         * @param value property value
         */
	public void setFilterName(java.lang.String value);
        /** Getter for filter-name property.
         * @return property value 
         */
	public java.lang.String getFilterName();
        /** Setter for filter-class property.
         * @param value property value
         */
	public void setFilterClass(java.lang.String value);
        /** Getter for filter-class property.
         * @return property value 
         */
	public java.lang.String getFilterClass();
        /** Setter for init-param element.
         * @param index position in the array of elements
         * @param valueInterface init-param element (InitParam object)
         */
	public void setInitParam(int index, org.netbeans.modules.j2ee.dd.api.web.InitParam valueInterface);
        /** Getter for init-param element.
         * @param index position in the array of elements
         * @return init-param element (InitParam object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.InitParam getInitParam(int index);
        /** Setter for init-param elements.
         * @param value array of init-param elements (InitParam objects)
         */
	public void setInitParam(org.netbeans.modules.j2ee.dd.api.web.InitParam[] value);
        /** Getter for init-param elements.
         * @return array of init-param elements (InitParam objects)
         */
	public org.netbeans.modules.j2ee.dd.api.web.InitParam[] getInitParam();
        /** Returns size of init-param elements.
         * @return number of init-param elements 
         */
	public int sizeInitParam();
        /** Adds init-param element.
         * @param valueInterface init-param element (InitParam object)
         * @return index of new init-param
         */
	public int addInitParam(org.netbeans.modules.j2ee.dd.api.web.InitParam valueInterface);
        /** Removes init-param element.
         * @param valueInterface init-param element (InitParam object)
         * @return index of the removed init-param
         */
	public int removeInitParam(org.netbeans.modules.j2ee.dd.api.web.InitParam valueInterface);

}
