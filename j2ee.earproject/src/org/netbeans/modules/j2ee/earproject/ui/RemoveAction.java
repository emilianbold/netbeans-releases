/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.ui;

import org.openide.nodes.Node;
import org.openide.nodes.FilterNode;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author  vkraemer
 */
public class RemoveAction extends NodeAction {
    
    public String getName() {
        return NbBundle.getMessage(this.getClass(), "LBL_RemoveAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        return activatedNodes.length >= 0;
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        ModuleNode n = null;
        for (int i = 0; i < activatedNodes.length; i++) {
            n = (ModuleNode) activatedNodes[i].getCookie(ModuleNode.class);
            n.removeFromJarContent();
        }
    }
    
}
