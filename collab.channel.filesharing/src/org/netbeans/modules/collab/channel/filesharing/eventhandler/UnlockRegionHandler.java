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

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.context.UnlockRegionContext;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingTimerTask;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroup;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroups;
import org.netbeans.modules.collab.channel.filesharing.msgbean.UnlockRegion;
import org.netbeans.modules.collab.channel.filesharing.msgbean.UnlockRegionData;


/**
 * UnlockRegionHandler
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class UnlockRegionHandler extends FilesharingEventHandler {
    /**
     * constructor
     *
     */
    public UnlockRegionHandler(CollabContext context) {
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

        UnlockRegion unlockRegion = new UnlockRegion();
        collab.setChUnlockRegion(unlockRegion);

        SharedFileGroup[] sharedFileGroups = null;
        UnlockRegionContext unlockRegionContext = (UnlockRegionContext) evContext;
        String fileGroupName = unlockRegionContext.getFileGroupName();

        if ((fileGroupName != null) && !fileGroupName.trim().equals("")) {
            SharedFileGroup sharedFileGroup = getContext().getSharedFileGroupManager().getSharedFileGroup(
                    fileGroupName
                );

            if (sharedFileGroup != null) {
                List sharedGroupList = new ArrayList();
                sharedGroupList.add(sharedFileGroup);
                sharedFileGroups = (SharedFileGroup[]) sharedGroupList.toArray(new SharedFileGroup[0]);
            }
        } else {
            sharedFileGroups = getContext().getSharedFileGroupManager().getAllSharedFileGroup();
        }

        if ((sharedFileGroups == null) || (sharedFileGroups.length == 0)) {
            return null;
        }

        List sharedFileGroupList = new ArrayList();

        for (int i = 0; i < sharedFileGroups.length; i++) {
            fileGroupName = sharedFileGroups[i].getName();

            CollabFileHandler[] fileHandlers = sharedFileGroups[i].getFileHandlers();

            for (int j = 0; j < fileHandlers.length; j++) {
                CollabFileHandler collabFileHandler = fileHandlers[j];

                if (collabFileHandler == null) {
                    continue;
                }

                boolean status = collabFileHandler.constructUnlockRegionData(unlockRegion);

                if (!status) {
                    continue;
                }

                try {
                    //sleep for 100 millis
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                sharedFileGroupList.add(getContext().getSharedFileGroupManager().getSharedFileGroup(fileGroupName));
            }
        }

        if (sharedFileGroupList.size() == 0) {
            return null;
        }

        FileGroups fileGroups = new FileGroups();
        getContext().constructFileGroups(
            fileGroups, (SharedFileGroup[]) sharedFileGroupList.toArray(new SharedFileGroup[0])
        );
        unlockRegion.setFileGroups(fileGroups);

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

        UnlockRegion unlockRegion = collabBean.getChUnlockRegion();
        UnlockRegionData[] UnlockRegionData = unlockRegion.getUnlockRegionData();

        for (int i = 0; i < UnlockRegionData.length; i++) {
            String fullPath = UnlockRegionData[i].getFileName();
            CollabFileHandler collabFileHandler = getContext().getSharedFileGroupManager().getFileHandler(fullPath);

            if (collabFileHandler == null) {
                continue;
            }

            collabFileHandler.setCurrentState(
                FilesharingContext.STATE_RECEIVEDUNLOCK, FilesharingTimerTask.PERIOD * 3,
                !collabFileHandler.isDocumentModified(), false
            );
            collabFileHandler.handleUnlock(messageOriginator, UnlockRegionData[i]);
        }
    }
}
