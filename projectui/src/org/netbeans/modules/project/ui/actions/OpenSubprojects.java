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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui.actions;

import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/** Action opening all "subprojects" of given project
 */
public class OpenSubprojects extends NodeAction {

    private static final String ICON = "org/netbeans/modules/project/ui/resources/openProject.gif"; //NOI18N

    /** Creates a new instance of BrowserAction */
    public OpenSubprojects() {
    }
        
    public String getName() {
        return NbBundle.getMessage( OpenSubprojects.class, "LBL_OpenSubprojectsAction_Name" ); // NOI18N        
    }
    
    public String iconResource() {
        return ICON;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        
        if ( activatedNodes == null || activatedNodes.length == 0 ) {
            return false; // No nodes no closing
        }
        
        // Find out whether all nodes have project in lookup 
        boolean someSubprojects = false; // And have some subprojects;
        for( int i = 0; i < activatedNodes.length; i++ ) {
            Project p = (Project)activatedNodes[i].getLookup().lookup( Project.class );
            if ( p == null ) {
                return false;
            }
            else {
                
                SubprojectProvider spp = (SubprojectProvider)p.getLookup().lookup( SubprojectProvider.class );
                
                if ( spp != null && !spp.getSubprojects().isEmpty() ) {
                    someSubprojects = true;
                }                
            }
        }
        
        return someSubprojects;
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
    
        for( int i = 0; i < activatedNodes.length; i++ ) {
            Project p = (Project)activatedNodes[i].getLookup().lookup( Project.class );
            if ( p != null ) {
                OpenProjectList.getDefault().open(new Project[] {p}, true, true);
            }
        }
        
    }
    
}
