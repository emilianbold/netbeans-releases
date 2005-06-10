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

package org.netbeans.modules.apisupport.project.universe;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.netbeans.modules.apisupport.project.*;

/**
 * Represents list of known modules.
 * @author Jesse Glick
 */
public final class ModuleList {
    
    static final String DEST_DIR_IN_NETBEANS_ORG = "nbbuild" + File.separatorChar + "netbeans"; // NOI18N
    /** Synch with org.netbeans.nbbuild.ModuleListParser.DEPTH_NB_ALL */
    private static final int DEPTH_NB_ALL = 3;
    
    /**
     * Cache of source-derived lists, by source root.
     */
    private static final Map/*<File,ModuleList>*/ sourceLists = new HashMap();
    /**
     * Cache of binary-derived lists, by binary root (~ dest dir).
     */
    private static final Map/*<File,ModuleList>*/ binaryLists = new HashMap();
    /**
     * Map from netbeans.org source roots, to cluster definitions,
     * where a cluster definition is from netbeans.org relative source path
     * to physical cluster directory.
     */
    private static final Map/*<File,Map<String,String>>*/ clusterLocations = new HashMap();
    
    /** All entries known to exist for a given included file path. */
    private static final Map/*<File,Set<Entry>>*/ knownEntries = new HashMap();

    /**
     * Find the list of modules associated with a project (itself, others in its suite, others in its platform, or others in netbeans.org).
     * <p>Do not cache the result; always call this method fresh, in case {@link #refresh} has been called.
     * @param basedir the project directory to start in
     * @return a module list
     */
    public static synchronized ModuleList getModuleList(File basedir) throws IOException {
        Element data = parseData(basedir);
        if (data == null) {
            throw new IOException("Not an NBM project in " + basedir); // NOI18N
        }
        boolean suiteComponent = Util.findElement(data, "suite-component", NbModuleProjectType.NAMESPACE_SHARED) != null; // NOI18N
        boolean standalone = Util.findElement(data, "standalone", NbModuleProjectType.NAMESPACE_SHARED) != null; // NOI18N
        assert !(suiteComponent && standalone) : basedir;
        if (suiteComponent) {
            PropertyEvaluator eval = parseProperties(basedir, null, true, false, "irrelevant"); // NOI18N
            String suiteS = eval.getProperty("suite.dir");
            if (suiteS == null) {
                throw new IOException("No suite.dir defined from " + basedir); // NOI18N
            }
            File suite = PropertyUtils.resolveFile(basedir, suiteS);
            return findOrCreateModuleListFromSuite(suite);
        } else if (standalone) {
            return findOrCreateModuleListFromStandaloneModule(basedir);
        } else {
            // netbeans.org module.
            File nbroot = findNetBeansOrg(basedir);
            if (nbroot == null) {
                throw new IOException("Could not find netbeans.org CVS root from " + basedir + "; note that 3rd-level modules (a/b/c) are permitted at the maximum"); // NOI18N
            }
            return findOrCreateModuleListFromNetBeansOrgSources(nbroot);
        }
    }
    
    /**
     * Find the known module entries which build to a given built file path (e.g. module JAR).
     * Applies to any entries which have been scanned.
     * @param file some file built as part of the module
     * @return a set of entries thought to build to this file (may be empty but not null)
     */
    public static Set/*<Entry>*/ getKnownEntries(File file) {
        synchronized (knownEntries) {
            Set/*<Entry>*/ entries = (Set) knownEntries.get(file);
            if (entries != null) {
                return new HashSet(entries);
            } else {
                return Collections.EMPTY_SET;
            }
        }
    }
    
    private static void registerEntry(ModuleEntry entry, Set/*<File>*/ files) {
        synchronized (knownEntries) {
            Iterator it = files.iterator();
            while (it.hasNext()) {
                File f = (File) it.next();
                Set/*<Entry>*/ entries = (Set) knownEntries.get(f);
                if (entries == null) {
                    entries = new HashSet();
                    knownEntries.put(f, entries);
                }
                entries.add(entry);
            }
        }
    }
    
    static ModuleList findOrCreateModuleListFromNetBeansOrgSources(File root) throws IOException {
        ModuleList list = (ModuleList) sourceLists.get(root);
        if (list == null) {
            list = createModuleListFromNetBeansOrgSources(root);
            sourceLists.put(root, list);
        }
        return list;
    }
    
    private static ModuleList createModuleListFromNetBeansOrgSources(File root) throws IOException {
        Util.err.log("ModuleList.createModuleListFromSources: " + root);
        File nbdestdir = new File(root, DEST_DIR_IN_NETBEANS_ORG);
        Map/*<String,Entry>*/ entries = new HashMap();
        doScanNetBeansOrgSources(entries, root, DEPTH_NB_ALL, root, nbdestdir, null);
        return new ModuleList(entries);
    }
    
    private static void doScanNetBeansOrgSources(Map/*<String,Entry>*/ entries, File dir, int depth,
            File root, File nbdestdir, String pathPrefix) throws IOException {
        if (depth == 0) {
            return;
        }
        File[] kids = dir.listFiles();
        if (kids == null) {
            return;
        }
        for (int i = 0; i < kids.length; i++) {
            if (!kids[i].isDirectory()) {
                continue;
            }
            String newPathPrefix = (pathPrefix != null) ? pathPrefix + "/" + kids[i].getName() : kids[i].getName();
            scanPossibleProject(kids[i], entries, false, false, root, nbdestdir, newPathPrefix);
            doScanNetBeansOrgSources(entries, kids[i], depth - 1, root, nbdestdir, newPathPrefix);
        }
    }
    
    private static void scanPossibleProject(File basedir, Map/*<String,Entry>*/ entries,
            boolean suiteComponent, boolean standalone, File root, File nbdestdir, String path) throws IOException {
        Element data = parseData(basedir);
        if (data == null) {
            return;
        }
        assert root != null ^ (standalone || suiteComponent);
        assert path != null ^ (standalone || suiteComponent);
        String cnb = Util.findText(Util.findElement(data, "code-name-base", NbModuleProjectType.NAMESPACE_SHARED)); // NOI18N
        PropertyEvaluator eval = parseProperties(basedir, root, suiteComponent, standalone, cnb);
        String cluster = eval.getProperty("cluster.dir"); // NOI18N
        String module = eval.getProperty("module.jar"); // NOI18N
        // Cf. ParseProjectXml.computeClasspath:
        StringBuffer cpextra = new StringBuffer();
        Iterator/*<Element>*/ exts = Util.findSubElements(data).iterator();
        while (exts.hasNext()) {
            Element ext = (Element) exts.next();
            if (!ext.getLocalName().equals("class-path-extension")) { // NOI18N
                continue;
            }
            Element binaryOrigin = Util.findElement(ext, "binary-origin", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
            String text;
            if (binaryOrigin != null) {
                text = Util.findText(binaryOrigin);
            } else {
                Element runtimeRelativePath = Util.findElement(ext, "runtime-relative-path", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                assert runtimeRelativePath != null : "Malformed <class-path-extension> in " + basedir;
                String reltext = Util.findText(runtimeRelativePath);
                // XXX assumes that module.jar is not overridden independently of module.jar.dir:
                text = "${netbeans.dest.dir}/${cluster.dir}/${module.jar.dir}/" + reltext;
            }
            String evaluated = eval.evaluate(text);
            if (evaluated == null) {
                continue;
            }
            File binary = PropertyUtils.resolveFile(basedir, evaluated);
            cpextra.append(':');
            cpextra.append(binary.getAbsolutePath());
        }
        File manifest = new File(basedir, "manifest.mf"); // NOI18N
        ManifestManager mm = (manifest.isFile() ? 
            ManifestManager.getInstance(manifest, false) : ManifestManager.NULL_INSTANCE);
        ModuleEntry entry;
        if (!suiteComponent && !standalone) {
            entry = new NetBeansOrgEntry(root, cnb, path, cluster, module, cpextra.toString(), 
                    mm.getReleaseVersion(), mm.getSpecificationVersion(),
                    ProjectXMLManager.findPublicPackages(data), mm.isDeprecated());
        } else {
            File clusterDir = PropertyUtils.resolveFile(basedir, eval.evaluate("${netbeans.dest.dir}/${cluster.dir}"));
            entry = new ExternalEntry(basedir, cnb, clusterDir, PropertyUtils.resolveFile(clusterDir, module),
                    cpextra.toString(), nbdestdir, mm.getReleaseVersion(), mm.getSpecificationVersion(),
                    ProjectXMLManager.findPublicPackages(data), mm.isDeprecated());
        }
        if (entries.containsKey(cnb)) {
            Util.err.log(ErrorManager.WARNING, "Warning: two modules found with the same code name base (" + cnb + "): " + entries.get(cnb) + " and " + entry);
        } else {
            entries.put(cnb, entry);
        }
        registerEntry(entry, findSourceNBMFiles(entry, eval));
    }
    
    /**
     * Look for files to be included in the NBM.
     * Some stock entries are always present: the module JAR, update_tracking/*.xml, config/Modules/*.xml,
     * config/ModuleAutoDeps/*.xml, ant/nblib/*.jar, modules/docs/*.jar (cf. common.xml#files-init).
     * Additionally, ${extra.module.files} if defined is parsed. Literal entries (no wildcards) are
     * always included; entries with Ant-style wildcards are included if matches can be found on disk.
     */
    private static Set/*<File>*/ findSourceNBMFiles(ModuleEntry entry, PropertyEvaluator eval) throws IOException {
        Set/*<File>*/ files = new HashSet();
        files.add(entry.getJarLocation());
        File cluster = entry.getClusterDirectory();
        String cnbd = entry.getCodeNameBase().replace('.', '-');
        String[] STANDARD_FILES = {
            "update_tracking/*.xml", // NOI18N
            "config/Modules/*.xml", // NOI18N
            "config/ModuleAutoDeps/*.xml", // NOI18N
            "ant/nblib/*.jar", // NOI18N
            "modules/docs/*.jar", // NOI18N
        };
        for (int i = 0; i < STANDARD_FILES.length; i++) {
            int x = STANDARD_FILES[i].indexOf('*');
            findSourceNBMFilesMaybeAdd(files, cluster, STANDARD_FILES[i].substring(0, x) + cnbd + STANDARD_FILES[i].substring(x + 1));
        }
        String emf = eval.getProperty("extra.module.files"); // NOI18N
        if (emf != null) {
            String[] entries = emf.split(" *, *"); // NOI18N
            for (int i = 0; i < entries.length; i++) {
                String pattern = entries[i];
                if (pattern.endsWith("/")) { // NOI18N
                    // Shorthand for /**
                    pattern += "**"; // NOI18N
                }
                if (pattern.indexOf('*') == -1) {
                    // Literal file location relative to cluster dir.
                    findSourceNBMFilesMaybeAdd(files, cluster, pattern);
                } else {
                    // Wildcard. Convert to regexp and do a brute-force search.
                    // Not the most efficient option but should probably suffice.
                    String regex = "\\Q" + pattern.replaceAll("\\*\\*", "__DBLASTERISK__"). // NOI18N
                                                   replaceAll("\\*", "\\\\E[^/]*\\\\Q"). // NOI18N
                                                   replaceAll("__DBLASTERISK__", "\\\\E.*\\\\Q") + "\\E"; // NOI18N
                    Pattern regexp = Pattern.compile(regex);
                    String[] clusterFiles = scanDirForFiles(cluster);
                    for (int j = 0; j < clusterFiles.length; j++) {
                        if (regexp.matcher(clusterFiles[j]).matches()) {
                            findSourceNBMFilesMaybeAdd(files, cluster, clusterFiles[j]);
                        }
                    }
                }
            }
        }
        return files;
    }
    private static void findSourceNBMFilesMaybeAdd(Set/*<File>*/ files, File cluster, String path) {
        File f = new File(cluster, path.replace('/', File.separatorChar));
        if (f.isFile()) {
            files.add(f);
        }
    }
    private static final Map/*<File,String[]>*/ DIR_SCAN_CACHE = new HashMap();
    private static String[] scanDirForFiles(File dir) {
        String[] files = (String[]) DIR_SCAN_CACHE.get(dir);
        if (files == null) {
            List/*<File>*/ l = new ArrayList(250);
            doScanDirForFiles(dir, l, "");
            files = (String[]) l.toArray(new String[l.size()]);
        }
        return files;
    }
    private static void doScanDirForFiles(File d, List/*<File>*/ files, String prefix) {
        File[] kids = d.listFiles();
        if (kids != null) {
            for (int i = 0; i < kids.length; i++) {
                File f = kids[i];
                if (f.isFile()) {
                    files.add(prefix + f.getName());
                } else if (f.isDirectory()) {
                    doScanDirForFiles(f, files, prefix + f.getName() + '/');
                }
            }
        }
    }
    
    public static ModuleList findOrCreateModuleListFromSuite(File root) throws IOException {
        PropertyEvaluator eval = parseSuiteProperties(root);
        String nbdestdirS = eval.getProperty("netbeans.dest.dir"); // NOI18N
        if (nbdestdirS == null) {
            throw new IOException("No netbeans.dest.dir defined in " + root); // NOI18N
        }
        File nbdestdir = PropertyUtils.resolveFile(root, nbdestdirS);
        return merge(new ModuleList[] {
            findOrCreateModuleListFromSuiteWithoutBinaries(root, nbdestdir, eval),
            findOrCreateModuleListFromBinaries(nbdestdir),
        });
    }
    
    private static ModuleList findOrCreateModuleListFromSuiteWithoutBinaries(File root, File nbdestdir, PropertyEvaluator eval) throws IOException {
        ModuleList sources = (ModuleList) sourceLists.get(root);
        if (sources == null) {
            Map/*<String,Entry>*/ entries = new HashMap();
            File[] modules = findModulesInSuite(root, eval);
            for (int i = 0; i < modules.length; i++) {
                scanPossibleProject(modules[i], entries, true, false, null, nbdestdir, null);
            }
            sources = new ModuleList(entries);
            sourceLists.put(root, sources);
        }
        return sources;
    }
    
    static ModuleList findOrCreateModuleListFromSuiteWithoutBinaries(File root) throws IOException {
        PropertyEvaluator eval = parseSuiteProperties(root);
        String nbdestdirS = eval.getProperty("netbeans.dest.dir"); // NOI18N
        if (nbdestdirS == null) {
            throw new IOException("No netbeans.dest.dir defined in " + root); // NOI18N
        }
        File nbdestdir = PropertyUtils.resolveFile(root, nbdestdirS);
        return findOrCreateModuleListFromSuiteWithoutBinaries(root, nbdestdir, eval);
    }
    
    private static PropertyEvaluator parseSuiteProperties(File root) throws IOException {
        Map/*<String,String>*/ predefs = new HashMap(System.getProperties());
        predefs.put("basedir", root.getAbsolutePath()); // NOI18N
        PropertyProvider predefsProvider = PropertyUtils.fixedPropertyProvider(predefs);
        List/*<PropertyProvider>*/ providers = new ArrayList();
        providers.add(loadPropertiesFile(new File(root, "nbproject" + File.separatorChar + "private" + File.separatorChar + "platform-private.properties"))); // NOI18N
        providers.add(loadPropertiesFile(new File(root, "nbproject" + File.separatorChar + "platform.properties"))); // NOI18N
        PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
        String buildS = eval.getProperty("user.properties.file"); // NOI18N
        if (buildS != null) {
            providers.add(loadPropertiesFile(PropertyUtils.resolveFile(root, buildS)));
        } else {
            // Never been opened, perhaps - so fake it.
            providers.add(PropertyUtils.globalPropertyProvider());
        }
        providers.add(loadPropertiesFile(new File(root, "nbproject" + File.separatorChar + "private" + File.separatorChar + "private.properties"))); // NOI18N
        providers.add(loadPropertiesFile(new File(root, "nbproject" + File.separatorChar + "project.properties"))); // NOI18N
        eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
        String platformS = eval.getProperty("nbplatform.active"); // NOI18N
        if (platformS != null) {
            providers.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap("netbeans.dest.dir", "${nbplatform." + platformS + ".netbeans.dest.dir}"))); // NOI18N
        }
        return PropertyUtils.sequentialPropertyEvaluator(predefsProvider, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
    }
    
    static File[] findModulesInSuite(File root) throws IOException {
        return findModulesInSuite(root, parseSuiteProperties(root));
    }
    
    private static File[] findModulesInSuite(File root, PropertyEvaluator eval) throws IOException {
        String modulesS = eval.getProperty("modules"); // NOI18N
        if (modulesS == null) {
            modulesS = ""; // NOI18N
        }
        String[] modulesA = PropertyUtils.tokenizePath(modulesS);
        File[] modules = new File[modulesA.length];
        for (int i = 0; i < modulesA.length; i++) {
            modules[i] = PropertyUtils.resolveFile(root, modulesA[i]);
        }
        return modules;
    }
    
    private static ModuleList findOrCreateModuleListFromStandaloneModule(File basedir) throws IOException {
        PropertyEvaluator eval = parseProperties(basedir, null, false, true, "irrelevant"); // NOI18N
        String nbdestdirS = eval.getProperty("netbeans.dest.dir"); // NOI18N
        if (nbdestdirS == null) {
            throw new IOException("No netbeans.dest.dir defined in " + basedir); // NOI18N
        }
        if (nbdestdirS.indexOf("${") != -1) { // NOI18N
            throw new IOException("Unevaluated properties in " + nbdestdirS + " from " + basedir + "; probably means platform definitions not loaded correctly"); // NOI18N
        }
        File nbdestdir = PropertyUtils.resolveFile(basedir, nbdestdirS);
        ModuleList binaries = findOrCreateModuleListFromBinaries(nbdestdir);
        ModuleList sources = (ModuleList) sourceLists.get(basedir);
        if (sources == null) {
            Map/*<String,Entry>*/ entries = new HashMap();
            scanPossibleProject(basedir, entries, false, true, null, nbdestdir, null);
            if (entries.isEmpty()) {
                throw new IOException("No module in " + basedir); // NOI18N
            }
            sources = new ModuleList(entries);
            sourceLists.put(basedir, sources);
        }
        return merge(new ModuleList[] {sources, binaries});
    }
    
    static ModuleList findOrCreateModuleListFromBinaries(File root) throws IOException {
        ModuleList list = (ModuleList) binaryLists.get(root);
        if (list == null) {
            list = createModuleListFromBinaries(root);
            binaryLists.put(root, list);
        }
        return list;
    }
    
    private static final String[] MODULE_DIRS = {
        "modules", // NOI18N
        "modules/eager", // NOI18N
        "modules/autoload", // NOI18N
        "lib", // NOI18N
        "core", // NOI18N
    };
    private static ModuleList createModuleListFromBinaries(File root) throws IOException {
        Util.err.log("ModuleList.createModuleListFromBinaries: " + root);
        // Loosely copied from o.n.nbbuild.ModuleListParser
        Map/*<String,Entry>*/ entries = new HashMap();
        File[] clusters = root.listFiles();
        if (clusters == null) {
            throw new IOException("Cannot examine dir " + root); // NOI18N
        }
        for (int i = 0; i < clusters.length; i++) {
            for (int j = 0; j < MODULE_DIRS.length; j++) {
                File dir = new File(clusters[i], MODULE_DIRS[j].replace('/', File.separatorChar));
                if (!dir.isDirectory()) {
                    continue;
                }
                File[] jars = dir.listFiles();
                if (jars == null) {
                    throw new IOException("Cannot examine dir " + dir); // NOI18N
                }
                for (int k = 0; k < jars.length; k++) {
                    File m = jars[k];
                    if (!m.getName().endsWith(".jar")) { // NOI18N
                        continue;
                    }
                    ManifestManager mm = ManifestManager.getInstanceFromJAR(m);
                    String codenamebase = mm.getCodeNameBase();
                    if (codenamebase == null) {
                            continue;
                    }
                    String cp = mm.getClassPath();
                    File[] exts;
                    if (cp == null) {
                        exts = new File[0];
                    } else {
                        String[] pieces = cp.trim().split(" +"); // NOI18N
                        exts = new File[pieces.length];
                        for (int l = 0; l < pieces.length; l++) {
                            exts[l] = new File(dir, pieces[l].replace('/', File.separatorChar));
                        }
                    }
                    ModuleEntry entry = new BinaryEntry(codenamebase, m, exts, root, clusters[i], 
                            mm.getReleaseVersion(), mm.getSpecificationVersion(),
                            mm.getPublicPackages(), mm.isDeprecated());
                    if (entries.containsKey(codenamebase)) {
                        Util.err.log(ErrorManager.WARNING, "Warning: two modules found with the same code name base (" + codenamebase + "): " + entries.get(codenamebase) + " and " + entry);
                    } else {
                        entries.put(codenamebase, entry);
                    }
                    registerEntry(entry, findBinaryNBMFiles(clusters[i], codenamebase, m));
                }
            }
        }
        return new ModuleList(entries);
    }
    
    /**
     * Try to find which files are part of a module's binary build (i.e. slated for NBM).
     * Tries to scan update tracking for the file, but also always adds in the module JAR
     * as a fallback (since this is the most important file for various purposes).
     * Note that update_tracking/*.xml is added as well as files it lists.
     */
    private static Set/*<File>*/ findBinaryNBMFiles(File cluster, String cnb, File jar) throws IOException {
        Set/*<File>*/ files = new HashSet();
        files.add(jar);
        File tracking = new File(new File(cluster, "update_tracking"), cnb.replace('.', '-') + ".xml"); // NOI18N
        if (tracking.isFile()) {
            files.add(tracking);
            Document doc;
            try {
                doc = XMLUtil.parse(new InputSource(tracking.toURI().toString()), false, false, null, null);
            } catch (SAXException e) {
                throw (IOException) new IOException(e.toString()).initCause(e);
            }
            Iterator it = Util.findSubElements(doc.getDocumentElement()).iterator();
            while (it.hasNext()) {
                Element moduleVersion = (Element) it.next();
                if (moduleVersion.getTagName().equals("module_version") && moduleVersion.getAttribute("last").equals("true")) { // NOI18N
                    Iterator it2 = Util.findSubElements(moduleVersion).iterator();
                    while (it2.hasNext()) {
                        Element fileEl = (Element) it2.next();
                        if (fileEl.getTagName().equals("file")) { // NOI18N
                            String name = fileEl.getAttribute("name"); // NOI18N
                            File f = new File(cluster, name.replace('/', File.separatorChar));
                            if (f.isFile()) {
                                files.add(f);
                            }
                        }
                    }
                }
            }
        }
        return files;
    }
    
    /**
     * Load a project.xml from a project.
     * @param basedir a putative project base directory
     * @return its primary configuration data (if there is an NBM project here), else null
     */
    static Element parseData(File basedir) throws IOException {
        File projectXml = new File(basedir, "nbproject" + File.separatorChar + "project.xml"); // NOI18N
        if (!projectXml.isFile()) {
            return null;
        }
        Document doc;
        try {
            doc = XMLUtil.parse(new InputSource(projectXml.toURI().toString()), false, true, null, null);
        } catch (SAXException e) {
            throw (IOException) new IOException(projectXml + ": " + e.toString()).initCause(e);
        }
        Element docel = doc.getDocumentElement();
        Element type = Util.findElement(docel, "type", "http://www.netbeans.org/ns/project/1"); // NOI18N
        if (!Util.findText(type).equals("org.netbeans.modules.apisupport.project")) { // NOI18N
            return null;
        }
        Element cfg = Util.findElement(docel, "configuration", "http://www.netbeans.org/ns/project/1"); // NOI18N
        return Util.findElement(cfg, "data", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
    }
    
    /**
     * Load properties for a project.
     * Only deals with certain properties of interest here (all file-type values assumed relative to basedir):
     * netbeans.dest.dir (file-valued)
     * module.jar (plain string)
     * module.jar.dir (plain string)
     * cluster.dir (plain string)
     * suite.dir (file-valued)
     * @param basedir project basedir
     * @param root root of sources (netbeans.org only)
     * @param suiteComponent whether this is an external module in a suite
     * @param standalone whether this is an external standalone module
     * @param cnb code name base of this project
     */
    static PropertyEvaluator parseProperties(File basedir, File root, boolean suiteComponent, boolean standalone, String cnb) throws IOException {
        assert !(suiteComponent && standalone) : basedir;
        Map/*<String,String>*/ predefs = new HashMap(System.getProperties());
        predefs.put("basedir", basedir.getAbsolutePath()); // NOI18N
        PropertyProvider predefsProvider = PropertyUtils.fixedPropertyProvider(predefs);
        List/*<PropertyProvider>*/ providers = new ArrayList();
        if (suiteComponent) {
            providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "private" + File.separatorChar + "suite-private.properties"))); // NOI18N
            providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "suite.properties"))); // NOI18N
            PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
            String suiteS = eval.getProperty("suite.dir"); // NOI18N
            if (suiteS != null) {
                File suite = PropertyUtils.resolveFile(basedir, suiteS);
                providers.add(loadPropertiesFile(new File(suite, "nbproject" + File.separatorChar + "private" + File.separatorChar + "platform-private.properties"))); // NOI18N
                providers.add(loadPropertiesFile(new File(suite, "nbproject" + File.separatorChar + "platform.properties"))); // NOI18N
            }
        } else if (standalone) {
            providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "private" + File.separatorChar + "platform-private.properties"))); // NOI18N
            providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "platform.properties"))); // NOI18N
        }
        if (suiteComponent || standalone) {
            PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
            String buildS = eval.getProperty("user.properties.file"); // NOI18N
            if (buildS != null) {
                providers.add(loadPropertiesFile(PropertyUtils.resolveFile(basedir, buildS)));
            } else {
                providers.add(PropertyUtils.globalPropertyProvider());
            }
            eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
            String platformS = eval.getProperty("nbplatform.active"); // NOI18N
            if (platformS != null) {
                providers.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap("netbeans.dest.dir", "${nbplatform." + platformS + ".netbeans.dest.dir}"))); // NOI18N
            }
        }
        // private.properties & project.properties.
        providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "private" + File.separatorChar + "private.properties"))); // NOI18N
        providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "project.properties"))); // NOI18N
        // Implicit stuff.
        Map/*<String,String>*/ defaults = new HashMap();
        boolean isNetBeansOrg = !suiteComponent && !standalone;
        if (isNetBeansOrg) {
            defaults.put("nb_all", root.getAbsolutePath()); // NOI18N
            defaults.put("netbeans.dest.dir", new File(root, DEST_DIR_IN_NETBEANS_ORG).getAbsolutePath()); // NOI18N
        }
        defaults.put("code.name.base.dashes", cnb.replace('.', '-')); // NOI18N
        defaults.put("module.jar.dir", "modules"); // NOI18N
        defaults.put("module.jar.basename", "${code.name.base.dashes}.jar"); // NOI18N
        defaults.put("module.jar", "${module.jar.dir}/${module.jar.basename}"); // NOI18N
        providers.add(PropertyUtils.fixedPropertyProvider(defaults));
        String cluster = null;
        if (isNetBeansOrg) {
            cluster = findClusterLocation(basedir, root);
        }
        if (cluster == null) {
            cluster = isNetBeansOrg ? "extra" : "devel"; // NOI18N
        }
        defaults.put("cluster.dir", cluster); // NOI18N
        return PropertyUtils.sequentialPropertyEvaluator(predefsProvider, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
    }
    
    private static PropertyProvider loadPropertiesFile(File f) throws IOException {
        if (!f.isFile()) {
            return PropertyUtils.fixedPropertyProvider(Collections.EMPTY_MAP);
        }
        Properties p = new Properties();
        InputStream is = new FileInputStream(f);
        try {
            p.load(is);
        } finally {
            is.close();
        }
        return PropertyUtils.fixedPropertyProvider(p);
    }
    
    /**
     * Refresh any existing lists, e.g. in response to a new module being created.
     */
    public static void refresh() {
        sourceLists.clear();
        binaryLists.clear();
        // XXX what about knownEntries?
    }
    
    /**
     * Whether whether a given dir is root of netbeans.org CVS.
     */
    public static boolean isNetBeansOrg(File dir) {
        return new File(dir, "nbbuild").isDirectory() && // NOI18N
                new File(dir, "openide").isDirectory(); // NOI18N
    }
    
    /**
     * Find the root of netbeans.org CVS starting from a project basedir.
     */
    public static File findNetBeansOrg(File basedir) {
        File f = basedir;
        for (int i = 0; i < DEPTH_NB_ALL; i++) {
            f = f.getParentFile();
            if (f == null) {
                return null;
            }
            if (isNetBeansOrg(f)) {
                return f;
            }
        }
        // Not here.
        return null;
    }
    
    /**
     * Find cluster location of a netbeans.org module.
     * @param basedir project basedir
     * @param nbroot location of netbeans.org source root
     */
    private static String findClusterLocation(File basedir, File nbroot) throws IOException {
        String path = PropertyUtils.relativizeFile(nbroot, basedir);
        assert path.indexOf("..") == -1 : path;
        Map/*<String,String>*/ clusterLocationsHere = (Map) clusterLocations.get(nbroot);
        if (clusterLocationsHere == null) {
            clusterLocationsHere = new HashMap();
            PropertyProvider pp = loadPropertiesFile(new File(nbroot, "nbbuild" + File.separatorChar + "cluster.properties")); // NOI18N
            PropertyEvaluator clusterEval = PropertyUtils.sequentialPropertyEvaluator(
                    PropertyUtils.fixedPropertyProvider(Collections.EMPTY_MAP),
                    new PropertyProvider[] {
                pp,
            });
            Map/*<String,String>*/ clusterDefs = clusterEval.getProperties();
            if (clusterDefs != null) {
                Iterator it = clusterDefs.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    String key = (String)entry.getKey();
                    String clusterDir = (String) clusterDefs.get(key + ".dir"); // NOI18N
                    if (clusterDir == null) {
                        // Not a list of modules.
                        // XXX could also just read clusters.list
                        continue;
                    }
                    String val = (String) entry.getValue();
                    StringTokenizer tok = new StringTokenizer(val, ", "); // NOI18N
                    while (tok.hasMoreTokens()) {
                        String p = tok.nextToken();
                        clusterLocationsHere.put(p, clusterDir);
                    }
                }
            }
            clusterLocations.put(nbroot, clusterLocationsHere);
        }
        return (String) clusterLocationsHere.get(path);
    }
    
    // NONSTATIC PART
    
    /** all module entries, indexed by cnb */
    private final Map/*<String,Entry>*/ entries;
    
    private ModuleList(Map/*<String,Entry>*/ entries) {
        this.entries = entries;
    }
    
    public String toString() {
        return "ModuleList" + entries.values(); // NOI18N
    }
    
    /**
     * Merge a bunch of module lists into one.
     * In case of conflict (by CNB), earlier entries take precedence.
     */
    private static ModuleList merge(ModuleList[] lists) {
        Map/*<String,Entry>*/ entries = new HashMap();
        for (int i = 0; i < lists.length; i++) {
            Iterator it = lists[i].entries.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String cnb = (String) entry.getKey();
                if (!entries.containsKey(cnb)) {
                    entries.put(cnb, (ModuleEntry) entry.getValue());
                }
            }
        }
        return new ModuleList(entries);
    }
    
    /**
     * Find an entry by name.
     * @param codeNameBase code name base of the module
     * @return the matching module, or null if there is none such
     */
    public ModuleEntry getEntry(String codeNameBase) {
        return (ModuleEntry) entries.get(codeNameBase);
    }
    
    /**
     * Get all known entries at once.
     * @return all known module entries
     */
    public Set/*<Entry>*/ getAllEntries() {
        return new HashSet(entries.values());
    }
    
    // XXX Similar (and better) code is in the NbModuleProject.findLocalizedBundlePath()
    // move to some utils
    static LocalizedBundleInfo loadBundleInfo(File sourceLocation) {
        LocalizedBundleInfo bundleInfo = null;
        try {
            File manifest = new File(sourceLocation, "manifest.mf");
            if (manifest.exists()) {
                ManifestManager mm = ManifestManager.getInstance(manifest, false);
                String locBundle = mm.getLocalizingBundle();
                if (locBundle != null) {
                    File locBundleFile = new File(
                        new File(sourceLocation, "src"), locBundle); // NOI18N
                    if (locBundleFile.exists()) {
                        bundleInfo = LocalizedBundleInfo.load(FileUtil.toFileObject(locBundleFile));
                    }
                }
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        return bundleInfo == null ? LocalizedBundleInfo.EMPTY : bundleInfo;
    }

    // XXX Similar (and better) code is in the NbModuleProject.findLocalizedBundlePath()
    static LocalizedBundleInfo loadBundleInfoFromBinary(File jarLocation) {
        LocalizedBundleInfo bundleInfo = null;
        try {
            ManifestManager mm = ManifestManager.getInstanceFromJAR(jarLocation);
            String locBundle = mm.getLocalizingBundle();
            if (locBundle != null) {
                JarFileSystem jfs = new JarFileSystem();
                jfs.setJarFile(jarLocation);
                FileObject locBundleF0 = jfs.getRoot().getFileObject(locBundle);
                bundleInfo = LocalizedBundleInfo.load(locBundleF0);
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        } catch (PropertyVetoException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        return bundleInfo == null ? LocalizedBundleInfo.EMPTY : bundleInfo;
    }

}
