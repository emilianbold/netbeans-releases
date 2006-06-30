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

import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public interface Application extends org.netbeans.modules.j2ee.dd.api.common.RootInterface {
	
        public static final String MODULE = "Module";	// NOI18N
	public static final String SECURITY_ROLE = "SecurityRole";	// NOI18N

        public static final String PROPERTY_VERSION="dd_version"; //NOI18N
        public static final String VERSION_1_3="1.3"; //NOI18N
        public static final String VERSION_1_4="1.4"; //NOI18N
        public static final int STATE_VALID=0;
        public static final int STATE_INVALID_PARSABLE=1;
        public static final int STATE_INVALID_UNPARSABLE=2;
        public static final String PROPERTY_STATUS="dd_status"; //NOI18N
    
        //public void setVersion(java.lang.String value);
        /** Getter for version property.
         * @return property value
         */
        public java.math.BigDecimal getVersion();
        /** Getter for SAX Parse Error property.
         * Used when deployment descriptor is in invalid state.
         * @return property value or null if in valid state
         */
        public org.xml.sax.SAXParseException getError();
        /** Getter for status property.
         * @return property value
         */
        public int getStatus();
    
	public void setModule(int index, Module value);

	public Module getModule(int index);

	public int sizeModule();

	public void setModule(Module[] value);

	public Module[] getModule();

	public int addModule(org.netbeans.modules.j2ee.dd.api.application.Module value);

	public int removeModule(org.netbeans.modules.j2ee.dd.api.application.Module value);

	public Module newModule();

	public void setSecurityRole(int index, org.netbeans.modules.j2ee.dd.api.common.SecurityRole value);

	public org.netbeans.modules.j2ee.dd.api.common.SecurityRole getSecurityRole(int index);

	public int sizeSecurityRole();

	public void setSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] value);

	public org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] getSecurityRole();

	public int addSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole value);

	public int removeSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole value);

	public org.netbeans.modules.j2ee.dd.api.common.SecurityRole newSecurityRole();

        
        //1.4
        public void setIcon(int index, org.netbeans.modules.j2ee.dd.api.common.Icon value) throws VersionNotSupportedException;

	public org.netbeans.modules.j2ee.dd.api.common.Icon getIcon(int index) throws VersionNotSupportedException;

	public int sizeIcon() throws VersionNotSupportedException;

	public void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon[] value) throws VersionNotSupportedException;

	//public org.netbeans.modules.j2ee.dd.api.common.Icon[] getIcon() throws VersionNotSupportedException;

	public int addIcon(org.netbeans.modules.j2ee.dd.api.common.Icon value)  throws VersionNotSupportedException;

	public int removeIcon(org.netbeans.modules.j2ee.dd.api.common.Icon value) throws VersionNotSupportedException;

	public org.netbeans.modules.j2ee.dd.api.common.Icon newIcon(); 

}

