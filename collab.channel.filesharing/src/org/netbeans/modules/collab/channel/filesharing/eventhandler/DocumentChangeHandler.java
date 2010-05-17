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

import javax.swing.text.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.event.DocumentChangeInsert;
import org.netbeans.modules.collab.channel.filesharing.event.DocumentChangeRemove;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandlerSupport;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabRegion;
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
                insertUpdate(document, devContext.getOffset(), devContext.getText());
            } catch (CollabException ce) {
                Debug.errorManager.notify(ce);
            }
        } else if (evContext.getEventID().equals(DocumentChangeRemove.getEventID())) {
            try {
                removeUpdate(document, devContext.getOffset(), devContext.getLength());
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
    private void insertUpdate(Document doc, final int offset, final String text)
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

        boolean regionExist =
                ((CollabFileHandlerSupport)collabFileHandler).isUserRegionExist(
                getLoginUser(), offset, textLength,1);

        if (!regionExist) {
            ((CollabFileHandlerSupport) collabFileHandler).lockFileForCreateRegion();
            // commented out: don't unlock forcibly, the region may be in first
            // phase of 2phase locking, sending unlock event now would cause
            // stale lock on the owner's side later (see #71965)
            //((CollabFileHandlerSupport) collabFileHandler).unlockAllUserRegions();
            createNewRegion(doc, true, offset, textLength, text);
        }
    }

    /**
     * this method is invoked by the FilesharingDocumentListener during text remove
     *
     * @param        offset                                        remove offset
     * @param        length                                        remove text length
     * @throws CollabException
     */
    private void removeUpdate(Document doc, final int offset, final int length)
    throws CollabException {
        Debug.log(
            "SendFileHandler", "DocumentChangeHandler, " + "removeUpdate offset: " + offset + " length: " + length
        ); //NoI18n	

        /*if ((offset == 0) && (length == 1)) {
            Debug.log("SendFileHandler", "DocumentChangeHandler, " + "offset==0 && length==1"); //NoI18n

            return;
        }*/

        //skip updateText related remove
        if (((CollabFileHandlerSupport) collabFileHandler).skipRemoveUpdate(offset, length)) {
            Debug.log("SendFileHandler", "DocumentChangeHandler, " + "skipRemoveUpdate: true"); //NoI18n

            return;
        }

        ((CollabFileHandlerSupport) collabFileHandler).updateStatusFileChanged(offset, -length, true);

        /*boolean regionExist = ((CollabFileHandlerSupport)collabFileHandler).isUserRegionExist(
                getLoginUser(), offset, -length, 1);             

        if (!regionExist) {
            ((CollabFileHandlerSupport)collabFileHandler).lockFileForCreateRegion();
            //((CollabFileHandlerSupport) collabFileHandler).unlockAllUserRegions();
            createNewRegion(false, offset, length, null);
        }*/
		
		CollabRegion userRegion = 
			((CollabFileHandlerSupport)collabFileHandler).
				getContainingUserRegion(getLoginUser(), offset, -length, 1);
		if(userRegion==null ||
				userRegion!=null && userRegion.getEndOffset()<(offset+length))
		{
			int newOffset=offset;
			
			//take beginOffset from the last line endoffset of the region found above
			if(userRegion!=null && userRegion.getEndOffset()<(offset+length))
				newOffset=userRegion.getEndOffset();
		
			((CollabFileHandlerSupport)collabFileHandler).lockFileForCreateRegion();
			createNewRegion(doc, false, newOffset, length, null);
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
    private void createNewRegion(Document doc, boolean insertUpdate, int offset, int length, String insertText)
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

        RegionQueueItem item = new RegionQueueItem(doc, beginLine, endLine, endOffsetCorrection);
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
