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

import org.openide.awt.UndoRedo;
import org.openide.windows.TopComponent;

import java.util.HashMap;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabRegion;

import com.sun.collablet.CollabException;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandlerSupport;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabLineRegion;
import org.netbeans.modules.collab.channel.filesharing.filehandler.EditorLock;
import org.netbeans.modules.collab.channel.filesharing.filehandler.RegionInfo;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegion;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.U2pResponse;
import org.netbeans.modules.collab.channel.filesharing.msgbean.Use2phase;


/**
 *
 * @author askhan
 */
public class LockRegionManager {
    private String lockID;
    private FilesharingContext context;
    private String userID;
    private String filename;
    private RegionInfo regionInfo;
    private CCollab collabBean;
    private int userCount;
    private HashMap replies = new HashMap();
    private EditorLock editorLock = null;
    private CollabFileHandler fileHandler = null;
    private TimerTask tt = null;
    private long lockRequestTime;
    private boolean isValid;
    private boolean isUserStarted2Phase = false;

    /** Creates a new instance of LockRegionManagerFactory */
    public LockRegionManager(String lockID, FilesharingContext context, String userID, RegionInfo regionInfo) {
        Debug.log("FilesharingContext", "LRM, constructed: lockID: " + lockID); //NoI18n
        this.lockID = lockID;
        this.context = context;
        this.userID = userID;
        this.filename = regionInfo.getFileName();
        this.regionInfo = regionInfo;
        getFileHandler(); //init	
        setValid(true);
    }

    private CollabFileHandler getFileHandler() {
        if (fileHandler == null) {
            fileHandler = getFileHandler(filename, context);
        }

        return fileHandler;
    }

    private static CollabFileHandler getFileHandler(String fname, FilesharingContext context) {
        return context.getSharedFileGroupManager().getFileHandler(fname);
    }

    public void start2PhaseLock(final CCollab collabBean, int userCount)
    throws CollabException {
        Debug.log(
            "FilesharingContext",
            "LRM, start2PhaseLock for user: " + userID + " region: " + regionInfo.getID() + " userCount: " + userCount
        ); //NoI18n
        this.collabBean = collabBean;
        this.isUserStarted2Phase = true;
        this.userCount = userCount;

        final long delay = FilesharingConstants.PERIOD * 2;
        tt = new TimerTask() {
                    public void run() {
                        try {
                            if (LockRegionManager.this.isValid()) {
                                Debug.log("FilesharingContext", "LRM, timertask start"); //NoI18n
                                sendBeginLockMessage(collabBean);
                            }
                        } catch (Throwable th) {
                            th.printStackTrace(Debug.out);
                        }
                    }
                };
        getContext().getTimer().schedule(tt, delay);
        lockRequestTime = System.currentTimeMillis();
        sendLockRequest(lockID, collabBean);
    }

    public void undoDocumentUpdate() {
        final CollabFileHandler fh = getFileHandler();

        if (fh == null) {
            return;
        }

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    try {
                        TopComponent tc = ((CollabFileHandlerSupport) fh).findTopComponent();

                        if (tc != null) {
                            UndoRedo undoRedo = tc.getUndoRedo();

                            if (undoRedo != null) {
                                ((CollabFileHandlerSupport)fh).setSkipUpdate(true);
                                undoRedo.undo();
                                ((CollabFileHandlerSupport)fh).setSkipUpdate(false);
                            }
                        }
                    } catch (Throwable th) {
                        Debug.log("FilesharingContext", "LRM, " + //NoI18n
                            "cannot Undo change "
                        ); //NoI18n
                        Debug.logDebugException("LRM, " + //NoI18n
                            "cannot Undo change", //NoI18n	
                            th, true
                        );
                    }
                }
            }
        );
    }

    public void sendLockRequest(String use2phaseID, CCollab collabBean)
    throws CollabException {
        Use2phase use2phase = new Use2phase();
        use2phase.setUse2phaseId(this.lockID);
        use2phase.setRequest(true);
        collabBean.getChLockRegion().setUse2phase(use2phase);
        sendMessage(collabBean);
    }

    public boolean processLockReply(String user, CCollab collabBean)
    throws CollabException {
        Debug.log("FilesharingContext", "LRM, processLockReply: " + userID); //NoI18n		

        boolean beginLock = false;
        Use2phase use2phase = collabBean.getChLockRegion().getUse2phase();

        //process "lock-region/use2phase/request" message
        if (use2phase.isRequest()) {
            U2pResponse u2pResponse = new U2pResponse();

            if (isContentionFound()) {
                CollabFileHandler fh = getFileHandler();
                ((CollabFileHandlerSupport) fh).lockFileForCreateRegion();
                u2pResponse.setContentionFound(true);
                undoDocumentUpdate();

                String regionName = regionInfo.getID();
                CollabRegion r = ((CollabFileHandlerSupport) fh).getRegion(regionName);

                if (r != null) {
                    r.setValid(false);
                }

                ((CollabFileHandlerSupport) fh).removeRegion(userID, regionName);
                ((CollabFileHandlerSupport) fh).unlockFileForCreateRegion();
            } else {
                u2pResponse.setContentionNotfound(true);
            }

            use2phase.setResponse(u2pResponse);

            //reset request
            use2phase.setRequest(false);
            sendMessage(collabBean);
            beginLock = false;
        } else if (use2phase.isBegin()) //process normal handleLock if "lock-region/use2phase/begin" message
         {
            beginLock = true;
        } else if (use2phase.getResponse() != null && isUserStarted2Phase) {
            //process response only if this user started 2phase Locking
            U2pResponse response = use2phase.getResponse();

            if (response.isContentionFound()) //release any lock and start over
             {
                beginLock = false;
                undoDocumentUpdate();

                CollabFileHandler fh = getFileHandler();
                String regionName = regionInfo.getID();
                CollabRegion r = ((CollabFileHandlerSupport) fh).getRegion(regionName);

                if (r != null) {
                    r.setValid(false);
                }

                ((CollabFileHandlerSupport) fh).removeRegion(userID, regionName);
                ((CollabFileHandlerSupport) fh).unlockFileForCreateRegion();
                cleanup();
            } else if (response.isContentionNotfound()) //store responses
             {
                synchronized (replies) {
                    replies.put(user, collabBean);
                    Debug.log(
                        "FilesharingContext",
                        "LRM, processLockReply: rsize: " + replies.size() + "userCount: " + userCount
                    ); //NoI18n					

                    if (replies.size() >= (userCount - 1)) {
                        beginLock = false;
                        sendBeginLockMessage(collabBean);
                    }
                }
            }
        }

        return beginLock;
    }

    private void sendBeginLockMessage(CCollab collabBean)
    throws CollabException {
        if (!isValid()) {
            return;
        }

        long lockBeginTime = System.currentTimeMillis();
        long delayLockBegin = lockBeginTime - lockRequestTime;
        Debug.log(
            "FilesharingContext",
            "LRM, sendBeginLockMessage" + " after waiting for: " + delayLockBegin + " millis  begin: " + lockBeginTime +
            " request: " + lockRequestTime + "user: " + userID + " lockID: " + lockID
        ); //NoI18n

        Use2phase use2phase = collabBean.getChLockRegion().getUse2phase();
        use2phase.setBegin(true);
        use2phase.setRequest(false);
        use2phase.setResponse(null);
        sendMessage(collabBean);
        ((CollabFileHandlerSupport) fileHandler).unlockFileForCreateRegion();
        
        cleanup();
    }
    
    private void cleanup() {
        //remove lrm
        if (tt != null) tt.cancel();
        setValid(false);
        getContext().removeLockManager(this.lockID);
    }

    public boolean isContentionFound() throws CollabException {
        boolean retVal = false;
        CollabFileHandler fh = getFileHandler();

        if (fh != null) {
            int beginLineN = -1;
            int endLineN = -1;
            Vector lineRegions = regionInfo.getLineRegion();

            if ((lineRegions != null) && (lineRegions.size() > 0)) {
                Debug.log("FilesharingContext","LRM, lineRegions.size: "+lineRegions.size());//NoI18n
                CollabLineRegion beginLine = (CollabLineRegion) lineRegions.firstElement();
                CollabLineRegion endLine = (CollabLineRegion) lineRegions.lastElement();
                
                try {
                    if(beginLine!=null && endLine!=null)
                        retVal=((CollabFileHandlerSupport)fh).isRegionOverlap(beginLine, endLine);
                } catch(Exception e) {
                    Debug.logDebugException("LRM, exception: ",e,true);//NoI18n
                    //allow lock originator to continue
                    retVal=false;
                }
            }
        }

        return retVal;
    }

    private FilesharingContext getContext() {
        return context;
    }

    private void sendMessage(CCollab collabBean) throws CollabException {
        context.sendMessage(collabBean);
        getContext().printAllData(
            "\nIn LRM::after sendMessage event: \n" + "sendMessageLockRegion/" +
            LockRegionManager.getLockMessageType(collabBean)
        ); //NoI18n		
    }

    public static String getLockMessageType(CCollab collabBean) {
        String type = "";
        Use2phase use2phase = collabBean.getChLockRegion().getUse2phase();

        if (use2phase == null) {
            return type;
        }

        if (use2phase.isRequest()) {
            type = "Request"; //NoI18n
        } else if (use2phase.isBegin()) {
            type = "Begin"; //NoI18n
        } else if (use2phase.getResponse() != null) {
            type = "Response"; //NoI18n

            U2pResponse response = use2phase.getResponse();

            if (response.isContentionFound()) {
                type += "/ContentionFound"; //NoI18n
            } else {
                type += "/ContentionNotFound"; //NoI18n
            }
        }

        return type;
    }

    public void setValid(boolean valid) {
        this.isValid = valid;
    }

    public boolean isValid() {
        return isValid;
    }
}
