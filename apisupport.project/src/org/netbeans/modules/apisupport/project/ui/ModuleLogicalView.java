/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.apisupport.project.metainf.ServiceNodeHandler;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.layers.LayerNode;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Provides a logical view of a NetBeans module project.
 * @author Jesse Glick
 */
public final class ModuleLogicalView implements LogicalViewProvider {
    
    /** Package private for unit tests only. */
    static final RequestProcessor RP = new RequestProcessor();
    
    private final NbModuleProject project;
    
    public ModuleLogicalView(NbModuleProject project) {
        this.project = project;
    }
    
    public Node createLogicalView() {
        return new RootNode(project);
    }
    
    /** cf. #45952 */
    public Node findPath(Node root, Object target) {
        if (root.getLookup().lookup(NbModuleProject.class) != project) {
            // Not intended for this project. Should not normally happen anyway.
            return null;
        }
        
        Node[] rootChildren = root.getChildren().getNodes(true);
        DataObject file;
        
        if (target instanceof FileObject) {
            try {
                file = DataObject.find((FileObject) target);
            } catch (DataObjectNotFoundException e) {
                throw new AssertionError(e);
            }
        } else if (target instanceof DataObject) {
            file = (DataObject) target;
        } else {
            // What is it?
            return null;
        }
        
        for (int i = 0; i < rootChildren.length; i++) {
            Node found = PackageView.findPath(rootChildren[i], target);
            //System.err.println("found " + found + " for " + target + " in " + rootChildren[i]);
            if (found != null) {
                return found;
            }
            // For Important Files node:
            if (rootChildren[i].getName().equals(IMPORTANT_FILES_NAME)) {
                Node[] ifChildren = rootChildren[i].getChildren().getNodes(true);
                for (int j = 0; j < ifChildren.length; j++) {
                    if (ifChildren[j].getCookie(DataObject.class) == file) {
                        return ifChildren[j];
                    }
                }
            }
        }
        
        return null;
    }
    
    private static final class RootNode extends AnnotatedNode {
        
        private final NbModuleProject project;
        
        public RootNode(NbModuleProject project) {
            
            // XXX add a NodePathResolver impl to lookup
            super(new RootChildren(project), Lookups.fixed(new Object[] {project}));
            this.project = project;
            setForceAnnotation(true);
            setIconBaseWithExtension(NbModuleProject.NB_PROJECT_ICON_PATH);
            ProjectInformation pi = ProjectUtils.getInformation(project);
            setDisplayName(pi.getDisplayName());
            setShortDescription(NbBundle.getMessage(ModuleLogicalView.class, "HINT_project_root_node", FileUtil.getFileDisplayName(project.getProjectDirectory())));
            setFiles(getProjectFiles());
            pi.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName() == ProjectInformation.PROP_DISPLAY_NAME) {
                        RootNode.this.setDisplayName((String) evt.getNewValue());
                    } else if (evt.getPropertyName() == ProjectInformation.PROP_NAME) {
                        RootNode.this.setName((String) evt.getNewValue());
                    }
                }
            });
        }
        
        private Set getProjectFiles() {
            Set roots = new HashSet();
            Sources sources = ProjectUtils.getSources(project);
            
            // TODO add Sources.addChangeListener(this)
            SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            for (int i = 0; i<groups.length; i++) {
                SourceGroup group = groups[i];
                FileObject fo = group.getRootFolder();
                if (fo != null) {
                    FileObject [] files = fo.getChildren();
                    for (int j = 0; j < files.length; j++) {
                        FileObject file = files[j];
                        if (group.contains(file)) {
                            roots.add(file);
                        }
                    }
                }
            }
            return roots;
        }
        
        public Action[] getActions(boolean ignore) {
            return ModuleActions.getProjectActions(project);
        }
        
        public Image getIcon(int type) {
            return annotateIcon(super.getIcon(type), type);
        }
        
        public Image getOpenedIcon(int type) {
            return getIcon(type); // the same in the meantime
        }
        
        public boolean canRename() {
            return true;
        }
        
        public String getName() {
            return ProjectUtils.getInformation(project).getDisplayName();
        }
        
        public void setName(String name) {
            DefaultProjectOperations.performDefaultRenameOperation(project, name);
        }
        
    }
    
    /** Package private for unit tests. */
    static final String IMPORTANT_FILES_NAME = "important.files"; // NOI18N
    
    /** Accessor for SuiteLogicalView to create its important node which shall
     * be as similar as module's project one
     */
    static Node createImportantFilesNode(Children ch) {
        return new ImportantFilesNode(ch);
    }
    
    private static final class RootChildren extends Children.Keys implements ChangeListener {
        
        private static final String[] SOURCE_GROUP_TYPES = {
            JavaProjectConstants.SOURCES_TYPE_JAVA,
            NbModuleProject.SOURCES_TYPE_JAVAHELP,
        };
        
        private final NbModuleProject project;
        
        RootChildren(NbModuleProject project) {
            this.project = project;
        }
        
        protected void addNotify() {
            super.addNotify();
            refreshKeys();
            ProjectUtils.getSources(project).addChangeListener(this);
        }
        
        private void refreshKeys() {
            List l = new ArrayList();
            Sources s = ProjectUtils.getSources(project);
            for (int i = 0; i < SOURCE_GROUP_TYPES.length; i++) {
                SourceGroup[] groups = s.getSourceGroups(SOURCE_GROUP_TYPES[i]);
                l.addAll(Arrays.asList(groups));
            }
            SourceGroup javadocDocfiles = makeJavadocDocfilesSourceGroup();
            if (javadocDocfiles != null) {
                l.add(javadocDocfiles);
            }
            l.add(IMPORTANT_FILES_NAME);
            l.add(LibrariesNode.LIBRARIES_NAME);
            if(resolveFileObjectFromProperty("test.unit.src.dir") != null) { //NOI18N
                l.add(UnitTestLibrariesNode.UNIT_TEST_LIBRARIES_NAME);
            }
            setKeys(l);
        }
        
        private FileObject resolveFileObjectFromProperty(String property){
            String filename = project.evaluator().getProperty(property);
            if (filename == null) {
                return null;
            }
            return project.getHelper().resolveFileObject(filename);
        }

        protected void removeNotify() {
            ProjectUtils.getSources(project).removeChangeListener(this);
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
        }
        
        protected Node[] createNodes(Object key) {
            Node n;
            if (key instanceof SourceGroup) {
                n = PackageView.createPackageView((SourceGroup) key);
            } else if (key == IMPORTANT_FILES_NAME) {
                n = new ImportantFilesNode(project);
            } else if (key == LibrariesNode.LIBRARIES_NAME) {
                n = new LibrariesNode(project);
            } else if (key == UnitTestLibrariesNode.UNIT_TEST_LIBRARIES_NAME) {
                n = new UnitTestLibrariesNode(project);
            } else {
                throw new AssertionError("Unknown key: " + key);
            }
            return new Node[] {n};
        }
        
       
        private SourceGroup makeJavadocDocfilesSourceGroup() {
            String propname = "javadoc.docfiles"; // NOI18N
            FileObject root = resolveFileObjectFromProperty(propname);
            if(root == null) {
                return null;
            }
            return GenericSources.group(project, root, propname, NbBundle.getMessage(ModuleLogicalView.class, "LBL_extra_javadoc_files"), null, null);
        }

        public void stateChanged(ChangeEvent e) {
            refreshKeys();
        }
        
    }
    
    /**
     * Show node "Important Files" with various config and docs files beneath it.
     */
    static final class ImportantFilesNode extends AnnotatedNode {
        
        public ImportantFilesNode(NbModuleProject project) {
            super(new ImportantFilesChildren(project));
        }
        
        ImportantFilesNode(Children ch) {
            super(ch);
        }
        
        public String getName() {
            return IMPORTANT_FILES_NAME;
        }
        
        private Image getIcon(boolean opened) {
            Image badge = Utilities.loadImage("org/netbeans/modules/apisupport/project/resources/config-badge.gif", true);
            return Utilities.mergeImages(UIUtil.getTreeFolderIcon(opened), badge, 8, 8);
        }
        
        private static final String DISPLAY_NAME = NbBundle.getMessage(ModuleLogicalView.class, "LBL_important_files");
        
        public String getDisplayName() {
            return annotateName(DISPLAY_NAME);
        }
        
        public String getHtmlDisplayName() {
            return computeAnnotatedHtmlDisplayName(DISPLAY_NAME, getFiles());
        }
        
        public Image getIcon(int type) {
            return annotateIcon(getIcon(false), type);
        }
        
        public Image getOpenedIcon(int type) {
            return annotateIcon(getIcon(true), type);
        }
        
    }
    
    /**
     * Actual list of important files.
     */
    private static final class ImportantFilesChildren extends Children.Keys {
        
        private List visibleFiles = new ArrayList();
        private FileChangeListener fcl;
        
        /** Abstract location to display name. */
        private static final java.util.Map<String,String> FILES = new LinkedHashMap();
        static {
            FILES.put("${manifest.mf}", NbBundle.getMessage(ModuleLogicalView.class, "LBL_module_manifest"));
            FILES.put("${javadoc.arch}", NbBundle.getMessage(ModuleLogicalView.class, "LBL_arch_desc"));
            FILES.put("${javadoc.apichanges}", NbBundle.getMessage(ModuleLogicalView.class, "LBL_api_changes"));
            FILES.put("${javadoc.overview}", NbBundle.getMessage(ModuleLogicalView.class, "LBL_javadoc_overview"));
            FILES.put("build.xml", NbBundle.getMessage(ModuleLogicalView.class, "LBL_build.xml"));
            FILES.put("nbproject/project.xml", NbBundle.getMessage(ModuleLogicalView.class, "LBL_project.xml"));
            FILES.put("nbproject/project.properties", NbBundle.getMessage(ModuleLogicalView.class, "LBL_project.properties"));
            FILES.put("nbproject/private/private.properties", NbBundle.getMessage(ModuleLogicalView.class, "LBL_private.properties"));
            FILES.put("nbproject/suite.properties", NbBundle.getMessage(ModuleLogicalView.class, "LBL_suite.properties"));
            FILES.put("nbproject/private/suite-private.properties", NbBundle.getMessage(ModuleLogicalView.class, "LBL_suite-private.properties"));
            FILES.put("nbproject/platform.properties", NbBundle.getMessage(ModuleLogicalView.class, "LBL_platform.properties"));
            FILES.put("nbproject/private/platform-private.properties", NbBundle.getMessage(ModuleLogicalView.class, "LBL_platform-private.properties"));
        }
        
        private final NbModuleProject project;
        
        public ImportantFilesChildren(NbModuleProject project) {
            this.project = project;
        }
        
        protected void addNotify() {
            super.addNotify();
            attachListeners();
            refreshKeys();
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            removeListeners();
            super.removeNotify();
        }
        
        protected Node[] createNodes(Object key) {
            if (key instanceof String) {
                String loc = (String) key;
                String locEval = project.evaluator().evaluate(loc);
                FileObject file = project.getHelper().resolveFileObject(locEval);
                try {
                    Node orig = DataObject.find(file).getNodeDelegate();
                    return new Node[] {new SpecialFileNode(orig, (String) FILES.get(loc))};
                } catch (DataObjectNotFoundException e) {
                    throw new AssertionError(e);
                }
            } else if (key instanceof LayerUtils.LayerHandle) {
                return new Node[] {/* #68240 */ new SpecialFileNode(new LayerNode((LayerUtils.LayerHandle) key), null)};
            } else if (key instanceof ServiceNodeHandler) {
                return new Node[]{((ServiceNodeHandler)key).createServiceRootNode()};
            } else {
                throw new AssertionError(key);
            } 
        }
        
        private void refreshKeys() {
            Set<FileObject> files = new HashSet();
            List newVisibleFiles = new ArrayList();
            LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
            FileObject layerFile = handle.getLayerFile();
            if (layerFile != null) {
                newVisibleFiles.add(handle);
                files.add(layerFile);
            }
            Iterator it = FILES.keySet().iterator();
            while (it.hasNext()) {
                String loc = (String) it.next();
                String locEval = project.evaluator().evaluate(loc);
                if (locEval == null) {
                    newVisibleFiles.remove(loc); // XXX why?
                    continue;
                }
                FileObject file = project.getHelper().resolveFileObject(locEval);
                if (file != null) {
                    newVisibleFiles.add(loc);
                    files.add(file);
                }
            }
            if (!isInitialized() || !newVisibleFiles.equals(visibleFiles)) {
                visibleFiles = newVisibleFiles;
                visibleFiles.add(project.getLookup().lookup(ServiceNodeHandler.class));
                RP.post(new Runnable() { // #72471
                    public void run() {
                        setKeys(visibleFiles);
                    }
                });
                ((ImportantFilesNode) getNode()).setFiles(files);
            }
        }
        
        private void attachListeners() {
            try {
                if (fcl == null) {
                    fcl = new FileChangeAdapter() {
                        public void fileDataCreated(FileEvent fe) {
                            refreshKeys();
                        }
                        public void fileDeleted(FileEvent fe) {
                            refreshKeys();
                        }
                    };
                    project.getProjectDirectory().getFileSystem().addFileChangeListener(fcl);
                }
            } catch (FileStateInvalidException e) {
                assert false : e;
            }
        }
        
        private void removeListeners() {
            if (fcl != null) {
                try {
                    project.getProjectDirectory().getFileSystem().removeFileChangeListener(fcl);
                } catch (FileStateInvalidException e) {
                    assert false : e;
                }
                fcl = null;
            }
        }
        
    }
    
    /**
     * Node to represent some special file in a project.
     * Mostly just a wrapper around the normal data node.
     */
    static final class SpecialFileNode extends FilterNode {
        
        private final String displayName;
        
        public SpecialFileNode(Node orig, String displayName) {
            super(orig);
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            if (displayName != null) {
                return displayName;
            } else {
                return super.getDisplayName();
            }
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
        
        public String getHtmlDisplayName() {
            String result = null;
            DataObject dob = (DataObject) getLookup().lookup(DataObject.class);
            if (dob != null) {
                Set files = dob.files();
                result = computeAnnotatedHtmlDisplayName(getDisplayName(), files);
            }
            return result;
        }
        
    }
    
    /**
     * Annotates <code>htmlDisplayName</code>, if it is needed, and returns the
     * result; <code>null</code> otherwise.
     */
    private static String computeAnnotatedHtmlDisplayName(
            final String htmlDisplayName, final Set files) {
        
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
    
}
