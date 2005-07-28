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

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabSession;
import com.sun.collablet.Conversation;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.*;
import org.netbeans.modules.collab.ui.CollabSessionCookie;
import org.netbeans.modules.collab.ui.ConversationCookie;


public class UnsubscribePublicConversationAction extends NodeAction {
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
        }

        return true;
    }

    public String getName() {
        return NbBundle.getMessage(SubscribeAction.class, "LBL_UnsubscribePublicConversationAction_Name");
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
                CollabSessionCookie sessionCookie = (CollabSessionCookie) nodes[i].getCookie(CollabSessionCookie.class);
                assert sessionCookie != null : "CollabSessionCookie was null despite enable check";

                CollabSession session = sessionCookie.getCollabSession();

                ConversationCookie conversationCookie = (ConversationCookie) nodes[i].getCookie(
                        ConversationCookie.class
                    );

                Conversation conversation = conversationCookie.getConversation();

                if ((conversation != null) && conversation.isValid()) {
                    conversation.leave();
                }

                session.unsubscribePublicConversation(nodes[i].getName());
            } catch (CollabException e) {
                Debug.errorManager.notify(e);
            }
        }

        //		NotifyDescriptor descriptor=new NotifyDescriptor.Confirmation(
        //			NbBundle.getMessage(UnsubscribePublicConversationAction.class,
        //				"MSG_UnsubscribePublicConversationAction_ConfirmUnsubscribe"),
        //			NotifyDescriptor.YES_NO_OPTION);
        //
        //		if (DialogDisplayer.getDefault().notify(descriptor)==
        //			NotifyDescriptor.YES_OPTION)
        //		{
        //			for (int i=0; i<nodes.length; i++) 
        //			{
        //				try {
        //					CollabSessionCookie sessionCookie=(CollabSessionCookie)
        //						nodes[i].getCookie(CollabSessionCookie.class);
        //					assert sessionCookie!=null:
        //						"CollabSessionCookie was null despite enable check";
        //
        //					CollabSession session=sessionCookie.getCollabSession();
        //					ConversationCookie conversationCookie=(ConversationCookie)
        //						nodes[i].getCookie(ConversationCookie.class);
        //					assert conversationCookie!=null:
        //						"ConversationCookie was null despite enable check";
        //
        //					Conversation conv = conversationCookie.getConversation();
        //					//session.unsubscribePublicConversation(conv);
        //					conv.unsubscribe();
        //				}catch (CollabException e)
        //				{
        //					Debug.errorManager.notify(e);
        //				}
        //
        //			}
        //		}
    }
}
