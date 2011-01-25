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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Hejl
 */
public final class WLJpa2SwitchSupport {

    private static final String OEPE_CONTRIBUTIONS_JAR = "oepe-contributions.jar";//NO18N
    
    private static final String JPA_JAR_1 = "javax.persistence_1.0.0.0_2-0-0.jar";//NO18N
    
    private static final String JPA_JAR_2 = "com.oracle.jpa2support_1.0.0.0_2-0.jar";//NO18N
    
    private final WLDeploymentManager deploymentManager;

    public WLJpa2SwitchSupport(WLDeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    public void enable() {
        File libDir = WLPluginProperties.getServerLibDirectory(deploymentManager, true);
        if (libDir != null) {
            libDir = FileUtil.normalizeFile(libDir);
        }
        File webLogicJarFile = WLPluginProperties.getWeblogicJar(deploymentManager);
        JarFile webLogicJar = null;
        try {
            webLogicJar = new JarFile(webLogicJarFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        File oepeJarFile = new File(libDir, OEPE_CONTRIBUTIONS_JAR);
        FileObject oepeFO = FileUtil.toFileObject(oepeJarFile);
        JarFile oepeJar = null;
        if (oepeFO != null) {
            //check if paths are correct
            try {
                oepeJar = new JarFile(oepeJarFile);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            Manifest manifest = null;
            try {
                manifest = oepeJar.getManifest();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            boolean override = true;
            if(manifest != null){
                String cp = manifest.getMainAttributes().getValue(Name.CLASS_PATH);
                if (cp != null) {
                    if (cp.indexOf(JPA_JAR_1) > -1 && cp.indexOf(JPA_JAR_2) > -1) {
                        override = false;
                    }//should ful path be checked?
                }
            }
            if(override){
                try {
                    backup(oepeJarFile);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                oepeFO = null;
            }
        }
        if (oepeFO == null) {
            //need to create zip file
            String path = getPathToModules(libDir);
            if (path.length() > 0) {
                path = path + "/"; // NOI18N
            }
            oepeFO = createOEPEJar(oepeJarFile, path + JPA_JAR_1 + " " // NOI18N
                    + path + JPA_JAR_2);//NOI18N
        }
        try {
            Manifest wlManifest = webLogicJar.getManifest();
            String cp = wlManifest.getMainAttributes().getValue(Name.CLASS_PATH);
            if (cp.indexOf(OEPE_CONTRIBUTIONS_JAR) == -1) {
                wlManifest.getMainAttributes().putValue(Name.CLASS_PATH.toString(), OEPE_CONTRIBUTIONS_JAR + " " + cp);
                replaceManifest(webLogicJarFile, wlManifest);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            deploymentManager.getJ2eePlatformImpl().notifyLibrariesChange();
        }       
    }

    public void disable() {
        try {
            File libDir = WLPluginProperties.getServerLibDirectory(deploymentManager, true);
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
            Exceptions.printStackTrace(ex);
        } finally {
            deploymentManager.getJ2eePlatformImpl().notifyLibrariesChange();
        }
    }

    public boolean isEnabled() {
        return deploymentManager.getJ2eePlatformImpl().isJpa2Available();
    }

    public boolean isEnabledViaSmartUpdate() {
        // TODO check for BUG9923849_WLS103MP4.jar on Library classpath from j2eePlatformImpl
        return false;
    }

    private FileObject backup(File jarFile) throws IOException {
        FileObject fo = FileUtil.toFileObject(jarFile);
        String bakName = FileUtil.findFreeFileName(fo.getParent(),
                jarFile.getName(), "bak"); // NOI18N
        return FileUtil.copyFile(fo, fo.getParent(), bakName, "bak"); // NOI18N
    }

    private void replaceManifest(File jarFile, Manifest manifest) throws IOException {
        FileObject bakJar = backup(jarFile);
        try {
            InputStream is = new BufferedInputStream(
                    new FileInputStream(FileUtil.toFile(bakJar)));
            try {
                OutputStream os = new BufferedOutputStream(
                        new FileOutputStream(jarFile));
                try {
                    replaceManifest(is, os, manifest);
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
        } finally {
            bakJar.delete();
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
    
    private FileObject createOEPEJar(File oepeJarFile, String classpath)
            {
            //need to create zip file
            FileOutputStream dest = null;
            try {
                dest = new FileOutputStream(oepeJarFile);
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().putValue(Name.MANIFEST_VERSION.toString(), "1.0");
            manifest.getMainAttributes().putValue(Name.CLASS_PATH.toString(), classpath);//NOI18N
            JarOutputStream out = null;
            try {
                out = new JarOutputStream(new BufferedOutputStream(dest), manifest);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        try {
            out.closeEntry();
            out.finish();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            if (out != null) {
                out.close();
            }
            if (dest != null) {
                dest.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return FileUtil.toFileObject(oepeJarFile);
    }
    
    private String getPathToModules(File from) {
        File mwHomeFile = null;
        String mwHome = deploymentManager.getProductProperties().getMiddlewareHome();
        if (mwHome == null) {
            File root = WLPluginProperties.getServerRoot(deploymentManager, false);
            if (root != null && root.getParentFile() != null) {
                mwHomeFile = root.getParentFile();
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
