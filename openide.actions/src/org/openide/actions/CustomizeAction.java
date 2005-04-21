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


/** Customize a node (rather than using its property sheet).
* @see NodeOperation#customize
* @author   Ian Formanek, Jan Jancura
*/
public class CustomizeAction extends NodeAction {
    protected void performAction(Node[] activatedNodes) {
        NodeOperation.getDefault().customize(activatedNodes[0]);
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        if ((activatedNodes == null) || (activatedNodes.length != 1)) {
            return false;
        }

        return activatedNodes[0].hasCustomizer();
    }

    public String getName() {
        return NbBundle.getMessage(CustomizeAction.class, "Customize");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizeAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/propertysheet/customize.gif"; // NOI18N
    }
}
