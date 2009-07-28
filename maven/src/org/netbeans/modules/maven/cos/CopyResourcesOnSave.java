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
package org.netbeans.modules.maven.cos;

import hidden.org.codehaus.plexus.util.DirectoryScanner;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.spi.cos.AdditionalDestination;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * @author mkleint
 */
public class CopyResourcesOnSave extends FileChangeAdapter {

    private static CopyResourcesOnSave instance = new CopyResourcesOnSave();

    private boolean isAdded = false;
    /** Creates a new instance of CopyOnSaveSupport */
    private CopyResourcesOnSave() {
    }

    public static CopyResourcesOnSave getInstance() {
        return instance;
    }

    public void checkOpenProjects() {
        boolean hasAnyMavens = false;
        for (Project prj : OpenProjects.getDefault().getOpenProjects()) {
            if (prj.getLookup().lookup(NbMavenProject.class) != null) {
                hasAnyMavens = true;
                break;
            }
        }
        if (hasAnyMavens) {
            if (!isAdded) {
                FileUtil.addFileChangeListener(this);
                isAdded = true;
            }
        } else {
            if (isAdded) {
                FileUtil.removeFileChangeListener(this);
                isAdded = false;
            }
        }
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

    private Project getOwningMavenProject(FileObject file) {
        Project prj = FileOwnerQuery.getOwner(file);
        if (prj == null) {
            return null;
        }
        NbMavenProject mvn = prj.getLookup().lookup(NbMavenProject.class);
        if (mvn == null) {
            return null;
        }
        if (RunUtils.hasTestCompileOnSaveEnabled(prj) || RunUtils.hasApplicationCompileOnSaveEnabled(prj)) {
            return prj;
        }
        return null;
    }

    /** Fired when a file is changed.
     * @param fe the event describing context where action has taken place
     */
    @Override
    public void fileChanged(final FileEvent fe) {
        if (SwingUtilities.isEventDispatchThread()) {//#167740
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    fileChanged(fe);
                }
            });
            return;
        }
        Project owning = getOwningMavenProject(fe.getFile());
        if (owning == null) {
            return;
        }
        try {
            handleCopyFileToDestDir(fe.getFile(), owning);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void fileDataCreated(final FileEvent fe) {
       if (SwingUtilities.isEventDispatchThread()) {//#167740
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    fileDataCreated(fe);
                }
            });
            return;
        }
        Project owning = getOwningMavenProject(fe.getFile());
        if (owning == null) {
            return;
        }
        try {
            handleCopyFileToDestDir(fe.getFile(), owning);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void fileRenamed(final FileRenameEvent fe) {
        if (SwingUtilities.isEventDispatchThread()) {//#167740
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    fileRenamed(fe);
                }
            });
            return;
        }
        try {
            FileObject fo = fe.getFile();
            Project owning = getOwningMavenProject(fo);
            if (owning == null) {
                return;
            }
            Tuple base = findAppropriateResourceRoots(fo, owning);
            if (base != null) {
                handleCopyFileToDestDir(base, fo, owning);
                FileObject parent = fo.getParent();
                String path;
                if (FileUtil.isParentOf(base.root, parent)) {
                    path = FileUtil.getRelativePath(base.root, fo.getParent()) +
                            "/" + fe.getName() + "." + fe.getExt(); //NOI18N
                } else {
                    path = fe.getName() + "." + fe.getExt(); //NOI18N
                }
                handleDeleteFileInDestDir(fo, path, base, owning);
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }

    @Override
    public void fileDeleted(final FileEvent fe) {
        if (SwingUtilities.isEventDispatchThread()) {//#167740
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    fileDeleted(fe);
                }
            });
            return;
        }
        Project owning = getOwningMavenProject(fe.getFile());
        if (owning == null) {
            return;
        }
        try {
            handleDeleteFileInDestDir(fe.getFile(), null, owning);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void handleDeleteFileInDestDir(FileObject fo, String path, Project project) throws IOException {
        Tuple tuple = findAppropriateResourceRoots(fo, project);
        handleDeleteFileInDestDir(fo, path, tuple, project);
    }

    private void handleDeleteFileInDestDir(FileObject fo, String path, Tuple tuple, Project project) throws IOException {
        if (tuple != null) {
            // inside docbase
            path = path != null ? path : FileUtil.getRelativePath(tuple.root, fo);
            path = addTargetPath(path, tuple.resource);
            FileObject toDelete = tuple.destinationRoot.getFileObject(path);
            if (toDelete != null) {
                toDelete.delete();
            }
            AdditionalDestination add = project.getLookup().lookup(AdditionalDestination.class);
            if (add != null) {
                add.delete(fo, path);
            }
        }
    }

    /** Copies a content file to an appropriate  destination directory, 
     * if applicable and relevant.
     */
    private void handleCopyFileToDestDir(FileObject fo, Project prj) throws IOException {
        Tuple tuple = findAppropriateResourceRoots(fo, prj);
        handleCopyFileToDestDir(tuple, fo, prj);
    }
    
    /** Copies a content file to an appropriate  destination directory,
     * if applicable and relevant.
     */
    private void handleCopyFileToDestDir(Tuple tuple, FileObject fo, Project project) throws IOException {
        if (tuple != null && !tuple.resource.isFiltering()) {
            //TODO what to do with filtering? for now ignore..
            String path = FileUtil.getRelativePath(tuple.root, fo);
            path = addTargetPath(path, tuple.resource);
            FileObject destFile = ensureDestinationFileExists(tuple.destinationRoot, path, fo.isFolder());
            copySrcToDest(fo, destFile);
            AdditionalDestination add = project.getLookup().lookup(AdditionalDestination.class);
            if (add != null) {
                add.copy(fo, path);
            }
        }
    }

    private String addTargetPath(String path, Resource resource) {
        String target = resource.getTargetPath();
        if (target != null) {
            target = target.replace("\\", "/");
            target = target.endsWith("/") ? target : (target + "/");
            path = target + path;
        }
        return path;
    }

    private Tuple findAppropriateResourceRoots(FileObject child, Project prj) {
        NbMavenProject nbproj = prj.getLookup().lookup(NbMavenProject.class);
        assert nbproj != null;
        boolean test = RunUtils.hasTestCompileOnSaveEnabled(prj);
        if (test) {
            Tuple tup = findResource(nbproj.getMavenProject().getTestResources(), prj, nbproj, child, true);
            if (tup != null) {
                return tup;
            }
        }
        boolean main = RunUtils.hasApplicationCompileOnSaveEnabled(prj);
        if (test || main) {
            Tuple tup = findResource(nbproj.getMavenProject().getResources(), prj, nbproj, child, false);
            if (tup != null) {
                return tup;
            }
        }
        return null;
    }

    private Tuple findResource(List<Resource> resources, Project prj, NbMavenProject nbproj, FileObject child, boolean test) {
        if (resources == null) {
            return null;
        }
        MavenProject mav = nbproj.getMavenProject();
        FileObject target = null;
        //now figure the destination output folder
        if (mav.getBuild() != null) {
            File fil = new File(test ? mav.getBuild().getTestOutputDirectory() : mav.getBuild().getOutputDirectory());
            fil = FileUtil.normalizeFile(fil);
            File stamp = new File(fil, CosChecker.NB_COS);
            if (stamp.exists()) {
                target = FileUtil.toFileObject(fil);
            } else {
                // no compile on save stamp, means no copying, classes don't get copied/compiled either.
                return null;
            }
        } else {
            //no output dir means no copying.
            return null;
        }

        resourceLoop:
        for (Resource res : resources) {
            URI uri = FileUtilities.getDirURI(prj.getProjectDirectory(), res.getDirectory());
            FileObject fo = FileUtil.toFileObject(new File(uri));
            if (fo != null && FileUtil.isParentOf(fo, child)) {
                String path = FileUtil.getRelativePath(fo, child);
                //now check includes and excludes
                @SuppressWarnings("unchecked")
                List<String> incls = res.getIncludes();
                if (incls.size() == 0) {
                    incls = Arrays.asList(CosChecker.DEFAULT_INCLUDES);
                }
                boolean included = false;
                for (String incl : incls) {
                    if (DirectoryScanner.match(incl, path)) {
                        included = true;
                        break;
                    }
                }
                if (!included) {
                    break;
                }
                @SuppressWarnings("unchecked")
                List<String> excls = new ArrayList<String>(res.getExcludes());
                excls.addAll(Arrays.asList(DirectoryScanner.DEFAULTEXCLUDES));
                for (String excl : excls) {
                    if (DirectoryScanner.match(excl, path)) {
                        continue resourceLoop;
                    }
                }

                return new Tuple(res, fo, target);
            }
        }
        return null;
    }

    /** Returns the destination file or folder
     */
    private FileObject ensureDestinationFileExists(FileObject root, String path, boolean isFolder) throws IOException {
        if (isFolder) {
            return FileUtil.createFolder(root, path);
        } else {
            return FileUtil.createData(root, path);
        }
    }

    private class Tuple {
        Resource resource;
        FileObject root;
        FileObject destinationRoot;

        private Tuple(Resource res, FileObject fo, FileObject destFolder) {
            resource = res;
            root = fo;
            destinationRoot = destFolder;
        }
    }
}
