/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.grunt;

import java.util.Collection;
import java.util.Collections;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.spi.ImportantFilesImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

public final class ImportantFilesImpl implements ImportantFilesImplementation {

    static final String GRUNT_FILE = "Gruntfile.js"; // NOI18N

    private final Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    final GruntFileListener gruntFileListener = new GruntFileListener();


    private ImportantFilesImpl(Project project) {
        assert project != null;
        this.project = project;
    }

    @ProjectServiceProvider(service = ImportantFilesImplementation.class, projectType = "org-netbeans-modules-web-clientproject") // NOI18N
    public static ImportantFilesImplementation forHtml5Project(Project project) {
        ImportantFilesImpl importantFiles = new ImportantFilesImpl(project);
        FileObject projectDirectory = project.getProjectDirectory();
        projectDirectory.addFileChangeListener(WeakListeners.create(FileChangeListener.class, importantFiles.gruntFileListener, projectDirectory));
        return importantFiles;
    }

    @Override
    public Collection<ImportantFilesImplementation.FileInfo> getFiles() {
        FileObject gruntFile = project.getProjectDirectory().getFileObject(GRUNT_FILE);
        if (gruntFile == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new ImportantFilesImplementation.FileInfo(gruntFile, gruntFile.getName(), null));
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    //~ Inner classes

    private final class GruntFileListener extends FileChangeAdapter {

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            check(fe.getFile().getNameExt());
            check(fe.getName() + "." + fe.getExt()); // NOI18N
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            check(fe.getFile().getNameExt());
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            check(fe.getFile().getNameExt());
        }

        private void check(String filename) {
            if (GRUNT_FILE.equals(filename)) {
                fireChange();
            }
        }

    }

}
