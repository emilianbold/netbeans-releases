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

import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;

import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabSession;
import org.netbeans.modules.collab.ui.CollabSessionCookie;

/**
 *
 *
 * @author Todd Fast <todd.fast@sun.com>
 */
public abstract class StatusActionBase extends CookieAction {
    /**
     *
     *
     */
    public StatusActionBase() {
        super();
    }

    /**
     *
     *
     */
    protected abstract String getDisplayName();

    /**
     *
     *
     */
    protected abstract int getStatus();

    /**
     *
     *
     */
    public String getName() {
        String result = getDisplayName();

        return result;
    }

    /**
     *
     *
     */
    protected boolean isCurrent() {
        CollabSession session = null;

        if (getActivatedNodes().length > 0) {
            CollabSessionCookie cookie = (CollabSessionCookie) getActivatedNodes()[0].getCookie(
                    CollabSessionCookie.class
                );

            if (cookie != null) {
                session = cookie.getCollabSession();
            }
        } else {
            CollabSession[] sessions = CollabManager.getDefault().getSessions();

            if ((sessions != null) && (sessions.length == 1)) {
                session = sessions[0];
            }
        }

        if (session == null) {
            return false;
        }

        return session.getUserPrincipal().getStatus() == getStatus();
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
        return MODE_EXACTLY_ONE;
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
    protected boolean asynchronous() {
        return true;
    }

    /**
     *
     *
     */
    public JMenuItem getPopupPresenter() {
        JMenuItem result = new JRadioButtonMenuItem(getDisplayName(), getIcon()) {
                public boolean isSelected() {
                    return isCurrent();
                }
            };

        Actions.connect(result, this);

        return result;
    }

    /**
     *
     *
     */
    public boolean isEnabled() {
        // If only one session is active, enable the action. Otherwise,
        // require a node selection to test enabled status.
        CollabSession[] sessions = CollabManager.getDefault().getSessions();

        if ((sessions != null) && (sessions.length == 1)) {
            return true;
        } else {
            return super.isEnabled();
        }
    }

    /**
     *
     *
     */
    public void performAction() {
        // If only one session is active, perform the action on it. Otherwise,
        // require a node selection.
        CollabSession[] sessions = CollabManager.getDefault().getSessions();

        if ((sessions != null) && (sessions.length == 1)) {
            setSessionStatus(sessions[0]);
        } else {
            super.performAction();
        }
    }

    /**
     *
     *
     */
    protected void performAction(Node[] nodes) {
        CollabSessionCookie cookie = (CollabSessionCookie) nodes[0].getCookie(CollabSessionCookie.class);
        assert cookie != null : "CollabSessionCookie was not available from the selected " +
        "node; performAction should not have been called";

        if (cookie == null) {
            return;
        }

        CollabSession session = session = cookie.getCollabSession();
        assert session != null : "Session was not available from the selected node; " +
        "performAction should not have been called";

        if (session == null) {
            return;
        }

        setSessionStatus(session);
    }

    /**
     *
     *
     */
    protected abstract void setSessionStatus(CollabSession session);
}
