/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.ruby.rubyproject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class RubyProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {
    
    private RubyProject project;
    
    public RubyProjectOperations(RubyProject project) {
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
        return files;
    }
    
    public List<FileObject> getDataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();
        files.addAll(Arrays.asList(project.getSourceRoots().getRoots()));
        files.addAll(Arrays.asList(project.getTestSourceRoots().getRoots()));
        return files;
    }
    
    public void notifyDeleting() throws IOException {
        RubyActionProvider ap = project.getLookup().lookup(RubyActionProvider.class);
        
        assert ap != null;
        
        // TODO: Clean
//        Properties p = new Properties();
//        String[] targetNames = ap.getTargetNames(ActionProvider.COMMAND_CLEAN, Lookup.EMPTY, p);
//        FileObject buildXML = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
//        
//        assert targetNames != null;
//        assert targetNames.length > 0;
//        
//        ActionUtils.runTarget(buildXML, targetNames, p).waitFinished();
    }
    
    public void notifyDeleted() throws IOException {
        project.getRakeProjectHelper().notifyDeleted();
    }
    
    public void notifyCopying() {
        //nothing.
    }
    
    public void notifyCopied(Project original, File originalPath, String nueName) {
        if (original == null) {
            //do nothing for the original project.
            return ;
        }
        
        fixDistJarProperty (nueName);
        project.getReferenceHelper().fixReferences(originalPath);
        
        project.setName(nueName);
    }
    
    public void notifyMoving() throws IOException {
        if (!this.project.getUpdateHelper().requestSave()) {
            throw new IOException (NbBundle.getMessage(RubyProjectOperations.class,
                "MSG_OldProjectMetadata"));
        }
        notifyDeleting();
    }
    
    public void notifyMoved(Project original, File originalPath, String nueName) {
        if (original == null) {
            project.getRakeProjectHelper().notifyDeleted();
            return ;
        }                
        
        fixDistJarProperty (nueName);
        project.setName(nueName);        
        project.getReferenceHelper().fixReferences(originalPath);
    }
    
    private static boolean isParent(File folder, File fo) {
        if (folder.equals(fo))
            return false;
        
        while (fo != null) {
            if (fo.equals(folder))
                return true;
            
            fo = fo.getParentFile();
        }
        
        return false;
    }
    
    private void fixDistJarProperty (final String newName) {
//        ProjectManager.mutex().writeAccess(new Runnable () {
//            public void run () {
//                ProjectInformation pi = (ProjectInformation) project.getLookup().lookup(ProjectInformation.class);
//                String oldDistJar = pi == null ? null : "${dist.dir}/"+PropertyUtils.getUsablePropertyName(pi.getDisplayName())+".jar"; //NOI18N
//                EditableProperties ep = project.getUpdateHelper().getProperties (RakeProjectHelper.PROJECT_PROPERTIES_PATH);
//                String propValue = ep.getProperty("dist.jar");  //NOI18N
//                if (oldDistJar != null && oldDistJar.equals (propValue)) {
//                    ep.put ("dist.jar","${dist.dir}/"+PropertyUtils.getUsablePropertyName(newName)+".jar"); //NOI18N
//                    project.getUpdateHelper().putProperties (RakeProjectHelper.PROJECT_PROPERTIES_PATH,ep);
//                }
//            }
//        });
    }
    
}
