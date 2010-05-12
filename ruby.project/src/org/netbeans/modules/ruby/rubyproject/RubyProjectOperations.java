/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;
import org.openide.util.NbBundle;

public class RubyProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {
    
    private final RubyProject project;
    /**
     * Holds the original private properties of a project that is being moved/copied. 
     * 
     * Needed since unlike in other project types we copy the contents of the orig private.properties
     * to the new private.properties - this needs to be done here as the project infrastructure 
     * does not copy private.properties. See #172794 for more.
     */
    private static EditableProperties origPrivateProperties;
    
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
        addFile(projectDirectory, "Capfile", files); // NOI18N
        addFile(projectDirectory, "README", files); // NOI18N
        addFile(projectDirectory, "LICENSE", files); // NOI18N
        return files;
    }
    
    public List<FileObject> getDataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();
        files.addAll(Arrays.asList(project.getSourceRoots().getRoots()));
        files.addAll(Arrays.asList(project.getTestSourceRoots().getRoots()));
        addFile(project.getProjectDirectory(), "Rakefile", files); // NOI18N
        return files;
    }
    
    public void notifyDeleting() throws IOException {
        // nothing needed in the meantime
    }
    
    public void notifyDeleted() throws IOException {
        project.getRakeProjectHelper().notifyDeleted();
    }
    
    public void notifyCopying() {
        origPrivateProperties = project.getRakeProjectHelper().getProperties(RakeProjectHelper.PRIVATE_PROPERTIES_PATH);
    }
    
    public void notifyCopied(Project original, File originalPath, String nueName) {
        if (original == null) {
            //do nothing for the original project.
            return ;
        }
        
        project.getReferenceHelper().fixReferences(originalPath);
        project.setName(nueName);
        copyPrivateProps();
    }
    
    public void notifyMoving() throws IOException {
        origPrivateProperties = project.getRakeProjectHelper().getProperties(RakeProjectHelper.PRIVATE_PROPERTIES_PATH);
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
        
        project.setName(nueName);        
        project.getReferenceHelper().fixReferences(originalPath);
        copyPrivateProps();
    }

    private void copyPrivateProps() {
        project.getReferenceHelper().copyToPrivateProperties(origPrivateProperties);
        origPrivateProperties = null;
    }
}
