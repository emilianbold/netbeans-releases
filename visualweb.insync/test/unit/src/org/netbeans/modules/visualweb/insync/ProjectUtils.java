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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.insync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jdeva
 */
public class ProjectUtils {
    public static Project openProject(File workDir, String zipPath, String projectName) throws IOException {
        assert(zipPath != null);
        File archiveFile = new File(zipPath);

        FileObject destFileObj = FileUtil.toFileObject(workDir);
        unZipFile(archiveFile, destFileObj);
        
        //Unzip results in adding the valid project directory as no such project because of
        //FileBuiltQuery implementation in insync, therefore we cannot open multiple zipped projects
        //The following statement should clear the cache but is not public
        //ProjectManager.getDefault().reset();
        
        assert(destFileObj.isValid() == true);
        FileObject testApp = destFileObj.getFileObject(projectName);

        assert(testApp != null);
        System.out.println("Children of " + projectName + ":" + Arrays.toString(testApp.getChildren()));
        //assertTrue( ProjectManager.getDefault().isProject(testApp));
        Project project = ProjectManager.getDefault().findProject(testApp);
        assert(project != null);
        OpenProjects.getDefault().open(new Project[]{project}, false);
        return project;
    }    
    
    public static void destroyProject(Project project) throws IOException {
        OpenProjects.getDefault().close(new Project[]{project});
        project.getProjectDirectory().delete();
        //Need to clear the cache, otherwise subsequent call to findProject will fail
        ProjectManager.getDefault().clearNonProjectCache();
    }
    
    private static void unZipFile(File archiveFile, FileObject destDir) throws IOException {
        FileInputStream fis = new FileInputStream(archiveFile);
        try {
            ZipInputStream str = new ZipInputStream(fis);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(destDir, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(destDir, entry.getName());
                    FileLock lock = fo.lock();
                    try {
                        OutputStream out = fo.getOutputStream(lock);
                        try {
                            FileUtil.copy(str, out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            }
        } finally {
            fis.close();
        }
    }    
}
