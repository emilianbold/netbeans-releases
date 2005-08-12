/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;
import javax.swing.Action;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.layers.LayerNode;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Provides a logical view of a NetBeans module project.
 * @author Jesse Glick
 */
public final class ModuleLogicalView implements LogicalViewProvider {
    
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
    
    private static final class RootNode extends AbstractNode implements Runnable, FileStatusListener {
        
        private final NbModuleProject project;
        
        private Set files;
        private Set fileSystemListeners;
        private RequestProcessor.Task task;
        
        public RootNode(NbModuleProject project) {
            // XXX add a NodePathResolver impl to lookup
            super(new RootChildren(project), Lookups.fixed(new Object[] {project}));
            this.project = project;
            setIconBaseWithExtension("org/netbeans/modules/apisupport/project/resources/module.gif"); // NOI18N
            ProjectInformation pi = ProjectUtils.getInformation(project);
            setName(pi.getName());
            setDisplayName(pi.getDisplayName());
            setShortDescription("Project in " + FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath());
            
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
            Sources sources = (Sources) project.getLookup().lookup(Sources.class);
            
            // TODO add Sources.addChengeListener(this)
            SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            for (int i = 0; i<groups.length; i++) {
                SourceGroup group = groups[i];
                FileObject fo = group.getRootFolder();
                if (fo != null) {
                    FileObject [] files = fo.getChildren();
                    for (int j = 0; j < files.length; j++) {
                        FileObject file = files[j];
                        if (group.contains(file)) roots.add(file);
                    }
                }
            }
            return roots;
        }
        
        protected final void setFiles(Set files) {
            fileSystemListeners = new HashSet();
            this.files = files;
            if (files == null) return;
            
            Iterator it = files.iterator();
            Set hookedFileSystems = new HashSet();
            while (it.hasNext()) {
                FileObject fo = (FileObject) it.next();
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (hookedFileSystems.contains(fs)) {
                        continue;
                    }
                    hookedFileSystems.add(fs);
                    FileStatusListener fsl = FileUtil.weakFileStatusListener(this, fs);
                    fs.addFileStatusListener(fsl);
                    fileSystemListeners.add(fsl);
                } catch (FileStateInvalidException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, "Can not get " + fo + " filesystem, ignoring...");  // NO18N
                    err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        
        
        public Action[] getActions(boolean ignore) {
            return ModuleActions.getProjectActions(project);
        }
        
        public java.awt.Image getIcon(int type) {
            java.awt.Image img = super.getIcon(type);
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            return img;
        }
        
        public java.awt.Image getOpenedIcon(int type) {
            java.awt.Image img = super.getIcon(type);
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            return img;
        }
        
        public void run() {
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, null);
        }
        
        public void annotationChanged(FileStatusEvent event) {
            if (task == null) {
                task = RequestProcessor.getDefault().create(this);
            }
            
            // any change in project's files/folders names or icons can change project's name or icon
            Iterator it = files.iterator();
            while (it.hasNext()) {
                FileObject fo = (FileObject) it.next();
                if (event.hasChanged(fo)) {
                    task.schedule(100);  // batch by 100 ms
                    break;
                }
            }
        }
    }
    
    private static final String IMPORTANT_FILES_NAME = "important.files";
    
    private static final class RootChildren extends Children.Keys {
        
        private static final String[] SOURCE_GROUP_TYPES = {
            JavaProjectConstants.SOURCES_TYPE_JAVA,
                    "javahelp", // NOI18N
        };
        
        private final NbModuleProject project;
        
        RootChildren(NbModuleProject project) {
            this.project = project;
        }
        
        protected void addNotify() {
            super.addNotify();
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
            setKeys(l);
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
        }
        
        protected Node[] createNodes(Object key) {
            Node n;
            if (key instanceof SourceGroup) {
                n = PackageView.createPackageView((SourceGroup) key);
            } else if (key == IMPORTANT_FILES_NAME) {
                n = new ImportantFilesNode(project);
            } else {
                throw new AssertionError("Unknown key: " + key);
            }
            return new Node[] {n};
        }
        
        private SourceGroup makeJavadocDocfilesSourceGroup() {
            String propname = "javadoc.docfiles"; // NOI18N
            String prop = project.evaluator().getProperty(propname);
            if (prop == null) {
                return null;
            }
            FileObject root = project.getHelper().resolveFileObject(prop);
            if (root == null) {
                return null;
            }
            return GenericSources.group(project, root, propname, "Extra Javadoc Files", null, null); // XXX I18N
        }
        
    }
    
    /**
     * Show node "Important Files" with various config and docs files beneath it.
     */
    private static final class ImportantFilesNode extends AbstractNode implements Runnable, FileStatusListener {
        
        private Set files;
        private Set fileSystemListeners;
        private RequestProcessor.Task task;
        private volatile boolean iconChange;
        private volatile boolean nameChange;
        
        public ImportantFilesNode(NbModuleProject project) {
            super(new ImportantFilesChildren(project));
        }
        
        protected final void setFiles(Set files) {
            fileSystemListeners = new HashSet();
            this.files = files;
            if (files == null) return;
            
            Iterator it = files.iterator();
            Set hookedFileSystems = new HashSet();
            while (it.hasNext()) {
                FileObject fo = (FileObject) it.next();
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (hookedFileSystems.contains(fs)) {
                        continue;
                    }
                    hookedFileSystems.add(fs);
                    FileStatusListener fsl = FileUtil.weakFileStatusListener(this, fs);
                    fs.addFileStatusListener(fsl);
                    fileSystemListeners.add(fsl);
                } catch (FileStateInvalidException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, "Can not get " + fo + " filesystem, ignoring...");  // NO18N
                    err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        
        public String getName() {
            return IMPORTANT_FILES_NAME;
        }
        
        private Image getIcon(boolean opened) {
            // XXX consider instead using UIManager.get("Nb.Explorer.Folder.openedIcon")
            Image base = Utilities.loadImage("org/netbeans/modules/apisupport/project/resources/defaultFolder" + (opened ? "Open" : "") + ".gif", true);
            Image badge = Utilities.loadImage("org/netbeans/modules/apisupport/project/resources/config-badge.gif", true);
            return Utilities.mergeImages(base, badge, 8, 8);
        }
        
        // XXX I18N
        private static final String DISPLAY_NAME = "Important Files";
        
        public String getDisplayName() {
            String s = DISPLAY_NAME;
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    s = fo.getFileSystem().getStatus().annotateName(s, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            return s;
        }
        
        public String getHtmlDisplayName() {
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    FileSystem.Status stat = fo.getFileSystem().getStatus();
                    if (stat instanceof FileSystem.HtmlStatus) {
                        FileSystem.HtmlStatus hstat = (FileSystem.HtmlStatus) stat;
                        
                        String result = hstat.annotateNameHtml(DISPLAY_NAME, files);
                        
                        // Make sure the super string was really modified (XXX why?)
                        if (!DISPLAY_NAME.equals(result)) {
                            return result;
                        }
                    }
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            return null;
        }
        
        public java.awt.Image getIcon(int type) {
            java.awt.Image img = getIcon(false);
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            return img;
        }
        
        public java.awt.Image getOpenedIcon(int type) {
            java.awt.Image img = getIcon(true);
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            return img;
        }
        
        public void run() {
            if (iconChange) {
                fireIconChange();
                fireOpenedIconChange();
                iconChange = false;
            }
            if (nameChange) {
                fireDisplayNameChange(null, null);
                nameChange = false;
            }
        }
        
        public void annotationChanged(FileStatusEvent event) {
            if (task == null) {
                task = RequestProcessor.getDefault().create(this);
            }
            
            if ((iconChange == false && event.isIconChange())  || (nameChange == false && event.isNameChange())) {
                Iterator it = files.iterator();
                while (it.hasNext()) {
                    FileObject fo = (FileObject) it.next();
                    if (event.hasChanged(fo)) {
                        iconChange |= event.isIconChange();
                        nameChange |= event.isNameChange();
                    }
                }
            }
            
            task.schedule(50);  // batch by 50 ms
        }
        
        
    }
    
    /**
     * Actual list of important files.
     */
    private static final class ImportantFilesChildren extends Children.Keys {
        
        /** Abstract location to display name. */
        private static final java.util.Map/*<String,String>*/ FILES = new LinkedHashMap();
        static {
            // XXX I18N
            FILES.put("${manifest.mf}", "Module Manifest");
            FILES.put("${javadoc.arch}", "Architecture Description");
            FILES.put("${javadoc.apichanges}", "API Changes");
            FILES.put("${javadoc.overview}", "Javadoc Overview");
            FILES.put("build.xml", "Build Script");
            FILES.put("nbproject/project.xml", "Project Metadata");
            FILES.put("nbproject/project.properties", "Project Properties");
            FILES.put("nbproject/private/private.properties", "Per-user Project Properties");
            FILES.put("nbproject/suite.properties", "Suite Locator");
            FILES.put("nbproject/private/suite-private.properties", "Per-user Suite Locator");
            FILES.put("nbproject/platform.properties", "NetBeans Platform Config");
            FILES.put("nbproject/private/platform-private.properties", "Per-user NetBeans Platform Config");
        }
        
        private final NbModuleProject project;
        
        public ImportantFilesChildren(NbModuleProject project) {
            this.project = project;
        }
        
        protected void addNotify() {
            super.addNotify();
            List l = new ArrayList();
            {
                // Try to add the layer node.
                FileObject manifestXML = project.getManifestFile();
                if (manifestXML != null) {
                    try {
                        InputStream is = manifestXML.getInputStream();
                        try {
                            Manifest m = new Manifest(is);
                            String layerLoc = m.getMainAttributes().getValue("OpenIDE-Module-Layer");
                            if (layerLoc != null) {
                                FileObject src = project.getSourceDirectory();
                                if (src != null) {
                                    FileObject layer = src.getFileObject(layerLoc);
                                    if (layer != null) {
                                        l.add(layer);
                                    }
                                }
                            }
                        } finally {
                            is.close();
                        }
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
            Iterator it = FILES.keySet().iterator();
            Set files = new HashSet();
            while (it.hasNext()) {
                String loc = (String) it.next();
                String locEval = project.evaluator().evaluate(loc);
                if (locEval == null) {
                    continue;
                }
                FileObject file = project.getHelper().resolveFileObject(locEval);
                if (file != null) {
                    l.add(loc);
                    files.add(file);
                }
            }
            setKeys(l);
            ((ImportantFilesNode)getNode()).setFiles(files);
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
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
            } else if (key instanceof FileObject) {
                FileObject fo = (FileObject) key;
                try {
                    return new Node[] {new LayerNode(DataObject.find(fo))};
                } catch (DataObjectNotFoundException e) {
                    throw new AssertionError(e);
                }
            } else {
                throw new AssertionError(key);
            }
        }
        
    }
    
    /**
     * Node to represent some special file in a project.
     * Mostly just a wrapper around the normal data node.
     */
    private static final class SpecialFileNode extends FilterNode {
        
        private final String displayName;
        
        public SpecialFileNode(Node orig, String displayName) {
            super(orig);
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
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
        
    }
    
}
