/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Parse a projectized module's <code>nbproject/project.xml</code> and
 * define various useful Ant properties based on the result.
 * @author Jesse Glick
 */
public final class ParseProjectXml extends Task {

    private static final String PROJECT_NS = "http://www.netbeans.org/ns/project/1";
    private static final String NBM_NS = "http://www.netbeans.org/ns/nb-module-project/1";

    private File project;
    /**
     * Set the NetBeans module project to work on.
     */
    public void setProject(File f) {
        project = f;
    }

    private File modulesXml;
    /**
     * Set the location of <code>nbbuild/templates/modules.xml</code>.
     */
    public void setModulesXml(File f) {
        modulesXml = f;
    }

    private boolean autoload;
    /**
     * Set whether this is an autoload module.
     */
    public void setAutoload(boolean b) {
        autoload = b;
    }

    private boolean eager;
    /**
     * Set whether this is an eager module.
     */
    public void setEager(boolean b) {
        eager = b;
    }

    private String publicPackagesProperty;
    /**
     * Set the property to set a list of
     * OpenIDE-Module-Public-Packages to.
     */
    public void setPublicPackagesProperty(String s) {
        publicPackagesProperty = s;
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

    private String moduleJarDirProperty;
    /**
     * Set the property to set the module JAR directory to, based on
     * whether this is an autoload or eager or regular module.
     */
    public void setModuleJarDirProperty(String s) {
        moduleJarDirProperty = s;
    }

    private String moduleClassPathProperty;
    /**
     * Set the property to set the computed module class path to,
     * based on the list of stated compile-time dependencies.
     */
    public void setModuleClassPathProperty(String s) {
        moduleClassPathProperty = s;
    }

    private void define(String prop, String val) {
        log("Setting " + prop + "=" + val, Project.MSG_VERBOSE);
        String old = getProject().getProperty(prop);
        if (old != null) {
            getProject().log("Warning: " + prop + " was already set to " + old, Project.MSG_WARN);
        }
        getProject().setNewProperty(prop, val);
    }

    public void execute() throws BuildException {
        try {
            if (project == null) {
                throw new BuildException("You must set 'project'", getLocation());
            }
            Document pDoc = XMLUtil.parse(new InputSource(new File(new File(project, "nbproject"), "project.xml").toURI().toString()),
                                          false, true, /*XXX*/null, null);
            if (publicPackagesProperty != null || javadocPackagesProperty != null) {
                String[] pkgs = getPublicPackages(pDoc);
                if (publicPackagesProperty != null) {
                    String val;
                    if (pkgs.length > 0) {
                        StringBuffer b = new StringBuffer(pkgs[0]);
                        b.append(".*");
                        for (int i = 1; i < pkgs.length; i++) {
                            b.append(", ");
                            b.append(pkgs[i]);
                            b.append(".*");
                        }
                        val = b.toString();
                    } else {
                        val = "-";
                    }
                    define(publicPackagesProperty, val);
                }
                if (javadocPackagesProperty != null) {
                    if (pkgs.length > 0) {
                        StringBuffer b = new StringBuffer(pkgs[0]);
                        for (int i = 1; i < pkgs.length; i++) {
                            b.append(", ");
                            b.append(pkgs[i]);
                        }
                        define(javadocPackagesProperty, b.toString());
                    }
                }
            }
            if (ideDependenciesProperty != null || moduleDependenciesProperty != null) {
                Dep[] deps = getDeps(pDoc);
                if (ideDependenciesProperty != null) {
                    Dep ide = null;
                    for (int i = 0; i < deps.length; i++) {
                        if (deps[i].codenamebase.equals("IDE")) {
                            ide = deps[i];
                            break;
                        }
                    }
                    if (ide != null) {
                        define(ideDependenciesProperty, ide.toString());
                    }
                }
                if (moduleDependenciesProperty != null) {
                    StringBuffer b = new StringBuffer();
                    for (int i = 0; i < deps.length; i++) {
                        if (deps[i].codenamebase.equals("IDE")) {
                            continue;
                        }
                        if (b.length() > 0) {
                            b.append(", ");
                        }
                        b.append(deps[i].toString());
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
                if (moduleJarDirProperty != null) {
                    if (eager && autoload) {
                        throw new BuildException("Cannot be both eager and autoload at once", getLocation());
                    }
                    String orig;
                    if (eager) {
                        orig = "nb.modules/eager.dir";
                    } else if (autoload) {
                        orig = "nb.modules/autoload.dir";
                    } else {
                        orig = "nb.modules.dir";
                    }
                    String val = getProject().getProperty(orig);
                    if (val == null) {
                        throw new BuildException("No value for " + orig, getLocation());
                    }
                    define(moduleJarDirProperty, val);
                }
                if (moduleClassPathProperty != null) {
                    if (modulesXml == null) {
                        throw new BuildException("You must set 'modulesxml'", getLocation());
                    }
                    Document mDoc = XMLUtil.parse(new InputSource(modulesXml.toURI().toString()),
                                                  false, true, /*XXX*/null, null);
                    String cp = computeClasspath(pDoc, mDoc);
                    if (cp != null) {
                        define(moduleClassPathProperty, cp);
                    }
                }
            
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
            throw new BuildException("No <data>", getLocation());
        }
        return d;
    }

    private String[] getPublicPackages(Document d) throws BuildException {
        Element cfg = getConfig(d);
        Element pp = XMLUtil.findElement(cfg, "public-packages", NBM_NS);
        if (pp == null) {
            throw new BuildException("No <public-packages>", getLocation());
        }
        List/*<Element>*/ l = XMLUtil.findSubElements(pp);
        String[] pkgs = new String[l.size()];
        Iterator it = l.iterator();
        int i = 0;
        while (it.hasNext()) {
            Element p = (Element)it.next();
            String t = XMLUtil.findText(p);
            if (t == null) {
                throw new BuildException("No text in <package>", getLocation());
            }
            pkgs[i++] = t;
        }
        return pkgs;
    }

    private static final class Dep {
        /** will be e.g. org.netbeans.modules.form or IDE */
        public String codenamebase;
        public int release = -1;
        public String spec = null;
        // XXX handle impl deps too
        public String toString() {
            StringBuffer b = new StringBuffer(codenamebase);
            if (release != -1) {
                b.append('/');
                b.append(release);
            }
            if (spec != null) {
                b.append(" > ");
                b.append(spec);
            }
            return b.toString();
        }
    }

    private Dep[] getDeps(Document pDoc) throws BuildException {
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
            Dep d = new Dep();
            Element cnb = XMLUtil.findElement(dep, "code-name-base", NBM_NS);
            if (cnb == null) {
                throw new BuildException("No <code-name-base>", getLocation());
            }
            String t = XMLUtil.findText(cnb);
            if (t == null) {
                throw new BuildException("No text in <code-name-base>", getLocation());
            }
            if (t.equals("org.openide")) {
                t = "IDE";
            }
            d.codenamebase = t;
            Element rd = XMLUtil.findElement(dep, "run-dependency", NBM_NS);
            if (rd != null) {
                Element rv = XMLUtil.findElement(rd, "release-version", NBM_NS);
                if (rv != null) {
                    t = XMLUtil.findText(rv);
                    if (t == null) {
                        throw new BuildException("No text in <release-version>", getLocation());
                    }
                    d.release = Integer.parseInt(t);
                }
                Element sv = XMLUtil.findElement(rd, "specification-version", NBM_NS);
                if (sv != null) {
                    t = XMLUtil.findText(sv);
                    if (t == null) {
                        throw new BuildException("No text in <specification-version>", getLocation());
                    }
                    d.spec = t;
                }
                deps.add(d);
            }
        }
        return (Dep[])deps.toArray(new Dep[deps.size()]);
    }

    private String getCodeNameBase(Document d) throws BuildException {
        Element root = d.getDocumentElement();
        Element name = XMLUtil.findElement(root, "name", PROJECT_NS);
        if (name == null) {
            throw new BuildException("No <name>", getLocation());
        }
        String t = XMLUtil.findText(name);
        if (t == null) {
            throw new BuildException("No text in <name>", getLocation());
        }
        return t;
    }

    private static final class ModuleEntry {
        public String path;
        public String cnb;
        public String jar;
    }

    private String computeClasspath(Document pDoc, Document mDoc) throws BuildException {
        // all module entries, indexed by cnb
        Map/*<String,Entry>*/ entries = new HashMap();
        List/*<Element>*/ l = XMLUtil.findSubElements(mDoc.getDocumentElement());
        Iterator it = l.iterator();
        while (it.hasNext()) {
            Element el = (Element)it.next();
            ModuleEntry me = new ModuleEntry();
            Element pathEl = XMLUtil.findElement(el, "path", null);
            me.path = XMLUtil.findText(pathEl);
            Element cnbEl = XMLUtil.findElement(el, "cnb", null);
            me.cnb = XMLUtil.findText(cnbEl);
            Element jarEl = XMLUtil.findElement(el, "jar", null);
            me.jar = XMLUtil.findText(jarEl);
            entries.put(me.cnb, me);
        }
        Element data = getConfig(pDoc);
        Element moduleDependencies = XMLUtil.findElement(data, "module-dependencies", NBM_NS);
        List/*<Element>*/ deps = XMLUtil.findSubElements(moduleDependencies);
        StringBuffer cp = new StringBuffer();
        it = deps.iterator();
        while (it.hasNext()) {
            if (cp.length() > 0) {
                cp.append(':');
            }
            Element dep = (Element)it.next();
            if (XMLUtil.findElement(dep, "compile-dependency", NBM_NS) == null) {
                continue;
            }
            Element cnbEl = XMLUtil.findElement(dep, "code-name-base", NBM_NS);
            String cnb = XMLUtil.findText(cnbEl);
            ModuleEntry module = (ModuleEntry)entries.get(cnb);
            if (module == null) {
                throw new BuildException("No dependent module " + cnb, getLocation());
            }
            // XXX if that module is projectized, check its public
            // packages; if it has none, halt the build, unless we are
            // declaring an impl dependency
            // Prototype: ${java/srcmodel.dir}/${nb.modules/autoload.dir}/java-src-model.jar
            // where: path=java/srcmodel jar = modules/autoload/java-src-model.jar
            String topdirProp = module.path + ".dir"; // "java/srcmodel.dir"
            String topdirVal = getProject().getProperty(topdirProp);
            if (topdirVal == null) {
                // Can happen while running clean: the dependent modules might not be here yet.
                log("Undefined: " + topdirProp, Project.MSG_VERBOSE);
                return null;
            }
            cp.append(topdirVal);
            cp.append('/');
            int slash = module.jar.lastIndexOf('/');
            String jarDir = module.jar.substring(0, slash); // "modules/autoload"
            String jarDirProp = "nb." + jarDir + ".dir"; // "nb.modules/autoload.dir"
            String jarDirVal = getProject().getProperty(jarDirProp);
            if (jarDirVal == null) {
                throw new BuildException("Undefined: " + jarDirProp);
            }
            cp.append(jarDirVal);
            String slashPlusJar = module.jar.substring(slash); // "/java-src-model.jar"
            cp.append(slashPlusJar);
        }
        return cp.toString();
    }

}
