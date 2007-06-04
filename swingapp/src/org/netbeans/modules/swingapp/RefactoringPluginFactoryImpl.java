/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.swingapp;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
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
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Refactoring support for classes that use Swing App Framework. If such a class
 * is renamed, moved, or copied, the corresponding properties files with
 * resources are kept in sync (i.e. renamed, moved or copied as necessary).
 * 
 * @author Tomas Pavek
 */
public class RefactoringPluginFactoryImpl implements RefactoringPluginFactory {

    public RefactoringPluginFactoryImpl() {
    }

    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        if (!Boolean.getBoolean("form.refactoring")) { // NOI18N
            return null;
        }

        if (refactoring instanceof RenameRefactoring
                || refactoring instanceof MoveRefactoring
                || refactoring instanceof SingleCopyRefactoring) {
            FileObject file = refactoring.getRefactoringSource().lookup(FileObject.class);
            if (file != null && isJavaFile(file)) {
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
                // let's try to rename/move the resource map for the source file
                DataObject propertiesDO;
                if (AppFrameworkSupport.isFrameworkEnabledProject(srcFile)
                        && (propertiesDO = ResourceUtils.getPropertiesDataObject(srcFile)) != null) {
                    RefactoringElementImplementation previewElement = null;
                    if (refactoring instanceof RenameRefactoring) {
                        String displayText = "Rename file " + propertiesDO.getPrimaryFile().getNameExt();
                        previewElement = new PreviewElement(srcFile, displayText);
                    } else if (refactoring instanceof MoveRefactoring) {
                        URL targetURL = ((MoveRefactoring)refactoring).getTarget().lookup(URL.class);
                        try {
                            File f = FileUtil.normalizeFile(new File(targetURL.toURI()));
                            if (f.isDirectory()) {
                                String displayText = "Move file " + propertiesDO.getPrimaryFile().getNameExt(); // + " to "
                                previewElement = new PreviewElement(srcFile, displayText);
                            }
                        } catch (URISyntaxException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    if (previewElement != null) {
                        refactoringElements.add(refactoring, previewElement);

                        DesignResourceMap resMap = ResourceUtils.getDesignResourceMap(srcFile, false);
                        ResourceMapUpdate update = new ResourceMapUpdate(refactoring, previewElement, propertiesDO, resMap);
                        // we need to rename/move the resource map before form gets updated
                        RefactoringInfo refInfo = refactoring.getContext().lookup(RefactoringInfo.class);
                        if (refInfo != null && refInfo.isForm()) {
                            FormRefactoringUpdate formUpdate = refInfo.getUpdateForFile(srcFile, true);
                            formUpdate.addPrecedingFileChange(update);
                        } else { // the source file is not a form - but still do the update
//                            refactoringElements.registerTransaction(
                            refactoringElements.addFileChange(refactoring,
                                new ResourceMapUpdate(refactoring, previewElement, propertiesDO, resMap));
                        }
                    }
                }
            } else if (pkg != null && refactoring instanceof RenameRefactoring) { // [can't package be also moved via MoveRefactoring???]
                // renaming a package - does not rename folder, but moves to a different folder
                // we need to move the resources folder as well
                FileObject pkgFolder = pkg.getFolder();
                if (AppFrameworkSupport.isFrameworkEnabledProject(pkgFolder)) {
                    FileObject resFolder = pkgFolder.getFileObject("resources"); // NOI18N
                    if (resFolder != null) {
                        refactoringElements.addFileChange(refactoring,
                            new ResourcePackageUpdate((RenameRefactoring) refactoring,
                                                      new PreviewElement(resFolder, "Rename resources package"),
                                                      resFolder));
                    }
                }
            }

            return null;
        }
    }

    private static class PreviewElement extends SimpleRefactoringElementImplementation {
        private FileObject file;
        private String displayText;

        PreviewElement(FileObject file, String displayText) {
            this.file = file;
            this.displayText = displayText;
        }

        public String getText() {
            return "Resources update";
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

    private static class ResourceMapUpdate extends SimpleRefactoringElementImplementation {//implements Transaction {
        private AbstractRefactoring refactoring;
        private RefactoringElementImplementation refElement;
        private DataObject propertiesDO; // original properties file
        private DesignResourceMap resourceMap;

        ResourceMapUpdate(AbstractRefactoring refactoring, RefactoringElementImplementation previewElement, DataObject propertiesDO, DesignResourceMap resMap) {
            this.refactoring = refactoring;
            this.refElement = previewElement;
            this.propertiesDO = propertiesDO;
            this.resourceMap = resMap;
        }

//        public void commit() {
        // RefactoringElementImplementation
        public void performChange() {
            if (!refElement.isEnabled()) {
                return;
            }

            final FileObject srcFile = refactoring.getRefactoringSource().lookup(FileObject.class);
            if (refactoring instanceof RenameRefactoring) {
                // source file renaming within the same package
                String newName = ((RenameRefactoring)refactoring).getNewName();
                try {
                    propertiesDO.rename(newName);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return;
                }
                if (resourceMap != null) { // stop using the old resource map (keeps old bundle names)
                    if (srcFile.getName().equals(newName)) {
                        ResourceUtils.unregisterDesignResourceMap(resourceMap.getSourceFile());
                    } else { // remove the resource map after the source file is renamed
                             // (otherwise could be created again when the source file is saved
                             // still with the old name after regular java refactoring)
                        srcFile.addFileChangeListener(new FileChangeAdapter() {
                            public void fileRenamed(FileRenameEvent fe) {
                                ResourceUtils.unregisterDesignResourceMap(resourceMap.getSourceFile());
                                srcFile.removeFileChangeListener(this);
                            }
                        });
                    }
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
                        if (resourceMap != null) {
                            ResourceUtils.unregisterDesignResourceMap(resourceMap.getSourceFile());
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

//        public void rollback() {
//        }

        // RefactoringElementImplementation
        public String getText() {
            return "Resources update";
        }

        // RefactoringElementImplementation
        public String getDisplayText() {
            return "Post-refactoring: Resources update";
        }

        // RefactoringElementImplementation
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        // RefactoringElementImplementation
        public FileObject getParentFile() {
            return propertiesDO.getPrimaryFile();
        }

        // RefactoringElementImplementation
        public PositionBounds getPosition() {
            return null;
        }
    }

    private static class ResourcePackageUpdate extends SimpleRefactoringElementImplementation {
        private RenameRefactoring refactoring;
        private RefactoringElementImplementation refElement;
        private FileObject resFolder;

        ResourcePackageUpdate(RenameRefactoring refactoring, RefactoringElementImplementation refElement, FileObject resFolder) {
            this.refactoring = refactoring;
            this.refElement = refElement;
            this.resFolder = resFolder;
        }

        // RefactoringElementImplementation
        public void performChange() {
            if (!refElement.isEnabled()) {
                return;
            }

            // TBD move the resources folder under the renamed package
        }

        // RefactoringElementImplementation
        public String getText() {
            return "Resources update";
        }

        // RefactoringElementImplementation
        public String getDisplayText() {
            return "Post-refactoring: Resources update";
        }

        // RefactoringElementImplementation
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        // RefactoringElementImplementation
        public FileObject getParentFile() {
            return resFolder;
        }

        // RefactoringElementImplementation
        public PositionBounds getPosition() {
            return null;
        }
    }

    // -----

    private static boolean isJavaFile(FileObject fo) {
        return "text/x-java".equals(fo.getMIMEType()); // NOI18N
    }
}
