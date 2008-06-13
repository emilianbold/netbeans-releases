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
package org.netbeans.modules.php.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 */
public class PhpProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation,
        MoveOperationImplementation {

    private PhpProject project;

    public PhpProjectOperations(PhpProject project) {
        assert project != null;
        this.project = project;
    }

    public void notifyDeleted() throws IOException {
        project.getHelper().notifyDeleted();
    }

    public void notifyDeleting() throws IOException {
    }


    public void notifyCopied(Project originalProject, File file, String newName) throws IOException {
        if (originalProject == null) {
            // do nothing for the original project.
            return;
        }
        project.setName(newName);
    }

    public void notifyCopying() throws IOException {
    }

    public void notifyMoved(Project originalProject, File file, String newName) throws IOException {
        if (originalProject == null) {
            project.getHelper().notifyDeleted();
            return;
        }
        project.setName(newName);
    }

    public void notifyMoving() throws IOException {
    }

    public List<FileObject> getDataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();

        // It seems that we add all sources, including external
        FileObject[] sources = Utils.getSourceObjects(project);
        files.addAll(Arrays.asList(sources));

        // TODO check that all files are added...
        /* SRC directory = project directory in our case.
            Therefore project dir wasn't removed
            - as non empty src dir because of 'nbbroject'
            - as non empty project dir bercause of source files.
            The folowing should help: */
        //copied from org.netbeans.modules.cnd.makeproject.MakeProjectOperations
        FileObject[] projectContent = project.getProjectDirectory().getChildren();
        List metadataFiles = getMetadataFiles();
        for (int i = 0; i < projectContent.length; i++) {
            if (metadataFiles.indexOf(projectContent[i]) < 0) {
                files.add(projectContent[i]);
            }
        }
        if (files.size() == 0) {
            // FIXUP: Don't return empty list. If the list is empty, the "Also Delete Sources" checkbox in the dialog is disabled and the project dir cannot be deleted.
            files.add(project.getProjectDirectory());
        }
        return files;
    }

    public List<FileObject> getMetadataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();

        // add nbproject dir
        FileObject projectXml = project.getHelper().resolveFileObject(AntProjectHelper.PROJECT_XML_PATH);
        if (projectXml != null) {
            FileObject nbProject = projectXml.getParent();
            if (nbProject != null) {
                files.add(nbProject);
            }
        }

        // add project dir ?
        files.add(project.getProjectDirectory());
        return files;
    }
}
