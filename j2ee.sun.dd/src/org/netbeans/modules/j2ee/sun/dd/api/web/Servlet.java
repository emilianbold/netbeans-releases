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
 * Servlet.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;

public interface Servlet extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

        public static final String SERVLET_NAME = "ServletName";	// NOI18N
	public static final String PRINCIPAL_NAME = "PrincipalName";	// NOI18N
	public static final String WEBSERVICE_ENDPOINT = "WebserviceEndpoint";	// NOI18N

        /** Setter for servlet-name property
         * @param value property value
         */
	public void setServletName(String value);
        /** Getter for servlet-name property.
         * @return property value
         */
	public String getServletName();
        /** Setter for principal-name property
         * @param value property value
         */
	public void setPrincipalName(String value);
        /** Getter for principal-name property.
         * @return property value
         */
	public String getPrincipalName();

	public void setWebserviceEndpoint(int index, WebserviceEndpoint value);
	public WebserviceEndpoint getWebserviceEndpoint(int index);
	public int sizeWebserviceEndpoint();
	public void setWebserviceEndpoint(WebserviceEndpoint[] value);
	public WebserviceEndpoint[] getWebserviceEndpoint();
	public int addWebserviceEndpoint(WebserviceEndpoint value);
	public int removeWebserviceEndpoint(WebserviceEndpoint value);
	public WebserviceEndpoint newWebserviceEndpoint();

}
