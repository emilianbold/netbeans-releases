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
 * SaveAction
 *
 *
 * Created: Wed Mar  1 16:59:48 2000
 *
 * @author Ana von Klopp Lemon
 * @version
 */

package org.netbeans.modules.web.monitor.client; 

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class SaveAction extends NodeAction {

    private static TransactionView tv = null;
    
    public SaveAction() {}

    /**
     * Sets the name of the action
     */
    public String getName() { 
	return NbBundle.getBundle(SaveAction.class).getString("MON_Save_20");
    }

    /**
     * Not implemented
     */
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }

    public void performAction() { 
	Node[] nodes = getActivatedNodes();
	tv.saveTransaction(nodes);
    }

    public void performAction(Node[] nodes) { 
	tv.saveTransaction(nodes);
    }

    public boolean enable(Node[] nodes) {
	return true;
    }
 
    public static void setTransactionView(TransactionView t) {
	tv = t;
    }

} // SaveAction
