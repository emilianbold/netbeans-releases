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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.common.runtime;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Map;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.w3c.dom.Element;


public class J2SEProjectType {
    
    private static final String J2SE_PROJECT_NAMESPACE_40 = "http://www.netbeans.org/ns/j2se-project/1";// NOI18N
    private static final String J2SE_PROJECT_NAMESPACE_41 = "http://www.netbeans.org/ns/j2se-project/2";// NOI18N
    private static final String J2SE_PROJECT_NAMESPACE_50 = "http://www.netbeans.org/ns/j2se-project/3";// NOI18N
    
    private static SpecificationVersion JDK15Version = new SpecificationVersion("1.5");// NOI18N
    
    public static boolean isProjectTypeSupported(Project project) {
        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
        Element e = aux.getConfigurationFragment("data", J2SE_PROJECT_NAMESPACE_50, true);// NOI18N
        if(e == null) {
            e = aux.getConfigurationFragment("data", J2SE_PROJECT_NAMESPACE_40, true);// NOI18N
            if (e== null) {
                e = aux.getConfigurationFragment("data", J2SE_PROJECT_NAMESPACE_41, true); // NOI18N
            }
        }
        return (e != null);
    }
    
    public static boolean isPlatformGreaterThanJDK15(Project project) {
        Properties projectProperties = getProjectProperties(project);
        JavaPlatform platform = null;
        String platformName = projectProperties.getProperty("platform.active");// NOI18N
        
        if(platformName == null || platformName.equals("default_platform"))// NOI18N
            platform = JavaPlatformManager.getDefault().getDefaultPlatform();
        else {
            JavaPlatform[] installedPlatforms =
                    JavaPlatformManager.getDefault().getPlatforms(null,
                    new Specification("j2se",null));   //NOI18N
            for (int i=0; i<installedPlatforms.length; i++) {
                String antName = installedPlatforms[i].getProperties().get("platform.ant.name"); //NOI18N
                if (antName != null && antName.equals(platformName)) {
                    platform = installedPlatforms[i];
                }
            }
        }
        return JDK15Version.compareTo(platform.getSpecification().getVersion()) < 0;
    }
    
    public static void overwriteProperty(Project project, final String key, final String value) throws Exception {
        FileObject privatePropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        final File projectPropsFile = FileUtil.toFile(project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH));
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws Exception {
                java.util.Properties p = new java.util.Properties();
                
                FileInputStream fis = new FileInputStream(projectPropsFile);
                try {
                    p.load(fis);
                    p.setProperty(key, value);
                }finally{
                    fis.close();
                }
                FileOutputStream fos = new FileOutputStream(projectPropsFile);
                try {
                    p.store(fos,null);
                }finally{
                    fos.close();
                }
                return null;
            }
        });
    }
    
    public static void addProjectProperties(final Map properties, Project project) throws MutexException {
        FileObject privatePropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        final File projectPropsFile = FileUtil.toFile(project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH));
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws Exception {
                java.util.Properties p = new java.util.Properties();
                
                FileInputStream fis = new FileInputStream(projectPropsFile);
                try {
                    p.load(fis);
                    p.putAll(properties);
                }finally{
                    fis.close();
                }
                FileOutputStream fos = new FileOutputStream(projectPropsFile);
                try {
                    p.store(fos,null);
                }finally{
                    fos.close();
                }
                return null;
            }
        });
    }
    
    public static Properties getProjectProperties(Project project) {
        Properties props = new Properties();
        FileObject privatePropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        FileObject projectPropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        File userPropsFile = InstalledFileLocator.getDefault().locate("build.properties", null, false);// NOI18N
        
        // the order is 1. private, 2. project, 3. user to reflect how Ant handles property definitions (immutable, once set property value cannot be changed)
        if (privatePropsFile != null) {
            try {
                InputStream is = privatePropsFile.getInputStream();
                try {
                    props.load(is);
                } finally {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        if (projectPropsFile != null) {
            try {
                InputStream is = projectPropsFile.getInputStream();
                try {
                    props.load(is);
                } finally {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        if (userPropsFile != null) {
            try {
                InputStream is = new BufferedInputStream(new FileInputStream(userPropsFile));
                try {
                    props.load(is);
                } finally {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return props;
    }
}
