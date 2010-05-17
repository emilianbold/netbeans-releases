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

package org.netbeans.lib.collab;

import java.util.Map;
import java.util.Collection;

/**
 * A Conference is an instant messaging session between 2 or more users.
 *
 *
 * @since version 0.1
 *
 */
public interface Conference {


    /**
     * No rights.  Giving a member this access level is used to kick this member out.
     */
    public final static int NONE = 0x0001;

    /**
     * The LISTEN operation is the ability to join the chat room as a passive user.
     */
    public final static int LISTEN = 0x0002;

    /**
     * ability to send messages
     */
    public final static int PUBLISH = 0x0004;

    /** ability to invite a non-member to become a member to a chat room.
     * The access level of invitees may not exceed that of the invitor.
     */
    public final static int INVITE = 0x0008;

    /** 
     * the MANAGE operation set includes shutting down 
     * the conference or bulletin board,
     * or modify other
     * members' access right to this conference or bulletin board.
     * The MANAGE access right is
     * initially given only to the creator of the conference or bulletin board.
     */
    public final static int MANAGE = 0x0010;

    /**
     * A status indicating that the message was submitted for moderation
     */
    public final static int STATUS_SUBMIT = 0x0020;

    /**
     * A status indicating that the message was pending for moderation
     */
    public final static int STATUS_PENDING = 0x0022;
    
    /**
     * A status indicating that the message was approved by the moderator
     */
    public final static int STATUS_APPROVED = 0x0024;
    
    /**
     * A status indicating that the message was modified by the moderator
     */
    public final static int STATUS_MODIFIED = 0x0026;
    
    /**
     * A status indicating that the message was rejected by the moderator
     */
    public final static int STATUS_REJECTED = 0x0028;
    
    /**
     * returns the conference's address.  
     */
    public String getDestination();
                
    /**
     * add a message, which will be received by other parties in the 
     * conference.
     * 
     * @param message message
     *
     */
    public void addMessage(Message message) throws CollaborationException;
    
    /**
     * This method should be used submit and approve moderated messages.
     * The users who have a LISTEN access to the room and wish to publish a message to the room
     * should use set the status as STATUS_SUBMIT. Moderators should use this method to send a 
     * status to the requestor.The room should already be moderated by some moderator otherwise a 
     * CollaborationException will be thrown.
     *@param message Message to be submitted for moderation by users with a LISTEN access or 
     * The modified message after approval by the moderator. If there was no modification
     * to the message then it should be same as the original message received.
     *@param status - The status of the message as defined in Conference. 
     *@param reason - The comments on the moderated message.While submitting the message the reason may be ignored.
     */
    public void addModeratedMessage(Message message, int status, String reason) throws CollaborationException;
    
    /**
     * leave the conference.  This makes this Conference object 
     * unusable.
     */
    public void leave();
    
    /**
     * invoked by the application to specify 
     * that the user accepts the conference and to pass the
     * conference listener.  It may only be called once, only by an 
     * invitee and before any other method in this class.  It has no effect
     * otherwise. It will use the current user id as the default nick name.
     * 
     * @param listener conference handler. This handler can also be instance of 
     * ConferencePasswordListener or ConferenceEventListener
     */
    public void join(ConferenceListener listener) throws CollaborationException;

    /**
     * invoked by the application to specify 
     * that the user accepts the conference and to pass the
     * conference listener and a nick name.  It can be called 
     * multiple times with different nick names.
     * @param nick The nick name to be used in the conference
     * @param history The detail about the history messages. It should be null if the default
     * behaviour is desired.
     * @param listener conference handler. This handler can also be instance of 
     * ConferencePasswordListener or ConferenceEventListener
     *
     */
    public void join(String nick, ConferenceHistory history, ConferenceListener listener) throws CollaborationException;
    
    /**
     * invoked by the application to send a custom presence 
     * to the conference 
     * @param p The new presence for this room
     *
     */
    //public void sendPresence(Presence p) throws CollaborationException;
    
    /**
     * create a new message
     * @return a brand new, empty Message.
     */
    public Message createMessage() throws CollaborationException;

    /**
     * create an invite message
     * @return the invite message
     */
    public InviteMessage createInviteMessage() throws CollaborationException;

    /**
     * invite another user to this conference.  May only be called 
     * by members with INVITE privileges.
     *
     * @param accessLevel privilege to assign to the invitee,
     * if different from the conference's
     * default.  It may not exceed  the caller's access level.
     * @param message invite message.  The list of invitees is 
     * provided as the list of receipients for the invite message
     * @param listener status listener for the invite
     */
    public void invite(int accessLevel, Message message, InviteMessageStatusListener listener) throws CollaborationException; 
	
     /**
      * get the privileges of the all the users affiliated with the bulletin board
      * @return map of users and the access levels that they have to the conference 
      *         Value of each element returned in the map is an Integer object.
      */
     public Map listPrivileges() throws CollaborationException;
     
     /** sets the privleges of the users to the conference on the server
      *@param map Map of users affiliated to the conference and their accesslevels.
      *       Value of each element in the map has to be an Integer object.
      *
      */
    public void setPrivileges(Map map) throws CollaborationException;
     
    
    /** return the current user's access level
     * @see Conference
     */
    public int getPrivilege() throws CollaborationException;

    /**
     * @param uid fully-qualified principal identifier
     * return the specified principal's access level
     */
    public int getPrivilege(String uid) throws CollaborationException;
    
    /** set a principal's privilege for this conference.
     * Can only be called by member with MANAGE
     *
     * @param accessLevel access level to set for this principal
     * @param uid fully-qualified principal identifier
     */
    public void setPrivilege(String uid, int accessLevel) throws CollaborationException;
    
    /** 
     * checks if the user has the required privilege for this conference.
     * 
     * @param accessLevel access level to set for this principal
     * @return boolean returns true if the user has the required privilege.
     * 
     */
    public boolean hasPrivilege(int accessLevel) throws CollaborationException;
    
    /** set the default access level for members, unless specified
     * otherwise.
     * Can only be called by member with MANAGE
     *
     * @param accessLevel access level
     */
    public void setDefaultPrivilege(int accessLevel) throws CollaborationException;
    
    /** return the conference's default access level
     */
    public int getDefaultPrivilege() throws CollaborationException;

    /**
     * Get the PrivacyList for the Conference room. 
     * Current User should have MANAGE access to get the list
     *
     */
    //public PrivacyList getPrivacyList() throws CollaborationException;
    
    /**
     * Get the PrivacyList for the Conference room. 
     * Current User should have atleast READ access to get the list
     *
     */
    //public ReadOnlyPrivacyList getReadOnlyPrivacyList() throws CollaborationException;
    
    /**
     * Set the PrivacyList for the Conference room
     *
     */
    //public void setPrivacyList(PrivacyList pl) throws CollaborationException;
    
    
    /**
     * terminates the conference
     * This will have the effect of destroying all currently archived messages 
     * (for a bulletin board) and unsubscribing all current subscribe members.
     * It can only be called with MANAGE privilege.
     */
    public void close() throws CollaborationException;

    /**
     * get a conference property
     * @param attribute property name
     * @return property value
     */
    public String getProperty(String attribute);
    
    /**
     * set a conference property
     * @param attribute property name
     * @param value property value
     */
    public void setProperty(String attribute, String value) throws CollaborationException;
    
    /**
     * Start or stop the moderation
     * @param start true if moderations is to be started
     */
    public void moderate(boolean start) throws CollaborationException;

    /**
     * Tells whether this conference is public.
     */
    public boolean isPublic();
     
    /**
     * Lists the participants in the room.
     *@return Collection a list of uids of the participants.
     */
    public Collection getParticipants() throws CollaborationException;
    
    /**
     * returns the conference display name
     * @return conference display name if available, null otherwise
     */
    public String getDisplayName();
     
    /**
     * sets the display name.  Only local storage is modified.
     * save must be called in order to commit the change of name
     * to the server.
     * @param name new display name
     */
    public void setDisplayName(String name) throws ServiceUnavailableException;

    /**
     * commit the current configuration to the server's persistent
     * store.  This operation requires MANAGE access.
     */
    public void save() throws CollaborationException;

    /**
     * Sets access levels for which conference events are generated.
     * The purpose of setting a presence broadcast mask is to 
     * support large conferences in which membership events for
     * most participants do not need to be communicated.
     *
     * Only local storage is modified.
     * save must be called in order to commit the change of name
     * to the server.
     *
     * @param mask bitmask of access values which defining for which
     * participants conference events are to be generated and received.
     * The service does not generate membership updates for participants
     * whose access level is matched by this mask.
     */  
    public void setEventMask(int mask) throws ServiceUnavailableException;

    /**
     * get access levels for which conference events are generated.
     *
     * @return bitmask of access values which defining for which
     * participants conference events are to be generated and received.
     * The service does not generate membership updates for participants
     * whose access level is matched by this mask.
     */
    public int getEventMask() throws ServiceUnavailableException;

	
}


