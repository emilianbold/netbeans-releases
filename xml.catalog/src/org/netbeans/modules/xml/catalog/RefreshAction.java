/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.catalog;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/** 
 * Action sensitive to the node selection that refreshs children.
 *
 * @author  Petr Kuzel
 */
public final class RefreshAction extends NodeAction {

    /** Serial Version UID */
    private static final long serialVersionUID =4798470042774935554L;
    
    protected void performAction (Node[] nodes) {        
        Refreshable node = (Refreshable) nodes[0];
        node.refresh();
    }

    protected boolean enable (Node[] nodes) {
        return nodes.length == 1 && nodes[0] instanceof Refreshable;
    }

    public String getName () {
        return Util.getString ("LBL_Action");
    }

    protected String iconResource () {
        return "RefreshActionActionIcon.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx (RefreshActionAction.class);
    }

    /** Perform extra initialization of this action's singleton.
     * PLEASE do not use constructors for this purpose!
    protected void initialize () {
	super.initialize ();
	putProperty ("someProp", value);
    }
    */

}
