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
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import java.beans.PropertyChangeEvent;

import java.util.*;

import javax.swing.SwingUtilities;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.context.SendFileContext;
import org.netbeans.modules.collab.channel.filesharing.event.SendDataObject;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.SendDataObjectTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.SwingThreadTask;
import org.netbeans.modules.collab.channel.filesharing.filesystem.CollabFilesystem;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;
import org.netbeans.modules.collab.core.Debug;


/**
 * SharedFileGroup
 *
 * @author  ayub.khan@sun.com
 * @version                1.0
 */
public class SharedFileGroupManager extends Object implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private boolean isValid = true;

    /* context */
    private FilesharingContext context = null;

    /*CollabFilesystem*/
    private CollabFilesystem fs = null;

    /* all shared filehandlers */
    private HashMap allSharedFileHandlers = new HashMap();

    /* all shared fileObjects */
    private HashMap allSharedFiles = new HashMap();

    /* owner shared fileGroup map */
    private HashMap ownerSharedFileGroupMap = new HashMap();

    /* all shared fileGroup map */
    private HashMap allSharedFileGroup = new HashMap();

    /**
     *
     * @param fileGroupName
     * @param user
     * @param fileHandlers
     */
    public SharedFileGroupManager(FilesharingContext context) {
        super();
        this.context = context;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////	

    /**
     * getContext
     *
     * @return context
     */
    public FilesharingContext getContext() {
        return this.context;
    }

    /**
     * getCollabFilesystem
     *
     * @return fs
     */
    public CollabFilesystem getCollabFilesystem() {
        if (fs == null) {
            fs = (CollabFilesystem) context.getCollabFilesystem();
        }

        return this.fs;
    }

    /**
     *
     * @param fileName
     */
    public void addFileGroup(SharedFileGroup sharedFileGroup) {
        synchronized (allSharedFileGroup) {
            if (!allSharedFileGroup.containsKey(sharedFileGroup.getName())) {
                this.allSharedFileGroup.put(sharedFileGroup.getName(), sharedFileGroup);
            }
        }
    }

    /**
     * removeFileGroup
     *
     * @param id
     * @param collabFileHandler
     */
    public void removeFileGroup(String fileName) throws CollabException {
        CollabFileHandler collabFileHandler = getFileHandler(fileName);

        synchronized (allSharedFiles) {
            allSharedFiles.remove(collabFileHandler.getFileObject());
        }

        synchronized (allSharedFileHandlers) {
            allSharedFileHandlers.remove(fileName);
        }
    }

    /**
     *
     * @param fileName
     */
    public void addFileHandler(CollabFileHandler fileHandler)
    throws CollabException {
        synchronized (allSharedFileHandlers) {
            if (!allSharedFileHandlers.containsKey(fileHandler.getName())) {
                this.allSharedFileHandlers.put(fileHandler.getName(), fileHandler);
                updateFileCount(allSharedFileGroup.size());
            }
        }

        synchronized (allSharedFiles) {
            if (allSharedFiles.containsKey(fileHandler.getFileObject())) {
                allSharedFiles.put(fileHandler.getFileObject(), fileHandler);
            }
        }
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
     *
     *
     */
    public CollabFileHandler getFileHandler(String fileName) {
        synchronized (allSharedFileHandlers) {
            return (CollabFileHandler) allSharedFileHandlers.get(fileName);
        }
    }

    public CollabFileHandler getFileHandler(FileObject fileObject)
    throws CollabException {
        CollabFileHandler[] fileHandlers = getAllFileHandlers();

        for (int i = 0; i < fileHandlers.length; i++) {
            Debug.out.println(
                "compare: file: " + fileObject.getPath() + "to fh.file: " + fileHandlers[i].getFileObject().getPath()
            );

            if (fileObject.getPath().equals(fileHandlers[i].getFileObject().getPath())) {
                return fileHandlers[i];
            }
        }

        return null;
    }

    /**
     *
     * @param fileName
     * @throws CollabException
     * @return
     */
    public CollabFileHandler getFileHandler(javax.swing.text.Document document) {
        return getFileHandler(((String) document.getProperty("COLLAB_FILEHANDLER_FILE_NAME")));
    }

    /**
     * removeCollabFileHandler
     *
     * @param id
     * @param collabFileHandler
     */
    public void removeFileHandler(String fileName) throws CollabException {
        CollabFileHandler collabFileHandler = getFileHandler(fileName);

        synchronized (allSharedFiles) {
            allSharedFiles.remove(collabFileHandler.getFileObject());
        }

        synchronized (allSharedFileHandlers) {
            allSharedFileHandlers.remove(fileName);
            updateFileCount(allSharedFileGroup.size());
        }
    }

    /**
     *
     * @return
     */
    public CollabFileHandler[] getAllFileHandlers() {
        synchronized (allSharedFileHandlers) {
            return (CollabFileHandler[]) allSharedFileHandlers.values().toArray(new CollabFileHandler[0]);
        }
    }

    /**
     *
     * @return
     */
    public SharedFileGroup[] getAllSharedFileGroup() {
        synchronized (allSharedFileGroup) {
            return (SharedFileGroup[]) allSharedFileGroup.values().toArray(new SharedFileGroup[0]);
        }
    }

    /**
     *
     * @param fileGroupName
     * @return
     */
    public SharedFileGroup getSharedFileGroup(String fileGroupName) {
        synchronized (allSharedFileGroup) {
            return (SharedFileGroup) allSharedFileGroup.get(fileGroupName);
        }
    }

    /**
     *
     * @param fileGroupName
     * @return
     */
    public SharedFileGroup getSharedFileGroup(FileObject fileObject)
    throws CollabException {
        CollabFileHandler[] fileHandlers = getAllFileHandlers();

        for (int i = 0; i < fileHandlers.length; i++) {
            if (fileObject == fileHandlers[i].getFileObject()) {
                return getSharedFileGroup(fileHandlers[i].getFileGroupName());
            }
        }

        return null;
    }

    /**
     *
     * @param id
     * @param sharedFileGroup
     * @throws CollabException
     */
    public void addToAllSharedFileGroup(String id, SharedFileGroup sharedFileGroup) {
        synchronized (allSharedFileGroup) {
            if (!allSharedFileGroup.containsKey(id)) {
                allSharedFileGroup.put(id, sharedFileGroup);
                updateFileCount(allSharedFileGroup.size());
            }
        }
    }

    private void updateFileCount(int fileCount) {
        Debug.out.println("SFGM updateFileCount File count: " + fileCount);

        PropertyChangeEvent event = new PropertyChangeEvent(
                this, FILE_COUNT_CHANGED, new Integer(fileCount), new Integer(fileCount)
            );

        if (getContext().getFilesystemExplorer() != null) {
            getContext().getFilesystemExplorer().propertyChange(event);
        }
    }

    /**
     * removeFromAllSharedFileGroup
     *
     * @param sharedFileGroup
     * @throws CollabException
     */
    public void removeFromAllSharedFileGroup(SharedFileGroup sharedFileGroup) {
        String fileGroupName = sharedFileGroup.getName();

        synchronized (allSharedFileGroup) {
            if (allSharedFileGroup.containsKey(fileGroupName)) {
                allSharedFileGroup.remove(fileGroupName);
                updateFileCount(allSharedFileGroup.size());
            }
        }
    }

    /**
     *
     * @param fileGroupName
     * @return
     */
    public boolean isSharedFileGroupExist(String fileGroupName) {
        synchronized (allSharedFileGroup) {
            return allSharedFileGroup.containsKey(fileGroupName);
        }
    }

    /**
     *
     * @return
     */
    public SharedFileGroup[] getOwnerSharedFileGroup() {
        synchronized (ownerSharedFileGroupMap) {
            return (SharedFileGroup[]) ownerSharedFileGroupMap.values().toArray(new SharedFileGroup[0]);
        }
    }

    /**
     *
     * @param fileGroupName
     * @return
     */
    public SharedFileGroup getOwnerSharedFileGroup(String fileGroupName) {
        synchronized (ownerSharedFileGroupMap) {
            return (SharedFileGroup) ownerSharedFileGroupMap.get(fileGroupName);
        }
    }

    /**
     *
     * @param name
     * @param object
     */
    public void addToOwnerSharedFile(String fileGroupName, SharedFileGroup object) {
        synchronized (ownerSharedFileGroupMap) {
            ownerSharedFileGroupMap.put(fileGroupName, object);
        }

        context.addToUserSharedFileGroupNames(context.getLoginUser(), fileGroupName);
    }

    /**
     *
     * @param fileGroupName
     * @param file
     * @throws CollabException
     */
    public void addToOwnerSharedFile(String fileGroupName, String projectName, CollabFileHandler fileHandler)
    throws CollabException {
        if (!isSharedFileGroupExist(fileGroupName)) {
            SharedFileGroup sharedFileGroup = SharedFileGroupFactory.getDefault().createSharedFileGroup(
                    fileGroupName, context.getLoginUser(), projectName, this
                );
            addFileGroup(sharedFileGroup);
            sharedFileGroup.addFileHandler(fileHandler);
            addToOwnerSharedFile(fileGroupName, sharedFileGroup);
            addToAllSharedFileGroup(fileGroupName, sharedFileGroup);
        } else {
            SharedFileGroup sharedFileGroup = getOwnerSharedFileGroup(fileGroupName);

            if (sharedFileGroup != null) {
                if (!sharedFileGroup.contains(fileHandler.getName())) {
                    sharedFileGroup.addFileHandler(fileHandler);
                }
            }
        }
    }

    /**
     * removeFromOwnerSharedFileGroup
     *
     * @param sharedFileGroup
     * @throws CollabException
     */
    public void removeFromOwnerSharedFileGroup(SharedFileGroup sharedFileGroup)
    throws CollabException {
        String fileGroupName = sharedFileGroup.getName();

        synchronized (ownerSharedFileGroupMap) {
            if (ownerSharedFileGroupMap.containsKey(fileGroupName)) {
                ownerSharedFileGroupMap.remove(fileGroupName);
            }
        }
    }

    public boolean isShared(FileObject fileObject) throws CollabException {
        CollabFileHandler[] fileHandlers = getAllFileHandlers();

        for (int i = 0; i < fileHandlers.length; i++) {
            if (fileObject == fileHandlers[i].getFileObject()) {
                return true;
            }
        }

        return false;
    }

    public void sendDataObject(String loginUser, String projectName, DataFolder leafFldr, final DataObject dataObject) {
        FileObject[] fileObjects = (FileObject[]) dataObject.files().toArray(new FileObject[0]);
        CollabFileHandler[] fileHandlers = new CollabFileHandler[fileObjects.length];
        String[] fileNames = new String[fileObjects.length];
        String fileGroupName = getCollabFilesystem().getPath(leafFldr, dataObject) + "_" +
            dataObject.getPrimaryFile().getExt();
        fileGroupName = FileshareUtil.getNormalizedPath(fileGroupName);

        for (int j = 0; j < fileObjects.length; j++) {
            fileNames[j] = FileshareUtil.getNormalizedPath(getCollabFilesystem().getPath(leafFldr, fileObjects[j]));
            Debug.log("ProjectsRootNode", "SFGM, files : " + fileNames[j]);
        }

        final LazySharedFileGroup lazySharedFileGroup = SharedFileGroupFactory.createLazySharedFileGroup(
                fileGroupName, loginUser, projectName, getContext().getSharedFileGroupManager(), fileNames, fileObjects
            );
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    try {
                        EditorCookie cookie = FileshareUtil.getEditorCookie(dataObject);
                        lazySharedFileGroup.setEditorCookie(cookie);
                    } catch (Throwable th) {
                        Debug.log(
                            "CollabFileHandlerSupport",
                            "CollabFileHandlerSupport, " + //NoI18n
                            "wait to getCollabFileHandler failed"
                        ); //NoI18n
                        Debug.logDebugException(
                            "CollabFileHandlerSupport, " + //NoI18n
                            "wait to getCollabFileHandler failed", //NoI18n	
                            th, true
                        );
                    }
                }
            }
        );

        TimerTask sendDataObjectTimerTask = new SwingThreadTask(
                new SendDataObjectTimerTask(
                    getContext().getChannelEventNotifier(),
                    new SendDataObject(new SendFileContext(SendDataObject.getEventID(), lazySharedFileGroup)),
                    getContext()
                )
            );
        getContext().addTimerTask(SEND_SENDFILE_TIMER_TASK, fileGroupName, sendDataObjectTimerTask);
        getContext().schedule(sendDataObjectTimerTask, FilesharingTimerTask.PERIOD * 2);
    }

    /**
     *        clear
     *
     */
    public void clear() {
        synchronized (ownerSharedFileGroupMap) {
            ownerSharedFileGroupMap.clear();
        }

        synchronized (allSharedFileGroup) {
            allSharedFileGroup.clear();
        }

        synchronized (allSharedFileHandlers) {
            allSharedFileHandlers.clear();
        }
    }

    /**
     *        print
     *
     */
    public void print() {
        if (!Debug.isAllowed("FilesharingContext")) {
            return;
        }

        synchronized (ownerSharedFileGroupMap) {
            Debug.log(
                "FilesharingContext", "ownerSharedFileGroupMap: " + //NoI18n	
                ownerSharedFileGroupMap.keySet().toString()
            ); //NoI18n
        }

        synchronized (allSharedFileGroup) {
            Debug.log(
                "FilesharingContext", "allSharedFileGroupMap: " + //NoI18n	
                allSharedFileGroup.keySet().toString()
            ); //NoI18n
        }

        synchronized (allSharedFileHandlers) {
            Debug.log(
                "FilesharingContext", "collabFileHandlerMap: " + //NoI18n	
                allSharedFileHandlers.keySet().toString()
            ); //NoI18n
        }
    }
}
