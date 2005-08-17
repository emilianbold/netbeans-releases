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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.modules.xml.tax.parser.XMLParsingSupport;
import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeObject;
import org.netbeans.tax.io.TreeStreamResult;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Task;
import org.xml.sax.InputSource;

/**
 * Misc support for dealing with layers.
 * @author Jesse Glick
 */
public class LayerUtils {
    
    private LayerUtils() {}
    
    /** translates nbres: into nbrescurr: for internal use... */
    static URL currentify(URL u, String suffix, ClassPath cp) {
        if (cp == null) {
            return u;
        }
        try {
            if (u.getProtocol().equals("nbres")) { // NOI18N
                String path = u.getFile();
                if (path.startsWith("/")) path = path.substring(1); // NOI18N
                FileObject fo = cp.findResource(path);
                if (fo != null) {
                    return fo.getURL();
                }
            } else if (u.getProtocol().equals("nbresloc")) { // NOI18N
                String path = u.getFile();
                if (path.startsWith("/")) path = path.substring(1); // NOI18N
                int idx = path.lastIndexOf('/');
                String folder;
                String nameext;
                if (idx == -1) {
                    folder = ""; // NOI18N
                    nameext = path;
                } else {
                    folder = path.substring(0, idx + 1);
                    nameext = path.substring(idx + 1);
                }
                idx = nameext.lastIndexOf('.');
                String name;
                String ext;
                if (idx == -1) {
                    name = nameext;
                    ext = ""; // NOI18N
                } else {
                    name = nameext.substring(0, idx);
                    ext = nameext.substring(idx);
                }
                List suffixes = new ArrayList(computeSubVariants(suffix));
                suffixes.add(suffix);
                Collections.reverse(suffixes);
                Iterator it = suffixes.iterator();
                while (it.hasNext()) {
                    String trysuffix = (String) it.next();
                    String trypath = folder + name + trysuffix + ext;
                    FileObject fo = cp.findResource(trypath);
                    if (fo != null) {
                        return fo.getURL();
                    }
                }
            }
        } catch (FileStateInvalidException fsie) {
            Util.err.notify(ErrorManager.WARNING, fsie);
        }
        return u;
    }
    
    // E.g. for name 'foo_f4j_ce_ja', should produce list:
    // 'foo', 'foo_ja', 'foo_f4j', 'foo_f4j_ja', 'foo_f4j_ce'
    // Will actually produce:
    // 'foo', 'foo_ja', 'foo_ce', 'foo_ce_ja', 'foo_f4j', 'foo_f4j_ja', 'foo_f4j_ce'
    // since impossible to distinguish locale from branding reliably.
    private static List/*<String>*/ computeSubVariants(String name) {
        int idx = name.indexOf('_');
        if (idx == -1) {
            return Collections.EMPTY_LIST;
        } else {
            String base = name.substring(0, idx);
            String suffix = name.substring(idx);
            List l = computeSubVariants(base, suffix);
            return l.subList(0, l.size() - 1);
        }
    }
    private static List/*<String>*/ computeSubVariants(String base, String suffix) {
        int idx = suffix.indexOf('_', 1);
        if (idx == -1) {
            List l = new LinkedList();
            l.add(base);
            l.add(base + suffix);
            return l;
        } else {
            String remainder = suffix.substring(idx);
            List l1 = computeSubVariants(base, remainder);
            List l2 = computeSubVariants(base + suffix.substring(0, idx), remainder);
            List l = new LinkedList(l1);
            l.addAll(l2);
            return l;
        }
    }
    
    // XXX needs to hold a strong ref only when modified, probably?
    private static final Map/*<NbModuleProject,LayerHandle>*/ layerHandleCache = new WeakHashMap();
    
    /**
     * Gets a handle for one project's XML layer.
     */
    public static LayerHandle layerForProject(NbModuleProject project) {
        LayerHandle handle = (LayerHandle) layerHandleCache.get(project);
        if (handle == null) {
            handle = new LayerHandle(project);
            layerHandleCache.put(project, handle);
        }
        return handle;
    }
    
    /**
     * Find the name of the external file that will be generated for a given
     * layer path if it is created with contents.
     * @param parent parent folder, or null
     * @param layerPath full path in layer
     * @return a simple file name
     */
    public static String findGeneratedName(FileObject parent, String layerPath) {
        Matcher m = Pattern.compile("(.+/)?([^/.]+)(\\.[^/]+)?").matcher(layerPath); // NOI18N
        assert m.matches() : layerPath;
        String base = m.group(2);
        String ext = m.group(3);
        if (ext == null) {
            ext = "";
        } else if (ext.equals(".java")) { // NOI18N
            ext = "_java"; // NOI18N
        } else if (ext.equals(".settings")) { // NOI18N
            ext = ".xml"; // NOI18N
        }
        String name = base + ext;
        if (parent == null || parent.getFileObject(name) == null) {
            return name;
        } else {
            for (int i = 1; true; i++) {
                name = base + '_' + i + ext;
                if (parent.getFileObject(name) == null) {
                    return name;
                }
            }
        }
    }
    
    /**
     * Representation of in-memory TAX tree which can be saved upon request.
     */
    interface SavableTreeEditorCookie extends TreeEditorCookie {
        
        /** property change fired when dirty flag changes */
        String PROP_DIRTY = "dirty"; // NOI18N
        
        /** true if there are in-memory mods */
        boolean isDirty();
        
        /** try to save any in-memory mods to disk */
        void save() throws IOException;
        
    }
    
    private static final class CookieImpl implements SavableTreeEditorCookie, FileChangeListener {
        private TreeDocumentRoot root;
        private boolean dirty;
        private Exception problem;
        private final FileObject f;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        public CookieImpl(FileObject f) {
            this.f = f;
            f.addFileChangeListener(FileUtil.weakFileChangeListener(this, f));
        }
        public TreeDocumentRoot getDocumentRoot() {
            return root;
        }
        public int getStatus() {
            if (problem != null) {
                return TreeEditorCookie.STATUS_ERROR;
            } else if (root != null) {
                return TreeEditorCookie.STATUS_OK;
            } else {
                return TreeEditorCookie.STATUS_NOT;
            }
        }
        public TreeDocumentRoot openDocumentRoot() throws IOException, TreeException {
            if (root == null) {
                try {
                    boolean oldDirty = dirty;
                    int oldStatus = getStatus();
                    root = new XMLParsingSupport().parse(new InputSource(f.getURL().toExternalForm()));
                    problem = null;
                    dirty = false;
                    pcs.firePropertyChange(PROP_DIRTY, oldDirty, false);
                    pcs.firePropertyChange(PROP_STATUS, oldStatus, TreeEditorCookie.STATUS_OK);
                    pcs.firePropertyChange(PROP_DOCUMENT_ROOT, null, root);
                } catch (IOException e) {
                    problem = e;
                    throw e;
                } catch (TreeException e) {
                    problem = e;
                    throw e;
                }
                ((TreeObject) root).addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        modified();
                    }
                });
            }
            return root;
        }
        public Task prepareDocumentRoot() {
            throw new UnsupportedOperationException();
        }
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        private void modified() {
            if (!dirty) {
                dirty = true;
                pcs.firePropertyChange(PROP_DIRTY, false, true);
            }
        }
        public boolean isDirty() {
            return dirty;
        }
        public void save() throws IOException {
            if (root == null && !dirty) {
                return;
            }
            FileLock lock = f.lock();
            try {
                OutputStream os = f.getOutputStream(lock);
                try {
                    new TreeStreamResult(os).getWriter(root).writeDocument();
                } catch (TreeException e) {
                    throw (IOException) new IOException(e.toString()).initCause(e);
                } finally {
                    os.close();
                }
            } finally {
                lock.releaseLock();
            }
            dirty = false;
            pcs.firePropertyChange(PROP_DIRTY, true, false);
        }
        public void fileChanged(FileEvent fe) {
            changed();
        }
        public void fileDeleted(FileEvent fe) {
            changed();
        }
        public void fileRenamed(FileRenameEvent fe) {
            changed();
        }
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore
        }
        public void fileFolderCreated(FileEvent fe) {
            assert false;
        }
        public void fileDataCreated(FileEvent fe) {
            assert false;
        }
        private void changed() {
            problem = null;
            dirty = false;
            root = null;
            pcs.firePropertyChange(PROP_DOCUMENT_ROOT, null, null);
        }
    }
    
    static SavableTreeEditorCookie cookieForFile(FileObject f) {
        return new CookieImpl(f);
    }
    
    /**
     * Manages one project's XML layer.
     */
    public static final class LayerHandle {
        
        private final NbModuleProject project;
        private FileSystem fs;
        private SavableTreeEditorCookie cookie;
        
        LayerHandle(NbModuleProject project) {
            this.project = project;
        }
        
        /**
         * Get the layer as a structured filesystem.
         * You can make whatever Filesystems API calls you like to it.
         * Just call {@link #save} when you are done so the modified XML document is saved
         * (or the user can save it explicitly if you don't).
         */
        public FileSystem layer() {
            if (fs == null) {
                FileObject xml = getLayerFile();
                if (xml == null) {
                    try {
                        // Check to see if the manifest entry is already specified.
                        String layerSrcPath = ManifestManager.getInstance(project.getManifest(), false).getLayer();
                        if (layerSrcPath == null) {
                            layerSrcPath = newLayerPath();
                            FileObject manifest = project.getManifestFile();
                            EditableManifest m = Util.loadManifest(manifest);
                            m.setAttribute(ManifestManager.OPENIDE_MODULE_LAYER, layerSrcPath, null);
                            Util.storeManifest(manifest, m);
                        }
                        xml = NbModuleProjectGenerator.createLayer(project.getProjectDirectory(), project.evaluator().getProperty("src.dir") + '/' + newLayerPath());
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                        return fs = FileUtil.createMemoryFileSystem();
                    }
                }
                try {
                    fs = new WritableXMLFileSystem(xml.getURL(), cookie = cookieForFile(xml), true);
                } catch (FileStateInvalidException e) {
                    throw new AssertionError(e);
                }
            }
            return fs;
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
            String path = ManifestManager.getInstance(project.getManifest(), false).getLayer();
            if (path == null) {
                return null;
            }
            return project.getSourceDirectory().getFileObject(path);
        }
        
        /**
         * Resource path in which to make a new XML layer.
         */
        private String newLayerPath() {
            return project.getCodeNameBase().replace('.', '/') + "/resources/layer.xml"; // NOI18N
        }
        
    }
    
}
