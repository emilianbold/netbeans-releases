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

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.context.UnlockRegionContext;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingTimerTask;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroup;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FileGroups;
import org.netbeans.modules.collab.channel.filesharing.msgbean.UnlockRegion;
import org.netbeans.modules.collab.channel.filesharing.msgbean.UnlockRegionData;


/**
 * UnlockRegionHandler
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class UnlockRegionHandler extends FilesharingEventHandler {
    /**
     * constructor
     *
     */
    public UnlockRegionHandler(CollabContext context) {
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

        UnlockRegion unlockRegion = new UnlockRegion();
        collab.setChUnlockRegion(unlockRegion);

        SharedFileGroup[] sharedFileGroups = null;
        UnlockRegionContext unlockRegionContext = (UnlockRegionContext) evContext;
        String fileGroupName = unlockRegionContext.getFileGroupName();

        if ((fileGroupName != null) && !fileGroupName.trim().equals("")) {
            SharedFileGroup sharedFileGroup = getContext().getSharedFileGroupManager().getSharedFileGroup(
                    fileGroupName
                );

            if (sharedFileGroup != null) {
                List sharedGroupList = new ArrayList();
                sharedGroupList.add(sharedFileGroup);
                sharedFileGroups = (SharedFileGroup[]) sharedGroupList.toArray(new SharedFileGroup[0]);
            }
        } else {
            sharedFileGroups = getContext().getSharedFileGroupManager().getAllSharedFileGroup();
        }

        if ((sharedFileGroups == null) || (sharedFileGroups.length == 0)) {
            return null;
        }

        List sharedFileGroupList = new ArrayList();

        for (int i = 0; i < sharedFileGroups.length; i++) {
            fileGroupName = sharedFileGroups[i].getName();

            CollabFileHandler[] fileHandlers = sharedFileGroups[i].getFileHandlers();

            for (int j = 0; j < fileHandlers.length; j++) {
                CollabFileHandler collabFileHandler = fileHandlers[j];

                if (collabFileHandler == null) {
                    continue;
                }

                boolean status = collabFileHandler.constructUnlockRegionData(unlockRegion);

                if (!status) {
                    continue;
                }

                try {
                    //sleep for 100 millis
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                sharedFileGroupList.add(getContext().getSharedFileGroupManager().getSharedFileGroup(fileGroupName));
            }
        }

        if (sharedFileGroupList.size() == 0) {
            return null;
        }

        FileGroups fileGroups = new FileGroups();
        getContext().constructFileGroups(
            fileGroups, (SharedFileGroup[]) sharedFileGroupList.toArray(new SharedFileGroup[0])
        );
        unlockRegion.setFileGroups(fileGroups);

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

        UnlockRegion unlockRegion = collabBean.getChUnlockRegion();
        UnlockRegionData[] UnlockRegionData = unlockRegion.getUnlockRegionData();

        for (int i = 0; i < UnlockRegionData.length; i++) {
            String fullPath = UnlockRegionData[i].getFileName();
            CollabFileHandler collabFileHandler = getContext().getSharedFileGroupManager().getFileHandler(fullPath);

            if (collabFileHandler == null) {
                continue;
            }

            collabFileHandler.setCurrentState(
                FilesharingContext.STATE_RECEIVEDUNLOCK, FilesharingTimerTask.PERIOD * 3,
                !collabFileHandler.isDocumentModified(), false
            );
            collabFileHandler.handleUnlock(messageOriginator, UnlockRegionData[i]);
        }
    }
}
