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
 * SunApplication.java
 *
 * Created on November 21, 2004, 12:47 AM
 */


package org.netbeans.modules.j2ee.sun.dd.api.app;

public interface Web extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
        public static final String WEB_URI = "WebUri";	// NOI18N
	public static final String CONTEXT_ROOT = "ContextRoot";	// NOI18N
        
        /** Setter for web-uri property
        * @param value property value
        */
	public void setWebUri(String value);
        /** Getter for web-uri property.
        * @return property value
        */
	public String getWebUri();
        /** Setter for context-root property
        * @param value property value
        */
	public void setContextRoot(String value);
        /** Getter for context-root property.
        * @return property value
        */
	public String getContextRoot();

}
