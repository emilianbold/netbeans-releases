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

package org.netbeans.modules.websvc.api.webservices;

import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.websvc.webservices.WebServicesSupportAccessor;
import org.netbeans.modules.websvc.spi.webservices.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

/** WebServicesSupport should be used to manipulate a projects representation
 *  of a web service implementation.
 * <p>
 * A client may obtain a WebServicesSupport instance using
 * <code>WebServicesSupport.getWebServicesSupport(fileObject)</code> static
 * method, for any FileObject in the project directory structure.
 *
 * @author Peter Williams
 */
public final class WebServicesSupport {
    
    private WebServicesSupportImpl impl;
    private static final Lookup.Result implementations =
    Lookup.getDefault().lookup(new Lookup.Template(WebServicesSupportProvider.class));
    
    static  {
        WebServicesSupportAccessor.DEFAULT = new WebServicesSupportAccessor() {
            public WebServicesSupport createWebServicesSupport(WebServicesSupportImpl spiWebServicesSupport) {
                return new WebServicesSupport(spiWebServicesSupport);
            }
            
            public WebServicesSupportImpl getWebServicesSupportImpl(WebServicesSupport wss) {
                return wss == null ? null : wss.impl;
            }
        };
    }
    
    private WebServicesSupport(WebServicesSupportImpl impl) {
        if (impl == null)
            throw new IllegalArgumentException();
        this.impl = impl;
    }
    
    /** Find the WebServicesSupport for given file or null if the file does not belong
     * to any module support web services.
     */
    public static WebServicesSupport getWebServicesSupport(FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to WebServicesSupport.getWebServicesSupport(FileObject)"); // NOI18N
        }
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            WebServicesSupportProvider impl = (WebServicesSupportProvider)it.next();
            WebServicesSupport wss = impl.findWebServicesSupport(f);
            if (wss != null) {
                return wss;
            }
        }
        return null;
    }
    
    // Delegated methods from WebServicesSupportImpl
    
    public void addServiceImpl(String serviceName, FileObject configFile, boolean fromWSDL) {
        impl.addServiceImpl(serviceName, configFile, fromWSDL);
    }
    
    public void addServiceEntriesToDD(String serviceName, String serviceEndpointInterface, String servantClassName){
        impl.addServiceEntriesToDD(serviceName, serviceEndpointInterface, servantClassName);
    }
    
    public FileObject getWebservicesDD() {
        return impl.getWebservicesDD();
    }
    
    public FileObject getWsDDFolder() {
        return impl.getWsDDFolder();
    }
    
    public String getArchiveDDFolderName() {
        return impl.getArchiveDDFolderName();
    }
    
    public String getImplementationBean(String linkName) {
        return impl.getImplementationBean(linkName);
    }
    
    public void removeServiceEntry(String linkName) {
        impl.removeServiceEntry(linkName);
    }
    
    public void removeProjectEntries(String serviceName){
        impl.removeProjectEntries(serviceName);
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return impl.getAntProjectHelper();
    }
    
    public String  generateImplementationBean(String name, FileObject pkg, Project project, String delegateData) throws java.io.IOException {
        return impl.generateImplementationBean(name, pkg, project, delegateData);
    }
    
    public void addServiceImplLinkEntry(ServiceImplBean serviceImplBean, String wsName) {
        impl.addServiceImplLinkEntry(serviceImplBean, wsName);
    }
    
    public ReferenceHelper getReferenceHelper(){
        return impl.getReferenceHelper();
    }
    
    public List/*WsCompileEditorSupport.ServiceSettings*/ getServices() {
        return impl.getServices();
    }
    
    public void addInfrastructure(String implBeanClass, FileObject pkg) {
        impl.addInfrastructure(implBeanClass, pkg);
    }
    
    public boolean isFromWSDL(String serviceName) {
        return impl.isFromWSDL(serviceName);
    }
    
}
