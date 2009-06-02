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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.apisupport.project.ManifestManager.PackageExport;
import org.netbeans.modules.apisupport.project.Util;
import org.openide.filesystems.FileUtil;

/**
 * A netbeans.org module as described by nbbuild/nbproject/private/scan-cache-full.ser.
 * See org.netbeans.nbbuild.ModuleListParser.{scanNetBeansOrgSources,Entry} for details.
 */
final class NetBeansOrgCachedEntry extends AbstractEntryWithSources {

    private static final Logger LOG = Logger.getLogger(NetBeansOrgCachedEntry.class.getName());

    private final File nb_all;
    private final File nbdestdir;
    private final String cnb;
    private final File jar;
    private final String classPathExtensions;
    private final File sourceLocation;
    private final String netbeansOrgPath;
    private final String[] buildPrerequisites;
    private final File cluster;
    private final String[] runtimeDependencies;
    private final String[] testDependencies;
    private ModuleEntry officialEntry;
    private URL javadoc;

    public NetBeansOrgCachedEntry(File nb_all, File nbdestdir, String cnb, File jar, File[] classPathExtensions, File sourceLocation, String netbeansOrgPath,
            String[] buildPrerequisites, String clusterName, String[] runtimeDependencies, String[] testDependencies) {
        super(/* will not be used */null);
        this.nb_all = nb_all;
        this.nbdestdir = FileUtil.normalizeFile(nbdestdir);
        this.cnb = cnb;
        this.jar = FileUtil.normalizeFile(jar);
        StringBuilder b = new StringBuilder();
        for (File f : classPathExtensions) {
            b.append(File.pathSeparatorChar);
            b.append(FileUtil.normalizeFile(f).getAbsolutePath());
        }
        this.classPathExtensions = b.toString();
        this.sourceLocation = FileUtil.normalizeFile(sourceLocation);
        this.netbeansOrgPath = netbeansOrgPath;
        this.buildPrerequisites = buildPrerequisites;
        this.cluster = new File(this.nbdestdir, clusterName);
        this.runtimeDependencies = runtimeDependencies;
        this.testDependencies = testDependencies;
    }

    public String getNetBeansOrgPath() {
        return netbeansOrgPath;
    }

    public File getSourceLocation() {
        return sourceLocation;
    }

    public String getCodeNameBase() {
        return cnb;
    }

    public File getClusterDirectory() {
        return cluster;
    }

    public File getJarLocation() {
        return jar;
    }

    public String getClassPathExtensions() {
        if (officialEntry != null) {
            // This is preferable because it may take into account <binary-origin> in project.xml.
            return officialEntry.getClassPathExtensions();
        } else {
            return classPathExtensions;
        }
    }

    private synchronized void ensureOfficialEntry(String why) {
        if (officialEntry == null) {
            LOG.fine("Had to fall back to loading official entry for " + cnb + " because of " + why);
            Map<String,ModuleEntry> entries = new HashMap<String,ModuleEntry>();
            try {
                ModuleList.scanPossibleProject(sourceLocation, entries, false, false, nb_all, nbdestdir, netbeansOrgPath);
                officialEntry = entries.get(cnb);
                if (officialEntry == null) {
                    LOG.fine("Failed to load official entry for " + cnb);
                }
            } catch (IOException x) {
                LOG.log(Level.FINE, null, x);
            }
        }
    }

    public String getReleaseVersion() {
        ensureOfficialEntry("getReleaseVersion");
        if (officialEntry != null) {
            return officialEntry.getReleaseVersion();
        } else {
            return null;
        }
    }

    public String[] getProvidedTokens() {
        ensureOfficialEntry("getProvidedTokens");
        if (officialEntry != null) {
            return officialEntry.getProvidedTokens();
        } else {
            return new String[0];
        }
    }

    public PackageExport[] getPublicPackages() {
        ensureOfficialEntry("getPublicPackages");
        if (officialEntry != null) {
            return officialEntry.getPublicPackages();
        } else {
            return new PackageExport[0];
        }
    }

    public boolean isDeclaredAsFriend(String cnb) {
        ensureOfficialEntry("isDeclaredAsFriend");
        if (officialEntry != null) {
            return officialEntry.isDeclaredAsFriend(cnb);
        } else {
            return false;
        }
    }

    public boolean isDeprecated() {
        ensureOfficialEntry("isDeprecated");
        if (officialEntry != null) {
            return officialEntry.isDeprecated();
        } else {
            return false;
        }
    }

    public @Override Set<String> getPublicClassNames() {
        ensureOfficialEntry("getPublicClassNames");
        if (officialEntry != null) {
            return officialEntry.getPublicClassNames();
        } else {
            return Collections.emptySet();
        }
    }

    public @Override String toString() {
        return "NetBeansOrgCachedEntry[" + getSourceLocation() + (officialEntry != null ? "->" + officialEntry : "") + "]"; // NOI18N
    }

    public URL getJavadoc(final NbPlatform platform) {
        if (javadoc == null)
            javadoc = NetBeansOrgEntry.findJavadocForNetBeansOrgModules(this, nbdestdir);
        return javadoc;
    }
}
