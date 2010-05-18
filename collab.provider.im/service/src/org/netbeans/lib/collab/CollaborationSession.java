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
import java.util.List;
import java.util.Collection;

/**
 * Authenticated instant communication session.  It can be used to
 * establish communication with another user or more than one user,
 * join a conference, or establish subscriptions to instant
 * bulletin boards.
 *
 *
 * @since version 0.1
 *
 */
public interface CollaborationSession {


    /**
     * Get the NotificationService associated with this session.
     * @throws CollaborationException
     */
    public NotificationService getNotificationService()
                                                throws CollaborationException;

    /**
     * Get the ConferenceService associated with this Session.
     * @throws CollaborationException
     */
    public ConferenceService getConferenceService()
                                                throws CollaborationException;

    /**
     * Get the NewsService associated with this Session.
     * @throws CollaborationException
     */
    public NewsService getNewsService() throws CollaborationException;

    /**
     * Get the PersonalStoreService associated with this Session.
     * @throws CollaborationException
     */
    public PersonalStoreService getPersonalStoreService()
                                                throws CollaborationException;


    /**
     * Get the PresenceService associated with this Session.
     * @throws CollaborationException
     */
    public PresenceService getPresenceService() throws CollaborationException;

    /**
     * Get the StreamingService associated with this Session.
     * @throws CollaborationException
     */
    public StreamingService getStreamingService() throws CollaborationException;

    /**
     * Adds the listener for the current session. All the added listeners will receive the notification.
     * @param listener implementation of CollaborationSessionListener or subclass(es) thereof
     */
    public void addSessionListener(CollaborationSessionListener listener);

    /**
     * Removes the listener for the current session. The listener should have already been added.
     * @param listener implementation of CollaborationSessionListener or subclass(es) thereof
     */
    public void removeSessionListener(CollaborationSessionListener listener);

    /**
     * terminate the session
     */
    public void logout();

    /**
     * create a principal object based on a fully-qualified user id
     * @param uid FQ user id.
     * @return a new Principal object.
     */
    public CollaborationPrincipal createPrincipal(String uid) throws CollaborationException;

    /**
     * create a principal object based on a fully-qualified user id
     * @param uid FQ user id.
     * @param displayName
     * @return a new Principal object.
     */
    public CollaborationPrincipal createPrincipal(String uid, String displayName) throws CollaborationException;

    /**
     * get the Principal object for the current session
     * @return the session owner's Principal object.
     */
    public CollaborationPrincipal getPrincipal() throws CollaborationException;


    /**
     * unregister the authenticated principal
     * @param listener callback object for getting the unregistration events notifications
     */
    public void unregister(RegistrationListener listener) throws CollaborationException;


    /**
     * changes the password of the authenticated principal
     * @param password new password
     * @param listener callback object for gettint the password change event notifications
     */
    public void changePassword(String password, RegistrationListener listener) throws CollaborationException;

    /**
     * Creates a PrivacyList object.
     * This does not save the PrivacyList on the server
     * @param name The name of the PrivacyList
     * @return PrivacyList object
     *
     */
    public PrivacyList createPrivacyList(String name) throws CollaborationException;

    /**
     * @param name The name of the privacy list
     * @return PrivacyList with the given name
     *
     */
    public PrivacyList getPrivacyList(String name) throws CollaborationException;

    /**
     * Adds the privacy list.
     * @param list stores the privacy list to the server
     *
     */
    public void addPrivacyList(PrivacyList list) throws CollaborationException;

    /**
     * gets this users default privacy list
     * @return The name of the Default Privacy List
     *
     */
    public String getDefaultPrivacyListName() throws CollaborationException;

    /**
     * sets this users default privacy list
     * @param name The Privacy List name
     *
     */
    public void setDefaultPrivacyListName(String name) throws CollaborationException;

    /**
     * gets this users active privacy list
     * @return The active PrivacyList name
     *
     */
    public String getActivePrivacyListName() throws CollaborationException;

    /**
     * set this privacy list as active.
     * @param name The PrivacyList name to be made as active. This privacy list will be enforced for the current users session
     *
     *
     */
    public void setActivePrivacyListName(String name) throws CollaborationException;

    /**
     * lists the name of the privacy lists
     * Each Element in the List is a string
     */
    public java.util.List listPrivacyLists() throws CollaborationException;

    /**
     * remove the named PrivacyList. This deletes the privacy list stored on the server.
     *@param name The name of the privacy list to be removed
     */
    public void removePrivacyList(String name) throws CollaborationException;
    
    
    /*
     * Get the ExtendedConferenceService 
     *  returns the same instance as returned by getConferenceService()
     *  
     */
    public ExtendedConferenceService getExtendedConferenceService() throws CollaborationException;

    /*
     * Get the P2P Service associated with this session
     */
    
    public P2PService getP2PService() throws CollaborationException;
}

