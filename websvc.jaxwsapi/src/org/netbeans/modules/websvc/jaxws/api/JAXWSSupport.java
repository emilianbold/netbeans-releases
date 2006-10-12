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

package org.netbeans.modules.websvc.jaxws.api;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.websvc.jaxws.JAXWSSupportAccessor;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportImpl;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/** WebServicesSupport should be used to manipulate a projects representation
 *  of a web service implementation.
 * <p>
 * A client may obtain a WebServicesSupport instance using
 * <code>WebServicesSupport.getWebServicesSupport(fileObject)</code> static
 * method, for any FileObject in the project directory structure.
 *
 * @author Peter Williams, Milan Kuchtiak
 */
public final class JAXWSSupport {
    
    private JAXWSSupportImpl impl;
    private static final Lookup.Result implementations =
    Lookup.getDefault().lookup(new Lookup.Template(JAXWSSupportProvider.class));
    
    static  {
        JAXWSSupportAccessor.DEFAULT = new JAXWSSupportAccessor() {
            public JAXWSSupport createJAXWSSupport(JAXWSSupportImpl spiWebServicesSupport) {
                return new JAXWSSupport(spiWebServicesSupport);
            }
            
            public JAXWSSupportImpl getJAXWSSupportImpl(JAXWSSupport wss) {
                return wss == null ? null : wss.impl;
            }
        };
    }
    
    private JAXWSSupport(JAXWSSupportImpl impl) {
        if (impl == null)
            throw new IllegalArgumentException();
        this.impl = impl;
    }
    
    /** Find the WebServicesSupport for given file or null if the file does not belong
     * to any module support web services.
     */
    public static JAXWSSupport getJAXWSSupport(FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to WebServicesSupport.getWebServicesSupport(FileObject)"); // NOI18N
        }
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            JAXWSSupportProvider supportProvider = (JAXWSSupportProvider)it.next();
            JAXWSSupport wss = supportProvider.findJAXWSSupport(f);
            if (wss != null) {
                return wss;
            }
        }
        return null;
    }
    
    // Delegated methods from WebServicesSupportImpl
    
    /*
     * Add web service to jax-ws.xml
     * intended for web services from java
     * @param serviceName service display name (name of the node ws will be presented in Netbeans), e.g. "SearchService"
     * @param serviceImpl package name of the implementation class, e.g. "org.netbeans.SerchServiceImpl"
     * @param isJsr109 Indicates if the web service is being created in a JSR 109 container
     */
    public void addService(String serviceName, String serviceImpl, boolean isJsr109) {
        impl.addService(serviceName, serviceImpl, isJsr109);
    }
    
    /*
     * Add web service to jax-ws.xml
     * intended for web services from wsdl
     * @param name service display name (name of the node ws will be presented in Netbeans), e.g. "SearchService"
     * @param serviceImpl package name of the implementation class, e.g. "org.netbeans.SerchServiceImpl"
     * @param wsdlUrl url of the local wsdl file, e.g. file:/home/userXY/documents/wsdl/SearchService.wsdl"
     * @param serviceName service name (from service wsdl element), e.g. SearchService
     * @param portName port name (from service:port element), e.g. SearchServicePort
     * @param packageName package name where java artifacts will be generated
     * @param isJsr109 Indicates if the web service is being created in a project that supports a JSR 109 container
     * @return returns the unique IDE service name
     */
    public String addService(String name, String serviceImpl, String wsdlUrl, 
            String serviceName, String portName, String packageName, boolean isJsr109) {
        return impl.addService(name, serviceImpl, wsdlUrl, serviceName, portName, packageName, isJsr109);
    }
    /**
     * Returns the list of web services in the project
     */
    public List getServices() {
        return impl.getServices();
    }  
  
    /**
     * Remove the web service entries from the project properties
     * project.xml files
     */
    public void removeService(String serviceName) {
        impl.removeService(serviceName);
    }
    
    /**
     * Notification when Service (created from java) is removed from jax-ws.xml
     * (JAXWSSupport needs to react when @WebService annotation is removed 
     * or when impl.class is removed (manually from project) 
     */
    public void serviceFromJavaRemoved(String serviceName) {
        impl.serviceFromJavaRemoved(serviceName);
    }
    
    /**
     * Returns the name of the implementation class
     * given the service (ide) name
     */
    public String getServiceImpl(String serviceName) {
        return impl.getServiceImpl(serviceName);
    }
    
    /**
     * Determine if the web service was created from WSDL
     */
    public boolean isFromWSDL(String serviceName) {
        return impl.isFromWSDL(serviceName);
    }
    
    /**
     *  return wsdl folder for local copy of WSDL
     */
    public FileObject getWsdlFolder(boolean create) throws java.io.IOException {
        return impl.getWsdlFolder(create);
    }
    
    /**
     *  return folder for local wsdl artifacts
     */
    public FileObject getLocalWsdlFolderForService(String serviceName, boolean createFolder) {
        return impl.getLocalWsdlFolderForService(serviceName,createFolder);
    }
    
    /**
     *  return folder for local wsdl bindings
     */
    public FileObject getBindingsFolderForService(String serviceName, boolean createFolder) {
        return impl.getBindingsFolderForService(serviceName,createFolder);
    }

    public AntProjectHelper getAntProjectHelper() {
        return impl.getAntProjectHelper();
    }
    
    /** Get EntityCatalog for local copies of wsdl and schema files
     */
    public URL getCatalog() {
        return impl.getCatalog();
    }
    
    /** Get wsdlLocation information 
     * Useful for web service from wsdl
     * @param name service "display" name
     */
    public String getWsdlLocation(String serviceName) {
        return impl.getWsdlLocation(serviceName);
    }
    
    public void removeNonJsr109Entries(String serviceName) throws IOException{
        impl.removeNonJsr109Entries(serviceName);
    }
    
    public FileObject getDeploymentDescriptorFolder(){
        return impl.getDeploymentDescriptorFolder();
    }
}
