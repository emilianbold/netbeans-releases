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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.swingapp;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Refactoring support for classes that use Swing App Framework. If such a class
 * is renamed, moved, or copied, the corresponding properties files with
 * resources are kept in sync (i.e. renamed, moved or copied as necessary). Also
 * takes care of renamed packages (renames the resources package as well).
 * 
 * @author Tomas Pavek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class)
public class RefactoringPluginFactoryImpl implements RefactoringPluginFactory {

    public RefactoringPluginFactoryImpl() {
    }

    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        if (refactoring instanceof RenameRefactoring
                || refactoring instanceof MoveRefactoring
                || refactoring instanceof SingleCopyRefactoring) {
            Lookup sourceLookup = refactoring.getRefactoringSource();
            FileObject srcFile = sourceLookup.lookup(FileObject.class);
            NonRecursiveFolder pkg = sourceLookup.lookup(NonRecursiveFolder.class);
            if ((srcFile != null && isJavaFile(srcFile)) || pkg != null) {
                return new RefactoringPluginImpl(refactoring);
            }
        }
        return null;
    }

    // -----

    private static class RefactoringPluginImpl implements RefactoringPlugin {
        private AbstractRefactoring refactoring;

        RefactoringPluginImpl(AbstractRefactoring refactoring) {
            this.refactoring = refactoring;
        }

        public Problem preCheck() {
            return null;
        }

        public Problem checkParameters() {
            return null;
        }

        public Problem fastCheckParameters() {
            return null;
        }

        public void cancelRequest() {
        }

        public Problem prepare(RefactoringElementsBag refactoringElements) {
            Lookup sourceLookup = refactoring.getRefactoringSource();
            NonRecursiveFolder pkg = sourceLookup.lookup(NonRecursiveFolder.class);
            if (pkg == null) {
                for (FileObject srcFile : sourceLookup.lookupAll(FileObject.class)) {
                    if (!srcFile.isFolder()) {
                        // renaming, moving or copying a source file - update the resources accordingly
                        DataObject propertiesDO;
                        if (AppFrameworkSupport.isFrameworkLibAvailable(srcFile)
                                && (propertiesDO = ResourceUtils.getPropertiesDataObject(srcFile)) != null) {
                            // there is a valid properties file for a resource map
                            RefactoringElementImplementation previewElement = null;
                            if (refactoring instanceof RenameRefactoring) {
                                String displayText = NbBundle.getMessage(RefactoringPluginFactoryImpl.class,
                                        "FMT_RenameFileRef", propertiesDO.getPrimaryFile().getNameExt()); // NOI18N
                                previewElement = new PreviewElement(srcFile, displayText);
                            } else if (refactoring instanceof MoveRefactoring) {
                                URL targetURL = ((MoveRefactoring)refactoring).getTarget().lookup(URL.class);
                                try {
                                    File f = FileUtil.normalizeFile(new File(targetURL.toURI()));
                                    if (f.isDirectory()) {
                                        String displayText = NbBundle.getMessage(RefactoringPluginFactoryImpl.class,
                                                "FMT_MoveFileRef", propertiesDO.getPrimaryFile().getNameExt()); // NOI18N
                                        previewElement = new PreviewElement(srcFile, displayText);
                                    }
                                } catch (URISyntaxException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            } else if (refactoring instanceof SingleCopyRefactoring) {
                                URL targetURL = ((SingleCopyRefactoring)refactoring).getTarget().lookup(URL.class);
                                try {
                                    File f = FileUtil.normalizeFile(new File(targetURL.toURI()));
                                    if (f.isDirectory()) {
                                        String displayText = NbBundle.getMessage(RefactoringPluginFactoryImpl.class,
                                                "FMT_CopyFileRef", propertiesDO.getPrimaryFile().getNameExt()); // NOI18N
                                        previewElement = new PreviewElement(srcFile, displayText);
                                    }
                                } catch (URISyntaxException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }

                            if (previewElement != null) {
                                refactoringElements.add(refactoring, previewElement);
                                // we need to rename/move the resources before the form is renamed/moved
                                ResourceMapUpdate update = new ResourceMapUpdate(refactoring, previewElement, srcFile, propertiesDO);
                                refactoringElements.registerTransaction(update);
                                refactoringElements.addFileChange(refactoring, update);
                            }
                        }
                    }
                }
            } else if (refactoring instanceof RenameRefactoring) { // [can't package be also moved via MoveRefactoring???]
                // renaming a package - not renaming a folder, but non-recursive move to another folder
                // we need to move the resources folder as well
                FileObject pkgFolder = pkg.getFolder();
                FileObject resFolder;
                if (AppFrameworkSupport.isFrameworkLibAvailable(pkgFolder)
                        && (resFolder = pkgFolder.getFileObject("resources")) != null) { // NOI18N
                    // there is an app framework's resources folder
                    RefactoringElementImplementation previewElement = new PreviewElement(
                            resFolder, NbBundle.getMessage(RefactoringPluginFactoryImpl.class, "CTL_ResourcesPackageRef1")); // NOI18N
                    refactoringElements.add(refactoring, previewElement);

                    ResourcePackageUpdate update = new ResourcePackageUpdate(
                            (RenameRefactoring) refactoring, previewElement, resFolder);
                    refactoringElements.registerTransaction(update);
                }
            }

            return null;
        }
    }

    // -----

    private static class PreviewElement extends SimpleRefactoringElementImplementation {
        private FileObject file;
        private String displayText;

        PreviewElement(FileObject file, String displayText) {
            this.file = file;
            this.displayText = displayText;
        }

        public String getText() {
            return "Resources update"; // NOI18N
        }

        public String getDisplayText() {
            return displayText;
        }

        public void performChange() {
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        public FileObject getParentFile() {
            return file;
        }

        public PositionBounds getPosition() {
            return null;
        }
    }

    // -----

    /**
     * Updates properties files for a resource map (rename/move/copy) according
     * to a change of the java source file.
     */
    private static class ResourceMapUpdate extends SimpleRefactoringElementImplementation implements Transaction {
        private AbstractRefactoring refactoring;
        private RefactoringElementImplementation refElement;

        private DataObject propertiesDO;
        private DataObject newPropertiesDO; // copied
        private String oldName; // original name of the source file
        private FileObject oldFolder; // original folder of the properties file
        private FileObject srcFileBefore;
        private FileObject srcFileAfter;

        ResourceMapUpdate(AbstractRefactoring refactoring, RefactoringElementImplementation previewElement,
                          FileObject srcFileBefore, DataObject propertiesDO) {
            this.refactoring = refactoring;
            this.refElement = previewElement;
            this.propertiesDO = propertiesDO;
            oldName = propertiesDO.getName();
            oldFolder = propertiesDO.getFolder().getPrimaryFile();
            this.srcFileBefore = srcFileBefore;
        }

        public void commit() {
            if (!refElement.isEnabled()) {
                return;
            }

            // Need to ensure the resources are renamed/moved/copied at the right
            // moment so form updates always find them. As a "transaction", this
            // runs after form's transactions and before form's "file changes".
            // At this point, the source file is not renamed/moved yet.

            if (refactoring instanceof RenameRefactoring) {
                // source file renaming within the same package
                srcFileAfter = srcFileBefore; // FileObject survives renaming
                String newName = ((RenameRefactoring)refactoring).getNewName();
                try {
                    propertiesDO.rename(newName);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (refactoring instanceof MoveRefactoring) {
                // source file moving to another package, but with the same name
                URL targetURL = ((MoveRefactoring)refactoring).getTarget().lookup(URL.class);
                FileObject targetFolder = null;
                try {
                    File f = FileUtil.normalizeFile(new File(targetURL.toURI()));
                    targetFolder = FileUtil.toFileObject(f);
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (targetFolder != null && targetFolder.isFolder()) {
                    try {
                        targetFolder = FileUtil.createFolder(targetFolder, "resources"); // NOI18N
                        propertiesDO.move(DataFolder.findFolder(targetFolder));
                        newPropertiesDO = propertiesDO; // DataObject survives move
                        // TODO: Also analyze the resource map and copy relatively referenced
                        // images (stored under the same resources folder). Probably we should
                        // not just move the image files - that would require to analyze if
                        // not used from elsewhere. Or if staying in the same project we could
                        // maybe just change the relative names to complete cp names.
                        if (srcFileBefore != null) {
                            ResourceUtils.unregisterDesignResourceMap(srcFileBefore);
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } else if (refactoring instanceof SingleCopyRefactoring) {
                // source file to be copied - create a copy of the resources
                SingleCopyRefactoring copyRef = (SingleCopyRefactoring) refactoring;
                URL targetURL = copyRef.getTarget().lookup(URL.class);
                FileObject targetFolder = null;
                try {
                    File f = FileUtil.normalizeFile(new File(targetURL.toURI()));
                    targetFolder = FileUtil.toFileObject(f);
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (targetFolder != null && targetFolder.isFolder()) {
                    String newName = copyRef.getNewName();
                    try {
                        targetFolder = FileUtil.createFolder(targetFolder, "resources"); // NOI18N
                        if (targetFolder.getFileObject(newName, "properties") == null) { // NOI18N
                            newPropertiesDO = propertiesDO.copy(DataFolder.findFolder(targetFolder));
                            newPropertiesDO.rename(newName);
                        }
                        // TODO: Also analyze the resource map and copy relatively referenced
                        // images (stored under the same resources folder). Probably we should
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        public void rollback() {
            if (!refElement.isEnabled()) {
                return;
            }

            // This is supposed to run after the form rename/move/copy is undone
            // (files returned back). Here we need to restore the resoures to
            // the original state.

            if (refactoring instanceof RenameRefactoring) {
                // source file renamed within the same package - rename properties file back
                try {
                    propertiesDO.rename(oldName);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (refactoring instanceof MoveRefactoring) {
                // source file moved to another package - move properties file back
                try {
                    propertiesDO.move(DataFolder.findFolder(oldFolder));
                    newPropertiesDO = null;
                    if (srcFileAfter != null) {
                        ResourceUtils.unregisterDesignResourceMap(srcFileAfter);
                        srcFileAfter = null;
                    }
                    srcFileBefore = oldFolder.getParent().getFileObject(oldName, "java"); // NOI18N
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (refactoring instanceof SingleCopyRefactoring) {
                // source file copied - delete the new properties file
                if (newPropertiesDO != null) {
                    try {
                        newPropertiesDO.delete();
                        newPropertiesDO = null;
                        if (srcFileAfter != null) {
                            ResourceUtils.unregisterDesignResourceMap(srcFileAfter);
                            srcFileAfter = null;
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        public void performChange() {
        }

        @Override
        public void undoChange() {
            if (!refElement.isEnabled()) {
                return;
            }

            // just remember the new file
            if (refactoring instanceof MoveRefactoring || refactoring instanceof SingleCopyRefactoring) {
                if (newPropertiesDO != null) {
                    FileObject targetFolder = newPropertiesDO.getPrimaryFile().getParent().getParent();
                    srcFileAfter = targetFolder.getFileObject(newPropertiesDO.getName(), "java"); // NOI18N
                }
            }
        }

        public String getText() {
            return "Resources update"; // NOI18N
        }

        public String getDisplayText() {
            return NbBundle.getMessage(RefactoringPluginFactoryImpl.class,
                    "CTL_ResourceMapRef", propertiesDO.getPrimaryFile().getNameExt()); // NOI18N
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        public FileObject getParentFile() {
            return propertiesDO.getPrimaryFile();
        }

        public PositionBounds getPosition() {
            return null;
        }
    }

    // -----

    /**
     * Moves resources folder according to package rename (i.e. non-recursive
     * folder move).
     */
    private static class ResourcePackageUpdate implements Transaction {
        private RenameRefactoring refactoring;
        private RefactoringElementImplementation refElement;

        private String oldPkgName;
        private DataFolder resFolder; // DataFolder survives the move

        ResourcePackageUpdate(RenameRefactoring refactoring, RefactoringElementImplementation refElement, FileObject resFolder) {
            this.refactoring = refactoring;
            this.refElement = refElement;
            this.resFolder = DataFolder.findFolder(resFolder);
            oldPkgName = ClassPath.getClassPath(resFolder, ClassPath.SOURCE)
                    .getResourceName(resFolder.getParent(), '.', false);
        }

        public void commit() {
            if (!refElement.isEnabled()) {
                return;
            }

            // This is supposed to run before the original package is renamed.
            // The package is renamed after "transactions", then form updates
            // are run.

            ClassPath cp = ClassPath.getClassPath(resFolder.getPrimaryFile(), ClassPath.SOURCE);
            FileObject srcRoot = cp.findOwnerRoot(resFolder.getPrimaryFile());
            String newName = refactoring.getNewName().replace('.', '/');
            try {
                // the new package folder is not there yet
                DataFolder targetFolder = DataFolder.findFolder(
                        FileUtil.createFolder(srcRoot, newName));
                resFolder.move(targetFolder);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public void rollback() {
            if (!refElement.isEnabled()) {
                return;
            }

            // This is suposed to run after the package is renamed back.

            ClassPath cp = ClassPath.getClassPath(resFolder.getPrimaryFile(), ClassPath.SOURCE);
            FileObject srcRoot = cp.findOwnerRoot(resFolder.getPrimaryFile());
            FileObject pkgFolder = resFolder.getPrimaryFile().getParent();
            try {
                DataFolder targetFolder = DataFolder.findFolder(
                    FileUtil.createFolder(srcRoot, oldPkgName.replace('.', '/')));
                resFolder.move(targetFolder);
                // remove the package folder if it is empty after moving resources
                if (pkgFolder != null && pkgFolder.isValid()
                        && pkgFolder.getChildren().length == 0) {
                    pkgFolder.delete();
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    // -----

    private static boolean isJavaFile(FileObject fo) {
        return "text/x-java".equals(fo.getMIMEType()); // NOI18N
    }

    static boolean isJavaFileOfForm(FileObject fo) {
        return isJavaFile(fo) && fo.existsExt("form"); // NOI18N
    }
}
