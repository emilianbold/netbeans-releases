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

import org.netbeans.api.web.dd.common.VersionNotSupportedException;

public interface Application extends org.netbeans.api.web.dd.common.RootInterface {
	
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

	public void setSecurityRole(int index, org.netbeans.modules.j2ee.dd.api.ejb.SecurityRole value);

	public org.netbeans.modules.j2ee.dd.api.ejb.SecurityRole getSecurityRole(int index);

	public int sizeSecurityRole();

	public void setSecurityRole(org.netbeans.modules.j2ee.dd.api.ejb.SecurityRole[] value);

	public org.netbeans.modules.j2ee.dd.api.ejb.SecurityRole[] getSecurityRole();

	public int addSecurityRole(org.netbeans.modules.j2ee.dd.api.ejb.SecurityRole value);

	public int removeSecurityRole(org.netbeans.modules.j2ee.dd.api.ejb.SecurityRole value);

	public org.netbeans.modules.j2ee.dd.api.ejb.SecurityRole newSecurityRole();

        
        //1.4
        public void setIcon(int index, org.netbeans.api.web.dd.Icon value) throws VersionNotSupportedException;

	public org.netbeans.api.web.dd.Icon getIcon(int index) throws VersionNotSupportedException;

	public int sizeIcon() throws VersionNotSupportedException;

	public void setIcon(org.netbeans.api.web.dd.Icon[] value) throws VersionNotSupportedException;

	//public org.netbeans.api.web.dd.Icon[] getIcon() throws VersionNotSupportedException;

	public int addIcon(org.netbeans.api.web.dd.Icon value)  throws VersionNotSupportedException;

	public int removeIcon(org.netbeans.api.web.dd.Icon value) throws VersionNotSupportedException;

	public org.netbeans.api.web.dd.Icon newIcon(); 

}

