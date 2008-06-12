/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.j2seplatform.platformdefinition.PlatformConvertor;
import org.netbeans.modules.java.j2seplatform.wizard.NewJ2SEPlatform;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public class JavaPlatformSupport {
    
    //private JavaPlatform[] nbPlfs; // All netbeans platforms
    private List<JavaPlatform> justCreatedPlatforms = new ArrayList<JavaPlatform>(); // platforms created during import
    private File defaultNetBeansPlatformFile = null; // NetBeans default platform directory

    private static JavaPlatformSupport inst;
    
    private JavaPlatformSupport() {
        //nbPlfs = JavaPlatformManager.getDefault().getInstalledPlatforms();
        JavaPlatform defPlf = JavaPlatformManager.getDefault().getDefaultPlatform();
        Collection installFolder = defPlf.getInstallFolders();
        if (!installFolder.isEmpty()) {
            defaultNetBeansPlatformFile = FileUtil.toFile((FileObject) installFolder.toArray()[0]);
        }
    }
    
    public static synchronized JavaPlatformSupport getJavaPlatformSupport() {
        if (inst == null) {
            inst = new JavaPlatformSupport();
        }
        return inst;
    }
    
    private List<JavaPlatform> getAllPlatforms() {
        List<JavaPlatform> all = new ArrayList<JavaPlatform>(justCreatedPlatforms);
        all.addAll(Arrays.<JavaPlatform>asList(JavaPlatformManager.getDefault().getInstalledPlatforms()));
        return all;
    }
    
    /** 
     * Returns and if necessary creates JavaPlatform of the given Eclipse project.
     * @return null for default platform
     */
    public JavaPlatform getJavaPlatform(EclipseProject eclProject, List<String> importProblems) {
        String eclPlfDir = eclProject.getJDKDirectory();
        // eclPlfDir can be null in a case when a JDK was set for an eclipse
        // project in Eclipse then the directory with JDK was deleted from
        // filesystem and then a project is imported into NetBeans
        if (eclPlfDir == null) {
            return null;
        }
        File eclPlfFile = FileUtil.normalizeFile(new File(eclPlfDir));
        if (defaultNetBeansPlatformFile != null && eclPlfFile.equals(defaultNetBeansPlatformFile)) { // use default platform
            return null;
        }
        JavaPlatform nbPlf = null;
        for (JavaPlatform current : getAllPlatforms()) {
            Collection instFolders = current.getInstallFolders();
            if (instFolders.isEmpty()) {
                // ignore
                continue;
            }
            File nbPlfDir = FileUtil.toFile((FileObject) instFolders.toArray()[0]);
            if (nbPlfDir.equals(eclPlfFile)) {
                nbPlf = current;
                // found
                break;
            }
        }
        if (nbPlf != null) {
            return nbPlf;
        }
        // If we are not able to find any platform let's use the "broken
        // platform" which can be easily added by user with "Resolve Reference
        // Problems" feature. Such behaviour is much better then using a default
        // platform when user imports more projects.
        FileObject fo = FileUtil.toFileObject(eclPlfFile);
        if (fo != null) {
            try {
                NewJ2SEPlatform plat = NewJ2SEPlatform.create(fo);
                plat.run();
                if (plat.isValid()) {
                    if (plat.findTool("javac") != null) {
                        //NOI18N
                        String displayName = createPlatformDisplayName(plat);
                        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(displayName, null);
                        if (platforms.length > 0) {
                            return platforms[0];
                        }
                        String antName = createPlatformAntName(displayName);
                        plat.setDisplayName(displayName);
                        plat.setAntName(antName);
                        FileObject platformsFolder = Repository.getDefault().getDefaultFileSystem().findResource("Services/Platforms/org-netbeans-api-java-Platform"); //NOI18N
                        assert platformsFolder != null;
                        DataObject dobj = PlatformConvertor.create(plat, DataFolder.findFolder(platformsFolder), antName);
                        nbPlf = (JavaPlatform) dobj.getNodeDelegate().getLookup().
                            lookup(JavaPlatform.class);
                        justCreatedPlatforms.add(nbPlf);
                    } else {
                        importProblems.add(NbBundle.getMessage(Importer.class, "MSG_JRECannotBeUsed", eclProject.getName()));
                    }
                } else {
                    importProblems.add("Cannot create J2SE platform for '" + eclPlfFile + "'. " + "Default platform will be used instead."); // NOI18N
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        } else {
            importProblems.add(NbBundle.getMessage(Importer.class, "MSG_JDKDoesnExistUseDefault", // NOI18N
                    eclProject.getName(), eclPlfFile.getAbsolutePath()));
        }
        return nbPlf;
    }
    
    private String createPlatformDisplayName(JavaPlatform plat) {
        Map<String, String> m = plat.getSystemProperties();
        String vmVersion = m.get("java.specification.version");        //NOI18N
        StringBuffer displayName = new StringBuffer("JDK ");
        if (vmVersion != null) {
            displayName.append(vmVersion);
        }
        return displayName.toString();
    }
    
    private String createPlatformAntName(String displayName) {
        assert displayName != null && displayName.length() > 0;
        String antName = PropertyUtils.getUsablePropertyName(displayName);
        if (platformExists(antName)) {
            String baseName = antName;
            int index = 1;
            antName = baseName + Integer.toString(index);
            while (platformExists(antName)) {
                index ++;
                antName = baseName + Integer.toString(index);
            }
        }
        return antName;
    }
    
    /**
     * Checks if the platform of given antName is already installed
     */
    private boolean platformExists(String antName) {
        assert antName != null && antName.length() > 0;
        for (JavaPlatform p : getAllPlatforms()) {
            String otherName = (String) p.getProperties().get("platform.ant.name");  //NOI18N
            if (antName.equals(otherName)) {
                return true;
            }
        }
        return false;
    }
}
