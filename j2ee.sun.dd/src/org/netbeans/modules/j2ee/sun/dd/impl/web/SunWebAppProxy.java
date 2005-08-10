/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * SunWebAppProxy.java
 *
 * Created on February 7, 2005, 7:33 PM
 */

package org.netbeans.modules.j2ee.sun.dd.impl.web;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDException;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;

import org.netbeans.modules.j2ee.sun.dd.impl.DTDRegistry;

import org.w3c.dom.Document;
/**
 *
 * @author Nitya Doraisamy
 */
public class SunWebAppProxy implements SunWebApp {
    
    private SunWebApp webRoot;
    private String version;
    private OutputProvider outputProvider;
    private int ddStatus;
    private org.xml.sax.SAXParseException error;    
    private java.util.List listeners; 
        
    
    /** Creates a new instance of SunWebAppProxy */
    public SunWebAppProxy(SunWebApp webRoot, String version) {
        this.webRoot = webRoot;
        this.version = version;
    }

    public int addEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef ejbRef) {
        return webRoot==null?-1:webRoot.addEjbRef(ejbRef);
    }

    public int addIdempotentUrlPattern(boolean param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.addIdempotentUrlPattern(param);
    }

    public int addMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination messageDestination) {
        return webRoot==null?-1:webRoot.addMessageDestination(messageDestination);
    }

    public int addWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty webProperty) {
        return webRoot==null?-1:webRoot.addWebProperty(webProperty);
    }

    public int addResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef resourceEnvRef) {
        return webRoot==null?-1:webRoot.addResourceEnvRef(resourceEnvRef);
    }

    public int addResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef resourceRef) {
        return webRoot==null?-1:webRoot.addResourceRef(resourceRef);
    }

    public int addSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping securityRoleMapping) {
        return webRoot==null?-1:webRoot.addSecurityRoleMapping(securityRoleMapping);
    }

    public int addServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef serviceRef) {
        return webRoot==null?-1:webRoot.addServiceRef(serviceRef);
    }

    public int addServlet(org.netbeans.modules.j2ee.sun.dd.api.web.Servlet servlet) {
        return webRoot==null?-1:webRoot.addServlet(servlet);
    }

    public int addWebserviceDescription(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription webserviceDescription) {
        return webRoot==null?-1:webRoot.addWebserviceDescription(webserviceDescription);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.Cache getCache() {
        return webRoot==null?null:webRoot.getCache();
    }

    public String getContextRoot() {
        return webRoot==null?null:webRoot.getContextRoot();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef[] getEjbRef() {
        return webRoot==null?null:webRoot.getEjbRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef getEjbRef(int param) {
        return webRoot==null?null:webRoot.getEjbRef(param);
    }

    public String getErrorUrl() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getErrorUrl();
    }

    public boolean[] getIdempotentUrlPattern() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getIdempotentUrlPattern();
    }

    public String getIdempotentUrlPatternNumOfRetries(int param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getIdempotentUrlPatternNumOfRetries(param);
    }

    public String getIdempotentUrlPatternUrlPattern(int param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getIdempotentUrlPatternUrlPattern(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig getJspConfig() {
        return webRoot==null?null:webRoot.getJspConfig();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetInfo getLocaleCharsetInfo() {
        return webRoot==null?null:webRoot.getLocaleCharsetInfo();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination[] getMessageDestination() {
        return webRoot==null?null:webRoot.getMessageDestination();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination getMessageDestination(int param) {
        return webRoot==null?null:webRoot.getMessageDestination(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.MyClassLoader getMyClassLoader() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getMyClassLoader();
    }

    public String getParameterEncodingDefaultCharset() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getParameterEncodingDefaultCharset();
    }

    public String getParameterEncodingFormHintField() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getParameterEncodingFormHintField();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] getWebProperty() {
        return webRoot==null?null:webRoot.getWebProperty();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty getWebProperty(int param) {
        return webRoot==null?null:webRoot.getWebProperty(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef[] getResourceEnvRef() {
        return webRoot==null?null:webRoot.getResourceEnvRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef getResourceEnvRef(int param) {
        return webRoot==null?null:webRoot.getResourceEnvRef(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef[] getResourceRef() {
        return webRoot==null?null:webRoot.getResourceRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef getResourceRef(int param) {
        return webRoot==null?null:webRoot.getResourceRef(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] getSecurityRoleMapping() {
        return webRoot==null?null:webRoot.getSecurityRoleMapping();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping getSecurityRoleMapping(int param) {
        return webRoot==null?null:webRoot.getSecurityRoleMapping(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef[] getServiceRef() {
        return webRoot==null?null:webRoot.getServiceRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef getServiceRef(int param) {
        return webRoot==null?null:webRoot.getServiceRef(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.Servlet[] getServlet() {
        return webRoot==null?null:webRoot.getServlet();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.Servlet getServlet(int param) {
        return webRoot==null?null:webRoot.getServlet(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.SessionConfig getSessionConfig() {
        return webRoot==null?null:webRoot.getSessionConfig();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription[] getWebserviceDescription() {
        return webRoot==null?null:webRoot.getWebserviceDescription();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription getWebserviceDescription(int param) {
        return webRoot==null?null:webRoot.getWebserviceDescription(param);
    }

    public boolean isIdempotentUrlPattern(int param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?false:webRoot.isIdempotentUrlPattern(param);
    }

    public boolean isMyClassLoader() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?false:webRoot.isMyClassLoader();
    }

    public boolean isParameterEncoding() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?false:webRoot.isParameterEncoding();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.Cache newCache() {
        return webRoot==null?null:webRoot.newCache();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef newEjbRef() {
        return webRoot==null?null:webRoot.newEjbRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig newJspConfig() {
        return webRoot==null?null:webRoot.newJspConfig();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetInfo newLocaleCharsetInfo() {
        return webRoot==null?null:webRoot.newLocaleCharsetInfo();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination newMessageDestination() {
        return webRoot==null?null:webRoot.newMessageDestination();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.MyClassLoader newMyClassLoader() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.newMyClassLoader();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty newWebProperty() {
        return webRoot==null?null:webRoot.newWebProperty();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef newResourceEnvRef() {
        return webRoot==null?null:webRoot.newResourceEnvRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef newResourceRef() {
        return webRoot==null?null:webRoot.newResourceRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping newSecurityRoleMapping() {
        return webRoot==null?null:webRoot.newSecurityRoleMapping();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef newServiceRef() {
        return webRoot==null?null:webRoot.newServiceRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.Servlet newServlet() {
        return webRoot==null?null:webRoot.newServlet();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.web.SessionConfig newSessionConfig() {
        return webRoot==null?null:webRoot.newSessionConfig();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription newWebserviceDescription() {
        return webRoot==null?null:webRoot.newWebserviceDescription();
    }

    public int removeEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef ejbRef) {
        return webRoot==null?-1:webRoot.removeEjbRef(ejbRef);
    }

    public int removeIdempotentUrlPattern(boolean param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.removeIdempotentUrlPattern(param);
    }

    public void removeIdempotentUrlPattern(int param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.removeIdempotentUrlPattern(param);
    }

    public int removeMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination messageDestination) {
        return webRoot==null?-1:webRoot.removeMessageDestination(messageDestination);
    }

    public int removeWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty webProperty) {
        return webRoot==null?-1:webRoot.removeWebProperty(webProperty);
    }

    public int removeResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef resourceEnvRef) {
        return webRoot==null?-1:webRoot.removeResourceEnvRef(resourceEnvRef);
    }

    public int removeResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef resourceRef) {
        return webRoot==null?-1:webRoot.removeResourceRef(resourceRef);
    }

    public int removeSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping securityRoleMapping) {
        return webRoot==null?-1:webRoot.removeSecurityRoleMapping(securityRoleMapping);
    }

    public int removeServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef serviceRef) {
        return webRoot==null?-1:webRoot.removeServiceRef(serviceRef);
    }

    public int removeServlet(org.netbeans.modules.j2ee.sun.dd.api.web.Servlet servlet) {
        return webRoot==null?-1:webRoot.removeServlet(servlet);
    }

    public int removeWebserviceDescription(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription webserviceDescription) {
        return webRoot==null?-1:webRoot.removeWebserviceDescription(webserviceDescription);
    }

    public void setCache(org.netbeans.modules.j2ee.sun.dd.api.web.Cache cache) {
        if (webRoot!=null) webRoot.setCache(cache);
    }

    public void setContextRoot(String str) {
        if (webRoot!=null) webRoot.setContextRoot(str);
    }

    public void setEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef[] ejbRef) {
        if (webRoot!=null) webRoot.setEjbRef(ejbRef);
    }

    public void setEjbRef(int param, org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef ejbRef) {
        if (webRoot!=null) webRoot.setEjbRef(param, ejbRef);
    }

    public void setErrorUrl(String str) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setErrorUrl(str);
    }

    public void setIdempotentUrlPattern(boolean[] values) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setIdempotentUrlPattern(values);
    }

    public void setIdempotentUrlPattern(int param, boolean param1) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setIdempotentUrlPattern(param, param1);
    }

    public void setIdempotentUrlPatternNumOfRetries(int param, String str) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setIdempotentUrlPatternNumOfRetries(param, str);
    }

    public void setIdempotentUrlPatternUrlPattern(int param, String str) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setIdempotentUrlPatternUrlPattern(param, str);
    }

    public void setJspConfig(org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig jspConfig) {
        if (webRoot!=null) webRoot.setJspConfig(jspConfig);
    }

    public void setLocaleCharsetInfo(org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetInfo localeCharsetInfo) {
        if (webRoot!=null) webRoot.setLocaleCharsetInfo(localeCharsetInfo);
    }

    public void setMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination[] messageDestination) {
        if (webRoot!=null) webRoot.setMessageDestination(messageDestination);
    }

    public void setMessageDestination(int param, org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination messageDestination) {
        if (webRoot!=null) webRoot.setMessageDestination(param, messageDestination);
    }

    public void setMyClassLoader(boolean param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setMyClassLoader(param);
    }

    public void setMyClassLoader(org.netbeans.modules.j2ee.sun.dd.api.web.MyClassLoader myClassLoader) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setMyClassLoader(myClassLoader);
    }

    public void setParameterEncoding(boolean param) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setParameterEncoding(param);
    }

    public void setParameterEncodingDefaultCharset(String str) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setParameterEncodingDefaultCharset(str);
    }

    public void setParameterEncodingFormHintField(String str) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setParameterEncodingFormHintField(str);
    }

    public void setWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] webProperty) {
        if (webRoot!=null) webRoot.setWebProperty(webProperty);
    }

    public void setWebProperty(int param, org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty webProperty) {
        if (webRoot!=null) webRoot.setWebProperty(param, webProperty);
    }

    public void setResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef[] resourceEnvRef) {
        if (webRoot!=null) webRoot.setResourceEnvRef(resourceEnvRef);
    }

    public void setResourceEnvRef(int param, org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef resourceEnvRef) {
        if (webRoot!=null) webRoot.setResourceEnvRef(param, resourceEnvRef);
    }

    public void setResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef[] resourceRef) {
        if (webRoot!=null) webRoot.setResourceRef(resourceRef);
    }

    public void setResourceRef(int param, org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef resourceRef) {
        if (webRoot!=null) webRoot.setResourceRef(param, resourceRef);
    }

    public void setSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] securityRoleMapping) {
        if (webRoot!=null) webRoot.setSecurityRoleMapping(securityRoleMapping);
    }

    public void setSecurityRoleMapping(int param, org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping securityRoleMapping) {
        if (webRoot!=null) webRoot.setSecurityRoleMapping(param, securityRoleMapping);
    }

    public void setServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef[] serviceRef) {
        if (webRoot!=null) webRoot.setServiceRef(serviceRef);
    }

    public void setServiceRef(int param, org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef serviceRef) {
        if (webRoot!=null) webRoot.setServiceRef(param, serviceRef);
    }

    public void setServlet(org.netbeans.modules.j2ee.sun.dd.api.web.Servlet[] servlet) {
        if (webRoot!=null) webRoot.setServlet(servlet);
    }

    public void setServlet(int param, org.netbeans.modules.j2ee.sun.dd.api.web.Servlet servlet) {
        if (webRoot!=null) webRoot.setServlet(param, servlet);
    }

    public void setSessionConfig(org.netbeans.modules.j2ee.sun.dd.api.web.SessionConfig sessionConfig) {
        if (webRoot!=null) webRoot.setSessionConfig(sessionConfig);
    }

    public void setWebserviceDescription(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription[] webserviceDescription) {
        if (webRoot!=null) webRoot.setWebserviceDescription(webserviceDescription);
    }

    public void setWebserviceDescription(int param, org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription webserviceDescription) {
        if (webRoot!=null) webRoot.setWebserviceDescription(param, webserviceDescription);
    }

    public int sizeEjbRef() {
        return webRoot==null?-1:webRoot.sizeEjbRef();
    }

    public int sizeIdempotentUrlPattern() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.sizeIdempotentUrlPattern();
    }

    public int sizeIdempotentUrlPatternNumOfRetries() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.sizeIdempotentUrlPatternNumOfRetries();
    }

    public int sizeIdempotentUrlPatternUrlPattern() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.sizeIdempotentUrlPatternUrlPattern();
    }

    public int sizeMessageDestination() {
        return webRoot==null?-1:webRoot.sizeMessageDestination();
    }

    public int sizeWebProperty() {
        return webRoot==null?-1:webRoot.sizeWebProperty();
    }

    public int sizeResourceEnvRef() {
        return webRoot==null?-1:webRoot. sizeResourceEnvRef();
    }

    public int sizeResourceRef() {
        return webRoot==null?-1:webRoot.sizeResourceRef();
    }

    public int sizeSecurityRoleMapping() {
        return webRoot==null?-1:webRoot.sizeSecurityRoleMapping();
    }

    public int sizeServiceRef() {
        return webRoot==null?-1:webRoot.sizeServiceRef();
    }

    public int sizeServlet() {
        return webRoot==null?-1:webRoot.sizeServlet();
    }

    public int sizeWebserviceDescription() {
        return webRoot==null?-1:webRoot.sizeWebserviceDescription();
    }
    
    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (webRoot != null) 
            webRoot.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
         if (webRoot != null) 
            webRoot.addPropertyChangeListener(pcl);
        listeners.add(pcl);
    }

    public SunWebApp getOriginal() {
        return webRoot;
    }
    
    public org.xml.sax.SAXParseException getError() {
        return error;
    }
    public void setError(org.xml.sax.SAXParseException error) {
        this.error = error;
    }  
    
    public void setVersion(java.math.BigDecimal version) {
        String newVersion = version.toString();
        if (this.version.equals(newVersion)) 
            return;
        if (webRoot != null) {
            Document document = null;
            if (webRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_3_0.SunWebApp) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_3_0.SunWebApp)webRoot).graphManager().getXmlDocument();
            }else if (webRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp)webRoot).graphManager().getXmlDocument();
            }else if (webRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp)webRoot).graphManager().getXmlDocument();
            }else if (webRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_5_0.SunWebApp) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_5_0.SunWebApp)webRoot).graphManager().getXmlDocument();
            }
            
            //remove the doctype
            //document = removeDocType(document);
            
            if(newVersion.equals(SunWebApp.VERSION_2_5_0)){
                //This will always be an upgrade
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_5_0.SunWebApp webGraph =
                        org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_5_0.SunWebApp.createGraph(document);
                webGraph.changeDocType(DTDRegistry.SUN_WEBAPP_250_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_250_DTD_SYSTEM_ID);
                this.webRoot = new SunWebAppProxy(webGraph, webGraph.getVersion().toString());
            }
            if(newVersion.equals(SunWebApp.VERSION_2_4_1)){
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp webGraph =
                        org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp.createGraph(document); 
                webGraph.changeDocType(DTDRegistry.SUN_WEBAPP_241_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_241_DTD_SYSTEM_ID);
                if(this.version.equals(SunWebApp.VERSION_2_5_0)){
                   //need to remove elements 
                }
                this.webRoot = new SunWebAppProxy(webGraph, webGraph.getVersion().toString());
            }
            if(newVersion.equals(SunWebApp.VERSION_2_4_0)){
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp webGraph =
                        org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp.createGraph(document);
                webGraph.changeDocType(DTDRegistry.SUN_WEBAPP_240_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_240_DTD_SYSTEM_ID);
                if(! this.version.equals(SunWebApp.VERSION_2_3_0)){
                    //need to remove elements 
                }
                this.webRoot = new SunWebAppProxy(webGraph, webGraph.getVersion().toString());
            }
            if(newVersion.equals(SunWebApp.VERSION_2_3_0)){
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_3_0.SunWebApp webGraph =
                        org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_3_0.SunWebApp.createGraph(document);
                webGraph.changeDocType(DTDRegistry.SUN_WEBAPP_230_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_230_DTD_SYSTEM_ID);
                //need to remove elements
                this.webRoot = new SunWebAppProxy(webGraph, webGraph.getVersion().toString());
            }
        }
    }

    private Document removeDocType(Document document){
        if (document != null) {
            org.w3c.dom.Element docElement = document.getDocumentElement();
            if (docElement != null) {
                org.w3c.dom.DocumentType docType = document.getDoctype();
                if (docType != null) {
                    document.removeChild(docType); //NOI18N
                }
            }
        }
        return document;
    } 
    
    public java.math.BigDecimal getVersion() {
        return new java.math.BigDecimal(version);
    }
    
    public Object getValue(String name) {
        return webRoot==null?null:webRoot.getValue(name);
    }
    
    public void write(java.io.OutputStream os) throws java.io.IOException {
        if (webRoot != null) {
            webRoot.write(os);
        }
    }
    
    public String dumpBeanNode() {
        if (webRoot != null) 
            return webRoot.dumpBeanNode();
        else
            return null;
    }

    public void setValue(String name, Object[] value) {
        if (webRoot!=null) webRoot.setValue(name, value);
    }

    public Object[] getValues(String name) {
        return webRoot==null?null:webRoot.getValues(name);
    }

    public void setValue(String name, int index, Object value) {
        if (webRoot!=null) webRoot.setValue(name, index, value);
    }

    public void setValue(String name, Object value) {
        if (webRoot!=null) webRoot.setValue(name, value);
    }

    public Object getValue(String name, int index) {
        return webRoot==null?null:webRoot.getValue(name, index);
    }

    public String getAttributeValue(String name) {
         return webRoot==null?null:webRoot.getAttributeValue(name);
    }

    public int size(String name) {
         return webRoot==null?-1:webRoot.size(name);
    }

    public int addValue(String name, Object value) {
        return webRoot==null?-1:webRoot.addValue(name, value);
    }

    public String[] findPropertyValue(String propName, Object value) {
        return webRoot==null?null:webRoot.findPropertyValue(propName, value);
    }

    public int removeValue(String name, Object value) {
        return webRoot==null?-1:webRoot.removeValue(name, value);
    }

    public void write(java.io.Writer w) throws java.io.IOException, DDException {
        if (webRoot!=null) webRoot.write(w);
    }

    public void removeValue(String name, int index) {
        if (webRoot!=null) webRoot.removeValue(name, index);
    }

    public Object clone() {
        SunWebAppProxy proxy = null;
        if (webRoot==null)
            proxy = new SunWebAppProxy(null, version);
        else {
            SunWebApp clonedSunWeb=(SunWebApp)webRoot.clone();
            proxy = new SunWebAppProxy(clonedSunWeb, version);
        }
        proxy.setError(error);
        return proxy;
    }

    public String getAttributeValue(String propName, String name) {
        return webRoot==null?null:webRoot.getAttributeValue(propName, name);
    }

    public String getAttributeValue(String propName, int index, String name) {
        return webRoot==null?null:webRoot.getAttributeValue(propName, index, name);
    }

    public void setAttributeValue(String name, String value) {
        if (webRoot!=null) webRoot.setAttributeValue(name, value);
    }

    public void setAttributeValue(String propName, String name, String value) {
        if (webRoot!=null) webRoot.setAttributeValue(propName, name, value);
    }

    public void setAttributeValue(String propName, int index, String name, String value) {
        if (webRoot!=null) webRoot.setAttributeValue(propName, index, name, value);
    }    
    
    public CommonDDBean getPropertyParent(String name) {
        return webRoot.getPropertyParent(name);
    }

    public void merge(CommonDDBean root, int mode) {
        if (webRoot != null) {
            if (root instanceof SunWebAppProxy)
                webRoot.merge(((SunWebAppProxy)root).getOriginal(), mode);
            else webRoot.merge(root, mode);
        }
    }

    public int removeMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.removeMessageDestinationRef(value);
    }

    public int addMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.addMessageDestinationRef(value);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef getMessageDestinationRef(int index) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getMessageDestinationRef(index);
    }

    public void setMessageDestinationRef(int index, org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setMessageDestinationRef(index, value);
    }

    public void setMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef[] value) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        if (webRoot!=null) webRoot.setMessageDestinationRef(value);
    }

    public int sizeMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?-1:webRoot.sizeMessageDestinationRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef newMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.newMessageDestinationRef();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef[] getMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException {
        return webRoot==null?null:webRoot.getMessageDestinationRef();
    }
    
    
    /** Contract between friend modules that enables 
    * a specific handling of write(FileObject) method for targeted FileObject
    */
    public static interface OutputProvider {
        public void write(SunWebApp webApp) throws java.io.IOException;
    }
    
}
