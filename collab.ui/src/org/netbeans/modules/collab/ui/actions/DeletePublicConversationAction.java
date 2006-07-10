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
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import com.sun.collablet.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.CollabSessionCookie;
import org.netbeans.modules.collab.ui.ConversationCookie;

public class DeletePublicConversationAction extends NodeAction {
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
            CollabSessionCookie sessionCookie = (CollabSessionCookie) nodes[i].getCookie(CollabSessionCookie.class);

            if (sessionCookie == null) {
                return false;
            }

            ConversationCookie cookie = (ConversationCookie) nodes[i].getCookie(ConversationCookie.class);

            if (cookie == null) {
                return false;
            }

            CollabSession session = sessionCookie.getCollabSession();

            if (!session.canManagePublicConversation(nodes[i].getName())) {
                return false;
            }
        }

        return true;
    }

    public String getName() {
        return NbBundle.getMessage(SubscribeAction.class, "LBL_DeletePublicConversationAction_Name");
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
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(DeletePublicConversationAction.class, "MSG_DeletePublicConversationAction_Confirm"),
                NotifyDescriptor.YES_NO_OPTION
            );

        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION) {
            for (int i = 0; i < nodes.length; i++) {
                try {
                    CollabSessionCookie sessionCookie = (CollabSessionCookie) nodes[i].getCookie(
                            CollabSessionCookie.class
                        );
                    assert sessionCookie != null : "CollabSessionCookie was null despite enable check";

                    CollabSession session = sessionCookie.getCollabSession();

                    ConversationCookie conversationCookie = (ConversationCookie) nodes[i].getCookie(
                            ConversationCookie.class
                        );

                    if (conversationCookie != null) {
                        Conversation conv = conversationCookie.getConversation();

                        if (conv != null) {
                            conv.leave();
                        }
                    }

                    session.deletePublicConversation(nodes[i].getName());
                    session.unsubscribePublicConversation(nodes[i].getName());
                } catch (CollabException e) {
                    Debug.errorManager.notify(e);
                }
            }
        }
    }
}
