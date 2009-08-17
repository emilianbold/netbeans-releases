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
