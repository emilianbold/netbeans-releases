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

package org.netbeans.modules.j2ee.deployment.impl.ui.actions;

import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/**
 * Customizer action displayes the server customizer in the Server Manager.
 * 
 * @author  sherold
 */
public class CustomizerAction extends NodeAction {
    
    public void performAction(Node[] nodes) {
        ServerInstance instance = (ServerInstance)nodes[0].getCookie(ServerInstance.class);        
        ServerManager.showCustomizer(instance.getUrl());
    }
    
    protected boolean enable(Node[] nodes) {
        return true;
    }
    
    public String getName() {
        return NbBundle.getMessage(CustomizerAction.class, "LBL_Properties");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
}
