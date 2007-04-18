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

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import java.io.File;
import java.util.List;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.netbeans.modules.compapp.javaee.util.ProjectUtil;
import org.netbeans.modules.compapp.javaee.codegen.model.JavaEEProject;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;

/**
 *
 * @author gpatil
 */
public class BuildJavaEESU extends Task{
    private static final String ANT_PROP_BUILD_DIR = "build.dir" ; // No I18N
    private static final String ANT_PROJ_BASE_DIR = "basedir" ; // No I18N
    
    private String subprojJar = null;
    private String subprojDir = null;
    private String subprojResource = null;
    private String projName = null;
    private String suExtractDir = null;
    private String caBuildDir = "build" ; // No I18N    
    
    public BuildJavaEESU() {
    }    
    
    public void setProjectName(String name){
        this.projName = name;
    }
    
    public void setSubprojJar(String s){
        this.subprojJar = s;
    }
    
    public void setSubprojDir(String d){
        this.subprojDir = d;
    }
    
    public void setSuExtractDir(String xDir){
        this.suExtractDir = xDir;
    }
    
    public void setSubprojResource(String resource) {
        this.subprojResource = resource;
    }
    
    private void createDir(String bDir){
        File buildDir = new File(bDir);
        if (!buildDir.exists()){
            buildDir.mkdir();
        }
    }
    
    public void execute() throws BuildException{
        log("Starting build on JavaEE Project:" + this.subprojDir);
        Project p = null;
        String projPath = null;
        String confDir = null;
        String buildDir = null;
        String jbiExtractDir = null;
        Object obj = null;
        JavaEEProject javaeeProj = null;
        String instrumentedJarPath = null;
        String jarName = null;
        
        try {
            p = this.getProject();
            
            projPath = p.getProperty(ANT_PROJ_BASE_DIR) + File.separator;
            
            if (p.getProperty(JbiProjectProperties.META_INF) != null) {        
                confDir = p.getProperty((JbiProjectProperties.META_INF));
            }
            
            if (p.getProperty(ANT_PROP_BUILD_DIR) != null) {        
                caBuildDir = p.getProperty(ANT_PROP_BUILD_DIR);   
            }

            // Convert project relative path to absolute path, as "pwd" may not be the project directory. 
            if ((this.subprojJar != null) && (this.subprojJar.startsWith(".."))){
                this.subprojJar = projPath + this.subprojJar;
            }
            
            if ((this.subprojResource != null) && (this.subprojResource.startsWith(".."))) {
                this.subprojResource = projPath + this.subprojResource;
            }
            
            javaeeProj = ProjectUtil.getJavaEEProject(this.projName, this.subprojJar, 
                    (projPath + File.separator + confDir + File.separator), this.subprojResource);
            buildDir = projPath + File.separator + caBuildDir;
            jbiExtractDir = projPath + File.separator + this.suExtractDir;
            createDir(buildDir);
            createDir(jbiExtractDir);
            instrumentedJarPath = javaeeProj.createJar(buildDir, jbiExtractDir);
            //jarName = javaeeProj.getJarName();
        } catch (Exception ex){
            ex.printStackTrace();
            throw new BuildException(ex);
        }
    }    
}
