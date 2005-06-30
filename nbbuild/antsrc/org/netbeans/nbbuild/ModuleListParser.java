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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Scans for known modules.
 * Precise algorithm summarized in issue #42681 and issue #58966.
 * @author Jesse Glick
 */
final class ModuleListParser {

    /** Synch with org.netbeans.modules.apisupport.project.ModuleList.DEPTH_NB_ALL */
    private static final int DEPTH_NB_ALL = 3;
    
    private static Map/*<File,Map<String,Entry>>*/ SOURCE_SCAN_CACHE = new HashMap();
    private static Map/*<File,Map<String,Entry>>*/ SUITE_SCAN_CACHE = new HashMap();
    private static Map/*<File,Entry>*/ STANDALONE_SCAN_CACHE = new HashMap();
    private static Map/*<File,Map<String,Entry>>*/ BINARY_SCAN_CACHE = new HashMap();
    
    /**
     * Find all NBM projects in a root, possibly from cache.
     */
    private static Map/*<String,Entry>*/ scanNetBeansOrgSources(File root, Hashtable properties, Project project) throws IOException {
        Map/*<String,Entry>*/ entries = (Map) SOURCE_SCAN_CACHE.get(root);
        if (entries == null) {
            if (project != null) {
                project.log("Scanning for modules in " + root);
            }
            entries = new HashMap();
            doScanNetBeansOrgSources(entries, root, DEPTH_NB_ALL, properties, null, project);
            if (project != null) {
                project.log("Found modules: " + entries.keySet(), Project.MSG_VERBOSE);
            }
            SOURCE_SCAN_CACHE.put(root, entries);
        }
        return entries;
    }
    
    /**
     * Scan a root for all NBM projects.
     */
    private static void doScanNetBeansOrgSources(Map/*<String,Entry>*/ entries, File dir, int depth, Hashtable properties, String pathPrefix, Project project) throws IOException {
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
            scanPossibleProject(kids[i], entries, properties, newPathPrefix, ParseProjectXml.TYPE_NB_ORG, project);
            doScanNetBeansOrgSources(entries, kids[i], depth - 1, properties, newPathPrefix, project);
        }
    }
    
    /**
     * Check a single dir to see if it is an NBM project, and if so, register it.
     */
    private static boolean scanPossibleProject(File dir, Map/*<String,Entry>*/ entries, Hashtable properties, String path, int moduleType, Project project) throws IOException {
        File nbproject = new File(dir, "nbproject");
        File projectxml = new File(nbproject, "project.xml");
        if (!projectxml.isFile()) {
            return false;
        }
        Document doc;
        try {
            doc = XMLUtil.parse(new InputSource(projectxml.toURI().toString()),
                                     false, true, /*XXX*/null, null);
        } catch (Exception e) { // SAXException, IOException (#60295: e.g. encoding problem in XML)
            // Include \n so that following line can be hyperlinked
            throw (IOException) new IOException("Error parsing project file\n" + projectxml + ": " + e.getMessage()).initCause(e);
        }
        Element typeEl = XMLUtil.findElement(doc.getDocumentElement(), "type", ParseProjectXml.PROJECT_NS);
        if (!XMLUtil.findText(typeEl).equals("org.netbeans.modules.apisupport.project")) {
            return false;
        }
        Element configEl = XMLUtil.findElement(doc.getDocumentElement(), "configuration", ParseProjectXml.PROJECT_NS);
        Element dataEl = XMLUtil.findElement(configEl, "data", ParseProjectXml.NBM_NS);
        if (dataEl == null) {
            if (project != null) {
                project.log(projectxml.toString() + ": warning: module claims to be a NBM project but is missing <data xmlns=\"" + ParseProjectXml.NBM_NS + "\">; maybe an old NB 4.[01] project?", Project.MSG_WARN);
            }
            return false;
        }
        Element cnbEl = XMLUtil.findElement(dataEl, "code-name-base", ParseProjectXml.NBM_NS);
        String cnb = XMLUtil.findText(cnbEl);
        // Clumsy but the best way I know of to evaluate properties.
        Project fakeproj = new Project();
        if (project != null) {
            // Try to debug any problems in the following definitions (cf. #59849).
            Iterator it = project.getBuildListeners().iterator();
            while (it.hasNext()) {
                fakeproj.addBuildListener((BuildListener) it.next());
            }
        }
        fakeproj.setBaseDir(dir); // in case ${basedir} is used somewhere
        Property faketask = new Property();
        faketask.setProject(fakeproj);
        switch (moduleType) {
        case ParseProjectXml.TYPE_NB_ORG:
            // do nothing here
            break;
        case ParseProjectXml.TYPE_SUITE:
            faketask.setFile(new File(nbproject, "private/suite-private.properties"));
            faketask.execute();
            faketask.setFile(new File(nbproject, "suite.properties"));
            faketask.execute();
            faketask.setFile(new File(fakeproj.replaceProperties("${suite.dir}/nbproject/private/platform-private.properties")));
            faketask.execute();
            faketask.setFile(new File(fakeproj.replaceProperties("${suite.dir}/nbproject/platform.properties")));
            faketask.execute();
            break;
        case ParseProjectXml.TYPE_STANDALONE:
            faketask.setFile(new File(nbproject, "private/platform-private.properties"));
            faketask.execute();
            faketask.setFile(new File(nbproject, "platform.properties"));
            faketask.execute();
            break;
        default:
            assert false : moduleType;
        }
        faketask.setFile(new File(nbproject, "private/private.properties".replace('/', File.separatorChar)));
        faketask.execute();
        faketask.setFile(new File(nbproject, "project.properties"));
        faketask.execute();
        faketask.setFile(null);
        faketask.setName("module.jar.dir");
        faketask.setValue("modules");
        faketask.execute();
        assert fakeproj.getProperty("module.jar.dir") != null : fakeproj.getProperties();
        faketask.setName("module.jar.basename");
        faketask.setValue(cnb.replace('.', '-') + ".jar");
        faketask.execute();
        faketask.setName("module.jar");
        faketask.setValue(fakeproj.replaceProperties("${module.jar.dir}/${module.jar.basename}"));
        faketask.execute();
        switch (moduleType) {
        case ParseProjectXml.TYPE_NB_ORG:
            assert path != null;
            // Find the associated cluster.
            Iterator it = properties.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String val = (String) entry.getValue();
                String[] modules = val.split(", *");
                if (Arrays.asList(modules).contains(path)) {
                    String key = (String) entry.getKey();
                    String clusterDir = (String) properties.get(key + ".dir");
                    if (clusterDir != null) {
                        faketask.setName("cluster.dir");
                        faketask.setValue(clusterDir);
                        faketask.execute();
                        break;
                    }
                }
            }
            faketask.setName("cluster.dir");
            faketask.setValue("extra"); // fallback
            faketask.execute();
            faketask.setName("netbeans.dest.dir");
            faketask.setValue((String) properties.get("netbeans.dest.dir"));
            faketask.execute();
            faketask.setName("cluster");
            faketask.setValue(fakeproj.replaceProperties("${netbeans.dest.dir}/${cluster.dir}"));
            faketask.execute();
            break;
        case ParseProjectXml.TYPE_SUITE:
            assert path == null;
            faketask.setName("suite.dir");
            faketask.setValue((String) properties.get("suite.dir"));
            faketask.execute();
            faketask.setName("cluster");
            faketask.setValue(fakeproj.replaceProperties("${suite.dir}/build/cluster"));
            faketask.execute();
            break;
        case ParseProjectXml.TYPE_STANDALONE:
            assert path == null;
            faketask.setName("cluster");
            faketask.setValue(fakeproj.replaceProperties("${basedir}/build/cluster"));
            faketask.execute();
            break;
        default:
            assert false : moduleType;
        }
        File jar = fakeproj.resolveFile(fakeproj.replaceProperties("${cluster}/${module.jar}"));
        List/*<File>*/ exts = new ArrayList();
        Iterator/*<Element>*/ extEls = XMLUtil.findSubElements(dataEl).iterator();
        while (extEls.hasNext()) {
            Element ext = (Element) extEls.next();
            if (!ext.getLocalName().equals("class-path-extension")) {
                continue;
            }
            Element binaryOrigin = XMLUtil.findElement(ext, "binary-origin", ParseProjectXml.NBM_NS);
            File binary;
            if (binaryOrigin != null) {
                String reltext = XMLUtil.findText(binaryOrigin);
                String nball = (String) properties.get("nb_all");
                if (nball != null) {
                    faketask.setName("nb_all");
                    faketask.setValue(nball);
                    faketask.execute();
                }
                fakeproj.setBaseDir(dir);
                binary = fakeproj.resolveFile(fakeproj.replaceProperties(reltext));
            } else {
                Element runtimeRelativePath = XMLUtil.findElement(ext, "runtime-relative-path", ParseProjectXml.NBM_NS);
                if (runtimeRelativePath == null) {
                    throw new IOException("Have malformed <class-path-extension> in " + projectxml);
                }
                String reltext = XMLUtil.findText(runtimeRelativePath);
                // No need to evaluate property refs in it - it is *not* substitutable-text in the schema.
                binary = new File(jar.getParentFile(), reltext.replace('/', File.separatorChar));
            }
            exts.add(binary);
        }
        Entry entry = new Entry(cnb, jar, (File[]) exts.toArray(new File[exts.size()]), dir);
        if (entries.containsKey(cnb)) {
            throw new IOException("Duplicated module " + cnb + ": found in " + entries.get(cnb) + " and " + entry);
        } else {
            entries.put(cnb, entry);
        }
        return true;
    }
    
    /**
     * Find all modules in a binary build, possibly from cache.
     */
    private static Map/*<String,Entry>*/ scanBinaries(Hashtable properties, Project project) throws IOException {
        String buildS = (String) properties.get("netbeans.dest.dir");
        File basedir = new File((String) properties.get("basedir"));
        if (buildS == null) {
            throw new IOException("No definition of netbeans.dest.dir in " + basedir);
        }
        // Resolve against basedir, and normalize ../ sequences and so on in case they are used.
        // Neither operation is likely to be needed, but just in case.
        File build = FileUtils.newFileUtils().normalize(FileUtils.newFileUtils().resolveFile(basedir, buildS).getAbsolutePath());
        if (!build.isDirectory()) {
            throw new IOException("No such netbeans.dest.dir: " + build);
        }
        Map/*<String,Entry>*/ entries = (Map) BINARY_SCAN_CACHE.get(build);
        if (entries == null) {
            if (project != null) {
                project.log("Scanning for modules in " + build);
            }
            entries = new HashMap();
            doScanBinaries(build, entries);
            if (project != null) {
                project.log("Found modules: " + entries.keySet());
            }
            BINARY_SCAN_CACHE.put(build, entries);
        }
        return entries;
    }
    
    private static final String[] MODULE_DIRS = {
        "modules",
        "modules/eager",
        "modules/autoload",
        "lib",
        "core",
    };
    /**
     * Look for all possible modules in a NB build.
     * Checks modules/{,autoload/,eager/}*.jar as well as well-known core/*.jar and lib/boot.jar in each cluster.
     * XXX would be slightly more precise to check config/Modules/*.xml rather than scan for module JARs.
     */
    private static void doScanBinaries(File build, Map/*<String,Entry>*/ entries) throws IOException {
        File[] clusters = build.listFiles();
        if (clusters == null) {
            throw new IOException("Cannot examine dir " + build);
        }
        for (int i = 0; i < clusters.length; i++) {
            for (int j = 0; j < MODULE_DIRS.length; j++) {
                File dir = new File(clusters[i], MODULE_DIRS[j].replace('/', File.separatorChar));
                if (!dir.isDirectory()) {
                    continue;
                }
                File[] jars = dir.listFiles();
                if (jars == null) {
                    throw new IOException("Cannot examine dir " + dir);
                }
                for (int k = 0; k < jars.length; k++) {
                    File m = jars[k];
                    if (!m.getName().endsWith(".jar")) {
                        continue;
                    }
                    JarFile jf = new JarFile(m);
                    try {
                        Attributes attr = jf.getManifest().getMainAttributes();
                        String codename = attr.getValue("OpenIDE-Module");
                        if (codename == null) {
                            continue;
                        }
                        String codenamebase;
                        int slash = codename.lastIndexOf('/');
                        if (slash == -1) {
                            codenamebase = codename;
                        } else {
                            codenamebase = codename.substring(0, slash);
                        }
                        String cp = attr.getValue("Class-Path");
                        File[] exts;
                        if (cp == null) {
                            exts = new File[0];
                        } else {
                            String[] pieces = cp.split(" +");
                            exts = new File[pieces.length];
                            for (int l = 0; l < pieces.length; l++) {
                                exts[l] = new File(dir, pieces[l].replace('/', File.separatorChar));
                            }
                        }
                        Entry entry = new Entry(codenamebase, m, exts, dir);
                        if (entries.containsKey(codenamebase)) {
                            throw new IOException("Duplicated module " + codenamebase + ": found in " + entries.get(codenamebase) + " and " + entry);
                        } else {
                            entries.put(codenamebase, entry);
                        }
                    } finally {
                        jf.close();
                    }
                }
            }
        }
    }
    
    private static Map/*<String,Entry>*/ scanSuiteSources(Hashtable properties, Project project) throws IOException {
        File basedir = new File((String) properties.get("basedir"));
        String suiteDir = (String) properties.get("suite.dir");
        if (suiteDir == null) {
            throw new IOException("No definition of suite.dir in " + basedir);
        }
        File suite = FileUtils.newFileUtils().resolveFile(basedir, suiteDir);
        if (!suite.isDirectory()) {
            throw new IOException("No such suite " + suite);
        }
        Map/*<String,Entry>*/ entries = (Map) SUITE_SCAN_CACHE.get(suite);
        if (entries == null) {
            if (project != null) {
                project.log("Scanning for modules in suite " + suite);
            }
            entries = new HashMap();
            doScanSuite(entries, suite, properties, project);
            if (project != null) {
                project.log("Found modules: " + entries.keySet(), Project.MSG_VERBOSE);
            }
            SUITE_SCAN_CACHE.put(suite, entries);
        }
        return entries;
    }
    
    private static void doScanSuite(Map/*<String,Entry>*/ entries, File suite, Hashtable properties, Project project) throws IOException {
        Project fakeproj = new Project();
        fakeproj.setBaseDir(suite); // in case ${basedir} is used somewhere
        Property faketask = new Property();
        faketask.setProject(fakeproj);
        faketask.setFile(new File(suite, "nbproject/private/private.properties".replace('/', File.separatorChar)));
        faketask.execute();
        faketask.setFile(new File(suite, "nbproject/project.properties".replace('/', File.separatorChar)));
        faketask.execute();
        String modulesS = fakeproj.getProperty("modules");
        if (modulesS == null) {
            throw new IOException("No definition of modules in " + suite);
        }
        String[] modules = Path.translatePath(fakeproj, modulesS);
        for (int i = 0; i < modules.length; i++) {
            File module = new File(modules[i]);
            if (!module.isDirectory()) {
                throw new IOException("No such module " + module + " referred to from " + suite);
            }
            if (!scanPossibleProject(module, entries, properties, null, ParseProjectXml.TYPE_SUITE, project)) {
                throw new IOException("No valid module found in " + module + " referred to from " + suite);
            }
        }
    }
    
    private static Entry scanStandaloneSource(Hashtable properties, Project project) throws IOException {
        File basedir = new File((String) properties.get("project"));
        Entry entry = (Entry) STANDALONE_SCAN_CACHE.get(basedir);
        if (entry == null) {
            Map/*<String,Entries>*/ entries = new HashMap();
            if (!scanPossibleProject(basedir, entries, properties, null, ParseProjectXml.TYPE_STANDALONE, project)) {
                throw new IOException("No valid module found in " + basedir);
            }
            assert entries.size() == 1;
            entry = (Entry) entries.values().iterator().next();
            STANDALONE_SCAN_CACHE.put(basedir, entry);
        }
        return entry;
    }
    
    /** all module entries, indexed by cnb */
    private final Map/*<String,Entry>*/ entries;
    
    /**
     * Initiates scan if not already parsed.
     * Properties interpreted:
     * <ol>
     * <li> ${nb_all} - location of NB sources (used only for netbeans.org modules)
     * <li> ${netbeans.dest.dir} - location of NB build
     * <li> ${basedir} - directory of this project (used only for standalone modules)
     * <li> ${suite.dir} - directory of the suite (used only for suite modules)
     * <li> ${nb.cluster.TOKEN} - list of module paths included in cluster TOKEN (comma-separated) (used only for netbeans.org modules)
     * <li> ${nb.cluster.TOKEN.dir} - directory in ${netbeans.dest.dir} where cluster TOKEN is built (used only for netbeans.org modules)
     * </ol>
     * @param properties some properties to be used (see above)
     * @param type the type of project
     * @param project a project ref, only for logging (may be null with no loss of semantics)
     */
    public ModuleListParser(Hashtable properties, int type, Project project) throws IOException {
        String nball = (String) properties.get("nb_all");
        if (type != ParseProjectXml.TYPE_NB_ORG) {
            // External module.
            File basedir = new File((String) properties.get("basedir"));
            if (nball != null) {
                throw new IOException("You must *not* declare <suite-component/> or <standalone/> for a netbeans.org module in " + basedir + "; fix project.xml to use the /2 schema");
            }
            entries = scanBinaries(properties, project);
            if (type == ParseProjectXml.TYPE_SUITE) {
                entries.putAll(scanSuiteSources(properties, project));
            } else {
                assert type == ParseProjectXml.TYPE_STANDALONE;
                Entry e = scanStandaloneSource(properties, project);
                entries.put(e.getCnb(), e);
            }
        } else {
            // netbeans.org module.
            if (nball == null) {
                throw new IOException("You must declare either <suite-component/> or <standalone/> for an external module in " + new File((String) properties.get("basedir")));
            }
            String netbeansDestDir = (String)properties.get("netbeans.dest.dir");
            boolean scanNetBeansSources = false;
            File d1 = new File(netbeansDestDir);
            if (d1.getName().equals("netbeans")) {
                File d2 = d1.getParentFile();
                if (d2 != null && d2.getName().equals("nbbuild")) {
                    File d3 = d2.getParentFile();
                    if (d3 != null && d3.equals(new File(nball))) {
                        scanNetBeansSources = true;
                    }
                }
            }
            // If netbeans.dest.dir is <nball>/nbbuild/netbeans scan sources otherwise binaries.
            if (scanNetBeansSources) {
              entries = scanNetBeansOrgSources(new File(nball), properties, project);
            } else {
              entries = scanBinaries(properties, project);
              // module itself has to be added because it doesn't have to be in binaries
              Entry e = scanStandaloneSource(properties, project);
              entries.put(e.getCnb(), e);
            }
        }
    }
    
    /**
     * Find one entry by code name base.
     * @param cnb the desired code name base
     * @return the matching entry or null
     */
    public Entry findByCodeNameBase(String cnb) {
        return (Entry)entries.get(cnb);
    }
    
    /**
     * One entry in the file.
     */
    public static final class Entry {
        
        private final String cnb;
        private final File jar;
        private final File[] classPathExtensions;
        private final File sourceLocation;
        
        Entry(String cnb, File jar, File[] classPathExtensions, File sourceLocation) {
            this.cnb = cnb;
            this.jar = jar;
            this.classPathExtensions = classPathExtensions;
            this.sourceLocation = sourceLocation;
        }
        
        /**
         * Get the code name base, e.g. org.netbeans.modules.ant.grammar.
         */
        public String getCnb() {
            return cnb;
        }
        
        /**
         * Get the absolute JAR location, e.g. .../ide5/modules/org-netbeans-modules-ant-grammar.jar.
         */
        public File getJar() {
            return jar;
        }
        
        /**
         * Get a list of extensions to the class path of this module (may be empty).
         */
        public File[] getClassPathExtensions() {
            return classPathExtensions;
        }
        
        public String toString() {
            return (sourceLocation != null ? sourceLocation : jar).getAbsolutePath();
        }
        
    }

}
