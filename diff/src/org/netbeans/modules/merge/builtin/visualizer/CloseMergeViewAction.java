/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.merge.builtin.visualizer;

import org.openide.util.actions.CookieAction;
import org.openide.cookies.CloseCookie;

/**
 *
 * @author  Martin Entlicher
 */
public class CloseMergeViewAction extends CookieAction {
    
    private static final long serialVersionUID = 2746214508313015932L;

    protected Class[] cookieClasses() {
        return new Class[] { CloseCookie.class };
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx (CloseMergeViewAction.class);
    }
    
    public String getName() {
        return org.openide.util.NbBundle.getMessage(CloseMergeViewAction.class, "CloseAction");
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected void performAction(org.openide.nodes.Node[] node) {
        if (node.length == 0) return;
        CloseCookie cc = (CloseCookie) node[0].getCookie (CloseCookie.class);
        if (cc != null) {
            cc.close();
        }
    }
    
}
