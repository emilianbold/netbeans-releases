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

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.Node;

import java.io.*;

import java.util.*;

import javax.swing.SwingUtilities;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingTimerTask;
import org.netbeans.modules.collab.channel.filesharing.filesystem.CollabFilesystem;
import org.netbeans.modules.collab.channel.filesharing.msgbean.SendFileData;
import org.netbeans.modules.collab.channel.filesharing.ui.*;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;
import org.netbeans.modules.collab.core.Debug;


/**
 * SharedFileGroup
 *
 * @author  ayub.khan@sun.com
 * @version                1.0
 */
public class SharedFileGroup extends Object implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////
    public final static int UNKNOWN_TYPE = -1;
    public final static int JAVA_TYPE = 1;
    public final static int FORM_TYPE = 2;

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private String fileGroupName;
    private int fileGroupType = UNKNOWN_TYPE;
    private String user;
    private String projectName;
    private SharedFileGroupManager manager = null;
    private FilesharingContext context = null;
    private List fileNames = new ArrayList();
    private boolean isValid = true;
    private boolean isSpecialFile = false;
    private FileLock fileLock = null;
    private EditorCookie cookie = null;
    private Node projectNode = null;

    /**
     *
     * @param fileGroupName
     * @param user
     * @param manager
     */
    public SharedFileGroup(String fileGroupName, String user, String projectName, SharedFileGroupManager manager) {
        super();
        this.fileGroupName = fileGroupName;
        this.user = user;
        this.projectName = projectName;
        this.manager = manager;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////                       

    /**
     *
     * @return
     */
    public String getName() {
        return this.fileGroupName;
    }

    /**
     *
     * @return
     */
    public int getType() {
        return this.fileGroupType;
    }

    /**
     *
     * @param
     */
    public void setType(int type) {
        this.fileGroupType = type;
    }

    /**
     *
     * @return
     */
    public String getUser() {
        return this.user;
    }

    /**
     *
     * @param user
     */
    public void setUser(String newUser) {
        this.user = newUser;
    }

    /**
     *
     * @return
     */
    public String getProjectName() {
        return this.projectName;
    }

    /**
     *
     * @return cookie
     */
    public EditorCookie getEditorCookie() {
        return this.cookie;
    }

    /**
     *
     * @param cookie
     */
    public void setEditorCookie(EditorCookie cookie) {
        this.cookie = cookie;
    }

    /**
     *
     * @param searchFileName
     * @return
     */
    public boolean contains(String searchFileName) {
        return fileNames.contains(searchFileName);
    }

    /**
     *
     * @param fileName
     */
    public void addFileHandler(CollabFileHandler fileHandler)
    throws CollabException {
        if ((fileHandler == null) || (fileHandler.getName() == null)) {
            throw new CollabException("Filename or FileHandler null"); //NoI18n
        }

        if (!fileNames.contains(fileHandler.getName())) {
            this.fileNames.add(fileHandler.getName());
        }

        manager.addFileHandler(fileHandler);
    }

    /**
     *
     * @return fileHandlers
     */
    public CollabFileHandler[] getFileHandlers() {
        List result = new ArrayList();

        for (int i = 0; i < fileNames.size(); i++) {
            CollabFileHandler fileHandler = manager.getFileHandler((String) fileNames.get(i));

            if (fileHandler != null) {
                result.add(fileHandler);
            }
        }

        return (CollabFileHandler[]) result.toArray(new CollabFileHandler[0]);
    }

    /**
     *
     * @return fileHandler
     */
    public CollabFileHandler getFileHandler(FileObject fileObject)
    throws CollabException {
        List result = new ArrayList();

        for (int i = 0; i < fileNames.size(); i++) {
            CollabFileHandler fileHandler = manager.getFileHandler((String) fileNames.get(i));

            if ((fileHandler != null) && (fileObject == fileHandler.getFileObject())) {
                return fileHandler;
            }
        }

        return null;
    }

    /**
     * setValid
     *
     * @param        status
     * @throws CollabException
     */
    public void setValid(boolean valid) throws CollabException {
        this.isValid = valid;
    }

    /**
     * getValid
     *
     * @return        status                                        if false handler is invalid
     * @throws CollabException
     */
    public boolean isValid() throws CollabException {
        return this.isValid;
    }

    /**
     * createFileHandler
     *
     * @param        fileGroupName
     * @param        fileObject
     * @return        collabFileHandler
     */
    public CollabFileHandler createFileHandler(
        final FilesharingContext context, final String fileOwner, final String projectName, final String fileName,
        final FileObject fileObject, final EditorCookie cookie
    ) throws CollabException {
        CollabFileHandler collabFileHandler = doCreateFileHandler(fileName, fileObject, false, cookie);

        if (collabFileHandler == null) {
            return null;
        }

        boolean sendFirstTime = collabFileHandler.isSendFirstTime();

        if (
            (collabFileHandler.getCurrentState() == FilesharingContext.STATE_SENDFILE) ||
                (collabFileHandler.getCurrentState() == FilesharingContext.STATE_RECEIVEDSENDFILE)
        ) {
            Debug.log(
                "CollabFileHandlerSupport",
                "CollabFileHandlerSupport, " + "current State: " + collabFileHandler.getCurrentState()
            ); //NoI18n			

            return null;
        }

        if (sendFirstTime) {
            collabFileHandler.setFileGroup(this);
            collabFileHandler.setFileObject(fileObject);

            //add file owner
            context.addFileOwner(fileOwner);

            //Add file to owner map for file annotations
            context.addToFileOwnerMap(fileName);

            try {
                manager.addToOwnerSharedFile(getName(), projectName, collabFileHandler);
            } catch (CollabException ce) {
                Debug.log("SendFileHandler", "SendFileHandler, " + //NoI18n
                    "addToOwnerSharedFile failed"
                ); //NoI18n
                Debug.logDebugException(
                    "SendFileHandler, " + //NoI18n
                    "addToOwnerSharedFile failed", //NoI18n	
                    ce, true
                );

                return null;
            }
        }

        //reset state
        collabFileHandler.setCurrentState(FilesharingContext.STATE_UNKNOWN);

        return collabFileHandler;
    }

    /**
     *
     * @param fileName
     * @param contentType
     * @throws CollabException
     * @return
     */
    public CollabFileHandler createFileHandler(String fileName, String fileContentType)
    throws CollabException {
        CollabFileHandler collabFileHandler = manager.getFileHandler(fileName);

        //Create an instance of CollabFileHandler
        if (collabFileHandler == null) {
            if (fileContentType == null) {
                //throw new CollabException("Unknown Content Type");
                return null;
            }

            Debug.log(this, "FilesharingContext, contentType: " + //NoI18n
                fileContentType + " for file: " + fileName
            ); //NoI18n			

            CollabFileHandlerFactory collabFileHandlerFactory = CollabFileHandlerResolver.getDefault().resolve(
                    fileContentType, FileshareUtil.getFileExt(fileName)
                );

            Debug.log(
                this, "FilesharingContext, collabFileHandlerFactory: " + //NoI18n
                collabFileHandlerFactory.getID()
            );

            if (
                (collabFileHandlerFactory != null) &&
                    collabFileHandlerFactory instanceof CollabDefaultFileHandlerFactory
            ) {
                if (
                    (fileContentType != null) &&
                        fileContentType.trim().equals(CollabTextFileHandlerFactory.TEXT_MIME_TYPE)
                ) {
                    collabFileHandlerFactory = new CollabTextFileHandlerFactory();
                } else {
                    Debug.log(this, "FilesharingContext, contentType null"); //NoI18n				
                }
            }

            if (collabFileHandlerFactory != null) {
                collabFileHandler = collabFileHandlerFactory.createCollabFileHandler();

                if (fileName.startsWith(FILE_SEPERATOR)) {
                    collabFileHandler.setFileName(fileName.substring(1));
                } else {
                    collabFileHandler.setFileName(fileName);
                }

                collabFileHandler.setFileGroup(this);
                collabFileHandler.setContext(getContext());

                if (collabFileHandlerFactory instanceof CollabTextFileHandlerFactory) {
                    String tmpcontentType = CollabTextFileHandlerFactory.TEXT_MIME_TYPE;

                    if (
                        (tmpcontentType == null) ||
                            ((tmpcontentType != null) &&
                            !tmpcontentType.trim().equals(CollabTextFileHandlerFactory.TEXT_MIME_TYPE))
                    ) {
                        Debug.log(
                            this,
                            "FilesharingContext, setting contentType: " + //NoI18n
                            CollabTextFileHandlerFactory.TEXT_MIME_TYPE
                        );

                        /*collabFileHandler.setContentType(
                                CollabTextFileHandlerFactory.TEXT_MIME_TYPE);*/
                    }
                }

                addFileHandler(collabFileHandler);
            } else {
                throw new CollabException(
                    "No CollabFileHandlerFactory matches for " + "Content Type: " + fileContentType
                );
            }
        }

        return collabFileHandler;
    }

    /**
     *
     * @param fileName
     * @param fileObject
     * @param skip Change FileHandler Type
     * @param cookie
     * @throws CollabException
     * @return
     */
    private CollabFileHandler doCreateFileHandler(
        String fileName, final FileObject fileObject, boolean skipChangeFHType, EditorCookie cookie
    ) throws CollabException {
        String contentType = fileObject.getMIMEType();

        if (fileName == null) {
            fileName = ((CollabFilesystem) getContext().getCollabFilesystem()).getPath(fileObject);
        }

        CollabFileHandler collabFileHandler = manager.getFileHandler(fileName);

        //Create an instance of CollabFileHandler
        if ((collabFileHandler == null) && (fileObject != null)) {
            Debug.log(this, "FilesharingContext, contentType: " + //NoI18n	
                contentType
            ); //NoI18n			

            String fileExt = fileObject.getExt();
            Debug.log(this, "FilesharingContext, fileExt: " + //NoI18n	
                fileExt
            ); //NoI18n

            CollabFileHandlerFactory collabFileHandlerFactory = CollabFileHandlerResolver.getDefault().resolve(
                    contentType, fileExt
                );
            Debug.log(this, "FilesharingContext, filehandler resolved to: " + //NoI18n
                collabFileHandlerFactory.getID()
            ); //NoI18n

            if (
                !skipChangeFHType && (collabFileHandlerFactory != null) &&
                    collabFileHandlerFactory instanceof CollabDefaultFileHandlerFactory && !fileExt.equals("class")
            ) {
                if (cookie != null) {
                    collabFileHandlerFactory = new CollabTextFileHandlerFactory();
                    isSpecialFile = true; //if rename fails we can do special processing

                    final String newFileName = fileObject.getNameExt();

                    try {
                        FileSystem fs = getContext().getCollabFilesystem();
                        fs.runAtomicAction(
                            new FileSystem.AtomicAction() {
                                public void run() throws IOException {
                                    try {
                                        fileLock = fileObject.lock();

                                        //fileObject.rename(fileLock, newFileName, "txt");
                                        isSpecialFile = false;
                                        Debug.log(
                                            this,
                                            "FilesharingContext, " + //NoI18n
                                            "file rename from: " + newFileName + " to: " + fileObject.getNameExt()
                                        ); //NoI18n									
                                    } catch (FileAlreadyLockedException ale) {
                                        Debug.log(
                                            this,
                                            "FilesharingContext, " + //NoI18n
                                            "file rename failed for: " + newFileName
                                        ); //NoI18n
                                        Debug.logDebugException(
                                            "FilesharingContext, " + //NoI18n
                                            "file rename failed for: " + newFileName, //NoI18n	
                                            ale, true
                                        );
                                    }
                                }
                            }
                        );
                    } catch (FileStateInvalidException ex) {
                        Debug.log(this, "FilesharingContext, " + //NoI18n
                            "file rename failed for: " + newFileName
                        ); //NoI18n
                        Debug.logDebugException(
                            "FilesharingContext, " + //NoI18n
                            "file rename failed for: " + newFileName, //NoI18n	
                            ex, true
                        );
                    } catch (IOException ex) {
                        Debug.log(this, "FilesharingContext, " + //NoI18n
                            "file rename failed for: " + newFileName
                        ); //NoI18n
                        Debug.logDebugException(
                            "FilesharingContext, " + //NoI18n
                            "file rename failed for: " + newFileName, //NoI18n	
                            ex, true
                        );
                    } catch (Exception ex) {
                        Debug.log(this, "FilesharingContext, " + //NoI18n
                            "file rename failed for: " + newFileName
                        ); //NoI18n
                        Debug.logDebugException(
                            "FilesharingContext, " + //NoI18n
                            "file rename failed for: " + newFileName, //NoI18n	
                            ex, true
                        );
                    } finally {
                        if (fileLock != null) {
                            fileLock.releaseLock();
                        }
                    }
                } else {
                    Debug.log(this, "FilesharingContext, cookie null"); //NoI18n				
                }
            }

            if (collabFileHandlerFactory != null) {
                collabFileHandler = collabFileHandlerFactory.createCollabFileHandler();

                if (fileName.startsWith(FILE_SEPERATOR)) {
                    collabFileHandler.setFileName(fileName.substring(1));
                } else {
                    collabFileHandler.setFileName(fileName);
                }

                if (isSpecialFile) {
                    collabFileHandler.setContentType(CollabFileHandler.TEXT_UNKNOWN);
                }

                collabFileHandler.setContext(getContext());
                manager.addFileHandler(collabFileHandler);
            } else {
                throw new CollabException("No CollabFileHandlerFactory matches for " + "Content Type: " + contentType);
            }
        }

        return collabFileHandler;
    }

    /**
     * removeCollabFileHandler
     *
     * @param id
     * @throws CollabException
     */
    public void removeFileHandler(String fileName) throws CollabException {
        CollabFileHandler fileHandler = manager.getFileHandler(fileName);
        fileHandler.setValid(false);

        //remove all regions if already exist
        fileHandler.clear();
        this.fileNames.remove(fileName);
        manager.removeFileHandler(fileName);
    }

    /**
     * handleSendFile
     *
     * @param sfds
     * @param owner
     * @throws CollabException
     */
    public void handleSendFile(final SendFileData[] sfds, final String messageOriginator)
    throws CollabException {
        if ((sfds == null) || (sfds.length == 0)) {
            return;
        }

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    try {
                        Debug.log(this, "SendFileHandler, in run len: " + sfds.length); //NoI18n

                        //create project node
                        final String primaryFileName = FileshareUtil.getNormalizedPath(
                                sfds[0].getFileData().getFileName()
                            );
                        String[] tokens = primaryFileName.split(FILE_SEPERATOR);
                        String owner = tokens[0];
                        String projectName = tokens[1];
                        createProjectNode(projectName, owner);

                        //process files
                        processSendFile(sfds, owner);

                        //add node
                        TimerTask t = new TimerTask() {
                                public void run() {
                                    try {
                                        addNode(primaryFileName, getFileHandlers()[0].getFileObject());
                                    } catch (Throwable th) {
                                        th.printStackTrace(Debug.out);
                                    }
                                }
                            };

                        long delay = FilesharingConstants.PERIOD * 2;

                        //form (==2 files) need time double time before addnode call
                        if (sfds.length > 1) {
                            delay = FilesharingConstants.PERIOD * 5;
                        }

                        getContext().getTimer().schedule(t, delay);
                    } catch (Throwable th) {
                        Debug.log(this, "SendFileHandler, " + //NoI18n
                            "update file failed "
                        ); //NoI18n
                        Debug.logDebugException(
                            "SendFileHandler, " + //NoI18n
                            "update file failed ", //NoI18n	
                            th, true
                        );
                    }
                }
            }
        );
    }

    private void createProjectNode(final String projectName, final String owner)
    throws CollabException {
        Debug.log(this, "SendFileHandler, SFH projectName: " + projectName);

        try {
            FileSystem fs = getContext().getCollabFilesystem();
            fs.runAtomicAction(
                new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        FileObject folder = null;
                        Debug.log(this, "SendFileHandler, SFH:: projectName: " + projectName);
                        projectNode = getContext().createProjectNode(owner, projectName);
                    }
                }
            );
        } catch (IOException iox) {
            throw new CollabException(iox);
        }
    }

    private void processSendFile(SendFileData[] sendFileData, final String owner)
    throws CollabException {
        for (int i = 0; i < sendFileData.length; i++) {
            String fullPath = FileshareUtil.getNormalizedPath(sendFileData[i].getFileData().getFileName());
            Debug.log(this, "SendFileHandler, in run: " + i); //NoI18n

            String contentType = sendFileData[i].getFileData().getContentType();
            Debug.log(this, "SendFileHandler, contentType: " + //NoI18n
                contentType
            );

            CollabFileHandler fh = createFileHandler(fullPath, contentType);
            addFileHandler(fh);

            if ((fh != null) && fh instanceof CollabFormFileHandler) {
                setType(SharedFileGroup.FORM_TYPE);
            }
        }

        for (int i = 0; i < sendFileData.length; i++) {
            String fullPath = FileshareUtil.getNormalizedPath(sendFileData[i].getFileData().getFileName());
            CollabFileHandler fh = manager.getFileHandler(fullPath);
            fh.setCurrentState(FilesharingContext.STATE_RECEIVEDSENDFILE, FilesharingTimerTask.PERIOD * 3, true, true);

            if (getType() == SharedFileGroup.FORM_TYPE) {
                fh.setRetrieveFileContentOnly(true);
                fh.setSkipUpdateAlways(true);
            }

            fh.handleSendFile(owner, sendFileData[i]);

            if (getType() == SharedFileGroup.FORM_TYPE) {
                fh.setSkipUpdateAlways(false);
            }
        }
    }

    private void addNode(final String primaryFileName, final FileObject primaryFile)
    throws CollabException {
        if (primaryFileName != null) {
            try {
                FileSystem fs = getContext().getCollabFilesystem();
                fs.runAtomicAction(
                    new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            FileObject folder = null;

                            try {
                                DataObject d = DataObject.find(primaryFile);
                                FilesystemExplorerPanel panel = getContext().getFilesystemExplorer();

                                if ((panel != null) && (panel.getRootNode() != null)) {
                                    panel.getRootNode().processPaste(projectNode, new DataObject[] { d }, null, false);
                                }
                            } catch (DataObjectNotFoundException ddnf) {
                                ddnf.printStackTrace(Debug.out);
                            }

                            //expand path
                            FilesystemExplorerPanel panel = getContext().getFilesystemExplorer();

                            if (panel != null) {
                                String filePath = primaryFileName;
                                int index = primaryFileName.lastIndexOf(FILE_SEPERATOR);

                                if (index != -1) {
                                    filePath = primaryFileName.substring(0, index);
                                }

                                panel.expandTreeNode(filePath);
                            }
                        }
                    }
                );
            } catch (IOException iox) {
                throw new CollabException(iox);
            }
        }
    }

    private FilesharingContext getContext() {
        if (context == null) {
            context = manager.getContext();
        }

        return context;
    }
}
