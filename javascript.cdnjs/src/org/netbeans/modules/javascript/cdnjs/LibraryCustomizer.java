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
package org.netbeans.modules.javascript.cdnjs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.javascript.cdnjs.api.CDNJSLibraries;
import org.netbeans.modules.javascript.cdnjs.ui.SelectionPanel;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Stola
 */
public class LibraryCustomizer implements ProjectCustomizer.CompositeCategoryProvider {
    private static final String CATEGORY_NAME = "JavaScriptFiles"; // NOI18N
    private static final String PREFERENCES_LIBRARY_FOLDER = "js.libs.folder"; // NOI18N
    private final CDNJSLibraries.CustomizerContext customizerContext;
    
    public LibraryCustomizer(CDNJSLibraries.CustomizerContext context) {
        this.customizerContext = context;
    }

    @Override
    @NbBundle.Messages("LibraryCustomizer.displayName=JavaScript Files")
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(
                CATEGORY_NAME, Bundle.LibraryCustomizer_displayName(), null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        Library.Version[] libraries = LibraryPersistence.getDefault().loadLibraries(project);
        File webRoot = customizerContext.getWebRoot(context);
        if (webRoot == null) {
            webRoot = FileUtil.toFile(project.getProjectDirectory());
        }
        String libraryFolder = getLibraryFolder(project);
        final SelectionPanel customizer = new SelectionPanel(libraries, webRoot, libraryFolder);
        category.setStoreListener(new StoreListener(project, webRoot, customizer));
        category.setCloseListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customizer.dispose();
            }
        });
        return customizer;
    }

    private static Preferences getProjectPreferences(Project project) {
        // Using class from web.clientproject.api for backward compatibility
        return ProjectUtils.getPreferences(project, WebClientProjectConstants.class, true);
    }

    static String getLibraryFolder(Project project) {
        return getProjectPreferences(project).get(PREFERENCES_LIBRARY_FOLDER, null);
    }

    static void storeLibraryFolder(Project project, String libraryFolder) {
        getProjectPreferences(project).put(PREFERENCES_LIBRARY_FOLDER, libraryFolder);
    }

    static class StoreListener implements ActionListener, Runnable {
        private final Project project;
        private final File webRoot;
        private final SelectionPanel customizer;

        StoreListener(Project project, File webRoot, SelectionPanel customizer) {
            this.project = project;
            this.webRoot = webRoot;
            this.customizer = customizer;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(this);
        }

        @Override
        @NbBundle.Messages("LibraryCustomizer.addingLibraries=Adding the selected JavaScript libraries...")
        public void run() {
            ProgressHandle progressHandle = ProgressHandle.createHandle(Bundle.LibraryCustomizer_addingLibraries());
            progressHandle.start();

            try {
                Library.Version[] selectedVersions = customizer.getSelectedLibraries();
                Library.Version[] versionsToStore = updateLibraries(selectedVersions);
                LibraryPersistence.getDefault().storeLibraries(project, versionsToStore);

                String libraryFolder = customizer.getLibraryFolder();
                storeLibraryFolder(project, libraryFolder);
            } finally {
                progressHandle.finish();
            }
        }

        @NbBundle.Messages({
            "# {0} - library name",
            "# {1} - library version name",
            "LibraryCustomizer.downloadFailed=Download of version {1} of library {0} failed!",
            "# {0} - library name",
            "# {1} - library version name",
            "LibraryCustomizer.installationFailed=Installation of version {1} of library {0} failed!",
            "LibraryCustomizer.libraryFolderFailure=Unable to create library folder!"
        })
        private Library.Version[] updateLibraries(Library.Version[] newLibraries) {
            List<String> errors = new ArrayList<>();
            
            Library.Version[] oldLibraries = LibraryPersistence.getDefault().loadLibraries(project);
            Map<String,Library.Version> oldMap = toMap(oldLibraries);
            Map<String,Library.Version> newMap = toMap(newLibraries);

            // Identify versions to install and keep
            List<Library.Version> toInstall = new ArrayList<>();
            List<String> toKeep = new ArrayList<>();
            for (Library.Version newVersion : newLibraries) {
                String libraryName = newVersion.getLibrary().getName();
                Library.Version oldVersion = oldMap.get(libraryName);
                if (oldVersion == null) {
                    toInstall.add(newVersion);
                } else if (oldVersion.getName().equals(newVersion.getName())) {
                    toKeep.add(libraryName);
                } else {
                    toInstall.add(newVersion);
                }
            }

            // Create library folder
            FileObject librariesFob;
            try {
                String librariesFolderName = customizer.getLibraryFolder();
                FileObject webRootFob = FileUtil.toFileObject(webRoot);
                librariesFob = FileUtil.createFolder(webRootFob, librariesFolderName);
            } catch (IOException ioex) {
                String errorMessage = Bundle.LibraryCustomizer_libraryFolderFailure();
                errors.add(errorMessage);
                Logger.getLogger(LibraryCustomizer.class.getName()).log(Level.INFO, errorMessage, ioex);
                return oldLibraries;
            }

            // Download versions to install
            Map<String,File[]> downloadedFiles = new HashMap<>();
            for (Library.Version version : toInstall) {
                String libraryName = version.getLibrary().getName();
                try {
                    File[] files = new File[version.getFiles().length];
                    for (int fileIndex=0; fileIndex<files.length; fileIndex++) {
                        files[fileIndex] = LibraryProvider.getInstance().downloadLibraryFile(version, fileIndex);
                    }
                    downloadedFiles.put(libraryName, files);
                } catch (IOException ioex) {
                    String errorMessage = Bundle.LibraryCustomizer_downloadFailed(libraryName, version.getName());
                    errors.add(errorMessage);
                    Logger.getLogger(LibraryCustomizer.class.getName()).log(Level.INFO, errorMessage, ioex);
                    Library.Version oldVersion = oldMap.get(libraryName);
                    if (oldVersion == null) {
                        newMap.remove(libraryName);
                    } else {
                        newMap.put(libraryName, oldVersion);
                    }
                }
            }

            // Identify versions to remove
            List<Library.Version> toRemove = new ArrayList<>();
            for (Library.Version oldVersion : oldLibraries) {
                String libraryName = oldVersion.getLibrary().getName();
                Library.Version newVersion = newMap.get(libraryName);
                if (newVersion == null || !newVersion.getName().equals(oldVersion.getName())) {
                    toRemove.add(oldVersion);
                }
            }

            // Remove the identified versions
            uninstallLibraries(toRemove);

            // Install the identified versions
            for (Library.Version version : toInstall) {
                String libraryName = version.getLibrary().getName();
                File[] files = downloadedFiles.get(libraryName);
                if (files != null) {
                    try {
                        installLibrary(librariesFob, version, files);
                        newMap.put(libraryName, version);
                    } catch (IOException ioex) {
                        String errorMessage = Bundle.LibraryCustomizer_installationFailed(libraryName, version.getName());
                        errors.add(errorMessage);
                        Logger.getLogger(LibraryCustomizer.class.getName()).log(Level.INFO, errorMessage, ioex);
                        newMap.remove(libraryName);
                    }
                }
            }

            // Install/remove files in the versions that are kept
            String newLibraryFolder = customizer.getLibraryFolder();
            String oldLibraryFolder = getLibraryFolder(project);
            boolean libFolderChanged = !newLibraryFolder.equals(oldLibraryFolder);
            for (String libraryName : toKeep) {
                Library.Version oldVersion = oldMap.get(libraryName);
                if (libFolderChanged) {
                    moveLibrary(librariesFob, oldVersion, errors);
                }
                Library.Version newVersion = newMap.get(libraryName);
                Library.Version versionToStore = updateLibrary(librariesFob, oldVersion, newVersion, errors);
                newMap.put(libraryName, versionToStore);
            }

            if (!errors.isEmpty()) {
                StringBuilder message = new StringBuilder();
                for (String error : errors) {
                    if (message.length() != 0) {
                        message.append('\n');
                    }
                    message.append(error);
                }
                NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                        message.toString(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(descriptor);
            }
 
            return newMap.values().toArray(new Library.Version[newMap.size()]);
        }

        @NbBundle.Messages({
            "# {0} - library name",
            "LibraryCustomizer.libraryFolderCreationFailed=Unable to create the folder for the library {0}!",
            "# {0} - library name",
            "# {1} - file path",
            "LibraryCustomizer.moveFailed=Move of the file {1} of the library {0} failed!",
        })
        private void moveLibrary(FileObject librariesFolder, Library.Version version, List<String> errors) {
            FileObject projectFob = project.getProjectDirectory();
            String libraryName = version.getLibrary().getName();
            try {
                FileObject libraryFolder = FileUtil.createFolder(librariesFolder, libraryName);
                File projectDir = FileUtil.toFile(projectFob);
                String[] files = version.getFiles();
                String[] localFiles = version.getLocalFiles();
                for (int i=0; i<files.length; i++) {
                    File file = PropertyUtils.resolveFile(projectDir, localFiles[i]);
                    if (file.exists()) {
                        try {
                            FileObject fob = FileUtil.toFileObject(file);
                            String[] pathElements = files[i].split("/"); // NOI18N
                            FileObject fileFolder = libraryFolder;
                            for (int j=0; j<pathElements.length-1; j++) {
                                fileFolder = FileUtil.createFolder(fileFolder, pathElements[j]);
                            }
                            String fileName = pathElements[pathElements.length-1];
                            int index = fileName.lastIndexOf('.');
                            if (index != -1) {
                                fileName = fileName.substring(0,index);
                            }
                            fob = FileUtil.moveFile(fob, fileFolder, fileName);
                            localFiles[i] = PropertyUtils.relativizeFile(projectDir, FileUtil.toFile(fob));
                            removeFile(file.getParentFile());
                        } catch (IOException ioex) {
                            String errorMessage = Bundle.LibraryCustomizer_moveFailed(libraryName, files[i]);
                            errors.add(errorMessage);
                            Logger.getLogger(LibraryCustomizer.class.getName()).log(Level.INFO, errorMessage, ioex);
                        }
                    }
                }
            } catch (IOException ioex) {
                String errorMessage = Bundle.LibraryCustomizer_libraryFolderCreationFailed(libraryName);
                errors.add(errorMessage);
                Logger.getLogger(LibraryCustomizer.class.getName()).log(Level.INFO, errorMessage, ioex);
            }
        }

        @NbBundle.Messages({
            "# {0} - library name",
            "# {1} - file name/path",
            "LibraryCustomizer.updateFailed=Modification of library {0} failed for file {1}."
        })
        private Library.Version updateLibrary(FileObject librariesFolder,
                Library.Version oldVersion, Library.Version newVersion,
                List<String> errors) {
            FileObject projectFob = project.getProjectDirectory();
            File projectDir = FileUtil.toFile(projectFob);
            LibraryProvider libraryProvider = LibraryProvider.getInstance();
            String libraryName = oldVersion.getLibrary().getName();
            FileObject libraryFolder = librariesFolder.getFileObject(libraryName);
            
            // Install missing files
            Map<String,String> oldFilesMap = new HashMap<>();
            String[] oldFiles = oldVersion.getFiles();
            String[] oldLocalFiles = oldVersion.getLocalFiles();
            for (int i=0; i<oldFiles.length; i++) {
                oldFilesMap.put(oldFiles[i], oldLocalFiles[i]);
            }
            List<String> fileList = new ArrayList<>();
            List<String> localFileList = new ArrayList<>();
            String[] filesToInstall = newVersion.getFiles();
            for (int fileIndex = 0; fileIndex < filesToInstall.length; fileIndex++) {
                String filePath = filesToInstall[fileIndex];
                try {
                    String localPath = oldFilesMap.get(filePath);
                    if (localPath == null) {
                        // Installation needed
                        String[] pathElements = filePath.split("/"); // NOI18N
                        FileObject fileFolder = libraryFolder;
                        for (int j=0; j<pathElements.length-1; j++) {
                            fileFolder = FileUtil.createFolder(fileFolder, pathElements[j]);
                        }
                        File file = libraryProvider.downloadLibraryFile(newVersion, fileIndex);
                        FileObject tmpFob = FileUtil.toFileObject(file);
                        String fileName = pathElements[pathElements.length-1];
                        int index = fileName.lastIndexOf('.');
                        if (index != -1) {
                            fileName = fileName.substring(0,index);
                        }
                        FileObject fob = FileUtil.moveFile(tmpFob, fileFolder, fileName);
                        file = FileUtil.toFile(fob);
                        localPath = PropertyUtils.relativizeFile(projectDir, file);
                    }
                    fileList.add(filePath);
                    localFileList.add(localPath);
                } catch (IOException ioex) {
                    String errorMessage = Bundle.LibraryCustomizer_updateFailed(libraryName, filePath);
                    errors.add(errorMessage);
                    Logger.getLogger(LibraryCustomizer.class.getName()).log(Level.INFO, errorMessage, ioex);
                }
            }

            // Remove files that are no longer needed
            for (int i=0; i<oldFiles.length; i++) {
                if (!fileList.contains(oldFiles[i])) {
                    uninstallFile(oldLocalFiles[i]);
                }
            }

            Collection<String> emptySet = Collections.emptySet();
            Library.Version versionToStore = newVersion.filterVersion(emptySet);
            versionToStore.setFileInfo(
                    fileList.toArray(new String[fileList.size()]),
                    localFileList.toArray(new String[localFileList.size()])
            );
            return versionToStore;
        }

        private void uninstallLibraries(List<Library.Version> libraries) {
            for (Library.Version version : libraries) {
                for (String fileName : version.getLocalFiles()) {
                    uninstallFile(fileName);
                }
            }
        }

        private void removeFile(File file) {
            while (!webRoot.equals(file)) {
                File parent = file.getParentFile();
                if (!file.delete()) {
                    // We have reached a parent directory that is not empty
                    break;
                }
                file = parent;
            }            
        }

        private void uninstallFile(String filePath) {
            File projectDir = FileUtil.toFile(project.getProjectDirectory());
            File file = PropertyUtils.resolveFile(projectDir, filePath);
            if (file.exists()) {
                removeFile(file);
            } else {
                Logger.getLogger(LibraryCustomizer.class.getName()).log(
                        Level.INFO, "Cannot delete file {0}. It no longer exists.",
                        new Object[]{file.getAbsolutePath()});
            }
        }

        private void installLibrary(FileObject librariesFolder,
                Library.Version version, File[] libraryFiles) throws IOException {
            String libraryName = version.getLibrary().getName();
            FileObject libraryFob = FileUtil.createFolder(librariesFolder, libraryName);

            FileObject projectFob = project.getProjectDirectory();
            File projectDir = FileUtil.toFile(projectFob);
            String[] fileNames = version.getFiles();
            String[] localFiles = new String[fileNames.length];
            for (int i=0; i<fileNames.length; i++) {
                FileObject tmpFob = FileUtil.toFileObject(libraryFiles[i]);
                String fileName = fileNames[i];
                int index = fileName.lastIndexOf('.');
                if (index != -1) {
                    fileName = fileName.substring(0, index);
                }
                String[] path = fileName.split("/"); // NOI18N
                FileObject fileFolder = libraryFob;
                for (int j=0; j<path.length-1; j++) {
                    fileFolder = FileUtil.createFolder(fileFolder, path[j]);
                }
                FileObject fob = FileUtil.moveFile(tmpFob, fileFolder, path[path.length-1]);
                File file = FileUtil.toFile(fob);
                localFiles[i] = PropertyUtils.relativizeFile(projectDir, file);
            }
            version.setFileInfo(fileNames, localFiles);
        }

        private Map<String,Library.Version> toMap(Library.Version[] libraries) {
            Map<String,Library.Version> map = new HashMap<>();
            for (Library.Version library : libraries) {
                map.put(library.getLibrary().getName(), library);
            }
            return map;
        }

    }

}
