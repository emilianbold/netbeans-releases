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
//import org.openide.src.ClassElement;
//import org.openide.src.Identifier;
//import org.openide.src.MethodElement;
///import org.openide.src.SourceException;
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
        // If you will provide context help then use:
        // return new HelpCtx(AddModuleAction.class);
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        return activatedNodes.length >= 0;
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        // launch add method dialog
        // open some kind of dialog to select a project
        //throw new UnsupportedOperationException(NbBundle.getMessage(this.getClass(), "EX_TEXT_UNIMPLEMENTED"));
        ModuleNode n = null;
        //FilterNode fn = null;
        for (int i = 0; i < activatedNodes.length; i++) {
            //fn = (FilterNode) activatedNodes[i];
            n = (ModuleNode) activatedNodes[i].getCookie(ModuleNode.class);
            //n.removeFromEar();     
        //}
        //if (null != n)
            n.removeFromJarContent();
        }
    }
    
}
