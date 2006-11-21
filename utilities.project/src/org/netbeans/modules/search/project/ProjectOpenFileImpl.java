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

package org.netbeans.modules.search.project;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.openfile.OpenFileImpl;
import org.openide.filesystems.FileObject;

/**
 * Opens projects.
 *
 * @author Jaroslav Tulach, Jesse Glick
 */
public class ProjectOpenFileImpl implements OpenFileImpl {

    public boolean open(FileObject fileObject, int line) {
        if (fileObject.isFolder()) {
            try {
                Project p = ProjectManager.getDefault().findProject(fileObject);
                if (p != null) {
                    OpenProjects.getDefault().open(new Project[] {p}, false);
                    OpenProjects.getDefault().setMainProject(p);
                    return true;
                }
            } catch (IOException ex) {
                Logger.getLogger(ProjectOpenFileImpl.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        return false;
    }

}
