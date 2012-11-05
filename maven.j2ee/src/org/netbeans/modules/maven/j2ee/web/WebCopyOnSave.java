/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee.web;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.CopyOnSave;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

@ProjectServiceProvider(service = CopyOnSave.class, projectType = {"org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR})
public class WebCopyOnSave extends CopyOnSave implements PropertyChangeListener {

    private static final RequestProcessor COS_PROCESSOR = new RequestProcessor("Maven Copy on Save", 5);
    private FileChangeListener listener = new FileListenerImpl();
    private FileObject docBase = null;
    private boolean active = false;


    public WebCopyOnSave(Project project) {
        super(project);
    }

    private WebModule getWebModule() {
        final J2eeModuleProvider moduleProvider = getJ2eeModuleProvider();
        if (moduleProvider != null && moduleProvider instanceof WebModuleProviderImpl) {
            return ((WebModuleProviderImpl) moduleProvider).findWebModule(getProject().getProjectDirectory());
        }
        return null;
    }

    private boolean isInPlace() throws IOException {
        final WebModule webModule = getWebModule();
        final J2eeModule j2eeModule = getJ2eeModule();

        if (j2eeModule == null || webModule == null) {
            return false;
        }

        final FileObject fo = j2eeModule.getContentDirectory();

        if (fo == null) {
            return false;
        }
        return fo.equals(webModule.getDocumentBase());
    }

    @Override
    public void initialize() throws FileStateInvalidException {
        if (!active) {
            smallinitialize();
            NbMavenProject.addPropertyChangeListener(getProject(), this);
            active = true;
        }
    }

    @Override
    public void cleanup() throws FileStateInvalidException {
        if (active) {
            smallcleanup();
            NbMavenProject.removePropertyChangeListener(getProject(), this);
            active = false;
        }
    }

    private void smallinitialize() throws FileStateInvalidException {
        WebModule webModule = getWebModule();

        if (webModule != null) {
            docBase = webModule.getDocumentBase();
            if (docBase != null) {
                docBase.getFileSystem().addFileChangeListener(listener);
            }
        }
    }

    private void smallcleanup() throws FileStateInvalidException {
        if (docBase != null) {
            docBase.getFileSystem().removeFileChangeListener(listener);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
            try {
                //TODO reduce cleanup to cases where the actual directory locations change..
                if (active) {
                    smallcleanup();
                    smallinitialize();
                }
            } catch (org.openide.filesystems.FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }

    private class FileListenerImpl extends FileChangeAdapter {

        /** Fired when a file is changed.
         * @param fe the event describing context where action has taken place
         */
        @Override
        public void fileChanged(final FileEvent fe) {
            if (SwingUtilities.isEventDispatchThread()) {//#167740
                COS_PROCESSOR.post(new Runnable() {
                    @Override
                    public void run() {
                        fileChanged(fe);
                    }
                });
                return;
            }
            try {
                if (!isInPlace()) {
                    handleCopyFileToDestDir(fe.getFile());
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }

        @Override
        public void fileDataCreated(final FileEvent fe) {
            if (SwingUtilities.isEventDispatchThread()) {//#167740
                COS_PROCESSOR.post(new Runnable() {
                    @Override
                    public void run() {
                        fileDataCreated(fe);
                    }
                });
                return;
            }
            try {
                if (!isInPlace()) {
                    handleCopyFileToDestDir(fe.getFile());
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }

        @Override
        public void fileRenamed(final FileRenameEvent fe) {
            if (SwingUtilities.isEventDispatchThread()) {//#167740
                COS_PROCESSOR.post(new Runnable() {
                    @Override
                    public void run() {
                        fileRenamed(fe);
                    }
                });
                return;
            }
            try {
                if (isInPlace()) {
                    return;
                }

                FileObject fo = fe.getFile();
                FileObject base = findWebDocRoot(fo);
                if (base != null) {
                    handleCopyFileToDestDir(fo);
                    FileObject parent = fo.getParent();
                    String path;
                    if (FileUtil.isParentOf(base, parent)) {
                        path = FileUtil.getRelativePath(base, fo.getParent()) +
                                "/" + fe.getName() + "." + fe.getExt(); //NOI18N
                    } else {
                        path = fe.getName() + "." + fe.getExt(); //NOI18N
                    }
                    if (!isSynchronizationAppropriate(path)) {
                        return;
                    }
                    handleDeleteFileInDestDir(fo, path);
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }

        @Override
        public void fileDeleted(final FileEvent fe) {
            if (SwingUtilities.isEventDispatchThread()) { //#167740
                COS_PROCESSOR.post(new Runnable() {
                    @Override
                    public void run() {
                        fileDeleted(fe);
                    }
                });
                return;
            }
            try {
                if (isInPlace()) {
                    return;
                }
                FileObject fo = fe.getFile();
                handleDeleteFileInDestDir(fo, null);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }


    private boolean isSynchronizationAppropriate(String filePath) {
        if (filePath.startsWith("WEB-INF/classes")) { //NOI18N
            return false;
        }
        if (filePath.startsWith("WEB-INF/src")) { //NOI18N
            return false;
        }
        if (filePath.startsWith("WEB-INF/lib")) { //NOI18N
            return false;
        }
        return true;
    }

    private void handleDeleteFileInDestDir(FileObject fo, String path) throws IOException {
        final FileObject root = findWebDocRoot(fo);
        if (root != null) {
            // inside docbase
            path = path != null ? path : FileUtil.getRelativePath(root, fo);
            if (!isSynchronizationAppropriate(path)) {
                return;
            }
            
            final J2eeModule j2eeModule = getJ2eeModule();
            if (j2eeModule == null) {
                return;
            }

            final FileObject webBuildBase = j2eeModule.getContentDirectory();
            if (webBuildBase != null) {
                // project was built
                FileObject toDelete = webBuildBase.getFileObject(path);
                if (toDelete != null) {
                    File fil = FileUtil.normalizeFile(FileUtil.toFile(toDelete));
                    toDelete.delete();
                    fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(fil)));
                }
            }
        }
    }

    /** Copies a content file to an appropriate  destination directory,
     * if applicable and relevant.
     */
    private void handleCopyFileToDestDir(FileObject fo) throws IOException {
        if (!fo.isVirtual()) {
            final FileObject documentBase = findWebDocRoot(fo);
            if (documentBase != null) {
                // inside docbase
                final String path = FileUtil.getRelativePath(documentBase, fo);
                if (!isSynchronizationAppropriate(path)) {
                    return;
                }

                final J2eeModule j2eeModule = getJ2eeModule();
                if (j2eeModule == null) {
                    return;
                }
                
                final FileObject webBuildBase = j2eeModule.getContentDirectory();
                if (webBuildBase != null) {
                    // project was built
                    if (FileUtil.isParentOf(documentBase, webBuildBase) || FileUtil.isParentOf(webBuildBase, documentBase)) {
                        //cannot copy into self
                        return;
                    }
                    FileObject destFile = ensureDestinationFileExists(webBuildBase, path, fo.isFolder());
                    File fil = FileUtil.normalizeFile(FileUtil.toFile(destFile));
                    copySrcToDest(fo, destFile);
                    fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(fil)));
                }
            }
        }
    }

    private FileObject findWebDocRoot(FileObject child) {
        WebModule webModule = getWebModule();

        if (webModule != null) {
            FileObject documentBase = webModule.getDocumentBase();
            if (documentBase != null && FileUtil.isParentOf(documentBase, child)) {
                return documentBase;
            }
        }
        return null;
    }

    @Override
    protected String getDestinationSubFolderName() {
        return "WEB-INF/classes"; // NOI18N
    }

}
