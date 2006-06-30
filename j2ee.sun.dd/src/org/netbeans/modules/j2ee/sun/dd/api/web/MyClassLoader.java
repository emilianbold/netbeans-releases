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
 * MyClassLoader.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

public interface MyClassLoader extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

        public static final String EXTRACLASSPATH = "ExtraClassPath";	// NOI18N
	public static final String DELEGATE = "Delegate";	// NOI18N
	public static final String DYNAMICRELOADINTERVAL = "DynamicReloadInterval";	// NOI18N
	public static final String PROPERTY = "WebProperty";	// NOI18N

         /** Setter for extra-class-path attribute
         * @param value attribute value
         */
	public void setExtraClassPath(java.lang.String value);
        /** Getter for extra-class-path attribute.
         * @return attribute value
         */
	public java.lang.String getExtraClassPath();
         /** Setter for delegate attribute
         * @param value attribute value
         */
	public void setDelegate(java.lang.String value);
        /** Getter for delegate attribute.
         * @return attribute value
         */
	public java.lang.String getDelegate();
         /** Setter for dynamic-reload-interval attribute
         * @param value attribute value
         */
	public void setDynamicReloadInterval(java.lang.String value) throws VersionNotSupportedException;
        /** Getter for dynamic-reload-interval attribute.
         * @return attribute value
         */
	public java.lang.String getDynamicReloadInterval() throws VersionNotSupportedException;

	public void setWebProperty(int index, WebProperty value) throws VersionNotSupportedException;
	public WebProperty getWebProperty(int index) throws VersionNotSupportedException;
	public int sizeWebProperty() throws VersionNotSupportedException;
	public void setWebProperty(WebProperty[] value) throws VersionNotSupportedException;
	public WebProperty[] getWebProperty() throws VersionNotSupportedException;
	public int addWebProperty(WebProperty value) throws VersionNotSupportedException;
	public int removeWebProperty(WebProperty value) throws VersionNotSupportedException;
	public WebProperty newWebProperty() throws VersionNotSupportedException;

}
