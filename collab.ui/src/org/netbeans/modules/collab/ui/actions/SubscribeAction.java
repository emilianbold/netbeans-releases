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
import org.openide.util.actions.NodeAction;

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabPrincipal;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.*;

public class SubscribeAction extends NodeAction {
    protected boolean enable(Node[] nodes) {
        if (nodes.length == 0) {
            return false;
        }

        // Sanity check to make sure the collab manager is present
        CollabManager manager = CollabManager.getDefault();

        if (manager == null) {
            return false;
        }

        for (int i = 0; i < nodes.length; i++) {
            ContactCookie cookie = (ContactCookie) nodes[i].getCookie(ContactCookie.class);

            if (cookie == null) {
                return false;
            }

            int status = cookie.getContact().getStatus();

            // This condition may change later based on IM's coming watcher API
            if (
                (status != CollabPrincipal.STATUS_WATCHED) && (status != CollabPrincipal.STATUS_PENDING) &&
                    (status != CollabPrincipal.STATUS_UNKNOWN)
            ) {
                return false;
            }
        }

        return true;
    }

    public String getName() {
        return NbBundle.getMessage(SubscribeAction.class, "LBL_SubscribeAction_Name");
    }

    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return true;
    }

    protected void performAction(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            try {
                ((ContactNode) nodes[i]).getContact().subscribe();
                ((ContactNode) nodes[i]).getContact().setStatus(CollabPrincipal.STATUS_PENDING);
            } catch (CollabException e) {
                Debug.errorManager.notify(e);
            }
        }
    }
}
