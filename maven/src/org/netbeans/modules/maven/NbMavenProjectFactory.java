/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */


package org.netbeans.modules.maven;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * factory of maven projects
 * @author  Milos Kleint
 */
public class NbMavenProjectFactory implements ProjectFactory {
    
    /** Creates a new instance of NbMavenProjectFactory */
    public NbMavenProjectFactory() {
    }
    
    public boolean isProject(FileObject fileObject)
    {
        File projectDir = FileUtil.toFile(fileObject);
        if (projectDir == null) {
            return false;
        }
        
        File project = new File(projectDir, "pom.xml"); // NOI18N
        if (project.isFile() && 
            "archetype-resources".equalsIgnoreCase(projectDir.getName()) && //NOI18N
            "resources".equalsIgnoreCase(projectDir.getParentFile().getName())) { //NOI18N
            //this is an archetype resource, happily ignore..
            return false;
        }
        return project.isFile() &&  !"nbproject".equalsIgnoreCase(projectDir.getName()); //NOI18N
    }
    
    public Project loadProject(FileObject fileObject, ProjectState projectState) throws IOException
    {
        if (FileUtil.toFile(fileObject) == null) {
            return null;
        }
        if ("nbproject".equalsIgnoreCase(fileObject.getName())) { //NOI18N
            return null;
        }
        FileObject projectFile = fileObject.getFileObject("pom.xml"); //NOI18N
        if (projectFile == null || !projectFile.isData()) {
            return null;
            
        }
        File projectDiskFile = FileUtil.normalizeFile(FileUtil.toFile(projectFile));
        if (projectDiskFile == null)  {
            return null;
        }
        if (projectDiskFile.isFile() && 
            "archetype-resources".equalsIgnoreCase(fileObject.getName()) && //NOI18N
            "resources".equalsIgnoreCase(fileObject.getParent().getName())) { //NOI18N
            //this is an archetype resource, happily ignore..
            return null;
        }
        try {
            NbMavenProjectImpl proj =  new NbMavenProjectImpl(fileObject, projectFile, projectDiskFile, projectState);
            return proj;
        } catch (Exception exc) {
            ErrorManager.getDefault().getInstance(NbMavenProjectFactory.class.getName()).notify(ErrorManager.INFORMATIONAL, exc);
            return null;
        }
    }
    
    public void saveProject(Project project) throws IOException {
        // what to do here??
    }
    
    
}
