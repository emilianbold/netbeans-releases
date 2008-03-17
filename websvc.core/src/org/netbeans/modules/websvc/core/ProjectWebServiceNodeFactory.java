/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.websvc.core;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ajit Bhate
 */
public class ProjectWebServiceNodeFactory implements NodeFactory {

    /** Creates a new instance of ProjectWebServiceNodeFactory */
    public ProjectWebServiceNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        assert p != null;
        return new WsNodeList(p);
    }

    private static class WsNodeList implements NodeList<ProjectWebServiceView.ViewType>, ChangeListener, LookupListener {

        private Project project;
        private ChangeSupport changeSupport;
        private List<ProjectWebServiceView> views;
        private Node serviceNode,  clientNode;

        public WsNodeList(Project proj) {
            project = proj;
            changeSupport = new ChangeSupport(this);
        }

        public List<ProjectWebServiceView.ViewType> keys() {
            List<ProjectWebServiceView.ViewType> result = new ArrayList<ProjectWebServiceView.ViewType>();
            for (ProjectWebServiceView view : views) {
                if (!view.isViewEmpty(ProjectWebServiceView.ViewType.SERVICE)) {
                    result.add(ProjectWebServiceView.ViewType.SERVICE);
                    break;
                }
            }
            for (ProjectWebServiceView view : views) {
                if (!view.isViewEmpty(ProjectWebServiceView.ViewType.CLIENT)) {
                    result.add(ProjectWebServiceView.ViewType.CLIENT);
                    break;
                }
            }
            return result;
        }

        public synchronized void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public synchronized void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        private void fireChange() {
            changeSupport.fireChange();
        }

        public Node node(ProjectWebServiceView.ViewType key) {
            switch (key) {
                case SERVICE:
                    if (serviceNode == null) {
                        serviceNode = new WSRootNode(new Children(key), createLookup(project));
                        serviceNode.setDisplayName(NbBundle.getBundle(ProjectWebServiceNodeFactory.class).getString("LBL_WebServices"));
                    }
                    return serviceNode;
                case CLIENT:
                    if (clientNode == null) {
                        clientNode = new WSRootNode(new Children(key), createLookup(project));
                        clientNode.setDisplayName(NbBundle.getBundle(ProjectWebServiceNodeFactory.class).getString("LBL_ServiceReferences"));
                    }
                    return clientNode;
            }
            return null;
        }

        public void addNotify() {
            initViews();
            if (!views.isEmpty()) {
                for (ProjectWebServiceView view : views) {
                    view.addChangeListener(this, ProjectWebServiceView.ViewType.SERVICE);
                    view.addChangeListener(this, ProjectWebServiceView.ViewType.CLIENT);
                    view.addNotify();
                }
            }
            ProjectWebServiceView.getProviders().addLookupListener(this);
        }

        public void removeNotify() {
            if (!views.isEmpty()) {
                for (ProjectWebServiceView view : views) {
                    view.removeChangeListener(this, ProjectWebServiceView.ViewType.SERVICE);
                    view.removeChangeListener(this, ProjectWebServiceView.ViewType.CLIENT);
                    view.removeNotify();
                }
            }
            ProjectWebServiceView.getProviders().removeLookupListener(this);
            views.clear();
            views = null;
        }

        public void stateChanged(ChangeEvent e) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    fireChange();
                }
            });
        }

        public void resultChanged(LookupEvent ev) {
            initViews();
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    fireChange();
                    if (serviceNode != null) {
                        ((Children) serviceNode.getChildren()).updateKeys();
                    }
                }
            });
        }

        private void initViews() {
            views = ProjectWebServiceView.createWebServiceViews(project);
        }

        private static Lookup createLookup(Project project) {
            return Lookups.fixed(new Object[]{project, project.getProjectDirectory()});
        }

        private class Children extends org.openide.nodes.Children.Keys<ProjectWebServiceView> implements ChangeListener {

            private ProjectWebServiceView.ViewType viewType;

            public Children(ProjectWebServiceView.ViewType viewType) {
                super();
                this.viewType = viewType;
            }

            @Override
            protected Node[] createNodes(ProjectWebServiceView view) {
                return view.createView(viewType);
            }

            @Override
            protected void addNotify() {
                super.addNotify();
                if (views != null && !views.isEmpty()) {
                    setKeys(views);
                    for (ProjectWebServiceView view : views) {
                        view.addChangeListener(this, viewType);
                    }
                } else {
                    setKeys(Collections.<ProjectWebServiceView>emptyList());
                }
            }

            @Override
            protected void removeNotify() {
                super.removeNotify();
                setKeys(Collections.<ProjectWebServiceView>emptyList());
                for (ProjectWebServiceView view : views) {
                    view.removeChangeListener(this, viewType);
                }
            }

            public void stateChanged(final ChangeEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        if (views.contains(e.getSource())) {
                            refreshKey((ProjectWebServiceView) e.getSource());
                        }
                    }
                });
            }

            private void updateKeys() {
                if (!isInitialized()) {
                    return;
                }
                if (views != null && !views.isEmpty()) {
                    setKeys(views);
                } else {
                    setKeys(Collections.<ProjectWebServiceView>emptyList());
                }
            }
        }
    }

    private static class WSRootNode extends AbstractNode {

        private static final String SERVICES_BADGE = "org/netbeans/modules/websvc/core/webservices/ui/resources/webservicegroup.png"; // NOI18N

        private Icon folderIconCache;
        private Icon openedFolderIconCache;
        private Image cachedServicesBadge;

        public WSRootNode(WsNodeList.Children children, Lookup lookup) {
            super(children, lookup);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{
                        CommonProjectActions.newFileAction(),
                        null,
                        SystemAction.get(FindAction.class),
                        null,
                        SystemAction.get(PasteAction.class),
                        null,
                        SystemAction.get(PropertiesAction.class)
                    };
        }

        @Override
        public Image getIcon(int type) {
            return computeIcon(false);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return computeIcon(true);
        }

        private java.awt.Image getServicesImage() {
            if (cachedServicesBadge == null) {
                cachedServicesBadge = Utilities.loadImage(SERVICES_BADGE);
            }
            return cachedServicesBadge;
        }

        /**
         * Returns Icon of folder on active platform
         * @param opened should the icon represent opened folder
         * @return the folder icon
         */
        private Icon getFolderIcon(boolean opened) {
            if (openedFolderIconCache == null) {
                Node n = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
                openedFolderIconCache = new ImageIcon(n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16));
                folderIconCache = new ImageIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            }
            if (opened) {
                return openedFolderIconCache;
            } else {
                return folderIconCache;
            }
        }

        private Image computeIcon(boolean opened) {
            Icon icon = getFolderIcon(opened);
            Image image = ((ImageIcon) icon).getImage();
            image = Utilities.mergeImages(image, getServicesImage(), 7, 7);
            return image;
        }
    }
}
