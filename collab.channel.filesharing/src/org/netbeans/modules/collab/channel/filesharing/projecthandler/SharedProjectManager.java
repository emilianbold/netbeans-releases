/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.projecthandler;

import com.sun.collablet.CollabException;

import org.openide.filesystems.FileObject;

import java.util.*;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.filesystem.CollabFilesystem;
import org.netbeans.modules.collab.core.Debug;


/**
 * SharedProjectManager
 *
 * @author  ayub.khan@sun.com
 * @version                1.0
 */
public class SharedProjectManager extends Object implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private boolean isValid = true;

    /* context */
    private FilesharingContext context = null;

    /*CollabFilesystem*/
    private CollabFilesystem fs = null;

    /* all shared projects in this conversation */
    private HashMap sharedProjects = new HashMap();

    /**
     *
     * @param context
     */
    public SharedProjectManager(FilesharingContext context) {
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

    public void addSharedProject(String userID, String projectName, SharedProject sharedProject) {
        synchronized (sharedProjects) {
            sharedProjects.put(userID + "_" + projectName, sharedProject);
        }
    }

    /**
     * isShared
     *
     * @param projectName
     */
    public boolean isShared(String userID, String projectName) {
        synchronized (sharedProjects) {
            return sharedProjects.containsKey(userID + "_" + projectName);
        }
    }

    /**
     * getProject
     *
     * @param project
     */
    public Project getProject(FileObject fo) {
        return FileOwnerQuery.getOwner(fo);
    }

    /**
     * isSharedOriginal
     *
     * @param project
     */
    public boolean isSharedOriginal(Project project) {
        Debug.log(
            "ProjectsRootNode",
            (("ProjectsRootNode, check project: " + project) != null) ? ProjectUtils.getInformation(project).getName()
                                                                      : "null"
        );

        SharedProject[] sharedProjects = getAllSharedProjects();

        for (int i = 0; i < sharedProjects.length; i++) {
            Debug.log("ProjectsRootNode", "ProjectsRootNode, sharedProjects: " + sharedProjects.length);

            Project originalProject = sharedProjects[i].getOriginalProject();

            //check if original project already shared
            if (project == originalProject) {
                return true;
            }
        }

        return false;
    }

    public SharedProject getSharedProject(String userID, String projectName) {
        synchronized (sharedProjects) {
            return (SharedProject) sharedProjects.get(userID + "_" + projectName);
        }
    }

    public SharedProject[] getOwnerSharedProjects(String userID) {
        if ((userID == null) || userID.equals("")) {
            return null;
        }

        List spList = new ArrayList();
        SharedProject[] sps = getAllSharedProjects();

        for (int i = 0; i < sps.length; i++) {
            if (userID.equals(sps[i].getProjectOwner())) {
                spList.add(sps[i]);
            }
        }

        return (SharedProject[]) spList.toArray(new SharedProject[0]);
    }

    public SharedProject[] getAllSharedProjects() {
        synchronized (sharedProjects) {
            return (SharedProject[]) sharedProjects.values().toArray(new SharedProject[0]);
        }
    }

    public void removeSharedProject(String userID, String projectName) {
        synchronized (sharedProjects) {
            sharedProjects.remove(userID + "_" + projectName);
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
     *        print
     *
     */
    public void clear() {
        SharedProject[] sps = getAllSharedProjects();

        for (int i = 0; i < sps.length; i++) {
            try {
                sps[i].finalize();
            } catch (Throwable th) {
                th.printStackTrace(Debug.out);
            }
        }

        synchronized (sharedProjects) {
            sharedProjects.clear();
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

        synchronized (sharedProjects) {
            Debug.log("FilesharingContext", "sharedProjects: " + //NoI18n	
                sharedProjects.keySet().toString()
            ); //NoI18n
        }
    }
}
