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

package org.netbeans.modules.websvc.manager.model;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.manager.WebServiceDescriptor;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;

/**
 * A webservice meta data. Holds the URL location, package name for code generation
 * When the WSDL is parsed for each service a WebServiceData is created and added to
 * the WebServiceListModel.
 * @author Winston Prakash, David Botterill, cao
 */
public class WebServiceData {
    public static final String JAX_WS = "jaxws";
    public static final String JAX_RPC = "jaxrpc";
    
    /** Unique Web service id*/
    private String websvcId;
    
    /** Web service URL (File) */
    private String wsdlUrl;
    private String catalog;
    
    /** Group ID to which this Web Service belogs */
    private String groupId;
    private String packageName;
    
    /** WSDL Service Model this meta model wraps */
    WsdlService wsdlService;
    
    /* Default package name for generated source */
    public final static String DEFAULT_PACKAGE_NAME = "webservice";
    
    /** This is the name of WsdlService WsdlService.getName()
     * Used to find the corresponding WsdlService during loading from
     * persstence
     */
    private String wsName;
    private boolean jaxWsEnabled;
    private boolean jaxRpcEnabled;
    private boolean compiled;
    
    // File descriptors for each web service type
    private WebServiceDescriptor jaxWsDescriptor;
    private WebServiceDescriptor jaxRpcDescriptor;
    
    private String jaxWsDescriptorPath;
    private String jaxRpcDescriptorPath;
    
    private List<WebServiceDataListener> listeners = new ArrayList<WebServiceDataListener>();
    
    /** Default constructor needed for persistence*/
    public WebServiceData() {
    }
    
    public WebServiceData(String url, String packageName, String groupId) {
        websvcId = WebServiceListModel.getInstance().getUniqueWebServiceId();
        wsdlUrl = url;
        this.packageName = packageName;
        this.groupId = groupId;
        this.compiled = false;
    }
    
    public WebServiceData(WsdlService service, String url, String packageName, String groupId){
        websvcId = WebServiceListModel.getInstance().getUniqueWebServiceId();
        wsdlUrl = url;
        wsdlService = service;
        wsName = service.getName();
        this.packageName = packageName;
        this.groupId = groupId;
        this.compiled = false;
    }
    
    public void setWsdlService(WsdlService svc){
        wsdlService = svc;
    }
    
    public WsdlService getWsdlService( ) {
        return wsdlService;
    }
    
    public void setId(String id){
        websvcId = id;
    }
    
    public String getId(){
        return websvcId;
    }
    
    
    public void setName(String name) {
        wsName = name;
    }
    
    public String getName() {
        return wsName;
    }
    
    public String getGroupId(){
        return groupId;
    }
    
    public void setGroupId(String id){
        setModelDirty();
        groupId = id;
    }
    
    public String getURL() {
        return wsdlUrl;
    }
    
    public void setURL(String url) {
        setModelDirty();
        wsdlUrl = url;
    }
    
    public void setPackageName(String inPackageName){
        setModelDirty();
        packageName = inPackageName;
    }
    
    public String getPackageName(){
        if(null == packageName) {
            packageName = DEFAULT_PACKAGE_NAME;
        }
        return packageName;
    }

    public WebServiceDescriptor getJaxWsDescriptor() {
        return jaxWsDescriptor;
    }

    public void setJaxWsDescriptor(WebServiceDescriptor jaxWsDescriptor) {
        this.jaxWsDescriptor = jaxWsDescriptor;
    }

    public WebServiceDescriptor getJaxRpcDescriptor() {
        return jaxRpcDescriptor;
    }

    public void setJaxRpcDescriptor(WebServiceDescriptor jaxRpcDescriptor) {
        this.jaxRpcDescriptor = jaxRpcDescriptor;
    }

    public String getJaxWsDescriptorPath() {
        return jaxWsDescriptorPath;
    }

    public void setJaxWsDescriptorPath(String jaxWsDescriptorPath) {
        this.jaxWsDescriptorPath = jaxWsDescriptorPath;
    }

    public String getJaxRpcDescriptorPath() {
        return jaxRpcDescriptorPath;
    }

    public void setJaxRpcDescriptorPath(String jaxRpcDescriptorPath) {
        this.jaxRpcDescriptorPath = jaxRpcDescriptorPath;
    }
    
    /**
     * Partial Fix for Bug: 5107518
     * Changed so the web services will only be persisted if there is a change.
     * - David Botterill 9/29/2004
     */
    private void setModelDirty() {
        WebServiceListModel.setDirty(true);
    }
    
    public boolean isJaxRpcEnabled() {
        return jaxRpcEnabled;
    }
    
    public boolean isJaxWsEnabled() {
        return jaxWsEnabled;
    }
    
    public void setJaxRpcEnabled(boolean b) {
        jaxRpcEnabled = b;
    }
    
    public void setJaxWsEnabled(boolean b) {
        jaxWsEnabled = b;
    }
    
    public boolean isCompiled() {
        return compiled;
    }

    public void setCompiled(boolean compiled) {
        boolean fireEvent = false;
        if (this.compiled == false && compiled == true) fireEvent = true;
        this.compiled = compiled;
        
        if (fireEvent) {
            for (WebServiceDataListener listener : listeners) {
                listener.webServiceCompiled(new WebServiceDataEvent(this));
            }
        }
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public void addWebServiceDataListener(WebServiceDataListener listener) {
        listeners.add(listener);
    }
}
