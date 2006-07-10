/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener;

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabMessage;

import org.netbeans.modules.collab.channel.filesharing.mdc.*;


/**
 * Support class for FilesharingCollablet
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public abstract class CollabChannelListener extends Object implements ChannelListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* context */
    protected CollabContext context = null;

    /* notifier */
    protected EventNotifier eventNotifier = null;

    /**
     * @param channel
     * @param eventNotifier
     */
    public CollabChannelListener(CollabContext context, EventNotifier eventNotifier) {
        super();
        this.context = context;
        this.eventNotifier = eventNotifier;
    }

    /**
     *
     *
     */
    public abstract void channelCreated();

    /**
     *
     *
     */
    public abstract void channelClosed();

    ////////////////////////////////////////////////////////////////////////////
    // Message handling methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @param message
     * @throws CollabException
     * @return status
     */
    public abstract boolean handleMessage(CollabMessage message, String loginUser)
    throws CollabException;

    /**
     * find if login user same as messageOriginator
     *
     * @param message
     * @param loginUser
     * @throws CollabException
     * @return status
     */
    public boolean isUserSame(CollabMessage message, String loginUser) {
        boolean isUserSame = false;
        String messageOriginator = message.getOriginator().getIdentifier();
        int index = messageOriginator.indexOf('@');

        if (index != -1) {
            messageOriginator = messageOriginator.substring(0, index);
        }

        if (messageOriginator.equals(loginUser)) {
            isUserSame = true;
        }

        return isUserSame;
    }

    /**
     * find if login user same as messageOriginator
     *
     * @param message
     * @throws CollabException
     * @return messageOriginator
     */
    public String getMessageOriginator(CollabMessage message)
    throws CollabException {
        String messageOriginator = message.getOriginator().getIdentifier();
        int index = messageOriginator.indexOf('@');

        if (index != -1) {
            messageOriginator = messageOriginator.substring(0, index);
        }

        return messageOriginator;
    }
}
