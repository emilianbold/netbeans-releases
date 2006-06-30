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
	public void setInitParam(int index, org.netbeans.modules.j2ee.dd.api.common.InitParam valueInterface);
        /** Getter for init-param element.
         * @param index position in the array of elements
         * @return init-param element (InitParam object)
         */
	public org.netbeans.modules.j2ee.dd.api.common.InitParam getInitParam(int index);
        /** Setter for init-param elements.
         * @param value array of init-param elements (InitParam objects)
         */
	public void setInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam[] value);
        /** Getter for init-param elements.
         * @return array of init-param elements (InitParam objects)
         */
	public org.netbeans.modules.j2ee.dd.api.common.InitParam[] getInitParam();
        /** Returns size of init-param elements.
         * @return number of init-param elements 
         */
	public int sizeInitParam();
        /** Adds init-param element.
         * @param valueInterface init-param element (InitParam object)
         * @return index of new init-param
         */
	public int addInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam valueInterface);
        /** Removes init-param element.
         * @param valueInterface init-param element (InitParam object)
         * @return index of the removed init-param
         */
	public int removeInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam valueInterface);

}
