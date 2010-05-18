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

import org.openide.awt.UndoRedo;
import org.openide.windows.TopComponent;

import java.util.HashMap;
import java.util.TimerTask;
import java.util.Vector;
import java.util.List;

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

        // remove lrm after 5 sec, since the 2phase lock should be done between 200ms to 2200ms
        TimerTask cleanupTask = new TimerTask() {
            public void run() {
                Debug.log("FilesharingContext","LRM, final cleanup"); //NoI18n
                cleanup();
            }
        };
        getContext().schedule(cleanupTask, 4000);
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
	
	//set use2phase element to lock message
        Use2phase use2phase = new Use2phase();
        use2phase.setUse2phaseId(this.lockID);
        collabBean.getChLockRegion().setUse2phase(use2phase);        

        CollabFileHandler fh=getFileHandler();

        //if this is the fileowner then no need for 2phase lock, send the beginlock msg
        if(getContext().isFileOwner(getContext().getLoginUser(), fh.getName())) {
            sendBeginLockMessage(collabBean);
            return;
        }
                  
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
        Debug.log("FilesharingContext","LRM, undoDocumentUpdate"); //NoI18n
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
        Debug.log("FilesharingContext","LRM, sendLockRequest: "+use2phaseID);//NoI18n
        Use2phase use2phase = collabBean.getChLockRegion().getUse2phase();
        if (use2phase == null) {
            use2phase = new Use2phase();
            use2phase.setUse2phaseId(this.lockID);
            collabBean.getChLockRegion().setUse2phase(use2phase);
        }
        use2phase.setRequest(true);
        sendMessage(collabBean);
    }

    public boolean processLockReply(String user, CCollab collabBean)
    throws CollabException {
        Debug.log("FilesharingContext", "LRM, processLockReply: " + userID); //NoI18n		

        boolean beginLock = false;
        Use2phase use2phase = collabBean.getChLockRegion().getUse2phase();
	
	CollabFileHandler fh = getFileHandler();
        if (fh == null) return false;
	
        //process "lock-region/use2phase/request" message if this user is file owner
        if (use2phase.isRequest() && getContext().isFileOwner(getContext().getLoginUser(), fh.getName())) {
            Debug.log("FilesharingContext","LRM, processing request");//NoI18n
            U2pResponse u2pResponse = new U2pResponse();
            ((CollabFileHandlerSupport)fh).lockFileForCreateRegion();
            if (isContentionFound()) {
                Debug.log("FilesharingContext","LRM, contention found");//NoI18n
                u2pResponse.setContentionFound(true);
            } else {
                Debug.log("FilesharingContext","LRM, contention not found");//NoI18n
                u2pResponse.setContentionNotfound(true);
                //temporarily asssign these lineregions, then unassign if the requester
                //didn't beginlock in 3 seconds
                Vector lineRegions=regionInfo.getLineRegion();
                long unassignDelay=3000;
                assignBeforeLockRegion(lineRegions, unassignDelay);                                
            }
            ((CollabFileHandlerSupport)fh).unlockFileForCreateRegion();

            use2phase.setResponse(u2pResponse);

            //reset request
            use2phase.setRequest(false);
            sendMessage(collabBean);
            beginLock = false;
        } else if (use2phase.isBegin()) { //process normal handleLock if "lock-region/use2phase/begin" message
            Debug.log("FilesharingContext","LRM, processing begin");//NoI18n
            beginLock = true;
            cleanup();
        } else if (use2phase.getResponse() != null && isUserStarted2Phase) {
            //process response only if this user started 2phase Locking
	    Debug.log("FilesharingContext","LRM, process response");//NoI18n
            U2pResponse response = use2phase.getResponse();

            if (response.isContentionFound()) { //release any lock and start over
                Debug.log("FilesharingContext","LRM, contention found");//NoI18n
                //received response from file owner, so start again
                beginLock = false;
                undoDocumentUpdate();
                unAssignRegion(regionInfo.getID());
                ((CollabFileHandlerSupport)fh).unlockFileForCreateRegion();                                
                cleanup();                                
            } else if (response.isContentionNotfound()) {
                Debug.log("FilesharingContext","LRM, contention not found");//NoI18n
                //received response from file owner, so beginlock
                beginLock=false;
                sendBeginLockMessage(collabBean);
            }
        }
        return beginLock;
    }

    private void unAssignRegion(String regionName) throws CollabException {
        Debug.log("FilesharingContext","LRM, unAssignRegion region: " +regionName); //NoI18n
        CollabFileHandler fh = getFileHandler();
        CollabRegion r = ((CollabFileHandlerSupport) fh).getRegion(regionName);
        if (r != null) r.setValid(false);

        List lines=((CollabFileHandlerSupport)fh).getLineRegions(regionName);
        if (lines != null) {
            for(int i=0;i<lines.size();i++) {
                CollabLineRegion liner=(CollabLineRegion)lines.get(i);
                if (liner != null) {
                    Debug.log("FilesharingContext","LRM, setAssigned " +
                        "false for: " + liner.getID()); //NoI18n
                    liner.setAssigned(null, false);
                }
            }
        }
        ((CollabFileHandlerSupport) fh).removeRegion(userID, regionName);
    }
    
    private void assignBeforeLockRegion(final Vector lineRegions, long unassignDelay) throws CollabException {
        Debug.log("FilesharingContext","LRM, assignBeforeLockRegion");//NoI18n
        if (lineRegions != null && lineRegions.size()>0) {
            Debug.log("FilesharingContext","LRM, lineRegions.size: "+lineRegions.size());//NoI18n
            for (int i=0;i<lineRegions.size();i++) {
                CollabLineRegion liner=(CollabLineRegion)lineRegions.get(i);
                if (liner != null) {
                    Debug.log("FilesharingContext","LRM, setAssigned " +
                        "true for: " + liner.getID()); //NoI18n
                    liner.setAssigned(null, true);
		}
            }
        }
	
        TimerTask unassignTask = new TimerTask() {
            public void run() {
	        CollabLineRegion beginLine=(CollabLineRegion)lineRegions.firstElement();
                CollabLineRegion endLine=(CollabLineRegion)lineRegions.lastElement();
                // If these lineregions are not assigned yet, then unassign them
                if (beginLine != null && endLine != null &&
                        beginLine.getAssignedRegion() == null &&
                        endLine.getAssignedRegion() == null) {
                    for (int i=0;i<lineRegions.size();i++) {
                        CollabLineRegion liner=(CollabLineRegion)lineRegions.get(i);
                        if (liner != null) {
                            if (liner.getAssignedRegion() == null) {
                                Debug.log("FilesharingContext","LRM, setAssigned " +
                                        "false for: " + liner.getID()); //NoI18n
                                liner.setAssigned(null, false);
                            }
                        }
                    }
                }
            }
        };
        getContext().schedule(unassignTask, unassignDelay);
    }

    private void sendBeginLockMessage(CCollab collabBean) throws CollabException {
        Debug.log("FilesharingContext","LRM, sendBeginLockMessage");//NoI18n
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
        Debug.log("FilesharingContext","LRM, cleanup");
        if (tt != null) tt.cancel();
        setValid(false);
        CollabFileHandler fh=getFileHandler();
        if (fh != null) {
            ((CollabFileHandlerSupport)fh).removeLockRegionManager(this.lockID);
        }
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
