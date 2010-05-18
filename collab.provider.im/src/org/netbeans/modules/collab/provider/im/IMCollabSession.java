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
package org.netbeans.modules.collab.provider.im;

import com.sun.collablet.Account;
import com.sun.collablet.CollabException;
import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;
import com.sun.collablet.CollabletFactoryManager;
import com.sun.collablet.ContactGroup;
import com.sun.collablet.Conversation;
import com.sun.collablet.ConversationPrivilege;

import java.beans.*;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.openide.util.NbBundle;

import org.netbeans.lib.collab.*;

import org.netbeans.modules.collab.core.Debug;


/**
 *
 *
 * @author  Todd Fast <todd.fast@sun.com>
 */
public class IMCollabSession extends Object implements CollabSession, CollaborationSessionListener,
    InviteMessageStatusListener, PresenceServiceListener, PersonalStoreServiceListener, NotificationServiceListener,
    ConferenceServiceListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private IMCollabManager manager;
    private CollaborationSession session;
    private PresenceService presenceService;
    private NewsService newsService;
    private ConferenceService conferenceService;
    private NotificationService messageService;
    private PersonalStoreService personalStoreService;
    private final Object SESSION_LOCK = new Object();
    private Account currentLogin;
    private IMCollabPrincipal userPrincipal;
    private List conversations = Collections.synchronizedList(new ArrayList());
    private List subscribedConversations = Collections.synchronizedList(new ArrayList());
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private Map principals = new HashMap();
    private IMContactList contactList;
    private boolean isLoaded = false;
    private PrivacyList visibleToAll = null;
    private PrivacyList invisibleToAll = null;
    private final String INVISIBLE_TO_ALL = NbBundle.getMessage(
            IMCollabSession.class, "LBL_IMCollabSession_InvisibleToAll"
        );
    private final String VISIBLE_TO_ALL = NbBundle.getMessage(
            IMCollabSession.class, "LBL_IMCollabSession_VisibleToAll"
        );
    private ConversationPrivilege[] _tempPrivileges = new ConversationPrivilege[0];
    private boolean criticalServerError = false;

    /**
     *
     *
     */
    protected IMCollabSession(IMCollabManager manager, Account account) {
        super();
        this.manager = manager;
        this.currentLogin = account;
    }

    /**
     *
     *
     */
    public CollabManager getManager() {
        return manager;
    }

    /**
     * Associates this session object with the provider session
     *
     */
    protected void attachSession(CollaborationSession session)
    throws CollabException, CollaborationException {
        this.session = session;

        // Allocation the personal store session first, so we can properly
        // look up our own principal.  NOTE: This is highly order dependent
        // and must be done before the remaining sessions are allocated
        // (particularly the presence session)
        userPrincipal = (IMCollabPrincipal) getPrincipal(session.getPrincipal());
        userPrincipal.setDisplayName(getDisplayName());

        personalStoreService = session.getPersonalStoreService();
        conferenceService = session.getConferenceService();
        presenceService = session.getPresenceService();
        newsService = session.getNewsService();

        personalStoreService.initialize(this);
        presenceService.initialize(this);
        conferenceService.initialize(this);

        String v = (String)personalStoreService.getProfile().getProperty("licenseKey", ""); // NOI18N
        if (v.trim().length()==0) {
            personalStoreService.getProfile().setProperty("licenseKey", getIDEVersion()); // NOI18N
            personalStoreService.getProfile().save();
        }
        String forumManage = personalStoreService.getProfile().getProperty("sunIMAllowForumManage", "deny"); // NOI18N

        userPrincipal.setConversationAdminRole(forumManage.equals("allow") ? true : false);

        // Get the current user's principal
        //		userPrincipal=(IMCollabPrincipal)getPrincipal(
        //			StringUtility.removeResource(
        //			session.getPrincipal().getUID()));
        // Create the contact list
        contactList = new IMContactList(this);

        // Republish events from the contact list
        contactList.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    changeSupport.firePropertyChange(event.getPropertyName(), event.getOldValue(), event.getNewValue());
                }
            }
        );

        contactList.load();
        isLoaded = true;

        loadSubscribedConversations();
        setVisibleToAll();

        // Set user online
        publishStatus(CollabPrincipal.STATUS_ONLINE, ""); // NOI18N
    }

    private String getIDEVersion () {
        String str = ""; // NOI18N
        try {
            str = MessageFormat.format(
                NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion"), // NOI18N
                new Object[] {System.getProperty("netbeans.buildnumber")} // NOI18N
            );
        } catch (MissingResourceException ex) {
            // Ignore
            Debug.debugNotify(ex);
        }
        return str;
    }

    /**
     *
     *
     */
    public CollabPrincipal getUserPrincipal() {
        return userPrincipal;
    }

    /**
     *
     *
     */
    public String getDisplayName() {
        return NbBundle.getMessage(
            IMCollabSession.class, "LBL_IMCollabSession_DisplayName", 
        //			getUserPrincipal().getDisplayName());
        getAccount().getDisplayName()
        );
    }

    ////////////////////////////////////////////////////////////////////////////
    // Session management methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public boolean isValid() {
        // TODO: Add additional checks here
        return isLoggedIn();
    }

    /**
     *
     *
     */
    public boolean isLoggedIn() {
        return session != null;
    }

    /**
     *
     *
     */
    public Account getAccount() {
        return currentLogin;
    }

    /**
     *
     *
     */
    public void logout() {
        try {
            synchronized (SESSION_LOCK) {
                if (session != null) {
                    // Leave all conversations
                    Conversation[] conversations = getConversations();

                    for (int i = 0; i < conversations.length; i++) {
                        try {
                            conversations[i].leave();
                        } catch (Exception e) {
                            // Ignore
                            Debug.debugNotify(e);
                        }
                    }

                    // Set the user offline
                    try {
                        if (!skipSend()) {
                            publishStatus(CollabPrincipal.STATUS_OFFLINE, ""); // NOI18N
                        }
                    } catch (Exception e) {
                        // Ignore
                        Debug.debugNotify(e);
                    }

                    // Logout from the conference session
                    try {
                        conferenceService = null;
                        personalStoreService = null;
                        presenceService = null;
                    } catch (Exception e) {
                        // Ignore
                        Debug.debugNotify(e);
                    }

                    // Logout from the collaboration session
                    try {
                        session.logout();
                        session = null;
                    } catch (Exception e) {
                        // Ignore
                        Debug.debugNotify(e);
                    }

                    Debug.out.println("Logged out successfully");
                }
            }
        } catch (Exception e) {
            // Ignore
            Debug.debugNotify(e);
        }

        // Remove ourselves from the manager
        ((IMCollabManager) getManager()).removeSession(this);
    }

    ////////////////////////////////////////////////////////////////////////////
    // IM session accessors
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public CollaborationSession getCollaborationSession() {
        return session;
    }

    /**
     *
     *
     */
    public PresenceService getPresenceService() {
        return presenceService;
    }

    /**
     *
     *
     */
    public NewsService getNewsService() {
        return newsService;
    }

    /**
     *
     *
     */
    public ConferenceService getConferenceService() {
        return conferenceService;
    }

    /**
     *
     *
     */
    public NotificationService getMessageService() {
        return messageService;
    }

    /**
     *
     *
     */
    public PersonalStoreService getPersonalStoreService() {
        return personalStoreService;
    }

    /**
     *
     *
     */
    protected IMContactList getContactList() {
        return contactList;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Conversation management methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public Conversation createConversation() throws CollabException {
        try {
            // TODO: Assume management access level for now; should this ever
            // be anything different?
            //			IMConversation conversation=new IMConversation(this);
            //			Conference conference=getConferenceService().setupConference(
            //				conversation,Conference.MANAGE);
            //			conversation.attachConference(conference);
            IMConversation conversation = new IMConversation(this, true);

            // Track this conversation
            addConversation(conversation);

            // Configure the channels for this conversation. We do this
            // after add the conversation to out list so that it has a chance
            // to fire channel change messages to listeners (not that this
            // is expected to be very likely, but it helps maintainability to 
            // preserve proper semantics).
            CollabletFactoryManager.getDefault().configureChannels(conversation);

            return conversation;
        } catch (CollaborationException e) {
            CollabException ex = new CollabException(e, "Could not create new confersation");
            throw ex;
        }
    }

    /**
     *
     *
     */
    public Conversation[] getConversations() {
        return (Conversation[]) conversations.toArray(new Conversation[conversations.size()]);
    }

    /**
     *
     *
     */
    public String[] getSubscribedPublicConversations() {
        return (String[]) subscribedConversations.toArray(new String[subscribedConversations.size()]);
    }

    /**
     *
     *
     */
    protected void addConversation(Conversation conversation) {
        conversations.add(conversation);

        // TODO: Include old/new conversation arrays
        changeSupport.firePropertyChange(PROP_CONVERSATIONS, null, null);
    }

    /**
     * Note, this method does not leave the conversation; it only removes it
     * from the internal list of current conversations.
     *
     */
    protected void removeConversation(Conversation conversation) {
        conversations.remove(conversation);

        // TODO: Include old/new conversation arrays
        changeSupport.firePropertyChange(PROP_CONVERSATIONS, null, null);
    }

    /**
     *
     *
     */
    protected void addSubscribedConversation(String conversationName) {
        subscribedConversations.add(conversationName);

        // TODO: Include old/new conversation arrays
        changeSupport.firePropertyChange(PROP_CONVERSATIONS, null, null);
    }

    /**
     * Note, this method does not leave the conversation; it only removes it
     * from the internal list of current conversations.
     *
     */
    protected void removeSubscribedConversation(String conversationName) {
        subscribedConversations.remove(conversationName);

        // TODO: Include old/new conversation arrays
        changeSupport.firePropertyChange(PROP_CONVERSATIONS, null, null);
    }

    /**
     *
     *
     */
    public String[] findPublicConversations(int searchType, String pattern) {
        try {
            int type = personalStoreService.SEARCHTYPE_CONTAINS;

            switch (searchType) {
            case CollabSession.SEARCHTYPE_CONTAINS:
                type = personalStoreService.SEARCHTYPE_CONTAINS;

                break;

            case CollabSession.SEARCHTYPE_ENDSWITH:
                type = personalStoreService.SEARCHTYPE_ENDSWITH;

                break;

            case CollabSession.SEARCHTYPE_EQUALS:
                type = personalStoreService.SEARCHTYPE_EQUALS;

                break;

            case CollabSession.SEARCHTYPE_STARTSWITH:
                type = personalStoreService.SEARCHTYPE_STARTSWITH;
            }

            PersonalStoreEntry[] entries = personalStoreService.search(type, pattern, PersonalStoreEntry.CONFERENCE);

            if (entries == null) {
                return null;
            }

            //			Conference[] conferences=new Conference[entries.length];
            //			IMConversation[] conversations=new IMConversation[entries.length];
            List conversations = new ArrayList();

            String[] ret = new String[entries.length];
            
            for (int i = 0; i < entries.length; i++) {
//              Conference conference = getConferenceService().getPublicConference(entries[i].getEntryId());
                ret[i] = entries[i].getEntryId();
            }

            return ret;
        } catch (CollaborationException ne) {
            Debug.debugNotify(ne);

            return null;
        }
    }

    /**
     *
     *
     */
    public String[] getPublicConversations() {
        return findPublicConversations(PersonalStoreService.SEARCHTYPE_EQUALS, "*");
    }

    /**
     *
     *
     */
    public void loadSubscribedConversations() throws CollabException {
        try {
            Collection conferences = getPersonalStoreService().getEntries(PersonalStoreEntry.CONFERENCE);
            Iterator it = new ArrayList(conferences).iterator();

            while (it.hasNext()) {
                PersonalConference entry = (PersonalConference) it.next();
                String address = entry.getAddress();
                Conference con = getConferenceService().getPublicConference(address);

                //				if (publicConversationExists(address))
                if (con != null) {
                    //					Conference conf = getConferenceService().getPublicConference(address);
                    addSubscribedConversation(con.getDestination());
                } else {
                    unsubscribePublicConversation(address);
                }
            }
        } catch (CollaborationException e) {
            throw new CollabException(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Service listener methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void onInvite(Conference conference, InviteMessage message) {
        try {
            //skip non-invite type messages
            //			if(!acceptMessage(message))
            //			{		    
            //				return;
            //			}
            //workaround for bug#6269975
            //return if not proper invitation message
            String inviteMsg = message.getContent();

            if ((inviteMsg != null) && (inviteMsg.indexOf("<?xml") != -1)) {
                return;
            }

            // Prompt user to join the conference
            boolean join = getManager().getUserInterface().acceptConversation(
                    this, getPrincipal(StringUtility.removeResource(message.getOriginator())),
                    conference.getDestination(), inviteMsg
                );

            // Repond as appropriate
            message.rsvp(join);

            if (join) {
                // Spawn a new listener for conference events
                IMConversation conversation = new IMConversation(this, conference);
                conference.join(conversation);

                // Add the conference to our internal list
                if (subscribedConversations.contains(conference.getDestination())) {
                    changeSupport.firePropertyChange(PROP_PUBLIC_CONVERSATIONS, null, conversation);
                }

                addConversation(conversation);

                // Configure the channels for this conversation. We do this
                // after add the conversation to out list so that it has a 
                // chance to fire channel change messages to listeners (not 
                // that this is expected to be very likely, but it helps 
                // maintainability to preserve proper semantics).
                CollabletFactoryManager.getDefault().configureChannels(conversation);

                // Make sure the conversation is showing
                getManager().getUserInterface().showConversation(conversation);
            }
        } catch (CollabException e) {
            // TODO: Need proper error handling
            Debug.errorManager.notify(e);
        } catch (CollaborationException e) {
            // TODO: Need proper error handling
            Debug.errorManager.notify(e);
        }
    }

    /**
     *
     *
     */
    public boolean acceptMessage(InviteMessage message)
    throws CollabException {
        //return true;
        // TODO: How do we discriminate chat messages from any other message?
        // TODO: Temporary impl
        try {
            //			String messageContent=message.getContent();
            //			if (messageContent==null || messageContent.trim().length()==0)
            //			{
            //				return true;
            //			}
            String channelType = message.getHeader("x-channel");

            if ((channelType != null) && "chat".equals(channelType)) {
                return true;
            }
        } catch (Exception e) {
            throw new CollabException(e);
        }

        return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Property change methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     *
     *
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    ////////////////////////////////////////////////////////////////////////////
    // CollaborationSessionListener methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void onError(CollaborationException e) {
        if (e!=null && e.getMessage()!=null && e.getMessage().indexOf("Server Disconnected") != -1) { //NOI18N
            criticalServerError = true;
	    ((IMCollabManager)getManager()).getReconnect().startReconnect(this);

            javax.swing.SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        logout();
                    }
                }
            );
        } else {
            e.printStackTrace(); // System.err -> ide.log
            Debug.logDebugException("onError called with exception", e, true);

            // Show a friendly user error
            getManager().getUserInterface().notifySessionError(this, e.getMessage());
        }
    }

    /**
     *
     *
     */
    public void onRsvp(String destination, boolean accepted) {
        onRsvp(destination, null, accepted);
    }

    /**
     *
     *
     */
    public void onRsvp(String destination, Message message, boolean accepted) {
        try {
            if (!accepted) {
                String msg = (message == null) ? "" : message.getContent();
                CollabPrincipal principal = getPrincipal(StringUtility.removeResource(destination));
                String decliner = principal.getDisplayName();

                // Notify user
                getManager().getUserInterface().notifyInvitationDeclined(decliner, msg);

                // Remove the decliner from the invited participant list.
                // Note, this is only a 90% solution, since we actually
                // don't know which conversation to remove them from.  In
                // practice, however, it's unlikely we'll have more than
                // one conversation on the same client inviting the same user,
                // and even if so, the lost of the invited user is not really
                // a significant issue.
                Conversation[] conversations = getConversations();

                for (int i = 0; i < conversations.length; i++) {
                    ((IMConversation) conversations[i]).removeInvitedParticipant(principal);
                }
            }
        } catch (CollaborationException e) {
            Debug.debugNotify(e);
        } catch (CollabException ce) {
            Debug.errorManager.notify(ce);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // ContactGroup methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public ContactGroup[] getContactGroups() {
        return getContactList().getContactGroups();
    }

    /**
     *
     *
     */
    public ContactGroup getContactGroup(String name) {
        return getContactList().getContactGroup(name);
    }

    /**
     *
     *
     */
    public ContactGroup createContactGroup(String name)
    throws CollabException {
        ContactGroup group = getContactList().getContactGroup(name);

        if (group != null) {
            throw new CollabException("Contact group \"" + name + // NOI18N
                "\" already exists"
            ); // NOI18N
        }

        group = new IMContactGroup(getContactList(), name);
        getContactList().addContactGroup(group);

        return group;
    }

    /**
     *
     *
     */
    public CollabPrincipal[] findPrincipals(int searchType, String pattern)
    throws CollabException {
        try {
            int type = PersonalStoreService.SEARCHTYPE_CONTAINS;

            switch (searchType) {
            case CollabSession.SEARCHTYPE_CONTAINS:
                type = PersonalStoreService.SEARCHTYPE_CONTAINS;

                break;

            case CollabSession.SEARCHTYPE_ENDSWITH:
                type = PersonalStoreService.SEARCHTYPE_ENDSWITH;

                break;

            case CollabSession.SEARCHTYPE_EQUALS:
                type = PersonalStoreService.SEARCHTYPE_EQUALS;

                break;

            case CollabSession.SEARCHTYPE_STARTSWITH:
                type = PersonalStoreService.SEARCHTYPE_STARTSWITH;
            }

            assert getPersonalStoreService() != null : "personalStoreService is null";

            // Search for principals
            CollaborationPrincipal[] principals = getPersonalStoreService().searchPrincipals(type, pattern);

            if (principals == null) {
                principals = new CollaborationPrincipal[0];
            }

            // Canonicalize the principal array
            CollabPrincipal[] result = new CollabPrincipal[principals.length];

            for (int i = 0; i < principals.length; i++)
                result[i] = getPrincipal(principals[i]);

            return result;
        } catch (CollaborationException e) {
            throw new CollabException(e, "Exception during principal search"); // NOI18N
        }
    }

    //	/**
    //	 *
    //	 *
    //	 */
    //	public void importContactList(File file) {
    ////	    buddyListManager.importContactList(file);
    //
    //		// TAF: We should do this from somewhere else I think
    ////	    changeSupport.firePropertyChange(PROP_CONTACTS,null,null);
    //	}
    ////////////////////////////////////////////////////////////////////////////
    // PresenceListener methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void onPresenceNotify(String presentityURL, String presenceInfo, java.util.Date expires) {
        //Debug.out.println("A presence notification was received");
        // TODO: implement onPresenceNotify
        // TAF: When does this occur?
    }

    /**
     *
     *
     */
    public void onPresence(Presence presence) {
        //		if (!isLoaded)
        //		{
        //			return;
        //		}
        Object[] tuples = presence.getTuples().toArray();
        PresenceTuple tuple = (PresenceTuple) tuples[0];

        String uid = StringUtility.removeResource(tuple.getContact());

        // Ignore presence from ourselves
        //		if (uid.equals(getUserPrincipal().getIdentifier()))
        //			return;
        // ignore presence from those just deleted from roster list
        //		try
        //		{
        //			PersonalStoreEntry entry = getPersonalStoreService().
        //				getEntry(PersonalStoreEntry.CONTACT, uid);
        //			if (entry==null)
        //				return;
        //		}
        //		catch (NullPointerException e)
        //		{
        //			Debug.debugNotify(e);
        //		}
        //		catch (CollaborationException ce)
        //		{
        //			// ignore, 
        //			return;
        //		}
        // Find the principal
        try {
            CollabPrincipal principal = getPrincipal(uid);

            // Handle user status
            String status = tuple.getStatus();
            String reason = tuple.getNote();
            setPrincipalStatus(principal, status);
        } catch (CollabException e) {
            // TODO: What should we do here?
            Debug.errorManager.notify(e);
        }
    }

    /**
     *
     *
     */
    public void onSubscribeRequest(Presence presence) {
        Object[] tuples = presence.getTuples().toArray();
        assert (tuples != null) && (tuples.length > 0) : "Presence tuple array was null or empty";

        PresenceTuple tuple = (PresenceTuple) tuples[0];

        try {
            /*
            String uid=StringUtility.getLocalPartFromAddress(
                    tuple.getContact());
             */
            String uid = tuple.getContact();
            CollabPrincipal principal = getPrincipal(StringUtility.removeResource(uid));

            boolean approve = getManager().getUserInterface().approvePresenceSubscription(this, principal);

            if (approve) {
                getPresenceService().authorize(uid);
            } else {
                getPresenceService().cancel(uid);
            }
        } catch (CollaborationException ce) {
            Debug.logDebugException("Exception in onSubscribeRequest", ce, true);
        } catch (CollabException e) {
            // TODO: Need proper error dialog here
            //			Debug.errorManager.notify(e);
            Debug.logDebugException("Exception in onSubscribeRequest", e, true);
        }
    }

    /**
     *
     *
     */
    public void onSubscribed(Presence presence) {
        getContactList().refreshWatcherGroup();
    }

    /**
     *
     *
     */
    public boolean onUnsubscribeRequest(Presence presence) {
        getContactList().refreshWatcherGroup();

        // TODO: Should we prompt user here?
        return true;
    }

    /**
     * This callback is invoked to indicate unsubscribing is successful
     *
     */
    public void onUnsubscribed(Presence presence) {
        Object[] tuples = presence.getTuples().toArray();
        assert (tuples != null) && (tuples.length > 0) : "Presence tuple array was null or empty";

        PresenceTuple tuple = (PresenceTuple) tuples[0];
        String uid = StringUtility.removeResource(tuple.getContact());
        String status = tuple.getStatus();

        try {
            CollabPrincipal principal = getPrincipal(uid);
            int oldStatus = principal.getStatus();
            principal.setStatus(CollabPrincipal.STATUS_UNKNOWN);

            /* If the status is 'pending', it's triggered by subscription denial
            *  otherwise, it's triggered by current user unsubscribing to contact
            *  in his roster
            */

            //			if (principal.getStatus() != CollabPrincipal.STATUS_PENDING)
            if (oldStatus != CollabPrincipal.STATUS_PENDING) {
                return;
            }

            getManager().getUserInterface().notifySubscriptionDenied(principal);
        } catch (CollabException e) {
            Debug.errorManager.notify(e);
        }
    }

    public void onUnsubscribe(Presence p) {
    }

    ////////////////////////////////////////////////////////////////////////////
    // NotificationServiceListener methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void onMessage(Message message) {
        // TODO: Implement onMessage (when does this occur?)
        Debug.out.println("Session message: " + message);
    }

    ////////////////////////////////////////////////////////////////////////////
    // PersonalStoreServiceListener methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *invoked when an change of event for PersonalStoreEntry happens
     */
    public void onEvent(PersonalStoreEvent personalStoreEvent) {
        //		Debug.out.println(" personal store entry changed");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Principal management methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected CollabPrincipal getPrincipal(CollaborationPrincipal principal)
    throws CollabException {
        CollabPrincipal result = (CollabPrincipal) principals.get(principal.getUID());

        if (result != null) {
            return result;
        }

        result = new IMCollabPrincipal(this, principal);

        //		try
        //		{
        //			Presence presence = getPresenceService().
        //									fetchPresence(principal.getUID());
        //			Object[] tuples=presence.getTuples().toArray();
        //			assert tuples!=null && tuples.length>0:
        //						"Presence tuple array was null or empty";
        //
        //			PresenceTuple tuple=(PresenceTuple)tuples[0];
        //			setPrincipalStatus(result, tuple.getStatus());
        //		}
        //		catch (CollaborationException ce)
        //		{
        //			throw new CollabException(ce,"Could not obtain status for principal "+ // NOI18N
        //				"for identifier \""+principal.getUID()+"\""); // NOI18N
        //		}
        addPrincipalToCache(result);

        return result;
    }

    /**
     *
     *
     */
    public CollabPrincipal getPrincipal(String identifier)
    throws CollabException {
        try {
            synchronized (principals) {
                CollabPrincipal element = (CollabPrincipal) principals.get(identifier);

                if (element != null) {
                    return element;
                }

                //				PersonalStoreEntry entry=getPersonalStoreSession().getEntry(
                //					PersonalStoreEntry.CONTACT,identifier);
                //				if (entry!=null)
                //				{
                //Debug.out.println("- Found as entry = "+identifier);
                //					element=new IMCollabPrincipal(this,entry.getPrincipal());
                //				}
                //				else
                //				{
//                CollaborationPrincipal[] users = getPersonalStoreService().searchPrincipals(
//                        PersonalStoreService.SEARCHTYPE_EQUALS, StringUtility.getLocalPartFromAddress(identifier)
//                    );

//                if ((users != null) && (users.length > 0)) {
//                    element = new IMCollabPrincipal(this, users[0]);
//                }

                if (element == null) {
                    element = new IMCollabPrincipal(this, getCollaborationSession().createPrincipal(identifier));
                }

                //				}
                // fetch presence and set principal's initial status
                //				Presence presence = getPresenceService().fetchPresence(identifier);
                //				Object[] tuples=presence.getTuples().toArray();
                //				assert tuples!=null && tuples.length>0:
                //					"Presence tuple array was null or empty";
                //
                //				PresenceTuple tuple=(PresenceTuple)tuples[0];
                //				setPrincipalStatus(element, tuple.getStatus());
                // Debug.out.println(" fetch presence of " + tuple.getContact() + " , status: "
                //	+ tuple.getStatus());
                addPrincipalToCache(element);

                return element;
            }
        } catch (CollaborationException e) {
            Debug.logDebugException("Could not obtain principal", e, true);

            return null;
        }
    }

    /**
     *
     *
     */
    protected void addPrincipalToCache(CollabPrincipal principal) {
        if (principal == null) {
            return;
        }

        principals.put(principal.getIdentifier(), principal);
    }

    /**
     *
     *
     */
    protected void addPrincipalsToCache(CollabPrincipal[] p) {
        if (p == null) {
            return;
        }

        for (int i = 0; i < p.length; i++)
            addPrincipalToCache(p[i]);
    }

    /**
     *
     *
     */
    protected boolean isPrincipalCached(String identifier) {
        return principals.containsKey(identifier);
    }

    /**
     *
     *
     */
    protected void clearPrincipalCache() {
        principals.clear();
    }

    /**
     *
     *
     */
    public void publishStatus(int status, String reason)
    throws CollabException {
        if (getUserPrincipal() == null) {
            return;
        }

        try {
            PresenceTuple tuple = new PresenceTuple(getUserPrincipal().getIdentifier());

            switch (status) {
            case CollabPrincipal.STATUS_ONLINE:
                tuple.setStatus(PresenceService.STATUS_OPEN);
                getUserPrincipal().setStatus(status);

                break;

            case CollabPrincipal.STATUS_OFFLINE:
                tuple.setStatus(PresenceService.STATUS_CLOSED);
                getUserPrincipal().setStatus(status);

                break;

            case CollabPrincipal.STATUS_BUSY:
                tuple.setStatus(PresenceService.STATUS_BUSY);
                getUserPrincipal().setStatus(status);

                break;

            case CollabPrincipal.STATUS_AWAY:
                tuple.setStatus(PresenceService.STATUS_AWAY);
                getUserPrincipal().setStatus(status);

                break;

            case CollabPrincipal.STATUS_IDLE:
                tuple.setStatus(PresenceService.STATUS_IDLE);
                getUserPrincipal().setStatus(status);

                break;

            default:
                tuple.setStatus(PresenceService.STATUS_OTHER);
                getUserPrincipal().setStatus(status);
            }

            tuple.addNote(reason);

            Presence presence = new Presence(getUserPrincipal().getIdentifier());
            presence.addTuple(tuple);
            getPresenceService().publish(presence);
        } catch (CollaborationException e) {
            throw new CollabException(e);
        }
    }

    /**
     *
     *
     */
    protected void setPrincipalStatus(CollabPrincipal principal, String status) {
        if (status.equals(PresenceService.STATUS_OPEN)) {
            //Debug.out.println("Presence of "+principal+" is online");
            principal.setStatus(CollabPrincipal.STATUS_ONLINE);
        } else if (status.equals(PresenceService.STATUS_CLOSED)) {
            //Debug.out.println("Presence of "+principal+" is offline");
            principal.setStatus(CollabPrincipal.STATUS_OFFLINE);
        } else if (status.equals(PresenceService.STATUS_IDLE)) {
            //Debug.out.println("Presence of "+principal+" is idle");
            principal.setStatus(CollabPrincipal.STATUS_IDLE);
        } else if (status.equals(PresenceService.STATUS_AWAY)) {
            //Debug.out.println("Presence of "+principal+" is away");
            principal.setStatus(CollabPrincipal.STATUS_AWAY);
        } else if (status.equals(PresenceService.STATUS_BUSY)) {
            //Debug.out.println("Presence of "+principal+" is busy");
            principal.setStatus(CollabPrincipal.STATUS_BUSY);
        } else if (status.equals(PresenceService.STATUS_CHAT)) {
            //Debug.out.println("Presence of "+principal+" is chat");
            principal.setStatus(CollabPrincipal.STATUS_CHAT);
        }
        /*
                else
                if (status.equals(PresenceService.STATUS_WATCHED))
                {
                        Debug.out.println("Presence of "+principal+" is watched");
                        principal.setStatus(CollabPrincipal.STATUS_WATCHED);
                }
         */
        else if (status.equals(PresenceService.STATUS_PENDING)) {
            //Debug.out.println("Presence of "+principal+" is pending");
            principal.setStatus(CollabPrincipal.STATUS_PENDING);
        } else {
            //Debug.out.println("Presence of "+principal+" is unknown");
            principal.setStatus(CollabPrincipal.STATUS_UNKNOWN);
        }
    }

    public void setInvisibleToAll() throws CollabException {
        try {
            //			PrivacyList invisibleToAll =
            //				getCollaborationSession().getPrivacyList(privacyListName);
            if (invisibleToAll == null) {
                invisibleToAll = getCollaborationSession().createPrivacyList(INVISIBLE_TO_ALL);

                PrivacyItem item = invisibleToAll.createPrivacyItem(null, PrivacyItem.DENY);
                item.setResource(PrivacyItem.PRESENCE_OUT);
                invisibleToAll.addPrivacyItem(item);
                getCollaborationSession().addPrivacyList(invisibleToAll);
            }

            getCollaborationSession().setActivePrivacyListName(INVISIBLE_TO_ALL);
        } catch (CollaborationException e) {
            // swallow it - many servers don't support privacy list
            // throw new CollabException(e);
        }
    }

    public void setVisibleToAll() throws CollabException {
        try {
            //			PrivacyList visibleToAll = 
            //				getCollaborationSession().getPrivacyList(privacyListName);
            if (visibleToAll == null) {
                visibleToAll = getCollaborationSession().createPrivacyList(VISIBLE_TO_ALL);

                PrivacyItem item = visibleToAll.createPrivacyItem(null, PrivacyItem.ALLOW);
                item.setResource(PrivacyItem.PRESENCE_OUT);
                visibleToAll.addPrivacyItem(item);
                getCollaborationSession().addPrivacyList(visibleToAll);
            }

            getCollaborationSession().setActivePrivacyListName(VISIBLE_TO_ALL);
        } catch (CollaborationException e) {
            // swallow it - many servers don't support privacy list
            // throw new CollabException(e);
        }
    }

    /**
     *
     *
     */
    public Conversation createPublicConversation(String name)
    throws CollabException {
        Iterator it = conversations.iterator();

        while (it.hasNext()) {
            Conversation conv = (Conversation) it.next();

            if (conv.getIdentifier().equals(name)) {
                return conv;
            }
        }

        try {
            IMConversation conv = new IMConversation(this);

            // Find the conference, this api has a bug, it returns null only on 
            // the first time when the unauthorized user queries it, so switch to
            // use publicConversationExists(name)
            //			Conference conf=getConferenceService().getPublicConference(name);
            // If it doesn't exist, create it
            boolean created = false;
            Conference conf;

            if (!publicConversationExists(name))//			if (conf==null)
             {
                // Create the conversation
                conf = getConferenceService().setupPublicConference(name, conv, Conference.LISTEN);
                created = true;
            } else {
                conf = getConferenceService().getPublicConference(name);
            }

            // Attach the conference to the conversation
            conv.attachConference(conf);

            if (created) {
                conv.subscribe();
            }

            conv.join();
            conversations.add(conv);
            changeSupport.firePropertyChange(PROP_PUBLIC_CONVERSATIONS, null, conv);

            return conv;
        } catch (CollaborationException e) {
            //throw new CollabException(e);
            Debug.logDebugException("Could not create the public conversation", e, false);

            return null;
        }
    }

    /**
     *
     *
     */
    public void subscribePublicConversation(String name)
    throws CollabException {
        try {
            if (subscribedConversations.contains(name)) {
                return;
            }

            PersonalConference pc = (PersonalConference) getPersonalStoreService().createEntry(
                    PersonalStoreEntry.CONFERENCE, name
                );
            pc.setAddress(name);
            pc.save();
            addSubscribedConversation(name);
        } catch (CollaborationException ce) {
            throw new CollabException(ce, ce.getMessage());
        }
    }

    /**
     *
     *
     *
     */
    public void unsubscribePublicConversation(String name)
    throws CollabException {
        try {
            PersonalConference pc = (PersonalConference) getPersonalStoreService().getEntry(
                    PersonalStoreEntry.CONFERENCE, name
                );

            if (pc != null) {
                ((PersonalStoreEntry) pc).remove();
            }

            //setValid(false);
            removeSubscribedConversation(name);
        } catch (CollaborationException ce) {
            throw new CollabException(ce, ce.getMessage());
        }
    }

    /**
     * find the public conversation by its name
     * get the privileges of all the users affiliated with this public
     * conversation
     *
     * return array of ConversationPrivilege objects
     */
    public ConversationPrivilege[] getPublicConversationPrivileges(String name)
    throws CollabException {
        ArrayList result = new ArrayList();

        try {
            Map map = getConferenceService().getPublicConference(name).listPrivileges();
            Iterator it = map.keySet().iterator();

            while (it.hasNext()) {
                String uid = (String) it.next();
                CollabPrincipal principal = getPrincipal(uid);
                int privilege = ((Integer) map.get(uid)).intValue();
                int access = convertPrivilegeToAccess(privilege);

                ConversationPrivilege conversationPrivilege = new ConversationPrivilege(principal, access);
                result.add(conversationPrivilege);

                //				Debug.out.println(" public conf privilege: " + principal + 
                //									"with access level " + access);
            }
        } catch (CollaborationException ce) {
            throw new CollabException(ce);
        }

        return (ConversationPrivilege[]) result.toArray(new ConversationPrivilege[result.size()]);
    }

    /**
     *
     *
     */
    public void setPublicConversationPrivileges(String name, ConversationPrivilege[] privileges)
    throws CollabException {
        if (!canManagePublicConversation(name)) {
            return;
        }

        try {
            Conference conf = getConferenceService().getPublicConference(name);
            Map map = new HashMap();

            for (int i = 0; i < privileges.length; i++) {
                String uid = privileges[i].getPrincipal().getIdentifier();
                int access = privileges[i].getAccess();

                //				Debug.out.println("Setting privilege: "+uid + "with access:" + access);
                map.put(uid, new Integer(convertAccessToPrivilege(access)));
            }

            conf.setPrivileges(map);
        } catch (CollaborationException ce) {
            Debug.logDebugException("Exception in setPublicConversationPrivileges", ce, true);
            throw new CollabException(ce, ce.getMessage());
        }
    }

    /**
    * find the public conversation previlige of conversation=name and principal
    * get the privileges of all the users affiliated with this public
    * conversation
    *
    * return array of ConversationPrivilege objects
    */
    public ConversationPrivilege getPublicConversationPrivilege(String name, CollabPrincipal forPrincipal)
    throws CollabException {
        try {
            Map map = getConferenceService().getPublicConference(name).listPrivileges();
            Iterator it = map.keySet().iterator();

            while (it.hasNext()) {
                String uid = (String) it.next();
                CollabPrincipal principal = getPrincipal(uid);

                //Debug.out.println(" uid: " + uid + " principal: "+principal.getIdentifier());                                 
                if ((forPrincipal != null) && forPrincipal.getIdentifier().equals(uid)) {
                    int privilege = ((Integer) map.get(uid)).intValue();
                    int access = convertPrivilegeToAccess(privilege);

                    //Debug.out.println(" public conf privilege: " + principal +  
                    //	"with access level " + access); 
                    ConversationPrivilege conversationPrivilege = new ConversationPrivilege(principal, access);

                    return conversationPrivilege;
                }
            }
        } catch (CollaborationException ce) {
            throw new CollabException(ce);
        }

        return getPublicConversationDefaultPrivilege(name);
    }

    /**
     *
     *
     */
    public boolean canManagePublicConversation(String name) {
        boolean canManage = false;

        if (!userPrincipal.hasConversationAdminRole()) return false;
        try {
            Conference conf = getConferenceService().getPublicConference(name);

            if (conf != null) {
                canManage = conf.hasPrivilege(Conference.MANAGE);
            }
        } catch (CollaborationException ce) {
            //			Debug.logDebugException(
            //				"Exception in canManagePublicConversation",ce,true);
        }

        return canManage;
    }

    /**
     *
     *
     */
    public ConversationPrivilege getPublicConversationDefaultPrivilege(String name)
    throws CollabException {
        int access;

        try {
            int privilege = getConferenceService().getPublicConference(name).getDefaultPrivilege();
            access = convertPrivilegeToAccess(privilege);
        } catch (CollaborationException e) {
            throw new CollabException(e);
        }

        return new ConversationPrivilege(null, access);
    }

    /** helper method to convert access level defined in IM api Conference
     * to access level defined in ConversationPrivilege
     *
     */
    public int convertPrivilegeToAccess(int privilege) {
        if ((privilege & Conference.MANAGE) != 0) return ConversationPrivilege.MANAGE;
        if ((privilege & Conference.PUBLISH) != 0) return ConversationPrivilege.WRITE;
        if ((privilege & Conference.LISTEN) != 0) return ConversationPrivilege.READ;
        return ConversationPrivilege.NONE;
    }

    /** helper method to convert access level defined in ConversationPrivilege
     * to access level defined in IM api Conference
     *
     */
    public int convertAccessToPrivilege(int access) {
        if (access == ConversationPrivilege.MANAGE) return Conference.MANAGE | Conference.PUBLISH | Conference.LISTEN;
        if (access == ConversationPrivilege.WRITE) return Conference.PUBLISH | Conference.LISTEN;
        if (access == ConversationPrivilege.READ) return Conference.LISTEN;
        return Conference.NONE;
    }

    /**
     *
     *
     */
    public void setPublicConversationDefaultPrivilege(String name, ConversationPrivilege privilege)
    throws CollabException {
        int access = privilege.getAccess();

        try {
            getConferenceService().getPublicConference(name).setDefaultPrivilege(convertAccessToPrivilege(access));
        } catch (CollaborationException ce) {
            throw new CollabException(ce);
        }
    }

    /**
     *        Terminates the conference.
     *        This will have the effect of destroying all currently archived messages
     *        and unsubscribing all current subscribe members.
     *        It can only be called with MANAGE privilege.
     *
     */
    public void deletePublicConversation(String name) throws CollabException {
        if (!canManagePublicConversation(name)) {
            return;
        }

        try {
            getConferenceService().getPublicConference(name).close();
        } catch (CollaborationException ce) {
            throw new CollabException(ce);
        }
    }

    /**
     *
     *
     */
    public boolean publicConversationExists(String name) {
        /*
        String[] conv = findPublicConversations(
                                        personalStoreService.SEARCHTYPE_EQUALS, name);
        //                personalStoreService.SEARCHTYPE_STARTSWITH, name);
        return (conv != null && conv.length>0 );
         */
        try {
            Conference conf = getConferenceService().getPublicConference(name);

            if (conf != null) {
                return true;
            } else {
                return false;
            }
        } catch (CollaborationException e) {
            return false;
        }
    }

    /**
     *
     *
     */
    public void changePassword(String newPassword) throws CollabException {
        Account account = getAccount();
        RegistrationListener listener = new IMCollabManager.Registration(account, newPassword);

        try {
            Debug.out.println(" changing password");
            getCollaborationSession().changePassword(newPassword, listener);
        } catch (CollaborationException e) {
            throw new CollabException(e);
        }
    }

    /**
     * skip send message or status if cannot contact server on
     * critical error
     *
     * @return criticalServerError
     */
    public boolean skipSend() {
        if (criticalServerError) {
            Debug.out.println("Critical Server Error occured");
        }

        return criticalServerError;
    }

    public Collection getParticipantsFromPublicConference(String destinationPublicConversation){
        try {
            ConferenceService service = getConferenceService();
            Conference conference = service.getPublicConference(destinationPublicConversation);
            
            if (conference!=null) {
                return conference.getParticipants();
            }
        }catch(Exception e){
            Debug.debugNotify(e);
        }
        
        return null;
    }

}
