/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectType;
import org.netbeans.modules.web.clientproject.remote.RemoteFS;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class ClientSideProjectLogicalView implements LogicalViewProvider {

    private ClientSideProject project;

    public ClientSideProjectLogicalView(ClientSideProject project) {
        this.project = project;
    }

    @Override
    public Node createLogicalView() {
        try {
            FileObject root = project.getProjectDirectory();

            DataFolder df =
                    DataFolder.findFolder(root);

            Node node = df.getNodeDelegate();

            return new ClientSideProjectNode(node, project);

        } catch (DataObjectNotFoundException e) {
            Exceptions.printStackTrace(e);
            return new AbstractNode(Children.LEAF);
        }
    }

    @Override
    public Node findPath(Node root, Object target) {
        return null;
    }
    
    
/** This is the node you actually see in the project tab for the project */
    private static final class ClientSideProjectNode extends FilterNode {

        final ClientSideProject project;

        public ClientSideProjectNode(Node node, ClientSideProject project) throws DataObjectNotFoundException {
            super(node, new ClientSideProjectChildren(project, node),
                    //The projects system wants the project in the Node's lookup.
                    //NewAction and friends want the original Node's lookup.
                    //Make a merge of both
                    new ProxyLookup(new Lookup[]{Lookups.singleton(project),
                        node.getLookup()
                    }));
            this.project = project;
        }

        @Override
        public Action[] getActions(boolean arg0) {
            return CommonProjectActions.forType(ClientSideProjectType.TYPE);
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage("org/netbeans/modules/web/clientproject/ui/resources/projecticon.png");
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getDisplayName() {
            return project.getProjectDirectory().getName();
        }

    }    
    
    private static class ClientSideProjectChildren extends Children.Keys {

        private static String REMOTE_FILES = "remote.files";
        
        private ClientSideProject project;
        private Node folderNode;

        public ClientSideProjectChildren(ClientSideProject project, Node folderNode) {
            this.project = project;
            this.folderNode = folderNode;
            this.folderNode.addNodeListener(new NodeListener() {
                @Override
                public void childrenAdded(NodeMemberEvent ev) {
                    updateKeys();
                }

                @Override
                public void childrenRemoved(NodeMemberEvent ev) {
                    updateKeys();
                }

                @Override
                public void childrenReordered(NodeReorderEvent ev) {
                    updateKeys();
                }

                @Override
                public void nodeDestroyed(NodeEvent ev) {
                }

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                }
            });
            project.getRemoteFiles().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    updateKeys();
                }
            });
        }

        @Override
        protected Node[] createNodes(Object k) {
            if (REMOTE_FILES.equals(k)) {
                return new Node[]{new RemoteFilesNode(project)};
            } else {
                Node key = (Node)k;
                if (key.getDisplayName().equals("nbproject")) {
                    return new Node[0];
                }
                // Hides build folder
                // TODO: need an API?
                if(key.getDisplayName().equals("build")) {
                    return new Node[0];
                }
                return new Node[]{key.cloneNode()};
            }
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            updateKeys();
        }
        
        private void updateKeys() {
            ArrayList keys = new ArrayList();
            if (!project.getRemoteFiles().getRemoteFiles().isEmpty()) {
                keys.add(REMOTE_FILES);
            }
            keys.addAll(Arrays.asList(folderNode.getChildren().getNodes(false)));
            setKeys(keys);
        }
        
    }
    
    @NbBundle.Messages("LBL_RemoteFiles=Remote Files")
    private static final class RemoteFilesNode extends AbstractNode {

        final ClientSideProject project;

        public RemoteFilesNode(ClientSideProject project) {
            super(new RemoteFilesChildren(project));
            this.project = project;
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage("org/netbeans/modules/web/clientproject/ui/resources/remotefiles.png");
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getDisplayName() {
            return Bundle.LBL_RemoteFiles();
        }

    }    
    
    private static class RemoteFilesChildren extends Children.Keys<URL> implements ChangeListener {

        final ClientSideProject project;

        public RemoteFilesChildren(ClientSideProject project) {
            this.project = project;
        }
        
        @Override
        protected Node[] createNodes(URL key) {
            FileObject fo = RemoteFS.getDefault().getFileForURL(key);
            DataObject dobj;
            try {
                dobj = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                return new Node[] {};
            }
            return new Node[] { dobj.getNodeDelegate() };
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            project.getRemoteFiles().addChangeListener(this);
            updateKeys();
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            project.getRemoteFiles().removeChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            updateKeys();
        }
        
        private void updateKeys() {
            List<URL> remoteFiles = project.getRemoteFiles().getRemoteFiles();
            if (remoteFiles.size() > 1) {
                remoteFiles = new ArrayList(remoteFiles);
                Collections.sort(remoteFiles, new Comparator<URL>() {
                    @Override
                    public int compare(URL o1, URL o2) {
                        String p1 = o1.getPath();
                        String p2 = o2.getPath();
                        int i = p1.lastIndexOf('/');
                        if (i > 0) {
                            p1 = p1.substring(i+1);
                        }
                        i = p2.lastIndexOf('/');
                        if (i > 0) {
                            p2 = p2.substring(i+1);
                        }
                        return p1.compareToIgnoreCase(p2);
                    }
                });
            }
            setKeys(remoteFiles);
        }

    }
    
}
