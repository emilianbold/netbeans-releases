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

import javax.swing.text.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.event.DocumentChangeInsert;
import org.netbeans.modules.collab.channel.filesharing.event.DocumentChangeRemove;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandlerSupport;
import org.netbeans.modules.collab.channel.filesharing.filehandler.RegionQueue;
import org.netbeans.modules.collab.channel.filesharing.filehandler.RegionQueueItem;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.util.DocumentEventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileChanged;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileChangedData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroup;
import org.netbeans.modules.collab.core.Debug;


/**
 * FileChangeHandler
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class DocumentChangeHandler extends FilesharingEventHandler implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* RegionQueue */
    RegionQueue queue = null;

    /* collabFileHandler */
    private CollabFileHandler collabFileHandler = null;

    /**
     * constructor
     *
     */
    public DocumentChangeHandler(CollabContext context) {
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
    public CCollab constructMsg(EventContext evContext) {
        Debug.log("SendFileHandler", "DocumentChangeHandler, constructMsg"); //NoI18n

        DocumentEventContext devContext = (DocumentEventContext) evContext;
        Document document = (Document) devContext.getSource();
        collabFileHandler = getContext().getSharedFileGroupManager().getFileHandler(document);

        if (collabFileHandler == null) {
            Debug.log("SendFileHandler", "DocumentChangeHandler, collabFileHandler null"); //NoI18n

            return null;
        }

        int fileHandlerState = collabFileHandler.getCurrentState();

        if (
            (fileHandlerState == FilesharingContext.STATE_SENDFILE) ||
                (fileHandlerState == FilesharingContext.STATE_RECEIVEDSENDCHANGE) ||
                (fileHandlerState == FilesharingContext.STATE_RECEIVEDLOCK) ||
                (fileHandlerState == FilesharingContext.STATE_RECEIVEDUNLOCK)
        ) {
            return null;
        }

        if (devContext.getEventID().equals(DocumentChangeInsert.getEventID())) {
            try {
                insertUpdate(devContext.getOffset(), devContext.getText());
            } catch (CollabException ce) {
                Debug.errorManager.notify(ce);
            }
        } else if (evContext.getEventID().equals(DocumentChangeRemove.getEventID())) {
            try {
                removeUpdate(devContext.getOffset(), devContext.getLength());
            } catch (CollabException ce) {
                Debug.errorManager.notify(ce);
            }
        }

        return null;
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

            collabFileHandler.handleSendChange(messageOriginator, fileChangedData[i]);
        }
    }

    /**
     * this method is invoked by the FilesharingDocumentListener during text insert
     *
     * @param        offset                                        insert offset
     * @param        text                                        insert text
     * @throws CollabException
     */
    public void insertUpdate(final int offset, final String text)
    throws CollabException {
        final int textLength = text.length();
        Debug.log(
            "SendFileHandler",
            "DocumentChangeHandler, " + "insertUpdate offset: " + offset + "text length: " + textLength + " text: [" +
            text + "]"
        ); //NoI18n

        //skip updateText related insert
        if (((CollabFileHandlerSupport) collabFileHandler).skipInsertUpdate(offset)) {
            Debug.log("SendFileHandler", "DocumentChangeHandler, " + "skipInsertUpdate: true"); //NoI18n

            return;
        }

        ((CollabFileHandlerSupport) collabFileHandler).updateStatusFileChanged(offset, textLength, true);

        boolean regionExist = ((CollabFileHandlerSupport) collabFileHandler).regionExist(offset, textLength, 1);

        if (!regionExist) {
            ((CollabFileHandlerSupport) collabFileHandler).unlockAllUserRegions();
            createNewRegion(true, offset, textLength, text);
        }
    }

    /**
     * this method is invoked by the FilesharingDocumentListener during text remove
     *
     * @param        offset                                        remove offset
     * @param        length                                        remove text length
     * @throws CollabException
     */
    public void removeUpdate(final int offset, final int length)
    throws CollabException {
        Debug.log(
            "SendFileHandler", "DocumentChangeHandler, " + "removeUpdate offset: " + offset + " length: " + length
        ); //NoI18n	

        if ((offset == 0) && (length == 1)) {
            Debug.log("SendFileHandler", "DocumentChangeHandler, " + "offset==0 && length==1"); //NoI18n

            return;
        }

        //skip updateText related remove
        if (((CollabFileHandlerSupport) collabFileHandler).skipRemoveUpdate(offset, length)) {
            Debug.log("SendFileHandler", "DocumentChangeHandler, " + "skipRemoveUpdate: true"); //NoI18n

            return;
        }

        ((CollabFileHandlerSupport) collabFileHandler).updateStatusFileChanged(offset, -length, true);

        boolean regionExist = ((CollabFileHandlerSupport) collabFileHandler).regionExist(offset, -length, 1);

        if (!regionExist) {
            ((CollabFileHandlerSupport) collabFileHandler).unlockAllUserRegions();
            createNewRegion(false, offset, length, null);
        }
    }

    /**
     * create a New Region, sends a lock message
     *
     * @param insertUpdate
     * @param offset
     * @param length
     * @throws CollabException
     */
    public void createNewRegion(boolean insertUpdate, int offset, int length, String insertText)
    throws CollabException {
        Debug.log(
            "SendFileHandler", "DocumentChangeHandler, " + "createNewRegion offset: " + offset + " length: " + length
        ); //NoI18n

        if (length == 0) {
            return;
        }

        StyledDocument fileDocument = ((CollabFileHandlerSupport) collabFileHandler).getDocument();
        int currLine = fileDocument.getDefaultRootElement().getElementIndex(offset);
        int beginLine = currLine;
        int endLine = currLine + 1;

        //do the correction for
        int endOffsetCorrection = 0;
        int elementCount = fileDocument.getDefaultRootElement().getElementCount();

        if (insertUpdate) {
            endOffsetCorrection = -length;
            endLine = fileDocument.getDefaultRootElement().getElementIndex(offset + length);
        } else {
            endOffsetCorrection = length;
            beginLine = currLine - 1;

            if (beginLine < 0) {
                beginLine = 0;
            }

            //adjust endLine for delete of last line, bug#6276326		
            if ((endLine == elementCount) && (endLine > 0)) {
                endLine -= 1;
            }
        }

        Debug.log(
            "SendFileHandler",
            "DocumentChangeHandler, " + "createNewRegion beginLine: " + beginLine + " endLine: " + endLine
        ); //NoI18n
        Debug.log("SendFileHandler", "DocumentChangeHandler, " + "createNewRegion elementCount: " + elementCount); //NoI18n		

        if ((endLine >= elementCount) || (beginLine >= elementCount)) {
            Debug.log(
                "SendFileHandler",
                "DocumentChangeHandler, " + "cannot create region, since " +
                "endLine>=elementCount || beginLine>=elementCount "
            ); //NoI18n

            return;
        }

        //Testing overlap 2 lines (beginLine, endLine)
        if (!((CollabFileHandlerSupport) collabFileHandler).testCreateRegionLineBounds(beginLine, endLine)) {
            beginLine = currLine;
            endLine = currLine;
            Debug.log(
                "SendFileHandler",
                "DocumentChangeHandler, " + "createNewRegion beginLine: " + beginLine + " endLine: " + endLine
            ); //NoI18n				
        }

        RegionQueueItem item = new RegionQueueItem(beginLine, endLine, endOffsetCorrection);
        addQueue(item);
    }

    /**
     * addQueue
     *
     * @param        qItem
     */
    public void addQueue(RegionQueueItem qItem) {
        ((CollabFileHandlerSupport) collabFileHandler).getQueue().addItem(qItem);
    }
}
