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

package org.netbeans.modules.form;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;

/**
 * Holds information about one refactoring. Knows the type of refactoring change,
 * the originating source file and its corresponding class name. For each
 * affected source file (form) keeps the transaction that loads and updates
 * the form. When refactoring starts, an instance of RefactoringInfo is attached
 * to the refactoring's context - so it can be accessed from different places
 * (refactoring plugin, guarded block handler).
 * 
 * @author Tomas Pavek
 */
public class RefactoringInfo {

    public enum ChangeType {
        VARIABLE_RENAME,  // field or local variable in initComponents
        CLASS_RENAME, CLASS_MOVE,  // can be a form, or a component class, or both
        CLASS_COPY,  // a form class
        PACKAGE_RENAME, FOLDER_RENAME,  // non-recursive folder and folder with subfolders
        EVENT_HANDLER_RENAME,  // method in a form class
        OTHER_FORM_CHANGE
    }

    private AbstractRefactoring refactoring;
    private ChangeType changeType;
    private FileObject primaryFile; // the source file where the refactoring change originated
//    private String oldClassName;
    private String oldName;
//    private String newName;
    private Map<FileObject,FormRefactoringUpdate> fileToUpdateMap = new HashMap<FileObject,FormRefactoringUpdate>();

    RefactoringInfo(AbstractRefactoring refactoring, ChangeType changeType, FileObject primaryFile, String oldName) {
        assert isJavaFile(primaryFile);
        this.refactoring = refactoring;
        this.changeType = changeType;
        this.primaryFile = primaryFile;
        this.oldName = oldName;
//        if (changeType == ChangeType.CLASS_RENAME) {
//            ClassPath cp = ClassPath.getClassPath(primaryFile, ClassPath.SOURCE);
//            oldClassName = cp.getResourceName(primaryFile, '.', false);
//            oldName = primaryFile.getName();
//        }
    }

    AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public FileObject getPrimaryFile() {
        return primaryFile;
    }

    public boolean isForm() {
        return primaryFile != null && isJavaFileOfForm(primaryFile);
    }

    void setOldName(String oldName) {
        this.oldName = oldName;
    }

//    void setNewName(String newName) {
//        this.newName = newName;
//    }

    String getOldName() {
        return oldName;
    }

    String getNewName() {
        return refactoring instanceof RenameRefactoring ? ((RenameRefactoring)refactoring).getNewName() : null;
//        return newName;
    }

//    public String getOriginalClassName() {
//        return originalClassName;
//    }

    public FormRefactoringUpdate getUpdateForFile(FileObject fo, boolean create) {
        FormRefactoringUpdate update = fileToUpdateMap.get(fo);
        if (update == null && create) {
            assert isJavaFileOfForm(fo);
            update = new FormRefactoringUpdate(this, fo);
            fileToUpdateMap.put(fo, update);
        }
        return update;
    }

    // -----

    static boolean isJavaFile(FileObject fo) {
        return "text/x-java".equals(fo.getMIMEType()); // NOI18N
    }

    static boolean isJavaFileOfForm(FileObject fo) {
        return isJavaFile(fo) && fo.existsExt("form"); // NOI18N
    }
}
