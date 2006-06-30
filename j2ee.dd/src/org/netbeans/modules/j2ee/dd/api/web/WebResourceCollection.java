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
 * Generated interface for WebResourceCollection element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface WebResourceCollection extends CommonDDBean, DescriptionInterface {
        /** Setter for web-resource-name property.
         * @param value property value
         */
	public void setWebResourceName(java.lang.String value);
        /** Getter for web-resource-name property.
         * @return property value 
         */
	public java.lang.String getWebResourceName();
        /** Setter for url-pattern property.
         * @param index position in the array of url-patterns
         * @param value property value 
         */
	public void setUrlPattern(int index, java.lang.String value);
        /** Getter for url-pattern property.
         * @param index position in the array of url-patterns
         * @return property value 
         */
	public java.lang.String getUrlPattern(int index);
        /** Setter for url-pattern property.
         * @param index position in the array of url-patterns
         * @param value array of url-pattern properties
         */
	public void setUrlPattern(java.lang.String[] value);
        /** Getter for url-pattern property.
         * @return array of url-pattern properties
         */
	public java.lang.String[] getUrlPattern();
        /** Returns size of url-pattern properties.
         * @return number of url-pattern properties 
         */
	public int sizeUrlPattern();
        /** Adds url-pattern property.
         * @param value url-pattern property
         * @return index of new url-pattern
         */
	public int addUrlPattern(java.lang.String value);
        /** Removes url-pattern property.
         * @param value url-pattern property
         * @return index of the removed url-pattern
         */
	public int removeUrlPattern(java.lang.String value);
        /** Setter for http-method property.
         * @param index position in the array of http-methods
         * @param value property value 
         */
	public void setHttpMethod(int index, java.lang.String value);
        /** Getter for http-method property.
         * @param index position in the array of http-methods
         * @return property value 
         */
	public java.lang.String getHttpMethod(int index);
        /** Setter for http-method property.
         * @param index position in the array of http-methods
         * @param value array of http-method properties
         */
	public void setHttpMethod(java.lang.String[] value);
        /** Getter for http-method property.
         * @return array of http-method properties
         */
	public java.lang.String[] getHttpMethod();
        /** Returns size of http-method properties.
         * @return number of http-method properties 
         */
	public int sizeHttpMethod();
        /** Adds http-method property.
         * @param value http-method property
         * @return index of new http-method
         */
	public int addHttpMethod(java.lang.String value);
        /** Removes http-method property.
         * @param value http-method property
         * @return index of the removed http-method
         */
	public int removeHttpMethod(java.lang.String value);

}
