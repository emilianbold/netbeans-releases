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
package org.netbeans.modules.collab.channel.filesharing.filehandler;

import com.sun.collablet.CollabException;

import org.openide.filesystems.*;

import javax.swing.text.StyledDocument;

import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener.CollabDocumentListener;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileChanged;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileChangedData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LockRegionData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.SendFile;
import org.netbeans.modules.collab.channel.filesharing.msgbean.SendFileData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.UnlockRegion;
import org.netbeans.modules.collab.channel.filesharing.msgbean.UnlockRegionData;


/**
 * Interface for Collab Filesharing FileHandlers
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public interface CollabFileHandler {
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////

    /* empty file content */
    public static final String EMPTY_CONTENT = "\n\n\n"; //NoI18n	

    /* empty file content */
    public static final String DIGEST = "xxxx"; //NoI18n

    /* empty file content */
    public static final String ENCODING = "base64"; //NoI18n

    /* COLLAB_LINEREGION_USER */
    public static final String COLLAB_LINEREGION_USER = "COLLAB_LINEREGION_USER"; //NoI18n

    /* special text handling contenttype */
    public static final String TEXT_UNKNOWN = "text/unknown"; //NoI18n

    ////////////////////////////////////////////////////////////////////////////
    // Message handler methods
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
    throws CollabException;

    /**
     * handles send-file message
     *
     * @param   messageOriginator   the sender of this message
    * @param   sendFileData                the send-file-data Node inside the message
     * @throws CollabException
     */
    public void handleSendFile(String messageOriginator, SendFileData sendFileData)
    throws CollabException;

    /**
     * constructs lock-region-data Node
     *
     * @param   lockRegionData                the intial lock-region-data Node
     * @throws CollabException
     */
    public void constructLockRegionData(LockRegionData lockRegionData)
    throws CollabException;

    /**
     * constructs lock-region-data Node
     *
     * @param   regionInfo                        the RegionInfo bean
     * @param   lockRegionData                the intial lock-region-data Node
     * @throws CollabException
     * @see                RegionInfo
     */
    public void constructLockRegionData(RegionInfo regionInfo, LockRegionData lockRegionData)
    throws CollabException;

    /**
     * handles lock message
     *
     * @param   messageOriginator   the sender of this message
    * @param   lockRegionData                the lock-region-data Node inside the message
     * @throws CollabException
     */
    public void handleLock(String messageOriginator, LockRegionData lockRegionData)
    throws CollabException;

    /**
     * constructs unlock-region-data Node
     *
     * @param   regionName                                the regionName
     * @param   unlockRegionData                the intial unlock-region-data Node
     * @throws CollabException
     */
    public void constructUnlockRegionData(String regionName, UnlockRegionData unlockRegionData)
    throws CollabException;

    /**
     * constructs unlock-region-data Node
     *
     * @param   unlockRegionData                the intial unlock-region-data Node
     * @throws CollabException
     */
    public boolean constructUnlockRegionData(UnlockRegion unlockRegion)
    throws CollabException;

    /**
     * handles unlock message
     *
     * @param   messageOriginator   the sender of this message
    * @param   unlockRegionData        the unlock-region-data Node inside the message
     * @throws CollabException
     */
    public void handleUnlock(String messageOriginator, UnlockRegionData unlockRegionData)
    throws CollabException;

    /**
     * constructs file-changed-data Node
     *
     * @param fileChanged the file-changed Node
     * @return fileChangedData     the file-changed-data Node
     * @throws CollabException
     */
    public FileChangedData constructFileChangedData(FileChanged fileChanged)
    throws CollabException;

    /**
     * handles send-change message
     *
     * @param   messageOriginator   the sender of this message
    * @param   fileChangedData                the fileChangedData Node inside the message
     * @throws CollabException
     */
    public void handleSendChange(String messageOriginator, FileChangedData fileChangedData)
    throws CollabException;

    /**
     * handles pause message
     *
     * @throws CollabException
     */
    public void handlePause() throws CollabException;

    /**
     * handles resume message
     *
     * @throws CollabException
     */
    public void handleResume() throws CollabException;

    /**
    * creates a CollabRegion, a super-class for all regions
    *
    * @param   regionName          the regionName
    * @param   beginOffset                        the beginOffset
    * @param        endOffset                        the endOffset
     * @return        CollabRegion                created user region
     * @throws CollabException
     */
    public CollabRegion createRegion(String regionName, int beginOffset, int endOffset)
    throws CollabException;

    /**
    * creates a CollabRegion, a super-class for all regions
    *
    * @param   regionName                        the regionName
    * @param   beginOffset                        the beginOffset
    * @param        endOffset                        the endOffset
     * @param        testOverlap                        testOverlap before create
     * @return        CollabRegion                created user region
     * @throws CollabException
     */
    public CollabRegion createRegion(String regionName, int beginOffset, int endOffset, boolean testOverlap)
    throws CollabException;

    /**
         * doUpdateRegion
         *
         * @param   messageOriginator   the sender of this message
         * @param JavaEditor#simpleSection
         * @param text
         * @throws CollabException
         */
    public boolean doUpdateRegion(String messageOriginator, CollabRegion region, String text)
    throws CollabException;

    ////////////////////////////////////////////////////////////////////////////
    // Document methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * add DocumentListener
     *
     * @return DocumentListener
     * @throws CollabException
     */
    public CollabDocumentListener addDocumentListener()
    throws CollabException;

    /**
     * get the file document content of a region
     *
     * @param   regionName                                the regionName
     * @return        document Content                content
     * @throws CollabException
     */
    public String getContent(String regionName) throws CollabException;

    /**
     * getCurrentState
     *
     * @return state
     */
    public int getCurrentState();

    /**
     * setCurrentState
     *
     * @param state
     */
    public void setCurrentState(int state);

    /**
     * setCurrentState
     *
     * @param currentState
     * @param delay
     * @param saveUnconditionally
     * @param changeSkipUpdate
     */
    public void setCurrentState(
        final int currentState, long delay, final boolean saveUnconditionally, final boolean changeSkipUpdate
    );

    ////////////////////////////////////////////////////////////////////////////
    // Filehandler methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * getter for fileObject
     *
     * @return        fileObject                                fileObject of file being handled
     * @throws CollabException
     */
    public FileObject getFileObject() throws CollabException;

    /**
     * return document object for this file
     *
     * @throws CollabException
     * @return document
     */
    public StyledDocument getDocument() throws CollabException;

    /**
     * getter for fileName
     *
     * @return        fileName                                name of file being handled
     */
    public String getName();

    /**
     * getter for fileSize
     *
     * @return        fileSize                                size of file being handled
     */
    public long getFileSize() throws CollabException;

    /**
     * setter for fileName
     *
     * @param        fileName                                name of file being handled
     */
    public void setFileName(String fileName);

    /**
     * getter for groupName where this file belongs
     *
     * @return        fileGroupName                        name of fileGroup
     */
    public String getFileGroupName();

    /**
     * setter for sharedFileGroup where this file belongs
     *
     * @param        sharedFileGroup
     */
    public void setFileGroup(SharedFileGroup sharedFileGroup);

    /**
     * getter for contentType
     *
     * @return contentType
     */
    public String getContentType();

    /**
     * setter for contentType
     *
     * @param        contentType
     */
    public void setContentType(String contentType);

    /**
     * isSendFirstTime
     *
     * @return        isSendFirstTime
     */
    public boolean isSendFirstTime();

    /**
     * setter for fileObject
     *
     * @param        fileObject                                fileObject of file being handled
     */
    public void setFileObject(FileObject fileObject);

    /**
     * setter for CollabContext
     *
     * @param        context
     */
    public void setContext(CollabContext context);

    /**
     * test if the file changed
     *
     * @return        true/false                                true if file changes
     */
    public boolean isChanged() throws CollabException;

    /**
     * register DocumentListener
     *
     * @throws CollabException
     */
    public void registerDocumentListener() throws CollabException;

    /**
     * skip Insert or Remove if set to true
     *
     * @param        status                                        if true skip insert/remove
     * @throws CollabException
     */
    public void setSkipUpdate(boolean skipStatus);

    /**
     * setSkipUpdateAlways
     *
     * @param skipUpdateAlways
     */
    public void setSkipUpdateAlways(boolean skipUpdateAlways);

    /**
     * isRetrieveFileContentOnly
     *
     */
    public boolean isRetrieveFileContentOnly();

    /**
     * setRetrieveFileContentOnly
     *
     */
    public void setRetrieveFileContentOnly(boolean flag);

    /**
     * isDocumentModified
     *
     */
    public boolean isDocumentModified() throws CollabException;

    /**
     * saveDocument
     *
     */
    public boolean saveDocument() throws CollabException;

    /**
     * setValid
     *
     * @param        status                                        if false handler is invalid
     * @throws CollabException
     */
    public void setValid(boolean valid) throws CollabException;

    /**
     * test if the filehandler is valid
     *
     * @return        true/false                                true if valid
     */
    public boolean isValid() throws CollabException;

    /**
     * clear
     *
     * @throws CollabException
     */
    public void clear() throws CollabException;
}
