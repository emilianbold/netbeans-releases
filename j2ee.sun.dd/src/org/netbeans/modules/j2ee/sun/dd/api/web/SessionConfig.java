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
 * SessionConfig.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

public interface SessionConfig extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
        
        public static final String SESSION_MANAGER = "SessionManager";	// NOI18N
	public static final String SESSION_PROPERTIES = "SessionProperties";	// NOI18N
	public static final String COOKIE_PROPERTIES = "CookieProperties";	// NOI18N
        
        /** Setter for session-manager property
         * @param value property value
         */
	public void setSessionManager(SessionManager value); 
        /** Getter for session-manager property.
         * @return property value
         */
	public SessionManager getSessionManager();

	public SessionManager newSessionManager();
        /** Setter for session-properties property
         * @param value property value
         */
	public void setSessionProperties(SessionProperties value); 
        /** Getter for session-properties property.
         * @return property value
         */
	public SessionProperties getSessionProperties();

	public SessionProperties newSessionProperties();
        /** Setter for cookie-properties property
         * @param value property value
         */
	public void setCookieProperties(CookieProperties value);
        /** Getter for cookie-properties property.
         * @return property value
         */
	public CookieProperties getCookieProperties();

	public CookieProperties newCookieProperties(); 

}
