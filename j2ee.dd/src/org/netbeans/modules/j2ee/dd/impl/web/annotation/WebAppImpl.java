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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.impl.web.annotation;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.ErrorPage;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.JspConfig;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMappingList;
import org.netbeans.modules.j2ee.dd.api.web.LoginConfig;
import org.netbeans.modules.j2ee.dd.api.web.MimeMapping;
import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.SessionConfig;
import org.netbeans.modules.j2ee.dd.api.web.Taglib;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonAnnotationHelper;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.openide.filesystems.FileObject;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Andrei Badea
 */
public class WebAppImpl implements WebApp {

    private final AnnotationModelHelper helper;
    private WebApp ddRoot;
    private ResourceRef[] resourceRefs;
    private ResourceEnvRef[] resourceEnvRefs = null;
    private EnvEntry[] envEntries = null;
    private MessageDestinationRef[] messageDestinationRefs = null;

    public WebAppImpl(AnnotationModelHelper helper) {
        this.helper = helper;
    }

    void ensureRoot(MetadataUnit metadataUnit) throws IOException {
        if (ddRoot != null) {
            return;
        }
        changeRoot(metadataUnit);
    }

    void changeRoot(MetadataUnit metadataUnit) throws IOException {
        ddRoot = getMetadataUnitDDRoot(metadataUnit);
    }

    private WebApp getMetadataUnitDDRoot(MetadataUnit metadataUnit) throws IOException {
        FileObject dd = metadataUnit.getDeploymentDescriptor();
        if (dd != null) {
            return DDProvider.getDefault().getDDRoot(dd);
        }
        return null;
    }
    
    private void initResourceRefs() {
        if (resourceRefs != null) {
            return;
        }
        resourceRefs = CommonAnnotationHelper.getResourceRefs(helper);
    }
    
    private void initResourceEnvRefs() {
        if (resourceEnvRefs != null) {
            return;
        }
        resourceEnvRefs = CommonAnnotationHelper.getResourceEnvRefs(helper);
    }
    
    private void initEnvEntries() {
        if (envEntries != null) {
            return;
        }
        envEntries = CommonAnnotationHelper.getEnvEntries(helper);
    }
    
    
    private void initMessageDestinationRefs() {
        if (messageDestinationRefs != null) {
            return;
        }
        messageDestinationRefs = CommonAnnotationHelper.getMessageDestinationRefs(helper);
    }
    public WebApp clone() {
        // no other model supports clone
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public String getVersion() {
        if (ddRoot == null) {
            // XXX return version from metadata unit
            throw new UnsupportedOperationException();
        }
        return ddRoot.getVersion();
    }

    public SAXParseException getError() {
        if (ddRoot != null) {
            return ddRoot.getError();
        }
        return null;
    }

    public int getStatus() {
        if (ddRoot != null) {
            return ddRoot.getStatus();
        }
        return 0;
    }

    public void setDistributable(boolean value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public boolean isDistributable() {
        if (ddRoot != null) {
            return ddRoot.isDistributable();
        }
        return false;
    }

    public void setContextParam(int index, InitParam valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public InitParam getContextParam(int index) {
        if (ddRoot != null) {
            return ddRoot.getContextParam(index);
        }
        return null;
    }

    public void setContextParam(InitParam[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public InitParam[] getContextParam() {
        if (ddRoot != null) {
            return ddRoot.getContextParam();
        }
        return new InitParam[0];
    }

    public int sizeContextParam() {
        if (ddRoot != null) {
            return ddRoot.sizeContextParam();
        }
        return 0;
    }

    public int addContextParam(InitParam valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeContextParam(InitParam valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setFilter(int index, Filter valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public Filter getFilter(int index) {
        if (ddRoot != null) {
            return ddRoot.getFilter(index);
        }
        return null;
    }

    public void setFilter(Filter[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public Filter[] getFilter() {
        if (ddRoot != null) {
            return ddRoot.getFilter();
        }
        return new Filter[0];
    }

    public int sizeFilter() {
        if (ddRoot != null) {
            return ddRoot.sizeFilter();
        }
        return 0;
    }

    public int addFilter(Filter valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeFilter(Filter valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setFilterMapping(int index, FilterMapping valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public FilterMapping getFilterMapping(int index) {
        if (ddRoot != null) {
            return ddRoot.getFilterMapping(index);
        }
        return null;
    }

    public void setFilterMapping(FilterMapping[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public FilterMapping[] getFilterMapping() {
        if (ddRoot != null) {
            return ddRoot.getFilterMapping();
        }
        return new FilterMapping[0];
    }

    public int sizeFilterMapping() {
        if (ddRoot != null) {
            return ddRoot.sizeFilterMapping();
        }
        return 0;
    }

    public int addFilterMapping(FilterMapping valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeFilterMapping(FilterMapping valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setListener(int index, Listener valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public Listener getListener(int index) {
        if (ddRoot != null) {
            return ddRoot.getListener(index);
        }
        return null;
    }

    public void setListener(Listener[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public Listener[] getListener() {
        if (ddRoot != null) {
            return ddRoot.getListener();
        }
        return new Listener[0];
    }

    public int sizeListener() {
        if (ddRoot != null) {
            return ddRoot.sizeListener();
        }
        return 0;
    }

    public int addListener(Listener valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeListener(Listener valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setServlet(int index, Servlet valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public Servlet getServlet(int index) {
        if (ddRoot != null) {
            return ddRoot.getServlet(index);
        }
        return null;
    }

    public void setServlet(Servlet[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public Servlet[] getServlet() {
        if (ddRoot != null) {
            return ddRoot.getServlet();
        }
        return new Servlet[0];
    }

    public int sizeServlet() {
        if (ddRoot != null) {
            return ddRoot.sizeServlet();
        }
        return 0;
    }

    public int addServlet(Servlet valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeServlet(Servlet valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setServletMapping(int index, ServletMapping valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public ServletMapping getServletMapping(int index) {
        if (ddRoot != null) {
            return ddRoot.getServletMapping(index);
        }
        return null;
    }

    public void setServletMapping(ServletMapping[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public ServletMapping[] getServletMapping() {
        if (ddRoot != null) {
            return ddRoot.getServletMapping();
        }
        return new ServletMapping[0];
    }

    public int sizeServletMapping() {
        if (ddRoot != null) {
            return ddRoot.sizeServletMapping();
        }
        return 0;
    }

    public int addServletMapping(ServletMapping valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeServletMapping(ServletMapping valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setSessionConfig(SessionConfig value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public SessionConfig getSingleSessionConfig() {
        if (ddRoot != null) {
            return ddRoot.getSingleSessionConfig();
        }
        return null;
    }

    public void setMimeMapping(int index, MimeMapping valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public MimeMapping getMimeMapping(int index) {
        if (ddRoot != null) {
            return ddRoot.getMimeMapping(index);
        }
        return null;
    }

    public void setMimeMapping(MimeMapping[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public MimeMapping[] getMimeMapping() {
        if (ddRoot != null) {
            return ddRoot.getMimeMapping();
        }
        return new MimeMapping[0];
    }

    public int sizeMimeMapping() {
        if (ddRoot != null) {
            return ddRoot.sizeMimeMapping();
        }
        return 0;
    }

    public int addMimeMapping(MimeMapping valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeMimeMapping(MimeMapping valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setWelcomeFileList(WelcomeFileList value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public WelcomeFileList getSingleWelcomeFileList() {
        if (ddRoot != null) {
            return ddRoot.getSingleWelcomeFileList();
        }
        return null;
    }

    public void setErrorPage(int index, ErrorPage valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public ErrorPage getErrorPage(int index) {
        if (ddRoot != null) {
            return ddRoot.getErrorPage(index);
        }
        return null;
    }

    public void setErrorPage(ErrorPage[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public ErrorPage[] getErrorPage() {
        if (ddRoot != null) {
            return ddRoot.getErrorPage();
        }
        return new ErrorPage[0];
    }

    public int sizeErrorPage() {
        if (ddRoot != null) {
            return ddRoot.sizeErrorPage();
        }
        return 0;
    }

    public int addErrorPage(ErrorPage valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeErrorPage(ErrorPage valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setJspConfig(JspConfig value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public JspConfig getSingleJspConfig() throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.getSingleJspConfig();
        }
        return null;
    }

    public int addJspConfig(JspConfig valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeJspConfig(JspConfig valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setSecurityConstraint(int index, SecurityConstraint valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public SecurityConstraint getSecurityConstraint(int index) {
        if (ddRoot != null) {
            return ddRoot.getSecurityConstraint(index);
        }
        return null;
    }

    public void setSecurityConstraint(SecurityConstraint[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public SecurityConstraint[] getSecurityConstraint() {
        if (ddRoot != null) {
            return ddRoot.getSecurityConstraint();
        }
        return new SecurityConstraint[0];
    }

    public int sizeSecurityConstraint() {
        if (ddRoot != null) {
            return ddRoot.sizeSecurityConstraint();
        }
        return 0;
    }

    public int addSecurityConstraint(SecurityConstraint valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeSecurityConstraint(SecurityConstraint valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setLoginConfig(LoginConfig value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public LoginConfig getSingleLoginConfig() {
        if (ddRoot != null) {
            return ddRoot.getSingleLoginConfig();
        }
        return null;
    }

    public void setSecurityRole(int index, SecurityRole valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public SecurityRole getSecurityRole(int index) {
        if (ddRoot != null) {
            return ddRoot.getSecurityRole(index);
        }
        return null;
    }

    public void setSecurityRole(SecurityRole[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public SecurityRole[] getSecurityRole() {
        // XXX merge
        return CommonAnnotationHelper.getSecurityRoles(helper);
    }

    public int sizeSecurityRole() {
        if (ddRoot != null) {
            return ddRoot.sizeSecurityRole();
        }
        return 0;
    }

    public int addSecurityRole(SecurityRole valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeSecurityRole(SecurityRole valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setEnvEntry(int index, EnvEntry valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setEnvEntry(EnvEntry[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int addEnvEntry(EnvEntry valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeEnvEntry(EnvEntry valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setEjbRef(int index, EjbRef valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public EjbRef getEjbRef(int index) {
        if (ddRoot != null) {
            return ddRoot.getEjbRef(index);
        }
        return null;
    }

    public void setEjbRef(EjbRef[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public EjbRef[] getEjbRef() {
        if (ddRoot != null) {
            return ddRoot.getEjbRef();
        }
        return new EjbRef[0];
    }

    public int sizeEjbRef() {
        if (ddRoot != null) {
            return ddRoot.sizeEjbRef();
        }
        return 0;
    }

    public int addEjbRef(EjbRef valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeEjbRef(EjbRef valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setEjbLocalRef(int index, EjbLocalRef valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public EjbLocalRef getEjbLocalRef(int index) {
        if (ddRoot != null) {
            return ddRoot.getEjbLocalRef(index);
        }
        return null;
    }

    public void setEjbLocalRef(EjbLocalRef[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public EjbLocalRef[] getEjbLocalRef() {
        if (ddRoot != null) {
            return ddRoot.getEjbLocalRef();
        }
        return new EjbLocalRef[0];
    }

    public int sizeEjbLocalRef() {
        if (ddRoot != null) {
            return ddRoot.sizeEjbLocalRef();
        }
        return 0;
    }

    public int addEjbLocalRef(EjbLocalRef valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeEjbLocalRef(EjbLocalRef valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setServiceRef(int index, ServiceRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public ServiceRef getServiceRef(int index) throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.getServiceRef(index);
        }
        return null;
    }

    public void setServiceRef(ServiceRef[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public ServiceRef[] getServiceRef() throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.getServiceRef();
        }
        return new ServiceRef[0];
    }

    public int sizeServiceRef() throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.sizeServiceRef();
        }
        return 0;
    }

    public int addServiceRef(ServiceRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeServiceRef(ServiceRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setResourceRef(int index, ResourceRef valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setResourceRef(ResourceRef[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public ResourceRef[] getResourceRef() {
        if (ddRoot != null) {
            return ddRoot.getResourceRef();
        }
        initResourceRefs();
        return resourceRefs;
    }

    public ResourceRef getResourceRef(int index) {
        if (ddRoot != null) {
            return ddRoot.getResourceRef(index);
        }
        initResourceRefs();
        return resourceRefs[index];
    }

    public int sizeResourceRef() {
        if (ddRoot != null) {
            return ddRoot.sizeResourceRef();
        }
        initResourceRefs();
        return resourceRefs.length;
    }

    
    public ResourceEnvRef[] getResourceEnvRef() {
        if (ddRoot != null) {
            return ddRoot.getResourceEnvRef();
        }
        initResourceEnvRefs();
        return resourceEnvRefs;
    }

    public ResourceEnvRef getResourceEnvRef(int index) {
        if (ddRoot != null) {
            return ddRoot.getResourceEnvRef(index);
        }
        initResourceEnvRefs();
        return resourceEnvRefs[index];
    }

    public int sizeResourceEnvRef() {
        if (ddRoot != null) {
            return ddRoot.sizeResourceEnvRef();
        }
        initResourceEnvRefs();
        return resourceEnvRefs.length;
    }

    public EnvEntry[] getEnvEntry() {
        if (ddRoot != null) {
            return ddRoot.getEnvEntry();
        }
        initEnvEntries();
        return envEntries;
    }

    public EnvEntry getEnvEntry(int index) {
        if (ddRoot != null) {
            return ddRoot.getEnvEntry(index);
        }
        initEnvEntries();
        return envEntries[index];
    }

    public int sizeEnvEntry() {
        if (ddRoot != null) {
            return ddRoot.sizeEnvEntry();
        }
        initEnvEntries();
        return envEntries.length;
    }
    
    public MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.getMessageDestinationRef();
        }
        initMessageDestinationRefs();
        return messageDestinationRefs;
    }

    public MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.getMessageDestinationRef(index);
        }
        initMessageDestinationRefs();
        return messageDestinationRefs[index];
    }

    public int sizeMessageDestinationRef() throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.sizeMessageDestinationRef();
        }
        initMessageDestinationRefs();
        return messageDestinationRefs.length;
    }
    
    public int addResourceRef(ResourceRef valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeResourceRef(ResourceRef valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setResourceEnvRef(int index, ResourceEnvRef valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setResourceEnvRef(ResourceEnvRef[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int addResourceEnvRef(ResourceEnvRef valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeResourceEnvRef(ResourceEnvRef valueInterface) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setMessageDestinationRef(int index, MessageDestinationRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setMessageDestinationRef(MessageDestinationRef[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int addMessageDestinationRef(MessageDestinationRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeMessageDestinationRef(MessageDestinationRef valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setMessageDestination(int index, MessageDestination valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public MessageDestination getMessageDestination(int index) throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.getMessageDestination(index);
        }
        return null;
    }

    public void setMessageDestination(MessageDestination[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public MessageDestination[] getMessageDestination() throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.getMessageDestination();
        }
        return new MessageDestination[0];
    }

    public int sizeMessageDestination() throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.sizeMessageDestination();
        }
        return 0;
    }

    public int addMessageDestination(MessageDestination valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeMessageDestination(MessageDestination valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public LocaleEncodingMappingList getSingleLocaleEncodingMappingList() throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.getSingleLocaleEncodingMappingList();
        }
        return null;
    }

    public void setLocaleEncodingMappingList(LocaleEncodingMappingList value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setTaglib(int index, Taglib valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public Taglib getTaglib(int index) throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.getTaglib(index);
        }
        return null;
    }

    public void setTaglib(Taglib[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public Taglib[] getTaglib() throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.getTaglib();
        }
        return new Taglib[0];
    }

    public int sizeTaglib() throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.sizeTaglib();
        }
        return 0;
    }

    public int addTaglib(Taglib valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public int removeTaglib(Taglib valueInterface) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setMetadataComplete(boolean value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public boolean isMetadataComplete() throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.isMetadataComplete();
        }
        return false;
    }

    public void write(FileObject fo) throws IOException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void merge(RootInterface root, int mode) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setId(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public String getId() {
        if (ddRoot != null) {
            return ddRoot.getId();
        }
        return null;
    }

    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setDescription(String description) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setAllDescriptions(Map descriptions) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public String getDescription(String locale) throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.getDescription(locale);
        }
        return null;
    }

    public String getDefaultDescription() {
        if (ddRoot != null) {
            return ddRoot.getDefaultDescription();
        }
        return null;
    }

    public Map getAllDescriptions() {
        if (ddRoot != null) {
            return ddRoot.getAllDescriptions();
        }
        return null;
    }

    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removeDescription() {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removeAllDescriptions() {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setDisplayName(String locale, String displayName) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setDisplayName(String displayName) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setAllDisplayNames(Map displayNames) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public String getDisplayName(String locale) throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.getDisplayName(locale);
        }
        return null;
    }

    public String getDefaultDisplayName() {
        if (ddRoot != null) {
            return ddRoot.getDefaultDisplayName();
        }
        return null;
    }

    public Map getAllDisplayNames() {
        if (ddRoot != null) {
            return ddRoot.getAllDisplayNames();
        }
        return null;
    }

    public void removeDisplayNameForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removeDisplayName() {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removeAllDisplayNames() {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public CommonDDBean addBean(String beanName, String[] propertyNames, Object[] propertyValues, String keyProperty) throws ClassNotFoundException, NameAlreadyUsedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setSmallIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setSmallIcon(String icon) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setLargeIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setLargeIcon(String icon) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setAllIcons(String[] locales, String[] smallIcons, String[] largeIcons) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void setIcon(Icon icon) {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public String getSmallIcon(String locale) throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.getSmallIcon(locale);
        }
        return null;
    }

    public String getSmallIcon() {
        if (ddRoot != null) {
            return ddRoot.getSmallIcon();
        }
        return null;
    }

    public String getLargeIcon(String locale) throws VersionNotSupportedException {
        if (ddRoot != null) {
            return ddRoot.getLargeIcon(locale);
        }
        return null;
    }

    public String getLargeIcon() {
        if (ddRoot != null) {
            return ddRoot.getLargeIcon();
        }
        return null;
    }

    public Icon getDefaultIcon() {
        if (ddRoot != null) {
            return ddRoot.getDefaultIcon();
        }
        return null;
    }

    public Map getAllIcons() {
        if (ddRoot != null) {
            return ddRoot.getAllIcons();
        }
        return null;
    }

    public void removeSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removeLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removeSmallIcon() {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removeLargeIcon() {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removeIcon() {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }

    public void removeAllIcons() {
        throw new UnsupportedOperationException("This operation is not implemented yet.");
    }
}
