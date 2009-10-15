/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.universe;

import org.netbeans.modules.apisupport.project.ui.customizer.*;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Utility methods for cluster-related tasks.
 * @author Richard Michalsky
 */
public final class ClusterUtils {

    private ClusterUtils() {
    }

    public static boolean isValidCluster(File file) {
        return (new File(file, "config/Modules")).isDirectory();
    }

    /**
     * Returns path to cluster dir for specified project.
     * Path is returned even if it does not currently exist.
     * @param prj NB Suite or Module project
     * @return Path to cluster dir or <tt>null</tt> if e.g. wrong type of project.
     */
    public static File getClusterDirectory(Project prj) {
        NbModuleProvider nbmp = prj.getLookup().lookup(NbModuleProvider.class);
        if (nbmp != null)
            return nbmp.getModuleJarLocation().getParentFile().getParentFile();
        SuiteProvider sprv = prj.getLookup().lookup(SuiteProvider.class);
        if (sprv != null)
            return sprv.getClusterDirectory();
        return null;
    }

    /**
     * Resolves single cluster path entry with respect to project root and ${nbplatform.active} root.
     * @param rawEntry Single cluster.path entry as stored in platform.properties
     * @param root Project root
     * @param eval Project property evaluator
     * @param nbPlatformRoot Platform root used to replace ${nbplatform.active} references in the entry
     * @return Absolute path to entry
     */
    public static File evaluateClusterPathEntry(String rawEntry, File root, PropertyEvaluator eval, File nbPlatformRoot) {
        // When cluster does not exist, it is either bare name or one with different number
        final Pattern pat = Pattern.compile("(?:.*[\\\\/])?([^/\\\\]*?)([0-9.]+)?[/\\\\]?$");
        final String nbDirProp = "${" + SuiteProperties.ACTIVE_NB_PLATFORM_DIR_PROPERTY + "}";
        if (rawEntry.startsWith(nbDirProp)) {
            rawEntry = nbPlatformRoot.getAbsolutePath()
                    + rawEntry.substring(nbDirProp.length());
        }

        File path = PropertyUtils.resolveFile(root, eval.evaluate(rawEntry));
        if (! path.exists()) {
            // search for corresponding numbered cluster
            final Matcher cm = pat.matcher(path.getAbsolutePath());
            if (cm.matches()) {
                File parent = path.getParentFile();
                if (parent != null) {
                    File[] alternate = parent.listFiles(new FilenameFilter() {

                        public boolean accept(File dir, String name) {
                            Matcher am = pat.matcher(name);
                            return am.matches() && cm.group(1).equalsIgnoreCase(am.group(1));
                        }
                    });
                    if (alternate == null) {
                        // not found, just return what we have
                        return path;
                    }
                    if (alternate.length > 0 && alternate[0].isDirectory()) {
                        return alternate[0];
                    }
                }
            }
        }
        return path;
    }

    public static Set<ClusterInfo> evaluateClusterPath(File root, PropertyEvaluator eval, File nbPlatformRoot) {
        Set<ClusterInfo> clusterPath = new LinkedHashSet<ClusterInfo>();
        String cpp = eval.getProperty(SuiteProperties.CLUSTER_PATH_PROPERTY);
        String[] paths = PropertyUtils.tokenizePath(cpp != null ? cpp : "");
        String cpwdcp = eval.getProperty(SuiteProperties.CLUSTER_PATH_WDC_PROPERTY);
        String[] pathsWDC = cpwdcp != null ? PropertyUtils.tokenizePath(cpwdcp) : null;
        String[] wp = (pathsWDC != null) ? pathsWDC : paths;
        Set<String> enabledPaths = new HashSet<String>();
        if (pathsWDC != null) {
            enabledPaths.addAll(Arrays.asList(paths));
        }

        Map<File, String> srcRootsMap = new HashMap<File, String>();
        Map<File, String> jRootsMap = new HashMap<File, String>();
        Map<String, String> props = eval.getProperties();
        for (Map.Entry<String, String> entry : props.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(SuiteProperties.CLUSTER_SRC_PREFIX)) {
                if (key.endsWith(NbPlatform.PLATFORM_SOURCES_SUFFIX)) {
                    String cd = key.substring(SuiteProperties.CLUSTER_SRC_PREFIX.length(),
                            key.length() - NbPlatform.PLATFORM_SOURCES_SUFFIX.length());
                    File cf = PropertyUtils.resolveFile(root, cd);
                    srcRootsMap.put(cf, entry.getValue());
                } else if (key.endsWith(NbPlatform.PLATFORM_JAVADOC_SUFFIX)) {
                    String cd = key.substring(SuiteProperties.CLUSTER_SRC_PREFIX.length(),
                            key.length() - NbPlatform.PLATFORM_JAVADOC_SUFFIX.length());
                    File cf = PropertyUtils.resolveFile(root, cd);
                    jRootsMap.put(cf, entry.getValue());
                }
            }
        }

        for (String path : wp) {
            boolean isPlaf = path.contains("${" + SuiteProperties.ACTIVE_NB_PLATFORM_DIR_PROPERTY + "}");
            File cd = evaluateClusterPathEntry(path, root, eval, nbPlatformRoot);
            Project prj = null;
            if (! cd.exists()) {
                // fallback for not-yet-built project clusters
                String p2 = cd.getAbsolutePath();

                int b = p2.length() - SuiteProperties.CLUSTER_DIR.length();
                if (b >= 0) {
                    if (SuiteProperties.CLUSTER_DIR.equals(p2.substring(b).replace(File.separatorChar, '/'))) {
                        cd = new File(p2.substring(0, b));
                    }
                }
            }
            FileObject fo = FileUtil.toFileObject(cd);
            if (fo != null) {
                if (! NbPlatform.isPlatformDirectory(cd.getParentFile())) { // #168804, allow custom platforms
                    prj = FileOwnerQuery.getOwner(fo);
                    if (prj != null
                            && prj.getLookup().lookup(NbModuleProvider.class) == null
                            && prj.getLookup().lookup(SuiteProvider.class) == null) {
                        // probably found nbbuild above the platform, use only regular NB module projects
                        prj = null;
                    }
                }
            }
            boolean enabled = (pathsWDC == null) || enabledPaths.contains(path);
            URL[] srcRoots = null;
            if (srcRootsMap.containsKey(cd)) {
                srcRoots = Util.findURLs(srcRootsMap.get(cd));
            }
            URL[] jRoots = null;
            if (jRootsMap.containsKey(cd)) {
                jRoots = Util.findURLs(jRootsMap.get(cd));
            }
            clusterPath.add(ClusterInfo.createFromCP(cd, prj, isPlaf, srcRoots, jRoots, enabled));
        }
        return clusterPath;
    }
}
