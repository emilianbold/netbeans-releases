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

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroup;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileChanged;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileChangedData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroup;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroups;


/**
 * SendMessageJoin
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class SendChangeHandler extends FilesharingEventHandler {
    /**
     * constructor
     *
     */
    public SendChangeHandler(CollabContext context) {
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
        CCollab collab = null;
        SharedFileGroup[] sharedFileGroups = getContext().getSharedFileGroupManager().getAllSharedFileGroup();

        if ((sharedFileGroups == null) || (sharedFileGroups.length == 0)) {
            return null;
        }

        List tmpSavedSharedFileGroups = new ArrayList();

        for (int i = 0; i < sharedFileGroups.length; i++) {
            String fileGroupName = sharedFileGroups[i].getName();
            CollabFileHandler[] fileHandlers = sharedFileGroups[i].getFileHandlers();
            int fileChangeCount = 0;

            for (int j = 0; j < fileHandlers.length; j++) {
                CollabFileHandler collabFileHandler = fileHandlers[j];

                if ((collabFileHandler == null) || !collabFileHandler.isValid()) {
                    continue;
                }

                int fileHandlerState = collabFileHandler.getCurrentState();

                if (
                    (fileHandlerState == FilesharingContext.STATE_SENDFILE) ||
                        (fileHandlerState == FilesharingContext.STATE_RECEIVEDSENDFILE) ||
                        (fileHandlerState == FilesharingContext.STATE_RECEIVEDSENDCHANGE) ||
                        (fileHandlerState == FilesharingContext.STATE_RECEIVEDLOCK) ||
                        (fileHandlerState == FilesharingContext.STATE_RECEIVEDUNLOCK)
                ) {
                    continue;
                }

                boolean isFileChanged = collabFileHandler.isChanged();

                if (isFileChanged) {
                    fileChangeCount++;
                }
            }

            if (fileChangeCount == 0) {
                continue;
            } else {
                tmpSavedSharedFileGroups.add(sharedFileGroups[i]);
            }
        }

        if (tmpSavedSharedFileGroups.size() == 0) //no changes send
         {
            return null;
        }

        int totalChangeCount = 0;

        for (int i = 0; i < tmpSavedSharedFileGroups.size(); i++) {
            SharedFileGroup sharedFileGroup = (SharedFileGroup) tmpSavedSharedFileGroups.get(i);
            String fileGroupName = sharedFileGroup.getName();
            collab = new CCollab();
            collab.setVersion(getVersion());

            FileChanged fileChanged = new FileChanged();
            collab.setChFileChanged(fileChanged);

            FileGroups fileGroups = new FileGroups();
            getContext().constructFileGroups(
                fileGroups, getContext().getSharedFileGroupManager().getSharedFileGroup(fileGroupName)
            );
            fileChanged.setFileGroups(fileGroups);

            CollabFileHandler[] fileHandlers = sharedFileGroups[i].getFileHandlers();
            int fileChangeCount = 0;

            for (int j = 0; j < fileHandlers.length; j++) {
                FileChangedData fileChangedData = fileHandlers[j].constructFileChangedData(fileChanged);

                if (fileChangedData != null) {
                    fileChangeCount++;
                    fileChanged.addFileChangedData(fileChangedData);
                }
            }

            if (fileChangeCount == 0) {
                totalChangeCount++;

                continue;
            }
        }

        if (totalChangeCount == sharedFileGroups.length) //no changes send
         {
            return null;
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
            return;
        }

        FileChanged fileChanged = collabBean.getChFileChanged();
        FileGroup[] fileGroups = fileChanged.getFileGroups().getFileGroup();

        for (int i = 0; i < fileGroups.length; i++) {
            String fileGroupName = fileGroups[i].getFileGroupName();
            String user = fileGroups[i].getUser().getId();
            String[] fileNames = fileGroups[i].getFileName();

            if (!getContext().getSharedFileGroupManager().isSharedFileGroupExist(fileGroupName)) {
                throw new CollabException("No file exist: " + fileGroupName);
            }
        }

        FileChangedData[] fileChangedData = fileChanged.getFileChangedData();

        for (int i = 0; i < fileChangedData.length; i++) {
            String fullPath = fileChangedData[i].getFileName();
            CollabFileHandler collabFileHandler = getContext().getSharedFileGroupManager().getFileHandler(fullPath);

            if (collabFileHandler == null) {
                continue;
            }

            collabFileHandler.setCurrentState(FilesharingContext.STATE_RECEIVEDSENDCHANGE);
            collabFileHandler.handleSendChange(messageOriginator, fileChangedData[i]);
            collabFileHandler.setCurrentState(FilesharingContext.STATE_UNKNOWN);
        }
    }
}
