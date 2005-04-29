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
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Property;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Scans for known modules.
 * Precise algorithm summarized in issue #42681.
 * @author Jesse Glick
 */
final class ModuleListParser {

    /** Synch with org.netbeans.modules.apisupport.project.ModuleList.DEPTH_NB_ALL */
    private static final int DEPTH_NB_ALL = 3;
    /** Synch with org.netbeans.modules.apisupport.project.ModuleList.DEPTH_EXTERNAL */
    private static final int DEPTH_EXTERNAL = 2;
    
    private static Map/*<File,Map<String,Entry>>*/ SOURCE_SCAN_CACHE = new HashMap();
    private static Map/*<File,Map<String,Entry>>*/ BINARY_SCAN_CACHE = new HashMap();
    
    /**
     * Find all NBM projects in a root, possibly from cache.
     */
    private static Map/*<String,Entry>*/ scanSources(File root, Hashtable properties, int depth, Project project) throws IOException {
        Map/*<String,Entry>*/ entries = (Map) SOURCE_SCAN_CACHE.get(root);
        if (entries == null) {
            if (project != null) {
                project.log("Scanning for modules in " + root);
            }
            entries = new HashMap();
            doScanSources(entries, root, depth, root, properties, null);
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
    private static void doScanSources(Map/*<String,Entry>*/ entries, File dir, int depth, File root, Hashtable properties, String pathPrefix) throws IOException {
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
            try {
                scanPossibleProject(kids[i], entries, root, properties, newPathPrefix);
            } catch (SAXException e) {
                throw (IOException) new IOException(e.toString()).initCause(e);
            }
            doScanSources(entries, kids[i], depth - 1, root, properties, newPathPrefix);
        }
    }
    
    /**
     * Check a single dir to see if it is an NBM project, and if so, register it.
     */
    private static void scanPossibleProject(File dir, Map/*<String,Entry>*/ entries, File root, Hashtable properties, String path) throws IOException, SAXException {
        File nbproject = new File(dir, "nbproject");
        File projectxml = new File(nbproject, "project.xml");
        if (!projectxml.isFile()) {
            return;
        }
        Document doc = XMLUtil.parse(new InputSource(projectxml.toURI().toString()),
                                     false, true, /*XXX*/null, null);
        Element typeEl = XMLUtil.findElement(doc.getDocumentElement(), "type", ParseProjectXml.PROJECT_NS);
        if (!XMLUtil.findText(typeEl).equals("org.netbeans.modules.apisupport.project")) {
            return;
        }
        Element configEl = XMLUtil.findElement(doc.getDocumentElement(), "configuration", ParseProjectXml.PROJECT_NS);
        Element dataEl = XMLUtil.findElement(configEl, "data", ParseProjectXml.NBM_NS_1_AND_2);
        Element cnbEl = XMLUtil.findElement(dataEl, "code-name-base", ParseProjectXml.NBM_NS_1_AND_2);
        String cnb = XMLUtil.findText(cnbEl);
        // Clumsy but the best way I know of to evaluate properties.
        Project fakeproj = new Project();
        Property faketask = new Property();
        faketask.setProject(fakeproj);
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
        File jar = fakeproj.resolveFile(fakeproj.replaceProperties("${netbeans.dest.dir}/${cluster.dir}/${module.jar}"));
        List/*<File>*/ exts = new ArrayList();
        Iterator/*<Element>*/ extEls = XMLUtil.findSubElements(dataEl).iterator();
        while (extEls.hasNext()) {
            Element ext = (Element) extEls.next();
            if (!ext.getLocalName().equals("class-path-extension")) {
                continue;
            }
            Element binaryOrigin = XMLUtil.findElement(ext, "binary-origin", ParseProjectXml.NBM_NS_1_AND_2[1]);
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
                Element runtimeRelativePath = XMLUtil.findElement(ext, "runtime-relative-path", ParseProjectXml.NBM_NS_1_AND_2[1]);
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
    }
    
    /**
     * Find all modules in a binary build, possibly from cache.
     */
    private static Map/*<String,Entry>*/ scanBinaries(Hashtable properties, Project project) throws IOException {
        File build = new File((String) properties.get("netbeans.dest.dir"));
        // Normalize ../ sequences and so on, since these are used e.g. in suite.properties for external modules.
        build = new File(build.toURI().normalize());
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
    
    /** all module entries, indexed by cnb */
    private final Map/*<String,Entry>*/ entries;
    
    /**
     * Initiates scan if not already parsed.
     * Properties interpreted:
     * <ol>
     * <li> ${nb_all} - location of NB sources, or null for external modules
     * <li> ${netbeans.dest.dir} - location of NB build
     * <li> ${basedir} - directory of this project (ignored unless path is set)
     * <li> ${nb.cluster.TOKEN} - list of module paths included in cluster TOKEN (comma-separated)
     * <li> ${nb.cluster.TOKEN.dir} - directory in ${netbeans.dest.dir} where cluster TOKEN is built
     * </ol>
     * @param properties some properties to be used (see above)
     * @param path path to this project from inside some external root, or null for nb.org modules
     * @param project a project ref, only for logging (may be null with no loss of semantics)
     */
    public ModuleListParser(Hashtable properties, String path, Project project) throws IOException {
        String nball = (String) properties.get("nb_all");
        if (path != null) {
            // External module.
            File basedir = new File((String) properties.get("basedir"));
            if (nball != null) {
                throw new IOException("You must *not* define <path> for a netbeans.org module in " + basedir + "; fix project.xml to use the /2 schema and delete <path>");
            }
            entries = scanBinaries(properties, project);
            String[] pieces = path.split("/");
            File root = basedir;
            for (int i = pieces.length - 1; i >= 0; i--) {
                if (!root.getName().equals(pieces[i])) {
                    throw new IOException("Mismatch in " + path + " for " + basedir + File.separator + "nbproject" + File.separator + "project.xml");
                }
                root = root.getParentFile();
            }
            entries.putAll(scanSources(root, properties, DEPTH_EXTERNAL, project));
        } else {
            // netbeans.org module.
            if (nball == null) {
                throw new IOException("You must define a <path> for an external module in " + new File((String) properties.get("basedir")));
            }
            entries = scanSources(new File(nball), properties, DEPTH_NB_ALL, project);
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
