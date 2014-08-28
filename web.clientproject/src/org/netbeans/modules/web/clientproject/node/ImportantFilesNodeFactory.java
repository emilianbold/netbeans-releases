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
package org.netbeans.modules.web.clientproject.node;

import java.awt.Image;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.netbeans.spi.search.SearchInfoDefinitionFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

@NodeFactory.Registration(projectType = "org-netbeans-modules-web-clientproject", position = 520)
public class ImportantFilesNodeFactory implements NodeFactory {

    private static final String IMPORTANT_FILES_NAME = "important.files"; // NOI18N
    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER = "Tree.openIcon"; // NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.openedIcon"; // NOI18N
    @StaticResource
    private static final String ICON_PATH = "org/netbeans/modules/web/clientproject/resources/defaultFolder.gif"; // NOI18N
    @StaticResource
    private static final String OPENED_ICON_PATH = "org/netbeans/modules/web/clientproject/resources/defaultFolderOpen.gif"; // NOI18N

    private static final RequestProcessor RP = new RequestProcessor(ImportantFilesNodeFactory.class.getName());

    public ImportantFilesNodeFactory() {
    }

    @Override
    public NodeList createNodes(Project p) {
        return new ImpFilesNL(p);
    }

    /**
     * Public RP serving as queue of calls into org.openide.nodes. All such
     * calls must be made outside ProjectManager#mutex(), this (shared) queue
     * ensures ordering of calls.
     *
     * @return Shared RP
     */
    public static RequestProcessor getNodesSyncRP() {
        return RP;
    }

    private static class ImpFilesNL implements NodeList<String> {

        private final ClientSideProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private final Listener listener;
        private ImportantFilesNode importantFilesNode;
        
        public ImpFilesNL(Project p) {
            project = (ClientSideProject)p;
            listener = new Listener();
            Set<File> temp = new HashSet();
            for (String name: ImportantFilesChildren.FILES.keySet()) {
                File f = FileUtil.normalizeFile(new File(project.getProjectDirectory().getPath() + "/" + name));
                if (temp.add(f)) {
                    FileUtil.addFileChangeListener(listener, f);
                }
            }
        }
        
        private class Listener extends FileChangeAdapter {            

            public Listener() {
            }

            @Override
            public void fileDataCreated(FileEvent fe) {
                changeSupport.fireChange();            
            }


            @Override
            public void fileDeleted(FileEvent fe) {
                changeSupport.fireChange();
            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {
                changeSupport.fireChange();
            }

            @Override
            public void fileFolderCreated(FileEvent fe) {
                changeSupport.fireChange();
            }
            
        }        

        public List<String> keys() {
            if (hasImportantFiles()) {
                return Collections.singletonList(IMPORTANT_FILES_NAME);
            } else {
                return Collections.emptyList();
            }
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
        public Node node(String key) {
            assert key.equals(IMPORTANT_FILES_NAME);
            if (project instanceof ClientSideProject) {
                if (importantFilesNode == null) {
                    importantFilesNode = new ImportantFilesNode((ClientSideProject) project);
                }
                return importantFilesNode;
            }
            return null;
        }

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

        private boolean hasImportantFiles() {
            for (String name: ImportantFilesChildren.FILES.keySet()) {
                if (project.getProjectDirectory().getFileObject(name) != null) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Show node "Important Files" with various config and docs files beneath
     * it.
     */
    static final class ImportantFilesNode extends AnnotatedNode {

        private static final String DISPLAY_NAME = NbBundle.getMessage(ImportantFilesNode.class, "LBL_important_files");
        @StaticResource
        private static final String NODE_ICON = "org/netbeans/modules/web/clientproject/resources/config-badge.gif"; // NOI18N

        public ImportantFilesNode(ClientSideProject project) {
            this(project, new ImportantFilesChildren(project));
        }

        ImportantFilesNode(Project project, Children ch) {
            super(ch, Lookups.fixed(project,
                    SearchInfoDefinitionFactory.createSearchInfoBySubnodes(ch)));
        }

        public @Override
        String getName() {
            return IMPORTANT_FILES_NAME;
        }

        private Image getIcon(boolean opened) {
            Image badge = ImageUtilities.loadImage(NODE_ICON, true);
            return ImageUtilities.mergeImages(getTreeFolderIcon(opened), badge, 8, 8);
        }

        /**
         * Returns default folder icon as {@link java.awt.Image}. Never returns
         * <code>null</code>.
         *
         * @param opened wheter closed or opened icon should be returned.
         */
        public static Image getTreeFolderIcon(boolean opened) {
            Image base = (Image) UIManager.get(opened ? OPENED_ICON_KEY_UIMANAGER_NB : ICON_KEY_UIMANAGER_NB); // #70263;
            if (base == null) {
                Icon baseIcon = UIManager.getIcon(opened ? OPENED_ICON_KEY_UIMANAGER : ICON_KEY_UIMANAGER); // #70263
                if (baseIcon != null) {
                    base = ImageUtilities.icon2Image(baseIcon);
                } else { // fallback to our owns
                    base = ImageUtilities.loadImage(opened ? OPENED_ICON_PATH : ICON_PATH, true);
                }
            }
            assert base != null;
            return base;
        }

        public @Override
        String getDisplayName() {
            return annotateName(DISPLAY_NAME);
        }

        public @Override
        String getHtmlDisplayName() {
            return computeAnnotatedHtmlDisplayName(DISPLAY_NAME, getFiles());
        }

        public @Override
        Image getIcon(int type) {
            return annotateIcon(getIcon(false), type);
        }

        public @Override
        Image getOpenedIcon(int type) {
            return annotateIcon(getIcon(true), type);
        }
    }

    public static String computeAnnotatedHtmlDisplayName(
            final String htmlDisplayName, final Set<? extends FileObject> files) {

        String result = null;
        if (files != null && files.iterator().hasNext()) {
            try {
                FileObject fo = (FileObject) files.iterator().next();
                FileSystem.Status stat = fo.getFileSystem().getStatus();
                if (stat instanceof FileSystem.HtmlStatus) {
                    FileSystem.HtmlStatus hstat = (FileSystem.HtmlStatus) stat;

                    String annotated = hstat.annotateNameHtml(htmlDisplayName, files);

                    // Make sure the super string was really modified (XXX why?)
                    if (!htmlDisplayName.equals(annotated)) {
                        result = annotated;
                    }
                }
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return result;
    }

    /**
     * Actual list of important files.
     */
    private static final class ImportantFilesChildren extends Children.Keys<String> {

        private final FileChangeListener fclStrong = new FileChangeAdapter() {
            public @Override
            void fileRenamed(FileRenameEvent fe) {
                refreshKeys();
            }

            public @Override
            void fileDataCreated(FileEvent fe) {
                refreshKeys();
            }

            public @Override
            void fileDeleted(FileEvent fe) {
                refreshKeys();
            }
        };
        private FileChangeListener fclWeak;

        /**
         * Abstract location to display name.
         */
        @NbBundle.Messages({
            "LBL_gruntfile.js=Gruntfile",
            "LBL_package.json=package.json",
            "LBL_plugins.properties=Cordova Plugins",
            "LBL_bower.json=bower.json",
            "LBL_bowerrc=.bowerrc"
        })
        private static final java.util.Map<String, String> FILES = new LinkedHashMap<>();

        static {
            FILES.put("gruntfile.js", Bundle.LBL_gruntfile_js());
            FILES.put("package.json", Bundle.LBL_package_json());
            FILES.put("Gruntfile.js", Bundle.LBL_gruntfile_js());
            FILES.put("Package.json", Bundle.LBL_package_json());
            FILES.put("bower.json", Bundle.LBL_bower_json());
            FILES.put(".bowerrc", Bundle.LBL_bowerrc());
            FILES.put("nbproject/plugins.properties", Bundle.LBL_plugins_properties());
        }

        private final ClientSideProject project;

        public ImportantFilesChildren(ClientSideProject project) {
            this.project = project;
        }

        protected @Override
        void addNotify() {
            super.addNotify();
            attachListeners();
            refreshKeys();
        }

        protected @Override
        void removeNotify() {
            setKeys(Collections.<String>emptyList());
            removeListeners();
            super.removeNotify();
        }

        protected @Override
        Node[] createNodes(String key) {
            String locEval = project.getEvaluator().evaluate(key);
            FileObject file = project.getProjectHelper().resolveFileObject(locEval);
            try {
                Node orig = DataObject.find(file).getNodeDelegate();
                return new Node[]{createSpecialFileNode(orig, FILES.get(key))};
            } catch (DataObjectNotFoundException e) {
                throw new AssertionError(e);
            }
        }

        public static Node createSpecialFileNode(Node orig, String displayName) {
            return new SpecialFileNode(orig, displayName);
        }

        /**
         * Node to represent some special file in a project. Mostly just a
         * wrapper around the normal data node.
         */
        private static final class SpecialFileNode extends FilterNode {

            private final String displayName;

            public SpecialFileNode(Node orig, String displayName) {
                super(orig);
                this.displayName = displayName;
            }

            public @Override
            String getDisplayName() {
                if (displayName != null) {
                    return displayName;
                } else {
                    return super.getDisplayName();
                }
            }

            public @Override
            boolean canRename() {
                return false;
            }

            public @Override
            boolean canDestroy() {
                return false;
            }

            public @Override
            boolean canCut() {
                return false;
            }

            public @Override
            String getHtmlDisplayName() {
                String result = null;
                DataObject dob = getLookup().lookup(DataObject.class);
                if (dob != null) {
                    Set<FileObject> files = dob.files();
                    result = computeAnnotatedHtmlDisplayName(getDisplayName(), files);
                }
                return result;
            }

        }

        protected void refreshKeys() {
            final TreeMap<FileObject, String> files = new TreeMap<>(new FileObjectComparator());
            for (String loc : FILES.keySet()) {
                FileObject file = project.getProjectDirectory().getFileObject(loc);
                if (file != null && loc.equals(FileUtil.getRelativePath(project.getProjectDirectory(), file))) {
                    files.put(file, loc);
                }
            }

                getNodesSyncRP().post(new Runnable() { // #72471
                    public void run() {
                    setKeys(files.values());
                    }
                });
            ((ImportantFilesNode) getNode()).setFiles(files.keySet());
            }

        private void attachListeners() {
            try {
                if (fclWeak == null) {
                    FileSystem fs = project.getProjectDirectory().getFileSystem();
                    fclWeak = FileUtil.weakFileChangeListener(fclStrong, fs);
                    fs.addFileChangeListener(fclWeak);
                }
            } catch (FileStateInvalidException e) {
                assert false : e;
            }
        }

        private void removeListeners() {
            if (fclWeak != null) {
                try {
                    project.getProjectDirectory().getFileSystem().removeFileChangeListener(fclWeak);
                } catch (FileStateInvalidException e) {
                    assert false : e;
                }
                fclWeak = null;
            }
        }
    }

    private static class FileObjectComparator implements Comparator<FileObject>{
        public FileObjectComparator() {
}

        @Override
        public int compare(FileObject o1, FileObject o2) {
            try {
                return DataFolder.SortMode.FOLDER_NAMES.compare(DataObject.find(o1), DataObject.find(o2));
            } catch (DataObjectNotFoundException ex) {
                return o1.getNameExt().compareTo(o2.getNameExt());
            }
        }

    }
}
