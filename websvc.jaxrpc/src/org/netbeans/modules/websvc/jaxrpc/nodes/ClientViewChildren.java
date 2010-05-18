/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.websvc.jaxrpc.nodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

import org.netbeans.modules.websvc.api.registry.WebServicesRegistryView;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileUtil;

/** Displays web service client nodes representing the web services for which 
 *  this module has been enabled to use as a client.  Driven by existence of
 *  wsdl files in the wsdl folder of the owner module.
 *
 * @author Peter Williams
 */
public class ClientViewChildren extends FilterNode.Children {
    FileObject wsdlFolder;
    private final ProjectXmlListener projectXmlListener;
    
    public ClientViewChildren(FileObject wsdlFolder) throws DataObjectNotFoundException {
        super(DataObject.find(wsdlFolder).getNodeDelegate());
        this.wsdlFolder = wsdlFolder;
        this.projectXmlListener = new ProjectXmlListener();
    }
    
    protected void addNotify(){
        super.addNotify();
        updateKeys();
        FileObject prjXml = FileOwnerQuery.getOwner(wsdlFolder).getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH);
        if(prjXml!=null) {
            prjXml.addFileChangeListener(FileUtil.weakFileChangeListener(projectXmlListener, prjXml) );
        }
    }

    private void updateKeys(){
        List<Node> children = new ArrayList<Node>();
        try {
            //we need to set the keys as empty children and 
            //then as right children for refresh to work
            this.setKeys(children);
            FileObject[] wsdls = wsdlFolder.getChildren();
            for(int i = 0; i < wsdls.length; i++){
                Node n = DataObject.find(wsdls[i]).getNodeDelegate();
                children.add(n);
            }
            this.setKeys(children);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
        }
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

        private final class ProjectXmlListener extends FileChangeAdapter {
            public void fileChanged(FileEvent fe) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        updateKeys();
                    }
                });
            }
        }
            
}
