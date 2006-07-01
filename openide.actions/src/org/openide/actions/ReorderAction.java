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
