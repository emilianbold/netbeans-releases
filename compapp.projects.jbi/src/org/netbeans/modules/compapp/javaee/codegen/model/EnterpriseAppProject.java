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

/*
 * EnterpriseAppProject.java
 *
 * Created on October 6, 2006, 4:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.codegen.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.compapp.javaee.util.ProjectUtil;


/**
 *
 * @author gpatil
 */
public class EnterpriseAppProject extends AbstractProject{
    private static Logger logger = Logger.getLogger(EnterpriseAppProject.class.getName());
    private static String JAR_SEPERATOR = "!/" ; //NOI18Ns
    private static String SU_FILE_EXT = "ear" ; //NOI18N
    
    private List<JavaEEProject> subProjs = new ArrayList<JavaEEProject>();
    
    /**
     * Creates a new instance of EnterpriseAppProject
     */
    public EnterpriseAppProject(String path2eEar) {
        super(path2eEar);
        this.projType = JavaEEProject.ProjectType.ENT;
        initEarProj();
    }
    
    @Override
    public void addSubproject(JavaEEProject subProj){
        this.subProjs.add(subProj);
    }

    @Override
    protected String renameToSvcUnitExtension(String javaEEName){
        StringBuffer ret = new StringBuffer();
        int index = -1;
        if (javaEEName != null){
            index = javaEEName.lastIndexOf("."); //NOI18N
            if (index >= 0){
                ret.append(javaEEName.substring(0, index + 1));
                ret.append(SU_FILE_EXT);
            }
        }
        
        return ret.toString();
    }
    
    private void initEarProj() {
        JarFile ear = null;
        String jarUrlPrefix = null;
        JarInJarProject subProject = null;
        URL subProjUrl = null;
        try {
            File earFile = new File(this.jarPath);
            jarUrlPrefix = "jar:" + earFile.toURL().toString() + JAR_SEPERATOR ; //NOI18N
            
            ear = new JarFile(this.jarPath);
            Enumeration<JarEntry> entries = ear.entries();
            JarEntry je = null;
            while (entries.hasMoreElements()){
                je = entries.nextElement();
                if ((!je.isDirectory()) && (
                        (je.getName().endsWith(".jar")) || //NOI18N
                        (je.getName().endsWith(".war"))    //NOI18N
                        )){
                    subProjUrl = new URL(jarUrlPrefix + je.getName() );
                    subProject = new JarInJarProject(subProjUrl);
                    this.addSubproject(subProject);
                }
            }
        } catch (IOException ex){
            logger.log(Level.SEVERE, "Exception while reading EAR archive.", ex);
        } finally {
            if (ear != null){
                ProjectUtil.close(ear);
            }
        }
    }
    
    @Override
    public List<Endpoint> getWebservicesEndpoints() throws IOException {
        Iterator<JavaEEProject> itr = this.subProjs.iterator();
        JavaEEProject subp = null;
        List<Endpoint> epts = new ArrayList<Endpoint>();
        while (itr.hasNext()){
            subp = itr.next();
            epts.addAll(subp.getWebservicesEndpoints());
        }
        
        return epts;
    }
}
