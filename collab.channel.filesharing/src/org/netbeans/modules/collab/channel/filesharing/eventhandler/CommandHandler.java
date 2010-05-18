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

import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

import java.io.IOException;

import javax.swing.Action;

import org.netbeans.modules.collab.channel.filesharing.context.MessageContext;
import org.netbeans.modules.collab.channel.filesharing.context.ProjectContext;
import org.netbeans.modules.collab.channel.filesharing.event.DeleteFileEvent;
import org.netbeans.modules.collab.channel.filesharing.event.ProjectActionListEvent;
import org.netbeans.modules.collab.channel.filesharing.event.ProjectPerformActionEvent;
import org.netbeans.modules.collab.channel.filesharing.event.RenameFileEvent;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.Commands;
import org.netbeans.modules.collab.channel.filesharing.msgbean.DeleteFile;
import org.netbeans.modules.collab.channel.filesharing.msgbean.FilesystemCommand;
import org.netbeans.modules.collab.channel.filesharing.msgbean.ProjectAction;
import org.netbeans.modules.collab.channel.filesharing.msgbean.ProjectActionList;
import org.netbeans.modules.collab.channel.filesharing.msgbean.ProjectCommand;
import org.netbeans.modules.collab.channel.filesharing.msgbean.ProjectPerformAction;
import org.netbeans.modules.collab.channel.filesharing.msgbean.RenameFile;
import org.netbeans.modules.collab.channel.filesharing.msgbean.User;
import org.netbeans.modules.collab.channel.filesharing.projecthandler.SharedProject;
import org.netbeans.modules.collab.channel.filesharing.projecthandler.SharedProjectFactory;
import org.netbeans.modules.collab.channel.filesharing.ui.ProjectsRootNode;
import org.netbeans.modules.collab.channel.filesharing.ui.actions.CollabProjectAction;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;
import org.netbeans.modules.collab.core.Debug;


/**
 * SendMessageJoin
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CommandHandler extends FilesharingEventHandler {
    /**
     * constructor
     *
     */
    public CommandHandler(CollabContext context) {
        super(context);
    }

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
            handleMsg(collabBean, messageOriginator, isUserSame);
        } else {
            boolean skipSend = skipSendMessage(eventID);
            CCollab collab = constructMsg(evContext);

            if (collab != null && !skipSend) {
                sendMessage(collab);
            }
        }
    }

    /**
     * constructMsg
     *
     * @param        evContext                                Event Context
     */
    public CCollab constructMsg(EventContext evContext)
    throws CollabException {
        CCollab collab = null;

        if (evContext.getEventID().equals(DeleteFileEvent.getEventID())) {
            collab = constructDeleteFile(evContext);

            if (collab != null) {
                getContext().printAllData("\nIn FEV::after constructDeleteFile");
            }
        } else if (evContext.getEventID().equals(RenameFileEvent.getEventID())) {
            collab = constructRenameFile(evContext);

            if (collab != null) {
                getContext().printAllData("\nIn FEV::after constructRenameFile");
            }
        } else if (evContext.getEventID().equals(ProjectActionListEvent.getEventID())) {
            collab = constructProjectActionList(evContext);

            if (collab != null) {
                getContext().printAllData("\nIn FEV::after constructProjectActionList");
            }
        } else if (evContext.getEventID().equals(ProjectPerformActionEvent.getEventID())) {
            collab = constructProjectPerformAction(evContext);

            if (collab != null) {
                getContext().printAllData("\nIn FEV::after constructProjectPerformAction");
            }
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
        Commands commands = collabBean.getChCommands();

        if (commands.getFilesystemCommand() != null) {
            FilesystemCommand filesystemCommand = commands.getFilesystemCommand();

            if (filesystemCommand != null) {
                if (filesystemCommand.getDeleteFile() != null) {
                    if (isUserSame) {
                        getContext().setReceivedMessageState(false);

                        return; //skip if fileowner==loginUser
                    }

                    DeleteFile deleteFile = filesystemCommand.getDeleteFile();
                    getContext().printAllData("\nIn FEV::before handleDeleteFile");
                    handleDeleteFile(deleteFile, messageOriginator, isUserSame);
                    getContext().printAllData("\nIn FEV::after handleDeleteFile");
                } else if (filesystemCommand.getRenameFile() != null) {
                    if (isUserSame) {
                        getContext().setReceivedMessageState(false);

                        return; //skip if fileowner==loginUser
                    }

                    RenameFile renameFile = filesystemCommand.getRenameFile();
                    getContext().printAllData("\nIn FEV::before handleRenameFile");
                    handleRenameFile(renameFile, messageOriginator, isUserSame);
                    getContext().printAllData("\nIn FEV::after handleRenameFile");
                }
            }
        } else if (commands.getProjectCommand() != null) {
            ProjectCommand projectCommand = commands.getProjectCommand();

            if (projectCommand.getProjectActionList() != null) {
                if (isUserSame) {
                    getContext().setReceivedMessageState(false);

                    return; //skip if fileowner==loginUser
                }

                ProjectActionList projectActionList = projectCommand.getProjectActionList();
                getContext().printAllData("\nIn FEV::before handleProjectActionList");
                handleProjectActionList(projectActionList, messageOriginator, isUserSame);
                getContext().printAllData("\nIn FEV::after handleProjectActionList");
            } else if (projectCommand.getProjectPerformAction() != null) {
                if (isUserSame) {
                    getContext().setReceivedMessageState(false);

                    return; //skip if fileowner==loginUser
                }

                ProjectPerformAction performAction = projectCommand.getProjectPerformAction();
                getContext().printAllData("\nIn FEV::before handleProjectPerformAction");
                handleProjectPerformAction(performAction, messageOriginator, isUserSame);
                getContext().printAllData("\nIn FEV::after handleProjectPerformAction");
            }
        }
    }

    /**
     * constructDeleteFile
     *
     * @param        evContext                                        Event Context
     */
    public CCollab constructDeleteFile(EventContext evContext)
    throws CollabException {
        Object source = evContext.getSource();

        if (!(source instanceof CollabFileHandler)) {
            return null;
        }

        CollabFileHandler fh = (CollabFileHandler) source;
        String fileGroupName = fh.getFileGroupName();

        String fileName = fh.getName();

        if ((fileName == null) || fileName.trim().equals("")) {
            return null;
        }

        //skip if .nbattrs file
        int lastIndex = fileName.lastIndexOf(FILE_SEPERATOR);
        String tmpFileName = fileName;

        if ((lastIndex != -1) && fileName.substring(lastIndex + 1).trim().equals(".nbattrs")) //NoI18n
         {
            return null;
        }

        lastIndex = fileName.lastIndexOf('.');

        String fileExt = null;

        if (lastIndex != -1) {
            fileExt = fileName.substring(lastIndex + 1);
        }

        Debug.log(this, "SendFileHandler, fileExt: " + fileExt + " for file: [" + fileName + "]"); //NoI18n

        if ((fileExt != null) && fileExt.equals("class")) //skip class delete
         {
            return null;
        }

        boolean skipSendDeleteFile = getContext().isSkipSendDeleteFile(fileName);

        if (skipSendDeleteFile) {
            return null;
        }

        getContext().setSkipSendDeleteFile(fileName, true);

        if (fh != null) {
            fh.setValid(false);
        }

        boolean status = deleteSharedFiles(fileGroupName, getContext().getLoginUser(), true); //remove handler only
        getContext().setSkipSendDeleteFile(fileName, false);

        CCollab collab = new CCollab();
        collab.setVersion(getVersion());

        Commands commands = new Commands();
        collab.setChCommands(commands);

        FilesystemCommand filesystemCommand = new FilesystemCommand();
        commands.setFilesystemCommand(filesystemCommand);

        DeleteFile deleteFile = new DeleteFile();
        filesystemCommand.setDeleteFile(deleteFile);
        deleteFile.setFileName(fileName);

        return collab;
    }

    /**
     * constructRenameFile
     *
     * @param        evContext                                        Event Context
     */
    public CCollab constructRenameFile(EventContext evContext)
    throws CollabException {
        Debug.out.println("CommandHandler, constructRenameFile");

        Object source = evContext.getSource();

        if (!(source instanceof CollabFileHandler)) {
            return null;
        }

        CollabFileHandler fh = (CollabFileHandler) source;
        String fileGroupName = fh.getFileGroupName();

        String fileName = fh.getName();

        if ((fileName == null) || fileName.trim().equals("")) {
            return null;
        }

        CCollab collab = new CCollab();
        collab.setVersion(getVersion());

        Commands commands = new Commands();
        collab.setChCommands(commands);

        FilesystemCommand filesystemCommand = new FilesystemCommand();
        commands.setFilesystemCommand(filesystemCommand);

        RenameFile renameFile = new RenameFile();
        filesystemCommand.setRenameFile(renameFile);
        renameFile.setFileName(fileName);

        String newFileName = fileName.substring(0, fileName.lastIndexOf(FILE_SEPERATOR) + 1) +
            fh.getFileObject().getNameExt();
        Debug.out.println("old File: " + fileName);
        Debug.out.println("new File: " + newFileName);
        renameFile.setToFileName(newFileName);

        return collab;
    }

    /**
     * constructProjectActionList
     *
     * @param        evContext                                        Event Context
     */
    public CCollab constructProjectActionList(EventContext evContext)
    throws CollabException {
        ProjectContext pContext = (ProjectContext) evContext;
        String projectName = pContext.getProjectName();
        SharedProject sharedProject = getContext().getSharedProjectManager().getSharedProject(
                getLoginUser(), projectName
            );
        Action[] actions = sharedProject.getProjectActions();

        CCollab collab = new CCollab();
        collab.setVersion(getVersion());

        Commands commands = new Commands();
        collab.setChCommands(commands);

        ProjectCommand projectCommand = new ProjectCommand();
        commands.setProjectCommand(projectCommand);

        ProjectActionList actionList = new ProjectActionList();
        projectCommand.setProjectActionList(actionList);

        actionList.setProjectName(projectName);

        User user = new User();
        user.setId(getContext().getLoginUser());
        actionList.setProjectUser(user);

        if (actions != null) {
            ProjectAction[] projectActions = new ProjectAction[actions.length];

            for (int i = 0; i < actions.length; i++) {
                String name = "PROJECT_ACTION_SEPERATOR";

                if (actions[i] != null) {
                    name = (String) actions[i].getValue(Action.NAME);
                }

                ProjectAction projectAction = new ProjectAction();
                projectAction.setName(name);
                projectAction.setDescription(name);
                projectActions[i] = projectAction;
            }

            actionList.setProjectAction(projectActions);
        }

        //cancel any scheduled task for this project
        getContext().cancelProjectActionListTimerTask(projectName);

        return collab;
    }

    /**
     * constructProjectActionList
     *
     * @param        evContext                                        Event Context
     */
    public CCollab constructProjectPerformAction(EventContext evContext)
    throws CollabException {
        ProjectContext pContext = (ProjectContext) evContext;
        String projectName = pContext.getProjectName();
        String projectOwner = pContext.getProjectOwner();
        Action[] actions = pContext.getProjectActions();

        CCollab collab = new CCollab();
        collab.setVersion(getVersion());

        Commands commands = new Commands();
        collab.setChCommands(commands);

        ProjectCommand projectCommand = new ProjectCommand();
        commands.setProjectCommand(projectCommand);

        ProjectPerformAction performAction = new ProjectPerformAction();
        projectCommand.setProjectPerformAction(performAction);
        performAction.setProjectName(projectName);

        User user = new User();
        user.setId(projectOwner);
        performAction.setProjectUser(user);

        ProjectAction[] projectActions = new ProjectAction[actions.length];

        for (int i = 0; i < actions.length; i++) {
            ProjectAction projectAction = new ProjectAction();
            String name = (String) actions[i].getValue(Action.NAME);
            projectAction.setName(name);
            projectAction.setDescription(name);
            projectActions[i] = projectAction;
        }

        performAction.setProjectAction(projectActions);

        return collab;
    }

    /**
     * Process delete file
     *
     * @param messageOriginator
     * @param collabBean
     */
    public void handleDeleteFile(DeleteFile deleteFile, String messageOriginator, boolean isUserSame)
    throws CollabException {
        Debug.out.println("CommandHandler, handleDeleteFile");

        String deleteFileName = deleteFile.getFileName();

        if ((deleteFileName == null) || deleteFileName.trim().equals("")) {
            return;
        }

        Debug.out.println("CommandHandler, file: " + deleteFileName);

        CollabFileHandler collabFileHandler = getContext().getSharedFileGroupManager().getFileHandler(deleteFileName);

        if (collabFileHandler != null) {
            collabFileHandler.setValid(false);

            String fileGroupName = collabFileHandler.getFileGroupName();
            Debug.out.println("CommandHandler, fileGroup: " + fileGroupName);
            try {
                deleteSharedFiles(fileGroupName, messageOriginator, false); //remove handler + files
            } catch(Exception e) {
                Debug.logDebugException("exception during delete file: " +
                        deleteFileName, e, true);
            }
        }
    }

    /**
     * Process rename file
     *
     * @param messageOriginator
     * @param collabBean
     */
    public void handleRenameFile(RenameFile renameFile, String messageOriginator, boolean isUserSame)
    throws CollabException {
        Debug.out.println("CommandHandler, handleRenameFile");

        String fileName = renameFile.getFileName();
        final String newFileName = renameFile.getToFileName();
        doRenameFile(fileName, newFileName);
    }
    
    private boolean doRenameFile(final String fromFileName, final String newFileName) {

        if ((fromFileName == null) || fromFileName.trim().equals("")) {
            return false;
        }

        Debug.out.println("old File: "+fromFileName);
 	Debug.out.println("new File: "+newFileName);   
        
        final CollabFileHandler fh = getContext().getSharedFileGroupManager().getFileHandler(fromFileName);
        if (fh == null) return false;

        try {
            getCollabFilesystem().runAtomicAction(
                new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        try {
                            String toFileName = newFileName.substring(newFileName.lastIndexOf(FILE_SEPERATOR) + 1);
                            String ext = "";
                            int lastIndex = toFileName.lastIndexOf('.');

                            if (lastIndex != -1) {
                                ext = toFileName.substring(lastIndex + 1);
                                toFileName = toFileName.substring(0, lastIndex);
                            }

                            FileLock fileLock = fh.getFileObject().lock();
                            fh.getFileObject().rename(fileLock, toFileName, ext);
                        } catch (FileAlreadyLockedException ale) {
                            Debug.log(this, "FilesharingContext, " + //NoI18n
                                "file rename failed for: " + newFileName
                            ); //NoI18n
                            Debug.logDebugException(
                                "FilesharingContext, " + //NoI18n
                                "file rename failed for: " + newFileName, //NoI18n	
                                ale, true
                            );
                        } catch (CollabException ce) {
                            Debug.log(this, "FilesharingContext, " + //NoI18n
                                "file rename failed for: " + newFileName
                            ); //NoI18n
                            Debug.logDebugException(
                                "FilesharingContext, " + //NoI18n
                                "file rename failed for: " + newFileName, //NoI18n	
                                ce, true
                            );
                        }
                    }
                }
            );
        } catch (IOException iox) {
            Debug.out.println("File Link Rename::ex: " + iox);
            iox.printStackTrace(Debug.out);
        } catch (Throwable th) {
            Debug.out.println("File Link Rename::ex: " + th);
            th.printStackTrace(Debug.out);
        }
        return true;
    }

    /**
     * Process delete file
     *
     * @param messageOriginator
     * @param collabBean
     */
    public void handleProjectActionList(
        ProjectActionList projectActionList, String messageOriginator, boolean isUserSame
    ) throws CollabException {
        String projectName = projectActionList.getProjectName();
        ProjectAction[] projectActions = projectActionList.getProjectAction();

        if ((projectActions == null) || (projectActions.length == 0)) {
            return;
        }

        Action[] pNodeactions = new Action[projectActions.length];

        for (int i = 0; i < projectActions.length; i++) {
            String name = "PROJECT_ACTION_SEPERATOR";

            if (projectActions[i].equals("PROJECT_ACTION_SEPERATOR")) {
                pNodeactions[i] = null;
            } else {
                pNodeactions[i] = new CollabProjectAction(projectActions[i].getName(), null, null, getContext(), null);
            }
        }

        SharedProject sharedProject = getContext().getSharedProjectManager().getSharedProject(
                messageOriginator, projectName
            );

        if (sharedProject == null) {
            Debug.log("ProjectsRootNode", "ProjectsRootNode, createSharedProject: " + projectName);
            sharedProject = SharedProjectFactory.createSharedProject(
                    projectName, messageOriginator, null, getContext().getSharedProjectManager()
                );
            getContext().getSharedProjectManager().addSharedProject(messageOriginator, projectName, sharedProject);
        }

        sharedProject.setProjectActions(pNodeactions);

        //set project node actions
        if (
            (getContext().getFilesystemExplorer() == null) ||
                (getContext().getFilesystemExplorer().getRootNode() == null)
        ) {
            return;
        }

        Children childs = getContext().getFilesystemExplorer().getRootNode().getChildren();
        Node[] userNodes = childs.getNodes();

        for (int i = 0; i < userNodes.length; i++) {
            Debug.out.println("CommandHandler CommandHandler, userNode: " + userNodes[i].getName());

            Node userNode = childs.findChild(messageOriginator);

            if (userNode != null) {
                Debug.out.println("CommandHandler CommandHandler, projectNodes: " + userNode.getChildren());

                Node projectNode = userNode.getChildren().findChild(projectName);

                if (projectNode != null) {
                    Debug.out.println("CommandHandler CommandHandler, projectNode: " + projectNode.getName());

                    ProjectsRootNode.ProjectNode pNode = (ProjectsRootNode.ProjectNode) projectNode;
                    FileshareUtil.setProjectActions(pNode, pNodeactions, null, getContext());
                }
            }
        }
    }

    /**
     * Process delete file
     *
     * @param messageOriginator
     * @param collabBean
     */
    public void handleProjectPerformAction(
        ProjectPerformAction performAction, String messageOriginator, boolean isUserSame
    ) throws CollabException {
        ProjectAction[] projectActions = performAction.getProjectAction();

        if ((projectActions == null) || (projectActions.length == 0)) {
            return;
        }

        String projectName = performAction.getProjectName();
        Debug.out.println("CommandHandler CommandHandler, projectName: " + projectName);

        String projectUser = performAction.getProjectUser().getId();
        Debug.out.println("CommandHandler CommandHandler, projectUser: " + projectUser);

        //set project node actions
        if (
            (getContext().getFilesystemExplorer() == null) ||
                (getContext().getFilesystemExplorer().getRootNode() == null)
        ) {
            return;
        }

        Children childs = getContext().getFilesystemExplorer().getRootNode().getChildren();
        Node[] userNodes = childs.getNodes();

        for (int i = 0; i < userNodes.length; i++) {
            Debug.out.println("CommandHandler CommandHandler, userNode: " + userNodes[i].getName());

            Node userNode = childs.findChild(getContext().getLoginUser());

            if (userNode != null) {
                Debug.out.println("CommandHandler CommandHandler, projectNodes: " + userNode.getChildren());

                Node projectNode = userNode.getChildren().findChild(projectName);

                if (projectNode != null) {
                    Debug.out.println("CommandHandler CommandHandler, projectNode: " + projectNode.getName());

                    ProjectsRootNode.ProjectNode pNode = (ProjectsRootNode.ProjectNode) projectNode;

                    for (int j = 0; j < projectActions.length; j++) {
                        String actionName = projectActions[j].getName();
                        Debug.out.println("CommandHandler CommandHandler, projectAction: " + actionName);

                        if ((actionName != null) && !actionName.equals("PROJECT_ACTION_SEPERATOR")) {
                            SystemAction pAction = (SystemAction) pNode.getAction(actionName);

                            if (pAction != null) {
                                Debug.out.println("CommandHandler CommandHandler, performAction: " + actionName);
                                pAction.actionPerformed(null);
                            }
                        }
                    }
                }
            }
        }
    }
}
