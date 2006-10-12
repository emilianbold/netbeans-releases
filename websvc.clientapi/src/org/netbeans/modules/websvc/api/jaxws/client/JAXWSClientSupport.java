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

package org.netbeans.modules.websvc.api.jaxws.client;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.websvc.jaxws.client.JAXWSClientSupportAccessor;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl;

import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportProvider;
import org.openide.nodes.Node;

/** WebServicesClientSupport should be used to manipulate a projects representation
 *  of a web service implementation.
 * <p>
 * A client may obtain a WebServicesClientSupport instance using
 * <code>WebServicesClientSupport.getWebServicesClientSupport(fileObject)</code> static
 * method, for any FileObject in the project directory structure.
 *
 * @author Peter Williams
 */
public final class JAXWSClientSupport {
    
    public static final String WSCLIENTUPTODATE_CLASSPATH = "wsclientuptodate.classpath";

    private JAXWSClientSupportImpl impl;
    private static final Lookup.Result implementations =
        Lookup.getDefault().lookup(new Lookup.Template(WebServicesClientSupportProvider.class));

    static  {
        JAXWSClientSupportAccessor.DEFAULT = new JAXWSClientSupportAccessor() {
            public JAXWSClientSupport createJAXWSClientSupport(JAXWSClientSupportImpl spiWebServicesClientSupport) {
                return new JAXWSClientSupport(spiWebServicesClientSupport);
            }

            public JAXWSClientSupportImpl getJAXWSClientSupportImpl(JAXWSClientSupport wscs) {
                return wscs == null ? null : wscs.impl;
            }
        };
    }

    private JAXWSClientSupport(JAXWSClientSupportImpl impl) {
        if (impl == null)
            throw new IllegalArgumentException ();
        this.impl = impl;
    }

    /** Find the WebServicesClientSupport for given file or null if the file does
     *  not belong to any module supporting web service clients.
     */
    public static JAXWSClientSupport getJaxWsClientSupport (FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to JAXWSClientSupport.getJAXWSClientSupport(FileObject)"); // NOI18N
        }
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            WebServicesClientSupportProvider impl = (WebServicesClientSupportProvider)it.next();
            JAXWSClientSupport wscs = impl.findJAXWSClientSupport (f);
            if (wscs != null) {
                return wscs;
            }
        }
        return null;
    }

    // Delegated methods from WebServicesClientSupportImpl

    /** Adds a service client to the module represented by this support object.
     *
     * 1. Add appropriate entries to project.xml and project.properties to add
     *    this service client to the build. Web/project implementation added
     *    wscompile targets directly to the build-impl.xsl script and adds some
     *    entries to project.xml to drive those fragments.
     * 2. Regenerate build-impl.xml (For web/project, this happens automatically
     *    when the modified project.xml/project.properties is saved.)
     * 3. Add J2EE Platform support
     * 4. Code completion source registration for generated interface files? (So
     *    the user can type "TemperatureService." and have the list of port methods
     *    show up.) This was implemented independent of web services by adding
     *    the build.classes.dir to the SourceForBinaryQuery classpath.
     * 5. DELETED add service-ref to module deployment descriptor
     *
     * @param serviceName name of this service (as specified in wsdl file.)
     * @param configFile config file for use by wscompile target
     */
    public String addServiceClient(String serviceName, String wsdlUrl, String packageName, boolean isJsr109) {
        return impl.addServiceClient(serviceName, wsdlUrl, packageName, isJsr109);
    }
    
    
    /**  Removes a service client from the module represented by this support object.
     *
     * 1. Removes everything associated with this service that was added in
     *    addServiceClient, assuming it is not needed by another service client.
     * 2. Anything specific only to this service should be removed.
     * 3. Anything specific to web service clients in general should be removed
     *    if there are no other clients, e.g. library support.
     * 4. Note there are a few items that are shared between web service
     *    implementations and web service clients. These items should only be
     *    removed if there are no services OR clients in the project after this
     *    action is performed.
     *
     * @param serviceName name of this service (as specified in wsdl file).
     */
    public void removeServiceClient(String serviceName) {
        impl.removeServiceClient(serviceName);
    }

    /** Get the WSDL folder (where WSDL files are to be stored) for this module.
     *
     * 1. Return the source folder where wsdl files for the services used by the
     *    client are to be stored. For web project, this is WEB-INF/wsdl
     * 2. Should this method return a higher level folder type? (if so, what
     *    would that type be? DataFolder?)
     * 3. Note that this is referring to the source folder, thus allowing freeform
     *    project to let the user set this if they want. For the build directory,
     *    wsdl location is enforced by J2EE 1.4 container to be WEB-INF/wsdl or
     *    META-INF/wsdl.
     *
     * @param create set to true if the folder should be created if it does not exist.
     * @return FileObject representing this folder.
     * @exception IOException if there is a problem accessing or creating the folder
     */
    public FileObject getWsdlFolder(boolean create) throws IOException {
        return impl.getWsdlFolder(create);
    }
    
    /**
     *  return folder for local wsdl artifacts
     */
    public FileObject getLocalWsdlFolderForClient(String clientName, boolean createFolder) {
        return impl.getLocalWsdlFolderForClient(clientName,createFolder);
    }
    
    /**
     *  return folder for local wsdl bindings
     */
    public FileObject getBindingsFolderForClient(String clientName, boolean createFolder) {
        return impl.getBindingsFolderForClient(clientName,createFolder);
    }
    
    /** returns the URL of catalog file in project
     */
    public URL getCatalog() {
        return impl.getCatalog();
    }
    
    public List/*Client*/ getServiceClients() {
        return impl.getServiceClients();
    }
    
     public String getServiceRefName(Node clientNode){
         return impl.getServiceRefName(clientNode);
     }
}