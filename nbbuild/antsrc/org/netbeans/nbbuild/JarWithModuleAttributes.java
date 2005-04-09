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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
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
 * Would not be necessary if this were implemented: http://issues.apache.org/bugzilla/show_bug.cgi?id=34366
 * Cf. projectized.xml#jar
 * @author Jesse Glick
 */
public class JarWithModuleAttributes extends Jar {
    
    public JarWithModuleAttributes() {}
    
    private static final Pattern COMMA_SPACE = Pattern.compile(", *");
    private static final Pattern IMPL_DEP = Pattern.compile(" *([a-zA-Z0-9_.]+)(/[0-9]+)? *= *(.+) *");

    public void setManifest(File manifestFile) throws BuildException {
        super.setManifest(manifestFile);
        Manifest added = new Manifest();
        try {
            String pubPkgs = getProject().getProperty("public.packages");
            if (pubPkgs == null) {
                throw new BuildException("Must have defined 'public.packages'", getLocation());
            }
            added.addConfiguredAttribute(new Manifest.Attribute("OpenIDE-Module-Public-Packages", pubPkgs));
            String ideDeps = getProject().getProperty("ide.dependencies");
            if (ideDeps != null) {
                added.addConfiguredAttribute(new Manifest.Attribute("OpenIDE-Module-IDE-Dependencies", ideDeps));
            }
            String moduleDeps = getProject().getProperty("module.dependencies");
            if (moduleDeps != null) {
                added.addConfiguredAttribute(new Manifest.Attribute("OpenIDE-Module-Module-Dependencies", moduleDeps));
            }
            // Check to see if OpenIDE-Module-Implementation-Version is already defined.
            String implVers;
            String myself;
            InputStream is = new FileInputStream(manifestFile);
            try {
                Manifest staticManifest = new Manifest(new InputStreamReader(is, "UTF-8"));
                Manifest.Section mainSection = staticManifest.getMainSection();
                implVers = mainSection.getAttributeValue("OpenIDE-Module-Implementation-Version");
                myself = mainSection.getAttributeValue("OpenIDE-Module");
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
                if (implVers != null) {
                    try {
                        Integer implVersI = new Integer(implVers);
                        specVersBase += "." + implVersI;
                    } catch (NumberFormatException e) {
                        // OK, ignore it, not numeric.
                        getProject().log(manifestFile + ": warning: use of spec.version.base with non-integer OpenIDE-Module-Implementation-Version", Project.MSG_WARN);
                    }
                }
                SortedMap/*<String,Integer>*/ additions = new TreeMap();
                String[] deps = {ideDeps, moduleDeps};
                for (int i = 0; i < 2; i++) {
                    String dep = deps[i];
                    if (dep == null) {
                        continue;
                    }
                    String[] individualDeps = COMMA_SPACE.split(dep);
                    for (int j = 0; j < individualDeps.length; j++) {
                        Matcher m = IMPL_DEP.matcher(individualDeps[j]);
                        if (m.matches()) {
                            String cnb = m.group(1);
                            String version = m.group(3);
                            try {
                                Integer versionI = new Integer(version);
                                additions.put(cnb, versionI);
                            } catch (NumberFormatException e) {
                                // OK, ignore this one, not numeric.
                                getProject().log("Warning: in " + myself + ", use of spec.version.base with non-integer OpenIDE-Module-Implementation-Version from " + cnb, Project.MSG_WARN);
                            }
                        }
                    }
                }
                Iterator versions = additions.values().iterator();
                while (versions.hasNext()) {
                    Integer version = (Integer) versions.next();
                    specVersBase += "." + version;
                }
                added.addConfiguredAttribute(new Manifest.Attribute("OpenIDE-Module-Specification-Version", specVersBase));
            } else if ((ideDeps != null && ideDeps.indexOf('=') != -1) || (moduleDeps != null && moduleDeps.indexOf('=') != -1)) {
                getProject().log("Warning: in " + myself + ", not using spec.version.base, yet declaring implementation dependencies; may lead to problems with Auto Update", Project.MSG_WARN);
            } else if (implVers != null) {
                try {
                    new Integer(implVers);
                } catch (NumberFormatException e) {
                    getProject().log(manifestFile + ": warning: use of non-integer OpenIDE-Module-Implementation-Version may be problematic for clients trying to use spec.version.base", Project.MSG_WARN);
                }
            }
            // Now ask the regular <jar> task to add all this stuff to the regular manifest.mf.
            addConfiguredManifest(added);
        } catch (Exception e) {
            throw new BuildException(e, getLocation());
        }
    }
    
}
