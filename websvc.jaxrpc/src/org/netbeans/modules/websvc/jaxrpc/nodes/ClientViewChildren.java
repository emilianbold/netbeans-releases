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

package org.netbeans.modules.websvc.jaxrpc.nodes;

import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

import org.netbeans.modules.websvc.api.registry.WebServicesRegistryView;

/** Displays web service client nodes representing the web services for which 
 *  this module has been enabled to use as a client.  Driven by existence of
 *  wsdl files in the wsdl folder of the owner module.
 *
 * @author Peter Williams
 */
public class ClientViewChildren extends FilterNode.Children {

    public ClientViewChildren(FileObject wsdlFolder) throws DataObjectNotFoundException {
        super(DataObject.find(wsdlFolder).getNodeDelegate());
    }

    protected Node[] createNodes(Node origNode) {
        Node [] results = new Node[0]; // Default return value is no nodes created.

        DataObject wsdlDataObject = (DataObject)origNode.getCookie(DataObject.class);

        if(wsdlDataObject != null && isClient(wsdlDataObject)) {
            WebServicesRegistryView registryView = (WebServicesRegistryView) Lookup.getDefault().lookup(WebServicesRegistryView.class);
            Node [] serviceNodes = registryView.getWebServiceNodes(wsdlDataObject.getPrimaryFile());
            if (serviceNodes != null) {
                results = new Node[serviceNodes.length];
                for(int i = 0; i < serviceNodes.length; i++) {
                    results[i] = new ServiceClientNode(origNode, serviceNodes[i]);
                }
            } else {
                results = new Node [] { new ServiceClientNode(origNode, null) };
                
                final FileObject wsdlFileObject = wsdlDataObject.getPrimaryFile();
                final WebServicesRegistryView regView = registryView;
                Thread registerThread = new Thread(new Runnable() {
                    public void run() {
                        regView.registerService(wsdlFileObject, true);
                    }
                }, "RegisterWSClient " + wsdlFileObject.getNameExt()); // NOI18N
                registerThread.start();
            }
        }

        return results;
    }

    /** We assume that any wsdl file in the source wsdl folder is a client wsdl
     *  except when there exists a mapping file in the parent ddfolder (which is
     *  either WEB-INF or META-INF).  In this case, the wsdl should be for a service
     *  that is being developed from wsdl rather than from SEI.
     *
     *  A more stable way would be to only reference clients mentioned in project.xml
     *  but that was a harder list to get at the time (easier now because there
     *  is actually an API for it.
     */
    private boolean isClient(DataObject dobj){
        FileObject wsdlFileObject = dobj.getPrimaryFile();
        WebServicesClientSupport wss = WebServicesClientSupport.getWebServicesClientSupport(wsdlFileObject);
        List wsClients = wss.getServiceClients();
        Iterator i = wsClients.iterator();
        while (i.hasNext()) {
            WsCompileClientEditorSupport.ServiceSettings wsClient = (WsCompileClientEditorSupport.ServiceSettings)i.next();
            String wsdlFileName = wsdlFileObject.getName();
            if (wsdlFileName.equals(wsClient.getServiceName())) {
                return true;
            }
        }
        return false;
    }
}
