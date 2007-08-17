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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.layers.LayerNode;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.metainf.ServiceNodeHandler;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
public class ImportantFilesNodeFactory implements NodeFactory {
    /** Package private for unit tests. */
    static final String IMPORTANT_FILES_NAME = "important.files"; // NOI18N
    /** Package private for unit tests only. */
    static final RequestProcessor RP = new RequestProcessor();
    
    /** Creates a new instance of ImportantFilesNodeFactory */
    public ImportantFilesNodeFactory() {
    }
    
    public NodeList createNodes(Project p) {
        return new ImpFilesNL(p);
    }

    private static class ImpFilesNL implements NodeList<String> {
        private Project project;
        
        public ImpFilesNL(Project p) {
            project = p;
        }
    
        public List<String> keys() {
            return Collections.singletonList(IMPORTANT_FILES_NAME);
        }

        public void addChangeListener(ChangeListener l) {
            //ignore, doesn't change
        }

        public void removeChangeListener(ChangeListener l) {
            //ignore, doesn't change
        }

        public Node node(String key) {
            assert key == IMPORTANT_FILES_NAME;
            if (project instanceof NbModuleProject) {
                return new ImportantFilesNode((NbModuleProject)project);
            }
            if (project instanceof SuiteProject) {
                return new ImportantFilesNode(new SuiteImportantFilesChildren((SuiteProject)project));
            }
            return null;
        }

        public void addNotify() {
        }

        public void removeNotify() {
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
    
    public static Node createLayerNode(Project prj) {
        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(prj);
        if (handle != null && handle.getLayerFile() != null) {
            return new SpecialFileNode(new LayerNode(handle), null);
        }
        return null;
    }
    
    /**
     * Actual list of important files.
     */
    private static final class ImportantFilesChildren extends Children.Keys<Object> {
        
        private List<Object> visibleFiles = new ArrayList<Object>();
        private FileChangeListener fcl;
        
        /** Abstract location to display name. */
        private static final java.util.Map<String,String> FILES = new LinkedHashMap<String,String>();
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
            setKeys(Collections.<String>emptyList());
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
                    return new Node[] {new SpecialFileNode(orig, FILES.get(loc))};
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
            Set<FileObject> files = new HashSet<FileObject>();
            List<Object> newVisibleFiles = new ArrayList<Object>();
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
            DataObject dob = getLookup().lookup(DataObject.class);
            if (dob != null) {
                Set<FileObject> files = dob.files();
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
    private static final class SuiteImportantFilesChildren extends Children.Keys<String> {
        
        private List<String> visibleFiles = new ArrayList<String>();
        private FileChangeListener fcl;
        
        /** Abstract location to display name. */
        private static final java.util.Map<String,String> FILES = new LinkedHashMap<String,String>();
        static {
            FILES.put("master.jnlp", NbBundle.getMessage(SuiteLogicalView.class,"LBL_jnlp_master"));
            FILES.put("build.xml", NbBundle.getMessage(SuiteLogicalView.class,"LBL_build.xml"));
            FILES.put("nbproject/project.properties", NbBundle.getMessage(SuiteLogicalView.class,"LBL_project.properties"));
            FILES.put("nbproject/private/private.properties", NbBundle.getMessage(SuiteLogicalView.class,"LBL_private.properties"));
            FILES.put("nbproject/platform.properties", NbBundle.getMessage(SuiteLogicalView.class,"LBL_platform.properties"));
            FILES.put("nbproject/private/platform-private.properties", NbBundle.getMessage(SuiteLogicalView.class,"LBL_platform-private.properties"));
        }
        
        private final SuiteProject project;
        
        public SuiteImportantFilesChildren(SuiteProject project) {
            this.project = project;
        }
        
        protected void addNotify() {
            super.addNotify();
            attachListeners();
            refreshKeys();
        }
        
        protected void removeNotify() {
            setKeys(Collections.<String>emptySet());
            removeListeners();
            super.removeNotify();
        }
        
        protected Node[] createNodes(String loc) {
            String locEval = project.getEvaluator().evaluate(loc);
            FileObject file = project.getHelper().resolveFileObject(locEval);
            
            try {
                Node orig = DataObject.find(file).getNodeDelegate();
                return new Node[] {new SpecialFileNode(orig, FILES.get(loc))};
            } catch (DataObjectNotFoundException e) {
                throw new AssertionError(e);
            }
        }
        
        private void refreshKeys() {
            List<String> newVisibleFiles = new ArrayList<String>();
            Iterator it = FILES.keySet().iterator();
            Set<FileObject> files = new HashSet<FileObject>();
            while (it.hasNext()) {
                String loc = (String) it.next();
                String locEval = project.getEvaluator().evaluate(loc);
                if (locEval == null) {
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
                RP.post(new Runnable() { // #72471
                    public void run() {
                        setKeys(visibleFiles);
                    }
                });
                ((ImportantFilesNode) getNode()).setFiles(files); // #72439
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
    
}
