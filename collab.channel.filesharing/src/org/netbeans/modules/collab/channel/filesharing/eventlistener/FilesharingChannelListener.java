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
package org.netbeans.modules.collab.channel.filesharing.eventlistener;

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabMessage;

import org.openide.*;

import javax.swing.SwingUtilities;

import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.context.MessageContext;
import org.netbeans.modules.collab.channel.filesharing.event.JoinFilesharingBegin;
import org.netbeans.modules.collab.channel.filesharing.event.LeaveFilesharingEvent;
import org.netbeans.modules.collab.channel.filesharing.event.ReceivedCommand;
import org.netbeans.modules.collab.channel.filesharing.event.ReceivedJoinFilesharingBegin;
import org.netbeans.modules.collab.channel.filesharing.event.ReceivedJoinFilesharingEnd;
import org.netbeans.modules.collab.channel.filesharing.event.ReceivedLeaveFilesharing;
import org.netbeans.modules.collab.channel.filesharing.event.ReceivedLockRegion;
import org.netbeans.modules.collab.channel.filesharing.event.ReceivedPauseFilesharing;
import org.netbeans.modules.collab.channel.filesharing.event.ReceivedResumeFilesharing;
import org.netbeans.modules.collab.channel.filesharing.event.ReceivedSendChange;
import org.netbeans.modules.collab.channel.filesharing.event.ReceivedSendFile;
import org.netbeans.modules.collab.channel.filesharing.event.ReceivedUnlockRegion;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabEvent;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventNotifier;
import org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener.CollabChannelListener;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.core.Debug;


/**
 * FilesharingChannelListener
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class FilesharingChannelListener extends CollabChannelListener {
    /**
     * @param channel
     * @param eventNotifier
     */
    public FilesharingChannelListener(CollabContext context, EventNotifier eventNotifier) {
        super(context, eventNotifier);
    }

    /**
     *
     *
     */
    public void channelCreated() {
        try {
            EventContext evContext = new EventContext(JoinFilesharingBegin.getEventID(), getContext());
            CollabEvent ce = new JoinFilesharingBegin(evContext);
            eventNotifier.notify(ce);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     *
     *
     */
    public void channelClosed() {
        try {
            EventContext evContext = new EventContext(LeaveFilesharingEvent.getEventID(), getContext().getChannel());
            CollabEvent ce = new LeaveFilesharingEvent(evContext);
            eventNotifier.notify(ce);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    public FilesharingContext getContext() {
        return (FilesharingContext) this.context;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Message handling methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @param message
     * @param loginUser
     * @throws CollabException
     * @return status
     */
    public boolean handleMessage(final CollabMessage message, final String loginUser)
    throws CollabException {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    try {
                        doHandleMessage(message, loginUser);
                    } catch (CollabException ce) {
                        Debug.log("FilesharingContext", "FilesharingChannelListener, doHandleMessage() failed"); //NoI18n
                        Debug.logDebugException("FilesharingChannelListener, " + "doHandleMessage() failed", ce, true); //NoI18n
                    }
                }
            }
        );

        return true;
    }

    /**
     *
     * @param message
     * @param loginUser
     * @throws CollabException
     * @return status
     */
    private boolean doHandleMessage(CollabMessage message, String loginUser)
    throws CollabException {
        getContext().setReceivedMessageState(true);

        CCollab collabBean = getContext().parse(message);
        CollabEvent event = null;

        if (collabBean == null) {
            getContext().setReceivedMessageState(false);

            return true;
        }

        boolean isUserSame = isUserSame(message, loginUser);
        String messageOriginator = getMessageOriginator(message);

        if (collabBean.getChSendFile() != null) //SENDFILE
         {
            if (isUserSame) {
                getContext().setReceivedMessageState(false);

                return true;
            }

            MessageContext msgContext = new MessageContext(
                    ReceivedSendFile.getEventID(), collabBean, messageOriginator, isUserSame
                );
            event = new ReceivedSendFile(msgContext);
        } else if (collabBean.getChJoinFilesharing() != null) //JOIN
         {
            if (collabBean.getChJoinFilesharing().isBeginJoin()) {
                if (isUserSame) {
                    getContext().setCurrentState(FilesharingContext.STATE_RECEIVEDJOINBEGIN);
                }

                MessageContext msgContext = new MessageContext(
                        ReceivedJoinFilesharingBegin.getEventID(), collabBean, messageOriginator, isUserSame
                    );
                event = new ReceivedJoinFilesharingBegin(msgContext);
            } else {
                if (isUserSame) {
                    getContext().setCurrentState(FilesharingContext.STATE_RECEIVEDJOINEND);
                }

                MessageContext msgContext = new MessageContext(
                        ReceivedJoinFilesharingEnd.getEventID(), collabBean, messageOriginator, isUserSame
                    );
                event = new ReceivedJoinFilesharingEnd(msgContext);
            }
        } else if (collabBean.getChPauseFilesharing() != null) //PAUSE
         {
            MessageContext msgContext = new MessageContext(
                    ReceivedPauseFilesharing.getEventID(), collabBean, messageOriginator, isUserSame
                );
            event = new ReceivedPauseFilesharing(msgContext);
            getContext().setCurrentState(FilesharingContext.STATE_PAUSE);
        } else if (collabBean.getChResumeFilesharing() != null) //RESUME
         {
            MessageContext msgContext = new MessageContext(
                    ReceivedResumeFilesharing.getEventID(), collabBean, messageOriginator, isUserSame
                );
            getContext().setCurrentState(FilesharingContext.STATE_RESUME);
            event = new ReceivedResumeFilesharing(msgContext);
        } else if (collabBean.getChLockRegion() != null) //LOCK
         {
            if (isUserSame) {
                getContext().setCurrentState(FilesharingContext.STATE_LOCK);
                getContext().setReceivedMessageState(false);

                return true; //skip if fileowner==loginUser
            }

            MessageContext msgContext = new MessageContext(
                    ReceivedLockRegion.getEventID(), collabBean, messageOriginator, isUserSame
                );
            event = new ReceivedLockRegion(msgContext);
        } else if (collabBean.getChFileChanged() != null) //FILECHANGED
         {
            if (isUserSame) {
                getContext().setReceivedMessageState(false);

                return true; //skip if fileowner==loginUser
            }

            MessageContext msgContext = new MessageContext(
                    ReceivedSendChange.getEventID(), collabBean, messageOriginator, isUserSame
                );
            event = new ReceivedSendChange(msgContext);
        } else if (collabBean.getChUnlockRegion() != null) //UNLOCK
         {
            if (isUserSame) {
                getContext().setCurrentState(FilesharingContext.STATE_UNLOCK);
                getContext().setReceivedMessageState(false);

                return true; //skip if fileowner==loginUser
            }

            MessageContext msgContext = new MessageContext(
                    ReceivedUnlockRegion.getEventID(), collabBean, messageOriginator, isUserSame
                );
            event = new ReceivedUnlockRegion(msgContext);
        } else if (collabBean.getChLeaveFilesharing() != null) //LEAVE
         {
            MessageContext msgContext = new MessageContext(
                    ReceivedLeaveFilesharing.getEventID(), collabBean, messageOriginator, isUserSame
                );
            event = new ReceivedLeaveFilesharing(msgContext);
        } else if (collabBean.getChCommands() != null) //LEAVE
         {
            MessageContext msgContext = new MessageContext(
                    ReceivedCommand.getEventID(), collabBean, messageOriginator, isUserSame
                );
            event = new ReceivedCommand(msgContext);
        }

        if (event == null) {
            //TODO handle unknown message
            return true;
        }

        try {
            eventNotifier.notify(event);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }

        getContext().setReceivedMessageState(false);

        return true;
    }

    public void onReceiveMessageJoin(CCollab collabBean) {
    }
}
