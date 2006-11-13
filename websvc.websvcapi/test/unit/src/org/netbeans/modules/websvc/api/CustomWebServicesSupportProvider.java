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

package org.netbeans.modules.websvc.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportFactory;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportImpl;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Lukas Jungmann
 */
public class CustomWebServicesSupportProvider implements WebServicesSupportProvider {
    
    private Map<FileObject, WebServicesSupport> cache = new HashMap<FileObject, WebServicesSupport>();
    
    /** Creates a new instance of CustomWebServicesSupportProvider */
    public CustomWebServicesSupportProvider() {
    }
    
    public WebServicesSupport findWebServicesSupport(FileObject file) {
        if (file.getExt().equals("ws")) {
            WebServicesSupport em  =  cache.get(file.getParent());
            if (em == null) {
                em = WebServicesSupportFactory.createWebServicesSupport(new CustomWebServicesSupport(file));
                cache.put(file.getParent(), em);
            }
            return em;
        }
        return null;
    }
    
    private static final class CustomWebServicesSupport implements WebServicesSupportImpl {
        private FileObject fo;
        
        CustomWebServicesSupport(FileObject fo) {
            this.fo = fo;
        }
        
        public void addServiceImpl(String serviceName, FileObject configFile,
                boolean fromWSDL, String[] wscompileFeatures) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void addServiceImpl(String serviceName, FileObject configFile,
                boolean fromWSDL) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void addServiceEntriesToDD(String serviceName,
                String serviceEndpointInterface,
                String serviceEndpoint) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getWebservicesDD() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getWsDDFolder() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String getArchiveDDFolderName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String getImplementationBean(String linkName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void removeServiceEntry(String linkName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void removeProjectEntries(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public AntProjectHelper getAntProjectHelper() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String generateImplementationBean(String wsName, FileObject pkg,
                Project project,
                String delegateData) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void addServiceImplLinkEntry(ServiceImplBean serviceImplBean,
                String wsName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public ReferenceHelper getReferenceHelper() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public List getServices() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void addInfrastructure(String implBeanClass, FileObject pkg) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public boolean isFromWSDL(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public ClassPath getClassPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
}
