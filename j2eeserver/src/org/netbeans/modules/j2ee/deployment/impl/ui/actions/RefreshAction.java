/*
 * RefreshAction.java
 *
 * Created on August 14, 2003, 8:16 AM
 */

package org.netbeans.modules.j2ee.deployment.impl.ui.actions;

import org.netbeans.modules.j2ee.deployment.impl.*;
import org.openide.nodes.*;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;

/**
 *
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
