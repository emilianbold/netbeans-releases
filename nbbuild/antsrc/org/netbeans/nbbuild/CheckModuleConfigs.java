/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Analyzes build.properties and cluster.properties and tries to diagnose any problems.
 * Also produces a summary of moduleconfig contents which is written to a golden file.
 * @author Jesse Glick
 */
public final class CheckModuleConfigs extends Task {
    
    private File nbroot;
    
    public CheckModuleConfigs() {}
    
    public void setNbroot(File f) {
        nbroot = f;
    }
    
    public @Override void execute() throws BuildException {
        if (nbroot == null) {
            throw new BuildException("Must define 'nbroot' param", getLocation());
        }
        File buildPropertiesFile = new File(nbroot, "nbbuild" + File.separatorChar + "build.properties");
        File clusterPropertiesFile = new File(nbroot, "nbbuild" + File.separatorChar + "cluster.properties");
        File goldenFile = new File(nbroot, "nbbuild" + File.separatorChar + "build" + File.separatorChar + "generated" + File.separatorChar + "moduleconfigs.txt");
        File masterProjectXml = new File(nbroot, "ide" + File.separatorChar + "allmodules" + File.separatorChar + "nbproject" + File.separatorChar + "project.xml");
        @SuppressWarnings("unchecked")
        Map<String,String> properties = getProject().getProperties();
        Map<String,Set<String>> configs = loadModuleConfigs(properties, buildPropertiesFile);
        Map<String,Set<String>> clusters = loadModuleClusters(properties, clusterPropertiesFile);
        Set<String> allClusterModules = new TreeSet<String>();
        for (Set<String> s : clusters.values()) {
            allClusterModules.addAll(s);
        }
        try {
            writeModuleConfigs(goldenFile, configs, buildPropertiesFile);
        } catch (IOException e) {
            throw new BuildException("Could not write to " + goldenFile, e, getLocation());
        }
        try {
            writeMasterProjectXml(masterProjectXml, allClusterModules);
        } catch (SAXException e) {
            throw new BuildException("Could not write to " + masterProjectXml, e, getLocation());
        } catch (IOException e) {
            throw new BuildException("Could not write to " + masterProjectXml, e, getLocation());
        }
        Set<String> s;
        /* Apparently wanted now?
        // Check that stable != daily-alpha-nbms:
        s = new TreeSet((Set) configs.get("stable"));
        s.retainAll((Set) configs.get("daily-alpha-nbms"));
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: stable and daily-alpha-nbms configs overlap: " + s);
        }
        */
        /* This is not actually desired; just includes everything:
        // Check that sigtest <= javadoc:
        s = new TreeSet((Set) configs.get("sigtest"));
        s.removeAll((Set) configs.get("javadoc"));
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: sigtest config contains entries not in javadoc config: " + s);
        }
        */
        /* This config is no more?
        // Check that platform-javadoc <= javadoc:
        s = new TreeSet((Set) configs.get("platform-javadoc"));
        s.removeAll((Set) configs.get("javadoc"));
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: platform-javadoc config contains entries not in javadoc config: " + s);
        }
        */
        // Check that javadoc <= stable + daily-alpha-nbms:
        s = new TreeSet<String>(configs.get("javadoc"));
        s.removeAll(configs.get("stable"));
        s.removeAll(configs.get("daily-alpha-nbms"));
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: javadoc config contains entries not in stable and daily-alpha-nbms configs: " + s);
        }
        /*
        // Check that platform-javadoc = javadoc where module in platform cluster:
        Set/ *<String>* / platformJavadoc = (Set) configs.get("platform-javadoc");
        Set/ *<String>* / platformClusterJavadoc = (Set) configs.get("javadoc");
        platformClusterJavadoc.retainAll((Set) clusters.get("nb.cluster.platform"));
        s = new TreeSet(platformJavadoc);
        s.removeAll(platformClusterJavadoc);
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: platform-javadoc config not equal to javadoc config for platform cluster modules: " + s);
        }
        s = new TreeSet(platformClusterJavadoc);
        s.removeAll(platformJavadoc);
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: platform-javadoc config not equal to javadoc config restricted to platform cluster modules: " + s);
        }
        */
        // Check that stable = modules in enumerated clusters:
        Set<String> stable = configs.get("all");
        s = new TreeSet<String>(stable);
        s.removeAll(allClusterModules);
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: 'all' config not equal to listed cluster modules: " + s);
        }
        s = new TreeSet<String>(allClusterModules);
        s.removeAll(stable);
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: 'all' config not equal to listed cluster modules: " + s);
        }
        // Check that platform = modules in platform cluster:
        Set<String> platform = configs.get("platform");
        Set<String> platformCluster = clusters.get("nb.cluster.platform");
        s = new TreeSet<String>(platform);
        s.removeAll(platformCluster);
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: platform config not equal to platform cluster modules: " + s);
        }
        s = new TreeSet<String>(platformCluster);
        s.removeAll(platform);
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: platform config not equal to platform cluster modules: " + s);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Set<String> split(String list, boolean warnIfUnsorted, String what) {
        List elements = Collections.list(new StringTokenizer(list, ", "));
        if (warnIfUnsorted) {
            List sorted = new ArrayList(elements);
            Collections.sort(sorted);
            if (!sorted.equals(elements)) {
                log("warning: unsorted list: " + elements);
            }
        }
        HashSet set = new HashSet(elements);
        for (Object o : set) {
            elements.remove(o);
        }
        if (!elements.isEmpty()) { // #147690
            log("warning: duplicates found in " + what + ": " + elements);
        }
        return set;
    }
    
    private Map<String,Set<String>> loadModuleConfigs(Map<String,String> buildProperties, File buildPropertiesFile) {
        Map<String,Set<String>> configs = new TreeMap<String,Set<String>>();
        for (String k : buildProperties.keySet()) {
            String prefix = "config.modules.";
            if (!k.startsWith(prefix)) {
                continue;
            }
            String config = k.substring(prefix.length());
            Set<String> modules = new TreeSet<String>(split(buildProperties.get(k), false, k));
            String fixedK = "config.fixedmodules." + config;
            String fixed = buildProperties.get(fixedK);
            if (fixed != null) {
                modules.addAll(split(fixed, false, fixedK));
            } else {
                log(buildPropertiesFile + ": warning: have " + k + " but no " + fixedK, Project.MSG_WARN);
            }
            configs.put(config, modules);
        }
        return configs;
    }

    private void writeModuleConfigs(File goldenFile, Map<String,Set<String>> configs, File buildPropertiesFile) throws IOException {
        log("Writing moduleconfigs " + configs.keySet() + " from " + buildPropertiesFile + " to " + goldenFile);
        goldenFile.getParentFile().mkdirs();
        Writer w = new FileWriter(goldenFile); // default encoding OK
        try {
            PrintWriter pw = new PrintWriter(w);
            pw.println("# To update, run: ant -f nbbuild/build.xml check-module-configs");
            for (Map.Entry<String,Set<String>> entry : configs.entrySet()) {
                String config = entry.getKey();
                for (String module : entry.getValue()) {
                    pw.println(config + ':' + module);
                }
            }
            pw.flush();
        } finally {
            w.close();
        }
    }

    private Map<String,Set<String>> loadModuleClusters(Map<String,String> clusterProperties, File clusterPropertiesFile) {
        String fullConfig = "clusters.config.full.list";
        String l = clusterProperties.get(fullConfig);
        if (l == null) {
            log(clusterPropertiesFile + ": warning: no definition for clusters.config.full.list", Project.MSG_WARN);
            return Collections.emptyMap();
        }
        Map<String,Set<String>> clusters = new TreeMap<String,Set<String>>();
        for (String cluster : split(l, false,fullConfig)) {
            l = clusterProperties.get(cluster);
            if (l == null) {
                log(clusterPropertiesFile + ": warning: no definition for " + cluster, Project.MSG_WARN);
                continue;
            }
            clusters.put(cluster, new TreeSet<String>(split(l, true,fullConfig)));
        }
        return clusters;
    }

    private void writeMasterProjectXml(File masterProjectXml, Set<String> allClusterModules) throws IOException, SAXException {
        log("Writing module list  to " + masterProjectXml);
        Document doc = XMLUtil.parse(new InputSource(masterProjectXml.toURI().toString()), false, true, null, null);
        NodeList nl = doc.getElementsByTagName("subprojects");
        if (nl.getLength() != 1) {
            throw new IOException("No or multiple <subprojects>");
        }
        Element sp  = (Element) nl.item(0);
        nl = sp.getChildNodes();
        while (nl.getLength() > 0) {
            sp.removeChild(nl.item(0));
        }
        sp.appendChild(doc.createComment(" To update, run: ant -f nbbuild/build.xml check-module-configs "));
        for (String module : allClusterModules) {
            if (new File(nbroot, (module + "/nbproject/project.xml").replace('/', File.separatorChar)).isFile()) {
                Element e = doc.createElementNS("http://www.netbeans.org/ns/freeform-project/1", "project");
                String path = "../../" + module;
                e.appendChild(doc.createTextNode(path.replaceFirst("^\\.\\./\\.\\./ide/", "../")));
                sp.appendChild(e);
            } else {
                sp.appendChild(doc.createComment(" Unprojectized: " + module + " "));
            }
        }
        OutputStream os = new FileOutputStream(masterProjectXml);
        try {
            XMLUtil.write(doc, os);
        } finally {
            os.close();
        }
    }
    
}
