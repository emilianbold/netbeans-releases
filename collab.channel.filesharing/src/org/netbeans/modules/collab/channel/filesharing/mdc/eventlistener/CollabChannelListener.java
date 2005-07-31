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
