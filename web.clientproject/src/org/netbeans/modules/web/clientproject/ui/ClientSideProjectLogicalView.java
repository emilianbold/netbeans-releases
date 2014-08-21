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
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.gsf.codecoverage.api.CoverageActionFactory;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.clientproject.api.remotefiles.RemoteFilesNodeFactory;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.actions.FileSystemAction;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.actions.ToolsAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.XMLUtil;

@ActionReferences({
    @ActionReference(
        id=@ActionID(id="org.netbeans.modules.project.ui.problems.BrokenProjectActionFactory", category="Project"),
        position=2950,
        path="Projects/org-netbeans-modules-web-clientproject/Actions")
})
public class ClientSideProjectLogicalView implements LogicalViewProvider {

    static final Logger LOGGER = Logger.getLogger(ClientSideProjectLogicalView.class.getName());

    static final RequestProcessor RP = new RequestProcessor(ClientSideProjectLogicalView.class);

    private final ClientSideProject project;

    public ClientSideProjectLogicalView(ClientSideProject project) {
        this.project = project;
    }

    @Override
    public Node createLogicalView() {
        return ClientSideProjectNode.createForProject(project);
    }

    @Override
    public Node findPath(Node root, Object target) {
        Project prj = root.getLookup().lookup(Project.class);
        if (prj == null) {
            return null;
        }

        FileObject fo;
        if (target instanceof FileObject) {
            fo = (FileObject) target;
        } else if (target instanceof DataObject) {
            fo = ((DataObject) target).getPrimaryFile();
        } else {
            // unsupported object
            return null;
        }
        // first check project
        Project owner = FileOwnerQuery.getOwner(fo);
        if (!prj.equals(owner)) {
            // another project
            return null;
        }

        // XXX later, use source roots here
        for (Node node : root.getChildren().getNodes(true)) {
            FileObject kid = node.getLookup().lookup(FileObject.class);
            if (kid == null) {
                continue;
            }
            if (kid == fo) {
                return node;
            } else if (FileUtil.isParentOf(kid, fo)) {
                Node found = findNode(node, kid, fo);
                if (found != null && hasObject(found, target)) {
                    return found;
                }
            }
        }
        return null;
    }

    private static Node findNode(Node node, FileObject root, FileObject fo) {
        String relPath = FileUtil.getRelativePath(root, fo);

        // first, try to find the file without extension (more common case)
        String[] path = relPath.split("/"); // NOI18N
        path[path.length - 1] = fo.getName();
        Node found = findNode(node, path);
        if (found != null) {
            return found;
        }
        // file not found, try to search for the name with the extension
        path[path.length - 1] = fo.getNameExt();
        return findNode(node, path);
    }

    private static Node findNode(Node start, String[] path) {
        Node found = null;
        try {
            found = NodeOp.findPath(start, path);
        } catch (NodeNotFoundException ex) {
            // ignored
        }
        return found;
    }

    private boolean hasObject(Node node, Object obj) {
        if (obj == null) {
            return false;
        }
        FileObject fileObject = node.getLookup().lookup(FileObject.class);
        if (fileObject == null) {
            return false;
        }
        if (obj instanceof DataObject) {
            DataObject dataObject = node.getLookup().lookup(DataObject.class);
            if (dataObject == null) {
                return false;
            }
            if (dataObject.equals(obj)) {
                return true;
            }
            return hasObject(node, ((DataObject) obj).getPrimaryFile());
        } else if (obj instanceof FileObject) {
            return obj.equals(fileObject);
        }
        return false;
    }


/** This is the node you actually see in the project tab for the project */
    private static final class ClientSideProjectNode extends AbstractNode implements ChangeListener, PropertyChangeListener {

        private final ClientSideProject project;
        private final ProjectInformation projectInfo;
        private final PropertyEvaluator evaluator;
        private final ProjectProblemsProvider problemsProvider;


        private ClientSideProjectNode(ClientSideProject project) {
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-web-clientproject/Nodes"), createLookup(project));
            this.project = project;
            projectInfo = ProjectUtils.getInformation(project);
            evaluator = project.getEvaluator();
            problemsProvider = project.getLookup().lookup(ProjectProblemsProvider.class);
        }

        public static ClientSideProjectNode createForProject(ClientSideProject project) {
            ClientSideProjectNode rootNode = new ClientSideProjectNode(project);
            rootNode.addListeners();
            return rootNode;
        }

        private void addListeners() {
            evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
            projectInfo.addPropertyChangeListener(WeakListeners.propertyChange(this, projectInfo));
            problemsProvider.addPropertyChangeListener(WeakListeners.propertyChange(this, problemsProvider));
        }

        private static Lookup createLookup(ClientSideProject project) {
            final InstanceContent instanceContent = new InstanceContent();
            instanceContent.add(project);
            instanceContent.add(project, new InstanceContent.Convertor<ClientSideProject, FileObject>() {
                @Override
                public FileObject convert(ClientSideProject obj) {
                    return obj.getProjectDirectory();
                }

                @Override
                public Class<? extends FileObject> type(ClientSideProject obj) {
                    return FileObject.class;
                }

                @Override
                public String id(ClientSideProject obj) {
                    final FileObject fo = obj.getProjectDirectory();
                    return fo == null ? "" : fo.getPath();  // NOI18N
                }

                @Override
                public String displayName(ClientSideProject obj) {
                    return obj.toString();
                }

            });
            instanceContent.add(project, new InstanceContent.Convertor<ClientSideProject, DataObject>() {
                @Override
                public DataObject convert(ClientSideProject obj) {
                    try {
                        final FileObject fo = obj.getProjectDirectory();
                        return fo == null ? null : DataObject.find(fo);
                    } catch (DataObjectNotFoundException ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                        return null;
                    }
                }

                @Override
                public Class<? extends DataObject> type(ClientSideProject obj) {
                    return DataObject.class;
                }

                @Override
                public String id(ClientSideProject obj) {
                    final FileObject fo = obj.getProjectDirectory();
                    return fo == null ? "" : fo.getPath();  // NOI18N
                }

                @Override
                public String displayName(ClientSideProject obj) {
                    return obj.toString();
                }

            });
            return new AbstractLookup(instanceContent);
        }

        @Override
        public Action[] getActions(boolean arg0) {
            List<Action> actions = new LinkedList<>(Arrays.asList(CommonProjectActions.forType("org-netbeans-modules-web-clientproject"))); // NOI18N
            addGruntActions(actions);
            addCodeCoverageAction(actions);
            return actions.toArray(new Action[actions.size()]);
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(ClientSideProject.PROJECT_ICON);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getName() {
            // i would expect getName() here but see #222588
            return projectInfo.getDisplayName();
        }

        @Override
        public String getHtmlDisplayName() {
            String dispName = super.getDisplayName();
            try {
                dispName = XMLUtil.toElementContent(dispName);
            } catch (CharConversionException ex) {
                return dispName;
            }
            return ClientSideProjectUtilities.hasErrors(project)
                    ? "<font color=\"#" + Integer.toHexString(ClientSideProjectUtilities.getErrorForeground().getRGB() & 0xffffff) + "\">" + dispName + "</font>" // NOI18N
                    : null;
        }

        @NbBundle.Messages({
            "# {0} - project directory",
            "ClientSideProjectNode.description=HTML5 application in {0}"
        })
        @Override
        public String getShortDescription() {
            return Bundle.ClientSideProjectNode_description(FileUtil.getFileDisplayName(project.getProjectDirectory()));
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
        public boolean canDestroy() {
            return false;
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    fireIconChange();
                    fireOpenedIconChange();
                    fireDisplayNameChange(null, null);
                }
            });
        }

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    fireNameChange(null, null);
                    fireDisplayNameChange(null, null);
                }
            });
        }

        private void addGruntActions(List<Action> actions) {
            ClientProjectEnhancedBrowserImplementation cfg = project.getEnhancedBrowserImpl();
            if (cfg == null) {
                return;
            }
            ActionProvider actionProvider = cfg.getActionProvider();
            if (actionProvider == null) {
                return;
            }
            Set<String> supportedActions = new HashSet<>(Arrays.asList(actionProvider.getSupportedActions()));
            boolean grunt = project.getProjectDirectory().getFileObject("Gruntfile.js") !=null;
            boolean buildSupported = supportedActions.contains(ActionProvider.COMMAND_BUILD) || grunt;
            boolean rebuildSupported = supportedActions.contains(ActionProvider.COMMAND_REBUILD) || grunt;
            boolean cleanSupported = supportedActions.contains(ActionProvider.COMMAND_CLEAN) || grunt;
            int index = 1; // right after New... action
            if (buildSupported
                    || rebuildSupported
                    || cleanSupported) {
                actions.add(index++, null);
            }
            if (buildSupported) {
                actions.add(index++, FileUtil.getConfigObject("Actions/Project/org-netbeans-modules-project-ui-BuildProject.instance", Action.class)); // NOI18N
            }
            if (rebuildSupported) {
                actions.add(index++, FileUtil.getConfigObject("Actions/Project/org-netbeans-modules-project-ui-RebuildProject.instance", Action.class)); // NOI18N
            }
            if (cleanSupported) {
                actions.add(index++, FileUtil.getConfigObject("Actions/Project/org-netbeans-modules-project-ui-CleanProject.instance", Action.class)); // NOI18N
            }
        }

        private void addCodeCoverageAction(List<Action> actions) {
            JsTestingProvider jsTestingProvider = project.getJsTestingProvider(false);
            if (jsTestingProvider == null
                    || !jsTestingProvider.isCoverageSupported(project)) {
                return;
            }
            int secondSeparatorIndex = actions.size();
            int separatorCount = 0;
            for (int i = 0; i < actions.size(); i++) {
                if (actions.get(i) == null) {
                    separatorCount++;
                }
                if (separatorCount == 2) {
                    secondSeparatorIndex = i;
                    break;
                }
            }
            actions.add(secondSeparatorIndex, CoverageActionFactory.createCollectorAction(null, null));
        }

    }

    private static enum BasicNodes {
        Sources,
        SiteRoot,
        Tests,
    }

    // TODO: all three nodes are registered at the same time - could be refactored and
    //       broken into individual nodes if there is a need to insert nodes in between them
    @NodeFactory.Registration(projectType="org-netbeans-modules-web-clientproject",position=500)
    public static final class BaseHTML5ProjectNodeFactory implements NodeFactory {

        public BaseHTML5ProjectNodeFactory() {
        }

        public NodeList createNodes(Project p) {
            return new ClientProjectNodeList((ClientSideProject)p);
        }

    }

    // #246318
    @NodeFactory.Registration(projectType="org-netbeans-modules-web-clientproject",position=510)
    public static final class OtherDirsNodeFactory implements NodeFactory {

        @Override
        public NodeList<?> createNodes(Project project) {
            ClientSideProject clientSideProject = project.getLookup().lookup(ClientSideProject.class);
            assert clientSideProject != null;
            return NodeFactorySupport.fixedNodeList(new AllFilesNode(clientSideProject));
        }

    }

    @NodeFactory.Registration(projectType="org-netbeans-modules-web-clientproject",position=537)
    public static NodeFactory createRemoteFiles() {
        return RemoteFilesNodeFactory.createRemoteFilesNodeFactory();
    }

    @NodeFactory.Registration(projectType = "org-netbeans-modules-web-clientproject",position = 1000)
    public static NodeFactory createJsTestingProvidersNodes() {
        return JsTestingProviders.getDefault().createJsTestingProvidersNodeFactory();
    }

    private static class ClientProjectNodeList implements NodeList<Key> {

        private ChangeSupport changeSupport = new ChangeSupport(this);
        private final ClientSideProject project;
        private final FileObject nbprojectFolder;
        private final Listener listener;
        private List<Key> keysCache = new ArrayList<>();

        public ClientProjectNodeList(ClientSideProject p) {
            this.project = p;
            nbprojectFolder = p.getProjectDirectory().getFileObject("nbproject"); // NOI18N
            assert nbprojectFolder != null : "Folder nbproject must exist for project " + project.getName();
            listener = new Listener();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public void addNotify() {
            // #230378 - use weak listeners otherwise project is not garbage collected
            project.getEvaluator().addPropertyChangeListener(
                    WeakListeners.propertyChange(listener, project.getEvaluator()));
            addFsListeners();
        }

        @Override
        public void removeNotify() {
            // #230378 - weak listeners are used so no need to call "removeListener"
        }

        private void addFsListeners() {
            FileObject projectDirectory = project.getProjectDirectory();
            projectDirectory.addRecursiveListener(
                    WeakListeners.create(FileChangeListener.class, listener, projectDirectory));
            addTestsListeners();
        }

        private void addTestsListeners() {
            FileObject projectDirectory = project.getProjectDirectory();
            FileObject testsFolder = project.getTestsFolder(false);
            if (testsFolder != null
                    && !projectDirectory.equals(testsFolder)
                    && !FileUtil.isParentOf(projectDirectory, testsFolder)) {
                testsFolder.addRecursiveListener(
                        WeakListeners.create(FileChangeListener.class, listener, testsFolder));
            }
        }

        private class Listener extends FileChangeAdapter implements PropertyChangeListener {

            private volatile boolean sourcesNodeHidden;
            private volatile boolean siteRootNodeHidden;
            private volatile boolean testsNodeHidden;

            public Listener() {
                sourcesNodeHidden = isNodeHidden(BasicNodes.Sources);
                siteRootNodeHidden = isNodeHidden(BasicNodes.SiteRoot);
                testsNodeHidden = isNodeHidden(BasicNodes.Tests);
            }

            @Override
            public void fileFolderCreated(FileEvent fe) {
                updateNodesVisibility();
            }

            @Override
            public void fileDataCreated(FileEvent fe) {
                updateNodesVisibility();
            }


            @Override
            public void fileDeleted(FileEvent fe) {
                updateNodesVisibility();
            }

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (ClientSideProjectConstants.PROJECT_SOURCE_FOLDER.equals(evt.getPropertyName())) {
                    sourcesNodeHidden = isNodeHidden(BasicNodes.Sources);
                    refreshKey(BasicNodes.Sources);
                } else if (ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER.equals(evt.getPropertyName())) {
                    siteRootNodeHidden = isNodeHidden(BasicNodes.SiteRoot);
                    refreshKey(BasicNodes.SiteRoot);
                } else if (ClientSideProjectConstants.PROJECT_TEST_FOLDER.equals(evt.getPropertyName())) {
                    testsNodeHidden = isNodeHidden(BasicNodes.Tests);
                    refreshKey(BasicNodes.Tests);
                    addTestsListeners();
                }
            }

            private void updateNodesVisibility() {
                boolean nodeHidden = isNodeHidden(BasicNodes.Sources);
                if (nodeHidden != sourcesNodeHidden) {
                    sourcesNodeHidden = nodeHidden;
                    refreshKey(BasicNodes.Sources);
                }
                nodeHidden = isNodeHidden(BasicNodes.SiteRoot);
                if (nodeHidden != siteRootNodeHidden) {
                    siteRootNodeHidden = nodeHidden;
                    refreshKey(BasicNodes.SiteRoot);
                }
                nodeHidden = isNodeHidden(BasicNodes.Tests);
                if (nodeHidden != testsNodeHidden) {
                    testsNodeHidden = nodeHidden;
                    refreshKey(BasicNodes.Tests);
                }
            }

        }

        private void refreshKey(final BasicNodes type) {
            // force key refresh:
            removeKey(type);
            changeSupport.fireChange();
        }

        private synchronized void removeKey(final BasicNodes type) {
            for (int i = 0; i < keysCache.size(); i++) {
                if (type.equals(keysCache.get(i).getNode())) {
                    keysCache.remove(i);
                    break;
                }
            }
        }

        @Override
        public Node node(Key k) {
            switch (k.getNode()) {
                case Sources:
                case SiteRoot:
                case Tests:
                    return createNodeForFolder(k.getNode());
                default:
                    return null;
            }
        }

        // #218736
        private List<File> getIgnoredFiles(BasicNodes basicNodes) {
            List<File> ignoredFiles = new ArrayList<File>();
            FileObject buildFolder = project.getProjectDirectory().getFileObject("build"); // NOI18N
            switch (basicNodes) {
                case Sources:
                case SiteRoot:
                case Tests:
                    addIgnoredFile(ignoredFiles, nbprojectFolder);
                    addIgnoredFile(ignoredFiles, buildFolder);
                    break;
                default:
                    throw new IllegalStateException("Unknown BasicNodes: " + basicNodes);
            }
            return ignoredFiles;
        }

        private void addIgnoredFile(List<File> ignoredFiles, FileObject fileObject) {
            if (fileObject == null) {
                return;
            }
            File file = FileUtil.toFile(fileObject);
            if (file != null) {
                ignoredFiles.add(file);
            }
        }

        private boolean isNodeHidden(BasicNodes type) {
            FileObject root = getRootForNode(type);
            return root == null
                    || !root.isValid()
                    || root.getChildren().length == 0;
        }

        private FileObject getRootForNode(BasicNodes node) {
            switch (node) {
                case Sources:
                    return project.getSourcesFolder();
                case SiteRoot:
                    return project.getSiteRootFolder();
                case Tests:
                    return project.getTestsFolder(false);
                default:
                    assert false : "Unknown node: " + node;
                    return null;
            }
        }

        // the same logic as createNodeForFolder() bellow but prevents calling
        // getNodeDelegate() from background thread (see eg. issue 230580)
        private boolean canCreateNodeFor(BasicNodes type) {
            FileObject root = getRootForNode(type);
            if (root != null && root.isValid()) {
                if (!isNodeHidden(type)) {
                    return true;
                }
            }
            return false;
        }

        private Node createNodeForFolder(BasicNodes type) {
            FileObject root = getRootForNode(type);
            if (root != null && root.isValid()) {
                DataFolder df = DataFolder.findFolder(root);
                if (!isNodeHidden(type)) {
                    return new FolderFilterNode(type, df.getNodeDelegate().cloneNode(), getIgnoredFiles(type));
                }
            }
            // missing root should be solved by project problems
            return null;
        }

        @Override
        public List<Key> keys() {
            // in order to resolve #225877 the only way to refresh a node in NodeList
            // is to add it or remove it. There is nothing like Children.Keys.refreshKey(...).
            // Hence I have to create a new key instance here as a workaround:
            ArrayList<Key> keys = new ArrayList<>();
            if (canCreateNodeFor(BasicNodes.Sources)) {
                keys.add(getKey(BasicNodes.Sources));
            }
            if (canCreateNodeFor(BasicNodes.SiteRoot)) {
                keys.add(getKey(BasicNodes.SiteRoot));
            }
            if (canCreateNodeFor(BasicNodes.Tests)) {
                keys.add(getKey(BasicNodes.Tests));
            }
            return keys;
        }

        private synchronized Key getKey(BasicNodes n) {
            for (Key k : keysCache) {
                if (n.equals(k.getNode())) {
                    return k;
                }
            }
            Key k = new Key(n);
            keysCache.add(k);
            return k;
        }

    }

    /**
     * The purpose of this Key class is to be able to create several different
     * instances of BasicNodes.Sources node in order "refresh" the node if project
     * was reconfigured.
     */
    private static class Key {
        private BasicNodes node;
        private int timestamp;
        private static int counter = 0;

        public Key(BasicNodes node) {
            this.node = node;
            this.timestamp = ++counter;
        }

        public BasicNodes getNode() {
            return node;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + (this.node != null ? this.node.hashCode() : 0);
            hash = hash + this.timestamp * 67;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            if (this.node != other.node) {
                return false;
            }
            if (this.timestamp != other.timestamp) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Key{" + "node=" + node + ", timestamp=" + timestamp + ", hash="+ hashCode()+"}";
        }

    }

    private static final class FolderFilterNode extends FilterNode {

        @StaticResource
        private static final String SOURCES_FILES_BADGE = "org/netbeans/modules/web/clientproject/ui/resources/sources-badge.gif"; // NOI18N
        @StaticResource
        private static final String SITE_ROOT_FILES_BADGE = "org/netbeans/modules/web/clientproject/ui/resources/siteroot-badge.gif"; // NOI18N

        private final BasicNodes nodeType;
        private final Node iconDelegate;
        private final Node delegate;


        public FolderFilterNode(BasicNodes nodeType, Node folderNode, List<File> ignoreList) {
            super(folderNode, folderNode.isLeaf() ? Children.LEAF :
                    new FolderFilterChildren(folderNode, ignoreList));
            this.nodeType = nodeType;
            iconDelegate = DataFolder.findFolder (FileUtil.getConfigRoot()).getNodeDelegate();
            delegate = folderNode;
        }

        @Override
        public Action[] getActions(boolean context) {
            List<Action> actions = new ArrayList<Action>();
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.add(SystemAction.get(FindAction.class));
            actions.add(null);
            actions.add(SystemAction.get(FileSystemAction.class));
            actions.add(null);
            actions.add(SystemAction.get(PasteAction.class));
            actions.add(null);
            actions.add(SystemAction.get(ToolsAction.class));
            actions.add(null);
            actions.add(CommonProjectActions.customizeProjectAction());
            return actions.toArray(new Action[actions.size()]);
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public Image getIcon(int type) {
            return computeIcon(nodeType, false, type);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return computeIcon(nodeType, true, type);
        }

        private Image computeIcon(BasicNodes node, boolean opened, int type) {
            Image image;
            String badge = null;
            switch (nodeType) {
                case Sources:
                case Tests:
                    badge = SOURCES_FILES_BADGE;
                    break;
                case SiteRoot:
                    badge = SITE_ROOT_FILES_BADGE;
                    break;
                default:
                    assert false : "Unknown nodeType: " + nodeType;
            }

            image = opened ? iconDelegate.getOpenedIcon(type) : iconDelegate.getIcon(type);
            if (badge != null) {
                image = ImageUtilities.mergeImages(image, ImageUtilities.loadImage(badge, false), 7, 7);
            }

            return image;
        }

        @Override
        public String getDisplayName() {
            switch (nodeType) {
                case Sources:
                    return java.util.ResourceBundle.getBundle("org/netbeans/modules/web/clientproject/ui/Bundle").getString("SOURCES");
                case SiteRoot:
                    return java.util.ResourceBundle.getBundle("org/netbeans/modules/web/clientproject/ui/Bundle").getString("SITE_ROOT");
                case Tests:
                    return java.util.ResourceBundle.getBundle("org/netbeans/modules/web/clientproject/ui/Bundle").getString("UNIT_TESTS");
                default:
                    throw new AssertionError(nodeType.name());
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.nodeType != null ? this.nodeType.hashCode() : 0);
            hash = 29 * hash + Objects.hashCode(this.delegate);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FolderFilterNode other = (FolderFilterNode) obj;
            if (this.nodeType != other.nodeType) {
                return false;
            }
            if (!Objects.equals(this.delegate, other.delegate)) {
                return false;
            }
            return true;
        }


    }

    private static class FolderFilterChildren extends FilterNode.Children {

        private final Set<File> ignoreList = new WeakSet<File>();

        public FolderFilterChildren(Node n, List<File> ignoreList) {
            super(n);
            this.ignoreList.addAll(ignoreList);
        }

        @Override
        protected Node[] createNodes(Node key) {
            FileObject fo = key.getLookup().lookup(FileObject.class);
            if (fo == null) {
                return super.createNodes(key);
            }
            File file = FileUtil.toFile(fo);
            if (file == null) {
                // XX add logging
                return super.createNodes(key);
            }
            if (!VisibilityQuery.getDefault().isVisible(fo)) {
                return new Node[0];
            }
            if (ignoreList.contains(file)) {
                return new Node[0];
            }
            return super.createNodes(key);
        }

    }

    private static final class AllFilesNode extends AbstractNode {

        private final Node iconDelegate;


        public AllFilesNode(ClientSideProject project) {
            super(Children.create(AllFilesChildFactory.create(project), true));
            iconDelegate = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
        }

        @NbBundle.Messages("AllFilesNode.name=All Files")
        @Override
        public String getDisplayName() {
            return Bundle.AllFilesNode_name();
        }

        @Override
        public Image getIcon(int type) {
            return iconDelegate.getIcon(type);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return iconDelegate.getOpenedIcon(type);
        }

    }

    private static final class AllFilesChildFactory extends ChildFactory<FileObject> implements FileChangeListener {

        private static final Comparator<FileObject> CHILDREN_CAMPARATOR = new FileObjectUiComparator();


        private final ClientSideProject project;


        private AllFilesChildFactory(ClientSideProject project) {
            this.project = project;
        }

        public static AllFilesChildFactory create(ClientSideProject project) {
            AllFilesChildFactory factory = new AllFilesChildFactory(project);
            FileObject projectDirectory = project.getProjectDirectory();
            projectDirectory.addFileChangeListener(FileUtil.weakFileChangeListener(factory, projectDirectory));
            return factory;
        }

        @Override
        protected boolean createKeys(List<FileObject> toPopulate) {
            FileObject[] children = project.getProjectDirectory().getChildren();
            for (FileObject fileObject : children) {
                if (isVisible(fileObject)) {
                    toPopulate.add(fileObject);
                }
            }
            // sort - really there is no easy/better way?
            Collections.sort(toPopulate, CHILDREN_CAMPARATOR);
            return true;
        }

        @Override
        protected Node createNodeForKey(FileObject key) {
            try {
                return DataObject.find(key).getNodeDelegate();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        private boolean isVisible(FileObject fileObject) {
            if ("nbproject".equals(fileObject.getNameExt())) { // NOI18N
                return false;
            }
            return VisibilityQuery.getDefault().isVisible(fileObject);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            refresh();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            refresh();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            refresh();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            refresh();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            refresh();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }

        private void refresh() {
            refresh(false);
        }

    }

    private static final class FileObjectUiComparator implements Comparator<FileObject> {

        private final Collator collator;


        public FileObjectUiComparator() {
            collator = Collator.getInstance();
        }

        @Override
        public int compare(FileObject file1, FileObject file2) {
            if (file1.isFolder()
                    && file2.isData()) {
                return -1;
            }
            if (file1.isData()
                    && file2.isFolder()) {
                return 1;
            }
            String name1 = file1.getNameExt();
            String name2 = file2.getNameExt();
            if (Character.isUpperCase(name1.charAt(0))
                    && Character.isLowerCase(name2.charAt(0))) {
                return -1;
            }
            if (Character.isLowerCase(name1.charAt(0))
                    && Character.isUpperCase(name2.charAt(0))) {
                return 1;
            }
            return collator.compare(name1, name2);
        }

    }

}
