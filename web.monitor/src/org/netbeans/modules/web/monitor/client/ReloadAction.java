/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * ReloadAction.java
 *
 * Created on June 23, 2004, 9:55 AM
 * 
 * @author  Stepan Herold
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class ReloadAction extends NodeAction {
    
    
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getBundle(ReloadAction.class).getString("MON_Reload_all");
    }
    
    public void performAction() {
        MonitorAction.getController().getTransactions();
    }
    
    protected void performAction(Node[] activatedNodes) {
        performAction();
    }

    public boolean asynchronous() { 
	return false; 
    }
}
