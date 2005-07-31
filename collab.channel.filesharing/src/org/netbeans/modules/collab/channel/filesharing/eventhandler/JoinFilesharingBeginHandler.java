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
package org.netbeans.modules.collab.channel.filesharing.eventhandler;

import com.sun.collablet.CollabException;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.context.JoinBeginContext;
import org.netbeans.modules.collab.channel.filesharing.event.PauseFilesharingEvent;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.PauseTimerTask;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.JoinFilesharing;
import org.netbeans.modules.collab.channel.filesharing.msgbean.User;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;


/**
 * SendMessageJoinBegin
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class JoinFilesharingBeginHandler extends FilesharingEventHandler implements FilesharingConstants {
    /**
     * constructor
     *
     */
    public JoinFilesharingBeginHandler(CollabContext context) {
        super(context);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event Handler methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * constructMsg
     *
     * @param        context                                        Context
     */
    public CCollab constructMsg(EventContext evContext) {
        CCollab collab = new CCollab();
        collab.setVersion(getVersion());

        JoinFilesharing joinFilesharing = new JoinFilesharing();
        collab.setChJoinFilesharing(joinFilesharing);

        //add this element to message when begin to join a conversation.
        joinFilesharing.setBeginJoin(true);
        getContext().setJoinFlag(true);

        User user = new User();
        String joinUser = getLoginUser();

        if (evContext instanceof JoinBeginContext) {
            joinUser = (String) ((JoinBeginContext) evContext).getSource();
        }

        user.setId(joinUser);
        joinFilesharing.setUser(user);

        return collab;
    }

    /**
     * handleMsg
     *
     * @param        collabBean
     * @param        messageOriginator
     * @param        isUserSame
     */
    public void handleMsg(CCollab collabBean, String messageOriginator, boolean isUserSame)
    throws CollabException {
        //diable explorer and file view
        getContext().onJoinBegin();

        JoinFilesharing joinFilesharing = collabBean.getChJoinFilesharing();
        String saveJoinUser = joinFilesharing.getUser().getId();
        getContext().setJoinUser(saveJoinUser);
        getContext().addUser(saveJoinUser);

        //create a pausetimertask
        PauseTimerTask sendPauseMessageTimerTask = new PauseTimerTask(
                getContext().getChannelEventNotifier(),
                new PauseFilesharingEvent(new EventContext(PauseFilesharingEvent.getEventID(), null)), getContext()
            );

        //send pause message if not join user and a moderator
        if (!saveJoinUser.equals(getLoginUser()) && getContext().isModerator(getLoginUser())) {
            //pause-filesharing, send to all
            getContext().addTimerTask(SEND_PAUSE_TIMER_TASK, sendPauseMessageTimerTask);
            sendPauseMessageTimerTask.schedule(
                FileshareUtil.getRandomCount(FilesharingTimerTask.PERIOD /*FilesharingTimerTask.PAUSE_DELAY*/)
            );
        }

        if (isUserSame) {
            //degenerate pause-filesharing, send to all usually happens when 
            //1) if same user and #participants==2 (including this user)
            //   and other user is readonly, send join-end message, or
            //2) pause message from other user is not send/blocked for some reason
            getContext().addTimerTask(SEND_PAUSE_TIMER_TASK, sendPauseMessageTimerTask);
            sendPauseMessageTimerTask.schedule(FileshareUtil.getRandomCount(FilesharingTimerTask.PAUSE_DELAY * 2));
        }
    }
}
