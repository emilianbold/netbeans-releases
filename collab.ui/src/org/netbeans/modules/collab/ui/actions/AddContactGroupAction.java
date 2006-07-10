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
package org.netbeans.modules.collab.ui.actions;

import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;

import org.netbeans.modules.collab.ui.*;

/**
 *
 *
 */
public class AddContactGroupAction extends CookieAction {
    /**
     *
     *
     */
    public String getName() {
        return NbBundle.getMessage(AddContactGroupAction.class, "LBL_AddContactGroupAction_Name"); // NOI18N
    }

    /**
     *
     *
     */
    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif"; // NOI18N
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     *
     *
     */
    protected Class[] cookieClasses() {
        return new Class[] { CollabSessionCookie.class };
    }

    /**
     *
     *
     */
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    /**
     *
     *
     */
    protected void performAction(Node[] nodes) {
        CollabSessionCookie sessionCookie = (CollabSessionCookie) nodes[0].getCookie(CollabSessionCookie.class);

        if (sessionCookie != null) {
            AddContactGroupForm form = new AddContactGroupForm(sessionCookie.getCollabSession());
            form.addContactGroup();
        }
    }
}
