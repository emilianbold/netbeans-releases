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
package org.netbeans.modules.groovy.grailsproject.ui;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import java.io.File;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;

/**
 *
 * @author Martin Adamek
 */
public final class TreeRootNode extends FilterNode implements PropertyChangeListener {

    private static Image PACKAGE_BADGE = ImageUtilities.loadImage("org/netbeans/modules/groovy/grailsproject/resources/packageBadge.gif"); // NOI18N
    private final SourceGroup g;

    public TreeRootNode(SourceGroup g, GrailsProject project) {
        this(DataFolder.findFolder(g.getRootFolder()), g, project);
        String pathName = g.getName();
        setShortDescription(pathName.substring(project.getProjectDirectory().getPath().length() + 1));
    }

    static String getDirName(SourceGroup g){
        // Source Groups always use a slash as file-separator, no matter
        // whether we are dealing with unix or windows:

        String pathName = g.getName();
        int lastSlash = pathName.lastIndexOf("/");
        String dirName = pathName.substring(lastSlash + 1);
        return dirName;
    }


    private TreeRootNode(DataFolder folder, SourceGroup g, GrailsProject project) {
        this(new FilterNode(folder.getNodeDelegate(), folder.createNodeChildren(new VisibilityQueryDataFilter(g))), g, project);
    }

    private TreeRootNode(Node originalNode, SourceGroup g, GrailsProject project) {
        super(originalNode, new PackageFilterChildren(originalNode),
                new ProxyLookup(
                originalNode.getLookup(),
                Lookups.fixed(  new PathFinder(g),  // no need for explicit search info
                                // Adding TemplatesImpl to Node's lookup to narrow-down
                                // number of displayed templates with the NewFile action.
                                // see # 122942
                                new TemplatesImpl(project, g)
                                )
                ));
        this.g = g;
        g.addPropertyChangeListener(WeakListeners.propertyChange(this, g));
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        return null;
    }

    /** Copied from PackageRootNode with modifications. */
    private Image computeIcon(boolean opened, int type) {
        Icon icon = g.getIcon(opened);
        if (icon == null) {
            Image image = opened ? super.getOpenedIcon(type) : super.getIcon(type);
            return ImageUtilities.mergeImages(image, PACKAGE_BADGE, 7, 7);
        } else {
            return ImageUtilities.icon2Image(icon);
        }
    }

    public Image getIcon(int type) {
        return computeIcon(false, type);
    }

    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }

    public String getName() {
        return g.getName();
    }

    public String getDisplayName() {
        return g.getDisplayName();
    }

    public boolean canRename() {
        return false;
    }

    public boolean canDestroy() {
        return false;
    }

    public boolean canCut() {
        return false;
    }

    public void propertyChange(PropertyChangeEvent ev) {
        // XXX handle SourceGroup.rootFolder change too
        fireNameChange(null, null);
        fireDisplayNameChange(null, null);
        fireIconChange();
        fireOpenedIconChange();
    }

    public static Node findPath(Node rootNode, Object object) {

        TreeRootNode.PathFinder pf = rootNode.getLookup().lookup(TreeRootNode.PathFinder.class);

        if (pf != null) {
            return pf.findPath(rootNode, object);
        } else {
            TreeRootNode.PathFinder pf2 = rootNode.getLookup().lookup(TreeRootNode.PathFinder.class);
            if (pf2 != null) {
                return pf2.findPath(rootNode, object);
            } else {
                return null;
            }
        }
    }

    /** Copied from PhysicalView and PackageRootNode. */
    public static final class PathFinder {

        private final SourceGroup g;

        PathFinder(SourceGroup g) {
            this.g = g;
        }

        public Node findPath(Node rootNode, Object o) {
            FileObject fo;
            if (o instanceof FileObject) {
                fo = (FileObject) o;
            } else if (o instanceof DataObject) {
                fo = ((DataObject) o).getPrimaryFile();
            } else {
                return null;
            }
            FileObject groupRoot = g.getRootFolder();
            if (FileUtil.isParentOf(groupRoot, fo) /* && group.contains(fo) */) {
                FileObject folder = fo.isFolder() ? fo : fo.getParent();
                String relPath = FileUtil.getRelativePath(groupRoot, folder);
                List<String> path = new ArrayList<String>();
                StringTokenizer strtok = new StringTokenizer(relPath, File.separator); // NOI18N
                while (strtok.hasMoreTokens()) {
                    String token = strtok.nextToken();
                    path.add(token);
                }
                try {
                    Node folderNode = folder.equals(groupRoot) ? rootNode : NodeOp.findPath(rootNode, Collections.enumeration(path));
                    if (fo.isFolder()) {
                        return folderNode;
                    } else {
                        Node[] childs = folderNode.getChildren().getNodes(true);
                        for (int i = 0; i < childs.length; i++) {
                            DataObject dobj = childs[i].getLookup().lookup(DataObject.class);
                            if (dobj != null && dobj.getPrimaryFile().getNameExt().equals(fo.getNameExt())) {
                                return childs[i];
                            }
                        }
                    }
                } catch (NodeNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (groupRoot.equals(fo)) {
                return rootNode;
            }
            return null;
        }
    }

    private static final class VisibilityQueryDataFilter implements ChangeListener, PropertyChangeListener, ChangeableDataFilter {

        private static final long serialVersionUID = 1L; // in case a DataFolder.ClonedFilterHandle saves me
        private final EventListenerList ell = new EventListenerList();
        private final SourceGroup g;

        public VisibilityQueryDataFilter(SourceGroup g) {
            this.g = g;
            VisibilityQuery.getDefault().addChangeListener(WeakListeners.change(this, VisibilityQuery.getDefault()));
            g.addPropertyChangeListener(WeakListeners.propertyChange(this, g));
        }

        public boolean acceptDataObject(DataObject obj) {
            FileObject fo = obj.getPrimaryFile();
            return g.contains(fo) && VisibilityQuery.getDefault().isVisible(fo);
        }

        public void stateChanged(ChangeEvent e) {
            fireChange();
        }

        public void propertyChange(PropertyChangeEvent e) {
            if (SourceGroup.PROP_CONTAINERSHIP.equals(e.getPropertyName())) {
                fireChange();
            }
        }

        private void fireChange() {
            Object[] listeners = ell.getListenerList();
            ChangeEvent event = null;
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ChangeListener.class) {
                    if (event == null) {
                        event = new ChangeEvent(this);
                    }
                    ((ChangeListener) listeners[i + 1]).stateChanged(event);
                }
            }
        }

        public void addChangeListener(ChangeListener listener) {
            ell.add(ChangeListener.class, listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            ell.remove(ChangeListener.class, listener);
        }
    }

    private static final class PackageFilterChildren extends FilterNode.Children {

        public PackageFilterChildren(final Node originalNode) {
            super(originalNode);
        }

        @Override
        protected Node copyNode(final Node originalNode) {
            DataObject dobj = originalNode.getLookup().lookup(DataObject.class);
            return (dobj instanceof DataFolder) ? new PackageFilterNode(originalNode) : super.copyNode(originalNode);
        }
    }

    private static final class PackageFilterNode extends FilterNode {

        public PackageFilterNode(final Node origNode) {
            super(origNode, new PackageFilterChildren(origNode));
        }

        @Override
        public void setName(final String name) {
            if (Utilities.isJavaIdentifier(name)) {
                super.setName(name);
            } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(TreeRootNode.class, "MSG_InvalidPackageName"), NotifyDescriptor.INFORMATION_MESSAGE));
            }
        }
    }
}
