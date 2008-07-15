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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.php.project;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.php.project.ui.actions.DebugSingleCommand;
import org.netbeans.modules.php.project.ui.actions.DownloadCommand;
import org.netbeans.modules.php.project.ui.actions.RunSingleCommand;
import org.netbeans.modules.php.project.ui.actions.UploadCommand;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.actions.FileSystemAction;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Radek Matous
 */
public class SourcesNodeFactory implements NodeFactory {
    private static final Logger LOGGER = Logger.getLogger(SourcesNodeFactory.class.getName());
    static final Image PACKAGE_BADGE = ImageUtilities.loadImage(
            "org/netbeans/modules/php/project/ui/resources/packageBadge.gif"); // NOI18N

    /** Creates a new instance of SourcesNodeFactory */
    public SourcesNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        PhpProject prj = p.getLookup().lookup(PhpProject.class);
        return new SourceChildrenList(prj);
    }

    private static class SourceChildrenList implements NodeList<SourceGroup>, ChangeListener {

        private java.util.Map<SourceGroup, PropertyChangeListener> groupsListeners;
        private final PhpProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public SourceChildrenList(PhpProject project) {
            this.project = project;
        }

        private Sources getSources() {
            return ProjectUtils.getSources(project);
        }

        public void addNotify() {
            getSources().addChangeListener(this);
        }

        public void removeNotify() {
            getSources().removeChangeListener(this);
        }

        public void stateChanged(ChangeEvent e) {
            // #132877 - discussed with tomas zezula
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    fireChange();
                }
            });
        }

        private void fireChange() {
            changeSupport.fireChange();
        }

        private DataFolder getFolder(FileObject fileObject) {
            if (fileObject != null && fileObject.isValid()) {
                try {
                    DataFolder dataFolder = DataFolder.findFolder(fileObject);
                    return dataFolder;
                } catch (Exception ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
            return null;
        }

        public List<SourceGroup> keys() {
            // update Sources listeners
            Sources sources = getSources();

            // parse SG
            // update SG listeners
            // XXX check if this is necessary
            final SourceGroup[] sourceGroups = Utils.getSourceGroups(project);
            final SourceGroup[] groups = new SourceGroup[sourceGroups.length];
            System.arraycopy(sourceGroups, 0, groups, 0, sourceGroups.length);

            List<SourceGroup> keysList = new ArrayList<SourceGroup>(groups.length);
            //Set<FileObject> roots = new HashSet<FileObject>();
            FileObject fileObject = null;
            for (int i = 0; i < groups.length; i++) {
                fileObject = groups[i].getRootFolder();
                DataFolder srcDir = getFolder(fileObject);

                if (srcDir != null) {
                    keysList.add(groups[i]);
                }
            //roots.add(fileObject);
            }
            return keysList;
        // Seems that we do not need to implement FileStatusListener
        // to listen to source groups root folders changes.
        // look at RubyLogicalViewRootNode for example.
        //updateSourceRootsListeners(roots);
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public Node node(SourceGroup key) {
            Node node = null;
            if (key != null) {
                DataFolder folder = getFolder(key.getRootFolder());
                if (folder != null) {
                    /* no need to use sourceGroup.getDisplayName() while we have only one sourceRoot.
                     * Now it contains not good-looking label.
                     * We put label there in PhpSources.configureSources()
                     */
                    //node = new SrcNode(folder, sourceGroup.getDisplayName());
                    node = new SrcNode(folder);
                }
            }
            return node;
        }

        private class SrcNode extends FilterNode {

            /**
             * creates source root node based on specified DataFolder.
             * Name is taken from bundle by 'LBL_PhpFiles' key.
             * <br/>
             * TODO : if we support several source roots, remove this constructor
             */
            SrcNode(DataFolder folder) {
                this(folder, NbBundle.getMessage(PhpLogicalViewProvider.class, "LBL_PhpFiles"));
            }

            /**
             * creates source root node based on specified DataFolder.
             * Uses specified name.
             */
            SrcNode(DataFolder folder, String name) {
                this(new FilterNode(folder.getNodeDelegate(), folder.createNodeChildren(new PhpSourcesFilter())), name);
            }

            private SrcNode(FilterNode node, String name) {
                super(node, new FolderChildren(node));
                disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME | DELEGATE_GET_SHORT_DESCRIPTION | DELEGATE_GET_ACTIONS);
                setDisplayName(name);
            }

            @Override
            public Image getIcon(int type) {
                return ImageUtilities.mergeImages(super.getIcon(type), PACKAGE_BADGE, 7, 7);
            }

            @Override
            public Image getOpenedIcon(int type) {
                return ImageUtilities.mergeImages(super.getOpenedIcon(type), PACKAGE_BADGE, 7, 7);
            }

            @Override
            public boolean canCopy() {
                return false;
            }

            @Override
            public boolean canCut() {
                return false;
            }

            @Override
            public boolean canRename() {
                return false;
            }

            @Override
            public boolean canDestroy() {
                return false;
            }

            @Override
            public Action[] getActions(boolean context) {
                PhpActionProvider provider = project.getLookup().lookup(PhpActionProvider.class);
                assert provider != null;
                Action[] actions = new Action[]{
                    CommonProjectActions.newFileAction(),
                    null,
                    provider.getAction(DownloadCommand.ID),
                    provider.getAction(UploadCommand.ID),
                    null,
                    SystemAction.get(FileSystemAction.class),
                    null,
                    SystemAction.get(FindAction.class),
                    null,
                    SystemAction.get(PasteAction.class),
                    null,
                    SystemAction.get(ToolsAction.class),
                    null,
                    CommonProjectActions.customizeProjectAction()
                };
                return actions;
            }
        }

        /**
         * Children for node that represents folder (SrcNode or PackageNode)
         */
        private static class FolderChildren extends FilterNode.Children {

            FolderChildren(final Node originalNode) {
                super(originalNode);
            }

            @Override
            protected Node[] createNodes(Node key) {
                return super.createNodes(key);
            }

            @Override
            protected Node copyNode(final Node originalNode) {
                DataObject dobj = originalNode.getLookup().lookup(DataObject.class);
                return (dobj instanceof DataFolder)
                        ? new PackageNode(originalNode)
                        : new ObjectNode(originalNode);
            }
        }

        private static final class PackageNode extends FilterNode {

            public PackageNode(final Node originalNode) {
                super(originalNode, new FolderChildren(originalNode));
            }

            @Override
            public Action[] getActions(boolean context) {
                return getOriginal().getActions(context);
            }
        }

        private static final class ObjectNode extends FilterNode {

            public ObjectNode(final Node originalNode) {
                super(originalNode);
            }

            @Override
            public Action[] getActions(boolean context) {
                List<Action> actions = new ArrayList<Action>();
                actions.addAll(Arrays.asList(getOriginal().getActions(context)));
                Action[] toAdd = new Action[]{
                    null,
                    ProjectSensitiveActions.projectCommandAction(RunSingleCommand.ID,
                    RunSingleCommand.DISPLAY_NAME, null),
                    ProjectSensitiveActions.projectCommandAction(DebugSingleCommand.ID,
                    DebugSingleCommand.DISPLAY_NAME, null)
                };
                int idx = actions.indexOf(SystemAction.get(PasteAction.class));
                for (int i = 0; i < toAdd.length; i++) {
                    if (idx >= 0 && idx + toAdd.length < actions.size()) {
                        //put on the proper place after paste
                        actions.add(idx + i + 1, toAdd[i]);
                    } else {
                        //else put at the tail
                        actions.add(toAdd[i]);
                    }
                }
                return actions.toArray(new Action[actions.size()]);
            }
        }

        private class PhpSourcesFilter implements ChangeListener, ChangeableDataFilter {

            private static final long serialVersionUID = -7439706583318056955L;
            private File projectXML = project.getHelper().resolveFile(AntProjectHelper.PROJECT_XML_PATH);
            private final EventListenerList ell = new EventListenerList();

            public PhpSourcesFilter() {
                VisibilityQuery.getDefault().addChangeListener(this);
            }

            public boolean acceptDataObject(DataObject object) {
                return isNotProjectFile(object) && VisibilityQuery.getDefault().isVisible(object.getPrimaryFile());
            }

            private boolean isNotProjectFile(DataObject object) {
                try {
                    if (projectXML != null) {
                        File f = FileUtil.toFile(object.getPrimaryFile()).getCanonicalFile();
                        File nbProject = projectXML.getParentFile().getCanonicalFile();
                        return nbProject != null && !nbProject.equals(f);
                    } else {
                        return true;
                    }
                } catch (IOException e) {
                    return false;
                }
            }

            public void stateChanged(ChangeEvent e) {
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
    }
}
