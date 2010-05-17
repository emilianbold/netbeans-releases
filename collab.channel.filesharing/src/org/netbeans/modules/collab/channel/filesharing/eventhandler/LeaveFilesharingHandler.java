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

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import java.io.IOException;

import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroup;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LeaveFilesharing;
import org.netbeans.modules.collab.channel.filesharing.msgbean.User;
import org.netbeans.modules.collab.core.Debug;


/**
 * LeaveFilesharing EventHandler
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class LeaveFilesharingHandler extends FilesharingEventHandler {
    /**
     * constructor
     *
     */
    public LeaveFilesharingHandler(CollabContext context) {
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
        CCollab collab = new CCollab();
        collab.setVersion(getVersion());

        LeaveFilesharing leaveFilesharing = new LeaveFilesharing();
        collab.setChLeaveFilesharing(leaveFilesharing);

        //add this element to message when begin to join a conversation.
        User userObj = new User();
        userObj.setId(getLoginUser());
        leaveFilesharing.setUser(userObj);

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
            getContext().setReceivedMessageState(false);

            return; //skip if fileowner==loginUser
        }

        LeaveFilesharing leaveFilesharing = collabBean.getChLeaveFilesharing();
        String newFileOwner = getContext().getNewFileOwner(leaveFilesharing.getNewFileOwner().getUsers());

        //delete all shared files that belong to this user
        String[] fileGroupNames = getContext().getUserSharedFileGroupNames(messageOriginator);

        if (fileGroupNames != null) {
            for (int i = 0; i < fileGroupNames.length; i++) {
                SharedFileGroup sharedFileGroup = getContext().getSharedFileGroupManager().getSharedFileGroup(
                        fileGroupNames[i]
                    );

                if (sharedFileGroup != null) {
                    //set valid to false before delete
                    CollabFileHandler[] fhs = sharedFileGroup.getFileHandlers();

                    for (int j = 0; j < fhs.length; j++) {
                        if (fhs[j] != null) {
                            fhs[j].setValid(false);

                            String fileGroupName = fhs[j].getFileGroupName();
                            Debug.out.println("LeaveFS, fileGroup: " + fileGroupName);
                            try {
                                deleteSharedFiles(fileGroupName, messageOriginator, false);
                            } catch(Exception e) {
                                Debug.logDebugException("LeaveFS, Exception " + 
                                        "removing files and filehandlers",e,true);
                            } 
                        }
                    }

                    //remove all previous file to owner map for messageOriginator
                    getContext().removeFileOwnerMap(sharedFileGroup);
                }
            }
        }

        if (
            (getContext().getFilesystemExplorer() != null) &&
                (getContext().getFilesystemExplorer().getRootNode() != null)
        ) {
            Node userNode = getContext().getFilesystemExplorer().getRootNode().getChildren().findChild(
                    messageOriginator
                );

            try {
                if (userNode != null) {
                    DataObject dd = (DataObject) userNode.getCookie(DataObject.class);

                    //delete node
                    if (userNode != null) {
                        userNode.destroy();
                    }
                }
            } catch (IOException iox) {
                Debug.out.println("LeaveFS delete userNode ex: " + iox);
                iox.printStackTrace(Debug.out);
            }
        }

        //remove the current file owner from the fileowner list
        getContext().removeFileOwner(messageOriginator);

        //remove the user that leaves filesharing, from the user list
        getContext().removeUser(messageOriginator);

        String newModerator = getContext().getNewModerator(leaveFilesharing.getNewModerator().getUsers());

        //We dont use this data, everyone is a moderator
        if ((newModerator != null) && !newModerator.equals("") && newModerator.equals(getLoginUser())) {
            //isModerator = true;
        }
    }
}
