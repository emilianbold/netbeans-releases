/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.layers.LayerUtils.SavableTreeEditorCookie;
import org.netbeans.modules.apisupport.project.layers.WritableXMLFileSystem;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.xml.sax.SAXException;

/**
 * Manages one project's XML layer.
 */
public final class LayerHandle {

    // XXX needs to hold a strong ref only when modified, probably?
    private static final Map<Project,LayerHandle> layerHandleCache = new WeakHashMap<Project,LayerHandle>();

    /**
     * Gets a handle for one project's XML layer.
     */
    public static LayerHandle forProject(Project project) {
        LayerHandle handle = layerHandleCache.get(project);
        if (handle == null) {
            handle = new LayerHandle(project, null);
            layerHandleCache.put(project, handle);
        }
        return handle;
    }

    private final Project project;
    private final FileObject layerXML;
    private FileSystem fs;
    private SavableTreeEditorCookie cookie;
    private boolean autosave;

    public LayerHandle(Project project, FileObject layerXML) {
        //System.err.println("new LayerHandle for " + project);
        this.project = project;
        this.layerXML = layerXML;
    }

    /**
     * Get the layer as a structured filesystem.
     * You can make whatever Filesystems API calls you like to it.
     * Just call {@link #save} when you are done so the modified XML document is saved
     * (or the user can save it explicitly if you don't).
     * If there is a {@code META-INF/generated-layer.xml} this will be included as well.
     * @param create if true, and there is no layer yet, create it now; if false, just return null
     */
    public FileSystem layer(boolean create) {
        return layer(create, null);
    }

    /**
     * Get the layer as a structured filesystem.
     * See {@link #layer(boolean)} for details.
     * @param create see {@link #layer(boolean)} for details
     * @param cp optional classpath to search for resources specified with <code>nbres:</code>
     *  or <code>nbresloc:</code> parameter; default is <code>null</code>
     */
    public synchronized FileSystem layer(boolean create, ClassPath cp) {
        if (fs == null) {
            FileObject xml = getLayerFile();
            if (xml == null) {
                if (!create) {
                    return new DualLayers(null);
                }
                try {
                    NbModuleProvider module = project.getLookup().lookup(NbModuleProvider.class);
                    FileObject manifest = module.getManifestFile();
                    if (manifest != null) { // #121056
                        // Check to see if the manifest entry is already specified.
                        String layerSrcPath = ManifestManager.getInstance(Util.getManifest(manifest), false).getLayer();
                        if (layerSrcPath == null) {
                            layerSrcPath = newLayerPath();
                            EditableManifest m = Util.loadManifest(manifest);
                            m.setAttribute(ManifestManager.OPENIDE_MODULE_LAYER, layerSrcPath, null);
                            Util.storeManifest(manifest, m);
                        }
                    }
                    xml = NbModuleProjectGenerator.createLayer(project.getProjectDirectory(), module.getResourceDirectoryPath(false) + '/' + newLayerPath());
                } catch (IOException e) {
                    Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    return fs = FileUtil.createMemoryFileSystem();
                }
            }
            try {
                fs = new DualLayers(new WritableXMLFileSystem(xml.getURL(), cookie = LayerUtils.cookieForFile(xml), cp));
            } catch (FileStateInvalidException e) {
                throw new AssertionError(e);
            }
            cookie.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    //System.err.println("changed in mem");
                    if (autosave && SavableTreeEditorCookie.PROP_DIRTY.equals(evt.getPropertyName())) {
                        //System.err.println("  will save...");
                        try {
                            save();
                        } catch (IOException e) {
                            Util.err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }
                }
            });
        }
        return fs;
    }

    private final class DualLayers extends MultiFileSystem implements FileChangeListener {
        private final FileSystem explicit;
        private final File generated;
        DualLayers(FileSystem explicit) {
            this.explicit = explicit;
            if (project instanceof NbModuleProject) {
                generated = new File(((NbModuleProject) project).getClassesDirectory(), ManifestManager.GENERATED_LAYER_PATH);
                FileUtil.addFileChangeListener(this, generated);
            } else {
                // XXX currently NbModuleProvider does not define location of target/classes
                generated = null;
            }
            configure();
        }
        private void configure() {
            List<FileSystem> layers = new ArrayList<FileSystem>(2);
            if (explicit != null) {
                layers.add(explicit);
            }
            if (generated != null && generated.isFile()) {
                try {
                    layers.add(new XMLFileSystem(generated.toURI().toString()));
                } catch (SAXException x) {
                    Logger.getLogger(DualLayers.class.getName()).log(Level.INFO, "could not load " + generated, x);
                }
            }
            setDelegates(layers.toArray(new FileSystem[layers.size()]));
        }
        public @Override void fileDataCreated(FileEvent fe) {
            configure();
        }
        public @Override void fileChanged(FileEvent fe) {
            configure();
        }
        public @Override void fileDeleted(FileEvent fe) {
            configure();
        }
        public @Override void fileRenamed(FileRenameEvent fe) {
            configure(); // ???
        }
        public @Override void fileFolderCreated(FileEvent fe) {}
        public @Override void fileAttributeChanged(FileAttributeEvent fe) {}
    }

    /**
     * Save the layer, if it was in fact modified.
     * Note that nonempty layer entries you created will already be on disk.
     */
    public void save() throws IOException {
        if (cookie == null) {
            throw new IOException("Cannot save a nonexistent layer"); // NOI18N
        }
        cookie.save();
    }

    /**
     * Find the XML layer file for this project, if it exists.
     * @return the layer, or null
     */
    public FileObject getLayerFile() {
        if (layerXML != null) {
            return layerXML;
        }
        NbModuleProvider module = project.getLookup().lookup(NbModuleProvider.class);
        if (module == null) { // #126939: other project type
            return null;
        }
        Manifest mf = Util.getManifest(module.getManifestFile());
        if (mf == null) {
            return null;
        }
        String path = ManifestManager.getInstance(mf, false).getLayer();
        if (path == null) {
            return null;
        }
        FileObject ret = Util.getResourceDirectory(project);
        return ret == null ? null : ret.getFileObject(path);
    }

    /**
     * Set whether to automatically save changes to disk.
     * @param true to save changes immediately, false to save only upon request
     */
    public void setAutosave(boolean autosave) {
        this.autosave = autosave;
        if (autosave && cookie != null) {
            try {
                cookie.save();
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }

    /**
     * Check whether this handle is currently in autosave mode.
     */
    public boolean isAutosave() {
        return autosave;
    }

    /**
     * Resource path in which to make a new XML layer.
     */
    private String newLayerPath() {
        NbModuleProvider module = project.getLookup().lookup(NbModuleProvider.class);
        FileObject manifest = module.getManifestFile();
        if (manifest != null) {
            String bundlePath = ManifestManager.getInstance(Util.getManifest(manifest), false).getLocalizingBundle();
            if (bundlePath != null) {
                return bundlePath.replaceFirst("/[^/]+$", "/layer.xml"); // NOI18N
            }
        }
        return module.getCodeNameBase().replace('.', '/') + "/layer.xml"; // NOI18N
    }

    public @Override String toString() {
        FileObject layer = getLayerFile();
        if (layer != null) {
            return FileUtil.getFileDisplayName(layer);
        } else {
            return FileUtil.getFileDisplayName(project.getProjectDirectory());
        }
    }

}
