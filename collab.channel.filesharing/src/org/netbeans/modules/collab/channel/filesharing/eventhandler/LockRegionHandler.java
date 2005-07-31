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
import org.netbeans.modules.collab.channel.filesharing.context.LockRegionContext;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.RegionInfo;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroup;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroups;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegion;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData;


/**
 * LockRegionHandler
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class LockRegionHandler extends FilesharingEventHandler {
    /**
     * constructor
     *
     */
    public LockRegionHandler(CollabContext context) {
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

        LockRegion lockRegion = new LockRegion();
        collab.setChLockRegion(lockRegion);

        FileGroups fileGroups = new FileGroups();
        LockRegionContext lockRegionContext = (LockRegionContext) evContext;
        RegionInfo regionInfo = lockRegionContext.getRegionInfo();

        if (regionInfo == null) {
            return null;
        }

        String fileGroupName = regionInfo.getFileGroupName();
        String fileName = regionInfo.getFileName();
        SharedFileGroup sharedFileGroup = getContext().getSharedFileGroupManager().getSharedFileGroup(fileGroupName);

        if (sharedFileGroup == null) {
            return null;
        }

        List tmpList = new ArrayList();
        tmpList.add(sharedFileGroup);
        getContext().copyFileGroups((SharedFileGroup[]) tmpList.toArray(new SharedFileGroup[0]), fileGroups);
        lockRegion.setFileGroups(fileGroups);

        LockRegionData lockRegionData = new LockRegionData();
        getContext().getSharedFileGroupManager().getFileHandler(fileName).constructLockRegionData(
            regionInfo, lockRegionData
        );
        lockRegion.addLockRegionData(lockRegionData);

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

        LockRegion lockRegion = collabBean.getChLockRegion();
        LockRegionData[] lockRegionData = lockRegion.getLockRegionData();

        for (int i = 0; i < lockRegionData.length; i++) {
            String fullPath = lockRegionData[i].getFileName();
            CollabFileHandler collabFileHandler = getContext().getSharedFileGroupManager().getFileHandler(fullPath);

            if (collabFileHandler == null) {
                continue;
            }

            collabFileHandler.setCurrentState(FilesharingContext.STATE_RECEIVEDLOCK);
            collabFileHandler.handleLock(messageOriginator, lockRegionData[i]);
            collabFileHandler.setCurrentState(FilesharingContext.STATE_UNKNOWN);
        }
    }
}
