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

package org.netbeans.modules.tomcat5.nodes.actions;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/** 
 * Action which displays context log for selected web module.
 *
 * @author Stepan Herold
 */
public class ContextLogAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            TomcatWebModuleCookie cookie = (TomcatWebModuleCookie)nodes[i].getCookie(TomcatWebModuleCookie.class);
            if (cookie != null) cookie.openLog();
        }        
    }

    protected boolean enable(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            TomcatWebModuleCookie cookie = (TomcatWebModuleCookie)nodes[i].getCookie(TomcatWebModuleCookie.class);
            if (cookie == null || !cookie.hasLogger()) return false;
        }         
        return true;
    }

    public String getName() {
        return NbBundle.getMessage(ContextLogAction.class, "LBL_ContextLogAction"); // NOI18N
    }
    
    protected boolean asynchronous() {
        return false;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}