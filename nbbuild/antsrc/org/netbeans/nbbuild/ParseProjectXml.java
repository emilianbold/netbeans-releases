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
import java.io.IOException;
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
import org.xml.sax.SAXException;

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
            if (codeNameBaseSlashesProperty != null) {
                String cnb = getCodeNameBase(pDoc);
                define(codeNameBaseSlashesProperty, cnb.replace('.', '/'));
            }
            if (moduleClassPathProperty != null) {
                if (modulesXml == null) {
                    throw new BuildException("You must set 'modulesxml'", getLocation());
                }
                String cp = computeClasspath(pDoc, modulesXml);
                if (cp != null) {
                    define(moduleClassPathProperty, cp);
                }
            }
            if (domainProperty != null) {
                String path = getPath(pDoc);
                int index = path.lastIndexOf('/');
                String domain;
                if (index == -1) {
                    domain = path;
                } else {
                    domain = path.substring(0, index);
                }
                define(domainProperty, domain);
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
        public String release = null;
        public String spec = null;
        // XXX handle impl deps too
        public String toString() {
            StringBuffer b = new StringBuffer(codenamebase);
            if (release != null) {
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
                deps.add(d);
            }
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

    private String getPath(Document d) throws BuildException {
        Element data = getConfig(d);
        Element path = XMLUtil.findElement(data, "path", NBM_NS);
        if (path == null) {
            throw new BuildException("No <path>", getLocation());
        }
        String t = XMLUtil.findText(path);
        if (t == null) {
            throw new BuildException("No text in <path>", getLocation());
        }
        return t;
    }

    private String computeClasspath(Document pDoc, File modulesXml) throws BuildException, IOException, SAXException {
        ModuleListParser modules = new ModuleListParser(modulesXml);
        Element data = getConfig(pDoc);
        Element moduleDependencies = XMLUtil.findElement(data, "module-dependencies", NBM_NS);
        List/*<Element>*/ deps = XMLUtil.findSubElements(moduleDependencies);
        StringBuffer cp = new StringBuffer();
        Iterator it = deps.iterator();
        while (it.hasNext()) {
            Element dep = (Element)it.next();
            if (XMLUtil.findElement(dep, "compile-dependency", NBM_NS) == null) {
                continue;
            }
            if (cp.length() > 0) {
                cp.append(':');
            }
            Element cnbEl = XMLUtil.findElement(dep, "code-name-base", NBM_NS);
            String cnb = XMLUtil.findText(cnbEl);
            ModuleListParser.Entry module = modules.findByCodeNameBase(cnb);
            if (module == null) {
                throw new BuildException("No dependent module " + cnb, getLocation());
            }
            // XXX if that module is projectized, check its public
            // packages; if it has none, halt the build, unless we are
            // declaring an impl dependency
            // Prototype: ${java/srcmodel.dir}/${nb.modules/autoload.dir}/java-src-model.jar
            // where: path=java/srcmodel jar = modules/autoload/java-src-model.jar
            String topdirProp = module.getPath() + ".dir"; // "java/srcmodel.dir"
            String topdirVal = getProject().getProperty(topdirProp);
            if (topdirVal == null) {
                throw new BuildException("Undefined: " + topdirProp + " (usually means you are missing a dependency in nbbuild/build.xml#all-*)", getLocation());
            }
            cp.append(topdirVal);
            cp.append('/');
            String jar = module.getJar();
            int slash = jar.lastIndexOf('/');
            String jarDir = jar.substring(0, slash); // "modules/autoload"
            String jarDirProp = "nb." + jarDir + ".dir"; // "nb.modules/autoload.dir"
            String jarDirVal = getProject().getProperty(jarDirProp);
            if (jarDirVal == null) {
                throw new BuildException("Undefined: " + jarDirProp);
            }
            cp.append(jarDirVal);
            String slashPlusJar = jar.substring(slash); // "/java-src-model.jar"
            cp.append(slashPlusJar);
        }
        return cp.toString();
    }

}
