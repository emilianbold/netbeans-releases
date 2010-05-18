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

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabMessage;
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;
import com.sun.collablet.Collablet;
import com.sun.collablet.CollabletFactoryManager;
import com.sun.collablet.Conversation;
import com.sun.collablet.ConversationPrivilege;
import com.sun.collablet.UserInterface;
import com.sun.collablet.chat.ChatCollablet;
import javax.swing.SwingUtilities;

import org.openide.util.*;

import java.beans.*;

import java.io.*;

import java.text.*;

import java.util.*;

import org.netbeans.lib.collab.*;

import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  Todd Fast <todd.fast@sun.com>
 */
public class IMConversation extends Object implements Conversation, ConferenceListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private IMCollabSession session;
    private Conference conference;
    private String identifier;
    private String displayName;
    private boolean valid;
    private boolean leaving;
    private List channels = Collections.synchronizedList(new ArrayList());
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private List participants = new ArrayList();
    private List invitedParticipants = new ArrayList();
    private Timer timer;
    private boolean joined;
    private boolean isPublic;

    /**
     *
     *
     */
    public IMConversation(IMCollabSession session) {
        super();
        this.session = session;
    }

    /**
     * This constructor can only be used to establish a new, anonymous private
     * conversation
     *
     */
    public IMConversation(IMCollabSession session, boolean establish)
    throws CollaborationException {
        super();
        this.session = session;

        if (establish) {
            // Synchronize so onEvent() cannot be called on this instance
            // concurrently, before we've cached the conference member.
            // Note, this doesn't actually work at this time, because onEvent()
            // is initially called on the current thread synchronously. This
            // implementation then is just precautionary, in case the IM 
            // behavior changes later.
            synchronized (this) {
                // TODO: Assume management access level for now; should this 
                // ever be anything different?
                attachConference(session.getConferenceService().setupConference(this, Conference.MANAGE));
            }
        }
    }

    /**
     *
     *
     */
    public IMConversation(IMCollabSession session, Conference conference)
    throws CollaborationException {
        this(session);
        this.conference = conference;

        // Assume we haven't already joined this conference
        //		conference.join(this);
        // Attach the conference as usual
        attachConference(conference);
    }

    /**
     *
     *
     */
    public void subscribe() throws CollabException {
        try {
            PersonalConference pc = (PersonalConference) ((IMCollabSession) getCollabSession()).getPersonalStoreService()
                                                          .createEntry(
                    PersonalStoreEntry.CONFERENCE, getConference().getDestination()
                );
            pc.setAddress(getConference().getDestination());
            pc.save();
            ((IMCollabSession) getCollabSession()).addSubscribedConversation(getConference().getDestination());

            //				StringUtility.getLocalPartFromAddress(
            //					getConference().getDestination()));
        } catch (CollaborationException ce) {
            throw new CollabException(ce, ce.getMessage());
        }
    }

    /**
     *
     *
     */
    public void unsubscribe() throws CollabException {
        try {
            PersonalConference pc = (PersonalConference) ((IMCollabSession) getCollabSession()).getPersonalStoreService()
                                                          .getEntry(
                    PersonalStoreEntry.CONFERENCE, getConference().getDestination()
                );

            if (pc != null) {
                ((PersonalStoreEntry) pc).remove();
            }

            setValid(false);
            ((IMCollabSession) getCollabSession()).removeSubscribedConversation(getConference().getDestination());
        } catch (CollaborationException ce) {
            throw new CollabException(ce, ce.getMessage());
        }
    }

    /**
     *
     *
     */
    protected void join() throws CollabException {
        // Only allow joining of the conversation once
        if (joined) {
            throw new IllegalStateException("Cannot join conference more than once"); // NOI18N
        }

        try {
            getConference().join(this);
            joined = true;

            // Configure the channels for this conversation. We do this
            // after add the conversation to out list so that it has a 
            // chance to fire channel change messages to listeners (not 
            // that this is expected to be very likely, but it helps 
            // maintainability to preserve proper semantics).
            CollabletFactoryManager.getDefault().configureChannels(this);
        } catch (CollaborationException ce) {
            throw new CollabException(ce, ce.getMessage());
        }
    }

    /**
     * Associates this conversation object with the provider conference. This
     * method must be called before any other methods are called on this
     * instance if the instance was created without a conference.
     *
     */
    protected synchronized void attachConference(Conference conference) {
        this.conference = conference;

        // Destination names take the form of xxx@yyy.  Derive the display 
        // name by removing the @yyy part.
        identifier = conference.getDestination();
        assert identifier != null : "Conference.getDestination() returned null";

        // Derive a default display name
        int index = identifier.indexOf("@");

        if (index != -1) {
            displayName = identifier.substring(0, index);
        }
        
        this.isPublic = getConference().isPublic();

        // Add ourself to the list of participants. Note, this probably
        // already happened during construction of the conference via
        // onEvent() (as of 6-11-2004)
        if (!isParticipant(getCollabSession().getUserPrincipal())) {
            addParticipant(getCollabSession().getUserPrincipal());
        }

        // Change state to valid
        setValid(true);

        updateDisplayName();
    }

    /**
     *
     *
     */
    public CollabSession getCollabSession() {
        return session;
    }

    /**
     *
     *
     */
    protected Conference getConference() {
        return conference;
    }

    /**
         *
         *
         */
    public boolean isValid() {
        return valid;
    }

    /**
         * Change the valid flag setting.  Note, this method does not modify
         * the connected state of this conversation.
         *
         */
    protected void setValid(boolean value) {
        valid = value;
        changeSupport.firePropertyChange(PROP_VALID, !value, value);
    }

    /**
     *
     *
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     *
     *
     */
    protected void setIdentifier(String value) {
        identifier = value;
    }

    /**
     *
     *
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     *
     *
     */
    protected void setDisplayName(String value) {
        displayName = value;
    }

    /**
     *
     *
     */
    protected void updateDisplayName() {
        if ((getConference() != null) && isPublic()) {
            return;
        }

        // Update the name of the conversation based on participants
        StringBuffer name = new StringBuffer();

        CollabPrincipal self = getCollabSession().getUserPrincipal();
        CollabPrincipal[] participants = getParticipants();

        int numAdded = 0;
        final int MAX_NAMES = 2;

        for (int i = 0; i < participants.length; i++) {
            // Don't include self in the list of participants
            if (participants[i].equals(self)) {
                continue;
            }

            // Condense the list of participants to MAX_NAMES + a message
            // indicating how many others are in the conversations (minus
            // the current use)
            if (numAdded == MAX_NAMES) {
                // If there are 2 or more names left to append, append a
                // message telling the user how many others are in the 
                // conversation.  Otherwise, just continue and add the
                // last particpant's name.
                if ((i + 1) == (participants.length - 1)) {
                    name.append(
                        NbBundle.getMessage(
                            IMConversation.class, "LBL_IMConversation_MoreParticpants",
                            new Integer(participants.length - 1 - MAX_NAMES)
                        )
                    );
                } else {
                    // Append the last name with a friendly "&"
                    name.append(
                        NbBundle.getMessage(
                            IMConversation.class, "LBL_IMConversation_AndFinalParticipant", // NOI18N
                            participants[i].getDisplayName()
                        )
                    );
                }

                break;
            } else {
                if (numAdded++ > 0) {
                    name.append(", "); // NOI18N
                }

                name.append(participants[i].getDisplayName());
            }
        }

        // If there are no other participants, we need some sort of name
        if (name.length() == 0) {
            name.append(NbBundle.getMessage(IMConversation.class, "LBL_IMConversation_EmptyConversation")); // NOI18N
        }

        setDisplayName(name.toString());
    }

    /**
     *
     *
     */
    public void leave() {
        try {
            // We need to know that we're leaving the conversation in order
            // to ignore any messages that we receive as a result
            leaving = true;

            // Close out all our channels
            for (Iterator i = channels.iterator(); i.hasNext();) {
                Collablet channel = (Collablet) i.next();

                try {
                    channel.close();
                } catch (Exception e) {
                    Debug.debugNotify(e);
                }

                i.remove();
            }

            // Leave the IM conference
            getConference().leave();
        } finally {
            setValid(false);
            leaving = false;
        }

        //		// Remove ourselves from the list of active conversations if it's a
        //		// private conversation or an unsubscribed public conversation
        //		String[] conversations=
        //			getCollabSession().getSubscribedPublicConversations();
        //		for (int i=0; i<conversations.length; i++)
        //		{
        //			// if this conversation is a subscribed public conversation leave
        //			// it in conversation list
        //			if (conversations[i].getIdentifier().equals(this.getIdentifier()))
        //			{
        //				return;
        //			}
        //		}
        ((IMCollabSession) getCollabSession()).removeConversation(this);
    }

    /**
     *
     *
     */
    public void invite(CollabPrincipal[] contacts, String inviteMessage) {
        try {
            IMCollabMessage newMsg = new IMCollabMessage(
                    getCollabSession().getUserPrincipal(), getConference().createInviteMessage()
                );

            for (int i = 0; i < contacts.length; i++) {
                // TAF: Removed recipient capability for now
                //				newMsg.addRecipient(contacts[i]);
                newMsg.getMessage().addRecipient(contacts[i].getIdentifier());
            }

            if ((inviteMessage != null) && !inviteMessage.trim().equals("")) {
                newMsg.getMessage().setContent(inviteMessage);
            } else {
                newMsg.getMessage().setContent("Please join the conversation");
            }

            // Add invited contacts
            for (int i = 0; i < contacts.length; i++)
                addInvitedParticipant(contacts[i]);

            // TODO: What's the proper access level here?  Do we want
            // invitees to themselves invite others?  I assume so...
            getConference().invite(
                Conference.INVITE, ((IMCollabMessage) newMsg).getMessage(), (IMCollabSession) getCollabSession()
            );
        }
        //		catch (CollabException ce)
        //		{
        //			Debug.errorManager.notify(ce);
        //		}
        catch (CollaborationException ce) {
            Debug.errorManager.notify(ce);
        }
    }

    /**
     *
     *
     */
    public CollabPrincipal[] getParticipants() {
        synchronized (participants) {
            return (CollabPrincipal[]) participants.toArray(new CollabPrincipal[participants.size()]);
        }
    }

    /**
     *
     *
     */
    public boolean isParticipant(CollabPrincipal principal) {
        synchronized (participants) {
            return participants.contains(principal);
        }
    }

    /**
     *
     *
     */
    protected void addParticipant(CollabPrincipal participant) {
        if (isParticipant(participant)) {
            return;
        }

        synchronized (participants) {
            participants.add(participant);
            updateDisplayName();
        }

        changeSupport.firePropertyChange(PROP_PARTICIPANTS, null, participant);
    }

    /**
     *
     *
     */
    protected void removeParticipant(CollabPrincipal participant) {
        if (!isParticipant(participant)) {
            return;
        }

        synchronized (participants) {
            participants.remove(participant);
            updateDisplayName();
        }

        changeSupport.firePropertyChange(PROP_PARTICIPANTS, participant, null);
    }

    /**
     *
     *
     */
    public CollabPrincipal[] getInvitees() {
        synchronized (invitedParticipants) {
            return (CollabPrincipal[]) invitedParticipants.toArray(new CollabPrincipal[invitedParticipants.size()]);
        }
    }

    /**
     *
     *
     */
    public boolean isInvitedParticipant(CollabPrincipal principal) {
        synchronized (invitedParticipants) {
            return invitedParticipants.contains(principal);
        }
    }

    /**
     *
     *
     */
    protected void addInvitedParticipant(CollabPrincipal participant) {
        if (isInvitedParticipant(participant)) {
            return;
        }

        synchronized (invitedParticipants) {
            invitedParticipants.add(participant);
        }

        changeSupport.firePropertyChange(PROP_INVITEES, null, participant);
    }

    /**
     *
     *
     */
    protected void removeInvitedParticipant(CollabPrincipal participant) {
        if (!isInvitedParticipant(participant)) {
            return;
        }

        synchronized (invitedParticipants) {
            invitedParticipants.remove(participant);
        }

        changeSupport.firePropertyChange(PROP_INVITEES, participant, null);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Channel methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public synchronized void addChannel(Collablet channel) {
        channels.add(channel);
        changeSupport.firePropertyChange(PROP_CHANNELS, null, channel);
    }

    /**
     *
     *
     */
    public synchronized Collablet[] getChannels() {
        return (Collablet[]) channels.toArray(new Collablet[channels.size()]);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Message methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public CollabMessage createMessage() throws CollabException {
        try {
            return createIMCollabMessage();
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    /**
     *
     *
     */
    protected IMCollabMessage createIMCollabMessage() throws CollaborationException {
        return new IMCollabMessage(getCollabSession().getUserPrincipal(), getConference().createMessage());
    }

    /**
     *
     *
     */
    public void sendMessage(CollabMessage message) throws CollabException {
        if (session.skipSend()) {
            Debug.out.println("Skipping send message: " + message.getID());

            return;
        }

        try {
            getConference().addMessage(((IMCollabMessage) message).getMessage());
        } catch (CollaborationException e) {
            throw new CollabException(e, e.getMessage());
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void onEvent(String event) {
        // Return if this object is no longer valid, or in the process of
        // being invalidated
        if (leaving || !isValid() || (getCollabSession() == null) || !getCollabSession().isValid()) {
            return;
        }

        // Removed from method so that we could check validity and return 
        // without sycnhronization.  This was the cause of the deadlock when
        // logging out with conversation windows still open.
        synchronized (this) {
            // TODO: Discriminate event types here and funnel appropriate ones
            // to the chat channel (message typed, etc.)
            try {
                ConferenceEventHelper helper = new ConferenceEventHelper(event);

                for (Iterator i = helper.getTuples().iterator(); i.hasNext();) {
                    ConferenceEventTuple tuple = (ConferenceEventTuple) i.next();

                    // TODO: I suspect we have to parse the destination to a 
                    // canonical form before trying to get the principal
                    final CollabPrincipal principal = getCollabSession().getPrincipal(
                            StringUtility.removeResource(tuple.destination)
                        );

                    int status = Integer.parseInt(tuple.status);

                    switch (status) {
                    case ConferenceEvent.ETYPE_USER_JOINED: {
                        if (!isParticipant(principal)) {
                            if (getConference() != null) {
                                Debug.out.println(
                                    tuple.destination + " joined " + // NOI18N
                                    getConference().getDestination()
                                );
                            }

                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    addParticipant(principal);
                                    getCollabSession().getManager().getUserInterface().notifyConversationEvent(
                                        IMConversation.this, UserInterface.NOTIFY_PARTICIPANT_JOINED);
                                    removeInvitedParticipant(principal);
                                }
                            });
                        }


                        break;
                    }

                    case ConferenceEvent.ETYPE_USER_LEFT: {
                        if (getConference() != null) {
                            Debug.out.println(tuple.destination + " left" + // NOI18N
                                getConference().getDestination()
                            );
                        }

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                removeParticipant(principal);
                                getCollabSession().getManager().getUserInterface().notifyConversationEvent(
                                    IMConversation.this, UserInterface.NOTIFY_PARTICIPANT_LEFT);
                            }
                        });

                        break;
                    }

                    case ConferenceEvent.ETYPE_ACCESS_MODIFIED:
                    case ConferenceEvent.ETYPE_ACTIVE:
                    case ConferenceEvent.ETYPE_CLOSE:
                    case ConferenceEvent.ETYPE_MODERATION_STARTED:
                    case ConferenceEvent.ETYPE_MODERATION_STOPED:
                    case ConferenceEvent.ETYPE_OTHER:break;

                    case ConferenceEvent.ETYPE_USER_INPUT_STARTED: {
                        if (principal != getCollabSession().getUserPrincipal()) {
                            changeSupport.firePropertyChange(USER_TYPING_ON, null, principal);
                        }

                        break;
                    }

                    case ConferenceEvent.ETYPE_USER_INPUT_STOPED: {
                        // TODO: Respond to select notifications
                        if (principal != getCollabSession().getUserPrincipal()) {
                            changeSupport.firePropertyChange(USER_TYPING_OFF, null, principal);
                        }

                        break;
                    }
                    }
                }
            } catch (Exception e) {
                // TODO: Handle this message appropriately
                Debug.errorManager.notify(e);
            }
        }
    }

    /**
     *
     *
     */
    public void onMessageAdded(Message msg) {
        try {
            // Determine the principal that sent the message.
            // TODO: Unfortunately, the message originator is just a string
            // in a particular format (xxx@muc.yyy/<user>).  AFAIK, it doesn't 
            // correspond to the actual user principal identifier.  So, the
            // question is whether or not we actually *need* an accurate 
            // principal as the originator, or if we just want a wrapper around
            // this string (or, do we just want to beg off on originator and
            // make it a simple string?).
            CollabPrincipal originator = getCollabSession().getPrincipal(
                    
                //				IMCollabPrincipal.parseName(msg.getOriginator());
                //	StringUtility.getLocalPartFromAddress(msg.getOriginator()));
                StringUtility.removeResource(msg.getOriginator())
                );

            // Construct a new wrapper CollabMessage
            IMCollabMessage message = new IMCollabMessage(originator, msg);

            //			Debug.dumpMessage(message);
            // Find a channel to handle the message
            boolean handled = false;
            Collablet chatChannel = null;

            for (Iterator i = channels.iterator(); i.hasNext();) {
                Collablet channel = (Collablet) i.next();

                // Special case the chat channel so it's always last
                if (channel instanceof ChatCollablet) {
                    chatChannel = channel;

                    continue;
                }

                if (channel.acceptMessage(message)) {
                    if (channel.handleMessage(message)) {
                        handled = true;

                        break;
                    }
                }
            }

            // Let the chat channel have a crack
            if (!handled && (chatChannel != null)) {
                if (chatChannel.acceptMessage(message)) {
                    handled = true;
                    chatChannel.handleMessage(message);
                }
            }

            // TODO: Should we take the opportunity here to notify the user
            // that a message was received that wasn't understood?  This would
            // be an opportunity to try to identify the module that sent the
            // message and notify the user that the module is not installed
            if (!handled) {
                Debug.out.println("No channel handled message " + message.getID());
            }
        } catch (CollabException e) {
            // TODO: Do something appropriate here
            Debug.errorManager.notify(e);
        } catch (Exception e) {
            // TODO: Do something appropriate here
            Debug.errorManager.notify(e);
        }
    }

    /**
     *
     *
     */
    public void onError(CollaborationException e) {
        // TODO: Do something appropriate here
        Debug.errorManager.notify(e);

        // Leave the conference
        leave();

        // Logout of the session; let the session handle this
        // TODO: Is it really necessary to close the session?
        ((IMCollabSession) getCollabSession()).onError(e);
    }

    /**
     *
     *
     */
    public void onModeratedMessageAdded(Message msg) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void onModeratedMessageStatus(Message msg, int status, String a) {
        // Do nothing
    }

    ////////////////////////////////////////////////////////////////////////////
    // Timer methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public synchronized Timer getTimer() {
        if (timer == null) {
            // Create timer as a daemon
            timer = new Timer(true);

            // When the conversation is invalid, stop the timer
            addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        assert IMConversation.this.timer != null : "Timer instance was null";
                        IMConversation.this.timer.cancel();
                    }
                }
            );
        }

        return timer;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Property change support
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

    /**
     *
     *
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     *
     *
     */
    public int getPrivilege() throws CollabException {
        if (!isPublic()) {
            return ConversationPrivilege.WRITE;
        }

        try {
            int p = conference.getPrivilege();

            return ((IMCollabSession) getCollabSession()).convertPrivilegeToAccess(p);
        } catch (CollaborationException ce) {
            //			Debug.out.println(" get exception in getPrivilege");
            return ConversationPrivilege.WRITE; // XXX - workaround of openfire problem
        }
    }

    /**
    *
    *
    */
    public int getPrivilege(CollabPrincipal principal)
    throws CollabException {
        if (!isPublic()) {
            return ConversationPrivilege.WRITE;
        }

        try {
            int p = conference.getPrivilege();
            ConversationPrivilege privilege = ((IMCollabSession) getCollabSession()).getPublicConversationPrivilege(
                    getIdentifier(), principal
                );

            if (privilege != null) {
                return privilege.getAccess();
            } else {
                return ConversationPrivilege.NONE;
            }
        } catch (CollaborationException ce) {
            //Debug.out.println(" get exception in getPrivilege"); 
            return ConversationPrivilege.NONE;
        }
    }
}
