/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
