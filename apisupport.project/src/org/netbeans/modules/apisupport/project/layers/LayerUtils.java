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

import java.io.IOException;
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
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;


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
     * Manages one project's XML layer.
     */
    public static final class LayerHandle {
        
        private final NbModuleProject project;
        private FileSystem fs;
        
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
                DataObject d;
                try {
                    d = DataObject.find(xml);
                } catch (DataObjectNotFoundException e) {
                    throw new AssertionError(e);
                }
                TreeEditorCookie cookie = cookieForDataObject(d);
                if (cookie == null) {
                    // Loaded by some other data loader?
                    Util.err.log(ErrorManager.WARNING, "No TreeEditorCookie for " + d);
                    return FileUtil.createMemoryFileSystem();
                }
                try {
                    fs = new WritableXMLFileSystem(xml.getURL(), cookie, true);
                } catch (FileStateInvalidException e) {
                    throw new AssertionError(e);
                }
            }
            return fs;
        }
        
        // Permit unit tests to override this, since it does not work without a lot of setup:
        public interface XmlDataObjectProvider {
            TreeEditorCookie cookieForDataObject(DataObject d);
        }
        public static XmlDataObjectProvider PROVIDER = new XmlDataObjectProvider() {
            public TreeEditorCookie cookieForDataObject(DataObject d) {
                return (TreeEditorCookie) d.getCookie(TreeEditorCookie.class);
            }
        };
        private static TreeEditorCookie cookieForDataObject(DataObject d) {
            return PROVIDER.cookieForDataObject(d);
        }
        
        /**
         * Save the layer, if it was in fact modified.
         * Note that nonempty layer entries you created will already be on disk.
         */
        public void save() throws IOException {
            FileObject xml = getLayerFile();
            if (xml == null) {
                throw new IOException("Cannot save a nonexistent layer"); // NOI18N
            }
            DataObject d = DataObject.find(xml);
            SaveCookie cookie = (SaveCookie) d.getCookie(SaveCookie.class);
            if (d.isModified() && cookie == null) {
                throw new IOException("Modified but no SaveCookie on " + d); // NOI18N
            }
            if (cookie != null) {
                cookie.save();
            }
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
