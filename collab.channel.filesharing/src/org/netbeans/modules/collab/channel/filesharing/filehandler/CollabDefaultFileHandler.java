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
package org.netbeans.modules.collab.channel.filesharing.filehandler;

import com.sun.collablet.CollabException;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.util.*;

import javax.swing.SwingUtilities;

import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.eventhandler.SendFileHandler;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.SendFileTimerTask;
import org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener.CollabDocumentListener;
import org.netbeans.modules.collab.channel.filesharing.msgbean.Content;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileChangedData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.SendFile;
import org.netbeans.modules.collab.channel.filesharing.msgbean.SendFileData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.UnlockRegionData;
import org.netbeans.modules.collab.core.Debug;


/**
 * FileHandler for Default files (non-editable files)
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabDefaultFileHandler extends CollabFileHandlerSupport implements CollabFileHandler {
    /**
     * DefaultFileHandler constructor
     *
     */
    public CollabDefaultFileHandler() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // File handler methods
    ////////////////////////////////////////////////////////////////////////////	

    /**
     * constructs send-file-data Node
     *
     * @param   sendFile                        the send-file Node
    * @param   syncOperation                is send-file for sync file during user join
     * @return        sendFileData                the send-file-data Node
     * @throws CollabException
     */
    public SendFileData constructSendFileData(SendFile sendFile)
    throws CollabException {
        if (!isValid()) {
            return null;
        }

        setCurrentState(FilesharingContext.STATE_SENDFILE);

        SendFileData sendFileData = new SendFileData();
        FileData fileData = new FileData();
        sendFileData.setFileData(fileData);

        fileData.setFileName(getName());
        fileData.setContentType(getContentType());

        Content content = new Content();
        sendFileData.setContent(content);

        content.setEncoding(getEncoding());
        content.setDigest(getDigest());

        String encodedFile = encodeBase64(getFileContentBytes());
        content.setData(encodedFile);

        if (firstTimeSend) {
            firstTimeSend = false;

            //add listener for this document
            FileObject fileObject = getFileObject();
            fileObject.addFileChangeListener(new DefaultFileChangeListener());
        }

        return sendFileData;
    }

    /**
     * handles send-file message
     *
     * @param   messageOriginator   the sender of this message
    * @param   sendFileData                the send-file-data Node inside the message
     * @throws CollabException
     */
    public void handleSendFile(String messageOriginator, SendFileData sendFileData)
    throws CollabException {
        setCurrentState(FilesharingContext.STATE_RECEIVEDSENDFILE);

        //copy contents from message to files; add files to CollabFileSystem
        try {
            String fullPath = sendFileData.getFileData().getFileName();

            Content sendFileContent = sendFileData.getContent();

            byte[] fileContent = decodeBase64(sendFileContent.getData());

            FileObject file = getFileObject(); //do not create 

            if (file == null) {
                file = createFileObject(fileContent);

                //add listener for this document
                file.addFileChangeListener(new DefaultFileChangeListener());
            } else {
                inReceiveSendFile = true;
                updateFileObject(fileContent);
                getFileObject().refresh(false);
                inReceiveSendFile = false;
            }

            firstTimeSend = false;
        } catch (IllegalArgumentException iargs) {
            throw new CollabException(iargs);
        }
    }

    /**
     * handles lock message
     *
     * @param   messageOriginator   the sender of this message
    * @param   lockRegionData                the lock-region-data Node inside the message
     * @throws CollabException
     */
    public void handleLock(String messageOriginator, LockRegionData lockRegionData)
    throws CollabException {
        throw new CollabException("UnSupported: Region Lock is not" + "not supported for this file: " + getName());
    }

    /**
     * handles send-change message
     *
     * @param   messageOriginator   the sender of this message
    * @param   fileChangedData                the fileChangedData Node inside the message
     * @throws CollabException
     */
    public void handleSendChange(String messageOriginator, FileChangedData fileChangedData)
    throws CollabException {
        throw new CollabException("UnSupported: File Change is not" + "not supported for this file: " + getName());
    }

    /**
     * handles unlock message
     *
     * @param   messageOriginator   the sender of this message
    * @param   unlockRegionData        the unlock-region-data Node inside the message
     * @throws CollabException
     */
    public void handleUnlock(String messageOriginator, UnlockRegionData unlockRegionData)
    throws CollabException {
        throw new CollabException("UnSupported: Region Unlock is not" + "not supported for this file: " + getName());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Document methods
    ////////////////////////////////////////////////////////////////////////////        

    /**
     * getter for contentType
     *
     * @return contentType
     */
    public String getContentType() {
        return CollabDefaultFileHandlerFactory.CONTENT_UNKNOWN;
    }

    /**
     * getter for filehandler
     *
     * @return        filehandler
     */
    public CollabFileHandler getFileHandler() {
        return this;
    }

    /**
     * add DocumentListener
     *
     * @return DocumentListener
     * @throws CollabException
     */
    public CollabDocumentListener addDocumentListener()
    throws CollabException {
        return null;
    }

    /**
     * this method is invoked by the CollabDocumentListener during text insert
     *
     * @param        offset                                        insert offset
     * @param        text                                        insert text
     * @throws CollabException
     */
    public void insertUpdate(int offset, String text) throws CollabException {
        return;
    }

    /**
     * this method is invoked by the CollabDocumentListener during text remove
     *
     * @param        offset                                        remove offset
     * @param        length                                        remove text length
     * @throws CollabException
     */
    public void removeUpdate(int offset, int length) throws CollabException {
        return;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Management methods
    ////////////////////////////////////////////////////////////////////////////        

    /**
     * creates a CollabRegion, a super-class for all regions
     *
     * @param testOverlap
     * @param regionName the regionName
     * @param beginOffset the beginOffset
     * @param endOffset the endOffset
     * @param testOverlap
     * @param guarded
     * @throws CollabException
     * @return
     */
    public CollabRegion createRegion(
        String regionName, int beginOffset, int endOffset, boolean testOverlap, boolean guarded
    ) throws CollabException {
        throw new CollabException("UnSupported: create Region is not" + "not supported for this file: " + getName());
    }

    /**
     * a callback for filechange, sends a send-file message
     *
     * @throws CollabException
     */
    public void notifyFileChanged() throws CollabException {
        Debug.log(
            "CollabFileHandlerSupport",
            "CollabFileHandlerSupport, " + //NoI18n
            "notifyFileChanged, inReceiveSendFile: " + inReceiveSendFile
        ); //NoI18n		

        if (!inReceiveSendFile) {
            updateStatusChanged(true);

            Debug.log("CollabFileHandlerSupport", "CollabFileHandlerSupport, " + //NoI18n
                "notifyFileChanged"
            ); //NoI18n

            //send refresh only from file-owner
            if (
                !getContext().isFileOwner(getContext().getLoginUser(), getName()) ||
                    getContext().isSkipSendFile(getName())
            ) {
                return;
            }

            //cancel any scheduled task for this fileGroup
            getContext().cancelSendFileMessageTimerTask(getFileGroupName());

            //Send refresh
            SharedFileGroup sharedFileGroup = getContext().getSharedFileGroupManager().getSharedFileGroup(
                    getFileGroupName()
                );

            if (sharedFileGroup != null) //shared file exist
             {
                boolean sendFirstTime = isSendFirstTime();
                Debug.log(this, "SendFileHandler, sendFirstTime: " + sendFirstTime + " for file: [" + getName() + "]"); //NoI18n

                int state = getCurrentState();
                Debug.log(this, "SendFileHandler, currentState: " + state + " for file: [" + getName() + "]"); //NoI18n

                if (
                    sendFirstTime || (state == FilesharingContext.STATE_SENDFILE) ||
                        (state == FilesharingContext.STATE_RECEIVEDSENDFILE)
                ) {
                    return;
                }

                SendFileTimerTask sendFileObjectTimerTask = getSendFileTimerTask();

                if (sendFileObjectTimerTask != null) {
                    sendFileObjectTimerTask.cancel();
                }

                //reset state
                setCurrentState(FilesharingContext.STATE_UNKNOWN);

                if (getContext().getSendFileMessageTimerTask(getFileGroupName()) != null) { //sync already in progress

                    return;
                }

                NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(
                            SendFileHandler.class, "MSG_SendFileHandler_SendSync",
                            getName().substring(getName().lastIndexOf(FILE_SEPERATOR_CHAR) + 1)
                        ), NotifyDescriptor.OK_CANCEL_OPTION
                    );

                if (DialogDisplayer.getDefault().notify(descriptor) != NotifyDescriptor.OK_OPTION) {
                    //If user selected not to send sync, then prevent
                    //any further sync messages for 6 sec

                    /*for(int i=0;i<fileHandlerList.size();i++)
                    {
                            CollabFileHandler collabFileHandler =
                                    (CollabFileHandler)fileHandlerList.get(i);
                            if(collabFileHandler!=null)
                            {
                                    collabFileHandler.setCurrentState(
                                            FilesharingContext.STATE_SENDFILE,
                                            FilesharingTimerTask.PERIOD*6,
                                            false);
                            }
                    }*/
                    return;
                }

                Debug.log(this, "SendFileHandler, user: " + getContext().getLoginUser()); //NoI18n						
                Debug.log(this, "SendFileHandler, Send refresh for " + //NoI18n
                    "fileGroupName: " + getFileGroupName()
                ); //NoI18n	

                final String tmpfileGroupName = getFileGroupName();
                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            try {
                                //cancel any scheduled task for this fileGroup
                                getContext().cancelSendFileMessageTimerTask(tmpfileGroupName);

                                SendFileTimerTask sendSendFileMessageTimerTask = getContext()
                                                                                     .startSendFileMessageTimerTask(
                                        tmpfileGroupName, 100
                                    );
                            } catch (Throwable th) {
                                Debug.log(
                                    "CollabFileHandlerSupport",
                                    "CollabFileHandlerSupport, " + //NoI18n
                                    "cannot sendFile fileGroup: " + tmpfileGroupName
                                ); //NoI18n
                                Debug.logDebugException(
                                    "CollabFileHandlerSupport, " + //NoI18n
                                    "cannot sendFile fileGroup: " + tmpfileGroupName, //NoI18n	
                                    th, true
                                );
                            }
                        }
                    }
                );

                return;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////        
    public class DefaultFileChangeListener extends Object implements FileChangeListener {
        /**
         * constructor
         *
         * @throws CollabException
         */
        public DefaultFileChangeListener() throws CollabException {
            super();
            Debug.log("CollabFileHandlerSupport", "DefaultFileChangeListener()");
        }

        ////////////////////////////////////////////////////////////////////////////
        // methods
        ////////////////////////////////////////////////////////////////////////////           

        /**
         *
         * @param fileAttributeEvent
         */
        public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
        }

        /**
         * fileChanged
         *
         * @param fileEvent
         */
        public void fileChanged(FileEvent fileEvent) {
            try {
                CollabDefaultFileHandler.this.notifyFileChanged();
            } catch (CollabException ce) {
                ErrorManager.getDefault().notify(ce);
            }
        }

        /**
         *
         * @param fileEvent
         */
        public void fileDataCreated(FileEvent fileEvent) {
        }

        /**
         *
         * @param fileEvent
         */
        public void fileDeleted(FileEvent fileEvent) {
        }

        /**
         *
         * @param fileEvent
         */
        public void fileFolderCreated(FileEvent fileEvent) {
        }

        /**
         *
         * @param fileRenameEvent
         */
        public void fileRenamed(FileRenameEvent fileRenameEvent) {
        }
    }
}
