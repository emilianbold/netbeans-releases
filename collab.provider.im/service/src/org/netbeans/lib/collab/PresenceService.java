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
 * presence service session.  A PresenceSession is use to access and
 * advertise presence information.  A presence information access unit
 * or user agent, is called a presentity.  Actual presence information
 * is accessed by performing actions on a presentity.
 *
 *
 * @since version 0.1
 *
 */
public interface PresenceService {
    /**
     * user agent online / user available to communicate
     */
    public static final String STATUS_OPEN = "OPEN";

    /**
     * user agent offline / user unreachable
     */
    public static final String STATUS_CLOSED = "CLOSED";

    /**
     * user agent online / user unavailable
     */
    public static final String STATUS_AWAY = "AWAY";

    /**
     * Extended status to signify that the user is away.
     * user agent online / user idle and probably unresponsive
     */
    public static final String STATUS_IDLE = "IDLE";

    /**
     * user agent online / user involved in other tasks and of
     * limited availability
     */
    public static final String STATUS_BUSY = "BUSY";

    /**
     * user agent offline / one-way messages forwarded to other
     * delivery mechanism
     */
    public static final String STATUS_FORWARDED = "FORWARDED";

    /**
     * user agent chatting
     */
    public static final String STATUS_CHAT = "CHAT";

    /**
     * other status
     */
    public static final String STATUS_OTHER = "OTHER";

    /**
     * user's subscription is pending
     */
    public static final String STATUS_PENDING = "PENDING";

    /**
     * user's is being watched
     */
    //public static final String STATUS_WATCHED = "WATCHED";

    /**
     * Subscribes to the presentity.
     * The result of subscription will be informed
     * through PresenceServiceListener
     * @param presentity presentity url
     *
     */
    public void subscribe(String presentity) throws CollaborationException;

    /**
     * Subscribes to the multiple presentity.
     * The result of subscription will be informed
     * through PresenceServiceListener
     * @param presentity a list of presentity urls
     *
     */
    public void subscribe(String[] presentity) throws CollaborationException;

    /**
     * Cancels any subscription made for a presentity.
     * The result of unsubscription will be informed
     * through PresenceServiceListener
     * @param presentity presentity url
     *
     */
    public void unsubscribe(String presentity) throws CollaborationException;

    /**
     * Cancels any subscription made to the specified presentities.
     * The result of unsubscription will be informed
     * through PresenceServiceListener
     * @param presentity array of presentity urls
     *
     */
    public void unsubscribe(String[] presentity) throws CollaborationException;

    /**
     * Update a unit of presence information in the relevant presence
     * stores.
     * @param presence Presence information
     */
    public void publish(Presence presence) throws CollaborationException;

    /**
     * Send a unit of presence information to a particular user
     * @param presence Presence information
     * @param rcpt The recipeint to whom the presence is directed.
     */
    public void publish(Presence presence, String rcpt) throws CollaborationException;

    /**
     * retrieves presence information from the presence store synchronously
     *
     * @param presentity presentity url
     * @return an presence object
     */
    public Presence fetchPresence(String presentity)
                                                throws CollaborationException;

    /**
     * retrieves presence information from the presence store synchronously
     *
     * @param presentity presentity url
     * @return an array of presence objects
     */
    public Presence[] fetchPresence(String[] presentity)
                                                throws CollaborationException;

    /**
     * cancels the previously granted subscription approval or
     * deny the subscription request.
     * @param presentity presentity url
     *
     */
    public void cancel(String presentity) throws CollaborationException;


    /**
     * Authorize the presentity to receive presence updates
     * The Presentity should have earlier requested for the Approval
     * @param presentity presentity url
     */
    public void authorize(String presentity) throws CollaborationException;

    /**
     * intialize the service by providing a PresenceServiceListener.
     * Service should be initialized by calling this method before using
     * any of the services of PresenceService
     * @param listener PresenceServiceListener
     */
    public void initialize(PresenceServiceListener listener)
                                                throws CollaborationException;

	/**
     * Add an additional PresenceServiceListener to receive the event notifications.
     * To receive all the initial events the first PresenceServiceListener should be
     * added while {@link #initialize initializing} PresenceService.
     * @param listener PresenceServiceListener The PresenceServiceListener to be added.
     */
    public void addPresenceServiceListener(PresenceServiceListener listener);

	/**
	 * Removes an already added ConfereneServiceListener. To prevent loss of any event
	 * notification it is advised to have atleast one PresenceServiceListener
	 * @param listener PresenceServiceListener The PresenceServiceListener to be removed.
	 */
    public void removePresenceServiceListener(PresenceServiceListener listener);

}
