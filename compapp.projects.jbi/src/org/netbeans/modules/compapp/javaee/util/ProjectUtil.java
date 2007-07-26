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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.javaee.util;
/*
 * ProjectUtil.java
 *
 * Created on October 18, 2006, 12:13 AM
 */

import org.netbeans.modules.compapp.javaee.codegen.model.JavaEEProject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author gpatil
 */
public class ProjectUtil {
    public static final String DEPLOY_THRU_CA = ".deployThruCa" ; // No I18N   
    
    private static final String DEFAULT_SRC_PATH = "src/java"; // No I18N
    private static final String PROJECT_PROP_FILE = "nbproject/project.properties"; // No I18N
    private static final String JAVA_EE_CONFIG_FILE = "javaee_config.properties"; // No I18N
    private static final String PROP_JAR_NAME = "jar.name"; // EJB and EAR - No I18N
    private static final String PROP_WAR_NAME = "war.name"; // WAR - No I18N
    private static final String PROP_DIST_DIR = "dist.dir"; // No I18N
    
    private static final String ENT_PROJ = "earproject" ;    // No I18N
    private static final String EJB_PROJ = "ejbjarproject" ; // No I18N
    private static final String WEB_PROJ = "web.project" ;   // No I18N
    private static final Logger log = Logger.getLogger(ProjectUtil.class.getName());
    
    private  ProjectUtil() {}
    
    public static Project getProject(String baseDir) throws IOException {
        Project proj = null;
        
        File projFolder = new File(baseDir);
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(projFolder));
        try {
            proj = ProjectManager.getDefault().findProject(fo);
        } catch (IllegalArgumentException ex){
            //log.warning("Unable to get Netbeans Project object for: " + baseDir);
        }
        return proj;
    }
    
    public static String  getProjectBaseDir(Project proj) {
        File projDirFile = FileUtil.toFile(proj.getProjectDirectory());
        return projDirFile.getPath();
    }
    
    public static String getProjectJarName(Project proj) throws IOException, FileNotFoundException {
        String jName = null;
        String srcPath = proj.getProjectDirectory().getPath() + File.separator + PROJECT_PROP_FILE;
        Properties prop = new Properties();
        
        FileInputStream fis = new FileInputStream(srcPath);
        try {
            prop.load(fis);
            jName = prop.getProperty(PROP_JAR_NAME);
            if (jName == null){
                jName = prop.getProperty(PROP_WAR_NAME);
            }
        } finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        return jName;
    }
    
    public static String getProjectJarPath(Project proj)  throws IOException, FileNotFoundException {
        String jName = null;
        String distDir = null;
        String projDir = proj.getProjectDirectory().getPath();
        String srcPath = projDir + File.separator + PROJECT_PROP_FILE;
        Properties prop = new Properties();
        FileInputStream fis = new FileInputStream(srcPath);
        try {
            prop.load(fis);
        } finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (Exception ex){
                    // Ignore
                }
            }
        }
        
        jName = prop.getProperty(PROP_JAR_NAME);
        if (jName == null){
            jName = prop.getProperty(PROP_WAR_NAME);
        }
        
        distDir = prop.getProperty(PROP_DIST_DIR);
        StringBuffer sb = new StringBuffer(projDir);
        sb.append(File.separator);
        sb.append(distDir);
        sb.append(File.separator);
        sb.append(jName);
        return sb.toString();
    }
    
    public static Set getSubprojects(Project proj){
        Set ret = null;
        SubprojectProvider sp = proj.getLookup().lookup(SubprojectProvider.class);
        if (sp != null){
            ret = sp.getSubprojects();
        } else {
            ret = new HashSet();
        }
        return ret;
    }
    
    public static Set getSubprojects(String projBaseDir) throws IOException{
        Set ret = null;
        Project proj = null;
        
        proj = getProject(projBaseDir);
        
        if (proj != null){
            SubprojectProvider sp = proj.getLookup().lookup(
                    SubprojectProvider.class);
            if (sp != null){
                ret = sp.getSubprojects();
            }
        } else {
            ret = new HashSet();
        }
        return ret;
    }
    
    public static List<String> getSubprojectsBaseDir(String projBaseDir) throws IOException{
        List<String> ret = new ArrayList<String>();
        Project proj = null;
        Set sprjs = null;
        SubprojectProvider sp = null;
        
        proj = getProject(projBaseDir);
        
        if (proj != null){
            sp = proj.getLookup().lookup(SubprojectProvider.class);
            if (sp != null){
                sprjs = sp.getSubprojects();
                Iterator itr = sprjs.iterator();
                while (itr.hasNext()){
                    Project sprj = (Project) itr.next();
                    if (isJavaEEProject(sprj)){
                        String baseDir = sprj.getProjectDirectory().getPath();
                        ret.add(baseDir);
                    }
                }
            }
        }
        return ret;
    }
    
    public static List<JavaEEProject> getSubJavaEEProjects(String projBaseDir) throws IOException{
        List<JavaEEProject> subProjects = new ArrayList<JavaEEProject>();
        Set subPrjs = null;
        Set nestedSubPrjs = null;
        Iterator itr = null;
        Iterator nestedItr = null;
        Project proj = null;
        Project sprj = null;
        ProjectInformation sProjInfo = null;
        String sProjName = null;
        String sProjDepVal = null;
        Project grandChildProject = null;
        JavaEEProject jProj = null;
        JavaEEProject javaeeGrandChild = null;
        String jarPath = null;
        boolean deployThruCA = true;
        SubprojectProvider sp = null;
        SubprojectProvider nestedSPProvider =  null;
        
        proj = getProject(projBaseDir);
        
        if (proj != null){
            sp = proj.getLookup().lookup(SubprojectProvider.class);
            
            JbiProject jbiProject = (JbiProject)  proj ; // proj.getLookup().lookup(JbiProject.class);
            Properties javaeeProjsProp = getJavaEECustomProperty(jbiProject);
            
            if (sp != null){
                subPrjs = sp.getSubprojects();
                itr = subPrjs.iterator();
                
                while (itr.hasNext()){
                    sprj = (Project) itr.next();
                    if (isJavaEEProject(sprj)){
                        deployThruCA = true;
                        sProjInfo = sprj.getLookup().lookup(ProjectInformation.class);
                        
                        if (sProjInfo != null){
                            sProjName = sProjInfo.getName();
                            sProjDepVal = javaeeProjsProp.getProperty(sProjName);
                            
                            if (sProjDepVal != null){
                                deployThruCA = Boolean.valueOf(sProjDepVal);
                            }
                        }
                        
                        jarPath = getProjectJarPath(sprj);
                        jProj = JavaEEProjectFactory.getProject(jarPath);
                        jProj.isDeployThruCA(deployThruCA);
                        subProjects.add(jProj);
                        
                        // Check whether we have sub projects for each of these...
                        nestedSPProvider =  sprj.getLookup().lookup(SubprojectProvider.class);
                        if (nestedSPProvider != null){
                            nestedSubPrjs = nestedSPProvider.getSubprojects();
                            nestedItr = nestedSubPrjs.iterator();
                            
                            while (nestedItr.hasNext()){
                                grandChildProject = (Project) nestedItr.next();
                                javaeeGrandChild = JavaEEProjectFactory.getProject(getProjectJarPath(grandChildProject));
                                jProj.addSubproject(javaeeGrandChild);
                            }
                            
                        }
                    }
                }
            }
        }
        
        return subProjects;
    }
    
    
    public static JavaEEProject getJavaEEProject(String projectName, String jarPath, String pathToConfigFolder, String resourceFolder){
        JavaEEProject proj = JavaEEProjectFactory.getProject(jarPath);
        Properties prop = readProperties(pathToConfigFolder + JAVA_EE_CONFIG_FILE);
        String sProjDepVal = prop.getProperty(projectName + DEPLOY_THRU_CA);
        boolean deployThruCA = true;
        if (sProjDepVal != null){
            deployThruCA = Boolean.valueOf(sProjDepVal);
        }
        proj.isDeployThruCA(deployThruCA);
        proj.setResourceFolder(resourceFolder);
        return proj;
    }
    
    public static String normalizePath(String path){
        String ret = null;
        
        if (path == null){
            return path;
        }
        ret = path.replaceAll("\\\\", "/");  // No I18N
        return ret;
    }
    
    public static List<String> getSubJavaEEJarPath(String projBaseDir, boolean normalize) throws IOException{
        List<String> subProjects = new ArrayList<String>();
        Set subPrjs = null;
        Iterator itr = null;
        Project proj = null;
        Project sprj = null;
        String jarPath = null;
        SubprojectProvider sp = null;
        
        proj = getProject(projBaseDir);
        
        if (proj != null){
            sp = proj.getLookup().lookup(SubprojectProvider.class);
            
            if (sp != null){
                subPrjs = sp.getSubprojects();
                itr = subPrjs.iterator();
                
                while (itr.hasNext()){
                    sprj = (Project) itr.next();
                    if (isJavaEEProject(sprj)){
                        jarPath = getProjectJarPath(sprj);
                        if (normalize){
                            jarPath = normalizePath(jarPath);
                        }
                        subProjects.add(jarPath);
                    }
                }
            }
        }
        
        return subProjects;
    }
    
    public static boolean isJavaEEProject(Project proj){
        boolean ret = false;
        String projClassName = proj.getClass().getName();
        
        if (projClassName.toLowerCase().indexOf(ENT_PROJ) > -1){
            ret = true;
        } else {
            if (projClassName.toLowerCase().indexOf(EJB_PROJ) > -1){
                ret = true;
            } else {
                if (projClassName.toLowerCase().indexOf(WEB_PROJ) > -1){
                    ret = true;
                }
            }
        }
        return ret;
    }
    
    private static String getJavaEEConfigFile(JbiProjectProperties prop, FileObject projDir){
        String ret = null;
        
        List os = (List) prop.get(JbiProjectProperties.META_INF);
        if ((os == null) || (os.size() < 1)) {
            return ret;
        }
        
        File pf = FileUtil.toFile(projDir);
        String projPath = pf.getPath() + File.separator;
        
        ret  = projPath + os.get(0).toString() + File.separator + JAVA_EE_CONFIG_FILE;
        return ret;
    }
    
    public static Properties readProperties(String javaeeConfigFile) {
        Properties ret = new Properties();
        FileInputStream fis = null;
        File configFile = new File( javaeeConfigFile );
        
        try {
            if ( !configFile.exists() ) {
                configFile.createNewFile();
            }
            fis = new FileInputStream(configFile);
            ret.load( fis );
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (Exception ex){
                    // ignore
                }
            }
        }
        
        return ret;
    }
    
    private static void storeProperties(String javaeeConfigFile, Properties prop) {
        OutputStream os = null;
        File configFile = new File( javaeeConfigFile );
        
        try {
            if ( !configFile.exists() ) {
                configFile.createNewFile();
            }
            os = new FileOutputStream( configFile);
            Date date = new Date();
            prop.store(os, date.toString());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (os != null){
                try {
                    os.close();
                } catch (Exception ex){
                    // ignore
                }
            }
        }
    }
    
    public static synchronized void setJavaEECustomProperty(JbiProject compApp, String propName, String value){
        JbiProjectProperties jbiProjProp = compApp.getProjectProperties();
        String javaEEConfigFile = getJavaEEConfigFile(jbiProjProp, compApp.getProjectDirectory());
        Properties prop = readProperties(javaEEConfigFile);
        prop.setProperty(propName, value);
        storeProperties(javaEEConfigFile, prop);
    }
    
    public static synchronized Properties getJavaEECustomProperty(JbiProject compApp){
        JbiProjectProperties jbiProjProp = compApp.getProjectProperties();
        String javaEEConfigFile = getJavaEEConfigFile(jbiProjProp, compApp.getProjectDirectory());
        Properties prop = readProperties(javaEEConfigFile);
        return prop;
    }
}
