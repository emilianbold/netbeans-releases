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
package org.netbeans.modules.xml.catalog;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.awt.StatusDisplayer;

/** 
 * Action sensitive to the node selection that refreshs children.
 *
 * @author  Petr Kuzel
 */
final class RefreshAction extends CookieAction {

    /** Serial Version UID */
    private static final long serialVersionUID =4798470042774935554L;
    
    protected void performAction (Node[] nodes) {        
        if (nodes == null) return;
        try {
            for (int i = 0; i<nodes.length; i++) {
                String msg = Util.THIS.getString("MSG_refreshing", nodes[i].getDisplayName());
                StatusDisplayer.getDefault().setStatusText(msg);
                Refreshable cake = (Refreshable) nodes[i].getCookie(Refreshable.class);
                cake.refresh();
            }
        } finally {
            String msg = Util.THIS.getString("MSG_refreshed");
            StatusDisplayer.getDefault().setStatusText(msg);
        }
    }

    public String getName () {
        return Util.THIS.getString ("LBL_Action");
    }

    protected String iconResource () {
        return null;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx(getClass());
    }

    protected Class[] cookieClasses() {
        return new Class[] {Refreshable.class};
    }
    
    protected int mode() {
        return CookieAction.MODE_ALL;
    }
    
    protected boolean asynchronous() {
        return false;
    }

}
