/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mercurial.ui.diff;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.versioning.util.common.FileTreeView;
import org.netbeans.swing.outline.RenderDataProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ondrej Vrabec
 */
class DiffFileTreeImpl extends FileTreeView<DiffNode> {
    
    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; //NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; //NOI18N
    private final static String PATH_SEPARATOR_REGEXP = File.separator.replace("\\", "\\\\"); //NOI18N
    
    /**
     * Defines labels for Diff view table columns.
     */ 
    private static final Map<String, String[]> columnLabels = new HashMap<String, String[]>(4);
    private static Image FOLDER_ICON;
    private final MultiDiffPanel master;

    {
        ResourceBundle loc = NbBundle.getBundle(DiffFileTreeImpl.class);
        columnLabels.put(DiffNode.COLUMN_NAME_STATUS, new String [] { 
                loc.getString("CTL_DiffTable_Column_Status_Title"), 
                loc.getString("CTL_DiffTable_Column_Status_Desc")});
        columnLabels.put(DiffNode.COLUMN_NAME_LOCATION, new String [] { 
                loc.getString("CTL_DiffTable_Column_Location_Title"), 
                loc.getString("CTL_DiffTable_Column_Location_Desc")});
    }

    public DiffFileTreeImpl (MultiDiffPanel master) {
        super();
        this.master = master;
        setupColumns();
    }
    
    @SuppressWarnings("unchecked")
    private void setupColumns() {
        view.setPropertyColumns(DiffNode.COLUMN_NAME_STATUS, columnLabels.get(DiffNode.COLUMN_NAME_STATUS)[0],
                DiffNode.COLUMN_NAME_LOCATION, columnLabels.get(DiffNode.COLUMN_NAME_LOCATION)[0]);
        view.setPropertyColumnDescription(DiffNode.COLUMN_NAME_STATUS, columnLabels.get(DiffNode.COLUMN_NAME_STATUS)[1]);
        view.setPropertyColumnDescription(DiffNode.COLUMN_NAME_LOCATION, columnLabels.get(DiffNode.COLUMN_NAME_LOCATION)[1]);
        view.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffFileTreeImpl.class, "ACSN_DiffTable")); // NOI18N
        view.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffFileTreeImpl.class, "ACSD_DiffTable")); // NOI18N
        view.getOutline().setRenderDataProvider(createRenderProvider());
    }
    
    @Override
    public Object prepareModel (DiffNode[] nodes) {
        Map<File, Collection<DiffNode>> sortedNodes = new HashMap<File, Collection<DiffNode>>();
        for (DiffNode n : nodes) {
            File repository = Mercurial.getInstance().getRepositoryRoot(n.getFile());
            Collection<DiffNode> repositorySetups = sortedNodes.get(repository);
            if (repositorySetups == null) {
                repositorySetups = new TreeSet<DiffNode>(new PathComparator());
                sortedNodes.put(repository, repositorySetups);
            }
            repositorySetups.add(n);
        }
        Node rootNode;
        if (sortedNodes.size() == 1) {
            Map.Entry<File, Collection<DiffNode>> e = sortedNodes.entrySet().iterator().next();
            rootNode = new RepositoryRootNode(e.getKey(), new ArrayList<DiffNode>(e.getValue()));
            ((DiffTreeViewChildren) rootNode.getChildren()).buildSubNodes();
        } else {
            rootNode = new RootNode(sortedNodes);
            ((DiffTreeViewChildren) rootNode.getChildren()).buildSubNodes();
        }
        return rootNode;
    }
    
    @Override
    protected DiffNode convertToAcceptedNode (Node node) {
        return node instanceof DiffNode ? (DiffNode) node : null;
    }

    private RenderDataProvider createRenderProvider () {
        return new AbstractRenderDataProvider() {
            @Override
            protected String annotateName (DiffNode node, String originalLabel) {
                if (HgModuleConfig.getDefault().isExcludedFromCommit(node.getSetup().getBaseFile().getAbsolutePath())) {
                    originalLabel = "<s>" + (originalLabel == null ? node.getName() : originalLabel) + "</s>"; //NOI18N
                }
                return originalLabel;
            }
        };
    }

    @Override
    protected void nodeSelected (DiffNode node) {
        master.nodeSelected(node);
    }

    @Override
    protected JPopupMenu getPopup () {
        return master.getPopup();
    }
    
    @Override
    protected void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int width = view.getWidth();
                view.getOutline().getColumnModel().getColumn(0).setPreferredWidth(width * 40 / 100);
                view.getOutline().getColumnModel().getColumn(1).setPreferredWidth(width * 20 / 100);
                view.getOutline().getColumnModel().getColumn(2).setPreferredWidth(width * 40 / 100);
            }
        });
    }
    
    private static class RootNode extends AbstractNode {
        public RootNode (Map<File, Collection<DiffNode>> nodes) {
            super(new RootNodeChildren(nodes));
        }
    }
    
    private static abstract class DiffTreeViewChildren extends Children.Array {
        abstract void buildSubNodes ();
    }

    private static class RootNodeChildren extends DiffTreeViewChildren {
        private final java.util.Map<File, Collection<DiffNode>> nestedNodes;
        
        public RootNodeChildren (java.util.Map<File, Collection<DiffNode>> setups) {
            this.nestedNodes = setups;
        }

        @Override
        void buildSubNodes () {
            add(createNodes());
        }
        
        private Node[] createNodes () {
            List<Node> nodes = new ArrayList<Node>(nestedNodes.size());
            for (java.util.Map.Entry<File, Collection<DiffNode>> e : nestedNodes.entrySet()) {
                RepositoryRootNode root = new RepositoryRootNode(e.getKey(), new ArrayList<DiffNode>(e.getValue()));
                ((DiffTreeViewChildren) root.getChildren()).buildSubNodes();
                nodes.add(root);
            }
            return nodes.toArray(new Node[nodes.size()]);
        }
    }
    
    private static class RepositoryRootNode extends AbstractNode {
        private final File repo;
        RepositoryRootNode (File repository, List<DiffNode> nestedNodes) {
            this(repository, nestedNodes.toArray(new DiffNode[nestedNodes.size()]));
        }

        private RepositoryRootNode (File repository, DiffNode[] nestedNodes) {
            super(new NodeChildren(new NodeData(new File(repository, getCommonPrefix(nestedNodes)), getCommonPrefix(nestedNodes), nestedNodes), true), Lookups.fixed(repository));
            this.repo = repository;
        }
        
        @Override
        public String getName () {
            return repo.getName();
        }

        @Override
        public Image getIcon (int type) {
            return getFolderIcon();
        }
    }

    private static String getCommonPrefix (DiffNode[] nodes) {
        String prefix = "";
        if (nodes.length > 0) {
            prefix = nodes[0].getLocation();
            int index = prefix.lastIndexOf(File.separator);
            if (index == -1) {
                prefix = "";
            } else {
                prefix = prefix.substring(0, index);
            }
        }
        boolean slashNeeded = !prefix.isEmpty();
        for (DiffNode n : nodes) {
            String location = n.getLocation();
            while (!location.startsWith(prefix)) {
                slashNeeded = false;
                int index = prefix.lastIndexOf(File.separator);
                if (index == -1) {
                    prefix = "";
                } else {
                    prefix = prefix.substring(0, index);
                }
            }
        }
        return slashNeeded ? prefix + File.separator : prefix;
    }
    
    private static class NodeChildren extends DiffTreeViewChildren {
        private final DiffNode[] nestedNodes;
        private final String path;
        private final boolean top;
        private final File file;
    
        public NodeChildren (NodeData data, boolean top) {
            this.nestedNodes = data.nestedNodes;
            this.path = data.path;
            this.file = data.file;
            this.top = top;
        }
    
        @Override
        void buildSubNodes () {
            List<NodeData> data = new ArrayList<NodeData>(nestedNodes.length);
            String prefix = null;
            List<DiffNode> subNodes = new ArrayList<DiffNode>();
            for (DiffNode n : nestedNodes) {
                String location = n.getLocation();
                if (prefix == null) {
                    prefix = path + location.substring(path.length()).split(PATH_SEPARATOR_REGEXP, 0)[0];
                }
                if (location.equals(prefix)) {
                    if (!subNodes.isEmpty()) {
                        data.add(new NodeData(getFile(prefix), prefix, subNodes.toArray(new DiffNode[subNodes.size()])));
                        subNodes.clear();
                    }
                    data.add(new NodeData(getFile(prefix), prefix, new DiffNode[] { n }));
                    prefix = null;
                } else if (location.startsWith(prefix)) {
                    subNodes.add(n);
                } else {
                    data.add(new NodeData(getFile(prefix), prefix, subNodes.toArray(new DiffNode[subNodes.size()])));
                    subNodes.clear();
                    prefix = path + location.substring(path.length()).split(PATH_SEPARATOR_REGEXP, 0)[0];
                    subNodes.add(n);
                }
            }
            if (!subNodes.isEmpty()) {
                data.add(new NodeData(getFile(prefix), prefix, subNodes.toArray(new DiffNode[subNodes.size()])));
            }
            
            add(createNodes(data));
        }
    
        private Node[] createNodes (List<NodeData> keys) {
            List<Node> toCreate = new ArrayList<Node>(keys.size());
            for (NodeData key : keys) {
                final Node node;
                if (key.nestedNodes.length == 0) {
                    continue;
                } else if (key.nestedNodes.length == 1 && key.path.equals(key.nestedNodes[0].getLocation())) {
                    node = key.nestedNodes[0];
                } else {
                    final String name;
                    if (top) {
                        name = key.path;
                    } else {
                        String[] segments = key.path.split(PATH_SEPARATOR_REGEXP);
                        name = segments[segments.length - 1];
                    }
                    final Image icon = getFolderIcon(key.file);
                    NodeChildren ch = new NodeChildren(new NodeData(key.file, key.path + File.separator, key.nestedNodes), false);
                    node = new AbstractNode(ch, Lookups.fixed(key.file)) {
                        @Override
                        public String getName () {
                            return name;
                        }

                        @Override
                        public Image getIcon (int type) {
                            return icon;
                        }
                    };
                    ch.buildSubNodes();
                }
                toCreate.add(node);
            }
            return toCreate.toArray(new Node[toCreate.size()]);
        }

        private Image getFolderIcon (File file) {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
            Icon icon = null;
            if (fo != null) {
                try {
                    ProjectManager.Result res = ProjectManager.getDefault().isProject2(fo);
                    if (res != null) {
                        icon = res.getIcon();
                    }
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(DiffFileTreeImpl.class.getName()).log(Level.INFO, null, ex);
                }
            }
            return icon == null ? DiffFileTreeImpl.getFolderIcon() : ImageUtilities.icon2Image(icon);
        }

        private File getFile (String prefix) {
            String p = prefix;
            if (prefix.startsWith(path)) {
                p = prefix.substring(path.length());
            }
            return new File(file, p);
        }
    }
    
    private static class NodeData {
        private final File file;
        private final String path;
        private final DiffNode[] nestedNodes;

        public NodeData (File file, String path, DiffNode[] nested) {
            this.file = file;
            this.path = path;
            this.nestedNodes = nested;
        }
    }

    private static Image getFolderIcon () {
        if (FOLDER_ICON == null) {
            Icon baseIcon = UIManager.getIcon(ICON_KEY_UIMANAGER);
            Image base;
            if (baseIcon != null) {
                base = ImageUtilities.icon2Image(baseIcon);
            } else {
                base = (Image) UIManager.get(ICON_KEY_UIMANAGER_NB);
                if (base == null) { // fallback to our owns
                    base = ImageUtilities.loadImage("org/openide/loaders/defaultFolder.gif"); //NOI18N
                }
            }
            FOLDER_ICON = base;
        }
        return FOLDER_ICON;
    }

    private static class PathComparator implements Comparator<DiffNode> {

        @Override
        public int compare (DiffNode o1, DiffNode o2) {
            String[] segments1 = o1.getLocation().split(PATH_SEPARATOR_REGEXP);
            String[] segments2 = o2.getLocation().split(PATH_SEPARATOR_REGEXP);
            for (int i = 0; i < Math.min(segments1.length, segments2.length); ++i) {
                String segment1 = segments1[i];
                String segment2 = segments2[i];
                int comp = segment1.compareTo(segment2);
                if (comp != 0) {
                    if (segment1.startsWith(segment2)) {
                        // xml.xdm must precede xml node
                        return segment2.length() - segment1.length();
                    } else if (segment2.startsWith(segment1)) {
                        // xml must follow xml.xdm node
                        return segment2.length() - segment1.length();
                    }
                    return comp;
                }
            }
            return segments2.length - segments1.length;
        }

    }
    
}
