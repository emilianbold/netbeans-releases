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

package org.netbeans.modules.j2ee.dd.api.webservices;

public interface Webservices extends org.netbeans.api.web.dd.common.RootInterface {
        public static final String PROPERTY_VERSION="dd_version"; //NOI18N
        public static final String VERSION_1_1="1.1"; //NOI18N
        public static final int STATE_VALID=0;
        public static final int STATE_INVALID_PARSABLE=1;
        public static final int STATE_INVALID_UNPARSABLE=2;
        public static final String PROPERTY_STATUS="dd_status"; //NOI18N
        
        public static final String VERSION = "Version";	// NOI18N
        public static final String WEBSERVICE_DESCRIPTION = "WebserviceDescription";	// NOI18N
        /** Getter for SAX Parse Error property. 
         * Used when deployment descriptor is in invalid state.
         * @return property value or null if in valid state
         */        
	public org.xml.sax.SAXParseException getError();      
        /** Getter for status property.
         * @return property value
         */        
	public int getStatus();      
        
        //public void setVersion(java.math.BigDecimal value);

	public java.math.BigDecimal getVersion();

	public void setWebserviceDescription(int index, WebserviceDescription value);

	public WebserviceDescription getWebserviceDescription(int index);

	public int sizeWebserviceDescription();

	public void setWebserviceDescription(WebserviceDescription[] value);

	public WebserviceDescription[] getWebserviceDescription();

	public int addWebserviceDescription(org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription value);

	public int removeWebserviceDescription(org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription value);

	public WebserviceDescription newWebserviceDescription();

}
