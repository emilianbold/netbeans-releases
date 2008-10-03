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
import org.netbeans.modules.form.FormRefactoringUpdate;
import org.netbeans.modules.form.RefactoringInfo;
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
            FileObject srcFile = sourceLookup.lookup(FileObject.class);
            NonRecursiveFolder pkg = sourceLookup.lookup(NonRecursiveFolder.class);
            if (srcFile != null && pkg == null && !srcFile.isFolder()) {
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

                        ResourceMapUpdate update = new ResourceMapUpdate(refactoring, previewElement, propertiesDO);

                        // we need to rename/move the resource map before form gets updated
                        RefactoringInfo refInfo = refactoring.getContext().lookup(RefactoringInfo.class);
                        if (refInfo != null) {
                            FileObject refFile = refInfo.getOriginalFiles()[0];
                            if (refFile.existsExt("form")) { // NOI18N
                                refInfo.getUpdateForFile(srcFile).addPrecedingFileChange(update);
                            } else { // the source file is not a form - but we still do the update
                                refactoringElements.addFileChange(refactoring, update);
                            }
                        } else { // the source file is not a form - but we still do the update
                            refactoringElements.addFileChange(refactoring, update);
                        }
                    }
                }
            } else if (pkg != null && refactoring instanceof RenameRefactoring) { // [can't package be also moved via MoveRefactoring???]
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

                    FormRefactoringUpdate formUpdate = null;
                    RefactoringInfo refInfo = refactoring.getContext().lookup(RefactoringInfo.class);
                    if (refInfo != null) {
                        for (FileObject fo : pkgFolder.getChildren()) {
                            if (isJavaFileOfForm(fo)) {
                                formUpdate = refInfo.getUpdateForFile(fo);
                                break;
                            }
                        }
                    }
                    if (formUpdate != null) {
                        formUpdate.addPrecedingFileChange(update);
                    } else {
                        refactoringElements.addFileChange(refactoring, update);
                    }
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
    private static class ResourceMapUpdate extends SimpleRefactoringElementImplementation {
        private AbstractRefactoring refactoring;
        private RefactoringElementImplementation refElement;

        private DataObject propertiesDO;
        private DataObject newPropertiesDO; // copied
        private String oldName; // original name of the source file
        private FileObject oldFolder; // original folder of the source file
        private FileObject srcFileBefore;
        private FileObject srcFileAfter;

        ResourceMapUpdate(AbstractRefactoring refactoring, RefactoringElementImplementation previewElement, DataObject propertiesDO) {
            this.refactoring = refactoring;
            this.refElement = previewElement;
            this.propertiesDO = propertiesDO;
            oldName = propertiesDO.getName();
            oldFolder = propertiesDO.getFolder().getPrimaryFile();
            srcFileBefore = refactoring.getRefactoringSource().lookup(FileObject.class);
        }

        public void performChange() {
            if (!refElement.isEnabled()) {
                return;
            }

            // This is supposed to run after the form is renamed/moved/copied
            // but before it does its own update. So the source file is in place,
            // but before processed as a form we need to update its resoures here.

            if (refactoring instanceof RenameRefactoring) {
                // source file renaming within the same package
                srcFileAfter = srcFileBefore; // FileObject survives renaming
                String newName = ((RenameRefactoring)refactoring).getNewName();
                try {
                    propertiesDO.rename(newName);
                    ResourceUtils.unregisterDesignResourceMap(srcFileBefore);
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
                    srcFileAfter = targetFolder.getFileObject(oldName);
                    try {
                        targetFolder = FileUtil.createFolder(targetFolder, "resources"); // NOI18N
                        propertiesDO.move(DataFolder.findFolder(targetFolder));
                        // TODO: Also analyze the resource map and copy relatively referenced
                        // images (stored under the same resources folder). Probably we should
                        // not just move the image files - that would require to analyze if
                        // not used from elsewhere. Or if staying in the same project we could
                        // maybe just change the relative names to complete cp names.
                        ResourceUtils.unregisterDesignResourceMap(srcFileBefore);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } else if (refactoring instanceof SingleCopyRefactoring) {
                // source file copied - create a copy of the resources
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
                    srcFileAfter = targetFolder.getFileObject(newName);
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

        @Override
        public void undoChange() {
            if (!refElement.isEnabled()) {
                return;
            }

            // This is supposed to run after the form rename/move/copy is undone
            // (files returned back) but before the form does its own update.
            // Here we need to restore the resoures to original state so the
            // form can be updated.

            if (refactoring instanceof RenameRefactoring) {
                // source file renamed within the same package - rename properties file back
                try {
                    propertiesDO.rename(oldName);
                    ResourceUtils.unregisterDesignResourceMap(srcFileAfter);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (refactoring instanceof MoveRefactoring) {
                // source file moved to another package - move properties file back
                try {
                    propertiesDO.move(DataFolder.findFolder(oldFolder));
                    ResourceUtils.unregisterDesignResourceMap(srcFileAfter);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                srcFileBefore = oldFolder.getParent().getFileObject(oldName);
            } else if (refactoring instanceof SingleCopyRefactoring) {
                // source file copied - delete the new properties file
                if (newPropertiesDO != null) {
                    try {
                        newPropertiesDO.delete();
                        newPropertiesDO = null;
                        ResourceUtils.unregisterDesignResourceMap(srcFileAfter);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
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
    private static class ResourcePackageUpdate extends SimpleRefactoringElementImplementation {
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

        public void performChange() {
            if (!refElement.isEnabled()) {
                return;
            }

            // This is supposed to run after the original package is renamed but
            // before the forms are updated - so when the forms are loaded they
            // have the resources in place.

            ClassPath cp = ClassPath.getClassPath(resFolder.getPrimaryFile(), ClassPath.SOURCE);
            DataFolder targetFolder = DataFolder.findFolder(
                    cp.findResource(refactoring.getNewName().replace('.', '/')));
            FileObject originalFolder = resFolder.getPrimaryFile().getParent();
            try {
                resFolder.move(targetFolder);
                // remove the original package folder if it empty after moving resources
                if (originalFolder != null && originalFolder.isValid()
                        && originalFolder.getChildren().length == 0) {
                    originalFolder.delete();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void undoChange() {
            if (!refElement.isEnabled()) {
                return;
            }

            // This is suposed to run after the package is renamed back but
            // before the forms perform their undo update - so when the forms
            // loaded they have the resources in place.

            ClassPath cp = ClassPath.getClassPath(resFolder.getPrimaryFile(), ClassPath.SOURCE);
            FileObject srcRoot = cp.findOwnerRoot(resFolder.getPrimaryFile());
            try {
                DataFolder targetFolder = DataFolder.findFolder(
                    FileUtil.createFolder(srcRoot, oldPkgName.replace('.', '/')));
                resFolder.move(targetFolder);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public String getText() {
            return "Resources update"; // NOI18N
        }

        public String getDisplayText() {
            return NbBundle.getMessage(RefactoringPluginFactoryImpl.class, "CTL_ResourcesPackageRef2"); // NOI18N
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        public FileObject getParentFile() {
            return resFolder.getPrimaryFile();
        }

        public PositionBounds getPosition() {
            return null;
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
