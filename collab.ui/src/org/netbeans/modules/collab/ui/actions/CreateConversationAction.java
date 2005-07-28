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
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;
import com.sun.collablet.Conversation;

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import java.awt.event.*;

import java.io.*;

import java.util.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.*;
import org.netbeans.modules.collab.ui.CollabSessionCookie;
import org.netbeans.modules.collab.ui.ContactCookie;
import org.netbeans.modules.collab.ui.ContactGroupCookie;
import org.netbeans.modules.collab.ui.ConversationCookie;


/**
 *
 *
 */
public class CreateConversationAction extends CookieAction {
    /**
     *
     *
     */
    public String getName() {
        return NbBundle.getMessage(CreateConversationAction.class, "LBL_CreateConversationAction_Name"); // NOI18N
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
        return new Class[] { CollabSessionCookie.class, ContactCookie.class };
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
        // or one or more contact nodes.  We need to discover which mode we're
        // operating in.
        if (nodes.length == 1) {
            if (nodes[0].getCookie(ContactCookie.class) != null) {
                // Enable based on a number of status codes
                ContactCookie contactCookie = (ContactCookie) nodes[0].getCookie(ContactCookie.class);

                return canContact(contactCookie.getContact());
            } else if (nodes[0].getCookie(ContactGroupCookie.class) != null) {
                // Invite the entire group
                ContactGroupCookie cookie = (ContactGroupCookie) nodes[0].getCookie(ContactGroupCookie.class);

                // But only if there is at least one person online in the group
                CollabPrincipal[] contacts = cookie.getContactGroup().getContacts();

                for (int i = 0; i < contacts.length; i++) {
                    if (canContact(contacts[i])) {
                        return true;
                    }
                }

                return false;
            }

            // Filter out certain nodes
            return nodes[0].getCookie(ConversationCookie.class) == null;
        } else {
            // Check to see if all the nodes are contact nodes, and make
            // sure they're all in the same session
            CollabSession session = null;

            for (int i = 0; i < nodes.length; i++) {
                ContactCookie contactCookie = (ContactCookie) nodes[i].getCookie(ContactCookie.class);

                if (contactCookie == null) {
                    return false;
                }

                CollabSessionCookie sessionCookie = (CollabSessionCookie) nodes[i].getCookie(CollabSessionCookie.class);
                assert sessionCookie != null : "CollabSessionCookie was null despite cookie check";

                if (session == null) {
                    session = sessionCookie.getCollabSession();
                }

                if (session != sessionCookie.getCollabSession()) {
                    return false;
                }

                if (canContact(contactCookie.getContact())) {
                    continue;
                } else {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     *
     *
     */
    protected void performAction(Node[] nodes) {
        // The selected nodes can either be the Conversations group node
        // or one or more contact nodes.  We need to discover which mode we're
        // operating in.
        CollabSessionCookie sessionCookie = (CollabSessionCookie) nodes[0].getCookie(CollabSessionCookie.class);
        assert sessionCookie != null : "CollabSessionCookie was null despite enable check";

        CollabSession session = sessionCookie.getCollabSession();

        CollabPrincipal[] invitees = new CollabPrincipal[0];

        if (nodes.length == 1) {
            if (nodes[0].getCookie(ContactCookie.class) != null) {
                // Invite the single contact
                ContactCookie cookie = (ContactCookie) nodes[0].getCookie(ContactCookie.class);
                invitees = new CollabPrincipal[] { cookie.getContact() };
            } else if (nodes[0].getCookie(ContactGroupCookie.class) != null) {
                // Invite the entire group
                ContactGroupCookie cookie = (ContactGroupCookie) nodes[0].getCookie(ContactGroupCookie.class);

                // But only invite the available contacts
                // TODO: Is this really necessary, or would the server
                // just ignore invitations for absent contacts?  What we
                // don't want is the the invitation to be stored until the
                // contact logs on.
                CollabPrincipal[] contacts = cookie.getContactGroup().getContacts();
                List inviteeList = new ArrayList();

                for (int i = 0; i < contacts.length; i++) {
                    if (canContact(contacts[i])) {
                        inviteeList.add(contacts[i]);
                    }
                }

                invitees = (CollabPrincipal[]) inviteeList.toArray(new CollabPrincipal[inviteeList.size()]);
            }
        } else {
            List contacts = new ArrayList();

            for (int i = 0; i < nodes.length; i++) {
                ContactCookie contactCookie = (ContactCookie) nodes[i].getCookie(ContactCookie.class);
                assert contactCookie != null : "ContactCookie not found despite enable check";

                // Add the contact to the list of invitees
                contacts.add(contactCookie.getContact());
            }

            invitees = (CollabPrincipal[]) contacts.toArray(new CollabPrincipal[contacts.size()]);
        }

        // Find or create the conversation
        Conversation conv = findExistingConversation(session, invitees);

        if (conv == null) {
            try {
                // Create a new empty conversation
                conv = session.createConversation();

                // Invite the contacts, if any
                if (invitees.length > 0) {
                    conv.invite(
                        invitees,
                        NbBundle.getMessage(ParticipantSearchForm.class, "MSG_ParticipantSearchForm_InvitationMessage")
                    );
                }
            } catch (CollabException e) {
                Debug.errorManager.notify(e);
            }
        }

        // Open the conversation view
        CollabManager.getDefault().getUserInterface().showConversation(conv);
    }

    /**
     *
     *
     */
    private boolean canContact(CollabPrincipal contact) {
        switch (contact.getStatus()) {
        case CollabPrincipal.STATUS_ONLINE:
        case CollabPrincipal.STATUS_CHAT:
        case CollabPrincipal.STATUS_IDLE:
            return true;

        default:
            return false;
        }
    }

    /**
     * Note, currently, this method will only return conversations that
     * consist of a single participant
     *
     */
    public static Conversation findExistingConversation(CollabSession session, CollabPrincipal[] participants) {
        // Don't try finding a conversation for more than a single participant
        if (participants.length != 1) {
            return null;
        }

        Conversation[] conversations = session.getConversations();

        for (int i = 0; i < conversations.length; i++) {
            CollabPrincipal[] conversationParticipants = conversations[i].getParticipants();

            // Don't bother trying to match big conversations
            if (conversationParticipants.length != 2) {
                continue;
            }

            // Make sure that the current user and the single invitee are
            // the only participants.  If so, then reuse the existing 
            // conversation.
            Set set = new HashSet(Arrays.asList(conversationParticipants));

            if (set.containsAll(Arrays.asList(participants))) {
                return conversations[i];
            }
        }

        return null;
    }
}
