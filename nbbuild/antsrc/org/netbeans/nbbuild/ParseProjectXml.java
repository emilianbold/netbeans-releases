/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parse a projectized module's <code>nbproject/project.xml</code> and
 * define various useful Ant properties based on the result.
 * @author Jesse Glick
 */
public final class ParseProjectXml extends Task {

    static final String PROJECT_NS = "http://www.netbeans.org/ns/project/1";
    static final String NBM_NS = "http://www.netbeans.org/ns/nb-module-project/2";
    
    static final int TYPE_NB_ORG = 0;
    static final int TYPE_SUITE = 1;
    static final int TYPE_STANDALONE = 2;

    private File project;
    /**
     * Set the NetBeans module project to work on.
     */
    public void setProject(File f) {
        project = f;
    }
    private File projectFile;
    /**
     * Another option is to directly point to project file.
     * Used only in unit testing.
     */
    public void setProjectFile (File f) {
        projectFile = f;
    }
    private File getProjectFile () {
        if (projectFile != null) {
            return projectFile;
        }
        return new File(new File(project, "nbproject"), "project.xml");
    }

    private String publicPackagesProperty;
    /**
     * Set the property to set a list of
     * OpenIDE-Module-Public-Packages to.
     */
    public void setPublicPackagesProperty(String s) {
        publicPackagesProperty = s;
    }
    
    private String friendsProperty;
    /**
     * Set the property to set a list of
     * OpenIDE-Module-Friends to.
     */
    public void setFriendsProperty(String s) {
        friendsProperty = s;
    }

    private String javadocPackagesProperty;
    /**
     * Set the property to set a list of public packages for Javadoc
     * to.
     */
    public void setJavadocPackagesProperty(String s) {
        javadocPackagesProperty = s;
    }

    private String moduleDependenciesProperty;
    /**
     * Set the property to set a list of
     * OpenIDE-Module-Module-Dependencies to, based on the list of
     * stated run-time dependencies.
     */
    public void setModuleDependenciesProperty(String s) {
        moduleDependenciesProperty = s;
    }

    private String codeNameBaseDashesProperty;
    /**
     * Set the property to set the module code name base (separated by
     * dashes not dots) to.
     */
    public void setCodeNameBaseDashesProperty(String s) {
        codeNameBaseDashesProperty = s;
    }

    private String codeNameBaseSlashesProperty;
    /**
     * Set the property to set the module code name base (separated by
     * slashes not dots) to.
     */
    public void setCodeNameBaseSlashesProperty(String s) {
        codeNameBaseSlashesProperty = s;
    }

    private String domainProperty;
    /**
     * Set the property to set the module's netbeans.org domain to.
     * Only applicable to modules in netbeans.org (i.e. no <path>).
     */
    public void setDomainProperty(String s) {
        domainProperty = s;
    }

    private String moduleClassPathProperty;
    /**
     * Set the property to set the computed module class path to,
     * based on the list of stated compile-time dependencies.
     */
    public void setModuleClassPathProperty(String s) {
        moduleClassPathProperty = s;
    }

    private String moduleRunClassPathProperty;
    /**
     * Set the property to set the computed module runtime class path to.
     * Currently identical to the regular class path with the exception
     * that original JARs are used, never public-package-only JARs.
     * XXX In the future should however reflect &lt;run-dependency/&gt;
     * rather than &lt;compile-dependency/&gt; and should also include
     * transitive dependencies.
     */
    public void setModuleRunClassPathProperty(String s) {
        moduleRunClassPathProperty = s;
    }
    
    private File publicPackageJarDir;
    /**
     * Set the location of a directory in which to look for and create
     * JARs containing just the public packages of appropriate
     * compile-time dependencies.
     */
    public void setPublicPackageJarDir(File d) {
        publicPackageJarDir = d;
    }
    
    private String classPathExtensionsProperty;
    /**
     * Set the property to set the declared Class-Path attribute to.
     */
    public void setClassPathExtensionsProperty(String s) {
        classPathExtensionsProperty = s;
    }

    private void define(String prop, String val) {
        log("Setting " + prop + "=" + val, Project.MSG_VERBOSE);
        String old = getProject().getProperty(prop);
        if (old != null && !old.equals(val)) {
            getProject().log("Warning: " + prop + " was already set to " + old, Project.MSG_WARN);
        }
        getProject().setNewProperty(prop, val);
    }
    
    public void execute() throws BuildException {
        try {
            if (getProjectFile() == null) {
                throw new BuildException("You must set 'project' or 'projectfile'", getLocation());
            }
            // XXX validate against nbm-project{,2}.xsd; does this require JDK 1.5?
            // Cf.: ant/project/eg/ValidateAllBySchema.java
            // XXX share parse w/ ModuleListParser
            Document pDoc = XMLUtil.parse(new InputSource(getProjectFile ().toURI().toString()),
                                          false, true, /*XXX*/null, null);
            if (publicPackagesProperty != null || javadocPackagesProperty != null) {
                PublicPackage[] pkgs = getPublicPackages(pDoc);
                if (publicPackagesProperty != null) {
                    String val;
                    if (pkgs.length > 0) {
                        String sep = "";
                        StringBuffer b = new StringBuffer();
                        for (int i = 0; i < pkgs.length; i++) {
                            b.append(sep);
                            
                            String name = pkgs[i].name;
                            if (name.indexOf (',') >= 0) {
                                throw new BuildException ("Package name cannot contain ',' as " + pkgs[i], getLocation ());
                            }
                            if (name.indexOf ('*') >= 0) {
                                throw new BuildException ("Package name cannot contain '*' as " + pkgs[i], getLocation ());
                            }
                            
                            b.append(name);
                            if (pkgs[i].subpackages) {
                                b.append (".**");
                            } else {
                                b.append(".*");
                            }
                            sep = ", ";
                        }
                        val = b.toString();
                    } else {
                        val = "-";
                    }
                    define(publicPackagesProperty, val);
                }
                NO_JAVA_DOC_PROPERTY_SET: if (javadocPackagesProperty != null) {
                    if (pkgs.length > 0) {
                        String sep = ", ";
                        StringBuffer b = new StringBuffer();
                        for (int i = 0; i < pkgs.length; i++) {
                            b.append(sep);
                            if (pkgs[i].subpackages) {
                                if (getProject().getProperty(javadocPackagesProperty) == null) {
                                    String msg = javadocPackagesProperty + " cannot be set as <subpackages> does not work for Javadoc (see <subpackages>" + pkgs[i].name + "</subpackages> tag in " + getProjectFile () + "). Set the property in project.properties if you want to build Javadoc.";
                                    // #52135: do not halt the build, just leave it.
                                    getProject().log("Warning: " + msg, Project.MSG_WARN);
                                }
                                break NO_JAVA_DOC_PROPERTY_SET;
                            }
                            b.append(pkgs[i].name);
                            sep = ", ";
                        }
                        define(javadocPackagesProperty, b.toString());
                    }
                }
            }
            if (friendsProperty != null) {
                String[] friends = getFriends(pDoc);
                if (friends != null) {
                    StringBuffer b = new StringBuffer();
                    for (int i = 0; i < friends.length; i++) {
                        if (i > 0) {
                            b.append(", ");
                        }
                        b.append(friends[i]);
                    }
                    define(friendsProperty, b.toString());
                }
            }
            ModuleListParser modules = null;
            Dep[] deps = null;
            if (moduleDependenciesProperty != null || moduleClassPathProperty != null || moduleRunClassPathProperty != null) {
                String nball = getProject().getProperty("nb_all");
                Hashtable properties = getProject().getProperties();
                properties.put("project", project.getAbsolutePath());
                modules = new ModuleListParser(properties, getModuleType(pDoc), getProject());
                ModuleListParser.Entry myself = modules.findByCodeNameBase(getCodeNameBase(pDoc));
                if (myself == null) { // #71130
                    ModuleListParser.resetCaches();
                    modules = new ModuleListParser(properties, getModuleType(pDoc), getProject());
                    myself = modules.findByCodeNameBase(getCodeNameBase(pDoc));
                    assert myself != null;
                }
                deps = getDeps(pDoc, modules);
            }
            if (moduleDependenciesProperty != null) {
                if (moduleDependenciesProperty != null) {
                    StringBuffer b = new StringBuffer();
                    for (int i = 0; i < deps.length; i++) {
                        if (!deps[i].run) {
                            continue;
                        }
                        if (b.length() > 0) {
                            b.append(", ");
                        }
                        b.append(deps[i]);
                    }
                    if (b.length() > 0) {
                        define(moduleDependenciesProperty, b.toString());
                    }
                }
            }
            if (codeNameBaseDashesProperty != null) {
                String cnb = getCodeNameBase(pDoc);
                define(codeNameBaseDashesProperty, cnb.replace('.', '-'));
            }
            if (codeNameBaseSlashesProperty != null) {
                String cnb = getCodeNameBase(pDoc);
                define(codeNameBaseSlashesProperty, cnb.replace('.', '/'));
            }
            if (moduleClassPathProperty != null) {
                String cp = computeClasspath(pDoc, modules, deps, false);
                define(moduleClassPathProperty, cp);
            }
            if (moduleRunClassPathProperty != null) {
                String cp = computeClasspath(pDoc, modules, deps, true);
                define(moduleRunClassPathProperty, cp);
            }
            if (domainProperty != null) {
                if (getModuleType(pDoc) != TYPE_NB_ORG) {
                    throw new BuildException("Cannot set " + domainProperty + " for a non-netbeans.org module", getLocation());
                }
                File nball = new File(getProject().getProperty("nb_all"));
                File basedir = getProject().getBaseDir();
                File dir = basedir;
                while (true) {
                    File parent = dir.getParentFile();
                    if (parent == null) {
                        throw new BuildException("Could not find " + basedir + " inside " + nball + " for purposes of defining " + domainProperty);
                    }
                    if (parent.equals(nball)) {
                        define(domainProperty, dir.getName());
                        break;
                    }
                    dir = parent;
                }
            }
            if (classPathExtensionsProperty != null) {
                String val = computeClassPathExtensions(pDoc);
                if (val != null) {
                    define(classPathExtensionsProperty, val);
                }
            }
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e, getLocation());
        }
    }

    private Element getConfig(Document pDoc) throws BuildException {
        Element e = pDoc.getDocumentElement();
        Element c = XMLUtil.findElement(e, "configuration", PROJECT_NS);
        if (c == null) {
            throw new BuildException("No <configuration>", getLocation());
        }
        Element d = XMLUtil.findElement(c, "data", NBM_NS);
        if (d == null) {
            throw new BuildException("No <data> in " + getProjectFile(), getLocation());
        }
        return d;
    }
    
    private static final class PublicPackage extends Object {
        public final String name;
        public boolean subpackages;
        
        public PublicPackage (String name, boolean subpackages) {
            this.name = name;
            this.subpackages = subpackages;
        }
    }

    private PublicPackage[] getPublicPackages(Document d) throws BuildException {
        Element cfg = getConfig(d);
        Element pp = XMLUtil.findElement(cfg, "public-packages", NBM_NS);
        if (pp == null) {
            pp = XMLUtil.findElement(cfg, "friend-packages", NBM_NS);
        }
        if (pp == null) {
            throw new BuildException("No <public-packages>", getLocation());
        }
        List/*<Element>*/ l = XMLUtil.findSubElements(pp);
        List/*<PublicPackage>*/ pkgs = new ArrayList();
        Iterator it = l.iterator();
        while (it.hasNext()) {
            Element p = (Element)it.next();
            boolean sub = false;
            if ("friend".equals(p.getNodeName())) {
                continue;
            }
            if (!"package".equals (p.getNodeName ())) {
                if (!("subpackages".equals (p.getNodeName ()))) {
                    throw new BuildException ("Strange element name, should be package or subpackages: " + p.getNodeName (), getLocation ());
                }
                sub = true;
            }
            
            String t = XMLUtil.findText(p);
            if (t == null) {
                throw new BuildException("No text in <package>", getLocation());
            }
            pkgs.add(new PublicPackage(t, sub));
        }
        return (PublicPackage[]) pkgs.toArray(new PublicPackage[pkgs.size()]);
    }
    
    private String[] getFriends(Document d) throws BuildException {
        Element cfg = getConfig(d);
        Element pp = XMLUtil.findElement(cfg, "friend-packages", NBM_NS);
        if (pp == null) {
            return null;
        }
        List/*<String>*/ friends = new ArrayList();
        List/*<Element>*/ l = XMLUtil.findSubElements(pp);
        Iterator it = l.iterator();
        boolean other = false;
        while (it.hasNext()) {
            Element p = (Element) it.next();
            if ("friend".equals(p.getNodeName())) {
                String t = XMLUtil.findText(p);
                if (t == null) {
                    throw new BuildException("No text in <friend>", getLocation());
                }
                friends.add(t);
            } else {
                other = true;
            }
        }
        if (friends.isEmpty()) {
            throw new BuildException("Must have at least one <friend> in <friend-packages>", getLocation());
        }
        if (!other) {
            throw new BuildException("Must have at least one <package> in <friend-packages>", getLocation());
        }
        return (String[]) friends.toArray(new String[friends.size()]);
    }

    private final class Dep {
        private final ModuleListParser modules;
        /** will be e.g. org.netbeans.modules.form */
        public String codenamebase;
        public String release = null;
        public String spec = null;
        public boolean impl = false;
        public boolean compile = false;
        public boolean run = false;
        
        public Dep(ModuleListParser modules) {
            this.modules = modules;
        }
        
        public String toString() throws BuildException {
            StringBuffer b = new StringBuffer(codenamebase);
            if (release != null) {
                b.append('/');
                b.append(release);
            }
            if (spec != null) {
                b.append(" > ");
                b.append(spec);
                assert !impl;
            }
            if (impl) {
                b.append(" = "); // NO18N
                String implVers = implementationVersionOf(modules, codenamebase);
                if (implVers == null) {
                    throw new BuildException("No OpenIDE-Module-Implementation-Version found in " + codenamebase);
                }
                b.append(implVers);
            }
            return b.toString();
        }
        
        private String implementationVersionOf(ModuleListParser modules, String cnb) throws BuildException {
            File jar = computeClasspathModuleLocation(modules, cnb, null, null);
            if (!jar.isFile()) {
                throw new BuildException("No such classpath entry: " + jar, getLocation());
            }
            try {
                JarFile jarFile = new JarFile(jar, false);
                try {
                    return jarFile.getManifest().getMainAttributes().getValue("OpenIDE-Module-Implementation-Version");
                } finally {
                    jarFile.close();
                }
            } catch (IOException e) {
                throw new BuildException(e, getLocation());
            }
        }

        private boolean matches(Attributes attr) {
            if (release != null) {
                String givenCodeName = attr.getValue("OpenIDE-Module");
                int slash = givenCodeName.indexOf('/');
                int givenRelease = -1;
                if (slash != -1) {
                    assert codenamebase.equals(givenCodeName.substring(0, slash));
                    givenRelease = Integer.parseInt(givenCodeName.substring(slash + 1));
                }
                int dash = release.indexOf('-');
                if (dash == -1) {
                    if (Integer.parseInt(release) != givenRelease) {
                        return false;
                    }
                } else {
                    int lower = Integer.parseInt(release.substring(0, dash));
                    int upper = Integer.parseInt(release.substring(dash + 1));
                    if (givenRelease < lower || givenRelease > upper) {
                        return false;
                    }
                }
            }
            if (spec != null) {
                String givenSpec = attr.getValue("OpenIDE-Module-Specification-Version");
                if (givenSpec == null) {
                    return false;
                }
                // XXX cannot use org.openide.modules.SpecificationVersion from here
                int[] specVals = digitize(spec);
                int[] givenSpecVals = digitize(givenSpec);
                int len1 = specVals.length;
                int len2 = givenSpecVals.length;
                int max = Math.max(len1, len2);
                for (int i = 0; i < max; i++) {
                    int d1 = ((i < len1) ? specVals[i] : 0);
                    int d2 = ((i < len2) ? givenSpecVals[i] : 0);
                    if (d1 < d2) {
                        break;
                    } else if (d1 > d2) {
                        return false;
                    }
                }
            }
            if (impl) {
                if (attr.getValue("OpenIDE-Module-Implementation-Version") == null) {
                    return false;
                }
            }
            return true;
        }
        private int[] digitize(String spec) throws NumberFormatException {
            StringTokenizer tok = new StringTokenizer(spec, ".");
            int len = tok.countTokens();
            int[] digits = new int[len];
            for (int i = 0; i < len; i++) {
                digits[i] = Integer.parseInt(tok.nextToken());
            }
            return digits;
        }
        
    }

    private Dep[] getDeps(Document pDoc, ModuleListParser modules) throws BuildException {
        Element cfg = getConfig(pDoc);
        Element md = XMLUtil.findElement(cfg, "module-dependencies", NBM_NS);
        if (md == null) {
            throw new BuildException("No <module-dependencies>", getLocation());
        }
        List/*<Element>*/ l = XMLUtil.findSubElements(md);
        List/*<Dep>*/ deps = new ArrayList();
        Iterator it = l.iterator();
        while (it.hasNext()) {
            Element dep = (Element)it.next();
            Dep d = new Dep(modules);
            Element cnb = XMLUtil.findElement(dep, "code-name-base", NBM_NS);
            if (cnb == null) {
                throw new BuildException("No <code-name-base>", getLocation());
            }
            String t = XMLUtil.findText(cnb);
            if (t == null) {
                throw new BuildException("No text in <code-name-base>", getLocation());
            }
            d.codenamebase = t;
            Element rd = XMLUtil.findElement(dep, "run-dependency", NBM_NS);
            if (rd != null) {
                d.run = true;
                Element rv = XMLUtil.findElement(rd, "release-version", NBM_NS);
                if (rv != null) {
                    t = XMLUtil.findText(rv);
                    if (t == null) {
                        throw new BuildException("No text in <release-version>", getLocation());
                    }
                    d.release = t;
                }
                Element sv = XMLUtil.findElement(rd, "specification-version", NBM_NS);
                if (sv != null) {
                    t = XMLUtil.findText(sv);
                    if (t == null) {
                        throw new BuildException("No text in <specification-version>", getLocation());
                    }
                    d.spec = t;
                }
                Element iv = XMLUtil.findElement(rd, "implementation-version", NBM_NS);
                if (iv != null) {
                    d.impl = true;
                }
            }
            d.compile = XMLUtil.findElement(dep, "compile-dependency", NBM_NS) != null;
            deps.add(d);
        }
        return (Dep[])deps.toArray(new Dep[deps.size()]);
    }

    private String getCodeNameBase(Document d) throws BuildException {
        Element data = getConfig(d);
        Element name = XMLUtil.findElement(data, "code-name-base", NBM_NS);
        if (name == null) {
            throw new BuildException("No <code-name-base>", getLocation());
        }
        String t = XMLUtil.findText(name);
        if (t == null) {
            throw new BuildException("No text in <code-name-base>", getLocation());
        }
        return t;
    }

    private int getModuleType(Document d) throws BuildException {
        Element data = getConfig(d);
        if (XMLUtil.findElement(data, "suite-component", NBM_NS) != null) {
            return TYPE_SUITE;
        } else if (XMLUtil.findElement(data, "standalone", NBM_NS) != null) {
            return TYPE_STANDALONE;
        } else {
            return TYPE_NB_ORG;
        }
    }

    private String computeClasspath(Document pDoc, ModuleListParser modules, Dep[] deps, boolean runtime) throws BuildException, IOException, SAXException {
        String myCnb = getCodeNameBase(pDoc);
        StringBuffer cp = new StringBuffer();
        String excludedClustersProp = getProject().getProperty("disabled.clusters");
        Set/*<String>*/ excludedClusters = excludedClustersProp != null ?
            new HashSet(Arrays.asList(excludedClustersProp.split(" *, *"))) :
            null;
        String excludedModulesProp = getProject().getProperty("disabled.modules");
        Set/*<String>*/ excludedModules = excludedModulesProp != null ?
            new HashSet(Arrays.asList(excludedModulesProp.split(" *, *"))) :
            null;
        for (int i = 0; i < deps.length; i++) { // XXX should operative transitively if runtime
            Dep dep = deps[i];
            if (!dep.compile) { // XXX should be sensitive to runtime
                continue;
            }
            String cnb = dep.codenamebase;
            File depJar = computeClasspathModuleLocation(modules, cnb, excludedClusters, excludedModules);
            
            Attributes attr;
            if (!depJar.isFile()) {
                throw new BuildException("No such classpath entry: " + depJar, getLocation());
            }
            JarFile jarFile = new JarFile(depJar, false);
            try {
                attr = jarFile.getManifest().getMainAttributes();
            } finally {
                jarFile.close();
            }
            
            if (!dep.matches(attr)) { // #68631
                throw new BuildException("Cannot compile against a module: " + depJar + " because of dependency: " + dep, getLocation());
            }

            List/*<File>*/ additions = new ArrayList();
            additions.add(depJar);
            // #52354: look for <class-path-extension>s in dependent modules.
            ModuleListParser.Entry entry = modules.findByCodeNameBase(cnb);
            if (entry != null) {
                File[] exts = entry.getClassPathExtensions();
                for (int j = 0; j < exts.length; j++) {
                    additions.add(exts[j]);
                }
            }
            
            if (!dep.impl) {
                String friends = attr.getValue("OpenIDE-Module-Friends");
                if (friends != null && !Arrays.asList(friends.split(" *, *")).contains(myCnb)) {
                    throw new BuildException("The module " + myCnb + " is not a friend of " + depJar, getLocation());
                }
                String pubpkgs = attr.getValue("OpenIDE-Module-Public-Packages");
                if ("-".equals(pubpkgs)) {
                    throw new BuildException("The module " + depJar + " has no public packages and so cannot be compiled against", getLocation());
                } else if (pubpkgs != null && !runtime && /* #71807 */ dep.run && publicPackageJarDir != null) {
                    File splitJar = createPublicPackageJar(additions, pubpkgs, publicPackageJarDir, cnb);
                    additions.clear();
                    additions.add(splitJar);
                }
            }
            
            Iterator it = additions.iterator();
            while (it.hasNext()) {
                if (cp.length() > 0) {
                    cp.append(':');
                }
                cp.append(((File) it.next()).getAbsolutePath());
            }
        }
        // Also look for <class-path-extension>s for myself and put them in my own classpath.
        ModuleListParser.Entry entry = modules.findByCodeNameBase(myCnb);
        assert entry != null;
        File[] exts = entry.getClassPathExtensions();
        for (int i = 0; i < exts.length; i++) {
            cp.append(':');
            cp.append(exts[i].getAbsolutePath());
        }
        return cp.toString();
    }
    
    private File computeClasspathModuleLocation(ModuleListParser modules, String cnb, Set/*<String>*/ excludedClusters, Set/*<String>*/ excludedModules) throws BuildException {
        ModuleListParser.Entry module = modules.findByCodeNameBase(cnb);
        if (module == null) {
            throw new BuildException("No dependent module " + cnb, getLocation());
        }
        if (excludedClusters != null && excludedClusters.contains(module.getClusterName())) { // #68716
            throw new BuildException("Module " + cnb + " part of cluster " + module.getClusterName() + " which is excluded from the target platform", getLocation());
        }
        if (excludedModules != null && excludedModules.contains(cnb)) { // again #68716
            throw new BuildException("Module " + cnb + " excluded from the target platform", getLocation());
        }
        return module.getJar();
    }
    
    private String computeClassPathExtensions(Document pDoc) {
        Element data = getConfig(pDoc);
        StringBuffer list = null;
        List/*<Element>*/ exts = XMLUtil.findSubElements(data);
        Iterator it = exts.iterator();
        while (it.hasNext()) {
            Element ext = (Element) it.next();
            if (!ext.getLocalName().equals("class-path-extension")) {
                continue;
            }
            Element runtimeRelativePath = XMLUtil.findElement(ext, "runtime-relative-path", NBM_NS);
            if (runtimeRelativePath == null) {
                throw new BuildException("Have malformed <class-path-extension> in " + getProjectFile(), getLocation());
            }
            String reltext = XMLUtil.findText(runtimeRelativePath);
            if (list == null) {
                list = new StringBuffer();
            } else {
                list.append(' ');
            }
            list.append(reltext);
        }
        return list != null ? list.toString() : null;
    }

    /**
     * Create a compact JAR containing only classes in public packages.
     * Forces the compiler to honor public package restrictions.
     * @see "#59792"
     */
    private File createPublicPackageJar(List/*<File>*/ jars, String pubpkgs, File dir, String cnb) throws IOException {
        if (!dir.isDirectory()) {
            throw new IOException("No such directory " + dir);
        }
        File ppjar = new File(dir, cnb.replace('.', '-') + ".jar");
        if (ppjar.exists()) {
            // Check if it is up to date first. Must be as new as any input JAR.
            boolean uptodate = true;
            long stamp = ppjar.lastModified();
            Iterator it = jars.iterator();
            while (it.hasNext()) {
                File jar = (File) it.next();
                if (jar.lastModified() > stamp) {
                    uptodate = false;
                    break;
                }
            }
            if (uptodate) {
                log("Distilled " + ppjar + " was already up to date", Project.MSG_VERBOSE);
                return ppjar;
            }
        }
        log("Distilling " + ppjar + " from " + jars);
        String corePattern = pubpkgs.
                replaceAll(" +", "").
                replaceAll("\\.", "/").
                replaceAll(",", "|").
                replaceAll("\\*\\*", "(.+/)?").
                replaceAll("\\*", "");
        Pattern p = Pattern.compile("(" + corePattern + ")[^/]+\\.class");
        // E.g.: (org/netbeans/api/foo/|org/netbeans/spi/foo/)[^/]+\.class
        OutputStream os = new FileOutputStream(ppjar);
        try {
            ZipOutputStream zos = new ZipOutputStream(os);
            Set/*<String>*/ addedPaths = new HashSet();
            Iterator it = jars.iterator();
            while (it.hasNext()) {
                File jar = (File) it.next();
                if (!jar.isFile()) {
                    log("Classpath entry " + jar + " does not exist; skipping", Project.MSG_WARN);
                }
                InputStream is = new FileInputStream(jar);
                try {
                    ZipInputStream zis = new ZipInputStream(is);
                    ZipEntry inEntry;
                    while ((inEntry = zis.getNextEntry()) != null) {
                        String path = inEntry.getName();
                        if (!addedPaths.add(path)) {
                            continue;
                        }
                        if (!p.matcher(path).matches()) {
                            continue;
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buf = new byte[4096];
                        int read;
                        while ((read = zis.read(buf)) != -1) {
                            baos.write(buf, 0, read);
                        }
                        byte[] data = baos.toByteArray();
                        ZipEntry outEntry = new ZipEntry(path);
                        outEntry.setSize(data.length);
                        CRC32 crc = new CRC32();
                        crc.update(data);
                        outEntry.setCrc(crc.getValue());
                        zos.putNextEntry(outEntry);
                        zos.write(data);
                    }
                } finally {
                    is.close();
                }
            }
            zos.close();
        } finally {
            os.close();
        }
        return ppjar;
    }

}
