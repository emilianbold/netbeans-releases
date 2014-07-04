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

package org.netbeans.modules.web.clientproject.node;

import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Becicka
 */
public abstract class NodeProblemProvider implements ProjectProblemsProvider {
    
    protected final ProjectProblemsProviderSupport problemsProviderSupport = new ProjectProblemsProviderSupport(this);
    protected final Project project;
    protected volatile FileChangeListener fileChangesListener = new FileChangesListener();

    public NodeProblemProvider(Project project) {
        assert project!=null;
        this.project = project;
    }

    protected void addFileChangesListeners(String ... paths) {
        for (String path:paths) {
            addFileChangeListener(getFile(path));
        }
    }

    private File getFile(String relPath) {
        FileObject root = project.getProjectDirectory();
        return new File(root.getPath() + "/" + relPath);
    }

    private void addFileChangeListener(File file) {
        if (file == null) {
            return;
        }
        try {
            FileUtil.addFileChangeListener(fileChangesListener, file);
        } catch (IllegalArgumentException ex) {
            // already listenening, ignore
        }
    }
    
    private final class FileChangesListener implements FileChangeListener {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            problemsProviderSupport.fireProblemsChange();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            // noop
        }

        @Override
        public void fileChanged(FileEvent fe) {
            // noop
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            problemsProviderSupport.fireProblemsChange();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            problemsProviderSupport.fireProblemsChange();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }

    }    

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        problemsProviderSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        problemsProviderSupport.removePropertyChangeListener(listener);
    }    
    
}
