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
 * EditReplayAction.java
 *
 *
 * Created: Wed Mar  1 16:59:21 2000
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client; 

import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class EditReplayAction extends NodeAction {
    
    private final static boolean debug = false; 

    public EditReplayAction() {}
    /**
     * Sets the name of the action
     */
    public String getName() { 
	return NbBundle.getBundle(EditReplayAction.class).getString("MON_EditReplay");
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
	editTransaction(nodes[0]);
    }

    public void performAction(Node[] nodes) { 
	editTransaction(nodes[0]);
    }

    private void editTransaction(Node node) {

	if(debug) log("Editing a transaction"); //NOI18N

	// Exit if the internal server is not running - the user
	// should start it before they do this. 
	if(!Controller.getInstance().checkServer(true)) return;
	if(node == null) { 
	    if(debug) log("No selected node, why is this?"); // NOI18N 
	    return;
	}
	EditPanel.displayEditPanel((TransactionNode)node);
    }

    private void log(String s) { 
	System.out.println("EditReplayAction" + s);
    } 

    public boolean asynchronous() { 
	return false; 
    } 
} // EditReplayAction
