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

import javax.swing.*;

import org.openide.*;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;

import org.netbeans.modules.collab.ui.CollabSessionCookie;

/**
 *
 *
 * @author Todd Fast <todd.fast@sun.com>
 */
public class LogoutAction extends CookieAction {
    private JMenuItem menuPresenter;

    public boolean isEnabled() {
        // Try to force the menu to revalidate its size, in case the name of
        // action has changed
        if (menuPresenter != null) {
            menuPresenter.invalidate();

            if (menuPresenter.getParent() != null) {
                menuPresenter.getParent().validate();
            }

            if (menuPresenter.getParent() instanceof JPopupMenu) {
                ((JPopupMenu) menuPresenter.getParent()).pack();
            }
        }

        return super.isEnabled();
    }

    public String getName() {
        String sessionName = null;

        if (getActivatedNodes().length > 0) {
            CollabSessionCookie cookie = (CollabSessionCookie) getActivatedNodes()[0].getCookie(
                    CollabSessionCookie.class
                );

            if (cookie != null) {
                sessionName = cookie.getCollabSession().getUserPrincipal().getDisplayName();
            }
        }

        String result = null;

        if (sessionName == null) {
            result = NbBundle.getMessage(LoginAction.class, "LBL_LogoutAction_NameDisabled"); // NOI18N
        } else {
            result = NbBundle.getMessage(LoginAction.class, "LBL_LogoutAction_Name", sessionName); // NOI18N
        }

        return result;
    }

    protected boolean surviveFocusChange() {
        return false;
    }

    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected Class[] cookieClasses() {
        return new Class[] { CollabSessionCookie.class };
    }

    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    public JMenuItem getMenuPresenter() {
        // Cache the menu presenter so we can force it to revalidate later
        menuPresenter = super.getMenuPresenter();

        return menuPresenter;
    }

    protected void performAction(Node[] nodes) {
        CollabSessionCookie cookie = (CollabSessionCookie) nodes[0].getCookie(CollabSessionCookie.class);
        assert cookie != null : "CollabSessionCookie was null; performAction should not " + "have been called";

        // Prompt for logoff
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(
                    LogoutAction.class, "MSG_LogoutAction_ConfirmLogout",
                    cookie.getCollabSession().getUserPrincipal().getDisplayName()
                ), NotifyDescriptor.YES_NO_OPTION
            );

        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION) {
            cookie.getCollabSession().logout();
        }
    }
}
