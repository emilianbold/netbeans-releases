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

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.jbi.CasaHelper;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.ProjectPropertyProvider;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;

import org.openide.nodes.Node;

import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.openide.windows.IOProvider;
import org.openide.windows.OutputWriter;

import java.util.List;
import org.netbeans.modules.compapp.projects.jbi.JbiSubprojectProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.actions.SystemAction;


/**
 * DOCUMENT ME!
 *
 * @author 
 * @version 
 */
public class DeleteModuleAction extends SystemAction {
    /**
     * DOCUMENT ME!
     *
     * @param activatedNodes DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected boolean asynchronous() {
        return false;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param activatedNodes DOCUMENT ME!
     */
    public void performAction(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            String mName = node.getDisplayName();
            //log("Delete Node: " + activatedNodes.length + ", " + mName);
            
            JbiProjectCookie jpc = (node.getParentNode().getCookie(JbiProjectCookie.class));
            
            if (jpc != null) {
                JbiProject jbiProject = jpc.getProject();
                CasaHelper.saveCasa(jbiProject);
                boolean success = removeProject(jbiProject, mName); 
                if (!success) {
                    String msg = NbBundle.getMessage(DeleteModuleAction.class, 
                            "MSG_CantDeleteModule", mName); // NOI18N
                    NotifyDescriptor d =
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        }
    }

    protected void deleteModuleProperties(Project jbiProject, 
                                VisualClassPathItem vcpi, 
                                String artifactName){
        // No action, to be overridden by Java EE/module specific delete actions
    }    
    
    protected void updateModuleProperties(Project jbiProject, 
                                JbiProjectProperties projProp, 
                                List<VisualClassPathItem> subprojJars, 
                                String subProjName){
        // No action, to be overridden by Java EE/module specific delete actions
    }
    
    private void deleteEmbeddedProjects(final Project jbiProject, 
            List<VisualClassPathItem> deletedProjsList){
        if ((deletedProjsList != null) && (deletedProjsList.size() > 0)){
            Iterator<VisualClassPathItem> itr = deletedProjsList.iterator();
            VisualClassPathItem vcpi = null;
            Project prj = null;
            FileObject projDir = null;
            FileObject jbiPrjDir = jbiProject.getProjectDirectory();
            while (itr.hasNext()){
                vcpi = itr.next();
                prj = vcpi.getAntArtifact().getProject();
                projDir = prj.getProjectDirectory();
                if (FileUtil.isParentOf(jbiPrjDir, projDir)){
                    //DefaultProjectOperations.performDefaultDeleteOperation(prj);
                    FileObject fo = projDir;
                    try {
                        fo.delete();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }
    
    /**
     * Removes a JBI module from a JBI project.
     * 
     * @param jbiProject    a JBI project
     * @param artifactName  the artifact name of a JBI module
     */
    public boolean removeProject(final Project jbiProject, String artifactName) {
        
        JbiProjectProperties projProperties = 
                ((ProjectPropertyProvider) jbiProject).getProjectProperties();
        @SuppressWarnings("unchecked")
        List<VisualClassPathItem> oldCompProjList = 
                (List) projProperties.get(JbiProjectProperties.JBI_CONTENT_ADDITIONAL);
        List<VisualClassPathItem> newCompProjList = 
                new ArrayList<VisualClassPathItem>();
        List<VisualClassPathItem> deletedProjsList = 
                new ArrayList<VisualClassPathItem>();
        
        String subProjName = null;        
        int itemRemovedIndex = -1;
        
        Project subproject = null;      
        
        for (int i = 0; i < oldCompProjList.size(); i++) {
            VisualClassPathItem cp = oldCompProjList.get(i);
            
            if (artifactName.equalsIgnoreCase(cp.getShortName())) {
                itemRemovedIndex = i;
                deleteModuleProperties(jbiProject, cp, artifactName);
                subProjName = cp.getProjectName();  
                subproject = cp.getAntArtifact().getProject();  
                deletedProjsList.add(oldCompProjList.get(i));
            } else {
                newCompProjList.add(oldCompProjList.get(i));
            }
        }
        
        if (itemRemovedIndex != -1) {
            // Need to keep target component list in sync.
            @SuppressWarnings("unchecked")
            List<String> targetComps = new ArrayList<String>(
                    (List) projProperties.get(JbiProjectProperties.JBI_CONTENT_COMPONENT));
            assert targetComps.size() == oldCompProjList.size() : 
                "Properties jbi.content.additional and jbi.content.component are not in sync."; // NOI18N
            
            targetComps.remove(itemRemovedIndex);
            
            projProperties.put(JbiProjectProperties.JBI_CONTENT_COMPONENT, targetComps);                
            
            projProperties.put(JbiProjectProperties.JBI_CONTENT_ADDITIONAL, newCompProjList);
            
            updateModuleProperties(jbiProject, projProperties, newCompProjList, subProjName);
            projProperties.store();   
            
            jbiProject.getLookup().lookup(JbiSubprojectProvider.class).subprojectRemoved(subproject);
            deleteEmbeddedProjects(jbiProject, deletedProjsList);
            return true;
        } else {
            return false;
        }
    }
        
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return NbBundle.getMessage(DeleteModuleAction.class, "LBL_DeleteModuleAction_Name"); // NOI18N
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        
        // If you will provide context help then use:
        // return new HelpCtx(AddModuleAction.class);
    }
    
    private void log(String str) {
        OutputWriter out = IOProvider.getDefault().getStdOut();
        out.println(str);
        out.flush();
    }

    public void actionPerformed(ActionEvent ev) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
