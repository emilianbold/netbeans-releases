/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * This interface has all of the bean info accessor methods.
 * 
 * @Generated
 */

package org.netbeans.modules.j2ee.dd.api.application;

import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public interface Web extends CommonDDBean {
	public static final String WEB_URI = "WebUri";	// NOI18N
	public static final String CONTEXT_ROOT = "ContextRoot";	// NOI18N
	public static final String CONTEXTROOTID = "ContextRootId";	// NOI18N
        
	public void setWebUri(String value);

	public String getWebUri();

	public void setWebUriId(java.lang.String value) throws VersionNotSupportedException; 

	public java.lang.String getWebUriId() throws VersionNotSupportedException;

	public void setContextRoot(String value);

	public String getContextRoot();

	public void setContextRootId(java.lang.String value);

	public java.lang.String getContextRootId();

}
