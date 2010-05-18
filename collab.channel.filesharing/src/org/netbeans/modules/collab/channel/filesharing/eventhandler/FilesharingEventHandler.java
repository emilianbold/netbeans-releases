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
import com.sun.collablet.CollabPrincipal;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.*;

import java.io.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingCollablet;
import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.context.MessageContext;
import org.netbeans.modules.collab.channel.filesharing.event.JoinFilesharingEnd;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.JoinEndTimerTask;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroup;
import org.netbeans.modules.collab.channel.filesharing.filesystem.CollabFilesystem;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventHandler;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;
import org.netbeans.modules.collab.core.Debug;


/**
 * FilesharingEventHandler
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public abstract class FilesharingEventHandler extends Object implements EventHandler, FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* valid */
    private boolean valid = false;

    /* channel */
    private FilesharingCollablet channel;

    /* context */
    private FilesharingContext context = null;

    /* CollabFilesystem */
    private CollabFilesystem fs = null;

    /**
     * constructor
     *
     */
    public FilesharingEventHandler(CollabContext context) {
        this.context = (FilesharingContext) context;
        this.channel = (FilesharingCollablet) this.context.getChannel();
    }

    public FilesharingCollablet getChannel() {
        return this.channel;
    }

    public FilesharingContext getContext() {
        return this.context;
    }

    public CollabFilesystem getCollabFilesystem() {
        if (fs == null) {
            fs = (CollabFilesystem) this.context.getCollabFilesystem();
        }

        return fs;
    }

    public String getLoginUser() {
        return getContext().getLoginUser();
    }

    /**
         * constructMsg
         *
         * @param        evContext                                        Event Context
         */
    public abstract CCollab constructMsg(EventContext evContext)
    throws CollabException;

    /**
         * handleMsg
         *
         * @param        collabBean
         * @param        messageOriginator
         * @param        isUserSame
         */
    public abstract void handleMsg(CCollab collabBean, String messageOriginator, boolean isUserSame)
    throws CollabException;

    ////////////////////////////////////////////////////////////////////////////
    // Event Handler methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * exec
     *
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
            getContext().printAllData("\nIn FEV::before handleMsg event: \n" + eventID); //NoI18n				
            handleMsg(collabBean, messageOriginator, isUserSame);
            getContext().printAllData("\nIn FEV::after handleMsg event: \n" + eventID); //NoI18n				
        } else {
            boolean skipSend = skipSendMessage(eventID);

            if (skipSend) {
                return;
            }

            CCollab collab = constructMsg(evContext);

            if (collab != null) {
                getContext().printAllData("\nIn FEV::after constructMsg event: \n" + eventID); //NoI18n				
                sendMessage(collab);
            }
        }

        /*if(!isUserSame)
        {
                showDialog(eventID, user);
        }*/
    }

    /**
     *
     * @param key
     * @param user
     */
    public void showDialog(String key, Object[] args) {
        String message = NbBundle.getMessage(FilesharingEventHandler.class, key, args);

        if ((message == null) || message.trim().equals("")) {
            return;
        }

        NotifyDescriptor descriptor = new NotifyDescriptor.Message(message);
        DialogDisplayer.getDefault().notify(descriptor);
    }

    /**
     *
     * @param key
     * @param user
     */
    public boolean showConfirmDialog(String key, Object[] args) {
        String message = NbBundle.getMessage(FilesharingEventHandler.class, key, args);

        if ((message == null) || message.trim().equals("")) {
            return false;
        }

        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(message, NotifyDescriptor.OK_CANCEL_OPTION);

        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION) {
            return true;
        }

        return false;
    }

    //	/**
    //	 * showWaitDialog
    //	 *
    //	 */
    //    public boolean showWaitDialog(String titleKey, String messageKey, Object[] args)
    //	{
    //        Frame parent = WindowManager.getDefault().getMainWindow();
    //        if(parent == null)
    //		{
    //			return false;
    //		}
    //
    //        JDialog waitDialog = 
    //			new JDialog(parent, NbBundle.getMessage(FilesharingEventHandler.class, 
    //			titleKey));
    //		getContext().setWaitDialog(waitDialog);
    //        JLabel loadText =
    //			new JLabel(
    //				NbBundle.getMessage(FilesharingEventHandler.class,
    //				messageKey, args),
    //				JLabel.CENTER);
    //        waitDialog.getContentPane().add(loadText,BorderLayout.CENTER);
    //        waitDialog.pack();
    //        Point parentCenter = parent.getLocation();
    //        double pXLoc = parentCenter.getX();
    //        double pYLoc = parentCenter.getY();
    //        int pX = parent.getWidth();
    //        int pY = parent.getHeight();
    //        int cX = waitDialog.getWidth();
    //        int cY = waitDialog.getHeight();
    //        int cXLoc = (int)pXLoc + pX/2 - cX/2;
    //        int cYLoc = (int)pYLoc + pY/2 - cY/2;
    //        Point childCenter = new Point(cXLoc, cYLoc);
    //        waitDialog.setLocation(childCenter);
    //        waitDialog.setVisible(true);
    //        parent.requestFocus();
    //        return true;
    //    }	

    /**
     *
     * @param collabBean
     * @throws CollabException
     */
    protected void sendMessage(CCollab collabBean) throws CollabException {
        getContext().sendMessage(collabBean);
    }

    /**
     * scheduleJoinEnd
     *
     * @param        delay
     */
    public void scheduleJoinEnd(long delay) throws CollabException {
        JoinEndTimerTask sendJoinMessageTimerTask = new JoinEndTimerTask(
                getContext().getChannelEventNotifier(),
                new JoinFilesharingEnd(new EventContext(JoinFilesharingEnd.getEventID(), null)), getContext()
            );
        getContext().addTimerTask(SEND_JOINEND_TIMER_TASK, sendJoinMessageTimerTask);
        sendJoinMessageTimerTask.schedule(FileshareUtil.getRandomCount(delay));
        getContext().setJoinFlag(false);
        getContext().clearExpectedFileMap();
    }

    /**
     * delete shared files
     *
     * @param file
     * @param user
     * @param skipDelete
     */
    public boolean deleteSharedFiles(String fileGroupName, String user, boolean skipDelete)
    throws CollabException {
        boolean status = false;
        SharedFileGroup sharedFileGroup = getContext().getSharedFileGroupManager().getSharedFileGroup(fileGroupName);

        if ((sharedFileGroup != null) && sharedFileGroup.isValid()) //shared file exist
         {
            sharedFileGroup.setValid(false);

            CollabFileHandler[] fileHandlers = sharedFileGroup.getFileHandlers();

            if (fileHandlers != null) {
                for (int i = 0; i < fileHandlers.length; i++) {
                    CollabFileHandler collabFileHandler = fileHandlers[i];

                    if (collabFileHandler != null) {
                        collabFileHandler.setValid(false);
                    }

                    String fileName = collabFileHandler.getName();
                    FileObject fileObject = collabFileHandler.getFileObject();
                    Debug.log("FilesharingEventHandler", "CommandHandler, handleDeleteFile: " + fileName); //NoI18n						
                    getContext().setSkipSendDeleteFile(fileName, true);

                    //FileObject fileObject=
                    //	getCollabFilesystem().findResource(fileName);
                    if (fileObject != null) {
                        status = deleteSharedFiles(fileObject, user, skipDelete); //remove handler + files
                    }

                    getContext().setSkipSendDeleteFile(fileName, false);
                }
            }

            getContext().removeAllFileHandlerRef(sharedFileGroup, user);
        }

        return status;
    }

    /**
     * delete shared files
     *
     * @param file
     * @param user
     * @param skipDelete
     */
    public boolean deleteSharedFiles(final FileObject file, String user, boolean skipDelete)
    throws CollabException {
        boolean deleteStatus = true;

        if (!skipDelete && (file == null)) {
            return deleteStatus;
        }

        Debug.out.println("deleteSharedFiles: file: " + file.getPath());

        if (file.isData()) {
            String fileName = getCollabFilesystem().getPath(file);

            if (fileName == null) {
                return deleteStatus;
            }

            if (fileName.startsWith(FILE_SEPERATOR)) {
                fileName = fileName.substring(1);
            }

            try {
                if (!skipDelete) {
                    DataObject dd = null;

                    try {
                        dd = FileshareUtil.getDataObject(file);
                    } catch (Throwable th) {
                        th.printStackTrace(Debug.out);
                    }

                    final DataObject dataObject = dd;

                    //close file before delete
                    getContext().closeFile(file.getNameExt());

                    FileSystem fs = getCollabFilesystem();
                    fs.runAtomicAction(
                        new FileSystem.AtomicAction() {
                            public void run() throws IOException {
                                try {
                                    if (dataObject != null) {
                                        dataObject.delete();
                                    } else {
                                        file.delete();
                                    }
                                } catch (FileAlreadyLockedException ale) {
                                    //ignore
                                }
                            }
                        }
                    );
                }
            } catch (FileAlreadyLockedException ale) {
                Debug.log(this, "FilesharingContext, delete " + "failed for file: " + fileName); //NoI18n				
                Debug.logDebugException(
                    "FilesharingContext, delete " + "failed for file: " + fileName, //NoI18n	
                    ale, true
                );
                deleteStatus = promptUserSave(user, file);

                if (deleteStatus && !skipDelete) {
                    try {
                        file.delete();
                    } catch (IOException iox1) {
                        Debug.log(
                            this,
                            "FilesharingContext, delete " + "failed for file: " + getCollabFilesystem().getPath(file)
                        ); //NoI18n				
                        Debug.logDebugException(
                            "FilesharingContext, delete " + "failed for file: " + getCollabFilesystem().getPath(file), //NoI18n	
                            iox1, true
                        );

                        //show delete failed dialog
                        String[] args = new String[] {
                                getContext().getPrincipal(user).getDisplayName(), getCollabFilesystem().getPath(file)
                            };
                        showDialog("MSG_FilesharingEventHandler_DeleteFileFailed", args); //NoI18n

                        return false;
                    }
                }
            } catch (IOException iox) {
                Debug.log(this, "FilesharingContext, delete " + "failed for file: " + fileName); //NoI18n				
                Debug.logDebugException(
                    "FilesharingContext, delete " + "failed for file: " + fileName, //NoI18n	
                    iox, true
                );
                deleteStatus = promptUserSave(user, file);

                if (deleteStatus && !skipDelete) {
                    try {
                        file.delete();
                    } catch (IOException iox1) {
                        Debug.log(
                            this,
                            "FilesharingContext, delete " + "failed for file: " + getCollabFilesystem().getPath(file)
                        ); //NoI18n				
                        Debug.logDebugException(
                            "FilesharingContext, delete " + "failed for file: " + getCollabFilesystem().getPath(file), //NoI18n	
                            iox, true
                        );

                        //show delete failed dialog
                        String[] args = new String[] {
                                getContext().getPrincipal(user).getDisplayName(), getCollabFilesystem().getPath(file)
                            };
                        showDialog("MSG_FilesharingEventHandler_DeleteFileFailed", args); //NoI18n

                        return false;
                    }
                }
            } finally {
                CollabFileHandler collabFileHandler = getContext().getSharedFileGroupManager().getFileHandler(fileName);

                if (collabFileHandler != null) {
                    String fileGroupName = collabFileHandler.getFileGroupName();
                    SharedFileGroup sharedFileGroup = getContext().getSharedFileGroupManager().getSharedFileGroup(
                            fileGroupName
                        );
                    getContext().removeAllFileHandlerRef(sharedFileGroup, user);
                } else {
                    Debug.log(this, "FEV, fileHandler null for file: " + fileName); //NoI18n			
                }
            }
        } else {
            FileObject[] children = file.getChildren();

            for (int i = 0; i < children.length; i++) {
                return deleteSharedFiles(children[i], user, skipDelete);
            }

            //Delete self
            try {
                if (!skipDelete) {
                    file.delete();
                }
            } catch (IOException iox) {
                Debug.log(
                    this, "FilesharingContext, delete " + "failed for file: " + getCollabFilesystem().getPath(file)
                ); //NoI18n				
                Debug.logDebugException(
                    "FilesharingContext, delete " + "failed for file: " + getCollabFilesystem().getPath(file), //NoI18n	
                    iox, true
                );
                deleteStatus = promptUserSave(user, file);

                if (deleteStatus && !skipDelete) {
                    try {
                        file.delete();
                    } catch (IOException iox1) {
                        Debug.log(
                            this,
                            "FilesharingContext, delete " + "failed for file: " + getCollabFilesystem().getPath(file)
                        ); //NoI18n				
                        Debug.logDebugException(
                            "FilesharingContext, delete " + "failed for file: " + getCollabFilesystem().getPath(file), //NoI18n	
                            iox, true
                        );

                        //show delete failed dialog
                        String[] args = new String[] {
                                getContext().getPrincipal(user).getDisplayName(), getCollabFilesystem().getPath(file)
                            };
                        showDialog("MSG_FilesharingEventHandler_DeleteFileFailed", args); //NoI18n

                        return false;
                    }
                }
            }
        }

        return deleteStatus;
    }

    /**
     * promptUserDeleteAction
     *
     * @param file
     */
    public boolean promptUserSave(String user, FileObject file)
    throws CollabException {
        String fileName = getCollabFilesystem().getPath(file);

        //show delete option dialog
        String[] args = new String[] { getContext().getPrincipal(user).getDisplayName(), fileName };
        boolean ok = showConfirmDialog("MSG_FilesharingEventHandler_DeleteFileConfirm", args); //NoI18n
        boolean saveOk = false;

        if (ok) {
            saveOk = saveFile(fileName);
        }

        return saveOk;
    }

    /**
     * saveFile
     *
     */
    protected boolean saveFile(String fileName) throws CollabException {
        Debug.log("CollabFileHandlerSupport", //NoI18n
            "saving File: " //NoI18n
             +fileName + " before delete"
        ); //NoI18n	

        boolean status = false;

        try {
            CollabFileHandler collabFileHandler = getContext().getSharedFileGroupManager().getFileHandler(fileName);

            if (collabFileHandler != null) {
                status = collabFileHandler.saveDocument();
            }
        } catch (CollabException ce) {
            Debug.log(
                "CollabFileHandlerSupport", //NoI18n
                "Exception occured while saving the file: " //NoI18n
                 +fileName + " on update"
            ); //NoI18n
            Debug.logDebugException(
                "Exception occured while saving the file: " //NoI18n
                 +fileName + " on update", //NoI18n	
                ce, true
            );

            return false;
        }

        return status;
    }

    /**
     * setValid
     *
     * @param        valid
     * @throws CollabException
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * test if the filehandler is valid
     *
     * @return        true/false                                true if valid
     */
    public boolean isValid() {
        return valid;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Support methods
    ////////////////////////////////////////////////////////////////////////////
    public String getVersion() {
        return getContext().getVersion();
    }

    /**
     * skipSendMessage
     *
     * @return        flag
     */
    public boolean skipSendMessage(String messageID) {
        //skip sendMessage if readOnly privilige for this user
        if (getContext().isReadOnlyConversation()) {
            Debug.log(
                "SendFileHandler",
                "FilesharingEH, skipSendMessage: " + "readOnlyConveration for user: " + getContext().getLoginUser()
            ); //NoI18n               

            return true;
        }

        //do not send any message if 0 or 1 user
        //String[] filesharingUsers = getContext().getUser();        
        CollabPrincipal[] convUsers = getContext().getConversation().getParticipants();

        if (((convUsers != null) && (convUsers.length > 1)) //conv users are enough 
        //|| (filesharingUsers!=null && filesharingUsers.length>1)
        ) {
            return false;
        } else {
            Debug.log(
                "SendFileHandler",
                "FilesharingEH, skipSendMessage: " + "#of participants<=1 for user: " + getContext().getLoginUser()
            ); //NoI18n			
        }

        Debug.log("SendFileHandler", "FilesharingEH, skipSendMessage: " + //NoI18n
            messageID
        );

        return true;
    }
}
