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
package org.netbeans.modules.collab.channel.filesharing.eventhandler;

import com.sun.collablet.CollabException;

import javax.swing.SwingUtilities;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.event.ResumeFilesharingEvent;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.ResumeTimerTask;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.JoinFilesharing;
import org.netbeans.modules.collab.channel.filesharing.msgbean.User;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;


/**
 * SendMessageJoin
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class JoinFilesharingEndHandler extends FilesharingEventHandler implements FilesharingConstants {
    /**
     * constructor
     *
     */
    public JoinFilesharingEndHandler(CollabContext context) {
        super(context);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event Handler methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * constructMsg
     *
     * @param        evContext                                Event Context
     */
    public CCollab constructMsg(EventContext evContext) {
        CCollab collab = new CCollab();
        collab.setVersion(getVersion());

        JoinFilesharing joinFilesharing = new JoinFilesharing();
        collab.setChJoinFilesharing(joinFilesharing);

        //construct this message after receiving all the shared files from 
        //all file-owners.
        joinFilesharing.setEndJoin(true);

        User user = new User();
        user.setId(getLoginUser());
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
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    SyncWaitPanel.hideDialog();
                }
            }
        );

        JoinFilesharing joinFilesharing = collabBean.getChJoinFilesharing();

        if (joinFilesharing.getUser().equals(getLoginUser())) {
            //sendMessageResume(); //send to all			
            ResumeTimerTask sendResumeMessageTimerTask = new ResumeTimerTask(
                    getContext().getChannelEventNotifier(),
                    new ResumeFilesharingEvent(new EventContext(ResumeFilesharingEvent.getEventID(), null)),
                    getContext()
                );
            getContext().addTimerTask(SEND_RESUME_TIMER_TASK, sendResumeMessageTimerTask);
            sendResumeMessageTimerTask.schedule(FileshareUtil.getRandomCount(FilesharingTimerTask.RESUME_DELAY));
        }

        //Resume all edit operation
        getContext().resumeAll();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
}
