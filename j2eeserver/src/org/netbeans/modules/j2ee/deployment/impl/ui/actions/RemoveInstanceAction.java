/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl.ui.actions;

import org.openide.util.actions.*;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.*;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;

/**
 * Remove instance action displays a confirmation dialog whether the server should
 * be removed. The server is stopped before removal if it was started from within
 * the IDE before.
 *
 * @author  nn136682
 */
public class RemoveInstanceAction extends CookieAction {
    
    protected void performAction(org.openide.nodes.Node[] nodes) {
        for (int i=0; i<nodes.length; i++) {
            ServerInstance instance = (ServerInstance) nodes[i].getCookie(ServerInstance.class);
            if (instance == null || instance.isRemoveForbidden()) {
                continue;
            }
            String title = NbBundle.getMessage(RemoveInstanceAction.class, "MSG_RemoveInstanceTitle", instance.getDisplayName());
            String msg = NbBundle.getMessage(RemoveInstanceAction.class, "MSG_ReallyRemoveInstance", instance.getDisplayName());
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, title, NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
                instance.remove();
            }
        }
    }
    
    protected boolean enable (Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            ServerInstance instance = (ServerInstance)nodes[i].getCookie(ServerInstance.class);
            if (instance == null || instance.isRemoveForbidden() 
                || instance.getServerState() == ServerInstance.STATE_WAITING) {
                return false;
            }
        }
        return true;
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {
            ServerInstance.class 
        };
    }
    
    protected int mode() {
        return MODE_ALL;
    }
    
    public String getName() {
        return NbBundle.getMessage(RemoveInstanceAction.class, "LBL_Remove");
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false; 
    }

}
