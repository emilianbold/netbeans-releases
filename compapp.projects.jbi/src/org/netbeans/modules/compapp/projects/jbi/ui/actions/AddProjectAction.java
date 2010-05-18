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

package org.netbeans.modules.compapp.projects.jbi.ui.actions;

import java.util.ArrayList;
import org.netbeans.modules.compapp.javaee.sunresources.SunResourcesUtil;
import org.netbeans.modules.compapp.projects.jbi.CasaHelper;
import org.netbeans.modules.compapp.projects.jbi.ProjectPropertyProvider;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.AntArtifactChooser;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;

import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;

import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.io.IOException;

import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.compapp.projects.jbi.ComponentHelper;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.JbiSubprojectProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * DOCUMENT ME!
 *
 * @author 
 * @version 
 */
public class AddProjectAction implements ProjectActionPerformer {
    
    private List<String> javaeeAntArtifactTypes = new ArrayList<String>();
    private String descEjbProjectsOnly = "EJB projects only" ;
    private String descWebProjectsOnly = "Web projects only" ;
    private String descEarProjectsOnly = "Ear projects only" ;
    
    
    private static final String STR_EJB_PROJECT = "EjbJarProject" ; // No I18N
    private static final String STR_WEB_PROJECT = "WebProject" ; // No I18N
    private static final String STR_EAR_PROJECT = "EarProject" ; // No I18N
    
    private static final String EJB_PROJ_DESC = "descEjbProject" ; // No I18N
    private static final String WEB_PROJ_DESC = "descWebProject" ; // No I18N
    private static final String EAR_PROJ_DESC = "descEarProject" ; // No I18N
    
    // whether the SU project to be added is internal or external
    private boolean internal;
      
    /**
     * Creates a new instance of AddProjectAction for adding an internal
     * SU project.
     */
    public AddProjectAction() {
        this(true);
    }

    /**
     * Creates a new instance of AddProjectAction for adding an internal or
     * external SU project.
     *
     * @param internal  whether the SU project to be added is internal
     *                  or external
     */
    public AddProjectAction(boolean internal) {
        this.internal = internal;
        init();
    }
    
    private void init() {
        javaeeAntArtifactTypes.addAll(JbiProjectConstants.JAVA_EE_AA_TYPES);
        javaeeAntArtifactTypes.add(JbiProjectConstants.ARTIFACT_TYPE_JBI_ASA);
        
        ResourceBundle rb = NbBundle.getBundle(this.getClass());
        descEjbProjectsOnly = rb.getString(EJB_PROJ_DESC);
        descWebProjectsOnly = rb.getString(WEB_PROJ_DESC);
        // TODO ear project does not have AntArtifactProvider yet
        // descEarProjectsOnly = rb.getString(EAR_PROJ_DESC);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param p DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean enable(Project p) {
        return true;
    }
    
    /**
     * DOCUMENT ME!
     *
     *
     * @param jbiProject DOCUMENT ME!
     */
    public void perform(Project jbiProject) {
        CasaHelper.saveCasa((JbiProject)jbiProject);
        
        JbiProjectProperties projProperties = ((ProjectPropertyProvider) jbiProject).getProjectProperties();
        List oldList = (List) projProperties.get(JbiProjectProperties.JBI_CONTENT_ADDITIONAL);
        
        // Check if there is broken reference problem first
        for (int j = 0; j < oldList.size(); j++) {
            VisualClassPathItem oldVi = (VisualClassPathItem) oldList.get(j);
            String oldProjName = oldVi.getProjectName();
            if (oldProjName.equals("")) {   // broken reference?
                String msg = NbBundle.getMessage(AddProjectAction.class, "MSG_ResolveBrokenReference"); // NOI18N
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                
                return;
            }
        }
        
        List<FileFilter> ffList = new ArrayList<FileFilter>();
        ffList.add(new EJBArtifactsFilter());
        ffList.add(new WebAppArtifactsFilter());
        ffList.add(new EarArtifactsFilter() );
        
        AntArtifact[] artifacts = AntArtifactChooser.showDialog(
                javaeeAntArtifactTypes, jbiProject, ffList, null);
        if (artifacts != null) {
            for (int i = 0; i < artifacts.length; i++) {
                addProject(jbiProject, artifacts[i]);
            }
        }
    }
    
    public boolean addProject(Project jbiProject, AntArtifact artifact) {
        JbiProjectProperties projProperties =
                ((ProjectPropertyProvider) jbiProject).getProjectProperties();
        
        List os = (List) projProperties.get(JbiProjectProperties.META_INF);
        List<VisualClassPathItem> oldList = (List<VisualClassPathItem>) 
                projProperties.get(JbiProjectProperties.JBI_CONTENT_ADDITIONAL);
        
        if ((os == null) || (os.size() < 1)) {
            return false;
        }
                
        VisualClassPathItem vi = new VisualClassPathItem(
                artifact, VisualClassPathItem.TYPE_ARTIFACT, null,
                artifact.getArtifactLocations()[0].toString(), 
                internal
                );
        String projName = vi.getProjectName();
        for (VisualClassPathItem oldVi : oldList) {
            String oldProjName = oldVi.getProjectName();
            if (oldProjName.equals(projName)) { // duplicate project
                String msg = NbBundle.getMessage(AddProjectAction.class,
                        "MSG_DuplicateJBIModule", projName); // NOI18N
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return false;
            }
        }
        
        List<VisualClassPathItem> newList = new ArrayList<VisualClassPathItem>();
        newList.add(vi);
        newList.addAll(oldList);
        
        List<String> oldTargetIDs =
                (List) projProperties.get(JbiProjectProperties.JBI_CONTENT_COMPONENT);
        
        String asaType = vi.getAsaType();
        
        ComponentHelper componentHelper = new ComponentHelper(jbiProject);        
        String newTargetID = componentHelper.getDefaultTarget(asaType);
        
        if (newTargetID == null) {
            String msg = NbBundle.getMessage(AddProjectAction.class,
                    "MSG_UnknownJBIModuleType", projName, asaType); // NOI18N
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return false;
        } 
        
        List<String> newTargetIDs = new ArrayList<String>();
        newTargetIDs.add(newTargetID);
        newTargetIDs.addAll(oldTargetIDs);
        
        projProperties.put(JbiProjectProperties.JBI_CONTENT_ADDITIONAL, newList);
        
        projProperties.put(JbiProjectProperties.JBI_CONTENT_COMPONENT, newTargetIDs);
        projProperties.store();
        
        // add server resources metadata
        SunResourcesUtil.addJavaEEResourceMetaData(jbiProject, artifact);
        
        Project subproject = artifact.getProject();        
        jbiProject.getLookup().lookup(JbiSubprojectProvider.class).subprojectAdded(subproject);
        
        return true;
    }
            
    public class EJBArtifactsFilter extends FileFilter {
        public EJBArtifactsFilter(){
        }
        
        public boolean accept(File f) {
            if ( f == null )
                return false;
            
            if (!f.isDirectory()){
                return false;
            }
            
            try {
                FileObject projectRoot = FileUtil.toFileObject( f );
                if ( projectRoot != null ) {
                    Project project = ProjectManager.getDefault().findProject( projectRoot );
                    if ( project != null ){
                        Lookup lookup = project.getLookup();
                        Class clz = project.getClass();
                        if ( clz.getName().indexOf( STR_EJB_PROJECT ) != -1 ) {
                            return true;
                        }
                    } else {
                        // List directory
                        return true;
                    }
                } 
            } catch ( IOException e ) {
                // no action
            } catch ( IllegalArgumentException iae ) {
                // nop
            }
            
            return false;
        }
        
        public String getDescription() {
            return descEjbProjectsOnly;
        }
    }
    
    public class WebAppArtifactsFilter extends FileFilter {
        public WebAppArtifactsFilter(){
            
        }
        public boolean accept(File f) {
            if ( f == null ){
                return false;
            }
            
            if (!f.isDirectory()){
                return false;
            }
            
            try {
                FileObject projectRoot = FileUtil.toFileObject( f );
                
                if ( projectRoot != null ) {
                    Project project = ProjectManager.getDefault().findProject( projectRoot );
                    if ( project != null ){
                        Lookup lookup = project.getLookup();
                        Class clz = project.getClass();
                        if ( clz.getName().indexOf( STR_WEB_PROJECT ) != -1 ) {
                            return true;
                        }
                    } else {
                        // List directory.
                        return true;
                    }
                } 
            } catch ( IOException e ) {
                // Return null
            } catch ( IllegalArgumentException iae ) {
                // nop
            }
            
            return false;
        }
        
        public String getDescription() {
            return descWebProjectsOnly;
        }
    }
    
    public class EarArtifactsFilter extends FileFilter {
        public EarArtifactsFilter() {
        }
        
        public boolean accept(File f) {
            if ( f == null ){
                return false;
            }
            
            if (!f.isDirectory()){
                return false;
            }
            
            try {
                FileObject projectRoot = FileUtil.toFileObject( f );
                
                if ( projectRoot != null ) {
                    Project project = ProjectManager.getDefault().findProject( projectRoot );
                    if ( project != null ){                    
                        Lookup lookup = project.getLookup();
                        Class clz = project.getClass();
                        if ( clz.getName().indexOf( STR_EAR_PROJECT ) != -1 ) {
                            return true;
                        }
                    } else {
                        // List plain directory
                        return true;
                    }
                }
            } catch ( IOException e ) {
                // Return null
            } catch ( IllegalArgumentException iae ) {
                // nop
            }
            
            return false;
        }
        
        public String getDescription() {
            return descEarProjectsOnly;
        }
    }
}
