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

import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;


/** Reorder items in a list with a dialog.
* @see Index
*
* @author   Petr Hamernik, Dafe Simonek
*/
public class ReorderAction extends CookieAction {
    protected boolean surviveFocusChange() {
        return false;
    }

    public String getName() {
        return NbBundle.getBundle(ReorderAction.class).getString("Reorder");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ReorderAction.class);
    }

    protected Class[] cookieClasses() {
        return new Class[] { Index.class };
    }

    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    protected void performAction(Node[] activatedNodes) {
        Node n = activatedNodes[0]; // we supposed that one node is activated
        Index order = (Index) n.getCookie(Index.class);
        order.reorder();
    }

    protected boolean asynchronous() {
        return false;
    }
}
