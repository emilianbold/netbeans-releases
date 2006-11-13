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

package org.netbeans.modules.websvc.client;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.websvc.api.client.ClientStubDescriptor;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportImpl;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportProvider;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Lukas Jungmann
 */
public class CustomWebServicesClientSupportProvider implements WebServicesClientSupportProvider {
    
    private Map/*<FileObject, WebServicesClientSupport>*/ cache = new HashMap/*<FileObject, WebServicesClientSupport>*/();
    private Map/*<FileObject, JAXWSClientSupport>*/ cache2 = new HashMap/*<FileObject, JAXWSClientSupport>*/();
    
    /** Creates a new instance of CustomWebServicesSupportProvider */
    public CustomWebServicesClientSupportProvider() {
    }
    
    public WebServicesClientSupport findWebServicesClientSupport(FileObject file) {
        if (file.getExt().equals("ws") || file.getExt().equals("both")) {
            WebServicesClientSupport em  =  (WebServicesClientSupport) cache.get(file.getParent());
            if (em == null) {
                em = WebServicesClientSupportFactory.createWebServicesClientSupport(new CustomWebServicesClientSupportImpl(file));
                cache.put(file.getParent(), em);
            }
            return em;
        }
        return null;
    }
    
    public JAXWSClientSupport findJAXWSClientSupport(FileObject file) {
        if (file.getExt().equals("jaxws") || file.getExt().equals("both")) {
            JAXWSClientSupport em  =  (JAXWSClientSupport) cache2.get(file.getParent());
            if (em == null) {
                em = JAXWSClientSupportFactory.createJAXWSClientSupport(new CustomJAXWSClientSupportImpl(file));
                cache2.put(file.getParent(), em);
            }
            return em;
        }
        return null;
    }
    
    private static final class CustomWebServicesClientSupportImpl implements WebServicesClientSupportImpl {
        
        private FileObject fo;
        
        CustomWebServicesClientSupportImpl(FileObject fo) {
            this.fo = fo;
        }
        
        public void addServiceClient(String serviceName, String packageName,
                String sourceUrl, FileObject configFile,
                ClientStubDescriptor stubDescriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void addServiceClient(String serviceName, String packageName,
                String sourceUrl, FileObject configFile,
                ClientStubDescriptor stubDescriptor,
                String[] wscompileFeatures) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void addServiceClientReference(String serviceName,
                String fqServiceName,
                String relativeWsdlPath,
                String mappingPath,
                String[] portSEIInfo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void removeServiceClient(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getWsdlFolder(boolean create) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getDeploymentDescriptor() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public List getStubDescriptors() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public List getServiceClients() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String getWsdlSource(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void setWsdlSource(String serviceName, String wsdlSource) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void setProxyJVMOptions(String proxyHost, String proxyPort) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String getServiceRefName(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    private static final class CustomJAXWSClientSupportImpl implements JAXWSClientSupportImpl {
        
        private FileObject fo;
        
        CustomJAXWSClientSupportImpl(FileObject fo) {
            this.fo = fo;
        }
        
        public String addServiceClient(String clientName, String wsdlUrl,
                String packageName, boolean isJsr109) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getWsdlFolder(boolean create) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getLocalWsdlFolderForClient(String clientName,
                boolean createFolder) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getBindingsFolderForClient(String clientName,
                boolean createFolder) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void removeServiceClient(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public List getServiceClients() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public URL getCatalog() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String getServiceRefName(Node clientNode) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
