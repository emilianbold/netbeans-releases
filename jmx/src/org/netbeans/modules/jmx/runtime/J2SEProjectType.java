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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.runtime;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;

import org.netbeans.spi.project.AuxiliaryConfiguration;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;

import org.openide.util.Mutex;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.MutexException;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

import org.openide.util.NbBundle;

import java.io.*;
import java.util.Properties;

import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.jmx.j2seproject.customizer.MonitoringPanel;
import org.openide.modules.SpecificationVersion;

public class J2SEProjectType {
    
    private static final String J2SE_PROJECT_NAMESPACE_40 = "http://www.netbeans.org/ns/j2se-project/1";// NOI18N
    private static final String J2SE_PROJECT_NAMESPACE_41 = "http://www.netbeans.org/ns/j2se-project/2";// NOI18N
    private static final String J2SE_PROJECT_NAMESPACE_50 = "http://www.netbeans.org/ns/j2se-project/3";// NOI18N
    
    private static final String STANDARD_IMPORT_STRING = "<import file=\"nbproject/build-impl.xml\"/>";// NOI18N
    private static final String MANAGEMENT_IMPORT_STRING = "<import file=\"nbproject/management-build-impl.xml\"/>";// NOI18N
    private static final String MANAGEMENT_NAME_SPACE = "http://www.netbeans.org/ns/jmx/1";// NOI18N
    private static SpecificationVersion JDK15Version = new SpecificationVersion("1.5");
    public static boolean isProjectTypeSupported(Project project) {
        AuxiliaryConfiguration aux = (AuxiliaryConfiguration) project.getLookup().lookup(AuxiliaryConfiguration.class);
        if (aux == null) {
            System.err.println("Auxiliary Configuration is null for Project: " + project);// NOI18N
            return false;
        }
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
                String antName = (String)
                        installedPlatforms[i].getProperties().get("platform.ant.name"); //NOI18N
                if (antName != null && antName.equals(platformName)) {
                    platform = installedPlatforms[i];
                }
            }
        }
        return JDK15Version.compareTo(platform.getSpecification().getVersion()) < 0;
    }
    
    public static boolean checkProjectCanBeManaged(Project project) {
        Properties pp = getProjectProperties(project);
        String mainClass = pp.getProperty("main.class");// NOI18N
        boolean res = false;
        if (mainClass != null && !"".equals(mainClass)) {// NOI18N
            FileObject fo = findFileForClass(mainClass, true);
            if (fo != null) res = true;
        }
        if (!res) {
            ManagementDialogs.getDefault().notify(
                    new NotifyDescriptor.Message(NbBundle.getMessage(J2SEProjectType.class, "ERR_MainClassNotSet"), NotifyDescriptor.WARNING_MESSAGE));// NOI18N
            
            return false;
        }
        return true;
    }
    
    public static FileObject findFileForClass(String className, boolean tryInnerclasses) {
        FileObject fo = null;
        try {
            String resourceName = className.replaceAll("\\.", "/") + ".java"; //NOI18N
            GlobalPathRegistry gpr = GlobalPathRegistry.getDefault();
            Set paths = gpr.getPaths("classpath/source"); //NOI18N
            for (Iterator iterator = paths.iterator(); iterator.hasNext();) {
                ClassPath cp = (ClassPath) iterator.next();
                fo = cp.findResource(resourceName);
                if (fo != null) break;
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        if ((fo == null) && tryInnerclasses) {
            // not found - will try without last .xxx to see if the last name is not an innerclass name
            int dotIndex = className.lastIndexOf('.');
            if (dotIndex != -1)
                return findFileForClass(className.substring(0, dotIndex), true);
        }
        return fo;
    }
    
    public static boolean checkProjectIsModifiedForManagement(Project project) {
        Element e = ((AuxiliaryConfiguration) project.getLookup().lookup(AuxiliaryConfiguration.class)).getConfigurationFragment("data", MANAGEMENT_NAME_SPACE, true);// NOI18N
        if (e != null) return true; // already modified, nothing more to do
        
        if (ManagementDialogs.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(J2SEProjectType.class, "WARN_BUILD_UPDATE"), // NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION)
                ) != NotifyDescriptor.OK_OPTION) {
            return false; // cancelled by the user
        }
        
        Element mgtFragment = XMLUtil.createDocument("ignore", null, null, null).createElementNS(MANAGEMENT_NAME_SPACE, "data");// NOI18N
        mgtFragment.setAttribute("version", "0.4");// NOI18N
        ((AuxiliaryConfiguration) project.getLookup().lookup(AuxiliaryConfiguration.class)).putConfigurationFragment(mgtFragment, true);
        try {
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException e1) {
            e1.printStackTrace(System.err);
            return false;
        }
        
        try {
            GeneratedFilesHelper gfh = new GeneratedFilesHelper(project.getProjectDirectory());
            gfh.refreshBuildScript("nbproject/management-build-impl.xml", J2SEProjectType.class.getResource("management-build-impl.xsl"), false);// NOI18N
        } catch (IOException e1) {
            return false;
        }
        
        String buildScript = ProjectUtilities.getProjectBuildScript(project);
        
        if (buildScript == null) {
            ManagementDialogs.getDefault().notify(
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage(J2SEProjectType.class, "ERR_BUILD_NOT_FOUND"), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE)
                    );
            return false;
        }
        
        if (!ProjectUtilities.backupBuildScript(project)) {
            if (ManagementDialogs.getDefault().notify(
                    new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(J2SEProjectType.class, "ERR_BUILD_NOT_BACKUP"), // NOI18N
                    NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE)
                    ) != NotifyDescriptor.OK_OPTION) {
                return false; // cancelled by the user
            }
        }
        
        StringBuffer newDataBuffer = new StringBuffer(buildScript.length() + 200);
        int importIndex = buildScript.indexOf(STANDARD_IMPORT_STRING);
        if (importIndex == -1) {
            // notify the user that the build script cannot be modified, and he should perform the change himself
            ManagementDialogs.getDefault().notify(
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage(J2SEProjectType.class, "ERR_BUILD_NOT_UPDATED"), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE)
                    );
            return false;
        }
        String indent = "";// NOI18N
        int idx = importIndex-1;
        while (idx >= 0) {
            if (buildScript.charAt(idx) == ' ') indent = " " + indent;// NOI18N
            else if (buildScript.charAt(idx) == '\t') indent = "\t" + indent;// NOI18N
            else break;
            idx--;
        }
        newDataBuffer.append(buildScript.substring(0, importIndex+STANDARD_IMPORT_STRING.length()+1));
        newDataBuffer.append("\n");// NOI18N
        newDataBuffer.append(indent);
        newDataBuffer.append(MANAGEMENT_IMPORT_STRING);
        newDataBuffer.append(buildScript.substring(importIndex+STANDARD_IMPORT_STRING.length()+1));
        
        FileObject buildFile = project.getProjectDirectory().getFileObject("build.xml");// NOI18N
        FileLock lock = null;
        PrintWriter writer = null;
        try {
            lock = buildFile.lock();
            writer = new PrintWriter(buildFile.getOutputStream(lock));
            writer.println(newDataBuffer.toString());
            
        } catch (FileNotFoundException e1) {
            e1.printStackTrace(System.err);
        } catch (IOException e1) {
            e1.printStackTrace(System.err);
        } finally {
            lock.releaseLock();
            if (writer != null)
                writer.close();
        }
        return true;
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
