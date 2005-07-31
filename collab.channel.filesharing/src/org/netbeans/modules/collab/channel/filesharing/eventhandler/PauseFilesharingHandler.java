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

import javax.swing.SwingUtilities;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingTimerTask;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroup;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroup;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroups;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileOwners;
import org.netbeans.modules.collab.channel.filesharing.msgbean.JoinUser;
import org.netbeans.modules.collab.channel.filesharing.msgbean.Moderator;
import org.netbeans.modules.collab.channel.filesharing.msgbean.PauseFilesharing;
import org.netbeans.modules.collab.channel.filesharing.msgbean.User;
import org.netbeans.modules.collab.channel.filesharing.msgbean.Users;
import org.netbeans.modules.collab.channel.filesharing.util.CollabQueue;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;
import org.netbeans.modules.collab.core.Debug;


/**
 * PauseFilesharingHandler
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class PauseFilesharingHandler extends FilesharingEventHandler implements FilesharingConstants {
    /**
     * constructor
     *
     */
    public PauseFilesharingHandler(CollabContext context) {
        super(context);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event Handler methods
    ////////////////////////////////////////////////////////////////////////////	

    /**
     * constructMsg
     *
     * @param        evContext                                        Event Context
     */
    public CCollab constructMsg(EventContext evContext)
    throws CollabException {
        CCollab collab = new CCollab();
        collab.setVersion(getVersion());

        PauseFilesharing pauseFilesharing = new PauseFilesharing();
        collab.setChPauseFilesharing(pauseFilesharing);

        JoinUser joinUser = new JoinUser();
        pauseFilesharing.setJoinUser(joinUser);

        User user = new User();
        user.setId(getContext().getJoinUser());
        joinUser.setUser(user);

        Moderator moderator = new Moderator();
        pauseFilesharing.setModerator(moderator);
        moderator.setUsers(getContext().constructUsers(getLoginUser()));

        FileOwners fileOwners = new FileOwners();
        pauseFilesharing.setFileOwners(fileOwners);

        String[] existingUsers = getContext().getUser();
        Users users = new Users();

        for (int i = 0; i < existingUsers.length; i++) {
            User tmpUser = new User();
            tmpUser.setId(existingUsers[i]);
            users.addUser(tmpUser);
        }

        pauseFilesharing.setUsers(users);

        FileGroups fileGroups = new FileGroups();
        pauseFilesharing.setFileGroups(fileGroups);

        String[] savedFileOwners = getContext().getSavedFileOwners();

        if ((savedFileOwners != null) || (savedFileOwners.length > 0)) {
            fileOwners.setUsers(getContext().constructUsers(savedFileOwners));

            SharedFileGroup[] sharedFileGroups = getContext().getSharedFileGroupManager().getAllSharedFileGroup();

            if ((sharedFileGroups != null) && (sharedFileGroups.length > 0)) {
                getContext().copyFileGroups(sharedFileGroups, fileGroups);
            }
        }

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
        if (isUserSame) {
            //pause all operations, do not cancel sendjoinEnd
            getContext().pauseAll(true);
        } else {
            //pause all operations
            getContext().pauseAll();
        }

        //show wait dialog if files shared
        SharedFileGroup[] allSharedFileGroups = getContext().getSharedFileGroupManager().getAllSharedFileGroup();

        if ((allSharedFileGroups != null) && (allSharedFileGroups.length > 0)) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        SyncWaitPanel.showDialog();
                    }
                }
            );
        }

        PauseFilesharing pauseFilesharing = collabBean.getChPauseFilesharing();

        //filegroups
        FileGroup[] fileGroups = pauseFilesharing.getFileGroups().getFileGroup();

        //add the moderator to userlist if not exist
        Moderator moderator = pauseFilesharing.getModerator();
        String moderatorUser = getContext().getModerator(moderator.getUsers());

        if ((moderatorUser != null) || !moderatorUser.equals("")) {
            getContext().addUser(moderatorUser);
        }

        //save for issuing join-end
        getContext().saveExpectedFile(fileGroups);

        User[] users = pauseFilesharing.getFileOwners().getUsers().getUser();

        if (getContext().isFileOwner(users)) //these users have to send files
         {
            //send owner files after 200 millisec
            getContext().startSendFileMessageTimerTask(true, 200 /*FilesharingTimerTask.PERIOD*/);
        } else {
            String joinUser = pauseFilesharing.getJoinUser().getUser().getId();

            if ((joinUser != null) && joinUser.equals(getContext().getLoginUser())) { //this block for user who joins
                getContext().setJoinFlag(true);

                //save users in conversation		
                Users usersObj = pauseFilesharing.getUsers();

                if (usersObj != null) {
                    User[] existingUsers = usersObj.getUser();

                    if (existingUsers != null) {
                        for (int i = 0; i < existingUsers.length; i++) {
                            String userID = existingUsers[i].getId();
                            getContext().getUserStyle(userID); //calc userstyle
                            getContext().addUser(userID);
                        }
                    }
                }

                getContext().getUserStyle(joinUser); //calc userstyle self

                if ((fileGroups == null) || (fileGroups.length == 0)) { //send join-end if no shared files in this conversation
                    scheduleJoinEnd(FilesharingTimerTask.JOIN_END_DELAY);
                }

                return;
            } else if (((users == null) || (users.length == 0)) && ((fileGroups != null) && (fileGroups.length > 0))) {
                //send all files after 1 sec
                getContext().startSendFileMessageTimerTask(false, FilesharingTimerTask.PERIOD);
            }
        }

        //Degenerate case of sending all files if a fileowner do not respond		
        if ((allSharedFileGroups != null) && (allSharedFileGroups.length > 0)) {
            Debug.log(this, "PauseFilesharingHandler, starting degerate send-file timer");

            int totalFileSize = 0;

            for (int i = 0; i < allSharedFileGroups.length; i++) {
                CollabFileHandler[] fileHandlers = allSharedFileGroups[i].getFileHandlers();

                for (int j = 0; j < fileHandlers.length; j++) {
                    if (fileHandlers[j] != null) {
                        Debug.log(
                            this,
                            "PauseFilesharingHandler, degenerate send-file, fileName: " + //NoI18n
                            fileHandlers[j].getName()
                        );
                        totalFileSize += fileHandlers[j].getFileSize();
                    }
                }
            }

            long delay = CollabQueue.calculateDelay(totalFileSize);

            if (delay <= (FilesharingTimerTask.PERIOD * 60)) {
                delay = FilesharingTimerTask.SENDFILE_DELAY;
            }

            //schedule sendFile
            getContext().startSendFileMessageTimerTask(
                getContext().getSaveExpectedFiles(), FileshareUtil.getRandomCount(delay)
            );

            //degenerate join-end
            scheduleJoinEnd((delay * 2) + FilesharingTimerTask.JOIN_END_DELAY);
        }
    }
}
