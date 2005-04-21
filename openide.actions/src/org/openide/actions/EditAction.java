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

import org.openide.cookies.EditCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;


/**
* Edit an object.
* @see EditCookie
*
* @author Jaroslav Tulach
*/
public class EditAction extends CookieAction {
    protected boolean surviveFocusChange() {
        return false;
    }

    public String getName() {
        return NbBundle.getBundle(EditAction.class).getString("Edit");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(EditAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/editorMode.gif"; // NOI18N
    }

    protected int mode() {
        return MODE_ALL;
    }

    protected Class[] cookieClasses() {
        return new Class[] { EditCookie.class };
    }

    protected void performAction(final Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            EditCookie es = (EditCookie) activatedNodes[i].getCookie(EditCookie.class);

            if (es != null) {
                es.edit();
            }
        }
    }

    protected boolean asynchronous() {
        return false;
    }
}
