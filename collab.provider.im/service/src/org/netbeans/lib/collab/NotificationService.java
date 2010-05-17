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
 * A Notification service is used to send and receive messages.  The
 * Notification Service is distinct from the conference service, despite
 * the fact that the same Message definition is used in both.
 * The differences include
 *  <ul>
 * <li>In the Notification Service, messages are acknowlegded end-to-end, or
 * at least can be</li>
 * <li>in the Notification service, messages can be replied to</li>
 * <li>in the Notification service, the destination is the actual recipient,
 * while in the Conference service, the destination is the conference</li>
 * </ul>
 *
 * <u>About Poll</u>
 * The poll feature is built on top of the notification service.
 * It uses specific content types defined here.
 * <a href="../../../application-iim-poll.html">here</a>.
 *
 * The service should be intialized by calling intialize() before using any
 * of the methods.
 *
 * 
 * @since version 0.1
 * 
 */
public interface NotificationService {
     /**
     * send an alert to a destination.  An alert is not sent in the context of
     * a conference, as it does not allow for a response and does not require
     * the destination to be online.
     * @param message alert message
     * @param listener callback object used by the provider to convey
     * message disposition notifications.
     */
    public void sendMessage(Message message, MessageStatusListener listener)
                                                throws CollaborationException;

    /**
     * create a message.
     * @param destination address of the recipient.
     * more recipient addresses can be added using the Message.addRecipient()
     * method.
     */
    public Message createMessage(String destination)
                                                throws CollaborationException;

    /**
     * create a message.
     * recipient addresses can be added using the Message.addRecipient()
     * method.
     */
    public Message createMessage() throws CollaborationException;


    /**
     * intialize the service by providing a NotificationServiceListener.
     * Service should be initialized by calling this method before using
     * any of the services of NoficationService
     * @param listener NotificationServiceListener
     */
     public void initialize(NotificationServiceListener listener)
                                                throws CollaborationException;

	/**
     * Add an additional NotificationServiceListener to receive the event notifications.
     * To receive all the initial events the first NotificationServiceListener should be
     * added while {@link #initialize(NotificationServiceListener) initializing} NotificationService.
     * @param listener NotificationServiceListener The NotificationServiceListener to be added.
     */
    public void addNotificationServiceListener(NotificationServiceListener listener);

    /**
     * Removes an already added ConfereneServiceListener. To prevent loss of any event
     * notification it is advised to have atleast one NotificationServiceListener
     * @param listener NotificationServiceListener The NotificationServiceListener to be removed.
     */
    public void removeNotificationServiceListener(NotificationServiceListener listener);
}
