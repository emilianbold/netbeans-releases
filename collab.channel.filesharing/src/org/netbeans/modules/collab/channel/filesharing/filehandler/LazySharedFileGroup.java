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
