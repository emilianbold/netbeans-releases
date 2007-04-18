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
 * JavaEEModulePropertiesAction.java
 *
 * Created on October 18, 2006, 9:15 PM
 *
 */

package org.netbeans.modules.compapp.projects.jbi.jeese.actions;

import java.awt.Dialog;
import java.util.ResourceBundle;
import org.netbeans.api.project.Project;
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
    private String name = "Deployment Settings";
    private static final String MSG_DEPLOY_OPTION = "titleDeployOption" ; // No I18N
    private static final String NAME = "nameProperties" ; // No I18N
    
    public JavaEEModulePropertiesAction() {
        init();
    }
    
    public boolean asynchronous(){
        return false;
    }
    
    private void init() {
        ResourceBundle rb = NbBundle.getBundle(this.getClass());
        name = rb.getString(NAME);
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
            String msgDeployOption = rb.getString(MSG_DEPLOY_OPTION);
            
            DeploymentOptionPanel pnl = new DeploymentOptionPanel();
            pnl.isDeployThruCA(depThruCA(jbiProject, vcpi.getProjectName()));
            
            DialogDescriptor dd = new DialogDescriptor(pnl, " " + vcpi.getProjectName() + msgDeployOption);
            Dialog dlg = DialogDisplayer.getDefault().createDialog( dd );
            
            //dlg.setSize( 400, 165 );
            dlg.setVisible( true );
            
            if ( dd.getValue() == dd.OK_OPTION ) {
                ProjectUtil.setJavaEECustomProperty(jbiProject,
                        vcpi.getProjectName() + ProjectUtil.DEPLOY_THRU_CA,
                        Boolean.toString(pnl.isDeployThruCA()));
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
