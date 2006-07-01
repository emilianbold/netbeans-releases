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

import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;


/** Opens a node (for example, in a web browser, or in the Editor).
* @see OpenCookie
*
* @author   Petr Hamernik
*/
public class OpenAction extends CookieAction {
    protected Class[] cookieClasses() {
        return new Class[] { OpenCookie.class };
    }

    protected boolean surviveFocusChange() {
        return false;
    }

    protected int mode() {
        return MODE_ANY;
    }

    public String getName() {
        return NbBundle.getMessage(OpenAction.class, "Open");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(OpenAction.class);
    }

    protected void performAction(final Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            OpenCookie oc = (OpenCookie) activatedNodes[i].getCookie(OpenCookie.class);

            if (oc != null) {
                oc.open();
            }
        }
    }

    protected boolean asynchronous() {
        return false;
    }
}
