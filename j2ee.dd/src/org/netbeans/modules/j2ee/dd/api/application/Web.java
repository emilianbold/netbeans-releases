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
