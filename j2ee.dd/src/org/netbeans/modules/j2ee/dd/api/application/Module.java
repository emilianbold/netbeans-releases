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

public interface Module extends CommonDDBean {
	public static final String CONNECTOR = "Connector";	// NOI18N
	public static final String EJB = "Ejb";	// NOI18N
	public static final String JAVA = "Java";	// NOI18N
	public static final String WEB = "Web";	// NOI18N
	public static final String ALT_DD = "AltDd";	// NOI18N
        
	public void setConnector(String value);

	public String getConnector();

	public void setConnectorId(java.lang.String value) throws VersionNotSupportedException;

	public java.lang.String getConnectorId() throws VersionNotSupportedException;

	public void setEjb(String value);

	public String getEjb();

	public void setEjbId(java.lang.String value) throws VersionNotSupportedException;

	public java.lang.String getEjbId() throws VersionNotSupportedException;

	public void setJava(String value);

	public String getJava();

	public void setJavaId(java.lang.String value) throws VersionNotSupportedException;

	public java.lang.String getJavaId() throws VersionNotSupportedException;

	public void setWeb(Web value);

	public Web getWeb();

	public Web newWeb();

	public void setAltDd(String value);

	public String getAltDd();

	public void setAltDdId(java.lang.String value) throws VersionNotSupportedException;

	public java.lang.String getAltDdId() throws VersionNotSupportedException;

}
