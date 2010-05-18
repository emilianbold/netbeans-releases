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

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.netbeans.modules.compapp.javaee.codegen.model.Endpoint;
import org.netbeans.modules.compapp.javaee.codegen.model.EndpointCfg;
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
            
            String cofigDirPath = projPath + File.separator + confDir + File.separator;
            File configDir = new File(cofigDirPath);
            
            javaeeProj = ProjectUtil.getJavaEEProject(this.projName, this.subprojJar, 
                    cofigDirPath, this.subprojResource);
            buildDir = projPath + File.separator + caBuildDir;
            jbiExtractDir = projPath + File.separator + this.suExtractDir;
            createDir(buildDir);
            createDir(jbiExtractDir);
            
            List<EndpointCfg> cfgs = ProjectUtil.getEndpointCfgs(configDir, projName);
            javaeeProj.setEndpointOverrides(cfgs);
            
            instrumentedJarPath = javaeeProj.createJar(buildDir, jbiExtractDir);
            
            List<Endpoint> epts = javaeeProj.getEndpoints();
            List<EndpointCfg> eptCfgs = javaeeProj.getEndpointOverrides();
            boolean newEPAdded = false;
            EndpointCfg epCfg = null;
            if (eptCfgs == null){
                eptCfgs = new ArrayList<EndpointCfg>();
            }
            if (epts != null){
                for (Endpoint ep: epts){
                    if (!eptCfgs.contains(ep)){
                        newEPAdded = true;
                        epCfg = new EndpointCfg();
                        
                        epCfg.setEndPointName(ep.getEndPointName());
                        epCfg.setEndPointType(ep.getEndPointType());
                        epCfg.setInterfaceName(ep.getInterfaceName());
                        epCfg.setServiceName(ep.getServiceName());
                        
                        eptCfgs.add(epCfg);
                    }
                }
            }
            
            if (newEPAdded){
                ProjectUtil.saveEndpointCfgs(configDir, projName, eptCfgs);
            }
            //jarName = javaeeProj.getJarName();
        } catch (Exception ex){
            ex.printStackTrace();
            throw new BuildException(ex);
        }
    }    
}
