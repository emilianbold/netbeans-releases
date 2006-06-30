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
 * CacheHelper.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

public interface CacheHelper extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

        public static final String NAME = "Name";	// NOI18N
	public static final String CLASSNAME = "ClassName";	// NOI18N
	public static final String PROPERTY = "WebProperty";	// NOI18N

        /** Setter for name attribute.
         * @param value attribute value
         */
	public void setName(java.lang.String value);
         /** Getter for name attribute.
         * @return attribute value
         */
	public java.lang.String getName();
        /** Setter for class-name attribute.
         * @param value attribute value
         */
	public void setClassName(java.lang.String value);
         /** Getter for class-name attribute.
         * @return attribute value
         */
	public java.lang.String getClassName();

	public void setWebProperty(int index, WebProperty value);
	public WebProperty getWebProperty(int index);
	public int sizeWebProperty();
	public void setWebProperty(WebProperty[] value);
	public WebProperty[] getWebProperty();
	public int addWebProperty(WebProperty value);
	public int removeWebProperty(WebProperty value);
	public WebProperty newWebProperty();

}
