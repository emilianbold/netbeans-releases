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
package org.netbeans.modules.collab.channel.filesharing.projecthandler;

import com.sun.collablet.CollabException;

import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;

import java.io.File;
import java.io.IOException;

import javax.swing.Action;

import org.netbeans.api.project.Project;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.FilesharingEventNotifierFactory;
import org.netbeans.modules.collab.channel.filesharing.FilesharingEventProcessorFactory;
import org.netbeans.modules.collab.channel.filesharing.event.DeleteFileEvent;
import org.netbeans.modules.collab.channel.filesharing.event.RenameFileEvent;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabEvent;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventNotifier;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventProcessor;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;
import org.netbeans.modules.collab.core.Debug;


/**
 * SharedProject
 *
 * @author  ayub.khan@sun.com
 * @version                1.0
 */
public class SharedProject extends Object implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////
    public final static int UNKNOWN_TYPE = -1;
    public final static int ANT_TYPE = 1;

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private String projectID;
    private String projectName;
    private int projectType = UNKNOWN_TYPE;
    private String projectOwner;
    private SharedProjectManager manager = null;
    private Project originalProject;
    private Action[] pNodeactions;
    private boolean isValid = true;
    private EventNotifier fileChangeNotifier;
    private FileSystem fs;
    private FileChangeAdapter fcl;

    /**
     *
     * @param projectName
     * @param user
     * @param manager
     */
    public SharedProject(
        String projectName, String projectOwner, Project originalProject, SharedProjectManager manager
    ) {
        super();
        this.projectID = projectOwner + "_" + projectName;
        this.projectName = projectName;
        this.projectOwner = projectOwner;
        this.originalProject = originalProject;
        this.manager = manager;
        addFilesystemListener();
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////                       

    /**
     *
     * @return
     */
    public String getID() {
        return projectID;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return projectName;
    }

    /**
     *
     * @return
     */
    public int getType() {
        return this.projectType;
    }

    /**
     *
     * @return
     */
    public String getProjectOwner() {
        return this.projectOwner;
    }

    /**
     *
     * @param user
     */
    public void setProjectOwner(String newUser) {
        this.projectOwner = newUser;
    }

    /**
     *
     * @return
     */
    public Project getOriginalProject() {
        return originalProject;
    }

    /**
     *
     * @return
     */
    public void setOriginalProject(Project originalProject) {
        this.originalProject = originalProject;
    }

    /**
     *
     * @return
     */
    public Action[] getProjectActions() {
        return this.pNodeactions;
    }

    /**
     *
     * @return
     */
    public void setProjectActions(Action[] pNodeactions) {
        this.pNodeactions = pNodeactions;
    }

    private FilesharingContext getContext() {
        return (manager != null) ? manager.getContext() : null;
    }

    private void addFilesystemListener() {
        if (getOriginalProject() == null) {
            return;
        }

        try {
            //create a notifer first
            EventProcessor ep = FilesharingEventProcessorFactory.getDefault().createEventProcessor(
                    getContext().getProcessorConfig(), getContext()
                );
            fileChangeNotifier = FilesharingEventNotifierFactory.getDefault().createEventNotifier(
                    getContext().getNotifierConfig(), ep
                );

            try {
                fs = getOriginalProject().getProjectDirectory().getFileSystem();
            } catch (Exception e) {
                Debug.out.println("SharedProject getFileSystem() failed" + e);
                e.printStackTrace(Debug.out);
            }

            if (fs == null) {
                return;
            }

            fcl = new SharedProjectFileListener();
            fs.addFileChangeListener(fcl);
        } catch (CollabException e) {
            e.printStackTrace(Debug.out);
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();

        if ((fs != null) && (fcl != null)) {
            fs.removeFileChangeListener(fcl);
        }
    }

    /**
     * setValid
     *
     * @param        status
     */
    public void setValid(boolean valid) {
        this.isValid = valid;
    }

    /**
     * getValid
     *
     * @return        status                                        if false handler is invalid
     */
    public boolean isValid() {
        return this.isValid;
    }

    public class SharedProjectFileListener extends FileChangeAdapter {
        public void fileDeleted(FileEvent fe) {
            FileObject fObj = fe.getFile();
            Debug.out.println("File Delete: " + fObj.getNameExt());

            try {
                if (!getContext().getSharedFileGroupManager().isShared(fObj)) {
                    return;
                }

                CollabFileHandler fh = getContext().getSharedFileGroupManager().getFileHandler(fObj);
                FileshareUtil.deleteFileLink(fh.getName(), getContext());

                EventContext evContext = new EventContext(DeleteFileEvent.getEventID(), fh);
                CollabEvent ce = new DeleteFileEvent(evContext);
                fileChangeNotifier.notify(ce);
            } catch (Exception e) {
                Debug.out.println("Exception sendDelete: " + e);
                e.printStackTrace(Debug.out);
            }
        }

        public void fileRenamed(FileRenameEvent fre) {
            FileObject fObj = fre.getFile();
            String origName = fre.getName();
            String origExt = fre.getExt();
            Debug.out.println("File Rename from: " + origName + "." + origExt);
            Debug.out.println("File Rename to: " + fObj.getNameExt());

            try {
                Debug.out.println("check isShared: " + fObj.getNameExt());

                if (!getContext().getSharedFileGroupManager().isShared(fObj)) {
                    return;
                }

                CollabFileHandler fh = getContext().getSharedFileGroupManager().getFileHandler(fObj);
                String fileName = fh.getName();
                Debug.out.println("shared: " + fileName);

                String folder = fileName.substring(0, fileName.lastIndexOf(FILE_SEPERATOR));
                File oldflnk = null;
                File newflnk = null;
                int lastIndex = fileName.lastIndexOf('.');

                if (lastIndex != -1) {
                    oldflnk = manager.getCollabFilesystem().getAbsoluteFile(
                            fileName.substring(0, lastIndex) + ".shadow"
                        ); //NoI18n								
                    newflnk = manager.getCollabFilesystem().getAbsoluteFile(
                            folder + FILE_SEPERATOR + fObj.getName() + ".shadow"
                        ); //NoI18n
                } else {
                    oldflnk = manager.getCollabFilesystem().getAbsoluteFile(fileName + ".shadow"); //NoI18n
                    newflnk = manager.getCollabFilesystem().getAbsoluteFile(
                            folder + FILE_SEPERATOR + fObj.getName() + ".shadow"
                        ); //NoI18n
                }

                final File oldfile = oldflnk;
                final File newfile = newflnk;

                try {
                    manager.getCollabFilesystem().runAtomicAction(
                        new FileSystem.AtomicAction() {
                            public void run() throws IOException {
                                Debug.out.println("File Link Rename: " + oldfile.getPath());
                                oldfile.renameTo(newfile);
                                manager.getCollabFilesystem().refresh(false);
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

                //fh.setFileName(folder+FILE_SEPERATOR+fObj.getNameExt());
                EventContext evContext = new EventContext(RenameFileEvent.getEventID(), fh);
                CollabEvent ce = new RenameFileEvent(evContext);
                fileChangeNotifier.notify(ce);
            } catch (Exception e) {
                Debug.out.println("Exception sendRename: " + e);
                e.printStackTrace(Debug.out);
            }
        }
    }
}
