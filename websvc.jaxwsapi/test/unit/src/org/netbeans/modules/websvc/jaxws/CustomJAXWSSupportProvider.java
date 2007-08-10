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

package org.netbeans.modules.websvc.jaxws;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportFactory;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportImpl;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Lukas Jungmann
 */
public class CustomJAXWSSupportProvider implements JAXWSSupportProvider {
    
   private Map<FileObject, JAXWSSupport> cache = new HashMap<FileObject, JAXWSSupport>();
   
    /** Creates a new instance of TestProjectJAXWSSupportProvider */
    public CustomJAXWSSupportProvider() {
    }
    
    public JAXWSSupport findJAXWSSupport(FileObject file) {
        if (file.getExt().equals ("ws")) {
            JAXWSSupport em  =  cache.get(file.getParent());
            if (em == null) {
                em = JAXWSSupportFactory.createJAXWSSupport(new CustomJAXWSSupport(file.getParent()));
                cache.put(file.getParent(), em);
            }
            return em;
        }
        return null;
    }
    
    private static class CustomJAXWSSupport implements JAXWSSupportImpl {
        
        private FileObject fo;
        
        CustomJAXWSSupport(FileObject fo) {
            this.fo = fo;
        }
        
        public void addService(String serviceName, String serviceImpl,
                boolean isJsr109) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String addService(String name, String serviceImpl, String wsdlUrl,
                String serviceName, String portName,
                String packageName, boolean isJsr109) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public List getServices() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void removeService(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void serviceFromJavaRemoved(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String getServiceImpl(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public boolean isFromWSDL(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getWsdlFolder(boolean create) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getLocalWsdlFolderForService(String serviceName,
                boolean createFolder) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getBindingsFolderForService(String serviceName,
                boolean createFolder) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public AntProjectHelper getAntProjectHelper() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public URL getCatalog() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String getWsdlLocation(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void removeNonJsr109Entries(String serviceName) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getDeploymentDescriptorFolder() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
