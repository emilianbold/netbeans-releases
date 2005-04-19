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

import org.netbeans.modules.j2ee.deployment.impl.*;
import org.openide.nodes.*;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;

/**
 * @author  nn136682
 */
public class RefreshAction extends CookieAction {
    public String getName() {
        return org.openide.util.NbBundle.getMessage(RefreshAction.class, "LBL_Refresh");
    }
    
    public HelpCtx getHelpCtx() {
        //PENDING:
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
    
    protected void performAction(Node[] nodes) {
        if (nodes.length > 0) {
            RefreshCookie r = (RefreshCookie) nodes[0].getCookie(RefreshCookie.class);
            r.refresh();
         }
    }
    
    protected Class[] cookieClasses() {
        return new Class[] { RefreshCookie.class };
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    public static interface RefreshCookie extends Node.Cookie {
        public void refresh();
    }
    
    protected boolean asynchronous() { return false; }
}
