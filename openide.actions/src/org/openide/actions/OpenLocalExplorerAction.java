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
package org.openide.actions;

import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/** Open an Explorer window with a particular root node.
* @see NodeOperation#explore
* @author   Ian Formanek
*/
public final class OpenLocalExplorerAction extends NodeAction {
    protected void performAction(Node[] activatedNodes) {
        NodeOperation.getDefault().explore(activatedNodes[0]);
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        if ((activatedNodes == null) || (activatedNodes.length != 1) || (activatedNodes[0].isLeaf())) {
            return false;
        }

        return true;
    }

    public String getName() {
        return NbBundle.getMessage(OpenLocalExplorerAction.class, "OpenLocalExplorer");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(OpenLocalExplorerAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/openLocalExplorer.gif"; // NOI18N
    }
}
