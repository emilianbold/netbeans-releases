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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.jar.Manifest;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Task which checks content of an update center to make sure module dependencies
 * are internally consistent. Can optionally also check against a previous update
 * center snapshot to make sure that updates of modules marked as newer (i.e. with
 * newer specification versions) would result in a consistent snapshot as well.
 * If there are any modules which cannot be loaded, the build fails with a description.
 * <p>
 * Actual NBMs are not downloaded. Everything necessary is present just in
 * the update center XML descriptor.
 * <p>
 * You must specify a classpath to load the NB module system from.
 * It should suffice to include those JARs in the NB platform cluster's <code>lib</code> folder.
 * @author Jesse Glick
 */
public final class VerifyUpdateCenter extends Task {

    public VerifyUpdateCenter() {}

    private URI updates;
    public void setUpdates(File f) {
        updates = f.toURI();
    }
    public void setUpdatesURL(URI u) {
        updates = u;
    }

    private URI oldUpdates;
    public void setOldUpdates(File f) {
        if (f.isFile()) {
            oldUpdates = f.toURI();
        } else {
            log("No such file: " + f, Project.MSG_WARN);
        }
    }
    public void setOldUpdatesURL(URI u) {
        if (u.toString().length() > 0) {
            oldUpdates = u;
        }
    }

    private Path classpath = new Path(getProject());
    public void addConfiguredClasspath(Path p) {
        classpath.append(p);
    }

    @Override
    public void execute() throws BuildException {
        if (updates == null) {
            throw new BuildException("you must specify updates");
        }
        ClassLoader loader = new AntClassLoader(getProject(), classpath);
        try {
            Set<Manifest> manifests = loadManifests(updates);
            checkForProblems(findInconsistencies(manifests, loader), "Inconsistency(ies) in " + updates);
            if (oldUpdates != null) {
                Map<String,Manifest> updated = new HashMap<String,Manifest>();
                for (Manifest m : loadManifests(oldUpdates)) {
                    updated.put(findCNB(m), m);
                }
                if (!findInconsistencies(new HashSet<Manifest>(updated.values()), loader).isEmpty()) {
                    log(oldUpdates + " is already inconsistent, skipping update check", Project.MSG_WARN);
                    return;
                }
                for (Manifest m : manifests) {
                    String cnb = findCNB(m);
                    boolean doUpdate = true;
                    Manifest old = updated.get(cnb);
                    if (old != null) {
                        String oldspec = old.getMainAttributes().getValue("OpenIDE-Module-Specification-Version");
                        String newspec = m.getMainAttributes().getValue("OpenIDE-Module-Specification-Version");
                        doUpdate = specGreaterThan(newspec, oldspec);
                    }
                    if (doUpdate) {
                        updated.put(cnb, m);
                    }
                }
                SortedMap<String,SortedSet<String>> updateProblems = findInconsistencies(new HashSet<Manifest>(updated.values()), loader);
                checkForProblems(updateProblems, "Inconsistency(ies) in " + updates + " relative to " + oldUpdates);
            }
        } catch (BuildException x) {
            throw x;
        } catch (Exception x) {
            throw new BuildException(x, getLocation());
        }
    }

    @SuppressWarnings("unchecked")
    private static SortedMap<String,SortedSet<String>> findInconsistencies(Set<Manifest> manifests, ClassLoader loader) throws Exception {
        return (SortedMap) loader.loadClass("org.netbeans.ConsistencyVerifier").
                getMethod("findInconsistencies", Set.class).invoke(null, manifests);
    }

    private Set<Manifest> loadManifests(URI u) throws BuildException {
        try {
            Document doc = XMLUtil.parse(new InputSource(u.toString()), false, false, null, new EntityResolver() {
                public InputSource resolveEntity(String pub, String sys) throws SAXException, IOException {
                    if (pub.contains("DTD Autoupdate Catalog")) {
                        return new InputSource(new StringReader(""));
                    } else {
                        return null;
                    }
                }
            });
            Set<Manifest> manifests = new HashSet<Manifest>();
            NodeList nl = doc.getElementsByTagName("manifest");
            for (int i = 0; i < nl.getLength(); i++) {
                Element m = (Element) nl.item(i);
                Manifest mani = new Manifest();
                NamedNodeMap map = m.getAttributes();
                for (int j = 0; j < map.getLength(); j++) {
                    Attr a = (Attr) map.item(j);
                    mani.getMainAttributes().putValue(a.getName(), a.getValue());
                }
                manifests.add(mani);
            }
            return manifests;
        } catch (Exception x) {
            throw new BuildException("Could not load " + u, x, getLocation());
        }
    }

    private static String findCNB(Manifest m) {
        String name = m.getMainAttributes().getValue("OpenIDE-Module");
        if (name == null) {
            throw new IllegalArgumentException();
        }
        return name.replaceFirst("/\\d+$", "");
    }

    private static boolean specGreaterThan(String newspec, String oldspec) {
        if (newspec == null) {
            return false;
        }
        if (oldspec == null) {
            return true;
        }
        String[] olddigits = oldspec.split("\\.");
        String[] newdigits = newspec.split("\\.");
        int oldlen = olddigits.length;
        int newlen = newdigits.length;
        int max = Math.max(oldlen, newlen);
        for (int i = 0; i < max; i++) {
            int oldd = (i < oldlen) ? Integer.parseInt(olddigits[i]) : 0;
            int newd = (i < newlen) ? Integer.parseInt(newdigits[i]) : 0;
            if (oldd != newd) {
                return newd > oldd;
            }
        }
        return false;
    }
    
    private void checkForProblems(SortedMap<String,SortedSet<String>> problems, String msg) throws BuildException {
        if (!problems.isEmpty()) {
            for (Map.Entry<String,SortedSet<String>> entry : problems.entrySet()) {
                log("Problems found for module " + entry.getKey() + ": " + entry.getValue(), Project.MSG_ERR);
            }
            throw new BuildException(msg, getLocation());
        }
    }

}
