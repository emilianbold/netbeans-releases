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

import org.netbeans.modules.j2ee.deployment.impl.ui.ServerManagerDialog;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;


/**
 * ServerManagerAction displays server manager.
 * 
 * @author  Stepan Herold
 */
public class ServerManagerAction extends CallableSystemAction {

    public ServerManagerAction () {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public void performAction() {
        ServerManagerDialog.showCustomizer();
    }
    
    public String getName() {
        return NbBundle.getMessage(ServerManagerAction.class,"CTL_ServerManager"); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
}
