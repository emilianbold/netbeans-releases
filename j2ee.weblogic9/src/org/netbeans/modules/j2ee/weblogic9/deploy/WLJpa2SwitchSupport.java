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
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Hejl
 */
public final class WLJpa2SwitchSupport {

    private static final String OEPECONTRIBUTIONSJAR = "oepe-contributions.jar";//NO18N
    private static final String JPAJAR1 = "javax.persistence_1.0.0.0_2-0-0.jar";//NO18N
    private static final String JPAJAR2 = "com.oracle.jpa2support_1.0.0.0_2-0.jar";//NO18N
    
    private final WLDeploymentManager deploymentManager;

    public WLJpa2SwitchSupport(WLDeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    public void enable() {
        File libDir = WLPluginProperties.getServerLibDirectory(deploymentManager, true);
        File webLogicJarFile = WLPluginProperties.getWeblogicJar(deploymentManager);
        JarFile webLogicJar = null;
        try {
            webLogicJar = new JarFile(webLogicJarFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        File oepeJarFile = new File(libDir, OEPECONTRIBUTIONSJAR);
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
                if(cp.indexOf(JPAJAR1)>-1 && cp.indexOf(JPAJAR2)>-1) override = false;//should ful path be checked?
            }
            if(override){
                backup(oepeJarFile);
                oepeFO = null;
            }
        }
        if (oepeFO == null) {
            //need to create zip file
            oepeFO = createOEPEJar(oepeJarFile, "../../../modules/" + JPAJAR1 + " ../../../modules/" + JPAJAR2);//NOI18N
        }
        Manifest wlManifest = null;
        try {
            wlManifest = webLogicJar.getManifest();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        String cp = wlManifest.getMainAttributes().getValue(Name.CLASS_PATH);
        if (cp.indexOf(OEPECONTRIBUTIONSJAR) == -1) {
            wlManifest.getMainAttributes().putValue(Name.CLASS_PATH.toString(), OEPECONTRIBUTIONSJAR + " " + cp);
            replaceManifest(webLogicJarFile, wlManifest);
        }
    }

    public void disable() {
        // delete referneces to jars in oepe-contributions.jar
        File libDir = WLPluginProperties.getServerLibDirectory(deploymentManager, true);
        File oepeJarFile = new File(libDir, OEPECONTRIBUTIONSJAR);
        backup(oepeJarFile);
        createOEPEJar(oepeJarFile, "");
    }

    public boolean isEnabled() {
        return deploymentManager.getJ2eePlatformImpl().isJpa2Available();
    }

    public boolean isEnabledViaSmartUpdate() {
        // TODO check for BUG9923849_WLS103MP4.jar on Library classpath from j2eePlatformImpl
        return false;
    }

    private FileObject backup(File jarFile){
        FileObject fo = FileUtil.toFileObject(jarFile);
        String bakName = FileUtil.findFreeFileName(fo.getParent(), jarFile.getName(), "bak");//NOI18N
        try {
            return FileUtil.copyFile(fo, fo.getParent(), bakName, "bak"); //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private void replaceManifest(File webLogicJarFile, Manifest wlManifest) {
        FileObject bakJar = backup(webLogicJarFile);
        //need replace
        FileOutputStream dest = null;
        try {
            dest = new FileOutputStream(webLogicJarFile);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        FileInputStream source = null;
        try {
            source = new FileInputStream(FileUtil.toFile(bakJar));
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        JarInputStream in = null;
        try {
            in = new JarInputStream(new BufferedInputStream(source));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        JarOutputStream out = null;
        try {
            out = new JarOutputStream(new BufferedOutputStream(dest), wlManifest);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        JarEntry entry = null;
        byte[] temp = new byte[32768];
        try {
            while ((entry = in.getNextJarEntry()) != null) {
                String name = entry.getName();
                if (name.equalsIgnoreCase("META-INF/MANIFEST.MF")) {//NOI18N
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
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (dest != null) {
                dest.close();
            }
            if (source != null) {
                source.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
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
}
