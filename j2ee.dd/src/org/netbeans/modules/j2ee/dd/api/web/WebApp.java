/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.dd.api.web;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;

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
        public static final String VERSION_2_5="2.5"; //NOI18N
        public static final String VERSION_3_0="3.0"; //NOI18N
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

	public void setContextParam(int index, InitParam valueInterface);

	public InitParam getContextParam(int index);

	public void setContextParam(InitParam[] value);

	public InitParam[] getContextParam();

	public int sizeContextParam();

	public int addContextParam(InitParam valueInterface);

	public int removeContextParam(InitParam valueInterface);

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
        
        public int addJspConfig(org.netbeans.modules.j2ee.dd.api.web.JspConfig valueInterface) throws VersionNotSupportedException;
	
        public int removeJspConfig(org.netbeans.modules.j2ee.dd.api.web.JspConfig valueInterface) throws VersionNotSupportedException;

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

	public void setEnvEntry(int index, EnvEntry valueInterface);

	public EnvEntry getEnvEntry(int index);

	public void setEnvEntry(EnvEntry[] value);

	public EnvEntry[] getEnvEntry();

	public int sizeEnvEntry();

	public int addEnvEntry(EnvEntry valueInterface);

	public int removeEnvEntry(EnvEntry valueInterface);

	public void setEjbRef(int index, EjbRef valueInterface);

	public EjbRef getEjbRef(int index);

	public void setEjbRef(EjbRef[] value);

	public EjbRef[] getEjbRef();

	public int sizeEjbRef();

	public int addEjbRef(EjbRef valueInterface);

	public int removeEjbRef(EjbRef valueInterface);

	public void setEjbLocalRef(int index, EjbLocalRef valueInterface);

	public EjbLocalRef getEjbLocalRef(int index);

	public void setEjbLocalRef(EjbLocalRef[] value);

	public EjbLocalRef[] getEjbLocalRef();

	public int sizeEjbLocalRef();

	public int addEjbLocalRef(EjbLocalRef valueInterface);

	public int removeEjbLocalRef(EjbLocalRef valueInterface);

	public void setServiceRef(int index, ServiceRef valueInterface) throws VersionNotSupportedException;

	public ServiceRef getServiceRef(int index) throws VersionNotSupportedException;

	public void setServiceRef(ServiceRef[] value) throws VersionNotSupportedException;

	public ServiceRef[] getServiceRef() throws VersionNotSupportedException;

	public int sizeServiceRef() throws VersionNotSupportedException;

	public int addServiceRef(ServiceRef valueInterface) throws VersionNotSupportedException;

	public int removeServiceRef(ServiceRef valueInterface) throws VersionNotSupportedException;

	public void setResourceRef(int index, ResourceRef valueInterface);

	public ResourceRef getResourceRef(int index);

	public void setResourceRef(ResourceRef[] value);

	public ResourceRef[] getResourceRef();

	public int sizeResourceRef();

	public int addResourceRef(ResourceRef valueInterface);

	public int removeResourceRef(ResourceRef valueInterface);

	public void setResourceEnvRef(int index, ResourceEnvRef valueInterface);

	public ResourceEnvRef getResourceEnvRef(int index);

	public void setResourceEnvRef(ResourceEnvRef[] value);

	public ResourceEnvRef[] getResourceEnvRef();

	public int sizeResourceEnvRef();

	public int addResourceEnvRef(ResourceEnvRef valueInterface);

	public int removeResourceEnvRef(ResourceEnvRef valueInterface);

	public void setMessageDestinationRef(int index, MessageDestinationRef valueInterface) throws VersionNotSupportedException;

	public MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException;

	public void setMessageDestinationRef(MessageDestinationRef[] value) throws VersionNotSupportedException;

	public MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException;

	public int sizeMessageDestinationRef() throws VersionNotSupportedException;

	public int addMessageDestinationRef(MessageDestinationRef valueInterface) throws VersionNotSupportedException;

	public int removeMessageDestinationRef(MessageDestinationRef valueInterface) throws VersionNotSupportedException;

	public void setMessageDestination(int index, MessageDestination valueInterface) throws VersionNotSupportedException;

	public MessageDestination getMessageDestination(int index) throws VersionNotSupportedException;

	public void setMessageDestination(MessageDestination[] value) throws VersionNotSupportedException;

	public MessageDestination[] getMessageDestination() throws VersionNotSupportedException;

	public int sizeMessageDestination() throws VersionNotSupportedException;

	public int addMessageDestination(MessageDestination valueInterface) throws VersionNotSupportedException;

	public int removeMessageDestination(MessageDestination valueInterface) throws VersionNotSupportedException;

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
        // This attribute is optional
	public void setMetadataComplete(boolean value) throws VersionNotSupportedException;
	public boolean isMetadataComplete() throws VersionNotSupportedException;
}
