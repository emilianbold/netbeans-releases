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
