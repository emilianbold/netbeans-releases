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

package org.netbeans.modules.apisupport.project.layers;

import java.awt.Image;
import java.io.CharConversionException;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.xml.XMLUtil;

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
        
        private static final String KEY_WAIT = "wait"; // NOI18N
        private static final Object KEY_RAW = "raw"; // NOI18N
        private static final Object KEY_CONTEXTUALIZED = "contextualized"; // NOI18N
        
        private final LayerUtils.LayerHandle handle;
        private ClassPath cp;
        private NbModuleProject p;
        private FileSystem layerfs;
        private FileSystem sfs;
        
        public LayerChildren(LayerUtils.LayerHandle handle) {
            this.handle = handle;
        }
        
        protected void addNotify() {
            super.addNotify();
            handle.setAutosave(true);
            setKeys(new Object[] {KEY_WAIT});
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        FileObject layer = handle.getLayerFile();
                        p = (NbModuleProject) FileOwnerQuery.getOwner(layer);
                        assert p != null : layer;
                        try {
                            cp = createClasspath(p);
                        } catch (IOException e) {
                            Util.err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                        layerfs = handle.layer(false);
                        setKeys(new Object[] {KEY_RAW, KEY_WAIT});
                        FileSystem _sfs = LayerUtils.getEffectiveSystemFilesystem(p);
                        if (cp != null) { // has not been removeNotify()d yet
                            sfs = _sfs;
                            setKeys(new Object[] {KEY_RAW, KEY_CONTEXTUALIZED});
                        }
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            });
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            cp = null;
            p = null;
            layerfs = null;
            sfs = null;
            super.removeNotify();
        }
        
        protected Node[] createNodes(Object key) {
            try {
                if (key == KEY_RAW) {
                    FileSystem fs = badge(layerfs, cp, handle.getLayerFile(), NbBundle.getMessage(LayerNode.class, "LBL_this_layer"), null);
                    return new Node[] {DataObject.find(fs.getRoot()).getNodeDelegate()};
                } else if (key == KEY_CONTEXTUALIZED) {
                    FileSystem fs = badge(sfs, cp, handle.getLayerFile(), NbBundle.getMessage(LayerNode.class, "LBL_this_layer_in_context"), handle.layer(false));
                    return new Node[] {DataObject.find(fs.getRoot()).getNodeDelegate()};
                } else if (key == KEY_WAIT) {
                    return new Node[] {new AbstractNode(Children.LEAF) {
                        public String getName() {
                            return KEY_WAIT;
                        }
                        public String getDisplayName() {
                            return NbBundle.getMessage(LayerNode.class, "LayerNode_please_wait");
                        }
                    }};
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
    private static FileSystem badge(final FileSystem base, final ClassPath cp, final FileObject layer, final String rootLabel, final FileSystem highlighted) {
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
                addFileChangeListener(new FileChangeListener() { // #65564
                    private void fire() {
                        fireFileStatusChanged(new FileStatusEvent(BadgingMergedFileSystem.this, true, true));
                    }
                    public void fileAttributeChanged(FileAttributeEvent fe) {
                        fire();
                    }
                    public void fileChanged(FileEvent fe) {
                        fire();
                    }
                    public void fileDataCreated(FileEvent fe) {
                        fire();
                    }
                    public void fileDeleted(FileEvent fe) {
                        fire();
                    }
                    public void fileFolderCreated(FileEvent fe) {
                        fire();
                    }
                    public void fileRenamed(FileRenameEvent fe) {
                        fire();
                    }
                });
            }
            public FileSystem.Status getStatus() {
                return new FileSystem.HtmlStatus() {
                    public String annotateNameHtml(String name, Set files) {
                        String nonHtmlLabel = status.annotateName(name, files);
                        if (files.size() == 1 && ((FileObject) files.iterator().next()).isRoot()) {
                            nonHtmlLabel = rootLabel;
                        }
                        String htmlLabel;
                        try {
                            htmlLabel = XMLUtil.toElementContent(nonHtmlLabel);
                        } catch (CharConversionException e) {
                            Util.err.notify(ErrorManager.INFORMATIONAL, e);
                            htmlLabel = nonHtmlLabel;
                        }
                        if (highlighted != null) {
                            // Boldface resources which do come from this project.
                            boolean local = false;
                            Iterator it = files.iterator();
                            while (it.hasNext()) {
                                FileObject f = (FileObject) it.next();
                                if (!f.isRoot() && highlighted.findResource(f.getPath()) != null) {
                                    local = true;
                                    break;
                                }
                            }
                            if (local) {
                                htmlLabel = "<b>" + htmlLabel + "</b>"; // NOI18N
                            }
                        }
                        return htmlLabel;
                    }
                    public String annotateName(String name, Set files) {
                        // Complex to explain why this is even called, but it is.
                        // Weird b/c hacks in the way DataNode.getHtmlDisplayName works.
                        return name;
                    }
                    public Image annotateIcon(Image icon, int iconType, Set files) {
                        return status.annotateIcon(icon, iconType, files);
                    }
                };
            }
            public String getDisplayName() {
                return FileUtil.getFileDisplayName(layer);
            }
            public SystemAction[] getActions(Set<FileObject> foSet) {
                return new SystemAction[] {
                    SystemAction.get(PickNameAction.class),
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
        NbModuleProvider.NbModuleType type = Util.getModuleType(p);
        if (type == NbModuleProvider.STANDALONE) {
            return LayerUtils.createLayerClasspath(Collections.singleton(p), LayerUtils.getPlatformJarsForStandaloneProject(p));
        } else if (type == NbModuleProvider.SUITE_COMPONENT) {
            SuiteProject suite = SuiteUtils.findSuite(p);
            if (suite == null) {
                throw new IOException("Could not load suite for " + p); // NOI18N
            }
            Set<NbModuleProject> modules = SuiteUtils.getSubProjects(suite);
            return LayerUtils.createLayerClasspath(modules, LayerUtils.getPlatformJarsForSuiteComponentProject(p, suite));
        } else if (type == NbModuleProvider.NETBEANS_ORG) {
            return LayerUtils.createLayerClasspath(LayerUtils.getProjectsForNetBeansOrgProject(p), Collections.EMPTY_SET);
        } else {
            throw new AssertionError(type);
        }
    }
    
}
