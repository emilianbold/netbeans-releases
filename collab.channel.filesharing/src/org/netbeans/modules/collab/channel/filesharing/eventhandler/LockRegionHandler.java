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

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.context.LockRegionContext;
import org.netbeans.modules.collab.channel.filesharing.context.MessageContext;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandlerSupport;
import org.netbeans.modules.collab.channel.filesharing.filehandler.RegionInfo;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroup;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroups;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegion;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.Use2phase;


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
     * exec
     * @param eventID
     * @param       evContext
     */
    public void exec(String eventID, EventContext evContext) throws CollabException {
        String user=getLoginUser();
        boolean isUserSame = true;

        if(eventID!=null && eventID.startsWith("receivedMessage")) { //NoI18n
            CCollab collabBean = ((MessageContext)evContext).getCollab();
            String messageOriginator = ((MessageContext)evContext).getMessageOriginator();
            user=messageOriginator;
            isUserSame = ((MessageContext)evContext).isUserSame();
            
            getContext().printAllData("\nIn LRH::before handleMsg event: \n" +
                    eventID+"/"+LockRegionManager.getLockMessageType(collabBean));
            
            handleMsg(collabBean, messageOriginator, isUserSame);
            
            getContext().printAllData("\nIn LRH::after handleMsg event: \n" +
                    eventID+"/"+LockRegionManager.getLockMessageType(collabBean));
        } else {
            boolean skipSend = skipSendMessage(eventID);
            if(!skipSend) {
                LockRegionContext lockRegionContext = (LockRegionContext)evContext;
                RegionInfo regionInfo = lockRegionContext.getRegionInfo();
		String fileName = regionInfo.getFileName();
		CollabFileHandler fh =
		    getContext().getSharedFileGroupManager().getFileHandler(fileName);
                if (fh != null) { 
                    //get self LockRegionManager
                    LockRegionManager lrmanager = 
                        ((CollabFileHandlerSupport)fh).createLockRegionManager(
                        getContext().getLoginUser(), regionInfo);
                    CCollab collab = constructMsg(evContext);
                    lrmanager.start2PhaseLock(collab, getContext().getConversation().getParticipants().length);
                }
            }
        }
    }

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
        Use2phase use2phase=lockRegion.getUse2phase();
        boolean beginLock=false;
        if(use2phase!=null) {
            LockRegionData[] lockRegionData = lockRegion.getLockRegionData();
            if (lockRegionData != null && lockRegionData.length > 0) {
                CollabFileHandler fh =
		    getContext().getSharedFileGroupManager().getFileHandler(
                        lockRegionData[0].getFileName());
                if (fh != null) {
                    //get self LockRegionManager
                    LockRegionManager lrmanager = ((CollabFileHandlerSupport)fh).createLockRegionManager(
                        getContext().getLoginUser(), lockRegion);
                    beginLock=lrmanager.processLockReply(messageOriginator, collabBean);
                    //return for response received from remote user either contention or not
                    if (!beginLock) return;
                }
            }
        } 

        //This section is processed either if beginLock=true or there is no use2phase  
        //mechanism to lock region 
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
