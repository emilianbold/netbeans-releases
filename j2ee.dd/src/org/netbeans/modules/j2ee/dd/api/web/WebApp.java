/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.api.web;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
/**
 * Generated interface for WebApp element.<br>
 * The WebApp object is the root of bean graph generated<br>
 * for deployment descriptor(web.xml) file.<br>
 * For getting the root (WebApp object) use the {@link DDProvider#getDDRoot} method.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface WebApp extends org.netbeans.modules.j2ee.dd.api.common.RootInterface {
        public static final String PROPERTY_VERSION="dd_version"; //NOI18N
        public static final String VERSION_2_3="2.3"; //NOI18N
        public static final String VERSION_2_4="2.4"; //NOI18N
        public static final int STATE_VALID=0;
        public static final int STATE_INVALID_PARSABLE=1;
        public static final int STATE_INVALID_UNPARSABLE=2;
        public static final String PROPERTY_STATUS="dd_status"; //NOI18N
    
	//public void setVersion(java.lang.String value);
        /** Getter for version property.
         * @return property value
         */        
	public java.lang.String getVersion();
        /** Getter for SAX Parse Error property. 
         * Used when deployment descriptor is in invalid state.
         * @return property value or null if in valid state
         */        
	public org.xml.sax.SAXParseException getError();      
        /** Getter for status property.
         * @return property value
         */        
	public int getStatus();      
        /** Setter for distributable property.
         * @param value property value
         */
        public void setDistributable(boolean value);
        /** Getter for distributable property.
         * @return property value 
         */
	public boolean isDistributable();        

	public void setContextParam(int index, org.netbeans.modules.j2ee.dd.api.web.InitParam valueInterface);

	public org.netbeans.modules.j2ee.dd.api.web.InitParam getContextParam(int index);

	public void setContextParam(org.netbeans.modules.j2ee.dd.api.web.InitParam[] value);

	public org.netbeans.modules.j2ee.dd.api.web.InitParam[] getContextParam();

	public int sizeContextParam();

	public int addContextParam(org.netbeans.modules.j2ee.dd.api.web.InitParam valueInterface);

	public int removeContextParam(org.netbeans.modules.j2ee.dd.api.web.InitParam valueInterface);

	public void setFilter(int index, org.netbeans.modules.j2ee.dd.api.web.Filter valueInterface);

	public org.netbeans.modules.j2ee.dd.api.web.Filter getFilter(int index);

	public void setFilter(org.netbeans.modules.j2ee.dd.api.web.Filter[] value);

	public org.netbeans.modules.j2ee.dd.api.web.Filter[] getFilter();

	public int sizeFilter();

	public int addFilter(org.netbeans.modules.j2ee.dd.api.web.Filter valueInterface);

	public int removeFilter(org.netbeans.modules.j2ee.dd.api.web.Filter valueInterface);

	public void setFilterMapping(int index, org.netbeans.modules.j2ee.dd.api.web.FilterMapping valueInterface);

	public org.netbeans.modules.j2ee.dd.api.web.FilterMapping getFilterMapping(int index);

	public void setFilterMapping(org.netbeans.modules.j2ee.dd.api.web.FilterMapping[] value);

	public org.netbeans.modules.j2ee.dd.api.web.FilterMapping[] getFilterMapping();

	public int sizeFilterMapping();

	public int addFilterMapping(org.netbeans.modules.j2ee.dd.api.web.FilterMapping valueInterface);

	public int removeFilterMapping(org.netbeans.modules.j2ee.dd.api.web.FilterMapping valueInterface);

	public void setListener(int index, org.netbeans.modules.j2ee.dd.api.web.Listener valueInterface);

	public org.netbeans.modules.j2ee.dd.api.web.Listener getListener(int index);

	public void setListener(org.netbeans.modules.j2ee.dd.api.web.Listener[] value);

	public org.netbeans.modules.j2ee.dd.api.web.Listener[] getListener();

	public int sizeListener();

	public int addListener(org.netbeans.modules.j2ee.dd.api.web.Listener valueInterface);

	public int removeListener(org.netbeans.modules.j2ee.dd.api.web.Listener valueInterface);

	public void setServlet(int index, org.netbeans.modules.j2ee.dd.api.web.Servlet valueInterface);

	public org.netbeans.modules.j2ee.dd.api.web.Servlet getServlet(int index);

	public void setServlet(org.netbeans.modules.j2ee.dd.api.web.Servlet[] value);

	public org.netbeans.modules.j2ee.dd.api.web.Servlet[] getServlet();

	public int sizeServlet();

	public int addServlet(org.netbeans.modules.j2ee.dd.api.web.Servlet valueInterface);

	public int removeServlet(org.netbeans.modules.j2ee.dd.api.web.Servlet valueInterface);

	public void setServletMapping(int index, org.netbeans.modules.j2ee.dd.api.web.ServletMapping valueInterface);

	public org.netbeans.modules.j2ee.dd.api.web.ServletMapping getServletMapping(int index);

	public void setServletMapping(org.netbeans.modules.j2ee.dd.api.web.ServletMapping[] value);

	public org.netbeans.modules.j2ee.dd.api.web.ServletMapping[] getServletMapping();

	public int sizeServletMapping();

	public int addServletMapping(org.netbeans.modules.j2ee.dd.api.web.ServletMapping valueInterface);

	public int removeServletMapping(org.netbeans.modules.j2ee.dd.api.web.ServletMapping valueInterface);

        public void setSessionConfig(org.netbeans.modules.j2ee.dd.api.web.SessionConfig value);
	public org.netbeans.modules.j2ee.dd.api.web.SessionConfig getSingleSessionConfig();

	public void setMimeMapping(int index, org.netbeans.modules.j2ee.dd.api.web.MimeMapping valueInterface);

	public org.netbeans.modules.j2ee.dd.api.web.MimeMapping getMimeMapping(int index);

	public void setMimeMapping(org.netbeans.modules.j2ee.dd.api.web.MimeMapping[] value);

	public org.netbeans.modules.j2ee.dd.api.web.MimeMapping[] getMimeMapping();

	public int sizeMimeMapping();

	public int addMimeMapping(org.netbeans.modules.j2ee.dd.api.web.MimeMapping valueInterface);

	public int removeMimeMapping(org.netbeans.modules.j2ee.dd.api.web.MimeMapping valueInterface);

        public void setWelcomeFileList(org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList value);
        public org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList getSingleWelcomeFileList();
     
	public void setErrorPage(int index, org.netbeans.modules.j2ee.dd.api.web.ErrorPage valueInterface);

	public org.netbeans.modules.j2ee.dd.api.web.ErrorPage getErrorPage(int index);

	public void setErrorPage(org.netbeans.modules.j2ee.dd.api.web.ErrorPage[] value);

	public org.netbeans.modules.j2ee.dd.api.web.ErrorPage[] getErrorPage();

	public int sizeErrorPage();

	public int addErrorPage(org.netbeans.modules.j2ee.dd.api.web.ErrorPage valueInterface);

	public int removeErrorPage(org.netbeans.modules.j2ee.dd.api.web.ErrorPage valueInterface);
        
	public void setJspConfig(org.netbeans.modules.j2ee.dd.api.web.JspConfig value) throws VersionNotSupportedException;
	public org.netbeans.modules.j2ee.dd.api.web.JspConfig getSingleJspConfig() throws VersionNotSupportedException;

	public void setSecurityConstraint(int index, org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint valueInterface);

	public org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint getSecurityConstraint(int index);

	public void setSecurityConstraint(org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint[] value);

	public org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint[] getSecurityConstraint();

	public int sizeSecurityConstraint();

	public int addSecurityConstraint(org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint valueInterface);

	public int removeSecurityConstraint(org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint valueInterface);
        
	public void setLoginConfig(org.netbeans.modules.j2ee.dd.api.web.LoginConfig value);
	public org.netbeans.modules.j2ee.dd.api.web.LoginConfig getSingleLoginConfig();

	public void setSecurityRole(int index, org.netbeans.modules.j2ee.dd.api.common.SecurityRole valueInterface);

	public org.netbeans.modules.j2ee.dd.api.common.SecurityRole getSecurityRole(int index);

	public void setSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] value);

	public org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] getSecurityRole();

	public int sizeSecurityRole();

	public int addSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole valueInterface);

	public int removeSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole valueInterface);

	public void setEnvEntry(int index, org.netbeans.modules.j2ee.dd.api.web.EnvEntry valueInterface);

	public org.netbeans.modules.j2ee.dd.api.web.EnvEntry getEnvEntry(int index);

	public void setEnvEntry(org.netbeans.modules.j2ee.dd.api.web.EnvEntry[] value);

	public org.netbeans.modules.j2ee.dd.api.web.EnvEntry[] getEnvEntry();

	public int sizeEnvEntry();

	public int addEnvEntry(org.netbeans.modules.j2ee.dd.api.web.EnvEntry valueInterface);

	public int removeEnvEntry(org.netbeans.modules.j2ee.dd.api.web.EnvEntry valueInterface);

	public void setEjbRef(int index, org.netbeans.modules.j2ee.dd.api.common.EjbRef valueInterface);

	public org.netbeans.modules.j2ee.dd.api.common.EjbRef getEjbRef(int index);

	public void setEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef[] value);

	public org.netbeans.modules.j2ee.dd.api.common.EjbRef[] getEjbRef();

	public int sizeEjbRef();

	public int addEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef valueInterface);

	public int removeEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef valueInterface);

	public void setEjbLocalRef(int index, org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef valueInterface);

	public org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef getEjbLocalRef(int index);

	public void setEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef[] value);

	public org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef[] getEjbLocalRef();

	public int sizeEjbLocalRef();

	public int addEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef valueInterface);

	public int removeEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef valueInterface);

	public void setServiceRef(int index, org.netbeans.modules.j2ee.dd.api.web.ServiceRef valueInterface) throws VersionNotSupportedException;

	public org.netbeans.modules.j2ee.dd.api.web.ServiceRef getServiceRef(int index) throws VersionNotSupportedException;

	public void setServiceRef(org.netbeans.modules.j2ee.dd.api.web.ServiceRef[] value) throws VersionNotSupportedException;

	public org.netbeans.modules.j2ee.dd.api.web.ServiceRef[] getServiceRef() throws VersionNotSupportedException;

	public int sizeServiceRef() throws VersionNotSupportedException;

	public int addServiceRef(org.netbeans.modules.j2ee.dd.api.web.ServiceRef valueInterface) throws VersionNotSupportedException;

	public int removeServiceRef(org.netbeans.modules.j2ee.dd.api.web.ServiceRef valueInterface) throws VersionNotSupportedException;

	public void setResourceRef(int index, org.netbeans.modules.j2ee.dd.api.common.ResourceRef valueInterface);

	public org.netbeans.modules.j2ee.dd.api.common.ResourceRef getResourceRef(int index);

	public void setResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef[] value);

	public org.netbeans.modules.j2ee.dd.api.common.ResourceRef[] getResourceRef();

	public int sizeResourceRef();

	public int addResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef valueInterface);

	public int removeResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef valueInterface);

	public void setResourceEnvRef(int index, org.netbeans.modules.j2ee.dd.api.web.ResourceEnvRef valueInterface);

	public org.netbeans.modules.j2ee.dd.api.web.ResourceEnvRef getResourceEnvRef(int index);

	public void setResourceEnvRef(org.netbeans.modules.j2ee.dd.api.web.ResourceEnvRef[] value);

	public org.netbeans.modules.j2ee.dd.api.web.ResourceEnvRef[] getResourceEnvRef();

	public int sizeResourceEnvRef();

	public int addResourceEnvRef(org.netbeans.modules.j2ee.dd.api.web.ResourceEnvRef valueInterface);

	public int removeResourceEnvRef(org.netbeans.modules.j2ee.dd.api.web.ResourceEnvRef valueInterface);

	public void setMessageDestinationRef(int index, org.netbeans.modules.j2ee.dd.api.web.MessageDestinationRef valueInterface) throws VersionNotSupportedException;

	public org.netbeans.modules.j2ee.dd.api.web.MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException;

	public void setMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.web.MessageDestinationRef[] value) throws VersionNotSupportedException;

	public org.netbeans.modules.j2ee.dd.api.web.MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException;

	public int sizeMessageDestinationRef() throws VersionNotSupportedException;

	public int addMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.web.MessageDestinationRef valueInterface) throws VersionNotSupportedException;

	public int removeMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.web.MessageDestinationRef valueInterface) throws VersionNotSupportedException;

	public void setMessageDestination(int index, org.netbeans.modules.j2ee.dd.api.web.MessageDestination valueInterface) throws VersionNotSupportedException;

	public org.netbeans.modules.j2ee.dd.api.web.MessageDestination getMessageDestination(int index) throws VersionNotSupportedException;

	public void setMessageDestination(org.netbeans.modules.j2ee.dd.api.web.MessageDestination[] value) throws VersionNotSupportedException;

	public org.netbeans.modules.j2ee.dd.api.web.MessageDestination[] getMessageDestination() throws VersionNotSupportedException;

	public int sizeMessageDestination() throws VersionNotSupportedException;

	public int addMessageDestination(org.netbeans.modules.j2ee.dd.api.web.MessageDestination valueInterface) throws VersionNotSupportedException;

	public int removeMessageDestination(org.netbeans.modules.j2ee.dd.api.web.MessageDestination valueInterface) throws VersionNotSupportedException;

	public org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMappingList getSingleLocaleEncodingMappingList() throws VersionNotSupportedException;
        
	public void setLocaleEncodingMappingList(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMappingList value) throws VersionNotSupportedException;

        // due to compatibility with servlet2.3
	public void setTaglib(int index, org.netbeans.modules.j2ee.dd.api.web.Taglib valueInterface) throws VersionNotSupportedException;
	public org.netbeans.modules.j2ee.dd.api.web.Taglib getTaglib(int index) throws VersionNotSupportedException;
	public void setTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib[] value) throws VersionNotSupportedException;
	public org.netbeans.modules.j2ee.dd.api.web.Taglib[] getTaglib() throws VersionNotSupportedException;
	public int sizeTaglib() throws VersionNotSupportedException;
	public int addTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib valueInterface) throws VersionNotSupportedException;
	public int removeTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib valueInterface) throws VersionNotSupportedException;
}
