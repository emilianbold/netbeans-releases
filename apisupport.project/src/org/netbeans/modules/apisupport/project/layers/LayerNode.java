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

package org.netbeans.modules.apisupport.project.layers;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Displays two views of a layer.
 * @author Jesse Glick
 */
public final class LayerNode extends FilterNode {
    
    public LayerNode(LayerUtils.LayerHandle handle) {
        super(getDataNode(handle), new LayerChildren(handle));
    }
    
    private static Node getDataNode(LayerUtils.LayerHandle handle) {
        FileObject layer = handle.getLayerFile();
        try {
            return DataObject.find(layer).getNodeDelegate();
        } catch (DataObjectNotFoundException e) {
            assert false : e;
            return Node.EMPTY;
        }
    }
    
    private static final class LayerChildren extends Children.Keys {
        
        private static final Object KEY_RAW = "raw"; // NOI18N
        private static final Object KEY_CONTEXTUALIZED = "contextualized"; // NOI18N
        
        private final LayerUtils.LayerHandle handle;
        private ClassPath cp;
        private NbModuleProject p;
        
        public LayerChildren(LayerUtils.LayerHandle handle) {
            this.handle = handle;
        }
        
        protected void addNotify() {
            super.addNotify();
            handle.setAutosave(true);
            setKeys(new Object[] {KEY_RAW, KEY_CONTEXTUALIZED});
            FileObject layer = handle.getLayerFile();
            p = (NbModuleProject) FileOwnerQuery.getOwner(layer);
            assert p != null : layer;
            try {
                cp = createClasspath(p);
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            cp = null;
            p = null;
            super.removeNotify();
        }
        
        protected Node[] createNodes(Object key) {
            try {
                if (key == KEY_RAW) {
                    FileSystem fs = badge(handle.layer(false), cp, handle.getLayerFile(), "<this layer>"); // XXX I18N
                    return new Node[] {DataObject.find(fs.getRoot()).getNodeDelegate()};
                } else if (key == KEY_CONTEXTUALIZED) {
                    FileSystem fs = badge(LayerUtils.getEffectiveSystemFilesystem(p), cp, handle.getLayerFile(), "<this layer in context>"); // XXX I18N
                    return new Node[] {DataObject.find(fs.getRoot()).getNodeDelegate()};
                } else {
                    throw new AssertionError(key);
                }
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
                return new Node[0];
            }
        }
        
    }
    
    /**
     * Add badging support to the plain layer.
     */
    private static FileSystem badge(final FileSystem base, final ClassPath cp, final FileObject layer, final String rootLabel) {
        class BadgingMergedFileSystem extends MultiFileSystem {
            private final BadgingSupport status;
            public BadgingMergedFileSystem() {
                super(new FileSystem[] {base});
                status = new BadgingSupport(this);
                status.addFileStatusListener(new FileStatusListener() {
                    public void annotationChanged(FileStatusEvent ev) {
                        fireFileStatusChanged(ev);
                    }
                });
                status.setClasspath(cp);
                // XXX loc/branding suffix?
            }
            public FileSystem.Status getStatus() {
                return new FileSystem.Status() {
                    public String annotateName(String name, Set files) {
                        if (files.size() == 1 && ((FileObject) files.iterator().next()).isRoot()) {
                            return rootLabel;
                        } else {
                            return status.annotateName(name, files);
                        }
                    }
                    public Image annotateIcon(Image icon, int iconType, Set files) {
                        return status.annotateIcon(icon, iconType, files);
                    }
                };
            }
            public String getDisplayName() {
                return FileUtil.getFileDisplayName(layer);
            }
            public SystemAction[] getActions(Set/*<FileObject>*/ foSet) {
                return new SystemAction[] {
                    SystemAction.get(PickIconAction.class),
                };
            }
        }
        return new BadgingMergedFileSystem();
        /* XXX loc/branding suffix possibilities:
        Matcher m = Pattern.compile("(.*" + "/)?[^_/.]+(_[^/.]+)?(\\.[^/]+)?").matcher(u);
        assert m.matches() : u;
        suffix = m.group(2);
        if (suffix == null) {
            suffix = "";
        }
        status.setSuffix(suffix);
         */
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(LayerNode.class, "LayerNode_label");
    }
    
    /**
     * Make a runtime classpath indicative of what is accessible from a sample resource.
     */
    private static ClassPath createClasspath(NbModuleProject p) throws IOException {
        NbModuleTypeProvider.NbModuleType type = ((NbModuleTypeProvider) p.getLookup().lookup(NbModuleTypeProvider.class)).getModuleType();
        if (type == NbModuleTypeProvider.STANDALONE) {
            return LayerUtils.createLayerClasspath(Collections.singleton(p), LayerUtils.getPlatformJarsForStandaloneProject(p));
        } else if (type == NbModuleTypeProvider.SUITE_COMPONENT) {
            SuiteProvider suiteProv = (SuiteProvider) p.getLookup().lookup(SuiteProvider.class);
            assert suiteProv != null : p;
            File suiteDir = suiteProv.getSuiteDirectory();
            if (suiteDir == null || !suiteDir.isDirectory()) {
                throw new IOException("Could not locate suite for " + p); // NOI18N
            }
            SuiteProject suite = (SuiteProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(suiteDir));
            if (suite == null) {
                throw new IOException("Could not load suite for " + p + " from " + suiteDir); // NOI18N
            }
            Set/*<Project>*/ modules = ((SubprojectProvider) suite.getLookup().lookup(SubprojectProvider.class)).getSubprojects();
            return LayerUtils.createLayerClasspath(modules, LayerUtils.getPlatformJarsForSuiteComponentProject(p, suite));
        } else if (type == NbModuleTypeProvider.NETBEANS_ORG) {
            return LayerUtils.createLayerClasspath(LayerUtils.getProjectsForNetBeansOrgProject(p), Collections.EMPTY_SET);
        } else {
            throw new AssertionError(type);
        }
    }

}
