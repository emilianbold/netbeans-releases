/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport;
import org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport.Context;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.WLProductProperties;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Hejl
 */
public final class WLJpa2SwitchSupport {

    private static final Version SWITCH_SUPPORTED_VERSION = Version.fromJsr277NotationWithFallback("10.3.4");

    private static final String OEPE_CONTRIBUTIONS_JAR = "oepe-contributions.jar"; // NO18N
    
    private static final String JPA_JAR_1 = "javax.persistence_1.0.0.0_2-0-0.jar"; // NO18N
    
    private static final String JPA_JAR_2 = "com.oracle.jpa2support_1.0.0.0_2-0.jar"; // NO18N
    
    private static final Logger LOGGER = Logger.getLogger(WLJpa2SwitchSupport.class.getName());
    
    private final File serverRoot;
    
    private final WLDeploymentManager dm;
    
    /** GuardedBy("this")*/
    private Version serverVersion;
    
    private boolean proggessSuccess = true;

    public WLJpa2SwitchSupport(File serverRoot) {
        this.dm = null;
        this.serverRoot = serverRoot;
    }

    public WLJpa2SwitchSupport(WLDeploymentManager dm) {
        this.dm = dm;
        this.serverRoot = WLPluginProperties.getServerRoot(dm, true);
    }

    public boolean isSwitchSupported() {
        Version version = null;
        synchronized (this) {
            if (serverVersion != null) {
                version = serverVersion;
            } else {
                if (dm != null) {
                    version = dm.getServerVersion();
                } else {
                    version = WLPluginProperties.getServerVersion(serverRoot);
                }
                serverVersion = version;
            }
        }
        return SWITCH_SUPPORTED_VERSION.getMajor().equals(version.getMajor())
                && SWITCH_SUPPORTED_VERSION.getMinor().equals(version.getMinor())
                && SWITCH_SUPPORTED_VERSION.getMicro().equals(version.getMicro());
    }

    public void enable() {
        if (!isSwitchSupported()) {
            throw new IllegalStateException("JPA2 switching is not supported for WebLogic " + serverRoot);
        }

        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();
        proggessSuccess = true;
        try {
            File libDir = WLPluginProperties.getServerLibDirectory(serverRoot);
            if (libDir != null) {
                libDir = FileUtil.normalizeFile(libDir);
            }

            String path = getPathToModules(libDir);
            if (path.length() > 0) {
                path = path + "/"; // NOI18N
            }
            final File oepeFile = new File(libDir, OEPE_CONTRIBUTIONS_JAR);
            final String relPath = path;
            final String contribPath = path + JPA_JAR_1 + " " // NOI18N
                    + path + JPA_JAR_2;//NOI18N

            //OEPE jar part
            actions.add(new ProgressSupport.BackgroundAction() {

                @Override
                protected void run(Context actionContext) {
                    actionContext.progress("Processing oepe_contributions.jar ");
                    try {
                        // oepe does not exist
                        if (!oepeFile.exists()) {
                            createContributionsJar(oepeFile, contribPath);
                            // exists so update cp
                        } else {
                            JarFile oepeJarFile = new JarFile(oepeFile);
                            try {
                                Manifest mf = oepeJarFile.getManifest();
                                String cp = mf.getMainAttributes().getValue(Name.CLASS_PATH);
                                if (cp == null) {
                                    cp = ""; // NOI18N
                                }
                                if (!cp.contains(JPA_JAR_1) || !cp.contains(JPA_JAR_2)) {
                                    StringBuilder updated = new StringBuilder(cp);
                                    if (cp != null) {
                                        // TODO full path check
                                        if (!cp.contains(JPA_JAR_2)) {
                                            updated.insert(0, " ").insert(0, JPA_JAR_2).insert(0, relPath);
                                        }
                                        if (!cp.contains(JPA_JAR_1)) {
                                            updated.insert(0, " ").insert(0, JPA_JAR_1).insert(0, relPath);
                                        }
                                    }
                                    if (cp.length() == 0) {
                                        updated.deleteCharAt(updated.length() - 1);
                                    }
                                    mf.getMainAttributes().put(Name.CLASS_PATH, updated.toString());
                                    replaceManifest(oepeFile, mf);
                                }
                            } finally {
                                oepeJarFile.close();
                            }
                        }
                    } catch (IOException ex) {
                        proggessSuccess = false;
                        Exceptions.printStackTrace(ex);
                    }
                }
            });

            //Weblogic.jar part
            actions.add(new ProgressSupport.BackgroundAction() {

                @Override
                protected void run(Context actionContext) {
                    if (!proggessSuccess) {
                        return;
                    }
                    actionContext.progress("Processing weblogic.jar ");
                    try {
                        // update weblogic.jar
                        File weblogicFile = WLPluginProperties.getWeblogicJar(serverRoot);
                        JarFile weblogicJarFile = new JarFile(weblogicFile);
                        try {
                            Manifest wlManifest = weblogicJarFile.getManifest();
                            String cp = wlManifest.getMainAttributes().getValue(Name.CLASS_PATH);
                            if (cp == null) {
                                cp = ""; // NOI18N
                            }
                            if (!cp.contains(OEPE_CONTRIBUTIONS_JAR)) {
                                if (cp.length() == 0) {
                                    cp = OEPE_CONTRIBUTIONS_JAR;
                                } else {
                                    cp = OEPE_CONTRIBUTIONS_JAR + " " + cp; // NOI18N
                                }
                                wlManifest.getMainAttributes().put(Name.CLASS_PATH, cp);
                                replaceManifest(weblogicFile, wlManifest);
                            }
                        } finally {
                            weblogicJarFile.close();
                        }
                    } catch (IOException ex) {
                        proggessSuccess = false;
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            ///////////////
            ProgressSupport.invoke(actions);

        } finally {
            if (dm != null) {
                dm.getJ2eePlatformImpl().notifyLibrariesChange();
            }
        }
    }

    public void disable() {
        if (!isSwitchSupported()) {
            throw new IllegalStateException("JPA2 switching is not supported for WebLogic " + serverRoot);
        }

        try {
            File libDir = WLPluginProperties.getServerLibDirectory(serverRoot);
            if (libDir != null) {
                libDir = FileUtil.normalizeFile(libDir);
            }
            File oepeJarFile = new File(libDir, OEPE_CONTRIBUTIONS_JAR);
            if (!oepeJarFile.exists() || !oepeJarFile.isFile()) {
                return;
            }
            JarFile file = new JarFile(oepeJarFile);
            try {
                Manifest mf = file.getManifest();
                String cp = mf.getMainAttributes().getValue(Name.CLASS_PATH);
                if (cp == null) {
                    return;
                }

                StringBuilder builder = new StringBuilder();
                for (String element : cp.split("\\s+")) { // NOI18N
                    if (!element.contains(JPA_JAR_1) && !element.contains(JPA_JAR_2)) {
                        builder.append(element).append(" "); // NOI18N
                    }
                }
                if (builder.length() > 0) {
                    mf.getMainAttributes().put(Name.CLASS_PATH,
                            builder.substring(0, builder.length() - 1));
                } else {
                    mf.getMainAttributes().remove(Name.CLASS_PATH);
                }
                replaceManifest(oepeJarFile, mf);
            } finally {
                file.close();
            }
        } catch (IOException ex) {
            // TODO some exception/message to the user
            Exceptions.printStackTrace(ex);
        } finally {
            if (dm != null) {
                dm.getJ2eePlatformImpl().notifyLibrariesChange();
            }
        }
    }

    public boolean isEnabled() {
        if (dm != null) {
            return dm.getJ2eePlatformImpl().isJpa2Available();
        } else {
            // TODO parse jar cp
            return false;
        }
    }

    public boolean isEnabledViaSmartUpdate() {
        //check for BUG9923849_WLS103MP4.jar on Library classpath from j2eePlatformImpl
        if (dm != null) {
            for (LibraryImplementation lib : dm.getJ2eePlatformImpl().getLibraries()) {
                List<URL> urls = lib.getContent("classpath"); // NOI18N
                if (urls != null) {
                    for (URL url : urls) {
                        String file = url.getFile();
                        if (file.endsWith("BUG9923849_WLS103MP4.jar!/")) { // NOI18N
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void replaceManifest(File jarFile, Manifest manifest) throws IOException {
        FileObject fo = FileUtil.toFileObject(jarFile);
        String tmpName = FileUtil.findFreeFileName(fo.getParent(),
                jarFile.getName(), "tmp"); // NOI18N
        File tmpJar = new File(jarFile.getParentFile(), tmpName + ".tmp"); // NOI18N
        try {
            InputStream is = new BufferedInputStream(
                    new FileInputStream(jarFile));
            try {
                OutputStream os = new BufferedOutputStream(
                        new FileOutputStream(tmpJar));
                try {
                    replaceManifest(is, os, manifest);
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }

            if (tmpJar.renameTo(jarFile)) {
                LOGGER.log(Level.FINE, "Successfully moved {0}", tmpJar);
                return;
            }
            LOGGER.log(Level.FINE, "Byte to byte copy {0}", tmpJar);
            copy(tmpJar, jarFile);
        } finally {
            tmpJar.delete();
        }
    }

    private void replaceManifest(InputStream is, OutputStream os, Manifest manifest) throws IOException {
        JarInputStream in = new JarInputStream(is);
        try {
            JarOutputStream out = new JarOutputStream(os, manifest);
            try {
                JarEntry entry = null;
                byte[] temp = new byte[32768];
                while ((entry = in.getNextJarEntry()) != null) {
                    String name = entry.getName();
                    if (name.equalsIgnoreCase("META-INF/MANIFEST.MF")) { // NOI18N
                        continue;
                    }
                    out.putNextEntry(entry);
                    while (in.available() != 0) {
                        int read = in.read(temp);
                        if (read != -1) {
                            out.write(temp, 0, read);
                        }
                    }
                    out.closeEntry();
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    private void createContributionsJar(File jarFile, String classpath) throws IOException {
        //need to create zip file
        OutputStream os = new BufferedOutputStream(new FileOutputStream(jarFile));
        try {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Name.MANIFEST_VERSION, "1.0"); // NOI18N
            manifest.getMainAttributes().put(Name.CLASS_PATH, classpath);
            JarOutputStream dest = new JarOutputStream(new BufferedOutputStream(os), manifest);
            try {
                dest.closeEntry();
                dest.finish();
            } finally {
                dest.close();
            }
        } finally {
            os.close();
        }
    }

    private void copy(File source, File dest) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(source));
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(dest));
            try {
                FileUtil.copy(is, os);
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
    }

    private String getPathToModules(File from) {
        File mwHomeFile = null;
        String mwHome = (dm != null)
                ? dm.getProductProperties().getMiddlewareHome()
                : WLProductProperties.getMiddlewareHome(serverRoot);
        if (mwHome == null) {
            if (serverRoot != null && serverRoot.getParentFile() != null) {
                mwHomeFile = serverRoot.getParentFile();
            }
        } else {
            mwHomeFile = new File(mwHome);
        }
        if (mwHomeFile != null) {
            File modules = FileUtil.normalizeFile(new File(mwHomeFile, "modules")); // NOI18N
            String relativePath = getRelativePath(from, modules);
            if (relativePath == null) {
                // FIXME forward slashes
                return modules.getAbsolutePath();
            }
            return relativePath;
        }
        // just improbable fallback :(
        return "../../../modules"; // NOI18N
    }

    // package for testing only
    static String getRelativePath(File from, File to) {
        String toPath = to.getAbsolutePath();
        String fromPath = from.getAbsolutePath();
        if (toPath.startsWith(fromPath)) {
            if (toPath.length() == fromPath.length()) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            File currentPath = to;
            while (!currentPath.equals(from)) {
                builder.insert(0, currentPath.getName());
                builder.insert(0, "/"); // NOI18N
                currentPath = currentPath.getParentFile();
            }
            return builder.substring(1);
        } else {
            File parent = from.getParentFile();
            if (parent == null) {
                return null;
            } else {
                return "../" + getRelativePath(parent, to); // NOI18N
            }
        }
    }
}
