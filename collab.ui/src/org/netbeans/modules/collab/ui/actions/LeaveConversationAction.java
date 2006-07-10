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


import org.openide.*;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;

import com.sun.collablet.CollabManager;
import com.sun.collablet.Conversation;
import org.netbeans.modules.collab.ui.CollabSessionCookie;
import org.netbeans.modules.collab.ui.ConversationCookie;

/**
 *
 *
 */
public class LeaveConversationAction extends CookieAction {
    /**
     *
     *
     */
    public String getName() {
        return NbBundle.getMessage(CreateConversationAction.class, "LBL_LeaveConversationAction_Name"); // NOI18N
    }

    /**
     *
     *
     */
    protected String iconResource() {
        return "org/netbeans/modules/collab/ui/resources/chat_png.gif"; // NOI18N
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
        return new Class[] { CollabSessionCookie.class, ConversationCookie.class };
    }

    /**
     *
     *
     */
    protected int mode() {
        return MODE_ALL;
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
    protected boolean enable(Node[] nodes) {
        if (nodes.length == 0) {
            return false;
        }

        // Sanity check to make sure the collab manager is present
        CollabManager manager = CollabManager.getDefault();

        if (manager == null) {
            return false;
        }

        // Disable if cookie not present
        if (!super.enable(nodes)) {
            return false;
        }

        // The selected nodes can either be the Conversations group node
        // or one or more conversation nodes.  We need to discover which mode we're
        // operating in.
        //return nodes[0].getCookie(ConversationCookie.class)!=null;
        for (int i = 0; i < nodes.length; i++) {
            ConversationCookie cookie = (ConversationCookie) nodes[i].getCookie(ConversationCookie.class);

            if (cookie == null) {
                return false;
            }

            Conversation conv = cookie.getConversation();

            if (conv == null) {
                return false;
            }
        }

        return true;
    }

    /**
     *
     *
     */
    protected void performAction(Node[] nodes) {
        /*
        CollabSessionCookie sessionCookie=(CollabSessionCookie)
                nodes[0].getCookie(CollabSessionCookie.class);

        assert sessionCookie!=null:
                "CollabSessionCookie was null despite enable check";

        CollabSession session=sessionCookie.getCollabSession();
        */
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(LeaveConversationAction.class, "MSG_LeaveConversationAction_ConfirmLeave"),
                NotifyDescriptor.YES_NO_OPTION
            );

        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION) {
            for (int i = 0; i < nodes.length; i++) {
                ConversationCookie conversationCookie = (ConversationCookie) nodes[i].getCookie(
                        ConversationCookie.class
                    );
                assert conversationCookie != null : "ConversationCookie not found despite enable check";

                Conversation conversation = conversationCookie.getConversation();

                if (conversation != null) {
                    conversation.leave();
                }
            }
        }
    }
}
