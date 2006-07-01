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

package org.openide.nodes;

/** Action to be used by tests.
 */
public class OpenAction extends org.openide.util.actions.CookieAction {
    protected void performAction (Node[] activatedNodes) {
    }

    protected int mode () {
        return MODE_ALL;
    }

    public String getName () {
        return "Open";
    }

    public org.openide.util.HelpCtx getHelpCtx () {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }

    protected Class[] cookieClasses () {
        return new Class[] { org.openide.cookies.OpenCookie.class };
    }

}
