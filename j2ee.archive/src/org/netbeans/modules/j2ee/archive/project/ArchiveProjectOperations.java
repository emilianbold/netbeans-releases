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

package org.netbeans.modules.j2ee.archive.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ludovic Champenois
 */
public class ArchiveProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {
    
    private ArchiveProject project;
    
    public ArchiveProjectOperations(ArchiveProject project) {
        this.project = project;
    }
    
    private static void addFile(FileObject projectDirectory, String fileName, List<FileObject> result) {
        FileObject file = projectDirectory.getFileObject(fileName);
        
        if (file != null) {
            result.add(file);
        }
    }
    
    public List<FileObject> getMetadataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        List<FileObject> files = new ArrayList<FileObject>();
        
        addFile(projectDirectory, "nbproject", files); // NOI18N
        addFile(projectDirectory, "build.xml", files); // NOI18N
        addFile(projectDirectory, "dist", files); // NOI18N
        
        return files;
    }
    
    private static final String TMPPROJ_LIT = "tmpproj";            // NOI18N
    private static final String SUBARCHIVES_LIT = "subarchives";    // NOI18N
    
    public List<FileObject> getDataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        List/*<FileObject>*/ files = new ArrayList();
        addFile(projectDirectory, TMPPROJ_LIT, files);
        addFile(projectDirectory, SUBARCHIVES_LIT, files);
        addFile(projectDirectory, "setup",files); // NOI18N
        
        return files;
    }
    
    public void notifyDeleting() throws IOException {
        
    }
    
    public void notifyDeleted() throws IOException {
        if (project.getProjectDirectory().getFileObject(TMPPROJ_LIT) == null) {
            project.getProjectDirectory().delete();
        }
        project.getAntProjectHelper().notifyDeleted();
    }
    
    public void notifyCopying() {
        
    }
    
    public void notifyCopied(final Project original, final File originalPath, final String nueName) {
        if (original == null) {
            //do nothing for the original project.
            return ;
        }
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    copyFilesToNewLocation(nueName, original);
                } catch (IOException ex) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "" + ex);
                }
            }
            
        });
    }
    
    private void copyFilesToNewLocation(final String nueName, final Project original) throws IOException {
        project.setName(nueName);
        
        AntProjectHelper helper = project.getAntProjectHelper();
        EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
        
        // copy the tmpproject to the new location
        if (!original.getProjectDirectory().equals(project.getProjectDirectory())) {
            doCopy(original.getProjectDirectory().getFileObject(TMPPROJ_LIT),project.getProjectDirectory());
            
            FileObject subdir = original.getProjectDirectory().getFileObject(SUBARCHIVES_LIT);
            if (null != subdir) {
                FileObject dest = FileUtil.createFolder(project.getProjectDirectory(),SUBARCHIVES_LIT);
                Enumeration subarchives = subdir.getFolders(false);
                while (subarchives.hasMoreElements()) {
                    FileObject t = (FileObject) subarchives.nextElement();
                    doCopy(t,dest);
                }
            }
        }
    }
    
    public void notifyMoving() throws IOException {
        
    }
    
    public void notifyMoved(final Project original, final File originalPath, final String newName) {
        if (original == null) {
            project.getAntProjectHelper().notifyDeleted();
            return ;
        }
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    copyFilesToNewLocation(newName, original);
                    FileObject subdir = original.getProjectDirectory().getFileObject(SUBARCHIVES_LIT);
                    if (!original.getProjectDirectory().equals(project.getProjectDirectory())) {                        
                        try {
                            if (null != subdir) {
                                subdir.delete();
                            }
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                        FileObject target = original.getProjectDirectory().getFileObject(TMPPROJ_LIT);
                        if (null != target) {
                            try {
                                target.delete();
                            } catch (IOException ex) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            }
                        }
                        target = original.getProjectDirectory();
                        if (null != target) {
                            try {
                                target.delete();
                            } catch (IOException ex) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                            }
                        }
                    }
                } catch (IOException ex) {
                    // this is actually pretty bad...
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
                
            }
        });
    }
    
    private static void doCopy(FileObject from, FileObject toParent) throws IOException {
        if (null != from) {
            if (from.isFolder()) {
                //FileObject copy = toParent.getF
                FileObject copy = FileUtil.createFolder(toParent,from.getNameExt());
                FileObject[] kids = from.getChildren();
                for (int i = 0; i < kids.length; i++) {
                    doCopy(kids[i], copy);
                }
            } else {
                assert from.isData();
                FileObject target = toParent.getFileObject(from.getName(),from.getExt());
                if (null == target) {
                    FileUtil.copyFile(from, toParent, from.getName(), from.getExt());
                }
            }
        }
    }
    
}
