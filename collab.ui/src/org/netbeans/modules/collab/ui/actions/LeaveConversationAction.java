/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui.actions;

import com.sun.collablet.CollabManager;
import com.sun.collablet.Conversation;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import java.awt.event.*;

import java.io.*;

import java.util.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.ui.*;
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
