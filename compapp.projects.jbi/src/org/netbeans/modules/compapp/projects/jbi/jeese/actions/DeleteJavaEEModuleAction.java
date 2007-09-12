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
 * DeleteJavaEEModuleAction.java
 *
 * Created on October 18, 2006, 9:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.projects.jbi.jeese.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.compapp.javaee.util.ProjectUtil;
import org.netbeans.modules.compapp.javaee.sunresources.SunResourcesUtil;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.DeleteModuleAction;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;
import org.openide.util.NbBundle;

/**
 * Action to delete JavaEE module/project.
 *
 */
public class DeleteJavaEEModuleAction extends DeleteModuleAction{
    private String name = "Delete Java EE Module" ;
    private final static String NAME = "nameDeleteAction" ; // No I18N
    private static final String BUILD_DIR = "build" ; // No I18N   
    
    public DeleteJavaEEModuleAction() {
        init();
    }
    
    private void init() {
        ResourceBundle rb = NbBundle.getBundle(this.getClass());
        name = rb.getString(NAME);
    }

    protected void deleteModuleProperties(Project jbiProject, VisualClassPathItem vcpi, String artifactName){
        String baseDir = ProjectUtil.getProjectBaseDir(jbiProject);
        String buildDir = baseDir + File.separator + BUILD_DIR + File.separator;                
        String projName = vcpi.getProjectName();   
        deleteFile(buildDir + artifactName);
        
    }
    
    protected void updateModuleProperties(Project jbiProject, JbiProjectProperties projProp, List<VisualClassPathItem> subprojJars, String subProjName){
        if (subprojJars != null){
            List<VisualClassPathItem> javaeeList = new ArrayList<VisualClassPathItem>();
            VisualClassPathItem vcpi = null;
            Iterator <VisualClassPathItem> itr = subprojJars.iterator();
            while (itr.hasNext()){
                vcpi = itr.next();
                if ((vcpi.getObject() instanceof AntArtifact) 
                    && (VisualClassPathItem.isJavaEEProjectAntArtifact((AntArtifact) vcpi.getObject()))){
                    javaeeList.add(vcpi);
                }
            }
            
            projProp.put(JbiProjectProperties.JBI_JAVAEE_JARS, javaeeList);
        }
        
        SunResourcesUtil.removeJavaEEResourceMetaData(jbiProject, subProjName);
    }
    
    
    private void deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            file.delete();
        } catch (Exception ex){
            // Log
        }
    }
    
    public String getName() {
        return name;
    }
}
