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
 * 
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
