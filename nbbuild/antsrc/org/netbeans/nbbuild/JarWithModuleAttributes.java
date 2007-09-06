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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;

/**
 * Task just like <jar> but predefines various module attributes.
 * Cf. projectized.xml#jar
 * @author Jesse Glick
 */
public class JarWithModuleAttributes extends Jar {

    public JarWithModuleAttributes() {}

    private static final Pattern COMMA_SPACE = Pattern.compile(", *");
    private static final Pattern IMPL_DEP = Pattern.compile(" *([a-zA-Z0-9_.]+)(/[0-9]+)? *= *(.+) *");

    public void setManifest(File manifestFile) throws BuildException {
        Manifest added = new Manifest();
        try {
            String pubPkgs = getProject().getProperty("public.packages");
            if (pubPkgs == null) {
                throw new BuildException("Must have defined 'public.packages'", getLocation());
            }
            added.addConfiguredAttribute(new Manifest.Attribute("OpenIDE-Module-Public-Packages", pubPkgs));
            String friends = getProject().getProperty("friends");
            if (friends != null) {
                added.addConfiguredAttribute(new Manifest.Attribute("OpenIDE-Module-Friends", friends));
            }
            // #52354: define Class-Path in the manifest automatically.
            String javahelpClassPathExtension = getProject().getProperty("javahelp.class.path.extension");
            String classPathExtensions = getProject().getProperty("class.path.extensions");
            String cp;
            if (javahelpClassPathExtension != null) {
                if (classPathExtensions != null) {
                    cp = classPathExtensions + " " + javahelpClassPathExtension;
                } else {
                    cp = javahelpClassPathExtension;
                }
            } else {
                if (classPathExtensions != null) {
                    cp = classPathExtensions;
                } else {
                    cp = null;
                }
            }
            if (cp != null) {
                added.addConfiguredAttribute(new Manifest.Attribute("Class-Path", cp));
            }
            String moduleDeps = getProject().getProperty("module.dependencies");
            if (moduleDeps != null) {
                added.addConfiguredAttribute(new Manifest.Attribute("OpenIDE-Module-Module-Dependencies", moduleDeps));
            }
            String javaDep = getProject().getProperty("javac.target");
            if (javaDep != null && javaDep.matches("[0-9]+(\\.[0-9]+)*")) {
                added.addConfiguredAttribute(new Manifest.Attribute("OpenIDE-Module-Java-Dependencies", "Java > " + javaDep));
            }
            // Check to see if OpenIDE-Module-Implementation-Version is already defined.
            String implVers;
            String ownCnb;
            Manifest staticManifest;
            InputStream is = new FileInputStream(manifestFile);
            try {
                staticManifest = new Manifest(new InputStreamReader(is, "UTF-8"));
                Manifest.Section mainSection = staticManifest.getMainSection();
                implVers = mainSection.getAttributeValue("OpenIDE-Module-Implementation-Version");
                String myself = mainSection.getAttributeValue("OpenIDE-Module");
                int slash = myself.indexOf('/');
                if (slash == -1) {
                    ownCnb = myself;
                } else {
                    ownCnb = myself.substring(0, slash);
                }
                String cnbs = getProject().getProperty("code.name.base.slashes");
                String cnbDots = (cnbs != null) ? cnbs.replace('/', '.') : null;
                if (!ownCnb.equals(cnbDots)) {
                    // #58248: make sure these stay in synch.
                    throw new BuildException("Mismatch in module code name base: manifest says " + ownCnb + " but project.xml says " + cnbDots, getLocation());
                }
            } finally {
                is.close();
            }
            String buildNumber = getProject().getProperty("buildnumber");
            if (buildNumber == null) {
                throw new BuildException("Must have defined 'buildnumber'", getLocation());
            }
            String attrToAdd = implVers != null ? "OpenIDE-Module-Build-Version" : "OpenIDE-Module-Implementation-Version";
            added.addConfiguredAttribute(new Manifest.Attribute(attrToAdd, buildNumber));
            // If spec.version.base is defined, use it, after tacking on any numeric impl deps (sorted by CNB of the dep for stability),
            // and also using the implementation version of this module as well if it is numeric.
            // This trick makes sure that if you have an impl dep on some module which changes its (numeric) impl version,
            // your spec version will also change correspondingly, so e.g. Auto Update will see a new version of your module too.
            String specVersBase = getProject().getProperty("spec.version.base");
            if (specVersBase != null) {
                boolean edited = false;
                if (implVers != null) {
                    try {
                        Integer implVersI = new Integer(implVers);
                        specVersBase += "." + implVersI;
                        edited = true;
                    } catch (NumberFormatException e) {
                        // OK, ignore it, not numeric.
                        getProject().log(manifestFile + ": warning: use of spec.version.base with non-integer OpenIDE-Module-Implementation-Version (see http://wiki.netbeans.org/wiki/view/DevFaqImplementationDependency)", Project.MSG_WARN);
                    }
                }
                SortedMap<String,Integer> additions = new TreeMap<String,Integer>();
                if (moduleDeps != null) {
                    for (String individualDep : COMMA_SPACE.split(moduleDeps)) {
                        Matcher m = IMPL_DEP.matcher(individualDep);
                        if (m.matches()) {
                            String cnb = m.group(1);
                            String version = m.group(3);
                            try {
                                if (version.length() > 1 && version.charAt(0) == '0') {
                                    // Could be interpreted as an integer, but not here - e.g. "050123" is a date.
                                    throw new NumberFormatException(version);
                                }
                                Integer versionI = new Integer(version);
                                additions.put(cnb, versionI);
                            } catch (NumberFormatException e) {
                                // OK, ignore this one, not numeric.
                                getProject().log("Warning: in " + ownCnb + ", use of spec.version.base with non-integer OpenIDE-Module-Implementation-Version from " + cnb + " (see http://wiki.netbeans.org/wiki/view/DevFaqImplementationDependency)", Project.MSG_WARN);
                            }
                        }
                    }
                }
                for (int version : additions.values()) {
                    specVersBase += "." + version;
                    edited = true;
                }
                if (!edited) {
                    getProject().log("Warning: in " + ownCnb + ", using spec.version.base for no reason; could just use OpenIDE-Module-Specification-Version statically in the manifest (see http://wiki.netbeans.org/wiki/view/DevFaqImplementationDependency)", Project.MSG_WARN);
                }
                if (staticManifest.getMainSection().getAttributeValue("OpenIDE-Module-Specification-Version") != null) {
                    getProject().log("Warning: in " + ownCnb + ", attempting to use spec.version.base while some OpenIDE-Module-Specification-Version is statically defined in manifest.mf; this cannot work (see http://wiki.netbeans.org/wiki/view/DevFaqImplementationDependency)", Project.MSG_WARN);
                } else {
                    added.addConfiguredAttribute(new Manifest.Attribute("OpenIDE-Module-Specification-Version", specVersBase));
                }
            } else if (moduleDeps != null && moduleDeps.indexOf('=') != -1) {
                getProject().log("Warning: in " + ownCnb + ", not using spec.version.base, yet declaring implementation dependencies; may lead to problems with Auto Update (see http://wiki.netbeans.org/wiki/view/DevFaqImplementationDependency)", Project.MSG_WARN);
            } else if (implVers != null) {
                try {
                    new Integer(implVers);
                } catch (NumberFormatException e) {
                    getProject().log(manifestFile + ": warning: use of non-integer OpenIDE-Module-Implementation-Version may be problematic for clients trying to use spec.version.base (see http://wiki.netbeans.org/wiki/view/DevFaqImplementationDependency)", Project.MSG_WARN);
                }
            }
            added.addConfiguredAttribute(new Manifest.Attribute("AutoUpdate-Show-In-Client", Boolean.toString( // #110572
                    !Project.toBoolean(getProject().getProperty("is.autoload")) && !Project.toBoolean(getProject().getProperty("is.eager")) &&
                    "modules".equals(getProject().getProperty("module.jar.dir")))));
            // Now ask the regular <jar> task to add all this stuff to the regular manifest.mf.
            added.merge(staticManifest);
            if (!"lib".equals (getProject().getProperty("module.jar.dir"))) {
                // modules in lib cannot request this token
                String key = "OpenIDE-Module-Requires";
                String token = "org.openide.modules.ModuleFormat1";
                String requires = staticManifest.getMainSection().getAttributeValue(key);
                String newRequires;
                if (requires != null) {
                    // #59671: have to modify it, not just use super.setManifest(manifestFile).
                    added.getMainSection().removeAttribute(key);
                    newRequires = requires + ", " + token;
                } else {
                    newRequires = token;
                }
                added.addConfiguredAttribute(new Manifest.Attribute(key, newRequires));
            }
            addConfiguredManifest(added);
        } catch (Exception e) {
            throw new BuildException(e, getLocation());
        }
    }

}
