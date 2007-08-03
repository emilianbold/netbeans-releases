/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, title, NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
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
