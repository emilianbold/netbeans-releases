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
import org.netbeans.modules.websvc.webservices.WebServicesSupportAccessor;
import org.netbeans.modules.websvc.spi.webservices.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;

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
    
    public void addServiceImpl(String serviceName, String serviceEndpointInterface, String serviceEndpoint, FileObject configFile) {
        impl.addServiceImpl(serviceName, serviceEndpointInterface, serviceEndpoint, configFile);
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
    
    public void removeServiceEntry(String serviceName, String linkName) {
        impl.removeServiceEntry(serviceName, linkName);
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return impl.getAntProjectHelper();
    }
    
    public String  generateImplementationBean(String name, FileObject pkg, Project project) throws java.io.IOException {
        return impl.generateImplementationBean(name, pkg, project);
    }
    
    public void addServiceImplLinkEntry(ServiceImplBean serviceImplBean, String wsName) {
        impl.addServiceImplLinkEntry(serviceImplBean, wsName);
    }
    
    
/* !! What to put here?
 *
        public boolean equals (Object obj) {
        if (!WebModule.class.isAssignableFrom(obj.getClass()))
            return false;
        WebModule wm = (WebModule) obj;
        return getDocumentBase().equals(wm.getDocumentBase())
            && getJ2eePlatformVersion().equals (wm.getJ2eePlatformVersion())
            && getContextPath().equals(wm.getContextPath());
    }
 
    public int hashCode () {
        return getDocumentBase ().getPath ().length () + getContextPath ().length ();
    }
 */
}
