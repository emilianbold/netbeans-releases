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
