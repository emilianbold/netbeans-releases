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
 * JavaEEModulePropertiesAction.java
 *
 * Created on October 18, 2006, 9:15 PM
 *
 */

package org.netbeans.modules.compapp.projects.jbi.jeese.actions;

import java.awt.Dialog;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.javaee.codegen.model.EndpointCfg;
import org.netbeans.modules.compapp.javaee.util.ProjectUtil;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.jeese.ui.DeploymentOptionPanel;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action to display/edit JavaEE module/project properties.
 */
public class JavaEEModulePropertiesAction extends NodeAction {
    private String name = "";//NOI18N
    
    public JavaEEModulePropertiesAction() {
        init();
    }
    
    public boolean asynchronous(){
        return false;
    }
    
    private void init() {
        ResourceBundle rb = NbBundle.getBundle(this.getClass());
        name = rb.getString("nameProperties");//NOI18N
    }
    
    public boolean enable(Project project) {
        return true;
    }
    
    protected void performAction(Node[] activatedNodes) {
        Node theNode = activatedNodes[ 0 ];
        
        Lookup lookup = theNode.getLookup();
        JbiProject jbiProject = (JbiProject)lookup.lookup( JbiProject.class );
        
        VisualClassPathItem vcpi = (VisualClassPathItem)lookup.lookup(
                VisualClassPathItem.class );
        
        if ( jbiProject != null && vcpi != null ) {
            ResourceBundle rb = NbBundle.getBundle(this.getClass());
            String msgDeployOption = rb.getString("titleDeployOption");//NOI18N
            List<EndpointCfg> cfgs = ProjectUtil.getEndpointCfgs(jbiProject, vcpi.getProjectName());
            DeploymentOptionPanel pnl = new DeploymentOptionPanel(cfgs);
            pnl.isDeployThruCA(depThruCA(jbiProject, vcpi.getProjectName()));
            
            DialogDescriptor dd = new DialogDescriptor(pnl, 
                    " " + msgDeployOption + vcpi.getProjectName());//NOI18N
            Dialog dlg = DialogDisplayer.getDefault().createDialog( dd );
            
            dlg.setVisible( true );
            
            if ( dd.getValue() == DialogDescriptor.OK_OPTION ) {
                ProjectUtil.setJavaEECustomProperty(jbiProject,
                        vcpi.getProjectName() + ProjectUtil.DEPLOY_THRU_CA,
                        Boolean.toString(pnl.isDeployThruCA()));
                if ((cfgs != null) && (cfgs.size() > 0)){
                    ProjectUtil.saveEndpointCfgs(jbiProject, vcpi.getProjectName(), cfgs);
                }
            }
        }
    }
    
    private boolean depThruCA(JbiProject jbiProj, String subProjName) {
        String val = ProjectUtil.getJavaEECustomProperty(jbiProj).getProperty(subProjName + ProjectUtil.DEPLOY_THRU_CA);
        boolean bool = true;
        
        if (val != null) {
            bool = Boolean.valueOf(val);
        }
        
        return bool;
    }
    
    public boolean enable(Node[] nodes) {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public String getName() {
        return name;
    }
}
