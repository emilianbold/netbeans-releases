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

import org.openide.cookies.*;
import org.openide.filesystems.*;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.context.MessageContext;
import org.netbeans.modules.collab.channel.filesharing.context.SendFileContext;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingTimerTask;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFormFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.LazySharedFileGroup;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroup;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroupFactory;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroup;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroups;
import org.netbeans.modules.collab.channel.filesharing.msgbean.SendFile;
import org.netbeans.modules.collab.channel.filesharing.msgbean.SendFileData;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;
import org.netbeans.modules.collab.channel.filesharing.util.QueueItem;
import org.netbeans.modules.collab.core.Debug;


/**
 * SendFileHandler
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class SendFileHandler extends FilesharingEventHandler implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////	

    /* totalFileSize */
    private long totalFileSize = 0;

    /* fileGroupName */
    private String fileGroupName = null;

    /* fileName */
    private String fileName = null;
    private boolean done = false;
    private int failureCount = 0;

    /**
     * constructor
     *
     */
    public SendFileHandler(CollabContext context) {
        super(context);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event Handler methods
    ////////////////////////////////////////////////////////////////////////////	

    /**
     * exec
     * @param eventID
     * @param        evContext
     */
    public void exec(String eventID, EventContext evContext)
    throws CollabException {
        String user = getLoginUser();
        boolean isUserSame = true;

        if ((eventID != null) && eventID.startsWith("receivedMessage")) //NoI18n
         {
            CCollab collabBean = ((MessageContext) evContext).getCollab();
            String messageOriginator = ((MessageContext) evContext).getMessageOriginator();
            user = messageOriginator;
            isUserSame = ((MessageContext) evContext).isUserSame();
            getContext().printAllData("\nIn SFH::before handleMsg event: \n" + eventID);
            handleMsg(collabBean, messageOriginator, isUserSame);
            getContext().printAllData("\nIn SFH::after handleMsg event: \n" + eventID);
        } else {
            boolean skipSend = skipSendMessage(eventID);
            CCollab collab = constructMsg(evContext);

            if ((collab != null) && !skipSend) {
                getContext().printAllData("\nIn SFH::after constructMsg event: \n" + eventID); //NoI18n				

                String fileGroupName = getFileGroupName();

                //total filesize (total filesize of all files in group)
                long size = getTotalFileSize();
                Debug.log(this, "SendFileHandler, Total file size: " + size); //NoI18n	

                QueueItem item = new QueueItem(fileGroupName, collab, size);
                addQueue(item);
            }
        }
    }

    /**
     * constructMsg
     *
     * @param        evContext                                Event Context
     */
    public CCollab constructMsg(EventContext evContext)
    throws CollabException {
        CCollab collab = new CCollab();
        collab.setVersion(getVersion());

        Object source = evContext.getSource();

        if (source instanceof LazySharedFileGroup) {
            Debug.log(this, "SendFileHandler, constructMsg: LazySharedFileGroup"); //NoI18n

            SharedFileGroup sharedFileGroup = null;

            if (evContext instanceof SendFileContext) {
                SendFileContext sendFileContext = (SendFileContext) evContext;
                LazySharedFileGroup lazySharedFileGroup = (LazySharedFileGroup) sendFileContext.getSharedFileGroup();
                fileGroupName = lazySharedFileGroup.getName();
                getContext().cancelTimerTask(SEND_SENDFILE_TIMER_TASK, fileGroupName);

                String projectName = lazySharedFileGroup.getProjectName();
                String[] fileNames = lazySharedFileGroup.getFileNames();
                final FileObject[] fileObjects = lazySharedFileGroup.getFiles();
                EditorCookie cookie = lazySharedFileGroup.getEditorCookie();

                if ((fileObjects == null) || (fileObjects.length == 0)) {
                    return null;
                }

                Debug.log("ProjectsRootNode", "ProjectsRootNode, files length : " + fileObjects.length);

                sharedFileGroup = SharedFileGroupFactory.createSharedFileGroup(
                        fileGroupName, getLoginUser(), projectName, getContext().getSharedFileGroupManager()
                    );
                getContext().getSharedFileGroupManager().addFileGroup(sharedFileGroup);
                getContext().getSharedFileGroupManager().addToOwnerSharedFile(fileGroupName, sharedFileGroup);

                for (int j = 0; j < fileObjects.length; j++) {
                    try {
                        CollabFileHandler fileHandler = sharedFileGroup.createFileHandler(
                                getContext(), getLoginUser(), projectName, fileNames[j], fileObjects[j], cookie
                            );

                        if (fileHandler == null) {
                            return null;
                        }

                        sharedFileGroup.addFileHandler(fileHandler);

                        if ((fileHandler != null) && fileHandler instanceof CollabFormFileHandler) {
                            sharedFileGroup.setType(SharedFileGroup.FORM_TYPE);
                        }

                        //register document Listener
                        if (!getContext().isReadOnlyConversation()) {
                            fileHandler.registerDocumentListener();
                        }
                    } catch (CollabException ce) {
                        Debug.log("ProjectsRootNode", "ProjectsRootNode, createFileHandler failed");

                        return null;
                    }
                }

                if (sharedFileGroup.getType() == SharedFileGroup.FORM_TYPE) {
                    CollabFileHandler[] fhs = sharedFileGroup.getFileHandlers();

                    for (int j = 0; j < fhs.length; j++) {
                        if (fhs[j] != null) {
                            fhs[j].setRetrieveFileContentOnly(true);
                        }
                    }
                }
            }

            //cancel any scheduled task for this fileGroup
            getContext().cancelSendFileMessageTimerTask(fileGroupName);

            return constructMsg(sharedFileGroup, collab);
        } else if (source instanceof SharedFileGroup) {
            Debug.log(this, "SendFileHandler, constructMsg: sharedFileGroup"); //NoI18n				

            SharedFileGroup sharedFileGroup = (SharedFileGroup) source;
            fileGroupName = sharedFileGroup.getName();
            Debug.log(this, "SendFileHandler, Sending file, sharedFileGroup group: " + fileGroupName); //NoI18n

            int groupType = sharedFileGroup.getType();

            return constructMsg(sharedFileGroup, collab);
        }

        return null;
    }

    /**
     * constructMsg
     *
     * @param        fileGroupName
     * @param        fileObjects
     * @return        collab
     */
    public CCollab constructMsg(SharedFileGroup sharedFileGroup, CCollab collab)
    throws CollabException {
        String fileGroupName = sharedFileGroup.getName();
        CollabFileHandler[] fileHandlers = sharedFileGroup.getFileHandlers();
        SendFile sendFile = new SendFile();
        collab.setChSendFile(sendFile);

        FileGroups fileGroups = new FileGroups();
        getContext().constructFileGroups(fileGroups, sharedFileGroup);
        sendFile.setFileGroups(fileGroups);
        Debug.log(this, "SendFileHandler, constructMsg: sharedFileGroup"); //NoI18n
        fileGroupName = sharedFileGroup.getName();
        Debug.log(this, "SendFileHandler, Sending file, sharedFileGroup group: " + fileGroupName); //NoI18n
        Debug.log(this, "SendFileHandler, Sending file length: " + fileHandlers.length); //NoI18n

        int groupType = sharedFileGroup.getType();
        Debug.log(this, "SendFileHandler, groupType: " + groupType); //NoI18n
        setFileGroupName(fileGroupName);
        this.fileGroupName = fileGroupName;
        this.totalFileSize = 0;

        try {
            for (int i = 0; i < fileHandlers.length; i++) {
                CollabFileHandler collabFileHandler = fileHandlers[i];
                Debug.log(this, "SendFileHandler, fileName: " + collabFileHandler.getName()); //NoI18n				

                SendFileData sendFileData = constructSendFileData(collabFileHandler, sendFile);

                if (sendFileData == null) {
                    throw new CollabException();
                }

                SendFileHandler.this.totalFileSize += collabFileHandler.getFileSize();
                sendFile.addSendFileData(sendFileData);
            }
        } catch (Throwable th) {
            SendFileHandler.this.failureCount++;

            if (SendFileHandler.this.failureCount >= 3) {
                SendFileHandler.this.done = true;
                Debug.log(this, "SendFileHandler, send failed for file: " + //NoI18n
                    SendFileHandler.this.fileGroupName
                );

                return null;
            }
        }

        return collab;
    }

    /**
     * constructSendFileData
     *
     * @param        collabFileHandler
     * @param        sendFile
     */
    public SendFileData constructSendFileData(CollabFileHandler collabFileHandler, SendFile sendFile)
    throws CollabException {
        if (
            (collabFileHandler.getCurrentState() == FilesharingContext.STATE_SENDFILE) ||
                (collabFileHandler.getCurrentState() == FilesharingContext.STATE_RECEIVEDSENDFILE)
        ) {
            return null;
        }

        collabFileHandler.setCurrentState(
            FilesharingContext.STATE_SENDFILE, FilesharingTimerTask.PERIOD * 3, true, true
        );

        SendFileData sendFileData = collabFileHandler.constructSendFileData(sendFile);

        return sendFileData;
    }

    /**
     * exec
     *
     * @param        collabBean                        collabBean
     * @param        messageOriginator        messageOriginator
     * @param        isUserSame                        isUserSame
     */
    public void handleMsg(CCollab collabBean, final String messageOriginator, boolean isUserSame)
    throws CollabException {
        SendFile sendFile = collabBean.getChSendFile();
        FileGroup[] fileGroups = sendFile.getFileGroups().getFileGroup();

        if (isUserSame) {
            for (int i = 0; i < fileGroups.length; i++) {
                String fileGroupName = FileshareUtil.getNormalizedPath(fileGroups[i].getFileGroupName());
                removeQueue(fileGroupName);
            }

            return;
        }

        //Add file owner
        getContext().addFileOwner(messageOriginator);

        //Add file to owner map for file annotations
        getContext().addToFileOwnerMap(fileGroups);

        //collect all file info into a map
        SendFileData[] sendFileData = sendFile.getSendFileData();
        HashMap sendFileDataMap = new HashMap();

        for (int i = 0; i < sendFileData.length; i++) {
            String fullPath = FileshareUtil.getNormalizedPath(sendFileData[i].getFileData().getFileName());

            if (!sendFileDataMap.containsKey(fullPath)) {
                sendFileDataMap.put(fullPath, sendFileData[i]);
            }
        }

        //now process sendfile data's for each filegroup 
        for (int i = 0; i < fileGroups.length; i++) {
            String fileGroupName = FileshareUtil.getNormalizedPath(fileGroups[i].getFileGroupName());
            String user = fileGroups[i].getUser().getId();
            String[] fileNames = fileGroups[i].getFileName();

            SharedFileGroup sharedFileGroup = getContext().getSharedFileGroupManager().getSharedFileGroup(
                    fileGroupName
                );

            if (sharedFileGroup == null) {
                String projectName = fileGroupName.split(FILE_SEPERATOR)[1];
                CollabFileHandler[] fileHandlers = new CollabFileHandler[fileNames.length];
                sharedFileGroup = SharedFileGroupFactory.createSharedFileGroup(
                        fileGroupName, user, projectName, getContext().getSharedFileGroupManager()
                    );
                getContext().getSharedFileGroupManager().addToAllSharedFileGroup(fileGroupName, sharedFileGroup);
                getContext().addToUserSharedFileGroupNames(messageOriginator, fileGroupName);
            }

            List sfdList = new ArrayList();

            for (int j = 0; j < fileNames.length; j++) {
                SendFileData sfd = (SendFileData) sendFileDataMap.get(fileNames[j]);

                if (sfd != null) {
                    sfdList.add(sfd);
                }
            }

            removeFromExpectedFiles(fileGroupName);
            sharedFileGroup.handleSendFile((SendFileData[]) sfdList.toArray(new SendFileData[0]), messageOriginator);
        }
    }

    private void removeFromExpectedFiles(String fileGroupName)
    throws CollabException {
        Debug.log(this, "SendFileHandler, handle user: " + //NoI18n
            getContext().getLoginUser()
        );
        Debug.log(this, "SendFileHandler, isJoinState: " + //NoI18n
            getContext().isJoinState()
        );

        HashMap saveExpectedFiles = getContext().getSaveExpectedFiles();

        synchronized (saveExpectedFiles) {
            if (
                (saveExpectedFiles != null) && (saveExpectedFiles.size() > 0) &&
                    saveExpectedFiles.containsKey(fileGroupName)
            ) {
                Debug.log(this, "SendFileHandler, removing fileGroupName: " + //NoI18n
                    fileGroupName
                );
                saveExpectedFiles.remove(fileGroupName);
            }

            Debug.log(this, "SendFileHandler, saveExpectedFiles size: " + //NoI18n
                saveExpectedFiles.size()
            );

            if ((saveExpectedFiles.size() == 0) && getContext().isJoinState()) {
                //join user to issue join-end
                //sendMessage join-end
                getContext().setJoinFlag(false);
                saveExpectedFiles.clear();
                scheduleJoinEnd(FilesharingTimerTask.JOIN_END_DELAY);
            }
        }
    }

    /**
    * addQueue
    *
    * @param        qItem
    */
    public void addQueue(QueueItem qItem) {
        getContext().getQueue().addItem(qItem);
    }

    /**
     * removeQueue
     *
     * @param        fileGroupName
     */
    public void removeQueue(String fileGroupName) {
        getContext().getQueue().removeItem(fileGroupName);
    }

    /**
     * getTotalFileSize
     *
     * @return totalFileSize
     */
    public long getTotalFileSize() {
        return this.totalFileSize;
    }

    /**
     * setFileGroupName
     *
     * @param fileGroupName
     */
    public void setFileGroupName(String fileGroupName) {
        this.fileGroupName = fileGroupName;
    }

    /**
     * getFileGroupName
     *
     * @return fileGroupName
     */
    public String getFileGroupName() {
        return this.fileGroupName;
    }
}
