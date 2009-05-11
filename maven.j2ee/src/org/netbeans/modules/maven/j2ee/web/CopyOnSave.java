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
package org.netbeans.modules.maven.j2ee.web;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.StringTokenizer;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jesse Glick, et al., Pavel Buzek
 * @author mkleint - copied and adjusted from netbeans.org web project until it gets rewritten there to
 *  be generic.
 */
public class CopyOnSave extends FileChangeAdapter implements PropertyChangeListener {

    private FileObject docBase = null;
    private Project project;
    private WebModuleProviderImpl provider;
    boolean active = false;
    private NbMavenProject mavenproject;

    /** Creates a new instance of CopyOnSaveSupport */
    public CopyOnSave(Project prj, WebModuleProviderImpl prov) {
        project = prj;
        provider = prov;
        mavenproject = project.getLookup().lookup(NbMavenProject.class);
    }

    private void copySrcToDest( FileObject srcFile, FileObject destFile) throws IOException {
        if (destFile != null && !srcFile.isFolder()) {
            InputStream is = null;
            OutputStream os = null;
            FileLock fl = null;
            try {
                is = srcFile.getInputStream();
                fl = destFile.lock();
                os = destFile.getOutputStream(fl);
                FileUtil.copy(is, os);
            } finally {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
                if (fl != null) {
                    fl.releaseLock();
                }
            }
        }
    }

    private WebModule getWebModule() {
        return provider.findWebModule(project.getProjectDirectory());
    }

    private J2eeModule getJ2eeModule() {
        return provider.getJ2eeModule();
    }

    private boolean isInPlace() throws IOException {
        FileObject fo = getJ2eeModule().getContentDirectory();
        return fo != null && fo.equals(getWebModule().getDocumentBase());
    }

    public void initialize() throws FileStateInvalidException {
        smallinitialize();
        NbMavenProject.addPropertyChangeListener(project, this);
        active = true;
    }

    public void cleanup() throws FileStateInvalidException {
        smallcleanup();
        NbMavenProject.removePropertyChangeListener(project, this);
        active = false;
    }

    public void smallinitialize() throws FileStateInvalidException {
        docBase = getWebModule().getDocumentBase();
        if (docBase != null) {
            docBase.getFileSystem().addFileChangeListener(this);
        }
    }

    public void smallcleanup() throws FileStateInvalidException {
        if (docBase != null) {
            docBase.getFileSystem().removeFileChangeListener(this);
        }
    }

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

    /** Fired when a file is changed.
     * @param fe the event describing context where action has taken place
     */
    @Override
    public void fileChanged(FileEvent fe) {
        try {
            if (!isInPlace()) {
                handleCopyFileToDestDir(fe.getFile());
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        try {
            if (!isInPlace()) {
                handleCopyFileToDestDir(fe.getFile());
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        try {
            if (isInPlace()) {
                return;
            }

            FileObject fo = fe.getFile();
            FileObject base = findAppropriateResourceRoots(fo);
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
    public void fileDeleted(FileEvent fe) {
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
        FileObject root = findAppropriateResourceRoots(fo);
        if (root != null) {
            // inside docbase
            path = path != null ? path : FileUtil.getRelativePath(root, fo);
            if (!isSynchronizationAppropriate(path)) {
                return;
            }

            FileObject webBuildBase = getJ2eeModule().getContentDirectory();
            if (webBuildBase != null) {
                // project was built
                FileObject webInfClasses = comesFromWebappRoot(fo) ? webBuildBase : webBuildBase.getFileObject("WEB-INF/classes");
                if (webInfClasses != null) {
                    FileObject toDelete = webInfClasses.getFileObject(path);
                    if (toDelete != null) {
                        toDelete.delete();
                    }
                }
            }
        }
    }

    /** Copies a content file to an appropriate  destination directory, 
     * if applicable and relevant.
     */
    private void handleCopyFileToDestDir(FileObject fo) throws IOException {
        if (!fo.isVirtual()) {
            FileObject documentBase = findAppropriateResourceRoots(fo);
            if (documentBase != null) {
                // inside docbase
                String path = FileUtil.getRelativePath(documentBase, fo);
                if (!isSynchronizationAppropriate(path)) {
                    return;
                }
                FileObject webBuildBase = getJ2eeModule().getContentDirectory();
                
                if (webBuildBase != null) {
                    // project was built
                    if (FileUtil.isParentOf(documentBase, webBuildBase) || FileUtil.isParentOf(webBuildBase, documentBase)) {
                        //cannot copy into self
                        return;
                    }
                    FileObject destinationFolder = comesFromWebappRoot(fo) ? webBuildBase : webBuildBase.getFileObject("WEB-INF/classes");
                    FileObject destFile = ensureDestinationFileExists(destinationFolder, path, fo.isFolder());
                    copySrcToDest(fo, destFile);
                }
            }
        }
    }

    private boolean comesFromWebappRoot(FileObject child) {
        FileObject documentBase = getWebModule().getDocumentBase();
        return documentBase != null && FileUtil.isParentOf(documentBase, child);
    }

    //#106522 make sure we also copy src/main/resource.. TODO for now ignore resource filtering or repackaging..
    private FileObject findAppropriateResourceRoots(FileObject child) {
        FileObject documentBase = getWebModule().getDocumentBase();
        if (documentBase != null && FileUtil.isParentOf(documentBase, child)) {
            return documentBase;
        }
        URI[] uris = mavenproject.getResources(false);
        for (URI uri : uris) {
            FileObject fo = FileUtil.toFileObject(new File(uri));
            if (fo != null && FileUtil.isParentOf(fo, child)) {
                return fo;
            }
        }
        return null;
    }

    /** Returns the destination (parent) directory needed to create file with relative path path under webBuilBase
     */
    private FileObject ensureDestinationFileExists(FileObject webBuildBase, String path, boolean isFolder) throws IOException {
        FileObject current = webBuildBase;
        StringTokenizer st = new StringTokenizer(path, "/"); //NOI18N
        while (st.hasMoreTokens()) {
            String pathItem = st.nextToken();
            FileObject newCurrent = current.getFileObject(pathItem);
            if (newCurrent == null) {
                // need to create it
                if (isFolder || st.hasMoreTokens()) {
                    // create a folder
                    newCurrent = FileUtil.createFolder(current, pathItem);
                } else {
                    newCurrent = FileUtil.createData(current, pathItem);
                }
            }
            current = newCurrent;
        }
        return current;
    }
}
