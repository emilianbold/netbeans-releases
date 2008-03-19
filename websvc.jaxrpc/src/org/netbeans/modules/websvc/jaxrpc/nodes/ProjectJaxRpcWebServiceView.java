/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.websvc.jaxrpc.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.core.ProjectWebServiceView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.netbeans.modules.j2ee.dd.api.webservices.DDProvider;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;
import org.netbeans.modules.websvc.api.registry.WebServicesRegistryView;
import org.netbeans.modules.websvc.core.AbstractProjectWebServiceViewImpl;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 *
 * @author Ajit
 */
final class ProjectJaxRpcWebServiceView extends AbstractProjectWebServiceViewImpl {

    private static final String WS_DD = "webservices.xml"; //NOI18N

    private static final String WSDL_EXT = "wsdl"; //NOI18N

    private WebServicesSupport wss;
    private WebServicesClientSupport wscs;
    boolean wsdlFolderCreated = false;
    private final FileChangeListener wsdlCreationListener;
    private final FileChangeListener wsddCreationListener;
    private final FileChangeListener projectXmlListener;
    private final PropertyChangeListener wsddListener;
    private int noOfClients = -1;

    ProjectJaxRpcWebServiceView(Project p) {
        super(p);
        FileObject projectDir = p.getProjectDirectory();
        wss = WebServicesSupport.getWebServicesSupport(projectDir);
        wscs = WebServicesClientSupport.getWebServicesClientSupport(projectDir);
        projectXmlListener = new ProjectXmlListener();
        wsddCreationListener = new WSDDCreationListener();
        wsddListener = new WSDDListener();
        wsdlCreationListener = new WsdlCreationListener();
    }

    public Node[] createView(ProjectWebServiceView.ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                return createServiceNodes();
            case CLIENT:
                return createClientNodes();
        }
        return new Node[0];
    }

    private Node[] createServiceNodes() {
        if(wss==null) {
            return new Node[0];
        }
        try {
            Webservices webServices = DDProvider.getDefault().getDDRoot(wss.getWebservicesDD());
            if (webServices == null) {
                return new Node[0];
            }
            ArrayList<FileObject> roots = new ArrayList<FileObject>();
            SourceGroup[] groups = ProjectUtils.getSources(getProject()).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (groups != null) {
                for (SourceGroup group : groups) {
                    roots.add(group.getRootFolder());
                }
            }
            if (roots == null || roots.isEmpty()) {
                return new Node[0];
            }
            ArrayList<Node> nodes = new ArrayList<Node>();
            for (WebserviceDescription description : webServices.getWebserviceDescription()) {
                for (FileObject srcRoot : roots) {
                    FileObject implBean = getImplBeanClass(srcRoot, description);
                    if (implBean != null) {
                        nodes.add(new WebServiceNode(webServices, description, srcRoot, implBean));
                        break;
                    }
                }
            }
            return nodes.toArray(new Node[nodes.size()]);
        } catch (IOException ex) {
        }

        return new Node[0];
    }

    private Node[] createClientNodes() {
        if(wscs==null) {
            return new Node[0];
        }
        ArrayList<Node> nodes = new ArrayList<Node>();
        FileObject wsdlFolder = wscs.getWsdlFolder();
        FileObject[] wsdls = wsdlFolder.getChildren();
        for (FileObject wsdl : wsdls) {
            if (!WSDL_EXT.equals(wsdl.getExt())) {
                continue;
            }
            try {
                DataObject dobj = DataObject.find(wsdl);
                if (!isClient(wscs, dobj)) {
                    continue;
                }
                Node origNode = dobj.getNodeDelegate();
                WebServicesRegistryView registryView = (WebServicesRegistryView) Lookup.getDefault().lookup(WebServicesRegistryView.class);
                Node[] serviceNodes = registryView.getWebServiceNodes(dobj.getPrimaryFile());
                if (serviceNodes != null) {
                    for (int i = 0; i < serviceNodes.length; i++) {
                        nodes.add(new ServiceClientNode(origNode, serviceNodes[i]));
                    }
                } else {
                    nodes.add(new ServiceClientNode(origNode, null));
                    final FileObject wsdlFileObject = dobj.getPrimaryFile();
                    final WebServicesRegistryView regView = registryView;
                    Thread registerThread = new Thread(new Runnable() {

                        public void run() {
                            regView.registerService(wsdlFileObject, true);
                        }
                    }, "RegisterWSClient " + wsdlFileObject.getNameExt()); // NOI18N

                    registerThread.start();
                }
            } catch (DataObjectNotFoundException e) {
            }
        }
        return nodes.toArray(new Node[nodes.size()]);
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

    public boolean isViewEmpty(ProjectWebServiceView.ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                return wss == null || wss.getServices().isEmpty();
            case CLIENT:
                if (wscs == null || wscs.getServiceClients().isEmpty()) {
                    return true;
                } else {
                    FileObject wsdlFolder = wscs.getWsdlFolder();
                    if (wsdlFolder != null) {
                        for (FileObject child : wsdlFolder.getChildren()) {
                            if (child.getExt().equalsIgnoreCase(WSDL_EXT)) { //NOI18N

                                return false;
                            }
                        }
                    }
                    return true;
                }
        }
        return true;
    }

    public void addNotify() {
        if (wss != null) {
            FileObject wsddFolder = wss.getWsDDFolder();
            if (wsddFolder != null) {
                wsddFolder.addFileChangeListener(WeakListeners.create(FileChangeListener.class, wsddCreationListener, wsddFolder));
                try {
                    Webservices webServices = DDProvider.getDefault().getDDRoot(wss.getWebservicesDD());
                    if (webServices != null) {
                        webServices.addPropertyChangeListener(WeakListeners.propertyChange(wsddListener, webServices));
                    }
                } catch (IOException ex) {
                }
            }
        }
        if (wscs != null) {
            FileObject prjXml = getProject().getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH);
            if (prjXml != null) {
                prjXml.addFileChangeListener(FileUtil.weakFileChangeListener(projectXmlListener, prjXml));
            }
            FileObject wsdlFolder = wscs.getWsdlFolder();
            if (wsdlFolder != null) {
                wsdlFolder.addFileChangeListener(WeakListeners.create(FileChangeListener.class, wsdlCreationListener, wsdlFolder));
                wsdlFolderCreated = true;
            }
        }
    }

    public void removeNotify() {
        if (wss != null) {
            FileObject wsddFolder = wss.getWsDDFolder();
            if (wsddFolder != null) {
                if (wsddCreationListener != null) {
                    wsddFolder.removeFileChangeListener(wsddCreationListener);
                }
                try {
                    Webservices webServices = DDProvider.getDefault().getDDRoot(wss.getWebservicesDD());
                    if (webServices != null && wsddListener != null) {
                        webServices.removePropertyChangeListener(wsddListener);
                    }
                } catch (IOException ex) {
                }
            }
        }
        if (wscs != null) {
            FileObject prjXml = getProject().getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH);
            if (prjXml != null) {
                prjXml.removeFileChangeListener(projectXmlListener);
            }
            FileObject wsdlFolder = wscs.getWsdlFolder();
            if (wsdlFolder != null) {
                wsdlFolder.removeFileChangeListener(wsdlCreationListener);
                wsdlFolderCreated = false;
            }
        }
    }

    private static FileObject getImplBeanClass(FileObject srcRoot, WebserviceDescription webServiceDescription) {
        PortComponent portComponent = webServiceDescription.getPortComponent(0); //assume one port per ws

        ServiceImplBean serviceImplBean = portComponent.getServiceImplBean();
        String link = serviceImplBean.getServletLink();
        if (link == null) {
            link = serviceImplBean.getEjbLink();
        }
        WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(srcRoot);
        String implBean = wsSupport.getImplementationBean(link);
        return srcRoot.getFileObject(implBean.replace('.', '/').concat(".java"));
    }

    private final class WSDDCreationListener extends FileChangeAdapter {

        @Override
        public void fileDataCreated(FileEvent fe) {
            if (WS_DD.equalsIgnoreCase(fe.getFile().getNameExt())) {
                fireChange(ProjectWebServiceView.ViewType.SERVICE);
                try {
                    Webservices webServices = DDProvider.getDefault().getDDRoot(wss.getWebservicesDD());
                    if (webServices != null) {
                        webServices.addPropertyChangeListener(wsddListener);
                    }
                } catch (IOException ex) {
                }
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (WS_DD.equalsIgnoreCase(fe.getFile().getNameExt())) {
                fireChange(ProjectWebServiceView.ViewType.SERVICE);
            }
        }
    }

    private final class WsdlCreationListener extends FileChangeAdapter {

        @Override
        public void fileDataCreated(FileEvent fe) {
            if (WSDL_EXT.equalsIgnoreCase(fe.getFile().getExt())) {
                fireChange(ProjectWebServiceView.ViewType.CLIENT);
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (WSDL_EXT.equalsIgnoreCase(fe.getFile().getExt())) {
                fireChange(ProjectWebServiceView.ViewType.CLIENT);
            } else if (fe.getFile().isFolder() && WSDL_EXT.equals(fe.getFile().getName())) {
                fireChange(ProjectWebServiceView.ViewType.CLIENT);
            }
        }
    }

    private final class WSDDListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            fireChange(ProjectWebServiceView.ViewType.SERVICE);
        }
    }

    private final class ProjectXmlListener extends FileChangeAdapter {

        @Override
        public void fileChanged(FileEvent fe) {
            int newNoOfClients = wscs.getServiceClients().size();
            if (newNoOfClients != noOfClients) {
                noOfClients = newNoOfClients;
                fireChange(ProjectWebServiceView.ViewType.CLIENT);
            }
            if (!wsdlFolderCreated) {
                FileObject wsdlFolder = wscs.getWsdlFolder();
                if (wsdlFolder != null) {
                    wsdlFolder.addFileChangeListener(WeakListeners.create(FileChangeListener.class, wsdlCreationListener, wsdlFolder));
                    wsdlFolderCreated = true;
                }
            }
        }
    }
}
