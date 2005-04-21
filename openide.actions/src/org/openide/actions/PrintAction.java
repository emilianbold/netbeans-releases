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

import org.openide.cookies.PrintCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;


/** Print the selected object.
* @see PrintCookie
*
* @author Ales Novak
*/
public class PrintAction extends CookieAction {
    public PrintAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    protected Class[] cookieClasses() {
        return new Class[] { PrintCookie.class };
    }

    protected void performAction(final Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            PrintCookie pc = (PrintCookie) activatedNodes[i].getCookie(PrintCookie.class);

            if (pc != null) {
                pc.print();
            }
        }
    }

    protected boolean asynchronous() {
        return true;
    }

    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(PrintAction.class, "Print");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(PrintAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/print.png"; // NOI18N
    }
}
