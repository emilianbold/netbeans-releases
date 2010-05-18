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
