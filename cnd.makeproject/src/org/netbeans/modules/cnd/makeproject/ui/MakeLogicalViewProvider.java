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
package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.Image;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider.Delta;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.BrokenLinks.BrokenLink;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup.Template;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.InstanceContent;

/**
 * Support for creating logical views.
 */
public class MakeLogicalViewProvider implements LogicalViewProvider {

    private static final String brokenLinkBadgePath = "org/netbeans/modules/cnd/makeproject/ui/resources/brokenProjectBadge.gif"; // NOI18N
    private static final String brokenProjectBadgePath = "org/netbeans/modules/cnd/makeproject/ui/resources/brokenProjectBadge.gif"; // NOI18N
    private static final String brokenIncludeImgPath = "org/netbeans/modules/cnd/makeproject/ui/resources/brokenIncludeBadge.png"; // NOI18N
    static final Image brokenLinkBadge = loadToolTipImage(brokenLinkBadgePath, "BrokenLinkTxt"); // NOI18N
    static final Image brokenProjectBadge = loadToolTipImage(brokenProjectBadgePath, "BrokenProjectTxt"); // NOI18N
    static final Image brokenIncludeBadge = loadToolTipImage(brokenIncludeImgPath, "BrokenIncludeTxt"); // NOI18N
    static final String SUBTYPE = "x-org-netbeans-modules-cnd-makeproject-uidnd"; // NOI18N
    static final String SUBTYPE_FOLDER = "x-org-netbeans-modules-cnd-makeproject-uidnd-folder"; // NOI18N
    static final String MASK = "mask"; // NOI18N
    final static RequestProcessor ANNOTATION_RP = new RequestProcessor("MakeLogicalViewProvider.AnnotationUpdater", 10); // NOI18N
    private final MakeProject project;
    private MakeLogicalViewRootNode projectRootNode;

    public MakeLogicalViewProvider(MakeProject project) {
        this.project = project;
        assert project != null;
    }

    @Override
    public Node createLogicalView() {
        if (gotMakeConfigurationDescriptor()) {
            MakeConfigurationDescriptor configurationDescriptor = getMakeConfigurationDescriptor();
            if (configurationDescriptor == null || configurationDescriptor.getState() == State.BROKEN || configurationDescriptor.getConfs().size() == 0) {
                return new MakeLogicalViewRootNodeBroken(project);
            } else {
                createRoot(configurationDescriptor);
                return projectRootNode;
            }
        } else {
            createLoadingRoot();
            return projectRootNode;
        }
    }

    private void createRoot(MakeConfigurationDescriptor configurationDescriptor) {
        InstanceContent ic = new InstanceContent();
        Folder logicalFolders = configurationDescriptor.getLogicalFolders();
        ic.add(logicalFolders);
        ic.add(getProject());
        SearchInfoDefinition searchInfo = project.getLookup().lookup(SearchInfoDefinition.class);
        ic.add(searchInfo);
        projectRootNode = new MakeLogicalViewRootNode(logicalFolders, this, ic);
    }

    private void createLoadingRoot() {
        InstanceContent ic = new InstanceContent();
        ic.add(getProject());
        SearchInfoDefinition searchInfo = project.getLookup().lookup(SearchInfoDefinition.class);
        ic.add(searchInfo);
        projectRootNode = new MakeLogicalViewRootNode(null, this, ic);
    }
    private final AtomicBoolean findPathMode = new AtomicBoolean(false);

    boolean isFindPathMode() {
        return findPathMode.get();
    }

    @Override
    public Node findPath(Node root, Object target) {
        Node returnNode = null;
        Project rootProject = root.getLookup().lookup(Project.class);
        if (rootProject == null) {
            return null;
        }

        if (target instanceof DataObject) {
            target = ((DataObject) target).getPrimaryFile();
        }

        if (!(target instanceof FileObject)) {
            return null;
        }

        // FIXUP: this doesn't work with file groups (jl: is this still true?)
        FileObject fo = (FileObject) target;
        if (!gotMakeConfigurationDescriptor() || !fo.isValid()) {
            // IZ 111884 NPE while creating a web project
            return null;
        }
        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor();
        Item item = makeConfigurationDescriptor.findItemByFileObject(fo);

        if (item == null) {
            item = makeConfigurationDescriptor.findExternalItemByPath(fo.getPath());
            if (item == null) {
                // try to find any item
                item = makeConfigurationDescriptor.findItemByPathSlowly(fo.getPath());
                if (item == null) {
                    //not found:
                    return null;
                }
            }
        }

        // prevent double entering
        if (findPathMode.compareAndSet(false, true)) {
            try {
                // FIXUP: assume nde node is last node in current folder. Is this always true?
                // Find the node and return it
                Node folderNode = findFolderNode(root, item.getFolder());
                if (folderNode != null) {
                    Node[] nodes = folderNode.getChildren().getNodes(true);
                    int index = 0;
                    for (index = 0; index < nodes.length; index++) {
                        Item nodeItem = (Item) nodes[index].getValue("Item"); // NOI18N
                        if (nodeItem == item) {
                            break;
                        }
                    }
                    if (nodes.length > 0 && index < nodes.length) {
                        returnNode = nodes[index];
                    }
                    /*
                     if (nodes.length > 0)
                     returnNode = nodes[nodes.length -1];
                     */
                }
            } finally {
                findPathMode.set(false);
            }
        }
        return returnNode;
    }

    /*
     * Recursive method to find the node in the tree with root 'root'
     * that is representing 'folder'
     */
    private static Node findFolderNode(Node root, Folder folder) {
        if (root.getValue("Folder") == folder) { // NOI18N
            return root;
        }
        Folder parent = folder.getParent();

        if (parent == null) {
            return root;
        }

        Node parentNode = findFolderNode(root, parent);

        if (parentNode == null) {
            return null;
        }

        Node[] nodes = parentNode.getChildren().getNodes(true);
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].getValue("Folder") == folder) { // NOI18N
                return nodes[i];
            }
        }
        return null;
    }

    /*
     * Recursive method to find the node in the tree with root 'root'
     * that is representing 'item'
     */
    private static Node findItemNode(Node root, Item item) {
        Node parentNode = findFolderNode(root, item.getFolder());
        if (parentNode != null) {
            Node[] nodes = parentNode.getChildren().getNodes(true);
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i].getValue("Item") == item) { // NOI18N
                    return nodes[i];
                }
            }
        }
        return null;
    }

    /**
     * HACK: set the folder node visible in the project explorer See IZ7551
     */
    public static void setVisible(Project project, Folder folder) {
        Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
        Node projectRoot = findProjectNode(rootNode, project);

        if (projectRoot == null) {
            return;
        }

        Node folderNode = findFolderNode(projectRoot, folder);
        try {
            ProjectTabBridge.getInstance().getExplorerManager().setSelectedNodes(new Node[]{folderNode});
        } catch (Exception e) {
            // skip
        }
    }

    public static void setVisible(final Project project, final Item[] items) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
                List<Node> nodes = new ArrayList<Node>();
                for (int i = 0; i < items.length; i++) {
                    Node root = findProjectNode(rootNode, project);

                    if (root != null) {
                        nodes.add(findItemNode(root, items[i]));
                    }
                }
                try {
                    ProjectTabBridge.getInstance().getExplorerManager().setSelectedNodes(nodes.toArray(new Node[0]));
                } catch (Exception e) {
                    // skip
                }
            }
        });
    }

    public static void checkForChangedName(final Project project) {
        if (CndUtils.isStandalone()) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
                Node root = findProjectNode(rootNode, project);
                if (root != null) {
                    ProjectInformation pi = ProjectUtils.getInformation(project);
                    if (pi != null) { // node will check whether it equals...
                        root.setDisplayName(pi.getDisplayName());
                    }
                }
            }
        });
    }

    public static void checkForChangedViewItemNodes(final Project project, final Delta delta) {
        if (CndUtils.isStandalone()) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
                Node root = findProjectNode(rootNode, project);
                if (root != null) {
                    MakeLogicalViewProvider provider = project.getLookup().lookup(MakeLogicalViewProvider.class);
                    if (provider != null && provider.projectRootNode != null) {
                        provider.projectRootNode.reInit(provider.projectRootNode.getMakeConfigurationDescriptor());
                    }
                }
            }
        });
    }

    public static void checkForChangedViewItemNodes(final Project project, final Folder folder, final Item item) {
        if (CndUtils.isStandalone()) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (item == null) {
                    checkForChangedViewItemNodes(project);
                    return;
                }
                Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
                Node root = findProjectNode(rootNode, project);
                if (root != null) {
                    Node node = findItemNode(root, item);
                    if (node instanceof FilterNode) {
                        Object o = node.getLookup().lookup(ViewItemNode.class);
                        if (o != null) {
                            ((ChangeListener) o).stateChanged(null);
                        }
                    }
                }
            }
        });
    }

    private static void checkForChangedViewItemNodes(Project project) {
        Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
        checkForChangedViewItemNodes(findProjectNode(rootNode, project));
    }

    private static class ChangedViewItemNodesChecker extends SwingWorker<Collection<ChangeListener>, Object> {

        private Node root = null;

        public ChangedViewItemNodesChecker(Node root) {
            this.root = root;
        }

        @Override
        protected Collection<ChangeListener> doInBackground() throws Exception {
            List<ChangeListener> result = new LinkedList<ChangeListener>();
            doWork(root, result);
            return result;
        }

        private void doWork(Node current, List<ChangeListener> result) {
            if (current != null) {
                for (Node node : current.getChildren().getNodes(true)) {
                    doWork(node, result);
                    if (node instanceof FilterNode) {
                        Object o = node.getLookup().lookup(ViewItemNode.class);
                        if (o != null) {
                            result.add((ChangeListener) o);
                        }
                    }
                }
            }
        }

        @Override
        protected void done() {
            try {
                for (ChangeListener listener : get()) {
                    listener.stateChanged(null);
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static void checkForChangedViewItemNodes(Node root) {
        (new ChangedViewItemNodesChecker(root)).execute();
    }

    /**
     * This is long operation, don't call from EDT.
     *
     * @param project
     */
    public static void refreshBrokenItems(final Project project) {
        Node rootNode = getRootNode();
        if (rootNode != null) {
            refreshBrokenItemsImpl(findProjectNode(rootNode, project));
        }
    }

    private static void refreshBrokenItemsImpl(Node root) {
        if (root != null) {
            if (root.isLeaf()) {
                BrokenViewItemNode brokenItem = root.getLookup().lookup(BrokenViewItemNode.class);
                if (brokenItem != null) {
                    brokenItem.refresh();
                }
            } else {
                for (Node node : root.getChildren().getNodes(true)) {
                    refreshBrokenItemsImpl(node);
                }
            }
        }
    }

    private static Node getRootNode() {
        // ProjectTabBridge.getExplorerManager() wants to be called from EDT
        if (SwingUtilities.isEventDispatchThread()) {
            return ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
        } else {
            final Node[] root = new Node[1];
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        root[0] = getRootNode();
                    }
                });
            } catch (InterruptedException ex) {
                // skip
            } catch (InvocationTargetException ex) {
                // skip
            }
            return root[0];
        }
    }

    private static Node findProjectNode(Node root, Project p) {
        Node[] n = root.getChildren().getNodes(true);
        Template<Project> t = new Template<Project>(null, null, p);

        for (int cntr = 0; cntr < n.length; cntr++) {
            if (n[cntr].getLookup().lookupItem(t) != null) {
                return n[cntr];
            }
        }

        return null;
    }

    // Private innerclasses ----------------------------------------------------
    public boolean hasBrokenLinks() {
        List<BrokenLink> errs = BrokenLinks.getBrokenLinks(project);
        return !errs.isEmpty();
    }

    private static Image loadToolTipImage(String imgResouce, String textResource) {
        Image img = ImageUtilities.loadImage(imgResouce);
        img = ImageUtilities.assignToolTipToImage(img,
                "<img src=\"" + MakeLogicalViewRootNode.class.getClassLoader().getResource(imgResouce) + "\">&nbsp;" // NOI18N
                + NbBundle.getMessage(MakeLogicalViewRootNode.class, textResource));
        return img;
    }

    static String getShortDescription(MakeProject project) {
        String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());
        DevelopmentHostConfiguration devHost = project.getDevelopmentHostConfiguration();
        if (devHost == null || devHost.isLocalhost()) {
            return NbBundle.getMessage(MakeLogicalViewProvider.class,
                    "HINT_project_root_node", prjDirDispName); // NOI18N
        } else {
            return NbBundle.getMessage(MakeLogicalViewProvider.class,
                    "HINT_project_root_node_on_host", prjDirDispName, devHost.getDisplayName(true)); // NOI18N
        }
    }

    MakeProject getProject() {
        return project;
    }

    MakeConfigurationDescriptor getMakeConfigurationDescriptor() {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        return makeConfigurationDescriptor;
    }

    boolean gotMakeConfigurationDescriptor() {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        return pdp.gotDescriptor();
    }
}
