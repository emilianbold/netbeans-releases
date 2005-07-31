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

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;


/**
 * SharedFileGroup
 *
 * @author  ayub.khan@sun.com
 * @version                1.0
 */
public class LazySharedFileGroup extends SharedFileGroup {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private String[] fileNames = null;
    private FileObject[] files = null;

    /**
     *
     * @param fileGroupName
     * @param user
     * @param manager
     * @param fileNames
     * @param files
     */
    public LazySharedFileGroup(
        String fileGroupName, String user, String projectName, SharedFileGroupManager manager, String[] fileNames,
        FileObject[] files
    ) {
        super(fileGroupName, user, projectName, manager);
        this.fileNames = fileNames;
        this.files = files;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////                       

    /**
     *
     * @return
     */
    public String[] getFileNames() {
        return fileNames;
    }

    /**
     *
     * @return
     */
    public FileObject[] getFiles() {
        return files;
    }

    /**
     *
     * @param searchFileName
     * @return
     */
    public boolean contains(String searchFileName) {
        return false;
    }

    /**
     *
     * @param fileName
     */
    public void addFileHandler(CollabFileHandler fileHandler)
    throws CollabException {
        //do nothing
    }

    /**
     *
     * @return fileHandlers
     */
    public CollabFileHandler[] getFileHandlers() { //return empty list

        return (CollabFileHandler[]) new ArrayList().toArray(new CollabFileHandler[0]);
    }

    /**
     * createFileHandler
     *
     * @param        fileGroupName
     * @param        fileObject
     * @return        collabFileHandler
     */
    public CollabFileHandler createFileHandler(
        FilesharingContext context, String fileOwner, String fileName, String contentType
    ) throws CollabException {
        return null;
    }

    /**
     * createFileHandler
     *
     * @param        fileGroupName
     * @param        fileObject
     * @return        collabFileHandler
     */
    public CollabFileHandler createFileHandler(
        final FilesharingContext context, String fileOwner, final String fileName, final FileObject fileObject
    ) throws CollabException {
        return null;
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
        return null;
    }

    /**
     *
     * @param fileName
     * @param contentType
     * @throws CollabException
     * @return
     */
    public CollabFileHandler createFileHandler(String fileName, FileObject fileObject)
    throws CollabException {
        return null;
    }

    /**
     *
     * @param fileName
     * @param contentType
     * @throws CollabException
     * @return
     */
    public CollabFileHandler getFileHandler(String fileName, final FileObject fileObject, boolean skipChangeFHType)
    throws CollabException {
        return null;
    }

    /**
     * removeCollabFileHandler
     *
     * @param id
     * @throws CollabException
     */
    public void removeFileHandler(String fileName) throws CollabException {
        //do nothing		
    }
}
