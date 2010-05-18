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

/**
 * The Conference service.
 * There are two types of conferences, private and public
 * The difference between private and public is public conferences persist
 * even when no members are present.
 * private conferences can be setup using setupConference(). Private conferences
 * can be joined by calling Conference.join(). Public conferences can be setup
 * using setupPublicConference(). Public conferences can be joined
 * using joinPublicConference().
 *
 * The service should be intialized by calling intialize() before using any
 * of the methods.
 * 
 * @since version 0.1
 * 
 */
public interface ConferenceService {
    /**
     * setup a new conference
     *
     * @param listener conference listener
     * @param accessLevel privilegdes to assign to the invited user.
     * @return a new conference handle.  The only member so far is the owner
     * of this session.  The invite method in the Conference object can then
     * be used to invite other users to the conference.
     */
    public Conference setupConference(
                            ConferenceListener listener, int accessLevel)
                                                throws CollaborationException;

    /**
     * join a public conference
     *
     * @param destination conference address
     * @param listener conference listener. This listener can also be instance of 
     * ConferencePasswordListener or ConferenceEventListener
     */
    public Conference joinPublicConference(
                        String destination, ConferenceListener listener)
                                                throws CollaborationException;

    /**
     * join a public conference
     *
     * @param nick The nick name to be used in conference room
     * @param history The detail about the history messages. It should be null if the default
     * behaviour is desired.
     * @param destination conference address
     * @param listener conference listener. This listener can also be instance of 
     * ConferencePasswordListener or ConferenceEventListener
     */
    public Conference joinPublicConference(
                        String nick, ConferenceHistory history, String destination, ConferenceListener listener)
                                                throws CollaborationException;

    
    /**
     * create a new public conference
     * A public conference is a conference which persists even when no
     * member is present.  It is generally used as a public instant discussion
     * forum, aka public chat room.  It differs from a bulletin board in that
     * messages are not persistant.
     * A public conference is joined by other users using the
     * joinPublicConference method.
     * @see #joinPublicConference joinPublicConference
     * @param destination identifier for this conference
     * @param listener conference listener. This listener can also be instance of 
     * ConferencePasswordListener or ConferenceEventListener
     * @param accessLevel default privilegdes to assign to the joining users.
     */
    public Conference setupPublicConference(
            String destination, ConferenceListener listener, int accessLevel)
                                                throws CollaborationException;
    /**
     * retrieve a public conference without joining
     * Verified that the conference exists
     * @param destination identifier for this conference
     */
    public Conference getPublicConference(String destination)
                                                throws CollaborationException;

    /**
     * Add an additional ConferenceServiceListener to receive the event notifications.
     * To receive all the initial events the first ConferenceServiceListener should be
     * added while {@link #initialize initializing} ConferenceService.
     * @param listener ConferenceServiceListener The ConferenceServiceListener to be added.
     */
    public void addConferenceServiceListener(ConferenceServiceListener listener);

    /**
     * Removes an already added ConfereneServiceListener. To prevent loss of any event
     * notification it is advised to have atleast one ConferenceServiceListener
     * @param listener ConferenceServiceListener The ConferenceServiceListener to be removed.
     */
    public void removeConferenceServiceListener(ConferenceServiceListener listener);

    /**
     * intialize the service by providing a ConferenceServiceListener.
     * Service should be initialized by calling this method before using
     * any of the services of ConferenceService
     * @param listener ConferenceServiceListener
     */
    public void initialize(ConferenceServiceListener listener)
                                                throws CollaborationException;

    /**
     * list the conference rooms with specified access
     * @param access The access level as defined in this class
     * @return An array of Conference objects
     */
    public Conference[] listConference(int access) throws CollaborationException;
}
