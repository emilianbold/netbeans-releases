/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.soa.pojo.wizards;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.soa.pojo.util.NBPOJOConstants;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * POJO Helper class
 * @author Sreenivasan Genipudi
 */
public class POJOHelper {

    public static final String IDE_MODULE_INSTALL_NAME = "modules/org-netbeans-modules-xml-wsdl-model.jar"; // NOI18N
    public static final String SOA_MODULE_INSTALL_NAME = "modules/org-netbeans-modules-compapp-projects-jbi.jar"; // NOI18N
    public static final String IDE_MODULE_INSTALL_CBN = "org.netbeans.modules.xml.wsdl.model"; // NOI18N
    public static final String SOA_MODULE_INSTALL_CBN = "org.netbeans.modules.compapp.projects.jbi"; // NOI18N
    public static final String IDE_MODULE_INSTALL_DIR = "ide.module.install.dir"; // NOI18N
    public static final String SOA_MODULE_INSTALL_DIR = "soa.module.install.dir"; // NOI18N

    public static void setPrivateProjPros(Project prj) {
        AntProjectHelper helper = getAntProjectHelper(prj);

        EditableProperties privateEP = helper.getProperties(
                AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        privateEP.setProperty("netbeans.user", //NOI18N
                System.getProperty("netbeans.user")); // NOI18N

        InstalledFileLocator installedFileLocator = InstalledFileLocator.getDefault();
        File f = installedFileLocator.locate(SOA_MODULE_INSTALL_NAME, SOA_MODULE_INSTALL_CBN, false);
        if (f != null) {
            privateEP.setProperty(SOA_MODULE_INSTALL_DIR, f.getParentFile().getPath());
        }

        f = installedFileLocator.locate(IDE_MODULE_INSTALL_NAME, IDE_MODULE_INSTALL_CBN, false);
        if (f != null) {
            privateEP.setProperty(IDE_MODULE_INSTALL_DIR, f.getParentFile().getPath());
        }

        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateEP);

        try {
            ProjectManager.getDefault().saveProject(prj);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    public static void setProjPros(Project prj) {
        AntProjectHelper helper = getAntProjectHelper(prj);

        EditableProperties projPorps = helper.getProperties(
                AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        projPorps.setProperty(NBPOJOConstants.PROP_POJO_ENABLED, "true"); //NOI18N
        projPorps.setProperty(NBPOJOConstants.PROP_POJO_PACKAGE_ALL, "true"); //NOI18N
        projPorps.setProperty(NBPOJOConstants.PROP_POJO_PROJECT_VERSION,
                NBPOJOConstants.LATEST_POJO_PRJ_VERSION.toString());
        projPorps.setProperty(NBPOJOConstants.PROP_POJO_JAR_PACKAGE_EXCLUDES,
                NBPOJOConstants.PROP_POJO_JAR_PACKAGE_EXCLUDES_VAL);

        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projPorps);

        try {
            ProjectManager.getDefault().saveProject(prj);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * Get AntProjectHelper for the project.
     * @param project
     * @return AntProjectHelper
     */
    private static AntProjectHelper getAntProjectHelper(Project project) {
        try {
            Method getAntProjectHelperMethod = project.getClass().getMethod(
                    "getAntProjectHelper"); //NOI18N

            if (getAntProjectHelperMethod != null) {
                AntProjectHelper helper = (AntProjectHelper) getAntProjectHelperMethod.invoke(project);

                return helper;
            }
        } catch (NoSuchMethodException nme) {
            Exceptions.printStackTrace(nme);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     * Get the project property value
     * @param prj project
     * @param filePath project file path
     * @param name property name
     * @return property value. null on not found.
     */
    private static String getProperty(Project prj, String filePath,
            String name) {
        AntProjectHelper aph = getAntProjectHelper(prj);
        EditableProperties ep = aph.getProperties(filePath);
        String str = null;
        String value = ep.getProperty(name);
        if (value != null) {
            PropertyEvaluator pe = aph.getStandardPropertyEvaluator();
            str = pe.evaluate(value);
        }
        return str;
    }

    /**
     * Get project property
     * @param prj project instance
     * @param prop property name
     * @return property value
     */
    public static String getProjectProperty(Project prj, String prop) {
        return getProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, prop);
    }

    public static void removeProjectProperty(Project prj, String prop) {
        deleteProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, prop);
    }

    /**
     * Save the project property
     * @param prj project
     * @param prop property name
     * @param value property value
     */
    public static void saveProjectProperty(Project prj, String prop,
            String value) {
        saveProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, prop,
                value);
    }

    /**
     * Save the project property
     * @param prj project
     * @param filePath project file path
     * @param name property name
     * @param value property value
     */
    private static void saveProperty(Project prj, String filePath, String name,
            String value) {
        AntProjectHelper aph = getAntProjectHelper(prj);
        EditableProperties ep = aph.getProperties(filePath);
        if (value != null) {
            ep.put(name, value);
            aph.putProperties(filePath, ep);
        }
    }

    private static void deleteProperty(Project prj, String filePath, String name) {
        AntProjectHelper aph = getAntProjectHelper(prj);
        EditableProperties ep = aph.getProperties(filePath);
        if (name != null) {
            ep.remove(name);
            aph.putProperties(filePath, ep);
        }
    }

    /**
     * Writes the content from inputstream to file.
     * @param is inputstream
     * @param outputFile File
     * @return true on success and false on failure.
     */
    private static boolean writeToFile(InputStream is, File outputFile) {
        PrintWriter pw = null;
        BufferedWriter bw = null;
        try {
            pw = new PrintWriter(outputFile);
            bw = new BufferedWriter(pw);
            int c = -1;
            while ((c = is.read()) != -1) {
                bw.write((char) c);
            }
            bw.flush();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        } finally {
            try {
                pw.close();
            } catch (Exception ex) {
            }
            try {
                bw.close();
            } catch (Exception ex) {
            }
            pw = null;
            bw = null;
        }
        return true;
    }

    /**
     * Creates a new build script if not present.
     * @param project
     * @return
     */
    public static FileObject getPOJOBuildFO(Project project, boolean createIfNotPresent, boolean forceRefresh) {
        //Build script in project folder
        File pojoBuildScript = new File(FileUtil.toFile(project.getProjectDirectory()),
                NBPOJOConstants.NBPROJECT_DIR + File.separator + NBPOJOConstants.POJO_BUILD_FILE_NAME);

        if ((forceRefresh) || (createIfNotPresent && (!pojoBuildScript.exists())) )  {
            //Get the build xml for POJO Project
            InputStream is = POJOHelper.class.getClassLoader().getResourceAsStream(
                    NBPOJOConstants.POJO_BUILD_RESOURCE);

            if (writeToFile(is, pojoBuildScript)) {
                return FileUtil.toFileObject(pojoBuildScript);
            }
        }

        return FileUtil.toFileObject(pojoBuildScript);
    }

    private static boolean isProjectPOJOEnabled(Project prj) {
        String val = getProjectProperty(prj, NBPOJOConstants.PROP_POJO_ENABLED); //NOI18N
        return ("true".equals(val)); //NOI18N
    }

    private static void enableProjectForPOJOEngine(Project prj) {
        addPojoLibrary(prj);
        setProjPros(prj);
    }


    public static void unregisterPOJOAntExt(Project project){
        AntBuildExtender ext = project.getLookup().lookup(AntBuildExtender.class);
        if (ext != null && ext.getExtension(NBPOJOConstants.POJO_ANT_XTN_NAME) != null) {
            ext.removeExtension(NBPOJOConstants.POJO_ANT_XTN_NAME);
        }
    }

    /**
     * Also deletes the old script file.
     * 
     * @param project
     */
    public static void unregisterOldPOJOBuildScript(Project project) {
        unregisterPOJOAntExt(project);
        
        File oldPOJOBuildFile = new File(FileUtil.toFile(project.getProjectDirectory()),
                NBPOJOConstants.NBPROJECT_DIR + File.separator + NBPOJOConstants.POJO_OLD_BUILD_FILE_NAME);
        if (oldPOJOBuildFile.exists()) {
            oldPOJOBuildFile.delete();
        }
    }


    public static void registerPOJOBuildScript(Project project) {
        if ("org.netbeans.modules.java.j2seproject.J2SEProject".equals(project.getClass().getName())) {//NOI18N
            //Get AntExtender
            AntBuildExtender ext = project.getLookup().lookup(AntBuildExtender.class);
            if (ext != null && ext.getExtension(NBPOJOConstants.POJO_ANT_XTN_NAME) == null) {
                if (!POJOHelper.isProjectPOJOEnabled(project)) {
                    POJOHelper.enableProjectForPOJOEngine(project);
                }

                FileObject buildXMLFileObject = getPOJOBuildFO(project, true, false);

                try {
                    AntBuildExtender.Extension pojoBuild = ext.addExtension(
                            NBPOJOConstants.POJO_ANT_XTN_NAME, buildXMLFileObject);
                    pojoBuild.addDependency(NBPOJOConstants.POJO_COMPILE_TARGET_DEPENDS,
                            NBPOJOConstants.POJO_COMPILE_TARGET);
                } catch (Exception ex1) {
                    Exceptions.printStackTrace(ex1);
                }
                try {
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                getPOJOBuildFO(project, true, false);
            }
        }
    }

    private static void addPojoLibrary(Project prj) {
        SourceGroup[] sgs = ProjectUtils.getSources(prj).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath compileClassPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.COMPILE);
        ClassPath bootClassPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.BOOT);
        ClassPath classPath = ClassPathSupport.createProxyClassPath(
                new ClassPath[]{compileClassPath, bootClassPath});
        Library pojoLib = LibraryManager.getDefault().getLibrary(
                NBPOJOConstants.POJO_LIB_NAME);
        Sources srcs = ProjectUtils.getSources(prj);
        if (srcs != null) {
            SourceGroup[] srg = srcs.getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA);
            if ((srg != null) && (srg.length > 0)) {
                try {
                    ProjectClassPathModifier.addLibraries(
                            new Library[]{pojoLib}, srg[0].getRootFolder(),
                            ClassPath.COMPILE);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
