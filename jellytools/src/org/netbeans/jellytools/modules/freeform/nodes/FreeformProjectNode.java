/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.modules.freeform.nodes;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.modules.freeform.actions.RedeployFreeformAction;
import org.netbeans.jellytools.modules.freeform.actions.RunFreeformAction;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Web Freeform Project root node class. It represents root node of a project
 * in Projects view.
 * @author Martin.Schovanek@sun.com 
 */
public class FreeformProjectNode extends ProjectRootNode {
    
    static final RunFreeformAction runFreeformAction = new RunFreeformAction();
    static final RedeployFreeformAction redeployFreeformAction =
            new RedeployFreeformAction();
   
    /** tests popup menu items for presence */    
    public void verifyPopup() {
        super.verifyPopup();
        verifyPopup(new Action[]{
            runFreeformAction,
            redeployFreeformAction
        });
    }
    
    /** Creates new FreeformProjectNode instance.
     * @param projectName display name of the project
     */
    public FreeformProjectNode(String projectName) {
        super(ProjectsTabOperator.invoke().tree(), projectName);
    }
    
    /** run project */    
    public void run() {
        runFreeformAction.perform(this);
    }
    
    /** build project */    
    public void redeploy() {
        redeployFreeformAction.perform(this);
    }
    
    /** perform a custom action */
    public void customAction(String action) {
        new ActionNoBlock(null, action).perform(this);
    }
}