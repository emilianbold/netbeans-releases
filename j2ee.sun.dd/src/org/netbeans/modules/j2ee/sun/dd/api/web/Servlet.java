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
