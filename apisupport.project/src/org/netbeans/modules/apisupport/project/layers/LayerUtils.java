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
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.modules.xml.tax.parser.XMLParsingSupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
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
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
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
        private boolean saving;
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
                    //pcs.firePropertyChange(PROP_DOCUMENT_ROOT, null, root);
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
        public synchronized void save() throws IOException {
            if (root == null || !dirty) {
                return;
            }
            assert !saving;
            saving = true;
            try {
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
            } finally {
                saving = false;
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
        private synchronized void changed() {
            if (saving) {
                return;
            }
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
        private boolean autosave;
        
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
                    fs = new WritableXMLFileSystem(xml.getURL(), cookie = cookieForFile(xml), /*XXX*/null);
                } catch (FileStateInvalidException e) {
                    throw new AssertionError(e);
                }
                fs.addFileChangeListener(new FileChangeListener() {
                    public void fileAttributeChanged(FileAttributeEvent fe) {
                        changed();
                    }
                    public void fileChanged(FileEvent fe) {
                        changed();
                    }
                    public void fileDataCreated(FileEvent fe) {
                        changed();
                    }
                    public void fileDeleted(FileEvent fe) {
                        changed();
                    }
                    public void fileFolderCreated(FileEvent fe) {
                        changed();
                    }
                    public void fileRenamed(FileRenameEvent fe) {
                        changed();
                    }
                    private void changed() {
                        if (autosave) {
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
            return project.getCodeNameBase().replace('.', '/') + "/resources/layer.xml"; // NOI18N
        }
        
    }
    
    /**
     * Get a filesystem that will look like what this project would "see".
     * <p>There are four possibilities:</p>
     * <ol>
     * <li><p>For a standalone module project, the filesystem will include all the XML
     * layers from all modules in the selected platform, plus this module's XML layer
     * as the writable layer (use {@link LayerHandle#save} to save changes as needed).</p></li>
     * <li><p>For a module suite project, the filesystem will include all the XML layers
     * from all modules in the selected platform which are not excluded in the current
     * suite configuration, plus the XML layers for modules in the suite (currently all
     * read-only, i.e. the filesystem is read-only).</p></li>
     * <li><p>For a suite component module project, the filesystem will include all XML
     * layers from non-excluded platform modules, plus the XML layers for modules in the
     * suite, with this module's layer being writable.</p></li>
     * <li><p>For a netbeans.org module, the filesystem will include all XML layers
     * from all netbeans.org modules that are not in the <code>extra</code> cluster,
     * plus the layer from this module (if it is in the <code>extra</code> cluster,
     * with this module's layer always writable.</p></li>
     * </ol>
     * <p>Does not currently attempt to cache the result,
     * though that could be attempted later as needed.</p>
     * <p>Will try to produce pleasant-looking display names and/or icons for files.</p>
     * <p>Note that parsing XML layers is not terribly fast so it would be wise to show
     * a "please wait" label or some other simple progress indication while this
     * is being called, if blocking the UI.</p>
     * @param project a project of one of the three types enumerated above
     * @return the effective system filesystem seen by that project
     * @throws IOException if there were problems loading layers, etc.
     * @see "#62257"
     */
    public static FileSystem getEffectiveSystemFilesystem(Project project) throws IOException {
        if (project instanceof NbModuleProject) {
            NbModuleProject p = (NbModuleProject) project;
            NbModuleTypeProvider.NbModuleType type = ((NbModuleTypeProvider) p.getLookup().lookup(NbModuleTypeProvider.class)).getModuleType();
            FileSystem projectLayer = layerForProject(p).layer();
            if (type == NbModuleTypeProvider.STANDALONE) {
                Set/*<File>*/ jars = getPlatformJarsForStandaloneProject(p);
                FileSystem platformLayers = getPlatformLayers(jars);
                ClassPath cp = createLayerClasspath(Collections.singleton(p), jars);
                return mergeFilesystems(projectLayer, new FileSystem[] {platformLayers}, cp);
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
                List/*<FileSystem>*/ readOnlyLayers = new ArrayList();
                Set/*<Project>*/ modules = ((SubprojectProvider) suite.getLookup().lookup(SubprojectProvider.class)).getSubprojects();
                Iterator it = modules.iterator();
                while (it.hasNext()) {
                    NbModuleProject sister = (NbModuleProject) it.next();
                    if (sister == p) {
                        continue;
                    }
                    LayerHandle handle = layerForProject(sister);
                    if (handle.getLayerFile() == null) {
                        continue;
                    }
                    readOnlyLayers.add(handle.layer());
                }
                Set/*<File>*/ jars = getPlatformJarsForSuiteComponentProject(p, suite);
                readOnlyLayers.add(getPlatformLayers(jars));
                ClassPath cp = createLayerClasspath(modules, jars);
                return mergeFilesystems(projectLayer, (FileSystem[]) readOnlyLayers.toArray(new FileSystem[readOnlyLayers.size()]), cp);
            } else if (type == NbModuleTypeProvider.NETBEANS_ORG) {
                Set/*<NbModuleProject>*/ projects = getProjectsForNetBeansOrgProject(p);
                List/*<URL>*/ otherLayerURLs = new ArrayList();
                Iterator it = projects.iterator();
                while (it.hasNext()) {
                    NbModuleProject p2 = (NbModuleProject) it.next();
                    ManifestManager mm = ManifestManager.getInstance(p2.getManifest(), false);
                    String layer = mm.getLayer();
                    if (layer == null) {
                        continue;
                    }
                    FileObject src = p2.getSourceDirectory();
                    if (src == null) {
                        continue;
                    }
                    FileObject layerXml = src.getFileObject(layer);
                    if (layerXml == null) {
                        continue;
                    }
                    otherLayerURLs.add(layerXml.getURL());
                }
                XMLFileSystem xfs = new XMLFileSystem();
                try {
                    xfs.setXmlUrls((URL[]) otherLayerURLs.toArray(new URL[otherLayerURLs.size()]));
                } catch (PropertyVetoException ex) {
                    assert false : ex;
                }
                ClassPath cp = createLayerClasspath(projects, Collections.EMPTY_SET);
                return mergeFilesystems(projectLayer, new FileSystem[] {xfs}, cp);
            } else {
                throw new AssertionError(type);
            }
        } else if (project instanceof SuiteProject) {
            SuiteProject p = (SuiteProject) project;
            throw new AssertionError("XXX not yet implemented");
        } else {
            throw new IllegalArgumentException(project.toString());
        }
    }
    
    /**
     * Get the platform JARs associated with a standalone module project.
     */
    static Set/*<File>*/ getPlatformJarsForStandaloneProject(NbModuleProject project) {
        NbPlatform platform = project.getPlatform();
        return getPlatformJars(platform, null, null);
    }
    
    static Set/*<File>*/ getPlatformJarsForSuiteComponentProject(NbModuleProject project, SuiteProject suite) {
        NbPlatform platform = suite.getActivePlatform();
        PropertyEvaluator eval = suite.getEvaluator();
        String[] excludedClusters = SuiteProperties.getArrayProperty(eval, SuiteProperties.DISABLED_CLUSTERS_PROPERTY);
        String[] excludedModules = SuiteProperties.getArrayProperty(eval, SuiteProperties.DISABLED_MODULES_PROPERTY);
        return getPlatformJars(platform, excludedClusters, excludedModules);
    }
    
    static Set/*<NbModuleProject>*/ getProjectsForNetBeansOrgProject(NbModuleProject project) throws IOException {
        ModuleList list = project.getModuleList();
        ModuleEntry myself = list.getEntry(project.getCodeNameBase());
        assert myself != null : project;
        Set/*<NbModuleProject>*/ projects = new HashSet();
        projects.add(project);
        Iterator it = list.getAllEntries().iterator();
        while (it.hasNext()) {
            ModuleEntry other = (ModuleEntry) it.next();
            if (other.getClusterDirectory().getName().equals("extra")) { // NOI18N
                continue;
            }
            File root = other.getSourceLocation();
            assert root != null : other;
            NbModuleProject p2 = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(root));
            if (p2 == null) {
                continue;
            }
            projects.add(p2);
        }
        return projects;
    }
    
    /**
     * Finds all the module JARs in the platform.
     * Can optionally pass non-null lists of cluster names and module CNBs to exclude, as per suite properties.
     */
    private static Set/*<File>*/ getPlatformJars(NbPlatform platform, String[] excludedClusters, String[] excludedModules) {
        Set/*<String>*/ excludedClustersS = (excludedClusters != null) ? new HashSet(Arrays.asList(excludedClusters)) : Collections.EMPTY_SET;
        Set/*<String>*/ excludedModulesS = (excludedModules != null) ? new HashSet(Arrays.asList(excludedModules)) : Collections.EMPTY_SET;
        ModuleEntry[] entries = platform.getModules();
        Set/*<File>*/ jars = new HashSet(entries.length);
        for (int i = 0; i < entries.length; i++) {
            if (excludedClustersS.contains(entries[i].getClusterDirectory().getName())) {
                continue;
            }
            if (excludedModulesS.contains(entries[i].getCodeNameBase())) {
                continue;
            }
            jars.add(entries[i].getJarLocation());
        }
        return jars;
    }

    /**
     * Constructs a filesystem representing the merged XML layers of the supplied platform module JARs.
     */
    private static FileSystem getPlatformLayers(Set/*<File>*/ platformJars) throws IOException {
        List/*<URL>*/ urls = new ArrayList();
        Iterator it = platformJars.iterator();
        while (it.hasNext()) {
            File jar = (File) it.next();
            ManifestManager mm = ManifestManager.getInstanceFromJAR(jar);
            String layer = mm.getLayer();
            if (layer != null) {
                urls.add(new URL("jar:" + jar.toURI() + "!/" + layer)); // NOI18N
            }
        }
        XMLFileSystem fs = new XMLFileSystem();
        try {
            // XXX properly speaking should topo sort by module deps so overrides work, but forget it
            // XXX nbres: and such URL protocols may not work in platform layers
            // (cf. org.openide.filesystems.ExternalUtil.findClass)
            fs.setXmlUrls((URL[]) urls.toArray(new URL[urls.size()]));
        } catch (PropertyVetoException ex) {
            assert false : ex;
        }
        return fs;
    }
    
    /**
     * Creates a classpath representing the source roots and platform binary JARs for a project/suite.
     */
    static ClassPath createLayerClasspath(Set/*<NbModuleProject>*/ moduleProjects, Set/*<File>*/ platformJars) throws IOException {
        List/*<URL>*/ roots = new ArrayList();
        Iterator it = moduleProjects.iterator();
        while (it.hasNext()) {
            NbModuleProject p = (NbModuleProject) it.next();
            FileObject src = p.getSourceDirectory();
            if (src != null) {
                roots.add(src.getURL());
            }
        }
        it = platformJars.iterator();
        while (it.hasNext()) {
            File jar = (File) it.next();
            roots.add(FileUtil.getArchiveRoot(jar.toURI().toURL()));
        }
        // XXX in principle, could add CP extensions from modules... but probably not necessary
        return ClassPathSupport.createClassPath((URL[]) roots.toArray(new URL[roots.size()]));
    }

    /**
     * Create a merged filesystem from one writable layer (may be null) and some read-only layers.
     * You should also pass a classpath that can be used to look up resource bundles and icons.
     */
    private static FileSystem mergeFilesystems(FileSystem writableLayer, FileSystem[] readOnlyLayers, final ClassPath cp) {
        if (writableLayer == null) {
            writableLayer = new XMLFileSystem();
        }
        final FileSystem[] layers = new FileSystem[readOnlyLayers.length + 1];
        layers[0] = writableLayer;
        System.arraycopy(readOnlyLayers, 0, layers, 1, readOnlyLayers.length);
        class BadgingMergedFileSystem extends MultiFileSystem {
            private final BadgingSupport status;
            public BadgingMergedFileSystem() {
                super(layers);
                status = new BadgingSupport(this);
                status.setClasspath(cp);
                // XXX listening?
                // XXX loc/branding suffix?
            }
            public FileSystem.Status getStatus() {
                return status;
            }
        }
        return new BadgingMergedFileSystem();
    }
    
}
