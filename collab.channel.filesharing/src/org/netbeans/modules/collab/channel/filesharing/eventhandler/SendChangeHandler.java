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

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroup;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileChanged;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileChangedData;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroup;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroups;


/**
 * SendMessageJoin
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class SendChangeHandler extends FilesharingEventHandler {
    /**
     * constructor
     *
     */
    public SendChangeHandler(CollabContext context) {
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
        CCollab collab = null;
        SharedFileGroup[] sharedFileGroups = getContext().getSharedFileGroupManager().getAllSharedFileGroup();

        if ((sharedFileGroups == null) || (sharedFileGroups.length == 0)) {
            return null;
        }

        List tmpSavedSharedFileGroups = new ArrayList();

        for (int i = 0; i < sharedFileGroups.length; i++) {
            String fileGroupName = sharedFileGroups[i].getName();
            CollabFileHandler[] fileHandlers = sharedFileGroups[i].getFileHandlers();
            int fileChangeCount = 0;

            for (int j = 0; j < fileHandlers.length; j++) {
                CollabFileHandler collabFileHandler = fileHandlers[j];

                if ((collabFileHandler == null) || !collabFileHandler.isValid()) {
                    continue;
                }

                int fileHandlerState = collabFileHandler.getCurrentState();

                if (
                    (fileHandlerState == FilesharingContext.STATE_SENDFILE) ||
                        (fileHandlerState == FilesharingContext.STATE_RECEIVEDSENDFILE) ||
                        (fileHandlerState == FilesharingContext.STATE_RECEIVEDSENDCHANGE) ||
                        (fileHandlerState == FilesharingContext.STATE_RECEIVEDLOCK) ||
                        (fileHandlerState == FilesharingContext.STATE_RECEIVEDUNLOCK)
                ) {
                    continue;
                }

                boolean isFileChanged = collabFileHandler.isChanged();

                if (isFileChanged) {
                    fileChangeCount++;
                }
            }

            if (fileChangeCount == 0) {
                continue;
            } else {
                tmpSavedSharedFileGroups.add(sharedFileGroups[i]);
            }
        }

        if (tmpSavedSharedFileGroups.size() == 0) //no changes send
         {
            return null;
        }

        int totalChangeCount = 0;

        for (int i = 0; i < tmpSavedSharedFileGroups.size(); i++) {
            SharedFileGroup sharedFileGroup = (SharedFileGroup) tmpSavedSharedFileGroups.get(i);
            String fileGroupName = sharedFileGroup.getName();
            collab = new CCollab();
            collab.setVersion(getVersion());

            FileChanged fileChanged = new FileChanged();
            collab.setChFileChanged(fileChanged);

            FileGroups fileGroups = new FileGroups();
            getContext().constructFileGroups(
                fileGroups, getContext().getSharedFileGroupManager().getSharedFileGroup(fileGroupName)
            );
            fileChanged.setFileGroups(fileGroups);

            CollabFileHandler[] fileHandlers = sharedFileGroups[i].getFileHandlers();
            int fileChangeCount = 0;

            for (int j = 0; j < fileHandlers.length; j++) {
                FileChangedData fileChangedData = fileHandlers[j].constructFileChangedData(fileChanged);

                if (fileChangedData != null) {
                    fileChangeCount++;
                    fileChanged.addFileChangedData(fileChangedData);
                }
            }

            if (fileChangeCount == 0) {
                totalChangeCount++;

                continue;
            }
        }

        if (totalChangeCount == sharedFileGroups.length) //no changes send
         {
            return null;
        }

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

            collabFileHandler.setCurrentState(FilesharingContext.STATE_RECEIVEDSENDCHANGE);
            collabFileHandler.handleSendChange(messageOriginator, fileChangedData[i]);
            collabFileHandler.setCurrentState(FilesharingContext.STATE_UNKNOWN);
        }
    }
}
