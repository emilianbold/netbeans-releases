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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProjectType;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbCollections;
import org.openide.util.NbCollections;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Represents list of known modules.
 * @author Jesse Glick
 */
public final class ModuleList {
    
    /** for performance measurement from ModuleListTest */
    static long timeSpentInXmlParsing;
    static int xmlFilesParsed;
    static int directoriesChecked;
    static int jarsOpened;
    
    public static final String DEST_DIR_IN_NETBEANS_ORG = "nbbuild" + File.separatorChar + "netbeans"; // NOI18N
    /** Synch with org.netbeans.nbbuild.ModuleListParser.DEPTH_NB_ALL */
    public static final int DEPTH_NB_ALL = 3;
    
    /**
     * Cache of source-derived lists, by source root.
     */
    private static final Map<File,ModuleList> sourceLists = new HashMap<File,ModuleList>();
    /**
     * Cache of binary-derived lists, by binary root (~ dest dir).
     */
    private static final Map<File,ModuleList> binaryLists = new HashMap<File,ModuleList>();
    /**
     * Map from netbeans.org source roots to cluster.properties loads.
     */
    private static final Map<File,Map<String,String>> clusterPropertiesFiles = new HashMap<File,Map<String,String>>();
    /**
     * Map from netbeans.org source roots, to cluster definitions,
     * where a cluster definition is from netbeans.org relative source path
     * to physical cluster directory.
     */
    private static final Map<File,Map<String,String>> clusterLocations = new HashMap<File,Map<String,String>>();
    
    /** All entries known to exist for a given included file path. */
    private static final Map<File,Set<ModuleEntry>> knownEntries = new HashMap<File,Set<ModuleEntry>>();

    /**
     * Find the list of modules associated with a project (itself, others in
     * its suite, others in its platform, or others in netbeans.org). <p>Do not
     * cache the result; always call this method fresh, in case {@link
     * #refresh} has been called. This method actually call {@link
     * #getModuleList(File, File)} with the <code>null</code> for the
     * <code>customNbDestDir</code> parameter.
     *
     * @param basedir the project directory to start in
     * @return a module list
     */
    public static ModuleList getModuleList(File basedir) throws IOException {
        return getModuleList(basedir, null);
    }
    
    /**
     * The same as {@link #getModuleList(File)}, but giving chance to specify a
     * custom NetBeans platform.
     *
     * @param basedir the project directory to start in
     * @param customNbDestDir custom NetBeans platform directory to be used for
     *        searching NB module instead of using the currently set one in a
     *        module's properties. If <code>null</code> is passed the
     *        default(active) platform from module's properties will be used
     * @return a module list
     */
    public static ModuleList getModuleList(final File basedir, final File customNbDestDir) throws IOException {
        try {
            return ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<ModuleList>() { // #69971
                public ModuleList run() throws IOException {
                    synchronized (binaryLists) { // need to protect caches from race conditions, so this seems OK
        timeSpentInXmlParsing = 0L;
        xmlFilesParsed = 0;
        directoriesChecked = 0;
        jarsOpened = 0;
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
            return findOrCreateModuleListFromSuite(suite, customNbDestDir);
        } else if (standalone) {
            return findOrCreateModuleListFromStandaloneModule(basedir, customNbDestDir);
        } else {
            // netbeans.org module.
            File nbroot = findNetBeansOrg(basedir);
            if (nbroot == null) {
                throw new IOException("Could not find netbeans.org CVS root from " + basedir + "; note that 3rd-level modules (a/b/c) are permitted at the maximum"); // NOI18N
            }
            return findOrCreateModuleListFromNetBeansOrgSources(nbroot);
        }
                    }
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /**
     * Check to see if there are <em>any</em> known module list entries.
     * @return false if {@link #getKnownEntries} cannot return a nonempty set at the moment, true if it might
     */
    public static boolean existKnownEntries() {
        synchronized (knownEntries) {
            return !knownEntries.isEmpty();
        }
    }
    
    /**
     * Find the known module entries which build to a given built file path (e.g. module JAR).
     * Applies to any entries which have been scanned.
     * @param file some file built as part of the module
     * @return a set of entries thought to build to this file (may be empty but not null)
     */
    public static Set<ModuleEntry> getKnownEntries(File file) {
        synchronized (knownEntries) {
            Set<ModuleEntry> entries = knownEntries.get(file);
            if (entries != null) {
                return new HashSet<ModuleEntry>(entries);
            } else {
                return Collections.emptySet();
            }
        }
    }
    
    private static void registerEntry(ModuleEntry entry, Set<File> files) {
        synchronized (knownEntries) {
            for (File f : files) {
                Set<ModuleEntry> entries = knownEntries.get(f);
                if (entries == null) {
                    entries = new HashSet<ModuleEntry>();
                    knownEntries.put(f, entries);
                }
                entries.add(entry);
            }
        }
    }
    
    static ModuleList findOrCreateModuleListFromNetBeansOrgSources(File root) throws IOException {
        ModuleList list = sourceLists.get(root);
        if (list == null) {
            list = createModuleListFromNetBeansOrgSources(root);
            sourceLists.put(root, list);
        }
        return list;
    }
    
    private static ModuleList createModuleListFromNetBeansOrgSources(File root) throws IOException {
        Util.err.log("ModuleList.createModuleListFromSources: " + root);
        File nbdestdir = new File(root, DEST_DIR_IN_NETBEANS_ORG);
        Map<String,ModuleEntry> entries = new HashMap<String,ModuleEntry>();
        scanNetBeansOrgStableSources(entries, root, nbdestdir);
        return new ModuleList(entries, root, true);
    }
    
    /**
     * Look just for stable modules in netbeans.org, assuming that this is most commonly what is wanted.
     * @see "#62221"
     */
    private static void scanNetBeansOrgStableSources(Map<String,ModuleEntry> entries, File root, File nbdestdir) throws IOException {
        Map<String,String> clusterProps = getClusterProperties(root);
        // Use ${clusters.list}, *not* ${nb.clusters.list}: we do want to include testtools,
        // since those modules contribute sources for JARs which are used in unit test classpaths for stable modules.
        String clusterList = clusterProps.get("clusters.list"); // NOI18N
        if (clusterList == null) {
            String config = clusterProps.get("cluster.config"); // NOI18N
            if (config != null) {
                clusterList = clusterProps.get("clusters.config." + config + ".list"); // NOI18N
            }
        }
        if (clusterList == null) {
            throw new IOException("No ${nb.clusters.list} found in " + root); // NOI18N
        }
        StringTokenizer tok = new StringTokenizer(clusterList, ", "); // NOI18N
        while (tok.hasMoreTokens()) {
            String clusterName = tok.nextToken();
            String moduleList = clusterProps.get(clusterName);
            if (moduleList == null) {
                throw new IOException("No ${" + clusterName + "} found in " + root); // NOI18N
            }
            StringTokenizer tok2 = new StringTokenizer(moduleList, ", "); // NOI18N
            while (tok2.hasMoreTokens()) {
                String module = tok2.nextToken();
                scanPossibleProject(new File(root, module.replace('/', File.separatorChar)), entries, false, false, root, nbdestdir, module, true);
            }
        }
    }
    
    public static final Set<String> EXCLUDED_DIR_NAMES = new HashSet<String>();
    static {
        EXCLUDED_DIR_NAMES.add("CVS"); // NOI18N
        EXCLUDED_DIR_NAMES.add("nbproject"); // NOI18N
        EXCLUDED_DIR_NAMES.add("www"); // NOI18N
        EXCLUDED_DIR_NAMES.add("test"); // NOI18N
        EXCLUDED_DIR_NAMES.add("build"); // NOI18N
        EXCLUDED_DIR_NAMES.add("src"); // NOI18N
        EXCLUDED_DIR_NAMES.add("org"); // NOI18N
    }
    private static void doScanNetBeansOrgSources(Map<String,ModuleEntry> entries, File dir, int depth,
            File root, File nbdestdir, String pathPrefix, boolean warnReDuplicates) {
        File[] kids = dir.listFiles();
        if (kids == null) {
            return;
        }
        for (File kid : kids) {
            if (!kid.isDirectory()) {
                continue;
            }
            String name = kid.getName();
            if (EXCLUDED_DIR_NAMES.contains(name)) {
                // #61579: known to not be project dirs, so skip to save time.
                continue;
            }
            String newPathPrefix = (pathPrefix != null) ? pathPrefix + "/" + name : name; // NOI18N
            try {
                scanPossibleProject(kid, entries, false, false, root, nbdestdir, newPathPrefix, warnReDuplicates);
            } catch (IOException e) {
                // #60295: make it nonfatal.
                Util.err.annotate(e, ErrorManager.UNKNOWN, "Malformed project metadata in " + kid + ", skipping...", null, null, null); // NOI18N
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
            if (depth > 1) {
                doScanNetBeansOrgSources(entries, kid, depth - 1, root, nbdestdir, newPathPrefix, warnReDuplicates);
            }
        }
    }
    
    private static void scanPossibleProject(File basedir, Map<String,ModuleEntry> entries,
            boolean suiteComponent, boolean standalone, File root, File nbdestdir, String path, boolean warnReDuplicates) throws IOException {
        directoriesChecked++;
        Element data = parseData(basedir);
        if (data == null) {
            return;
        }
        assert root != null ^ (standalone || suiteComponent);
        assert path != null ^ (standalone || suiteComponent);
        String cnb = Util.findText(Util.findElement(data, "code-name-base", NbModuleProjectType.NAMESPACE_SHARED)); // NOI18N
        PropertyEvaluator eval = parseProperties(basedir, root, suiteComponent, standalone, cnb);
        String module = eval.getProperty("module.jar"); // NOI18N
        // Cf. ParseProjectXml.computeClasspath:
        StringBuffer cpextra = new StringBuffer();
        for (Element ext : Util.findSubElements(data)) {
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
                text = "${cluster}/${module.jar.dir}/" + reltext; // NOI18N
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
        File clusterDir = PropertyUtils.resolveFile(basedir, eval.evaluate("${cluster}")); // NOI18N
        ModuleEntry entry;
        ManifestManager.PackageExport[] publicPackages = ProjectXMLManager.findPublicPackages(data);
        String[] friends = ProjectXMLManager.findFriends(data);
        String src = eval.getProperty("src.dir"); // NOI18N
        if (src == null) {
            src = "src"; // NOI18N
        }
        if (!suiteComponent && !standalone) {
            entry = new NetBeansOrgEntry(root, cnb, path, clusterDir, module, cpextra.toString(),
                    mm.getReleaseVersion(), mm.getProvidedTokens(),
                    publicPackages, friends, mm.isDeprecated(), src);
        } else {
            entry = new ExternalEntry(basedir, cnb, clusterDir, PropertyUtils.resolveFile(clusterDir, module),
                    cpextra.toString(), nbdestdir, mm.getReleaseVersion(),
                    mm.getProvidedTokens(), publicPackages, friends, mm.isDeprecated(), src);
        }
        if (entries.containsKey(cnb)) {
            if (warnReDuplicates) {
                Util.err.log(ErrorManager.WARNING, "Warning: two modules found with the same code name base (" + cnb + "): " + entries.get(cnb) + " and " + entry);
            }
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
     * And anything in the release/ directory is added.
     */
    private static Set<File> findSourceNBMFiles(ModuleEntry entry, PropertyEvaluator eval) throws IOException {
        Set<File> files = new HashSet<File>();
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
        for (String f : STANDARD_FILES) {
            int x = f.indexOf('*');
            findSourceNBMFilesMaybeAdd(files, cluster, f.substring(0, x) + cnbd + f.substring(x + 1));
        }
        String emf = eval.getProperty("extra.module.files"); // NOI18N
        if (emf != null) {
            for (String pattern : emf.split(" *, *")) { // NOI18N
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
                    for (String clusterFile : scanDirForFiles(cluster)) {
                        if (regexp.matcher(clusterFile).matches()) {
                            findSourceNBMFilesMaybeAdd(files, cluster, clusterFile);
                        }
                    }
                }
            }
        }
        File src = entry.getSourceLocation();
        assert src != null && src.isDirectory() : entry;
        // XXX handle overrides of release.dir
        File releaseDir = new File(src, "release"); // NOI18N
        if (releaseDir.isDirectory()) {
            for (String releaseFile : scanDirForFiles(releaseDir)) {
                findSourceNBMFilesMaybeAdd(files, cluster, releaseFile);
            }
        }
        return files;
    }
    private static void findSourceNBMFilesMaybeAdd(Set<File> files, File cluster, String path) {
        File f = new File(cluster, path.replace('/', File.separatorChar));
        files.add(f);
    }
    private static final Map<File,String[]> DIR_SCAN_CACHE = new HashMap<File,String[]>();
    private static String[] scanDirForFiles(File dir) {
        String[] files = DIR_SCAN_CACHE.get(dir);
        if (files == null) {
            List<String> l = new ArrayList<String>(250);
            doScanDirForFiles(dir, l, "");
            files = l.toArray(new String[l.size()]);
        }
        return files;
    }
    private static void doScanDirForFiles(File d, List<String> files, String prefix) {
        directoriesChecked++;
        File[] kids = d.listFiles();
        if (kids != null) {
            for (File f : kids) {
                if (f.isFile()) {
                    files.add(prefix + f.getName());
                } else if (f.isDirectory()) {
                    doScanDirForFiles(f, files, prefix + f.getName() + '/');
                }
            }
        }
    }
    
    public static ModuleList findOrCreateModuleListFromSuite(
            File root, File customNbDestDir) throws IOException {
        PropertyEvaluator eval = parseSuiteProperties(root);
        File nbdestdir;
        if (customNbDestDir == null) {
            String nbdestdirS = eval.getProperty("netbeans.dest.dir"); // NOI18N
            if (nbdestdirS == null) {
                throw new IOException("No netbeans.dest.dir defined in " + root); // NOI18N
            }
            nbdestdir = PropertyUtils.resolveFile(root, nbdestdirS);
        } else {
            nbdestdir = customNbDestDir;
        }
        return merge(new ModuleList[] {
            findOrCreateModuleListFromSuiteWithoutBinaries(root, nbdestdir, eval),
            findOrCreateModuleListFromBinaries(nbdestdir),
        }, root);
    }
    
    private static ModuleList findOrCreateModuleListFromSuiteWithoutBinaries(File root, File nbdestdir, PropertyEvaluator eval) throws IOException {
        ModuleList sources = sourceLists.get(root);
        if (sources == null) {
            Map<String,ModuleEntry> entries = new HashMap<String,ModuleEntry>();
            for (File module : findModulesInSuite(root, eval)) {
                try {
                    scanPossibleProject(module, entries, true, false, null, nbdestdir, null, true);
                } catch (IOException e) {
                    Util.err.annotate(e, ErrorManager.UNKNOWN, "Malformed project metadata in " + module + ", skipping...", null, null, null); // NOI18N
                    Util.err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            sources = new ModuleList(entries, root, false);
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
        Map<String,String> predefs = NbCollections.checkedMapByCopy(System.getProperties(), String.class, String.class, false);
        predefs.put("basedir", root.getAbsolutePath()); // NOI18N
        PropertyProvider predefsProvider = PropertyUtils.fixedPropertyProvider(predefs);
        List<PropertyProvider> providers = new ArrayList<PropertyProvider>();
        providers.add(loadPropertiesFile(new File(root, "nbproject" + File.separatorChar + "private" + File.separatorChar + "platform-private.properties"))); // NOI18N
        providers.add(loadPropertiesFile(new File(root, "nbproject" + File.separatorChar + "platform.properties"))); // NOI18N
        PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, providers.toArray(new PropertyProvider[providers.size()]));
        String buildS = eval.getProperty("user.properties.file"); // NOI18N
        if (buildS != null) {
            providers.add(loadPropertiesFile(PropertyUtils.resolveFile(root, buildS)));
        } else {
            // Never been opened, perhaps - so fake it.
            providers.add(PropertyUtils.globalPropertyProvider());
        }
        providers.add(loadPropertiesFile(new File(root, "nbproject" + File.separatorChar + "private" + File.separatorChar + "private.properties"))); // NOI18N
        providers.add(loadPropertiesFile(new File(root, "nbproject" + File.separatorChar + "project.properties"))); // NOI18N
        eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, providers.toArray(new PropertyProvider[providers.size()]));
        String platformS = eval.getProperty("nbplatform.active"); // NOI18N
        if (platformS != null) {
            providers.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap("netbeans.dest.dir", "${nbplatform." + platformS + ".netbeans.dest.dir}"))); // NOI18N
        }
        return PropertyUtils.sequentialPropertyEvaluator(predefsProvider, providers.toArray(new PropertyProvider[providers.size()]));
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
    
    private static ModuleList findOrCreateModuleListFromStandaloneModule(
            File basedir, File customNbDestDir) throws IOException {
        PropertyEvaluator eval = parseProperties(basedir, null, false, true, "irrelevant"); // NOI18N
        File nbdestdir;
        if (customNbDestDir == null) {
            String nbdestdirS = eval.getProperty("netbeans.dest.dir"); // NOI18N
            if (nbdestdirS == null) {
                throw new IOException("No netbeans.dest.dir defined in " + basedir); // NOI18N
            }
            if (nbdestdirS.indexOf("${") != -1) { // NOI18N
                throw new IOException("Unevaluated properties in " + nbdestdirS + " from " + basedir + "; probably means platform definitions not loaded correctly"); // NOI18N
            }
            nbdestdir = PropertyUtils.resolveFile(basedir, nbdestdirS);
        } else {
            nbdestdir = customNbDestDir;
        }
        ModuleList binaries = findOrCreateModuleListFromBinaries(nbdestdir);
        ModuleList sources = sourceLists.get(basedir);
        if (sources == null) {
            Map<String,ModuleEntry> entries = new HashMap<String,ModuleEntry>();
            scanPossibleProject(basedir, entries, false, true, null, nbdestdir, null, true);
            if (entries.isEmpty()) {
                throw new IOException("No module in " + basedir); // NOI18N
            }
            sources = new ModuleList(entries, basedir, false);
            sourceLists.put(basedir, sources);
        }
        return merge(new ModuleList[] {sources, binaries}, basedir);
    }
    
    static ModuleList findOrCreateModuleListFromBinaries(File root) throws IOException {
        ModuleList list = binaryLists.get(root);
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
        Map<String,ModuleEntry> entries = new HashMap<String,ModuleEntry>();
        File[] clusters = root.listFiles();
        if (clusters == null) {
            throw new IOException("Cannot examine dir " + root); // NOI18N
        }
        for (File cluster : clusters) {
            for (String moduleDir : MODULE_DIRS) {
                File dir = new File(cluster, moduleDir.replace('/', File.separatorChar));
                if (!dir.isDirectory()) {
                    continue;
                }
                File[] jars = dir.listFiles();
                if (jars == null) {
                    throw new IOException("Cannot examine dir " + dir); // NOI18N
                }
                for (File m : jars) {
                    if (!m.getName().endsWith(".jar")) { // NOI18N
                        continue;
                    }
                    jarsOpened++;
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
                    ModuleEntry entry = new BinaryEntry(codenamebase, m, exts, root, cluster,
                            mm.getReleaseVersion(), mm.getSpecificationVersion(), mm.getProvidedTokens(),
                            mm.getPublicPackages(), mm.getFriends(), mm.isDeprecated(), mm.getModuleDependencies());
                    if (entries.containsKey(codenamebase)) {
                        Util.err.log(ErrorManager.WARNING, "Warning: two modules found with the same code name base (" + codenamebase + "): " + entries.get(codenamebase) + " and " + entry);
                    } else {
                        entries.put(codenamebase, entry);
                    }
                    registerEntry(entry, findBinaryNBMFiles(cluster, codenamebase, m));
                }
            }
        }
        return new ModuleList(entries, root, false);
    }
    
    /**
     * Try to find which files are part of a module's binary build (i.e. slated for NBM).
     * Tries to scan update tracking for the file, but also always adds in the module JAR
     * as a fallback (since this is the most important file for various purposes).
     * Note that update_tracking/*.xml is added as well as files it lists.
     */
    private static Set<File> findBinaryNBMFiles(File cluster, String cnb, File jar) throws IOException {
        Set<File> files = new HashSet<File>();
        files.add(jar);
        File tracking = new File(new File(cluster, "update_tracking"), cnb.replace('.', '-') + ".xml"); // NOI18N
        if (tracking.isFile()) {
            files.add(tracking);
            Document doc;
            try {
                xmlFilesParsed++;
                timeSpentInXmlParsing -= System.currentTimeMillis();
                doc = XMLUtil.parse(new InputSource(tracking.toURI().toString()), false, false, null, null);
                timeSpentInXmlParsing += System.currentTimeMillis();
            } catch (SAXException e) {
                throw (IOException) new IOException(e.toString()).initCause(e);
            }
            for (Element moduleVersion : Util.findSubElements(doc.getDocumentElement())) {
                if (moduleVersion.getTagName().equals("module_version") && moduleVersion.getAttribute("last").equals("true")) { // NOI18N
                    for (Element fileEl : Util.findSubElements(moduleVersion)) {
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
    
    private static final String PROJECT_XML = "nbproject" + File.separatorChar + "project.xml"; // NOI18N
    /**
     * Load a project.xml from a project.
     * @param basedir a putative project base directory
     * @return its primary configuration data (if there is an NBM project here), else null
     */
    static Element parseData(File basedir) throws IOException {
        File projectXml = new File(basedir, PROJECT_XML);
        // #61579: tboudreau claims File.exists is much cheaper on some systems
        //System.err.println("parseData: " + basedir);
        if (!projectXml.exists() || !projectXml.isFile()) {
            return null;
        }
        Document doc;
        try {
            xmlFilesParsed++;
            timeSpentInXmlParsing -= System.currentTimeMillis();
            doc = XMLUtil.parse(new InputSource(projectXml.toURI().toString()), false, true, null, null);
            timeSpentInXmlParsing += System.currentTimeMillis();
        } catch (SAXException e) {
            throw (IOException) new IOException(projectXml + ": " + e.toString()).initCause(e); // NOI18N
        }
        Element docel = doc.getDocumentElement();
        Element type = Util.findElement(docel, "type", "http://www.netbeans.org/ns/project/1"); // NOI18N
        if (!Util.findText(type).equals("org.netbeans.modules.apisupport.project")) { // NOI18N
            return null;
        }
        Element cfg = Util.findElement(docel, "configuration", "http://www.netbeans.org/ns/project/1"); // NOI18N
        Element data = Util.findElement(cfg, "data", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        if (data != null) {
            return data;
        } else {
            data = Util.findElement(cfg, "data", NbModuleProjectType.NAMESPACE_SHARED_2); // NOI18N
            if (data != null) {
                return Util.translateXML(data, NbModuleProjectType.NAMESPACE_SHARED);
            } else {
                return null;
            }
        }
    }
    
    /**
     * Load properties for a project.
     * Only deals with certain properties of interest here (all file-type values assumed relative to basedir):
     * netbeans.dest.dir (file-valued)
     * module.jar (plain string)
     * module.jar.dir (plain string)
     * cluster (file-valued)
     * suite.dir (file-valued)
     * @param basedir project basedir
     * @param root root of sources (netbeans.org only)
     * @param suiteComponent whether this is an external module in a suite
     * @param standalone whether this is an external standalone module
     * @param cnb code name base of this project
     */
    static PropertyEvaluator parseProperties(File basedir, File root, boolean suiteComponent, boolean standalone, String cnb) throws IOException {
        assert !(suiteComponent && standalone) : basedir;
        Map<String,String> predefs = NbCollections.checkedMapByCopy(System.getProperties(), String.class, String.class, false);
        predefs.put("basedir", basedir.getAbsolutePath()); // NOI18N
        PropertyProvider predefsProvider = PropertyUtils.fixedPropertyProvider(predefs);
        List<PropertyProvider> providers = new ArrayList<PropertyProvider>();
        if (suiteComponent) {
            providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "private" + File.separatorChar + "suite-private.properties"))); // NOI18N
            providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "suite.properties"))); // NOI18N
            PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, providers.toArray(new PropertyProvider[providers.size()]));
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
            PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, providers.toArray(new PropertyProvider[providers.size()]));
            String buildS = eval.getProperty("user.properties.file"); // NOI18N
            if (buildS != null) {
                providers.add(loadPropertiesFile(PropertyUtils.resolveFile(basedir, buildS)));
            } else {
                providers.add(PropertyUtils.globalPropertyProvider());
            }
            eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, providers.toArray(new PropertyProvider[providers.size()]));
            String platformS = eval.getProperty("nbplatform.active"); // NOI18N
            if (platformS != null) {
                providers.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap("netbeans.dest.dir", "${nbplatform." + platformS + ".netbeans.dest.dir}"))); // NOI18N
            }
        }
        // private.properties & project.properties.
        providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "private" + File.separatorChar + "private.properties"))); // NOI18N
        providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "project.properties"))); // NOI18N
        // Implicit stuff.
        Map<String,String> defaults = new HashMap<String,String>();
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
        if (suiteComponent) {
            defaults.put("cluster", "${suite.dir}/build/cluster"); // NOI18N
        } else if (standalone) {
            defaults.put("cluster", "build/cluster"); // NOI18N
        } else {
            // netbeans.org
            String cluster = findClusterLocation(basedir, root);
            if (cluster == null) {
                cluster = "extra"; // NOI18N
            }
            defaults.put("cluster", "${netbeans.dest.dir}/" + cluster); // NOI18N
        }
        return PropertyUtils.sequentialPropertyEvaluator(predefsProvider, providers.toArray(new PropertyProvider[providers.size()]));
    }
    
    private static PropertyProvider loadPropertiesFile(File f) throws IOException {
        if (!f.isFile()) {
            return PropertyUtils.fixedPropertyProvider(Collections.<String,String>emptyMap());
        }
        Properties p = new Properties();
        InputStream is = new FileInputStream(f);
        try {
            p.load(is);
        } finally {
            is.close();
        }
        return PropertyUtils.fixedPropertyProvider(NbCollections.checkedMapByFilter(p, String.class, String.class, true));
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
     * Refresh cached module list for the given suite. If there is not such a
     * cached list yet, the method is just no-op.
     */
    public static void refreshSuiteModuleList(File suiteDir) {
        sourceLists.remove(suiteDir);
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
    
    private static Map<String,String> getClusterProperties(File nbroot) throws IOException {
        Map<String,String> clusterDefs = clusterPropertiesFiles.get(nbroot);
        if (clusterDefs == null) {
            PropertyProvider pp = loadPropertiesFile(new File(nbroot, "nbbuild" + File.separatorChar + "cluster.properties")); // NOI18N
            PropertyEvaluator clusterEval = PropertyUtils.sequentialPropertyEvaluator(
                    PropertyUtils.fixedPropertyProvider(Collections.<String,String>emptyMap()), pp);
            clusterDefs = clusterEval.getProperties();
            if (clusterDefs == null) {
                // Definition failure of some sort.
                clusterDefs = Collections.emptyMap();
            }
        }
        return clusterDefs;
    }
    
    /**
     * Find cluster location of a netbeans.org module.
     * @param basedir project basedir
     * @param nbroot location of netbeans.org source root
     */
    private static String findClusterLocation(File basedir, File nbroot) throws IOException {
        String path = PropertyUtils.relativizeFile(nbroot, basedir);
        assert path.indexOf("..") == -1 : path;
        Map<String,String> clusterLocationsHere = clusterLocations.get(nbroot);
        if (clusterLocationsHere == null) {
            clusterLocationsHere = new HashMap<String,String>();
            Map<String,String> clusterDefs = getClusterProperties(nbroot);
            for (Map.Entry<String,String> entry : clusterDefs.entrySet()) {
                String key = entry.getKey();
                String clusterDir = clusterDefs.get(key + ".dir"); // NOI18N
                if (clusterDir == null) {
                    // Not a list of modules.
                    // XXX could also just read clusters.list
                    continue;
                }
                String val = entry.getValue();
                StringTokenizer tok = new StringTokenizer(val, ", "); // NOI18N
                while (tok.hasMoreTokens()) {
                    String p = tok.nextToken();
                    clusterLocationsHere.put(p, clusterDir);
                }
            }
            clusterLocations.put(nbroot, clusterLocationsHere);
        }
        return clusterLocationsHere.get(path);
    }
    
    // NONSTATIC PART
    
    /** all module entries, indexed by cnb */
    private Map<String,ModuleEntry> entries;
    
    /** originally passed top-level dir */
    private final File home;

    /** whether this list is for netbeans.org and may not yet include experimental modules; cf. #62221 */
    private boolean lazyNetBeansOrgList;
    
    private ModuleList(Map<String,ModuleEntry> entries, File home, boolean lazyNetBeansOrgList) {
        this.entries = entries;
        this.home = home;
        this.lazyNetBeansOrgList = lazyNetBeansOrgList;
    }
    
    public @Override String toString() {
        return "ModuleList[" + home + "]" + (lazyNetBeansOrgList ? "[lazy]" : "") + entries.values(); // NOI18N
    }
    
    /**
     * Merge a bunch of module lists into one.
     * In case of conflict (by CNB), earlier entries take precedence.
     */
    private static ModuleList merge(ModuleList[] lists, File home) {
        Map<String,ModuleEntry> entries = new HashMap<String,ModuleEntry>();
        for (ModuleList list : lists) {
            list.maybeRescanNetBeansOrgSources();
            for (Map.Entry<String,ModuleEntry> entry : list.entries.entrySet()) {
                String cnb = entry.getKey();
                if (!entries.containsKey(cnb)) {
                    entries.put(cnb, entry.getValue());
                }
            }
        }
        return new ModuleList(entries, home, false);
    }
    
    private void maybeRescanNetBeansOrgSources() {
        if (lazyNetBeansOrgList) {
            lazyNetBeansOrgList = false;
            File nbdestdir = new File(home, DEST_DIR_IN_NETBEANS_ORG);
            Map<String,ModuleEntry> _entries = new HashMap<String,ModuleEntry>(entries); // #68513: possible race condition
            doScanNetBeansOrgSources(_entries, home, DEPTH_NB_ALL, home, nbdestdir, null, false);
            entries = _entries;
        }
    }
    
    /**
     * Find an entry by name.
     * @param codeNameBase code name base of the module
     * @return the matching module, or null if there is none such
     */
    public ModuleEntry getEntry(String codeNameBase) {
        ModuleEntry e = entries.get(codeNameBase);
        if (e != null) {
            return e;
        } else {
            maybeRescanNetBeansOrgSources();
            return entries.get(codeNameBase);
        }
    }
    
    /**
     * Get all known entries at once.
     * @return all known module entries
     */
    public Set<ModuleEntry> getAllEntries() {
        maybeRescanNetBeansOrgSources();
        return new HashSet<ModuleEntry>(entries.values());
    }
    
    /**
     * Get all known entries at once, but do not look for experimental netbeans.org modules.
     * If a previous call to {@link #getAllEntries} or {@link #getEntry} has forced a full
     * source scan of netbeans.org in order to find experimental modules (i.e. those not in
     * the standard clusters), then this will be the same as {@link #getAllEntries}. Otherwise
     * it will include only the stable modules, which may be faster. For module lists that are
     * not from a netbeans.org source tree, this is the same as {@link #getAllEntries}.
     * @return all known module entries
     */
    public Set<ModuleEntry> getAllEntriesSoft() {
        return new HashSet<ModuleEntry>(entries.values());
    }
    
    public static LocalizedBundleInfo loadBundleInfo(File projectDir) {
        LocalizedBundleInfo bundleInfo = Util.findLocalizedBundleInfo(projectDir);
        return bundleInfo == null ? LocalizedBundleInfo.EMPTY : bundleInfo;
    }

}
