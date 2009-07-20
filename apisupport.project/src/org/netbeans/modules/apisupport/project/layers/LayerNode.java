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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project.layers;

import java.awt.Image;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.queries.ClassPathProviderImpl;
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
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.Environment;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 * Displays two views of a layer.
 * @author Jesse Glick
 */
public final class LayerNode extends FilterNode implements Node.Cookie {
    
    private final boolean specialDisplayName;

    public LayerNode(LayerUtils.LayerHandle handle) {
        this(getDataNode(handle), handle, true);
    }
    
    private LayerNode(Node delegate, LayerUtils.LayerHandle handle, boolean specialDisplayName) {
        super(delegate, new LayerChildren(handle));
        this.specialDisplayName = specialDisplayName;
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
    
    public static Environment.Provider createProvider() {
        class EP implements Environment.Provider {
            public Lookup getEnvironment(DataObject obj) {
                DataNode dn = new DataNode(obj, Children.LEAF);
                dn.setIconBaseWithExtension("org/netbeans/modules/apisupport/project/ui/resources/layerObject.gif"); // NOI18N
                LayerNode ln = new LayerNode(dn, new LayerUtils.LayerHandle(null, obj.getPrimaryFile()), false);
                return Lookups.singleton(ln);
            }
        }
        return new EP();
    }
    
    private static final class LayerChildren extends Children.Keys<LayerChildren.KeyType> {

        enum KeyType {WAIT, RAW, CONTEXTUALIZED}
        
        private final LayerUtils.LayerHandle handle;
        private ClassPath cp;
        private Project p;
        private FileSystem layerfs;
        private FileSystem sfs;
        
        public LayerChildren(LayerUtils.LayerHandle handle) {
            this.handle = handle;
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            handle.setAutosave(true);
            setKeys(Collections.singleton(KeyType.WAIT));
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        FileObject layer = handle.getLayerFile();
                        p = FileOwnerQuery.getOwner(layer);
                        assert p != null : layer;
                        try {
                            cp = createClasspath(p);
                        } catch (IOException e) {
                            Util.err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                        // just this project's source path, whole cp is too slow
                        ClassPathProviderImpl cppi = p.getLookup().lookup(ClassPathProviderImpl.class);
                        ClassPath srcPath = cppi.findClassPath(p.getProjectDirectory(), ClassPath.SOURCE);
                        layerfs = handle.layer(false, srcPath);
                        setKeys(Arrays.asList(KeyType.RAW, KeyType.WAIT));
                        Project p = FileOwnerQuery.getOwner(handle.getLayerFile());
                        boolean context = false;
                        if (p != null) {
                            LayerUtils.LayerHandle h = LayerUtils.layerForProject(p);
                            h.setAutosave(true); // #135376
                            if (h != null && layer.equals(h.getLayerFile())) {
                                FileSystem _sfs = LayerUtils.getEffectiveSystemFilesystem(p);
                                if (cp != null) { // has not been removeNotify()d yet
                                    sfs = _sfs;
                                    setKeys(Arrays.asList(KeyType.RAW, KeyType.CONTEXTUALIZED));
                                    context = true;
                                }
                            }
                        }
                        if (!context) {
                            setKeys(Collections.singleton(KeyType.RAW));
                        }
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            });
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.<KeyType>emptySet());
            cp = null;
            p = null;
            layerfs = null;
            sfs = null;
            super.removeNotify();
        }
        
        protected Node[] createNodes(KeyType key) {
            try {
                switch (key) {
                case RAW:
                    FileSystem fs = badge(layerfs, cp, handle.getLayerFile(), NbBundle.getMessage(LayerNode.class, "LBL_this_layer"), null);
                    return new Node[] {DataObject.find(fs.getRoot()).getNodeDelegate()};
                case CONTEXTUALIZED:
                    fs = badge(sfs, cp, handle.getLayerFile(), NbBundle.getMessage(LayerNode.class, "LBL_this_layer_in_context"), handle.layer(false));
                    return new Node[] {DataObject.find(fs.getRoot()).getNodeDelegate()};
                case WAIT:
                    return new Node[] {new AbstractNode(Children.LEAF) {
                        public @Override String getDisplayName() {
                            return NbBundle.getMessage(LayerNode.class, "LayerNode_please_wait");
                        }
                    }};
                default:
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
            @Override
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
            @Override
            public String getDisplayName() {
                return FileUtil.getFileDisplayName(layer);
            }
            @Override
            public SystemAction[] getActions(Set<FileObject> foSet) {
                return new SystemAction[] {
                    SystemAction.get(PickNameAction.class),
                    SystemAction.get(PickIconAction.class),
                    SystemAction.get(OpenLayerFilesAction.class),
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
    
    @Override
    public String getDisplayName() {
        if (specialDisplayName) {
            return NbBundle.getMessage(LayerNode.class, "LayerNode_label");
        } else {
            return super.getDisplayName();
        }
    }
    
    /**
     * Make a runtime classpath indicative of what is accessible from a sample resource.
     */
    private static ClassPath createClasspath(Project p) throws IOException {
        NbModuleProvider.NbModuleType type = Util.getModuleType(p);
        if (type == NbModuleProvider.STANDALONE) {
            return LayerUtils.createLayerClasspath(Collections.singleton(p), LayerUtils.getPlatformJarsForStandaloneProject(p));
        } else if (type == NbModuleProvider.SUITE_COMPONENT) {
            SuiteProject suite = SuiteUtils.findSuite(p);
            if (suite == null) {
                throw new IOException("Could not load suite for " + p); // NOI18N
            }
            Set<NbModuleProject> modules = SuiteUtils.getSubProjects(suite);
            return LayerUtils.createLayerClasspath(modules, LayerUtils.getPlatformJarsForSuiteComponentProject(suite));
        } else if (type == NbModuleProvider.NETBEANS_ORG) {
            //Can cast to NbModuleProject here..
            return LayerUtils.createLayerClasspath(LayerUtils.getProjectsForNetBeansOrgProject((NbModuleProject)p), Collections.<File>emptySet());
        } else {
            throw new AssertionError(type);
        }
    }
    
}
