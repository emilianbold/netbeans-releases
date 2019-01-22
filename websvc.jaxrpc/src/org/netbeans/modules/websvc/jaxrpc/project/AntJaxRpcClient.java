/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.jaxrpc.project;


import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;
import org.netbeans.modules.websvc.api.registry.WebServicesRegistryView;
import org.netbeans.modules.websvc.jaxrpc.nodes.ServiceClientNode;
import org.netbeans.modules.websvc.project.api.ServiceDescriptor;
import org.netbeans.modules.websvc.project.api.WebService;
import org.netbeans.modules.websvc.project.api.WebService.Type;
import org.netbeans.modules.websvc.project.spi.WebServiceImplementation;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * 
 */
public class AntJaxRpcClient implements WebServiceImplementation {

    private FileObject wsdlFo;
    private Project prj;
    private WebServicesClientSupport wscs;

    /** Constructor.
     *
     * @param service JaxWsService
     * @param prj project
     */
    public AntJaxRpcClient(Project prj, WebServicesClientSupport wscs,  FileObject wsdlFo) {
        this.wsdlFo = wsdlFo;
        this.prj = prj;
    }

    public String getIdentifier() {
         return wsdlFo.getName();
    }

    public boolean isServiceProvider() {
        return false;
    }

    public Type getServiceType() {
        return WebService.Type.SOAP;
    }

    public ServiceDescriptor getServiceDescriptor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Node createNode() {
        try {
            DataObject dobj = DataObject.find(wsdlFo);
            if (!isClient(wscs, dobj)) {
                return null;
            }
            Node origNode = dobj.getNodeDelegate();
            WebServicesRegistryView registryView = (WebServicesRegistryView) Lookup.getDefault().lookup(WebServicesRegistryView.class);
            Node[] serviceNodes = registryView.getWebServiceNodes(dobj.getPrimaryFile());
            if (serviceNodes != null) {
                return new ServiceClientNode(origNode, serviceNodes[0]);
//                for (int i = 0; i < serviceNodes.length; i++) {
//                    nodes.add(new ServiceClientNode(origNode, serviceNodes[i]));
//                }
            } else {
                final FileObject wsdlFileObject = dobj.getPrimaryFile();
                final WebServicesRegistryView regView = registryView;
                Thread registerThread = new Thread(new Runnable() {

                    public void run() {
                        regView.registerService(wsdlFileObject, true);
                    }
                }, "RegisterWSClient " + wsdlFileObject.getNameExt()); // NOI18N

                registerThread.start();

                return new ServiceClientNode(origNode, null);
            }
        } catch (DataObjectNotFoundException e) {
        }
        return null;
    }

    private static boolean isClient(WebServicesClientSupport wscs, DataObject dobj) {
        FileObject wsdlFileObject = dobj.getPrimaryFile();
        for (Object obj : wscs.getServiceClients()) {
            WsCompileClientEditorSupport.ServiceSettings wsClient = (WsCompileClientEditorSupport.ServiceSettings) obj;
            if (wsdlFileObject.getName().equals(wsClient.getServiceName())) {
                return true;
            }
        }
        return false;
    }

}
