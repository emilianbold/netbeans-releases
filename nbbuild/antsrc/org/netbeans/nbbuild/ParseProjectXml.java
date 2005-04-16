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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
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

    private static final String PROJECT_NS = "http://www.netbeans.org/ns/project/1"; //NOI18N
    private static final String[] NBM_NS_1_AND_2 = {
        "http://www.netbeans.org/ns/nb-module-project/1",
        "http://www.netbeans.org/ns/nb-module-project/2",
    };

    private static final String CLASS_PATH_EXTENSIONS_FILE = "build" + File.separatorChar + "class-path-extensions.txt";

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
        return new File(new File(project, "nbproject"), "project.xml"); //NOI18N
    }

    private File modulesXml;
    /**
     * Set the location of <code>nbbuild/templates/modules.xml</code>.
     */
    public void setModulesXml(File f) {
        modulesXml = f;
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

    private String ideDependenciesProperty;
    /**
     * Set the property to set a list of
     * OpenIDE-Module-IDE-Dependencies to, based on the list of stated
     * run-time dependencies.
     */
    public void setIdeDependenciesProperty(String s) {
        ideDependenciesProperty = s;
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
                            if (name.indexOf (',') >= 0) { //NOI18N
                                throw new BuildException ("Package name cannot contain ',' as " + pkgs[i], getLocation ());
                            }
                            if (name.indexOf ('*') >= 0) { //NOI18N
                                throw new BuildException ("Package name cannot contain '*' as " + pkgs[i], getLocation ());
                            }
                            
                            b.append(name);
                            if (pkgs[i].subpackages) {
                                b.append (".**"); //NOI18N
                            } else {
                                b.append(".*"); //NOI18N
                            }
                            sep = ", "; //NOI18N
                        }
                        val = b.toString();
                    } else {
                        val = "-"; //NOI18N
                    }
                    define(publicPackagesProperty, val);
                }
                NO_JAVA_DOC_PROPERTY_SET: if (javadocPackagesProperty != null) {
                    if (pkgs.length > 0) {
                        String sep = ", "; //NOI18N
                        StringBuffer b = new StringBuffer();
                        for (int i = 0; i < pkgs.length; i++) {
                            b.append(sep);
                            if (pkgs[i].subpackages) {
                                String msg = javadocPackagesProperty + " cannot be set as <subpackages> does not work for Javadoc (see <subpackages>" + pkgs[i].name + "</subpackages> tag in " + getProjectFile () + "). Set the property in project.properties if you want to build Javadoc.";
                                // #52135: do not halt the build, just leave it.
                                getProject().log("Warning: " + msg, Project.MSG_WARN);
                                break NO_JAVA_DOC_PROPERTY_SET;
                            }
                            b.append(pkgs[i].name);
                            sep = ", "; //NOI18N
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
            if (moduleDependenciesProperty != null || moduleClassPathProperty != null) {
                modules = new ModuleListParser(modulesXml);
            }
            if (ideDependenciesProperty != null || moduleDependenciesProperty != null) {
                Dep[] deps = getDeps(pDoc);
                if (ideDependenciesProperty != null) {
                    Dep ide = null;
                    for (int i = 0; i < deps.length; i++) {
                        if (deps[i].codenamebase.equals("IDE")) { //NOI18N
                            ide = deps[i];
                            break;
                        }
                    }
                    if (ide != null) {
                        define(ideDependenciesProperty, ide.toString(modules));
                    }
                }
                if (moduleDependenciesProperty != null) {
                    StringBuffer b = new StringBuffer();
                    for (int i = 0; i < deps.length; i++) {
                        if (deps[i].codenamebase.equals("IDE")) { //NOI18N
                            continue;
                        }
                        if (b.length() > 0) {
                            b.append(", "); //NOI18N
                        }
                        b.append(deps[i].toString(modules));
                    }
                    if (b.length() > 0) {
                        define(moduleDependenciesProperty, b.toString());
                    }
                }
            }
            if (codeNameBaseDashesProperty != null) {
                String cnb = getCodeNameBase(pDoc);
                define(codeNameBaseDashesProperty, cnb.replace('.', '-')); //NOI18N
            }
            if (codeNameBaseSlashesProperty != null) {
                String cnb = getCodeNameBase(pDoc);
                define(codeNameBaseSlashesProperty, cnb.replace('.', '/')); //NOI18N
            }
            if (moduleClassPathProperty != null) {
                if (modulesXml == null) {
                    throw new BuildException("You must set 'modulesxml'", getLocation());
                }
                String cp = computeClasspath(pDoc, modules);
                if (cp != null) {
                    define(moduleClassPathProperty, cp);
                }
            }
            if (domainProperty != null) {
                String path = getPath(pDoc);
                int index = path.indexOf('/'); //NOI18N
                String domain;
                if (index == -1) {
                    domain = path;
                } else {
                    domain = path.substring(0, index);
                }
                define(domainProperty, domain);
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
        Element c = XMLUtil.findElement(e, "configuration", PROJECT_NS); //NOI18N
        if (c == null) {
            throw new BuildException("No <configuration>", getLocation());
        }
        Element d = XMLUtil.findElement(c, "data", NBM_NS_1_AND_2); //NOI18N
        if (d == null) {
            throw new BuildException("No <data>", getLocation());
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
        Element pp = XMLUtil.findElement(cfg, "public-packages", NBM_NS_1_AND_2); //NOI18N
        if (pp == null) {
            pp = XMLUtil.findElement(cfg, "friend-packages", NBM_NS_1_AND_2[1]); // NOI18N
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
            if ("friend".equals(p.getNodeName())) { // NOI18N
                continue;
            }
            if (!"package".equals (p.getNodeName ())) { //NOI18N
                if (!("subpackages".equals (p.getNodeName ()))) { //NOI18N
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
        Element pp = XMLUtil.findElement(cfg, "friend-packages", NBM_NS_1_AND_2[1]); // NOI18N
        if (pp == null) {
            return null;
        }
        List/*<String>*/ friends = new ArrayList();
        List/*<Element>*/ l = XMLUtil.findSubElements(pp);
        Iterator it = l.iterator();
        boolean other = false;
        while (it.hasNext()) {
            Element p = (Element) it.next();
            if ("friend".equals(p.getNodeName())) { // NOI18N
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
        /** will be e.g. org.netbeans.modules.form or IDE */
        public String codenamebase;
        public String release = null;
        public String spec = null;
        public boolean impl = false;
        public String toString(ModuleListParser modules) throws IOException, BuildException {
            StringBuffer b = new StringBuffer(codenamebase);
            if (release != null) {
                b.append('/'); //NOI18N
                b.append(release);
            }
            if (spec != null) {
                b.append(" > "); //NOI18N
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
        private String implementationVersionOf(ModuleListParser modules, String cnb) throws IOException {
            File jar = new File(computeClasspathModuleLocation(modules, cnb));
            if (!jar.isFile()) {
                throw new BuildException("No such classpath entry: " + jar, getLocation());
            }
            JarFile jarFile = new JarFile(jar, false);
            try {
                return jarFile.getManifest().getMainAttributes().getValue("OpenIDE-Module-Implementation-Version");
            } finally {
                jarFile.close();
            }
        }
    }

    private Dep[] getDeps(Document pDoc) throws BuildException {
        Element cfg = getConfig(pDoc);
        Element md = XMLUtil.findElement(cfg, "module-dependencies", NBM_NS_1_AND_2); //NOI18N
        if (md == null) {
            throw new BuildException("No <module-dependencies>", getLocation());
        }
        List/*<Element>*/ l = XMLUtil.findSubElements(md);
        List/*<Dep>*/ deps = new ArrayList();
        Iterator it = l.iterator();
        while (it.hasNext()) {
            Element dep = (Element)it.next();
            Dep d = new Dep();
            Element cnb = XMLUtil.findElement(dep, "code-name-base", NBM_NS_1_AND_2); //NOI18N
            if (cnb == null) {
                throw new BuildException("No <code-name-base>", getLocation());
            }
            String t = XMLUtil.findText(cnb);
            if (t == null) {
                throw new BuildException("No text in <code-name-base>", getLocation());
            }
            if (t.equals("org.openide")) { //NOI18N
                t = "IDE"; //NOI18N
            }
            d.codenamebase = t;
            Element rd = XMLUtil.findElement(dep, "run-dependency", NBM_NS_1_AND_2); //NOI18N
            if (rd != null) {
                Element rv = XMLUtil.findElement(rd, "release-version", NBM_NS_1_AND_2);
                if (rv != null) {
                    t = XMLUtil.findText(rv);
                    if (t == null) {
                        throw new BuildException("No text in <release-version>", getLocation());
                    }
                    d.release = t;
                }
                Element sv = XMLUtil.findElement(rd, "specification-version", NBM_NS_1_AND_2); //NOI18N
                if (sv != null) {
                    t = XMLUtil.findText(sv);
                    if (t == null) {
                        throw new BuildException("No text in <specification-version>", getLocation());
                    }
                    d.spec = t;
                }
                // <implementation-version> added in /2:
                Element iv = XMLUtil.findElement(rd, "implementation-version", NBM_NS_1_AND_2[1]); //NOI18N
                if (iv != null) {
                    d.impl = true;
                }
                deps.add(d);
            }
        }
        return (Dep[])deps.toArray(new Dep[deps.size()]);
    }

    private String getCodeNameBase(Document d) throws BuildException {
        Element data = getConfig(d);
        Element name = XMLUtil.findElement(data, "code-name-base", NBM_NS_1_AND_2); //NOI18N
        if (name == null) {
            throw new BuildException("No <code-name-base>", getLocation());
        }
        String t = XMLUtil.findText(name);
        if (t == null) {
            throw new BuildException("No text in <code-name-base>", getLocation());
        }
        return t;
    }

    private String getPath(Document d) throws BuildException {
        Element data = getConfig(d);
        Element path = XMLUtil.findElement(data, "path", NBM_NS_1_AND_2); //NOI18N
        if (path == null) {
            throw new BuildException("No <path>", getLocation());
        }
        String t = XMLUtil.findText(path);
        if (t == null) {
            throw new BuildException("No text in <path>", getLocation());
        }
        return t;
    }

    private String computeClasspath(Document pDoc, ModuleListParser modules) throws BuildException, IOException, SAXException {
        Element data = getConfig(pDoc);
        Element moduleDependencies = XMLUtil.findElement(data, "module-dependencies", NBM_NS_1_AND_2); //NOI18N
        List/*<Element>*/ deps = XMLUtil.findSubElements(moduleDependencies);
        StringBuffer cp = new StringBuffer();
        Iterator it = deps.iterator();
        while (it.hasNext()) {
            Element dep = (Element)it.next();
            if (XMLUtil.findElement(dep, "compile-dependency", NBM_NS_1_AND_2) == null) { //NOI18N
                continue;
            }
            if (cp.length() > 0) {
                cp.append(':');
            }
            Element cnbEl = XMLUtil.findElement(dep, "code-name-base", NBM_NS_1_AND_2); //NOI18N
            String cnb = XMLUtil.findText(cnbEl);
            cp.append(computeClasspathModuleLocation(modules, cnb));
            // #52354: look for <class-path-extension>s in dependent modules.
            ModuleListParser.Entry entry = modules.findByCodeNameBase(cnb);
            if (entry != null) {
                File subproject = new File(modulesXml.getParentFile().getParentFile().getParentFile(),
                                           entry.getPath().replace('/', File.separatorChar));
                File record = new File(subproject, CLASS_PATH_EXTENSIONS_FILE);
                if (record.isFile()) {
                    InputStream is = new FileInputStream(record);
                    try {
                        byte[] buf = new byte[1024];
                        int len;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        while ((len = is.read(buf)) != -1) {
                            baos.write(buf, 0, len);
                        }
                        cp.append(baos.toString("UTF-8"));
                    } finally {
                        is.close();
                    }
                }
            }
        }
        // Also look for <class-path-extension>s for myself and put them in my own classpath.
        // Also record the classpath addition in a place where it can be quickly and easily read
        // by modules which depend on me (see above).
        StringBuffer cpextra = null;
        List/*<Element>*/ exts = XMLUtil.findSubElements(data);
        it = exts.iterator();
        while (it.hasNext()) {
            Element ext = (Element) it.next();
            if (!ext.getLocalName().equals("class-path-extension")) {
                continue;
            }
            Element binaryOrigin = XMLUtil.findElement(ext, "binary-origin", NBM_NS_1_AND_2[1]);
            String text;
            if (binaryOrigin != null) {
                text = XMLUtil.findText(binaryOrigin);
            } else {
                Element runtimeRelativePath = XMLUtil.findElement(ext, "runtime-relative-path", NBM_NS_1_AND_2[1]);
                if (runtimeRelativePath == null) {
                    throw new BuildException("Have malformed <class-path-extension> in " + getProjectFile(), getLocation());
                }
                String reltext = XMLUtil.findText(runtimeRelativePath);
                // XXX assumes that module.jar.dir=${nb.modules.dir} was not overridden!
                text = "${netbeans.dest.dir}/${cluster.dir}/${nb.modules.dir}/" + reltext;
            }
            String eval = getProject().replaceProperties(text);
            File binary = getProject().resolveFile(eval);
            if (cpextra == null) {
                cpextra = new StringBuffer();
            }
            cpextra.append(':');
            cpextra.append(binary.getAbsolutePath());
        }
        if (cpextra != null) {
            cp.append(cpextra);
            File record = new File(project, CLASS_PATH_EXTENSIONS_FILE);
            record.getParentFile().mkdirs();
            OutputStream os = new FileOutputStream(record);
            try {
                Writer w = new OutputStreamWriter(os, "UTF-8");
                w.write(cpextra.toString());
                w.flush();
            } finally {
                os.close();
            }
        }
        return cp.toString();
    }
    
    private String computeClasspathModuleLocation(ModuleListParser modules, String cnb) throws BuildException {
        ModuleListParser.Entry module = modules.findByCodeNameBase(cnb);
        if (module == null) {
            throw new BuildException("No dependent module " + cnb, getLocation());
        }
        // XXX if that module is projectized, check its public
        // packages; if it has none, halt the build, unless we are
        // declaring an impl dependency
        // Prototype: ${java/srcmodel.dir}/${nb.modules/autoload.dir}/java-src-model.jar
        // where: path=java/srcmodel jar = modules/autoload/java-src-model.jar
        String topdirProp = module.getPath() + ".dir"; // "java/srcmodel.dir" //NOI18N
        String topdirVal = getProject().getProperty(topdirProp);
        if (topdirVal == null) {
            throw new BuildException("Undefined: " + topdirProp + " (usually means you are missing a dependency in nbbuild/build.xml#all-*)", getLocation());
        }
        String jar = module.getJar();
        int slash = jar.lastIndexOf('/'); //NOI18N
        String jarDir = jar.substring(0, slash); // "modules/autoload"
        String jarDirProp = "nb." + jarDir + ".dir"; // "nb.modules/autoload.dir" //NOI18N
        String jarDirVal = getProject().getProperty(jarDirProp);
        if (jarDirVal == null) {
            throw new BuildException("Undefined: " + jarDirProp);
        }
        String slashPlusJar = jar.substring(slash); // "/java-src-model.jar"
        return topdirVal + '/' + jarDirVal + slashPlusJar;
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
            Element runtimeRelativePath = XMLUtil.findElement(ext, "runtime-relative-path", NBM_NS_1_AND_2[1]);
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

}
