/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * DisplayAction.java
 *
 *
 * Created: Wed Mar  1 16:58:40 2000
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client; 

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class DisplayAction extends NodeAction {

    /**
     * Default constructor
     */
    public DisplayAction() {}
    
    /**
     * Sets the name of the action
     */
    public String getName() { 
	return NbBundle.getBundle(DisplayAction.class).getString("MON_Display_12");
    }

    /**
     * Not implemented
     */
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }

    public boolean enable(Node[] nodes) {
	if(nodes != null && nodes.length == 1) return true;
	else return false;
    }
    
    public void performAction() { 
	Node[] nodes = getActivatedNodes();
	TransactionView.getInstance().displayTransaction(nodes[0]);
    }

    public void performAction(Node[] nodes) { 
	TransactionView.getInstance().displayTransaction(nodes[0]);
    }

    public boolean asynchronous() { 
	return false; 
    } 
} // DisplayAction
